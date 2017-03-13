package util;

import org.json.JSONObject;

/**
 * Utility methods for JSON data.
 */
public class JSONUtil {

	/**
	 * Gets a string from a JSON key, or a default if the key isn't found.
	 * @param json The JSON object to get a string from.
	 * @param key The key to get a default from.
	 * @param defaultString The default string to return if the key isn't found.
	 * @return The string from the JSON key or the default string.
	 */
	public static String getOrDefault(JSONObject json, String key, String defaultString) {
		return json.has(key) ? json.getString(key) : defaultString;
	}

	/**
	 * Gets an int from a JSON key, or a default if the key isn't found.
	 * @param json The JSON object to get a string from.
	 * @param key The key to get a default from.
	 * @param defaultString The default int to return if the key isn't found.
	 * @return The int from the JSON key or the default int.
	 */
	public static int getOrDefault(JSONObject json, String key, int defaultInt) {
		return json.has(key) ? json.getInt(key) : defaultInt;
	}
}
