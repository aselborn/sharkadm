/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.fileimport.misc;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.List;

import se.smhi.sharkadm.datasets.fileimport.SingleFileImport;
import se.smhi.sharkadm.facades.ModelFacade;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.species_old.PEGObject;
import se.smhi.sharkadm.species_old.TaxonManager;
import se.smhi.sharkadm.species_old.TaxonNode;
import se.smhi.sharkadm.utils.ParseFileUtil;

public class FileImportPEGList extends SingleFileImport {

	private static TaxonManager taxonManager = TaxonManager.instance();
	
	public FileImportPEGList(PrintStream logInfo) {
		super(logInfo);
	}
	
	public void importFiles(String zipFileName, Dataset dataset) {
		
	}
	
	public void importFiles(String zipFileName) {
		
		// Avoid multiple calls to this method.
		if (taxonManager.isPegListImported()) {
			return;
		}
		taxonManager.setPegListImported(true);
		
		List<String[]> fileContent;
//		ClassLoader classLoader = this.getClass().getClassLoader();
//		InputStream inputStream;
		BufferedReader bufferedReader;
		
		// Import BVOL NOMP. 
		bufferedReader = ParseFileUtil.GetSharkConfigFile("bvol_nomp.txt");
		fileContent = ParseFileUtil.parseDataFile(bufferedReader, false);
		if (fileContent != null) {
			importPegList(fileContent);
			// Sort top node list and children lists on valid names.
			taxonManager.sortTaxonLists();
		}

		// Used by the Observer pattern.
		ModelFacade.instance().modelChanged();
	}
	
