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
 * Import format for Physical and Chemical.
 * 
 */
public class FormatFilePhysicalChemical extends FormatFileBase {

	private List<SamplingInfo> samplingInfoList = new ArrayList<SamplingInfo>();
	private List<AnalyseInfo> analyseInfoList = new ArrayList<AnalyseInfo>();

	public FormatFilePhysicalChemical(PrintStream logInfo, FileImportInfo importInfo) {
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

		loadKeyTranslator(importMatrixColumn, "import_matrix_physicalchemical.txt");
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
			if (Files.exists(Paths.get(zipFileName, "processed_data", "sampling_info.txt"))) {
				filePath = Paths.get(zipFileName, "processed_data", "sampling_info.txt");
				bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));
				fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
				if (fileContent != null) {
					importSamplingInfo(fileContent);
				} else {
					importInfo.addConcatWarning("File missing or empty: sampling_info.txt.");
				}
			}
			if (Files.exists(Paths.get(zipFileName, "processed_data", "analyse_info.txt"))) {
				filePath = Paths.get(zipFileName, "processed_data", "analyse_info.txt");
				bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));
				fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
				if (fileContent != null) {
					importAnalyseInfo(fileContent);
				} else {
					importInfo.addConcatWarning("File missing or empty: analyse_info.txt.");
				}
			}
//			if (Files.exists(Paths.get(zipFileName, "processed_data", "data.txt"))) {
//				filePath = Paths.get(zipFileName, "processed_data", "data.txt");
//				bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));
//				fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
//				if (fileContent != null) {
//					importAllData(fileContent);
//				} else {
//					importInfo.addConcatWarning("File missing or empty: data.txt.");
//				}
//			}

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
				importAllData(fileContent);
			}
		} catch (Exception e) {
			importInfo.addConcatError("FAILED TO IMPORT FILE.");
		}
		// Used by the Observer pattern.
		ModelFacade.instance().modelChanged();
	}

	private void importAllData(List<String[]> fileContent) {
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
				currentVariable = new Variable(false);
				currentSample.addVariable(currentVariable);
								
				// Add each column value.
				for (String columnName : header) {
					String key = translateKey(columnName);
					addVariableField(key, getCell(row, columnName));
					
//					// If it is a parameter, then add more data.  
//					if (key.startsWith("variable.COPY_VARIABLE")) {
//						addSamplingAndAnalyseInfo(key, row);
//					}
				}					
				addedItems++;
			}
		}
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Data file, processed rows: " + addedItems + ".");
		}
	}

