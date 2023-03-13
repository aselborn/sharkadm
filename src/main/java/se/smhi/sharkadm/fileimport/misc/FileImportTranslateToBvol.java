/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.fileimport.misc;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import se.smhi.sharkadm.species.BvolTranslate;
import se.smhi.sharkadm.utils.ParseFileUtil;

public class FileImportTranslateToBvol {

	private static FileImportTranslateToBvol instance = new FileImportTranslateToBvol(); // Singleton.
	private boolean fileLoaded = false;
	
	private FileImportTranslateToBvol() { // Singleton.
	}
	
	public static FileImportTranslateToBvol instance() { // Singleton.
		return instance;
	}
	
	public boolean isFileLoaded() {
		return fileLoaded;
	}

	public void loadFile() {
		List<String[]> fileContent;
		BufferedReader bufferedReader;
		
		BvolTranslate.instance().clear();				

		try {
			bufferedReader = ParseFileUtil.GetSharkConfigFile("translate_bvol_name_size.txt");

			fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
			if (fileContent != null) {				
				importContentNameSize(fileContent);
			}
			fileLoaded = true;
			
		} catch (Exception e) {
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("'Translate to BVOL' import");
			messageBox.setMessage("Failed to import 'translate_bvol_name_size.txt'.");
			messageBox.open();
		}		
		
		fileContent = null;
		bufferedReader = null;
		try {
			bufferedReader = ParseFileUtil.GetSharkConfigFile("translate_bvol_name.txt");

			fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
			if (fileContent != null) {				
				importContentName(fileContent);
			}
			fileLoaded = true;
			
		} catch (Exception e) {
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("'Translate to BVOL' import");
			messageBox.setMessage("Failed to import 'translate_bvol_name.txt'.");
			messageBox.open();
		}		
	}
	
	private void importContentNameSize(List<String[]> fileContent) {
		String[] header = null;
				
		for (String[] row : fileContent) {
			if (header == null) { 
				// The first line contains the header.
				header = row;
			} else {	
				// Used columns:
				String fromTaxa = row[0]; // Scientific name from.
				String fromSize = row[1]; // Size class from.
				String toTaxa = row[2]; // Scientific name to.
				String toSize = row[3]; // Size class to.
				
				BvolTranslate.instance().addNameSizeTranslate(fromTaxa, fromSize, toTaxa, toSize);
			}
		}
	}
	
	private void importContentName(List<String[]> fileContent) {
		String[] header = null;
				
		for (String[] row : fileContent) {
			if (header == null) { 
				// The first line contains the header.
				header = row;
			} else {	
				// Used columns:
				String fromTaxa = row[0]; // Scientific name from.
				String toTaxa = row[1]; // Scientific name to.
				
				BvolTranslate.instance().addNameTranslate(fromTaxa, toTaxa);
			}
		}
	}
}
