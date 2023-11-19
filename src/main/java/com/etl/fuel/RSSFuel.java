package com.etl.fuel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.etl.ETLMessage;
import com.etl.MappingInfo;
import com.etl.RSSStruct;
import com.etl.utils.FileUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.storage.jpa.Enums.FileType;

@Component
public class RSSFuel implements Fuel {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RSSStruct struct;

	@Value("${etl-engine.output-file-path}")
	private String outputFilePath;

	private static final Logger LOGGER = Logger.getLogger(RSSFuel.class.getName());

	@Override
	public void transform(List<MappingInfo> mappingInfos, ETLMessage etlMessage, JsonNode templateJsonFileNode)
			throws Exception {
		LOGGER.info(
				"Transforming JSON data... Parameters: mappingInfos=" + mappingInfos + ", etlMessage=" + etlMessage);
		FileInputStream inputStream = new FileInputStream(etlMessage.getRawFilePath());
		// Create a SyndFeedInput to read the feed
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = input.build(new XmlReader(inputStream));
		mappingInfos.stream().forEach(mappingInfo -> {
			if (mappingInfo.getMappingForPaths().isEmpty()) {
				handleDirectObjectJsonCreation(mappingInfo, feed, templateJsonFileNode);
			} else {
				JsonNode templateJsonObjectNode = templateJsonFileNode.at(mappingInfo.getTemplateFilePathKey());
				if (templateJsonObjectNode instanceof MissingNode) {
					throw new RuntimeException("Path not found in json for " + mappingInfo.getTemplateFilePathKey());
				} else if (templateJsonObjectNode instanceof ArrayNode) {
					handleNestedArrayObjects(mappingInfo, feed, (ArrayNode) templateJsonObjectNode);
				} else {
					handleNestedObjectJsonCreation(mappingInfo, feed, templateJsonObjectNode);
				}
			}
		});
		saveFile(templateJsonFileNode, etlMessage.getRawFilePath());
		LOGGER.info("Transformation complete.");
	}

	public void saveFile(JsonNode templateJsonFileNode, String filePath) {
		String outputPath = outputFilePath + "/" + FileUtils.getFileName(filePath) + ".json";
		LOGGER.info(String.format("Saved file location  - : %s ", outputPath));
		File file = new File(outputPath);
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		try (FileWriter fileWriter = new FileWriter(outputPath);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
			// Write the content to the file
			bufferedWriter
					.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(templateJsonFileNode));
		} catch (IOException e) {
			throw new RuntimeException("Failed to save file. Reason - " + e.getLocalizedMessage());
		}
	}

	private void handleDirectObjectJsonCreation(MappingInfo mappingInfo, SyndFeed syndFeed,
			JsonNode templateJsonFileNode) {
		LOGGER.info("Handling direct object JSON creation... Parameters: mappingInfo=" + mappingInfo);
		LOGGER.info("processing for key " + mappingInfo.getRawFilePathKey());
		String templateFilePathKey = mappingInfo.getTemplateFilePathKey();
		int lastIndexOfRawFilePath = templateFilePathKey.lastIndexOf("/");
		String precedingRawFilePath = lastIndexOfRawFilePath != -1
				? templateFilePathKey.substring(0, templateFilePathKey.lastIndexOf("/"))
				: templateFilePathKey;
		String lastWordTemplateFilePath = templateFilePathKey.substring(templateFilePathKey.lastIndexOf("/") + 1);
		JsonNode templateJsonObjectNode = templateJsonFileNode.at(precedingRawFilePath);
		((com.fasterxml.jackson.databind.node.ObjectNode) templateJsonObjectNode).put(lastWordTemplateFilePath,
				struct.getValue(mappingInfo.getRawFilePathKey(), syndFeed));
		LOGGER.info("Direct object JSON creation completed.");
	}

	private void handleNestedObjectJsonCreation(MappingInfo mappingInfo, SyndFeed feed,
			JsonNode templateJsonObjectNode) {
		LOGGER.fine("Handling nested object JSON creation... Parameters: mappingInfo=" + mappingInfo + ", feed=" + feed
				+ ", templateJsonObjectNode=" + templateJsonObjectNode);
		mappingInfo.getMappingForPaths().keySet().forEach(key -> {
			LOGGER.info("processing for key " + mappingInfo.getRawFilePathKey() + "/" + key);
			String internalFilePath = mappingInfo.getRawFilePathKey() + "/" + key;
			SyndEntry entry = feed.getEntries().get(0);
			if ("/document/category".equals(internalFilePath)) {
				struct.addCategory(entry, mappingInfo.getMappingForPaths().get(key), templateJsonObjectNode);
			} else {
				((ObjectNode) templateJsonObjectNode).put(mappingInfo.getMappingForPaths().get(key),
						struct.getValue(internalFilePath, feed));
			}
		});
		if (LOGGER.isLoggable(java.util.logging.Level.FINE)) {
			LOGGER.fine("Nested object JSON creation completed.");
		}
	}

	private void handleNestedArrayObjects(MappingInfo mappingInfo, SyndFeed feed, ArrayNode templateJsonObjectNode) {
		LOGGER.fine("Handling nested array JSON creation... Parameters: mappingInfo=" + mappingInfo
				+ ", rawFileJsonFilePatfeedhNode=" + feed + ", templateJsonObjectNode=" + templateJsonObjectNode);
		LOGGER.info("processing for key " + mappingInfo.getRawFilePathKey());
		SyndEntry entry = feed.getEntries().get(0);
		mappingInfo.getMappingForPaths().keySet().forEach(key -> {
			if ("/document/enclosure".equals(mappingInfo.getRawFilePathKey()) && entry.getEnclosures() != null
					&& entry.getEnclosures().size() > 0) {
				struct.addEnclosure(templateJsonObjectNode, entry, mappingInfo);
			} else {
				LOGGER.info("Ignoring " + mappingInfo.getRawFilePathKey());
			}
		});

		LOGGER.info("Nested array JSON creation completed.");
	}

	@Override
	public FileType getFuelName() {
		return FileType.RSS;
	}
}
