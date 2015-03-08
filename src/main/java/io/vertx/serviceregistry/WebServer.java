package io.vertx.serviceregistry;

import io.vertx.apiutils.ETagPostProcessor;
import io.vertx.apiutils.ETagPreProcessor;
import io.vertx.apiutils.JsonFinalizer;
import io.vertx.apiutils.JsonPreProcessor;
import io.vertx.apiutils.PaginationPostProcessor;
import io.vertx.apiutils.PaginationPreProcessor;
import io.vertx.componentdiscovery.DiscoveryService;
import io.vertx.componentdiscovery.model.Artifact;
import io.vertx.componentdiscovery.model.TaskReport;
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
import java.util.List;

/**
 * Main Verticle handling web stuff
 * 
 * Intantiate/Orchestrates everything, responds to clients
 * 
 * @author aesteve
 */
public class WebServer implements Verticle {

	private Vertx vertx;
	private HttpServer server;

	private DAO<Artifact> artifactsDAO;
	private JsonReportDAO reportsDAO;

	private ServicesContextHandler servicesContextHandler;
	private ServicesApiHandler servicesApiHandler;
	private ReportApiHandler reportApiHandler;
	private CloudTagApiHandler cloudTagApiHandler;
	private ETagCachingService eTagCachingService;

	private DiscoveryService discoveryService;
	private StaticTemplatesRegistry tplRegistry;

	@Override
	public void start(Future<Void> future) throws Exception {
		Router router = Router.router(vertx);

		StaticHandler staticHandler = StaticHandler.create();
		staticHandler.setWebRoot(Config.get().getSiteDir());
		staticHandler.setDirectoryListing(false);
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
			routingContext.response().sendFile(Config.get().getSiteDir() + "/report.html");
		});
		router.get("/cloud").handler(routingContext -> {
			routingContext.response().sendFile(Config.get().getSiteDir() + "/cloud.html");
		});

		router.route("/sockets/*").handler(SockJSFactory.createSocketHandler(vertx));
		// router.route().failureHandler(new DevErrorHandler("error.html"));

		server = vertx.createHttpServer(Config.get().getServerOptions());
		server.requestHandler(router::accept);

		discoveryService.start(handler -> {
			discoveryService.crawl(crawlHandler -> {
				TaskReport report = crawlHandler.result();
				reportsDAO.add(report);
				report.subTasks().forEach(subTask -> {
					System.out.println(subTask.name());
					if (!subTask.hasFailed()) {
						List<Artifact> result = ((List<Artifact>) subTask.result());
						artifactsDAO.replace(result);
					}
				});
				vertx.fileSystem().writeFileBlocking(Config.get().getArtifactsFile(), Buffer.buffer(ApiObjectMarshaller.marshallArtifacts(artifactsDAO.getAll()).toString()));
				vertx.fileSystem().writeFileBlocking(Config.get().getReportsFile(), Buffer.buffer(ApiObjectMarshaller.marshallReports(reportsDAO.getAll()).toString()));
				vertx.eventBus().send(EbAddresses.PAGE_GENERATOR.toString(), "generate");
			});

			PageGenerator pageWorker = new PageGenerator(artifactsDAO, reportsDAO, tplRegistry);
			vertx.deployVerticle(pageWorker, new DeploymentOptions().setWorker(true), workerHandler -> {
				server.listen();
				future.complete();
				HttpServerOptions options = Config.get().getServerOptions();
				System.out.println("Vertx-Service-Registry listening on port " + options.getPort() + " , address " + options.getHost());
			});

		});
	}

	@Override
	public void stop(Future<Void> future) throws Exception {
		future.complete();
		System.out.println("Vertx-Service-Registry stopped");
	}

	@Override
	public Vertx getVertx() {
		return vertx;
	}

	@Override
	public void init(Vertx vertx, Context context) {
		this.vertx = vertx;
		JsonArray crawlers = new JsonArray();
		crawlers.add(new JsonObject().put("name", "Maven Central"));

		Config conf = Config.create(vertx, new JsonObject().put("crawlers", crawlers));

		this.discoveryService = DiscoveryService.create(vertx, conf.getCrawlers());

		// Load artifacts from FS
		File arts = new File(conf.getArtifactsFile());
		if (arts.exists() && arts.isFile()) {
			Buffer b = vertx.fileSystem().readFileBlocking(conf.getArtifactsFile());
			artifactsDAO = new JsonArtifactsDAO(b.toString("UTF-8"));
		} else {
			artifactsDAO = new JsonArtifactsDAO("[]");
		}
		artifactsDAO.load();

		// Load reports from FS
		// Load artifacts from FS
		File reports = new File(conf.getReportsFile());
		if (reports.exists() && reports.isFile()) {
			Buffer b2 = vertx.fileSystem().readFileBlocking(conf.getReportsFile());
			reportsDAO = new JsonReportDAO(b2.toString("UTF-8"));
		} else {
			reportsDAO = new JsonReportDAO("[]");
		}
		reportsDAO.load();

		// Pages-related (context, paths)
		servicesContextHandler = new ServicesContextHandler(artifactsDAO);
		tplRegistry = new StaticTemplatesRegistry(vertx, conf.getTplDir());

		// Api
		eTagCachingService = new InMemoryETagCachingService();
		servicesApiHandler = new ServicesApiHandler(artifactsDAO);
		reportApiHandler = new ReportApiHandler(reportsDAO);
		cloudTagApiHandler = new CloudTagApiHandler(artifactsDAO);
	}
}
