package com.etl;

import java.io.FileInputStream;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rometools.rome.feed.module.DCModuleImpl;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

@Component
public class RSSStruct {

	private Map<String, Function<SyndEntry, String>> itemFieldMapping = new HashMap<>();
	private Map<String, Function<SyndFeed, String>> feedFieldMapping = new HashMap<>();
	private Map<String, Function<DCModuleImpl, String>> moduleMapping = new HashMap<>();
	private String DUBLIN_SCHEMA_URL = "http://purl.org/dc/elements/1.1/";

	@Autowired
	private ObjectMapper objectMapper;

	public RSSStruct() {
		populateFileInformation();
		populateDocumentInformation();
		populateDcInformation();
	}

	private void populateFileInformation() {
		feedFieldMapping.put("/file/title", SyndFeed::getTitle);
		feedFieldMapping.put("/file/sourceLink", SyndFeed::getLink);
		feedFieldMapping.put("/file/description", SyndFeed::getDescription);
		feedFieldMapping.put("/file/language",
				feed -> feed.getLanguage() != null ? feed.getLanguage() : getDcModuleValue("/file/language", feed));
		feedFieldMapping.put("/file/publicationDate", feed -> feed.getPublishedDate() != null
				? feed.getPublishedDate().toInstant().toString()
				: getPrismValue("publicationDate", feed) != null ? getPrismValue("publicationDate", feed) : null);
		feedFieldMapping.put("/file/generator", SyndFeed::getGenerator);
		feedFieldMapping.put("/file/creator", SyndFeed::getManagingEditor);
		feedFieldMapping.put("/file/webMaster", SyndFeed::getWebMaster);
		feedFieldMapping.put("/file/source", feed -> getDcModuleValue("/file/source", feed));
		feedFieldMapping.put("/file/copyright",
				feed -> getDcModuleValue("/file/rights", feed) != null ? getDcModuleValue("/file/rights", feed)
						: getPrismValue("copyright", feed));
		feedFieldMapping.put("/file/date", feed -> getDcModuleValue("/file/date", feed));
		feedFieldMapping.put("/file/publicationName", feed -> getPrismValue("publicationName", feed));
		feedFieldMapping.put("/file/publicationIssn", feed -> getPrismValue("issn", feed));
		feedFieldMapping.put("/file/rightsAgent", feed -> getPrismValue("rightsAgent", feed));
		feedFieldMapping.put("/file/id", SyndFeed::getUri);
	}

	private void populateDocumentInformation() {
		itemFieldMapping.put("/document/title", entry -> {
			return entry.getTitle() != null ? entry.getTitle() : getDcModuleValue("/document/title", entry);
		});
		itemFieldMapping.put("/document/sourceLink", entry -> {
			return entry.getLink() != null ? entry.getLink()
					: getPrismValue("/document/url", entry) != null ? getPrismValue("/document/url", entry)
							: entry.getSource() != null ? entry.getSource().getUri() : null;
		});
		itemFieldMapping.put("/document/description",
				entry -> entry.getDescription() != null ? entry.getDescription().getValue() : null);
		itemFieldMapping.put("/document/id",
				entry -> entry.getUri() != null ? entry.getUri() : getDcModuleValue("/document/id", entry));
		itemFieldMapping.put("/document/publicationDate",
				entry -> entry.getPublishedDate() != null ? entry.getPublishedDate().toInstant()
						.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null);
		itemFieldMapping.put("/document/creator",
				entry -> entry.getAuthor() != null ? entry.getAuthor()
						: getDcModuleValue("/document/creator", entry) != null
								? getDcModuleValue("/document/creator", entry)
								: getDcModuleValue("/document/source", entry));
		itemFieldMapping.put("/document/source", entry -> entry.getSource() != null ? entry.getSource().getTitle()
				: getDcModuleValue("/document/publisher", entry));
		itemFieldMapping.put("/document/body",
				entry -> entry.getContents() != null && entry.getContents().isEmpty() ? null
						: entry.getContents().get(0).getValue());
		itemFieldMapping.put("/document/language", entry -> getDcModuleValue("/document/language", entry));
		itemFieldMapping.put("/document/rights",
				entry -> getDcModuleValue("/document/rights", entry) != null
						? getDcModuleValue("/document/rights", entry)
						: getPrismValue("copyright", entry));
		itemFieldMapping.put("/document/publicationDate",
				entry -> getDcModuleValue("/document/publicationDate", entry) != null
						? getDcModuleValue("/document/publicationDate", entry)
						: getPrismValue("publicationDate", entry));
		itemFieldMapping.put("/document/publicationSection", entry -> getPrismValue("section", entry));
		itemFieldMapping.put("/document/publicationVolume", entry -> getPrismValue("volume", entry));
		itemFieldMapping.put("/document/publicationNumber", entry -> getPrismValue("number", entry));
		itemFieldMapping.put("/document/publicationStartingPage", entry -> getPrismValue("startingPage", entry));
		itemFieldMapping.put("/document/publicationEndingPage", entry -> getPrismValue("endingPage", entry));
		itemFieldMapping.put("/document/publicationDoi", entry -> getPrismValue("doi", entry));
		itemFieldMapping.put("/document/publicationCoverDate", entry -> getPrismValue("coverDate", entry));
		itemFieldMapping.put("/document/publicationCoverDisplayDate",
				entry -> getPrismValue("coverDisplayDate", entry));
	}

