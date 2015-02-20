package io.vertx.serviceregistry

import io.vertx.core.http.HttpServerOptions
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.core.buffer.Buffer
import io.vertx.groovy.core.http.HttpServer
import io.vertx.groovy.core.http.HttpServerRequest
import io.vertx.groovy.ext.apex.Router
import io.vertx.groovy.ext.apex.RoutingContext
import io.vertx.groovy.ext.apex.handler.StaticHandler
import io.vertx.lang.groovy.GroovyVerticle
import io.vertx.serviceregistry.api.ApiResources
import io.vertx.serviceregistry.factory.ArtifactsFactory

class WebServer extends GroovyVerticle {

	private final static String DEFAULT_ADDRESS = "localhost"
	private final static Integer DEFAULT_PORT = 8080
	private final static String DEFAULT_DATA_DIR = "./"

	HttpServer server
	ApiResources apiResources
	Integer port
	String address
	String dataDir
	HttpServerOptions options

	@Override
	public void start() throws Exception {
		checkConfig()
		apiResources = new ApiResources()
		Buffer b = vertx.fileSystem().readFileBlocking("${dataDir}export.json")
		ArtifactsFactory.load(b.toString("UTF-8"))


		Router router = Router.router(vertx)

		StaticHandler staticHandler = StaticHandler.create()

		staticHandler.setDirectoryListing(false)

		router.route("/assets").handler({ RoutingContext routingContext ->
			staticHandler.delegate.handle(routingContext.delegate)
		})

		router.routeWithRegex("/api/1/*").handler({ RoutingContext routingContext ->
			ApiResources.serveResource(routingContext.request())
		})

		router.route("/").handler({ RoutingContext routingContext ->
			routingContext.response().sendFile("webroots/index.html")
		})



		server = vertx.createHttpServer([port:port, address:address])
		server.requestHandler({HttpServerRequest request ->
			router.delegate.accept(request.delegate)
		})
		server.listen()
		println("Vertx-Service-Registry listening on port $port , address $address")
	}

	@Override
	public void stop() throws Exception {
	}

	private void checkConfig(){
		final String envPort = System.getenv('OPENSHIFT_VERTX_PORT')
		if (envPort)
			port = Integer.parseInt(envPort)
		else
			port = DEFAULT_PORT

		address = System.getenv('OPENSHIFT_VERTX_IP')

		if (!address)
			address = DEFAULT_ADDRESS

		dataDir = System.getenv("OPENSHIFT_DATA_DIR")

		if (!dataDir)
			dataDir = DEFAULT_DATA_DIR
	}
}
