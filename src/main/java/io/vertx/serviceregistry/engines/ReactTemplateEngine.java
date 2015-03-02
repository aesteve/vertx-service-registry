package io.vertx.serviceregistry.engines;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.templ.TemplateEngine;
import io.vertx.serviceregistry.engines.react.ReactComponentParser;
import io.vertx.serviceregistry.engines.react.exceptions.ComponentParsingException;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;

import org.apache.commons.io.Charsets;

import com.google.common.io.Resources;

public class ReactTemplateEngine implements TemplateEngine {

	private final String htmlTpl;

	/**
	 * Creates a template engine
	 */
	public ReactTemplateEngine(String htmlTpl) {
		this.htmlTpl = htmlTpl;
	}

	/**
	 * Renders the React rootComponent into an html canvas.
	 * Injects libs for React to work on the browser
	 * 
	 * @param context the routingContext from which we'll read request/response and properties
	 * @param rootComponent the root component describing the webapp, which requires sub components
	 * @param handler the handler that will be called with a result containing the buffer or a failure.
	 */
	@Override
	public void render(RoutingContext context, String rootComponent, Handler<AsyncResult<Buffer>> handler) {
		// TODO : make it a whole pipeline thing : just render from a minified
		// js file in prod env + store generated html to cache
		// TODO : make it async : do not block everything
		ReactComponentParser parser = new ReactComponentParser("sites/scripts/server-bundle.js");
		try {
			String reactHtml = parser.parseComponentTree(context.data());
			String fullHtml = decorateTplWith(reactHtml);
			Buffer buffer = Buffer.buffer(fullHtml);
			Future<Buffer> future = Future.succeededFuture(buffer);
			handler.handle(future);
		} catch (ComponentParsingException | IOException e) {
			handler.handle(Future.failedFuture(e));
		}
	}

	private String decorateTplWith(String html) throws IOException {
		URL url = Resources.getResource(htmlTpl);
		String fullPage = Resources.toString(url, Charsets.UTF_8);
		// replace "content" with html
		fullPage = fullPage.replaceAll("\\{content\\}", Matcher.quoteReplacement(html));
		fullPage = fullPage.replaceAll("\\{scripts\\}", getScriptTags());
		return fullPage;
	}

	private String getScriptTags() {
		return "<script src=\"/assets/scripts/browser-bundle.js\" charset=\"UTF-8\" async></script>";
	}
}
