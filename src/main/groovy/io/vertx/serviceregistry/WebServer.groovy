package io.vertx.serviceregistry

import io.vertx.serviceregistry.api.ApiResources
import io.vertx.serviceregistry.factory.ArtifactsFactory

import org.vertx.groovy.core.http.HttpServer
import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.groovy.core.http.HttpServerResponse
import org.vertx.groovy.platform.Verticle

class WebServer extends Verticle {

	HttpServer server
	ApiResources apiResources
	File jsonFile

	@Override
	def start(){
		checkConfig()
		apiResources = new ApiResources()
		ArtifactsFactory.instance().jsonFile = jsonFile
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
		server.listen(80)
	}

	private void checkConfig(){
		String filePath = container.config["JSON_FILE"] as String
		if (!filePath)
			throw new IllegalArgumentException("No json file configured, please add it into mod.json")
		jsonFile = new File(filePath)
		if(!jsonFile.exists() || !jsonFile.isFile())
			throw new IllegalArgumentException("The specified json file doesn't exist")
	}

	@Override
	def stop(){
	}
}
