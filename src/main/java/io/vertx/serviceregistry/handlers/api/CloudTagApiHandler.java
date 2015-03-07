package io.vertx.serviceregistry.handlers.api;

import io.vertx.componentdiscovery.model.Artifact;
import io.vertx.core.Handler;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.serviceregistry.dao.DAO;
import io.vertx.serviceregistry.filters.SearchCriteria;
import io.vertx.serviceregistry.http.exceptions.BadRequestException;
import io.vertx.serviceregistry.http.pagination.PaginationContext;
import io.vertx.serviceregistry.io.ApiObjectMarshaller;
import io.vertx.serviceregistry.model.WordAndWeight;

import java.util.Collection;

public class CloudTagApiHandler implements Handler<RoutingContext> {

	private DAO<Artifact> artifactsDAO;

	public CloudTagApiHandler(DAO<Artifact> artifactsDAO) {
		this.artifactsDAO = artifactsDAO;
	}

	@Override
	public void handle(RoutingContext context) {
		try {
			PaginationContext paginationContext = (PaginationContext) context.get("paginationContext");
			SearchCriteria criteria = (SearchCriteria) context.get("filters");
			Collection<WordAndWeight> words = artifactsDAO.getWords(paginationContext, criteria);
			context.data().put("payload", ApiObjectMarshaller.marshallCloud(words));
		} catch (BadRequestException bre) {
			context.response().setStatusCode(bre.getStatusCode());
			context.response().setStatusMessage(bre.getStatusMessage());
			context.data().put("apiFailure", bre.getStatusMessage());
			context.fail(bre.getStatusCode());
			return;
		}
		context.next();
	}

}
