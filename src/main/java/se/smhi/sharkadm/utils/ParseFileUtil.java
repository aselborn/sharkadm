/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ParseFileUtil {

	/**
	 * Returns a data resource file. Search order for the file is:
	 * - Current directory.
	 * - File service at SMHI. Used when running via CITRIX.
	 * - As resource bundled in jar-file. 
	 * @param configFileName
	 * @return BufferedReader
	 */
	public static BufferedReader GetSharkConfigFile(String configFileName) {
		// For old usage with default charset.
		return GetSharkConfigFileCharset(configFileName, "");
	}
		
	public static BufferedReader GetSharkConfigFileCharset(String configFileName, String charset) {
			
//		System.out.println("DEBUG: GetSharkConfigFile: " + configFileName);
		
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
//		ClassLoader classLoader = this.getClass().getClassLoader();
		InputStream inputStream;
		BufferedReader bufferedReader = null;		
		try {
			// Checks if file exist in current directory.
			File external_file = new File("TEST_SHARK_CONFIG\\" + configFileName);
			if (external_file.exists()) {

				if (charset.equals("")) {
					// Old style. Use default.
					bufferedReader = new BufferedReader(new FileReader(external_file));
				} else {
					// Use for example "UTF-8" in charset parameter.
					bufferedReader = new BufferedReader(
				        new InputStreamReader(
				            new FileInputStream(external_file), charset)); 
				}
				
				System.out.println("\nNOTE: LOCAL COPY IN TEST_SHARK_CONFIG:" + configFileName + "\n");
			} else {
				// Checks if file exist outside jar bundle. File service at SMHI.
				external_file = new File("\\\\winfs\\data\\prodkap\\sharkweb\\SHARK_CONFIG\\" + configFileName);
				if (external_file.exists()) {
					bufferedReader = new BufferedReader(new FileReader(external_file));
				} else {
//					// File is bundled in jar.
//					inputStream = classLoader
//							.getResourceAsStream("SHARK_CONFIG\\" + configFileName);
//					bufferedReader = new BufferedReader(new InputStreamReader(
//							inputStream));
//					
					System.out.println("Could not find the config file. File name: " + configFileName + ".");
				}
			}			
		} catch (Exception e) {
			System.out.println("Failed to load config file. File name: " + configFileName + ".");
		}
		return bufferedReader;
	}

	/**
	 * This method is used when parsing an indata file embedded in the zip-file. 
	 * @param in
	 * @param delimiter
	 * @return List of String-arrays.
	 */
	public static List<String[]> parseDataFile(BufferedReader in, boolean cleanup) {
		String line; 
		String delimiter = ";"; // Default delimiter.
		Boolean firstRow = true;
		Boolean emptyRow = true;
		
		int numberOfColumns = -1;
		int columnIndex;
		String[] columns;
		
		if (in == null) {
			return null;
		}

		List<String[]> rows = new ArrayList<String[]>();
		try {
			while ((line = in.readLine()) != null) {
				// Check header row.
				if (firstRow) {
					firstRow = false;
					// Check which field delimiter to use. 
					if (line.contains(";")) {
						delimiter = ";"; // ";" is used.
					}
					else if (line.contains("	")) {
						delimiter = "	"; // Tab is used;
					}
					else if (line.contains("�")) {
						delimiter = "�"; // � is used;
					}
					// Check number of header fields.
//					numberOfColumns = line.split(delimiter).length;
					numberOfColumns = line.split(Pattern.quote(delimiter)).length;
				}
				
				// Read one row.
				columns = new String[numberOfColumns];
				
				
				// Correction made for errors introduced by Excel, etc.
				// " is problematic on the web. Remove them.
				line = line.replace("\t\"\t", "\t");
				
				
				String[] rowItems = line.split(Pattern.quote(delimiter));
				columnIndex = 0;
				emptyRow = true;
				for (String item : rowItems) {
					if (columnIndex < numberOfColumns) {
						String string  = item;
						if (cleanup) {
							 // " is problematic on the web. Remove them. Most of them are generated by Excel.
							string  = string.replace("\"", "");
						}
						columns[columnIndex] = string.trim();
						if (columns[columnIndex].length() > 0) {
							emptyRow = false;
						}
					}
					columnIndex++;
				}
				// Add spaces to the remaining fields.
				while (columnIndex < numberOfColumns) {
					columns[columnIndex] = "";
					columnIndex++;
				}
				
				 // Don't add empty rows.
				if (emptyRow == false) {
					rows.add(columns);
				}
			}
			in.close();

		} catch (FileNotFoundException e) {
			System.out.println("ERROR when parsing file.");
			e.printStackTrace();
			System.exit(-1);
		} catch (Exception e) {
			System.out.println("ERROR when parsing file.");
			e.printStackTrace();
			System.exit(-1);
		}
		return rows;
	}

}
