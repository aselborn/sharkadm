/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.fileimport;

import java.io.PrintStream;
import java.util.regex.Pattern;

import se.smhi.sharkadm.datasets.formats.FormatBase;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.ModelVisitor;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.utils.ConvUtils;

/**
 *	Generic reorganization of fields placed on wrong level in cluster hierarchy.
 *	Mostly used when multiple files or XML files are imported.
 *	When moving down in hierarchy no checks are needed.
 *	Will generate errors when moving up and old values that differs are overwritten.  
 *	The code is based on the Visitor pattern.
 */
public class MemoryModelReorganizeData extends ModelVisitor {

	protected PrintStream logInfo;
	protected FileImportInfo importInfo;
	private FormatBase fileImport;
	
	private boolean debug = false;
//	private boolean debug = true;

	public MemoryModelReorganizeData(PrintStream logInfo, FileImportInfo importInfo, FormatBase fileImport) {
		this.logInfo = logInfo;
		this.importInfo = importInfo;
		this.fileImport = fileImport;
	}
	
	@Override
	public void visitDataset(Dataset dataset) {
		
		// Move field values up or down to the right level.
		if (dataset.getFieldKeys() != null) {
			for (String key : dataset.getFieldKeys().toArray(new String[0])) {
				if (dataset.containsField(key)) { // Check if removed.
					String modelPart;
					if (key.contains(".")) {
						modelPart = key.substring(0, key.indexOf("."));
					} else {
						modelPart = key;
					}
					
					if (modelPart.equals("dataset")) {
						// OK.
					}
					else if (modelPart.equals("visit")) {
						for (Visit visit : dataset.getVisits()) {
							visit.addField(key, dataset.getField(key));
						}
						if (!debug) { dataset.removeField(key); } // Remove from dataset level.
					}
					else if (modelPart.equals("sample")) {
						for (Visit visit : dataset.getVisits()) {
							for (Sample sample : visit.getSamples()) {
//								sample.addField(key, visit.getField(key));
								sample.addField(key, dataset.getField(key));
							}
						}
						if (!debug) { dataset.removeField(key); } // Remove from dataset level.
					}
					else if (modelPart.equals("variable")) {
						for (Visit visit : dataset.getVisits()) {
							for (Sample sample : visit.getSamples()) {
								for (Variable variable : sample.getVariables()) {
//									variable.addField(key, sample.getField(key));
									variable.addField(key, dataset.getField(key));
								}
							}
						}
						if (!debug) { dataset.removeField(key); } // Remove from dataset level.
					} else {
						if (!key.equals("NOT_USED") && !modelPart.equals("TEMP")) {
							importInfo.addConcatError("Invalid key at cluster level: " + key);
						}
					}
				}
			}
		}
		
		// Makes it possible for import format specific actions.
		if (fileImport != null) {
			fileImport.postReorganizeDataset(dataset);
		}

		// Used by the Visitor pattern.
		for (Visit visit : dataset.getVisits().toArray(new Visit[0])) {			
			visit.Accept(this);
		}
	}

