package util;

import java.util.List;

/**
 * Utility methods for lists.
 */
public class ListUtil {

	/**
	 * Adds an object to a list if it is not null.
	 * @param list The list to add an object to.
	 * @param object The object to add.
	 */
	public static <T> void addIfNotNull(List<T> list, T object) {
		if (object != null) {
			list.add(object);
		}
	}
}
