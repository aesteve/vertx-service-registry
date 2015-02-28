package io.vertx.apiutils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class PayloadUtils {
	public static boolean isPayloadAcceptable(Object payload) {
		return ((payload instanceof JsonArray) || (payload instanceof JsonObject));
	}
}
