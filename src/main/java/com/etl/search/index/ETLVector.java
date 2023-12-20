package com.etl.search.index;

import java.util.UUID;

public class ETLVector {

	private UUID identifier;

	private String jsonFilePath;

	/**
	 * @return the identifier
	 */
	public UUID getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(UUID identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the jsonFilePath
	 */
	public String getJsonFilePath() {
		return jsonFilePath;
	}

	/**
	 * @param jsonFilePath the jsonFilePath to set
	 */
	public void setJsonFilePath(String jsonFilePath) {
		this.jsonFilePath = jsonFilePath;
	}

}
