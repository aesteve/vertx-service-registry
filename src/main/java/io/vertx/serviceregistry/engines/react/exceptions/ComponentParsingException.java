package io.vertx.serviceregistry.engines.react.exceptions;

public class ComponentParsingException extends Throwable {

	private static final long serialVersionUID = -3883083018358178462L;

	public ComponentParsingException(Throwable cause) {
		super(cause);
	}

	public ComponentParsingException(String message) {
		super(message);
	}
}
