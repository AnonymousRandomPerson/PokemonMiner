package miner.wiki;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import database.Database;
import miner.Miner;
import miner.storage.DexNumberComparator;
import miner.storage.DexNumberContainer;
import miner.storage.LevelUpEntry;
import miner.storage.Move;
import miner.storage.Pokemon;
import miner.storage.PreEvoEntry;
import pixelmon.Effectiveness;
import pixelmon.EnumPokemon;
import pixelmon.EnumType;
import util.APIConnection;
import util.StringUtil;

/**
 * Creates articles about Pokémon.
 */
public class PokemonArticleMiner extends Miner {

	/**
	 * Creates an article about a Pokémon.
	 * @param pokemon The Pokémon to create an article for.
	 * @return The wikicode for the Pokémon article.
	 */
	public String getPokemonArticle(String pokemon) {
		if (pokemon.isEmpty()) {
			pokemon = "Greninja";
		}

		builder = new StringBuilder();

		String englishName = Pokemon.getTranslatedName(pokemon);
		if (englishName == null) {
			System.out.println("Pokémon not found: " + pokemon);
			return "";
		}
		totalRaw = APIConnection.getArticleSourcePixelmon(englishName);
		boolean missingHeader = false;
		if (!totalRaw.isEmpty()) {
			char firstChar = totalRaw.charAt(0);
			if (firstChar == '=') {
				builder.append("=<div style=\"border-bottom:1px solid #000;\">{{PAGENAME}}</div>=\n");
				missingHeader = true;
			} else if (firstChar == '\n') {
				builder.append("\n\n");
			}
		}

		builder.append("{{PrevNext");

		DexNumberContainer dexContainer = database.getDexContainer();
		Pokemon currentPokemon = dexContainer.getPokemonFromName(pokemon);
		Pokemon nextPokemon = dexContainer.getNextPokemon(pokemon);
		Pokemon prevPokemon = dexContainer.getPreviousPokemon(pokemon);
		appendTableField("prev", prevPokemon.getTranslatedName());

		appendTableField("next", nextPokemon.getTranslatedName());

		StringBuilder query = new StringBuilder("SELECT ");
		StringUtil.addCommaSeparated(query, false, "a.PIXELMONID, b.NAME AS PIXELMONTYPE1NAME",
				"c.NAME AS PIXELMONTYPE2NAME", "d.NAME AS ABILITY1NAME", "e.NAME AS ABILITY2NAME",
				"f.NAME AS ABILITYHIDDENNAME", "a.EGGGROUP1ID", "a.EGGGROUP2ID", "g.NAME AS EGGGROUP1NAME",
				"h.NAME AS EGGGROUP2NAME", "a.MALEPERCENT", "a.CATCHRATE", "a.RARITY", "i.SPAWNTIMENAME", "a.BASEHP",
				"a.BASEATK", "a.BASEDEF", "a.BASESPATK", "a.BASESPDEF", "a.BASESPD", "a.EVGAINHP", "a.EVGAINATK",
				"a.EVGAINDEF", "a.EVGAINSPATK", "a.EVGAINSPDEF", "a.EVGAINSPD", "a.MINSPAWNLEVEL", "a.MAXSPAWNLEVEL",
				"a.PERCENTTIMID", "a.PERCENTAGRESSIVE", "a.ISRIDEABLE", "a.CANFLY", "a.CANSWIM ");
		query.append("FROM PIXELMON a ");
		query.append("LEFT JOIN TYPES b ON a.PIXELMONTYPE1ID = b.TYPEID ");
		query.append("LEFT JOIN TYPES c ON a.PIXELMONTYPE2ID = c.TYPEID ");
		query.append("LEFT JOIN ABILITIES d ON a.ABILITY1ID = d.ABILITYID ");
		query.append("LEFT JOIN ABILITIES e ON a.ABILITY2ID = e.ABILITYID ");
		query.append("LEFT JOIN ABILITIES f ON a.ABILITYHIDDENID = f.ABILITYID ");
		query.append("LEFT JOIN EGGGROUPS g ON a.EGGGROUP1ID = g.EGGGROUPID ");
		query.append("LEFT JOIN EGGGROUPS h ON a.EGGGROUP2ID = h.EGGGROUPID ");
		query.append("LEFT JOIN PIXELMONSPAWNTIMES i ON a.SPAWNTIMEID = i.SPAWNTIMEID ");
		query.append("WHERE a.PIXELMONNAME = '");
		query.append(pokemon);
		query.append("' ");

		ResultSet result = database.executeQuery(query);

		if (prevPokemon.nationalPokedexNumber != currentPokemon.nationalPokedexNumber - 1
				|| prevPokemon.nationalPokedexNumber < 100) {
			appendTableField("prevnum", formatDexNumber(prevPokemon.nationalPokedexNumber));
		}

		String dexNumberString = formatDexNumber(currentPokemon.nationalPokedexNumber);
		appendTableField("curnum", dexNumberString);

		if (nextPokemon.nationalPokedexNumber != currentPokemon.nationalPokedexNumber + 1
				|| nextPokemon.nationalPokedexNumber < 100) {
			appendTableField("nextnum", formatDexNumber(nextPokemon.nationalPokedexNumber));
		}

		builder.append("}}\n{{PokémonInfobox");

		appendTableField("name", englishName);

		try {
			if (!result.next()) {
				return "";
			}

			StringUtil.currentRaw = null;
			if (!totalRaw.isEmpty()) {
				int statIndex = totalRaw.indexOf("==Stats==");
				if (statIndex == -1) {
					statIndex = totalRaw.indexOf("==Base stats==");
				}
				StringUtil.currentRaw = totalRaw.substring(0, statIndex);

				String altName = StringUtil.getTableEntry("altname");
				if (altName != null) {
					appendTableField("altname", altName.trim());
				}

				String imagePath = StringUtil.getTableEntry("image");
				if (imagePath != null) {
					appendTableField("image", imagePath.trim());
				}

				String sketchfabHash = StringUtil.getTableEntry("sketchfab");
				if (sketchfabHash != null) {
					appendTableField("sketchfab", sketchfabHash.trim());
				}
			}

			StringBuilder pastModels = new StringBuilder();
			int startIndex = totalRaw.indexOf("==Other forms==");
			if (startIndex >= 0) {
				int currentIndex = totalRaw.indexOf("<gallery>") + 10;
				int imageEnd = totalRaw.indexOf("|", currentIndex);
				String imageName = totalRaw.substring(currentIndex, imageEnd);
				String defaultShiny = englishName + "S.png";
				if (!imageName.equals(defaultShiny)) {
					appendTableField("shinyimage", imageName);
				}
				currentIndex = totalRaw.indexOf("=", currentIndex);
				if (currentIndex > -1) {
					pastModels.append("\n==Past models==\n");
					pastModels.append(totalRaw.substring(currentIndex));
				}
			}
			if (pastModels.length() == 0) {
				pastModels.append("\n[[Category:Pokémon]]");
			} else {
				int categoryDuplicate = pastModels.indexOf("\n[[Category:Pokémon]]",
						pastModels.indexOf("[[Category:Pokémon]]"));
				if (categoryDuplicate > -1) {
					pastModels.delete(categoryDuplicate, pastModels.length());
				}
			}

			int databaseID = result.getInt("PIXELMONID");

			String type1 = result.getString("PIXELMONTYPE1NAME");
			appendTableField("type1", type1);

			String type2 = result.getString("PIXELMONTYPE2NAME");
			appendTableField("type2", type2);
			if (StringUtil.equalsAny("Fairy", type1, type2)) {
				System.out.println("Fairy retype detected.");
			}

			appendTableField("ndex", dexNumberString);

			appendTableField("catchrate", result.getInt("CATCHRATE"));

			StringBuilder abilityText = new StringBuilder();
			String ability1 = result.getString("ABILITY1NAME");
			ability1 = StringUtil.translateAbility(ability1);
			appendLink(abilityText, ability1);
			String ability2 = result.getString("ABILITY2NAME");
			if (ability2 != null && !ability1.equals(ability2)) {
				abilityText.append("/");
				ability2 = StringUtil.translateAbility(ability2);
				appendLink(abilityText, ability2);
			} else {
				ability2 = null;
			}
			appendTableField("abilities", abilityText.toString());

			StringBuilder hiddenText = new StringBuilder();
			String hiddenAbility = result.getString("ABILITYHIDDENNAME");
			hiddenAbility = StringUtil.translateAbility(hiddenAbility);
			if (hiddenAbility != null && !hiddenAbility.equals(ability1)) {
				appendLink(hiddenText, hiddenAbility);
			} else {
				hiddenAbility = null;
				hiddenText.append("None");
			}
			appendTableField("hidden", hiddenText.toString());
			String[] abilityNames = new String[] { ability1, ability2, hiddenAbility };
			String[] typeAbilities = new String[] { "Delta Stream", "Desolate Land", "Drizzle", "Drought", "Dry Skin",
					"Filter", "Flash Fire", "Heatproof", "Levitate", "Lightning Rod", "Motor Drive", "Primordial Sea",
					"Sap Sipper", "Solid Rock", "Storm Drain", "Thick Fat", "Volt Absorb", "Water Absorb",
					"Wonder Guard" };
			for (String abilityName : abilityNames) {
				if (abilityName != null && StringUtil.equalsAny(abilityName, typeAbilities)) {
					System.out.println("Ability modifies type effectiveness: " + abilityName);
				}
			}

			int minSpawnLevel = result.getInt("MINSPAWNLEVEL");
			int maxSpawnLevel = result.getInt("MAXSPAWNLEVEL") - 1;
			String levelString = Integer.toString(minSpawnLevel);
			if (maxSpawnLevel > minSpawnLevel) {
				levelString += "-" + maxSpawnLevel;
			}
			appendTableField("levels", levelString);

			String[] evColumns = new String[] { "HP", "ATK", "DEF", "SPATK", "SPDEF", "SPD" };
			String[] evNames = new String[] { "HP", "Attack", "Defense", "Special Attack", "Special Defense", "Speed" };
			boolean hasEV = false;
			StringBuilder evString = new StringBuilder();
			for (int i = 0; i < evColumns.length; i++) {
				int evValue = result.getInt("EVGAIN" + evColumns[i]);
				if (evValue > 0) {
					if (hasEV) {
						evString.append(", ");
					}
					evString.append(evValue);
					evString.append(' ');
					evString.append(evNames[i]);
					hasEV = true;
				}
			}
			appendTableField("evyield", evString);

			StringBuilder eggString = new StringBuilder();
			int egg1ID = result.getInt("EGGGROUP1ID");
			int egg2ID = -1;
			String egg2IDString = result.getString("EGGGROUP2ID");
			if (egg2IDString != null) {
				egg2ID = Integer.parseInt(egg2IDString);
			}
			String egg1 = result.getString("EGGGROUP1NAME");
			String egg2 = result.getString("EGGGROUP2NAME");
			appendLink(eggString, convertEggName(egg1));
			if (egg2 != null && !egg1.equals(egg2)) {
				eggString.append('/');
				appendLink(eggString, convertEggName(egg2));
			}
			appendTableField("egg", eggString);

			appendTableField("catchrate", result.getInt("CATCHRATE"));

			int genderRatio = result.getInt("MALEPERCENT");
			if (genderRatio != 50) {
				appendTableField("gender", genderRatio);
			}
			if (genderRatio == 0) {
				System.out.println("100% female; check Egg parents.");
			}

			if (result.getBoolean("ISRIDEABLE")) {
				String rideString = "Land";
				if (result.getBoolean("CANFLY")) {
					rideString = "Flying";
				} else if (result.getBoolean("CANSWIM")) {
					rideString = "Water";
				}
				appendTableField("rideable", rideString);
			}

			int timidChance = result.getInt("PERCENTTIMID");
			int aggressiveChance = result.getInt("PERCENTAGRESSIVE");
			if (timidChance != 80 || aggressiveChance != 20) {
				appendTableField("behavior1", "Neutral " + (100 - timidChance - aggressiveChance) + "%");
				appendTableField("behavior2", "Timid " + timidChance + "%");
				appendTableField("behavior3", "Aggressive " + aggressiveChance + "%");
			}

			int hp = result.getInt("BASEHP");
			int attack = result.getInt("BASEATK");
			int defense = result.getInt("BASEDEF");
			int specialAttack = result.getInt("BASESPATK");
			int specialDefense = result.getInt("BASESPDEF");
			int speed = result.getInt("BASESPD");

			int rarity = result.getInt("RARITY");
			List<String> biomes = new ArrayList<>();
			float[] timeRarities = new float[3];
			if (rarity != 0) {
				String spawnTime = "";
				String spawnTimeName = result.getString("SPAWNTIMENAME");
				switch (spawnTimeName) {
				case "DayOnly":
					spawnTime = "Day";
					break;
				case "NightOnly":
					spawnTime = "Night";
					break;
				case "DuskOnly":
					spawnTime = "Dawn/Dusk";
					break;
				case "DayDusk":
					spawnTime = "Dawn/Day/Dusk";
					break;
				case "NightDusk":
					spawnTime = "Dawn/Dusk/Night";
					break;
				case "NotDusk":
					spawnTime = "Day/Night";
					break;
				case "MostlyDay":
					spawnTime = rarity == -1 ? "Day" : "Dawn/'''Day'''/Dusk";
					break;
				case "MostlyNight":
					spawnTime = rarity == -1 ? "Night" : "Dawn/Dusk/'''Night'''";
					break;
				case "HigherDay":
					spawnTime = rarity == -1 ? "Day" : "Dawn/'''Day'''/Dusk/Night";
					break;
				case "HigherDusk":
					spawnTime = rarity == -1 ? "Dawn/Dusk" : "'''Dawn'''/Day/'''Dusk'''/Night";
					break;
				case "HigherNight":
					spawnTime = rarity == -1 ? "Night" : "Dawn/Day/Dusk/'''Night'''";
					break;
				case "AllTimes":
					spawnTime = "Dawn/Day/Dusk/Night";
					break;
				}

				result = database.executeQuery(
						"SELECT MULTIPLIERDUSKDAWN, MULTIPLIERDAY, MULTIPLIERNIGHT FROM PIXELMONSPAWNTIMES WHERE SPAWNTIMENAME = '"
								+ spawnTimeName + "'");
				result.next();
				for (int i = 0; i < timeRarities.length; i++) {
					timeRarities[i] = result.getFloat(i + 1);
				}

				result = database
						.executeQuery("SELECT BIOMENAME FROM PIXELMONSPAWNBIOMES WHERE PIXELMONID = " + databaseID);
				while (result.next()) {
					String biome = result.getString(1);
					switch (biome) {
					case "BirchForest Hills M":
						biome = "Birch Forest Hills M";
						break;
					case "DesertHills":
						biome = "Desert Hills";
						break;
					case "ForestHills":
						biome = "Forest Hills";
						break;
					case "FrozenOcean":
						biome = "Frozen Ocean";
						break;
					case "FrozenRiver":
						biome = "Frozen River";
						break;
					case "JungleEdge":
						biome = "Jungle Edge";
						break;
					case "JungleEdge M":
						biome = "Jungle Edge M";
						break;
					case "JungleHills":
						biome = "Jungle Hills";
						break;
					case "MushroomIsland":
						biome = "Mushroom Island";
						break;
					case "MushroomIslandShore":
						biome = "Mushroom Island Shore";
						break;
					case "TaigaHills":
						biome = "Taiga Hills";
						break;
					}
					biomes.add(biome);
				}
				Collections.sort(biomes);
				StringBuilder biomeString = new StringBuilder();
				for (int i = 0; i < biomes.size(); i++) {
					String biome = biomes.get(i);
					if (i > 0) {
						biomeString.append(", ");
					}
					appendLink(biomeString, biome);
				}
				if (!biomes.isEmpty()) {
					appendTableField("time", spawnTime);
					appendTableField("biomes", biomeString.toString());
				} else {
					rarity = 0;
				}
			}

			List<Integer> preEvoIDs = getPreEvolutions(databaseID);
			List<Integer> evoIDs = getEvolutions(databaseID);
			List<String> evoTypes = new ArrayList<>();
			for (Integer evoID : evoIDs) {
				query = new StringBuilder();
				query.append("SELECT b.NAME AS TYPE1NAME, c.NAME AS TYPE2NAME FROM PIXELMON a ");
				query.append("LEFT JOIN TYPES b ON a.PIXELMONTYPE1ID = b.TYPEID ");
				query.append("LEFT JOIN TYPES c ON a.PIXELMONTYPE2ID = c.TYPEID ");
				query.append("WHERE a.PIXELMONID = " + evoID);
				result = database.executeQuery(query);
				while (result.next()) {
					String evoType1 = result.getString("TYPE1NAME");
					if (!preEvoIDs.contains(evoType1)) {
						evoTypes.add(evoType1);
					}
					String evoType2 = result.getString("TYPE2NAME");
					if (evoType2 != null && !preEvoIDs.contains(evoType2)) {
						evoTypes.add(evoType2);
					}
				}
			}

			result = database
					.executeQuery("SELECT LOCATION FROM PIXELMONSPAWNLOCATIONS WHERE PIXELMONID = " + databaseID);
			List<String> locations = new ArrayList<>();
			while (result.next()) {
				String location = result.getString(1);
				if (location.equals("airpersistent")) {
					location = "Air persistent";
				} else {
					location = (char) (location.charAt(0) - 32) + location.substring(1);
				}
				locations.add(location);
			}
			Collections.sort(locations);
			int numLocations = locations.size();
			if (numLocations > 1 || numLocations > 0 && !locations.get(0).equals("Land")) {
				StringBuilder locationString = new StringBuilder();
				for (int i = 0; i < numLocations; i++) {
					if (i != 0) {
						locationString.append('/');
					}
					locationString.append(locations.get(i));
				}
				appendTableField("location", locationString.toString());
			}

			builder.append("\n}}\n");

			if (StringUtil.currentRaw != null) {
				String descriptionArea = StringUtil.currentRaw.substring(StringUtil.currentRaw.indexOf("catchrate"));
				startIndex = descriptionArea.indexOf('}') + 3;
				startIndex = descriptionArea.indexOf(englishName, startIndex);
				descriptionArea = descriptionArea.substring(startIndex);
				String description = StringUtil.getSubstringBetween(descriptionArea, null, "\n");
				description = description.replaceFirst("-type", "-[[type]]");
				builder.append(description);
				builder.append("\n\n");
			} else {
				builder.append(englishName);
				builder.append(" is a ");
				appendLink(builder, type1);
				if (type2 != null) {
					builder.append('/');
					appendLink(builder, type2);
				}
				builder.append("-[[type]] Pokémon.");

				boolean evolveLink = false;
				query = new StringBuilder();
				query.append("SELECT b.PIXELMONNAME, a.EVOLVELEVEL, a.EVOLVECONDITION FROM PIXELMONEVOLUTIONS a ");
				query.append("LEFT JOIN PIXELMON b ON a.PIXELMONFROMID = b.PIXELMONID ");
				query.append("WHERE a.PIXELMONTOID = ");
				query.append(databaseID);
				query.append(" OR EVOLVECONDITION LIKE '%");
				query.append(pokemon);
				query.append("%'");
				result = database.executeQuery(query);
				if (result.next()) {
					String preEvoName = Pokemon.getTranslatedName(result.getString("PIXELMONNAME"));
					int level = result.getInt("EVOLVELEVEL");
					if (level == 0) {
						if (preEvoName == null) {
							preEvoName = "";
						}
						System.out.println(
								"Pre-evolution needed for " + preEvoName + ": " + result.getString("EVOLVECONDITION"));
					} else {
						builder.append(" It [[evolves]] from ");
						appendLink(builder, preEvoName);
						builder.append(" at level ");
						builder.append(level);
						evolveLink = true;
					}
				}

				query = new StringBuilder();
				query.append("SELECT b.PIXELMONNAME, a.EVOLVELEVEL, a.EVOLVECONDITION FROM PIXELMONEVOLUTIONS a ");
				query.append("LEFT JOIN PIXELMON b ON a.PIXELMONTOID = b.PIXELMONID ");
				query.append("WHERE a.PIXELMONFROMID = ");
				query.append(databaseID);
				result = database.executeQuery(query);
				if (result.next()) {
					String evoName = Pokemon.getTranslatedName(result.getString("PIXELMONNAME"));
					int level = result.getInt("EVOLVELEVEL");
					if (level == 0) {
						System.out.println("Evolution needed for " + result.getString("EVOLVECONDITION"));
					} else {
						builder.append(evolveLink ? ", and evolves" : " It [[evolves]]");
						builder.append(" into ");
						appendLink(builder, evoName);
						builder.append(" at level ");
						builder.append(level);
						evolveLink = true;
					}
				}
				if (evolveLink) {
					builder.append('.');
				}
				builder.append("\n\n");
			}

			String pokedexDescription = database.getLangMap()
					.get("pixelmon." + currentPokemon.name.toLowerCase() + ".description");
			builder.append(pokedexDescription);
			if (StringUtil.containsAny(pokedexDescription, " mile", " mph", " feet", "Fahrenheit")) {
				System.out.println("Units found in Pokédex entry.");
			}

			if (missingHeader) {
				builder.append("\n__TOC__");
			}

			if (rarity != 0) {
				builder.append("\n==Spawn rate==");
				if (rarity > 0) {
					builder.append("\n{{rateH}}");

					for (String biome : biomes) {
						for (String location : locations) {
							for (int i = 0; i < timeRarities.length; i++) {
								int totalRarity = Math.round(timeRarities[i] * rarity);
								String timeString = null;
								if (i == 0) {
									timeString = "|Dusk";
								} else if (i == 2) {
									timeString = "|Night";
								}
								if (totalRarity > 0) {
									builder.append("\n{{rate|");
									builder.append(totalRarity);
									builder.append("|");
									builder.append(biome);
									if (timeString != null) {
										builder.append(timeString);
									}
									if (location.equals("Air")) {
										location = "Land";
									} else if (location.equals("Air persistent")) {
										location = "Air";
									}
									if (!location.equals("Land")) {
										builder.append("|");
										if (timeString == null) {
											builder.append("4=");
										}
										builder.append(location);
									}
									builder.append("}}");
								}
							}
						}
					}

					builder.append("\n{{rateF}}");
				} else {
					builder.append("\nSee [[Legendary Pokémon#Spawning]].");
				}
			}
			
			DropsMiner dropsMiner = new DropsMiner();
			builder.append('\n');
			builder.append(dropsMiner.getDrops(pokemon));

			builder.append("\n==Stats==\n{{BaseStats");
			appendTableField("type", type1.toLowerCase());
			if (type2 != null) {
				appendTableField("type2", type2.toLowerCase());
			}
			appendTableField("HP", hp);
			appendTableField("Attack", attack);
			appendTableField("Defense", defense);
			appendTableField("SpAtk", specialAttack);
			appendTableField("SpDef", specialDefense);
			appendTableField("Speed", speed);
			builder.append("\n}}");

			builder.append("\n==[[Type]] effectiveness==\n{{Type effectiveness");
			EnumType type1Enum = EnumType.parseType(type1);
			EnumType type2Enum = null;
			if (type2 != null) {
				type2Enum = EnumType.parseType(type2);
			}
			for (EnumType enumType : EnumType.values()) {
				float typeEffectiveness = Effectiveness.Normal.value;
				typeEffectiveness *= EnumType.getEffectiveness(enumType, type1Enum);
				if (type2Enum != null) {
					typeEffectiveness *= EnumType.getEffectiveness(enumType, type2Enum);
				}
				if (typeEffectiveness != Effectiveness.Normal.value) {
					appendTableField(enumType.name().toLowerCase(), typeEffectiveness);
				}
			}
			builder.append("\n}}");

			Set<String> fixedMoves = new HashSet<>();
			String[] fixedMoveArray = new String[] { "Bide", "Counter", "Dragon Rage", "Endeavor", "Final Gambit",
					"Fissure", "Guillotine", "Hidden Power", "Horn Drill", "Metal Burst", "Mirror Coat", "Natural Gift",
					"Nature Power", "Night Shade", "Psywave", "Seismic Toss", "Sheer Cold", "SonicBoom", "Super Fang" };
			for (String move : fixedMoveArray) {
				fixedMoves.add(move);
			}

			builder.append("\n==Moves==\n===By level===\n{{learnlist/levelh|");
			String tableDetails = englishName + "|" + type1;
			if (type2 != null) {
				tableDetails += "|" + type2;
			}
			builder.append(tableDetails);
			builder.append("}}");
			query = new StringBuilder(
					"SELECT a.LEARNLEVEL, b.NAME AS MOVE, c.NAME AS TYPE, d.NAME AS CATEGORY, b.POWER, b.ACCURACY, b.PP FROM PIXELMONLEVELSKILLS a ");
			query.append("JOIN MOVES b ON a.MOVEID = b.MOVEID ");
			query.append("JOIN TYPES c ON b.TYPEID = c.TYPEID ");
			query.append("JOIN MOVECATEGORIES d ON b.MOVECATEGORYID = d.MOVECATEGORYID ");
			query.append("WHERE PIXELMONID = ");
			query.append(databaseID);
			result = database.executeQuery(query);
			boolean hasSTAB = false;
			boolean hasEvoSTAB = false;
			Set<String> levelUpMoves = new HashSet<>();
			while (result.next()) {
				builder.append("\n{{learnlist/level5|");
				int level = result.getInt("LEARNLEVEL");
				if (level == 0) {
					level = 1;
				}
				builder.append(level);
				builder.append('|');
				String moveName = result.getString("MOVE");
				levelUpMoves.add(moveName);
				builder.append(Move.translate(moveName));
				builder.append('|');
				String moveType = result.getString("TYPE");
				builder.append(moveType);
				builder.append('|');
				String category = result.getString("CATEGORY");
				builder.append(category);
				builder.append('|');
				int power = result.getInt("POWER");
				builder.append(power == 0 ? "&mdash;" : power);
				builder.append('|');
				int accuracy = result.getInt("ACCURACY");
				builder.append(accuracy == 0 ? "&mdash;" : accuracy);
				builder.append('|');
				builder.append(result.getInt("PP"));
				if (!category.equals("Status") && !fixedMoves.contains(moveName)) {
					if (moveType.equals(type1) || moveType.equals(type2)) {
						builder.append("||'''");
						hasSTAB = true;
					} else if (evoTypes.contains(moveType)) {
						builder.append("||''");
						hasEvoSTAB = true;
					}
				}
				builder.append("}}");
			}
			builder.append("\n{{learnlist/levelf|");
			builder.append(tableDetails);
			if (hasEvoSTAB) {
				builder.append("|6=1");
			}
			if (!hasSTAB) {
				builder.append("|7=1");
			}
			builder.append("}}");

			boolean hasPriorMoves = false;
			hasSTAB = false;
			hasEvoSTAB = false;
			Set<String> preEvoMoves = new HashSet<>();
			if (!preEvoIDs.isEmpty()) {
				Map<String, PreEvoEntry> preEvoEntries = new HashMap<>();
				List<PreEvoEntry> preEvoList = new ArrayList<>();
				for (int i = preEvoIDs.size() - 1; i >= 0; i--) {
					Integer preEvoID = preEvoIDs.get(i);
					query = new StringBuilder();
					query.append(
							"SELECT DISTINCT e.PIXELMONNAME, e.NATIONALPOKEDEXNUMBER, a.LEARNLEVEL, b.NAME AS MOVE, c.NAME AS TYPE, d.NAME AS CATEGORY, b.POWER, b.ACCURACY, b.PP FROM PIXELMONLEVELSKILLS a ");
					query.append("JOIN MOVES b ON a.MOVEID = b.MOVEID ");
					query.append("JOIN TYPES c ON b.TYPEID = c.TYPEID ");
					query.append("JOIN MOVECATEGORIES d ON b.MOVECATEGORYID = d.MOVECATEGORYID ");
					query.append("JOIN PIXELMON e ON a.PIXELMONID = e.PIXELMONID ");
					query.append("WHERE a.PIXELMONID = ");
					query.append(preEvoID);
					query.append(" ORDER BY a.LEARNLEVEL, b.NAME");
					result = database.executeQuery(query);
					while (result.next()) {
						String moveName = result.getString("MOVE");
						if (!levelUpMoves.contains(moveName)) {
							PreEvoEntry entry = preEvoEntries.get(moveName);
							if (entry == null) {
								entry = new PreEvoEntry();
								Move move = new Move(moveName);
								move.type = result.getString("TYPE");
								move.category = result.getString("CATEGORY");
								move.power = result.getInt("POWER");
								move.accuracy = result.getInt("ACCURACY");
								move.pp = result.getInt("PP");
								entry.move = move;
								preEvoEntries.put(moveName, entry);
								preEvoList.add(entry);
							}
							Pokemon preEvoPokemon = new Pokemon(result.getString("PIXELMONNAME"));
							preEvoPokemon.nationalPokedexNumber = result.getInt("NATIONALPOKEDEXNUMBER");
							if (!entry.preEvolutions.contains(preEvoPokemon)) {
								entry.preEvolutions.add(preEvoPokemon);
							}
						}
					}
				}
				if (!preEvoEntries.isEmpty()) {
					hasPriorMoves = true;
					builder.append("\n===By prior [[evolution]]===\n{{learnlist/prevoh|");
					builder.append(tableDetails);
					builder.append("}}");
				}
				for (PreEvoEntry entry : preEvoList) {
					Pokemon pokemon1 = entry.preEvolutions.get(0);
					Pokemon pokemon2 = null;
					if (entry.preEvolutions.size() > 1) {
						pokemon2 = entry.preEvolutions.get(1);
					}
					Move move = entry.move;
					builder.append("\n{{learnlist/prevo5|");
					builder.append(pokemon1.nationalPokedexNumber);
					builder.append('|');
					builder.append(pokemon1.getTranslatedName());
					builder.append("||");
					if (pokemon2 != null) {
						builder.append(pokemon2.nationalPokedexNumber);
					}
					builder.append('|');
					if (pokemon2 != null) {
						builder.append(pokemon2.getTranslatedName());
					}
					builder.append("||");
					preEvoMoves.add(move.name);
					builder.append(Move.translate(move.name));
					builder.append('|');
					String moveType = move.type;
					builder.append(moveType);
					builder.append('|');
					String category = move.category;
					builder.append(category);
					builder.append('|');
					int power = move.power;
					builder.append(power == 0 ? "&mdash;" : power);
					builder.append('|');
					int accuracy = move.accuracy;
					builder.append(accuracy == 0 ? "&mdash;" : accuracy);
					builder.append('|');
					builder.append(move.pp);
					if (!category.equals("Status") && !fixedMoves.contains(move.name)) {
						if (moveType.equals(type1) || moveType.equals(type2)) {
							builder.append("|'''");
							hasSTAB = true;
						} else if (evoTypes.contains(moveType)) {
							builder.append("|''");
							hasEvoSTAB = true;
						}
					}
					builder.append("}}");
				}
			}

			if (hasPriorMoves) {
				builder.append("\n{{learnlist/prevof|");
				builder.append(tableDetails);
				if (hasEvoSTAB) {
					builder.append("|6=1");
				}
				if (!hasSTAB) {
					builder.append("|7=1");
				}
				builder.append("}}");
			}

			query = new StringBuilder();
			query.append("SELECT * FROM (");
			query.append(
					"SELECT DISTINCT b.NAME AS MOVE, b.TMID, b.TUTORTYPE AS HMID, c.NAME AS TYPE, d.NAME AS CATEGORY, b.POWER, b.ACCURACY, b.PP FROM PIXELMONTMHMSKILLS a ");
			query.append("JOIN MOVES b ON a.MOVEID = b.MOVEID ");
			query.append("JOIN TYPES c ON b.TYPEID = c.TYPEID ");
			query.append("JOIN MOVECATEGORIES d ON b.MOVECATEGORYID = d.MOVECATEGORYID ");
			query.append("WHERE PIXELMONID = ");
			query.append(databaseID);
			query.append(" AND b.TMID IS NOT NULL");
			query.append(" ORDER BY b.TMID) ");
			query.append("UNION ALL ");
			query.append("SELECT * FROM (");
			query.append(
					"SELECT DISTINCT b.NAME, b.TUTORTYPE, b.HMID, c.NAME AS TYPE, d.NAME AS CATEGORY, b.POWER, b.ACCURACY, b.PP FROM PIXELMONTMHMSKILLS a ");
			query.append("JOIN MOVES b ON a.MOVEID = b.MOVEID ");
			query.append("JOIN TYPES c ON b.TYPEID = c.TYPEID ");
			query.append("JOIN MOVECATEGORIES d ON b.MOVECATEGORYID = d.MOVECATEGORYID ");
			query.append("WHERE PIXELMONID = ");
			query.append(databaseID);
			query.append(" AND b.HMID IS NOT NULL");
			query.append(" ORDER BY b.HMID)");
			result = database.executeQuery(query);
			hasSTAB = false;
			hasEvoSTAB = false;
			boolean hasTMMoves = false;
			while (result.next()) {
				if (!hasTMMoves) {
					builder.append("\n===By [[TM]]/[[HM]]===\n{{learnlist/tmh|");
					builder.append(tableDetails);
					builder.append("}}");
					hasTMMoves = true;
				}
				builder.append("\n{{learnlist/tm5|");
				int tmID = result.getInt("TMID");
				boolean isHM = tmID == 0;
				if (isHM) {
					tmID = result.getInt("HMID");
				}
				builder.append(isHM ? "HM" : "TM");
				if (tmID < 10) {
					builder.append(0);
				}
				builder.append(tmID);
				builder.append('|');
				String moveName = result.getString("MOVE");
				builder.append(Move.translate(moveName));
				builder.append('|');
				String moveType = result.getString("TYPE");
				builder.append(moveType);
				builder.append('|');
				String category = result.getString("CATEGORY");
				builder.append(category);
				builder.append('|');
				int power = result.getInt("POWER");
				builder.append(power == 0 ? "&mdash;" : power);
				builder.append('|');
				int accuracy = result.getInt("ACCURACY");
				builder.append(accuracy == 0 ? "&mdash;" : accuracy);
				builder.append('|');
				builder.append(result.getInt("PP"));
				if (!category.equals("Status") && !fixedMoves.contains(moveName)) {
					if (moveType.equals(type1) || moveType.equals(type2)) {
						builder.append("||'''");
						hasSTAB = true;
					} else if (evoTypes.contains(moveType)) {
						builder.append("||''");
						hasEvoSTAB = true;
					}
				}
				builder.append("}}");
			}
			if (hasTMMoves) {
				builder.append("\n{{learnlist/tmf|");
				builder.append(tableDetails);
				if (hasEvoSTAB) {
					builder.append("|6=1");
				}
				if (!hasSTAB) {
					builder.append("|7=1");
				}
				builder.append("}}");
			}

			boolean isSmeargle = pokemon.equals("Smeargle");

			query = new StringBuilder(
					"SELECT DISTINCT b.NAME AS MOVE, c.NAME AS TYPE, d.NAME AS CATEGORY, b.POWER, b.ACCURACY, b.PP FROM ");
			if (isSmeargle) {
				query.append("MOVES b ");
			} else {
				query.append("PIXELMONTUTORSKILLS a ");
				query.append("JOIN MOVES b ON a.MOVEID = b.MOVEID ");
			}
			query.append("JOIN TYPES c ON b.TYPEID = c.TYPEID ");
			query.append("JOIN MOVECATEGORIES d ON b.MOVECATEGORYID = d.MOVECATEGORYID ");
			if (isSmeargle) {
				query.append("WHERE b.NAME IS NOT 'Struggle' AND b.NAME IS NOT 'Chatter'");
			} else {
				query.append("WHERE PIXELMONID = ");
				query.append(databaseID);
				query.append(" AND b.TUTORTYPE = 1");
			}
			query.append(" ORDER BY MOVE");
			result = database.executeQuery(query);
			hasSTAB = false;
			hasEvoSTAB = false;
			boolean hasTutorMove = false;
			while (result.next()) {
				if (!hasTutorMove) {
					hasTutorMove = true;
					if (isSmeargle) {
						builder.append("\n===By [[Sketch]]===");
						builder.append(
								"\nUsing [[Sketch]], Smeargle can learn any move except [[Chatter]] and [[Struggle]].");
					} else {
						builder.append("\n===By [[move tutor]]===");
					}
					builder.append("\n{{learnlist/tutorh|");
					builder.append(tableDetails);
					builder.append("}}");
				}
				String moveName = Move.translate(result.getString("MOVE"));
				if (isSmeargle) {
					Set<String> unavailable = StringUtil.getUnavailableMoves();
					if (unavailable.contains(moveName)) {
						continue;
					}
				}
				builder.append("\n{{learnlist/tutor|");
				builder.append(moveName);
				builder.append('|');
				String moveType = result.getString("TYPE");
				builder.append(moveType);
				builder.append('|');
				String category = result.getString("CATEGORY");
				builder.append(category);
				builder.append('|');
				int power = result.getInt("POWER");
				builder.append(power == 0 ? "&mdash;" : power);
				builder.append('|');
				int accuracy = result.getInt("ACCURACY");
				builder.append(accuracy == 0 ? "&mdash;" : accuracy);
				builder.append('|');
				builder.append(result.getInt("PP"));
				if (!category.equals("Status") && !fixedMoves.contains(moveName)) {
					if (moveType.equals(type1) || moveType.equals(type2)) {
						builder.append("||'''");
						hasSTAB = true;
					} else if (evoTypes.contains(moveType)) {
						builder.append("||''");
						hasEvoSTAB = true;
					}
				}
				builder.append("}}");
			}
			if (hasTutorMove) {
				builder.append("\n{{learnlist/tutorf|");
				builder.append(tableDetails);
				if (hasEvoSTAB) {
					builder.append("|4=1");
				}
				if (!hasSTAB) {
					builder.append("|5=1");
				}
				builder.append("}}");
			}

			if (egg1.equals("Undiscovered")) {
				if (pokemon.equals("Nidorina") || pokemon.equals("Nidoqueen")) {
					egg1ID = 0; // Monster
					egg2ID = 3; // Field
				} else if (!evoIDs.isEmpty()) {
					query = new StringBuilder();
					query.append("SELECT EGGGROUP1ID, EGGGROUP2ID FROM PIXELMON WHERE PIXELMONID = ");
					query.append(evoIDs.get(0));
					result = database.executeQuery(query);
					result.next();
					egg1ID = result.getInt("EGGGROUP1ID");
					egg2ID = result.getInt("EGGGROUP2ID");
					if (result.wasNull()) {
						egg2ID = -1;
					}
				}
			}

			query = new StringBuilder();
			query.append(
					"SELECT DISTINCT b.MOVEID, b.NAME AS MOVE, c.NAME AS TYPE, d.NAME AS CATEGORY, b.POWER, b.ACCURACY, b.PP FROM PIXELMONEGGSKILLS a ");
			query.append("JOIN MOVES b ON a.MOVEID = b.MOVEID ");
			query.append("JOIN TYPES c ON b.TYPEID = c.TYPEID ");
			query.append("JOIN MOVECATEGORIES d ON b.MOVECATEGORYID = d.MOVECATEGORYID ");
			query.append("WHERE a.PIXELMONID = ");
			query.append(databaseID);
			query.append(" AND b.TMID IS NULL");
			query.append(" AND b.HMID IS NULL");
			query.append(" ORDER BY MOVE");
			result = database.executeQuery(query);
			Set<String> eggMoveSet = new HashSet<>();
			StringBuilder eggMovesTable = new StringBuilder();

			hasSTAB = false;
			hasEvoSTAB = false;
			boolean hasEggMove = false;

			Set<String> availablePokemon = EnumPokemon.getAllPokemon();

			boolean hasChainMove = false;
			boolean maleOnly = genderRatio == 100
					&& !StringUtil.equalsAny(pokemon, "Nidoranmale", "Nidorino", "Nidoking", "Volbeat");
			while (result.next()) {
				String moveName = result.getString("MOVE");
				if (!levelUpMoves.contains(moveName) && !preEvoMoves.contains(moveName)) {
					if (!hasEggMove) {
						eggMovesTable.append("\n===By [[breeding]]===\n{{learnlist/breedh|");
						eggMovesTable.append(type1);
						if (type2 != null) {
							eggMovesTable.append('|');
							eggMovesTable.append(type2);
						}
						eggMovesTable.append("}}");
						hasEggMove = true;
					}
					eggMovesTable.append("\n{{learnlist/breed5|");
					Statement eggStatement = null;
					ResultSet eggResult = null;
					boolean chainMove = false;
					try {
						eggStatement = database.createNewStatement();
						query = new StringBuilder();
						query.append(
								"SELECT DISTINCT c.PIXELMONID, c.PIXELMONNAME, c.NATIONALPOKEDEXNUMBER, c.FORM FROM (SELECT a.PIXELMONID FROM PIXELMON a ");
						query.append("WHERE (a.EGGGROUP1ID = ");
						query.append(egg1ID);
						query.append(" OR a.EGGGROUP2ID = ");
						query.append(egg1ID);
						if (egg2ID > -1) {
							query.append(" OR a.EGGGROUP1ID = ");
							query.append(egg2ID);
							query.append(" OR a.EGGGROUP2ID = ");
							query.append(egg2ID);
						}
						query.append(") AND a.MALEPERCENT > 0) b ");
						query.append("JOIN PIXELMON c ON b.PIXELMONID = c.PIXELMONID ");
						query.append("INNER JOIN (SELECT PIXELMONID FROM PIXELMONLEVELSKILLS WHERE MOVEID = ");
						query.append(result.getInt("MOVEID"));
						query.append(") d ON c.PIXELMONID = d.PIXELMONID ");
						query.append("ORDER BY c.NATIONALPOKEDEXNUMBER");
						eggResult = database.executeQuery(query, eggStatement);
						boolean hasPokemon = false;
						while (eggResult.next()) {
							String parentName = eggResult.getString("PIXELMONNAME");
							if (availablePokemon.contains(parentName)) {
								parentName = StringUtil.getFormName(parentName, eggResult.getInt("FORM"));
								if (!parentName.isEmpty()) {
									if (maleOnly
											&& !getEvolutions(databaseID).contains(eggResult.getInt("PIXELMONID"))) {
										continue;
									}
									hasPokemon = true;
									eggMovesTable.append("{{p|");
									eggMovesTable
											.append(Pokemon.getTranslatedName(eggResult.getString("PIXELMONNAME")));
									eggMovesTable.append("|1}}");
								}
							}
						}
						if (!hasPokemon) {
							System.out.println(pokemon + " has no parents for " + moveName + ".");
							chainMove = true;
							hasChainMove = true;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					} finally {
						database.closeStatement(eggStatement, eggResult);
					}
					eggMovesTable.append('|');
					eggMovesTable.append(Move.translate(moveName));
					eggMovesTable.append('|');
					String moveType = result.getString("TYPE");
					eggMovesTable.append(moveType);
					eggMovesTable.append('|');
					String category = result.getString("CATEGORY");
					eggMovesTable.append(category);
					eggMovesTable.append('|');
					int power = result.getInt("POWER");
					eggMovesTable.append(power == 0 ? "&mdash;" : power);
					eggMovesTable.append('|');
					int accuracy = result.getInt("ACCURACY");
					eggMovesTable.append(accuracy == 0 ? "&mdash;" : accuracy);
					eggMovesTable.append('|');
					eggMovesTable.append(result.getInt("PP"));
					if (!category.equals("Status") && !fixedMoves.contains(moveName)) {
						if (moveType.equals(type1) || moveType.equals(type2)) {
							eggMovesTable.append("|'''");
							hasSTAB = true;
						} else if (evoTypes.contains(moveType)) {
							eggMovesTable.append("|''");
							hasEvoSTAB = true;
						}
					}
					if (chainMove) {
						eggMovesTable.append("|9=*");
					}
					eggMovesTable.append("}}");
					eggMoveSet.add(moveName);
				}
			}
			if (hasEggMove) {
				eggMovesTable.append("\n{{learnlist/breedf|");
				eggMovesTable.append(tableDetails);
				if (hasEvoSTAB) {
					eggMovesTable.append("|4=1");
				}
				if (!hasSTAB) {
					eggMovesTable.append("|5=1");
				}
				if (hasChainMove) {
					eggMovesTable.append("|6=1");
				}
				eggMovesTable.append("}}");
			}

			query = new StringBuilder(
					"SELECT DISTINCT b.NAME AS MOVE, c.NAME AS TYPE, d.NAME AS CATEGORY, b.POWER, b.ACCURACY, b.PP FROM PIXELMONTUTORSKILLS a ");
			query.append("JOIN MOVES b ON a.MOVEID = b.MOVEID ");
			query.append("JOIN TYPES c ON b.TYPEID = c.TYPEID ");
			query.append("JOIN MOVECATEGORIES d ON b.MOVECATEGORYID = d.MOVECATEGORYID ");
			query.append("WHERE PIXELMONID = ");
			query.append(databaseID);
			query.append(" AND b.TUTORTYPE = 2");
			query.append(" ORDER BY MOVE");
			result = database.executeQuery(query);
			hasSTAB = false;
			hasEvoSTAB = false;
			boolean hasEventTutorMove = false;
			while (result.next()) {
				String moveName = result.getString("MOVE");
				if (!levelUpMoves.contains(moveName) && !eggMoveSet.contains(moveName)
						&& !preEvoMoves.contains(moveName)) {
					if (!hasEventTutorMove) {
						hasEventTutorMove = true;
						builder.append("\n====By [[event move tutor]]====\n{{learnlist/tutorh|");
						builder.append(tableDetails);
						builder.append("}}");
					}
					builder.append("\n{{learnlist/tutor|");
					builder.append(Move.translate(moveName));
					builder.append('|');
					String moveType = result.getString("TYPE");
					builder.append(moveType);
					builder.append('|');
					String category = result.getString("CATEGORY");
					builder.append(category);
					builder.append('|');
					int power = result.getInt("POWER");
					builder.append(power == 0 ? "&mdash;" : power);
					builder.append('|');
					int accuracy = result.getInt("ACCURACY");
					builder.append(accuracy == 0 ? "&mdash;" : accuracy);
					builder.append('|');
					builder.append(result.getInt("PP"));
					if (!category.equals("Status") && !fixedMoves.contains(moveName)) {
						if (moveType.equals(type1) || moveType.equals(type2)) {
							builder.append("||'''");
							hasSTAB = true;
						} else if (evoTypes.contains(moveType)) {
							builder.append("||''");
							hasEvoSTAB = true;
						}
					}
					builder.append("}}");
				}
			}
			if (hasEventTutorMove) {
				builder.append("\n{{learnlist/tutorf|");
				builder.append(tableDetails);
				if (hasEvoSTAB) {
					builder.append("|4=1");
				}
				if (!hasSTAB) {
					builder.append("|5=1");
				}
				builder.append("}}");
			}

			builder.append(eggMovesTable);
			builder.append('\n');

			builder.append(pastModels);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return builder.toString();
	}

	/**
	 * Creates an article about a move.
	 * @param move The move to create an article about.
	 * @return The wikicode for the move article.
	 */
	public String getMoveArticle(String move) {
		if (move.isEmpty()) {
			move = "Zen Headbutt";
		}

		builder = new StringBuilder();
		builder.append("{{MoveInfobox");
		String translatedMove = Move.translate(move);
		appendTableField("name", translatedMove);

		StringBuilder query = new StringBuilder();
		query.append(
				"SELECT a.MOVEID, a.TMID, a.HMID, b.NAME AS TYPE, c.NAME AS CATEGORY, a.POWER, a.ACCURACY, a.PP, a.HITSALL, a.HITSOPPOSITEFOE, a.HITSSELF, a.HITSADJACENTALLY, a.TUTORTYPE, a.EFFECT FROM MOVES a ");
		query.append("JOIN TYPES b ON a.TYPEID = b.TYPEID ");
		query.append("JOIN MOVECATEGORIES c ON a.MOVECATEGORYID = c.MOVECATEGORYID ");
		query.append("WHERE a.NAME = '");
		query.append(move);
		query.append("'");
		ResultSet result = database.executeQuery(query);

		try {
			if (!result.next()) {
				System.out.println("Move not found: " + move);
				return "";
			}
			int moveID = result.getInt("MOVEID");

			String type = result.getString("TYPE");
			appendTableField("type", type);

			String category = result.getString("CATEGORY");
			appendTableField("damagecategory", category);

			String target;
			boolean hitsAll = result.getBoolean("HITSALL");
			boolean hitsFoe = result.getBoolean("HITSOPPOSITEFOE");
			boolean hitsSelf = result.getBoolean("HITSSELF");
			boolean hitsAlly = result.getBoolean("HITSADJACENTALLY");
			if (!hitsAll && hitsFoe && !hitsSelf && hitsAlly) {
				target = "single";
			} else if (hitsAll && hitsFoe && !hitsSelf && !hitsAlly) {
				target = "both";
			} else if (hitsAll && hitsFoe && !hitsSelf && hitsAlly) {
				target = "allothers";
			} else if (hitsAll && hitsFoe && hitsSelf && hitsAlly) {
				target = "all";
			} else if (!hitsFoe && hitsSelf && !hitsAlly) {
				target = "self";
			} else if (!hitsAll && !hitsFoe && hitsSelf && hitsAlly) {
				target = "selforpartner";
			} else if (!hitsAll && !hitsFoe && !hitsSelf && hitsAlly) {
				target = "partner";
			} else if (hitsAll && !hitsFoe && hitsSelf && hitsAlly) {
				target = "team";
			} else if (!hitsAll && hitsFoe && !hitsAlly) {
				target = "random";
			} else {
				System.out.println("Unrecognized targeting info. All: " + hitsAll + ", Foe: " + hitsFoe + ", Self: "
						+ hitsSelf + ", Ally: " + hitsAlly + ".");
				target = null;
			}
			appendTableField("target", target);

			appendTableField("basepp", result.getInt("PP"));

			int power = result.getInt("POWER");
			appendTableField("power", power == 0 ? "&mdash;" : power);

			int accuracy = result.getInt("ACCURACY");
			appendTableField("accuracy", accuracy == 0 ? "&mdash;" : accuracy);

			String effect = result.getString("EFFECT");
			int priorityIndex = effect.indexOf("Priority");
			if (priorityIndex > -1) {
				priorityIndex += +"Priority".length() + 1;
				String priority = effect.substring(priorityIndex, priorityIndex + 2);
				appendTableField("priority", priority);
			}

			String tm = "";
			int tmNum = result.getInt("TMID");
			int hmNum = result.getInt("HMID");
			int tutorType = result.getInt("TUTORTYPE");
			if ("Rock Smash".equals(move)) {
				tm = "TM94/HM06";
			} else if (tmNum != 0) {
				tm = "TM";
				if (tmNum < 10) {
					tm += "0";
				}
				tm += tmNum;
			} else if (hmNum != 0) {
				tm = "HM";
				if (hmNum < 10) {
					tm += "0";
				}
				tm += hmNum;
			} else if (tutorType != 0) {
				if (tutorType == 2) {
					tm += "Event m";
				} else {
					tm += "M";
				}
				tm += "ove tutor";
			}
			if (!tm.isEmpty()) {
				appendTableField("tm", tm);
			}

			String moveArticle = move;
			if (StringUtil.equalsAny(move, "Growth", "Metronome", "Psychic", "Wood Hammer")) {
				moveArticle += " (move)";
			}
			totalRaw = APIConnection.getArticleSourcePixelmon(moveArticle);
			if (totalRaw.startsWith("#REDIRECT") && !moveArticle.equals(translatedMove)) {
				totalRaw = APIConnection.getArticleSourcePixelmon(translatedMove);
			}
			if (!totalRaw.isEmpty()) {
				StringUtil.currentRaw = totalRaw;
				String externalMove = StringUtil.getTableEntry("external");
				if (externalMove != null) {
					appendTableField("external", externalMove);
				}
			}

			builder.append("\n}}\n");

			int descriptionIndex = totalRaw.indexOf(translatedMove + " is");
			if (descriptionIndex == -1) {
				System.out.println("Can't find move description: " + translatedMove);
				builder.append(move);
				builder.append(" is a ");
				boolean isDamage = !category.equals("Status");
				if (isDamage) {
					builder.append("damaging ");
				}
				appendLink(builder, type);
				builder.append("-[[type]] move");
				builder.append(isDamage ? '.' : ' ');
				builder.append('\n');
			} else {
				String description = totalRaw.substring(descriptionIndex, totalRaw.indexOf("==Learnset=="));
				description = description.replace("-type", "-[[type]]");
				if (description.contains("opponent")) {
					description = description.replace("opponent", "target");
				}
				builder.append(description);
			}

			builder.append("==Learnset==");

			Set<String> availablePokemon = EnumPokemon.getAllPokemon();
			query = new StringBuilder();
			query.append(
					"SELECT DISTINCT b.PIXELMONID, b.PIXELMONNAME, b.NATIONALPOKEDEXNUMBER, c.NAME AS TYPE1, d.NAME AS TYPE2, a.LEARNLEVEL, b.FORM FROM PIXELMONLEVELSKILLS a ");
			query.append("JOIN PIXELMON b ON a.PIXELMONID = b.PIXELMONID ");
			query.append("JOIN TYPES c ON b.PIXELMONTYPE1ID = c.TYPEID ");
			query.append("LEFT JOIN TYPES d ON b.PIXELMONTYPE2ID = d.TYPEID ");
			query.append("WHERE MOVEID = ");
			query.append(moveID);
			query.append(" ORDER BY b.NATIONALPOKEDEXNUMBER, b.FORM, a.LEARNLEVEL");
			result = database.executeQuery(query);
			Map<String, LevelUpEntry> levelUpMap = new HashMap<>();
			List<LevelUpEntry> levelUpList = new ArrayList<>();
			boolean hasLevelUp = false;
			Set<Integer> learnIDs = new HashSet<>();
			Set<Integer> evolutions = new HashSet<>();
			while (result.next()) {
				String pokemon = result.getString("PIXELMONNAME");
				if (availablePokemon.contains(pokemon)) {
					int id = result.getInt("PIXELMONID");
					learnIDs.add(id);
					evolutions.addAll(getEvolutions(id));
					pokemon = StringUtil.getFormName(pokemon, result.getInt("FORM"));
					if (!"".equals(pokemon)) {
						LevelUpEntry entry = levelUpMap.get(pokemon);
						if (entry == null) {
							entry = new LevelUpEntry(pokemon);
							entry.pokemon.type1 = result.getString("TYPE1");
							entry.pokemon.type2 = result.getString("TYPE2");
							levelUpMap.put(pokemon, entry);
							levelUpList.add(entry);
						}
						entry.addLevel(result.getInt("LEARNLEVEL"));
						hasLevelUp = true;
					}
				}
			}
			if (hasLevelUp) {
				builder.append("\n===By level===\n{{levelmoveh}}");
				for (LevelUpEntry entry : levelUpList) {
					builder.append("\n{{levelmove|");
					builder.append(entry.pokemon.name);
					builder.append('|');
					builder.append(entry.levels.get(0));
					for (int i = 1; i < entry.levels.size(); i++) {
						builder.append(", ");
						builder.append(entry.levels.get(i));
					}
					builder.append('|');
					builder.append(entry.pokemon.type1);
					if (entry.pokemon.type2 != null) {
						builder.append('|');
						builder.append(entry.pokemon.type2);
					}
					builder.append("}}");
				}
				builder.append("\n{{levelmovef}}");

				query = new StringBuilder();
				query.append(
						"SELECT b.PIXELMONNAME, b.NATIONALPOKEDEXNUMBER, c.NAME AS TYPE1, d.NAME AS TYPE2, b.FORM FROM PIXELMON b ");
				query.append("JOIN TYPES c ON b.PIXELMONTYPE1ID = c.TYPEID ");
				query.append("LEFT JOIN TYPES d ON b.PIXELMONTYPE2ID = d.TYPEID ");
				query.append("WHERE b.PIXELMONID = ");
				boolean first = true;
				for (Integer id : evolutions) {
					if (!learnIDs.contains(id)) {
						if (!first) {
							query.append(" OR b.PIXELMONID = ");
						}
						first = false;
						query.append(id);
						learnIDs.add(id);
					}
				}
				query.append(" ORDER BY b.NATIONALPOKEDEXNUMBER");
				if (first) {
					result.close();
				} else {
					result = database.executeQuery(query);
				}

				boolean hasEvolutionMove = false;
				while (!result.isClosed() && result.next()) {
					String pokemon = result.getString("PIXELMONNAME");
					if (availablePokemon.contains(pokemon)) {
						String formName = StringUtil.getFormName(pokemon, result.getInt("FORM"));
						if (!formName.isEmpty()) {
							if (!hasEvolutionMove) {
								builder.append("\n===By prior [[evolution]]===\n{{moveh}}");
								hasEvolutionMove = true;
							}
							builder.append("\n{{moveentry|");
							builder.append(StringUtil.getFormName(pokemon, result.getInt("FORM")));
							builder.append('|');
							builder.append(result.getString("TYPE1"));
							String type2 = result.getString("TYPE2");
							if (type2 != null) {
								builder.append('|');
								builder.append(type2);
							}
							builder.append("}}");
						}
					}
				}
				if (hasEvolutionMove) {
					builder.append("\n{{movef}}");
				}
			}

			StringBuilder eggBuilder = new StringBuilder();
			if (tmNum == 0 && hmNum == 0) {
				boolean hasEggMove = false;
				query = new StringBuilder();
				query.append("SELECT DISTINCT b.PIXELMONID, b.NATIONALPOKEDEXNUMBER, b.FORM FROM PIXELMONEGGSKILLS a ");
				query.append("JOIN PIXELMON b ON a.PIXELMONID = b.PIXELMONID ");
				query.append("WHERE a.MOVEID = ");
				query.append(moveID);
				query.append(" ORDER BY b.NATIONALPOKEDEXNUMBER, b.FORM");
				result = database.executeQuery(query);
				List<List<Pokemon>> eggFamilies = new ArrayList<>();
				Set<Integer> eggSet = new HashSet<>();
				List<Integer> initEggList = new ArrayList<>();
				while (result.next()) {
					initEggList.add(result.getInt("PIXELMONID"));
				}
				for (Integer pokemonID : initEggList) {
					if (!eggSet.contains(pokemonID)) {
						query = new StringBuilder();
						query.append("SELECT a.PIXELMONNAME FROM PIXELMON a ");
						query.append("WHERE a.PIXELMONID = ");
						query.append(pokemonID);
						result = database.executeQuery(query);
						result.next();
						String pokemonName = result.getString("PIXELMONNAME");
						if (availablePokemon.contains(pokemonName)) {
							List<Integer> familyIDs = new ArrayList<>();
							familyIDs.add(pokemonID);
							familyIDs.addAll(getEvolutions(pokemonID));
							List<Integer> preEvoIDs = getPreEvolutions(pokemonID);
							for (Integer preEvoID : preEvoIDs) {
								familyIDs.add(0, preEvoID);
							}
							List<Pokemon> family = new ArrayList<>();
							for (Integer id : familyIDs) {
								query = new StringBuilder();
								query.append(
										"SELECT a.PIXELMONNAME, a.FORM, a.EGGGROUP1ID, a.EGGGROUP2ID, a.MALEPERCENT, a.NATIONALPOKEDEXNUMBER FROM PIXELMON a ");
								query.append("WHERE a.PIXELMONID = ");
								query.append(id);
								result = database.executeQuery(query);
								result.next();
								String idName = result.getString("PIXELMONNAME");
								if (availablePokemon.contains(idName)) {
									Pokemon pokemon = new Pokemon(idName);
									pokemon.form = result.getInt("FORM");
									pokemon.eggGroup1ID = result.getInt("EGGGROUP1ID");
									pokemon.eggGroup2ID = Database.getIntNull(result, "EGGGROUP2ID");
									pokemon.listIndex = id;
									pokemon.genderRatio = result.getInt("MALEPERCENT");
									pokemon.nationalPokedexNumber = result.getInt("NATIONALPOKEDEXNUMBER");
									family.add(pokemon);
									eggSet.add(id);
								}
							}
							if (!family.isEmpty()) {
								eggFamilies.add(family);
							}
						}
					}
				}
				for (List<Pokemon> family : eggFamilies) {
					if (family.get(0).name.equals("Eevee")) {
						family.sort(new DexNumberComparator());
					}
					Set<Integer> canLearnMove = new HashSet<>();
					List<String> childNames = new ArrayList<String>();
					List<Integer> familyIDs = new ArrayList<Integer>();
					for (Pokemon pokemon : family) {
						familyIDs.add(pokemon.listIndex);
						query = new StringBuilder();
						query.append("SELECT LEVELSKILLID FROM PIXELMONLEVELSKILLS WHERE PIXELMONID = ");
						query.append(pokemon.listIndex);
						query.append(" AND MOVEID = ");
						query.append(moveID);
						result = database.executeQuery(query);
						if (result.next()) {
							canLearnMove.add(pokemon.listIndex);
							continue;
						} else if (!canLearnMove.isEmpty()) {
							List<Integer> preEvoIDs = getPreEvolutions(pokemon.listIndex);
							boolean preEvoLearnMove = false;
							for (Integer preEvoID : preEvoIDs) {
								if (canLearnMove.contains(preEvoID)) {
									preEvoLearnMove = true;
									break;
								}
							}
							if (preEvoLearnMove) {
								continue;
							}
						}
						if (!hasEggMove) {
							hasEggMove = true;
							eggBuilder.append("\n===By [[breeding]]===\n{{eggH}}");
						}
						String formName = StringUtil.getFormName(pokemon.name, pokemon.form);
						learnIDs.add(pokemon.listIndex);
						if (!formName.isEmpty()) {
							if (childNames.isEmpty()) {
								eggBuilder.append("\n{{egg|");
							}
							childNames.add(formName);
						}
					}
					if (childNames.isEmpty()) {
						continue;
					}
					Pokemon checkPokemon = null;
					for (Pokemon pokemon : family) {
						// Undiscovered.
						if (pokemon.eggGroup1ID != 14) {
							checkPokemon = pokemon;
						}
						if (StringUtil.equalsAny(pokemon.name, "Nincada")) {
							break;
						}
					}
					query = new StringBuilder();
					query.append(
							"SELECT DISTINCT c.PIXELMONID, c.PIXELMONNAME, c.NATIONALPOKEDEXNUMBER, c.FORM FROM (SELECT a.PIXELMONID FROM PIXELMON a ");
					query.append("WHERE (a.EGGGROUP1ID = ");
					query.append(checkPokemon.eggGroup1ID);
					query.append(" OR a.EGGGROUP2ID = ");
					query.append(checkPokemon.eggGroup1ID);
					if (checkPokemon.eggGroup2ID > -1) {
						query.append(" OR a.EGGGROUP1ID = ");
						query.append(checkPokemon.eggGroup2ID);
						query.append(" OR a.EGGGROUP2ID = ");
						query.append(checkPokemon.eggGroup2ID);
					}
					query.append(") AND a.MALEPERCENT > 0) b ");
					query.append("JOIN PIXELMON c ON b.PIXELMONID = c.PIXELMONID ");
					query.append("INNER JOIN (SELECT PIXELMONID FROM PIXELMONLEVELSKILLS WHERE MOVEID = ");
					query.append(moveID);
					query.append(") d ON c.PIXELMONID = d.PIXELMONID ");
					query.append("ORDER BY c.NATIONALPOKEDEXNUMBER");
					result = database.executeQuery(query);
					List<String> parentNames = new ArrayList<>();
					while (result.next()) {
						String parentName = result.getString("PIXELMONNAME");
						if (availablePokemon.contains(parentName)) {
							if (checkPokemon.genderRatio == 100 && !familyIDs.contains(result.getInt("PIXELMONID"))
									&& !StringUtil.equalsAny(checkPokemon.name, "Nidoking", "Volbeat", "Gallade")) {
								continue;
							}
							parentName = StringUtil.getFormName(parentName, result.getInt("FORM"));
							if (!parentName.isEmpty()) {
								parentNames.add(parentName);
							}
						}
					}
					boolean first = true;
					boolean hasParents = !parentNames.isEmpty();
					for (String child : childNames) {
						if (!first) {
							eggBuilder.append("{{-}}");
						}
						eggBuilder.append("{{p|");
						eggBuilder.append(child);
						eggBuilder.append("}}");
						if (!hasParents) {
							eggBuilder.append("{{tt|*|Chain breed}}");
						}
						first = false;
					}
					eggBuilder.append('|');
					for (String parent : parentNames) {
						eggBuilder.append("{{p|");
						eggBuilder.append(parent);
						eggBuilder.append("|1}}");
					}
					if (!hasParents) {
						System.out.println("No parents for " + checkPokemon.name);
					}
					eggBuilder.append("}}");
				}
				if (hasEggMove) {
					eggBuilder.append("\n{{eggF}}");
				}
			}

			boolean addEggMoves = false;
			if (!tm.isEmpty()) {
				String header;
				String table = "PIXELMONTUTORSKILLS";
				if (tmNum > 0 || hmNum > 0) {
					if (tmNum == 94) {
						header = "TM]]/[[HM";
					} else {
						header = tmNum > 0 ? "TM" : "HM";
					}
					table = "PIXELMONTMHMSKILLS";
				} else if (tutorType == 2) {
					header = "event move tutor";
					addEggMoves = true;
				} else {
					header = "move tutor";
				}
				if (addEggMoves) {
					builder.append(eggBuilder);
				}
				query = new StringBuilder();
				query.append(
						"SELECT DISTINCT b.PIXELMONID, b.PIXELMONNAME, b.NATIONALPOKEDEXNUMBER, c.NAME AS TYPE1, d.NAME AS TYPE2, b.FORM FROM ");
				query.append(table);
				query.append(" a ");
				query.append("JOIN PIXELMON b ON a.PIXELMONID = b.PIXELMONID ");
				query.append("JOIN TYPES c ON b.PIXELMONTYPE1ID = c.TYPEID ");
				query.append("LEFT JOIN TYPES d ON b.PIXELMONTYPE2ID = d.TYPEID ");
				query.append("WHERE MOVEID = ");
				query.append(moveID);
				query.append(" ORDER BY b.NATIONALPOKEDEXNUMBER, b.FORM");
				result = database.executeQuery(query);
				boolean hasMove = false;
				while (result.next()) {
					String pokemon = result.getString("PIXELMONNAME");
					if (availablePokemon.contains(pokemon)
							&& (tutorType != 2 || !learnIDs.contains(result.getInt("PIXELMONID")))) {
						String formName = StringUtil.getFormName(pokemon, result.getInt("FORM"));
						if (!formName.isEmpty()) {
							if (!hasMove) {
								builder.append("\n===By [[");
								builder.append(header);
								builder.append("]]===\n{{moveh}}");
								hasMove = true;
							}
							builder.append("\n{{moveentry|");
							builder.append(formName);
							builder.append('|');
							builder.append(result.getString("TYPE1"));
							String type2 = result.getString("TYPE2");
							if (type2 != null) {
								builder.append('|');
								builder.append(type2);
							}
							builder.append("}}");
						}
					}
				}
				if (hasMove) {
					builder.append("\n{{movef}}");
				}
			}
			if (!addEggMoves) {
				builder.append(eggBuilder);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		builder.append("\n[[Category:Moves]]");

		return builder.toString();
	}

	/**
	 * Generates wikicode for biome articles
	 * @param biome The biome to create an article for.
	 * @return Wikicode for the specified biome article.
	 */
	public String getBiomeArticle(String biome) {
		if (biome.isEmpty()) {
			biome = "Desert M";
		}

		builder = new StringBuilder();

		StringBuilder main = new StringBuilder();
		StringBuilder template = new StringBuilder();

		template.append("{{#switch:{{{1|}}}");

		totalRaw = APIConnection.getArticleSourcePixelmon(biome.replace("+", "%2B"));
		String description = totalRaw.substring(0, totalRaw.indexOf("=="));
		main.append(description);
		main.append("==Pokémon==");
		String[] times = new String[] { "Dusk", "Day", "Night" };
		String[] locations = new String[] { "Land", "Water", "Air", "Underground" };
		String biomeData = biome;
		switch (biome) {
		case "Birch Forest Hills M":
			biomeData = "BirchForest Hills M";
			break;
		case "Desert Hills":
			biomeData = "DesertHills";
			break;
		case "Forest Hills":
			biomeData = "ForestHills";
			break;
		case "Frozen Ocean":
			biomeData = "FrozenOcean";
			break;
		case "Frozen River":
			biomeData = "FrozenRiver";
			break;
		case "Jungle Edge":
			biomeData = "JungleEdge";
			break;
		case "Jungle Edge M":
			biomeData = "JungleEdge M";
			break;
		case "Jungle Hills":
			biomeData = "JungleHills";
			break;
		case "Mushroom Island":
			biomeData = "MushroomIsland";
			break;
		case "Mushroom Island Shore":
			biomeData = "MushroomIslandShore";
			break;
		case "Taiga Hills":
			biomeData = "TaigaHills";
			break;
		}
		Set<String> availablePokemon = EnumPokemon.getAllPokemon();
		for (String time : times) {
			String timeHeader = time;
			String timeTable = time;
			if (time.equals("Dusk")) {
				timeHeader = "Dawn/Dusk";
				timeTable = "DUSKDAWN";
			}
			timeTable = "MULTIPLIER" + timeTable;
			main.append("\n===");
			main.append(timeHeader);
			main.append("===");
			for (String location : locations) {
				String locationData = location.toLowerCase();
				String timeLocation = time + location;
				if (locationData.equals("air")) {
					locationData = "airpersistent";
				} else if (locationData.equals("land")) {
					locationData = "land' OR d.LOCATION = 'air";
				}
				boolean hasPokemon = false;
				StringBuilder query = new StringBuilder();
				query.append(
						"SELECT b.PIXELMONNAME, b.RARITY, b.FORM, b.MINSPAWNLEVEL, b.MAXSPAWNLEVEL, e.NAME AS TYPE1, f.NAME AS TYPE2, c.");
				query.append(timeTable);
				query.append(" FROM PIXELMONSPAWNBIOMES a ");
				query.append("JOIN PIXELMON b ON a.PIXELMONID = b.PIXELMONID ");
				query.append("JOIN PIXELMONSPAWNTIMES c ON b.SPAWNTIMEID = c.SPAWNTIMEID ");
				query.append("JOIN PIXELMONSPAWNLOCATIONS d ON b.PIXELMONID = d.PIXELMONID ");
				query.append("JOIN TYPES e ON b.PIXELMONTYPE1ID = e.TYPEID ");
				query.append("LEFT JOIN TYPES f ON b.PIXELMONTYPE2ID = f.TYPEID ");
				query.append("WHERE a.BIOMENAME = '");
				query.append(biomeData);
				query.append("' AND (d.LOCATION = '");
				query.append(locationData);
				query.append("') AND c.");
				query.append(timeTable);
				query.append(" > 0 ");
				query.append("AND b.RARITY IS NOT NULL ");
				query.append("ORDER BY b.PIXELMONNAME, b.FORM");
				ResultSet result = database.executeQuery(query);
				int totalRarity = 0;
				try {
					while (result.next()) {
						String pokemon = result.getString("PIXELMONNAME");
						if (availablePokemon.contains(pokemon)) {
							int rarity = result.getInt("RARITY");
							float multiplier = result.getFloat(timeTable);
							rarity = Math.round(rarity * multiplier);
							if (rarity == 0) {
								continue;
							}
							if (!hasPokemon) {
								hasPokemon = true;
								main.append("\n====");
								main.append(location);
								main.append("====\n{{BiomeRarityH}}");

								template.append('|');
								template.append(timeLocation);
								template.append('=');
							}
							main.append("\n{{BiomeRarity|");
							main.append(timeLocation);
							main.append('|');
							main.append(StringUtil.getFormName(pokemon, result.getInt("FORM")));
							main.append('|');
							main.append(rarity);
							main.append('|');
							int minSpawnLevel = result.getInt("MINSPAWNLEVEL");
							main.append(minSpawnLevel);
							int maxSpawnLevel = result.getInt("MAXSPAWNLEVEL") - 1;
							if (maxSpawnLevel > minSpawnLevel) {
								main.append('-');
								main.append(maxSpawnLevel);
							}
							main.append('|');
							String type1 = result.getString("TYPE1");
							String type2 = result.getString("TYPE2");
							main.append(type1);
							if (type2 != null && !type2.equals(type1)) {
								main.append('|');
								main.append(type2);
							}
							main.append("}}");

							if (rarity > 0) {
								totalRarity += rarity;
							}
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				if (hasPokemon) {
					main.append("\n|}");
					template.append(totalRarity);
				}
			}
		}

		template.append("}}");
		builder.append(template);
		builder.append("\n");
		builder.append(main);

		preserveSection("===Other===");

		preserveSection("==[[NPCs]]==");
		int currentSize = builder.length();
		if (preserveSection("==[[Trainers]]==")) {
			int trainerIndex = builder.indexOf("Trainers", currentSize);
			builder.replace(trainerIndex, trainerIndex + 8, "NPCs");
		}
		preserveSection("==Other==\n");

		return builder.toString();
	}

	/**
	 * Gets wikicode for a type article.
	 * @param type The type to get an article for.
	 * @return Wikicode for the specified type article.
	 */
	public String getTypeArticle(String type) {
		if (type.isEmpty()) {
			type = "Steel";
		}

		builder = new StringBuilder();

		String articleName = type;
		if ("Psychic".equals(type)) {
			articleName += " (type)";
		}

		totalRaw = APIConnection.getArticleSourcePixelmon(articleName);
		int indexTOC = builder.indexOf("__NOTOC__\n");
		if (indexTOC == -1) {
			indexTOC = 0;
		}
		indexTOC += 10;
		int headerIndex = totalRaw.indexOf("==");
		if (headerIndex > -1) {
			String description = totalRaw.substring(indexTOC, headerIndex);
			builder.append(description);
			int index17 = builder.indexOf("17");
			if (index17 == -1) {
				index17 = builder.indexOf("seventeen");
				builder.replace(index17, index17 + "seventeen".length(), "18");
			} else {
				builder.replace(index17, index17 + 2, "18");
			}
		} else {
			builder.append("The ");
			builder.append(type);
			builder.append(" type is one of the 18 Pokémon [[types]].");
		}

		builder.append("\n==Type effectiveness==\n===Offensive===\n{{Type effectiveness");
		EnumType enumType = EnumType.parseType(type);
		for (EnumType otherType : EnumType.values()) {
			float effectiveness = EnumType.getEffectiveness(enumType, otherType);
			if (effectiveness != Effectiveness.Normal.value) {
				builder.append("\n|");
				builder.append(otherType.name().toLowerCase());
				builder.append('=');
				builder.append(effectiveness);
			}
		}
		builder.append("\n}}\n===Defensive===\n{{Type effectiveness");
		for (EnumType otherType : EnumType.values()) {
			float effectiveness = EnumType.getEffectiveness(otherType, enumType);
			if (effectiveness != Effectiveness.Normal.value) {
				builder.append("\n|");
				builder.append(otherType.name().toLowerCase());
				builder.append('=');
				builder.append(effectiveness);
			}
		}

		builder.append("\n}}\n==Pokémon==\n{{moveh}}");
		StringBuilder query = new StringBuilder();
		query.append(
				"SELECT a.PIXELMONNAME, a.NATIONALPOKEDEXNUMBER, b.NAME AS TYPE1, c.NAME AS TYPE2, a.FORM FROM PIXELMON a ");
		query.append("JOIN TYPES b ON a.PIXELMONTYPE1ID = b.TYPEID ");
		query.append("LEFT JOIN TYPES c ON a.PIXELMONTYPE2ID = c.TYPEID ");
		query.append("WHERE b.NAME = '");
		query.append(type);
		query.append("' OR c.NAME = '");
		query.append(type);
		query.append("' ORDER BY a.NATIONALPOKEDEXNUMBER, a.FORM");
		ResultSet result = database.executeQuery(query);
		Set<String> availablePokemon = EnumPokemon.getAllPokemon();
		try {
			while (result.next()) {
				String pokemon = result.getString("PIXELMONNAME");
				if (availablePokemon.contains(pokemon)) {
					String formName = StringUtil.getFormName(pokemon, result.getInt("FORM"));
					if (formName.isEmpty()) {
						formName = pokemon;
					}
					builder.append("\n{{moveentry|");
					builder.append(formName);
					builder.append('|');
					builder.append(result.getString("TYPE1"));
					String type2 = result.getString("TYPE2");
					if (type2 != null) {
						builder.append('|');
						builder.append(type2);
					}
					builder.append("}}");
				}
			}

			builder.append("\n{{movef}}\n==Moves==\n{{typemoveh|");
			builder.append(type);
			builder.append("}}");
			query = new StringBuilder(
					"SELECT b.NAME AS MOVE, c.NAME AS TYPE, d.NAME AS CATEGORY, b.POWER, b.ACCURACY, b.PP FROM MOVES b ");
			query.append("JOIN TYPES c ON b.TYPEID = c.TYPEID ");
			query.append("JOIN MOVECATEGORIES d ON b.MOVECATEGORYID = d.MOVECATEGORYID ");
			query.append("WHERE c.NAME = '");
			query.append(type);
			query.append("' ORDER BY b.NAME");
			result = database.executeQuery(query);
			Set<String> unavailable = new HashSet<>();
			unavailable.addAll(Arrays.asList("Blue Flare", "Bolt Strike", "Dark Void", "Freeze Shock", "Fusion Bolt",
					"Fusion Flare", "Glaciate", "Head Charge", "Heart Swap", "Horn Leech", "Ice Burn", "Judgment",
					"Lunar Dance", "Magma Storm", "Relic Song", "Roar of Time", "Sacred Sword", "Searing Shot",
					"Secret Sword", "Seed Flare", "Shadow Force", "Simple Beam", "Spacial Rend", "Tail Glow",
					"Tail Slap", "Techno Blast", "Aromatic Mist", "Crafty Shield", "Electrify", "Fairy Lock",
					"Flower Shield", "Flying Press", "Forest's Curse", "Geomancy", "King's Shield", "Land's Wrath",
					"Noble Roar", "Oblivion Wing", "Parabolic Charge", "Parting Shot", "Powder", "Topsy-Turvy",
					"Trick-or-Treat", "Diamond Storm", "Hyperspace Hole", "Hyperspace Fury", "Steam Eruption",
					"Thousand Arrows", "Thousand Waves", "Light of Ruin"));
			while (result.next()) {
				String moveName = result.getString("MOVE");
				moveName = Move.translate(moveName);
				if (!unavailable.contains(moveName)) {
					builder.append("\n{{typemove|");
					builder.append(moveName);
					builder.append('|');
					String category = result.getString("CATEGORY");
					builder.append(category);
					builder.append('|');
					int power = result.getInt("POWER");
					builder.append(power == 0 ? "&mdash;" : power);
					builder.append('|');
					int accuracy = result.getInt("ACCURACY");
					builder.append(accuracy == 0 ? "&mdash;" : accuracy);
					builder.append('|');
					builder.append(result.getInt("PP"));
					builder.append("}}");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		builder.append("\n{{typemovef|");
		builder.append(type);
		builder.append("}}");

		return builder.toString();
	}

	/**
	 * Creates wikicode for an Ability article.
	 * @param ability The Ability to create an article for.
	 * @return The Ability article wikicode.
	 */
	public String getAbilityArticle(String ability) {
		if (ability.isEmpty()) {
			ability = "Wonder Skin";
		}

		builder = new StringBuilder();

		String abilityArticle = ability;
		if (abilityArticle.equals("Healer")) {
			abilityArticle += " (Ability)";
		}
		totalRaw = APIConnection.getArticleSourcePixelmon(abilityArticle);
		int headerIndex = totalRaw.indexOf("==");
		if (headerIndex == -1) {
			builder.append('\n');
		} else {
			String description = totalRaw.substring(0, headerIndex);
			builder.append(description);
		}

		builder.append("==Pokémon==\n{{abilityh}}");

		String databaseAbility = ability.replace(" ", "");
		if (StringUtil.equalsAny(databaseAbility, "BattleArmor", "ShellArmor")) {
			databaseAbility = databaseAbility.replaceAll("Armor", "Armour");
		}
		StringBuilder query = new StringBuilder();
		query.append(
				"SELECT a.PIXELMONNAME, a.NATIONALPOKEDEXNUMBER, b.NAME AS ABILITY1, c.NAME AS ABILITY2, d.NAME AS HIDDENABILITY, e.NAME AS TYPE1, f.NAME AS TYPE2, a.FORM FROM PIXELMON a ");
		query.append("JOIN ABILITIES b ON a.ABILITY1ID = b.ABILITYID ");
		query.append("LEFT JOIN ABILITIES c ON a.ABILITY2ID = c.ABILITYID ");
		query.append("LEFT JOIN ABILITIES d ON a.ABILITYHIDDENID = d.ABILITYID ");
		query.append("JOIN TYPES e ON a.PIXELMONTYPE1ID = e.TYPEID ");
		query.append("LEFT JOIN TYPES f ON a.PIXELMONTYPE2ID = f.TYPEID ");
		query.append("WHERE b.NAME = '");
		query.append(databaseAbility);
		query.append("' OR c.NAME = '");
		query.append(databaseAbility);
		query.append("' OR d.NAME = '");
		query.append(databaseAbility);
		query.append("' ORDER BY a.NATIONALPOKEDEXNUMBER, a.FORM");
		ResultSet result = database.executeQuery(query);
		Set<String> availablePokemon = EnumPokemon.getAllPokemon();
		try {
			while (result.next()) {
				String pokemon = result.getString("PIXELMONNAME");
				if (availablePokemon.contains(pokemon)) {
					String formName = StringUtil.getFormName(pokemon, result.getInt("FORM"));
					if (!formName.isEmpty()) {
						builder.append("\n{{abilityentry|");
						builder.append(formName);
						builder.append('|');
						builder.append(result.getString("TYPE1"));
						String type2 = result.getString("TYPE2");
						if (type2 != null) {
							builder.append('|');
							builder.append(type2);
						}
						builder.append("|a1=");
						String ability1 = result.getString("ABILITY1");
						builder.append(StringUtil.translateAbility(ability1));
						String ability2 = result.getString("ABILITY2");
						if (ability2 != null && !ability2.equals(ability1)) {
							builder.append("|a2=");
							builder.append(StringUtil.translateAbility(ability2));
						}
						String hiddenAbility = result.getString("HIDDENABILITY");
						if (hiddenAbility != null && !ability1.equals(hiddenAbility)) {
							builder.append("|ha=");
							builder.append(StringUtil.translateAbility(hiddenAbility));
						}
						builder.append("}}");
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		builder.append("\n{{abilityf}}");

		return builder.toString();
	}

	/**
	 * Gets wikicode for an Egg Group article.
	 * @param eggGroup The Egg Group to get an Article for.
	 * @return Wikicode for the Egg Group article.
	 */
	public String getEggGroupArticle(String eggGroup) {
		if (eggGroup.isEmpty()) {
			eggGroup = "Water 3";
		}

		builder = new StringBuilder();

		builder.append(eggGroup);
		builder.append(" is one of the fifteen [[Egg Groups]].\n==Pokémon==\n{{egggrouph}}");

		String databaseEggGroup = eggGroup.replace(" ", "").replace("-", "");

		StringBuilder query = new StringBuilder();
		query.append(
				"SELECT a.PIXELMONNAME, a.NATIONALPOKEDEXNUMBER, b.NAME AS EGGGROUP1, c.NAME AS EGGGROUP2, a.FORM FROM PIXELMON a ");
		query.append("JOIN EGGGROUPS b ON a.EGGGROUP1ID = b.EGGGROUPID ");
		query.append("LEFT JOIN EGGGROUPS c ON a.EGGGROUP2ID = c.EGGGROUPID ");
		query.append("WHERE b.NAME = '");
		query.append(databaseEggGroup);
		query.append("' OR c.NAME = '");
		query.append(databaseEggGroup);
		query.append("' ORDER BY a.NATIONALPOKEDEXNUMBER, a.FORM");
		ResultSet result = database.executeQuery(query);
		Set<String> availablePokemon = EnumPokemon.getAllPokemon();
		try {
			while (result.next()) {
				String pokemon = result.getString("PIXELMONNAME");
				if (availablePokemon.contains(pokemon) && result.getInt("FORM") == 0) {
					builder.append("\n{{egggroupentry|");
					builder.append(Pokemon.getTranslatedName(pokemon));
					builder.append('|');
					builder.append(translateEggGroup(result.getString("EGGGROUP1")));
					String eggGroup2 = result.getString("EGGGROUP2");
					if (eggGroup2 != null) {
						builder.append('|');
						builder.append(translateEggGroup(eggGroup2));
					}
					builder.append("}}");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		builder.append("\n{{egggroupf}}");

		return builder.toString();
	}

	/**
	 * Appends a wiki table field to an existing table.
	 * @param fieldName The name of the table field.
	 * @param fieldParameter The parameter to pass into the table.
	 */
	private void appendTableField(String fieldName, Object fieldParameter) {
		StringUtil.appendTableField(builder, fieldName, fieldParameter);
	}

	/**
	 * Formats a Pokédex number to always be three digits long.
	 * @param dexNumber The Pokédex number to format.
	 * @return The formatted Pokédex number.
	 */
	private String formatDexNumber(int dexNumber) {
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
	 * Accounts for parentheses in egg group article names.
	 * @param eggGroup The Egg group to convert.
	 * @return The appropriate Egg group name for the wiki.
	 */
	private String convertEggName(String eggGroup) {
		if (StringUtil.equalsAny(eggGroup, "Bug", "Ditto", "Dragon", "Fairy", "Flying", "Grass")) {
			return eggGroup + " (Egg Group)|" + eggGroup;
		}
		return translateEggGroup(eggGroup);
	}

	/**
	 * Appends a page link to a string builder.
	 * @param stringBuilder The string builder to append to.
	 * @param page The page to link to.
	 */
	private void appendLink(StringBuilder stringBuilder, String page) {
		stringBuilder.append("[[");
		stringBuilder.append(page);
		if (page.equals("Psychic")) {
			stringBuilder.append(" (type)|Psychic");
		} else if (page.equals("Healer")) {
			stringBuilder.append(" (Ability)|Healer");
		}
		stringBuilder.append("]]");
	}

	/**
	 * Keeps a section of wikicode that was in the original article.
	 * @param sectionName The name of the section.
	 */
	private boolean preserveSection(String sectionName) {
		int startIndex = totalRaw.indexOf(sectionName);
		if (startIndex >= 0) {
			int endIndex = totalRaw.indexOf("==", startIndex + sectionName.length());
			if (endIndex == -1) {
				endIndex = totalRaw.length();
			}
			builder.append('\n');
			builder.append(totalRaw.substring(startIndex, endIndex).trim());
			return true;
		}
		return false;
	}

	/**
	 * Gets the database IDs of pre-evolutions of a Pokémon.
	 * @param databaseID The database ID of the Pokémon to get pre-evolutions for.
	 * @param name The name of the Pokémon to get pre-evolutions for.
	 * @return The database IDs of pre-evolutions of a Pokémon.
	 */
	private List<Integer> getPreEvolutions(int databaseID) {
		List<Integer> preEvolutions = new ArrayList<>();

		try {
			ResultSet result = database
					.executeQuery("SELECT PIXELMONNAME FROM PIXELMON WHERE PIXELMONID = " + databaseID);
			result.next();
			String name = result.getString(1);
			StringBuilder query = new StringBuilder("SELECT PIXELMONFROMID FROM PIXELMONEVOLUTIONS ");
			query.append("WHERE PIXELMONTOID = ");
			query.append(databaseID);
			query.append(" OR EVOLVECONDITION LIKE '%");
			query.append(name);
			query.append("' AND EVOLVECONDITION LIKE '%:%'");
			result = database.executeQuery(query);
			int preEvoID = -1;
			if (result.next()) {
				preEvoID = result.getInt("PIXELMONFROMID");
			}
			if (preEvoID == -1) {
				String preEvoName = null;
				if (StringUtil.equalsAny(name, "Hitmonchan", "Hitmonlee", "Hitmontop")) {
					preEvoName = "Tyrogue";
				} else if (name.equals("Mantine")) {
					preEvoName = "Mantyke";
				} else if (StringUtil.equalsAny(name, "Wormadam", "Mothim")) {
					preEvoName = "Burmy";
				} else if (name.equals("Shedinja")) {
					preEvoName = "Nincada";
				}
				if (preEvoName != null) {
					result = database
							.executeQuery("SELECT PIXELMONID FROM PIXELMON WHERE PIXELMONNAME = '" + preEvoName + "'");
					result.next();
					preEvoID = result.getInt(1);
				}
			}
			if (preEvoID != -1) {
				preEvolutions.add(preEvoID);
				preEvolutions.addAll(getPreEvolutions(preEvoID));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return preEvolutions;
	}

	/**
	 * Gets the database IDs of evolutions of a Pokémon.
	 * @param databaseID The database ID of the Pokémon to get evolutions for.
	 * @param name The name of the Pokémon to get evolutions for.
	 * @return The database IDs of evolutions of a Pokémon.
	 */
	private List<Integer> getEvolutions(int databaseID) {
		List<Integer> evolutions = new ArrayList<>();

		Statement statement = null;
		ResultSet evoResult = null;
		Statement newStatement = null;
		ResultSet newResult = null;
		try {
			StringBuilder query = new StringBuilder("SELECT PIXELMONTOID, EVOLVECONDITION FROM PIXELMONEVOLUTIONS ");
			query.append("WHERE PIXELMONFROMID = ");
			query.append(databaseID);
			statement = database.createNewStatement();
			evoResult = database.executeQuery(query, statement);
			while (evoResult.next()) {
				int evoID = evoResult.getInt("PIXELMONTOID");
				if (evoID == 0) {
					List<String> evoNames = new ArrayList<>();
					String evoName = evoResult.getString("EVOLVECONDITION");
					int colonIndex = evoName.lastIndexOf(':');
					if (colonIndex > -1) {
						evoNames.add(evoName.substring(colonIndex + 1));
						if (evoName.contains("Silcoon")) {
							evoNames.add("Cascoon");
							evoNames.add("Beautifly");
							evoNames.add("Dustox");
						} else if (evoName.equals("Nincada")) {
							evoNames.add("Shedinja");
						}
					} else if (evoName.equals("Burmy")) {
						evoNames.add("Wormadam");
						evoNames.add("Mothim");
					} else if (evoName.equals("Tyrogue")) {
						evoNames.add("Hitmonlee");
						evoNames.add("Hitmonchan");
						evoNames.add("Hitmontop");
					} else if (evoName.equals("Mantyke")) {
						evoNames.add("Mantine");
					}
					if (!evoNames.isEmpty()) {
						for (String evo : evoNames) {
							newStatement = database.createNewStatement();
							newResult = database.executeQuery(
									"SELECT PIXELMONID FROM PIXELMON WHERE PIXELMONNAME = '" + evo + "'", newStatement);
							if (newResult.next()) {
								int newEvoID = newResult.getInt(1);
								evolutions.add(newEvoID);
								evolutions.addAll(getEvolutions(newEvoID));
							}
							database.closeStatement(newStatement, newResult);
						}
					}
				}
				if (evoID != 0 && !evolutions.contains(evoID)) {
					evolutions.add(evoID);
					if (databaseID == 396) {
						// Nincada->Shedinja
						evolutions.add(398);
					}
					evolutions.addAll(getEvolutions(evoID));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			database.closeStatement(statement, evoResult);
			database.closeStatement(newStatement, newResult);
		}
		return evolutions;
	}

	/**
	 * Converts an Egg Group to its English name.
	 * @param eggGroup The database name of the Egg Group.
	 * @return The English name of the Egg Group.
	 */
	private String translateEggGroup(String eggGroup) {
		switch (eggGroup) {
		case "HumanLike":
			return "Human-Like";
		case "Water1":
			return "Water 1";
		case "Water2":
			return "Water 2";
		case "Water3":
			return "Water 3";
		default:
			return eggGroup;
		}
	}
}
