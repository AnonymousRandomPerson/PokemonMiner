package miner.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import database.Database;
import util.StringUtil;

/**
 * A Gym moveset for a Pokémon.
 */
public class Moveset implements Comparable<Moveset> {

	/** The name of the Pokémon. */
	public String name;
	/** The types of the Pokémon. */
	public List<String> types = new ArrayList<>();
	/** The minimum level the Pokémon can be. */
	public int minLevel = 1;
	/** The maximum level the Pokémon can be. */
	public int maxLevel = 100;
	/** The possible moves the Pokémon can have. */
	public List<List<String>> moves = new ArrayList<List<String>>(4);
	
	public List<String> abilities = new ArrayList<>();
	/** The possible Natures the Pokémon can have. */
	public List<String> natures = new ArrayList<>();
	/** The possible held items the Pokémon can have. */
	public List<String> heldItems = new ArrayList<>();
	/** A string representing the EVs that the Pokémon can have. */
	public String evString;
	
	/**
	 * Initializes a moveset.
	 * @param pokemon The Pokémon JSON object to initialize the moveset from.
	 * @param set The moveset JSON object to initialize the moveset from.
	 */
	public Moveset(JSONObject pokemon, JSONObject set) {
		String unlocalizedName = pokemon.getString("name");
		int form = getOrDefaultInt(set, "form", -1);
		name = StringUtil.getFormName(unlocalizedName, form);
		
		minLevel = getOrDefaultInt(pokemon, "minLevel", minLevel);
		minLevel = getOrDefaultInt(set, "minLevel", minLevel);
		maxLevel = getOrDefaultInt(pokemon, "maxLevel", maxLevel);
		for (int i = 0; i < 4; i++) {
			List<String> moveslot = getStringList(set, "move" + (i + 1));
			for (int j = 0; j < moveslot.size(); j++) {
				String move = moveslot.get(j);
				String translatedMove = Move.translate(move);
				if (translatedMove == null) {
					System.out.println("Move not found: " + move);
				}
				moveslot.set(j, translatedMove);
			}
			Collections.sort(moveslot);
			moves.add(moveslot);
		}
		abilities = getStringList(set, "ability");
		natures = getSortedStringList(set, "nature");
		heldItems = getStringList(set, "heldItem");
		
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT ");
			StringUtil.addCommaSeparated(query, false, "b.NAME AS ABILITY1NAME", "c.NAME AS ABILITY2NAME",
			"d.NAME AS ABILITYHIDDENNAME", "e.NAME AS PIXELMONTYPE1NAME",
			"f.NAME AS PIXELMONTYPE2NAME");
			query.append(" FROM PIXELMON a ");
			query.append("LEFT JOIN ABILITIES b ON a.ABILITY1ID = b.ABILITYID ");
			query.append("LEFT JOIN ABILITIES c ON a.ABILITY2ID = c.ABILITYID ");
			query.append("LEFT JOIN ABILITIES d ON a.ABILITYHIDDENID = d.ABILITYID ");
			query.append("LEFT JOIN TYPES e ON a.PIXELMONTYPE1ID = e.TYPEID ");
			query.append("LEFT JOIN TYPES f ON a.PIXELMONTYPE2ID = f.TYPEID ");
			query.append("WHERE a.PIXELMONNAME = '");
			query.append(unlocalizedName);
			query.append("'");
			if (form != -1) {
				query.append(" AND FORM = ");
				query.append(form);
			}
			ResultSet result = Database.getDatabase().executeQuery(query);
			
			if (result.next()) {
				types.add(result.getString("PIXELMONTYPE1NAME"));
				String type2 = result.getString("PIXELMONTYPE2NAME");
				if (type2 != null) {
					types.add(type2);
				}
	
				if (abilities.isEmpty()) {
					String[] abilityRows = new String[] { "ABILITY1NAME", "ABILITY2NAME", "ABILITYHIDDENNAME" };
					for (String row : abilityRows) {
						String ability = result.getString(row);
						if (ability != null) {
							abilities.add(ability);
						}
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < abilities.size(); i++) {
			String ability = StringUtil.translateAbility(abilities.get(i));
			if (ability.equals("Healer")) {
				ability = "Healer (Ability)|Healer";
			}
			abilities.set(i, ability);
		}
		Collections.sort(abilities);
		
		if (natures.isEmpty()) {
			natures.add("Random");
		}

		for (int i = 0; i < heldItems.size(); i++) {
			String heldItem = StringUtil.translateHeldItem(heldItems.get(i));
			if (heldItem.equals("Stick")) {
				heldItem = "Stick||Stick (held item)|image=Stick2";
			}
			heldItems.set(i, heldItem);
		}
		Collections.sort(heldItems);
		
		String[] evKeys = new String[] { "HP", "Atk", "Def", "SpAtk", "SpDef", "Speed" };
		String[] evNames = new String[] { "HP", "Attack", "Defense", "Special Attack", "Special Defense", "Speed" };
		StringBuilder evBuilder = new StringBuilder();
		boolean first = true;
		for (int i = 0; i < evKeys.length; i++) {
			String evKey = "ev" + evKeys[i];
			if (set.has(evKey)) {
				if (!first) {
					evBuilder.append(", ");
				}
				evBuilder.append(set.getInt(evKey));
				evBuilder.append(' ');
				evBuilder.append(evNames[i]);
				first = false;
			}
		}
		evString = evBuilder.toString();
	}
	
	/**
	 * Gets an int from a JSON key. Returns a default value if the key doesn't exist.
	 * @param json The JSON object to get a value from.
	 * @param key The key of the value.
	 * @param fallback The default value to return if the key doesn't exist.
	 * @return The int corresponding to the specified key, or the default value if the key doesn't exist.
	 */
	private int getOrDefaultInt(JSONObject json, String key, int fallback) {
		if (json.has(key)) {
			return json.getInt(key);
		}
		return fallback;
	}
	
	/**
	 * Gets a list of strings from a JSON key.
	 * @param json The JSON object to get strings from.
	 * @param key The key to get strings from.
	 * @return A list of strings from the specified key, or an empty list if the key doesn't exist.
	 */
	private List<String> getStringList(JSONObject json, String key) {
		List<String> list = new ArrayList<>();
		if (json.has(key)) {
			JSONArray stringArray = json.getJSONArray(key);
			
			for (Object string : stringArray) {
				list.add((String) string);
			}
		}
		return list;
	}

	/**
	 * Gets a sorted list of strings from a JSON key.
	 * @param json The JSON object to get strings from.
	 * @param key The key to get strings from.
	 * @return A sorted list of strings from the specified key, or an empty list if the key doesn't exist.
	 */
	private List<String> getSortedStringList(JSONObject json, String key) {
		List<String> list = getStringList(json, key);
		Collections.sort(list);
		return list;
	}
	
	/**
	 * Checks if the moveset is in range of a certain level.
	 * @param level The level to check range for.
	 * @return Whether the moveset is in range of the specified level.
	 */
	public boolean inRange(int level) {
		return level >= minLevel && level <= maxLevel;
	}
	
	@Override
	public int compareTo(Moveset other) {
		int stringCompare = name.compareTo(other.name);
		if (stringCompare != 0) {
			return stringCompare;
		}
		return minLevel - other.minLevel;
	}
}
