package com.etl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.storage.jpa.JpaEtlSubscriptionMapping;
import com.storage.jpa.JpaSubscription;
import com.storage.repository.JpaMappingRepository;
import com.storage.repository.JpaSubscriptionRepository;

/**
 * The MappingExtractor class is responsible for extracting mapping information
 * based on a given subscription ID. It retrieves mapping data from a database
 * and processes it into a list of MappingInfo objects, which represent the
 * mapping information for different paths.
 */
@Configuration
public class MagicalMappingExtractor extends Extractor {

	@Autowired
	private JpaMappingRepository mappingRepository;

	@Autowired
	private JpaSubscriptionRepository subscriptionRepository;

	/**
	 * Creates mapping information for a given subscription ID.
	 *
	 * @param subscriptionId The ID of the subscription for which mapping
	 *                       information is to be created.
	 * @return A list of MappingInfo objects representing the mapping information.
	 * @throws EntityNotFoundException If the subscription with the specified ID is
	 *                                 not found.
	 */
	public List<MappingInfo> createMagicalMappings(ETLMessage message) {
		List<MappingInfo> mappingInfos = new ArrayList<>();
		JpaSubscription subscription = subscriptionRepository.findById(message.getSubscriptionId())
				.orElseThrow(EntityNotFoundException::new);
		List<JpaEtlSubscriptionMapping> mappings = mappingRepository.findBySubscription(subscription);
		// Iterate through the JSON data
		for (JpaEtlSubscriptionMapping mapping : mappings) {
			Map<String, String> paths = new HashMap<>();
			String rawFilePathKey = mapping.getSourcekey();
			String templateFilePathKey = mapping.getInternalkey();
			// Creating the raw file first path that ends before the last dot.
			int lastIndexOfRawFilePath = rawFilePathKey.lastIndexOf("/");
			String precedingRawFilePath = lastIndexOfRawFilePath != -1
					? rawFilePathKey.substring(0, rawFilePathKey.lastIndexOf("/"))
					: rawFilePathKey;
			String lastWordRawFilePath = rawFilePathKey.substring(rawFilePathKey.lastIndexOf("/") + 1);
			int lastIndexOfTemplateFilePath = templateFilePathKey.lastIndexOf("/");
			String precedingTemplateFilePath = lastIndexOfTemplateFilePath != -1
					? templateFilePathKey.substring(0, templateFilePathKey.lastIndexOf("/"))
					: templateFilePathKey;
			String lastWordTemplateFilePath = templateFilePathKey.substring(templateFilePathKey.lastIndexOf("/") + 1);
			// This is done to ensure that parent level mappings are maintained where we do
			// not have any '/'
			if (!lastWordRawFilePath.equalsIgnoreCase(precedingRawFilePath)) {
				paths.put(lastWordRawFilePath, lastWordTemplateFilePath);
			} else {
				precedingTemplateFilePath += "/" + lastWordTemplateFilePath;
				precedingTemplateFilePath = precedingTemplateFilePath.replace("[*]", "");
			}
			MappingInfo mappingInfo = new MappingInfo(precedingTemplateFilePath);
			if (mappingInfos.contains(mappingInfo)) {
				MappingInfo tempMappingInfo = mappingInfos.get(mappingInfos.indexOf(mappingInfo));
				tempMappingInfo.getMappingForPaths().put(lastWordRawFilePath, lastWordTemplateFilePath);
			} else {
				mappingInfo.setTemplateFilePathKey(precedingTemplateFilePath);
				mappingInfo.setMappingForPaths(paths);
				mappingInfo.setRawFilePathKey(precedingRawFilePath);
				mappingInfos.add(mappingInfo);
			}
		}
		return mappingInfos;
	}
}