//	private void addSamplingAndAnalyseInfo(String key, String[] row) {
//		// Add columns from sampling_info.
//		for (SamplingInfo samplingInfo : samplingInfoList) {
//			if (samplingInfo.parameter.equals(key)) {
//				if (!samplingInfo.validFrom.equals("")) {
//					if (samplingInfo.validFrom.compareTo(getCellByKey(row, "visit.visit_date")) > 0) {
//						continue;
//					}
//				}
//				if (!samplingInfo.validTo.equals("")) {
//					if (samplingInfo.validTo.compareTo(getCellByKey(row, "visit.visit_date")) < 0) {
//						continue;
//					}
//				}
//				for (String infoMapKey : samplingInfo.infoMap.keySet()) {
//					String translatedKey = translateKey(infoMapKey);
//					addVariableField(translatedKey, samplingInfo.infoMap.get(infoMapKey));
//				}
//			}					
//		}					
//		// Add columns from analyse_info.
//		for (AnalyseInfo analyseInfo : analyseInfoList) {
//			if (analyseInfo.parameter.equals(key)) {
//				if (!analyseInfo.validFrom.equals("")) {
//					if (analyseInfo.validFrom.compareTo(getCellByKey(row, "visit.visit_date")) > 0) {
//						continue;
//					}
//				}
//				if (!analyseInfo.validTo.equals("")) {
//					if (analyseInfo.validTo.compareTo(getCellByKey(row, "visit.visit_date")) < 0) {
//						continue;
//					}
//				}
//				for (String infoMapKey : analyseInfo.infoMap.keySet()) {
//					String translatedKey = translateKey(infoMapKey);
//					addVariableField(translatedKey, analyseInfo.infoMap.get(infoMapKey));
//				}				
//			}
//		}
//	}
	
	private void addSamplingAndAnalyseInfo(Variable variable) {
		// Add columns from sampling_info.
		for (SamplingInfo samplingInfo : samplingInfoList) {
			if (samplingInfo.parameter.equals(variable.getParameter())) {
				if (!samplingInfo.validFrom.equals("")) {
					if (samplingInfo.validFrom.compareTo(variable.getParent().getParent().getField("visit.visit_date")) > 0) {
						continue;
					}
				}
				if (!samplingInfo.validTo.equals("")) {
					if (samplingInfo.validTo.compareTo(variable.getParent().getParent().getField("visit.visit_date")) < 0) {
						continue;
					}
				}
				for (String infoMapKey : samplingInfo.infoMap.keySet()) {
					String translatedKey = translateKey(infoMapKey);
//					addVariableField(translatedKey, samplingInfo.infoMap.get(infoMapKey));
					variable.addField(translatedKey, samplingInfo.infoMap.get(infoMapKey));
				}
			}					
		}					
		// Add columns from analyse_info.
		for (AnalyseInfo analyseInfo : analyseInfoList) {
			if (analyseInfo.parameter.equals(variable.getParameter())) {
				if (!analyseInfo.validFrom.equals("")) {
					if (analyseInfo.validFrom.compareTo(variable.getParent().getParent().getField("visit.visit_date")) > 0) {
						continue;
					}
				}
				if (!analyseInfo.validTo.equals("")) {
					if (analyseInfo.validTo.compareTo(variable.getParent().getParent().getField("visit.visit_date")) < 0) {
						continue;
					}
				}
				for (String infoMapKey : analyseInfo.infoMap.keySet()) {
					String translatedKey = translateKey(infoMapKey);
//					addVariableField(translatedKey, analyseInfo.infoMap.get(infoMapKey));
					variable.addField(translatedKey, analyseInfo.infoMap.get(infoMapKey));
				}				
			}
		}
	}
	
	private void importSamplingInfo(List<String[]> fileContent) {
		String[] header = null;
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Sampling info file: " + fileContent.size() + " rows (header included).");
		}
		int addedItems = 0;

		for (String[] row : fileContent) {
			if (header == null) {
				header = row; // Header row in imported file.
				setHeaderFields(header);
			} else {
				SamplingInfo samplingInfo = new SamplingInfo();
				// Add each column value.
				for (String columnName : header) {
					if (!getCell(row, columnName).equals("")) {
						if (columnName.equals("PARAM")) {

							String tmpParam = getCell(row, columnName);
							tmpParam = translateKey(getCell(row, columnName));
							
//							samplingInfo.parameter = tmpParam;
							String[] tmpParamItems = tmpParam.split(Pattern.quote("."));
							if (tmpParamItems.length > 2) {
								samplingInfo.parameter = tmpParamItems[2].trim();
							} else {
								System.out.println("DEBUG: Sampling info file, parameter not found: " + tmpParam);
								continue;
							}
						}
						else if (columnName.equals("VALIDFR")) {
							String validFrom = (getCell(row, columnName));
							if ((validFrom.length() == 10) && 
								(validFrom.substring(4, 5).equals("-")) && 
								(validFrom.substring(7, 8).equals("-"))) {
									samplingInfo.validFrom = getCell(row, columnName);
								} else {
									importInfo.addConcatError("Wrong data format in sampling_info.txt. Value: " + validFrom);
								}
						}
						else if (columnName.equals("VALIDTO")) {
							String validTo = (getCell(row, columnName));
							if ((validTo.length() == 10) && 
								(validTo.substring(4, 5).equals("-")) && 
								(validTo.substring(7, 8).equals("-"))) {
									samplingInfo.validTo = getCell(row, columnName);
								} else {
									importInfo.addConcatError("Wrong data format in sampling_info.txt. Value: " + validTo);
									
								}
						}
						else {
							samplingInfo.infoMap.put(columnName, getCell(row, columnName));
						}
					}
				}					
				samplingInfoList.add(samplingInfo);
			}
		}
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Sampling info file, processed rows: " + addedItems + ".");
		}
	}
	
	private void importAnalyseInfo(List<String[]> fileContent) {
		String[] header = null;
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Analyse info file: " + fileContent.size() + " rows (header included).");
		}
		int addedItems = 0;

		for (String[] row : fileContent) {
			if (header == null) {
				header = row; // Header row in imported file.
				setHeaderFields(header);
			} else {
				AnalyseInfo analyseInfo = new AnalyseInfo();
				// Add each column value.
				for (String columnName : header) {
					if (!getCell(row, columnName).equals("")) {
						if (columnName.equals("PARAM")) {

							String tmpParam = getCell(row, columnName);
							tmpParam = translateKey(getCell(row, columnName));
							
//							analyseInfo.parameter = tmpParam;
							String[] tmpParamItems = tmpParam.split(Pattern.quote("."));
							if (tmpParamItems.length > 2) {
								analyseInfo.parameter = tmpParamItems[2].trim();
							} else {
								System.out.println("DEBUG: Analyse info file, parameter not found: " + tmpParam);
								continue;
							}							
						}
						else if (columnName.equals("VALIDFR")) {
							String validFrom = (getCell(row, columnName));
							if ((validFrom.length() == 10) && 
								(validFrom.substring(4, 5).equals("-")) && 
								(validFrom.substring(7, 8).equals("-"))) {
								analyseInfo.validFrom = getCell(row, columnName);
								} else {
									importInfo.addConcatError("Wrong data format in analyse_info.txt. Value: " + validFrom);
								}
						}
						else if (columnName.equals("VALIDTO")) {
							String validTo = (getCell(row, columnName));
							if ((validTo.length() == 10) && 
								(validTo.substring(4, 5).equals("-")) && 
								(validTo.substring(7, 8).equals("-"))) {
								analyseInfo.validTo = getCell(row, columnName);
								} else {
									importInfo.addConcatError("Wrong data format in analyse_info.txt. Value: " + validTo);
								}
						}
						else {
							analyseInfo.infoMap.put(columnName, getCell(row, columnName));
						}
					}
				}					
				analyseInfoList.add(analyseInfo);
			}
		}
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Analyse info file, processed rows: " + addedItems + ".");
		}
	}

	@Override
	public void getCurrentVisit(String[] row) {

		String keyString = 
			getCellByKey(row, "dataset.country_code") + ":" +
			getCellByKey(row, "visit.visit_year") + ":" +
			getCellByKey(row, "visit.platform_code") + ":" +
			getCellByKey(row, "visit.visit_id");
			
//			getCellByKey(row, "visit.visit_date") + ":" +
//			getCellByKey(row, "sample.sample_date") + ":" + // If visit_date not used.
//			getCellByKey(row, "visit.reported_station_name");
		
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

		String keyString = 
			getCellByKey(row, "dataset.country_code") + ":" +
			getCellByKey(row, "visit.visit_year") + ":" +
			getCellByKey(row, "visit.platform_code") + ":" +
			getCellByKey(row, "visit.visit_id") + ":" +

//			getCellByKey(row, "visit.visit_date") + ":" +
//			getCellByKey(row, "sample.sample_date") + ":" + // If visit_date not used.
//			getCellByKey(row, "visit.reported_station_name") + ":" +
			getCellByKey(row, "sample.sample_id") + ":" +
//			getCellByKey(row, "sample.sample_series") + ":" +
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

//		// If it is a parameter, then add more data.  
////		if (key.startsWith("variable.COPY_VARIABLE")) {
//			addSamplingAndAnalyseInfo(variable);
////		}
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
		
		// If it is a parameter, then add more data.  
//		if (key.startsWith("variable.COPY_VARIABLE")) {
			addSamplingAndAnalyseInfo(variable);
//		}

		// Add QFLAG stored as TempField.  
		for (String tempKey : variable.getTempFieldKeys().toArray(new String[variable.getTempFieldKeys().size()])) {
			if (tempKey.startsWith("TEMP.QFLAG." + variable.getParameter())) {
				
				variable.addField("variable.quality_flag", variable.getTempField(tempKey));
				variable.removeTempField(tempKey);
				
				// Translate.
				String qualityFlagValue = variable.getField("variable.quality_flag");
				qualityFlagValue = FileImportTranslateAllColumns.instance().translateValue("quality_flag", qualityFlagValue);
				variable.addField("variable.quality_flag", qualityFlagValue);
				
				// Convert quality_flag value. 
				String tmpValue = variable.getField("variable.quality_flag");
				if 		(tmpValue.equals("")) { tmpValue = "A"; } // "A - Accepted by DV (PhysChem only)."
				else if (tmpValue.equals("C")) { tmpValue = ""; } // "C - value from CTD (T/S/Flour)"
				else if (tmpValue.equals("X")) { tmpValue = ""; } // "X - don't use to construct T/S profile (T/S)"
				else if (tmpValue.equals("!")) { tmpValue = "E"; } // "Extreme/suspicious value, but checked and found ok."
//				else if (tmpValue.equals("I")) { tmpValue = "R"; } // "Manually interpolated"
				else if (tmpValue.equals("?")) { tmpValue = "S"; } // "Questionable"
				variable.addField("variable.quality_flag", tmpValue);
			}			
		}
		// Secchi should be available both as parameter and on visit level.
		// (It is not possible to use one value for both porposes via the import matrix.)
		if (variable.getParameter().equals("Secchi depth")) {
			variable.getParent().getParent().addField("visit.secchi_depth_m", variable.getValue());
			variable.getParent().getParent().addField("visit.secchi_quality_flag", variable.getField("variable.quality_flag"));
		}
	}
	
	// Local class.
	class AnalyseInfo {    	
    	String parameter = "";
    	String validFrom = "";
    	String validTo = "";
    	Map<String, String> infoMap = new HashMap<String, String>();
	}

	// Local class.
	class SamplingInfo {
		String parameter = "";
    	String validFrom = "";
    	String validTo = "";
    	Map<String, String> infoMap = new HashMap<String, String>();

    }

}
