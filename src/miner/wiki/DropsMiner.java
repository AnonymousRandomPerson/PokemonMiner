package miner.wiki;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import miner.Miner;
import miner.storage.DropComparatorItem;
import miner.storage.DropComparatorPokemon;
import miner.storage.DropEntry;
import pixelmon.EnumPokemon;
import util.FileIO;
import util.JSONUtil;
import util.ListUtil;

/**
 * Generates pages for drops based on Pokémon drop data.
 */
public class DropsMiner extends Miner {

	/** Minecraft items that have articles on the Pixelmon wiki. */
	private Set<String> itemMCArticles;

	/** Map of items that have special requirements for the i template. */
	private Map<String, String> specialItemTemplates;
	
	/** Items to ignore when getting drops. */
	private Set<String> ignoreItems;
	
	/** The JSON file containing drop data. */
	private static final String JSON_FILE = "pokedrops.json";

	/**
	 * Initializes a drops miner.
	 */
	public DropsMiner() {
		super();

		itemMCArticles = new HashSet<>();
		itemMCArticles.addAll(Arrays.asList("Apple", "Blaze Powder", "Blaze Rod", "Bone", "Cobblestone", "Diamond",
				"Dirt", "Ink Sac", "Bone Meal", "Cactus Green", "Cocoa Beans", "Egg", "Emerald", "End Stone",
				"Ender Pearl", "Feather", "Flower Pot", "Ghast Tear", "Glass Bottle", "Glowstone Dust", "Gold Ingot",
				"Gold Nugget", "Gravel", "Gunpowder", "Iron Ingot", "Leather", "Prismarine Shard", "Redstone", "Sand",
				"Sandstone", "Slimeball", "Soul Sand", "Spider Eye", "Stick", "Stone", "Granite", "String", "Sugar",
				"Seeds", "Wool", "Yellow Wool", "Purple Wool"));

		specialItemTemplates = new HashMap<>();
		specialItemTemplates.put("Clay Block", "Clay|mc=Clay_(block)|image=Clay Block");
		specialItemTemplates.put("Mushroom1", "Mushroom|image=Brown Mushroom");
		specialItemTemplates.put("Mushroom2", "Mushroom|image=Red Mushroom");
		specialItemTemplates.put("Mushroom2Block", "Mushroom|mc=Mushroom_(block)|image=Huge Red Mushroom");
		specialItemTemplates.put("Music Disc strad", "Music Disc|image=Music Disc dusk");
		
		ignoreItems = new HashSet<>();
		ignoreItems.addAll(Arrays.asList("Cleanse Tag", "Clever Wing", "Fluffy Tail", "Genius Wing", "Iron Nugget", "Shulker Shell"));
	}

	/**
	 * Gets the wiki page for all drops.
	 * @return The wiki page for all drops.
	 */
	public String getAllDrops() {
		builder = new StringBuilder();

		List<String> dropKeys = new ArrayList<>();
		Map<String, List<DropEntry>> dropMap = getDropMap(dropKeys);

		builder.append("{|class=\"wikitable\"\n!Drop\n!Pokémon\n!Chance\n!Quantity");

		for (String item : dropKeys) {
			List<DropEntry> dropEntries = dropMap.get(item);
			builder.append("\n|-\n|");
			int numEntries = dropEntries.size();
			if (numEntries > 1) {
				builder.append("rowspan=\"");
				builder.append(numEntries);
				builder.append("\"|");
			}
			builder.append(makeItemTemplate(item, dropEntries.get(0).item));
			boolean first = true;
			dropEntries.sort(new DropComparatorPokemon());
			for (DropEntry entry : dropEntries) {
				if (first) {
					first = false;
				} else {
					builder.append("\n|-");
				}
				builder.append("\n|{{p|");
				builder.append(entry.pokemon);

				builder.append("}}");

				builder.append("\n|style=\"text-align:center\"|{{percent|");
				builder.append(entry.getPercent());
				builder.append("|100}}");

				builder.append("\n|style=\"text-align:center\"|");
				builder.append(entry.getRangeString());
			}
		}

		builder.append("\n|}");

		return builder.toString();
	}

	/**
	 * Gets a wiki table for a Pokémon's drops.
	 * @param pokemon The Pokémon to get a drop table for.
	 * @return The Pokémon's drop table.
	 */
	public String getDrops(String pokemon) {
		builder = new StringBuilder();

		if (pokemon.isEmpty()) {
			pokemon = "Bulbasaur";
		}
		
		JSONArray json = FileIO.getJSONArrayFromFile(JSON_FILE);
		List<DropEntry> dropEntries = null;
		for (Object object : json) {
			JSONObject pokemonJSON = (JSONObject) object;
			String name = pokemonJSON.getString("pokemon");
			if (!pokemon.equals(name)) {
				continue;
			}

			List<DropEntry> allDropEntries = getDropEntries(pokemonJSON);
		
			dropEntries = new ArrayList<>();
			for (DropEntry dropEntry : allDropEntries) {
				if (!ignoreItems.contains(dropEntry.translatedItem)) {
					dropEntries.add(dropEntry);
				}
			}
			break;
		}
		
		if (dropEntries == null) {
			System.out.println("Pokémon not found.");
			return "";
		} else if (dropEntries.isEmpty()) {
			System.out.println("Pokémon has no drops.");
			return "";
		}
		
		dropEntries.sort(new DropComparatorItem());
		
		builder.append("==[[Drops]]==\n");
		builder.append("{{DropH}}\n");
		
		for (DropEntry dropEntry : dropEntries) {
			builder.append("{{Drop|");
			builder.append(makeItemTemplate(dropEntry.translatedItem, dropEntry.item));
			builder.append('|');

			builder.append(dropEntry.getPercent());
			builder.append('|');

			builder.append(dropEntry.getRangeString());
			builder.append("}}\n");
		}
		
		builder.append("{{DropF}}\n");

		return builder.toString();
	}
	
