package io.vertx.serviceregistry.handlers;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.serviceregistry.dao.ArtifactsDAO;

import java.util.HashMap;
import java.util.Map;

public class ServicesContextHandler implements Handler<RoutingContext> {

	private ArtifactsDAO dao;

	public ServicesContextHandler(ArtifactsDAO dao) {
		this.dao = dao;
	}

	@Override
	public void handle(RoutingContext context) {
		HttpServerRequest request = context.request();
		// context.put("services", ArtifactsMarshaller.marshall(dao.getMatchingArtifacts(null)));
		Map<String, Object> filters = new HashMap<String, Object>();
		String textSearch = request.getParam("search");
		filters.put("textSearch", textSearch);
		context.put("filters", filters);
		context.next();
	}
}
