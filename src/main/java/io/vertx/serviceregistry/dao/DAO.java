package io.vertx.serviceregistry.dao;

import io.vertx.componentdiscovery.model.ApiObject;
import io.vertx.serviceregistry.filters.SearchCriteria;
import io.vertx.serviceregistry.http.exceptions.BadRequestException;
import io.vertx.serviceregistry.http.pagination.PaginationContext;
import io.vertx.serviceregistry.model.WordAndWeight;

import java.util.List;

public interface DAO<T extends ApiObject> {

	public void load();

	public void addAll(List<T> newItems);

	public void add(T newItem);

	public void replace(List<T> newItems);

	public List<T> getAll();

	public List<T> getMatchingItems(SearchCriteria criteria);

	public T byId(String id);

	public List<T> getPaginatedItems(PaginationContext context, SearchCriteria criteria) throws BadRequestException;

	public List<WordAndWeight> getWords(PaginationContext context, SearchCriteria criteria) throws BadRequestException;
}
