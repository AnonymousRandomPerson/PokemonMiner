package miner.wiki;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import miner.Miner;
import miner.storage.DexNumberContainer;
import miner.storage.Move;
import miner.storage.Pokemon;
import pixelmon.EnumPokemon;
import util.APIConnection;
import util.StringUtil;

/**
 * Creates articles for Pokémon lists.
 */
public class ListMiner extends Miner {

	/** The last Pokédex number of each Generation. */
	private final int[] generationEnd = new int[] { 0, 151, 251, 386, 493, 649, 721, 802 };

	/**
	 * Generates the available Pokémon progress page.
	 * @return Wikicode for the available Pokémon progress page.
	 */
	public String getAvailableProgress() {
		builder = new StringBuilder();

		DexNumberContainer dexContainer = database.getDexContainer();
		totalRaw = APIConnection.getArticleSource("List of Pokémon by National Pokédex number");
		int currentIndex = 0;

		int generation = 1;
		int totalAvailable = 0;
		int generationPokemon = 0;
		StringBuilder images = null;
		for (int i = 1; i <= 721; i++) {
			if (i == generationEnd[generation - 1] + 1) {
				builder.append("==Generation ");
				builder.append(generation);
				builder.append("==\n");
				images = new StringBuilder();
			}

			String dexString = StringUtil.convertDexNumberString(i);
			currentIndex = totalRaw.indexOf("|" + dexString + "|", currentIndex);
			if (totalRaw.charAt(currentIndex - 1) == 'x') {
				currentIndex = totalRaw.indexOf("|" + dexString + "|", currentIndex + 1);
			}
			int nameIndex = currentIndex + 5;
			int endIndex = totalRaw.indexOf('|', nameIndex);
			String pokemonName = totalRaw.substring(nameIndex, endIndex);
			pokemonName.trim();

			images.append("[[File:");
			images.append(pokemonName);
			if (dexContainer.hasDexNumber(i)) {
				images.append(".png|link=");
				images.append(pokemonName);
				generationPokemon++;
			} else {
				images.append("Gray.png");
			}
			images.append("|32px]]\n");

			if (i == generationEnd[generation]) {
				builder.append('*');
				builder.append(generationPokemon);
				builder.append('/');
				int totalGeneration = generationEnd[generation] - generationEnd[generation - 1];
				builder.append(totalGeneration);
				builder.append(" Pokémon ({{percent|");
				builder.append(generationPokemon);
				builder.append('|');
				builder.append(totalGeneration);
				builder.append("}})\n\n");
				builder.append(images);
				totalAvailable += generationPokemon;
				generation++;
				generationPokemon = 0;
			}
		}

		StringBuilder start = new StringBuilder();
		start.append("There are ");
		start.append(totalAvailable);
		start.append(" [[available Pokémon]] in [[Pixelmon]] out of the 802 Pokémon in existence, or {{percent|");
		start.append(totalAvailable);
		start.append("|802}}.\n");
		builder.insert(0, start);

		builder.append("\n==Generation 7==\n");
		builder.append("*0/81 Pokémon ({{percent|0|81}})\n");
		builder.append("*No Pokémon from Generation 7 will be added until Generation 8 is released.");

		builder.append("\n==Mega Evolutions==\n");
		String folder = "megas";
		File megaFolder = new File(folder);
		File[] megaFiles = megaFolder.listFiles();
		int totalMegas = megaFiles.length;
		int currentMegas = 0;
		images = new StringBuilder();
		for (File file : megaFiles) {
			String fileName = file.getName();
			String[] nameSplit = fileName.replace(".png", "").split("-");
			int dexNumber = Integer.parseInt(nameSplit[0]);
			String dexString = StringUtil.convertDexNumberString(dexNumber);

			currentIndex = totalRaw.indexOf("|" + dexString + "|");
			if (dexNumber <= 151) {
				currentIndex = totalRaw.indexOf("|" + dexString + "|", currentIndex + 1);
			}
			int nameIndex = currentIndex + 5;
			int endIndex = totalRaw.indexOf('|', nameIndex);

			String pokemonName = totalRaw.substring(nameIndex, endIndex);
			pokemonName.trim();

			String prefix = StringUtil.capitalizeFirst(nameSplit[1]);
			String suffix = nameSplit.length > 2 ? StringUtil.capitalizeFirst(nameSplit[2]) : "";
			boolean hasMega = EnumPokemon.hasMega(pokemonName);
			if (hasMega) {
				currentMegas++;
			}
			images.append("[[File:");
			images.append(prefix);
			if (hasMega) {
				images.append(' ');
			}
			images.append(pokemonName);
			if (!suffix.isEmpty()) {
				if (hasMega) {
					images.append(' ');
				}
				images.append(suffix);
			}
			if (!hasMega) {
				images.append("Gray");
			}
			images.append(".png|32px");
			if (EnumPokemon.isPokemon(pokemonName)) {
				images.append("|link=");
				images.append(pokemonName);
			}
			images.append("]]\n");
		}
		builder.append('*');
		builder.append(currentMegas);
		builder.append('/');
		builder.append(totalMegas);
		builder.append(" [[Mega Evolutions]] ({{percent|");
		builder.append(currentMegas);
		builder.append('|');
		builder.append(totalMegas);
		builder.append("}})\n\n");
		builder.append(images);

		return builder.toString();
	}

