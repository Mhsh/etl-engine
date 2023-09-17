package com.etl.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonArrayExample {
	public static void main(String[] args) {
		try {
			// Sample JSON string with deeper nesting
			String json = "{\"parent\": {\"child\": {\"grandchild\": {\"array\": [1, 2, 3]}}}}";

			// Path to the JSON array (deeper nesting)
			String path = "parent/child/grandchild/array";

			// Create an ObjectMapper
			ObjectMapper objectMapper = new ObjectMapper();

			// Parse the JSON
			JsonNode root = objectMapper.readTree(json);

			// Split the path by "/"
			String[] pathSegments = path.split("/");

			// Traverse the JSON structure using the path
			JsonNode currentNode = root;
			for (String segment : pathSegments) {
				currentNode = currentNode.get(segment);
				if (currentNode == null) {
					System.out.println("Path not found: " + path);
					return;
				}
			}

			// Check if the final node is an array
			if (currentNode.isArray()) {
				// Iterate over the elements of the array
				for (JsonNode element : currentNode) {
					System.out.println(element);
				}
			} else {
				System.out.println("Path does not point to an array: " + path);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
