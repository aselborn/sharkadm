/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.calc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.smhi.sharkadm.datasets.fileimport.FileImportInfo;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.species.BvolManager;
import se.smhi.sharkadm.species.BvolObject;
import se.smhi.sharkadm.species_old.PEGObject;
import se.smhi.sharkadm.species_old.TaxonManager;
import se.smhi.sharkadm.species_old.TaxonNode;
import se.smhi.sharkadm.species_old.TaxonObject;
import se.smhi.sharkadm.utils.ConvUtils;

public class PhytoplanktonCalcObject {
	
	FileImportInfo importInfo; // Used for logging.
	
	private String taxonName = "";
	private String sizeClass = "";
	
	private BvolObject bvolObject = null;
	
	private boolean paramAbund = false;
	private boolean paramCountnr = false;
	private boolean paramConc1_5 = false;
	private boolean paramBiovol = false;
	private boolean paramCarbonPerUnit = false;
	private boolean paramCarbonPerLitre = false;
	private boolean colCoeff = false;
	private boolean colReportedCevol = false;
	
	private Double paramAbundValue = 0.0;
	private Double paramCountnrValue = 0.0;
	private Double paramConc1_5Value = 0.0;
	private Double paramBiovolValue = 0.0;
	private Double paramCarbonPerUnitValue = 0.0;
	private Double paramCarbonPerLitreValue = 0.0;
	private Double colCoeffValue = 0.0;
	private Double colReportedCevolValue = 0.0;

	private Double newAbundValue = 0.0;
	private Double newBiovolValue = 0.0;
	private Double newCarbonValue = 0.0;
	private Double usedBiovolValue = 0.0;
	private Double usedCevolValue = 0.0;
	private Double usedCarbonPerUnitValue = 0.0;
	
	public PhytoplanktonCalcObject(FileImportInfo importInfo) {
		super();
		this.importInfo = importInfo;
	}

	public void checkExistingParametersAndFields(List<Variable> variables) {
		for (Variable v : variables) {
			// Get taxon name, size and HELCOM PEG object.
			if (taxonName.equals("")) {
				taxonName = v.getField("variable.bvol_scientific_name");
//				taxonName = v.getField("variable.scientific_name");
				if (taxonName.equals("")) {
					taxonName = v.getField("variable.reported_scientific_name");
				}
				sizeClass = v.getField("variable.bvol_size_class");
//				sizeClass = v.getField("variable.size_class");
				getPegObject();
			}
			
			String parameter = v.getField("variable.parameter");
			
			if (parameter.equals("# counted")) {
				paramCountnrValue = ConvUtils.convStringToDouble(v.getField("variable.value"));
				paramCountnr = true;
				if (paramCountnrValue == null) {
					System.out.println("DEBUG: Can't convert string to double. paramCountnrValue");
				}
			}
			if (parameter.equals("Abundance")) {
				paramAbundValue = ConvUtils.convStringToDouble(v.getField("variable.value"));
				paramAbund = true;
				if (paramAbundValue == null) {
					System.out.println("DEBUG: Can't convert string to double. paramAbundValue: " + v.getField("variable.value"));
				}
			}
			if (parameter.equals("Abundance class")) {
				paramConc1_5Value = ConvUtils.convStringToDouble(v.getField("variable.value"));
				paramConc1_5 = true;
				if (paramConc1_5Value == null) {
					System.out.println("DEBUG: Can't convert string to double. paramConc1_5Value");
				}
			}
			if (parameter.equals("Biovolume concentration")) {
				paramBiovolValue = ConvUtils.convStringToDouble(v.getField("variable.value"));
				paramBiovol = true;
				if (paramBiovolValue == null) {
					System.out.println("DEBUG: Can't convert string to double. colBiovolValue");
				}
			}
			if (parameter.equals("Carbon ???")) {
				paramCarbonPerUnitValue = ConvUtils.convStringToDouble(v.getField("variable.value"));
				paramCarbonPerUnit = true;
				if (paramCarbonPerUnitValue == null) {
					System.out.println("DEBUG: Can't convert string to double. colCarbonPerUnitValue");
				}
			}
			if (parameter.equals("Carbon concentration")) {
				paramCarbonPerLitreValue = ConvUtils.convStringToDouble(v.getField("variable.value"));
				paramCarbonPerLitre = true;
				if (paramCarbonPerLitreValue == null) {
					System.out.println("DEBUG: Can't convert string to double. colCarbonPerLitreValue");
				}
			}
			if (!v.getField("variable.coefficient").equals("")) {
				colCoeffValue = ConvUtils.convStringToDouble(v.getField("variable.coefficient"));
				colCoeff = true;
				if (colCoeffValue == null) {
					System.out.println("DEBUG: Can't convert string to double. colCoeffValue");
				}
			}
			if (!v.getField("variable.reported_cell_volume_um3").equals("")) {
				colReportedCevolValue = ConvUtils.convStringToDouble(v.getField("variable.reported_cell_volume_um3"));
				colReportedCevol = true;
				if (colReportedCevolValue == null) {
					System.out.println("DEBUG: Can't convert string to double. colReportedCevol");
				}
			}
		}
		
	}

