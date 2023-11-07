package com.etl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.etl.fuel.Fuel;
import com.etl.fuel.FuelPump;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.storage.jpa.JpaClientTemplate;
import com.storage.repository.JpaClientTemplateRepository;

@Component
public class ETLEngineGearBox {

	@Autowired
	private MagicalMappingExtractor mappingExtractor;

	@Autowired
	private JpaClientTemplateRepository clientTemplateRepository;

	@Autowired
	private FuelPump fuelPump;

	@Autowired
	private ObjectMapper objectMapper;

	private String internalRoot = "/internal";

	public void startTransformation(ETLMessage etlMessage) {
		JpaClientTemplate template = clientTemplateRepository.findByClient_Id(etlMessage.getClientId());

		try {
			if (template == null) {
				throw new RuntimeException("Template is not configured for client " + etlMessage.getClientId()
						+ ". Please contact administrator.");
			}
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
			List<MappingInfo> mappingInfos = mappingExtractor.createMagicalMappings(etlMessage);
			Fuel fuel = getFuel.apply(etlMessage.getFileType());
			if (fuel == null) {
				throw new RuntimeException("Unhandled type. " + etlMessage.getFileType()
						+ ". No code present to handle this file type. Please contact administrator.");
			}
			fuel.transform(mappingInfos, etlMessage, templateJsonFileNode);
		} catch (Exception e) {
			throw new RuntimeException("Failed for transformation. Reason - " + e.getLocalizedMessage());
		}

	}

	private void populateInternalData(JsonNode templateJsonFileNode, Map<String, String> internalMap) {
		JsonNode templateJsonObjectNode = templateJsonFileNode.at(internalRoot);
		internalMap.keySet().stream().forEach(internalKey -> {
			((ObjectNode) templateJsonObjectNode).put(internalKey, internalMap.get(internalKey));
		});
	}

	Function<String, Fuel> getFuel = (fuelType) -> fuelPump.getHandler(fuelType);

}
