package miner.storage;

import java.util.Comparator;

/**
 * Compares drop entries by Pokémon name.
 */
public class DropComparatorPokemon implements Comparator<DropEntry> {

	@Override
	public int compare(DropEntry o1, DropEntry o2) {
		return o1.pokemon.compareTo(o2.pokemon);
	}

}
