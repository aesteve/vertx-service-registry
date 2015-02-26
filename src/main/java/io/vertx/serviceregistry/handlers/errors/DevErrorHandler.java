package io.vertx.serviceregistry.handlers.errors;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.apex.RoutingContext;

import java.net.URL;

import org.apache.commons.io.Charsets;

import com.google.common.io.Resources;

public class DevErrorHandler implements Handler<RoutingContext> {

    private final String errorTpl;

    public DevErrorHandler(String errorTpl) {
        this.errorTpl = errorTpl;
    }

    @Override
    public void handle(RoutingContext context) {
        HttpServerResponse response = context.response();
        Throwable rootCause = context.failure();
        final String message = "Internal server error";
        response.setStatusCode(500);
        response.setStatusMessage(message);
        String errorPage;
        try {
            URL url = Resources.getResource(errorTpl);
            errorPage = Resources.toString(url, Charsets.UTF_8);
        } catch (Exception e) {
            response.end("error template not found");
            return;
        }
        StringBuilder stack = new StringBuilder();
        formatException(stack, rootCause);
        response.putHeader(HttpHeaders.CONTENT_TYPE, "text/html");
        errorPage = errorPage.replace("{pageTitle}", message);
        errorPage = errorPage.replace("{errorCode}", "500");
        errorPage = errorPage.replace("{errorMessage}", message);
        errorPage = errorPage.replace("{stackTrace}", stack.toString());
        response.end(errorPage);
    }

    private StringBuilder formatException(StringBuilder sb, Throwable t) {
        if (t == null)
            return sb;
        sb.append("<p class=\"error-message\">");
        sb.append(String.valueOf(t));
        sb.append(" : ");
        sb.append(t.getMessage());
        sb.append("</p>\n<ul>\n");
        for (StackTraceElement elem : t.getStackTrace()) {
            sb.append("<li>").append(elem).append("</li>");
        }
        sb.append("</ul>\n");
        if (t.getCause() != null) {
            sb.append("<p>Caused by</p>\n");
            formatException(sb, t.getCause());
        }
        return sb;
    }
}
