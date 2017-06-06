package miner;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import database.Database;
import miner.storage.Move;
import miner.storage.Pokemon;
import util.APIConnection;
import util.FileIO;
import util.StringUtil;

/**
 * Gets Pokémon data from Bulbapedia.
 */
public class PokemonMiner extends Miner {

	/**
	 * Gets new Pokémon data from Bulbapedia.
	 * @return The database query for the new Pokémon data.
	 */
	public String getPokemonData() {
		Map<String, Integer> typeMap = database.getTypeMap();

		Map<String, Integer> eggGroupMap = new HashMap<>();
		database.loadMap(eggGroupMap, "EGGGROUPID", "NAME", "EGGGROUPS");

		Map<String, Integer> abilityMap = new HashMap<>();
		database.loadMap(abilityMap, "ABILITYID", "NAME", "ABILITIES");

		StringBuilder baseQuery = new StringBuilder();
		baseQuery.append("INSERT INTO PIXELMON (");
		StringUtil.addCommaSeparated(baseQuery, false, "NATIONALPOKEDEXNUMBER", "PIXELMONFULLNAME", "PIXELMONNAME",
				"PIXELMONTYPE1ID", "PIXELMONTYPE2ID", "ABILITY1ID", "ABILITY2ID", "ABILITYHIDDENID", "EGGGROUP1ID",
				"EGGGROUP2ID", "EGGCYCLES", "POKEDEXHEIGHT", "POKEDEXWIDTH", "POKEDEXLENGTH", "POKEDEXWEIGHT",
				"POKEDEXDESCRIPTION", "MALEPERCENT", "CATCHRATE", "SPAWNTIMEID", "MINGROUPSIZE", "MAXGROUPSIZE",
				"BASEEXP", "EXPERIENCEGROUP", "BASEHP", "BASEATK", "BASEDEF", "BASESPATK", "BASESPDEF", "BASESPD",
				"EVGAINHP", "EVGAINATK", "EVGAINDEF", "EVGAINSPATK", "EVGAINSPDEF", "EVGAINSPD", "MINSPAWNLEVEL",
				"MAXSPAWNLEVEL", "MODELSCALE", "PERCENTTIMID", "PERCENTAGRESSIVE", "ISRIDEABLE", "CANFLY", "CANSWIM",
				"DOESHOVER", "BASEFRIENDSHIP");
		baseQuery.append(") VALUES \n");

		String[] gen6Array = APIConnection.getGen6Pokemon();
		int numPokemon = gen6Array.length;

		for (int i = 0; i < numPokemon; i++) {
			String pokemonName = gen6Array[i];
			Pokemon pokemon = new Pokemon(pokemonName);
			pokemon.truncateName();
			System.out.println(pokemon.name);

			String totalRaw = APIConnection.getArticleSource(pokemonName);
			StringUtil.currentRaw = totalRaw.substring(0, totalRaw.indexOf("Biology"));

			pokemon.nationalPokedexNumber = StringUtil.getTableEntryInt("ndex");
			pokemon.type1 = StringUtil.getTableEntry("type1");
			pokemon.type2 = StringUtil.getTableEntry("type2");
			pokemon.ability1 = StringUtil.removeSpaces(StringUtil.getTableEntry("ability1"));
			pokemon.ability2 = StringUtil.removeSpaces(StringUtil.getTableEntry("ability2"));
			pokemon.abilityHidden = StringUtil.removeSpaces(StringUtil.getTableEntry("abilityd"));
			pokemon.eggGroup1 = StringUtil.removeSpaces(StringUtil.getTableEntry("egggroup1"));
			pokemon.eggGroup2 = StringUtil.removeSpaces(StringUtil.getTableEntry("egggroup2"));
			pokemon.eggCycles = StringUtil.getTableEntryInt("eggcycles");
			pokemon.height = StringUtil.getTableEntryFloat("height-m");
			pokemon.weight = StringUtil.getTableEntryFloat("weight-kg");

			int genderCode = StringUtil.getTableEntryInt("gendercode");
			switch (genderCode) {
			case 256:
			case 255:
				pokemon.genderRatio = -1;
				break;
			case 254:
				pokemon.genderRatio = 0;
				break;
			case 223:
				pokemon.genderRatio = 12;
				break;
			case 191:
				pokemon.genderRatio = 25;
				break;
			case 127:
				pokemon.genderRatio = 50;
				break;
			case 63:
				pokemon.genderRatio = 75;
				break;
			case 31:
				pokemon.genderRatio = 87;
				break;
			case 0:
				pokemon.genderRatio = 100;
				break;
			}

			pokemon.catchRate = StringUtil.getTableEntryInt("catchrate");
			pokemon.expYield = StringUtil.getTableEntryInt("expyield");

			int maxExp = StringUtil.getTableEntryInt("lv100exp");
			switch (maxExp) {
			case 600000:
				pokemon.expGroup = "Erratic";
				break;
			case 800000:
				pokemon.expGroup = "Fast";
				break;
			case 1000000:
				pokemon.expGroup = "MediumFast";
				break;
			case 1059860:
				pokemon.expGroup = "MediumSlow";
				break;
			case 1250000:
				pokemon.expGroup = "Slow";
				break;
			case 1640000:
				pokemon.expGroup = "Fluctuating";
				break;
			}

			pokemon.evHP = StringUtil.getTableEntryInt("evhp");
			pokemon.evAtk = StringUtil.getTableEntryInt("evat");
			pokemon.evDef = StringUtil.getTableEntryInt("evde");
			pokemon.evSpAtk = StringUtil.getTableEntryInt("evsa");
			pokemon.evSpDef = StringUtil.getTableEntryInt("evsd");
			pokemon.evSpd = StringUtil.getTableEntryInt("evsp");

			StringUtil.currentRaw = StringUtil.getSubstringBetween(totalRaw, "=Base stats=", "=Type effectiveness=");
			pokemon.baseHP = StringUtil.getTableEntryInt("HP");
			pokemon.baseAtk = StringUtil.getTableEntryInt("Attack");
			pokemon.baseDef = StringUtil.getTableEntryInt("Defense");
			pokemon.baseSpAtk = StringUtil.getTableEntryInt("SpAtk");
			pokemon.baseSpDef = StringUtil.getTableEntryInt("SpDef");
			pokemon.baseSpd = StringUtil.getTableEntryInt("Speed");

			pokemon.baseFriendship = StringUtil.getTableEntryInt("Friendship");

			baseQuery.append("(");

			StringUtil.addCommaSeparated(baseQuery, true, pokemon.nationalPokedexNumber, pokemon.name, pokemon.name,
					typeMap.get(pokemon.type1), typeMap.get(pokemon.type2), abilityMap.get(pokemon.ability1),
					abilityMap.get(pokemon.ability2), abilityMap.get(pokemon.abilityHidden),
					eggGroupMap.get(pokemon.eggGroup1), eggGroupMap.get(pokemon.eggGroup2), pokemon.eggCycles,
					pokemon.height, pokemon.height, pokemon.height, pokemon.weight, "", pokemon.genderRatio,
					pokemon.catchRate, 1, 1, 1, pokemon.expYield, pokemon.expGroup, pokemon.baseHP, pokemon.baseAtk,
					pokemon.baseDef, pokemon.baseSpAtk, pokemon.baseSpDef, pokemon.baseSpd, pokemon.evHP, pokemon.evAtk,
					pokemon.evDef, pokemon.evSpAtk, pokemon.evSpDef, pokemon.evSpd, 1, 100, 1.0, 80, 20, false, false,
					false, false, pokemon.baseFriendship);
			baseQuery.append(")");
			baseQuery.append(i < numPokemon - 1 ? ",\n" : ";\n");
		}

		return baseQuery.toString();
	}

