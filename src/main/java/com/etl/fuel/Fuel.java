package com.etl.fuel;

import java.util.List;

import com.etl.ETLMessage;
import com.etl.MappingInfo;
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

	/**
	 * Transforms data based on the specified mapping rules and configurations.
	 *
	 * @param mappingInfos A list of {@code MappingInfo} objects containing the
	 *                     mapping rules for transforming the data.
	 * @param etlMessage   An {@code ETLMessage} containing information about the
	 *                     data to be transformed.
	 * @param template     A {@code JpaClientTemplate} instance used for processing
	 *                     and loading the transformed data.
	 * @throws Exception If an error occurs during the transformation process.
	 */
	void transform(List<MappingInfo> mappingInfos, ETLMessage etlMessage, JpaClientTemplate template) throws Exception;

	/**
	 * Retrieves the name of the fuel type associated with this object.
	 *
	 * @return A {@code String} representing the name of the fuel type.
	 */
	FileType getFuelName();
}
