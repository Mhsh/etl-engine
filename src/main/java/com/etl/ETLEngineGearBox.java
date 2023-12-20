package com.etl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import com.etl.fuel.Delegate;
import com.etl.fuel.Fuel;
import com.etl.fuel.FuelPump;
import com.etl.jms.ETLMessage;
import com.etl.search.index.SearchIndexer;
import com.etl.utils.FileUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.storage.jpa.Enums.FileType;
import com.storage.jpa.JpaClientTemplate;
import com.storage.jpa.JpaETLFile;
import com.storage.jpa.JpaEtlErrorDetail;
import com.storage.repository.JpaClientTemplateRepository;
import com.storage.repository.JpaETLErrorDetailRepository;
import com.storage.repository.JpaETLFileRepository;
import com.storage.repository.JpaSubscriptionDetailRepository;
import com.storage.repository.JpaSubscriptionRepository;

@Component
public class ETLEngineGearBox {

	@Autowired
	private MagicalMappingExtractor mappingExtractor;

	@Autowired
	private JpaClientTemplateRepository clientTemplateRepository;

	@Autowired
	private JpaETLFileRepository etlFileRepository;

	@Autowired
	private JpaSubscriptionDetailRepository subscriptionDetailRepository;

	@Autowired
	private JpaSubscriptionRepository subscriptionRepository;

	@Autowired
	private JpaETLErrorDetailRepository etlErrorDetailRepository;

	@Autowired
	private FuelPump fuelPump;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	protected StreamBridge streamBridge;

	private String internalRoot = "/internal";

	@Value("${etl-engine.output-file-path}")
	private String outputFilePath;

	@Autowired
	private SearchIndexer searchIndexer;

	private static final Logger LOGGER = Logger.getLogger(ETLEngineGearBox.class.getName());

	public void startTransformation(ETLMessage etlMessage) {
		try {
			String filePath = transform(etlMessage);
			etlFileRepository.save(convertToETLFile(etlMessage));
			searchIndexer.index(etlMessage.getIdentifier(), filePath);
		} catch (Exception e) {
			etlErrorDetailRepository.save(convertToErrorDetail(etlMessage, e));
			throw new RuntimeException("Failed for transformation. Reason - " + e.getLocalizedMessage());
		}
	}

	public String transform(ETLMessage etlMessage) throws Exception {
		JpaClientTemplate template = clientTemplateRepository.findByClient_Id(etlMessage.getClientId());
		if (template == null) {
			throw new RuntimeException("Template is not configured for client " + etlMessage.getClientId()
					+ ". Please contact administrator.");
		}
		List<MappingInfo> mappingInfos = mappingExtractor.createMagicalMappings(etlMessage);
		Fuel fuel = getFuel.apply(etlMessage.getFileType());
		if (fuel == null) {
			throw new RuntimeException("Unhandled type. " + etlMessage.getFileType()
					+ ". No code present to handle this file type. Please contact administrator.");
		}
		Delegate delegate = fuel.getDelegate();
		delegate.intialiseDelegate(etlMessage);
		JsonNode templateJsonFileNode = objectMapper.readTree(template.getTemplate());
		populateInternalData(templateJsonFileNode, etlMessage.getInternalData());
		// The below mapping info contains the
		// 1. raw json key with last word emitted
		// 2. template key with last word excluded.
		// 3. map that contains the mapping paths of raw and template.
		// e.g:
		// MappingInfo [rawFilePathKey=/ClassificationInfo,
		// templateFilePathKey=/regulatory/regulatoryClassification,
		// mappingForPaths={RegionMarket=regionOrMarket, ProductType=productType,
		// RegulatoryClass=regulatoryClass}]

		// another example if it is not json array. In this case mappings will be empty
		// and it is not nested array.:
		// MappingInfo [rawFilePathKey=DocumentID,
		// templateFilePathKey=/regulatory/metadata/documentID, mappingForPaths={}],
		LOGGER.info(
				"Transforming JSON data... Parameters: mappingInfos=" + mappingInfos + ", etlMessage=" + etlMessage);
		mappingInfos.stream().forEach(mappingInfo -> {
			if (mappingInfo.getMappingForPaths().isEmpty()) {
				fuel.handleDirectObjectJsonCreation(mappingInfo, delegate, templateJsonFileNode);
			} else {
				JsonNode templateJsonObjectNode = templateJsonFileNode.at(mappingInfo.getTemplateFilePathKey());
				if (templateJsonObjectNode instanceof MissingNode) {
					throw new RuntimeException("Path not found in json for " + mappingInfo.getTemplateFilePathKey());
				} else if (templateJsonObjectNode instanceof ArrayNode) {
					fuel.handleNestedArrayObjects(mappingInfo, delegate, (ArrayNode) templateJsonObjectNode);
				} else {
					fuel.handleNestedObjectJsonCreation(mappingInfo, delegate, templateJsonObjectNode);
				}
			}
		});
		String filePath = saveFile(templateJsonFileNode, etlMessage.getRawFilePath());
		LOGGER.info("Transformation complete.");
		return filePath;
	}

