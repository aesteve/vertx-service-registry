package io.vertx.serviceregistry.api;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.serviceregistry.factory.ArtifactsFactory;
import io.vertx.serviceregistry.model.Artifact;

import java.util.List;

public class ApiResources {
	public static void serveResource(HttpServerRequest request) {
		HttpServerResponse response = request.response();
		if ("/api/1/services".equals(request.path()) && request.method() == HttpMethod.GET) {
			List<Artifact> artifacts = ArtifactsFactory.artifacts;
			String json = new JsonArray(artifacts).toString();
			request.response().end(json);
		} else {
			response.setStatusCode(404);
			response.end("Not found");
		}
	}
}
