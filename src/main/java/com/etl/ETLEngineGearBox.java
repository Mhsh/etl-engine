package com.etl;

import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.etl.fuel.Fuel;
import com.etl.fuel.FuelPump;
import com.storage.jpa.JpaClientTemplate;
import com.storage.repository.JpaClientTemplateRepository;

@Component
public class ETLEngineGearBox {

	@Autowired
	private MappingExtractor mappingExtractor;

	@Autowired
	private JpaClientTemplateRepository clientTemplateRepository;

	@Autowired
	private FuelPump fuelPump;

	public void startTransformation(ETLMessage etlMessage) {
		JpaClientTemplate template = clientTemplateRepository.findByClient_Id(etlMessage.getClientId());
		if (template == null) {
			throw new RuntimeException("Template is not configured for client " + etlMessage.getClientId()
					+ ". Please contact administrator.");
		}
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
		List<MappingInfo> mappingInfos = mappingExtractor.createMagicalMappings(etlMessage.getSubscriptionId());
		Fuel fuel = getFuel.apply(etlMessage.getFileType());
		if (fuel == null) {
			throw new RuntimeException("Unhandled type. " + etlMessage.getFileType()
					+ ". No code present to handle this file type. Please contact administrator.");
		}
		try {
			fuel.transform(mappingInfos, etlMessage, template);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	Function<String, Fuel> getFuel = (fuelType) -> fuelPump.getHandler(fuelType);

}
