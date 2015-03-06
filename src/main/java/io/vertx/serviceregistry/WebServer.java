package io.vertx.serviceregistry;

import io.vertx.apiutils.ETagPostProcessor;
import io.vertx.apiutils.ETagPreProcessor;
import io.vertx.apiutils.JsonFinalizer;
import io.vertx.apiutils.JsonPreProcessor;
import io.vertx.apiutils.PaginationPostProcessor;
import io.vertx.apiutils.PaginationPreProcessor;
import io.vertx.componentdiscovery.DiscoveryService;
import io.vertx.componentdiscovery.model.Artifact;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.handler.StaticHandler;
import io.vertx.ext.apex.handler.TemplateHandler;
import io.vertx.serviceregistry.dao.DAO;
import io.vertx.serviceregistry.dao.impl.JsonArtifactsDAO;
import io.vertx.serviceregistry.dao.impl.JsonReportDAO;
import io.vertx.serviceregistry.engines.ReactTemplateEngine;
import io.vertx.serviceregistry.handlers.ServicesContextHandler;
import io.vertx.serviceregistry.handlers.SockJSFactory;
import io.vertx.serviceregistry.handlers.api.ReportApiHandler;
import io.vertx.serviceregistry.handlers.api.ServicesApiHandler;
import io.vertx.serviceregistry.handlers.errors.ApiErrorHandler;
import io.vertx.serviceregistry.http.etag.ETagCachingService;
import io.vertx.serviceregistry.http.etag.impl.InMemoryETagCachingService;
import io.vertx.serviceregistry.io.ApiObjectMarshaller;
import io.vertx.serviceregistry.io.ArtifactsPageGenerator;

import java.io.File;

public class WebServer implements Verticle {

    private final static String DEFAULT_ADDRESS = "localhost";
    private final static Integer DEFAULT_PORT = 8080;
    private final static String DEFAULT_DATA_DIR = ".";
    private final static String DEFAULT_ARTIFACTS_FILE = "export.json";
    private final static String DEFAULT_REPORTS_FILE = "reports.json";

    private JsonObject config;

    private HttpServer server;
    private String dataDir;
    private String artifactsFile;
    private String reportsFile;
    private HttpServerOptions options;
    private ReactTemplateEngine engine;
    private ServicesContextHandler servicesContextHandler; // TODO : rename as something related to "injects queryParams into context"
    private ServicesApiHandler servicesApiHandler;
    private ReportApiHandler reportApiHandler;
    private DAO<Artifact> artifactsDAO;
    private JsonReportDAO reportsDAO;
    private Vertx vertx;
    private ETagCachingService eTagCachingService;
    private ArtifactsPageGenerator pageGenerator;

    private DiscoveryService discoveryService;

    @Override
    public void start(Future<Void> future) throws Exception {
        Router router = Router.router(vertx);

        StaticHandler staticHandler = StaticHandler.create();
        staticHandler.setWebRoot("site");
        staticHandler.setDirectoryListing(false);
        router.route("/assets").handler(staticHandler);

        Router apiRouter = Router.router(vertx);
        // pre-processing
        apiRouter.route().handler(new JsonPreProcessor());
        apiRouter.route().handler(new PaginationPreProcessor());
        apiRouter.route().handler(new ETagPreProcessor(eTagCachingService));
        // data-processing
        apiRouter.get("/report").handler(reportApiHandler);
        apiRouter.get("/services").handler(servicesApiHandler);
        apiRouter.get("/services/:serviceId").handler(servicesApiHandler);
        // post-processing
        apiRouter.route().handler(new PaginationPostProcessor());
        apiRouter.route().handler(new ETagPostProcessor(eTagCachingService));
        apiRouter.route().handler(new JsonFinalizer());
        apiRouter.route().failureHandler(new ApiErrorHandler());
        router.mountSubRouter("/api/2", apiRouter);

        router.get("/sites").handler(servicesContextHandler);
        router.get("/sites").handler(TemplateHandler.create(engine, "", "text/html"));
        router.get("/report").handler(routingContext -> {
            routingContext.response().sendFile("sites/report.html");
        });

        router.route("/sockets").handler(SockJSFactory.createSocketHandler(vertx));
        // router.route().failureHandler(new DevErrorHandler("error.html"));

        server = vertx.createHttpServer(options);
        server.requestHandler(router::accept);

        discoveryService.start(handler -> {
            server.listen();
            future.complete();
            /*
             * discoveryService.crawl(crawlHandler -> {
             * TaskReport report = crawlHandler.result();
             * reportsDAO.add(report);
             * report.subTasks().forEach(subTask -> {
             * if (!subTask.hasFailed()) {
             * List<Artifact> result = new ArrayList<Artifact>();
             * artifactsDAO.replace((List<Artifact>) result);
             * }
             * });
             * vertx.fileSystem().writeFileBlocking(DEFAULT_ARTIFACTS_FILE, Buffer.buffer(ApiObjectMarshaller.marshallArtifacts(artifactsDAO.getAll()).toString()));
             * vertx.fileSystem().writeFileBlocking(DEFAULT_REPORTS_FILE, Buffer.buffer(ApiObjectMarshaller.marshallReports(reportsDAO.getAll()).toString()));
             * });
             */

            System.out.println("Vertx-Service-Registry listening on port " + options.getPort() + " , address " + options.getHost());
        });
        vertx.setPeriodic(30000, timerId -> {
            pageGenerator.generateForCurrentServices(pageGeneratorHandler -> {
                reportsDAO.add(pageGeneratorHandler);
                System.out.println("Page generation finished");
            });
        });

    }

