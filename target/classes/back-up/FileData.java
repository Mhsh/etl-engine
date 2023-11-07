package com.avro.archieve;

import com.google.gson.Gson;

public class FileData {

	private String fileName;
	private String id;
	private String url;
	private String type;
	private String extraConfiguration;
	private String provider;
	
	// Constructors, getters, and setters

	public String toJsonString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public static FileData fromJsonString(String jsonString) {
		Gson gson = new Gson();
		return gson.fromJson(jsonString, FileData.class);
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getExtraConfiguration() {
		return extraConfiguration;
	}

	public void setExtraConfiguration(String extraConfiguration) {
		this.extraConfiguration = extraConfiguration;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

}