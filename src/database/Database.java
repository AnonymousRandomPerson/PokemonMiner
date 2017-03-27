package database;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import miner.storage.DexNumberContainer;
import util.FileIO;

/**
 * Gets data from the Pixelmon database.
 */
public class Database {

	/** The connection to the Pixelmon database. */
	private Connection connection = null;
	/** A statement to query the database with. */
	private Statement statement = null;
	/** The result of a database query. */
	private ResultSet resultSet = null;
	/** Whether a connection has been made to the database. */
	private boolean madeConnection = false;

	/** A map from type names to their database indices. */
	private Map<String, Integer> typeMap;
	/** A map from Pokémon names to their database indices. */
	private Map<String, Integer> pokemonMap;
	/** A map from move names to their database indices. */
	private Map<String, Integer> moveMap;

	/** All TM moves in Pixelmon. */
	private Set<String> tmMoves;
	
	/** Indexes Pokémon by Pokédex number. */
	private DexNumberContainer dexContainer;
	
	/** Maps lang keys to text strings. */
	private Map<String, String> langMap;
	
	/** The singleton instance of the database. */
	private static Database instance;
	
	/**
	 * Connects to the database.
	 */
	private Database() {
		getConnection();
	}
	
	/**
	 * Gets the singleton instance of the database.
	 * @return The singleton instance of the database.
	 */
	public static Database getDatabase() {
		if (instance == null) {
			instance = new Database();
		}
		instance.getConnection();
		return instance;
	}

	/**
	 * Gets the connection to the path.
	 */
	public void getConnection() {
		try {
			if (!madeConnection || connection.isClosed()) {
				Class.forName("org.h2.Driver");
				connection = DriverManager.getConnection("jdbc:h2:file:Pixelmon2");
				madeConnection = true;
				statement = connection.createStatement();
				System.out.println("Database connection successful.");
			}
		} catch (Exception e) {
			System.out.println("Could not get a connection to database.");
			e.printStackTrace();
		}
	}

