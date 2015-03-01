package io.vertx.serviceregistry.dao.impl;

import io.vertx.core.json.JsonArray;
import io.vertx.serviceregistry.dao.ArtifactsDAO;
import io.vertx.serviceregistry.filters.SearchCriteria;
import io.vertx.serviceregistry.io.ArtifactsMarshaller;
import io.vertx.serviceregistry.model.Artifact;

import java.util.List;
import java.util.Optional;

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

	@Override
	public List<Artifact> getMatchingArtifacts(SearchCriteria criteria) {
		return artifacts; // TODO : match
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
