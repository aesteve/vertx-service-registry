package io.vertx.serviceregistry.dao;

import io.vertx.serviceregistry.filters.SearchCriteria;
import io.vertx.serviceregistry.model.Artifact;

import java.util.List;

public interface ArtifactsDAO {

	public void load();

	public List<Artifact> getMatchingArtifacts(SearchCriteria criteria);

	public Artifact byId(String id);
}
