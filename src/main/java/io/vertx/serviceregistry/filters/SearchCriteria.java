package io.vertx.serviceregistry.filters;

import io.vertx.core.http.HttpServerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Either coming from an API a request or an http request
 * 
 * @author aesteve
 */
public class SearchCriteria {
	public String textSearch;
	public String sortBy;
	public List<String> tags;

	private SearchCriteria() {

	}

	public static SearchCriteria fromPageRequest(HttpServerRequest request) {
		return null;
	}

	public static SearchCriteria fromApiRequest(HttpServerRequest request) {
		SearchCriteria criteria = new SearchCriteria();
		criteria.textSearch = request.getParam("q");
		String requestTags = request.getParam("tags");
		criteria.tags = new ArrayList<String>();
		if (requestTags != null) {
			StringTokenizer tokenizer = new StringTokenizer(requestTags, ",");
			while (tokenizer.hasMoreTokens()) {
				criteria.tags.add(tokenizer.nextToken());
			}
		}
		return criteria;
	}
}