	public void modifyVariable(Variable variable, 
			   String parameter, Double value, String unit) { 
		
		String oldParameter = variable.getField("variable.parameter");
		String oldValue = variable.getField("variable.value");
		String oldUnit = variable.getField("variable.unit");

		variable.addField("variable.parameter", parameter);
		variable.addField("variable.value", ConvUtils.convDoubleToString(value));
		variable.addField("variable.unit", unit);

		variable.addField("variable.calc_by_dc", "Y");
		
		variable.addField("variable.reported_parameter", oldParameter);
		variable.addField("variable.reported_value", oldValue);
		variable.addField("variable.reported_unit", oldUnit);
	}

	public void addVariable(Variable variable, 
							String parameter, Double value, String unit) {

		Variable newVariable = variable.copyVariableAndData();
		variable.getParent().addVariable(newVariable);
		
		newVariable.addField("variable.parameter", parameter);
		newVariable.addField("variable.value", ConvUtils.convDoubleToString(value));
		newVariable.addField("variable.unit", unit);
		
		newVariable.addField("variable.calc_by_dc", "Y");
		// Remove these if they were added by a previous modifyVariable().
		newVariable.removeField("variable.reported_parameter");
		newVariable.removeField("variable.reported_value");
		newVariable.removeField("variable.reported_unit");
	}
	
	public void calculateAbundance(List<Variable> variables, String usedUnit) {
		// Countnr and Coeff needed to calculate Abundance.
		if (isParamCountnr() && isColCoeff()) {
			// Find variable to modify.
			Variable usedVariable = null;
			if (paramAbund) {
				for (Variable v : variables) {
					if (v.getField("variable.parameter").equals("Abundance")) {
						if (usedVariable == null) {
							usedVariable = v;
						} else {
							importInfo.addConcatWarning(
							"PP calculations, abundance: Duplicated rows for 'taxon-size-sflag-trophy'. Species/size: " + 
							this.getTaxonName() + " / " + this.getSizeClass() ); 
						}
					}
				}
			} else {
				if (variables.size() > 0) {
					usedVariable = variables.get(0);
				}
			}
				
			if (usedVariable != null) {
				// Calculate ABUND = COEFF * COUNTNR.
				newAbundValue = paramCountnrValue * colCoeffValue;
				if (paramAbund) {
//					modifyVariable(usedVariable, "Abundance", newAbundValue, "ind/l or 100 um pieces/l");
					modifyVariable(usedVariable, "Abundance", newAbundValue, usedUnit);
					
					// Check if the new calculated value is reasonably near the reported value.
					try {
						if ((newAbundValue > (paramAbundValue * 2.0)) ||
							(newAbundValue < (paramAbundValue * 0.5))) {
							importInfo.addConcatWarning(
									"PP calculations: Calculated value differ too much from reported value. Parameter: Abundance."); 
							importInfo.addConcatWarning(
									"Calculated value differ too much from reported value. Parameter: Abundance." + 
								  	" Calculated value: " + newAbundValue + 								
						  			" Reported value: " + paramAbundValue +
						  			" Species/size: " + this.getTaxonName() + " / " + this.getSizeClass());								
						}
					} catch (Exception e) {
						System.out.println("DEBUG: Failed to check diff. for calc. value. Parameter: Abundance." + 
									  	" Calculated value: " + newAbundValue + 								
							  			" Reported value: " + paramAbundValue);								
					}

				} else {
					if (variables.size() > 0) {
//						addVariable(variables.get(0), "Abundance", newAbundValue, "ind/l or 100 um pieces/l");
						addVariable(variables.get(0), "Abundance", newAbundValue, usedUnit);
					}
				}
			}
		}
	}

