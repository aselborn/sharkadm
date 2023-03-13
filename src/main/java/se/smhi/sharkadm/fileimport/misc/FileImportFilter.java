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

public class FileImportFilter {
	
	// Map used for translations. 
	// Key format: <column header>+<field value for filtered rows>.
	// Value format: not used, should be empty. 
	private Map<String, String> filterMap = new HashMap<String, String>();
	private boolean filterUsed = false; // To avoid time consuming translations if not needed. 
	
	@SuppressWarnings("unused")
	private FileImportFilter() { // Default constructor not used.
		
	}

	// To be used during import. 
	public Boolean shouldBeFiltered(String[] header, String[] row) {
		if (filterUsed == false) {
			return false;
		}
		for (int i = 0; i < header.length; i++) {
			if (filterMap.containsKey(header[i] + "+" + row[i])) {
				return true;
			}
		}
		return false;
	}

	public FileImportFilter(String importFileName) {

		// This file should be a part of the zip:ed import file.
		Path filePath = Paths.get(importFileName, "processed_data", "filter.txt");
		if (Files.notExists(filePath)) {
			return;
		}	
		
		// Put value in publicChangelogComment.
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));
			parseFilterFile(bufferedReader);
			if (!filterMap.isEmpty()) {
				filterUsed = true;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Converts the text based filter file into a list of key/value pairs.
	private void parseFilterFile(BufferedReader in) {
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
				if (rowItems.length > 1) { // Two columns needed to be valid.
					filterMap.put(rowItems[0].trim() + "+" + rowItems[1].trim(), "");
				}
			}
			in.close();
			
		} catch (FileNotFoundException e) {
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("FileImportFilter: File not found. ");
			messageBox.setMessage("Error: " + e.getMessage());
			messageBox.open();
			e.printStackTrace();
//			System.exit(-1);
		} catch (Exception e) {
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("FileImportFilter: Exception in parseFilterFile(). ");
			messageBox.setMessage("Error: " + e.getMessage());
			messageBox.open();
			e.printStackTrace();
//			System.exit(-1);
		}
	}

	public boolean isFilterUsed() {
		return filterUsed;
	}

}
