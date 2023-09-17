package com.avro.archieve;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

public class AvroUtils {

	public static void addInRecord(GenericRecord record, String path, Object value) {
		String[] segments = path.split("/");

		for (int i = 0; i < segments.length - 1; i++) {
			String fieldName = segments[i];
			Schema.Field field = record.getSchema().getField(fieldName);
			if (field == null) {
				throw new IllegalArgumentException("Field not found: " + fieldName);
			}

			Object fieldValue = record.get(fieldName);
			if (fieldValue == null) {
				fieldValue = new GenericData.Record(field.schema());
				record.put(fieldName, fieldValue);
			}

			if (!(fieldValue instanceof GenericRecord)) {
				throw new IllegalArgumentException("Field is not a record: " + fieldName);
			}

			record = (GenericRecord) fieldValue;
		}

		String leafFieldName = segments[segments.length - 1];
		record.put(leafFieldName, value);
	}

}
