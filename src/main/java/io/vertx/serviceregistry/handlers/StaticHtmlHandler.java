package io.vertx.serviceregistry.handlers;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.TemplateHandler;
import io.vertx.serviceregistry.engines.ReactTemplateEngine;
import io.vertx.serviceregistry.fileutils.StaticTemplatesRegistry;
import io.vertx.serviceregistry.http.Headers;
import io.vertx.serviceregistry.http.pagination.PaginationContext;

public class StaticHtmlHandler implements Handler<RoutingContext> {
	private StaticTemplatesRegistry registry;
	private Handler<RoutingContext> notCachedHandler;

	public StaticHtmlHandler(StaticTemplatesRegistry registry) {
		this.registry = registry;
		notCachedHandler = TemplateHandler.create(new ReactTemplateEngine("server-index.html"), "", "text/html");
	}

	@Override
	public void handle(RoutingContext context) {
		PaginationContext paginationContext = (PaginationContext) context.data().get("paginationContext");
		if (paginationContext == null) {
			notCachedHandler.handle(context);
			return;
		}
		String fileName = registry.getCached("services", paginationContext);
		if (fileName == null) {
			notCachedHandler.handle(context);
			return;
		}
		HttpServerResponse response = context.request().response();
		response.headers().add(Headers.CONTENT_TYPE.toString(), "text/html");
		response.sendFile(fileName);
	}
}
