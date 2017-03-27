package miner.storage;

import util.StringUtil;

/**
 * Data about a Pokémon's drops.
 */
public class DropEntry {
	
	/** The Pokemon dropping the items. */
	public String pokemon;
	/** The name of the item drop. */
	public String item;
	/** The translated name of the item drop. */
	public String translatedItem;
	/** The minimum number of items to drop. */
	public int min;
	/** The maximum number of items to drop. */
	public int max;
	/** Whether the item is a "rare" drop. */
	public boolean isRare;
	/** The percentage chance of the item drop occurring. */
	public float percent;
	
	/**
	 * Initializes a drop entry.
	 * @param item The name of the item drop.
	 * @param min The minimum number of items to drop.
	 * @param max The maximum number of items to drop.
	 * @param isRare Whether the item is a "rare" drop.
	 */
	public DropEntry(String pokemon, String item, int min, int max, boolean isRare) {
		this.pokemon = StringUtil.correctPokemonName(pokemon);
		this.item = item;
		this.translatedItem = StringUtil.guessItemName(item);
		this.min = min;
		this.max = max;
		this.isRare = isRare;
		this.percent = getPercent();
	}

	/**
	 * Gets a string representing the quantity range that the item can drop at.
	 * @return A string representing the quantity range that the item can drop at.
	 */
	public String getRangeString() {
		int newMin = Math.max(min, 1);
		String range;
		if (newMin == max) {
			range = Integer.toString(newMin);
		} else {
			range = newMin + "-" + max;
		}
		return range;
	}
	
	/**
	 * Gets the percentage chance of the item drop occurring.
	 * @return The percentage chance of the item drop occurring.
	 */
	public float getPercent() {
		if (percent == 0) {
			percent = 100;
			if (min == 0) {
				percent *= max / (max + 1f);
			}
			if (isRare) {
				percent *= 0.1f;
			}
		}
		return percent;
	}
	
	/**
	 * Checks if the drop entry has the same item as another entry.
	 * @param other The entry to compare against.
	 * @return Whether the drop entry has the same item as another entry.
	 */
	public boolean hasSameItem(DropEntry other) {
		return item.equals(other.item);
	}
	
	/**
	 * Combines two drop entries into a new drop entry.
	 * @param other The drop entry to combine with this entry.
	 * @return The combined drop entry.
	 */
	public DropEntry combineEntries(DropEntry other) {
		int newMin = min + other.min;
		int newMax = max + other.max;
		boolean newIsRare = isRare && other.isRare;
		DropEntry newEntry = new DropEntry(pokemon, item, newMin, newMax, newIsRare);
		newEntry.percent = 100 - ((1 - percent / 100) * (1 - other.percent / 100) * 100);
		return newEntry;
	}
}
