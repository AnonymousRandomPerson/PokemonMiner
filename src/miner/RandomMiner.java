package miner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import miner.storage.Pokemon;
import util.APIConnection;
import util.StringUtil;

/**
 * Does assorted one-off tasks.
 */
public class RandomMiner extends Miner {

	/**
	 * Gets database queries to account for Castform and Deoxys forms.
	 * @return Database queries to account for Castform and Deoxys forms.
	 */
	public String getNewForms() {
		builder = new StringBuilder();

		int[] castformForms = new int[] { 722, 723, 724 };
		String query = "SELECT LEARNLEVEL, MOVEID FROM PIXELMONLEVELSKILLS WHERE PIXELMONID = 446";
		ResultSet result = database.executeQuery(query);
		try {
			while (result.next()) {
				int learnLevel = result.getInt("LEARNLEVEL");
				int moveID = result.getInt("MOVEID");
				for (int castformForm : castformForms) {
					builder.append("INSERT INTO PIXELMONLEVELSKILLS (PIXELMONID, LEARNLEVEL, MOVEID) values (");
					StringUtil.addCommaSeparated(builder, true, castformForm, learnLevel, moveID);
					builder.append(");\n");
				}
			}
			String[] moveTables = new String[] { "PIXELMONTMHMSKILLS", "PIXELMONTUTORSKILLS", "PIXELMONEGGSKILLS" };
			for (String moveTable : moveTables) {
				query = "SELECT MOVEID FROM " + moveTable + " WHERE PIXELMONID = 446";
				result = database.executeQuery(query);
				while (result.next()) {
					int moveID = result.getInt("MOVEID");
					for (int castformForm : castformForms) {
						builder.append("INSERT INTO ");
						builder.append(moveTable);
						builder.append(" (PIXELMONID, MOVEID) values (");
						StringUtil.addCommaSeparated(builder, true, castformForm, moveID);
						builder.append(");\n");
					}
				}
			}

			totalRaw = APIConnection.getArticleSource("Deoxys (Pokémon)/Generation_VI_learnset");
			int[] deoxysFormIDs = new int[] { 797, 798, 799 };
			String[] deoxysForms = new String[] { "Normal", "Attack", "Defense", "Speed" };
			query = "SELECT * FROM PIXELMON WHERE PIXELMONNAME = 'Deoxys'";
			result = database.executeQuery(query);
			result.next();
			Pokemon pokemon = new Pokemon("Deoxys");
			pokemon.nationalPokedexNumber = result.getInt("NATIONALPOKEDEXNUMBER");
			pokemon.type1 = result.getString("PIXELMONTYPE1ID");
			pokemon.ability1 = result.getString("ABILITY1ID");
			pokemon.eggGroup1ID = result.getInt("EGGGROUP1ID");
			pokemon.eggCycles = result.getInt("EGGCYCLES");
			pokemon.height = result.getFloat("POKEDEXHEIGHT");
			pokemon.weight = result.getFloat("POKEDEXWEIGHT");
			pokemon.genderRatio = result.getInt("MALEPERCENT");
			pokemon.catchRate = result.getInt("CATCHRATE");
			pokemon.expYield = result.getInt("BASEEXP");
			pokemon.expGroup = result.getString("EXPERIENCEGROUP");
			pokemon.baseHP = result.getInt("BASEHP");
			pokemon.baseFriendship = result.getInt("BASEFRIENDSHIP");
			builder.append("UPDATE PIXELMON SET FORM = 0 WHERE PIXELMONID = 476;\n");
			for (int i = 0; i < deoxysFormIDs.length; i++) {
				switch (i) {
				case 0:
					pokemon.baseAtk = pokemon.baseSpAtk = 180;
					pokemon.baseDef = pokemon.baseSpDef = 20;
					pokemon.evAtk = 2;
					pokemon.evSpAtk = 1;
					break;
				case 1:
					pokemon.baseAtk = pokemon.baseSpAtk = 70;
					pokemon.baseDef = pokemon.baseSpDef = 160;
					pokemon.baseSpd = 90;
					pokemon.evDef = 2;
					pokemon.evSpDef = 1;
					pokemon.evAtk = pokemon.evSpAtk = 0;
					break;
				case 2:
					pokemon.baseAtk = pokemon.baseSpAtk = 95;
					pokemon.baseDef = pokemon.baseSpDef = 90;
					pokemon.baseSpd = 180;
					pokemon.evSpd = 3;
					pokemon.evDef = pokemon.evSpDef = 0;
					break;
				}
				builder.append("INSERT INTO PIXELMON (");
				StringUtil.addCommaSeparated(builder, false, "NATIONALPOKEDEXNUMBER", "PIXELMONFULLNAME",
						"PIXELMONNAME", "PIXELMONTYPE1ID", "PIXELMONTYPE2ID", "ABILITY1ID", "ABILITY2ID",
						"ABILITYHIDDENID", "EGGGROUP1ID", "EGGGROUP2ID", "EGGCYCLES", "POKEDEXHEIGHT", "POKEDEXWIDTH",
						"POKEDEXLENGTH", "POKEDEXWEIGHT", "POKEDEXDESCRIPTION", "MALEPERCENT", "CATCHRATE",
						"SPAWNTIMEID", "MINGROUPSIZE", "MAXGROUPSIZE", "BASEEXP", "EXPERIENCEGROUP", "BASEHP",
						"BASEATK", "BASEDEF", "BASESPATK", "BASESPDEF", "BASESPD", "EVGAINHP", "EVGAINATK", "EVGAINDEF",
						"EVGAINSPATK", "EVGAINSPDEF", "EVGAINSPD", "MINSPAWNLEVEL", "MAXSPAWNLEVEL", "MODELSCALE",
						"PERCENTTIMID", "PERCENTAGRESSIVE", "ISRIDEABLE", "CANFLY", "CANSWIM", "DOESHOVER",
						"BASEFRIENDSHIP", "FORM");
				builder.append(") VALUES (");
				StringUtil.addCommaSeparated(builder, true, pokemon.nationalPokedexNumber, pokemon.name, pokemon.name,
						pokemon.type1, pokemon.type2, pokemon.ability1, pokemon.ability2, pokemon.abilityHidden,
						pokemon.eggGroup1ID, pokemon.eggGroup2, pokemon.eggCycles, pokemon.height, pokemon.height,
						pokemon.height, pokemon.weight, "", pokemon.genderRatio, pokemon.catchRate, 2, 1, 1,
						pokemon.expYield, pokemon.expGroup, pokemon.baseHP, pokemon.baseAtk, pokemon.baseDef,
						pokemon.baseSpAtk, pokemon.baseSpAtk, pokemon.baseSpd, pokemon.evHP, pokemon.evAtk,
						pokemon.evDef, pokemon.evSpAtk, pokemon.evSpDef, pokemon.evSpd, 70, 70, 1.0, 80, 20, false,
						false, false, false, pokemon.baseFriendship, i + 1);
				builder.append(");\n");
			}

			for (int i = 0; i < deoxysFormIDs.length; i++) {
				String currentRaw = StringUtil.getSubstringBetween(totalRaw, deoxysForms[i + 1], "levelf");
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
					addLevelUpEntry(builder, deoxysFormIDs[i], move, level);
				}
			}

			query = "SELECT MOVEID FROM PIXELMONTMHMSKILLS WHERE PIXELMONID = 476";
			result = database.executeQuery(query);
			while (result.next()) {
				int moveID = result.getInt("MOVEID");
				for (Integer deoxysForm : deoxysFormIDs) {
					builder.append("INSERT INTO PIXELMONTMHMSKILLS (PIXELMONID, MOVEID) VALUES (");
					StringUtil.addCommaSeparated(builder, true, deoxysForm, moveID);
					builder.append(");\n");
				}
			}

			builder.append("DELETE FROM PIXELMONTUTORSKILLS WHERE PIXELMONID = 476;\n");

			Set<String> tmMoves = database.getTMMoves();
			Set<String> tutorMoves = new HashSet<>();
			String tutorRaw = totalRaw.substring(totalRaw.indexOf("Move Tutor"));
			for (int i = 0; i < deoxysForms.length; i++) {
				String currentRaw = tutorRaw.substring(tutorRaw.indexOf(deoxysForms[i]));
				String[] moveSplit = StringUtil.getTutorSplit6(currentRaw);
				if (moveSplit.length == 1) {
					continue;
				}
				for (int j = 1; j < moveSplit.length; j++) {
					String line = moveSplit[j];
					String move = StringUtil.getTutorMoveFromLine(line);
					if (tmMoves.contains(move)) {
						continue;
					}
					tutorMoves.add(move);
				}
			}
			for (String tutorMove : tutorMoves) {
				addTutorEntry(builder, 476, tutorMove);
				for (Integer deoxysForm : deoxysFormIDs) {
					addTutorEntry(builder, deoxysForm, tutorMove);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		builder.append("update pixelmon set maxspawnlevel = 45 where pixelmonname = 'Torkoal';\n");
		builder.append("delete from pixelmontrades where tradeid = 75 or tradeid = 74;\n");

		return builder.toString();
	}
	
	/**
	 * Fixes database errors for base Special Defense and Special Attack EVs.
	 * @return
	 */
	public String fixSpecial() {
		builder = new StringBuilder();

		String[] gen6Array = APIConnection.getGen6Pokemon();
		int numPokemon = gen6Array.length;

		for (int i = 0; i < numPokemon; i++) {
			String pokemonName = gen6Array[i];
			Pokemon pokemon = new Pokemon(pokemonName);
			pokemon.truncateName();
			System.out.println(pokemon.name);
			
			String totalRaw = APIConnection.getArticleSource(pokemonName);
			StringUtil.currentRaw = totalRaw.substring(0, totalRaw.indexOf("Biology"));
			pokemon.evSpAtk = StringUtil.getTableEntryInt("evsa");
			StringUtil.currentRaw = StringUtil.getSubstringBetween(totalRaw, "=Base stats=", "=Type effectiveness=");
			pokemon.baseSpDef = StringUtil.getTableEntryInt("SpDef");
			
			builder.append("UPDATE PIXELMON SET EVGAINSPATK=");
			builder.append(pokemon.evSpAtk);
			builder.append(",BASESPDEF=");
			builder.append(pokemon.baseSpDef);
			builder.append(" WHERE PIXELMONNAME='");
			builder.append(pokemon.name);
			builder.append("';\n");
		}
		
		return builder.toString();
	}
	
	public String fixEggMoveSpaces() {
		builder = new StringBuilder();

		String[] gen6Array = APIConnection.getGen6Pokemon();
		int numPokemon = gen6Array.length;

		Map<String, Integer> eggGroupMap = new HashMap<>();
		database.loadMap(eggGroupMap, "EGGGROUPID", "NAME", "EGGGROUPS");

		for (int i = 0; i < numPokemon; i++) {
			String pokemonName = gen6Array[i];
			Pokemon pokemon = new Pokemon(pokemonName);
			pokemon.truncateName();
			System.out.println(pokemon.name);
			
			String totalRaw = APIConnection.getArticleSource(pokemonName);
			StringUtil.currentRaw = totalRaw.substring(0, totalRaw.indexOf("Biology"));
			pokemon.eggGroup1 = StringUtil.getTableEntry("egggroup1");
			pokemon.eggGroup2 = StringUtil.getTableEntry("egggroup2");
			if (pokemon.eggGroup1.contains(" ")) {
				pokemon.eggGroup1 = StringUtil.removeSpaces(pokemon.eggGroup1);
				Integer eggGroupID = eggGroupMap.get(pokemon.eggGroup1);
				if (eggGroupID == null) {
					System.out.println("No Egg Group found for " + pokemon.eggGroup1);
				} else {
					builder.append("UPDATE PIXELMON SET EGGGROUP1ID=");
					builder.append(eggGroupID);
					builder.append(" WHERE PIXELMONNAME='");
					builder.append(pokemon.name);
					builder.append("';\n");
				}
			}
			if (pokemon.eggGroup2 != null && pokemon.eggGroup2.contains(" ")) {
				pokemon.eggGroup2 = StringUtil.removeSpaces(pokemon.eggGroup2);
				Integer eggGroupID = eggGroupMap.get(pokemon.eggGroup2);
				if (eggGroupID == null) {
					System.out.println("No Egg Group found for " + pokemon.eggGroup2);
				} else {
					builder.append("UPDATE PIXELMON SET EGGGROUP2ID=");
					builder.append(eggGroupID);
					builder.append(" WHERE PIXELMONNAME='");
					builder.append(pokemon.name);
					builder.append("';\n");
				}
			}
		}
		
		return builder.toString();
	}

	/**
	 * Adds a level up entry database query to a string builder.
	 * @param builder The string builder to add to.
	 * @param pokemonID The database ID of the Pokémon learning the move.
	 * @param move The name of the move.
	 * @param level The level that the move is learned at.
	 */
	private void addLevelUpEntry(StringBuilder builder, int pokemonID, String move, int level) {
		builder.append("INSERT INTO PIXELMONLEVELSKILLS (PIXELMONID, LEARNLEVEL, MOVEID) VALUES (");
		Integer moveID = database.getMoveIndex(move);
		if (level == 1) {
			level = 0;
		}
		StringUtil.addCommaSeparated(builder, true, pokemonID, level, moveID);
		builder.append(");\n");
	}

	/**
	 * Adds a tutor entry database query to a string builder.
	 * @param builder The string builder to add to.
	 * @param pokemon The ID of the Pokémon learning the move.
	 * @param move The name of the move.
	 */
	private void addTutorEntry(StringBuilder builder, int pokemonID, String move) {
		addLearnMoveEntry(builder, "PIXELMONTUTORSKILLS", pokemonID, move);
	}

	/**
	 * Adds a move learning database query to a string builder.
	 * @param builder The string builder to add to.
	 * @param pokemon The ID of the Pokémon learning the move.
	 * @param move The name of the move.
	 */
	private void addLearnMoveEntry(StringBuilder builder, String tableName, int pokemonID, String move) {
		builder.append("INSERT INTO " + tableName + " (PIXELMONID, MOVEID) VALUES (");
		Integer moveID = database.getMoveIndex(move);
		StringUtil.addCommaSeparated(builder, true, pokemonID, moveID);
		builder.append(");\n");
	}
}
