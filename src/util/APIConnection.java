package util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utility methods for accessing Bulbapedia's API.
 */
public class APIConnection {

	/** The base URL for querying from Bulbapedia. */
	private static final String BULBAPEDIA_API = "http://bulbapedia.bulbagarden.net/w/api.php?action=query&format=json";
	/** The URL for getting raw source data from Bulbapedia. */
	private static final String ARTICLE_URL = "http://bulbapedia.bulbagarden.net/w/index.php?action=raw&title=";
	/** The URL for getting raw source data from the Pixelmon wiki. */
	private static final String PIXELMON_API = "http://pixelmonmod.com/wiki/index.php?action=raw&title=";
	/** The URL for querying categories on Bulbapedia. */
	private static final String CATEGORY_API = BULBAPEDIA_API + "&list=categorymembers&&cmlimit=1000&cmtitle=Category:";
	/** Appendage for a Pokémon's Generation 5 learnset page. */
	public static final String GEN_5_LEARNSET = "/Generation_V_learnset";

	/**
	 * Gets data from a URL.
	 * @param urlToRead The URL to get data from.
	 * @return The data from the URL.
	 */
	public static String getURL(String urlToRead) {
		StringBuilder result = new StringBuilder();
		try {
			URL url = new URL(urlToRead);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			BufferedReader rd = null;
			try {
				rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line;
				while ((line = rd.readLine()) != null) {
					result.append(line);
					result.append('\n');
				}
			} catch (FileNotFoundException e) {
				System.out.println("Page not found: " + urlToRead);
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			} finally {
				try {
					if (rd != null) {
						rd.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result.toString();
	}

	/**
	 * Gets data from a URL as a JSON.
	 * @param urlToRead The URL to get data from.
	 * @return The JSON data from the URL.
	 */
	private static JSONObject getJSONFromURL(String urlToRead) {
		return new JSONObject(getURL(urlToRead));
	}
	
	/**
	 * Gets the source for a wiki article.
	 * @param url The base URL of the wiki API.
	 * @param articleName The name of the article.
	 * @return The source for the article.
	 */
	private static String getArticleSourceURL(String url, String articleName) {
		return getURL(url + StringUtil.convertSpaces(articleName));
	}

	/**
	 * Gets the source for a Bulbapedia article.
	 * @param articleName The name of the article.
	 * @return The source for the article.
	 */
	public static String getArticleSource(String articleName) {
		return getArticleSourceURL(ARTICLE_URL, articleName);
	}

	/**
	 * Gets the source for a Pixelmon wiki article.
	 * @param articleName The name of the article.
	 * @return The source for the article.
	 */
	public static String getArticleSourcePixelmon(String articleName) {
		return getArticleSourceURL(PIXELMON_API, articleName);
	}

	/**
	 * Gets the names of Pokémon in certain Generations.
	 * @param generations The Generations to get Pokémon from.
	 * @return The names of Pokémon in certain Generations.
	 */
	public static List<String> getGenerationPokemon(String... generations) {
		return getGenerationItems("Pokémon", generations);
	}

	/**
	 * Gets all elements of a certain type of item in certain Generations.
	 * @param generations The Generations to get items from.
	 * @return The names of items in certain Generations.
	 */
	public static List<String> getGenerationItems(String type, String... generations) {
		List<String> pokemonList = new ArrayList<>();
		for (String generation : generations) {
			String[] generationList = getCategoryMembers("Generation_" + generation + "_" + type);
			for (String pokemon : generationList) {
				pokemonList.add(pokemon);
			}
		}
		return pokemonList;
	}

	/**
	 * Gets the names of articles that are members of a category.
	 * @param categoryName The name of the category to get members for.
	 * @return The names of articles that are members of the category.
	 */
	public static String[] getCategoryMembers(String categoryName) {
		JSONObject json = getJSONFromURL(CATEGORY_API + categoryName);
		json = json.getJSONObject("query");
		JSONArray array = json.getJSONArray("categorymembers");
		int numMembers = array.length();
		String[] members = new String[numMembers];
		for (int i = 0; i < numMembers; i++) {
			members[i] = array.getJSONObject(i).getString("title");
		}
		return members;
	}

	/**
	 * Gets the names of all Generation 6 Pokémon.
	 * @return The names of all Generation 6 Pokémon.
	 */
	public static String[] getGen6Pokemon() {
		return getCategoryMembers("Generation_VI_Pokémon");
	}

}
