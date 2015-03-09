package io.vertx.serviceregistry;

import io.vertx.componentdiscovery.impl.TaskAsyncResult;
import io.vertx.componentdiscovery.model.Artifact;
import io.vertx.componentdiscovery.model.TaskReport;
import io.vertx.componentdiscovery.utils.EbAddresses;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.serviceregistry.dao.DAO;
import io.vertx.serviceregistry.engines.react.ReactComponentParser;
import io.vertx.serviceregistry.fileutils.StaticTemplatesRegistry;
import io.vertx.serviceregistry.handlers.SockJSFactory;
import io.vertx.serviceregistry.http.exceptions.BadRequestException;
import io.vertx.serviceregistry.http.pagination.PaginationContext;
import io.vertx.serviceregistry.io.ApiObjectMarshaller;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import org.apache.commons.io.Charsets;

import com.google.common.io.Resources;

public class PageGenerator implements Verticle {

	private static final Logger log = Logger.getLogger("FILE");

	private final String htmlTpl = "server-index.html";

	private Vertx vertx;
	private DAO<TaskReport> reportsDAO;
	private DAO<Artifact> artifactsDAO;
	private StaticTemplatesRegistry tplRegistry;

	public PageGenerator(DAO<Artifact> artifactsDAO, DAO<TaskReport> reportDAO, StaticTemplatesRegistry tplRegistry) {
		this.reportsDAO = reportDAO;
		this.artifactsDAO = artifactsDAO;
		this.tplRegistry = tplRegistry;
	}

	@Override
	public Vertx getVertx() {
		return vertx;
	}

	@Override
	public void init(Vertx vertx, Context context) {
		this.vertx = vertx;
	}

	@Override
	public void start(Future<Void> future) throws Exception {
		vertx.eventBus().consumer(EbAddresses.PAGE_GENERATOR.toString(), message -> {
			generateForCurrentServices(pageGeneratorHandler -> {
				reportsDAO.add(pageGeneratorHandler.result());
				System.out.println("Page generation finished");
			});
		});
		future.complete();
	}

	@Override
	public void stop(Future<Void> future) throws Exception {
		future.complete();
	}

	private void generateForCurrentServices(Handler<TaskAsyncResult> onceDone) {
		TaskReport global = new TaskReport("Generate static pages");
		global.start();
		if (artifactsDAO.getAll() == null) {
			global.terminate(0);
			onceDone.handle(new TaskAsyncResult(global));
			return;
		}
		int perPage = PaginationContext.DEFAULT_PER_PAGE;
		int nbPages = artifactsDAO.getAll().size() / perPage + 1;
		global.setTotalTasks(nbPages);
		// prepare report
		for (int i = 1; i <= nbPages; i++) {
			TaskReport pageReport = new TaskReport("Page number : " + i);
			pageReport.setTotalTasks(1);
			global.addTask(pageReport);
		}
		for (int i = 1; i <= nbPages; i++) {
			ReactComponentParser parser = new ReactComponentParser(Config.get().getJSServerBundle());
			TaskReport pageReport = global.subTasks().get(i - 1);
			pageReport.start();
			SockJSFactory.notifyClients(global.toJsonObject()); // progress
			PaginationContext paginationContext = new PaginationContext(i, null);
			Map<String, Object> props = new HashMap<String, Object>();
			props.put("paginationContext", paginationContext);
			String staticHtml;
			try {
				List<Artifact> services = artifactsDAO.getPaginatedItems(paginationContext, null);
				props.put("services", ApiObjectMarshaller.marshallArtifacts(services));
				StringJoiner joiner = new StringJoiner(" , ");
				services.forEach(service -> {
					joiner.add(service.toJsonObject().toString());
				});
				staticHtml = joiner.toString();
				// parser.parseComponentTree(props); // doesn't work on OpenShift : crashes cartridge
				URL url = Resources.getResource(htmlTpl);
				String fullPage = Resources.toString(url, Charsets.UTF_8);
				fullPage = fullPage.replaceAll("\\{content\\}", Matcher.quoteReplacement(staticHtml));
				fullPage = fullPage.replaceAll("\\{scripts\\}", getScriptTags());
				pageReport.progress();
				tplRegistry.store("services", paginationContext, fullPage);
				global.progress();
			} catch (/* ComponentParsingException | */BadRequestException | IOException e) {
				log.log(Level.SEVERE, "Failed : ", e);
				System.out.println("FAILED : " + e.getMessage());
				pageReport.fail(e);
				break;
			}
		}
		global.terminate(global.subTasks().size());
		SockJSFactory.notifyClients(global.toJsonObject()); // with finished status
		onceDone.handle(new TaskAsyncResult(global));
	}

	/**
	 * TODO : mutualize between this file and ReactTemplateEngine (and there is a lot of other stuff to mutualize too)
	 */
	private String getScriptTags() {
		return "<script src=\"/assets/scripts/browser-bundle.js\" charset=\"UTF-8\" async></script>";
	}

}
