package io.vertx.serviceregistry.engines;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.templ.TemplateEngine;

public class ReactTemplateEngine implements TemplateEngine {

	public ReactTemplateEngine(String templatesDir) {

	}

	@Override
	public void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
	}
}
