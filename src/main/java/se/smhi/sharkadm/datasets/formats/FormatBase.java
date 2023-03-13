/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.formats;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import se.smhi.sharkadm.datasets.fileimport.FileImportInfo;
import se.smhi.sharkadm.datasets.fileimport.FileImportUtils;
import se.smhi.sharkadm.facades.ModelFacade;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.utils.ErrorLogger;
import se.smhi.sharkadm.utils.ParseFileUtil;

/**
 * Base class for import scripts. 
 * Import scripts based on this abstract class should:
 * - import data on the form Dataset - Visit - Sample - Variable,
 * - use the key translation mechanism in the file "import_matrix_<datatype>.txt",
 * - support the control flow defined in the class FileImportManager.    
 */
public abstract class FormatBase {
	
	protected Dataset dataset; // Top node for current import.
	protected Visit currentVisit = null;
	protected Sample currentSample = null;
	protected Variable currentVariable = null;

	// TODO: Implement generic file reader connected to import format.
	protected Map<String, String> keyTranslate = new HashMap<String, String>();
	//
	protected String visitKeyColumns = "";
	protected String sampleKeyColumns = "";
	
	protected PrintStream logInfo;	
	protected FileImportInfo importInfo;
	protected FileImportUtils utils;

	public FormatBase(PrintStream logInfo, FileImportInfo importInfo) {
		this.logInfo = logInfo;
		this.importInfo = importInfo;
		this.utils = new FileImportUtils(importInfo);
	}
	
	// These abstract functions must be implemented in sub-classes.

	

	public abstract void importFiles(String zipFileName, Dataset dataset);
	
	public abstract void getCurrentVisit(String[] row);
	
	public abstract void getCurrentSample(String[] row);
	
	public abstract void getCurrentVariable(String[] row);

	// These abstract functions must be implemented in sub-classes.
	public abstract void postReorganizeDataset(Dataset dataset);
	
	public abstract void postReorganizeVisit(Visit visit);
	
	public abstract void postReorganizeSample(Sample sample);
	
	public abstract void postReorganizeVariable(Variable variable);
	
	// These abstract functions must be implemented in sub-classes.
	public abstract void postReformatDataset(Dataset dataset);
	
	public abstract void postReformatVisit(Visit visit);
	
	public abstract void postReformatSample(Sample sample);
	
	public abstract void postReformatVariable(Variable variable);	

	public String translateKey(String key) {
		if (keyTranslate.containsKey(key)) {
			return keyTranslate.get(key);
		}
		return "MISSING KEY: " + key;
	}	
	
	public int getTranslateKeySize() {
		return keyTranslate.size();
	}	
	
	public void loadKeyTranslator(String formatColumnName, String importMatrixFileName) {
		List<String[]> fileContent;
		String[] header = null;
		int usedFormatColumn = -1;
		ClassLoader classLoader = this.getClass().getClassLoader();
		InputStream inputStream;
		BufferedReader bufferedReader;
		
		try {
			// Checks if file exist outside jar bundle. Use this one first, if exists.
			
//			bufferedReader = ParseFileUtil.GetSharkConfigFile("import_matrix.txt");
			bufferedReader = ParseFileUtil.GetSharkConfigFile(importMatrixFileName);

//			File external_file = new File("SHARK_CONFIG/import_matrix.txt");
//			if (external_file.exists()) {
//				bufferedReader = new BufferedReader(new FileReader(external_file));
//			} else {
//				// File is bundled in jar.
//				inputStream = classLoader
//						.getResourceAsStream("SHARK_CONFIG/import_matrix.txt");
//				bufferedReader = new BufferedReader(new InputStreamReader(
//						inputStream));
//			}

			fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
			if (fileContent != null) {
				keyTranslate.clear();
				for (String[] row : fileContent) {
					if (header == null) {
						header = row; // Header row in imported file.
						for (int i = 0; i < row.length; i++) {
							if (row[i].equals(formatColumnName)) {
								usedFormatColumn = i;
							}
						}
						if (usedFormatColumn < 0) {
//							importInfo.addConcatError("Can't find expected format column in the import format file (SHARK_CONFIG/import_matrix.txt).");
							importInfo.addConcatError("Can't find expected format column in the import format file (SHARK_CONFIG/" + importMatrixFileName + ").");
						}
					} else {
						
						try {
						
							if ((usedFormatColumn > 0) && // If not right column was found.
								(row.length > usedFormatColumn) && // Some rows are shorter or empty.
								(!row[usedFormatColumn].equals(""))) {	
								
	//							System.out.println("DEBUG: row-item: " + row[usedFormatColumn]);
	
								if (row[0].equals("VISIT_KEY")) {
									visitKeyColumns = row[usedFormatColumn];
								}
								else if (row[0].equals("SAMPLE_KEY")) {
									sampleKeyColumns = row[usedFormatColumn];
								}
								else if (row[usedFormatColumn].contains("<or>")) {
									// Separator exists.
									for (String colName : row[usedFormatColumn].split(Pattern.quote("<or>"))) {
										if (!colName.equals("")) {
											keyTranslate.put(colName.trim(), row[0]);
										}
									}
								} 
								else if ((row[usedFormatColumn].length() >= 5) && 
										 (row[usedFormatColumn].substring(0, 5).contains("<not>"))) {
									// Separator character <-> in the first column = not to be used.
									for (String colName : row[usedFormatColumn].split(Pattern.quote(Pattern.quote("<not>")))) {
										if (!colName.equals("")) {
											keyTranslate.put(colName.trim(), "NOT_USED");										
										}
									}
								}
								else if (row[usedFormatColumn].contains("<|>")) {
									// Separator exists.
	//								for (String colName : row[usedFormatColumn].split("<|>")) {
									for (String colName : row[usedFormatColumn].split(Pattern.quote("<|>"))) {
										if (!colName.equals("")) {
											keyTranslate.put(colName.trim(), row[0]);
										}
									}
								} 
								else if ((row[usedFormatColumn].length() >= 3) && 
										 (row[usedFormatColumn].substring(0, 3).contains("<->"))) {
									// Separator character <-> in the first column = not to be used.
	//								for (String colName : row[usedFormatColumn].split("<->")) {
									for (String colName : row[usedFormatColumn].split(Pattern.quote("<->"))) {
										if (!colName.equals("")) {
											keyTranslate.put(colName.trim(), "NOT_USED");										
										}
									}
								} 
								else {
									// Does not contain separator character.
									keyTranslate.put(row[usedFormatColumn], row[0]);
								}
							}
						} catch (Exception e) {
							importInfo.addConcatError("Failed to load import format file. First column: " + row[0] + ".");
						}
					}
				}
			}
			
			ErrorLogger.println("Debug: KeyTranslator column: " + formatColumnName + ". Size: " + keyTranslate.size());
			
		} catch (Exception e) {
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			importInfo.addConcatError("Failed to load import format file. KeyTranslator column: " + formatColumnName + ".");
		}
		
		// Used by the Observer pattern.
		ModelFacade.instance().modelChanged();
	}

}
