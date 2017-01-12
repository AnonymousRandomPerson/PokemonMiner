package miner.storage;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores information about a move learned by pre-evolutions.
 */
public class PreEvoEntry {

	/** The pre-evolutions who can learn the move. */
	public List<Pokemon> preEvolutions = new ArrayList<>();
	/** The move learned by the pre-evolutions. */
	public Move move;
}
