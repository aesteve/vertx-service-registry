package io.vertx.serviceregistry;

import io.vertx.apiutils.ETagPostProcessor;
import io.vertx.apiutils.ETagPreProcessor;
import io.vertx.apiutils.JsonFinalizer;
import io.vertx.apiutils.JsonPreProcessor;
import io.vertx.apiutils.PaginationPostProcessor;
import io.vertx.apiutils.PaginationPreProcessor;
import io.vertx.componentdiscovery.DiscoveryService;
import io.vertx.componentdiscovery.model.Artifact;
import io.vertx.componentdiscovery.utils.EbAddresses;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
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
import io.vertx.serviceregistry.dao.DAO;
import io.vertx.serviceregistry.dao.impl.JsonArtifactsDAO;
import io.vertx.serviceregistry.dao.impl.JsonReportDAO;
import io.vertx.serviceregistry.fileutils.StaticTemplatesRegistry;
import io.vertx.serviceregistry.handlers.ServicesContextHandler;
import io.vertx.serviceregistry.handlers.SockJSFactory;
import io.vertx.serviceregistry.handlers.StaticHtmlHandler;
import io.vertx.serviceregistry.handlers.api.CloudTagApiHandler;
import io.vertx.serviceregistry.handlers.api.ReportApiHandler;
import io.vertx.serviceregistry.handlers.api.ServicesApiHandler;
import io.vertx.serviceregistry.handlers.errors.ApiErrorHandler;
import io.vertx.serviceregistry.http.etag.ETagCachingService;
import io.vertx.serviceregistry.http.etag.impl.InMemoryETagCachingService;
import io.vertx.serviceregistry.io.ApiObjectMarshaller;

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
	private String tplDir;
	private String artifactsFile;
	private String reportsFile;
	private HttpServerOptions options;
	private ServicesContextHandler servicesContextHandler; // TODO : rename as something related to "injects queryParams into context"
	private ServicesApiHandler servicesApiHandler;
	private ReportApiHandler reportApiHandler;
	private CloudTagApiHandler cloudTagApiHandler;
	private DAO<Artifact> artifactsDAO;
	private JsonReportDAO reportsDAO;
	private Vertx vertx;
	private ETagCachingService eTagCachingService;

	private DiscoveryService discoveryService;
	private StaticTemplatesRegistry tplRegistry;

	@Override
	public void start(Future<Void> future) throws Exception {
		Router router = Router.router(vertx);

		StaticHandler staticHandler = StaticHandler.create();
		staticHandler.setWebRoot("site");
		staticHandler.setDirectoryListing(true);
		router.route("/assets/*").handler(staticHandler);

		Router apiRouter = Router.router(vertx);
		// pre-processing
		apiRouter.route().handler(new JsonPreProcessor());
		apiRouter.route().handler(new PaginationPreProcessor());
		apiRouter.route().handler(new ETagPreProcessor(eTagCachingService));
		// data-processing
		apiRouter.get("/cloud").handler(cloudTagApiHandler);
		apiRouter.get("/report").handler(reportApiHandler);
		apiRouter.get("/services").handler(servicesApiHandler);
		apiRouter.get("/services/:serviceId").handler(servicesApiHandler);
		// post-processing
		apiRouter.route().handler(new PaginationPostProcessor());
		apiRouter.route().handler(new ETagPostProcessor(eTagCachingService));
		apiRouter.route().handler(new JsonFinalizer());
		apiRouter.route().failureHandler(new ApiErrorHandler());
		router.mountSubRouter("/api/2", apiRouter);

		router.get("/services").handler(servicesContextHandler);
		router.get("/services").handler(new StaticHtmlHandler(tplRegistry));
		router.get("/report").handler(routingContext -> {
			routingContext.response().sendFile("site/report.html");
		});
		router.get("/cloud").handler(routingContext -> {
			routingContext.response().sendFile("site/cloud.html");
		});

		router.route("/sockets/*").handler(SockJSFactory.createSocketHandler(vertx));
		// router.route().failureHandler(new DevErrorHandler("error.html"));

		server = vertx.createHttpServer(options);
		server.requestHandler(router::accept);

		discoveryService.start(handler -> {
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
				PageGenerator pageWorker = new PageGenerator(artifactsDAO, reportsDAO, tplRegistry);
				vertx.deployVerticle(pageWorker, new DeploymentOptions().setWorker(true), workerHandler -> {
					server.listen();
					future.complete();
					System.out.println("Vertx-Service-Registry listening on port " + options.getPort() + " , address " + options.getHost());
					vertx.eventBus().publish(EbAddresses.PAGE_GENERATOR.toString(), "generate");
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
		tplDir = dataDir + "/tpl";
		if (!vertx.fileSystem().existsBlocking(tplDir)) {
			vertx.fileSystem().mkdirBlocking(tplDir);
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
		tplRegistry = new StaticTemplatesRegistry(vertx, tplDir);

		// Api
		eTagCachingService = new InMemoryETagCachingService();
		servicesApiHandler = new ServicesApiHandler(artifactsDAO);
		reportApiHandler = new ReportApiHandler(reportsDAO);
		cloudTagApiHandler = new CloudTagApiHandler(artifactsDAO);
	}
}
