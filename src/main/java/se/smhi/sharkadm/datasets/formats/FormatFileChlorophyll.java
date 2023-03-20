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
import se.smhi.sharkadm.fileimport.misc.FileImportTranslateAllColumns;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.utils.ParseFileUtil;

/**
 * Import format for Chlorophyll.
 * 
 */
public class FormatFileChlorophyll extends FormatFileBase {

	public FormatFileChlorophyll(PrintStream logInfo, FileImportInfo importInfo) {
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

		loadKeyTranslator(importMatrixColumn, "import_matrix_chlorophyll.txt");
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
				
				// Create variable for this row.
				currentVariable = new Variable(false);
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
			logInfo.println("Info: Data file, processed rows: " + addedItems + ".");
		}
	}
	
	@Override
	public void getCurrentVisit(String[] row) {
		String keyString = getCellByKey(row, "visit.visit_date") + ":" +
				   getCellByKey(row, "visit.reported_station_name") +
				   getCellByKey(row, "visit.platform_code");
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
						   getCellByKey(row, "visit.platform_code") + ":" +
						   getCellByKey(row, "sample.sample_id") + ":" +
						   getCell(row, "SERNO") + ":" +
						   getCellByKey(row, "sample.sample_depth_m");
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
		// Add QFLAG stored as TempField.  
		for (String tempKey : variable.getTempFieldKeys().toArray(new String[variable.getTempFieldKeys().size()])) {
			if (tempKey.startsWith("TEMP.QFLAG." + variable.getParameter())) {
				variable.addField("variable.quality_flag", variable.getTempField(tempKey));
				variable.removeTempField(tempKey);
			}
			if (tempKey.startsWith("TEMP.SFLAG." + variable.getParameter())) {
				variable.addField("variable.status_flag", variable.getTempField(tempKey));
				variable.removeTempField(tempKey);
			}
		}
		
		// Translate.
		String statusFlagValue = variable.getField("variable.status_flag");
		String qualityFlagValue = variable.getField("variable.quality_flag");
		statusFlagValue = FileImportTranslateAllColumns.instance().translateValue("status_flag", statusFlagValue);
		qualityFlagValue = FileImportTranslateAllColumns.instance().translateValue("quality_flag", qualityFlagValue);
		variable.addField("variable.status_flag", statusFlagValue);
		variable.addField("variable.quality_flag", qualityFlagValue);
		
		if (statusFlagValue.equals("0")) { // SFLAG.
			// Remove variable by setting parameter/value/unit to blank.
			variable.addField("variable.parameter", "");
			variable.addField("variable.value", "");
			variable.addField("variable.unit", "");
			variable.addField("variable.quality_flag", "");
		} else {
			if (statusFlagValue.equals("1")) { // SFLAG.
				variable.addField("variable.quality_flag", "");
			}
			if (qualityFlagValue.equals("1")) { // QFLAG.
				variable.addField("variable.quality_flag", "");
			}
			else if (qualityFlagValue.equals("A")) { // QFLAG.
				variable.addField("variable.quality_flag", "");
			}
			else if (qualityFlagValue.equals("<")) { // QFLAG.
				variable.addField("variable.quality_flag", "<");
			}
			else if (qualityFlagValue.equals("?")) { // QFLAG.
				variable.addField("variable.quality_flag", "S");
			}
			else if (qualityFlagValue.equals("!")) { // QFLAG.
				variable.addField("variable.quality_flag", "E");
			}
		}

		// SFLAG<|>SAFLG:
		//    0 = no sample/data [läses inte in i db]
		//    1 = data OK [QFLAG=A]
		//
		// QFLAG<|>QCHLA:
		//    "<" = less than [behåll "<" som QFLAG]
		//    A [behåll "A" som QFLAG]
		//
		// Ifall både QFLAG och SFLAG, så har QFLAG företräde.		
	}
}
