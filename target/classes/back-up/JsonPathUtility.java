package com.avro.archieve;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

public class JsonPathUtility {

	public static void extractUserInfo(Object userObj, List<String> jsonPaths) {
		for (String jsonPath : jsonPaths) {
			System.out.println("JSONPath: " + jsonPath);
			try {
				Object result = JsonPath.read(userObj, jsonPath);

				if (result instanceof JSONArray) {
					JSONArray resultArray = (JSONArray) result;
					for (Object item : resultArray) {
						System.out.println(item);
					}
				} else {
					System.out.println(result);
				}
			} catch (Exception e) {
				System.out.println("Error evaluating JSONPath: " + jsonPath);
			}
		}
	}

	public static void computeJsonPaths(Object parsedJson, List<String> jsonPaths, String parentPath) {
		Map<String, Object> jsonMap = (Map) parsedJson;
		if (StringUtils.hasText(parentPath)) {
			Object arrayObj = getValueByKeyPath(jsonMap, parentPath);
			if (arrayObj instanceof JSONArray) {
				JSONArray jsonArray = (JSONArray) arrayObj;
				for (Object userObj : jsonArray) {
					System.out.println("Array Information:");
					extractUserInfo(userObj, jsonPaths);
					System.out.println();
				}
			}
		} else
			extractUserInfo(parsedJson, jsonPaths);
	}

	public static Object getValueByKeyPath(Map<String, Object> map, String keyPath) {
		String[] keys = keyPath.split("\\."); // Split the key path into individual keys

		Object current = map;
		for (String key : keys) {
			if (current instanceof Map) {
				current = ((Map<?, ?>) current).get(key);
			} else {
				return null; // Key path not found
			}
		}

		return current;
	}

	public static void main(String[] args) {
		// Direct object json and jsonPaths
		String directObjectJson = "{\"userID\": \"14\",\n" + "\"userUserName\": \"Third User\",\n"
				+ "\"userRealName\": \"Third Real Name\",\n" + "\"mainInterests\": [\n"
				+ "    {\"interest\": \"Painting\"},\n" + "    {\"interest\": \"Reading\"},\n"
				+ "    {\"interest\": \"Swimming\"}\n" + "],\n" + "\"userAbout\": \"About the third user...\",\n"
				+ "\"userComments\": [\n" + "    {\"comment\": \"comment by the third user\", \"contentID\": \"89\"},\n"
				+ "    {\"comment\": \"third user's comment\", \"contentID\": \"101\"}\n" + "]\n" + "}";
		List<String> directObjectJsonPaths = Arrays.asList("$.userUserName", "$.userRealName", "$.mainInterests",
				"$.mainInterests[*]", "$.mainInterests[*].interest", "$.userID", "$.userComments", "$.userComments[*]",
				"$.userComments[*].contentID", "$.userComments[*].comment", "$.userAbout");

		// Array json and jsonPaths
		String arrayJson = "{\"user\": ["
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

		List<String> arrayJsonPaths = Arrays.asList( "$.userComments[*].comment","$.userComments[*].contentID");

		// Complex object nested json and jsonPaths
		String complexNextedJson = "{\n" + "  \"order\": {\n" + "    \"id\": 123,\n" + "    \"customer\": {\n"
				+ "      \"id\": 1,\n" + "      \"name\": \"John Doe\"\n" + "    },\n" + "    \"items\": [\n"
				+ "      {\n" + "        \"productId\": 101,\n" + "        \"quantity\": 2\n" + "      },\n"
				+ "      {\n" + "        \"productId\": 102,\n" + "        \"quantity\": 1\n" + "      }\n" + "    ]\n"
				+ "  }\n" + "}";

		List<String> complexNextedJsonPaths = Arrays.asList("$.order.customer", "$.order.items[*].productId");

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

		List<String> itemJsonPaths = Arrays.asList("$.Description", "$.Auth");
		computeJsonPaths(JsonPath.parse(arrayJson).json(), arrayJsonPaths, "user");
	}
}
