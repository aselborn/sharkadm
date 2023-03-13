/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.species_old;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.smhi.sharkadm.database.SaveSpecies;
import se.smhi.sharkadm.fileimport.misc.FileImportDyntaxaWhiteList;

public class TaxonManager {
	
	private static TaxonManager instance = new TaxonManager(); // Singleton.
	
	// Top nodes in taxonomy tree.
	private List<TaxonNode> topNodeList = new ArrayList<TaxonNode>(); 
	
	// Maps taxon-id string to taxon node object.
	private Map<String, TaxonNode> importIdLookup = new HashMap<String, TaxonNode>();
	
	// Maps taxon-name to taxon-id string.
	// Used to map imported environment monitoring data to taxon-id. 
	// May contain more taxa than in the taxonomy tree.
	private Map<String, String> taxonNameLookup = new HashMap<String, String>();
	
	private Boolean pegListImported = false;
	
	private static String[] taxonTypes = new String[] { 
//		"", "Kingdom", "Phylum", "Subphylum", "Superclass", 
//		"Class", "Subclass", "Superorder", "Order", "Suborder", 
//		"Superfamily", "Family", "Subfamily", "Tribe", "Genus", 
//		"Subgenus", "Section", "Species", "Subspecies", "Variety", 
//		"Form", "Hybrid", "Cultural variety", "Population", 
//		"Group of families", "Infraclass", "Parvclass", 
//		"Sensu latu", "Species pair", "Infraorder" };
	
//		// 2012:
//		"", "Rike", "Stam", "Understam", "Överklass", "Klass", "Underklass", 
//		"Överordning", "Ordning", "Underordning", "Överfamilj", 
//		"Familj", "Underfamilj", "Tribus", 
//		"Släkte", "Undersläkte", "Sektion", 
//		"Art", "Underart", "Varietet", "Form", "Hybrid", 
//		"Sort", "Population", "Infraklass", "Parvklass", 
//		"Kollektivtaxon", "Artkomplex", "Infraordning", 
//		"Avdelning", "Underavdelning", "Morfotyp", 
//		"Organismgrupp", "Domän", "Underrik", "Gren", 
//		"Infrarike", "Överstam", "Infrastam", "Överavdelning", 
//		"Infraavdelning", "Infrafamilj", "Övertribus", 
//		"Undertribus", "Infratribus", "Undersektion", "Serie", 
//		"Underserie", "Aggregat", "Småart", "Sortgrupp" };

//		// 2014:
//		"", "Rot", "Rike", "StamUnderstam", "Överklass", "Klass", 
//		"Underklass", "Överordning", "Ordning", "Underordning", 
//		"Överfamilj", "Familj", "Underfamilj", "Tribus", "Släkte", 
//		"Undersläkte", "Sektion", "Art", "Underart", "Varietet", 
//		"Form", "Hybrid", "Sort", "Population", "", "Infraklass", 
//		"Parvklass", "Kollektivtaxon", "Artkomplex", "Infraordning", 
//		"Avdelning", "Underavdelning", "Morfotyp", "Organismgrupp", 
//		"Domän", "Underrike", "Gren", "Infrarike", "Överstam", 
//		"Infrastam", "Överavdelning", "Infraavdelning", "Infrafamilj", 
//		"Övertribus", "Undertribus", "Infratribus", "Undersektion", 
//		"Serie", "Underserie", "Aggregat", "Småart", "Sortgrupp", 
//		"Ranglös"

		// 2021, DarwinCore export. Also check FileImportDynamicTaxa.java.
		"",
		"domain",
		"superkingdom",
		"kingdom",
		"subkingdom",
		"infrakingdom",
			
		"superphylum",
		"phylum",
		"subphylum",
		"infraphylum",
			
		"superdivision",
		"division",
		"subdivision",
		
		"superclass",
		"class",
		"subclass",
		"infraclass",
		"parvclass",
			
		"superorder",
		"order",
		"suborder",
		"infraorder",
		"parvorder",
			
		"superfamily",
		"family",
		"subfamily",
		"infrafamily",
			
		"supertribe",
		"tribe",
		"subtribe",
		"infratribe",
		
		"genus",
		"subgenus",
			
		"superspecies",
		"species",
		"subspecies",
		"variety",
		"form",
			
		"section",
		"unranked",
		"cultivar",
		"speciesAggregate",
		"forma specialis",
	};