	public void calculateBiovolume(List<Variable> variables) {
		
//		if (taxonName.equals("Snowella") && sizeClass.equals("2")) {
//			System.out.println("DEBUG: Snowella.");
//		}

		// Find variable.
		Variable usedVariable = null;
		if (paramBiovol) {
			for (Variable v : variables) {
				if (v.getField("variable.parameter").equals("Biovolume concentration")) {
					if (usedVariable == null) {
						usedVariable = v;
					} else {
						importInfo.addConcatWarning(
						"PP calculations, biovolume: Duplicated rows for 'taxon-size-sflag-trophy'. Species/size: " + 
						this.getTaxonName() + " / " + this.getSizeClass() ); 
					}
				}
			}
		} else {
			if (variables.size() > 0) {
				usedVariable = variables.get(0);
			}
		}
		
		// Get CEVOL.
		if (bvolObject != null) {
			// Use PEG value (unit um^3) if available.
			try {
				usedCevolValue = ConvUtils.convStringToDouble(bvolObject.getCalculatedVolume()) * Math.pow(10, -9);  // mm^3
			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage());
				e.printStackTrace();
			}
		} else {
			if (colReportedCevol) {
				usedCevolValue = colReportedCevolValue * Math.pow(10, -9);  // assuming um^3 and convert
			}
		}
		
