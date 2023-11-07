package com.avro.archieve;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonTemplateReplacer {
	static ObjectMapper objectMapper = new ObjectMapper();

	public static String transformJson(String jsonTemplate, Map<String, String> mappingValues) throws Exception {
		// Parse the JSON template as a JSON object

		JsonNode jsonNode = objectMapper.readTree(jsonTemplate);

		// Replace the tags in the JSON object with the corresponding values from the
		// incoming document
		jsonNode = replaceTags(jsonNode, mappingValues);

		// Serialize the updated JSON object back to a string
		return objectMapper.writeValueAsString(jsonNode);
	}

	private static JsonNode replaceTags(JsonNode jsonNode, Map<String, String> mappingValues) {
		if (jsonNode.isArray()) {
			// If the field value is an array, create a new ArrayNode and add the updated
			// elements to it
			ArrayNode arrayNode = objectMapper.createArrayNode();
			for (int i = 0; i < jsonNode.size(); i++) {
				JsonNode element = jsonNode.get(i);
				arrayNode.add(replaceTags(element, mappingValues));
			}
			return arrayNode;
		} else if (jsonNode.isObject()) {
			// If the field value is an object, create a new ObjectNode and add the updated
			// fields to it
			ObjectNode objectNode = objectMapper.createObjectNode();
			jsonNode.fields().forEachRemaining(entry -> {
				String fieldName = entry.getKey();
				JsonNode fieldValue = entry.getValue();

				// If the field value is a string and contains a tag, replace the tag with the
				// corresponding value from the incoming document
				if (fieldValue.isTextual() && fieldValue.asText().startsWith("<")
						&& fieldValue.asText().endsWith(">")) {
					String tag = fieldValue.asText();
					String value = getValueForTag(tag, mappingValues);
					objectNode.put(fieldName, value);
				} else {
					// Recursively replace the tags in the nested fields
					objectNode.set(fieldName, replaceTags(fieldValue, mappingValues));
				}
			});
			return objectNode;
		} else {
			// If the field value is not an object or an array, return it as is
			return jsonNode;
		}
	}

	private static String getValueForTag(String tag, Map<String, String> mappingValues) {
		// Replace the angle brackets with quotes to get the field name
		String fieldName = tag.replace("<", "").replace(">", "");
		return mappingValues.get(fieldName);
	}

	public static void main(String[] args) {
		try {
			String jsonTemplate = "{\n" + "	\"regulatory\": {\n" + "\n" + "		\"regulatoryCorrespondence\": [{\n"
					+ "			\"correspondenceID\": \"<correspondenceID>\",\n"
					+ "			\"correspondenceDate\": \"<correspondenceDate>\"\n" + "		}]\n" + "	}\n" + "}";
			String incomingDocument = "{\"documentID\": \"123\", \"documentTitle\": \"Sample Document\", \"documentVersion\": \"1.0\", \"labelingInfo\": \"Sample Label\", \"packageComponents\": \"Sample Components\", \"Box\": \"Sample Box\", \"Bottle\": \"Sample Bottle\", \"approvalSignature\": [\"Signature 1\", \"Signature 2\", \"Signature 3\"], \"approverName\": [\"Approver 1\", \"Approver 2\", \"Approver 3\"]}";
			Map<String, String> mappingValues = new HashMap<>();
			mappingValues.put("correspondenceID", "TestCorrespondencId");
			mappingValues.put("correspondenceDate", "TestcorrespondenceDate");
			String updatedJson = transformJson(jsonTemplate, mappingValues);
			System.out.println(updatedJson);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}