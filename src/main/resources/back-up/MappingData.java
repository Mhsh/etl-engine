package com.avro.archieve;

public class MappingData {
	
	private String avroField;

	private String mappingField;

	public MappingData(String fieldName, String jsonPath) {
		this.avroField = fieldName;
		this.mappingField = jsonPath;
	}

	public String getAvroField() {
		return avroField;
	}

	public String getMappingField() {
		return mappingField;
	}

}
