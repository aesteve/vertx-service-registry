package io.vertx.apiutils;

import io.vertx.core.Handler;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.serviceregistry.http.pagination.PaginationContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PaginationPostProcessor implements Handler<RoutingContext> {

	private final static Logger logger = Logger.getLogger("Pagination");

	@Override
	public void handle(RoutingContext context) {
		PaginationContext paginationContext = (PaginationContext) context.data().get("paginationContext");
		if (paginationContext == null) {
			logger.log(Level.WARNING, "The pagination conext is null, didn't you forget to add a PaginationPreprocessor handler ?");
			context.next();
			return;
		}
		String navLinks = paginationContext.buildLinkHeader(context.request());
		if (navLinks != null) {
			context.response().headers().add("Link", navLinks);
		}
		context.next();
	}

}
