package io.vertx.serviceregistry

import io.vertx.serviceregistry.api.ApiResources
import io.vertx.serviceregistry.factory.ArtifactsFactory

import org.vertx.groovy.core.AsyncResult
import org.vertx.groovy.core.buffer.Buffer
import org.vertx.groovy.core.file.AsyncFile
import org.vertx.groovy.core.http.HttpServer
import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.groovy.core.http.HttpServerResponse
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.Handler

class WebServer extends Verticle {
	private final static String DEFAULT_ADDRESS = "localhost"
	private final static Integer DEFAULT_PORT = 90

	HttpServer server
	ApiResources apiResources
	File jsonFile
	Integer port
	String address

	@Override
	def start(){
		checkConfig()
		apiResources = new ApiResources()
		vertx.fileSystem.readFile("export.json", { AsyncResult<Buffer> result ->
			if (result.succeeded) {
				Buffer b = result.result
				ArtifactsFactory.load(b.toString())
			} else {
				println "Failed to read", result.cause
			}		
		})
		server = vertx.createHttpServer()
		server.requestHandler { HttpServerRequest request ->
			request.response.with { HttpServerResponse response ->
				if (request.path == "/") {
					response.sendFile("assets/index.html")
				} else if (request.path == "favicon.ico") {
					response.sendFile("assets/favicon.ico")
				} else if (request.path.startsWith("/assets")) {
					response.sendFile(request.path.substring(1))
				} else if (request.path == "/api/1/services") {
					try {
						ApiResources.serveResource(request)
					} catch(all) {
						container.logger.error(all)
						response.statusCode = 500
						response.end("Unexpected error occurred")
					}
				} else {
					response.statusCode = 404
					response.end("Not found")
				}
			}
		}
		server.listen(port, address)
		println("Vertx-Service-Registry listening on port $port , address $address")
	}

	private void checkConfig(){
		if (container.env['OPENSHIFT_VERTX_PORT'])
			port = Integer.parseInt(container.env['OPENSHIFT_VERTX_PORT'])
		else
			port = Integer.parseInt("8080")

		address = container.env['OPENSHIFT_VERTX_IP']
		if (!port)
			port = DEFAULT_PORT

		if (!address)
			address = DEFAULT_ADDRESS
	}

	@Override
	def stop(){
	}
}
