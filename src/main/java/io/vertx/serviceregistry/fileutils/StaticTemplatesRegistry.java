package io.vertx.serviceregistry.fileutils;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.serviceregistry.http.pagination.PaginationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO : /!\ Take SearchCriteria into account /!\
 * 
 * @author aesteve
 */
public class StaticTemplatesRegistry {

	private static final Logger log = Logger.getLogger("FILE");

	private final String baseDir;
	private Vertx vertx;
	private Map<String, String> addresses;

	public StaticTemplatesRegistry(Vertx vertx, String baseDir) {
		this.baseDir = baseDir;
		this.vertx = vertx;
		this.addresses = new HashMap<String, String>();
	}

	public void store(String basePath, PaginationContext context, String html) {
		String address = getAddress(basePath, context);
		String storage = getFileName(basePath, context);
		vertx.fileSystem().writeFile(storage, Buffer.buffer(html), onceDone -> {
			if (onceDone.succeeded()) {
				addresses.put(address, storage);
			} else {
				log.log(Level.SEVERE, "Could not store file on fs", onceDone.cause());
			}
		});
	}

	private String getAddress(String basePath, PaginationContext context) {
		return basePath + ":" + context.getPageAsked() + ":" + context.getItemsPerPage();
	}

	private String getFileName(String basePath, PaginationContext context) {
		return baseDir + "/" + basePath + "_" + context.getPageAsked() + "_" + context.getItemsPerPage() + ".html";
	}

	public String getCached(String basePath, PaginationContext context) {
		return this.addresses.get(this.getAddress(basePath, context));
	}
}
