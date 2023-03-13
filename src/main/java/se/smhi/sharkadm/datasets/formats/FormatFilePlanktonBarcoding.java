/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.formats;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import se.smhi.sharkadm.datasets.calc.PhytoplanktonCalcObject;
import se.smhi.sharkadm.datasets.fileimport.FileImportInfo;
import se.smhi.sharkadm.facades.ImportFacade;
import se.smhi.sharkadm.facades.ModelFacade;
import se.smhi.sharkadm.fileimport.misc.FileImportFilter;
import se.smhi.sharkadm.fileimport.misc.FileImportPEGList;
import se.smhi.sharkadm.fileimport.misc.FileImportTranslate;
import se.smhi.sharkadm.fileimport.misc.FileImportTranslateAllColumns;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.utils.ParseFileUtil;
import se.smhi.sharkadm.utils.StringUtils;

/**
 * Import format for PlanktonBarcoding.
 * 
 */
public class FormatFilePlanktonBarcoding extends FormatFileBase {

	public FormatFilePlanktonBarcoding(PrintStream logInfo, FileImportInfo importInfo) {
		super(logInfo, importInfo);
	}

	public void importFiles(String zipFileName, Dataset dataset) {
		this.dataset = dataset;
		
		String importMatrixColumn = "";
		if (dataset.getImport_format().contains(":")) {
			String[] strings = dataset.getImport_format().split( Pattern.quote(":"));
			importMatrixColumn = strings[1];
		} else {
			importInfo.addConcatError("Error in format description in 'delivery_note.txt'. Import aborted. ");
			return;
		}

		loadKeyTranslator(importMatrixColumn, "import_matrix_plankton_barcoding.txt");
		dataset.setImport_matrix_column(importMatrixColumn);
		
		if (getTranslateKeySize() == 0) {
			importInfo.addConcatError("Empty column in import matrix. Import aborted.");
			return;
		}
		dataset.setImport_status("DATA");

		// Use translate.txt for cell content replacement, if available.
		valueTranslate = new FileImportTranslate(zipFileName);
		if (valueTranslate.isTranslateUsed()) {
			importInfo.addConcatInfo("Translate file (translate.txt) from ZIP file is used.");
		}

		// Use filter.txt to remove rows, if available.
		filter = new FileImportFilter(zipFileName);
		if (filter.isFilterUsed()) {
			importInfo.addConcatInfo("Filter file (filter.txt) from ZIP file is used.");
		}

		// PEG-list needed for phytoplankton. 
		new FileImportPEGList(null).importFiles("");		
		
		// Imports the data file.
		List<String[]> fileContent;
		BufferedReader bufferedReader = null;
		Path filePath = null;
		
		try {
			if (Files.exists(Paths.get(zipFileName, "processed_data", "data.txt"))) {
				filePath = Paths.get(zipFileName, "processed_data", "data.txt");
				bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));
			} else if (Files.exists(Paths.get(zipFileName, "processed_data", "data.dat"))) {
				filePath = Paths.get(zipFileName, "processed_data", "data.dat");
				bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));
			} else if (Files.exists(Paths.get(zipFileName, "processed_data", "data.skv"))) {
				filePath = Paths.get(zipFileName, "processed_data", "data.skv");
				bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));
			}
			// Import.
			if (bufferedReader != null) {
				fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
				importData(fileContent);
			} 
		} catch (Exception e) {
			importInfo.addConcatError("FAILED TO IMPORT FILE.");
		}
		// Used by the Observer pattern.
		ModelFacade.instance().modelChanged();
	}

	private void importData(List<String[]> fileContent) {
		String[] header = null;
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Data file: " + fileContent.size() + " rows (header included).");
		}
		int addedItems = 0;

		currentVisit = null;
		currentSample = null;
		currentVariable = null;

		for (String[] row : fileContent) {
			if (header == null) {
				header = row; // Header row in imported file.
				setHeaderFields(header);
			} else {
				
				// Remove row if filter conditions are met.
				if (filter.shouldBeFiltered(header, row)) {
					continue; // Move to next row.
				}
				
				
				// Create or reuse visit for this row.
				getCurrentVisit(row);
				
				// Create or reuse sample for this row.
				getCurrentSample(row);
				
				// Create community variable for this row.
				currentVariable = new Variable(true);
				currentSample.addVariable(currentVariable);
								
				// Add each column value.
				for (String columnName : header) {
					String key = translateKey(columnName);

					// Check if values for stations and species are translated.
					// Use value before translation for reported value and after as used value.
					// If species it is possible to translate to taxon-id.
					if (((valueTranslate != null) && (valueTranslate.isTranslateUsed())) && (key.equals("variable.reported_scientific_name"))) {
						addVariableField("variable.reported_scientific_name", getCellNoTranslation(row, columnName));
//						String translatedValue = valueTranslate.translateImportValue(columnName, getCellNoTranlation(row, columnName));
						String translatedValue = getCell(row, columnName);
//						System.out.println("DEBUG: " + translatedValue);
						if (StringUtils.isNumeric(translatedValue)) {
							// Translated to a numeric taxon-id.
							addVariableField("variable.dyntaxa_id", translatedValue);
						} else {
							// Translated to taxon name.
							addVariableField("variable.scientific_name", translatedValue);
						}
					}
					else if (((valueTranslate != null) && (valueTranslate.isTranslateUsed())) && (key.equals("visit.reported_station_name"))) {
						addVariableField("visit.reported_station_name", getCellNoTranslation(row, columnName));
						addVariableField("visit.station_name", getCell(row, columnName));
					}
					else {
						// The normal case.
						addVariableField(key, getCell(row, columnName));
					}

				
				}					
				addedItems++;
			}
		}
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Data file, processed rows: " + addedItems + ".");
		}
	}
	
	@Override
	public void getCurrentVisit(String[] row) {
		String keyString = getCellByKey(row, "visit.platform_code") + ":" +
		                   getCellByKey(row, "visit.visit_date") + ":" +
						   getCellByKey(row, "visit.visit_reported_latitude") + ":" +
						   getCellByKey(row, "visit.visit_reported_longitude") + ":" +
		                   getCellByKey(row, "visit.reported_station_name");
		
		Visit visit = null;
		for (Visit v : dataset.getVisits()) {
			if (v.getFileImportKeyString().equals(keyString)) {
				visit = v;
				currentVisit = v;
			}
		}
		if (visit == null) {
			currentVisit = new Visit(keyString);
			dataset.addVisit(currentVisit);
		}
	}
	
	@Override
	public void getCurrentSample(String[] row) {
		String keyString = getCellByKey(row, "visit.platform_code") + ":" +
                           getCellByKey(row, "visit.visit_date") + ":" +
			               getCellByKey(row, "sample.sample_time") + ":" +
						   getCellByKey(row, "visit.visit_reported_latitude") + ":" +
						   getCellByKey(row, "visit.visit_reported_longitude") + ":" +
						   getCellByKey(row, "visit.reported_station_name") + ":" +
						   getCellByKey(row, "sample.sample_id") + ":" +
						   getCellByKey(row, "sample.sampler_type_code") + ":" +
						   getCellByKey(row, "sample.sample_depth_m") + ":" +
						   getCellByKey(row, "sample.sample_min_depth_m") + ":" +
						   getCellByKey(row, "sample.sample_max_depth_m") + ":" +
						   getCellByKey(row, "sample.sample_max_depth_m");
		
		Sample sample = null;
		for (Visit v : dataset.getVisits()) {
			for (Sample s : v.getSamples()) {
				if (s.getFileImportKeyString().equals(keyString)) {
					sample = s;
					currentSample = s;
				}
			}
		}
		if (sample == null) {
			currentSample = new Sample(keyString);
			currentVisit.addSample(currentSample);
			
			
			// currentSample.addField("sample.shark_sample_id_keystring", keyString);
			// currentSample.addField("sample.shark_sample_id_md5", StringUtils.convToMd5(keyString));
			
			
		}
	}
	
	@Override
	public void getCurrentVariable(String[] row) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postReorganizeDataset(Dataset dataset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postReorganizeVisit(Visit visit) {
//		visit.addField("visit.visit_year", 
//				utils.convNoDecimal(visit.getField("visit.visit_year")));
//		visit.addField("visit.visit_date", 
//				utils.convDate(visit.getField("visit.visit_date")));
	}

	@Override
	public void postReorganizeSample(Sample sample) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postReorganizeVariable(Variable variable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postReformatDataset(Dataset dataset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postReformatVisit(Visit visit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postReformatSample(Sample sample) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void postReformatVariable(Variable variable) {
		// TODO Auto-generated method stub
		
	}	
}
