package miner;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.APIConnection;
import util.StringUtil;

/**
 * Mines data for language files.
 */
public class LangMiner {

	/**
	 * Gets translated attack names and descriptions.
	 * @return Translated attack names and descriptions.
	 */
	@SuppressWarnings({ "null", "unused" })
	public String getAttackDetails() {
		StringBuilder builder = new StringBuilder();
		List<String> moveList = APIConnection.getGenerationItems("moves", StringUtil.ALL_GENERATIONS);
		String debugMove = "";
		if (!debugMove.isEmpty()) {
			debugMove += " (move)";
		}
		for (String move : moveList) {
			try {
				if (!debugMove.isEmpty()) {
					move = debugMove;
				}
				if (move.contains("Category") || move.contains("User:")) {
					continue;
				}
				String raw = APIConnection.getArticleSource(move);
				String currentRaw = StringUtil.getSubstringBetween(raw, "{{movedesc", "|}");
				StringUtil.currentRaw = raw;
				if (move.contains("Shadow") && "Shadow".equals(StringUtil.getTableEntry("type"))) {
					continue;
				}

				String description = StringUtil.getSubstringBetween(currentRaw, "ORAS}}|", null);
				description = description.substring(0, description.lastIndexOf("}}"));
				description = StringUtil.excludeStrings(description, "{{m|", "}}", "<!--[sic]-->", "[", "]", "{{t|", "{{type|", "{{a|");
				if (description.contains("{{gameabbrev7|SM")) {
					description = StringUtil.getSubstringBetween(description, "{{gameabbrev7|SM", null);
					description = description.substring(description.lastIndexOf('|') + 1);
				}

				String moveName = StringUtil.shortenString(move, 7);
				String moveKey = StringUtil.convertMoveSpelling(moveName).toLowerCase();
				builder.append("attack.");
				builder.append(moveKey);
				builder.append(".name=");
				builder.append(moveName);
				builder.append("\nattack.");
				builder.append(moveKey);
				builder.append(".description=");
				builder.append(description);
				builder.append("\n");
				if (!debugMove.isEmpty()) {
					System.out.println(description);
					break;
				}
			} catch (Exception e) {
				System.out.println(move);
				throw e;
			}
		}
		return builder.toString();
	}
	
	/**
	 * Gets Pokédex entries for the lang files.
	 * @return Lang entries for Pokédex entries.
	 */
	public String getPokedexEntries() {
		StringBuilder builder = new StringBuilder();
		List<String> pokemonList = APIConnection.getGenerationPokemon(StringUtil.ALL_GENERATIONS);
		String debugPokemon = "";
		if (!debugPokemon.isEmpty()) {
			debugPokemon += " (Pokémon)";
		}
		Pattern endPattern = Pattern.compile("(\"|\\.|!) ?\\}\\}");
		Pattern mPattern = Pattern.compile("\\{\\{m\\|.*\\|(.*)\\}\\}");
		Pattern ttPattern = Pattern.compile("\\{\\{tt\\|(.*)\\|(.*)\\}\\}");
		Matcher matcher;
		for (String pokemon : pokemonList) {
			if (!debugPokemon.isEmpty()) {
				pokemon = debugPokemon;
			}
			String pokemonName = StringUtil.shortenString(pokemon, 10);
			try {
				String raw = APIConnection.getArticleSource(pokemon);
				String entry = StringUtil.getSubstringBetween(raw, "Dex/Gen|gen=VI", "Dex/Footer");
				entry = StringUtil.getSubstringBetween(entry, "Omega Ruby", null);
				matcher = endPattern.matcher(entry);
				matcher.find();
				String matcher0 = matcher.group(0);
				entry = StringUtil.getSubstringBetween(entry, "entry=", matcher0) + matcher0;
				entry = entry.replace(matcher0, matcher.group(1));
				
				matcher = mPattern.matcher(entry);
				while (matcher.find()) {
					entry = entry.replace(matcher.group(0), matcher.group(1));
				}
				
				matcher = ttPattern.matcher(entry);
				while (matcher.find()) {
					entry = entry.replace(matcher.group(0), matcher.group(2));
				}
				
				entry = StringUtil.excludeStrings(entry, "{{wp|", "{{m|", "{{p|", "{{type|", "}}", "[[", "]]");
				String pokemonLower = StringUtil.convertPokemonSpelling(pokemonName).toLowerCase();
				builder.append("pixelmon.");
				builder.append(pokemonLower);
				builder.append(".name=");
				builder.append(pokemonName);
				builder.append("\npixelmon.");
				builder.append(pokemonLower);
				builder.append(".description=");
				builder.append(entry);
				builder.append('\n');
				if (!debugPokemon.isEmpty()) {
					System.out.println(entry);
					break;
				}
			} catch (Exception e) {
				System.out.println(pokemonName);
				throw e;
			}
		}
		return builder.toString();
	}
}
