package io.vertx.serviceregistry.factory

import groovy.json.JsonSlurper
import io.vertx.serviceregistry.model.Artifact

class ArtifactsFactory {

	private static JsonSlurper slurper
	private static ArtifactsFactory instance

	static Set<Artifact> artifacts

	static void load(String json){
		if (!slurper)
			slurper = new JsonSlurper()
		artifacts = slurper.parseText(json)
	}
}