	@Override
	public void visitVisit(Visit visit) {

		// Create variables from column values at sample level.
		if (visit.getFieldKeys() != null) {
			for (String key : visit.getFieldKeys().toArray(new String[0])) {
				if (visit.containsField(key)) { // Check if removed.
//					String[] keyParts = key.split("[.]");
					String[] keyParts = key.split(Pattern.quote("."));
					if ((keyParts.length >= 3) && // Note: Unit may be missing. 
						(keyParts[1].equals("CREATE_VARIABLE"))) {
						if ((!visit.getField(key).equals("")) && 
							(!visit.getField(key).equals("NaN"))) {
							Sample sample = new Sample();
							visit.addSample(sample);						
							Variable variable = new Variable(false);
							sample.addVariable(variable);						
							variable.addField("variable.parameter", keyParts[2]);
							variable.addField("variable.value", visit.getField(key));
							if (keyParts.length >= 4) {
								variable.addField("variable.unit", keyParts[3]);
							} else {
								variable.addField("variable.unit", "");
							}
						}		
						if (!debug) { visit.removeField(key); } // Also removes NaN.
					}
					if ((keyParts.length >= 5) &&
						(keyParts[1].equals("CREATE_VARIABLE_DIVIDE"))) {
						if ((!visit.getField(key).equals("")) && 
							(!visit.getField(key).equals("NaN"))) {
							Sample sample = new Sample();
							visit.addSample(sample);						
							Variable variable = new Variable(false);
							sample.addVariable(variable);
							try {
								variable.addField("variable.parameter", keyParts[2]);
								Double value = ConvUtils.convStringToDouble(visit.getField(key)) / 
								ConvUtils.convStringToDouble(keyParts[4]);
//								variable.addField("variable.value", value.toString());
								variable.addField("variable.value", ConvUtils.convDoubleToString(value));
								variable.addField("variable.unit", keyParts[3]);
							} catch (Exception e) {
								importInfo.addConcatWarning("Failed to convert this value to float value: " + variable.getField(key));
							}								

						}		
						if (!debug) { visit.removeField(key); } // Also removes NaN.
					}
					if ((keyParts.length >= 5) &&
						(keyParts[1].equals("CREATE_VARIABLE_MULTIPLY"))) {
						if ((!visit.getField(key).equals("")) && 
							(!visit.getField(key).equals("NaN"))) {
							Sample sample = new Sample();
							visit.addSample(sample);						
							Variable variable = new Variable(false);
							sample.addVariable(variable);
							try {
								variable.addField("variable.parameter", keyParts[2]);
								Double value = ConvUtils.convStringToDouble(visit.getField(key)) / 
								ConvUtils.convStringToDouble(keyParts[4]);
//								variable.addField("variable.value", value.toString());
								variable.addField("variable.value", ConvUtils.convDoubleToString(value));
								variable.addField("variable.unit", keyParts[3]);
							} catch (Exception e) {
								importInfo.addConcatWarning("Failed to convert this value to float value: " + variable.getField(key));
							}								

						}		
						if (!debug) { visit.removeField(key); } // Also removes NaN.
					}
				}				
			}
		}

		
		// Move field values up or down to the right level.
		if (visit.getFieldKeys() != null) {
			for (String key : visit.getFieldKeys().toArray(new String[0])) {
				if (visit.containsField(key)) { // Check if removed.
					String modelPart;
					if (key.contains(".")) {
						modelPart = key.substring(0, key.indexOf("."));
					} else {
						modelPart = key;
					}
					if (modelPart.equals("dataset")) {
						// Check if exists and if content differ.
						if (visit.getParent().containsField(key)) {
							String value = visit.getParent().getField(key);
//							if (visit.getField(key) != value) {
							if (!compareValuesWithZeroInDecimalPart(visit.getField(key), value)) {
								importInfo.addConcatError("Old value is overwritten." + 
										" Key: " + key + " Old: " + value + 
										" New: " + visit.getField(key));
							}
						}
						visit.getParent().addField(key, visit.getField(key));
						if (!debug) { visit.removeField(key); } // Remove from visit level.				
					}
					else if (modelPart.equals("visit")) {
						// OK.
					}
					else if (modelPart.equals("sample")) {
						for (Sample sample : visit.getSamples()) {
							sample.addField(key, visit.getField(key));
						}
						if (!debug) { visit.removeField(key); } // Remove from visit level.				
					}
					else if (modelPart.equals("variable")) {
						for (Sample sample : visit.getSamples()) {
							for (Variable variable : sample.getVariables()) {
//								variable.addField(key, sample.getField(key));
								variable.addField(key, visit.getField(key));
							}
						}
						if (!debug) { visit.removeField(key); } // Remove from visit level.				
					} 
					else {
						if (!key.equals("NOT_USED") && !modelPart.equals("TEMP")) {
							importInfo.addConcatError("Invalid key at cluster level: " + key);
						}
					}
				}
			}
		}

		// Makes it possible for import format specific actions.
		if (fileImport != null) {
			fileImport.postReorganizeVisit(visit);
		}

		// Used by the Visitor pattern.
		for (Sample sample : visit.getSamples().toArray(new Sample[0])) {			
			sample.Accept(this);
		}
	}