		if (usedCevolValue > 0.0) {
			// Calculate BIOVOLUME = ABUND * CEVOL.  (#/l)
			if (newAbundValue > 0.0) {
				newBiovolValue = newAbundValue * usedCevolValue;
			} else {
				newBiovolValue = paramAbundValue * usedCevolValue;
			}
//			newBiovolValue = Math.round(newBiovolValue * 10000.0) / 10000.0; // 4 decimals.
			if (paramBiovol) {
				modifyVariable(usedVariable, "Biovolume concentration", newBiovolValue, "mm3/l");
				
				// Check if the new calculated value is reasonably near the reported value.
				try {
					if ((newBiovolValue > (paramBiovolValue * 2.0)) ||
						(newBiovolValue < (paramBiovolValue * 0.5))) {
						importInfo.addConcatWarning(
								"PP calculations: Calculated value differ too much from reported value. Parameter: Biovolume concentration."); 
						importInfo.addConcatWarning(
									"Calculated value differ too much from reported value. Parameter: Biovolume concentration." + 
								  	" Calculated value: " + newBiovolValue + 								
						  			" Reported value: " + paramBiovolValue +
						  			" Species/size: " + this.getTaxonName() + " / " + this.getSizeClass());								
					}
				} catch (Exception e) {
					System.out.println("DEBUG: Failed to check diff. for calc. value. Parameter: Biovolume concentration." + 
								  	" Calculated value: " + newBiovolValue + 								
						  			" Reported value: " + paramBiovolValue);								
				}

			} else {
				addVariable(usedVariable, "Biovolume concentration", newBiovolValue, "mm3/l");

			}
		}
	}
		
	public void calculateCarbon(List<Variable> variables) {

		Variable usedVariable = null;
		if (paramCarbonPerLitre) {
			for (Variable v : variables) {
				if (v.getField("variable.parameter").equals("Carbon concentration")) {
					if (usedVariable == null) {
						usedVariable = v;
					} else {
						importInfo.addConcatWarning(
						"PP calculations, carbon: Duplicated rows for 'taxon-size-sflag-trophy'. Species/size: " + 
						this.getTaxonName() + " / " + this.getSizeClass() ); 
					}
				}
			}
		} else {
			if (variables.size() > 0) {
				usedVariable = variables.get(0);
			}
		}
		
		// Get CarbonPerUnit.
		if (bvolObject != null) {
			// Use PEG value if available.
			try {
				usedCarbonPerUnitValue = ConvUtils.convStringToDouble(bvolObject.getCalculatedCarbon());
			} catch (Exception e) {
				System.out.println("DEBUG: Can't convert CalculatedCarbon from PEG.");
				usedCarbonPerUnitValue = 0.0;
			}
		}
		
		if ((usedCarbonPerUnitValue != null) && (usedCarbonPerUnitValue > 0.0)){
			// Calculate BIOVOLUME = ABUND * CEVOL.
			if (newAbundValue > 0.0) {
				newCarbonValue = newAbundValue * usedCarbonPerUnitValue / 1000000; // 1000: pg to ug.
			} else {
				newCarbonValue = paramAbundValue * usedCarbonPerUnitValue / 1000000; // 1000: pg to ug.
			}
//			newCarbonValue = Math.round(newCarbonValue * 10000.0) / 10000.0; // 4 decimals.
			if (paramCarbonPerLitre) {
				modifyVariable(usedVariable, "Carbon concentration", newCarbonValue, "ugC/l");
				
				// Check if the new calculated value is reasonably near the reported value.
				try {
					if ((newCarbonValue > (paramCarbonPerLitreValue * 2.0)) ||
						(newCarbonValue < (paramCarbonPerLitreValue * 0.5))) {
						importInfo.addConcatWarning(
								"PP calculations: Calculated value differ too much from reported value. Parameter: Carbon concentration."); 
						importInfo.addConcatWarning(
									"Calculated value differ too much from reported value. Parameter: Carbon concentration." + 
								  	" Calculated value: " + newCarbonValue + 								
						  			" Reported value: " + paramCarbonPerLitreValue +
						  			" Species/size: " + this.getTaxonName() + " / " + this.getSizeClass());								
					}
				} catch (Exception e) {
					System.out.println("DEBUG: Failed to check diff. for calc. value. Parameter: Carbon concentration." + 
								  	" Calculated value: " + newCarbonValue + 								
						  			" Reported value: " + paramCarbonPerLitreValue);								
				}

			} else {
				addVariable(usedVariable, "Carbon concentration", newCarbonValue, "ugC/l");
			}
		}
	}
	
	public void getPegObject() {
		
//		if (taxonName.equals("Snowella") && sizeClass.equals("2")) {
//			System.out.println("DEBUG: Snowella.");
//		}
		bvolObject = null;
		bvolObject = BvolManager.instance().getBvolObject(taxonName, sizeClass);
		
		if (bvolObject == null) {
			importInfo.addConcatWarning("PP calculations: Size group not in BVOL." + 
					" Species: " + taxonName + ", size: " + sizeClass);

//			System.out.println("DEBUG: PP calculations: Size group not in BVOL." +
//			" Species/size: " + this.getTaxonName() + " / " + this.getSizeClass());	
		}
	}
	
	public String getTaxonName() {
		return taxonName;
	}
	public void setTaxonName(String taxonName) {
		this.taxonName = taxonName;
	}
	public String getSizeClass() {
		return sizeClass;
	}
	public void setSizeClass(String sizeClass) {
		this.sizeClass = sizeClass;
	}
	
	public boolean isParamAbund() {
		return paramAbund;
	}
	public void setParamAbund(boolean paramAbund) {
		this.paramAbund = paramAbund;
	}
	public boolean isParamCountnr() {
		return paramCountnr;
	}
	public void setParamCountnr(boolean paramCountnr) {
		this.paramCountnr = paramCountnr;
	}
	public boolean isParamConc1_5() {
		return paramConc1_5;
	}
	public void setParamConc1_5(boolean paramConc1_5) {
		this.paramConc1_5 = paramConc1_5;
	}
	public boolean isColCoeff() {
		return colCoeff;
	}
	public void setColCoeff(boolean colCoeff) {
		this.colCoeff = colCoeff;
	}
	public boolean isColReportedCevol() {
		return colReportedCevol;
	}
	public void setColReportedCevol(boolean colReportedCevol) {
		this.colReportedCevol = colReportedCevol;
	}
	public boolean isParamBiovol() {
		return paramBiovol;
	}
	public void setParamBiovol(boolean paramBiovol) {
		this.paramBiovol = paramBiovol;
	}
	public boolean isParamCarbonPerUnit() {
		return paramCarbonPerUnit;
	}
	public void setParamCarbonPerUnit(boolean paramCarbonPerUnit) {
		this.paramCarbonPerUnit = paramCarbonPerUnit;
	}
	public boolean isParamCarbonPerLitre() {
		return paramCarbonPerLitre;
	}
	public void setParamCarbonPerLitre(boolean paramCarbonPerLitre) {
		this.paramCarbonPerLitre = paramCarbonPerLitre;
	}
}
