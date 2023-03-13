/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.fileimport.misc;

import java.io.BufferedReader;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import se.smhi.sharkadm.species.TaxaWorms;
import se.smhi.sharkadm.utils.ParseFileUtil;

public class FileImportTaxaWorms {

	private static FileImportTaxaWorms instance = new FileImportTaxaWorms(); // Singleton.
	private boolean fileLoaded = false;
	
	private FileImportTaxaWorms() { // Singleton.
	}
	
	public static FileImportTaxaWorms instance() { // Singleton.
		return instance;
	}
	
	public boolean isFileLoaded() {
		return fileLoaded;
	}

	public void loadFiles() {
		List<String[]> fileContent;
		BufferedReader bufferedReader;
		
		TaxaWorms.instance().clear();				
		
		try {
			bufferedReader = ParseFileUtil.GetSharkConfigFile("taxa_worms.txt");

			fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
			if (fileContent != null) {				
				importTaxaWorms(fileContent);
			}
			bufferedReader = ParseFileUtil.GetSharkConfigFile("translate_to_worms.txt");

			fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
			if (fileContent != null) {				
				importTranslateToWorms(fileContent);
			}
			fileLoaded = true;
			
			bufferedReader = ParseFileUtil.GetSharkConfigFile("translate_to_worms.txt");

			fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
			if (fileContent != null) {				
				importTranslateToWorms(fileContent);
			}
			bufferedReader = ParseFileUtil.GetSharkConfigFile("translate_to_worms.txt");

			fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
			if (fileContent != null) {				
				importTranslateToWorms(fileContent);
			}
			fileLoaded = true;
			
		} catch (Exception e) {
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("'Taxa WoRMS import");
			messageBox.setMessage("Failed to import 'taxa_worms.txt'.");
			messageBox.open();
		}		
	}
	
	private void importTaxaWorms(List<String[]> fileContent) {
		String[] header = null;
				
		for (String[] row : fileContent) {
			if (header == null) { 
				// The first line contains the header.
				header = row;
			} else {	
				// Used columns:
				String scientificName = ""; // scientific_name.
				String aphiaId = ""; // aphia_id.
				
				Integer index = 0;
				for (String headerItem : header) {
					if (headerItem.equals("scientific_name")) {
						scientificName = row[index].trim();
					}
					else if (headerItem.equals("aphia_id")) {
						aphiaId = row[index].trim();
					}
					index += 1;
				}
				if (!scientificName.equals("")) {
					if (!aphiaId.equals("")) {
						TaxaWorms.instance().addTaxaWorms(scientificName, aphiaId);
					}
				}				
			}
		}
	}
	private void importTranslateToWorms(List<String[]> fileContent) {
		String[] header = null;
				
		for (String[] row : fileContent) {
			if (header == null) { 
				// The first line contains the header.
				header = row;
			} else {	
				// Used columns:
				String scientificNameFrom = ""; // scientific_name_from.
				String scientificNameTo = ""; // scientific_name_to.
				
				Integer index = 0;
				for (String headerItem : header) {
					if (headerItem.equals("scientific_name_from")) {
						scientificNameFrom = row[index].trim();
					}
					else if (headerItem.equals("scientific_name_to")) {
						scientificNameTo = row[index].trim();
					}
					index += 1;
				}
				if (!scientificNameFrom.equals("")) {
					if (!scientificNameTo.equals("")) {
						TaxaWorms.instance().addTranslateToWorms(scientificNameFrom, scientificNameTo);
					}
				}				
			}
		}
	}
}
