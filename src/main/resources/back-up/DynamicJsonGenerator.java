package com.avro.archieve;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DynamicJsonGenerator {

	ObjectMapper objectMapper = null;

	public DynamicJsonGenerator(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public static void main(String[] args) throws IOException {
		// Sample template JSON
		String REG_TEMPLATE = "src/main/resources/json/regulatory_template.json";
		String reg_template = readFile(REG_TEMPLATE);
		// Read input values (for demonstration, using hardcoded values)
		String[] approvalSignatures = { "Signature 1", "Signature 2", "Signature 3" };
		String[] approvalNames = { "Name 1", "Name 2", "Name 3" };

		// Map for metadata values
		Map<String, String> metadataValues = new HashMap<>();
		metadataValues.put("documentID", "123");
		metadataValues.put("documentTitle", "Sample Document");

		// Map for labeling and packaging values
		Map<String, String> labelingAndPackagingValues = new HashMap<>();
		labelingAndPackagingValues.put("labelingInfo", "Label Info Example");
		labelingAndPackagingValues.put("packageComponents", "Component Example");
		labelingAndPackagingValues.put("Box", "Box Description Example");
		labelingAndPackagingValues.put("Bottle", "Bottle Description Example");

		// Parse the template JSON using Jackson
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode templateJson = objectMapper.readTree(reg_template);

		// Populate metadata values
		JsonNode metadata = templateJson.at("/regulatory/metadata");
		metadataValues
				.forEach((key, value) -> ((com.fasterxml.jackson.databind.node.ObjectNode) metadata).put(key, value));

		// Access the "regulatoryCorrespondence" array and populate it with objects from
		// input values
		JsonNode correspondenceArray = templateJson.at("/regulatory/regulatoryCorrespondence");
		JsonNode correspondenceObject = objectMapper.createObjectNode().put("correspondenceID", "12345")
				.put("correspondenceDate", "2023-08-29");
		((com.fasterxml.jackson.databind.node.ArrayNode) correspondenceArray).add(correspondenceObject);

		// Populate labeling and packaging values
		JsonNode labelingAndPackaging = templateJson.at("/regulatory/labelingAndPackaging");
		labelingAndPackagingValues.forEach((key, value) -> {
			if (key.equals("packageDescriptions")) {
				JsonNode packageDescriptions = labelingAndPackaging.at("/" + key);
				labelingAndPackagingValues.forEach((innerKey, innerValue) -> {
					if (packageDescriptions.has(innerKey)) {
						((com.fasterxml.jackson.databind.node.ObjectNode) packageDescriptions).put(innerKey,
								innerValue);
					}
				});
			} else {
				JsonNode node = labelingAndPackaging.at("/" + key);
				if (node.isObject()) {
					((com.fasterxml.jackson.databind.node.ObjectNode) node).put(key, value);
				}
			}
		});

		// Access the "approvers" array and populate it with objects from input values
		JsonNode approversArray = templateJson.at("/regulatory/approvalsAndSignOffs");
		((com.fasterxml.jackson.databind.node.ArrayNode) approversArray).removeAll();
		for (int i = 0; i < approvalSignatures.length; i++) {
			JsonNode approver = objectMapper.createObjectNode().put("approvalSignature", approvalSignatures[i])
					.put("approvalName", approvalNames[i]);
			((com.fasterxml.jackson.databind.node.ArrayNode) approversArray).add(approver);
		}

		// Print the final JSON with populated values
		System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(templateJson));
	}

	public void addObjectList(JsonNode templateJson, String nodeArrayPath, String nodeToCreate, List<String> values) {
		JsonNode nodeAray = templateJson.at(nodeArrayPath);
		for (String value : values) {
			JsonNode node = objectMapper.createObjectNode().put(nodeToCreate, value);
			((com.fasterxml.jackson.databind.node.ArrayNode) nodeAray).add(node);
		}
	}

	public void addObject(JsonNode templateJson, String nodeArrayPath, String nodeToCreate, String value) {
		JsonNode nodeObject = templateJson.at(nodeArrayPath);
		((com.fasterxml.jackson.databind.node.ObjectNode) nodeObject).put(nodeToCreate, value);
	}

	private static String readFile(String filePath) throws IOException {
		Path path = Paths.get(filePath);
		return new String(Files.readAllBytes(path));
	}

}
