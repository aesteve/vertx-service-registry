package io.vertx.serviceregistry.engines;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.templ.TemplateEngine;
import io.vertx.react.filesystem.ClasspathFileResolver;
import io.vertx.serviceregistry.factory.ArtifactsFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ReactTemplateEngine implements TemplateEngine {

	@Override
	public void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
		ClasspathFileResolver.init();
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine nashorn = mgr.getEngineByName("nashorn");
		nashorn = mgr.getEngineByName("nashorn");

		try {
			nashorn.eval(getScript("jvm-npm.js")); // tweaked version of vertx's
													// jvm-npm
			nashorn.eval(getScript("vertx-js/util/console.js"));
			nashorn.eval(getScript("vertx-js/util/utils.js"));
			nashorn.eval("var process = {env:{}}");
			nashorn.eval("var global = this;");
			nashorn.eval(getBundledReact());
			nashorn.eval(getJSXTransformer());
			nashorn.eval(getScript("webroots/scripts/libs/underscore-1.7.0.min.js"));
			try {
				nashorn.eval(new FileReader("sites/components/App.jsx"));
				String markup = new String(Files.readAllBytes(Paths.get("sites/index.html")));
				JsonObject props = new JsonObject();
				props.put("services", ArtifactsFactory.artifacts);
				String content = (String) nashorn.eval("React.renderToString(React.createFactory(FullApp)(" + props.toString() + "));");
				markup = markup.replace("{scripts}", "");
				markup = markup.replace("{content}", content);
				context.response().end(markup);
			} catch (IOException ioe) {
				ioe.printStackTrace();
				context.fail(500);
			}
			// String markup = context.response().end(markup);
		} catch (ScriptException se) {
			se.printStackTrace();
			context.fail(500);
		}
	}

	private Reader getBundledReact() {
		return getScript("webroots/scripts/libs/react-with-addons.min.js");
	}

	private Reader getJSXTransformer() {
		return getScript("webroots/scripts/libs/JSXTransformer.js");
	}

	private Reader getScript(String path) {
		return new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(path));
	}
}
