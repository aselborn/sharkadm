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
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class GetChildren {

	private String sqlGetDatasetFileList = "select " + "dataset_package_name " 
			+ "from dataset order by dataset_package_name;";

	private String sqlGetDataset = "select " + "oid " + "from dataset " 
			+ "where dataset_package_name = ?;";

	private String sqlGetDatasetVersionFilter = "select " + "oid " + "from dataset " 
			+ "where dataset_package_name LIKE ?;";

	private String sqlGetChildrenDataset = "select " + "oid "
			+ "from visit " + "where dataset_oid = ?;";

	private String sqlGetChildrenSamplingEvent = "select " + "oid "
			+ "from sample " + "where visit_oid = ?;";

	private String sqlGetChildrenSample = "select " + "oid "
			+ "from variable " + "where sample_oid = ?;";

	private PreparedStatement stmtGetChildrenDataset;

	private PreparedStatement stmtGetDataset;
	private PreparedStatement stmtGetDatasetVersionFilter;

	private PreparedStatement stmtGetDatasetFileList;

	private PreparedStatement stmtGetChildrenVisit;

	private PreparedStatement stmtGetChildrenSample;

	private static GetChildren instance = new GetChildren(); // Singleton.

	public static GetChildren instance() { // Singleton.
		return instance;
	}

	private Connection lastUsedConnection = null;

	private GetChildren() { // Singleton.
		initiatePreparedStatements();
	}

	private void initiatePreparedStatements() {
		try {
			lastUsedConnection = DbConnect.instance().getConnection();
			stmtGetDatasetFileList = DbConnect.instance().getConnection().prepareStatement(sqlGetDatasetFileList);
			stmtGetDataset = DbConnect.instance().getConnection().prepareStatement(sqlGetDataset);
			stmtGetDatasetVersionFilter = DbConnect.instance().getConnection().prepareStatement(sqlGetDatasetVersionFilter);
			stmtGetChildrenDataset = DbConnect.instance().getConnection().prepareStatement(sqlGetChildrenDataset);
			stmtGetChildrenVisit = DbConnect.instance().getConnection().prepareStatement(sqlGetChildrenSamplingEvent);
			stmtGetChildrenSample = DbConnect.instance().getConnection().prepareStatement(sqlGetChildrenSample);

		} catch (SQLException e) {
			HandleError(e);
		}
	}
	
	public List<Integer> getDatasetList() {
		if ((stmtGetDatasetFileList == null) || (lastUsedConnection != DbConnect.instance().getConnection())) {
			initiatePreparedStatements();
		}
		ResultSet rs = null;
		try {
			List<Integer> datasetIds = new ArrayList<Integer>();
			Integer datasetId;

			rs = stmtGetDatasetFileList.executeQuery();
			while (rs.next()) {
				datasetId = rs.getInt("id");
				datasetIds.add(datasetId);
			}
			return datasetIds;

		} catch (SQLException e) {
			HandleError(e);
			return null;
		}
	}

//	public int getDataset(String importedFileName) {
//		if ((stmtGetDataset == null) || (lastUsedConnection != DbConnect.instance().getConnection())) {
//			initiatePreparedStatements();
//		}
//		ResultSet rs = null;
//		try {
//			int datasetId = -1;
//
//			stmtGetDataset.setString(1, importedFileName);
//
//			rs = stmtGetDataset.executeQuery();
//			if (rs.next()) {
//				datasetId = rs.getInt("oid");
//			}
//			
//			return datasetId;
//
//		} catch (SQLException e) {
//			HandleError(e);
//			return 0;
//		}
//	}

	public int getDatasetUseVersionFilter(String importedFileName) {
		if ((stmtGetDataset == null) || (lastUsedConnection != DbConnect.instance().getConnection())) {
			initiatePreparedStatements();
		}
		
		int substringIndex = importedFileName.indexOf("version");
		if (substringIndex != -1) {
			importedFileName = importedFileName.substring(0, substringIndex) + "version%"; // % = wildcard in LIKE.
		}
		
		ResultSet rs = null;
		try {
			int datasetId = -1;

			stmtGetDatasetVersionFilter.setString(1, importedFileName);

			rs = stmtGetDatasetVersionFilter.executeQuery();
			if (rs.next()) {
				datasetId = rs.getInt("oid");
			}
			
			return datasetId;

		} catch (SQLException e) {
			HandleError(e);
			return 0;
		}
	}

	public List<Integer> getChildrenDataset(int id) {
		if ((stmtGetChildrenDataset == null) || (lastUsedConnection != DbConnect.instance().getConnection())) {
			initiatePreparedStatements();
		}
		ResultSet rs = null;
		try {
			List<Integer> children = new ArrayList<Integer>();
			Integer childId;

			stmtGetChildrenDataset.setInt(1, id);

			rs = stmtGetChildrenDataset.executeQuery();
			while (rs.next()) {
				childId = rs.getInt("oid");
				children.add(childId);
			}
			return children;

		} catch (SQLException e) {
			HandleError(e);
			return null;
		}
	}

	public List<Integer> getChildrenVisit(int id) {
		if ((stmtGetChildrenVisit == null) || (lastUsedConnection != DbConnect.instance().getConnection())) {
			initiatePreparedStatements();
		}
		ResultSet rs = null;
		try {
			List<Integer> children = new ArrayList<Integer>();
			Integer childId;

			stmtGetChildrenVisit.setInt(1, id);

			rs = stmtGetChildrenVisit.executeQuery();
			while (rs.next()) {
				childId = rs.getInt("oid");
				children.add(childId);
			}
			return children;

		} catch (SQLException e) {
			HandleError(e);
			return null;
		}
	}

	public List<Integer> getChildrenSample(int id) {
		if ((stmtGetChildrenSample == null) || (lastUsedConnection != DbConnect.instance().getConnection())) {
			initiatePreparedStatements();
		}
		ResultSet rs = null;
		try {
			List<Integer> children = new ArrayList<Integer>();
			Integer childId;

			stmtGetChildrenSample.setInt(1, id);

			rs = stmtGetChildrenSample.executeQuery();
			while (rs.next()) {
				childId = rs.getInt("oid");
				children.add(childId);
			}
			return children;

		} catch (SQLException e) {
			HandleError(e);
			return null;
		}
	}

	private void HandleError(SQLException e) {
		// Note: It is not recommended to put message dialog here. Should be in the UI layer.
		MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
		messageBox.setText("SQL error in GetChildren");
		messageBox.setMessage("Error: " + e.getMessage());
		messageBox.open();
		e.printStackTrace();
		System.exit(-1);
	}

}
