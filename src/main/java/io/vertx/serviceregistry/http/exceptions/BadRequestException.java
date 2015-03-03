package io.vertx.serviceregistry.http.exceptions;

import org.apache.commons.httpclient.HttpStatus;

public class BadRequestException extends HttpException {
    private static final long serialVersionUID = -8177274609736272204L;

    public BadRequestException(String message) {
        super(HttpStatus.SC_BAD_REQUEST, message);
    }
}