	/**
	 * Gets data about new Generation 6 moves.
	 * @return The database query for adding new moves.
	 */
	public String getMoveData() {
		Map<String, Integer> typeMap = database.getTypeMap();

		Map<String, Integer> moveCategoryMap = new HashMap<>();
		database.loadMap(moveCategoryMap, "MOVECATEGORYID", "NAME", "MOVECATEGORIES");

		StringBuilder baseQuery = new StringBuilder();
		baseQuery.append("INSERT INTO MOVES (");
		StringUtil.addCommaSeparated(baseQuery, false, "NAME", "TYPEID", "MOVECATEGORYID", "POWER", "ACCURACY", "PP",
				"PPMAX", "EFFECT", "MAKESCONTACT", "HITSALL", "HITSOPPOSITEFOE", "HITSADJACENTFOE", "HITSEXTENDEDFOE",
				"HITSSELF", "HITSADJACENTALLY", "HITSEXTENDEDALLY", "DESCRIPTION");
		baseQuery.append(") VALUES \n");

		String[] gen6Moves = APIConnection.getCategoryMembers("Generation_VI_moves");
		int numMoves = gen6Moves.length;
		for (int i = 0; i < numMoves; i++) {
			String moveName = gen6Moves[i];
			String movePage = APIConnection.getArticleSource(moveName);
			Move move = new Move(moveName);
			move.truncateName();
			System.out.println(move.name);

			StringUtil.currentRaw = movePage.substring(0, movePage.indexOf("==Effect=="));

			move.type = StringUtil.removeSpaces(StringUtil.getTableEntry("type"));
			move.category = StringUtil.removeSpaces(StringUtil.getTableEntry("damagecategory"));
			move.power = StringUtil.getTableEntryInteger("power");
			move.accuracy = StringUtil.getTableEntryInteger("accuracy");
			move.pp = StringUtil.getTableEntryInt("basepp");
			move.ppMax = StringUtil.getTableEntryInt("maxpp");
			move.contact = StringUtil.getTableEntryBoolean("touches");

			String moveRange = StringUtil.removeSpaces(StringUtil.getTableEntry("target"));
			switch (moveRange) {
			case "adjacentally":
				move.hitsAdjacentAlly = true;
				break;
			case "adjacentfoes":
				move.hitsAll = move.hitsAdjacentFoe = move.hitsOpposite = true;
				break;
			case "alladjacent":
				move.hitsAll = move.hitsAdjacentAlly = move.hitsOpposite = move.hitsAdjacentFoe = true;
				break;
			case "anyadjacent":
				move.hitsAdjacentAlly = move.hitsAdjacentFoe = move.hitsOpposite = true;
				break;
			case "any":
				move.hitsAdjacentAlly = move.hitsAdjacentFoe = move.hitsExtendedAlly = move.hitsExtendedFoe = move.hitsOpposite = true;
				break;
			case "foes":
				move.hitsAll = move.hitsAdjacentFoe = move.hitsExtendedFoe = move.hitsOpposite = true;
				break;
			case "all":
			case "self":
				move.hitsSelf = true;
				break;
			case "team":
				move.hitsAll = move.hitsAdjacentAlly = move.hitsExtendedAlly = move.hitsSelf = true;
				break;
			default:
				throw new IllegalArgumentException(moveRange + " not found.");
			}

			baseQuery.append("(");

			StringUtil.addCommaSeparated(baseQuery, true, move.name.replace("'", "''"), typeMap.get(move.type),
					moveCategoryMap.get(move.category), move.power, move.accuracy, move.pp, move.ppMax, "None",
					move.contact, move.hitsAll, move.hitsOpposite, move.hitsAdjacentFoe, move.hitsExtendedFoe,
					move.hitsSelf, move.hitsAdjacentAlly, move.hitsExtendedAlly, "");
			baseQuery.append(")");
			baseQuery.append(i < numMoves - 1 ? ",\n" : ";\n");
		}
		return baseQuery.toString();
	}

