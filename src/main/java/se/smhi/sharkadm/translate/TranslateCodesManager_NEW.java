/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.translate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class TranslateCodesManager_NEW {
	
	private boolean filesLoaded = false;
	
	private static TranslateCodesManager_NEW instance = new TranslateCodesManager_NEW(); // Singleton.
	
	protected List<TranslateCodesObject_NEW> translateObjectList = new ArrayList<TranslateCodesObject_NEW>();
	protected Map<String, TranslateCodesObject_NEW> lookupFieldValueToObject = new HashMap<String, TranslateCodesObject_NEW>();
	protected Map<String, String> lookupFieldSynonymToPublicValue = new HashMap<String, String>();
	protected Map<String, TranslateCodesObject_NEW> usedInDatasetList = new HashMap<String, TranslateCodesObject_NEW>();
	protected Set<String> managedFields = new HashSet<String>();

	private TranslateCodesManager_NEW() { // Singleton.
	}
	
	public static TranslateCodesManager_NEW instance() { // Singleton.
		return instance;
	}
	
	public boolean isFilesLoaded() {
		return this.filesLoaded;
	}

	public void setFilesLoaded(boolean filesLoaded) {
		this.filesLoaded = filesLoaded;
	}

	public void clear() {
		this.translateObjectList.clear();
		this.lookupFieldValueToObject.clear();
		this.lookupFieldSynonymToPublicValue.clear();
		this.usedInDatasetList.clear();
	}

	public void clearUsedInDatasetList() {
		this.usedInDatasetList.clear();
	}

	public void addTranslateObject(TranslateCodesObject_NEW translateObject) {
		this.translateObjectList.add(translateObject);
	}

	public void prepareTranslateLists() {
	
		for (TranslateCodesObject_NEW translateObject : this.translateObjectList) {
			
			// Prepare the list for object lookup from internal value.
			String field = translateObject.getField();
			String value = translateObject.getPublicValue();
			String fieldAndValue = translateObject.getFieldAndPublicValue();
			if (!this.lookupFieldValueToObject.containsKey(fieldAndValue)) {
				this.lookupFieldValueToObject.put(fieldAndValue, translateObject);
				this.managedFields.add(field);
			} else {
				System.out.println("DEBUG: Translate object already exists: " + fieldAndValue);
			}
			
			// Prepare for synonyms. Split if lists with <or>.
			String synonyms = translateObject.getSynonyms();
			synonyms = synonyms.replaceAll("<OR>", "<or>");
			String[] parts = synonyms.split(Pattern.quote("<or>"));
			for (String part : parts) {
				if (!part.equals("")) {
//					String synonymFieldValue = translateObject.getField() + "<+>" + part.trim().replace(" ", "").toLowerCase();
					String synonymFieldValue = translateObject.getField() + "<+>" + part.trim();
					this.lookupFieldSynonymToPublicValue.put(synonymFieldValue, value);
				}
			}
			// Also add value, code, swedish and english as synonyms.
			if (!value.equals("")) {
				String synonymFieldValue = translateObject.getField() + "<+>" + value.trim();
				this.lookupFieldSynonymToPublicValue.put(synonymFieldValue, value);
			}
			String code = translateObject.getCode();
			if (!code.equals("")) {
				if (!code.equals(value)) {
					String synonymFieldValue = translateObject.getField() + "<+>" + code.trim();
					this.lookupFieldSynonymToPublicValue.put(synonymFieldValue, value);
				}
			}
			String swedish = translateObject.getSwedish();
			if (!swedish.equals("")) {
				if (!swedish.equals(value)) {
					String synonymFieldValue = translateObject.getField() + "<+>" + swedish.trim();
					this.lookupFieldSynonymToPublicValue.put(synonymFieldValue, value);
				}
			}
			String english = translateObject.getEnglish();
			if (!english.equals("")) {
				if (!english.equals(value)) {
					String synonymFieldValue = translateObject.getField() + "<+>" + english.trim();
					this.lookupFieldSynonymToPublicValue.put(synonymFieldValue, value);
				}
			}


		}
	}

	public String translateSynonym(String field, String synonym) {
//		String fieldSynonym = field + "<+>" + synonym.trim().replace(" ", "").toLowerCase();
		String fieldSynonym = field + "<+>" + synonym.trim();
		String resultValue = synonym;
		if (this.managedFields.contains(field)) {
			if ((!field.equals("parameter")) && (!field.equals("value")) && (!field.equals("unit")) ) {			
				// Empty string indicates missing translation for managed codes.
				resultValue = "";
			}
		}
		if (this.lookupFieldSynonymToPublicValue.containsKey(fieldSynonym)) {
			resultValue = this.lookupFieldSynonymToPublicValue.get(fieldSynonym);			
		}
		return resultValue;
	}

	public void checkIfCodeIsUsed(String field, String value) {
		if (!field.equals("")) {
			if (!value.equals("")) {
				String fieldValue = "";
				fieldValue += field + "<+>";
				fieldValue += value;
		
				if (this.lookupFieldValueToObject.containsKey(fieldValue)) {
					if (!this.usedInDatasetList.containsKey(fieldValue)) {
						this.usedInDatasetList.put(fieldValue, this.lookupFieldValueToObject.get(fieldValue));
					}
				}
			}
		}
	}

	public List<String> getUsedRows() {
		String header = "";
		List<String> usedRows = new ArrayList<String>();
		for (TranslateCodesObject_NEW translateObject : this.usedInDatasetList.values()) {
			if (usedRows.size() == 0) {
				header = translateObject.getHeader();
			}
			usedRows.add(translateObject.getRow());				
		}
		Collections.sort(usedRows);
		usedRows.add(0, header);
		
		return usedRows;
	}

	public TranslateCodesObject_NEW getCodeObject(String field, String value) {
		if (!field.equals("")) {
			if (!value.equals("")) {
				String fieldValue = "";
				fieldValue += field + "<+>";
				fieldValue += value;
		
				if (this.lookupFieldValueToObject.containsKey(fieldValue)) {
					return this.lookupFieldValueToObject.get(fieldValue);
				}
			}
		}
		return null;
	}

}
