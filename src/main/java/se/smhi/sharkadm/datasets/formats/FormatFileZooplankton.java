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
 * Import format for Zooplankton.
 * 
 */
public class FormatFileZooplankton extends FormatFileBase {

	public FormatFileZooplankton(PrintStream logInfo, FileImportInfo importInfo) {
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

		loadKeyTranslator(importMatrixColumn, "import_matrix_zooplankton.txt");
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
			logInfo.println("Info: Data file, processed rows: " + addedItems + ".");
		}
	}
	
	@Override
	public void getCurrentVisit(String[] row) {
		String keyString = getVisitKey(row);
		if (keyString.equals("")) {
			// Old style.
			keyString = getCellByKey(row, "visit.visit_date") + ":" +
						getCellByKey(row, "visit.reported_station_name");
		}
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
		String keyString = getSampleKey(row);
		if (keyString.equals("")) {
			// Old style.
			keyString = getCellByKey(row, "visit.visit_date") + ":" +
						getCellByKey(row, "visit.reported_station_name") + ":" +
						getCellByKey(row, "sample.sample_id") + ":" +
						getCellByKey(row, "sample.sample_min_depth_m") + ":" +
						getCellByKey(row, "sample.sample_max_depth_m");
		}
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
		
		// Don't calculate if jellyfish.
		String formatName = variable.getParent().getParent().getParent().getImport_format();
		if (formatName.toLowerCase().contains("jelly")) {
			return;
		}
		
		// Calculate values and change parameters/units.
		try {
//			if (variable.getParameter().equals("COUNTNR")) {
			if (variable.getParameter().equals("# counted")) {
				Double value = variable.getValueAsDouble();

				Double samplerArea = variable.getParent().getFieldAsDouble("sample.sampler_area_cm2");
				Double countedPortions = variable.getFieldAsDouble("variable.counted_portions");
				Double sampledVolume = variable.getParent().getFieldAsDouble("sample.sampled_volume_l");
				Double minDepth = variable.getParent().getFieldAsDouble("sample.sample_min_depth_m");
				Double maxDepth = variable.getParent().getFieldAsDouble("sample.sample_max_depth_m");
				Double usedIndwetwt = variable.getFieldAsDouble("variable.used_indwetwt");
				Double numberOfPortions = variable.getFieldAsDouble("variable.number_of_portions");
				if (numberOfPortions == null) {
					numberOfPortions = 1.0;
				}

				if (samplerArea != null) {					
					
// TODO: We need a better solution to handle subsamples, for example 0-10 and 10-20 m.
//
//					// VALUE = (COUNTNR * NPORT) / (SAREA * CPORT) * 10000.0
//					Double param = (value * numberOfPortions)
//							/ (samplerArea * countedPortions) * 10000.0;
//	//				param = Math.round(param * 10000.0) / 10000.0; // 4 decimals.
//	
//					Variable newVariable = variable.copyVariableAndData();
//	//				newVariable.setParameter("ABUND");
//					newVariable.setParameter("Abundance");
////					newVariable.setValue(param.toString());
//					newVariable.setValue(ConvUtils.convDoubleToString(param));
//					newVariable.setUnit("ind/m2");
//					newVariable.addField("variable.calc_by_dv", "Y");
//					
//					// Check if the same parameter was calculated by reporting institute.
//					for (Object var :  variable.getParent().getVariables().toArray()) { // toArray: Variables are deleted during the loop.			
//						Variable tmpVar = (Variable) var;
////						if (	(tmpVar.getField("variable.parameter").equals("ABUNDANCE")) &&
//						if (	(tmpVar.getField("variable.parameter").equals("Abundance")) &&
//								(tmpVar.getField("variable.unit").equals("ind/m2")) &&
//								(tmpVar.getField("variable.reported_scientific_name").equals(variable.getField("variable.reported_scientific_name"))) &&
//								(tmpVar.getField("variable.size_class").equals(variable.getField("variable.size_class"))) && 
//								(tmpVar.getField("variable.species_flag_code").equals(variable.getField("variable.species_flag_code"))) &&
//								(tmpVar.getField("variable.trophic_type").equals(variable.getField("variable.trophic_type"))) &&
//								(tmpVar.getField("variable.sex_code").equals(variable.getField("variable.sex_code"))) &&
//								(tmpVar.getField("variable.dev_stage_code").equals(variable.getField("variable.dev_stage_code"))) &&
//								(tmpVar.getField("variable.size_min_um").equals(variable.getField("variable.size_min_um"))) &&
//								(tmpVar.getField("variable.size_max_um").equals(variable.getField("variable.size_max_um")))
//																										) {
//							newVariable.addField("variable.reported_parameter", tmpVar.getField("variable.parameter"));
//							newVariable.addField("variable.reported_value", tmpVar.getField("variable.value"));
//							newVariable.addField("variable.reported_unit", tmpVar.getField("variable.unit"));
//							variable.getParent().removeVariable(tmpVar);
//							break; // Break loop. 
//						}
//					}
//					variable.getParent().addVariable(newVariable);
//					
//					// Check if the new calculated value is reasonably near the reported value.
//					try {
//						if (!newVariable.getField("variable.reported_value").equals("")) {
//							Double calculatedValue = ConvUtils.convStringToDouble(newVariable.getValue());
//							Double reportedValue = ConvUtils.convStringToDouble(newVariable.getField("variable.reported_value"));
//							if ((calculatedValue > (reportedValue * 2.0)) ||
//								(calculatedValue < (reportedValue * 0.5))) {
//								importInfo.addConcatWarning("Calculated value differ too much from reported value. Parameter: " + newVariable.getParameter());								
//							}	
//						}
//					} catch (Exception e) {
//						System.out.println("DEBUG: Failed to check diff. for calc. value. Reporded value: " + newVariable.getField("variable.reported_value"));
//					}
					
				}

				if (sampledVolume != null) {					
					
					// VALUE = (COUNTNR * NPORT) / (CPORT * SMVOL) * 1000.0
					Double param = (value * numberOfPortions)
							/ (countedPortions * sampledVolume) * 1000.0;
//					param = Math.round(param * 10000.0) / 10000.0; // 4 decimals.

					Variable newVariable = variable.copyVariableAndData();
//					newVariable.setParameter("CONC");
					newVariable.setParameter("Abundance");
//					newVariable.setValue(param.toString());
					newVariable.setValue(ConvUtils.convDoubleToString(param));
					newVariable.setUnit("ind/m3");					
					newVariable.addField("variable.calc_by_dv", "Y");
					
					// Check if the same parameter was calculated by reporting institute.
					for (Object var :  variable.getParent().getVariables().toArray()) { // toArray: Variables are deleted during the loop.			
						Variable tmpVar = (Variable) var;
//						if (	(tmpVar.getField("variable.parameter").equals("CONC")) &&
						if (	(tmpVar.getField("variable.parameter").equals("Abundance")) &&
								(tmpVar.getField("variable.unit").equals("ind/m3")) &&
								(tmpVar.getField("variable.reported_scientific_name").equals(variable.getField("variable.reported_scientific_name"))) &&
								(tmpVar.getField("variable.size_class").equals(variable.getField("variable.size_class"))) && 
								(tmpVar.getField("variable.species_flag_code").equals(variable.getField("variable.species_flag_code"))) &&
								(tmpVar.getField("variable.trophic_type").equals(variable.getField("variable.trophic_type"))) &&
								(tmpVar.getField("variable.sex_code").equals(variable.getField("variable.sex_code"))) &&
								(tmpVar.getField("variable.dev_stage_code").equals(variable.getField("variable.dev_stage_code"))) &&
								(tmpVar.getField("variable.size_min_um").equals(variable.getField("variable.size_min_um"))) &&
								(tmpVar.getField("variable.size_max_um").equals(variable.getField("variable.size_max_um")))
																											) {
							newVariable.addField("variable.reported_parameter", tmpVar.getField("variable.parameter"));
							newVariable.addField("variable.reported_value", tmpVar.getField("variable.value"));
							newVariable.addField("variable.reported_unit", tmpVar.getField("variable.unit"));
							variable.getParent().removeVariable(tmpVar);
							break; // Break loop. 
						}
					}
					variable.getParent().addVariable(newVariable);						
					
					// Check if the new calculated value is reasonably near the reported value.
					try {
						if (!newVariable.getField("variable.reported_value").equals("")) {
							Double calculatedValue = ConvUtils.convStringToDouble(newVariable.getValue());
							Double reportedValue = ConvUtils.convStringToDouble(newVariable.getField("variable.reported_value"));
							if ((calculatedValue > (reportedValue * 2.0)) ||
								(calculatedValue < (reportedValue * 0.5))) {
								importInfo.addConcatWarning("Calculated value differ too much from reported value. Parameter: " + newVariable.getParameter());								
							}
						}
					} catch (Exception e) {
						System.out.println("DEBUG: Failed to check diff. for calc. value. Reporded value: " + newVariable.getField("variable.reported_value"));
					}
					
				} else if (samplerArea != null) {
					
					// VALUE = (COUNTNR * NPORT) / (CPORT * (MXDEP - MNDEP)) / (SAREA / 10000.0)
					Double param = (value * numberOfPortions)
							/ (countedPortions * (maxDepth - minDepth)) / (samplerArea / 10000.0);
//					param = Math.round(param * 10000.0) / 10000.0; // 4 decimals.

					Variable newVariable = variable.copyVariableAndData();
//					newVariable.setParameter("CONC");
					newVariable.setParameter("Abundance");
//					newVariable.setValue(param.toString());
					newVariable.setValue(ConvUtils.convDoubleToString(param));
					newVariable.setUnit("ind/m3");					
					newVariable.addField("variable.calc_by_dv", "Y");
					
					// Check if the same parameter was calculated by reporting institute.
					for (Object var :  variable.getParent().getVariables().toArray()) { // toArray: Variables are deleted during the loop.			
						Variable tmpVar = (Variable) var;
//						if (	(tmpVar.getField("variable.parameter").equals("CONC")) &&
						if (	(tmpVar.getField("variable.parameter").equals("Abundance")) &&
								(tmpVar.getField("variable.unit").equals("ind/m3")) &&
								(tmpVar.getField("variable.reported_scientific_name").equals(variable.getField("variable.reported_scientific_name"))) &&
								(tmpVar.getField("variable.size_class").equals(variable.getField("variable.size_class"))) && 
								(tmpVar.getField("variable.species_flag_code").equals(variable.getField("variable.species_flag_code"))) &&
								(tmpVar.getField("variable.trophic_type").equals(variable.getField("variable.trophic_type"))) &&
								(tmpVar.getField("variable.sex_code").equals(variable.getField("variable.sex_code"))) &&
								(tmpVar.getField("variable.dev_stage_code").equals(variable.getField("variable.dev_stage_code"))) &&
								(tmpVar.getField("variable.size_min_um").equals(variable.getField("variable.size_min_um"))) &&
								(tmpVar.getField("variable.size_max_um").equals(variable.getField("variable.size_max_um")))
																											) {
							newVariable.addField("variable.reported_parameter", tmpVar.getField("variable.parameter"));
							newVariable.addField("variable.reported_value", tmpVar.getField("variable.value"));
							newVariable.addField("variable.reported_unit", tmpVar.getField("variable.unit"));
							variable.getParent().removeVariable(tmpVar);
							break; // Break loop. 
						}
					}
					variable.getParent().addVariable(newVariable);						
					
					// Check if the new calculated value is reasonably near the reported value.
					try {
						if (!newVariable.getField("variable.reported_value").equals("")) {
							Double calculatedValue = ConvUtils.convStringToDouble(newVariable.getValue());
							Double reportedValue = ConvUtils.convStringToDouble(newVariable.getField("variable.reported_value"));
							if ((calculatedValue > (reportedValue * 2.0)) ||
								(calculatedValue < (reportedValue * 0.5))) {
								importInfo.addConcatWarning("Calculated value differ too much from reported value. Parameter: " + newVariable.getParameter());								
							}
						}
					} catch (Exception e) {
						System.out.println("DEBUG: Failed to check diff. for calc. value. Reporded value: " + newVariable.getField("variable.reported_value"));
					}
					
				}
				
				if (usedIndwetwt != null) {
					
					
					
					
// New: 2016-06-02.					
					// VALUE = COUNTNR * INDWETET
					Double param = value * usedIndwetwt;

					Variable newVariable = variable.copyVariableAndData();
					newVariable.setParameter("Wet weight");
					newVariable.setValue(ConvUtils.convDoubleToString(param));
					newVariable.setUnit("mg/analysed sample fraction");					
					newVariable.addField("variable.calc_by_dc", "Y");
					
					// Check if the same parameter was calculated by reporting institute.
					for (Object var :  variable.getParent().getVariables().toArray()) { // toArray: Variables are deleted during the loop.			
						Variable tmpVar = (Variable) var;
						if (	(tmpVar.getField("variable.parameter").equals("Wet weight")) &&
								(tmpVar.getField("variable.unit").equals("mg/analysed sample fraction")) &&
								(tmpVar.getField("variable.reported_scientific_name").equals(variable.getField("variable.reported_scientific_name"))) &&
								(tmpVar.getField("variable.size_class").equals(variable.getField("variable.size_class"))) && 
								(tmpVar.getField("variable.species_flag_code").equals(variable.getField("variable.species_flag_code"))) &&
								(tmpVar.getField("variable.trophic_type_code").equals(variable.getField("variable.trophic_type_code"))) &&
								(tmpVar.getField("variable.sex_code").equals(variable.getField("variable.sex_code"))) &&
								(tmpVar.getField("variable.dev_stage_code").equals(variable.getField("variable.dev_stage_code"))) &&
								(tmpVar.getField("variable.size_min_um").equals(variable.getField("variable.size_min_um"))) &&
								(tmpVar.getField("variable.size_max_um").equals(variable.getField("variable.size_max_um")))
																										) {
							newVariable.addField("variable.reported_parameter", tmpVar.getField("variable.parameter"));
							newVariable.addField("variable.reported_value", tmpVar.getField("variable.value"));
							newVariable.addField("variable.reported_unit", tmpVar.getField("variable.unit"));
							variable.getParent().removeVariable(tmpVar);
							break; // Break loop. 
						}
					}

					variable.getParent().addVariable(newVariable);						
				}
				
				if (usedIndwetwt != null) {
	
// TODO: We need a better solution to handle subsamples, for example 0-10 and 10-20 m.
//
//						// VALUE = (COUNTNR * INDWETWT * NPORT) / (SAREA * CPORT) * 10000.0
//						Double param = (value * usedIndwetwt * numberOfPortions)
//								/ (samplerArea * countedPortions) * 10000.0;
//		//				param = Math.round(param * 10000.0) / 10000.0; // 4 decimals.
//		
//						Variable newVariable = variable.copyVariableAndData();
//	//					newVariable.setParameter("WWEIGHT ABUND");
//						newVariable.setParameter("Wet weight/area");
////						newVariable.setValue(param.toString());
//						newVariable.setValue(ConvUtils.convDoubleToString(param));
//	//					newVariable.setUnit("mg wet weight/m2");
//						newVariable.setUnit("mg/m2");
//						newVariable.addField("variable.calc_by_dv", "Y");
//		
//						
//						// Check if the same parameter was calculated by reporting institute.
//						for (Object var :  variable.getParent().getVariables().toArray()) { // toArray: Variables are deleted during the loop.			
//							Variable tmpVar = (Variable) var;
////							if (	(tmpVar.getField("variable.parameter").equals("WWEIGHT ABUND")) &&
//							if (	(tmpVar.getField("variable.parameter").equals("Wet weight/area")) &&
//									(tmpVar.getField("variable.unit").equals("g wet weight/m2")) &&
//									(tmpVar.getField("variable.reported_scientific_name").equals(variable.getField("variable.reported_scientific_name"))) &&
//									(tmpVar.getField("variable.size_class").equals(variable.getField("variable.size_class"))) && 
//									(tmpVar.getField("variable.species_flag_code").equals(variable.getField("variable.species_flag_code"))) &&
//									(tmpVar.getField("variable.trophic_type").equals(variable.getField("variable.trophic_type"))) &&
//									(tmpVar.getField("variable.sex_code").equals(variable.getField("variable.sex_code"))) &&
//									(tmpVar.getField("variable.dev_stage_code").equals(variable.getField("variable.dev_stage_code"))) &&
//									(tmpVar.getField("variable.size_min_um").equals(variable.getField("variable.size_min_um"))) &&
//									(tmpVar.getField("variable.size_max_um").equals(variable.getField("variable.size_max_um")))
//																												) {
//								newVariable.addField("variable.reported_parameter", tmpVar.getField("variable.parameter"));
//								newVariable.addField("variable.reported_value", tmpVar.getField("variable.value"));
//								newVariable.addField("variable.reported_unit", tmpVar.getField("variable.unit"));
//								variable.getParent().removeVariable(tmpVar);
//								break; // Break loop. 
//							}
//						}
//						variable.getParent().addVariable(newVariable);
//						
//						// Check if the new calculated value is reasonably near the reported value.
//						try {
//							if (!newVariable.getField("variable.reported_value").equals("")) {
//								Double calculatedValue = ConvUtils.convStringToDouble(newVariable.getValue());
//								Double reportedValue = ConvUtils.convStringToDouble(newVariable.getField("variable.reported_value"));
//								if ((calculatedValue > (reportedValue * 2.0)) ||
//									(calculatedValue < (reportedValue * 0.5))) {
//									importInfo.addConcatWarning("Calculated value differ too much from reported value. Parameter: " + newVariable.getParameter());								
//								}
//							}
//						} catch (Exception e) {
//							System.out.println("DEBUG: Failed to check diff. for calc. value. Reporded value: " + newVariable.getField("variable.reported_value"));
//						}
//						
//					}
	
					if (sampledVolume != null) {					
						
						// VALUE = (COUNTNR * INDWETWT * NPORT) / (CPORT * SMVOL) * 1000.0
						Double param = (value * usedIndwetwt * numberOfPortions)
								/ (countedPortions * sampledVolume) * 1000.0;
	//					param = Math.round(param * 10000.0) / 10000.0; // 4 decimals.
	
						Variable newVariable = variable.copyVariableAndData();
//						newVariable.setParameter("BIOMASS CONC");
						newVariable.setParameter("Wet weight/volume");
//						newVariable.setValue(param.toString());
						newVariable.setValue(ConvUtils.convDoubleToString(param));
//						newVariable.setUnit("mg wet weight/m3");
						newVariable.setUnit("mg/m3");
						newVariable.addField("variable.calc_by_dv", "Y");
	
						// Check if the same parameter was calculated by reporting institute.
						for (Object var :  variable.getParent().getVariables().toArray()) { // toArray: Variables are deleted during the loop.			
							Variable tmpVar = (Variable) var;
//							if (	(tmpVar.getField("variable.parameter").equals("BIOMASS CONC")) &&
							if (	(tmpVar.getField("variable.parameter").equals("Wet weight/volume")) &&
//									(tmpVar.getField("variable.unit").equals("g wet weight/m3")) &&
									(tmpVar.getField("variable.unit").equals("mg/m3")) &&
									(tmpVar.getField("variable.reported_scientific_name").equals(variable.getField("variable.reported_scientific_name"))) &&
									(tmpVar.getField("variable.size_class").equals(variable.getField("variable.size_class"))) && 
									(tmpVar.getField("variable.species_flag_code").equals(variable.getField("variable.species_flag_code"))) &&
									(tmpVar.getField("variable.trophic_type").equals(variable.getField("variable.trophic_type"))) &&
									(tmpVar.getField("variable.sex_code").equals(variable.getField("variable.sex_code"))) &&
									(tmpVar.getField("variable.dev_stage_code").equals(variable.getField("variable.dev_stage_code"))) &&
									(tmpVar.getField("variable.size_min_um").equals(variable.getField("variable.size_min_um"))) &&
									(tmpVar.getField("variable.size_max_um").equals(variable.getField("variable.size_max_um")))
																											) {
								newVariable.addField("variable.reported_parameter", tmpVar.getField("variable.parameter"));
								newVariable.addField("variable.reported_value", tmpVar.getField("variable.value"));
								newVariable.addField("variable.reported_unit", tmpVar.getField("variable.unit"));
								variable.getParent().removeVariable(tmpVar);
								break; // Break loop. 
							}
						}
						variable.getParent().addVariable(newVariable);						
							
						// Check if the new calculated value is reasonably near the reported value.
						try {
							if (!newVariable.getField("variable.reported_value").equals("")) {
								Double calculatedValue = ConvUtils.convStringToDouble(newVariable.getValue());
								Double reportedValue = ConvUtils.convStringToDouble(newVariable.getField("variable.reported_value"));
								if ((calculatedValue > (reportedValue * 2.0)) ||
									(calculatedValue < (reportedValue * 0.5))) {
									importInfo.addConcatWarning("Calculated value differ too much from reported value. Parameter: " + newVariable.getParameter());								
								}
							}
						} catch (Exception e) {
							System.out.println("DEBUG: Failed to check diff. for calc. value. Reporded value: " + newVariable.getField("variable.reported_value"));
						}
						
					} else if (samplerArea != null) {
						
						// VALUE = (COUNTNR * INDWETWT * NPORT) / (CPORT * (MXDEP - MNDEP)) / (SAREA / 10000.0)
						Double param = (value * usedIndwetwt * numberOfPortions)
								/ (countedPortions * (maxDepth - minDepth)) / (samplerArea / 10000.0);
	//					param = Math.round(param * 10000.0) / 10000.0; // 4 decimals.
	
						Variable newVariable = variable.copyVariableAndData();
//						newVariable.setParameter("BIOMASS CONC");
						newVariable.setParameter("Wet weight/volume");
//						newVariable.setValue(param.toString());
						newVariable.setValue(ConvUtils.convDoubleToString(param));
//						newVariable.setUnit("mg wet weight/m3");					
						newVariable.setUnit("mg/m3");					
						newVariable.addField("variable.calc_by_dv", "Y");
	
						// Check if the same parameter was calculated by reporting institute.
						for (Object var :  variable.getParent().getVariables().toArray()) { // toArray: Variables are deleted during the loop.			
							Variable tmpVar = (Variable) var;
//							if (	(tmpVar.getField("variable.parameter").equals("BIOMASS CONC")) &&
							if (	(tmpVar.getField("variable.parameter").equals("Wet weight/volume")) &&
//									(tmpVar.getField("variable.unit").equals("g wet weight/m3")) &&
									(tmpVar.getField("variable.unit").equals("mg/m3")) &&
									(tmpVar.getField("variable.reported_scientific_name").equals(variable.getField("variable.reported_scientific_name"))) &&
									(tmpVar.getField("variable.size_class").equals(variable.getField("variable.size_class"))) && 
									(tmpVar.getField("variable.species_flag_code").equals(variable.getField("variable.species_flag_code"))) &&
									(tmpVar.getField("variable.trophic_type").equals(variable.getField("variable.trophic_type"))) &&
									(tmpVar.getField("variable.sex_code").equals(variable.getField("variable.sex_code"))) &&
									(tmpVar.getField("variable.dev_stage_code").equals(variable.getField("variable.dev_stage_code"))) &&
									(tmpVar.getField("variable.size_min_um").equals(variable.getField("variable.size_min_um"))) &&
									(tmpVar.getField("variable.size_max_um").equals(variable.getField("variable.size_max_um")))
																												) {
								newVariable.addField("variable.reported_parameter", tmpVar.getField("variable.parameter"));
								newVariable.addField("variable.reported_value", tmpVar.getField("variable.value"));
								newVariable.addField("variable.reported_unit", tmpVar.getField("variable.unit"));
								variable.getParent().removeVariable(tmpVar);
								break; // Break loop. 
							}
						}
						variable.getParent().addVariable(newVariable);						

						
						// Check if the new calculated value is reasonably near the reported value.
						try {
							if (!newVariable.getField("variable.reported_value").equals("")) {
								Double calculatedValue = ConvUtils.convStringToDouble(newVariable.getValue());
								Double reportedValue = ConvUtils.convStringToDouble(newVariable.getField("variable.reported_value"));
								if ((calculatedValue > (reportedValue * 2.0)) ||
									(calculatedValue < (reportedValue * 0.5))) {
									importInfo.addConcatWarning("Calculated value differ too much from reported value. Parameter: " + newVariable.getParameter());								
								}
							}
						} catch (Exception e) {
							System.out.println("DEBUG: Failed to check diff. for calc. value. Reporded value: " + newVariable.getField("variable.reported_value"));
						}
						
					}
				}
			}
		} catch (Exception e) {
			importInfo.addConcatWarning("Failed to calculate value. Parameter:" + variable.getParameter());
		}		
	}
	
}
