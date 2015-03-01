package io.vertx.serviceregistry.api.services;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.serviceregistry.ServiceRegistryTestBase;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

public class ServicesApiTest extends ServiceRegistryTestBase {
	@Test
	public void testStandardRequest() throws Exception {
		JsonArray artifactsTest = new JsonArray(new String(Files.readAllBytes(Paths.get(getTestFileOnFs())), "UTF-8"));
		testRequest(HttpMethod.GET, "/api/1/services", request -> {
			request.headers().add("Accept", "application/json");
		}, response -> {
			response.bodyHandler(buffer -> {
				JsonArray artifactsReceived = new JsonArray(buffer.toString("UTF-8"));
				assertEquals(artifactsTest, artifactsReceived);
				testComplete();
			});
		}, 200, "OK", null);
	}

	@Test
	public void withAnEndingSlash() throws Exception {
		JsonArray artifactsTest = new JsonArray(new String(Files.readAllBytes(Paths.get(getTestFileOnFs())), "UTF-8"));
		testRequest(HttpMethod.GET, "/api/1/services/", request -> {
			request.headers().add("Accept", "application/json");
		}, response -> {
			response.bodyHandler(buffer -> {
				JsonArray artifactsReceived = new JsonArray(buffer.toString("UTF-8"));
				assertEquals(artifactsTest, artifactsReceived);
				testComplete();
			});
		}, 200, "OK", null);
	}

}
