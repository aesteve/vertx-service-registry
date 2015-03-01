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

	private String groupId;
	private String artifactId;
	private List<Version> versions;
	private List<String> tags;
	private String md5;
	private List<String> availablePackages;
	private Map<String, Object> complementaryInfos;

	public Artifact() {
		versions = new ArrayList<Version>();
		tags = new ArrayList<String>();
		availablePackages = new ArrayList<String>();
		complementaryInfos = new HashMap<String, Object>();
	}

	public String fullId() {
		return groupId + ":" + artifactId;
	}

	@Override
	public String toString() {
		return "Artifact : " + fullId();
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

	public static Artifact fromMap(Map<String, Object> map) {
		Artifact art = new Artifact();
		art.groupId = (String) map.get("groupId");
		art.artifactId = (String) map.get("artifactId");
		List<Map<String, Object>> versions = (List<Map<String, Object>>) map.get("versions");
		versions.forEach(versionMap -> art.versions.add(Version.fromMap(versionMap)));
		art.tags = (List<String>) map.get("tags");
		art.md5 = (String) map.get("md5");
		art.availablePackages = (List<String>) map.get("availablePackages");
		art.complementaryInfos = (Map<String, Object>) map.get("complementaryInfos");
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

	/**
	 * @return the groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return the artifactId
	 */
	public String getArtifactId() {
		return artifactId;
	}

	/**
	 * @param artifactId the artifactId to set
	 */
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	/**
	 * @return the versions
	 */
	public List<Version> getVersions() {
		return versions;
	}

	/**
	 * @param versions the versions to set
	 */
	public void setVersions(List<Version> versions) {
		this.versions = versions;
	}

	/**
	 * @return the tags
	 */
	public List<String> getTags() {
		return tags;
	}

	/**
	 * @param tags the tags to set
	 */
	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	/**
	 * @return the md5
	 */
	public String getMd5() {
		return md5;
	}

	/**
	 * @param md5 the md5 to set
	 */
	public void setMd5(String md5) {
		this.md5 = md5;
	}

	/**
	 * @return the availablePackages
	 */
	public List<String> getAvailablePackages() {
		return availablePackages;
	}

	/**
	 * @param availablePackages the availablePackages to set
	 */
	public void setAvailablePackages(List<String> availablePackages) {
		this.availablePackages = availablePackages;
	}

	/**
	 * @return the complementaryInfos
	 */
	public Map<String, Object> getComplementaryInfos() {
		return complementaryInfos;
	}

	/**
	 * @param complementaryInfos the complementaryInfos to set
	 */
	public void setComplementaryInfos(Map<String, Object> complementaryInfos) {
		this.complementaryInfos = complementaryInfos;
	}
}
