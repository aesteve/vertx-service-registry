package io.vertx.serviceregistry.handlers.api;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.serviceregistry.factory.ArtifactsFactory;
import io.vertx.serviceregistry.http.pagination.PaginationContext;
import io.vertx.serviceregistry.model.Artifact;

import java.util.List;

import org.apache.commons.httpclient.HttpStatus;

public class ServicesApiHandler implements Handler<RoutingContext> {

	@Override
	public void handle(RoutingContext context) {
		PaginationContext paginationContext = (PaginationContext) context.data().get("paginationContext");
		if (paginationContext == null) {
			context.data().put("payload", new JsonArray(ArtifactsFactory.artifacts));
			context.next();
			return;
		}
		List<Artifact> matchingArtifacts = ArtifactsFactory.getMatchingArtifacts(null);
		paginationContext.setNbItems(matchingArtifacts.size());
		int lowerBound = paginationContext.firstItemInPage();
		if (lowerBound > matchingArtifacts.size()) {
			context.data().put("apiFailure", "The page you requested is off limits");
			context.response().setStatusCode(HttpStatus.SC_BAD_REQUEST);
			context.fail(HttpStatus.SC_BAD_REQUEST);
			return;
		}
		int upperBound = paginationContext.lastItemInPage();
		upperBound = Math.min(upperBound, matchingArtifacts.size());
		List<Artifact> payload = matchingArtifacts.subList(lowerBound, upperBound);
		System.out.println("payload size : " + lowerBound + " -> " + upperBound);
		context.data().put("payload", new JsonArray(payload));
		context.next();
	}
}
