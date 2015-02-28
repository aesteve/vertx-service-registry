package io.vertx.serviceregistry;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class ServiceRegistryTestBase extends VertxTestBase {

	protected final static int port = 8080;
	protected final static String host = "localhost";
	protected HttpClient client;
	protected String deploymentId;

	public String getTestFile() {
		return "artifacts_test.json";
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		Map<String, Object> config = new HashMap<String, Object>();
		config.put("host", host);
		config.put("port", port);
		config.put("data-dir", "src/test/resources");
		config.put("artifacts-file", getTestFile());
		DeploymentOptions options = new DeploymentOptions();
		options.setConfig(new JsonObject(config));
		CountDownLatch latch = new CountDownLatch(1);
		vertx.deployVerticle("io.vertx.serviceregistry.WebServer", options, asyncResult -> {
			assertTrue(asyncResult.succeeded());
			deploymentId = asyncResult.result();
			latch.countDown();
		});
		client = vertx.createHttpClient(new HttpClientOptions().setDefaultHost(host).setDefaultPort(port));
		awaitLatch(latch);
	}

	@Override
	public void tearDown() throws Exception {
		if (deploymentId != null) {
			CountDownLatch latch = new CountDownLatch(1);
			vertx.undeploy(deploymentId, asyncResult -> {
				latch.countDown();
			});
			awaitLatch(latch);
		}
		super.tearDown();
	}

	protected void testRequest(
			HttpMethod method,
			String path,
			Consumer<HttpClientRequest> requestAction,
			Consumer<HttpClientResponse> responseAction,
			int statusCode,
			String statusMessage,
			String responseBody) throws Exception {
		testRequestBuffer(method, path, requestAction, responseAction, statusCode, statusMessage, responseBody != null ? Buffer.buffer(responseBody) : null);
	}

	protected void testRequestBuffer(
			HttpMethod method,
			String path,
			Consumer<HttpClientRequest> requestAction,
			Consumer<HttpClientResponse> responseAction,
			int statusCode,
			String statusMessage,
			Buffer responseBodyBuffer) throws Exception {
		CountDownLatch latch = new CountDownLatch(1);
		HttpClientRequest req = client.request(method, port, host, path, resp -> {
			assertEquals(statusCode, resp.statusCode());
			assertEquals(statusMessage, resp.statusMessage());
			if (responseAction != null) {
				responseAction.accept(resp);
			}
			if (responseBodyBuffer == null) {
				latch.countDown();
			} else {
				resp.bodyHandler(buff -> {
					assertEquals(responseBodyBuffer, buff);
					latch.countDown();
				});
			}
		});
		if (requestAction != null) {
			requestAction.accept(req);
		}
		req.end();
		awaitLatch(latch);
	}

	protected void testGet(
			String path,
			Consumer<HttpClientRequest> requestAction,
			Consumer<HttpClientResponse> responseAction,
			int statusCode,
			String statusMessage,
			String responseBody) throws Exception {
		testRequest(HttpMethod.GET, path, requestAction, responseAction, statusCode, statusMessage, responseBody);
	}

	protected void testGetStatus(String path, int statusCode, String message) throws Exception {
		testRequestStatus(HttpMethod.GET, path, statusCode, message);
	}

	protected void testRequestStatus(HttpMethod method, String path, int statusCode, String message) throws Exception {
		testRequest(HttpMethod.GET, path, null, null, statusCode, message, null);
	}

}
