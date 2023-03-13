/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.calc;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import se.smhi.sharkadm.datasets.fileimport.FileImportInfo;
import se.smhi.sharkadm.fileimport.misc.FileImportBenticQualityIndex;
import se.smhi.sharkadm.species_old.TaxonManager;
import se.smhi.sharkadm.species_old.TaxonNameObject;
import se.smhi.sharkadm.species_old.TaxonNode;
import se.smhi.sharkadm.species_old.TaxonObject;

public class BqiSpecies {

	private static BqiSpecies instance = new BqiSpecies(); // Singleton.
	private static boolean filesLoaded = false;
	
	// Species dictionaries and lists.
	private Map<String, Double> westcoastTaxa = new HashMap<String, Double>();
	private Map<String, Double> westcoastTaxaLookup = new HashMap<String, Double>();
	private Map<String, Double> eastcoastTaxa = new HashMap<String, Double>();
	private Map<String, Double> eastcoastTaxaLookup = new HashMap<String, Double>();
	private Map<String, Double> westcoastBedaTaxa = new HashMap<String, Double>();
	private Map<String, Double> westcoastBedaTaxaLookup = new HashMap<String, Double>();
	private Map<String, Double> eastcoastBedaTaxa = new HashMap<String, Double>();
	private Map<String, Double> eastcoastBedaTaxaLookup = new HashMap<String, Double>();
	
	private Map<String, String> excludeTaxa = new HashMap<String, String>();
	private Map<String, Boolean> excludeTaxaLookup = new HashMap<String, Boolean>();
	
	// Taxa groups for Chironomidae, Clitellata (Oligochaeta) and Ostracoda.
	private Map<String, String> groupedTaxa = new HashMap<String, String>();
	
	private FileImportInfo importInfo = null;
	
	private BqiSpecies() { // Singleton.
		
	}
	
	public static BqiSpecies instance() { // Singleton.
		return instance;
	}
	
	public void loadSpeciesLists(FileImportInfo importInfo) {
		
		this.importInfo = importInfo;
		
		// Load first time only.
		if (filesLoaded == false) {
			filesLoaded = true;
			this.clearSpecies();
			PrintStream dummyStream = null;
			new FileImportBenticQualityIndex(dummyStream).importFiles("");
		}
	}

	public void clearSpecies() {
		this.westcoastTaxa.clear();
		this.eastcoastTaxa.clear();
		this.westcoastBedaTaxa.clear();
		this.eastcoastBedaTaxa.clear();
		this.excludeTaxa.clear();
		this.excludeTaxaLookup.clear();
	}
	
	public boolean checkIfSpeciesListsAreLoaded() {
		// Check if all mandatory files are loaded.
		if (this.westcoastTaxa.size() <= 1) { return false; }
		if (this.eastcoastTaxa.size() <= 1) { return false; }
		if (this.westcoastBedaTaxa.size() <= 1) { return false; }
		if (this.eastcoastBedaTaxa.size() <= 1) { return false; }
		if (this.excludeTaxa.size() <= 1) { return false; }
		return true;
	}
	
	public Boolean isSpeciesExcluded(String scientificName) {
		
		// Check if used earlier. Reuse result.
		if (!excludeTaxaLookup.containsKey(scientificName)) {
			// Check if scientific name is used.
			if (excludeTaxa.containsKey(scientificName)) {
				excludeTaxaLookup.put(scientificName, true);
//				System.out.println("DEBUG BQI: Excluded: " + scientificName);
			} else {
				// Check all levels in classification chain.
				excludeTaxaLookup.put(scientificName, false);
				String taxonId = TaxonManager.instance().getTaxonIdFromName(scientificName);
				TaxonNode taxonNode = TaxonManager.instance().getTaxonNodeFromImportId(taxonId);
				if (taxonNode != null) {
					TaxonNode parentNode = taxonNode.getParent();
					while (parentNode != null) {
						TaxonObject parentObject = parentNode.getTaxonObject();
						
						
//						String validName = parentObject.getValidNameObject().getName();
//						if (excludeTaxa.containsKey(validName)) {
//							excludeTaxaLookup.put(scientificName, true);
////						System.out.println("DEBUG BQI: Excluded: " + validName + " : " + scientificName);
//							break;
//						}

						TaxonNameObject taxonNameObject = parentObject.getValidNameObject();
						if (taxonNameObject != null) {
						String validName = parentObject.getValidNameObject().getName();
						if (excludeTaxa.containsKey(validName)) {
							excludeTaxaLookup.put(scientificName, true);
//							System.out.println("DEBUG BQI: Excluded: " + validName + " : " + scientificName);
							break;
							}
						} else {
							this.importInfo.addConcatWarning("BQIm: Taxa not found in DynTaxa: " + scientificName);
//							System.out.println("DEBUG: " + scientificName);
						}
						parentNode = parentNode.getParent();
					}
				}
			}
		}
		return excludeTaxaLookup.get(scientificName);
	}	

//	public Boolean isSpeciesClassified(String scientificName) {
//		
//		Boolean result = false;
//		if (westcoastTaxaLookup.containsKey(scientificName) || eastcoastTaxaLookup.containsKey(scientificName)) {
//			result = true;
//		}
//		return result;
//	}	