	/**
	 * Gets a wiki table for the Pokémon that drop a certain item.
	 * @param item The item to get Pokémon for.
	 * @return A wiki table for the Pokémon that drop the specified item.
	 */
	public String getDropPokemon(String item) {
		builder = new StringBuilder();

		if (item.isEmpty()) {
			item = "Stone";
		}
		
		Map<String, List<DropEntry>> dropMap = getDropMap(null);
		if (!dropMap.containsKey(item)) {
			System.out.println("No drop found.");
			return "";
		}
		
		builder.append("==Pokémon [[drops]]==\n");
		builder.append("{{DropPokémonH}}\n");
		
		List<DropEntry> dropEntries = dropMap.get(item);
		dropEntries.sort(new DropComparatorPokemon());
		for (DropEntry entry : dropEntries) {
			builder.append("{{DropPokémon|");
			builder.append(entry.pokemon);
			builder.append('|');

			builder.append(entry.getPercent());
			builder.append('|');

			builder.append(entry.getRangeString());
			builder.append("}}\n");
		}
		builder.append("{{DropPokémonF}}");

		return builder.toString();
	}
	
	/**
	 * Creates wikicode for the item (i) template.
	 * @param item The item to pass into the template.
	 * @param id The item ID for the item.
	 * @return Wikicode for the item template.
	 */
	private String makeItemTemplate(String item, String id) {
		StringBuilder builder = new StringBuilder();
		builder.append("{{i|");
		if (specialItemTemplates.containsKey(item)) {
			builder.append(specialItemTemplates.get(item));
		} else {
			builder.append(item);
			if (!itemMCArticles.contains(item) && id.startsWith("minecraft:")) {
				builder.append("|mc=");
			 	builder.append(item.replaceAll(" ", "_"));
			}
		}
		builder.append("}}");
		return builder.toString();
	}
	
	/**
	 * Gets a map of drops to the Pokémon who drop them.
	 * @param dropKeys A list to fill with all possible drops.
	 * @return A map of drops to the Pokémon who drop them.
	 */
	private Map<String, List<DropEntry>> getDropMap(List<String> dropKeys) {
		Set<String> pokemonSet = EnumPokemon.getAllPokemon();
		Map<String, List<DropEntry>> dropMap = new HashMap<>();
		JSONArray json = FileIO.getJSONArrayFromFile(JSON_FILE);
		for (Object object : json) {
			JSONObject pokemonJSON = (JSONObject) object;
			String name = pokemonJSON.getString("pokemon");
			if (!pokemonSet.contains(name)) {
				continue;
			}

			List<DropEntry> dropEntries = getDropEntries(pokemonJSON);
			for (DropEntry dropEntry : dropEntries) {
				List<DropEntry> dropList;
				if (ignoreItems.contains(dropEntry.translatedItem)) {
					continue;
				} else if (dropMap.containsKey(dropEntry.translatedItem)) {
					dropList = dropMap.get(dropEntry.translatedItem);
				} else {
					dropList = new ArrayList<>();
					if (dropKeys != null) {
						dropKeys.add(dropEntry.translatedItem);
					}
				}
				dropList.add(dropEntry);
				dropMap.put(dropEntry.translatedItem, dropList);
			}
		}
		if (dropKeys != null) {
			Collections.sort(dropKeys);
		}
		return dropMap;
	}

	/**
	 * Gets a list of drop entries for a Pokémon JSON entry.
	 * @param pokemonJSON The Pokémon JSON entry to get drop entries for.
	 * @return A list of drop entries for a Pokémon JSON entry.
	 */
	private List<DropEntry> getDropEntries(JSONObject pokemonJSON) {
		List<DropEntry> dropList = new ArrayList<>();
		ListUtil.addIfNotNull(dropList, getDropEntry(pokemonJSON, "maindrop", false));
		ListUtil.addIfNotNull(dropList, getDropEntry(pokemonJSON, "optdrop1", false));
		ListUtil.addIfNotNull(dropList, getDropEntry(pokemonJSON, "optdrop2", false));
		ListUtil.addIfNotNull(dropList, getDropEntry(pokemonJSON, "raredrop", true));
		return dropList;
	}

	/**
	 * Gets a single drop entry for a Pokémon.
	 * @param pokemonJSON The Pokémon JSON entry to get a drop entry for.
	 * @param dataKey The JSON key for the item to get.
	 * @param isRare Whether the entry is a rare drop.
	 * @return A single drop entry for a Pokémon.
	 */
	private DropEntry getDropEntry(JSONObject pokemonJSON, String dataKey, boolean isRare) {
		String checkData = dataKey + "data";
		if (!pokemonJSON.has(checkData)) {
			return null;
		}
		String item = pokemonJSON.getString(checkData);

		DropEntry entry = new DropEntry(pokemonJSON.getString("pokemon"), item,
				JSONUtil.getOrDefault(pokemonJSON, dataKey + "min", 0),
				JSONUtil.getOrDefault(pokemonJSON, dataKey + "max", 1), isRare);
		return entry;
	}
}
