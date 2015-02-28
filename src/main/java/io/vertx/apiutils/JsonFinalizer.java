package io.vertx.apiutils;

import static io.vertx.serviceregistry.http.Headers.CONTENT_TYPE;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.apex.RoutingContext;

import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;

/**
 * /!\ THIS WILL FINALIZE THE RESPONSE and send it : always, always call last !!!
 * 
 * @author aesteve
 */
public class JsonFinalizer implements Handler<RoutingContext> {

	/**
	 * Does nothing if the response has already been written (to avoid exceptions)
	 * Relies mainly on the "payload" attribute within the context's data
	 * context fails with an OperationNotSupportedException if payload is not a JsonArray or a JsonObject
	 */
	@Override
	public void handle(RoutingContext context) {
		HttpServerResponse response = context.response();
		if (response.ended())
			return;
		response.headers().add(CONTENT_TYPE.toString(), "application/json");
		Map<String, Object> data = context.data();
		Object payload = data.get("payload");
		if (payload == null) {
			response.setStatusCode(HttpStatus.SC_NO_CONTENT);
			return;
		}
		if (!PayloadUtils.isPayloadAcceptable(payload)) {
			context.fail(new UnsupportedOperationException("A payload must be an instance of JsonArray or JsonObject"));
		}
		response.end(payload.toString());
	}
}
