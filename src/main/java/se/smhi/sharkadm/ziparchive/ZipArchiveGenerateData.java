/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.ziparchive;

import java.util.ArrayList;
import java.util.List;

import se.smhi.sharkadm.datasets.columns.ColumnInfoManager;
import se.smhi.sharkadm.datasets.columns.ColumnViewManager;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.ModelVisitor;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.translate.TranslateCodesManager_NEW;

public class ZipArchiveGenerateData extends ModelVisitor {

	private List<String> headerStringList = new ArrayList<String>();
	private List<String> variableRowStringList = new ArrayList<String>();
    // Public result:
	List<String> dataFileContent = new ArrayList<String>();
	
	ColumnViewManager columnViewManager;
	ColumnInfoManager columnInfoManager;
	TranslateCodesManager_NEW translateCodesManager = TranslateCodesManager_NEW.instance();
	
	@Override
	public void visitDataset(Dataset dataset) {
		
		// Clear all.
		headerStringList.clear();
		dataFileContent.clear();
		// Clear content for "translate_codes.txt".
		translateCodesManager.clearUsedInDatasetList();
		
		// Load header.
		String column_view_name = "";
		
//		sharkdata_bacterioplankton
//		sharkdata_chlorophyll
//		sharkdata_epibenthos
//		sharkdata_epibenthos_dropvideo
//		sharkdata_greyseal
//		sharkdata_harbourporpoise
//		sharkdata_harbourseal
//		sharkdata_physicalchemical
//		sharkdata_physicalchemical_columns
//		sharkdata_phytoplankton
//		sharkdata_picoplankton
//		sharkdata_planktonbarcoding
//		sharkdata_jellyfish
//		sharkdata_primaryproduction
//		sharkdata_profile
//		sharkdata_ringedseal
//		sharkdata_sealpathology
//		sharkdata_sedimentation
//		sharkdata_zoobenthos
//		sharkdata_zooplankton
//		sharkweb_all
//		sharkweb_overview

		String datatypeCode = dataset.getDeliveryDatatypeCode();
		String datatypeLowerCase = datatypeCode.toLowerCase().trim(); // Use lower case when comparing.
		datatypeLowerCase = datatypeLowerCase.replace(" ", ""); // Use lower case when comparing.
		
		if (datatypeLowerCase.equals("bacterioplankton")) { 
			column_view_name = "sharkdata_bacterioplankton"; 
		} else if (datatypeLowerCase.equals("chlorophyll")) { 
			column_view_name = "sharkdata_chlorophyll"; 
		} else if (datatypeLowerCase.equals("epibenthos")) { 
			column_view_name = "sharkdata_epibenthos"; 
		} else if (datatypeLowerCase.equals("epibenthosdropvideo")) { 
			column_view_name = "sharkdata_epibenthos_dropvideo"; 
		} else if (datatypeLowerCase.equals("greyseal")) { 
			column_view_name = "sharkdata_greyseal"; 
		} else if (datatypeLowerCase.equals("harbourporpoise")) { 
			column_view_name = "sharkdata_harbourporpoise"; 
		} else if (datatypeLowerCase.equals("harbourseal")) { 
			column_view_name = "sharkdata_harbourseal"; 
		} else if (datatypeLowerCase.equals("physicalandchemical")) { 
			column_view_name = "sharkdata_physicalchemical";
		} else if (datatypeLowerCase.equals("phytoplankton")) { 
			column_view_name = "sharkdata_phytoplankton"; 
		} else if (datatypeLowerCase.equals("picoplankton")) { 
			column_view_name = "sharkdata_picoplankton"; 
		} else if (datatypeLowerCase.equals("planktonbarcoding")) { 
			column_view_name = "sharkdata_planktonbarcoding";
		} else if (datatypeLowerCase.equals("jellyfish")) { 
			column_view_name = "sharkdata_jellyfish";
		} else if (datatypeLowerCase.equals("primaryproduction")) { 
			column_view_name = "sharkdata_primaryproduction";
		} else if (datatypeLowerCase.equals("profile")) { 
			column_view_name = "sharkdata_profile";
		} else if (datatypeLowerCase.equals("ringedseal")) { 
			column_view_name = "sharkdata_ringedseal"; 
		} else if (datatypeLowerCase.equals("sealpathology")) { 
			column_view_name = "sharkdata_sealpathology"; 
		} else if (datatypeLowerCase.equals("sedimentation")) { 
			column_view_name = "sharkdata_sedimentation";
		} else if (datatypeLowerCase.equals("zoobenthos")) { 
			column_view_name = "sharkdata_zoobenthos"; 
		} else if (datatypeLowerCase.equals("zooplankton")) { 
			column_view_name = "sharkdata_zooplankton"; 
		} else { 
			column_view_name = "sharkweb_overview";
		}
		
		columnViewManager = new ColumnViewManager(column_view_name);
		for (String item : columnViewManager.getColumnList()) {
			// Don't show columns with data from the db-tables 'taxon' and 'location'.
			if ((!item.startsWith("taxon_")) && (!item.startsWith("location_"))) {
				// Remove last column. Same info as zip file name. Problems with CRC check.
				if (!item.startsWith("dataset_file_name")) {
					// Use English for orderer, project and analytical_laboratory in SHARKdata.
					if (item.endsWith("_name_sv")) {
						item = item.replace("_name_sv", "_name_en");
					}
					headerStringList.add(item);
				}
			}
		}
		
		// If column_view_name not available use "sharkweb_overview".
		if (headerStringList.size() == 0) {
			column_view_name = "sharkweb_overview";
			columnViewManager = new ColumnViewManager(column_view_name);
			// Don't show columns with data from the db-tables 'taxon' and 'location'.
			for (String item_2 : columnViewManager.getColumnList()) {
				if ((!item_2.startsWith("taxon_")) && (!item_2.startsWith("location_"))) {
					// Remove last column. Same info as zip file name. Problems with CRC check.
					if (!item_2.startsWith("dataset_file_name")) {
						// Use English for orderer, project and analytical_laboratory in SHARKdata.
						if (item_2.endsWith("_name_sv")) {
							item_2 = item_2.replace("_name_sv", "_name_en");
						}
						headerStringList.add(item_2);
					}
				}
			}
		}
		
		// Load column info.
		columnInfoManager = new ColumnInfoManager();

		dataFileContent.add(this.getHeaderAsString());

//		// Add dataset data to row.
//		for (String item : headerStringList) {
//			if (columnInfoManager.getNodeLevel(item).equals("dataset")) {
//				datasetRowStringList.add(dataset.getField(columnInfoManager.getInternalFieldName(item)));
//			}
//		}
		
		
		
		

		for (Visit visit : dataset.getVisits()) {
			visit.Accept(this);
		}
	}

