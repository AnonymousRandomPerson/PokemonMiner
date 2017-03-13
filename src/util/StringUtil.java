package util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import database.Database;
import miner.storage.Pokemon;

/**
 * Utility methods for manipulating strings.
 */
public class StringUtil {

	/** Map from Generation 6 to Generation 5 move spellings. */
	private static Map<String, String> moveConvertMap;
	/** Map from Generation 6 to Generation 5 Pokémon spellings. */
	private static Map<String, String> pokemonConvertMap;

	/** The current raw data to be processed. */
	public static String currentRaw;

	/** An array of all existing Generations. */
	public static final String[] ALL_GENERATIONS = { "I", "II", "III", "IV", "V", "VI" };
	
	/** All moves that no available Pokémon can learn. */
	private static Set<String> unavailableMoves;
	
	/** Map from item IDs to abnormal item names. */
	private static Map<String, String> itemNameMap;
	
	/** Corrects the names of certain Pokémon that are spelled incorrectly due to restrictions. */
	private static Map<String, String> pokemonCorrectMap;

	/**
	 * Shortens a string by a certain amount.
	 * @param initString The string to shorten.
	 * @param amount The number of characters to shorten the string by.
	 * @return The shortened string.
	 */
	public static String shortenString(String initString, int amount) {
		return initString.substring(0, initString.length() - amount);
	}

	/**
	 * Gets the substring between two parts of a string.
	 * @param initString The string to get a substring from.
	 * @param start The start of the substring.
	 * @param end The end of the substring.
	 * @return The substring between the start string and the end string.
	 */
	public static String getSubstringBetween(String initString, String start, String end) {
		int startIndex = 0;
		if (start != null) {
			startIndex = initString.indexOf(start) + start.length();
		}
		if (startIndex == -1) {
			return "";
		}
		int endIndex = initString.length();
		if (end != null) {
			endIndex = initString.indexOf(end, startIndex);
		}
		if (endIndex == -1) {
			return "";
		}
		return initString.substring(startIndex, endIndex);
	}

	/**
	 * Removes all spaces from a string.
	 * @param string The string to remove spaces from.
	 * @return The given string with all spaces removed.
	 */
	public static String removeSpaces(String string) {
		return string == null ? null : string.replaceAll(" ", "");
	}

	/**
	 * Converts spaces to underscores.
	 * @param string The string to convert.
	 * @return The converted string.
	 */
	public static String convertSpaces(String string) {
		return string.replaceAll(" ", "_");
	}

	/**
	 * Adds an array of strings to a string builder, comma-separated.
	 * @param stringBuilder The string builder to add to.
	 * @param isValue Whether the strings are insert values.
	 * @param items The strings to add.
	 */
	public static void addCommaSeparated(StringBuilder stringBuilder, boolean isValue, Object... items) {
		String[] strings = new String[items.length];
		for (int i = 0; i < items.length; i++) {
			if (items[i] == null) {
				strings[i] = "null";
			} else if (items[i] instanceof String) {
				strings[i] = (String) items[i];
				if (isValue) {
					strings[i] = "'" + strings[i] + "'";
				}
			} else {
				strings[i] = items[i].toString();
			}
		}
		for (int i = 0; i < strings.length; i++) {
			stringBuilder.append(strings[i]);
			if (i < strings.length - 1) {
				stringBuilder.append(",");
			}
		}
	}

	/**
	 * Gets an entry in a Pokémon table.
	 * @param section The section to get an entry from.
	 * @return The entry in the Pokémon table, or null if the entry doesn't exist.
	 */
	public static String getTableEntry(String section) {
		int startIndex = currentRaw.indexOf(section);
		if (startIndex == -1) {
			return null;
		}
		startIndex += section.length() + 1;
		int endIndex = currentRaw.indexOf("|", startIndex);
		int newLineIndex = currentRaw.indexOf("\n", startIndex);
		int indexBreak = currentRaw.indexOf("}}", startIndex);
		int startBrace = currentRaw.indexOf("{{", startIndex);
		if (startBrace != -1 && startBrace < indexBreak && startBrace < newLineIndex) {
			int newEndIndex = currentRaw.indexOf("|", indexBreak);
			if (newEndIndex > -1) {
				endIndex = newEndIndex;
				indexBreak = currentRaw.indexOf("}}", indexBreak + 2);
			}
		}
		if (endIndex == -1 || indexBreak < endIndex && indexBreak != -1) {
			endIndex = indexBreak;
		}
		if (currentRaw.charAt(endIndex - 1) == '\n') {
			endIndex--;
		}
		String entry = currentRaw.substring(startIndex, endIndex);
		if (!entry.isEmpty() && entry.charAt(0) == '=') {
			entry = entry.replaceFirst("= ", "");
		}
		return entry;
	}
	
