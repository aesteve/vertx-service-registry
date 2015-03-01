package io.vertx.serviceregistry.http.pagination;

import io.vertx.core.http.HttpServerRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Reflects the pagination state for some HttpServerRequest
 * Provides service methods to generate Link headers
 * 
 * @author aesteve
 */
public class PaginationContext {

	// TODO : (to make it a generic service)
	// That's some configuration stuff, should not be found in an instanciated object (reflecting one request)
	// -> move it in config or let the user define it (set/get)
	public static final String CURRENT_PAGE_QUERY_PARAM = "page";
	public static final String PER_PAGE_QUERY_PARAM = "perPage";
	public static final Integer DEFAULT_PER_PAGE = 30;
	public static final Integer MAX_PER_PAGE = 100;

	private Integer pageAsked = 1; // ofc
	private Integer itemsPerPage = DEFAULT_PER_PAGE; // ofc too
	private Integer totalPages; // will be set once the request is processed

	public PaginationContext(Integer pageAsked, Integer itemsPerPage) {
		if (pageAsked != null)
			this.pageAsked = pageAsked;
		if (itemsPerPage != null)
			this.itemsPerPage = itemsPerPage;
	}

	public Integer getPageAsked() {
		return pageAsked;
	}

	public Integer getItemsPerPage() {
		return itemsPerPage;
	}

	public Integer getTotalPages() {
		return totalPages;
	}

	public void setNbItems(Integer nbTotalItems) {
		this.totalPages = nbTotalItems / itemsPerPage;
		int modulo = nbTotalItems % itemsPerPage;
		if (modulo > 0) {
			this.totalPages = this.totalPages + 1;
		}
	}

	public boolean hasMorePages() {
		return totalPages > pageAsked;
	}

	public int firstItemInPage() {
		return itemsPerPage * (pageAsked - 1);
	}

	public int lastItemInPage() {
		return firstItemInPage() + itemsPerPage;
	}

	public String buildLinkHeader(HttpServerRequest request) {
		List<String> links = getNavLinks(request);
		if (links == null) {
			return null;
		}
		String s = String.join(", ", links);
		return s;
	}

	public List<String> getNavLinks(HttpServerRequest request) {
		if (totalPages == null || totalPages == 1) {
			return null;
		}
		List<String> links = new ArrayList<String>();
		if (pageAsked > 1) {
			links.add(pageUrl(request, 1, "first"));
			links.add(pageUrl(request, pageAsked - 1, "prev"));
		}
		if (pageAsked < totalPages) {
			links.add(pageUrl(request, totalPages, "last"));
			links.add(pageUrl(request, pageAsked + 1, "next"));
		}
		return links;
	}

	// FIXME : refactor that in a cleaner way once I'm not tired as hell
	private String pageUrl(HttpServerRequest request, int pageNum, String rel) {
		StringBuilder sb = new StringBuilder("<");
		String url = request.absoluteURI();
		if (request.params().isEmpty()) {
			url += "?" + CURRENT_PAGE_QUERY_PARAM + "=" + pageNum;
			url += "&" + PER_PAGE_QUERY_PARAM + "=" + itemsPerPage;
		} else {
			if (url.indexOf(CURRENT_PAGE_QUERY_PARAM + "=") > url.indexOf("?")) {
				url = url.replaceAll(CURRENT_PAGE_QUERY_PARAM + "=([^&]+)", CURRENT_PAGE_QUERY_PARAM + "=" + pageNum);
			} else {
				url += "&" + CURRENT_PAGE_QUERY_PARAM + "=" + pageNum;
			}
			if (url.indexOf("&" + PER_PAGE_QUERY_PARAM) == -1 && url.indexOf("?" + PER_PAGE_QUERY_PARAM) == -1) {
				url += "&" + PER_PAGE_QUERY_PARAM + "=" + itemsPerPage;
			}
		}
		sb.append(url);
		sb.append(">; ");
		sb.append("rel=\"" + rel + "\"");
		return sb.toString();

	}
}
