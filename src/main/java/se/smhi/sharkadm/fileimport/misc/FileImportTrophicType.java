/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.fileimport.misc;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import se.smhi.sharkadm.datasets.fileimport.SingleFileImport;
import se.smhi.sharkadm.facades.ModelFacade;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.species.TrophicTypeManager;
import se.smhi.sharkadm.species.TrophicTypeObject;
import se.smhi.sharkadm.utils.ParseFileUtil;

public class FileImportTrophicType extends SingleFileImport {
	
//	public FileImportTrophicType(PrintStream logInfo) {
//		super(logInfo);
//	}
	public FileImportTrophicType(PrintStream logInfo) {
		super(logInfo);
	}
	
	public void importFiles(String zipFileName, Dataset dataset) {
		
	}

	public void importFiles(String zipFileName) {
		List<String[]> fileContent;
		BufferedReader bufferedReader;
		
		try {
			bufferedReader = ParseFileUtil.GetSharkConfigFile("trophictype_smhi.txt");

			fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
			if (fileContent != null) {				
				importSpeciesTrophicTypeList(fileContent);
			}
			
		} catch (Exception e) {
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Trophic type import");
			messageBox.setMessage("Failed to import trophic type list. Error: " + e.getMessage());
			messageBox.open();
		}
		
		// Used by the Observer pattern.
		ModelFacade.instance().modelChanged();
	}
	
	private void importSpeciesTrophicTypeList(List<String[]> fileContent) {
		String[] header = null;
//		setExpectedColumns(stationHeader);
		
		int rowCounter = 1;
		int addedItems = 0;
		for (String[] row : fileContent) {
			if (header == null) { 
				// The first line contains the header.
				header = row;
				checkHeader(header);
			} else {
				rowCounter++;				
				// Used columns:
				// - scientific_name
				// - size_class
				// - trophic_type

				TrophicTypeObject trophicTypeObject = new TrophicTypeObject();
				trophicTypeObject.setScientificName(getCell(row, "scientific_name"));
				trophicTypeObject.setSizeClass(getCell(row, "size_class"));
				trophicTypeObject.setTrophicType(getCell(row, "trophic_type"));
								
				TrophicTypeManager.instance().addTrophicType(trophicTypeObject);

				addedItems++;
			}
		}
		System.out.println("INFO: Added trophic types (scientific_name/size_class): " + addedItems + ".");
	}

	@Override
	public void visitDataset(Dataset dataset) {
	}

	@Override
	public void visitSample(Sample sample) {
	}

	@Override
	public void visitVariable(Variable variable) {
	}

	@Override
	public void visitVisit(Visit visit) {
	}
}
