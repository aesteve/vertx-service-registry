package io.vertx.apiutils;

import io.vertx.core.Handler;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.serviceregistry.http.exceptions.BadRequestException;
import io.vertx.serviceregistry.http.pagination.PaginationContext;

public class PaginationPreProcessor implements Handler<RoutingContext> {

    /**
     * Injects the Paginator object into context's data
     * basically : context.data().put("paginator", paginator)
     * So that it's available to the next handlers (currentPage asked, items per page, etc...)
     */
    @Override
    public void handle(RoutingContext context) {
        try {
            context.data().put("paginationContext", PaginationContext.fromContext(context));
        } catch (BadRequestException be) {
            context.data().put("apiFailure", be.getStatusMessage());
            context.response().setStatusCode(be.getStatusCode());
            context.fail(be.getStatusCode());
            return;
        }
        context.next();
    }
}
