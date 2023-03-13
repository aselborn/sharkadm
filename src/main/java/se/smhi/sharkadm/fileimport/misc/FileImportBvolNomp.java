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
import se.smhi.sharkadm.species.BvolManager;
import se.smhi.sharkadm.species.BvolObject;
import se.smhi.sharkadm.species.TrophicTypeManager;
import se.smhi.sharkadm.species.TrophicTypeObject;
import se.smhi.sharkadm.utils.ParseFileUtil;

public class FileImportBvolNomp extends SingleFileImport {

	public FileImportBvolNomp(PrintStream logInfo) {
		super(logInfo);
	}

	public void importFiles(String zipFileName, Dataset dataset) {
		
	}

	public void importFiles(String zipFileName) {
		List<String[]> fileContent;
		BufferedReader bufferedReader;
		
		try {
			bufferedReader = ParseFileUtil.GetSharkConfigFile("bvol_nomp.txt");

			fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
			if (fileContent != null) {				
				importContent(fileContent);
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
	
	private void importContent(List<String[]> fileContent) {
		String[] header = null;
				
		for (String[] row : fileContent) {
			if (header == null) { 
				// The first line contains the header.
				header = row;
				checkHeader(header);
			} else {
				BvolObject bvolObject = new BvolObject();
				
// [List, HELCOM area, OSPAR area, Division, Class, Order, Genus, 
// Species, SFLAG, STAGE, Author, AphiaID, AphiaID_link, Trophy, Geometric_shape, FORMULA, 
// SizeClassNo, Nonvalid_SIZCL, Not_accepted_name, Unit, SizeRange, Length(l1)µm, Length(l2)µm, Width(w)µm, Height(h)µm, 
// Diameter(d1)µm, Diameter(d2)µm, No_of_cells/counting_unit, Calculated_volume_um3, Comment, 
// Filament_length_of_cell(µm), Calculated_Carbon_pg/counting_unit, Comment_on_Carbon_calculation, 
// Corrections/Additions, Synonyms - NOT IMPORTED, NOT handled by ICES, WORMS Rank, Comment_on_colony_form]
				
				bvolObject.setReferenceList(getCell(row, "List"));
				bvolObject.setScientificName(getCell(row, "Species"));
				bvolObject.setAphiaId(getCell(row, "AphiaID"));
				bvolObject.setSizeClass(getCell(row, "SizeClassNo"));
				bvolObject.setTrophicType(getCell(row, "Trophy"));
				bvolObject.setCalculatedVolume(getCell(row, "Calculated_volume_um3"));
				bvolObject.setCalculatedCarbon(getCell(row, "Calculated_Carbon_pg/counting_unit"));
								
				BvolManager.instance().addBvol(bvolObject);
			}
		}
	}

	@Override
	public void visitDataset(Dataset dataset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitVisit(Visit visit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitSample(Sample sample) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitVariable(Variable variable) {
		// TODO Auto-generated method stub
		
	}
}