	/**
	 * Gets wikicode for the Available Pokémon article.
	 * @return Wikicode for the Available Pokémon article.
	 */
	public String getAvailablePokemon() {
		builder = new StringBuilder();

		builder.append("<languages/>\n");
		builder.append("<translate>\n");
		builder.append("<!--T:1-->\n");
		builder.append("This is a list of all ");
		builder.append(EnumPokemon.values().length);
		builder.append(" Pokémon currently available in [[Pixelmon]].\n");
		builder.append("\n<!--T:2-->\n");
		builder.append(
				"The progress of available Pokémon compared to all existing Pokémon in the ''Pokémon'' games can be found [[Available Pokémon/Progress|here]].\n");
		builder.append("<div class=\"row\">");

		String[] ordinals = { "First", "Second", "Third", "Fourth", "Fifth", "Sixth" };
		DexNumberContainer dexContainer = database.getDexContainer();
		List<Pokemon> dexList = dexContainer.dexList;
		int currentIndex = 0;
		int currentNumber = 0;

		for (int i = 0; i < ordinals.length; i++) {
			builder.append("\n<div class=\"col-md-2");
			builder.append("\">");
			builder.append("\n<center>'''''");
			builder.append(ordinals[i]);
			builder.append(" Generation'''''</center>");
			boolean first = true;
			while (currentIndex < dexList.size()) {
				Pokemon pokemon = dexList.get(currentIndex);
				currentNumber = pokemon.nationalPokedexNumber;
				if (currentNumber <= generationEnd[i + 1]) {
					builder.append('\n');
					if (!first) {
						builder.append("<br />");
					}
					builder.append("'''#");
					builder.append(StringUtil.convertDexNumberString(currentNumber));
					builder.append(" {{p|");
					builder.append(pokemon.getTranslatedName());
					builder.append("}}'''");
					currentIndex++;
					first = false;
				} else {
					break;
				}
			}
			builder.append("\n</div>");
		}

		builder.append("\n</div>\n");
		builder.append("</translate>");

		return builder.toString();
	}

	/**
	 * Gets wikicode for the Shiny Pokémon/Pictures article.
	 * @return Wikicode for the Shiny Pokémon/Pictures article.
	 */
	public String getShinyPictures() {
		builder = new StringBuilder();

		builder.append("This is a list of pictures of each [[available Pokémon]]'s [[Shiny]] form.\n");
		builder.append("__TOC__");

		DexNumberContainer dexContainer = database.getDexContainer();
		List<Pokemon> dexList = dexContainer.dexList;
		int currentIndex = 0;
		int currentNumber = 0;

		for (int i = 1; i <= 6; i++) {
			builder.append("\n==Generation ");
			builder.append(i);
			builder.append("==");
			builder.append("\n<gallery>");

			while (currentIndex < dexList.size()) {
				Pokemon pokemon = dexList.get(currentIndex);
				currentNumber = pokemon.nationalPokedexNumber;
				if (currentNumber <= generationEnd[i]) {
					String translatedName = pokemon.getTranslatedName();
					StringUtil.currentRaw = APIConnection.getArticleSourcePixelmon(translatedName);
					totalRaw = StringUtil.currentRaw;
					String imageName = StringUtil.getTableEntry("shinyimage");
					if (imageName == null) {
						imageName = translatedName + "S.png";
					}
					builder.append('\n');
					builder.append(imageName);
					builder.append("|[[");
					builder.append(translatedName);
					builder.append("]]");
					currentIndex++;
				} else {
					break;
				}
			}

			builder.append("\n</gallery>");
		}
		
		preserveSection("[[Mega Evolutions]]");

		return builder.toString();
	}