	/**
	 * Gets level-up moveset data for Generation 6.
	 * @return Database queries to insert level-up moveset data.
	 */
	public String getLevelUpData() {
		StringBuilder builder = new StringBuilder();

		List<String> pokemonList = APIConnection.getGenerationPokemon(StringUtil.ALL_GENERATIONS);

		int numPokemon = pokemonList.size();
		for (int i = 0; i < numPokemon; i++) {
			String pokemon = pokemonList.get(i);
			String totalRaw = APIConnection.getArticleSource(pokemon + "/Generation_VI_learnset");
			pokemon = pokemon.substring(0, pokemon.length() - 10);

			String currentRaw = StringUtil.getSubstringBetween(totalRaw, "levelh", "levelf");
			if (currentRaw.contains("level6")) {
				String[] moveSplit = currentRaw.split("level6\\|");
				for (int j = 1; j < moveSplit.length; j++) {
					String moveRow = moveSplit[j];
					int pipeIndex1 = moveRow.indexOf('|');
					int pipeIndex2 = moveRow.indexOf('|', pipeIndex1 + 1);

					int level;
					try {
						level = Integer.parseInt(moveRow.substring(0, pipeIndex1));
					} catch (NumberFormatException e) {
						continue;
					}
					String move = moveRow.substring(pipeIndex1 + 1, pipeIndex2);
					addLevelUpEntry(builder, pokemon, move, level);
				}
			} else {
				String[] moveSplit = currentRaw.split("levelVI\\|");
				for (int j = 1; j < moveSplit.length; j++) {
					String moveRow = moveSplit[j];
					int pipeIndex1 = moveRow.indexOf('|');
					int pipeIndex2 = moveRow.indexOf('|', pipeIndex1 + 1);
					int pipeIndex3 = moveRow.indexOf('|', pipeIndex2 + 1);

					int level;
					try {
						level = Integer.parseInt(moveRow.substring(pipeIndex1 + 1, pipeIndex2));
					} catch (NumberFormatException e) {
						continue;
					}
					String move = moveRow.substring(pipeIndex2 + 1, pipeIndex3);
					addLevelUpEntry(builder, pokemon, move, level);
				}
			}
		}

		return builder.toString();
	}

	/**
	 * Adds a level up entry database query to a string builder.
	 * @param builder The string builder to add to.
	 * @param pokemon The Pokémon learning the move.
	 * @param move The name of the move.
	 * @param level The level that the move is learned at.
	 */
	private void addLevelUpEntry(StringBuilder builder, String pokemon, String move, int level) {
		builder.append("INSERT INTO PIXELMONLEVELSKILLS (PIXELMONID, LEARNLEVEL, MOVEID) VALUES (");
		Integer pokemonID = database.getPokemonIndex(pokemon);
		Integer moveID = database.getMoveIndex(move);
		if (level == 1) {
			level = 0;
		}
		StringUtil.addCommaSeparated(builder, true, pokemonID, level, moveID);
		builder.append(");\n");
	}

	/**
	 * Gets data for TM moves learned by new Pokémon.
	 * @return The database query for inserting new TM moves into the database.
	 */
	public String getTMData() {

		StringBuilder query = new StringBuilder();
		String[] gen6 = APIConnection.getGen6Pokemon();

		int numPokemon = gen6.length;
		for (int i = 0; i < numPokemon; i++) {
			String pokemon = gen6[i];
			String totalRaw = APIConnection.getArticleSource(pokemon + "/Generation_VI_learnset");
			pokemon = pokemon.substring(0, pokemon.length() - 10);
			System.out.println(pokemon);

			String[] moveSplit = StringUtil.getTMSplit6(totalRaw);
			if (moveSplit.length == 1) {
				continue;
			}
			for (int j = 1; j < moveSplit.length; j++) {
				String line = moveSplit[j];
				if (line.contains("TM94|Rock Smash")) {
					continue;
				}
				String move = StringUtil.getTMMoveFromLine(line);
				addTMEntry(query, pokemon, move);
			}
		}

		return query.toString();
	}

