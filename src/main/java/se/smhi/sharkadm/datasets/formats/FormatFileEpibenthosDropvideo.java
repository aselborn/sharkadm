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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import se.smhi.sharkadm.datasets.fileimport.FileImportInfo;
import se.smhi.sharkadm.facades.ImportFacade;
import se.smhi.sharkadm.facades.ModelFacade;
import se.smhi.sharkadm.fileimport.misc.FileImportTranslate;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.utils.ParseFileUtil;

/**
 * Import format for Bacterioplankton.
 *
 */
public class FormatFileEpibenthosDropvideo extends FormatFileBase {


	private Map<String, String> columnParamsDict = new HashMap<String, String>();
	
	public FormatFileEpibenthosDropvideo(PrintStream logInfo, FileImportInfo importInfo) {
		super(logInfo, importInfo);
		
		columnParamsDict.put("section_hard_clay_cover_class", "Hard clay cover");
		columnParamsDict.put("section_silt_soft_clay_cover_class", "Silt soft clay cover");
		columnParamsDict.put("section_sand_cover_class", "Sand cover");
		columnParamsDict.put("section_gravel_cover_class", "Gravel cover");
		columnParamsDict.put("section_stone_cover_class", "Stone cover");
		columnParamsDict.put("section_boulder_cover_class", "Boulder cover");
		columnParamsDict.put("section_rock_cover_class", "Rock cover");
		columnParamsDict.put("section_shell_gravel_cover_class", "Shell gravel cover");
		columnParamsDict.put("section_shell_cover_class", "Shell cover");
		columnParamsDict.put("section_bare_substrate_cover_class", "Bare substrate cover");
		columnParamsDict.put("section_debris_cover_class", "Debris cover");
		columnParamsDict.put("section_epi_zostera_cover_class", "Epi zostera cover");
		columnParamsDict.put("section_unidentified_plantae_cover_class", "Unidentified plantae cover");
		columnParamsDict.put("section_nassarius_tracks_cover_class", "Nassarius tracks cover");
		columnParamsDict.put("section_paguridae_tracks_cover_class", "Paguridae tracks cover");
		columnParamsDict.put("section_animalia_burrows_cover_class", "Animalia burrows cover");
		columnParamsDict.put("section_animalia_tracks_cover_class", "Animalia tracks cover");
		columnParamsDict.put("section_unidentified_algae_cover_class", "Unidentified algae cover");
		
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

		loadKeyTranslator(importMatrixColumn, "import_matrix_epibenthos_dropvideo.txt");
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
				currentVariable = new Variable(true);
				currentSample.addVariable(currentVariable);
				boolean firstVariableInSample = true;
				
//				// NOTE. All species are bacteria.
//				currentVariable.addField("variable.dyntaxa_id", "5000052"); // Bacteria
								
				// Add each column value.
				Integer numberOfParameterValuesAdded = 0;
				for (String columnName : header) {
					String key = translateKey(columnName);
					if (!key.contains("MISSING KEY")) {
						// Add values for columns specified in the import matrix.
						addVariableField(key, getCell(row, columnName));
					} else {
						// Species columns contains "MISSING KEY" since they are not specified in the import matrix. Used for species columns.
						if (!getCell(row, columnName).equals("")) {
							// Create variable for this row.
							numberOfParameterValuesAdded += 1;
							Variable newVariable;
							if (firstVariableInSample) {
								// Reuse if first time.
								newVariable = currentVariable;
								firstVariableInSample = false;
							} else {
								newVariable = currentVariable.copyVariableAndData();
								currentVariable.getParent().addVariable(newVariable);
							}
							
							
							// Split. "MISSING KEY" - Scientific_name - SFLAG+value - STRID+value. 
							String lastControlString = "";
							String scientificName = "";
							String speciesFlag = "";
							String stratumId = "";
							String stage = "";
							String[] parts = key.split(Pattern.quote(" "));
							for (String part : parts) {
								if (part.equals("MISSING")) {
									lastControlString = part;
								}
								if (part.equals("KEY:")) {
									lastControlString = part;
								}
								else if (part.equals("SFLAG")) {
									lastControlString = part;									
								}
								else if (part.equals("STRID")) {
									lastControlString = part;
								}
								else if (part.equals("STAGE")) {
									lastControlString = part;
								}
								else {
									if (lastControlString.equals("KEY:")) {
										if (scientificName.equals("")) {
											scientificName = part;
										} else {
											scientificName = scientificName + ' ' + part;											
										}
									}
									else if (lastControlString.equals("SFLAG")) {
										if (speciesFlag.equals("")) {
											speciesFlag = part;
										} else {
											speciesFlag = speciesFlag + ' ' + part;											
										}
									}
									else if (lastControlString.equals("STRID")) {
										if (stratumId.equals("")) {
											stratumId = part;
										} else {
											stratumId = stratumId + ' ' + part;											
										}
									}
									else if (lastControlString.equals("STAGE")) {
										if (stage.equals("")) {
											stage = part;
										} else {
											stage = stage + ' ' + part;											
										}
									}
								}
							}
							
							// System.out.println("DEBUG: ScientificName: " + scientificName + "  SpeciesFlag: " + speciesFlag + "  StratumId: " + stratumId);
							
							if (getCellByKey(row, "variable.image_sequence").equals("Hela")) {
								//
								newVariable.addField("variable.reported_scientific_name", scientificName);
								newVariable.addField("variable.parameter", "Observed species");
								newVariable.addField("variable.value", "Y");
								newVariable.addField("variable.unit", "");
								newVariable.addField("variable.species_flag", speciesFlag);
								newVariable.addField("variable.stratum_id", stratumId);
								newVariable.addField("variable.dev_stage_code", stage);
							} else {
								newVariable.addField("variable.reported_scientific_name", scientificName);
								newVariable.addField("variable.parameter", "Cover class");
								newVariable.addField("variable.value", getCell(row, columnName));
								newVariable.addField("variable.unit", "");
								newVariable.addField("variable.species_flag", speciesFlag);
								newVariable.addField("variable.stratum_id", stratumId);
								newVariable.addField("variable.dev_stage_code", stage);
							}
						}
					}
				}
				if (numberOfParameterValuesAdded == 0) {
					currentVariable.addField("variable.parameter", "No species found.");
					currentVariable.addField("variable.value", "");
					currentVariable.addField("variable.unit", "");
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
						   getCellByKey(row, "sample.transect_start_latitude_dd") + ":" +
						   getCellByKey(row, "sample.transect_start_longitude_dd") + ":" +
						   getCellByKey(row, "sample.transect_end_latitude_dd") + ":" +
						   getCellByKey(row, "sample.transect_end_longitude_dd") + ":" +
						   getCellByKey(row, "sample.sample_id");
		
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

		try {
		
			// Dictionaries and lists.
			Map<String, Integer> calcValueDict = new HashMap<String, Integer>();
			Set<String> imageIdSet = new HashSet<String>();
			//
			Map<String, Integer> columnParamsValueDict = new HashMap<String, Integer>();
			Set<String> imageForColumnParamsIdSet = new HashSet<String>();
			
			imageForColumnParamsIdSet.clear();
			
			// Loop over species. Add counted cover values in calcValueDict.
			for (Variable variable : sample.getVariables()) {			
				String imageId = variable.getField("variable.image_sequence");
				String scientificName = variable.getField("variable.reported_scientific_name");
				String parameter = variable.getField("variable.parameter");
				String value = variable.getField("variable.value");
				String speciesFlag = variable.getField("variable.species_flag");
				String stratumId = variable.getField("variable.stratum_id");
				String stage = variable.getField("variable.dev_stage_code");
				if (parameter.equals("Cover class") &&
					(imageId.equals("1") || imageId.equals("2") || imageId.equals("3") || imageId.equals("4") || 
					imageId.equals("5") || imageId.equals("6") || imageId.equals("7") || imageId.equals("8") || 
					imageId.equals("9") || imageId.equals("10"))) {
				
					String key = scientificName + "<+>" + speciesFlag + "<+>" + stratumId + "<+>" + stage;
					try {
						Integer valueInt = Integer.parseInt(value);
						
						if (!calcValueDict.containsKey(key)) {
							calcValueDict.put(key, 0);
						}
						calcValueDict.put(key, calcValueDict.get(key) + valueInt);
					} catch (Exception e) {
						// e.printStackTrace();
						System.out.println("DEBUG: Failed to parse integer: " + value);
						importInfo.addConcatError("Failed to parse integer: " + value);
					}
				}
				// Used to check if 10 images are used.
				if (!imageId.equals("Hela")) {
					if (!imageId.equals("")) {
						imageIdSet.add(imageId);
					}
				}
				
				// Sum columns values.
				if (!imageForColumnParamsIdSet.contains(imageId)) { // It's enough to check one image in each sample.
					for (String key : columnParamsDict.keySet()) {
						String columnValue = variable.getField("variable." + key);
						if (!columnValue.equals("")) {
							imageForColumnParamsIdSet.add(imageId); // Value found, must be community variable.
							try {
								Integer columnValueInt = Integer.parseInt(columnValue);
								if (!columnParamsValueDict.containsKey(key)) {
									columnParamsValueDict.put(key, 0);
								}
								columnParamsValueDict.put(key, columnParamsValueDict.get(key) + columnValueInt);
							} catch (Exception e) {
								// e.printStackTrace();
								System.out.println("DEBUG: Failed to parse integer: " + columnValue);
								importInfo.addConcatError("Failed to parse integer: " + columnValue);
							}
						}
					}
				}
			}
			// Create new variables for aggregated values from 10 images. Value 0-10 per image gives 0-100 % on sample. 
			if (imageIdSet.size() == 10) {
				for (String key : calcValueDict.keySet()) {
					
					String[] keyParts = key.split(Pattern.quote("<+>"));
					String scientificName = keyParts[0].trim();
					String speciesFlag = "";
					String stratumId = "";
					String stage = "";
					String value = calcValueDict.get(key).toString();
					
					if (keyParts.length > 1) {
						speciesFlag = keyParts[1].trim();
					}
					if (keyParts.length > 2) {
						stratumId = keyParts[2].trim();
					}
					if (keyParts.length > 3) {
						stage = keyParts[3].trim();
					}
					Variable newVariable = currentVariable.copyVariableAndData();
					sample.addVariable(newVariable);
	
					newVariable.addField("variable.reported_scientific_name", scientificName);
					newVariable.addField("variable.parameter", "Cover");
					newVariable.addField("variable.value", value);
					newVariable.addField("variable.unit", "%");
					newVariable.addField("variable.species_flag", speciesFlag);
					newVariable.addField("variable.stratum_id", stratumId);
					newVariable.addField("variable.dev_stage_code", stage);
				}
	
			
				for (String key : columnParamsValueDict.keySet()) {
					
					String parameterName = columnParamsDict.get(key);
					String value = columnParamsValueDict.get(key).toString();
	
					Variable newVariable = new Variable(false);
					sample.addVariable(newVariable);
					
					newVariable.addField("variable.parameter", parameterName);
					newVariable.addField("variable.value", value);
					newVariable.addField("variable.unit", "%");
				}
	
			
			} else {
				if (imageIdSet.size() > 0) {
					importInfo.addConcatError("Dropvideo transect does not contain 10 images." +
							   " Date: " + sample.getParent().getField("visit.visit_date") + 
							   " Station: " + sample.getParent().getField("visit.reported_station_name"));
				}
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("DEBUG: Exception in postReorganizeSample for dropvideo. ");
		}
 
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
