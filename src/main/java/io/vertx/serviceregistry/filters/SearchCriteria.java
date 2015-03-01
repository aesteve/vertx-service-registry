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
	private String textSearch;
	private String sortBy;
	private List<String> tags;

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

	@Override
	public String toString() {
		return "criteria : [textSearch:" + textSearch + ", tags:" + tags + ",sortBy:" + sortBy + "]";
	}

	/**
	 * @return the textSearch
	 */
	public String getTextSearch() {
		return textSearch;
	}

	/**
	 * @param textSearch the textSearch to set
	 */
	public void setTextSearch(String textSearch) {
		this.textSearch = textSearch;
	}

	/**
	 * @return the sortBy
	 */
	public String getSortBy() {
		return sortBy;
	}

	/**
	 * @param sortBy the sortBy to set
	 */
	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	/**
	 * @return the tags
	 */
	public List<String> getTags() {
		return tags;
	}

	/**
	 * @param tags the tags to set
	 */
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
}
