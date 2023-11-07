package com.avro.archieve;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.Predicate;
import com.jayway.jsonpath.ReadContext;

import net.minidev.json.JSONArray;

public class JsonPathValueExtractor {

	public Map<String, List<String>> extractValuesFromPath(String jsonRoot, String jsonData, List<String> properties) {
		Configuration configuration = Configuration.defaultConfiguration();
		ReadContext context = JsonPath.using(configuration).parse(jsonData);
		Object root = context.read(jsonRoot);
		Map<String, List<String>> extractedValuesMap = new HashMap<>();

		if (root instanceof JSONArray) {
			List<Map<String, Object>> lists = context.read(jsonRoot, List.class);
			for (Map<String, Object> list : lists) {
				Map<String, List<String>> propertyValues = extractProperty(list, properties);
				extractedValuesMap.putAll(propertyValues);
			}
		} else {
			Map<String, List<String>> propertyValues = extractProperty((Map<String, Object>) root, properties);
			extractedValuesMap.putAll(propertyValues);
		}

		return extractedValuesMap;
	}

	private Map<String, List<String>> extractProperty(Map<String, Object> list, List<String> properties) {
		Map<String, List<String>> extractedValuesMap = new HashMap<>();

		for (String property : properties) {
			List<String> extractedValues = new ArrayList<>();
			try {
				Predicate propPredicate = new Predicate() {
					@Override
					public boolean apply(PredicateContext ctx) {
						return ctx.item(Map.class).containsKey(property);
					}
				};
				Object value = JsonPath.parse(list).read(property, propPredicate);
				extractedValues.add(value.toString());
			} catch (PathNotFoundException e) {
				extractedValues.add(null);
			}
			extractedValuesMap.put(property, extractedValues);
		}

		return extractedValuesMap;
	}

	public List<String> extractProperty(String jsonRoot, String jsonData, String property) {
		Configuration configuration = Configuration.defaultConfiguration();
		ReadContext context = JsonPath.using(configuration).parse(jsonData);
		Object root = context.read(jsonRoot);
		List<String> extractedValues = new ArrayList<>();
		try {
			Predicate propPredicate = new Predicate() {
				@Override
				public boolean apply(PredicateContext ctx) {
					return ctx.item(Map.class).containsKey(property);
				}
			};
			Object value = JsonPath.parse(((Map<String, Object>) root)).read(property, propPredicate);
			extractedValues.add(value.toString());
		} catch (PathNotFoundException e) {
			extractedValues.add(null);
		}
		return extractedValues;
	}

}
