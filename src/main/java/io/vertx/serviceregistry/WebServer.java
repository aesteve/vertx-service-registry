package io.vertx.serviceregistry;

import io.vertx.apiutils.ETagPostProcessor;
import io.vertx.apiutils.ETagPreProcessor;
import io.vertx.apiutils.JsonFinalizer;
import io.vertx.apiutils.JsonPreProcessor;
import io.vertx.apiutils.PaginationPostProcessor;
import io.vertx.apiutils.PaginationPreProcessor;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.handler.StaticHandler;
import io.vertx.serviceregistry.dao.ArtifactsDAO;
import io.vertx.serviceregistry.dao.impl.JsonArtifactsDAO;
import io.vertx.serviceregistry.engines.JSLibrary;
import io.vertx.serviceregistry.engines.ReactTemplateEngine;
import io.vertx.serviceregistry.handlers.ServicesContextHandler;
import io.vertx.serviceregistry.handlers.api.ServicesApiHandler;
import io.vertx.serviceregistry.handlers.errors.ApiErrorHandler;
import io.vertx.serviceregistry.handlers.errors.DevErrorHandler;
import io.vertx.serviceregistry.http.etag.ETagCachingService;
import io.vertx.serviceregistry.http.etag.impl.InMemoryETagCachingService;

import java.util.ArrayList;
import java.util.Collection;

public class WebServer implements Verticle {

	private final static String DEFAULT_ADDRESS = "localhost";
	private final static Integer DEFAULT_PORT = 8080;
	private final static String DEFAULT_DATA_DIR = ".";
	private final static String DEFAULT_ARTIFACTS_FILE = "export.json";

	private JsonObject config;

	private HttpServer server;
	private String dataDir;
	private String artifactsFile;
	private HttpServerOptions options;
	private ReactTemplateEngine engine;
	private ServicesContextHandler servicesContextHandler; // TODO : rename as something related to "injects queryParams into context"
	private ServicesApiHandler servicesApiHandler;
	private ArtifactsDAO artifactsDAO;
	private Vertx vertx;
	private ETagCachingService eTagCachingService;

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
		apiRouter.getWithRegex("/services(/|$)").handler(servicesApiHandler);
		apiRouter.get("/services/:serviceId").handler(servicesApiHandler);
		// post-processing
		apiRouter.route().handler(new PaginationPostProcessor());
		apiRouter.route().handler(new ETagPostProcessor(eTagCachingService));
		apiRouter.route().handler(new JsonFinalizer());
		apiRouter.route().failureHandler(new ApiErrorHandler());
		router.mountSubRouter("/api/1", apiRouter);

		router.route("/").handler(servicesContextHandler);
		// router.get("/").handler(TemplateHandler.create(engine, "", "text/html"));
		router.get("/").handler(request -> request.response().sendFile("sites/index.html"));
		router.get("/").failureHandler(new DevErrorHandler("error.html"));

		server = vertx.createHttpServer(options);
		server.requestHandler(router::accept);
		server.listen();
		future.complete();
		System.out.println("Vertx-Service-Registry listening on port " + options.getPort() + " , address " + options.getHost());
	}

	@Override
	public void stop(Future<Void> future) throws Exception {
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
		}
		else {
			final String envPort = System.getenv("OPENSHIFT_DIY_PORT");
			if (envPort != null)
				options.setPort(Integer.parseInt(envPort));
			else
				options.setPort(DEFAULT_PORT);
		}

		if (config.getString("host") != null) {
			options.setHost(config.getString("host"));
		}
		else {
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
	}

	@Override
	public Vertx getVertx() {
		return vertx;
	}

	@Override
	public void init(Vertx vertx, Context context) {
		this.vertx = vertx;
		this.config = context.config();

		checkConfig();
		// Load artifacts from FS
		Buffer b = vertx.fileSystem().readFileBlocking(dataDir + "/" + artifactsFile);
		artifactsDAO = new JsonArtifactsDAO(b.toString("UTF-8"));
		artifactsDAO.load();

		// Pages-related (context, paths)
		servicesContextHandler = new ServicesContextHandler(artifactsDAO);

		// Javascript + server-side rendering
		Collection<JSLibrary> customLibs = new ArrayList<JSLibrary>();
		customLibs.add(new JSLibrary("webroots/scripts/libs/underscore-1.7.0.min.js", "/assets/scripts/libs/underscore-1.7.0.min.js"));
		engine = new ReactTemplateEngine("webapp-tpl.html", "C:/Dev/Tests/react/", customLibs);

		// Api
		eTagCachingService = new InMemoryETagCachingService();
		servicesApiHandler = new ServicesApiHandler(artifactsDAO);
	}
}
