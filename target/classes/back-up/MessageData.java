package com.avro.archieve;

import com.google.gson.Gson;

public class MessageData {

	private String fileName;
	private String providerId;
	private String type;
	private String rawFilePath;
	// Constructors, getters, and setters

	public String toJsonString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public static MessageData fromJsonString(String jsonString) {
		Gson gson = new Gson();
		return gson.fromJson(jsonString, MessageData.class);
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRawFilePath() {
		return rawFilePath;
	}

	public void setRawFilePath(String rawFilePath) {
		this.rawFilePath = rawFilePath;
	}

}
