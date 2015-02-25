import io.vertx.serviceregistry.engines.react.ReactComponentParser;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class TestRenderComponent {
	private final static String testComponentsRoot = "C:/Dev/Tests/jsx/RootComponent.jsx";

	private ReactComponentParser parser;

	@Before
	public void createRenderComponent() {
		parser = new ReactComponentParser(testComponentsRoot);
	}

	@Test
	public void renderSimpleComponent() throws Throwable {
		Map<String, Object> props = new HashMap<String, Object>();
		props.put("test", "I'm a property !!");
		System.out.println(parser.parseComponentTree(props));
	}
}
