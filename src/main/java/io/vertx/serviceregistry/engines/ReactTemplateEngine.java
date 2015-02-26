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
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.apache.commons.io.Charsets;

import com.google.common.io.Resources;

public class ReactTemplateEngine implements TemplateEngine {

    private final String componentsDir;
    private final String workDir;
    private final String htmlTpl;
    private final Collection<JSLibrary> customLibs;

    public ReactTemplateEngine(String htmlTpl, String componentsDir, Collection<JSLibrary> customLibs) {
        this(htmlTpl, componentsDir, null, customLibs);
    }

    /**
     * Creates a template engine
     * 
     * @param htmlTpl the htmlTemplate that will be decorated by server-side rendered webapp + js libs for client-side rendering
     * @param componentsDir the directory containing "raw" components (jsx files)
     * @param workDir the directory within the templateEngine will generate js files (dev purposes only)
     */
    public ReactTemplateEngine(String htmlTpl, String componentsDir, String workDir, Collection<JSLibrary> customLibs) {
        this.componentsDir = componentsDir;
        if (workDir == null)
            this.workDir = componentsDir;
        else
            this.workDir = workDir;
        this.htmlTpl = htmlTpl;
        this.customLibs = customLibs;
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
        // TODO : this is only for dev pipeline : instanciates a new parser for
        // every request
        // TODO : make it a whole pipeline thing : just render from a minified
        // js file in prod env + store generated html to cache
        // TODO : make it async : do not block everything
        List<String> customLibsPaths = customLibs.stream().map(lib -> lib.getPathOnClasspath()).collect(Collectors.toList());
        ReactComponentParser parser = new ReactComponentParser(componentsDir, workDir, rootComponent, customLibsPaths);
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

        // include libs for client-side rendering
        fullPage = fullPage.replaceAll("\\{scripts\\}", getScriptTags());
        return fullPage;
    }

    private String getScriptTags() {
        if (customLibs == null)
            return "";
        List<String> scriptTags = customLibs.stream().map(jsLib -> jsLib.asScriptTag(false)).collect(Collectors.toList());
        StringJoiner joiner = new StringJoiner("\n");
        scriptTags.forEach(scriptTag -> {
            joiner.add(scriptTag);
        });
        return joiner.toString();

    }
}
