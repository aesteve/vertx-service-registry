package io.vertx.serviceregistry.api;

import io.vertx.core.http.HttpMethod;
import io.vertx.serviceregistry.ServiceRegistryTestBase;

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

	@Test
	public void testNoAcceptHeader() throws Exception {
		testGetStatus(SERVICES_API, 406, "Not Acceptable");
	}

	@Test
	public void testStandardRequest() throws Exception {
		testRequest(HttpMethod.GET, SERVICES_API, request -> {
			request.headers().add("Accept", "application/json");
		}, null, 200, "OK", null);
	}
}
