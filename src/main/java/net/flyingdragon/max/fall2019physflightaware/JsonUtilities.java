package net.flyingdragon.max.fall2019physflightaware;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtilities {
	private static final Logger log = LoggerFactory.getLogger(JsonUtilities.class);
	
	
	/**
	 * Use jackson to parse string into java Map
	 * @param jsonString
	 * @return
	 */
	public static Map<String, Object> toMap(String jsonString)
	{
		
		ObjectMapper mapper = new ObjectMapper();

		try {
		    //convert JSON string to Map
			Map<String,Object> map = mapper.readValue(jsonString, new TypeReference<HashMap<String,Object>>(){});
			return map;
		} catch (Exception e) {
		     log.info("Exception converting {} to map", jsonString, e);
		     throw new IllegalArgumentException("Unable to convert json to map");
		}

	}
	
	/**
	 * Use jackson to parse string into JsonNode which allows null-safe path() nesting.
	 * 
	 * See
	 *   https://fasterxml.github.io/jackson-databind/javadoc/2.7/com/fasterxml/jackson/databind/ObjectMapper.html#readTree(java.lang.String)
	 *   and
	 *   http://fasterxml.github.io/jackson-core/javadoc/2.7/com/fasterxml/jackson/core/TreeNode.html?is-external=true
	 *   and
	 *   https://stackoverflow.com/questions/29858248/reading-value-of-nested-key-in-json-with-java-jackson
	 * @param jsonString
	 * @return
	 */
	public static JsonNode parseJson(String jsonString) {
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			return mapper.readTree(jsonString);
		} catch (Exception e) {
			log.error("Exception parsing json: {}", jsonString, e);
			throw new IllegalArgumentException("Unable to parse json");
		}
	}
}
