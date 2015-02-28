package io.vertx.serviceregistry.api.etag;

import io.vertx.core.http.HttpMethod;
import io.vertx.serviceregistry.ServiceRegistryTestBase;

import org.junit.Test;

public class NoCollisionETagTest extends ServiceRegistryTestBase {
	@Override
	public String getTestFile() {
		return "another_artifacts_test_file.json";
	}

	@Test
	public void eTagIsDifferent() throws Exception {
		testRequest(HttpMethod.GET, "/api/1/services", request -> {
			request.headers().add("Accept", "application/json");
		}, response -> {
			assertNotNull(response.headers().get("ETag"));
			assertFalse(response.headers().get("ETag").equals(ETagApiTest.TEST_DATA_ETAG));
			testComplete();
		}, 200, "OK", null);
		await();
	}
}
