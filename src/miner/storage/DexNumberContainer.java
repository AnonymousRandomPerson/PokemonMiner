package miner.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.Database;
import pixelmon.EnumPokemon;

/**
 * Holds information about Pokédex numbers of available Pokémon.
 */
public class DexNumberContainer {

	/** Maps Pokédex numbers to Pokémon. */
	private Map<Integer, Pokemon> dexMap;
	/** Maps Pokémon names to Pokédex numbers. */
	private Map<String, Integer> nameMap;
	/** A list of available Pokémon. */
	public List<Pokemon> dexList;
	
	/**
	 * Initializes the container.
	 */
	public DexNumberContainer() {
		dexMap = new HashMap<>();
		nameMap = new HashMap<>();
		dexList = new ArrayList<>();
		Database database = Database.getDatabase();
		
		for (EnumPokemon pokemon : EnumPokemon.values()) {
			String name = pokemon.name.toLowerCase();
			int dexNumber = 0;
			ResultSet result = database.executeQuery("SELECT NATIONALPOKEDEXNUMBER FROM PIXELMON WHERE PIXELMONNAME = '" + name + "'");
			try {
				if (result.next()) {
					dexNumber = result.getInt(1);
				}
				result.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (dexNumber > 0) {
				Pokemon newPokemon = new Pokemon(pokemon.name);
				dexMap.put(dexNumber, newPokemon);
				nameMap.put(pokemon.name, dexNumber);
				newPokemon.nationalPokedexNumber = dexNumber;
				dexList.add(newPokemon);
			}
		}
		dexList.sort(new DexNumberComparator());
		for (int i = 0; i < dexList.size(); i++) {
			dexList.get(i).listIndex = i;
		}
	}
	
	/**
	 * Gets the Pokémon after this Pokémon.
	 * @param name The name of the original Pokémon.
	 * @return The Pokémon after the original Pokémon by dex number.
	 */
	public Pokemon getNextPokemon(String name) {
		Pokemon pokemon = getPokemonFromName(name);
		int nextIndex = pokemon.listIndex + 1;
		if (nextIndex >= dexList.size()) {
			nextIndex = 0;
		}
		return dexList.get(nextIndex);
	}
	
	/**
	 * Gets the Pokémon before this Pokémon.
	 * @param name The name of the original Pokémon.
	 * @return The Pokémon before the original Pokémon by dex number.
	 */
	public Pokemon getPreviousPokemon(String name) {
		Pokemon pokemon = getPokemonFromName(name);
		int prevIndex = pokemon.listIndex - 1;
		if (prevIndex < 0) {
			prevIndex = dexList.size() - 1;
		}
		return dexList.get(prevIndex);
	}
	
	/**
	 * Gets a Pokémon object from its name.
	 * @param name The name of the Pokémon.
	 * @return The Pokémon object with the given name.
	 */
	public Pokemon getPokemonFromName(String name) {
		return dexMap.get(nameMap.get(name)); 
	}
	
	/**
	 * Gets a Pokémon's Pokédex number from its name.
	 * @param pokemon The Pokemon to get a Pokédex number for.
	 * @return The Pokémon's Pokédex number.
	 */
	public int getDexNumber(String pokemon) {
		if (nameMap.containsKey(pokemon)) {
			return nameMap.get(pokemon);
		} else {
			System.out.println(pokemon + " not found.");
			return -1;
		}
	}
	
	/**
	 * Gets a Pokémon from its Pokédex number.
	 * @param dexNumber The Pokédex number to get a Pokémon from.
	 * @return The Pokémon corresponding to the Pokédex number.
	 */
	public Pokemon getPokemonFromNumber(int dexNumber) {
		return dexMap.get(dexNumber);
	}
	
	/**
	 * Checks if a Pokédex number corresponds to an available Pokémon.
	 * @param dexNumber The Pokédex number to check.
	 * @return Whether the Pokédex number corresponds to an available Pokémon.
	 */
	public boolean hasDexNumber(int dexNumber) {
		return dexMap.containsKey(dexNumber);
	}
}
