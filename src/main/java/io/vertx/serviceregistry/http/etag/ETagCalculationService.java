package io.vertx.serviceregistry.http.etag;

import io.vertx.apiutils.PayloadUtils;
import io.vertx.ext.apex.RoutingContext;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ETagCalculationService {
	private static MessageDigest digest;

	public static String calculateETag(RoutingContext context) {
		Object payload = context.data().get("payload");
		// no payload => no etag
		if (payload == null) {
			return null;
		}
		// ensures that the toString() method will reflect the data structure (JsonObject or JsonArray)
		if (!PayloadUtils.isPayloadAcceptable(payload)) {
			context.fail(new UnsupportedOperationException("Cannot calculate ETag. " + payload.getClass() + " is not a JsonObject or a JsonArray"));
			return null;
		}
		String strPayload = payload.toString();
		return digestString(strPayload);
	}

	private static String digestString(String input) {
		if (digest == null) {
			try {
				digest = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException nsae) {
				// no MD5 ? really ?
			}
		}
		StringBuffer sb = new StringBuffer();
		digest.reset(); // is it even necessary ?
		digest.update(input.getBytes());
		byte[] digested = digest.digest();
		for (byte b : digested) {
			sb.append(String.format("%02x", b & 0xff));
		}
		return sb.toString();
	}
}
