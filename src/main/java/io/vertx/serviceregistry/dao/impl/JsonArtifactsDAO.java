package io.vertx.serviceregistry.dao.impl;

import io.vertx.componentdiscovery.model.Artifact;
import io.vertx.core.json.JsonArray;
import io.vertx.serviceregistry.dao.DAO;
import io.vertx.serviceregistry.filters.SearchCriteria;
import io.vertx.serviceregistry.http.exceptions.BadRequestException;
import io.vertx.serviceregistry.http.pagination.PaginationContext;
import io.vertx.serviceregistry.io.ApiObjectMarshaller;
import io.vertx.serviceregistry.model.WordAndWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
			return getAll();
		}
		return artifacts.stream().filter(artifact -> {
			boolean matches = false;
			String txtSearch = criteria.getTextSearch();
			if (txtSearch != null && !"".equals(txtSearch)) {
				if (artifact.fullId().indexOf(txtSearch) > 0) {
					matches = true;
				}
				if (artifact.getTags() != null && artifact.getTags().size() > 0 && !matches) {
					Optional<String> optional = artifact.getTags().stream().filter(tag -> {
						return tag.startsWith(txtSearch);
					}).findFirst();
					matches = matches || optional.isPresent();
				}
			} else {
				matches = true;
			}
			if (criteria.getTags() != null && criteria.getTags().size() > 0) {
				if (artifact.getTags() == null || artifact.getTags().size() == 0) {
					return false;
				}
				criteria.getTags().retainAll(artifact.getTags());
				return criteria.getTags().size() > 0;
			}
			return matches;
		}).collect(Collectors.toList());
	}

	@Override
	public List<Artifact> getPaginatedItems(PaginationContext paginationContext, SearchCriteria criteria) throws BadRequestException {
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

	@Override
	public List<WordAndWeight> getWords(PaginationContext context, SearchCriteria criteria) throws BadRequestException {
		List<Artifact> artifacts = getPaginatedItems(context, criteria);
		List<WordAndWeight> words = new ArrayList<WordAndWeight>();
		artifacts.forEach(artifact -> {
			Set<String> tags = artifact.getTags();
			if (tags != null && !tags.isEmpty()) {
				tags.forEach(tag -> {
					Optional<WordAndWeight> optional = words.stream().filter(waw -> {
						return waw.word.equals(tag);
					}).findFirst();
					WordAndWeight waw;
					if (optional.isPresent()) {
						waw = optional.get();
						waw.weight = waw.weight + 1;
					} else {
						waw = new WordAndWeight(tag);
						words.add(waw);
					}
				});
			}
		});
		return words;
	}
}
