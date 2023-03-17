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
 * Import format for Bacterioplankton.
 *
 */
public class FormatFileBacterioplankton extends FormatFileBase {

	public FormatFileBacterioplankton(PrintStream logInfo, FileImportInfo importInfo) {
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

		loadKeyTranslator(importMatrixColumn, "import_matrix_bacterioplankton.txt");
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
			bufferedReader = verifyDataFile(filePath.toFile());

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
				currentVariable = new Variable(true);
				currentSample.addVariable(currentVariable);
				
				// NOTE. All species are bacteria.
				currentVariable.addField("variable.dyntaxa_id", "5000052"); // Bacteria
								
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
						   getCellByKey(row, "sample.sample_series") + ":" +
						   getCellByKey(row, "sample.sampler_type_code") + ":" +
						   getCellByKey(row, "sample.sample_min_depth_m") + ":" +
						   getCellByKey(row, "sample.sample_max_depth_m") + ":" +
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

				// Translate.
				String qualityFlagValue = variable.getField("variable.quality_flag");
				qualityFlagValue = FileImportTranslateAllColumns.instance().translateValue("quality_flag", qualityFlagValue);
				variable.addField("variable.quality_flag", qualityFlagValue);
			}
		}
		
		// Add field "variable.quality_flag"
		String tmpValue = variable.getField("variable.quality_flag");
		
		if (tmpValue.equals("0")) {
			// Remove variable by setting parameter/value/unit to blank.
			variable.addField("variable.parameter", "");
			variable.addField("variable.value", "");
			variable.addField("variable.unit", "");
			variable.addField("variable.quality_flag", "");
		} else if (tmpValue.equals("A")) {
			variable.addField("variable.quality_flag", "");
		} else if (tmpValue.equals("1")) {
//			variable.addField("variable.quality_flag", "A");
			variable.addField("variable.quality_flag", "");
		} else if (tmpValue.equals("2")) {
			variable.addField("variable.quality_flag", "");
		} else if (tmpValue.equals("3")) {
			variable.addField("variable.quality_flag", "S");
		} else if (tmpValue.equals("4")) {
			variable.addField("variable.quality_flag", "S");
		} else if (tmpValue.equals("5")) {
			variable.addField("variable.quality_flag", "S");
			variable.appendToField("variable.variable_comment", "Traces (contamination) in data.");
		} else if (tmpValue.equals("6")) {
			variable.addField("variable.quality_flag", ">");
		} else if (tmpValue.equals("7")) {
			variable.addField("variable.quality_flag", "<");
		} else if (tmpValue.equals("9")) {
			variable.addField("variable.quality_flag", "S");
		} else if (tmpValue.equals("10")) {
			variable.addField("variable.quality_flag", "");
			variable.appendToField("variable.variable_comment", "Value modified by data originator.");
		} else if (tmpValue.equals("11")) {
			variable.addField("variable.quality_flag", "I");
			variable.appendToField("variable.variable_comment", "Manually interpolated value.");
		} else if (tmpValue.equals("40")) {
			variable.addField("variable.quality_flag", "S");				
			variable.appendToField("variable.variable_comment", "Bad chemical data from Ship 40, Sweden.");
		}

//		- Bactgr. och Bactabu. och Bactpl. Koder: Inom [] står hur datavärden lagrar informationen
//		0 No data
//		1 Data checked and OK [QFLAG=A]
//		2 Unchecked data [Lämnas blank]
//		3 Questionable data[QFLAG=S]
//		5 Traces (contamination) in data [QFLAG=S + COMNT_VAR=Traces (contamination) in data.]
//		6 Value greater than upper detection limit [QFLAG: >]
//		7 Value less than lower detection limit [QFLAG: <]
//		9 Data supplier indicates questionable data [QFLAG=S]
//		10 Modified value [COMNT_VAR=Modified value.]
//		11 Manually interpolated value [COMNT_VAR=Manually interpolated value.]
//		40 Bad chemical data from Ship 40, Sweden [QFLAG: S samt COMNT_VAR: Bad chemical data from Ship 40, Sweden.]
	}
}
