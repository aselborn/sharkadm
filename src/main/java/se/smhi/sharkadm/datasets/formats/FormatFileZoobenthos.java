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
import java.util.List;
import java.util.regex.Pattern;

import se.smhi.sharkadm.datasets.calc.BenticQualityIndex;
import se.smhi.sharkadm.datasets.fileimport.FileImportInfo;
import se.smhi.sharkadm.facades.ImportFacade;
import se.smhi.sharkadm.facades.ModelFacade;
import se.smhi.sharkadm.fileimport.misc.FileImportTranslate;
import se.smhi.sharkadm.fileimport.misc.FileImportTranslateAllColumns;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.utils.ConvUtils;
import se.smhi.sharkadm.utils.ParseFileUtil;

/**
 * Import format for Zoobenthos.
 * 
 */
public class FormatFileZoobenthos extends FormatFileBase {

	public FormatFileZoobenthos(PrintStream logInfo, FileImportInfo importInfo) {
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

		loadKeyTranslator(importMatrixColumn, "import_matrix_zoobenthos.txt");
//		loadKeyTranslator(importMatrixColumn, "import_matrix.txt");
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

		// Imports the data file.
		List<String[]> fileContent;
		BufferedReader bufferedReader = null;
		Path filePath = null;
		
		try {
			if (Files.exists(Paths.get(zipFileName, "processed_data", "data.txt"))) {
				filePath = Paths.get(zipFileName, "processed_data", "data.txt");
			} else if (Files.exists(Paths.get(zipFileName, "processed_data", "data.dat"))) {
				filePath = Paths.get(zipFileName, "processed_data", "data.dat");
			} else if (Files.exists(Paths.get(zipFileName, "processed_data", "data.skv"))) {
				filePath = Paths.get(zipFileName, "processed_data", "data.skv");
			}

			//Verify that DATA.txt HAS MPROG! If not, add it, then read it.
			bufferedReader = verifyDataFile(filePath.toFile(), "MPROG");

			if (bufferedReader == null)
				bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));

			// Import of file DATA.txt.
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
					
//					if (key.equals("sample.sampler_area_cm2")) {
//						// System.out.println("DEBUG" + getCell(row, columnName));
//						if (getCell(row, columnName).equals("")) {
//							System.out.println("DEBUG" + getCell(row, columnName));
//						}
//					}
					
					addVariableField(key, getCell(row, columnName));
				}					

				// Depth is not an interval for zoobenthos. Min/max depth are equals to water_depth. 
				if (currentSample.getField("sample.sample_min_depth_m").equals("")) {
					currentSample.addField("sample.sample_min_depth_m",
							currentVisit.getField("visit.water_depth_m")); 
					currentSample.addField("sample.sample_max_depth_m",
							currentVisit.getField("visit.water_depth_m")); 
				}
				// Sometimes it's reported on sample.
				if (currentSample.getField("sample.sample_min_depth_m").equals("")) {
					currentSample.addField("sample.sample_min_depth_m",
							currentVisit.getField("sample.water_depth_m")); 
					currentSample.addField("sample.sample_max_depth_m",
							currentVisit.getField("sample.water_depth_m")); 
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
		String keyString = getCellByKey(row, "visit.visit_date") + ":" +
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
		String keyString = getCellByKey(row, "visit.visit_date") + ":" +
						   getCellByKey(row, "visit.reported_station_name") + ":" +
						   getCellByKey(row, "sample.sample_id") + ":" +
						   getCellByKey(row, "sample.sample_depth_m") + ":" +
		   				   getCellByKey(row, "sample.sampler_type_code");
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
		// TODO Auto-generated method stub
		
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

		// We need a dummy variable to indicate empty samples.
		if ((sample.getField("sample.fauna_flora_found").equals("N")) ||
			(sample.getField("sample.fauna_flora_found").equals("1"))){
			Variable newVariable = new Variable(true);
			sample.addVariable(newVariable);						
//			newVariable.setParameter("NO SPECIES IN SAMPLE");
			newVariable.setParameter("No species in sample");
			newVariable.setValue("0");
			newVariable.setUnit("ind");
			newVariable.addField("variable.reported_scientific_name", "<no fauna/flora>");		
		}
		
		// Calculate BQI, Bentic Quality Index.
		BenticQualityIndex.instance().calculateBqiForSample(sample, importInfo);		
	}

	@Override
	public void postReformatVariable(Variable variable) {
		// Add QFLAG stored as TempField.  
		for (String tempKey : variable.getTempFieldKeys().toArray(new String[variable.getTempFieldKeys().size()])) {
			if (tempKey.startsWith("TEMP.QFLAG." + variable.getParameter())) {
				
				variable.addField("variable.quality_flag", variable.getTempField(tempKey));
				variable.removeTempField(tempKey);
				
				// Translate.
				String qualityFlagValue = variable.getField("variable.quality_flag");
				qualityFlagValue = FileImportTranslateAllColumns.instance().translateValue("quality_flag", qualityFlagValue);
				variable.addField("variable.quality_flag", qualityFlagValue);
			}
		}

		// QFLAG should only be set on parameter 'Wet weight'. 
		if (!variable.getParameter().equals("Wet weight")) {
			if (variable.containsField("variable.quality_flag")) {
				variable.addField("variable.quality_flag", "");
				importInfo.addConcatWarning("Zoobenthos. QFLAG removed for parameter: " + variable.getParameter());
			}
		}

		// Calculate values and change parameters/units.
		try {
//			if (variable.getParameter().equals("COUNTNR")) {
			if (variable.getParameter().equals("# counted")) {
				Double value = ConvUtils.convStringToDouble(variable.getValue());
				Double samplerArea = ConvUtils.convStringToDouble(
						variable.getParent().getField("sample.sampler_area_cm2"));

				// VALUE = COUNTNR / SAREA * 10000
				Double param = value / samplerArea * 10000.0;
//				param = Math.round(param * 10000.0) / 10000.0; // 4 decimals.

				Variable newVariable = variable.copyVariableAndData();
				variable.getParent().addVariable(newVariable);						
//				newVariable.setParameter("ABUND");
				newVariable.setParameter("Abundance");
//				newVariable.setValue(param.toString());
				newVariable.setValue(ConvUtils.convDoubleToString(param));
				newVariable.setUnit("ind/m2");
				newVariable.addField("variable.calc_by_dc", "Y");

//			} else if (variable.getParameter().equals("WET WEIGHT")) {
			} else if (variable.getParameter().equals("Wet weight")) {
				Double value = ConvUtils.convStringToDouble(variable.getValue());
				Double samplerArea = ConvUtils.convStringToDouble(
						variable.getParent().getField("sample.sampler_area_cm2"));

				// VALUE = WETWT / SAREA * 10000
				Double param = value / samplerArea * 10000.0;
//				param = Math.round(param * 10000.0) / 10000.0; // 4 decimals.
				
				Variable newVariable = variable.copyVariableAndData();
				variable.getParent().addVariable(newVariable);						
//				newVariable.setParameter("WWEIGHT ABUND");
				newVariable.setParameter("Wet weight/area");
//				newVariable.setValue(param.toString());
				newVariable.setValue(ConvUtils.convDoubleToString(param));
				newVariable.setUnit("g wet weight/m2");
				newVariable.addField("variable.calc_by_dc", "Y");
			}
		} catch (Exception e) {
			importInfo.addConcatWarning("Failed to calculate value. Parameter:" + variable.getParameter());
		}
	}
}
