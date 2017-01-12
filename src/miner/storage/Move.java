package miner.storage;

import database.Database;

/**
 * Data for a Pokémon move.
 */
public class Move {
	
	/** The name of the move. */
	public String name;
	/** The move's type. */
	public String type;
	/** The move's damage category. */
	public String category;
	/** The move's power. */
	public Integer power;
	/** The move's accuracy. */
	public Integer accuracy;
	/** The move's base PP. */
	public int pp;
	/** The move's maximum PP. */
	public int ppMax;
	/** Whether the move makes contact. */
	public boolean contact;
	/** Whether the move hits all Pokémon that it can target. */
	public boolean hitsAll;
	/** Whether the move can hit an opponent directly opposite from the user. */
	public boolean hitsOpposite;
	/** Whether the move can hit an opponent near the user. */
	public boolean hitsAdjacentFoe;
	/** Whether the move can hit an opponent far from the user. */
	public boolean hitsExtendedFoe;
	/** Whether the move hits the user. */
	public boolean hitsSelf;
	/** Whether the move can hit a nearby ally. */
	public boolean hitsAdjacentAlly;
	/** Whether the move can hit a faraway ally. */
	public boolean hitsExtendedAlly;

	/**
	 * Initializes a move.
	 * @param name The name of the move.
	 */
	public Move(String name) {
		this.name = name;
	}
	
	/**
	 * Removes " (move)" from a move's name.
	 */
	public void truncateName() {
		name = name.substring(0, name.length() - 7).replace("_", " ");
	}
	
	/**
	 * Translates a move's database name into English.
	 * @param name The name of the move to translate.
	 * @return The translated move name.
	 */
	public static String translate(String name) {
		return Database.getDatabase().getLangMap().get("attack." + name.toLowerCase() + ".name");
	}
}