	/**
	 * Gets data for tutor moves learned by new Pokémon.
	 * @return The database query for inserting new tutor moves into the database.
	 */
	public String getTutorData() {
		Set<String> tmMoves = database.getTMMoves();

		StringBuilder query = new StringBuilder();
		String[] gen6 = APIConnection.getGen6Pokemon();

		int numPokemon = gen6.length;
		for (int i = 0; i < numPokemon; i++) {
			String pokemon = gen6[i];
			String totalRaw = APIConnection.getArticleSource(pokemon + "/Generation_VI_learnset");
			pokemon = pokemon.substring(0, pokemon.length() - 10);
			System.out.println(pokemon);

			String[] moveSplit = StringUtil.getTutorSplit6(totalRaw);
			if (moveSplit.length == 1) {
				continue;
			}
			for (int j = 1; j < moveSplit.length; j++) {
				String line = moveSplit[j];
				String move = StringUtil.getTutorMoveFromLine(line);
				if (tmMoves.contains(move)) {
					addTMEntry(query, pokemon, move);
				} else {
					addTutorEntry(query, pokemon, move);
				}
				// System.out.println(move);
			}
		}

		return query.toString();
	}

	/**
	 * Gets data for Egg moves learned by new Pokémon.
	 * @return The database query for inserting new Egg moves into the database.
	 */
	public String getEggMoveData() {

		StringBuilder query = new StringBuilder();
		String[] gen6 = APIConnection.getGen6Pokemon();

		int numPokemon = gen6.length;
		for (int i = 0; i < numPokemon; i++) {
			String pokemon = gen6[i];
			String totalRaw = APIConnection.getArticleSource(pokemon + "/Generation_VI_learnset");
			pokemon = pokemon.substring(0, pokemon.length() - 10);
			System.out.println(pokemon);

			String currentRaw = StringUtil.getSubstringBetween(totalRaw, "breedh/6", "breedf/6");
			String[] moveSplit = currentRaw.split("breed6\\|");
			if (moveSplit.length == 1) {
				continue;
			}
			for (int j = 1; j < moveSplit.length; j++) {
				String line = moveSplit[j];
				int pipeIndex1 = line.indexOf("}}|") + 3;
				int pipeIndex2 = line.indexOf('|', pipeIndex1);
				String move = line.substring(pipeIndex1, pipeIndex2);
				// System.out.println(move);
				addLearnMoveEntry(query, "PIXELMONEGGSKILLS", pokemon, move);
			}
		}

		return query.toString();
	}

	/**
	 * Gets data for Egg moves learned by old Pokémon.
	 * @return The database query for inserting new Egg moves into the database.
	 */
	public String getEggMoveDataOld() {

		StringBuilder query = new StringBuilder();
		List<String> pokemonList = APIConnection.getGenerationPokemon(new String[] { "I", "II", "III", "IV", "V" });

		String[] gen6Moves = APIConnection.getCategoryMembers("Generation_VI_moves");
		Set<String> moveSet = new HashSet<>();
		for (String move : gen6Moves) {
			moveSet.add(move.replace(" (move)", ""));
		}

		int numPokemon = pokemonList.size();
		for (int i = 0; i < numPokemon; i++) {
			String pokemon = pokemonList.get(i);
			String totalRaw = APIConnection.getArticleSource(pokemon + "/Generation_VI_learnset");
			pokemon = pokemon.substring(0, pokemon.length() - 10);

			String currentRaw = StringUtil.getSubstringBetween(totalRaw, "breedh/6", "breedf/6");
			String[] moveSplit = currentRaw.split("breed6\\|");
			if (moveSplit.length == 1) {
				continue;
			}
			for (int j = 1; j < moveSplit.length; j++) {
				String line = moveSplit[j];
				int pipeIndex1 = line.indexOf("}}|") + 3;
				int pipeIndex2 = line.indexOf('|', pipeIndex1);
				String move = line.substring(pipeIndex1, pipeIndex2);
				if (moveSet.contains(move)) {
					System.out.println(pokemon + " " + move);
					addLearnMoveEntry(query, "PIXELMONEGGSKILLS", pokemon, move);
				}
			}
		}

		return query.toString();
	}

