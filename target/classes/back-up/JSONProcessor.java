package com.avro.archieve;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

@Component
@Qualifier("xmlProcessor")
public class JSONProcessor extends ETLProcessor {

	@Override
	protected List<MappingData> loadMappings() {
		// TODO - This needs to be read from database.
		String json = "{\"owner/name\":\"$.name\",\n"
				+ "	\"owner/address/zipcode\":\"$.address.zipcode\"\n" + "}";
		List<MappingData> mappings = new ArrayList<>();
		ObjectMapper mapper = new ObjectMapper();

		try {
			JsonNode root = mapper.readTree(json);
			Iterator<String> fieldNames = root.fieldNames();

			while (fieldNames.hasNext()) {
				String fieldName = fieldNames.next();
				String jsonPath = root.get(fieldName).asText();
				mappings.add(new MappingData(fieldName, jsonPath));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mappings;
	}

	@Override
	protected GenericRecord createAvroRecord(List<MappingData> mappingData, byte[] jsonContent, Schema schema)
			throws Exception {
		// Create a GenericRecord to hold the data
		GenericRecord record = new GenericData.Record(schema);
		DocumentContext jsonContext = JsonPath.parse(new String(jsonContent, StandardCharsets.UTF_8));
		mappingData.stream().forEach(data -> {
			AvroUtils.addInRecord(record, data.getAvroField(), jsonContext.read(data.getMappingField()));
		});
		return record;
	}

}
