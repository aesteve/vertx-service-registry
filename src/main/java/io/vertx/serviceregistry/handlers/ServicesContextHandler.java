package io.vertx.serviceregistry.handlers;

import java.util.List;

import io.vertx.core.Handler;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.serviceregistry.dao.ArtifactsDAO;
import io.vertx.serviceregistry.filters.SearchCriteria;
import io.vertx.serviceregistry.http.exceptions.HttpException;
import io.vertx.serviceregistry.http.pagination.PaginationContext;
import io.vertx.serviceregistry.io.ArtifactsMarshaller;
import io.vertx.serviceregistry.model.Artifact;

public class ServicesContextHandler implements Handler<RoutingContext> {

    private ArtifactsDAO dao;

    public ServicesContextHandler(ArtifactsDAO dao) {
        this.dao = dao;
    }

    @Override
    public void handle(RoutingContext context) {
        SearchCriteria criteria = SearchCriteria.fromPageRequest(context.request());
        context.put("filters", criteria);
        try {
            PaginationContext paginationContext = PaginationContext.fromContext(context);
            context.put("paginationContext", paginationContext);
            List<Artifact> artifacts = dao.getPaginatedArtifacts(context, criteria);
            context.put("services", ArtifactsMarshaller.marshall(artifacts));
        } catch (HttpException he) {
            context.response().setStatusCode(he.getStatusCode());
            context.response().setStatusMessage(he.getStatusMessage());
            context.fail(he.getStatusCode());
            return;
        }
        context.next();
    }
}
