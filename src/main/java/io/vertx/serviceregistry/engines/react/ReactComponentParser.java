package io.vertx.serviceregistry.engines.react;

import io.vertx.core.json.JsonArray;
import io.vertx.serviceregistry.engines.react.exceptions.ComponentParsingException;
import io.vertx.serviceregistry.fileutils.ScriptsResolver;

import java.io.FileNotFoundException;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ReactComponentParser {
	private ScriptEngine nashorn;
	private String rootComponentFile;
	private Bindings bindings;

	private ScriptEngineManager scriptEngineManager;

	public ReactComponentParser(String rootComponentFile) {
		this.rootComponentFile = rootComponentFile;
		scriptEngineManager = new ScriptEngineManager();
	}

	public void init() throws ScriptException {
		if (nashorn != null) {
			return;
		}
		nashorn = scriptEngineManager.getEngineByName("nashorn");
		ScriptContext ctx = nashorn.getContext();
		bindings = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
		bindings.put("reactRenderedResult", "");
		bindings.put("console", "{}");
		bindings.put("utils", "{}");
		nashorn.eval(ScriptsResolver.getScriptFromClasspath("console.js"));
	}

	public String parseComponentTree(Map<String, Object> props) throws ComponentParsingException {
		try {
			init();
		} catch (ScriptException se) {
			throw new ComponentParsingException(se);
		}

		try {
			nashorn.eval("initialProps = {}");
			System.out.println(props.get("services").getClass());
			JsonArray array = (JsonArray) props.get("services");
			bindings.put("services", array.encode());
			nashorn.eval("initialProps.services = JSON.parse(services);");
			nashorn.eval(ScriptsResolver.getScriptReaderFromFile(rootComponentFile));
			return (String) bindings.get("reactRenderedResult");
		} catch (FileNotFoundException | ScriptException e) {
			throw new ComponentParsingException(e);
		}
	}
}
