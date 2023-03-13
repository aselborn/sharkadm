/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.columns;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import se.smhi.sharkadm.settings.SettingsManager;
import se.smhi.sharkadm.utils.ParseFileUtil;

/**
 * Loads one column from the config file "column_views.txt".
 */
public class ColumnViewManager {
	
	List<String> columnList = new ArrayList<String>();
	
	public ColumnViewManager(String column_name) {
		super();
		importColumnViewsFile(column_name);
	}

	public void clear() {
		columnList.clear();
	}

	public List<String> getColumnList() {
		return columnList;
	}
	
	public void importColumnViewsFile(String column_name) {
		List<String[]> fileContent;
		BufferedReader bufferedReader = null;
		
		SettingsManager.instance().clearSettingsList();
		
		try {
			bufferedReader = ParseFileUtil.GetSharkConfigFile("column_views.txt");
			
			fileContent = ParseFileUtil.parseDataFile(bufferedReader, false);
			if (fileContent != null) {					
				importColumnViews(fileContent, column_name);
			}
			
		} catch (Exception e) {
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Column view manager");
			messageBox.setMessage("Failed to import 'column_views.txt'.");
			messageBox.open();
		}
	}

	private void importColumnViews(List<String[]> fileContent, String column_name) {
		String[] header = null;
		int selectedColumnIndex = -1;
		columnList.clear();
		for (String[] row : fileContent) {
			if (header == null) { 
				// The first line contains the header.
				header = row;
				int index = 0;
				for (String item : row) {
					if (item.equals(column_name)) {
						selectedColumnIndex = index;
						break;
					}
					index += 1;
				}
			} else {
				if (selectedColumnIndex == -1) {
					return; // Column not found.
				}
				
				if (row.length > selectedColumnIndex) {
					if (row[selectedColumnIndex].trim().length() > 0) {
						columnList.add(row[selectedColumnIndex].trim());
					}
				}
			}
		}
	}
}
