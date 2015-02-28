package io.vertx.serviceregistry.http.etag;

import io.vertx.core.http.HttpServerRequest;

public interface ETagCachingService {

	public void storeETag(String eTag, HttpServerRequest request);

	public String getETag(HttpServerRequest request);

	public void resourceHasChanged(String requestUri);
}