	public Double getSensitivityValue(String scientificName, String westEastArea, Boolean beda) {
		
		Double value = null;
		Map<String, Double> taxaValues = null;
		Map<String, Double> taxaLookup = null;
		// Select lists to use.
		if (!beda) {
			if (westEastArea.equals("EAST")) {
				taxaValues = eastcoastTaxa;
				taxaLookup = eastcoastTaxaLookup;
			}
			else if (westEastArea.equals("WEST")) {
				taxaValues = westcoastTaxa;
				taxaLookup = westcoastTaxaLookup;
			}
		} else {
			if (westEastArea.equals("EAST")) {
				taxaValues = eastcoastBedaTaxa;
				taxaLookup = eastcoastBedaTaxaLookup;
			}
			else if (westEastArea.equals("WEST")) {
				taxaValues = westcoastBedaTaxa;
				taxaLookup = westcoastBedaTaxaLookup;
			}
		}
		// Check if used earlier. Reuse result.
		if (!taxaLookup.containsKey(scientificName)) {
			// Check if scientific name is used.
			if (taxaValues.containsKey(scientificName)) {
				taxaLookup.put(scientificName, taxaValues.get(scientificName));
//				System.out.println("DEBUG BQI: SensitivityValue Taxa: " + scientificName + "  Value: " + taxaValues.get(scientificName));
			} else {
				// Check all levels in classification chain.
				taxaLookup.put(scientificName, null);
				String taxonId = TaxonManager.instance().getTaxonIdFromName(scientificName);
				TaxonNode taxonNode = TaxonManager.instance().getTaxonNodeFromImportId(taxonId);
				if (taxonNode != null) {
//					TaxonNode parentNode = taxonNode.getParent();
					TaxonNode parentNode = taxonNode;
					while (parentNode != null) {
						TaxonObject parentObject = parentNode.getTaxonObject();
						if (parentObject.getValidNameObject() != null) {
							String validName = parentObject.getValidNameObject().getName();
							// Chironomidae, Clitellata (Oligochaeta) and Ostracoda
							if (validName.equals("Chironomidae") || 
								validName.equals("Clitellata") || 
								validName.equals("Oligochaeta") || 
								validName.equals("Ostracoda") ) {
								groupedTaxa.put(scientificName, validName);
							}
							if (taxaValues.containsKey(validName)) {
								
								
								
//								if (	//validName.equals("Idotea") ||
//										//validName.equals("Lymnaeidae") ||
//										//validName.equals("Hydrobiidae") ||
//										//validName.equals("Nemertea") ||
//										validName.equals("Nephtys") ||
//										validName.equals("Eteone") ||
//										validName.equals("Marenzelleria") ||
//										validName.equals("Capitella") ||
//										validName.equals("Gammarus") ||
//										validName.equals("Jaera") ||
//										validName.equals("Turbellaria") ) {
//									System.out.println("DEBUG-1: " + validName + "    " + scientificName);
//								}
								
								
								
								taxaLookup.put(scientificName, taxaValues.get(validName));
								System.out.println("DEBUG BQI: SensitivityValue Taxa: [" + validName + "] " + scientificName + "  Value: " + taxaValues.get(validName));
								break;
							}
						} else {
							System.out.println("DEBUG BQI: No valid name for some level in classification for: " + scientificName);
							this.importInfo.addConcatWarning("BQIm: No valid name for some level in classification for: " + scientificName);
						}
						parentNode = parentNode.getParent();
					}
				}
			}
		}
		
		return taxaLookup.get(scientificName);

	}
		
	public String translateToGroupedTaxa(String scientificName) {
		
		if (groupedTaxa.containsKey(scientificName)) {
			return groupedTaxa.get(scientificName);
		}
		return scientificName;
	}

