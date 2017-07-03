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

	/**
	 * Keeps a section of wikicode that was in the original article.
	 * @param sectionName The name of the section.
	 */
	protected boolean preserveSection(String sectionName) {
		return preserveSection(sectionName, builder);
	}

	/**
	 * Keeps a section of wikicode that was in the original article.
	 * @param sectionName The name of the section.
	 * @param builder The string builder to add the wikicode to.
	 */
	protected boolean preserveSection(String sectionName, StringBuilder builder) {
		int startIndex = totalRaw.indexOf(sectionName);
		if (startIndex >= 0) {
			int endIndex = totalRaw.indexOf("==", startIndex + sectionName.length());
			if (endIndex == -1) {
				endIndex = totalRaw.length();
			}
			builder.append('\n');
			builder.append(totalRaw.substring(startIndex, endIndex).trim());
			return true;
		}
		return false;
	}
}