	private void importPegList(List<String[]> fileContent) {
		
		String[] header = null;
//		setExpectedColumns(pegHeader);
				
//		logInfo.add("");
//		logInfo.add("PEG_BVOL file: " + fileContent.size() + " rows");

		int rowCounter = 1;
		int addedItems = 0;
		for (String[] row : fileContent) {
			if (header == null) { 
				// The first line contains the header.
				header = row;
				checkHeader(header);
			} else {
				rowCounter++;

// PEG 2013: Division	Class	Order	Species	SFLAG	STAGE	Author	AphiaID	Trophy	Geometric shape	FORMULA	SizeClassNo	Nonvalid_SIZCL	Not_accepted	Unit	Size range	Length(l1)µm	Length(l2)µm	Width(w)µm	Height(h)µm	Diameter(d1)µm	Diameter(d2)µm	No. of cells/ counting unit	Calculated  volume µm3	Comment	Filament: length of cell (µm)	Calculated Carbon pg/counting unit	Comment on Carbon calculation	Corrections/Additions 2013
// PEG 2014: Division	Class	Order	Species	SFLAG	STAGE	Author	AphiaID	AphiaID_link	Trophy	Geometric_shape	FORMULA	SizeClassNo	Nonvalid_SIZCL	Not_accepted_name	Unit	SizeRange	
// Length(l1)µm	Length(l2)µm	Width(w)µm	Height(h)µm	Diameter(d1)µm	Diameter(d2)µm	No._of_cells/counting_unit	Calculated _volume_µm3	Comment	
// Filament_length_of_cell(µm)	Calculated_Carbon_pg/counting_unit	Comment_on_Carbon_calculation	Corrections/Additions_2014	Synonyms - NOT IMPORTED, NOT handled by ICES
				

								
				PEGObject pegObject = new PEGObject();
				
//				"Division", 
//				"Class", 
//				"Order", 
//				"Species", 
				pegObject.setSpecies(getCell(row, "Species"));
//				"SFLAG (sp., spp., cf., complex, group)",
//				"SFLAG (SP, SPP, CF, CPX, GRP)",
//				"SFLAG",
				pegObject.setSpecies_flag_code(getCell(row, "SFLAG"));
//				"STAGE (cyst, naked)", 
//				"STAGE", 
				pegObject.setStage(getCell(row, "STAGE"));
//				"Author", 
//				"AphiaID", 
				pegObject.setAphiaId(getCell(row, "AphiaID"));
//				"Trophy", 
				pegObject.setTrophy(getCell(row, "Trophy"));
//				"Geometric shape", 
				pegObject.setGeometricShape(getCell(row, "Geometric_shape"));
//				"FORMULA", 
				pegObject.setFormula(getCell(row, "FORMULA"));
//				"Size class No", 
//				"SizeClassNo", 
				pegObject.setSizeClassNo(getCellNoDecimals(row, "SizeClassNo"));
//				"Unit",  
				pegObject.setUnit(getCell(row, "Unit"));
//				"size range, ", 
//				"Size range", 
				pegObject.setSizeRange(getCell(row, "SizeRange"));
//				"Length (l1), µm", 
//				"Length(l1)µm", 
				pegObject.setLengthL1(getCell(row, "Length(l1)µm"));
//				"Length (l2), µm", 
//				"Length(l2)µm", 
				pegObject.setLengthL2(getCell(row, "Length(l2)µm"));
//				"Width (w), µmv",
//				"Width(w)µm",
				pegObject.setWidth(getCell(row, "Width(w)µm"));
//				"Height (h), µm", 
//				"Height(h)µm", 
				pegObject.setHeight(getCell(row, "Height(h)µm"));
//				"Diameter (d1), µm", 
//				"Diameter (d1), µm", 
				pegObject.setDiameterD1(getCell(row, "Diameter(d1)µm"));
//				"Diameter (d2), µm", 
//				"Diameter(d1)µm", 
				pegObject.setDiameterD2(getCell(row, "Diameter(d2)µm"));
//				"No. of cells/ counting unit", 
				pegObject.setNoOfCellsPerCountingUnit(getCell(row, "No_of_cells/counting_unit")); // No_of_cells/counting_unit
//				"Calculated  volume, µm3",
//				"Calculated  volume µm3",
//				"Calculated _volume_µm3"
//				"Calculated_volume_µm3"
//				pegObject.setCalculatedVolume(getCell(row, "Calculated_volume_µm3")); //Calculated_volume_µm3
				pegObject.setCalculatedVolume(getCell(row, "Calculated_volume_um3")); //Calculated_volume_µm3
//				"Comment",
				pegObject.setComment(getCell(row, "Comment"));
//				"Filament: length of cell (µm)", 
				pegObject.setFilament(getCell(row, "Filament_length_of_cell(µm)"));
//				"Calculated Carbon pg/counting unit        (Menden-Deuer & Lessard 2000)",
//				"Calculated Carbon pg/counting unit",
				pegObject.setCalculatedCarbon(getCell(row, 
						"Calculated_Carbon_pg/counting_unit"));
//				"Comment on Carbon calculation"
				pegObject.setCommentOnCarbonCalculation(getCell(row, "Comment_on_Carbon_calculation"));
				
//				// In PEG "v." is used instead of "var.".  
//				String taxonName = getCell(row, "Species");
//				taxonName = taxonName.replace(" v. ", " var. ");
//
//				
//				
//				// Species name corrected in PEG, but not in DynTaxa yet.
//
//				// Aphanothece paralleliformis (PEG) = Aphanothece parallelliformis (DynTaxa)
//				// Mesoporus perforatus
//				// Prorocentrum redfieldii (PEG) = Prorocentrum redfeldii (DynTaxa)
//				// Heterocapsa arctica ssp. frigida
//				// Chaetoceros salsugineus
//				// Chaetoceros salsugineus
//				// Navicula transitans var. deresa f. delicatula
//				// Lobocystis planctonica
//				// Flagellates
//				// Unicell
//				if (taxonName.equals("Aphanothece paralleliformis")) {
//					taxonName = "Aphanothece parallelliformis";
//					System.out.println("TODO: Temporary species name correction. Aphanothece paralleliformis -> Aphanothece parallelliformis");
//				}
//				else if (taxonName.equals("Prorocentrum redfieldii")) {
//					taxonName = "Prorocentrum redfeldii";
//					System.out.println("TODO: Temporary species name correction. Aphanothece paralleliformis -> Aphanothece parallelliformis");
//				}
				
				String taxonName = getCell(row, "Species");
				
//				if ( taxonName.equals("Aphanothece paralleliformis") ) {
//					System.out.println("TEST Aphanothece paralleliformis");
//				}
//				if ( taxonName.equals("Aphanothece parallelliformis") ) {
//				    System.out.println("TEST Aphanothece parallelliformis");
//			    }
				if ( taxonName.equals("Unicell") ) {
				    System.out.println("DEBUG Unicell");
			    }
				
				String taxonId = "";
				taxonId = taxonManager.getTaxonIdFromName(taxonName);
//				if (pegToDynTaxaMap.containsKey(taxonName)) {
//					taxonId = taxonManager.getTaxonIdFromName(pegToDynTaxaMap.get(taxonName));
//				} else {
//					taxonId = taxonManager.getTaxonIdFromName(taxonName);
//				}
					
				TaxonNode taxonNode = taxonManager.getTaxonNodeFromImportId(taxonId);

				if (taxonNode != null) {
					pegObject.setDyntaxaId(taxonId);
					taxonNode.addPegObject(pegObject);					
				} else {
//					ErrorLogger.println("ERROR: PEG species not found: " + getCell(row, "Species") + ' ' + getCell(row, "Size class No"));
//					System.out.println("ERROR: PEG species not found test: " + taxonName);
					System.out.println("ERROR: PEG species not found: " + getCell(row, "Species") + ' ' + getCell(row, "SizeClassNo"));
				}
				addedItems++;
			}
		}
//		logInfo.add("INFO: Added items: " + addedItems + ".");
	}

	@Override
	public void visitDataset(Dataset dataset) {
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

	@Override
	public void visitVisit(Visit visit) {
		// TODO Auto-generated method stub
		
	}
}
