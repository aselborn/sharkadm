/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.fileimport.misc;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import se.smhi.sharkadm.database.SaveSpecies;
import se.smhi.sharkadm.facades.ModelFacade;
import se.smhi.sharkadm.species_old.TaxonManager;
import se.smhi.sharkadm.species_old.TaxonNameObject;
import se.smhi.sharkadm.species_old.TaxonNode;
import se.smhi.sharkadm.species_old.TaxonObject;
import se.smhi.sharkadm.utils.ParseFileUtil;

public class FileImportDyntaxaWhiteList {

	private Map<String, String> whiteListMap = new HashMap<String, String>();
	private List<String> whiteListParents = new ArrayList<String>();

	public FileImportDyntaxaWhiteList() {
		this.importFile();
	}

	private static FileImportDyntaxaWhiteList instance = new FileImportDyntaxaWhiteList(); // Singleton.

	public static FileImportDyntaxaWhiteList instance() { // Singleton.
		return instance;
	}
	
	public String getWhitelistRank(String scientificName) {
		if (whiteListMap.containsKey(scientificName)) {
			return whiteListMap.get(scientificName);
		}
		return "";
	}

	public boolean isWhiteListParent(String scientificName) {
		if (whiteListParents.contains(scientificName)) {
			return true;
		}
		return false;
	}

	public void importFile() {
		List<String[]> fileContent;
		BufferedReader bufferedReader;

		try {
			bufferedReader = ParseFileUtil
					.GetSharkConfigFile("dyntaxa_whitelist.txt");

			fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
			if (fileContent != null) {
				importDyntaxaWhiteList(fileContent);
			}

		} catch (Exception e) {
			// Note: It is not recommended to put a message dialog here. Should
			// be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR
					| SWT.OK);
			messageBox.setText("Dyntaxa White List import");
			messageBox.setMessage("Failed to import 'dyntaxa_whitelist.txt'.");
			messageBox.open();
		}

		// Used by the Observer pattern.
		ModelFacade.instance().modelChanged();
	}

	private void importDyntaxaWhiteList(List<String[]> fileContent) {
		String[] header = null;

		int rowCounter = 1;
		int addedItems = 0;
		for (String[] row : fileContent) {
			if (header == null) {
				// The first line contains the header.
				header = row;
			} else {
				rowCounter++;
				String scientificName = row[0].trim(); // "scientific_name".
				String rank = row[1].trim(); // "rank".

				if ((!scientificName.equals("")) && (!rank.equals(""))) {
					whiteListMap.put(scientificName, rank);
				}

				// Add all parents to the valid scientific name list.
				String dyntaxaId = TaxonManager.instance().getTaxonIdFromName(scientificName);
				if (dyntaxaId.equals("")) {
					continue;
				}
				TaxonNode taxonNode = TaxonManager.instance().getTaxonNodeFromImportId(dyntaxaId);
				TaxonNode parentNode = taxonNode.getParent();
				while (parentNode != null) {
					try {
						TaxonObject parentObject = parentNode.getTaxonObject();
						TaxonNameObject parentNameObject = parentObject.getValidNameObject();
						String parentName = parentNameObject.getName();
						
						if (!whiteListParents.contains((parentName))) {
							whiteListParents.add(parentName);
						}
						
					} catch (Exception e) {
						
					}
					parentNode = parentNode.getParent();
				}
				
				addedItems++;
			}
		}
	}
}
