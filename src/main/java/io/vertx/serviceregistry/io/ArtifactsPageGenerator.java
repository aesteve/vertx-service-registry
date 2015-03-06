package io.vertx.serviceregistry.io;

import io.vertx.componentdiscovery.model.Artifact;
import io.vertx.componentdiscovery.model.TaskReport;
import io.vertx.core.Vertx;
import io.vertx.serviceregistry.dao.DAO;
import io.vertx.serviceregistry.engines.react.ReactComponentParser;
import io.vertx.serviceregistry.engines.react.exceptions.ComponentParsingException;
import io.vertx.serviceregistry.handlers.SockJSFactory;
import io.vertx.serviceregistry.http.exceptions.BadRequestException;
import io.vertx.serviceregistry.http.pagination.PaginationContext;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import org.apache.commons.io.Charsets;

import com.google.common.io.Resources;

public class ArtifactsPageGenerator {
    private DAO<Artifact> dao;
    private Vertx vertx;

    private static final Logger log = Logger.getLogger("FILE");

    private final String htmlTpl = "server-index.html";

    public ArtifactsPageGenerator(Vertx vertx, DAO<Artifact> dao) {
        this.dao = dao;
        this.vertx = vertx;
    }

    public void generateForCurrentServices(Consumer<TaskReport> onceDone) {
        TaskReport global = new TaskReport("Generate static pages");
        global.start();
        if (dao.getAll() == null) {
            global.terminate(0);
            onceDone.accept(global);
            return;
        }
        int perPage = PaginationContext.DEFAULT_PER_PAGE;
        int nbPages = dao.getAll().size() / perPage + 1;
        global.setTotalTasks(nbPages);
        // prepare report
        for (int i = 1; i <= nbPages; i++) {
            TaskReport pageReport = new TaskReport("Page number : " + i);
            pageReport.setTotalTasks(1);
            global.addTask(pageReport);

        }
        for (int i = 1; i <= nbPages; i++) {
            ReactComponentParser parser = new ReactComponentParser("sites/scripts/server-bundle.js");
            TaskReport pageReport = global.subTasks().get(i - 1);
            pageReport.start();
            SockJSFactory.notifyClients(global.toJsonObject()); // progress
            PaginationContext paginationContext = new PaginationContext(i, null);
            Map<String, Object> props = new HashMap<String, Object>();
            props.put("paginationContext", paginationContext);
            String staticHtml;
            try {
                props.put("services", ApiObjectMarshaller.marshallArtifacts(dao.getPaginatedItems(paginationContext, null)));
                staticHtml = parser.parseComponentTree(props);
                URL url = Resources.getResource(htmlTpl);
                String fullPage = Resources.toString(url, Charsets.UTF_8);
                fullPage = fullPage.replaceAll("\\{content\\}", Matcher.quoteReplacement(staticHtml));
                fullPage = fullPage.replaceAll("\\{scripts\\}", getScriptTags());
                pageReport.terminate(1, null);
                global.progress();
            } catch (ComponentParsingException | BadRequestException | IOException e) {
                log.log(Level.SEVERE, "Failed : ", e);
                System.out.println("FAILED : " + e.getMessage());
                pageReport.fail(e);
                break;
            }
        }
        global.terminate(global.subTasks().size());
        SockJSFactory.notifyClients(global.toJsonObject()); // with finished status
        onceDone.accept(global);
    }

    /**
     * TODO : mutualize between this file and ReactTemplateEngine (and there is a lot of other stuff to mutualize too
     */
    private String getScriptTags() {
        return "<script src=\"/assets/scripts/browser-bundle.js\" charset=\"UTF-8\" async></script>";
    }
}
