package io.vertx.serviceregistry;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.handler.StaticHandler;
import io.vertx.ext.apex.handler.TemplateHandler;
import io.vertx.serviceregistry.api.ApiResources;
import io.vertx.serviceregistry.engines.JSLibrary;
import io.vertx.serviceregistry.engines.ReactTemplateEngine;
import io.vertx.serviceregistry.factory.ArtifactsFactory;
import io.vertx.serviceregistry.handlers.ServicesContextHandler;
import io.vertx.serviceregistry.handlers.errors.DevErrorHandler;

import java.util.ArrayList;
import java.util.Collection;

public class WebServer implements Verticle {

    private final static String DEFAULT_ADDRESS = "localhost";
    private final static Integer DEFAULT_PORT = 8080;
    private final static String DEFAULT_DATA_DIR = "./";

    private HttpServer server;
    private String dataDir;
    private HttpServerOptions options;
    private ReactTemplateEngine engine;
    private ServicesContextHandler servicesContextHandler;
    private Vertx vertx;

    @Override
    public void start(Future<Void> future) throws Exception {
        checkConfig();

        Collection<JSLibrary> customLibs = new ArrayList<JSLibrary>();
        customLibs.add(new JSLibrary("webroots/scripts/libs/underscore-1.7.0.min.js", "/assets/scripts/libs/underscore-1.7.0.min.js"));
        engine = new ReactTemplateEngine("webapp-tpl.html", "C:/Dev/Tests/react/", customLibs);
        servicesContextHandler = new ServicesContextHandler();

        Buffer b = vertx.fileSystem().readFileBlocking(dataDir + "export.json");
        ArtifactsFactory.load(b.toString("UTF-8"));

        Router router = Router.router(vertx);

        StaticHandler staticHandler = StaticHandler.create();
        staticHandler.setWebRoot("site");

        staticHandler.setDirectoryListing(false);

        router.route("/assets").handler(staticHandler);

        router.routeWithRegex("/api/1/*").handler(routingContext -> {
            ApiResources.serveResource(routingContext.request());
        });

        router.route("/").handler(servicesContextHandler);

        router.get("/").handler(TemplateHandler.create(engine, "", "text/html"));
        // router.get("/").handler(requestHandler -> {
        // requestHandler.response().sendFile("webroots/index.html");
        // });

        router.route().failureHandler(new DevErrorHandler("error.html"));

        server = vertx.createHttpServer(options);
        server.requestHandler(router::accept);
        server.listen();
        System.out.println("Vertx-Service-Registry listening on port " + options.getPort() + " , address " + options.getHost());
    }

    @Override
    public void stop(Future<Void> future) throws Exception {
    }

    private void checkConfig() {
        options = new HttpServerOptions();

        final String envPort = System.getenv("OPENSHIFT_DIY_PORT");
        if (envPort != null)
            options.setPort(Integer.parseInt(envPort));
        else
            options.setPort(DEFAULT_PORT);

        String address = System.getenv("OPENSHIFT_DIY_IP");
        if (address != null)
            options.setHost(address);
        else
            options.setHost(DEFAULT_ADDRESS);

        dataDir = System.getenv("OPENSHIFT_DATA_DIR");
        if (dataDir == null)
            dataDir = DEFAULT_DATA_DIR;
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
    }
}
