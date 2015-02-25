package io.vertx.serviceregistry.engines.react;

import io.vertx.core.json.JsonObject;
import io.vertx.serviceregistry.engines.react.exceptions.ComponentParsingException;
import io.vertx.serviceregistry.engines.react.exceptions.JSXTransformException;
import io.vertx.serviceregistry.fileutils.ScriptsResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.FilenameUtils;

public class ReactComponentParser {
	private ScriptEngine nashorn;
	private ScriptEngineManager scriptEngineManager;
	private JSXTransformer transformer;
	private String rootComponentPath;
	private Bindings bindings;

	public ReactComponentParser(String rootComponentPath) {
		scriptEngineManager = new ScriptEngineManager();
		this.rootComponentPath = rootComponentPath;
	}

	public void init() throws ScriptException {
		if (nashorn != null && transformer != null)
			return;

		transformer = new JSXTransformer();

		nashorn = scriptEngineManager.getEngineByName("nashorn");
		nashorn.eval("var process = {env:{}}"); // node-modules expect that (react)
		nashorn.eval("var global = this;"); // react expects that
		ScriptContext ctx = nashorn.getContext();
		bindings = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
		bindings.put("reactRenderedResult", "");
		bindings.put("workingDir", rootComponentPath);
		nashorn.eval(ScriptsResolver.getScriptFromClasspath("jvm-npm.js")); // add require
		nashorn.eval(ScriptsResolver.getScriptFromClasspath("console.js"));
		nashorn.eval(ScriptsResolver.getScriptFromClasspath("webroots/scripts/libs/react-with-addons.min.js"));
	}

	public String parseComponentTree(Map<String, Object> props) throws ComponentParsingException {
		return parseComponentTree(new JsonObject(props));
	}

	public String parseComponentTree(JsonObject props) throws ComponentParsingException {
		try {
			init();
		} catch (ScriptException se) {
			throw new ComponentParsingException(se);
		}

		File componentFile = new File(rootComponentPath);
		if (!isJSXFile(componentFile)) {
			throw new ComponentParsingException("Expecting a jsx file for" + rootComponentPath);
		}

		parseComponentFile(componentFile, true, props);

		File parent = componentFile.getParentFile();

		for (File f : parent.listFiles()) {
			if (isJSXFile(f) && !f.equals(componentFile)) {
				parseComponentFile(f, false, null);
			}
		}

		String jsPath = getJSPath(componentFile);
		try {
			nashorn.eval(ScriptsResolver.getScriptReaderFromFile(jsPath));
			return (String) bindings.get("reactRenderedResult");
		} catch (FileNotFoundException | ScriptException e) {
			throw new ComponentParsingException(e);
		}
	}

	private void parseComponentFile(File file, boolean isRootComponent, JsonObject props) throws ComponentParsingException {
		if (!isJSXFile(file) || !file.isFile())
			return;
		String filePath = file.getAbsolutePath();
		try {
			String componentCode = transformer.transformFile(filePath);
			if (isRootComponent) {
				componentCode += "\n";
				componentCode += "var __rcp__component = module.exports;\n";
				componentCode += "var __rcp__factory = React.createFactory(__rcp__component);\n";
				String propsStr = props == null ? "{}" : props.toString();
				componentCode += "var __rcp__componentInstance = __rcp__factory(" + propsStr + ");\n";
				componentCode += "reactRenderedResult = React.renderToString(__rcp__componentInstance);\n";
			}
			ScriptsResolver.writeScriptToPath(componentCode, getJSPath(file));

		} catch (JSXTransformException | IOException e) {
			throw new ComponentParsingException(e);
		}
	}

	private boolean isJSXFile(File f) {
		return f.exists() && f.getAbsolutePath().endsWith(".jsx");

	}

	private String getJSPath(File componentFile) {
		String filePath = componentFile.getAbsolutePath();
		return componentFile.getParentFile().getAbsolutePath() + "/" + FilenameUtils.getBaseName(filePath) + ".js";
	}
}
