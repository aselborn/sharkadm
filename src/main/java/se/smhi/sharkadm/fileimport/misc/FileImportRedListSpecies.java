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

import se.smhi.sharkadm.facades.ModelFacade;
import se.smhi.sharkadm.utils.ParseFileUtil;

public class FileImportRedListSpecies {

	private Map<String, String> dyntaxaIdToRedListCategoryMap = new HashMap<String, String>();

	public FileImportRedListSpecies() {
		this.importFile();
	}

	public String getRedListCategory(String dyntaxaId) {
		if (dyntaxaIdToRedListCategoryMap.containsKey(dyntaxaId)) {
			return dyntaxaIdToRedListCategoryMap.get(dyntaxaId);
		}
		return "Ej r�dlistad";
	}

	public void importFile() {
		List<String[]> fileContent;
		BufferedReader bufferedReader;

		try {
			bufferedReader = ParseFileUtil
					.GetSharkConfigFile("red_list_species.txt");

			fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
			if (fileContent != null) {
				importRedListSpeciesList(fileContent);
			}

		} catch (Exception e) {
			// Note: It is not recommended to put a message dialog here. Should
			// be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR
					| SWT.OK);
			messageBox.setText("Red list species import");
			messageBox.setMessage("Failed to import 'red_list_species.txt'.");
			messageBox.open();
		}

		// Used by the Observer pattern.
		ModelFacade.instance().modelChanged();
	}

	private void importRedListSpeciesList(List<String[]> fileContent) {
		String[] header = null;

		int rowCounter = 1;
		int addedItems = 0;
		for (String[] row : fileContent) {
			if (header == null) {
				// The first line contains the header.
				header = row;
			} else {
				rowCounter++;
				// Available columns:

				// 0: TaxonId
				// Svenskt namn
				// Vetenskapligt namn
				// Organismgrupp
				// Kategori
				// Observationer
				// Landskapstyp
				// 7: R�dlistekategori
				// R�dlistekriterium

				String taxonId = row[0].trim(); // "TaxonId".
				String category = row[7].trim(); // "Kategori".

				if ((!taxonId.equals("")) && (!category.equals(""))) {
					dyntaxaIdToRedListCategoryMap.put(taxonId, category);
				}

				addedItems++;
			}
		}
	}
}
