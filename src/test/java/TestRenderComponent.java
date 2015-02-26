import static org.junit.Assert.assertEquals;
import io.vertx.serviceregistry.engines.react.ReactComponentParser;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class TestRenderComponent {
    private final static String componentsDir = "C:/Dev/Tests/jsx/";
    private final static String workingDir = "C:/Dev/Tests/js/";
    private final static String rootComponent = "RootComponent.jsx";

    private ReactComponentParser parser;

    @Before
    public void createRenderComponent() {
        parser = new ReactComponentParser(componentsDir, workingDir, rootComponent, null);
    }

    public void renderSimpleComponent() throws Throwable {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("test", "I'm a property !!");
        System.out.println(parser.parseComponentTree(props));
    }

    @Test
    public void testCurlyBraces() {
        assertEquals("Hello Snoop, how are you ?", "Hello {name}, how are you ?".replaceAll("\\{name\\}", "Snoop"));
    }
}
