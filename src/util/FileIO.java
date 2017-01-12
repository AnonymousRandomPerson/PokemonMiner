package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Used to read from and write to files.
 */
public class FileIO {

	/** The name of the file that queries are written to. */
	private static final String OUTPUT_FILE = "Output.txt";
	/** The name of the file that data is read from. */
	private static final String INPUT_FILE = "en_US.lang";

	/** Reads file data into a buffer. */
	private FileReader fileReader = null;
	/** Used to store file data that was read. */
	private BufferedReader buffer = null;

	/** Writes file data from a buffer. */
	private FileWriter fileWriter = null;
	/** Used to store file data into a buffer. */
	private BufferedWriter bufferedWriter = null;
	
	/** The singleton instance of the file interfacer. */
	private static FileIO instance;
	
	/**
	 * Initializes the input and output files.
	 */
	public FileIO() {
		try {
			fileWriter = new FileWriter(new File(OUTPUT_FILE));
			bufferedWriter = new BufferedWriter(fileWriter);
	
			fileReader = new FileReader(new File(INPUT_FILE));
			buffer = new BufferedReader(fileReader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		instance = this;
	}
	
	/**
	 * Closes the file readers and writers.
	 */
	public void closeFiles() {
		try {
			if (bufferedWriter != null) {
				bufferedWriter.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if (fileWriter != null) {
				fileWriter.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if (buffer != null) {
				buffer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if (fileReader != null) {
				fileReader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the buffer that read data.
	 * @return The buffer that read data.
	 */
	public static BufferedReader getReadBuffer() {
		return instance.buffer;
	}
	
	/**
	 * Writes a string to the file.
	 * @param string The string to write.
	 */
	public void write(String string) {
		try {
			bufferedWriter.write(string);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
