package io.vertx.serviceregistry.http;

public enum Headers {
	/* Content related */
	ACCEPT("Accept"), CONTENT_TYPE("Content-Type"),
	/* Server/Network related */
	HOST("Host"),
	/* Cache related */
	LAST_MODIFIED("Last-Modified"), ETAG("Etag"), IF_MODIFIED_SINCE("If-Modified-Since"), IF_NONE_MATCH("If-None-Match");

	private String representation;

	private Headers(String representation) {
		this.representation = representation;
	}

	public static Headers fromString(String s) {
		for (Headers header : Headers.values()) {
			if (header.representation.equalsIgnoreCase(s))
				return header;
		}
		return null;
	}

	@Override
	public String toString() {
		return representation;
	}

}
