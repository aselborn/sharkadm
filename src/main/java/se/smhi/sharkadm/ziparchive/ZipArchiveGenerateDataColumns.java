/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.ziparchive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import se.smhi.sharkadm.datasets.columns.ColumnInfoManager;
import se.smhi.sharkadm.datasets.columns.ColumnViewManager;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.ModelVisitor;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;

public class ZipArchiveGenerateDataColumns extends ModelVisitor {

	private List<String> headerStringList = new ArrayList<String>();
	private List<String> rowStringList = new ArrayList<String>();
    // Public result:
	List<String> dataFileContent = new ArrayList<String>();

	Map<String, ParameterValueUnitQflag> paramValueList = new HashMap<String, ParameterValueUnitQflag>();
	Map<String, List<Variable>> variableMap = new HashMap<String, List<Variable>>();

	ColumnViewManager columnViewManager;
	ColumnInfoManager columnInfoManager;
	@Override
	public void visitDataset(Dataset dataset) {
		
		// Clear all.
		headerStringList.clear();
		dataFileContent.clear();

		// Load header.
		String column_view_name = "";
		
//		if (dataset.getDeliveryDatatypeCode().equals("Physical and Chemical")) { 
//			column_view_name = "sharkdata_physicalchemical_columns";
//			
//		} else if (dataset.getDeliveryDatatypeCode().equals("Zoobenthos")) { 
//			column_view_name = "sharkdata_zoobenthos_columns"; 
//		} else if (dataset.getDeliveryDatatypeCode().equals("Zooplankton")) { 
//			column_view_name = "sharkdata_zooplankton_columns"; 
//		} else if (dataset.getDeliveryDatatypeCode().equals("Epibenthos")) { 
//			column_view_name = "sharkdata_epibenthos_columns"; 
//		} else if (dataset.getDeliveryDatatypeCode().equals("Phytoplankton")) { 
//		column_view_name = "sharkdata_phytoplankton_columns"; 
//
//		} else if (dataset.getDeliveryDatatypeCode().equals("Picoplankton")) { 
//		column_view_name = "sharkdata_picoplankton_columns"; 
//
//		} else if (dataset.getDeliveryDatatypeCode().equals("GreySeal")) { 
//			column_view_name = "sharkdata_greyseal_columns"; 
//		} else if (dataset.getDeliveryDatatypeCode().equals("HarborSeal")) { 
//			column_view_name = "sharkdata_harborseal_columns"; 
//		} else if (dataset.getDeliveryDatatypeCode().equals("RingedSeal")) { 
//			column_view_name = "sharkdata_ringedseal_columns"; 
//		} else if (dataset.getDeliveryDatatypeCode().equals("SealPathology")) { 
//			column_view_name = "sharkdata_sealpathology_columns"; 
//	
//		} else if (dataset.getDeliveryDatatypeCode().equals("Bacterioplankton")) { 
//			column_view_name = "sharkdata_bacterioplankton_columns";
//
//		} else if (dataset.getDeliveryDatatypeCode().equals("Chlorophyll")) { 
//			column_view_name = "sharkdata_chlorophyll_columns"; 
//		
//		} else if (dataset.getDeliveryDatatypeCode().equals("PrimaryProduction")) { 
//			column_view_name = "sharkdata_primaryproduction_columns";
//			
//		} else if (dataset.getDeliveryDatatypeCode().equals("Sedimentation")) { 
//			column_view_name = "sharkdata_sedimentation_columns";
//		}

		
		if (column_view_name.equals("")) {			
			return;
		} else {
			columnViewManager = new ColumnViewManager(column_view_name);
			// Exit if column not found.
			if (columnViewManager.getColumnList().size() == 0) {
				return;
			}
		}
			
		for (String param : columnViewManager.getColumnList()) {
			if ((param.startsWith("PARAM<+>")) || (param.startsWith("QFLAG<+>"))) {
//				String[] parts = param.split(":");
				String[] parts = param.split(Pattern.quote("<+>"));
				ParameterValueUnitQflag paramObject = new ParameterValueUnitQflag();
				if (parts.length > 1) {
					paramObject.searchForParameter = parts[1].trim();
				}
				if (parts.length > 2) {
					paramObject.searchForUnit = parts[2].trim();
				}
				paramObject.column_key = param;
				paramValueList.put(param, paramObject);
			}
		}

		headerStringList = columnViewManager.getColumnList();
		// Load column info.
		columnInfoManager = new ColumnInfoManager();

		dataFileContent.add(this.getHeaderAsString());

		for (Visit visit : dataset.getVisits()) {
			visit.Accept(this);
		}
	}

	@Override
	public void visitVisit(Visit visit) {
		
		for (Sample sample : visit.getSamples()) {
			sample.Accept(this);
		}
	}

