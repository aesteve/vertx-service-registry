package io.vertx.serviceregistry.api

import groovy.json.JsonBuilder
import io.vertx.core.http.HttpMethod
import io.vertx.groovy.core.http.HttpServerRequest
import io.vertx.groovy.core.http.HttpServerResponse
import io.vertx.serviceregistry.factory.ArtifactsFactory
import io.vertx.serviceregistry.model.Artifact

class ApiResources {
	static void serveResource(HttpServerRequest request){
		HttpServerResponse response = request.response()
		println request.path()
		if(request.path() == "/api/1/services" && request.method() == HttpMethod.GET){
			Set<Artifact> artifacts = ArtifactsFactory.artifacts
			String json = new JsonBuilder(artifacts).toString()
			request.response().end(json)
		} else {
			response.statusCode = 404
			response.end("Not found")
		}
	}
}
