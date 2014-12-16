package io.vertx.serviceregistry.model

class Version implements Comparable {
	String name
	Long timestamp

	public Version(String name, Long timestamp){
		this.name = name
		this.timestamp = timestamp
	}

	@Override
	public int compareTo(Object other){
		timestamp.compareTo(other.timestamp)
	}

	@Override
	public String toString(){
		this.name
	}
}
