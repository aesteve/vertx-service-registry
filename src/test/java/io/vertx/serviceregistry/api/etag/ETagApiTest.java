package io.vertx.serviceregistry.api.etag;

import io.vertx.core.http.HttpMethod;
import io.vertx.serviceregistry.ServiceRegistryTestBase;

import org.junit.Test;

public class ETagApiTest extends ServiceRegistryTestBase {

	public static final String TEST_DATA_ETAG = "50fb9692cffd5b61afc73ea5d41b08fe";

	@Test
	public void eTagIsSentAndCorrect() throws Exception {
		testRequest(HttpMethod.GET, SERVICES_API, request -> {
			request.headers().add("Accept", "application/json");
		}, response -> {
			assertEquals(TEST_DATA_ETAG, response.headers().get("ETag"));
			testComplete();
		}, 200, "OK", null);
		await();
	}

	@Test
	public void eTagIsRead() throws Exception {
		// We have to make a first request even though we can't store its ETag (due to Java final behaviour)
		// Since there's no warranty this test will be run after the one above
		// TODO : use test suite (vertx-unit) in this case
		testRequest(HttpMethod.GET, SERVICES_API, request -> {
			request.headers().add("Accept", "application/json");
		}, null, 200, "OK", null);

		testRequest(HttpMethod.GET, SERVICES_API, request -> {
			request.headers().add("Accept", "application/json");
			request.headers().add("If-None-Match", TEST_DATA_ETAG);
		}, null, 304, "Not Modified", null);
	}
}
