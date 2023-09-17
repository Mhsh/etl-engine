package com.etl.fuel;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.etl.ETLMessage;
import com.etl.MappingInfo;
import com.etl.utils.FileUtils;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.storage.jpa.Enums.FileType;
import com.storage.jpa.JpaClientTemplate;

@Component
public class JsonFuel implements Fuel {

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${etl-engine.output-file-path}")
	private String outputFilePath;

	private static final Logger LOGGER = Logger.getLogger(JsonFuel.class.getName());

	@Override
	public void transform(List<MappingInfo> mappingInfos, ETLMessage etlMessage, JpaClientTemplate template)
			throws Exception {
		LOGGER.info("Transforming JSON data...");
		JsonNode templateJsonFileNode = objectMapper.readTree(template.getTemplate());
		JsonNode rawFileJsonFileNode = objectMapper.readTree(FileUtils.readFile(etlMessage.getRawFilePath()));
		// Parse the JSON
		String arrayPath = null;
		MappingInfo searchMappingInfo = new MappingInfo("ROOT");
		if (mappingInfos.contains(searchMappingInfo)) {
			arrayPath = mappingInfos.get(mappingInfos.indexOf(searchMappingInfo)).getRawFilePathKey();
		}
		if (arrayPath != null) {
			LOGGER.info("Using arrayPath: " + arrayPath);

			// Traverse the JSON structure using the path
			JsonNode currentNode = rawFileJsonFileNode;
			// Split the path by "/"
			String[] pathSegments = arrayPath.split("/");

			for (String segment : pathSegments) {
				currentNode = currentNode.get(segment);
				// validating every path given is present or not.
				if (currentNode == null) {
					LOGGER.warning("Wrong array path provided in ROOT. Path " + arrayPath);
					throw new RuntimeException("Wrong array path provided in ROOT. Path " + arrayPath);
				}
			}
			// Check if the final node is an array
			if (currentNode.isArray()) {
				// Iterate over the elements of the array
				for (JsonNode element : currentNode) {
					igniteMapping(mappingInfos, templateJsonFileNode, element);
				}
			} else {
				LOGGER.warning(
						"ROOT mapping is provided but the target object in path is not an array. Path " + arrayPath);
				throw new RuntimeException(
						"ROOT mapping is provided but the target object in path is not an array. Path " + arrayPath);
			}
		} else {
			LOGGER.info("No arrayPath specified, proceeding with default mapping.");
			igniteMapping(mappingInfos, templateJsonFileNode, rawFileJsonFileNode);
		}
		LOGGER.info("Transformation complete.");
	}

	private void igniteMapping(List<MappingInfo> mappingInfos, JsonNode templateJsonFileNode,
			JsonNode rawFileJsonFileNode)
			throws JsonGenerationException, JsonMappingException, JsonProcessingException, IOException {
		LOGGER.info("Igniting mapping...");

		mappingInfos.stream().forEach(mappingInfo -> {
			// checking whether it is array nested mapping or direct mapping.
			// For non-array children we have not populated the mapping path.
			// as this will be direct replacement. Still we need to do some logic for
			// template
			// since replacement requires the location and tag to be replaced.
			// This case is done for parent level mapping from raw file like
			// {
			// "DocumentID": "REGDOC001"
			// }
			if (mappingInfo.getMappingForPaths().isEmpty()) {
				handleDirectObjectJsonCreation(mappingInfo, rawFileJsonFileNode, templateJsonFileNode);
			} else {
				JsonNode rawFileJsonFilePathNode = rawFileJsonFileNode.at(mappingInfo.getRawFilePathKey());
				JsonNode templateJsonObjectNode = templateJsonFileNode.at(mappingInfo.getTemplateFilePathKey());
				if (rawFileJsonFileNode instanceof MissingNode) {
					throw new RuntimeException("No mapping found for input raw file for path " + rawFileJsonFileNode);
				}
				if (rawFileJsonFilePathNode instanceof ArrayNode) {
					handleNestedArrayObjects(mappingInfo, (ArrayNode) rawFileJsonFilePathNode,
							(ArrayNode) templateJsonObjectNode);
				} else {
					handleNestedObjectJsonCreation(mappingInfo, rawFileJsonFilePathNode, templateJsonObjectNode);
				}
			}
		});
		saveTransformedFile(templateJsonFileNode, outputFilePath);
		LOGGER.info("Mapping completed.");
	}

	private void saveTransformedFile(JsonNode templateJsonFileNode, String rawFilePath)
			throws JsonGenerationException, JsonMappingException, JsonProcessingException, IOException {
		File outputFile = new File(outputFilePath + FileUtils.getFileName(rawFilePath));
		objectMapper.writeValue(outputFile,
				objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(templateJsonFileNode));
	}