	private TaxonManager() { // Singleton.
	}
	
	public static TaxonManager instance() { // Singleton.
		return instance;
	}
	
	public void clearLists() {
		topNodeList.clear();
//		nameLookup.clear();
		importIdLookup.clear();
	}

	public void addTopNode(TaxonNode taxonNode) {
		this.topNodeList.add(taxonNode);
	}

	public void addImportIdLookup(String importId, TaxonNode taxonode) {
		this.importIdLookup.put(importId, taxonode);
	}

	public void addImportNameLookup(String taxonName, String taxonId) {
		if (taxonName.equals("")) {
///			ErrorLogger.println("DEBUG: Taxonname=<empty string>. TaxonId: " + taxonId);
		} else {
//			if ((taxonNameLookup.containsKey(taxonName)) &&
//				(!taxonId.equals(taxonNameLookup.get(taxonName)))	) {
//
//				// Multiple alternatives to 
//				ErrorLogger.println("DEBUG: Duplicated data in taxonNameLookup. " + taxonName + " " + taxonId + " " + taxonNameLookup.get(taxonName));
//				this.taxonNameLookup.put(taxonName, "0");
//			} else {
				this.taxonNameLookup.put(taxonName, taxonId);
			}
//		}
	}

	public List<TaxonNode> getTopNodeList() {
		return topNodeList;
	}

	public Map<String, TaxonNode> getImportIdLookup() {
		return importIdLookup;
	}

//	public Map<String, String> getImportNameLookup() {
//		return taxonNameLookup;
//	}

	public TaxonNode getTaxonNodeFromImportId(String importId) {
		if (importIdLookup.containsKey(importId)) {
			return importIdLookup.get(importId);
		} else {
			return null;
		}
	}
	
	public String getTaxonIdFromName(String taxonName) {
		if (taxonName.equals("")) {
///			ErrorLogger.println("DEBUG: TaxonManager:getTaxonIdFromName: taxonName=<empty string>. ");
			return "";
		}
		if (taxonNameLookup.containsKey(taxonName)) {
			return taxonNameLookup.get(taxonName);
		} else {
			// Conversion from "Aaa bbb v. ccc" to "Aaa bbb var. ccc". 
			// (This was earlier handled in the species_peg_to_dyntaxa.txt file.)
			if (taxonName.contains(" v. ")) {
				String tmpTaxonName = taxonName.replace(" v. ", " var. ");
				if (taxonNameLookup.containsKey(tmpTaxonName)) {
					return taxonNameLookup.get(tmpTaxonName);
				}				
			}			
		}
		return "";
	}
	
	// Converts the tree to a list. 
	public List<TaxonNode> getTaxonList() {
		List<TaxonNode> taxonList = new ArrayList<TaxonNode>();
		for (TaxonNode node : topNodeList) {
			taxonList.add(node);
			node.addChildrenToList(taxonList);
		}
//		ErrorLogger.println("TAXONLIST Size: " + taxonList.size());
		return taxonList;
	}
	
	// Used to sort topNodeList and children lists.
	public void sortTaxonLists() {
		Collections.sort(topNodeList, new TaxonNameComparator());
		for (TaxonNode node : topNodeList) {
			node.sortChildrenLists();
		}
	}
	
	public void saveSpeciesToDatabase() {
		SaveSpecies.instance().deleteSpecies();
		
		// Taxon with taxonid = 0 needed to handle non community variables.
		SaveSpecies.instance().insertSpeciesDummy();
		
		// Whitelist used to reduce number of taxa stored in Sharkweb.
		FileImportDyntaxaWhiteList.instance().importFile();
		
		for (TaxonNode taxonNode : this.getTaxonList()) {
			SaveSpecies.instance().insertSpecies(taxonNode.getTaxonObject());
//			for (PEGObject pegObject : taxonNode.getPegList()) {
//				SaveSpecies.instance().insertSpeciesPeg(pegObject);
//			}
		}
	}

	public void removeSpeciesInDatabase() {
		SaveSpecies.instance().deleteSpecies();
	}

	// Utility:
	public static String convertTaxonTypeCodeToString(int TaxonTypeCode) {
		return taxonTypes[TaxonTypeCode];
	}

	public Boolean isPegListImported() {
		return pegListImported;
	}

	public void setPegListImported(Boolean pegListImported) {
		this.pegListImported = pegListImported;
	}
}
