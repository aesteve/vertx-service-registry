package io.vertx.serviceregistry.dao.impl;

import io.vertx.core.json.JsonArray;
import io.vertx.serviceregistry.dao.ArtifactsDAO;
import io.vertx.serviceregistry.filters.SearchCriteria;
import io.vertx.serviceregistry.io.ArtifactsMarshaller;
import io.vertx.serviceregistry.model.Artifact;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonArtifactsDAO implements ArtifactsDAO {
	public static List<Artifact> artifacts;
	private String json;

	public JsonArtifactsDAO(String json) {
		this.json = json;
	}

	@Override
	public void load() {
		JsonArray array = new JsonArray(json);
		artifacts = ArtifactsMarshaller.unmarshall(array);
	}

	/**
	 * This method belongs to the DAO since it could query something
	 * (not in JSON implementation but in an SQL one for instance)
	 */
	@Override
	public List<Artifact> getMatchingArtifacts(final SearchCriteria criteria) {
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
}
