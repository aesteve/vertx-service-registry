package io.vertx.serviceregistry.handlers;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.serviceregistry.factory.ArtifactsFactory;

import java.util.HashMap;
import java.util.Map;

public class ServicesContextHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext context) {
        HttpServerRequest request = context.request();
        context.put("services", ArtifactsFactory.artifacts);
        Map<String, Object> filters = new HashMap<String, Object>();
        String textSearch = request.getParam("search");
        filters.put("textSearch", textSearch);
        context.put("filters", filters);
        context.next();
    }
}