	/**
	 * Gets moves that are new to Generation 6.
	 * @return Database query to add moves that are new to Generation 6.
	 */
	public String getNewMoves() {

		StringBuilder query = new StringBuilder();

		List<String> pokemonList = APIConnection.getGenerationPokemon("I", "II", "III", "IV", "V");

		int numPokemon = pokemonList.size();
		for (int i = 0; i < numPokemon; i++) {
			String pokemonArticle = pokemonList.get(i);
			String totalRaw = APIConnection.getArticleSource(pokemonArticle + APIConnection.GEN_5_LEARNSET);
			String pokemon = pokemonArticle.substring(0, pokemonArticle.length() - 10);
			// System.out.println(pokemon);
			pokemon = StringUtil.convertPokemonSpelling(pokemon);

			String[] moveSplit = StringUtil.getSplit6(totalRaw, "tmh", "tmf", "tm5\\|");
			Set<String> gen5Moves = new HashSet<>();
			StringUtil.addMultipleToSet(gen5Moves, "Confide", "Dazzling Gleam", "Focus Punch", "Infestation",
					"Nature Power", "Power-Up Punch", "Roost", "Secret Power", "Shock Wave", "Steel Wing",
					"Water Pulse");
			if (moveSplit.length < 10) {
				continue;
			}
			for (int j = 1; j < moveSplit.length; j++) {
				String line = moveSplit[j];
				String move = StringUtil.getTMMoveFromLine(line);
				gen5Moves.add(move);
			}
			moveSplit = StringUtil.getSplit6(totalRaw, "tutorh", "tutorf", "tutor5\\|");
			for (int j = 1; j < moveSplit.length; j++) {
				String line = moveSplit[j];
				String move = StringUtil.getTutorMoveFromLine(line);
				gen5Moves.add(move);
			}

			totalRaw = APIConnection.getArticleSource(pokemonArticle);
			moveSplit = StringUtil.getTMSplit6(totalRaw);
			for (int j = 1; j < moveSplit.length; j++) {
				String line = moveSplit[j];
				String move = StringUtil.getTMMoveFromLine(line);
				move = StringUtil.convertMoveSpelling(move);
				if (!gen5Moves.contains(move)) {
					System.out.println(pokemon + " " + move);
					addTMEntry(query, pokemon, move);
				}
			}
			moveSplit = StringUtil.getTutorSplit6(totalRaw);
			for (int j = 1; j < moveSplit.length; j++) {
				String line = moveSplit[j];
				String move = StringUtil.getTutorMoveFromLine(line);
				move = StringUtil.convertMoveSpelling(move);
				if (!gen5Moves.contains(move)) {
					System.out.println(pokemon + " " + move);
					try {
						addTutorEntry(query, pokemon, move);
					} catch (IllegalArgumentException e) {
						System.out.println(e.getMessage());
					}
				}
			}
		}

		return query.toString();
	}

	/**
	 * Gets event moves that were introduced in Generation 6.
	 * @return Database update query for event moves introduced in Generation 6.
	 */
	@SuppressWarnings("null")
	public String getEventData() {
		StringBuilder builder = new StringBuilder();

		List<String> pokemonList = APIConnection.getGenerationPokemon(StringUtil.ALL_GENERATIONS);
		String[] articles = { "", APIConnection.GEN_5_LEARNSET, "/Generation_IV_learnset", "/Generation_III_learnset" };

		String startPokemon = "Accelgor";
		if (startPokemon != null) {
			startPokemon += " (Pokémon)";
		}

		int numPokemon = pokemonList.size();
		int startIndex = startPokemon == null ? 0 : pokemonList.indexOf(startPokemon);
		for (int i = startIndex; i < numPokemon; i++) {
			String pokemonArticle = pokemonList.get(i);
			String pokemon = pokemonArticle.substring(0, pokemonArticle.length() - 10);
			// System.out.println(pokemon);
			pokemon = StringUtil.convertPokemonSpelling(pokemon);
			String currentArticle = "";

			try {
				Set<String> eventMoves = new HashSet<>();
				int numArticles = articles.length;
				if (i >= 649) {
					numArticles = 1;
				} else if (i >= 493) {
					numArticles = 2;
				} else if (i >= 386) {
					numArticles = 3;
				}
				for (int k = 0; k < numArticles; k++) {
					currentArticle = pokemonArticle + articles[k];
					String totalRaw = APIConnection.getArticleSource(currentArticle);

					String currentRaw = StringUtil.getSubstringBetween(totalRaw, "By {{pkmn2|event}}s", "eventf");
					if (!currentRaw.isEmpty()) {
						String[] moveSplit = currentRaw.split("event[3-6]\\|");
						for (int j = 1; j < moveSplit.length; j++) {
							String moveRow = moveSplit[j];
							String move = StringUtil.getTMMoveFromLine(moveRow);
							move = StringUtil.convertMoveSpelling(move);
							eventMoves.add(move);
						}
					}
				}
				if (!eventMoves.isEmpty()) {
					ResultSet resultSet = database.executeQuery("SELECT c.NAME FROM PIXELMONLEVELSKILLS a "
							+ "JOIN PIXELMON b ON (a.PIXELMONID = b.PIXELMONID) "
							+ "JOIN MOVES c ON (a.MOVEID = c.MOVEID) " + "WHERE b.PIXELMONFULLNAME = '" + pokemon + "' "
							+ "UNION " + "SELECT c.NAME FROM PIXELMONTMHMSKILLS a "
							+ "JOIN PIXELMON b ON (a.PIXELMONID = b.PIXELMONID) "
							+ "JOIN MOVES c ON (a.MOVEID = c.MOVEID) " + "WHERE b.PIXELMONFULLNAME = '" + pokemon + "' "
							+ "UNION " + "SELECT c.NAME FROM PIXELMONEGGSKILLS a "
							+ "JOIN PIXELMON b ON (a.PIXELMONID = b.PIXELMONID) "
							+ "JOIN MOVES c ON (a.MOVEID = c.MOVEID) " + "WHERE b.PIXELMONFULLNAME = '" + pokemon + "' "
							+ "UNION " + "SELECT c.NAME FROM PIXELMONTUTORSKILLS a "
							+ "JOIN PIXELMON b ON (a.PIXELMONID = b.PIXELMONID) "
							+ "JOIN MOVES c ON (a.MOVEID = c.MOVEID) " + "WHERE b.PIXELMONFULLNAME = '" + pokemon
							+ "' ");
					Set<String> existingMoves = new HashSet<>();
					while (resultSet.next()) {
						existingMoves.add(resultSet.getString("NAME"));
					}
					for (String move : eventMoves) {
						if (!existingMoves.contains(move)) {
							System.out.println(pokemon + " " + move);
							addTutorEntry(builder, pokemon, move);
						}
					}
				}
			} catch (Exception e) {
				System.out.println(currentArticle);
				e.printStackTrace();
			}
		}

		return builder.toString();
	}

