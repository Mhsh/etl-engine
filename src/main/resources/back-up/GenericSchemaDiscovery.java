package com.avro.archieve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.Predicate;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.Predicate.PredicateContext;

import net.minidev.json.JSONArray;

public class GenericSchemaDiscovery {
	// Your JSON data and other code...

	public static void extractAndDisplayValues(String jsonRoot, String jsonData, List<String> properties) {
		Configuration configuration = Configuration.defaultConfiguration();
		ReadContext context = JsonPath.using(configuration).parse(jsonData);
		Object test = context.read(jsonRoot);
		if (test instanceof JSONArray) {
			List<Map<String, Object>> users = context.read(jsonRoot, List.class);
			for (Map<String, Object> user : users) {
				extractInformation(user, properties);
			}
		} else
			extractInformation((Map) test, properties);
	}

	private static void extractInformation(Map<String, Object> user, List<String> properties) {
		System.out.println("User Information:");
		for (String property : properties) {
			List<String> extractedValues = new ArrayList<>();
			try {
				Predicate propPredicate = new Predicate() {
					@Override
					public boolean apply(PredicateContext ctx) {
						return ctx.item(Map.class).containsKey(property);
					}
				};
				Object value = JsonPath.parse(user).read(property, propPredicate);

				extractedValues.add(value.toString());
			} catch (PathNotFoundException e) {
				extractedValues.add("Not found");
			}
			System.out.println(property + ": " + extractedValues.get(0));
		}
		System.out.println();
	}

	public static void main(String[] args) {
		String json = "{\"user\": ["
				+ "{\"userID\": \"12\",\"userUserName\": \"My Username\",\"userRealName\": \"My Real Name\","
				+ "\"mainInterests\": [{\"interest\": \"Guitar\"},{\"interest\": \"Web Design\"},{\"interest\": \"Hiking\"}],"
				+ "\"userAbout\": \"All about me...\",\"userComments\": ["
				+ "{\"comment\": \"this is a comment\", \"contentID\": \"12\"},"
				+ "{\"comment\": \"this is another comment\", \"contentID\": \"123\"}" + "]}"
				+ ",{\"userID\": \"13\",\"userUserName\": \"Another User\",\"userRealName\": \"Another Real Name\","
				+ "\"mainInterests\": [{\"interest\": \"Photography\"},{\"interest\": \"Travel\"},{\"interest\": \"Cooking\"}],"
				+ "\"userAbout\": \"About another user...\",\"userComments\": ["
				+ "{\"comment\": \"a comment by another user\", \"contentID\": \"45\"},"
				+ "{\"comment\": \"another comment\", \"contentID\": \"67\"}" + "]}"
				+ ",{\"userID\": \"14\",\"userUserName\": \"Third User\",\"userRealName\": \"Third Real Name\","
				+ "\"mainInterests\": [{\"interest\": \"Painting\"},{\"interest\": \"Reading\"},{\"interest\": \"Swimming\"}],"
				+ "\"userAbout\": \"About the third user...\",\"userComments\": ["
				+ "{\"comment\": \"comment by the third user\", \"contentID\": \"89\"},"
				+ "{\"comment\": \"third user's comment\", \"contentID\": \"101\"}" + "]}" + "]}";
		String jsonRoot = "$.user";
		List<String> jsonPaths = Arrays.asList("userUserName", "userRealName", "mainInterests[*]",
				"mainInterests[*].interest", "userComments[*].comment");

		// Item
		String itemJson = "{\n" + "	\"count\": 1425,\n" + "	\"entries\": [{\n" + "		\"API\": \"AdoptAPet\",\n"
				+ "		\"Description\": \"Resource to help get pets adopted\",\n" + "		\"Auth\": \"apiKey\",\n"
				+ "		\"HTTPS\": true,\n" + "		\"Cors\": \"yes\",\n"
				+ "		\"Link\": \"https://www.adoptapet.com/public/apis/pet_list.html\",\n"
				+ "		\"Category\": \"Animals\"\n" + "	}, {\n" + "		\"API\": \"Axolotl\",\n"
				+ "		\"Description\": \"Collection of axolotl pictures and facts\",\n" + "		\"Auth\": \"\",\n"
				+ "		\"HTTPS\": true,\n" + "		\"Cors\": \"no\",\n"
				+ "		\"Link\": \"https://theaxolotlapi.netlify.app/\",\n" + "		\"Category\": \"Animals\"\n"
				+ "	}]\n" + "}";
		List<String> itemJsonPaths = Arrays.asList("Description", "Auth");
		String itemJsonRoot = "$.entries";

		// Direct object json and jsonPaths
		String directObjectJson = "{\"userID\": \"14\",\n" + "\"userUserName\": \"Third User\",\n"
				+ "\"userRealName\": \"Third Real Name\",\n" + "\"mainInterests\": [\n"
				+ "    {\"interest\": \"Painting\"},\n" + "    {\"interest\": \"Reading\"},\n"
				+ "    {\"interest\": \"Swimming\"}\n" + "],\n" + "\"userAbout\": \"About the third user...\",\n"
				+ "\"userComments\": [\n" + "    {\"comment\": \"comment by the third user\", \"contentID\": \"89\"},\n"
				+ "    {\"comment\": \"third user's comment\", \"contentID\": \"101\"}\n" + "]\n" + "}";
		List<String> directObjectJsonPaths = Arrays.asList("userUserName", "userRealName", "mainInterests",
				"mainInterests[*]", "mainInterests[*].interest", "userID", "userComments", "userComments[*]",
				"userComments[*].contentID", "userComments[*].comment", "userAbout");
		String directObjectRoot = "$";
		String insideObjectJson = "{\n" + "	\"user\": [{\n" + "		\"userId\": \"test\",\n"
				+ "		\"address\": {\n" + "			\"city\": \"test\",\n" + "			\"state\": \"Gujarat\"\n"
				+ "		}\n" + "	},\n" + "			{\n" + "		\"userId\": \"test1\",\n"
				+ "		\"address\": {\n" + "			\"city\": \"test1\",\n" + "			\"state\": \"Gujarat1\"\n"
				+ "		}\n" + "	}]\n" + "}";
		String templateJsonString = "{\"regulatory\":{\"metadata\":{\"documentID\":\"<documentID>\",\"documentTitle\":\"<documentTitle>\"},\"regulatoryCorrespondence\":[],\"labelingAndPackaging\":{\"labelingInfo\":\"<labelingInfo>\",\"packageComponents\":\"<packageComponents>\",\"packageDescriptions\":{\"Box\":\"<Box>\",\"Bottle\":\"<Bottle>\"}},\"approvalsAndSignOffs\":{\"approvers\":[]}}}";
		String templateObjectRoot = "$.regulatory";
		List<String> insideObjectJsonPaths = Arrays.asList("address.city");
		String insideObjectRoot = "$.user";
		extractAndDisplayValues(templateObjectRoot, templateJsonString, jsonPaths);
	}

}
