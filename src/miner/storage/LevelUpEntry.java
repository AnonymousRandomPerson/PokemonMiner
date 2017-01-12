package miner.storage;

import java.util.ArrayList;
import java.util.List;

/**
 * An entry in a move's level-up learnset.
 */
public class LevelUpEntry {

	/** The Pokémon learning the move. */
	public Pokemon pokemon;
	/** The levels where the Pokémon learns the move at. */
	public List<Integer> levels = new ArrayList<>();
	
	/**
	 * Initializes an entry.
	 * @param pokemon The Pokémon learning the move.
	 */
	public LevelUpEntry(String pokemon) {
		this.pokemon = new Pokemon(pokemon);
	}
	
	/**
	 * Adds a learn level to the entry.
	 * @param level The learn level to add.
	 */
	public void addLevel(int level) {
		if (level == 0) {
			level = 1;
		}
		levels.add(level);
	}
}