	private void populateDcInformation() {
		moduleMapping.put("/document/title", DCModuleImpl::getTitle);
		moduleMapping.put("/document/id", DCModuleImpl::getIdentifier);
		moduleMapping.put("/document/rights", DCModuleImpl::getRights);
		moduleMapping.put("/document/creator", DCModuleImpl::getCreator);
		moduleMapping.put("/document/source", DCModuleImpl::getSource);
		moduleMapping.put("/document/publisher", DCModuleImpl::getPublisher);
		moduleMapping.put("/document/publicationDate", entry -> entry.getDate().toInstant().atOffset(ZoneOffset.UTC)
				.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		moduleMapping.put("/file/source", DCModuleImpl::getPublisher);
		moduleMapping.put("/document/language", DCModuleImpl::getLanguage);
		moduleMapping.put("/file/rights", DCModuleImpl::getRights);
		moduleMapping.put("/file/language", DCModuleImpl::getLanguage);
		moduleMapping.put("/file/date",
				module -> module.getDate() != null ? module.getDate().toInstant().toString() : null);
	}

	/**
	 * 
	 * @param key
	 * @param entry
	 * @return
	 */
	private String getPrismValue(String key, SyndEntry entry) {
		return entry.getForeignMarkup().stream()
				.filter(element -> "prism".equals(element.getNamespace().getPrefix()) && key.equals(element.getName()))
				.map(element -> element.getValue()).findFirst().orElse(null);
	}

	private String getPrismValue(String key, SyndFeed feed) {
		return feed.getForeignMarkup().stream()
				.filter(element -> "prism".equals(element.getNamespace().getPrefix()) && key.equals(element.getName()))
				.map(element -> element.getValue()).findFirst().orElse(null);
	}

	public String getDcModuleValue(String key, SyndEntry entry) {
		Module dcModule = entry.getModule(DUBLIN_SCHEMA_URL);
		if (dcModule instanceof DCModuleImpl) {
			return moduleMapping.get(key).apply((DCModuleImpl) dcModule);
		}
		return null;

	}

	public String getDcModuleValue(String key, SyndFeed feed) {
		Module dcModule = feed.getModule(DUBLIN_SCHEMA_URL);
		if (dcModule instanceof DCModuleImpl) {
			return moduleMapping.get(key).apply((DCModuleImpl) dcModule);
		}
		return null;

	}

	public void addCategory(SyndEntry entry, String key, JsonNode templateJsonObjectNode) {
		if (entry.getCategories() != null) {
			ArrayNode templateNewObject = objectMapper.createArrayNode();
			entry.getCategories().stream().forEach(category -> {
				templateNewObject.add(category.getName());
			});
			((ObjectNode) templateJsonObjectNode).putArray(key).addAll(templateNewObject);
		} else {
			Module dcModule = entry.getModule(DUBLIN_SCHEMA_URL);
			if (dcModule instanceof DCModuleImpl) {
				DCModuleImpl moduleImpl = (DCModuleImpl) dcModule;
				if (moduleImpl.getSubjects() != null) {
					ArrayNode templateNewObject = objectMapper.createArrayNode();
					moduleImpl.getSubjects().stream().forEach(category -> {
						templateNewObject.add(category.getValue());
					});
					((ObjectNode) templateJsonObjectNode).putArray(key).addAll(templateNewObject);
				}
			}
		}
	}

	public String getValue(String internalFilePath, SyndFeed feed) {
		String value = null;
		if (internalFilePath.startsWith("/file")) {
			value = feedFieldMapping.get(internalFilePath).apply(feed);
		} else if (internalFilePath.contains("/dc/")) {
			value = getDcModuleValue(internalFilePath, feed.getEntries().get(0));
		} else {
			value = itemFieldMapping.get(internalFilePath).apply(feed.getEntries().get(0));
		}
		return value;
	}

	public void addEnclosure(ArrayNode templateJsonObjectNode, SyndEntry entry, MappingInfo mappingInfo) {
		templateJsonObjectNode.removeAll();
		for (SyndEnclosure enclosuer : entry.getEnclosures()) {
			ObjectNode templateNewObject = objectMapper.createObjectNode();
			mappingInfo.getMappingForPaths().keySet().stream().forEach(key -> {
				if ("length".equals(key)) {
					templateNewObject.put(mappingInfo.getMappingForPaths().get(key), enclosuer.getLength());
				} else if ("type".equals(key)) {
					templateNewObject.put(mappingInfo.getMappingForPaths().get(key), enclosuer.getType());
				} else if ("url".equals(key)) {
					templateNewObject.put(mappingInfo.getMappingForPaths().get(key), enclosuer.getUrl());
				}
			});
			((ArrayNode) templateJsonObjectNode).add(templateNewObject);
		}
	}

	public static void main(String[] args) throws Exception {
		RSSStruct struct = new RSSStruct();
		FileInputStream inputStream = new FileInputStream("src/main/resources/sample-rss.xml");
		// Create a SyndFeedInput to read the feed
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = input.build(new XmlReader(inputStream));
		struct.itemFieldMapping.keySet().forEach(key -> {
			System.out.println(key + "--->" + struct.itemFieldMapping.get(key).apply(feed.getEntries().get(0)));
		});
		struct.feedFieldMapping.keySet().forEach(key -> {
			System.out.println(key + "--->" + struct.feedFieldMapping.get(key).apply(feed));
		});

	}

}
