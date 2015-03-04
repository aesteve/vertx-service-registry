package io.vertx.serviceregistry.io;

import io.vertx.componentdiscovery.model.Artifact;
import io.vertx.componentdiscovery.model.TaskReport;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApiObjectMarshaller {

	public static JsonArray marshallArtifacts(Collection<Artifact> items) {
		if (items == null) {
			return null;
		}
		List<JsonObject> objs = items.stream().map(artifact -> artifact.toJsonObject()).collect(Collectors.toList());
		return new JsonArray(objs);
	}

	@SuppressWarnings("rawtypes")
	public static JsonArray marshallReports(Collection<TaskReport> items) {
		if (items == null) {
			return null;
		}
		List<JsonObject> objs = items.stream().map(report -> report.toJsonObject()).collect(Collectors.toList());
		return new JsonArray(objs);
	}

	@SuppressWarnings("unchecked")
	public static List<Artifact> unmarshallArtifacts(JsonArray array) {
		if (array == null) {
			return null;
		}
		List<Map<String, Object>> list = array.getList();
		return list.stream().map(map -> Artifact.fromMap(map)).collect(Collectors.toList());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<TaskReport> unmarshallReports(JsonArray array) {
		if (array == null) {
			return null;
		}
		List<Map<String, Object>> list = array.getList();
		return list.stream().map(map -> TaskReport.fromMap(map)).collect(Collectors.toList());
	}

}
