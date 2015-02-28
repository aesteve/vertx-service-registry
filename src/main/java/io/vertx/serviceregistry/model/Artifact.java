package io.vertx.serviceregistry.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO : cleanUp and reafactor (fromExport in a specific marshaller)
// distributions ? are they used ? 
// + getters/setters (or is it really evil ?)
public class Artifact implements ApiObject {
	enum Dists {
		MOD("-mod.zip"), JAR(".jar");

		private String extension;

		private Dists(String extension) {
			this.extension = extension;
		}

		@Override
		public String toString() {
			return extension;
		}
	}

	public String groupId;
	public String artifactId;
	public List<Version> versions;
	public List<String> tags;
	public String md5;
	public List<String> availablePackages;
	public Map<String, String> complementaryInfos;

	public Artifact() {
		versions = new ArrayList<Version>();
		tags = new ArrayList<String>();
		availablePackages = new ArrayList<String>();
		complementaryInfos = new HashMap<String, String>();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Artifact fromExport(JsonObject json) {
		Artifact art = new Artifact();
		art.groupId = json.getString("g");
		art.artifactId = json.getString("a");
		art.tags = json.getJsonArray("tags").getList();
		art.availablePackages = json.getJsonArray("ec").getList();

		art.versions = new ArrayList<Version>();

		List l = json.getJsonArray("versions").getList();
		l.forEach(map -> {
			JsonObject o = (JsonObject) map;
			Version v = new Version(o.getString("name"), o.getLong("timestamp"));
			art.versions.add(v);
		});

		return art;
	}

	@Override
	public JsonObject toJsonObject() {
		JsonObject json = new JsonObject();
		json.put("groupId", groupId);
		json.put("artifactId", artifactId);
		JsonArray versions = new JsonArray();
		this.versions.forEach(version -> versions.add(version.toJsonObject()));
		json.put("versions", versions);
		json.put("tags", new JsonArray(this.tags));
		json.put("md5", md5);
		json.put("availablePackages", this.availablePackages);
		json.put("complementaryInfos", complementaryInfos);
		return json;
	}
}
