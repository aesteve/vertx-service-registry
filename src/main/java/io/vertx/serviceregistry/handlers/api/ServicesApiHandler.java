package io.vertx.serviceregistry.handlers.api;

import io.vertx.componentdiscovery.model.Artifact;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.serviceregistry.dao.DAO;
import io.vertx.serviceregistry.filters.SearchCriteria;
import io.vertx.serviceregistry.http.exceptions.HttpException;
import io.vertx.serviceregistry.http.pagination.PaginationContext;
import io.vertx.serviceregistry.io.ApiObjectMarshaller;

import java.util.List;

import org.apache.commons.httpclient.HttpStatus;

public class ServicesApiHandler implements Handler<RoutingContext> {

    private DAO<Artifact> dao;

    public ServicesApiHandler(DAO<Artifact> dao) {
        this.dao = dao;
    }

    @Override
    public void handle(RoutingContext context) {
        String serviceId = context.request().getParam("serviceId");
        if (serviceId == null) {
            try {
                PaginationContext paginationContext = (PaginationContext) context.data().get("paginationContext");
                List<Artifact> services = dao.getPaginatedItems(paginationContext, SearchCriteria.fromApiRequest(context.request()));
                if (services != null) {
                    context.data().put("payload", ApiObjectMarshaller.marshallArtifacts(services));
                }
                context.next();
            } catch (HttpException he) {
                context.response().setStatusCode(he.getStatusCode());
                context.response().setStatusMessage(he.getStatusMessage());
                context.data().put("apiFailure", he.getStatusMessage());
                context.fail(he.getStatusCode());
            }
        } else {
            JsonObject payload = getService(context, serviceId);
            if (payload != null) {
                context.data().put("payload", payload);
                context.next();
            }
        }
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
