package com.etl.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class JsonSchemaDiscovery {

    public List<String> collectPaths(JsonNode jsonNode, String currentPath, String separator) {
        List<String> paths = new ArrayList<>();
        if (jsonNode.isObject()) {
            Iterator<String> fieldNames = jsonNode.fieldNames();
            while (fieldNames.hasNext()) {
                String tag = fieldNames.next();
                String newPath = StringUtils.hasText(currentPath) ? currentPath + separator + tag : tag;
                JsonNode value = jsonNode.get(tag);
                if (value.isObject()) {
                    paths.addAll(collectPaths(value, newPath, separator));
                } else if (value.isArray()) {
                    String arrayPath = newPath + "[*]";
                    ArrayNode valueArray = (ArrayNode) value;
                    if (!valueArray.isEmpty() && valueArray.get(0).isObject()) {
                        paths.addAll(collectPaths(valueArray.get(0), arrayPath, separator));
                    } else {
                        paths.add(newPath);
                    }
                } else {
                    paths.add(newPath);
                }
            }
        }
        return paths;
    }

    public static void main(String[] args) throws IOException {
        String sampleRegulatory = readFile("src/main/resources/json/regulatory_template.json");
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(sampleRegulatory);
        JsonSchemaDiscovery schemaDiscovery = new JsonSchemaDiscovery();
        System.out.println(schemaDiscovery.collectPaths(jsonNode, null, "/"));
    }

    private static String readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return new String(Files.readAllBytes(path));
    }
}
