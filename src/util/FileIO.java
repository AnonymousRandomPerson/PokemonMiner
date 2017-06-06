package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Used to read from and write to files.
 */
public class FileIO {

	/** The name of the file that queries are written to. */
	private static final String OUTPUT_FILE = "Output.txt";

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
		} catch (Exception e) {
			e.printStackTrace();
		}
		instance = this;
	}
	
	/**
	 * Returns the current instance of the FileIO object.
	 * @return The current instance of the FileIO object.
	 */
	public static FileIO getInstance() {
		return instance;
	}
	
	/**
	 * Initializes a file reader.
	 * @param path The path of the file to read.
	 */
	public void initializeReader(String path) {
		closeReadFile();
		try {
			fileReader = new FileReader(new File(path));
			buffer = new BufferedReader(fileReader);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
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
		closeReadFile();
	}
	
	/**
	 * Closes the file currently being read.
	 */
	private void closeReadFile() {
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
	
	/**
	 * Gets the contents of a text file.
	 * @param path The path of the file to read.
	 * @return The contents of the specified file.
	 */
	public static String getFileContents(String path) {
		instance.closeReadFile();
		instance.initializeReader(path);
		StringBuilder builder = new StringBuilder();
		String line;
		boolean first = true;
		try {
			while ((line = instance.buffer.readLine()) != null) {
				if (!first) {
					builder.append('\n');
				}
				builder.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}
	
	/**
	 * Gets a JSON object from a file.
	 * @param path The path of the file to get a JSON object from.
	 * @return The JSON object from the file.
	 */
	public static JSONObject getJSONFromFile(String path) {
		String jsonString = getFileContents(path);
		return new JSONObject(jsonString);
	}
	
	/**
	 * Gets a JSON array from a file.
	 * @param path The path of the file to get a JSON array from.
	 * @return The JSON array from the file.
	 */
	public static JSONArray getJSONArrayFromFile(String path) {
		String jsonString = getFileContents(path);
		return new JSONArray(jsonString);
	}
}
