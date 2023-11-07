package com.etl.utils;

import java.io.File;
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
		if (filePath == null) {
			return null;
		}

		File file = new File(filePath);
		String fileName = file.getName();

		int lastDotIndex = fileName.lastIndexOf('.');
		if (lastDotIndex > 0) {
			return fileName.substring(0, lastDotIndex);
		} else {
			// No extension found, return the full name
			return fileName;
		}
	}

	public static void main(String[] args) {
		System.out.println(getFileName(
				"/Users/my/tmp/ingestion//Factiva/58c29733-0ba2-4465-8e69-3924aa7d46d2/2023-11-07/NIFBSALQOC_483e809c-793b-4bee-b70e-8a012f52024b.xml"));
	}
}
