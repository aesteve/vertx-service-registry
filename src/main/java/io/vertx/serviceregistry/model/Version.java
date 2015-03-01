package io.vertx.serviceregistry.model;

import io.vertx.core.json.JsonObject;

import java.util.Map;

public class Version implements Comparable<Version>, ApiObject {
	public String name;
	public Long timestamp;

	public Version(String name, Long timestamp) {
		this.name = name;
		this.timestamp = timestamp;
	}

	@Override
	public int compareTo(Version other) {
		return timestamp.compareTo(other.timestamp);
	}

	@Override
	public String toString() {
		return this.name;
	}

	public static Version fromMap(Map<String, Object> map) {
		String name = (String) map.get("name");
		Long timestamp = (Long) map.get("timestamp");
		return new Version(name, timestamp);
	}

	@Override
	public JsonObject toJsonObject() {
		JsonObject json = new JsonObject();
		json.put("name", name);
		json.put("timestamp", timestamp);
		return json;
	}
}
