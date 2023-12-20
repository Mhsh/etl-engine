package com.etl.fuel;

import com.etl.MappingInfo;
import com.etl.jms.ETLMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.storage.jpa.Enums.FileType;
import com.storage.jpa.JpaClientTemplate;

/**
 * The {@code Fuel} interface represents a component responsible for
 * transforming data using a set of mapping rules and configurations.
 * Implementations of this interface define the specific logic for data
 * transformation.
 *
 * <p>
 * The transformation process typically involves mapping data fields from a
 * source format to a target format as specified by a list of
 * {@code MappingInfo} objects. The transformed data is then processed and
 * loaded using a provided {@code JpaClientTemplate} instance.
 *
 * <p>
 * Implementors of this interface should provide custom logic to handle data
 * transformation for a particular use case or domain.
 *
 * @see MappingInfo
 * @see ETLMessage
 * @see JpaClientTemplate
 */
public interface Fuel {

	void handleDirectObjectJsonCreation(MappingInfo mappingInfo, Delegate delegate, JsonNode templateJsonFileNode);

	void handleNestedArrayObjects(MappingInfo mappingInfo, Delegate delegate, ArrayNode templateJsonObjectNode);

	void handleNestedObjectJsonCreation(MappingInfo mappingInfo, Delegate delegate, JsonNode templateJsonObjectNode);

	/**
	 * Retrieves the name of the fuel type associated with this object.
	 *
	 * @return A {@code String} representing the name of the fuel type.
	 */
	FileType getFuelName();

	Delegate getDelegate();

}
