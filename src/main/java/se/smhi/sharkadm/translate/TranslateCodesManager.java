/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.translate;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import se.smhi.sharkadm.datasets.fileimport.FileImportInfo;
import se.smhi.sharkadm.settings.SettingsManager;
import se.smhi.sharkadm.utils.ParseFileUtil;

public class TranslateCodesManager {
	
	List<TranslateCodesObject> translateObjectList = new ArrayList<TranslateCodesObject>();
	protected Map<String, String> synonymToObject = new HashMap<String, String>(); // key = internalKey<+>synonymPart.
	protected Map<String, TranslateCodesObject> internalNameToObject = new HashMap<String, TranslateCodesObject>(); // key = internalKey<+>internalName.
	
	public void clear() {
		translateObjectList.clear();
		synonymToObject.clear();
		internalNameToObject.clear();
	}
	
	public String translateSynonym(String key, String value, FileImportInfo importInfo) {
		
		if (value.equals("")) {
			return "";
		}
		// If the synonym contains "," don't split before check.
		String internalKeyAndValue = key + "<+>" + value.trim().replace(" ", "").toLowerCase();
		if (synonymToObject.containsKey(internalKeyAndValue)) {
			return synonymToObject.get(internalKeyAndValue);
		}
		// Split on ",".
		String result = "";
		String delimiter = "";
		String[] parts = value.split(Pattern.quote(","));
		for (String partValue : parts) {
			internalKeyAndValue = key + "<+>" + partValue.trim().replace(" ", "").toLowerCase();
			result += delimiter;
			if (synonymToObject.containsKey(internalKeyAndValue)) {
				result += synonymToObject.get(internalKeyAndValue);
			}
			else if (internalNameToObject.containsKey(internalKeyAndValue)) {
					result += partValue.trim();
			} else {
//				result += partValue.trim();
				importInfo.addConcatWarning("Failed to translate coded value. Code: " + key + "   Value: " + partValue);
				return value; // If some part not in the synonym list, don't translate the rest of them.
			}
//			delimiter = ", ";
			delimiter = "<->"; // Use internally, replace with , before save to db.
		}
		if (result.equals("")) {
			result = value;
		}
		return result;

//		String internalKeyAndValue = key + "<+>" + value.replace(" ", "").toLowerCase();
//		String result = value;
//		if (synonymToObject.containsKey(internalKeyAndValue)) {
//			result = synonymToObject.get(internalKeyAndValue);
//		}
//		return result;
	}
	
	public String translateToEnglishName(String key, String value) {
		String result = "";
		String delimiter = "";
//		String[] parts = value.split(Pattern.quote(","));
		String[] parts = value.split(Pattern.quote("<->"));
		for (String partValue : parts) {			
			String internalKeyAndValue = key + "<+>" + partValue.trim();
			result += delimiter;
			if (internalNameToObject.containsKey(internalKeyAndValue)) {
				result += internalNameToObject.get(internalKeyAndValue).getEnglishName();
			} else {
				result += partValue.trim();
			}
//			delimiter = ", ";
			delimiter = "<->";
		}
		if (result.equals("")) {
			result = value;
		}
		return result;
	}
	
	public String translateToSwedishName(String key, String value) {
		String result = "";
		String delimiter = "";
//		String[] parts = value.split(Pattern.quote(","));
		String[] parts = value.split(Pattern.quote("<->"));
		for (String partValue : parts) {			
			String internalKeyAndValue = key + "<+>" + partValue.trim();
			result += delimiter;
			if (internalNameToObject.containsKey(internalKeyAndValue)) {
				result += internalNameToObject.get(internalKeyAndValue).getSwedishName();
			} else {
				result += partValue.trim();
			}
//			delimiter = ", ";
			delimiter = "<->";
		}
		if (result.equals("")) {
			result = value;
		}
		return result;
	}
	
	public void importTranslateCodesFile() {
		List<String[]> fileContent;
		BufferedReader bufferedReader = null;
		
		SettingsManager.instance().clearSettingsList();
		
		// TRANSLATE CODES.
		try {
			bufferedReader = ParseFileUtil.GetSharkConfigFile("translate_codes.txt");
			
			fileContent = ParseFileUtil.parseDataFile(bufferedReader, false);
			if (fileContent != null) {					
				importTranslateCodesList(fileContent);
			}
			
			// Prepare the list for synonym translations. 
			for (TranslateCodesObject object : translateObjectList) {
				
				String synonymeNames = object.getSynonyms();
				synonymeNames = synonymeNames.replaceAll("<OR>", "<or>");
				
				String[] parts = synonymeNames.split(Pattern.quote("<or>"));
				for (String part : parts) {
					if (!part.equals("")) {
//						synonymToObject.put(object.getInternalKey() + "<+>" + part, object.getInternalValue());
						synonymToObject.put(object.getInternalKey() + "<+>" + part.trim().replace(" ", "").toLowerCase(), 
											object.getInternalValue());
					}
				}
				
				
				// Short, english and swedish names should also be accepted as synonyms.
				if (!object.getShortName().equals("")) {
					synonymToObject.put(object.getInternalKey() + "<+>" + object.getShortName().replace(" ", "").toLowerCase(), 
										object.getInternalValue());
				}
				if (!object.getEnglishName().equals("")) {
					synonymToObject.put(object.getInternalKey() + "<+>" + object.getEnglishName().replace(" ", "").toLowerCase(), 
										object.getInternalValue());
				}
				if (!object.getSwedishName().equals("")) {
					synonymToObject.put(object.getInternalKey() + "<+>" + object.getSwedishName().replace(" ", "").toLowerCase(), 
										object.getInternalValue());
				}
			
			}

			// Prepare the list for object lookup from internal value. 
			for (TranslateCodesObject object : translateObjectList) {
				internalNameToObject.put(object.getInternalKey() + "<+>" + object.getInternalValue(), object);				
			}

		} catch (Exception e) {
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Settings import");
			messageBox.setMessage("Failed to import 'translate_codes.txt'.");
			messageBox.open();
		}
	}

	private void importTranslateCodesList(List<String[]> fileContent) {
		String[] header = null;

		for (String[] row : fileContent) {
			if (header == null) { 
				// The first line contains the header.
				header = row;
			} else {
				if (row.length < 4) {
					continue;
				}

				TranslateCodesObject object = new TranslateCodesObject();
				Integer index = 0;
				for (String headerItem : header) {
					if (headerItem.equals("internal_key")) {
						object.setInternalKey(row[index]);
					}
					else if (headerItem.equals("internal_value")) {
						object.setInternalValue(row[index]);
					}
					else if (headerItem.equals("synonyms")) {
						object.setSynonyms(row[index]);
					}
					else if (headerItem.equals("short_name")) {
						object.setShortName(row[index]);
					}
					else if (headerItem.equals("english_name")) {
						object.setEnglishName(row[index]);
					}
					else if (headerItem.equals("swedish_name")) {
						object.setSwedishName(row[index]);
					}
					else if (headerItem.equals("prefix")) {
						object.setPrefix(row[index]);
					}
					index += 1;
				}
				translateObjectList.add(object);
			}
		}
	}
}
