package io.vertx.serviceregistry.io;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceregistry.model.Artifact;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ArtifactsMarshaller {

	public static JsonArray marshall(Collection<Artifact> artifacts) {
		if (artifacts == null) {
			return null;
		}
		List<JsonObject> objs = artifacts.stream().map(artifact -> artifact.toJsonObject()).collect(Collectors.toList());
		return new JsonArray(objs);
	}

	@SuppressWarnings("unchecked")
	public static List<Artifact> unmarshall(JsonArray array) {
		if (array == null) {
			return null;
		}
		List<Map<String, Object>> list = array.getList();
		return list.stream().map(map -> Artifact.fromMap(map)).collect(Collectors.toList());

	}
}
