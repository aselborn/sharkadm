/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class ReadDataset {
	
	private String sqlDataset = "select "
		+ "dataset.* "
		+ "from dataset "
//		+ "order by dataset_package_name desc;";
		+ "order by import_datetime desc;";

	private static ReadDataset instance = new ReadDataset(); // Singleton.

	public static ReadDataset instance() { // Singleton.
		return instance;
	}

	public List<Map<String, String>> readDatasetListTable() {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();

		if (DbConnect.instance().isConnected()) {
			try {
				Connection connection = DbConnect.instance().getConnection();
				PreparedStatement stmtDataset = connection.prepareStatement(sqlDataset);
	
				ResultSet rs = null;				
				rs = stmtDataset.executeQuery();
				
				while (rs.next()) {
					Map<String, String> item = new HashMap<String, String>();
					item.put("oid", rs.getString("oid"));
					item.put("dataset_package_name", rs.getString("dataset_package_name"));
					item.put("reported_by", rs.getString("reported_by"));
					item.put("import_file_name", rs.getString("import_file_name"));
					item.put("import_file_path", rs.getString("import_file_path"));
					item.put("import_format", rs.getString("import_format"));
					item.put("imported_by", rs.getString("imported_by"));
					item.put("import_datetime", rs.getString("import_datetime"));
					item.put("delivery_comment", rs.getString("delivery_comment"));

					item.put("import_status", rs.getString("import_status"));
					item.put("import_matrix_column", rs.getString("import_matrix_column"));
	
					list.add(item);
				}
				
				stmtDataset.close();
			
			} catch (SQLException e) {
				e.printStackTrace();
				MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
				messageBox.setText("Error in ReadDataset");
				messageBox.setMessage("Database error: " + e.getMessage());
				messageBox.open();
				System.exit(-1);
			}
		}
		return list;
	}

}
