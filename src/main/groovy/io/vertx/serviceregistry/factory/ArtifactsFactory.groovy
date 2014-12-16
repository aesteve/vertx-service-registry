package io.vertx.serviceregistry.factory

import groovy.json.JsonSlurper
import io.vertx.serviceregistry.model.Artifact

class ArtifactsFactory {

	File jsonFile

	private JsonSlurper slurper
	private static ArtifactsFactory instance

	static ArtifactsFactory instance(){
		if(!instance)
			instance = new ArtifactsFactory()
		instance
	}

	private ArtifactsFactory(){
		slurper = new JsonSlurper()
	}

	Set<Artifact> fromJsonFile(){
		if(!jsonFile || !jsonFile.exists())
			throw new IllegalArgumentException("The export file hasn't been configured")
		slurper.parse(jsonFile)
	}
}
