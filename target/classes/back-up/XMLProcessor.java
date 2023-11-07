package com.avro.archieve;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Component
@Qualifier("xmlProcessor")
public class XMLProcessor extends ETLProcessor {

	@Override
	protected GenericRecord createAvroRecord(List<MappingData> mappingData, byte[] xmlContent, Schema schema)
			throws Exception {
		// Create a GenericRecord to hold the data
		GenericRecord record = new GenericData.Record(schema);
		// Create an XML document builder
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		// Parse the XML content
		InputSource inputSource = new InputSource(new StringReader(new String(xmlContent, StandardCharsets.UTF_8)));
		Document document = builder.parse(inputSource);

		// Use XPath to extract data from the XML content
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xPath = xPathFactory.newXPath();
		mappingData.stream().forEach(data -> {
			try {
				AvroUtils.addInRecord(record, data.getAvroField(), xPath.evaluate(data.getMappingField(), document));
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
		});
		return record;
	}

	// Read mappings
	protected List<MappingData> loadMappings() {
		// TODO - This needs to be read from database.
		String json = "{\"owner/name\": \"/root/owner/name\",\n"
				+ "	\"owner/address/zipcode\": \"/root/owner/address/zipcode\"\n" + "}";

		List<MappingData> mappings = new ArrayList<>();
		// Create a Gson instance
		Gson gson = new Gson();

		// Define the Type for the Map<String, String>
		Type type = new TypeToken<Map<String, String>>() {
		}.getType();

		// Parse the JSON string into a Map<String, String>
		Map<String, String> mappingData = gson.fromJson(json, type);

		// Iterate over the map and print the key-value pairs
		for (Map.Entry<String, String> entry : mappingData.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			mappings.add(new MappingData(key, value));
		}
		return mappings;
	}

}
