package com.etl.search.index;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import com.storage.jpa.JpaTemplate;
import com.storage.repository.JpaTemplateRepository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.transport.endpoints.BooleanResponse;

@Service
public class SearchIndexer {

	private static String indexName = "document-store";

	@Autowired
	private ElasticsearchClient elasticsearchClient;

	private static final Logger LOGGER = Logger.getLogger(SearchIndexer.class.getName());

	@Autowired
	private JpaTemplateRepository templateRepository;

	@Autowired
	private StreamBridge streamBridge;

	public void index(UUID id, String filePath) {
		try {
			FileReader file = new FileReader(filePath);
			LOGGER.info("Adding document to index.");
			IndexRequest<JsonData> req = IndexRequest.of(b -> b.index(indexName).id(id.toString()).withJson(file));
			elasticsearchClient.index(req);
			LOGGER.info("Index added ");
			streamBridge.send("vector-exchange", convertToVector(id, filePath));
			LOGGER.info("Message sent to vector queue");

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to transform document with id " + id + " and path " + filePath);
		}
	}

	// @PostConstruct
	void createIndex() throws ElasticsearchException, IOException {
		// Index name and mapping JSON
		LOGGER.info("Start creating index with name " + indexName);
		JpaTemplate template = templateRepository.findById("global").orElseThrow(EntityNotFoundException::new);
		CreateIndexRequest req = CreateIndexRequest.of(b -> b.index(indexName)
				.withJson(new ByteArrayInputStream(template.getTemplate().getBytes(StandardCharsets.UTF_8))));
		BooleanResponse indexExists = elasticsearchClient.indices().exists(ExistsRequest.of(b -> b.index(indexName)));
		if (indexExists.value()) {
			LOGGER.info("Index " + indexName + " already exists.");
		} else
			elasticsearchClient.indices().create(req).acknowledged();
	}

	protected ETLVector convertToVector(UUID id, String jsonFilePath) {
		ETLVector etlVector = new ETLVector();
		etlVector.setIdentifier(id);
		etlVector.setJsonFilePath(jsonFilePath);
		return etlVector;
	}

}