	public String saveFile(JsonNode templateJsonFileNode, String filePath) {
		String outputPath = outputFilePath + "/" + FileUtils.getFileName(filePath) + ".json";
		LOGGER.info(String.format("Saved file location  - : %s ", outputPath));
		File file = new File(outputPath);
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		try (FileWriter fileWriter = new FileWriter(outputPath);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
			// Write the content to the file
			bufferedWriter
					.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(templateJsonFileNode));
		} catch (IOException e) {
			throw new RuntimeException("Failed to save file. Reason - " + e.getLocalizedMessage());
		}
		return outputPath;
	}

	private void populateInternalData(JsonNode templateJsonFileNode, Map<String, String> internalMap) {
		JsonNode templateJsonObjectNode = templateJsonFileNode.at(internalRoot);
		internalMap.keySet().stream().forEach(internalKey -> {
			((ObjectNode) templateJsonObjectNode).put(internalKey, internalMap.get(internalKey));
		});
	}

	Function<FileType, Fuel> getFuel = (fuelType) -> fuelPump.getHandler(fuelType);

	private JpaETLFile convertToETLFile(ETLMessage etlMessage) {
		JpaETLFile etlFile = new JpaETLFile();
		etlFile.setClientId(etlMessage.getClientId());
		etlFile.setId(etlMessage.getSubscriptionId());
		etlFile.setRawFilePath(etlMessage.getRawFilePath());
		etlFile.setSubscriptionDetail(
				subscriptionDetailRepository.findById(etlMessage.getSubscriptionDetailId()).orElse(null));
		etlFile.setSubscription(subscriptionRepository.findById(etlMessage.getSubscriptionId()).orElse(null));
		etlFile.setFileType(etlMessage.getFileType());
		etlFile.setConnectorType(etlMessage.getConnectorType());
		return etlFile;
	}

	private JpaEtlErrorDetail convertToErrorDetail(ETLMessage etlMessage, Exception e) {
		JpaEtlErrorDetail errorDetail = new JpaEtlErrorDetail();
		String newErrorDetail = "Message - " + e.getLocalizedMessage() + ". Cause - " + e.getCause();
		String newErrorCode = e.getClass().getSimpleName();
		errorDetail.setConnectorType(null);
		errorDetail.setErrorDetail(newErrorDetail);
		errorDetail.setFileType(etlMessage.getFileType());
		errorDetail.setSubscriptionDetail(
				subscriptionDetailRepository.findById(etlMessage.getSubscriptionDetailId()).orElse(null));
		errorDetail.setSubscription(subscriptionRepository.findById(etlMessage.getSubscriptionId()).orElse(null));
		errorDetail.setErrorType(newErrorCode);
		return errorDetail;
	}

}