	/**
	 * Gets an entry in a Pokémon table, with backup entries for nonexistence. 
	 * @param section The section to get an entry from.
	 * @return The entry in the Pokémon table, or null if the entry doesn't exist.
	 */
	public static String getTableEntryFallback(String... sections) {
		for (String section : sections) {
			String entry = getTableEntry(section);
			if (entry != null) {
				return entry;
			}
		}
		return null;
	}

	/**
	 * Gets an entry in a Pokémon table as a boolean.
	 * @param section The section to get an entry from.
	 * @return The entry in the Pokémon table as a boolean.
	 */
	public static boolean getTableEntryBoolean(String section) {
		String tableEntry = getTableEntry(section);
		return "yes".equals(tableEntry);
	}

	/**
	 * Gets an entry in a Pokémon table as an integer.
	 * @param section The section to get an entry from.
	 * @return The entry in the Pokémon table as an integer.
	 */
	public static int getTableEntryInt(String section) {
		String tableEntry = getTableEntry(section);
		if (tableEntry == null) {
			return 0;
		} else {
			tableEntry = tableEntry.replaceAll(",", "");
			tableEntry = removeSpaces(tableEntry);
			return Integer.parseInt(tableEntry);
		}
	}

	/**
	 * Gets an entry in a Pokémon table as an integer.
	 * @param section The section to get an entry from.
	 * @return The entry in the Pokémon table as an integer.
	 */
	public static Integer getTableEntryInteger(String section) {
		try {
			return getTableEntryInt(section);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Gets an entry in a Pokémon table as a float.
	 * @param section The section to get an entry from.
	 * @return The entry in the Pokémon table as a float.
	 */
	public static float getTableEntryFloat(String section) {
		String tableEntry = getTableEntry(section);
		if (tableEntry == null) {
			return 0;
		} else {
			return Float.parseFloat(tableEntry);
		}
	}

	/**
	 * Gets a table split by row.
	 * @param totalRaw
	 * @param header The string in the header of the table.
	 * @param footer The string in the footer of the table.
	 * @param split The string to split rows by.
	 * @return The table, split by row.
	 */
	public static String[] getSplit6(String totalRaw, String header, String footer, String split) {
		currentRaw = getSubstringBetween(totalRaw, header, footer);
		return currentRaw.split(split);
	}

	/**
	 * Gets a TM table split by row.
	 * @param totalRaw The document to get a TM table from.
	 * @return The TM table, split by row.
	 */
	public static String[] getTMSplit6(String totalRaw) {
		return getSplit6(totalRaw, "tmh/6", "tmf/6", "tm6\\|");
	}

	/**
	 * Gets a tutor table split by row.
	 * @param totalRaw The document to get a tutor table from.
	 * @return The tutor table, split by row.
	 */
	public static String[] getTutorSplit6(String totalRaw) {
		return getSplit6(totalRaw, "tutorh/6", "tutorf/6", "tutor6\\|");
	}

	/**
	 * Gets a pre-evolution table split by row.
	 * @param totalRaw The document to get a pre-evolution table from.
	 * @return The pre-evolution table, split by row.
	 */
	public static String[] getPrevoSplit6(String totalRaw) {
		return getSplit6(totalRaw, "prevoh/6", "prevof/6", "prevo6\\|");
	}

	/**
	 * Converts a name's spelling if needed.
	 * @param convertMap The map of names that can be converted.
	 * @param name The name to convert.
	 * @return The converted spelling.
	 */
	public static String convertSpelling(Map<String, String> convertMap, String name) {
		String newSpelling = convertMap.get(name);
		return newSpelling == null ? name : newSpelling;
	}

	/**
	 * Converts a Pokémon's spelling from Generation 6 to Generation 5.
	 * @param pokemon The Pokémon spelling to convert.
	 * @return The converted Pokémon spelling.
	 */
	public static String convertPokemonSpelling(String pokemon) {
		if (pokemonConvertMap == null) {
			pokemonConvertMap = new HashMap<>();
			pokemonConvertMap.put("Mime Jr.", "Mime_Jr.");
			pokemonConvertMap.put("Mr. Mime", "MrMime");
			pokemonConvertMap.put("Nidoran♀", "Nidoranfemale");
			pokemonConvertMap.put("Nidoran♂", "Nidoranmale");
			pokemonConvertMap.put("Farfetch'd", "Farfetchd");
		}
		return convertSpelling(pokemonConvertMap, pokemon);
	}

	/**
	 * Converts a move's spelling from Generation 6 to Generation 5.
	 * @param move The move spelling to convert.
	 * @return The converted move spelling.
	 */
	public static String convertMoveSpelling(String move) {
		if (moveConvertMap == null) {
			moveConvertMap = new HashMap<>();
			moveConvertMap.put("Ancient Power", "AncientPower");
			moveConvertMap.put("Bubble Beam", "BubbleBeam");
			moveConvertMap.put("Double Slap", "DoubleSlap");
			moveConvertMap.put("Dragon Breath", "DragonBreath");
			moveConvertMap.put("Dynamic Punch", "DynamicPunch");
			moveConvertMap.put("Extreme Speed", "ExtremeSpeed");
			moveConvertMap.put("Feather Dance", "FeatherDance");
			moveConvertMap.put("Feint Attack", "Faint Attack");
			moveConvertMap.put("Grass Whistle", "GrassWhistle");
			moveConvertMap.put("High Jump Kick", "Hi Jump Kick");
			moveConvertMap.put("Sand Attack", "Sand-Attack");
			moveConvertMap.put("Self-Destruct", "Selfdestruct");
			moveConvertMap.put("Smelling Salts", "SmellingSalt");
			moveConvertMap.put("Smokescreen", "SmokeScreen");
			moveConvertMap.put("Soft-Boiled", "Softboiled");
			moveConvertMap.put("Solar Beam", "SolarBeam");
			moveConvertMap.put("Sonic Boom", "SonicBoom");
			moveConvertMap.put("ThunderPunch", "Thunder Punch");
			moveConvertMap.put("Thunder Shock", "ThunderShock");
		}
		return convertSpelling(moveConvertMap, move);
	}

	/**
	 * Adds multiple strings to a set at once.
	 * @param set The set to add to.
	 * @param strings The strings to add to the set.
	 */
	public static void addMultipleToSet(Set<String> set, String... strings) {
		for (String string : strings) {
			set.add(string);
		}
	}

	/**
	 * Gets a TM move's name from a table line.
	 * @param line The line to get a TM move from.
	 * @return The TM move's name.
	 */
	public static String getTMMoveFromLine(String line) {
		int pipeIndex1 = line.indexOf('|');
		int linkIndex = line.lastIndexOf("]]");
		int templateIndex = line.lastIndexOf("}}", line.lastIndexOf("}}") - 15);
		int startIndex = Math.max(linkIndex, templateIndex);
		if (startIndex > -1) {
			pipeIndex1 = line.indexOf('|', startIndex);
		}
		int pipeIndex2 = line.indexOf('|', pipeIndex1 + 1);
		return line.substring(pipeIndex1 + 1, pipeIndex2);
	}

	/**
	 * Gets a tutor move's name from a table line.
	 * @param line The line to get a tutor move from.
	 * @return The tutor move's name.
	 */
	public static String getTutorMoveFromLine(String line) {
		int pipeIndex1 = line.indexOf('|');
		return line.substring(0, pipeIndex1);
	}

	/**
	 * Gets a pre-evolution move's name from a table line.
	 * @param line The line to get a pre-evolution move from.
	 * @return The pre-evolution move's name.
	 */
	public static String getPrevoMoveFromLine(String line) {
		if (line.contains("|e|")) {
			return "";
		}
		int pipeIndex = 0;
		for (int i = 0; i < 6; i++) {
			pipeIndex = line.indexOf('|', pipeIndex + 1);
		}
		int pipeIndex2 = line.indexOf('|', pipeIndex + 1);
		return line.substring(pipeIndex + 1, pipeIndex2);
	}

	/**
	 * Excludes certain strings from a string.
	 * @param initString The string to exclude strings from.
	 * @param strings The strings to exclude.
	 * @return The filtered string.
	 */
	public static String excludeStrings(String initString, String... strings) {
		StringBuilder regex = new StringBuilder();
		boolean first = true;
		for (String string : strings) {
			if (!first) {
				regex.append('|');
			}
			for (char c : string.toCharArray()) {
				if (c == '{' || c == '}' || c == '[' || c == ']' || c == '|') {
					regex.append("\\");
				}
				regex.append(c);
			}
			first = false;
		}
		return initString.replaceAll(regex.toString(), "");
	}

	/**
	 * Checks if a string equals any of a list of strings.
	 * @param string The string to look for equality with.
	 * @param others A list of strings that can match the first string.
	 * @return Whether the first string equals any of the other strings.
	 */
	public static boolean equalsAny(String string, String... others) {
		for (String other : others) {
			if (string.equals(other)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if a string contains any of a list of strings.
	 * @param string The string to search in.
	 * @param others A list of strings that can be contained by the first string.
	 * @return Whether the first string contains any of the other strings.
	 */
	public static boolean containsAny(String string, String... others) {
		for (String other : others) {
			if (string.contains(other)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Converts a Pokédex number into a string.
	 * @param dexNumber The Pokédex number to convert.
	 * @return The converted Pokédex number.
	 */
	public static String convertDexNumberString(int dexNumber) {
		String dexString = Integer.toString(dexNumber);
		if (dexNumber < 100) {
			dexString = '0' + dexString;
			if (dexNumber < 10) {
				dexString = '0' + dexString;
			}
		}
		return dexString;
	}

	/**
	 * Gets the name of a particular form of a Pokémon.
	 * @param pokemon The base Pokémon species name.
	 * @param form The form index of the Pokémon.
	 * @return The name of the specified form of the Pokémon.
	 */
	public static String getFormName(String pokemon, int form) {
		if (pokemon.equals("Wormadam")) {
			switch (form) {
			case 0:
				return "Wormadam-Plant";
			case 1:
				return "Wormadam-Sandy";
			case 2:
				return "Wormadam-Trash";
			}
		} else if (pokemon.equals("Castform")) {
			return form <= 0 ? pokemon : "";
		} else if (pokemon.equals("Deoxys")) {
			switch (form) {
			case 0:
				return "Deoxys";
			case 1:
				return "Deoxys-Attack";
			case 2:
				return "Deoxys-Defense";
			case 3:
				return "Deoxys-Speed";
			}
		}
		return Pokemon.getTranslatedName(pokemon);
	}
	
	/**
	 * Gets all moves that no Pokémon can learn.
	 * @return A set of all moves that no Pokémon can learn.
	 */
	public static Set<String> getUnavailableMoves() {
		if (unavailableMoves == null) {
			unavailableMoves = new HashSet<>();
			unavailableMoves.addAll(
				Arrays.asList("Blue Flare", "Bolt Strike", "Dark Void", "Freeze Shock", "Fusion Bolt", "Fusion Flare",
						"Glaciate", "Head Charge", "Heart Swap", "Horn Leech", "Ice Burn", "Judgment", "Lunar Dance",
						"Magma Storm", "Relic Song", "Roar of Time", "Sacred Sword", "Searing Shot", "Secret Sword",
						"Seed Flare", "Shadow Force", "Simple Beam", "Spacial Rend", "Tail Slap", "Techno Blast",
						"Aromatic Mist", "Crafty Shield", "Electrify", "Fairy Lock", "Flying Press", "Forest's Curse",
						"Geomancy", "King's Shield", "Land's Wrath", "Noble Roar", "Oblivion Wing", "Parabolic Charge",
						"Parting Shot", "Powder", "Topsy-Turvy", "Trick-or-Treat", "Diamond Storm", "Hyperspace Hole",
						"Hyperspace Fury", "Steam Eruption", "Thousand Arrows", "Thousand Waves", "Light of Ruin"));
		}
		return unavailableMoves;
	}

	/**
	 * Translates an Ability into English.
	 * @param ability The database name of the Ability.
	 * @return The Ability's English name.
	 */
	public static String translateAbility(String ability) {
		ability = ability.replace(" ", "");
		return Database.getDatabase().getLangMap().get("ability." + ability + ".name");
	}
	
	/**
	 * Translates a held item ID into English.
	 * @param heldItem The ID of the held item.
	 * @return The held item's English name.
	 */
	public static String translateHeldItem(String heldItem) {
		heldItem = heldItem.replace("pixelmon:", "");
		return Database.getDatabase().getLangMap().get("item." + heldItem + ".name");
	}
	
	/**
	 * Attempts to form a translated item name based on its ID.
	 * @param itemID The ID of the item.
	 * @return The translated item name
	 */
	public static String guessItemName(String itemID) {
		String filteredID = StringUtil.getSubstringBetween(itemID, ":", null);
		filteredID = filteredID.replaceAll(":0", "");
		Map<String, String> itemNameMap = getItemNameMap();
		if (itemNameMap.containsKey(filteredID)) {
			return itemNameMap.get(filteredID);
		}
		String[] splitString = filteredID.split("_");
		String newName = "";
		for (String part : splitString) {
			if (!newName.isEmpty()) {
				newName += " ";
			}
			newName += Character.toUpperCase(part.charAt(0));
			if (part.length() > 1) {
				newName += part.substring(1);
			}
		}
		return newName;
	}
	
	/**
	 * Gets the item name map. Initializes it if not already initialized.
	 * @return The item name map.
	 */
	private static Map<String, String> getItemNameMap() {
		if (itemNameMap == null) {
			itemNameMap = new HashMap<>();
			itemNameMap.put("beef", "Raw Beef");
			itemNameMap.put("brown_mushroom", "Mushroom1");
			itemNameMap.put("chicken", "Raw Chicken");
			itemNameMap.put("dirt:1", "Coarse Dirt");
			itemNameMap.put("double_plant", "Sunflower");
			itemNameMap.put("double_plant:4", "Rose Bush");
			itemNameMap.put("double_plant:5", "Peony");
			itemNameMap.put("dye", "Ink Sac");
			itemNameMap.put("dye:15", "Bone Meal");
			itemNameMap.put("dye:2", "Cactus Green");
			itemNameMap.put("dye:3", "Cocoa Beans");
			itemNameMap.put("fish", "Raw Fish");
			itemNameMap.put("fish:1", "Raw Salmon");
			itemNameMap.put("fish:3", "Pufferfish");
			itemNameMap.put("iron_block", "Block of Iron");
			itemNameMap.put("log", "Oak Wood");
			itemNameMap.put("log:1", "Spruce Wood");
			itemNameMap.put("mutton", "Raw Mutton");
			itemNameMap.put("noteblock", "Note Block");
			itemNameMap.put("porkchop", "Raw Porkchop");
			itemNameMap.put("prismarine:2", "Dark Prismarine");
			itemNameMap.put("quartz", "Nether Quartz");
			itemNameMap.put("rabbit", "Raw Rabbit");
			itemNameMap.put("record_strad", "Music Disc strad");
			itemNameMap.put("red_flower", "Poppy");
			itemNameMap.put("red_flower:2", "Allium");
			itemNameMap.put("red_flower:3", "Azure Bluet");
			itemNameMap.put("red_flower:5", "Orange Tulip");
			itemNameMap.put("red_flower:6", "Pink Tulip");
			itemNameMap.put("red_mushroom", "Mushroom2");
			itemNameMap.put("red_mushroom_block", "Mushroom2Block");
			itemNameMap.put("reeds", "Sugar Canes");
			itemNameMap.put("sandstone:3", "Smooth Sandstone");
			itemNameMap.put("sapling:3", "Jungle Sapling");
			itemNameMap.put("slime_ball", "Slimeball");
			itemNameMap.put("stained_hardened_clay:6", "Pink Hardened Clay");
			itemNameMap.put("stone:1", "Granite");
			itemNameMap.put("stone:2", "Polished Granite");
			itemNameMap.put("stone:3", "Diorite");
			itemNameMap.put("stone:5", "Andesite");
			itemNameMap.put("stone:6", "Polished Andesite");
			itemNameMap.put("tallgrass", "Grass");
			itemNameMap.put("vine", "Vines");
			itemNameMap.put("waterlily", "Lily Pad");
			itemNameMap.put("web", "Cobweb");
			itemNameMap.put("wheat:1", "Wheat");
			itemNameMap.put("wheat_seeds", "Seeds");
			itemNameMap.put("wool:10", "Purple Wool");
			itemNameMap.put("wool:4", "Yellow Wool");
			itemNameMap.put("yellow_flower", "Dandelion");
			itemNameMap.put("aluminium_ingot", "Aluminum Ingot");
			itemNameMap.put("aluminium_plate", "Aluminum Plate");
			itemNameMap.put("ever_stone", "Everstone");
			itemNameMap.put("kings_rock", "King's Rock");
			itemNameMap.put("poke_ball_lid", "Poké Ball Lid");
			itemNameMap.put("pokemail_air", "Air Mail");
			itemNameMap.put("pokemail_bubble", "Bubble Mail");
			itemNameMap.put("pokemail_grass", "Grass Mail");
			itemNameMap.put("pokemail_inquiry", "Inquiry Mail");
			itemNameMap.put("pokemail_wave", "Wave Mail");
			itemNameMap.put("pokemail_wood", "Wood Mail");
			itemNameMap.put("up-grade", "Up-Grade");
			itemNameMap.put("clay_ball", "Clay");
			itemNameMap.put("clay", "Clay Block");
			itemNameMap.put("leaves", "Oak Leaves");
			itemNameMap.put("flint_and_steel", "Flint and Steel");
			itemNameMap.put("gs_ball", "GS Ball");
			itemNameMap.put("never_melt_ice", "Never-Melt Ice");
			itemNameMap.put("pc", "PC");
			itemNameMap.put("poke_ball", "Poké Ball");
			itemNameMap.put("pokemail_glitter", "Glitter Mail");
			itemNameMap.put("rabbit_foot", "Rabbit's Foot");
		}
		return itemNameMap;
	}

	/**
	 * Appends a wiki table field to an existing table.
	 * @param builder The string builder to append to.
	 * @param fieldName The name of the table field.
	 * @param fieldParameter The parameter to pass into the table.
	 */
	public static void appendTableField(StringBuilder builder, String fieldName, Object fieldParameter) {
		if (fieldParameter != null) {
			builder.append("\n|");
			builder.append(fieldName);
			builder.append("=");
			builder.append(fieldParameter.toString());
		}
	}
	
	/**
	 * Encloses a list of strings with characters.
	 * @param strings The strings to enclose.
	 * @param start The string to add at the start of strings.
	 * @param middle The string to add between strings.
	 * @param end The string to add at the end of string.
	 * @return The combined, enclosed string.
	 */
	public static String encloseStrings(List<String> strings, String start, String middle, String end) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (String string : strings) {
			if (!first) {
				builder.append(middle);
			}
			first = false;
			builder.append(start);
			builder.append(string);
			builder.append(end);
		}
		return builder.toString();
	}

	/**
	 * Separates a list of strings with characters.
	 * @param strings The strings to separate.
	 * @param middle The string to add between strings.
	 * @return The combined, separated string.
	 */
	public static String separateStrings(List<String> strings, String middle) {
		return encloseStrings(strings, "", middle, "");
	}
	
	/**
	 * Corrects the name of a Pokémon whose name is spelled incorrectly.
	 * @param name The name of the Pokémon.
	 * @return The corrected name.
	 */
	public static String correctPokemonName(String name) {
		Map<String, String> pokemonCorrectMap = getPokemonCorrectMap();
		if (pokemonCorrectMap.containsKey(name)) {
			return pokemonCorrectMap.get(name);
		}
		return name;
	}
	
	/**
	 * Gets the Pokémon corrected name map. Loads the map if uninitialized.
	 * @return The Pokémon corrected name map.
	 */
	private static Map<String, String> getPokemonCorrectMap() {
		if (pokemonCorrectMap == null) {
			pokemonCorrectMap = new HashMap<>();
			pokemonCorrectMap.put("Ho-oh", "Ho-Oh");
			pokemonCorrectMap.put("MrMime", "Mr. Mime");
			pokemonCorrectMap.put("Nidoranfemale", "Nidoran♀");
			pokemonCorrectMap.put("Nidoranmale", "Nidoran♂");
			pokemonCorrectMap.put("Farfetchd", "Farfetch'd");
		}
		return pokemonCorrectMap;
	}
}
