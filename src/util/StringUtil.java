package util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
		int indexBreak = currentRaw.indexOf("}}", startIndex);
		int startBrace = currentRaw.indexOf("{{", startIndex);
		if (startBrace != -1 && startBrace < indexBreak) {
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
			return form == 0 ? pokemon : "";
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
}