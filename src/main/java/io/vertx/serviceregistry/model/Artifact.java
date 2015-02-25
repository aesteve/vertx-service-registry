package io.vertx.serviceregistry.model;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Artifact {
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
}
