package io.vertx.serviceregistry;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Single class holding the whole app configuration
 * The configuration can be the default one or be retrieved thanks to env variables (Openshift)
 * TODO : explain in details what is read from conf.json
 * 
 * @author aesteve
 */
public class Config {

	private final static String DEFAULT_ADDRESS = "localhost";
	private final static Integer DEFAULT_PORT = 8080;
	private final static String DEFAULT_DATA_DIR = ".";
	private final static String ARTIFACTS_FILE = "export.json";
	private final static String REPORTS_FILE = "reports.json";
	private final static String SITE_SUFFIX = "site";
	private final static String WORK_SUFFIX = "export";
	private final static String TPL_SUFFIX = "tpl";

	private static Config instance;

	private String address;
	private Integer port;
	private String dataDir;
	private JsonArray crawlers;

	private Config() {
		address = DEFAULT_ADDRESS;
		port = DEFAULT_PORT;
		dataDir = DEFAULT_DATA_DIR;
	}

	public static Config create(Vertx vertx, JsonObject config) {
		instance = new Config();

		final String envPort = System.getenv("OPENSHIFT_DIY_PORT");
		final String envAddress = System.getenv("OPENSHIFT_DIY_IP");
		final String envDataDir = System.getenv("OPENSHIFT_DATA_DIR");

		if (envDataDir != null) {
			instance.dataDir = envDataDir;
		}

		if (envPort != null) {
			instance.port = Integer.parseInt(envPort);
		}

		if (envAddress != null) {
			instance.address = envAddress;
		}

		if (!vertx.fileSystem().existsBlocking(instance.getWorkDir())) {
			vertx.fileSystem().mkdirBlocking(instance.getWorkDir());
		}

		if (!vertx.fileSystem().existsBlocking(instance.getTplDir())) {
			vertx.fileSystem().mkdirBlocking(instance.getTplDir());
		}

		System.out.println("config = " + config.toString());
		instance.crawlers = config.getJsonArray("crawlers");

		return instance;
	}

	public static Config get() {
		return instance;
	}

	public HttpServerOptions getServerOptions() {
		HttpServerOptions options = new HttpServerOptions();
		options.setHost(address);
		options.setPort(port);
		return options;
	}

	public String getSiteDir() {
		return dataDir + "/" + SITE_SUFFIX;
	}

	public String getWorkDir() {
		return dataDir + "/" + WORK_SUFFIX;
	}

	public String getTplDir() {
		return dataDir + "/" + TPL_SUFFIX;
	}

	public String getArtifactsFile() {
		return getWorkDir() + ARTIFACTS_FILE;
	}

	public String getReportsFile() {
		return getWorkDir() + REPORTS_FILE;
	}

	public JsonArray getCrawlers() {
		return crawlers;
	}

	public String getJSServerBundle() {
		return getSiteDir() + "/scripts/server-bundle.js";
	}

}
