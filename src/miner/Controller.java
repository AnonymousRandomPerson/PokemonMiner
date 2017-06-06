package miner;

import java.awt.datatransfer.*;
import java.awt.Toolkit;
import java.util.Scanner;

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
		String name = "";
		boolean repeat = true;
		Scanner input = new Scanner(System.in);
		do {
			if (args.length > 0) {
				minerType = args[0];
			} else {
				System.out.println("Enter a miner:");
				minerType = input.nextLine();
			}
			if (args.length > 1) {
				name = args[1];
				for (int i = 2; i < args.length; i++) {
					name += " " + args[i];
				}
			} else if (args.length == 0) {
				System.out.println("Enter a name:");
				name = input.nextLine();
			}
			do {
				runMiner(minerType, name);
				
				if (repeat) {
					System.out.println("Enter a name:");
					name = input.nextLine();
					if (name.equals("b")) {
						break;
					}
				}
			} while (repeat && !name.equals("q"));
		} while (repeat && !name.equals("q"));
		input.close();
	}
	
	/**
	 * Runs the chosen miner.
	 * @param minerType The type of miner to run.
	 * @param name A name to pass into the chosen miner.
	 */
	private static void runMiner(String minerType, String name) {
		boolean copyClipboard = true;
		long beforeTime = System.currentTimeMillis();
		FileIO fileIO = new FileIO();
		String data = "";
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
					break;
				case "unavailablemega":
					imageMiner.getUnavailableMegas();
					break;
				case "shinydump":
					data = imageMiner.getShinyDump();
					break;
				default:
					System.out.println("No miner found for: " + minerType);
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
		if (copyClipboard && !data.isEmpty()) {
			StringSelection stringSelection = new StringSelection(data);
			Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
			clpbrd.setContents(stringSelection, null);
		}
		System.out.println("Finished in " + ((System.currentTimeMillis() - beforeTime) / 1000) + " seconds.");
	}
}
