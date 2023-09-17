package com.etl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.storage.jpa.JpaMapping;
import com.storage.jpa.JpaSubscription;
import com.storage.repository.JpaMappingRepository;
import com.storage.repository.JpaSubscriptionRepository;

/**
 * The MappingExtractor class is responsible for extracting mapping information based on a given subscription ID.
 * It retrieves mapping data from a database and processes it into a list of MappingInfo objects, which represent
 * the mapping information for different paths.
 */
@Configuration
public class MappingExtractor {

    @Autowired
    private JpaMappingRepository mappingRepository;

    @Autowired
    private JpaSubscriptionRepository subscriptionRepository;

    /**
     * Creates mapping information for a given subscription ID.
     *
     * @param subscriptionId The ID of the subscription for which mapping information is to be created.
     * @return A list of MappingInfo objects representing the mapping information.
     * @throws EntityNotFoundException If the subscription with the specified ID is not found.
     */
    public List<MappingInfo> createMagicalMappings(Long subscriptionId) throws EntityNotFoundException {
        List<MappingInfo> mappingInfos = new ArrayList<>();
        Optional<JpaSubscription> subscriptionOptional = subscriptionRepository.findById(subscriptionId);

        if (subscriptionOptional.isPresent()) {
            JpaSubscription subscription = subscriptionOptional.get();
            List<JpaMapping> mappings = mappingRepository.findBySubscription(subscription);
            
            // Iterate through the JSON data
            for (JpaMapping mapping : mappings) {
                Map<String, String> paths = new HashMap<>();
                String rawFilePathKey = mapping.getSourcekey();
                String templateFilePathKey = mapping.getInternalkey();
                // Creating the raw file first path that ends before the last dot.
                int lastIndexOfRawFilePath = rawFilePathKey.lastIndexOf(".");
                String precedingRawFilePath = lastIndexOfRawFilePath != -1 ? "/" + rawFilePathKey
                        .substring(0, rawFilePathKey.lastIndexOf(".")).replaceAll("\\[\\*\\]", "").replace(".", "/")
                        : rawFilePathKey.replaceAll("\\[\\*\\]", "").replace(".", "/");
                String lastWordRawFilePath = rawFilePathKey.substring(rawFilePathKey.lastIndexOf(".") + 1);
                int lastIndexOfTemplateFilePath = templateFilePathKey.lastIndexOf(".");
                String precedingTemplateFilePath = lastIndexOfTemplateFilePath != -1
                        ? "/" + templateFilePathKey.substring(0, templateFilePathKey.lastIndexOf("."))
                                .replaceAll("\\[\\*\\]", "").replace(".", "/")
                        : templateFilePathKey.replaceAll("\\[\\*\\]", "").replace(".", "/");
                String lastWordTemplateFilePath = templateFilePathKey
                        .substring(templateFilePathKey.lastIndexOf(".") + 1);
                // This is done to ensure that parent level mappings are maintained where we do
                // not have any '/'
                if (!lastWordRawFilePath.equalsIgnoreCase(precedingRawFilePath)) {
                    paths.put(lastWordRawFilePath, lastWordTemplateFilePath);
                } else {
                    precedingTemplateFilePath += "/" + lastWordTemplateFilePath;
                }
                MappingInfo mappingInfo = new MappingInfo(precedingRawFilePath);
                if (mappingInfos.contains(mappingInfo)) {
                    MappingInfo tempMappingInfo = mappingInfos.get(mappingInfos.indexOf(mappingInfo));
                    tempMappingInfo.getMappingForPaths().put(lastWordRawFilePath, lastWordTemplateFilePath);
                } else {
                    mappingInfo.setTemplateFilePathKey(precedingTemplateFilePath);
                    mappingInfo.setMappingForPaths(paths);
                    mappingInfos.add(mappingInfo);
                }
            }
        } else {
            throw new EntityNotFoundException("Subscription not found with ID: " + subscriptionId);
        }

        return mappingInfos;
    }
}