	/**
	 * Converts event move output from getEventData() to database queries.
	 * @return Database update queries for new event moves.
	 */
	public String convertEventToDatabase() {
		StringBuilder builder = new StringBuilder();
		try {
			FileIO.getInstance().initializeReader(Database.LANG_FILE);
			BufferedReader buffer = FileIO.getReadBuffer();

			String line;
			ResultSet resultSet = database.executeQuery("SELECT NAME FROM MOVES WHERE TUTORTYPE = 2");
			Set<String> eventTutorMoves = new HashSet<>();
			while (resultSet.next()) {
				eventTutorMoves.add(resultSet.getString("NAME"));
			}

			List<String> pokemonList = APIConnection.getGenerationPokemon(StringUtil.ALL_GENERATIONS);
			String availableRaw = APIConnection
					.getURL("http://pixelmonmod.com/wiki/index.php?action=raw&title=Available_Pok%C3%A9mon");
			Set<String> availableList = new HashSet<>();
			String[] availableSplit = availableRaw.split("\\{\\{p\\|");
			for (int i = 1; i < availableSplit.length; i++) {
				String currentLine = availableSplit[i];
				availableList.add(currentLine.substring(0, currentLine.indexOf("}}")) + " (Pokémon)");
			}
			for (String pokemon : pokemonList) {
				if (!availableList.contains(pokemon)) {
					String totalRaw = APIConnection.getArticleSource(pokemon);
					pokemon = pokemon.substring(0, pokemon.length() - 10);
					// System.out.println(pokemon);
					pokemon = StringUtil.convertPokemonSpelling(pokemon);

					try {
						String[] moveLines = StringUtil.getPrevoSplit6(totalRaw);
						if (moveLines.length > 1) {
							for (int i = 1; i < moveLines.length; i++) {
								String moveLine = moveLines[i];
								String move = StringUtil.getPrevoMoveFromLine(moveLine);
								if (eventTutorMoves.contains(move)) {
									addTutorEntry(builder, pokemon, move);
									System.out.println(pokemon + " " + move);
								}
							}
						}
					} catch (Exception e) {
						System.out.println(pokemon);
						throw e;
					}
				}
			}

			resultSet = database.executeQuery("SELECT NAME FROM MOVES WHERE TUTORTYPE = 1");
			while (resultSet.next()) {
				eventTutorMoves.add(resultSet.getString("NAME"));
			}

			List<String> newTutorMoves = new ArrayList<>();
			while (null != (line = buffer.readLine())) {
				String data[] = line.split(" ", 2);
				if (eventTutorMoves.add(data[1])) {
					newTutorMoves.add(data[1]);
				}
				addTutorEntry(builder, data[0], data[1]);
			}

			builder.append("UPDATE MOVES SET TUTORTYPE = 2 WHERE NAME = '");
			boolean first = true;
			for (String tutorMove : newTutorMoves) {
				if (!first) {
					builder.append("' OR NAME = '");
				}
				builder.append(tutorMove);
				first = false;
				System.out.println(tutorMove);
			}
			builder.append("';\n");

			for (String pokemon : pokemonList) {
				String totalRaw = APIConnection.getArticleSource(pokemon);
				pokemon = pokemon.substring(0, pokemon.length() - 10);
				// System.out.println(pokemon);
				pokemon = StringUtil.convertPokemonSpelling(pokemon);

				try {
					String[] moveLines = StringUtil.getPrevoSplit6(totalRaw);
					if (moveLines.length > 1) {
						for (int i = 1; i < moveLines.length; i++) {
							String moveLine = moveLines[i];
							String move = StringUtil.getPrevoMoveFromLine(moveLine);
							if (newTutorMoves.contains(move)) {
								addTutorEntry(builder, pokemon, move);
								System.out.println(pokemon + " " + move);
							}
						}
					}
				} catch (Exception e) {
					System.out.println(pokemon);
					throw e;
				}
			}

			int numPokemon = pokemonList.size();
			for (int i = 0; i < numPokemon; i++) {
				String pokemonArticle = pokemonList.get(i);
				String pokemon = pokemonArticle.substring(0, pokemonArticle.length() - 10);
				// System.out.println(pokemon);
				pokemon = StringUtil.convertPokemonSpelling(pokemon);

			}
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}

		return builder.toString();
	}

	/**
	 * Adds a TM entry database query to a string builder.
	 * @param builder The string builder to add to.
	 * @param pokemon The Pokémon learning the move.
	 * @param move The name of the move.
	 */
	private void addTMEntry(StringBuilder builder, String pokemon, String move) {
		addLearnMoveEntry(builder, "PIXELMONTMHMSKILLS", pokemon, move);
	}

