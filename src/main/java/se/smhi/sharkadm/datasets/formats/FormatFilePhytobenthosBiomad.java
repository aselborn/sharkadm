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

import se.smhi.sharkadm.datasets.fileimport.FileImportInfo;
import se.smhi.sharkadm.facades.ImportFacade;
import se.smhi.sharkadm.facades.ModelFacade;
import se.smhi.sharkadm.fileimport.misc.FileImportTranslate;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.utils.ConvUtils;
import se.smhi.sharkadm.utils.ParseFileUtil;

/**
 * Import format for PhytobenthosBiomad.
 * 
 */
public class FormatFilePhytobenthosBiomad extends FormatFileBase {

	public FormatFilePhytobenthosBiomad(PrintStream logInfo, FileImportInfo importInfo) {
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

		loadKeyTranslator(importMatrixColumn, "import_matrix_epibenthos.txt");
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
			if (Files.exists(Paths.get(zipFileName, "processed_data", "station.skv"))) {
				filePath = Paths.get(zipFileName, "processed_data", "station.skv");

				bufferedReader = verifyDataFile(filePath.toFile(), "MPROG");

				if (bufferedReader  == null)
					bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));

				fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
				if (fileContent != null) {
					importStation(fileContent);
				} else {
					importInfo.addConcatWarning("File missing or empty: station.skv.");
				}
			}
			if (Files.exists(Paths.get(zipFileName, "processed_data", "sample.skv"))) {
				filePath = Paths.get(zipFileName, "processed_data", "sample.skv");

				bufferedReader = verifyDataFile(filePath.toFile(), "MPROG");

				if (bufferedReader  == null)
					bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));

				fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
				if (fileContent != null) {
					importSample(fileContent);
				} else {
					importInfo.addConcatWarning("File missing or empty: sample.skv.");
				}
			}
			if (Files.exists(Paths.get(zipFileName, "processed_data", "abundance.skv"))) {
				filePath = Paths.get(zipFileName, "processed_data", "abundance.skv");
				bufferedReader = verifyDataFile(filePath.toFile(), "MPROG");

				if (bufferedReader  == null)
					bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));

				fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
				if (fileContent != null) {
					importAbundance(fileContent);
				} else {
					importInfo.addConcatWarning("File missing or empty: abundance.skv.");
				}
			}
		} catch (Exception e) {
			importInfo.addConcatError("FAILED TO IMPORT FILE.");
		}
	
		// Used by the Observer pattern.
		ModelFacade.instance().modelChanged();
	}

	private void importStation(List<String[]> fileContent) {
		String[] header = null;
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Station file: " + fileContent.size() + " rows (header included).");
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
				
				// Add each column value.
				for (String columnName : header) {
					String key = translateKey(columnName);					
					addVisitField(key, getCell(row, columnName));
				}					
				addedItems++;
			}
		}
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Station file, processed rows: " + addedItems + ".");
		}
	}
	
	private void importSample(List<String[]> fileContent) {
		String[] header = null;
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Sample file: " + fileContent.size() + " rows (header included).");
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
				
				// Add each column value.
				for (String columnName : header) {
					String key = translateKey(columnName);
					addSampleField(key, getCell(row, columnName));
				}					
				addedItems++;
			}
		}
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Sample file, processed rows: " + addedItems + ".");
		}
	}				

	private void importAbundance(List<String[]> fileContent) {
		String[] header = null;
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Abundance file: " + fileContent.size() + " rows (header included).");
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
					addVariableField(key, getCell(row, columnName));
				}					
				addedItems++;
			}
		}
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Abundance file, processed rows: " + addedItems + ".");
		}
	}				

	@Override
	public void getCurrentVisit(String[] row) {
		String keyString = getCellByKey(row, "visit.visit_date") + ":" +
						   getCellByKey(row, "visit.reported_station_name") + ":" +
						   getCellByKey(row, "visit.transect_id");
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
						   getCellByKey(row, "sample.transect_id") + ":" +
						   getCellByKey(row, "sample.sample_depth_m") + ":" +
//						   getCellByKey(row, "sample.sample_id"); // Both RPSNO and SMPNO are used.
						   getCell(row, "SMPNO") + ":" +
						   getCell(row, "RPSNO"); // RPSN used if not Biomad.

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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postReformatVariable(Variable variable) {

		// Calculate values and change parameters/units.
		try {
//			if (variable.getParameter().equals("COUNTNR")) {
			if (variable.getParameter().equals("# counted")) {
				Double value = ConvUtils.convStringToDouble(variable.getValue());
				Double samplerArea = ConvUtils.convStringToDouble(
						variable.getParent().getField("sample.sampler_area_cm2")); //TODO

				// VALUE = COUNTNR / SAREA * 10000
				Double param = value / samplerArea * 10000.0;
//				param = Math.round(param * 10000.0) / 10000.0; // 4 decimals.

//				variable.setParameter("ABUND");
				variable.setParameter("Abundance");
//				variable.setValue(param.toString());
				variable.setValue(ConvUtils.convDoubleToString(param));
				variable.setUnit("ind/m2");

//			} else if (variable.getParameter().equals("COVER_AREA")) { // or COVER AREA.
			} else if (variable.getParameter().equals("Cover area")) {
				Double value = ConvUtils.convStringToDouble(variable.getValue());
				Double samplerArea = ConvUtils.convStringToDouble(
						variable.getParent().getField("sample.sampler_area_cm2")); //TODO

				// VALUE = COVER_AREA / SAREA * 10000 * 100
				Double param = value / samplerArea * 1000000.0;
//				param = Math.round(param * 10000.0) / 10000.0; // 4 decimals.
				
//				variable.setParameter("COVER %");
				variable.setParameter("Cover %");
				// Rounded to four decimals.
//				variable.setValue(param.toString());
				variable.setValue(ConvUtils.convDoubleToString(param));
				variable.setUnit("%");
								
//			} else if (variable.getParameter().equals("DEPOS_AREA")) { // or DEPOS AREA.
			} else if (variable.getParameter().equals("Sediment deposition cover area")) {
				Double value = ConvUtils.convStringToDouble(variable.getValue());
				Double samplerArea = ConvUtils.convStringToDouble(
						variable.getParent().getField("sample.sampler_area_cm2")); //TODO

				// VALUE = DEPOS_AREA / SAREA * 10000 * 100
				Double param = value / samplerArea * 1000000.0;
//				param = Math.round(param * 10000.0) / 10000.0; // 4 decimals.
				
//				variable.setParameter("DEPOS COVER %");
				variable.setParameter("Deposition cover %");
				// Rounded to four decimals.
//				variable.setValue(param.toString());
				variable.setValue(ConvUtils.convDoubleToString(param));
				variable.setUnit("%");
			}
		} catch (Exception e) {
			importInfo.addConcatWarning("Failed to calculate value. Parameter:" + variable.getParameter());
		}
	}

}

