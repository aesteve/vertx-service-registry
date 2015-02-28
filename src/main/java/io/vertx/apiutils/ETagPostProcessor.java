package io.vertx.apiutils;

import io.vertx.core.Handler;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.serviceregistry.http.etag.ETagCachingService;
import io.vertx.serviceregistry.http.etag.ETagCalculationService;

public class ETagPostProcessor implements Handler<RoutingContext> {

	private ETagCachingService service;

	public ETagPostProcessor(ETagCachingService service) {
		this.service = service;
	}

	@Override
	public void handle(RoutingContext context) {
		String eTag = ETagCalculationService.calculateETag(context);
		if (eTag != null) {
			service.storeETag(eTag, context.request());
			context.response().headers().add("ETag", eTag);
		}
		context.next();
	}
}