	/**
	 * Closes the database connection and all related fields.
	 */
	public void closeConnection() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (statement != null) {
				statement.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (resultSet != null && !resultSet.isClosed()) {
				resultSet.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		connection = null;
		statement = null;
		resultSet = null;
		madeConnection = false;
	}

	/**
	 * Gets a result from a database query.
	 * @param query The database query to get a result from.
	 * @return The result of the database query.
	 */
	public ResultSet executeQuery(StringBuilder query) {
		return executeQuery(query.toString());
	}
	
	/**
	 * Gets a result from a database query.
	 * @param query The database query to get a result from.
	 * @return The result of the database query.
	 */
	public ResultSet executeQuery(String query) {
		try {
			if (resultSet != null && !resultSet.isClosed()) {
				resultSet.close();
			}
			resultSet = statement.executeQuery(query);
			return resultSet;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Gets a result from a database query.
	 * @param query The database query to get a result from.
	 * @param currentStatement The statement to query the database with.
	 * @return The result of the database query.
	 */
	public ResultSet executeQuery(StringBuilder query, Statement currentStatement) {
		return executeQuery(query.toString(), currentStatement);
	}
	
	/**
	 * Gets a result from a database query.
	 * @param query The database query to get a result from.
	 * @param currentStatement The statement to query the database with.
	 * @return The result of the database query.
	 */
	public ResultSet executeQuery(String query, Statement currentStatement) {
		try {
			ResultSet currentResultSet = currentStatement.executeQuery(query);
			return currentResultSet;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Creates a new database statement.
	 * @return The database statement to create.
	 */
	public Statement createNewStatement() {
		try {
			return connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Closes a statement and a result set.
	 * @param currentStatement The statement to close.
	 * @param currentResultSet The result set to close.
	 */
	public void closeStatement(Statement currentStatement, ResultSet currentResultSet) {
		try {
			if (currentStatement != null && !currentStatement.isClosed()) {
				currentStatement.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (currentResultSet != null && !currentResultSet.isClosed()) {
				currentResultSet.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads a table into a map.
	 * @param map The map to load into.
	 * @param id The primary key name in the table.
	 * @param name The table column with information to associate with the primary key.
	 * @param table The name of the table to load from.
	 */
	public void loadMap(Map<String, Integer> map, String id, String name, String table) {
		try {
			resultSet = executeQuery("SELECT " + id + ", " + name + " FROM " + table);
			while (resultSet.next()) {
				map.put(resultSet.getString(name), resultSet.getInt(id));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the Pokémon map from the database.
	 * @return The Pokémon map from the database.
	 */
	public Map<String, Integer> getPokemonMap() {
		if (pokemonMap == null) {
			pokemonMap = new HashMap<>();
			loadMap(pokemonMap, "PIXELMONID", "PIXELMONNAME", "PIXELMON");
			addMapDuplicate(pokemonMap, "Mime Jr.", "Mime_Jr.");
			addMapDuplicate(pokemonMap, "Mr. Mime", "MrMime");
			addMapDuplicate(pokemonMap, "Nidoran♀", "Nidoranfemale");
			addMapDuplicate(pokemonMap, "Nidoran♂", "Nidoranmale");
			addMapDuplicate(pokemonMap, "Farfetch'd", "Farfetchd");
		}
		return pokemonMap;
	}
	
	/**
	 * Gets the move map from the database.
	 * @return The move map from the database.
	 */
	public Map<String, Integer> getMoveMap() {
		if (moveMap == null) {
			moveMap = new HashMap<>();
			loadMap(moveMap, "MOVEID", "NAME", "MOVES");
			addMapDuplicate(moveMap, "Ancient Power", "AncientPower");
			addMapDuplicate(moveMap, "Bubble Beam", "BubbleBeam");
			addMapDuplicate(moveMap, "Double Slap", "DoubleSlap");
			addMapDuplicate(moveMap, "Dragon Breath", "DragonBreath");
			addMapDuplicate(moveMap, "Dynamic Punch", "DynamicPunch");
			addMapDuplicate(moveMap, "Extreme Speed", "ExtremeSpeed");
			addMapDuplicate(moveMap, "Feather Dance", "FeatherDance");
			addMapDuplicate(moveMap, "Feint Attack", "Faint Attack");
			addMapDuplicate(moveMap, "Grass Whistle", "GrassWhistle");
			addMapDuplicate(moveMap, "High Jump Kick", "Hi Jump Kick");
			addMapDuplicate(moveMap, "Sand Attack", "Sand-Attack");
			addMapDuplicate(moveMap, "Self-Destruct", "Selfdestruct");
			addMapDuplicate(moveMap, "Smelling Salts", "SmellingSalt");
			addMapDuplicate(moveMap, "Smokescreen", "SmokeScreen");
			addMapDuplicate(moveMap, "Soft-Boiled", "Softboiled");
			addMapDuplicate(moveMap, "Solar Beam", "SolarBeam");
			addMapDuplicate(moveMap, "Sonic Boom", "SonicBoom");
			addMapDuplicate(moveMap, "ThunderPunch", "Thunder Punch");
			addMapDuplicate(moveMap, "Thunder Shock", "ThunderShock");
			addMapDuplicate(moveMap, "U-turn", "U-Turn");
			addMapDuplicate(moveMap, "V-create", "V-Create");
		}
		return moveMap;
	}
	
	/**
	 * Gets the type map from the database.
	 * @return The type map from the database.
	 */
	public Map<String, Integer> getTypeMap() {
		if (typeMap == null) {
			typeMap = new HashMap<>();
			loadMap(typeMap, "TYPEID", "NAME", "TYPES");
		}
		return typeMap;
	}

	/**
	 * Adds a duplicate key to a map with a different name.
	 * @param map The map to add to.
	 * @param duplicate The duplicate key to add.
	 * @param original The original key to duplicate.
	 */
	private void addMapDuplicate(Map<String, Integer> map, String duplicate, String original) {
		map.put(duplicate, map.get(original));
	}

	/**
	 * Gets the database index of a Pokémon.
	 * @param pokemon The name of the Pokémon.
	 * @return The database index of the Pokémon.
	 */
	public Integer getPokemonIndex(String pokemon) {
		return getMapIndex(getPokemonMap(), pokemon);
	}

	/**
	 * Gets the database index of a move.
	 * @param move The name of the move.
	 * @return The database index of the move.
	 */
	public Integer getMoveIndex(String move) {
		return getMapIndex(getMoveMap(), move);
	}

	/**
	 * Gets the database index of a certain type of data.
	 * @param map The map to get the database index from.
	 * @param key The item to get the database index of.
	 * @return The database index of the item.
	 */
	private Integer getMapIndex(Map<String, Integer> map, String key) {
		Integer id = map.get(key);
		if (id == null) {
			throw new IllegalArgumentException(key + " not found.");
		}
		return id;
	}
	
	/**
	 * Gets the TM moves in Pixelmon.
	 */
	public Set<String> getTMMoves() {
		if (tmMoves == null) {
			tmMoves = new HashSet<String>();
			String nameKey = "NAME";
			try {
				resultSet = executeQuery("SELECT " + nameKey + " FROM MOVES WHERE TMID IS NOT NULL OR HMID IS NOT NULL;");
				while (resultSet.next()) {
					tmMoves.add(resultSet.getString(nameKey));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return tmMoves;
	}
	
	/**
	 * Gets the Pokédex number mapping container.
	 * @return The Pokédex number mapping container.
	 */
	public DexNumberContainer getDexContainer() {
		if (dexContainer == null) {
			dexContainer = new DexNumberContainer();
		}
		return dexContainer;
	}
	
	/**
	 * Fills the lang key map and returns it.
	 * @return The lang key map.
	 */
	public Map<String, String> getLangMap() {
		if (langMap == null) {
			langMap = new HashMap<>();
			BufferedReader reader = FileIO.getReadBuffer();
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					String[] keyValue = line.split("=", 2);
					if (keyValue.length == 2) {
						langMap.put(keyValue[0], keyValue[1]);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return langMap;
	}
	
	/**
	 * Gets an integer field in a query, returning -1 if the field is null;
	 * @param result The result to get the integer field from.
	 * @param field The name of the integer field.
	 * @return The integer field value, or -1 if the field is null'
	 */
	public static int getIntNull(ResultSet result, String field) {
		try {
			int fieldInt = result.getInt(field);
			if (result.wasNull()) {
				return -1;
			} else {
				return fieldInt;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
}
