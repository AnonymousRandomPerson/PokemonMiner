package miner;

import database.Database;

/**
 * Base class for all data miners.
 */
public abstract class Miner {

	/** The string builder currently being used to create output. */
	protected StringBuilder builder;
	/** The connection to the Pixelmon database. */
	protected Database database;
	/** The raw wikicode in the original article. */
	protected String totalRaw;
	
	/**
	 * Loads the Pixelmon database.
	 */
	public Miner() {
		database = Database.getDatabase();
	}
}
