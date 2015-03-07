package io.vertx.serviceregistry.engines.react;

import io.vertx.core.json.JsonArray;
import io.vertx.serviceregistry.engines.react.exceptions.ComponentParsingException;
import io.vertx.serviceregistry.fileutils.ScriptsResolver;
import io.vertx.serviceregistry.http.pagination.PaginationContext;

import java.io.FileNotFoundException;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ReactComponentParser {
	private ScriptEngine nashorn;
	private String jsBundle;
	private Bindings bindings;

	private ScriptEngineManager scriptEngineManager;

	public ReactComponentParser(String jsBundle) {
		this.jsBundle = jsBundle;
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
		bindings.put("process", "{}");
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
			JsonArray list = (JsonArray) props.get("services");
			bindings.put("nashorn_services", list);
			PaginationContext paginationContext = (PaginationContext) props.get("paginationContext");
			bindings.put("paginationContext", paginationContext.toJsonObject());
			nashorn.eval(ScriptsResolver.getScriptReaderFromFile(jsBundle));
			return bindings.get("reactRenderedResult").toString();
		} catch (FileNotFoundException | ScriptException e) {
			throw new ComponentParsingException(e);
		}
	}
}