	private void handleDirectObjectJsonCreation(MappingInfo mappingInfo, JsonNode rawFileJsonFileNode,
			JsonNode templateJsonFileNode) {
		LOGGER.info("Handling direct object JSON creation...");
		// Get the raw file node path where we have to replace the path.
		JsonNode rawFileObjectNode = rawFileJsonFileNode.get(mappingInfo.getRawFilePathKey());
		// We are constructing the template paths for replacing the values.
		// Basically this is finding the path after removing the last word and
		// then using this path add value to the last word.
		String templateFilePathKey = mappingInfo.getTemplateFilePathKey();
		int lastIndexOfRawFilePath = templateFilePathKey.lastIndexOf("/");
		String precedingRawFilePath = lastIndexOfRawFilePath != -1
				? templateFilePathKey.substring(0, templateFilePathKey.lastIndexOf("/"))
				: templateFilePathKey;
		String lastWordTemplateFilePath = templateFilePathKey.substring(templateFilePathKey.lastIndexOf("/") + 1);

		JsonNode templateJsonObjectNode = templateJsonFileNode.at(precedingRawFilePath);
		((com.fasterxml.jackson.databind.node.ObjectNode) templateJsonObjectNode).put(lastWordTemplateFilePath,
				rawFileObjectNode.asText());

		LOGGER.info("Direct object JSON creation completed.");
	}

	private void handleNestedObjectJsonCreation(MappingInfo mappingInfo, JsonNode rawFileJsonFilePathNode,
			JsonNode templateJsonObjectNode) {
		LOGGER.info("Handling nested object JSON creation...");
		// This is nested object
		// "LabelingAndPackages": {
		// "LabelingInformation": "Package Inserts and Labels provided",
		// "PackageDescriptions": {
		// "OuterBox": "Outer packaging",
		// "InnerBottle": "Inner container"
		// }
		// }
		mappingInfo.getMappingForPaths().keySet().forEach(key -> {
			// some nested object directly contains the array string so this handling is for
			// that.
			// "regulatoryCompliance" : {
			// "requirementsAndStandards" : [ "HIPAA", "FDA", "QSR" ],
			// "complianceStatus" : ""
			// }
			if (rawFileJsonFilePathNode.get(key) instanceof ArrayNode) {
				ArrayNode templateNewObject = objectMapper.createArrayNode();
				for (JsonNode rawFileJsonArrayNode : rawFileJsonFilePathNode.get(key)) {
					templateNewObject.add(rawFileJsonArrayNode);
				}
				((ObjectNode) templateJsonObjectNode).putArray(mappingInfo.getMappingForPaths().get(key))
						.addAll(templateNewObject);
			} else
				((ObjectNode) templateJsonObjectNode).put(mappingInfo.getMappingForPaths().get(key),
						rawFileJsonFilePathNode.get(key).asText());
		});

		LOGGER.info("Nested object JSON creation completed.");
	}

	private void handleNestedArrayObjects(MappingInfo mappingInfo, ArrayNode rawFileJsonFilePathNode,
			ArrayNode templateJsonObjectNode) {
		LOGGER.info("Handling nested array JSON creation...");
		// This is a nested array case where the target replacement is an array.
		// So we already know from where (mappingInfo.getRawFilePathKey()) we have to
		// take the value.
		// From which field to take value is defined as key of map that we have stored.
		// Similarly we know the value where we have to replace the value
		// (mappingInfo.getTemplateFilePathKey())
		// The node at which value needs to be replaced is at value of map.
		// {
		// "ApprovalsAndSignOffs": [
		// {
		// "Approver": "Sarah Johnson",
		// "ApprovalDate": "2023-07-30"
		// },
		// {
		// "Approver": "Sarah Johnson1",
		// "ApprovalDate": "2023-07-31"
		// }
		// ]
		// }
		// clearing the template that we have unfortunately for schema discovery.
		templateJsonObjectNode.removeAll();
		// create multiple nodes and then add at the end.
		for (JsonNode rawFileJsonArrayNode : rawFileJsonFilePathNode) {
			ObjectNode templateNewObject = objectMapper.createObjectNode();
			mappingInfo.getMappingForPaths().keySet().forEach(key -> {
				templateNewObject.put(mappingInfo.getMappingForPaths().get(key),
						rawFileJsonArrayNode.get(key).asText());
			});
			((ArrayNode) templateJsonObjectNode).add(templateNewObject);

		}

		LOGGER.info("Nested array JSON creation completed.");
	}

	@Override
	public FileType getFuelName() {
		return FileType.JSON;
	}
}
