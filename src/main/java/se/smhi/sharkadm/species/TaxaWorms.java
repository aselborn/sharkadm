/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.species;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import se.smhi.sharkadm.fileimport.misc.FileImportTaxaWorms;

public class TaxaWorms {
	
	private static TaxaWorms instance = new TaxaWorms(); // Singleton.
	
	private Map<String, String> taxaWormsMap = new HashMap<String, String>();
	private Map<String, String> translateToWormsMap = new HashMap<String, String>();
	
	private TaxaWorms() { // Singleton.
	}
	
	public static TaxaWorms instance() { // Singleton.
		return instance;
	}
	
	public void clear() {
		this.taxaWormsMap.clear();
		this.translateToWormsMap.clear();
	}

//	public boolean contains(String scientificName) {
//		// Check if file is loaded.
//		if (!FileImportTaxaWorms.instance().isFileLoaded()) {
//			FileImportTaxaWorms.instance().loadFiles();
//		}
//		return taxaWormsMap.containsKey(scientificName);
//	}

	public String getAphiaId(String scientificName) {
		// Check if file is loaded.
		if (!FileImportTaxaWorms.instance().isFileLoaded()) {
			FileImportTaxaWorms.instance().loadFiles();
		}

		if (this.taxaWormsMap.containsKey(scientificName)) {
			return taxaWormsMap.get(scientificName);
		} else {
			if (this.translateToWormsMap.containsKey(scientificName)) {
				String newName = this.translateToWormsMap.get(scientificName);
				if (this.taxaWormsMap.containsKey(newName)) {
					return taxaWormsMap.get(newName);
				}
			}
		}
		return "";
	}

	public void addTaxaWorms(String scientificName, String aphiaId) {

		this.taxaWormsMap.put(scientificName, aphiaId);
	}

	public void addTranslateToWorms(String scientificNameFrom, String scientificNameTo) {

		this.translateToWormsMap.put(scientificNameFrom, scientificNameTo);
	}
}
