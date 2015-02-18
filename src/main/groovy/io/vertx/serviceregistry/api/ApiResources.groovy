package io.vertx.serviceregistry.api

import groovy.json.JsonBuilder
import io.vertx.serviceregistry.factory.ArtifactsFactory
import io.vertx.serviceregistry.model.Artifact

import org.vertx.groovy.core.http.HttpServerRequest

class ApiResources {
	static void serveResource(HttpServerRequest request){
		if(request.path == "/api/1/services" && request.method == "GET"){
			Set<Artifact> artifacts = ArtifactsFactory.artifacts
			String json = new JsonBuilder(artifacts).toString()
			request.response.end(json)
		} else {
			request.response.statusCode = 404
			request.response.end("Not found")
		}
	}
}
