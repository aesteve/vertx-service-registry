package io.vertx.serviceregistry.handlers;

import io.vertx.componentdiscovery.model.Artifact;
import io.vertx.core.Handler;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.serviceregistry.dao.DAO;
import io.vertx.serviceregistry.filters.SearchCriteria;
import io.vertx.serviceregistry.http.exceptions.HttpException;
import io.vertx.serviceregistry.http.pagination.PaginationContext;
import io.vertx.serviceregistry.io.ApiObjectMarshaller;

import java.util.List;

public class ServicesContextHandler implements Handler<RoutingContext> {

	private DAO<Artifact> dao;

	public ServicesContextHandler(DAO<Artifact> dao) {
		this.dao = dao;
	}

	@Override
	public void handle(RoutingContext context) {
		SearchCriteria criteria = SearchCriteria.fromPageRequest(context.request());
		context.put("filters", criteria);
		try {
			PaginationContext paginationContext = PaginationContext.fromContext(context);
			context.put("paginationContext", paginationContext);
			List<Artifact> artifacts = dao.getPaginatedItems(context, criteria);
			context.put("services", ApiObjectMarshaller.marshallArtifacts(artifacts));
		} catch (HttpException he) {
			context.response().setStatusCode(he.getStatusCode());
			context.response().setStatusMessage(he.getStatusMessage());
			context.fail(he.getStatusCode());
			return;
		}
		context.next();
	}
}
