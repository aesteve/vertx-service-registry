package io.vertx.serviceregistry.dao;

import io.vertx.ext.apex.RoutingContext;
import io.vertx.serviceregistry.filters.SearchCriteria;
import io.vertx.serviceregistry.http.exceptions.BadRequestException;
import io.vertx.serviceregistry.model.Artifact;

import java.util.List;

public interface ArtifactsDAO {

    public void load();

    public List<Artifact> getMatchingArtifacts(SearchCriteria criteria);

    public Artifact byId(String id);

    public List<Artifact> getPaginatedArtifacts(RoutingContext context, SearchCriteria criteria) throws BadRequestException;
}
