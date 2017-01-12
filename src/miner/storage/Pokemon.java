package miner.storage;

import database.Database;

/**
 * A Pokémon and its data.
 */
public class Pokemon {
	
	/** The name of the Pokémon. */
	public String name;
	/** The national Pokédex number of the Pokémon. */
	public int nationalPokedexNumber;
	/** The primary type of the Pokémon. */
	public String type1;
	/** The secondary type of the Pokémon. */
	public String type2;
	/** The Pokémon's first Ability. */
	public String ability1;
	/** The Pokémon's second Ability. */
	public String ability2;
	/** The Pokémon's hidden Ability. */
	public String abilityHidden;
	/** The Pokémon's first Egg Group. */
	public String eggGroup1;
	/** The Pokémon's second Egg Group. */
	public String eggGroup2;
	/** The index of the Pokémon's first Egg Group. */
	public int eggGroup1ID;
	/** The index of the Pokémon's second Egg Group. */
	public int eggGroup2ID;
	/** The number of Egg cycles that the Pokémon's Egg takes to hatch. */
	public int eggCycles;
	/** The Pokémon's height in meters. */
	public float height;
	/** The Pokémon's weight in kilograms. */
	public float weight;
	/** The chance of the Pokémon being male. */
	public int genderRatio;
	/** The Pokémon's catch rate. */
	public int catchRate;
	/** Base modifier for the Pokémon's experience yield. */
	public int expYield;
	/** The experience group that the Pokémon's is in. */
	public String expGroup;
	/** The Pokémon's base HP. */
	public int baseHP;
	/** The Pokémon's base Attack. */
	public int baseAtk;
	/** The Pokémon's base Defense. */
	public int baseDef;
	/** The Pokémon's base Special Attack. */
	public int baseSpAtk;
	/** The Pokémon's base Special Defense. */
	public int baseSpDef;
	/** The Pokémon's base Speed. */
	public int baseSpd;
	/** The Pokémon's HP EV yield. */
	public int evHP;
	/** The Pokémon's Attack EV yield. */
	public int evAtk;
	/** The Pokémon's Defense EV yield. */
	public int evDef;
	/** The Pokémon's Special Attack EV yield. */
	public int evSpAtk;
	/** The Pokémon's Special Defense EV yield. */
	public int evSpDef;
	/** The Pokémon's Speed EV yield. */
	public int evSpd;
	/** The Pokémon's base friendship. */
	public int baseFriendship;
	/** The Pokémon's form index. */
	public int form;
	
	/** The Pokémon's index in the dex number container list. */
	public int listIndex;
	
	/**
	 * Initializes a Pokémon.
	 * @param name The name of the Pokémon.
	 */
	public Pokemon(String name) {
		this.name = name;
	}
	
	/**
	 * Removes " (Pokémon)" from the Pokémon's name.
	 */
	public void truncateName() {
		name = name.substring(0, name.length() - 10);
	}
	
	/**
	 * Gets the human-language name for the Pokémon.
	 * @return The human-language name for the Pokémon.
	 */
	public String getTranslatedName() {
		return getTranslatedName(name);
	}

	/**
	 * Gets the human-language name for a Pokémon.
	 * @param name The database name of the Pokémon.
	 * @return The human-language name for the Pokémon.
	 */
	public static String getTranslatedName(String name) {
		if (name == null) {
			return "";
		} else {
			return Database.getDatabase().getLangMap().get("pixelmon." + name.toLowerCase() + ".name");
		}
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Pokemon) {
			Pokemon otherP = (Pokemon) other;
			return otherP.name.equals(name);
		} else {
			return false;
		}
	}
}