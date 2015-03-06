package io.vertx.serviceregistry.handlers.api;

import io.vertx.componentdiscovery.model.TaskReport;
import io.vertx.core.Handler;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.serviceregistry.dao.DAO;
import io.vertx.serviceregistry.filters.SearchCriteria;
import io.vertx.serviceregistry.http.exceptions.HttpException;
import io.vertx.serviceregistry.http.pagination.PaginationContext;
import io.vertx.serviceregistry.io.ApiObjectMarshaller;

import java.util.List;

public class ReportApiHandler implements Handler<RoutingContext> {

    private DAO<TaskReport> dao;

    public ReportApiHandler(DAO<TaskReport> dao) {
        this.dao = dao;
    }

    @Override
    public void handle(RoutingContext context) {
        try {
            PaginationContext paginationContext = (PaginationContext) context.data().get("paginationContext");
            List<TaskReport> reports = dao.getPaginatedItems(paginationContext, SearchCriteria.fromApiRequest(context.request()));
            if (reports != null) {
                context.data().put("payload", ApiObjectMarshaller.marshallReports(reports));
            }
            context.next();
        } catch (HttpException he) {
            context.response().setStatusCode(he.getStatusCode());
            context.response().setStatusMessage(he.getStatusMessage());
            context.data().put("apiFailure", he.getStatusMessage());
            context.fail(he.getStatusCode());
        }

    }

}
