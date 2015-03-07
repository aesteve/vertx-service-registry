import java.io.FileReader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.junit.Test;

public class ServerSideRendering {
	@Test
	public void renderServer() throws Exception {
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("nashorn");
		ScriptContext ctx = engine.getContext();
		Bindings bindings = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
		bindings.put("reactRenderedResult", "");
		bindings.put("console", "{}");
		bindings.put("initialProps", "{services:[]}");
		engine.eval(new FileReader("src/main/resources/console.js"));
		engine.eval(new FileReader("site/scripts/server-bundle.js"));
	}
}