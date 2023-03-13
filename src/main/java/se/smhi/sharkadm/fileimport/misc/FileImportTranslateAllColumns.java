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
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import se.smhi.sharkadm.facades.ModelFacade;
import se.smhi.sharkadm.utils.ParseFileUtil;

public class FileImportTranslateAllColumns {

	private static FileImportTranslateAllColumns instance = new FileImportTranslateAllColumns(); // Singleton.
	
	private Map<String, String> translateAllColumnsMap = new HashMap<String, String>();
	
	private FileImportTranslateAllColumns() { // Singleton.
	}
	
	public static FileImportTranslateAllColumns instance() { // Singleton.
		return instance;
	}
	
	public String translateValue(String columnInternalKey, String reportedValue) {
		
		String key = columnInternalKey + "<+>" + reportedValue;
		if (translateAllColumnsMap.containsKey(key)) {
			return translateAllColumnsMap.get(key);
		}
		return reportedValue;
	}
	
	public void loadFile() {
		List<String[]> fileContent;
		BufferedReader bufferedReader;
		
		try {
			bufferedReader = ParseFileUtil.GetSharkConfigFile("translate_all_columns.txt");

			fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
			if (fileContent != null) {				
				translateAllColumnsMap.clear();				
				importTranslateAllColumns(fileContent);
			}
			
		} catch (Exception e) {
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("'translate all columns' import");
			messageBox.setMessage("Failed to import 'translate_all_columns.txt'.");
			messageBox.open();
		}
		
		// Used by the Observer pattern.
		ModelFacade.instance().modelChanged();
	}
	
	private void importTranslateAllColumns(List<String[]> fileContent) {
		String[] header = null;
				
		for (String[] row : fileContent) {
			if (header == null) { 
				// The first line contains the header.
				header = row;
			} else {	
				// Used columns:
				String internalKey = row[0]; // internal_key.
				String internalValue = row[1]; // internal_value.
				String synonymes = row[2]; // synonyms.
				
				synonymes = synonymes.replaceAll("<OR>", "<or>");
				
				String[] parts = synonymes.split(Pattern.quote("<or>"));
				for (String synonym : parts) {
					String key = internalKey + "<+>" + synonym;
					translateAllColumnsMap.put(key, internalValue);
				}
			}
		}
	}
}
