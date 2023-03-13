/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.fileimport.misc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class FileImportTranslate {
	
	// Map used for translations. 
	// Key format: <column-header> + "<+>" + <field-value>.
	// Value format: <field-value-replacement>. 
	private Map<String, String> translateMap = new HashMap<String, String>();
	private boolean translateUsed = false; // To avoid time consuming translations if not needed. 
	
	@SuppressWarnings("unused")
	private FileImportTranslate() { // Default constructor not used.
		
	}

	// To be used during import. Format for columnAndValue: <column-header>;<field-value>.
	public String translateImportValue(String column, String value) {
		if (translateUsed == false) {
			return value;
		}
		if (translateMap.containsKey(column + "<+>" + value)) {
			return translateMap.get(column + "<+>" + value);
		}
		return value;
	}

	public FileImportTranslate(String importFileName) {

		// This file should be a part of the zip:ed import file.
		Path filePath = Paths.get(importFileName, "processed_data", "translate.txt");
		if (Files.notExists(filePath)) {
			return;
		}	
		
		// Put value in publicChangelogComment.
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));
			parseTranslateFile(bufferedReader);
			if (!translateMap.isEmpty()) {
				translateUsed = true;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Converts the text based Translate file into a list of key/value pairs.
	private void parseTranslateFile(BufferedReader in) {
		try {
			String line;
			int commentIndex;
			
			while ((line = in.readLine()) != null) {				
				// Remove comments.
				commentIndex = line.indexOf("#", 0);
				if (commentIndex > 0) {
					line = line.substring(0, commentIndex);
				}
				
				String delimiter = ";"; // Default delimiter.
				if (line.contains(";")) 		{ delimiter = ";"; } // ";" is used.
				else if (line.contains("	")) { delimiter = "	"; } // Tab is used;
				else if (line.contains("¤")) 	{ delimiter = "¤"; } // ¤ is used;
				
//				String[] rowItems = line.split(delimiter);
				String[] rowItems = line.split(Pattern.quote(delimiter));
				if (rowItems.length >= 3) { // Three items needed to be valid.
					translateMap.put(rowItems[0].trim() + "<+>" + rowItems[1].trim(), rowItems[2].trim());
				}
				
			}
			in.close();
			
		} catch (FileNotFoundException e) {
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("FileImportTranslate: File not found. ");
			messageBox.setMessage("Error: " + e.getMessage());
			messageBox.open();
			e.printStackTrace();
//			System.exit(-1);
		} catch (Exception e) {
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("FileImportTranslate: Exception in parseTranslateFile(). ");
			messageBox.setMessage("Error: " + e.getMessage());
			messageBox.open();
			e.printStackTrace();
//			System.exit(-1);
		}
	}

	public boolean isTranslateUsed() {
		return translateUsed;
	}

}
