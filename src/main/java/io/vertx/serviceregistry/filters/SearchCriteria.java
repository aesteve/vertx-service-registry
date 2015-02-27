package io.vertx.serviceregistry.filters;

import io.vertx.core.http.HttpServerRequest;

/**
 * Either coming from an API a request or an http request
 * 
 * @author aesteve
 */
public class SearchCriteria {
    public String textSearch;
    public String sortBy;

    public static SearchCriteria fromPageRequest(HttpServerRequest request) {
        return new SearchCriteria();

    }

    public static SearchCriteria fromApiRequest(HttpServerRequest request) {
        return new SearchCriteria();

    }
}
