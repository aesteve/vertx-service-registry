package io.vertx.serviceregistry.dao.impl;

import io.vertx.componentdiscovery.model.TaskReport;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.serviceregistry.dao.DAO;
import io.vertx.serviceregistry.filters.SearchCriteria;
import io.vertx.serviceregistry.http.exceptions.BadRequestException;
import io.vertx.serviceregistry.http.pagination.PaginationContext;
import io.vertx.serviceregistry.io.ApiObjectMarshaller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
// it's generic on purpose
public class JsonReportDAO implements DAO<TaskReport> {
	private static List<TaskReport> reports;
	private String json;

	public JsonReportDAO(String json) {
		this.json = json;
	}

	@Override
	public void load() {
		JsonArray array = new JsonArray(json);
		reports = ApiObjectMarshaller.unmarshallReports(array);
	}

	@Override
	public List<TaskReport> getMatchingItems(SearchCriteria criteria) {
		if (criteria == null) {
			return reports;
		}
		return reports.stream().filter(report -> {
			if (criteria.getTextSearch() != null && !"".equals(criteria.getTextSearch())) {
				if (report.name().indexOf(criteria.getTextSearch()) == -1) {
					return false;
				}
			}
			return true;
		}).collect(Collectors.toList());
	}

	@Override
	public TaskReport byId(String id) {
		if (id == null) {
			return null;
		}
		Optional<TaskReport> optional = reports.stream().filter(report -> {
			return report.name().equalsIgnoreCase(id);
		}).findFirst();

		if (optional.isPresent()) {
			return optional.get();
		} else {
			return null;
		}
	}

	@Override
	public List<TaskReport> getPaginatedItems(RoutingContext context, SearchCriteria criteria) throws BadRequestException {
		PaginationContext paginationContext = (PaginationContext) context.data().get("paginationContext");
		if (paginationContext == null) {
			return getMatchingItems(criteria);
		}
		List<TaskReport> matchingArtifacts = getMatchingItems(criteria);
		paginationContext.setNbItems(matchingArtifacts.size());
		int lowerBound = paginationContext.firstItemInPage();
		if (lowerBound > matchingArtifacts.size()) {
			throw new BadRequestException("The page you requested is off limits");
		}
		int upperBound = paginationContext.lastItemInPage();
		upperBound = Math.min(upperBound, matchingArtifacts.size());
		return matchingArtifacts.subList(lowerBound, upperBound);
	}

	@Override
	public void addAll(List<TaskReport> newItems) {
		if (reports == null) {
			reports = new ArrayList<TaskReport>();
		}
		reports.addAll(newItems);
	}

	@Override
	public void replace(List<TaskReport> newItems) {
		reports = newItems;
	}

	@Override
	public void add(TaskReport newItem) {
		if (reports == null) {
			reports = new ArrayList<TaskReport>();
		}
		reports.add(newItem);
	}

	@Override
	public List<TaskReport> getAll() {
		return reports;
	}

}
