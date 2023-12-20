package com.etl.jms;

import java.util.UUID;

public class VectorMessage {

	private UUID id;

	private String transformedFilePath;

	private String clientId;

	/**
	 * @return the id
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(UUID id) {
		this.id = id;
	}

	/**
	 * @return the transformedFilePath
	 */
	public String getTransformedFilePath() {
		return transformedFilePath;
	}

	/**
	 * @param transformedFilePath the transformedFilePath to set
	 */
	public void setTransformedFilePath(String transformedFilePath) {
		this.transformedFilePath = transformedFilePath;
	}

	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

}