	/**
	 * Adds a tutor entry database query to a string builder.
	 * @param builder The string builder to add to.
	 * @param pokemon The Pokémon learning the move.
	 * @param move The name of the move.
	 */
	private void addTutorEntry(StringBuilder builder, String pokemon, String move) {
		addLearnMoveEntry(builder, "PIXELMONTUTORSKILLS", pokemon, move);
	}

	/**
	 * Adds a move learning database query to a string builder.
	 * @param builder The string builder to add to.
	 * @param pokemon The Pokémon learning the move.
	 * @param move The name of the move.
	 */
	private void addLearnMoveEntry(StringBuilder builder, String tableName, String pokemon, String move) {
		builder.append("INSERT INTO " + tableName + " (PIXELMONID, MOVEID) VALUES (");
		Integer pokemonID = database.getPokemonIndex(pokemon);
		Integer moveID = database.getMoveIndex(move);
		StringUtil.addCommaSeparated(builder, true, pokemonID, moveID);
		builder.append(");\n");
	}

	/**
	 * Gets Mega Evolution data from Bulbapedia.
	 * @return The database query for the Mega Evolution data.
	 */
	public String getMegaData() {
		Map<String, Integer> typeMap = database.getTypeMap();

		Map<String, Integer> eggGroupMap = new HashMap<>();
		database.loadMap(eggGroupMap, "EGGGROUPID", "NAME", "EGGGROUPS");

		Map<String, Integer> abilityMap = new HashMap<>();
		database.loadMap(abilityMap, "ABILITYID", "NAME", "ABILITIES");
		abilityMap.put("ShellArmor", abilityMap.get("ShellArmour"));

		StringBuilder baseQuery = new StringBuilder();
		baseQuery.append("INSERT INTO PIXELMON (");
		StringUtil.addCommaSeparated(baseQuery, false, "NATIONALPOKEDEXNUMBER", "PIXELMONFULLNAME", "PIXELMONNAME",
				"PIXELMONTYPE1ID", "PIXELMONTYPE2ID", "ABILITY1ID", "ABILITY2ID", "ABILITYHIDDENID", "EGGGROUP1ID",
				"EGGGROUP2ID", "EGGCYCLES", "POKEDEXHEIGHT", "POKEDEXWIDTH", "POKEDEXLENGTH", "POKEDEXWEIGHT",
				"POKEDEXDESCRIPTION", "MALEPERCENT", "CATCHRATE", "SPAWNTIMEID", "MINGROUPSIZE", "MAXGROUPSIZE",
				"BASEEXP", "EXPERIENCEGROUP", "BASEHP", "BASEATK", "BASEDEF", "BASESPATK", "BASESPDEF", "BASESPD",
				"EVGAINHP", "EVGAINATK", "EVGAINDEF", "EVGAINSPATK", "EVGAINSPDEF", "EVGAINSPD", "MINSPAWNLEVEL",
				"MAXSPAWNLEVEL", "MODELSCALE", "PERCENTTIMID", "PERCENTAGRESSIVE", "ISRIDEABLE", "CANFLY", "CANSWIM",
				"DOESHOVER", "BASEFRIENDSHIP", "FORM");
		baseQuery.append(") VALUES \n");

		String[] megaArray = APIConnection.getCategoryMembers("Pokémon_with_Mega_Evolutions");
		List<String> megaList = new ArrayList<>();
		Collections.addAll(megaList, megaArray);
		int origNumPokemon = megaArray.length;
		megaList.add("Charizard (Pokémon)");
		megaList.add("Mewtwo (Pokémon)");
		int numMega = megaList.size();
		megaList.add("Groudon (Pokémon)");
		megaList.add("Kyogre (Pokémon)");
		int numPokemon = megaList.size();

		for (int i = 0; i < numPokemon; i++) {
			boolean secondMega = i >= origNumPokemon && i < numMega;
			boolean primal = i >= numMega;
			String pokemonName = megaList.get(i);
			Pokemon pokemon = new Pokemon(pokemonName);
			pokemon.truncateName();
			System.out.println(pokemon.name);

			String totalRaw = APIConnection.getArticleSource(pokemonName);
			StringUtil.currentRaw = totalRaw.substring(0, totalRaw.indexOf("Biology"));

			pokemon.nationalPokedexNumber = StringUtil.getTableEntryInt("ndex");

			pokemon.type1 = StringUtil.getTableEntryFallback(secondMega ? "form3type1" : "form2type1", "type1");
			pokemon.type2 = StringUtil.getTableEntryFallback(secondMega ? "form3type2" : "form2type2", "type2");
			pokemon.ability1 = StringUtil.removeSpaces(
					StringUtil.getTableEntryFallback(secondMega ? "abilitym2" : "abilitym", "ability2-1", "ability1"));
			pokemon.ability2 = null;
			pokemon.abilityHidden = null;
			pokemon.eggGroup1 = StringUtil.removeSpaces(StringUtil.getTableEntry("egggroup1"));
			pokemon.eggGroup2 = StringUtil.removeSpaces(StringUtil.getTableEntry("egggroup2"));
			pokemon.eggCycles = StringUtil.getTableEntryInt("eggcycles");
			pokemon.height = StringUtil.getTableEntryFloat(secondMega ? "height-m3" : "height-m2");
			pokemon.weight = StringUtil.getTableEntryFloat(secondMega ? "weight-kg3" : "weight-kg2");

			int genderCode = StringUtil.getTableEntryInt("gendercode");
			switch (genderCode) {
			case 256:
			case 255:
				pokemon.genderRatio = -1;
				break;
			case 254:
				pokemon.genderRatio = 0;
				break;
			case 223:
				pokemon.genderRatio = 12;
				break;
			case 191:
				pokemon.genderRatio = 25;
				break;
			case 127:
				pokemon.genderRatio = 50;
				break;
			case 63:
				pokemon.genderRatio = 75;
				break;
			case 31:
				pokemon.genderRatio = 87;
				break;
			case 0:
				pokemon.genderRatio = 100;
				break;
			}

			pokemon.catchRate = StringUtil.getTableEntryInt("catchrate");
			try {
				pokemon.expYield = StringUtil.getTableEntryInt("expyield");
			} catch (NumberFormatException e) {
				String expYieldString = StringUtil.getTableEntry("expyield");
				expYieldString = StringUtil.getSubstringBetween(expYieldString, "--", "in");
				expYieldString = expYieldString.trim();
				pokemon.expYield = Integer.parseInt(expYieldString);
			}

			int maxExp = StringUtil.getTableEntryInt("lv100exp");
			switch (maxExp) {
			case 600000:
				pokemon.expGroup = "Erratic";
				break;
			case 800000:
				pokemon.expGroup = "Fast";
				break;
			case 1000000:
				pokemon.expGroup = "MediumFast";
				break;
			case 1059860:
				pokemon.expGroup = "MediumSlow";
				break;
			case 1250000:
				pokemon.expGroup = "Slow";
				break;
			case 1640000:
				pokemon.expGroup = "Fluctuating";
				break;
			}

			pokemon.evHP = StringUtil.getTableEntryInt("evhp");
			pokemon.evAtk = StringUtil.getTableEntryInt("evat");
			pokemon.evDef = StringUtil.getTableEntryInt("evde");
			pokemon.evSpAtk = StringUtil.getTableEntryInt("evsa");
			pokemon.evSpDef = StringUtil.getTableEntryInt("evsd");
			pokemon.evSpd = StringUtil.getTableEntryInt("evsp");

			StringUtil.currentRaw = StringUtil.getSubstringBetween(totalRaw, "==Base stats", "ness=");
			if (secondMega) {
				StringUtil.currentRaw = StringUtil.getSubstringBetween(StringUtil.currentRaw, "==Mega", "effective");
				StringUtil.currentRaw = StringUtil.getSubstringBetween(StringUtil.currentRaw, "===Mega", "=Type ");
			} else {
				StringUtil.currentRaw = StringUtil.getSubstringBetween(StringUtil.currentRaw,
						primal ? "==Primal" : "==Mega", "=Type effective");
			}
			pokemon.baseHP = StringUtil.getTableEntryInt("HP");
			pokemon.baseAtk = StringUtil.getTableEntryInt("Attack");
			pokemon.baseDef = StringUtil.getTableEntryInt("Defense");
			pokemon.baseSpAtk = StringUtil.getTableEntryInt("SpAtk");
			pokemon.baseSpDef = StringUtil.getTableEntryInt("SpDef");
			pokemon.baseSpd = StringUtil.getTableEntryInt("Speed");

			pokemon.baseFriendship = StringUtil.getTableEntryInt("Friendship");

			baseQuery.append("(");

			int form = secondMega ? 2 : 1;

			ResultSet result = database.executeQuery(
					"SELECT SPAWNTIMEID, PERCENTTIMID, PERCENTAGRESSIVE, ISRIDEABLE, CANFLY, CANSWIM, DOESHOVER FROM PIXELMON WHERE PIXELMONNAME = '"
							+ pokemon.name + "'");
			try {
				result.next();
				StringUtil.addCommaSeparated(baseQuery, true, pokemon.nationalPokedexNumber, pokemon.name, pokemon.name,
						typeMap.get(pokemon.type1), typeMap.get(pokemon.type2), abilityMap.get(pokemon.ability1),
						abilityMap.get(pokemon.ability2), abilityMap.get(pokemon.abilityHidden),
						eggGroupMap.get(pokemon.eggGroup1), eggGroupMap.get(pokemon.eggGroup2), pokemon.eggCycles,
						pokemon.height, pokemon.height, pokemon.height, pokemon.weight, "", pokemon.genderRatio,
						pokemon.catchRate, result.getInt("SPAWNTIMEID"), 1, 1, pokemon.expYield, pokemon.expGroup,
						pokemon.baseHP, pokemon.baseAtk, pokemon.baseDef, pokemon.baseSpAtk, pokemon.baseSpDef,
						pokemon.baseSpd, pokemon.evHP, pokemon.evAtk, pokemon.evDef, pokemon.evSpAtk, pokemon.evSpDef,
						pokemon.evSpd, 1, 100, 1.0, result.getInt("PERCENTTIMID"), result.getInt("PERCENTAGRESSIVE"),
						result.getBoolean("ISRIDEABLE"), result.getBoolean("CANFLY"), result.getBoolean("CANSWIM"),
						result.getBoolean("DOESHOVER"), pokemon.baseFriendship, form);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			baseQuery.append(")");
			baseQuery.append(i < numPokemon - 1 ? ",\n" : ";\n");
		}

		return baseQuery.toString();
	}
}
