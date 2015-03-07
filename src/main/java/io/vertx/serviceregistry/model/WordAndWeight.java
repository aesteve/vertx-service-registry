package io.vertx.serviceregistry.model;

import io.vertx.componentdiscovery.model.ApiObject;
import io.vertx.core.json.JsonObject;

public class WordAndWeight implements ApiObject {
	public final String word;
	public Integer weight;

	public WordAndWeight(String word) {
		this.word = word;
		this.weight = 1;
	}

	@Override
	public JsonObject toJsonObject() {
		JsonObject obj = new JsonObject();
		obj.put("word", word);
		obj.put("weight", weight);
		return obj;
	}

}
