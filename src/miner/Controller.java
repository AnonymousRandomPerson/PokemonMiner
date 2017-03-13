package miner;

import database.Database;
import miner.wiki.DropsMiner;
import miner.wiki.GymMiner;
import miner.wiki.ImageMiner;
import miner.wiki.ListMiner;
import miner.wiki.PokemonArticleMiner;
import util.FileIO;

/**
 * Calls methods as needed to mine data.
 */
class Controller {

	/**
	 * Main method.
	 * @param args Argument 0: Miner type, Argument 1: Name to pass into miner.
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		String minerType = "";
		if (args.length > 0) {
			minerType = args[0];
		}
		String name = "";
		if (args.length > 1) {
			name = args[1];
			for (int i = 2; i < args.length; i++) {
				name += " " + args[i];
			}
		}
		long beforeTime = System.currentTimeMillis();
		FileIO fileIO = new FileIO();
		try {
			Database database = Database.getDatabase();
			PokemonMiner miner = new PokemonMiner();
			LangMiner langMiner = new LangMiner();
			PokemonArticleMiner pokemonArticleMiner = new PokemonArticleMiner();
			ImageMiner imageMiner = new ImageMiner();
			RandomMiner randomMiner = new RandomMiner();
			ListMiner listMiner = new ListMiner();
			GymMiner gymMiner = new GymMiner();
			DropsMiner dropsMiner = new DropsMiner();
			String data = "";

			try {
				switch (minerType) {
				case "pokemon":
					data = miner.getPokemonData();
					break;
				case "move":
					data = miner.getMoveData();
					break;
				case "levelup":
					data = miner.getLevelUpData();
					break;
				case "tm":
					data = miner.getTMData();
					break;
				case "tutor":
					data = miner.getTutorData();
					break;
				case "egg":
					data = miner.getEggMoveData();
					break;
				case "eggold":
					data = miner.getEggMoveDataOld();
					break;
				case "newmoves":
					data = miner.getNewMoves();
					break;
				case "event":
					data = miner.getEventData();
					break;
				case "langmove":
					data = langMiner.getAttackDetails();
					break;
				case "pokedex":
					data = langMiner.getPokedexEntries();
					break;
				case "article":
					data = pokemonArticleMiner.getPokemonArticle(name);
					break;
				case "movearticle":
					data = pokemonArticleMiner.getMoveArticle(name);
					break;
				case "biome":
					data = pokemonArticleMiner.getBiomeArticle(name);
					break;
				case "type":
					data = pokemonArticleMiner.getTypeArticle(name);
					break;
				case "ability":
					data = pokemonArticleMiner.getAbilityArticle(name);
					break;
				case "egggroup":
					data = pokemonArticleMiner.getEggGroupArticle(name);
					break;
				case "shiny":
					imageMiner.getShinyMinisprites();
					break;
				case "unavailable":
					imageMiner.getUnavailablePokemon();
					break;
				case "available":
					data = listMiner.getAvailablePokemon();
					break;
				case "availableprogress":
					data = listMiner.getAvailableProgress();
					break;
				case "forms":
					data = randomMiner.getNewForms();
					break;
				case "pictures":
					data = listMiner.getShinyPictures();
					break;
				case "evyield":
					data = listMiner.getEVYield();
					break;
				case "fixspecial":
					data = randomMiner.fixSpecial();
					break;
				case "spawnlocation":
					data = listMiner.getSpawnLocations();
					break;
				case "moveids":
					data = listMiner.getMoveIDs();
					break;
				case "tutorlist":
					data = listMiner.getTutorList();
					break;
				case "fixegggroups":
					data = randomMiner.fixEggMoveSpaces();
					break;
				case "mega":
					data = miner.getMegaData();
					break;
				case "gym":
					data = gymMiner.getGymData(name);
					break;
				case "dropsall":
					data = dropsMiner.getAllDrops();
					break;
				case "drops":
					data = dropsMiner.getDrops(name);
					break;
				case "dropitem":
					data = dropsMiner.getDropPokemon(name);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				database.closeConnection();
			}

			fileIO.write(data);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			fileIO.closeFiles();
		}
		System.out.println("Finished in " + ((System.currentTimeMillis() - beforeTime) / 1000) + " seconds.");
	}
}
