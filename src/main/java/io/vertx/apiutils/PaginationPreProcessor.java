package io.vertx.apiutils;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.serviceregistry.http.pagination.PaginationContext;

import org.apache.commons.httpclient.HttpStatus;

public class PaginationPreProcessor implements Handler<RoutingContext> {

	/**
	 * Injects the Paginator object into context's data
	 * basically : context.data().put("paginator", paginator)
	 * So that it's available to the next handlers (currentPage asked, items per page, etc...)
	 */
	@Override
	public void handle(RoutingContext context) {
		HttpServerRequest request = context.request();
		String pageStr = request.getParam(PaginationContext.CURRENT_PAGE_QUERY_PARAM);
		String perPageStr = request.getParam(PaginationContext.PER_PAGE_QUERY_PARAM);
		Integer page = null;
		Integer perPage = null;
		try {
			if (pageStr != null) {
				page = Integer.parseInt(pageStr);
			}
			if (perPageStr != null) {
				perPage = Integer.parseInt(perPageStr);
			}
		} catch (NumberFormatException e) {
			System.out.println("Failing with status code");
			context.data().put("apiFailure", "Invalid pagination parameters : expecting integers");
			context.response().setStatusCode(HttpStatus.SC_BAD_REQUEST);
			context.fail(HttpStatus.SC_BAD_REQUEST);
			return;
		}
		if (perPage != null && perPage > PaginationContext.MAX_PER_PAGE) {
			context.data().put("apiFailure", "Invalid " + PaginationContext.PER_PAGE_QUERY_PARAM + " parameter, max is " + PaginationContext.MAX_PER_PAGE);
			context.response().setStatusCode(HttpStatus.SC_BAD_REQUEST);
			context.fail(HttpStatus.SC_BAD_REQUEST);
			return;
		}
		PaginationContext paginationContext = new PaginationContext(page, perPage);
		context.data().put("paginationContext", paginationContext);
		context.next();
	}

}
