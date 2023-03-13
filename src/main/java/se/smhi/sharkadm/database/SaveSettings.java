/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import se.smhi.sharkadm.utils.SqlPreparedStatementRow;

public class SaveSettings {

	private String sqlInsertSettings = "insert into shark_settings ("
		+ "settings_key, " 
		+ "settings_value " 
		+ ") values (?, ?);";

	private static SaveSettings instance = new SaveSettings(); // Singleton.

	public static SaveSettings instance() { // Singleton.
		return instance;
	}

	private SaveSettings() { // Singleton.

	}

	public int countSettings() {
		if (!DbConnect.instance().isConnected()) {
			return 0;
		}
		Statement stmt = null;
		ResultSet rs = null;
		int counter = 0;
		try {
			stmt = DbConnect.instance().getConnection().createStatement();
			rs = stmt.executeQuery("select count(*) from shark_settings; ");
			while (rs.next()) {
				counter = rs.getInt(1);
			}
			stmt.close();
			return counter;

		} catch (SQLException e) {
			HandleError(e);
			return 0;
		}
	}

	public void deleteSettings() {
		if (!DbConnect.instance().isConnected()) {
			return;
		}
		Statement stmt = null;
		try {			
			stmt = DbConnect.instance().getConnection().createStatement();
			stmt.execute("delete from shark_settings; ");
			stmt.close();
		} catch (SQLException e) {
			HandleError(e);
		}		
	}

	public int insertSettings(String settingsKey, String settingsValue) {

		try {
			PreparedStatement stmtInsertSettings = DbConnect.instance().getConnection().prepareStatement(sqlInsertSettings);
			SqlPreparedStatementRow row = new SqlPreparedStatementRow(stmtInsertSettings);
			row.addString(settingsKey); // settings_key
			row.addString(settingsValue); // settings_value

			stmtInsertSettings.executeUpdate();
			return 0;

		} catch (SQLException e) {
			HandleError(e);
			return 0;
		}
	}

	private void HandleError(SQLException e) {
		// Note: It is not recommended to put message dialog here. Should be in the UI layer.
		MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
		messageBox.setText("SQL error in SaveSettings");
		messageBox.setMessage("Error: " + e.getMessage());
		messageBox.open();
		e.printStackTrace();
		System.exit(-1);
	}
	
}
