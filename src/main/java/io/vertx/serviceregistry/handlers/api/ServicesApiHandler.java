package io.vertx.serviceregistry.handlers.api;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.serviceregistry.dao.ArtifactsDAO;
import io.vertx.serviceregistry.filters.SearchCriteria;
import io.vertx.serviceregistry.http.exceptions.HttpException;
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
            try {
                List<Artifact> services = dao.getPaginatedArtifacts(context, SearchCriteria.fromApiRequest(context.request()));
                if (services != null) {
                    context.data().put("payload", new JsonArray(services));
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
