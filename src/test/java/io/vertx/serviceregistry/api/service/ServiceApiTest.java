package io.vertx.serviceregistry.api.service;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceregistry.ServiceRegistryTestBase;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.Test;

public class ServiceApiTest extends ServiceRegistryTestBase {
	@SuppressWarnings("unchecked")
	@Test
	public void testStandardRequest() throws Exception {
		JsonArray artifactsTest = new JsonArray(new String(Files.readAllBytes(Paths.get(getTestFileOnFs())), "UTF-8"));
		Map<String, Object> artifact = (Map<String, Object>) artifactsTest.getList().get(0);
		String serviceId = (String) artifact.get("groupId") + ":" + (String) artifact.get("artifactId");
		testRequest(HttpMethod.GET, "/api/1/services/" + serviceId, request -> {
			request.headers().add("Accept", "application/json");
		}, response -> {
			response.bodyHandler(buffer -> {
				JsonObject artifactReceived = new JsonObject(buffer.toString("UTF-8"));
				JsonObject obj = new JsonObject(artifact);
				assertEquals(obj, artifactReceived);
				testComplete();
			});
		}, 200, "OK", null);
		await();
	}
}
