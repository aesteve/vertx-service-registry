package io.vertx.serviceregistry.model

class Artifact {
	enum Dists {
		MOD("-mod.zip"),
		JAR(".jar")

		private String extension

		public Dists(String extension){
			this.extension = extension
		}

		@Override
		String toString(){
			extension
		}
	}

	String groupId
	String artifactId
	SortedSet<Version> versions
	Set<String> tags
	String md5
	Set<String> availablePackages
	Map complementaryInfos

	static fromCentral(Map json) {
		Artifact art = new Artifact()
		art.groupId = json["g"]
		art.artifactId = json["a"]
		art.versions = [
			new Version(json["v"], json["timestamp"] as Long)
		] as SortedSet
		art.tags = json["tags"]
		art.availablePackages = json["ec"]

		art
	}

	static fromExport(Map json) {
		Artifact art = new Artifact()

		json.each { key, value ->
			if(key != "versions") {
				art.setProperty(key, value)
			} else {
				// it's a bit more complicated
				art.versions = [] as SortedSet
				value.each { Map map ->
					Version version = new Version(map["name"], map["timestamp"])
					art.versions << version
				}
			}

		}
		art
	}


	Map<String, String> downloadRequestParams(){
		[filePath:"${groupId.replaceAll('[.]','/')}/${artifactId}/${versions[-1]}/${artifactId}-${versions[-1]}${fileExtension()}"]
	}

	Map<String, String> downloadMd5Params(){
		[filePath:"${groupId.replaceAll('[.]','/')}/${artifactId}/${versions[-1]}/${artifactId}-${versions[-1]}${fileExtension()}.md5"]
	}

	String jarLocalPath(){
		"${groupId}-${artifactId}-${versions[-1]}${fileExtension()}"
	}

	boolean hasJar(){
		availablePackages.contains(Dists.JAR.toString())
	}

	boolean hasMod(){
		availablePackages.contains(Dists.MOD.toString())
	}

	String fileExtension(){
		if(!hasMod() && !hasJar())
			throw new IllegalArgumentException("Can't download a file that's neither a mod nor a jar")
		hasMod() ? Dists.MOD.toString() : (hasJar() ? Dists.JAR.toString() : null)
	}

	@Override
	public String toString(){
		"${groupId}-${artifactId}"
	}
}