	@Override
	public void visitSample(Sample sample) {
		
		// Group variables in sample. Scientific name, size, parameter, unit etc. must be unique.
		// Result is stored in variableMap.
		variableMap.clear();
		for (Variable variable : sample.getVariables()) {
			variable.Accept(this);
		}
		
		// Iterate over the group.
		for (List<Variable> variableList: variableMap.values()) {
			// Clear values.
			for (ParameterValueUnitQflag object : paramValueList.values()) {
				object.variable = null;
			}
			// Add variable objects for each corresponding group.
			for (Variable variable : variableList) {
				// Store variable if matches.
				for (ParameterValueUnitQflag object : paramValueList.values()) {
					if (object.searchForUnit.equals("")) {
						if (variable.getParameter().equals(object.searchForParameter)) {
							object.variable = variable;
						}
					} else {
						
						String param1 = variable.getParameter();
						String param2 =object.searchForParameter;
						String unit1 = variable.getUnit();
						String unit2 = object.searchForUnit;
						
						System.out.println("DEBUG: Param: " + param1 + " Unit: " + unit1 +
										   "   Search: " + param2 + " Unit: " + unit2);
						
						if ((variable.getParameter().equals(object.searchForParameter)) && 
							(variable.getUnit().equals(object.searchForUnit)) ) {
							object.variable = variable;
						}
					}
				}				
			}
			
			// Add variable data to row. 
			rowStringList.clear();
			for (String item : headerStringList) {
				if (columnInfoManager.getNodeLevel(item).equals("dataset")) {
					if (columnInfoManager.getFieldFormat(item).equals("float")) {
						rowStringList.add(sample.getParent().getParent().getFieldDecimalPoint(columnInfoManager.getInternalFieldName(item)));
					} else {
						rowStringList.add(sample.getParent().getParent().getFieldAsCleanString(columnInfoManager.getInternalFieldName(item)));
					}
				}
				else if (columnInfoManager.getNodeLevel(item).equals("visit")) {
					if (columnInfoManager.getFieldFormat(item).equals("float")) {
						rowStringList.add(sample.getParent().getFieldDecimalPoint(columnInfoManager.getInternalFieldName(item)));
					} else {
						rowStringList.add(sample.getParent().getFieldAsCleanString(columnInfoManager.getInternalFieldName(item)));
					}
				}
				else if (columnInfoManager.getNodeLevel(item).equals("sample")) {
					if (columnInfoManager.getFieldFormat(item).equals("float")) {
						rowStringList.add(sample.getFieldDecimalPoint(columnInfoManager.getInternalFieldName(item)));
					} else {
						rowStringList.add(sample.getFieldAsCleanString(columnInfoManager.getInternalFieldName(item)));
					}
				}
				else if (columnInfoManager.getNodeLevel(item).equals("variable")) {
					if (variableList.size() > 0) {
						if (columnInfoManager.getFieldFormat(item).equals("float")) {
							rowStringList.add(variableList.get(0).getFieldDecimalPoint(columnInfoManager.getInternalFieldName(item)));
						} else {
							rowStringList.add(variableList.get(0).getFieldAsCleanString(columnInfoManager.getInternalFieldName(item)));
						}
					} else {
						rowStringList.add("");
					}
				}
				else if (item.startsWith("QFLAG<+>")) {
					// Added in the following else-if.
				}
				else if (paramValueList.containsKey(item)) {
					if (paramValueList.get(item).variable != null) {
						rowStringList.add(paramValueList.get(item).variable.getValueAsDecimalPoint());
						rowStringList.add(paramValueList.get(item).variable.getField("variable.quality_flag"));
					} else {
						rowStringList.add("");
						rowStringList.add("");						
					}
				}
	
				else {
					rowStringList.add("");
				}
			}				
			
			dataFileContent.add(this.getRowAsString());
		}
		
	}

	@Override
	public void visitVariable(Variable variable) {

		// If the variable doesn't contain param/value/unit, it should not 
		// result in a row in sharkdata_*_columns.txt.
		if ((variable.getParameter().equals("")) && 
			(variable.getValue().equals("")) && 
			(variable.getUnit().equals(""))) {
			return;
		}
		
		// Unique sets of scientific names, sizes, etc., will result in multiple rows in a sample.
		String speciesKey = variable.getField("variable.scientific_name") + ":" +
							variable.getField("variable.size_class") + ":" +
							variable.getField("variable.size_min_um") + ":" +
							variable.getField("variable.size_max_um") + ":" +
							variable.getField("variable.trophic_type_code") + ":" +
							variable.getField("variable.sex_code") + ":" +
							variable.getField("variable.dev_stage_code");
		
		if ( ! variableMap.containsKey(speciesKey)) {
			variableMap.put(speciesKey, new ArrayList<Variable>());
		}
		variableMap.get(speciesKey).add(variable);		
	}
	
	public String translateCodedValues(String value, String headerItem) {
		
		return value;
		
	}
	
	public String getHeaderAsString() {
		String result = "";
		String separator = "";
		for (String item : headerStringList) {
			
			if ((item.contains("PARAM<+>")) || (item.contains("QFLAG<+>"))) {
				// This is for sharkdata_*_columns. Example: "PARAM<+>Abundance<+>ind/m2".
//				String[] parts = item.split(":");
				String[] parts = item.split(Pattern.quote("<+>"));
				if (parts.length > 1) {
					result += separator + parts[1].trim();
				} else {
					result += separator + item;
				}
			} else {
				result += separator + item;
			}
			separator = "\t"; // Tab.
		}
		return result;
	}

	public String getRowAsString() {
		String result = "";
		String separator = "";
		for (String item : rowStringList) {
			result += separator + item;
			separator = "\t"; // Tab.
		}
		return result;
	}
	
	// Local class.
	class ParameterValueUnitQflag {
    	String column_key = "";
    	String searchForParameter = "";
    	String searchForUnit = "";
    	Variable variable = null;
    }
 
}
