package com.avro.archieve;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.etl.MappingInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TransformationService {

	private static String RAW_FILE = "src/main/resources/json/connector_downloaded.json";
	private static String REG_TEMPLATE = "src/main/resources/json/regulatory_template.json";
	private static String MAPPING_FILE = "src/main/resources/json/mapping_data.json";

	static ObjectMapper objectMapper = new ObjectMapper();
	static JsonPathValueExtractor valueExtractor = new JsonPathValueExtractor();

	public static void main(String[] args) throws IOException {
		String reg_template = readFile(REG_TEMPLATE);
		JsonNode templateJson = objectMapper.readTree(reg_template);
		String raw_file = readFile(RAW_FILE);

		JSONObject jsonObject = new JSONObject(raw_file);
		String sampleRegulatoryRoot = "$";
		Map<String, List<MappingInfo>> mappings = populateMappings();
		mappings.keySet().stream().forEach(key -> {
			Map<String, List<String>> finalList = new HashMap<>();
			List<MappingInfo> mappingData = mappings.get(key);
			mappingData.stream().forEach(mapping -> {
				String outputKey = mapping.getTemplateFilePathKey();
				List<String> values = valueExtractor.extractProperty(sampleRegulatoryRoot, raw_file, outputKey);
				finalList.put(mapping.getTemplateFilePathKey(), values);
			});
			int maxSize = finalList.values().stream().mapToInt(List::size).max().orElse(0);

		});
	}

	public static void main1(String[] args) throws IOException {
		DynamicJsonGenerator jsonGenerator = new DynamicJsonGenerator(objectMapper);
		String raw_file = readFile(RAW_FILE);
		Map<String, List<String>> extractedValues = extractValues(raw_file, ".");
		String reg_template = readFile(REG_TEMPLATE);
		JsonNode templateJson = objectMapper.readTree(reg_template);
		extractedValues.keySet().stream().forEach(key -> {
			String internalKey = getInternalKey(key);
			if (internalKey != null) {
				List<String> values = extractedValues.get(key);
				int lastDotIndex = internalKey.lastIndexOf(".");
				String lastWord = internalKey.substring(lastDotIndex + 1);
				String precedingStr = "/" + internalKey.substring(0, lastDotIndex);
				boolean isArray = precedingStr.endsWith("[*]") ? true : false;
				precedingStr = precedingStr.replaceAll("\\[\\*\\]", "").replace(".", "/");
				if (isArray) {
					jsonGenerator.addObjectList(templateJson, precedingStr, lastWord, values);
				} else if (values.size() > 0)
					jsonGenerator.addObject(templateJson, precedingStr, lastWord, values.get(0));
				else
					System.out.println("ERRRORRRR+++++++++++++++++++++");
			} else {
				System.out.println("ERRRORRRR+++++++++++++++++++++");
			}
		});
		System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(templateJson));

	}

	private static String readFile(String filePath) throws IOException {
		Path path = Paths.get(filePath);
		return new String(Files.readAllBytes(path));
	}

	private static Map<String, List<String>> extractValues(String sampleRegulatory, String separator)
			throws IOException {
		JsonPathValueExtractor valueExtractor = new JsonPathValueExtractor();
		JSONObject jsonObject = new JSONObject(sampleRegulatory);
		String sampleRegulatoryRoot = "$";
		JsonSchemaDiscovery schemaDiscovery = new JsonSchemaDiscovery();
		List<String> jsonPaths = schemaDiscovery.collectPaths(jsonObject, null, separator);
		return valueExtractor.extractValuesFromPath(sampleRegulatoryRoot, sampleRegulatory, jsonPaths);
	}

	public static String getInternalKey(String sourceKey) {
		try {
			String mapping_file = readFile(MAPPING_FILE);
			JsonNode jsonNode = objectMapper.readTree(mapping_file);
			for (JsonNode node : jsonNode) {
				String currentSourceKey = node.get("sourcekey").asText();
				if (currentSourceKey.equals(sourceKey)) {
					return node.get("internalkey").asText();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Map<String, List<MappingInfo>> populateMappings() throws IOException {
		Map<String, List<MappingInfo>> mappings_map = new HashMap<>();
		String mapping_file = readFile(MAPPING_FILE);
		ObjectMapper objectMapper = new ObjectMapper();
		List<MappingInformation> mappings = objectMapper.readValue(mapping_file,
				new TypeReference<List<MappingInformation>>() {
				});
		Map<String, List<String>> resultMap = new HashMap<>();
		mappings.stream().forEach(mapping -> {
			String[] parts = mapping.getSourceKey().split("/");
			if (parts.length > 1) {
				// Build the key by joining all parts except the last one
				StringBuilder keyBuilder = new StringBuilder("/" + parts[0]);
				for (int i = 1; i < parts.length - 1; i++) {
					keyBuilder.append("/").append(parts[i]);
				}
				String key = keyBuilder.toString();

				// The last part is the value
				String value = parts[parts.length - 1];

				// Add the value to the list associated with the key
				resultMap.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
			}
		});
		return mappings_map;
	}

}
