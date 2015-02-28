package io.vertx.serviceregistry.api.pagination;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.serviceregistry.ServiceRegistryTestBase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class PaginationTest extends ServiceRegistryTestBase {
	@Override
	public String getTestFile() {
		return "pagination_artifacts_test.json";
	}

	@Test
	public void invalidPerPageParam() throws Exception {
		testRequest(HttpMethod.GET, "/api/1/services?perPage=notAnInt", request -> {
			request.headers().add("Accept", "application/json");
		}, null, 400, "Bad Request", null);
	}

	@Test
	public void perPageParamTooBig() throws Exception {
		testRequest(HttpMethod.GET, "/api/1/services?perPage=10000", request -> {
			request.headers().add("Accept", "application/json");
		}, null, 400, "Bad Request", null);
	}

	@Test
	public void invalidPageParam() throws Exception {
		testRequest(HttpMethod.GET, "/api/1/services?page=notAnInt", request -> {
			request.headers().add("Accept", "application/json");
		}, null, 400, "Bad Request", null);
	}

	@Test
	public void pageParamTooBig() throws Exception {
		testRequest(HttpMethod.GET, "/api/1/services?page=10000", request -> {
			request.headers().add("Accept", "application/json");
		}, null, 400, "Bad Request", null);
	}

	@Test
	public void perPageParamIsRespected() throws Exception {
		int perPage = 10;
		testRequest(HttpMethod.GET, "/api/1/services?perPage=" + perPage, request -> {
			request.headers().add("Accept", "application/json");
		}, response -> {
			response.bodyHandler(buffer -> {
				JsonArray artifacts = new JsonArray(buffer.toString("UTF-8"));
				assertEquals(perPage, artifacts.size());
				testComplete();
			});
		}, 200, "OK", null);
		await();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void firstPageByDefault() throws Exception {
		int perPage = 10;
		testRequest(HttpMethod.GET, "/api/1/services?perPage=" + perPage, request -> {
			request.headers().add("Accept", "application/json");
		}, response -> {
			response.bodyHandler(buffer -> {
				JsonArray artifacts = new JsonArray(buffer.toString("UTF-8"));
				List<Map<String, Object>> l = artifacts.getList();
				Map<String, Object> artifact = l.get(0);
				assertEquals("0", artifact.get("artifactId"));
				testComplete();
			});
		}, 200, "OK", null);
		await();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void pageParamIsRespected() throws Exception {
		int perPage = 10;
		int page = 3;
		// the first artifactId (in file) is 0 :
		// -> the first on page 3 must be 20
		// -> the last on page 3 must be 29
		testRequest(HttpMethod.GET, "/api/1/services?perPage=" + perPage + "&page=" + page, request -> {
			request.headers().add("Accept", "application/json");
		}, response -> {
			response.bodyHandler(buffer -> {
				JsonArray artifacts = new JsonArray(buffer.toString("UTF-8"));
				List<Map<String, Object>> l = artifacts.getList();
				Map<String, Object> firstArtifact = l.get(0);
				assertEquals("20", firstArtifact.get("artifactId"));
				Map<String, Object> lastArtifact = l.get(l.size() - 1);
				assertEquals("29", lastArtifact.get("artifactId"));
				testComplete();
			});
		}, 200, "OK", null);
		await();
	}

	@Test
	public void linkHeadersOnMiddlePage() throws Exception {
		int perPage = 10;
		int page = 5;
		// the first artifactId (in file) is 0 :
		// -> the first on page 3 must be 20
		// -> the last on page 3 must be 29
		testRequest(HttpMethod.GET, "/api/1/services?perPage=" + perPage + "&page=" + page, request -> {
			request.headers().add("Accept", "application/json");
		}, response -> {
			assertTrue(response.headers().contains("Link"));
			String linkHeader = response.headers().get("Link");
			Map<String, String> links = parseLinkHeader(linkHeader);
			assertEquals(4, links.size());
			String first = links.get("first");
			String prev = links.get("prev");
			String next = links.get("next");
			String last = links.get("last");
			assertNotNull(first);
			assertNotNull(prev);
			assertNotNull(next);
			assertNotNull(last);
			assertFalse(first.equals(prev));
			assertFalse(next.equals(last));
			links.forEach((rel, link) -> {
				assertTrue(perPageIsPreserved(perPage, link));
				assertTrue(pageIsPreserved(link));
			});

			testComplete();
		}, 200, "OK", null);
		await();
	}

	private Map<String, String> parseLinkHeader(String link) {
		Map<String, String> relLinks = new HashMap<String, String>();
		String[] rawLinks = link.split(",");
		for (String rawLink : rawLinks) {
			String[] sections = rawLink.split(";");
			String url = sections[0].replaceAll("<(.*)>", "$1");
			String rel = sections[1].replaceAll("rel=\\\"(.*)\\\"", "$1");
			System.out.println("[" + rel + " :|: " + url + "]");
			relLinks.put(rel.trim(), url);
		}
		return relLinks;
	}

	private boolean perPageIsPreserved(int perPage, String link) {
		String queryParam = "perPage=" + perPage;
		return link.indexOf(queryParam) > link.indexOf("?") && // is in the query params
				link.indexOf(queryParam) == link.lastIndexOf(queryParam); // has replaced the old value, not foolishly appended

	}

	private boolean pageIsPreserved(String link) {
		String queryParam = "page=";
		return link.indexOf(queryParam) > link.indexOf("?") && // is in the query params
				link.indexOf(queryParam) == link.lastIndexOf(queryParam); // has replaced the old value, not foolishly appended
	}
}