    @Override
    public void stop(Future<Void> future) throws Exception {
        vertx.fileSystem().writeFileBlocking(DEFAULT_ARTIFACTS_FILE, Buffer.buffer(ApiObjectMarshaller.marshallArtifacts(artifactsDAO.getAll()).toString()));
        vertx.fileSystem().writeFileBlocking(DEFAULT_REPORTS_FILE, Buffer.buffer(ApiObjectMarshaller.marshallReports(reportsDAO.getAll()).toString()));
        future.complete();
        System.out.println("Vertx-Service-Registry stopped");
    }

    private void checkConfig() {
        // TODO : describe it as a general behaviour :
        // first config
        // then openshift
        // then default

        options = new HttpServerOptions();

        if (config.getInteger("port") != null) {
            options.setPort(config.getInteger("port"));
        } else {
            final String envPort = System.getenv("OPENSHIFT_DIY_PORT");
            if (envPort != null)
                options.setPort(Integer.parseInt(envPort));
            else
                options.setPort(DEFAULT_PORT);
        }

        if (config.getString("host") != null) {
            options.setHost(config.getString("host"));
        } else {
            String address = System.getenv("OPENSHIFT_DIY_IP");
            if (address != null)
                options.setHost(address);
            else
                options.setHost(DEFAULT_ADDRESS);
        }

        if (config.getString("data-dir") != null) {
            dataDir = config.getString("data-dir");
        } else {
            dataDir = System.getenv("OPENSHIFT_DATA_DIR");
            if (dataDir == null)
                dataDir = DEFAULT_DATA_DIR;
        }

        if (config.getString("artifacts-file") != null) {
            artifactsFile = config.getString("artifacts-file");
        } else {
            artifactsFile = DEFAULT_ARTIFACTS_FILE;
        }

        reportsFile = DEFAULT_REPORTS_FILE;
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
        this.config = context.config();

        // FIXME TODO : why can't I read the conf.json file ????
        config.put("crawlers", new JsonArray().add(new JsonObject().put("name", "Maven Central")));

        this.discoveryService = DiscoveryService.create(vertx, config);

        checkConfig();
        // Load artifacts from FS
        File arts = new File(dataDir + "/" + artifactsFile);
        if (arts.exists() && arts.isFile()) {
            Buffer b = vertx.fileSystem().readFileBlocking(dataDir + "/" + artifactsFile);
            artifactsDAO = new JsonArtifactsDAO(b.toString("UTF-8"));
        } else {
            artifactsDAO = new JsonArtifactsDAO("[]");
        }
        artifactsDAO.load();

        // Load reports from FS
        // Load artifacts from FS
        File reports = new File(dataDir + "/" + reportsFile);
        if (reports.exists() && reports.isFile()) {
            Buffer b2 = vertx.fileSystem().readFileBlocking(dataDir + "/" + reportsFile);
            reportsDAO = new JsonReportDAO(b2.toString("UTF-8"));
        } else {
            reportsDAO = new JsonReportDAO("[]");
        }
        reportsDAO.load();

        // Pages-related (context, paths)
        servicesContextHandler = new ServicesContextHandler(artifactsDAO);
        pageGenerator = new ArtifactsPageGenerator(vertx, artifactsDAO);

        engine = new ReactTemplateEngine("server-index.html");

        // Api
        eTagCachingService = new InMemoryETagCachingService();
        servicesApiHandler = new ServicesApiHandler(artifactsDAO);
        reportApiHandler = new ReportApiHandler(reportsDAO);
    }
}
