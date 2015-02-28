package io.vertx.serviceregistry.http.etag.impl;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.serviceregistry.http.etag.ETagCachingService;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Simple stores requests/etags in an in-memory HashMap
 * 
 * @author aesteve
 */
public class InMemoryETagCachingService implements ETagCachingService {
	private static Map<String, String> eTags = new HashMap<String, String>();

	@Override
	public void storeETag(String eTag, HttpServerRequest request) {
		eTags.put(request.uri(), eTag);
	}

	@Override
	public String getETag(HttpServerRequest request) {
		return eTags.get(request.uri());
	}

	@Override
	public void resourceHasChanged(final String resourcePath) {
		// invalidate cache properly
		eTags = eTags.entrySet().stream().
				filter(entry ->
						entry.getKey().startsWith(resourcePath)
				).collect(
						Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())
				);
	}

}
