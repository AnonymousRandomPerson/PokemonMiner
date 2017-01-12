package miner.storage;

import java.util.Comparator;

/**
 * Compares Pokémon by Pokédex number.
 */
public class DexNumberComparator implements Comparator<Pokemon> {

	@Override
	public int compare(Pokemon p1, Pokemon p2) {
		return p1.nationalPokedexNumber - p2.nationalPokedexNumber;
	}
}
