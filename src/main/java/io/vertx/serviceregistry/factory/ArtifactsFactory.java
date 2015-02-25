package io.vertx.serviceregistry.factory;

import io.vertx.core.json.JsonArray;
import io.vertx.serviceregistry.model.Artifact;

import java.util.List;

public class ArtifactsFactory {

	public static List<Artifact> artifacts;

	@SuppressWarnings("unchecked")
	public static void load(String json) {
		JsonArray obj = new JsonArray(json);
		artifacts = obj.getList();
	}
}
