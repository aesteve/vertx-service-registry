package io.vertx.serviceregistry.handlers.api;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.serviceregistry.dao.ArtifactsDAO;
import io.vertx.serviceregistry.filters.SearchCriteria;
import io.vertx.serviceregistry.http.pagination.PaginationContext;
import io.vertx.serviceregistry.model.Artifact;

import java.util.List;

import org.apache.commons.httpclient.HttpStatus;

public class ServicesApiHandler implements Handler<RoutingContext> {

	private ArtifactsDAO dao;

	public ServicesApiHandler(ArtifactsDAO dao) {
		this.dao = dao;
	}

	@Override
	public void handle(RoutingContext context) {
		String serviceId = context.request().getParam("serviceId");
		if (serviceId == null) {
			JsonArray payload = getServices(context, SearchCriteria.fromApiRequest(context.request()));
			if (payload != null) {
				context.data().put("payload", payload);
				context.next();
			}
		} else {
			JsonObject payload = getService(context, serviceId);
			if (payload != null) {
				context.data().put("payload", payload);
				context.next();
			}
		}
	}

	private JsonArray getServices(RoutingContext context, SearchCriteria criteria) {
		PaginationContext paginationContext = (PaginationContext) context.data().get("paginationContext");
		if (paginationContext == null) {
			return new JsonArray(dao.getMatchingArtifacts(criteria));
		}
		List<Artifact> matchingArtifacts = dao.getMatchingArtifacts(criteria);
		paginationContext.setNbItems(matchingArtifacts.size());
		int lowerBound = paginationContext.firstItemInPage();
		if (lowerBound > matchingArtifacts.size()) {
			context.data().put("apiFailure", "The page you requested is off limits");
			context.response().setStatusCode(HttpStatus.SC_BAD_REQUEST);
			context.fail(HttpStatus.SC_BAD_REQUEST);
			return null;
		}
		int upperBound = paginationContext.lastItemInPage();
		upperBound = Math.min(upperBound, matchingArtifacts.size());
		List<Artifact> payload = matchingArtifacts.subList(lowerBound, upperBound);
		return new JsonArray(payload);
	}

	private JsonObject getService(RoutingContext context, String serviceId) {
		Artifact artifact = dao.byId(serviceId);
		if (artifact == null) {
			context.data().put("apiFailure", "Service can't be found");
			context.response().setStatusCode(HttpStatus.SC_NOT_FOUND);
			context.fail(HttpStatus.SC_NOT_FOUND);
			return null;
		}
		return artifact.toJsonObject();
	}
}
