package miner.wiki;

import miner.Miner;
import miner.storage.Moveset;
import util.FileIO;
import util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.*;

/**
 * Creates wiki tables for Gym Pokémon.
 */
public class GymMiner extends Miner {

	/**
	 * Gets wiki tables for Gym Pokémon.
	 * @param gym The Gym to get tables for.
	 * @return Wiki tables for the specified Gym's Pokémon.
	 */
	public String getGymData(String gym) {
		builder = new StringBuilder();
		
		if (gym == "") {
			gym = "watergym";
		}
		
		database.getLangMap();
		
		String path = "gyms/" + gym + ".json";
		JSONObject json = FileIO.getJSONFromFile(path);
		JSONArray pokemonArray = json.getJSONArray("pokemon");
		List<Moveset> movesets = new ArrayList<>();
		
		List<String> types = new ArrayList<String>();
		JSONArray typeArray = json.getJSONArray("type");
		for (Object typeObject : typeArray) {
			types.add(typeObject.toString());
		}
		String singleType = null;
		if (types.size() == 1) {
			singleType = types.get(0);
		}
		
		for (Object pokemonObject : pokemonArray) {
			JSONObject pokemon = (JSONObject) pokemonObject;
			JSONArray sets = pokemon.getJSONArray("sets");
			for (Object setObject : sets) {
				JSONObject set = (JSONObject) setObject;
				Moveset moveset = new Moveset(pokemon, set);
				movesets.add(moveset);
			}
		}
		
		Collections.sort(movesets);
		
		builder.append("\n==Pokémon==\n");
		builder.append("<spoiler text=\"Expand\">");
		builder.append("Note: [[Natures]] and [[EVs]] are only set for the Gym Leader's Pokémon. Normal [[NPC Trainer]] Pokémon have random Natures and 0 EVs.\n");
		int[] levels = new int[] { 15, 25, 35, 50, 100 };
		
		for (int level : levels) {
			if (level == 100) {
				builder.append("===Equal Level===\n");
			} else {
				builder.append("===Level ");
				builder.append(Integer.toString(level));
				builder.append("===\n");
			}
			
			for (Moveset moveset : movesets) {
				if (!moveset.inRange(level)) {
					continue;
				}
				
				builder.append("{{GymPokemonInfobox");
				
				String type = null;
				if (singleType == null) {
					for (String pokemonType: moveset.types) {
						if (types.contains(pokemonType)) {
							type = pokemonType;
						}
					}
					if (type == null) {
						type = types.get(0);
					}
				} else {
					type = singleType;
				}
				StringUtil.appendTableField(builder, "type", type);
				StringUtil.appendTableField(builder, "name", moveset.name);
				StringBuilder moveBuilder = new StringBuilder();
				boolean firstSlot = true;
				for (List<String> moveslot : moveset.moves) {
					if (!firstSlot && !moveslot.isEmpty()) {
						moveBuilder.append("{{-}}\n");
					}
					firstSlot = false;
					boolean firstMove = true;
					for (String move : moveslot) {
						if (!firstMove) {
							moveBuilder.append('/');
						}
						moveBuilder.append("[[");
						if (move.startsWith("Hidden Power")) {
							moveBuilder.append("Hidden Power|");
						}
						moveBuilder.append(move);
						moveBuilder.append("]]");
						firstMove = false;
					}
				}
				StringUtil.appendTableField(builder, "moves", moveBuilder.toString());
				StringUtil.appendTableField(builder, "ability", StringUtil.encloseStrings(moveset.abilities, "[[", "/", "]]"));
				StringUtil.appendTableField(builder, "helditem", StringUtil.encloseStrings(moveset.heldItems, "{{i|", "/", "}}"));
				StringUtil.appendTableField(builder, "nature", StringUtil.separateStrings(moveset.natures, "/"));
				StringUtil.appendTableField(builder, "evs", moveset.evString);
				
				builder.append("\n}}\n");
			}
		}
		builder.append("</spoiler>");
		builder.append("\n<div style=\"clear:left;\"></div>");
		
		return builder.toString();
	}
}
