package com.avro.archieve;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonToAvroConverter {

	public static void main(String[] args) throws IOException {
		String json = "[\n" + "    {\n" + "        \"key\": \"username\",\n" + "        \"type\": \"STRING\"\n"
				+ "    },\n" + "    {\n" + "        \"key\": \"realname\",\n" + "        \"type\": \"STRING\"\n"
				+ "    },\n" + "    {\n" + "        \"key\": \"user\",\n" + "        \"type\": \"array\",\n"
				+ "        \"fields\": [\n" + "            {\n" + "                \"maininterest\": {\n"
				+ "                    \"interest\": \"Golf\"\n" + "                },\n"
				+ "                \"hobby\": \"Guitar\"\n" + "            }\n" + "        ]\n" + "    }\n" + "]";

		Schema schema = generateAvroSchema(json);
		List<GenericRecord> records = convertToJsonToAvro(json, schema);

		System.out.println("Generated Avro records:");
		for (GenericRecord record : records) {
			System.out.println(record.toString());
		}
	}

	private static Schema generateAvroSchema(String json) {
		SchemaBuilder.FieldAssembler<Schema> fields = SchemaBuilder.record("JsonToAvroRecord").fields();

		JSONArray jsonArray = new JSONArray(json);
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject fieldObject = jsonArray.getJSONObject(i);
			String fieldName = fieldObject.getString("key");
			String fieldType = fieldObject.getString("type");

			if ("STRING".equalsIgnoreCase(fieldType)) {
				fields = fields.name(fieldName).type().stringType().noDefault();
			} else if ("array".equalsIgnoreCase(fieldType)) {
				// Handle array type
				JSONArray arrayFields = fieldObject.getJSONArray("fields");
				Schema arraySchema = generateArraySchema(arrayFields);
				fields = fields.name(fieldName).type().array().items(arraySchema).noDefault();
			}
			// Handle other types if needed
		}

		return fields.endRecord();
	}

	private static Schema generateArraySchema(JSONArray arrayFields) {
		SchemaBuilder.FieldAssembler<Schema> fields = SchemaBuilder.array().items().record("ArrayRecord").fields();

		JSONObject arrayFieldObject = arrayFields.getJSONObject(0);
		JSONObject mainInterest = arrayFieldObject.getJSONObject("maininterest");
		String mainInterestValue = mainInterest.getString("interest");

		fields = fields.name("maininterest").type().stringType().noDefault();
		fields = fields.name("hobby").type().stringType().noDefault();

		return fields.endRecord();
	}

	private static List<GenericRecord> convertToJsonToAvro(String json, Schema schema) throws IOException {
		List<GenericRecord> records = new ArrayList<>();
		JSONArray jsonArray = new JSONArray(json);

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			GenericRecord record = jsonObjectToRecord(jsonObject, schema);
			records.add(record);
		}

		return records;
	}

	private static GenericRecord jsonObjectToRecord(JSONObject jsonObject, Schema schema) {
		GenericRecord genericRecord = new GenericData.Record(schema);

		for (Schema.Field field : schema.getFields()) {
			String fieldName = field.name();
			if (jsonObject.has(fieldName)) {
				Object fieldValue = jsonObject.get(fieldName);

				// Convert JSON value to Avro-compatible value
				Object avroValue = convertToAvroValue(field.schema(), fieldValue);
				genericRecord.put(fieldName, avroValue);
			}
		}

		return genericRecord;
	}

	private static Object convertToAvroValue(Schema schema, Object jsonValue) {
		switch (schema.getType()) {
		case STRING:
			return jsonValue.toString();
		case INT:
			return Integer.parseInt(jsonValue.toString());
		case BOOLEAN:
			return Boolean.parseBoolean(jsonValue.toString());
		// Handle array conversion if needed
		default:
			return null; // Handle unsupported types gracefully
		}
	}
}