	public void addClassifiedSpecies(String scientificName, String sensitivityValue, String westEastArea) {
		
		String validName = scientificName;
		try {
			// Translate scientific name.
			String taxonId = TaxonManager.instance().getTaxonIdFromName(scientificName);
			TaxonNode taxonNode = TaxonManager.instance().getTaxonNodeFromImportId(taxonId);
			validName = taxonNode.getTaxonObject().getValidNameObject().getName();
			
			if (!scientificName.equals(validName)) {
				if (importInfo != null) {
					this.importInfo.addConcatInfo("BQIm: Importing sensitivity values. Scientific name translated: " + scientificName + " to " + validName);
				}
			}
		} catch (Exception e) {		
			if (importInfo != null) {
				this.importInfo.addConcatWarning("BQIm: Importing sensitivity values. Scientific name NOT found: " + scientificName);
			}
		}

		Double value = Double.parseDouble(sensitivityValue);
		
		if (westEastArea.equals("EAST")) {
			if (eastcoastTaxa.containsKey(validName)) {
				// Taxa probably renamed ("lump"). Check overwritten value.
				String oldSensitivityValue = eastcoastTaxa.get(validName).toString();
				this.importInfo.addConcatWarning("BQIm: Multiple alternatives for sensitivity values, EAST. Scientific name (translated): " + validName + 
						                         " Value old/new: " + oldSensitivityValue + " / " + sensitivityValue);
			}
			//
			eastcoastTaxa.put(validName, value);
			
			
			// System.out.println("DEBUG-EAST: " + validName + "    " + sensitivityValue);
			
			
			
		}
		else if (westEastArea.equals("WEST")) {
			if (westcoastTaxa.containsKey(validName)) {
				// Taxa probably renamed ("lump"). Check overwritten value.
				String oldSensitivityValue = westcoastTaxa.get(validName).toString();
				this.importInfo.addConcatWarning("BQIm: Multiple alternatives for sensitivity values, WEST. Scientific name (translated): " + validName + 
						                         " Value old/new: " + oldSensitivityValue + " / " + sensitivityValue);
			}
			//
			westcoastTaxa.put(validName, value);
		}
		
		if (westEastArea.equals("EAST-BEDA")) {
			if (eastcoastBedaTaxa.containsKey(validName)) {
				// Taxa probably renamed ("lump"). Check overwritten value.
				String oldSensitivityValue = eastcoastBedaTaxa.get(validName).toString();
				this.importInfo.addConcatWarning("BQIm: Multiple alternatives for sensitivity values, EAST-BEDA. Scientific name (translated): " + validName + 
						                         " Value old/new: " + oldSensitivityValue + " / " + sensitivityValue);
			}
			//
			eastcoastBedaTaxa.put(validName, value);
			
			
			// System.out.println("DEBUG-EAST: " + validName + "    " + sensitivityValue);
			
			
			
		}
		else if (westEastArea.equals("WEST-BEDA")) {
			if (westcoastBedaTaxa.containsKey(validName)) {
				// Taxa probably renamed ("lump"). Check overwritten value.
				String oldSensitivityValue = westcoastBedaTaxa.get(validName).toString();
				this.importInfo.addConcatWarning("BQIm: Multiple alternatives for sensitivity values, WEST-BEDA. Scientific name (translated): " + validName + 
						                         " Value old/new: " + oldSensitivityValue + " / " + sensitivityValue);
			}
			//
			westcoastBedaTaxa.put(validName, value);
		}
		
	}
	
	public void addExcludedSpecies(String scientificName, String rank) {
		
		String validName = scientificName;
		try {
			// Translate scientific name.
			String taxonId = TaxonManager.instance().getTaxonIdFromName(scientificName);
			TaxonNode taxonNode = TaxonManager.instance().getTaxonNodeFromImportId(taxonId);
			validName = taxonNode.getTaxonObject().getValidNameObject().getName();
			
			if (!scientificName.equals(validName)) {
				if (importInfo != null) {
					this.importInfo.addConcatInfo("BQIm: Importing exclude taxa. Scientific name translated: " + scientificName + " to " + validName);
				}
			}
		} catch (Exception e) {		
			if (importInfo != null) {
				this.importInfo.addConcatWarning("BQIm: Importing exclude taxa. Scientific name NOT found: " + scientificName);
			}
		}
		
		excludeTaxa.put(validName, rank);
	}

	
//	// TEST
//	public static void main(String[] args) throws Exception {
//		
//		PrintStream dummyStream = null;
//		new FileImportBenticQualityIndex(dummyStream).importFiles("");
//		
//		Boolean excludedTaxa;
//		excludedTaxa = BqiSpecies.instance().isSpeciesExcluded("Metridium");
//		System.out.println("TEST: " + excludedTaxa.toString());
//		
//		Double value;
//		value = BqiSpecies.instance().getSensitivityValue("Tharyx killariensis", "WEST");
//		System.out.println("TEST: " + value.toString());
//    }
}
