package io.vertx.serviceregistry.api;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.serviceregistry.ServiceRegistryTestBase;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

public class JsonApiTest extends ServiceRegistryTestBase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	public void tearDown() throws Exception {
		if (client != null)
			client.close();
		super.tearDown();
	}

	public void testNoAcceptHeader() throws Exception {
		testGetStatus("/api/1/services", 406, "Not Acceptable");
	}

	@Test
	public void testStandardRequest() throws Exception {
		JsonArray artifactsTest = new JsonArray(new String(Files.readAllBytes(Paths.get("src/test/resources/artifacts_test.json")), "UTF-8"));
		testRequest(HttpMethod.GET, "/api/1/services", request -> {
			request.headers().add("Accept", "application/json");
		}, response -> {
			response.bodyHandler(buffer -> {
				JsonArray artifactsReceived = new JsonArray(buffer.toString("UTF-8"));
				assertEquals(artifactsTest, artifactsReceived);
				testComplete();
			});
		}, 200, "OK", null);
		await();
	}
}
