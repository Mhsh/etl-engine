package com.etl;

import java.util.Map;
import java.util.Objects;

/**
 * The `MappingInfo` class represents information used for mapping between raw
 * file paths and template file paths, along with associated mapping data.
 * 
 * This class encapsulates the following properties: - `rawFilePathKey`: A key
 * that identifies the raw file path. - `templateFilePathKey`: A key that
 * identifies the template file path. - `mappingForPaths`: A map containing
 * mappings between specific keys related to paths.
 * 
 * This class provides methods for accessing and modifying these properties, as
 * well as overrides methods for proper handling of object comparison and string
 * representation.
 * 
 * Usage example: ``` MappingInfo mappingInfo = new
 * MappingInfo("rawFilePathKey");
 * mappingInfo.setTemplateFilePathKey("templateFilePathKey");
 * mappingInfo.setMappingForPaths(someMappingData); ```
 * 
 * @author Your Name
 * @version 1.0
 * @since 2023-09-10
 */
public class MappingInfo {
	/**
	 * The key that identifies the raw file path.
	 */
	private String rawFilePathKey;

	/**
	 * The key that identifies the template file path.
	 */
	private String templateFilePathKey;

	/**
	 * A map containing mappings between specific keys related to paths.
	 */
	private Map<String, String> mappingForPaths;

	/**
	 * Constructs a `MappingInfo` object with the specified raw file path key.
	 * 
	 * @param rawFilePathKey The key that identifies the raw file path.
	 */
	public MappingInfo(String rawFilePathKey) {
		this.rawFilePathKey = rawFilePathKey;
	}

	/**
	 * Gets the raw file path key.
	 * 
	 * @return The raw file path key.
	 */
	public String getRawFilePathKey() {
		return rawFilePathKey;
	}

	/**
	 * Sets the raw file path key.
	 * 
	 * @param rawFilePathKey The new raw file path key to set.
	 */
	public void setRawFilePathKey(String rawFilePathKey) {
		this.rawFilePathKey = rawFilePathKey;
	}

	/**
	 * Gets the template file path key.
	 * 
	 * @return The template file path key.
	 */
	public String getTemplateFilePathKey() {
		return templateFilePathKey;
	}

	/**
	 * Sets the template file path key.
	 * 
	 * @param templateFilePathKey The new template file path key to set.
	 */
	public void setTemplateFilePathKey(String templateFilePathKey) {
		this.templateFilePathKey = templateFilePathKey;
	}

	/**
	 * Gets the mapping for paths, which is a map containing mappings between
	 * specific keys related to paths.
	 * 
	 * @return The mapping for paths as a map.
	 */
	public Map<String, String> getMappingForPaths() {
		return mappingForPaths;
	}

	/**
	 * Sets the mapping for paths, which is a map containing mappings between
	 * specific keys related to paths.
	 * 
	 * @param mappingForPaths The new mapping for paths to set.
	 */
	public void setMappingForPaths(Map<String, String> mappingForPaths) {
		this.mappingForPaths = mappingForPaths;
	}

	/**
	 * Computes the hash code for this `MappingInfo` object based on the raw file
	 * path key.
	 * 
	 * @return The computed hash code.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(rawFilePathKey);
	}

	/**
	 * Compares this `MappingInfo` object to another object for equality.
	 * 
	 * @param obj The object to compare with.
	 * @return `true` if the objects are equal, `false` otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MappingInfo other = (MappingInfo) obj;
		return Objects.equals(rawFilePathKey, other.rawFilePathKey);
	}

	/**
	 * Returns a string representation of this `MappingInfo` object, including its
	 * properties.
	 * 
	 * @return A string representation of the object.
	 */
	@Override
	public String toString() {
		return "MappingInfo [rawFilePathKey=" + rawFilePathKey + ", templateFilePathKey=" + templateFilePathKey
				+ ", mappingForPaths=" + mappingForPaths + "]";
	}
}
