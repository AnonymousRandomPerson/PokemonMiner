package miner.storage;

import java.util.Comparator;

/**
 * Compares drop entries by item name.
 */
public class DropComparatorItem implements Comparator<DropEntry> {

	@Override
	public int compare(DropEntry o1, DropEntry o2) {
		return o1.translatedItem.compareTo(o2.translatedItem);
	}

}
