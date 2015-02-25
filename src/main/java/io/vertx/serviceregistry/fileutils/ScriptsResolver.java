package io.vertx.serviceregistry.fileutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ScriptsResolver {

	public static Reader getScriptFromClasspath(String path) {
		return new InputStreamReader(ScriptsResolver.class.getClassLoader().getResourceAsStream(path));
	}

	public static Reader getScriptReaderFromFile(String path) throws FileNotFoundException {
		return new FileReader(new File(path));
	}

	public static String getCodeFromClasspathResource(String name) throws IOException, URISyntaxException {
		return new String(Files.readAllBytes(getClassPath(name)));

	}

	public static Path getClassPath(String name) throws URISyntaxException {
		return Paths.get(ClassLoader.getSystemResource(name).toURI());
	}

	public static void writeScriptToPath(String content, String path) throws IOException {
		Files.write(Paths.get(path), content.getBytes());
	}

	public static String getCodeFromStaticFile(String absolutePath) throws IOException {
		return new String(Files.readAllBytes(Paths.get(absolutePath)));
	}
}
