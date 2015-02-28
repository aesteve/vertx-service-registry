package io.vertx.serviceregistry.handlers.errors;

import io.vertx.core.Handler;
import io.vertx.ext.apex.RoutingContext;

public class ApiErrorHandler implements Handler<RoutingContext> {

	@Override
	public void handle(RoutingContext context) {
		if (context.failure() != null) {
			context.failure().printStackTrace(); // FIXME : use logs instead
		}
		if (context.response().getStatusCode() <= 0) { // 0 ? -1 ?
			context.response().setStatusCode(500);
		}
		if (context.response().getStatusMessage() == null) {
			context.response().setStatusMessage("Internal Server Error");
		}
		String message = context.response().getStatusMessage();
		if (context.data().get("apiFailure") != null) {
			message = context.data().get("apiFailure").toString();
		}
		context.response().end(message);
	}
}
