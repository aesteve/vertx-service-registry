package io.vertx.apiutils;

import static io.vertx.serviceregistry.http.Headers.ACCEPT;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.ext.apex.RoutingContext;

import org.apache.commons.httpclient.HttpStatus;

public class JsonPreProcessor implements Handler<RoutingContext> {

	private final static String JSON_CONTENT_TYPE = "application/json";

	@Override
	public void handle(RoutingContext context) {
		MultiMap requestHeaders = context.request().headers();
		if (!acceptsJson(requestHeaders)) {
			context.response().setStatusCode(HttpStatus.SC_NOT_ACCEPTABLE);
			context.fail(HttpStatus.SC_NOT_ACCEPTABLE);
		}
		context.next();
	}

	private boolean acceptsJson(MultiMap requestHeaders) {
		String accept = requestHeaders.get(ACCEPT.toString());
		return accept != null && accept.equalsIgnoreCase(JSON_CONTENT_TYPE);
	}

}
