/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.species_old;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import se.smhi.sharkadm.fileimport.misc.FileImportDyntaxaWhiteList;
import se.smhi.sharkadm.utils.ErrorLogger;

public class ExportSpeciesList {
	
	private static TaxonManager taxonManager = TaxonManager.instance();
	private static PrintWriter out = null;
	
	private static String[] header = {
		"Gällande namn",
		"Auktor",
		"Taxontyp",
		"Taxon-id",
		"Synonymer",
//		"ITIS-nummer",
//		"ERMS-namn",
		"Klass",
		"Ordning",
		"Familj",
		"Släkte",
		"Taxonomisk hierarki",
		"RecommendedGUID"
	};

	public static void export(String exportDirectory, String fileName) {
		
		// Whitelist used to reduce number of taxa in list.
		FileImportDyntaxaWhiteList.instance().importFile();
		
		try {
			File exportdir = new File(exportDirectory);
			File file = new File(exportdir, fileName);
			out = new PrintWriter(new FileWriter(file));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		String headerString = "";
		String delimiter = "";
		for (String item: header) {
			headerString += delimiter;
			headerString += item;
			delimiter = "	";
		}
//		ErrorLogger.println(headerString); // Debug.
		out.println(headerString);
				
		List<TaxonNode> taxonNodeList = taxonManager.getTaxonList();
		
		String[] row = new String[100];
		
		traverseAndPrint(taxonNodeList, row);
		out.close();
	}
	
	private static void traverseAndPrint(List<TaxonNode> taxonNodeList, String[] row) {
		for (TaxonNode node : taxonNodeList) {
			printRow(node.getTaxonObject());
		}
	}
	
	private static void printRow(TaxonObject taxon) {
		String[] row = {"", "", "", "", "", "", "", "", "", "", ""};
		String tmp = "";
		TaxonNameObject nameObject = taxon.getValidNameObject();
		
		if (nameObject == null) {
			ErrorLogger.println("ERROR: No valid name found. Taxon-id: " + taxon.getDyntaxaId());
		}

		// Check if taxon is valid. Don't list invalid taxa in the public list.   
		if ( ! taxon.isActive()) {
			ErrorLogger.println(
				"ExportSpeciesList: Taxon not valid. " + 
				"Taxon-id: " + taxon.getDyntaxaId() + 
				" Name: " + nameObject.getName() + 
				" Valid from: " + taxon.getValidFromDate() + 
				" Valid to: " + taxon.getValidToDate());
			return;
		}
		
		// "Gällande namn"
		if (nameObject != null) {
			row[0] = nameObject.getName();
		} else {
			row[0] = "NO VALID NAME";
		}
		
		// "Auktor"
		if (nameObject != null) {
			row[1] = nameObject.getAuthor();
		} else {
			row[1] = "";
		}
		
		// "Taxontyp"
//		String taxonRank = taxonManager.convertTaxonTypeCodeToString(((TaxonObject) taxon).getTaxonTypeId());
		String taxonRank = taxon.getTaxonRank();
		row[2] = taxonRank;
		
		// "Taxon-id"
		row[3] = Integer.toString(taxon.getDyntaxaId());;
		
		// "Synonymer"
		row[4] = taxon.getSynonymNames();
		
//		// "ITIS-nummer"
//		row[5] = taxon.getTaxonName(10);
//		
//		// "ERMS-namn"
//		row[6] = taxon.getTaxonName(11);
		
		// "Class"
		row[5] = "";
		if (taxonRank.equals("class")) {
			row[5] = nameObject.getName();
		}
		
		// "Order"
		row[6] = "";
		if (taxonRank.equals("order")) {
			row[6] = nameObject.getName();
		}
		
		// "Family"
		row[7] = "";
		if (taxonRank.equals("family")) {
			row[7] = nameObject.getName();
		}
		
		// "Genus"
		row[8] = "";
		if (taxonRank.equals("genus")) {
			row[8] = nameObject.getName();
		}
		
		// "Taxonomisk hierarki"
		if (nameObject != null) {
			tmp = nameObject.getName();
		} else {
			tmp = "";
		}
		TaxonNode parentNode = taxon.getTaxonNode().getParent();
		while (parentNode != null) {
			try {
				TaxonObject parentObject = parentNode.getTaxonObject();
				TaxonNameObject parentNameObject = parentObject.getValidNameObject();
//				String parentRank = taxonManager.convertTaxonTypeCodeToString(parentObject.getTaxonTypeId());
				String parentRank = parentObject.getTaxonRank();
				String parentName = parentNameObject.getName();
				
				// Class, order, family, genus.
				if (parentRank.equals("class")) {
					row[5] = parentName;
				}
				if (parentRank.equals("order")) {
					row[6] = parentName;
				}
				if (parentRank.equals("family")) {
					row[7] = parentName;
				}
				if (parentRank.equals("genus")) {
					row[8] = parentName;
				}
				
				tmp = parentName + " - " + tmp;
				
			} catch (Exception e) {
				tmp = "NO VALID NAME" + " - " + tmp;
			}
			parentNode = parentNode.getParent();
		}
		row[9] = tmp;
		
		
		// "RecommendedGUID" 
		row[10] = taxon.getRecommendedGUID();
		
		
//		ErrorLogger.println(row[0] + "	" + row[1] + "	" + row[2] + 
//				"	" + row[3] + "	" + row[4] + "	" + row[5] +
//				"	" + row[6] + "	" + row[7] + "	" + row[8]);
//		String outRow = row[0] + "	" + row[1] + "	" + row[2] + 
//				"	" + row[3] + "	" + row[4] + "	" + row[5] +
//				"	" + row[6] + "	" + row[7] + "	" + row[8];

		String outRow = row[0] + "	" + row[1] + "	" + row[2] + 
				"	" + row[3] + "	" + row[4] + "	" + row[5] +
				"	" + row[6] + "	" + row[7] + "	" + row[8] +
				"	" + row[9] + "	" + row[10];

		// Replace characters DB don't like.
		if (outRow.indexOf("'") >= 0) {
			outRow = outRow.replace("'", "");
		} 
		if (outRow.indexOf("´") >= 0) {
			outRow = outRow.replace("´", "");
		}
		if (outRow.indexOf("×") >= 0) {
			outRow = outRow.replace("×", "x");
		}
		
		
//		// Only store to db if taxa in whitelist or higher taxa from whitelist taxa.
		if (FileImportDyntaxaWhiteList.instance().isWhiteListParent(row[0])) {
			out.println(outRow);
			return;
		} 
		// Rank = genus.
		String rank = FileImportDyntaxaWhiteList.instance().getWhitelistRank(row[8]);
		if (rank.equals("genus")) {
			out.println(outRow);
			return;
		}				
		// Rank = family.
		rank = FileImportDyntaxaWhiteList.instance().getWhitelistRank(row[7]);
		if (rank.equals("family")) {
			out.println(outRow);
			return;
		}				
		// Rank = order.
		rank = FileImportDyntaxaWhiteList.instance().getWhitelistRank(row[6]);
		if (rank.equals("order")) {
			out.println(outRow);
			return;
		}				
		// Rank = class.
		rank = FileImportDyntaxaWhiteList.instance().getWhitelistRank(row[5]);
		if (rank.equals("class")) {
			out.println(outRow);
			return;
		}				
	}
	
}