	@Override
	public void visitVisit(Visit visit) {

//		visitRowStringList.clear();
//		sampleRowStringList.clear();
//		variableRowStringList.clear();

//		// Add visit data to row. 
//		for (String item : headerStringList) {
//			if (columnInfoManager.getNodeLevel(item).equals("visit")) {
//				visitRowStringList.add(visit.getField(columnInfoManager.getInternalFieldName(item)));
//			}
//		}		

		for (Sample sample : visit.getSamples()) {
			sample.Accept(this);
		}
	}

	@Override
	public void visitSample(Sample sample) {

//		sampleRowStringList.clear();
//		variableRowStringList.clear();

//		// Add sample data to row. 
//		for (String item : headerStringList) {
//			if (columnInfoManager.getNodeLevel(item).equals("sample")) {
//				sampleRowStringList.add(sample.getField(columnInfoManager.getInternalFieldName(item)));
//			}
//		}		

		for (Variable variable : sample.getVariables()) {
			variable.Accept(this);
		}
	}

	@Override
	public void visitVariable(Variable variable) {

		// If the variable doesn't contain param/value/unit, it should not 
		// result in a row in shark_data.txt.
		// This reduction is done late which makes it possible to search for
		// errors during import.
		if ((variable.getParameter().equals(""))
				&& (variable.getValue().equals(""))
				&& (variable.getUnit().equals(""))) {
			return;
		}
//		variableRowStringList.clear();
		
//		// Add variable data to row. 
//		for (String item : headerStringList) {
//			if (columnInfoManager.getNodeLevel(item).equals("variable")) {
//				variableRowStringList.add(variable.getField(columnInfoManager.getInternalFieldName(item)));
//			}
//		}
		
		variableRowStringList.clear();

		// Add variable data to row. 
		for (String item : headerStringList) {
//			// Use English for orderer, project and analytical_laboratory in SHARKdata.
//			if (item.endsWith("_name_sv")) {
//				item = item.replace("_name_sv", "_name_en");
//			}
			String value = "";
			if (columnInfoManager.getNodeLevel(item).equals("dataset")) {
				if (columnInfoManager.getFieldFormat(item).equals("float")) {
					value = variable.getParent().getParent().getParent().getFieldDecimalPoint(columnInfoManager.getInternalFieldName(item));
				} else {
//					value = variable.getParent().getParent().getParent().getFieldAsCleanString(columnInfoManager.getInternalFieldName(item));
					value = cleanupCodedValues(variable.getParent().getParent().getParent().getFieldAsCleanString(columnInfoManager.getInternalFieldName(item)), item);
				}
			}
			else if (columnInfoManager.getNodeLevel(item).equals("visit")) {
				if (columnInfoManager.getFieldFormat(item).equals("float")) {
					value = variable.getParent().getParent().getFieldDecimalPoint(columnInfoManager.getInternalFieldName(item));
				} else {
//					value = variable.getParent().getParent().getFieldAsCleanString(columnInfoManager.getInternalFieldName(item));
					value = cleanupCodedValues(variable.getParent().getParent().getFieldAsCleanString(columnInfoManager.getInternalFieldName(item)), item);
				}
			}
			else if (columnInfoManager.getNodeLevel(item).equals("sample")) {
				if (columnInfoManager.getFieldFormat(item).equals("float")) {
					value = variable.getParent().getFieldDecimalPoint(columnInfoManager.getInternalFieldName(item));
				} else {
//					value = variable.getParent().getFieldAsCleanString(columnInfoManager.getInternalFieldName(item));
					value = cleanupCodedValues(variable.getParent().getFieldAsCleanString(columnInfoManager.getInternalFieldName(item)), item);
				}
			}
			else if (columnInfoManager.getNodeLevel(item).equals("variable")) {
				if (columnInfoManager.getFieldFormat(item).equals("float")) {
					value = variable.getFieldDecimalPoint(columnInfoManager.getInternalFieldName(item));
				} else {
//					value = variable.getFieldAsCleanString(columnInfoManager.getInternalFieldName(item));
					value = cleanupCodedValues(variable.getFieldAsCleanString(columnInfoManager.getInternalFieldName(item)), item);
				}
			}
			
			// Add value to row.
			variableRowStringList.add(value);
			
			// Check if translations are used, then add to "translate_codes.txt".
			translateCodesManager.checkIfCodeIsUsed(item, value);
						
		}
		dataFileContent.add(this.getRowAsString());
	}
	
	public String cleanupCodedValues(String value, String headerItem) {
		String result = value;
		if (headerItem.equals("sample_project_name_en")) {
			result = value.replace("<->", ", ");
		}
		else if (headerItem.equals("sample_orderer_name_en")) {
			result = value.replace("<->", ", ");
		}
		return result;
	}
	
	public String getHeaderAsString() {
		String result = "";
		String separator = "";
		for (String item : headerStringList) {
			result += separator + item;
			separator = "\t"; // Tab.
		}
		return result;
	}

	public String getRowAsString() {
		String result = "";
		String separator = "";
//		for (String item : datasetRowStringList) {
//			result += separator + item;
//			separator = "\t"; // Tab.
//		}
//		for (String item : visitRowStringList) {
//			result += separator + item;
//			separator = "\t"; // Tab.
//		}
//		for (String item : sampleRowStringList) {
//			result += separator + item;
//			separator = "\t"; // Tab.
//		}
		for (String item : variableRowStringList) {
			result += separator + item;
			separator = "\t"; // Tab.
		}
		return result;
	}
}
