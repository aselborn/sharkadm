/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.columns;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.ModelElement;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.utils.ParseFileUtil;

/**
 * Loads data for SHARKadm internal use from the config file "column_info.txt".
 * 
 * The keyValueStringList is for temporal use. Load all data on a specific 
 * node level (addAllFieldsOnNodeLevel(ModelElement node)) and get the 
 * result (getKeyValueStringListAsString()). 
 */
public class ColumnInfoManager {
	
	List<ColumnInfoObject> columnInfoList = new ArrayList<ColumnInfoObject>();
	Map<String, ColumnInfoObject> columnInfoMap = new HashMap<String, ColumnInfoObject>();
	Map<String, ColumnInfoObject> internalKeyInfoMap = new HashMap<String, ColumnInfoObject>();
	// 
	List<String> keyValueStringList = new ArrayList<String>();

	public ColumnInfoManager() {
		super();
		importColumnsInfoFile();
	}
	
	public ColumnInfoObject getColumnInfoObjectFromKey(String key) {
		if (columnInfoMap.containsKey(key)) {
			return columnInfoMap.get(key);
		}
		return new ColumnInfoObject();
	}
	
	public ColumnInfoObject getColumnInfoObjectFromInternalKey(String key) {
		if (internalKeyInfoMap.containsKey(key)) {
			return internalKeyInfoMap.get(key);
		}
		return new ColumnInfoObject();
	}
	
	public String getNodeLevel(String key) {
		if (columnInfoMap.containsKey(key)) {
			return columnInfoMap.get(key).getNodeLevel();
		} else {
//			System.out.println("ERROR in column-lists. 'column_info.txt' does not contain the key: " + key);			
		}
		return "";
	}
	
	public String getFieldFormat(String key) {
		return columnInfoMap.get(key).getFieldFormat();
	}
	
	public String getInternalFieldName(String key) {
		return columnInfoMap.get(key).getInternalFieldName();
	}
	
	public void clearKeyValueStringList() {
		keyValueStringList.clear();
	}
	
	public void addAllFieldsOnNodeLevel(ModelElement node, String language) {
		String nodeLevel = "";
		if (node instanceof Dataset) {
			nodeLevel = "dataset";
		}
		else if (node instanceof Visit) {
			nodeLevel = "visit";
		}
		else if (node instanceof Sample) {
			nodeLevel = "sample";
		}
		else if (node instanceof Variable) {
			nodeLevel = "variable";
		}
		for (ColumnInfoObject object : columnInfoList) {
			if (object.getNodeLevel().equals(nodeLevel)) {
				if (node.containsField(object.getInternalFieldName())) {
					
					if ((object.getKey().equals("value")) &&
						(node.getField("variable.quality_flag").equals("B"))) {
						// If QFLAG=B the value should be Blank/NaN.
						this.addToKeyValueStringList(object.getKey(), "");
					} else {
						this.addToKeyValueStringList(object.getKey(), node.getField(object.getInternalFieldName()));										
					}
					
				}
			}
		}
	}
	
	public void addToKeyValueStringList(String key, String value) {
		// Don't add if empty.
		if (value.equals("")) {
			return;
		}
		// Replace if value contains LF or '"'.
		value = value.replace("\"", "");
		value = value.replace("\n", " ");		

		keyValueStringList.add(key + ":" + value);
	}
	
	public String getKeyValueStringListAsString() {
		String result = "";
		String separator = "";
		for (String item : keyValueStringList) {
			result += separator + item;
			separator = "\t"; // Tab.
		}
		return result;
	}
	
	public void importColumnsInfoFile() {
		List<String[]> fileContent;
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = ParseFileUtil.GetSharkConfigFile("column_info.txt");
			fileContent = ParseFileUtil.parseDataFile(bufferedReader, false);
			if (fileContent != null) {					
				importColumnsInfo(fileContent);
			}
		} catch (Exception e) {
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Column info manager");
			messageBox.setMessage("Failed to import 'column_info.txt'.");
			messageBox.open();
		}
	}

	private void importColumnsInfo(List<String[]> fileContent) {
		String[] header = null;
		for (String[] row : fileContent) {
			if (header == null) { 
				// The first line contains the header.
				header = row;
			} else {
				if (row.length < 4) {
					continue;
				}
				String key = row[0].trim();
				String nodeLevel = row[1].trim();
				String fieldFormat = row[2].trim();
				String modelFieldName = row[3].trim();
				//
				ColumnInfoObject object = new ColumnInfoObject();
				object.setNodeLevel(nodeLevel);
				object.setKey(key);
				object.setFieldFormat(fieldFormat);
				object.setInternalFieldName(modelFieldName);
				//
				columnInfoList.add(object);
				//
				columnInfoMap.put(key, object);
				internalKeyInfoMap.put(modelFieldName, object);
			}
		}
	}

}
