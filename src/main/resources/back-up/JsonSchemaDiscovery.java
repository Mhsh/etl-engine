package com.avro.archieve;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

public class JsonSchemaDiscovery {

	public List<String> collectPaths(JSONObject jsonObject, String currentPath, String separator) {
		List<String> paths = new ArrayList<>();
		for (String tag : jsonObject.keySet()) {
			String newPath = tag;
			if (StringUtils.hasText(currentPath)) {
				newPath = currentPath + separator + tag;
			}
			Object value = jsonObject.get(tag);
			if (value instanceof JSONObject) {
				paths.addAll(collectPaths((JSONObject) value, newPath, separator));
			} else if (value instanceof JSONArray) {
				String arrayPath = newPath + "[*]";
				JSONArray valueArray = (JSONArray) value;
				if (valueArray.length() > 0) {
					if (valueArray.get(0) instanceof JSONObject) {
						paths.addAll(collectPaths(valueArray.getJSONObject(0), arrayPath, separator));
					} else {
						paths.add(newPath);
					}
				}
			} else {
				paths.add(newPath);
			}
		}
		return paths;
	}

	public static void main(String[] args) throws IOException {
		String sampleRegulatory = readFile("src/main/resources/json/regulatory_template.json");
		JSONObject jsonObject = new JSONObject(sampleRegulatory);
		JsonSchemaDiscovery schemaDiscovery = new JsonSchemaDiscovery();
		System.out.println(schemaDiscovery.collectPaths(jsonObject, null, "/"));
	}

	private static String readFile(String filePath) throws IOException {
		Path path = Paths.get(filePath);
		return new String(Files.readAllBytes(path));
	}

}
