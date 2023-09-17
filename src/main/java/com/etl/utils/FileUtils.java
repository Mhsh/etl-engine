package com.etl.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

	public static String readFile(String filePath) throws IOException {
		Path path = Paths.get(filePath);
		return new String(Files.readAllBytes(path));
	}

	public static String getFileName(String filePath) {
		Path path = Paths.get(filePath);
		return path.getFileName().toString();

	}
}