	@Override
	public void visitSample(Sample sample) {
		
		// Create variables from column values at sample level.
		if (sample.getFieldKeys() != null) {
			for (String key : sample.getFieldKeys().toArray(new String[0])) {
				if (sample.containsField(key)) { // Check if removed.
//					String[] keyParts = key.split("[.]");
					String[] keyParts = key.split(Pattern.quote("."));
					if ((keyParts.length >= 3) && // Note: Unit may be missing.
						(keyParts[1].equals("CREATE_VARIABLE"))) {
						if ((!sample.getField(key).equals("")) && 
							(!sample.getField(key).equals("NaN"))) {
							Variable variable = new Variable(false);
							sample.addVariable(variable);						
							variable.addField("variable.parameter", keyParts[2]);
							variable.addField("variable.value", sample.getField(key));
							if (keyParts.length >= 4) {
								variable.addField("variable.unit", keyParts[3]);
							} else {
								variable.addField("variable.unit", "");
							}
						}		
						if (!debug) { sample.removeField(key); } // Also removes NaN.
					}
					if ((keyParts.length >= 5) &&
						(keyParts[1].equals("CREATE_VARIABLE_DIVIDE"))) {
						if ((!sample.getField(key).equals("")) && 
							(!sample.getField(key).equals("NaN"))) {
							Variable variable = new Variable(false);
							sample.addVariable(variable);
							try {
								variable.addField("variable.parameter", keyParts[2]);
								Double value = ConvUtils.convStringToDouble(sample.getField(key)) / 
								ConvUtils.convStringToDouble(keyParts[4]);
//								variable.addField("variable.value", value.toString());
								variable.addField("variable.value", ConvUtils.convDoubleToString(value));
								variable.addField("variable.unit", keyParts[3]);
							} catch (Exception e) {
								importInfo.addConcatWarning("Failed to convert this value to float value: " + variable.getField(key));
							}								

						}		
						if (!debug) { sample.removeField(key); } // Also removes NaN.
					}
					if ((keyParts.length >= 5) &&
						(keyParts[1].equals("CREATE_VARIABLE_MULTIPLY"))) {
						if ((!sample.getField(key).equals("")) && 
							(!sample.getField(key).equals("NaN"))) {
							Variable variable = new Variable(false);
							sample.addVariable(variable);
							try{
								variable.addField("variable.parameter", keyParts[2]);
								Double value = ConvUtils.convStringToDouble(sample.getField(key)) / 
								ConvUtils.convStringToDouble(keyParts[4]);
//								variable.addField("variable.value", value.toString());
								variable.addField("variable.value", ConvUtils.convDoubleToString(value));
								variable.addField("variable.unit", keyParts[3]);
							} catch (Exception e) {
								importInfo.addConcatWarning("Failed to convert this value to float value: " + variable.getField(key));
							}								

						}		
						if (!debug) { sample.removeField(key); } // Also removes NaN.
					}
				}				
			}
		}
		
		// Move field values up or down to the right level.
		if (sample.getFieldKeys() != null) {
			for (String key : sample.getFieldKeys().toArray(new String[0])) {
				if (sample.containsField(key)) { // Check if removed.
					String cluster;
					if (key.contains(".")) {
						cluster = key.substring(0, key.indexOf("."));
					} else {
						cluster = key;
					}
					if (cluster.equals("dataset")) {
						// Check if exists and if content differ.
						if (sample.getParent().getParent().containsField(key)) {
							String value = sample.getParent().getParent().getField(key);
//							if (visit.getField(key) != value) {
							if (!compareValuesWithZeroInDecimalPart(sample.getField(key), value)) {
								importInfo.addConcatError("Old value is overwritten." + 
										" Key: " + key + " Old: " + value + 
										" New: " + sample.getField(key));
							}
						}
						sample.getParent().getParent().addField(key, sample.getField(key));
						if (!debug) { sample.removeField(key); } // Remove from sample level.				
					}
					else if (cluster.equals("visit")) {
						// Check if exists and if content differ.
						if (sample.getParent().containsField(key)) {
							String value = sample.getParent().getField(key);
//							if (visit.getField(key) != value) {
							if (!compareValuesWithZeroInDecimalPart(sample.getField(key), value)) {
								importInfo.addConcatError("Old value is overwritten." + 
										" Key: " + key + " Old: " + value + 
										" New: " + sample.getField(key));
							}
						}						
						sample.getParent().addField(key, sample.getField(key));
						if (!debug) { sample.removeField(key); } // Remove from sample level.				
					}
					else if (cluster.equals("sample")) {
						// OK.				
					}
					else if (cluster.equals("variable")) {
						for (Variable variable : sample.getVariables()) {
							variable.addField(key, sample.getField(key));
						}
						if (!debug) { sample.removeField(key); } // Remove from sample level.				
					}
					else if (cluster.equals("TEMP")) {
						// OK.
					} else {
						if (!key.equals("NOT_USED") && !cluster.equals("TEMP")) {
							importInfo.addConcatError("Invalid key at cluster level: " + key);
						}
					}
				}				
			}
		}
		
		// Makes it possible for import format specific actions.
		if (fileImport != null) {
			fileImport.postReorganizeSample(sample);
		}

		// Used by the Visitor pattern.
		// Array needed since variables are created.
		for (Variable variable : sample.getVariables().toArray(new Variable[0])) {			
			variable.Accept(this);
		}
	}

