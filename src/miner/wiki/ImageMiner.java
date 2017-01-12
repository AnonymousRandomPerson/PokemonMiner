package miner.wiki;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.imageio.ImageIO;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;

import miner.Miner;
import miner.storage.DexNumberContainer;
import pixelmon.EnumPokemon;
import util.APIConnection;
import util.StringUtil;

/**
 * Handles bulk image processing.
 */
public class ImageMiner extends Miner {

	/**
	 * Gets the Shiny minisprites of all available Pokémon.
	 */
	public void getShinyMinisprites() {
		DexNumberContainer dexContainer = database.getDexContainer();
		FileSystem fileSystem = FileSystems.getDefault();
		for (EnumPokemon pokemon : EnumPokemon.values()) {
			int dexNumber = dexContainer.getDexNumber(pokemon.name);
			String dexString = StringUtil.convertDexNumberString(dexNumber);
			if (dexNumber > 649) {
				dexString += 's';
			}
			dexString += ".png";
			Path originalPath = fileSystem.getPath("shinypokemon", dexString);
			String newImage = pokemon.name() + "SMS.png";
			Path newPath = fileSystem.getPath("images", newImage);

			try {
				Files.copy(originalPath, newPath, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates grayscale images of Pokémon that are not in Pixelmon.
	 */
	public void getUnavailablePokemon() {
		DexNumberContainer dexContainer = database.getDexContainer();
		totalRaw = APIConnection.getArticleSource("List of Pokémon by National Pokédex number");
		int currentIndex = 0;
		for (int i = 1; i <= 721; i++) {
			if (!dexContainer.hasDexNumber(i)) {
				String dexString = StringUtil.convertDexNumberString(i);
				String originalPath = "pokemon/" + dexString + ".png";

				currentIndex = totalRaw.indexOf("|" + dexString + "|", currentIndex);
				int nameIndex = currentIndex + 5;
				int endIndex = totalRaw.indexOf('|', nameIndex);
				String pokemonName = totalRaw.substring(nameIndex, endIndex);
				pokemonName.trim();
				String newPath = "images/" + pokemonName + "Gray.png";

				ImageIcon icon = new ImageIcon(originalPath);
				Image originalImage = icon.getImage();
				Image image = originalImage;
				int iconWidth = icon.getIconWidth();
				if (iconWidth == -1) {
					System.out.println("Image not found for " + dexString + " " + pokemonName + ".");
					continue;
				}
				
				boolean isBlank;
				BufferedImage bi;
				do {
					image = GrayFilter.createDisabledImage(originalImage);
					System.out.println(image.getWidth(null) + " " + image.getHeight(null));
					bi = new BufferedImage(iconWidth, icon.getIconHeight(),
							BufferedImage.TYPE_INT_ARGB);
					Graphics2D graphics = bi.createGraphics();
					graphics.drawImage(image, 0, 0, null);
					graphics.dispose();
					WritableRaster raster = bi.getAlphaRaster();
					isBlank = true;
					int rasterWidth = raster.getWidth();
					int rasterHeight = raster.getHeight();
					int[] pixels = raster.getPixels(0, 0, rasterWidth, rasterHeight, new int[rasterWidth * rasterHeight]);
					for (int pixel : pixels) {
						if (pixel != 0) {
							isBlank = false;
							break;
						}
					}
				} while (isBlank);
				File newImage = new File(newPath);
				try {
					if (!ImageIO.write(bi, "png", newImage)) {
						System.out.println("Writing failed for " + pokemonName + ".");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
