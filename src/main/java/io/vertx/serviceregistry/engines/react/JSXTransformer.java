package io.vertx.serviceregistry.engines.react;

import io.vertx.serviceregistry.engines.react.exceptions.JSXTransformException;
import io.vertx.serviceregistry.fileutils.ScriptsResolver;

import java.io.IOException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class JSXTransformer {

	private Invocable engine;
	private JSObject transformer;
	private ScriptEngineManager scriptEngineManager;

	public JSXTransformer() {
		scriptEngineManager = new ScriptEngineManager();
	}

	public void init() throws JSXTransformException {
		if (engine != null && transformer != null)
			return;

		ScriptEngine nashorn = scriptEngineManager.getEngineByName("nashorn");
		try {
			nashorn.eval("var process = {env:{}}"); // node-modules expect that (react)
			nashorn.eval("var global = this;"); // react expects that
			nashorn.eval(ScriptsResolver.getScriptFromClasspath("webroots/scripts/libs/react-with-addons.min.js"));
			nashorn.eval(ScriptsResolver.getScriptFromClasspath("JSXTransformer.js"));
			transformer = (JSObject) nashorn.eval("JSXTransformer");
			engine = (Invocable) nashorn;
		} catch (ScriptException se) {
			throw new JSXTransformException(JSXTransformException.Phase.INIT, se);
		}
	}

	public String transformFile(String resourcePath) throws JSXTransformException {
		try {
			String code = ScriptsResolver.getCodeFromStaticFile(resourcePath);
			return transformCode(code);
		} catch (JSXTransformException | IOException e) {
			throw new JSXTransformException(JSXTransformException.Phase.TRANSFORM, e);
		}
	}

	public String transformCode(String code) throws JSXTransformException {
		try {
			init();
			ScriptObjectMirror jsxMirror = (ScriptObjectMirror) engine.invokeMethod(transformer, "transform", code);
			return (String) jsxMirror.getMember("code");
		} catch (JSXTransformException | ScriptException | NoSuchMethodException e) {
			throw new JSXTransformException(JSXTransformException.Phase.TRANSFORM, e);
		}
	}
}
