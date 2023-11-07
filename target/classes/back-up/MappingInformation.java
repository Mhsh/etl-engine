package com.avro.archieve;

import java.util.Map;

/**
 * Represents mapping information that associates a source key with path
 * mappings and a data type. This class is typically used to define mappings
 * between data source keys, their corresponding paths, and the data type
 * associated with the mapping.
 * The pathMappings contains the list one to one relationship between the input key and 
 * raw path key. 
 * The source key is the path excluding last word so that we know where to look for value.
 */
public class MappingInformation {

	/**
	 * A map that associates path mappings with keys. Each entry in the map
	 * represents a path mapping where the key is the path, and the value is the
	 * mapping value.
	 */
	private Map<String, String> pathMappings;

	/**
	 * The source key, which is a unique identifier for the mapping information.
	 */
	private String sourceKey;

	/**
	 * The data type associated with the mapping information.
	 */
	private String type;

	/**
	 * Gets the path mappings associated with this mapping information.
	 *
	 * @return A map of path mappings where the key is the path, and the value is
	 *         the mapping value.
	 */
	public Map<String, String> getPathMappings() {
		return pathMappings;
	}

	/**
	 * Sets the path mappings associated with this mapping information.
	 *
	 * @param pathMappings A map of path mappings where the key is the path, and the
	 *                     value is the mapping value.
	 */
	public void setPathMappings(Map<String, String> pathMappings) {
		this.pathMappings = pathMappings;
	}

	/**
	 * Gets the source key, which is a unique identifier for the mapping
	 * information.
	 *
	 * @return The source key.
	 */
	public String getSourceKey() {
		return sourceKey;
	}

	/**
	 * Sets the source key, which is a unique identifier for the mapping
	 * information.
	 *
	 * @param sourceKey The source key to set.
	 */
	public void setSourceKey(String sourceKey) {
		this.sourceKey = sourceKey;
	}

	/**
	 * Gets the data type associated with the mapping information.
	 *
	 * @return The data type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the data type associated with the mapping information.
	 *
	 * @param type The data type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}
}
