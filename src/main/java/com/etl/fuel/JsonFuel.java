package com.etl.fuel;

import org.springframework.stereotype.Component;

import com.etl.MappingInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.storage.jpa.Enums.FileType;

@Component
public class JsonFuel implements Fuel {

	@Override
	public void handleDirectObjectJsonCreation(MappingInfo mappingInfo, Delegate delegate,
			JsonNode templateJsonFileNode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleNestedArrayObjects(MappingInfo mappingInfo, Delegate delegate, ArrayNode templateJsonObjectNode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleNestedObjectJsonCreation(MappingInfo mappingInfo, Delegate delegate,
			JsonNode templateJsonObjectNode) {
		// TODO Auto-generated method stub

	}

	@Override
	public FileType getFuelName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Delegate getDelegate() {
		// TODO Auto-generated method stub
		return null;
	}
}
