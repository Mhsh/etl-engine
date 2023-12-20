package com.etl.fuel;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.etl.MappingInfo;
import com.etl.RSSStruct;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rometools.rome.feed.synd.SyndEntry;
import com.storage.jpa.Enums.FileType;

@Component
public class RSSFuel implements Fuel {

	@Autowired
	private RSSStruct struct;

	private static final Logger LOGGER = Logger.getLogger(RSSFuel.class.getName());

	@Override
	public Delegate getDelegate() {
		return new RSSDelegate();
	}

	@Override
	public void handleDirectObjectJsonCreation(MappingInfo mappingInfo, Delegate delegate,
			JsonNode templateJsonFileNode) {
		LOGGER.info("Handling direct object JSON creation... Parameters: mappingInfo=" + mappingInfo);
		// LOGGER.info("processing for key " + mappingInfo.getRawFilePathKey());
		String templateFilePathKey = mappingInfo.getTemplateFilePathKey();
		int lastIndexOfRawFilePath = templateFilePathKey.lastIndexOf("/");
		String precedingRawFilePath = lastIndexOfRawFilePath != -1
				? templateFilePathKey.substring(0, templateFilePathKey.lastIndexOf("/"))
				: templateFilePathKey;
		String lastWordTemplateFilePath = templateFilePathKey.substring(templateFilePathKey.lastIndexOf("/") + 1);
		JsonNode templateJsonObjectNode = templateJsonFileNode.at(precedingRawFilePath);
		((com.fasterxml.jackson.databind.node.ObjectNode) templateJsonObjectNode).put(lastWordTemplateFilePath,
				struct.getValue(mappingInfo.getRawFilePathKey(), ((RSSDelegate) delegate).getFeed()));
		LOGGER.info("Direct object JSON creation completed.");
	}

	@Override
	public void handleNestedObjectJsonCreation(MappingInfo mappingInfo, Delegate delegate,
			JsonNode templateJsonObjectNode) {
		LOGGER.fine("Handling nested object JSON creation... Parameters: mappingInfo=" + mappingInfo + ", feed="
				+ ((RSSDelegate) delegate).getFeed() + ", templateJsonObjectNode=" + templateJsonObjectNode);
		mappingInfo.getMappingForPaths().keySet().forEach(key -> {
			LOGGER.info("processing for key " + mappingInfo.getRawFilePathKey() + "/" + key);
			String internalFilePath = mappingInfo.getRawFilePathKey() + "/" + key;
			SyndEntry entry = ((RSSDelegate) delegate).getFeed().getEntries().get(0);
			if ("/document/category".equals(internalFilePath)) {
				struct.addCategory(entry, mappingInfo.getMappingForPaths().get(key), templateJsonObjectNode);
			} else {
				((ObjectNode) templateJsonObjectNode).put(mappingInfo.getMappingForPaths().get(key),
						struct.getValue(internalFilePath, ((RSSDelegate) delegate).getFeed()));
			}
		});
		if (LOGGER.isLoggable(java.util.logging.Level.FINE)) {
			LOGGER.fine("Nested object JSON creation completed.");
		}
	}

	@Override
	public void handleNestedArrayObjects(MappingInfo mappingInfo, Delegate delegate, ArrayNode templateJsonObjectNode) {
		LOGGER.fine("Handling nested array JSON creation... Parameters: mappingInfo=" + mappingInfo
				+ ", rawFileJsonFilePatfeedhNode=" + ((RSSDelegate) delegate).getFeed() + ", templateJsonObjectNode="
				+ templateJsonObjectNode);
		LOGGER.info("processing for key " + mappingInfo.getRawFilePathKey());
		SyndEntry entry = ((RSSDelegate) delegate).getFeed().getEntries().get(0);
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
