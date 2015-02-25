package io.vertx.serviceregistry.model;

public class Version implements Comparable<Version> {
	public String name;
	public Long timestamp;

	public Version(String name, Long timestamp) {
		this.name = name;
		this.timestamp = timestamp;
	}

	@Override
	public int compareTo(Version other) {
		return timestamp.compareTo(other.timestamp);
	}

	@Override
	public String toString() {
		return this.name;
	}
}
