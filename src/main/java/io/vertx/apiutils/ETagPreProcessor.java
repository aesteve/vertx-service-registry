package io.vertx.apiutils;

import static io.vertx.serviceregistry.http.Headers.IF_NONE_MATCH;
import io.vertx.core.Handler;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.serviceregistry.http.etag.ETagCachingService;

import org.apache.commons.httpclient.HttpStatus;

public class ETagPreProcessor implements Handler<RoutingContext> {

	private ETagCachingService service;

	public ETagPreProcessor(ETagCachingService service) {
		this.service = service;
	}

	@Override
	public void handle(RoutingContext context) {
		String eTag = service.getETag(context.request());
		if (eTag != null) {
			// analyse request header, see if it matches
			String requestETag = context.request().getHeader(IF_NONE_MATCH.toString());
			if (eTag.equals(requestETag)) {
				context.response().setStatusCode(HttpStatus.SC_NOT_MODIFIED);
				context.response().end();
				return; // no need to go further, that's the purpose of an ETag
			}
		}
		context.next();
	}
}
