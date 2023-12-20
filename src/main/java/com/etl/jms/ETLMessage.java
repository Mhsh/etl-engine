package com.etl.jms;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.storage.jpa.Enums.ConnectorType;
import com.storage.jpa.Enums.FileType;

/**
 * The ETLMessage class represents a message used in an Extract, Transform, Load
 * (ETL) process. It contains information about a subscription ID and the raw
 * file path associated with the message.
 */
public class ETLMessage {

	/**
	 * The subscription ID associated with the ETL message.
	 */
	private UUID subscriptionId;

	/**
	 * The file path of the transformed data file.
	 */
	private String rawFilePath;

	/**
	 * The client id for subscription.
	 */
	private String clientId;

	/**
	 * The file type to be handled.
	 */
	private FileType fileType;

	private Map<String, String> internalData = new HashMap<>();

	private UUID subscriptionDetailId;

	private ConnectorType connectorType;

	private UUID identifier;

	/**
	 * Gets the subscription ID associated with the ETL message.
	 *
	 * @return The subscription ID.
	 */
	public UUID getSubscriptionId() {
		return subscriptionId;
	}

	/**
	 * Sets the subscription ID for the ETL message.
	 *
	 * @param subscriptionId The subscription ID to set.
	 */
	public void setSubscriptionId(UUID subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	/**
	 * Gets the file path of the raw data file.
	 *
	 * @return The raw file path.
	 */
	public String getRawFilePath() {
		return rawFilePath;
	}

	/**
	 * Sets the file path for the raw data file.
	 *
	 * @param rawFilePath The raw file path to set.
	 */
	public void setRawFilePath(String rawFilePath) {
		this.rawFilePath = rawFilePath;
	}

	/**
	 * Gets the client id for subscription.
	 *
	 * @return The client id.
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * Sets the client id for subscription.
	 *
	 * @param clientId The raw file path to set.
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return the fileType
	 */
	public FileType getFileType() {
		return fileType;
	}

	/**
	 * @param fileType the fileType to set
	 */
	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	/**
	 * @return the internalData
	 */
	public Map<String, String> getInternalData() {
		return internalData;
	}

	/**
	 * @param internalData the internalData to set
	 */
	public void setInternalData(Map<String, String> internalData) {
		this.internalData = internalData;
	}

	/**
	 * @return the subscriptionDetailId
	 */
	public UUID getSubscriptionDetailId() {
		return subscriptionDetailId;
	}

	/**
	 * @param subscriptionDetailId the subscriptionDetailId to set
	 */
	public void setSubscriptionDetailId(UUID subscriptionDetailId) {
		this.subscriptionDetailId = subscriptionDetailId;
	}

	/**
	 * @return the connectorType
	 */
	public ConnectorType getConnectorType() {
		return connectorType;
	}

	/**
	 * @param connectorType the connectorType to set
	 */
	public void setConnectorType(ConnectorType connectorType) {
		this.connectorType = connectorType;
	}

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

	@Override
	public String toString() {
		return "ETLMessage [subscriptionId=" + subscriptionId + ", rawFilePath=" + rawFilePath + ", clientId="
				+ clientId + ", fileType=" + fileType + ", internalData=" + internalData + ", subscriptionDetailId="
				+ subscriptionDetailId + ", connectorType=" + connectorType + ", identifier=" + identifier + "]";
	}

}
