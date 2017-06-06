package miner.storage;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track of Pokémon that can inherit an Egg move from other Pokémon.
 */
public class EggParents {
	
	/** The Pokémon inheriting the Egg move. */
	public List<Pokemon> family;
	/** The Pokémon that do not learn the Egg move through other means. */
	public List<String> childNames;
	/** The Pokémon passing down the Egg move. */
	public List<Pokemon> parents;
	/** The Pokémon to check for Egg Groups with. */
	public Pokemon checkPokemon;
	/** Whether the Pokémon needs to chain breed for the move. */
	public boolean chainBreed;
	/** The Egg Groups that the family is a part of. */
	public List<Integer> eggGroupIDs = new ArrayList<>();

	/**
	 * Initializes an Egg parent tracker.
	 * @param family The Pokémon inheriting the Egg move.
	 * @param childNames The Pokémon that do not learn the Egg move through other means.
	 * @param parents The Pokémon passing down the Egg move.
	 * @param checkPokemon The Pokémon to check for Egg Groups with.
	 */
	public EggParents(List<Pokemon> family, List<String> childNames, List<Pokemon> parents, Pokemon checkPokemon) {
		this.family = family;
		this.childNames = childNames;
		this.parents = parents;
		this.checkPokemon = checkPokemon;
		for (int eggGroup : new int[] { checkPokemon.eggGroup1ID,
				checkPokemon.eggGroup2ID }) {
			if (eggGroup > -1) {
				eggGroupIDs.add(eggGroup);
			}
		}
	}
	
	/**
	 * Checks if the container contains a certain Pokémon in its family.
	 * @param pokemon The Pokémon to look for.
	 * @return Whether the container contains the specified Pokémon in its family.
	 */
	public boolean containsPokemon(String pokemon) {
		for (Pokemon p : family) {
			if (p.name.equals(pokemon)) {
				return true;
			}
		}
		return false;
	}
}
