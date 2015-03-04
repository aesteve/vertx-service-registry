package io.vertx.serviceregistry.dao.impl;

import io.vertx.componentdiscovery.model.Artifact;
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

public class JsonArtifactsDAO implements DAO<Artifact> {
	private static List<Artifact> artifacts;
	private String json;

	public JsonArtifactsDAO(String json) {
		this.json = json;
	}

	@Override
	public void load() {
		JsonArray array = new JsonArray(json);
		artifacts = ApiObjectMarshaller.unmarshallArtifacts(array);
	}

	/**
	 * This method belongs to the DAO since it could query something
	 * (not in JSON implementation but in an SQL one for instance)
	 */
	@Override
	public List<Artifact> getMatchingItems(final SearchCriteria criteria) {
		if (criteria == null) {
			return artifacts;
		}
		return artifacts.stream().filter(artifact -> {
			if (criteria.getTextSearch() != null && !"".equals(criteria.getTextSearch())) {
				if (artifact.fullId().indexOf(criteria.getTextSearch()) == -1) {
					return false;
				}
			}
			if (criteria.getTags() != null && criteria.getTags().size() > 0) {
				if (artifact.getTags() == null || artifact.getTags().size() == 0) {
					return false;
				}
				// if the intersection of both list is not empty
				return criteria.getTags().retainAll(artifact.getTags());
			}
			return true;
		}).collect(Collectors.toList());
	}

	@Override
	public List<Artifact> getPaginatedItems(RoutingContext context, SearchCriteria criteria) throws BadRequestException {
		PaginationContext paginationContext = (PaginationContext) context.data().get("paginationContext");
		if (paginationContext == null) {
			return getMatchingItems(criteria);
		}
		List<Artifact> matchingArtifacts = getMatchingItems(criteria);
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
	public Artifact byId(String id) {
		if (id == null) {
			return null;
		}
		Optional<Artifact> optional = artifacts.stream().filter(artifact -> {
			return artifact.fullId().equalsIgnoreCase(id);
		}).findFirst();

		if (optional.isPresent()) {
			return optional.get();
		} else {
			return null;
		}
	}

	@Override
	public void addAll(List<Artifact> newItems) {
		if (artifacts == null) {
			artifacts = new ArrayList<Artifact>();
		}
		artifacts.addAll(newItems);
	}

	@Override
	public void replace(List<Artifact> newItems) {
		artifacts = newItems;
	}

	@Override
	public void add(Artifact newItem) {
		if (artifacts == null) {
			artifacts = new ArrayList<Artifact>();
		}
		artifacts.add(newItem);
	}

	@Override
	public List<Artifact> getAll() {
		return artifacts;
	}
}
