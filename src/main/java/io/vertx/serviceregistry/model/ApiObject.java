package io.vertx.serviceregistry.model;

import io.vertx.core.json.JsonObject;

public interface ApiObject {
	public JsonObject toJsonObject();
}