	@Override
	public void visitVariable(Variable variable) {
		
		// Move field values up or down to the right level.
		if (variable.getFieldKeys() != null) {
			for (String key : variable.getFieldKeys().toArray(new String[0])) {
				if (variable.containsField(key)) { // Check if removed.
					String modelPart;
					if (key.contains(".")) {
						modelPart = key.substring(0, key.indexOf("."));
					} else {
						modelPart = key;
					}
					if (modelPart.equals("dataset")) {
						// Check if exists and if content differ.
						if (variable.getParent().getParent().getParent().containsField(key)) {
							String value = variable.getParent().getParent().getParent().getField(key);
//							if (visit.getField(key) != value) {
							if (!compareValuesWithZeroInDecimalPart(variable.getField(key), value)) {
								importInfo.addConcatError("Old value is overwritten." + 
										" Key: " + key + " Old: " + value + 
										" New: " + variable.getField(key));
							}
						}
						variable.getParent().getParent().getParent().addField(key, variable.getField(key));
						if (!debug) { variable.removeField(key); } // Remove from variable level.				
					}
					else if (modelPart.equals("visit")) {
						// Check if exists and if content differ.
						if (variable.getParent().getParent().containsField(key)) {
							String value = variable.getParent().getParent().getField(key);
//							if (visit.getField(key) != value) {
							if (!compareValuesWithZeroInDecimalPart(variable.getField(key), value)) {
								importInfo.addConcatError("Old value is overwritten." + 
										" Key: " + key + " Old: " + value + 
										" New: " + variable.getField(key));
							}
						}
						variable.getParent().getParent().getParent().addField(key, variable.getField(key));
						if (!debug) { variable.removeField(key); } // Remove from variable level.				
					}
					else if (modelPart.equals("sample")) {
						// Check if exists and if content differ.
						if (variable.getParent().containsField(key)) {
							String value = variable.getParent().getField(key);
//							if (visit.getField(key) != value) {
							if (!compareValuesWithZeroInDecimalPart(variable.getField(key), value)) {
								importInfo.addConcatError("Old value is overwritten." + 
										" Key: " + key + " Old: " + value + 
										" New: " + variable.getField(key));
							}
						}
						variable.getParent().getParent().addField(key, variable.getField(key));
						if (!debug) { variable.removeField(key); } // Remove from variable level.				
					}
					else if (modelPart.equals("variable")) {
						// OK.
					}
					else if (modelPart.equals("TEMP")) {
						// OK.
					} else {
						if (!key.equals("NOT_USED") && !modelPart.equals("TEMP")) {
							importInfo.addConcatError("Invalid key at cluster level: " + key);
						}
					}	
				}
			}

			// Modify parameter/value/unit or copy variable and modify parameter/value/unit. 
			for (String key : variable.getFieldKeys().toArray(new String[0])) {
				if (variable.containsField(key)) { // Check if removed.
//					String[] keyParts = key.split("[.]");
					String[] keyParts = key.split(Pattern.quote("."));
					if ((keyParts.length >= 3) && // Note: Unit may be missing.
						(keyParts[1].equals("MODIFY_VARIABLE"))) {
						if ((!variable.getField(key).equals("")) && 
							(!variable.getField(key).equals("NaN"))) {
							variable.addField("variable.parameter", keyParts[2]);
							variable.addField("variable.value", variable.getField(key));
							if (keyParts.length >= 4) {
								variable.addField("variable.unit", keyParts[3]);
							} else {
								variable.addField("variable.unit", "");
							}
						}		
						if (!debug) { variable.removeField(key); } // Also removes NaN.
					}
					if ((keyParts.length >= 5) &&
						(keyParts[1].equals("MODIFY_VARIABLE_DIVIDE"))) {
						if ((!variable.getField(key).equals("")) && 
							(!variable.getField(key).equals("NaN"))) {
							try {
								variable.addField("variable.parameter", keyParts[2]);
								Double value = ConvUtils.convStringToDouble(variable.getField(key)) / 
								ConvUtils.convStringToDouble(keyParts[4]);
//								variable.addField("variable.value", value.toString());
								variable.addField("variable.value", ConvUtils.convDoubleToString(value));
								variable.addField("variable.unit", keyParts[3]);
							} catch (Exception e) {
								importInfo.addConcatWarning("Failed to convert this value to float value: " + variable.getField(key));
							}								
							
						}		
						if (!debug) { variable.removeField(key); } // Also removes NaN.
					}
					if ((keyParts.length >= 5) &&
						(keyParts[1].equals("MODIFY_VARIABLE_MULTIPLY"))) {
						if ((!variable.getField(key).equals("")) && 
							(!variable.getField(key).equals("NaN"))) {
							try {
								variable.addField("variable.parameter", keyParts[2]);
								Double value = ConvUtils.convStringToDouble(variable.getField(key)) * 
								ConvUtils.convStringToDouble(keyParts[4]);
//								variable.addField("variable.value", value.toString());
								variable.addField("variable.value", ConvUtils.convDoubleToString(value));
								variable.addField("variable.unit", keyParts[3]);
							} catch (Exception e) {
								importInfo.addConcatWarning("Failed to convert this value to float value: " + variable.getField(key));
							}								
						}		
						if (!debug) { variable.removeField(key); } // Also removes NaN.
					}
					if ((keyParts.length >= 3) && // Note: Unit may be missing.
						(keyParts[1].equals("COPY_VARIABLE"))) {
						if ((!variable.getField(key).equals("")) && 
							(!variable.getField(key).equals("NaN"))) {
							
							Variable newVariable;
							// Don't add a new variable if the current variable is empty. 
							if ((variable.getParameter().equals("")) && 
								(variable.getValue().equals("")) && 
								(variable.getField("variable.parameter").equals(""))  && 
								(variable.getField("variable.value").equals("")) ) {
								
								newVariable = variable;
							} else {
								if (variable.isCommunity()) {
									newVariable = new Variable(true);
								} else {
									newVariable = new Variable(false);
								}								
								variable.getParent().addVariable(newVariable);						
								newVariable.copyData(variable);
							}

							newVariable.addField("variable.parameter", keyParts[2]);
							newVariable.addField("variable.value", variable.getField(key));
							if (keyParts.length >= 4) {
								newVariable.addField("variable.unit", keyParts[3]);
							} else {
								newVariable.removeField("variable.unit");
							}
						}		
						if (!debug) { variable.removeField(key); } // Also removes NaN.
					}
					if ((keyParts.length >= 5) &&
						(keyParts[1].equals("COPY_VARIABLE_DIVIDE"))) {
						if ((!variable.getField(key).equals("")) && 
							(!variable.getField(key).equals("NaN"))) {

							Variable newVariable;
							// Don't add a new variable if the current variable is empty. 
							if ((variable.getParameter().equals("")) && 
								(variable.getValue().equals("")) && 
								(variable.getField("variable.parameter").equals(""))  && 
								(variable.getField("variable.value").equals("")) ) {
								
								newVariable = variable;
							} else {
								if (variable.isCommunity()) {
									newVariable = new Variable(true);
								} else {
									newVariable = new Variable(false);
								}								
								variable.getParent().addVariable(newVariable);						
								newVariable.copyData(variable);
							}

							try {
								newVariable.addField("variable.parameter", keyParts[2]);
								Double value = ConvUtils.convStringToDouble(variable.getField(key)) / 
								ConvUtils.convStringToDouble(keyParts[4]);
//								newVariable.addField("variable.value", value.toString());
								newVariable.addField("variable.value", ConvUtils.convDoubleToString(value));
								newVariable.addField("variable.unit", keyParts[3]);
							} catch (Exception e) {
								importInfo.addConcatWarning("Failed to convert this value to float value: " + variable.getField(key));
							}							
						}		
						if (!debug) { variable.removeField(key); } // Also removes NaN.
					}
					if ((keyParts.length >= 5) &&
						(keyParts[1].equals("COPY_VARIABLE_MULTIPLY"))) {
						if ((!variable.getField(key).equals("")) && 
							(!variable.getField(key).equals("NaN"))) {
							
							Variable newVariable;
							// Don't add a new variable if the current variable is empty. 
							if ((variable.getParameter().equals("")) && 
								(variable.getValue().equals("")) && 
								(variable.getField("variable.parameter").equals(""))  && 
								(variable.getField("variable.value").equals("")) ) {
								
								newVariable = variable;
							} else {
								if (variable.isCommunity()) {
									newVariable = new Variable(true);
								} else {
									newVariable = new Variable(false);
								}								
								variable.getParent().addVariable(newVariable);						
								newVariable.copyData(variable);
							}
					
							try {
								newVariable.addField("variable.parameter", keyParts[2]);
								Double value = ConvUtils.convStringToDouble(variable.getField(key)) * 
								ConvUtils.convStringToDouble(keyParts[4]);
//								newVariable.addField("variable.value", value.toString());
								newVariable.addField("variable.value", ConvUtils.convDoubleToString(value));
								newVariable.addField("variable.unit", keyParts[3]);
							} catch (Exception e) {
								importInfo.addConcatWarning("Failed to convert this value to float value: " + variable.getField(key));
							}								
						}		
						if (!debug) { variable.removeField(key); } // Also removes NaN.
					}
					
//					// Not reorganization, but multiplies or divides values if necessary.
//					// Example: From "variable.indwetwt.DIVIDE.1000"
//					// to "variable.indwetwt" and the value is divided by 1000.
//				
//					if ((keyParts.length >= 5) &&
//						(keyParts[3].equals("MULTIPLY"))) {
//						if ((!variable.getField(key).equals("")) && 
//							(!variable.getField(key).equals("NaN"))) {
//								
//							Double value = ConvUtils.convStringToDouble(variable.getField(key)) * 
//										   ConvUtils.convStringToDouble(keyParts[4]);
////						variable.addField(keyParts[0] + "." + keyParts[1] + "." + keyParts[2], value.toString());
//							variable.addField(keyParts[0] + "." + keyParts[1] + "." + keyParts[2], ConvUtils.convDoubleToString(value));
//						}		
//						if (!debug) { variable.removeField(key); } } // Also removes NaN.
//					}
//
//					if ((keyParts.length >= 5) &&
//						(keyParts[3].equals("DIVIDE"))) {
//						if ((!variable.getField(key).equals("")) && 
//							(!variable.getField(key).equals("NaN"))) {
//								
//							Double value = ConvUtils.convStringToDouble(variable.getField(key)) / 
//										   ConvUtils.convStringToDouble(keyParts[4]);
////						variable.addField(keyParts[0] + "." + keyParts[1] + "." + keyParts[2], value.toString());
//							variable.addField(keyParts[0] + "." + keyParts[1] + "." + keyParts[2], ConvUtils.convDoubleToString(value));
//						}		
//						if (!debug) { variable.removeField(key); } } // Also removes NaN.
//					}
				}
			}
		}
		
		// Makes it possible for import format specific actions.
		if (fileImport != null) {
			fileImport.postReorganizeVariable(variable);
		}
	}

	public boolean compareValuesWithZeroInDecimalPart(String firstValue, String secondValue) {
		// Normal compare. 
		if (firstValue.length() == secondValue.length()) {
			if (firstValue.equals(secondValue)) {
				return true;
			} else {
				return false;
			}
		}
		// Compare with different lengths.
		String first = firstValue;
		String second = secondValue;
		if ((firstValue.endsWith(".00")) || (firstValue.endsWith(":00"))) {
			first = firstValue.replace(".00", "").replace(":00", "");
		}
		if ((secondValue.endsWith(".00")) || (secondValue.endsWith(":00"))) {
			second = secondValue.replace(".00", "").replace(":00", "");
		}
		if (first.equals(second)) {
			return true;
		} else {
			return false;
		}		
	}

}