	/**
	 * Gets wikicode for the EV yield article.
	 * @return Wikicode for the EV yield article.
	 */
	public String getEVYield() {
		builder = new StringBuilder();

		builder.append("This is a list of [[available Pokémon]] by their [[effort value]] yield.\n");
		builder.append("{| class=\"wikitable sortable\"\n");
		builder.append("|-\n");
		builder.append("! Pokémon\n");
		builder.append("! style=\"text-align:center;width:90px\"| HP\n");
		builder.append("! style=\"text-align:center;width:90px\"| Attack\n");
		builder.append("! style=\"text-align:center;width:90px\"| Defense\n");
		builder.append("! style=\"text-align:center;width:90px\"| Special{{-}}Attack\n");
		builder.append("! style=\"text-align:center;width:90px\"| Special{{-}}Defense\n");
		builder.append("! style=\"text-align:center;width:90px\"| Speed\n");
		builder.append("! style=\"text-align:center;width:90px\"| Total");

		DexNumberContainer dexContainer = database.getDexContainer();
		String query = "SELECT NATIONALPOKEDEXNUMBER, PIXELMONNAME, FORM, EVGAINHP, EVGAINATK, EVGAINDEF, EVGAINSPATK, EVGAINSPDEF, EVGAINSPD FROM PIXELMON"
				+ " ORDER BY NATIONALPOKEDEXNUMBER, FORM";
		ResultSet result = database.executeQuery(query);
		String[] evs = { "HP", "ATK", "DEF", "SPATK", "SPDEF", "SPD" };
		try {
			Set<String> usedNames = new HashSet<>();
			while (result.next()) {
				int dexNumber = result.getInt("NATIONALPOKEDEXNUMBER");
				if (dexContainer.hasDexNumber(dexNumber)) {
					String translatedName = StringUtil.getFormName(result.getString("PIXELMONNAME"),
							result.getInt("FORM"));
					if (!translatedName.isEmpty()) {
						if (usedNames.contains(translatedName)) {
							continue;
						}
						usedNames.add(translatedName);
						builder.append("\n{{EVList|");
						builder.append(translatedName);
						for (String ev : evs) {
							int evGain = result.getInt("EVGAIN" + ev);
							if (evGain > 0) {
								builder.append('|');
								builder.append(ev.toLowerCase());
								builder.append('=');
								builder.append(evGain);
							}
						}
						builder.append("}}");
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		builder.append("\n|}");

		return builder.toString();
	}

	/**
	 * Gets wikicode for the spawn location article.
	 * @return Wikicode for the spawn location article.
	 */
	public String getSpawnLocations() {
		builder = new StringBuilder();

		builder.append(
				"A Pokémon's spawn location determines which environment(s) it can spawn in. This includes land, water, air, air persistent, and underground. Some Pokémon are able to spawn in multiple spawn locations.\n\n");
		builder.append(
				"Spawn location is not to be confused with spawn [[biome]], which is another factor determining where Pokémon can spawn.\n\n");
		builder.append(
				"Note: Although several Pokémon cannot spawn naturally, these Pokémon are still affected by spawn location when spawned by [[Pixelmon spawners]] or if modified to spawn naturally through means such as [[Spawn of Psyduck]].\n");
		builder.append("==Land spawns==\n");
		builder.append(
				"These Pokémon spawn on land. They will only spawn on dirt-based blocks ({{mc|Grass_Block|grass}}, {{mc|dirt}}, {{mc|mycelium}}, {{mc|podzol}}), stone-based blocks ([[stone]], [[cobblestone]], ores, metal blocks, {{mc|sandstone}}, {{mc|gravel}}), [[sand]], {{mc|Soul_Sand|soul sand}}, {{mc|leaves}}, {{mc|snow}}, {{mc|ice}}, and {{mc|Packed_Ice|packed ice}}.");
		addLocationSpawns("land");

		builder.append("\n==Water spawns==\n");
		builder.append("These Pokémon spawn in {{mc|water}}.");
		addLocationSpawns("water");

		builder.append("\n==Air spawns==\n");
		builder.append(
				"Despite the name of the spawn location, air spawns are mostly the same as land spawns with regards to spawning. The only differences are related to movement AI and [[Pixelmon spawners]].");
		addLocationSpawns("air");

		builder.append("\n==Air persistent spawns==\n");
		builder.append("These Pokémon spawn in the air.");
		addLocationSpawns("airpersistent");

		builder.append("\n==Underground spawns==\n");
		builder.append(
				"These Pokémon spawn in enclosed spaces such as caves. They spawn on dirt-based blocks ({{mc|Grass_Block|grass}}, {{mc|dirt}}, {{mc|mycelium}}, {{mc|podzol}}) and stone-based blocks ([[stone]], [[cobblestone]], ores, metal blocks, {{mc|sandstone}}, {{mc|gravel}}).");
		addLocationSpawns("underground");

		return builder.toString();
	}

	/**
	 * Gets wikicode for the move IDs article.
	 * @return Wikicode for the move IDs article.
	 */
	public String getMoveIDs() {
		builder = new StringBuilder();

		builder.append(
				"This is a list of moves and their internal IDs. These IDs are used with NBT tags and in the Pixelmon database.\n");
		builder.append("{| class=\"wikitable sortable\"\n");
		builder.append("|-\n");
		builder.append("!Move\n");
		builder.append("!ID");
		ResultSet result = database.executeQuery("SELECT MOVEID, NAME FROM MOVES ORDER BY NAME");
		Set<String> unavailable = StringUtil.getUnavailableMoves();
		Set<String> link = new HashSet<>();
		link.addAll(Arrays.asList("Growth", "Metronome", "Psychic", "Wood Hammer"));
		try {
			while (result.next()) {
				String name = Move.translate(result.getString("NAME"));
				builder.append("\n{{MoveID|");
				builder.append(result.getInt("MOVEID"));
				builder.append('|');
				builder.append(name);
				if (unavailable.contains(name)) {
					builder.append("|1");
				} else if (link.contains(name)) {
					builder.append("|link=");
					builder.append(name);
					builder.append(" (move)");
				}
				builder.append("}}");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		builder.append("\n|}");

		return builder.toString();
	}

	/**
	 * Gets wikicode for all possible tutor moves.
	 * @return Wikicode for all possible tutor moves.
	 */
	public String getTutorList() {
		builder = new StringBuilder();
		Set<String> unavailable = StringUtil.getUnavailableMoves();

		try {
			ResultSet result = database.executeQuery("SELECT NAME FROM MOVES WHERE TUTORTYPE = 1 ORDER BY NAME");
			builder.append("===Tutor moves===");
			while (result.next()) {
				String name = Move.translate(result.getString("NAME"));
				if (!unavailable.contains(name)) {
					builder.append("\n*[[");
					builder.append(name);
					builder.append("]]");
				}
			}
			result = database.executeQuery("SELECT NAME FROM MOVES WHERE TUTORTYPE = 2 ORDER BY NAME");
			builder.append("\n===Event tutor moves===");
			while (result.next()) {
				String name = Move.translate(result.getString("NAME"));
				if (!unavailable.contains(name)) {
					builder.append("\n*[[");
					builder.append(name);
					if (name.equals("Growth")) {
						builder.append(" (move)|Growth");
					}
					builder.append("]]");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return builder.toString();
	}

	/**
	 * Adds Pokémon to the list that spawn in a specific spawn location.
	 * @param location The spawn location to make a list for.
	 */
	private void addLocationSpawns(String location) {
		DexNumberContainer dexContainer = database.getDexContainer();

		StringBuilder query = new StringBuilder();
		query.append(
				"SELECT DISTINCT b.NATIONALPOKEDEXNUMBER, b.PIXELMONNAME, a.LOCATION FROM PIXELMONSPAWNLOCATIONS a ");
		query.append("JOIN PIXELMON b ON a.PIXELMONID = b.PIXELMONID ");
		query.append("WHERE a.LOCATION = '");
		query.append(location);
		query.append("' ORDER BY b.PIXELMONNAME");
		ResultSet result = database.executeQuery(query);

		try {
			while (result.next()) {
				if (dexContainer.hasDexNumber(result.getInt("NATIONALPOKEDEXNUMBER"))) {
					builder.append("\n*{{p|");
					builder.append(Pokemon.getTranslatedName(result.getString("PIXELMONNAME")));
					builder.append("}}");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
