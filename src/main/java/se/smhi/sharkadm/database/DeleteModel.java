/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.database;

import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import se.smhi.sharkadm.facades.DatabaseFacade;

public class DeleteModel {

	GetChildren children = GetChildren.instance(); 
	Statement statement = null;
	
	private static DeleteModel instance = new DeleteModel(); // Singleton.

	public static DeleteModel instance() { // Singleton.
		return instance;
	}

	private DeleteModel() { // Singleton.

	}

	public void deleteAll() {
		Statement stmt = null;
		try {
			stmt = DbConnect.instance().getConnection().createStatement();
			
			stmt.execute(
					" begin WORK; " +
					" DELETE FROM variable; " +
					" commit WORK; " +
					" begin WORK; " +
					" DELETE FROM sample; " +
					" commit WORK; " +
					" begin WORK; " +
					" DELETE FROM visit; " +
					" commit WORK; " +
					" begin WORK; " +
					" DELETE FROM dataset; " +
					" commit WORK; " +
					" begin WORK; " +
					" DELETE FROM visit_location; " +
					" commit WORK; " +
					" begin WORK; " +
					" DELETE FROM shark_settings; " +
					" commit WORK; " +
					" begin WORK; " +
					" DELETE FROM taxon; " +
					" commit WORK; "
						);

//			stmt.execute("delete from variable; ");
//			stmt.execute("commit; ");
//			stmt.execute("delete from sample; ");
//			stmt.execute("commit; ");
//			stmt.execute("delete from visit; ");
//			stmt.execute("commit; ");
//			stmt.execute("delete from dataset; ");
//			stmt.execute("commit; ");
//			stmt.execute("delete from visit_location; ");
//			stmt.execute("commit; ");
//			stmt.execute("delete from taxon; ");
//			stmt.execute("commit; ");
	
			stmt.close();
		} catch (SQLException e) {
			HandleError(e);
		}
	}

	public void deleteDataset(int oid) {
		// Create sql statement to be used by all methods.
		try {
			statement = DbConnect.instance().getConnection().createStatement();
		} catch (SQLException e) {
			HandleError(e);
		}
		// Delete the dataset.
		try {
			statement.execute("delete from dataset where oid = " + oid + "; ");
			statement.execute("commit; ");

			DatabaseFacade.instance().dataChanged();

		} catch (SQLException e) {
			HandleError(e);
		}
	}
	
	public void clearDbCache() {
		try {
			statement = DbConnect.instance().getConnection().createStatement();
		} catch (SQLException e) {
			HandleError(e);
		}
		// Clear cached result for .
		try {
			statement.execute("delete from result_cache; ");
			statement.execute("commit; ");
		} catch (SQLException e) {
			HandleError(e);
		}
	}

	private void HandleError(SQLException e) {
		// Note: It is not recommended to put message dialog here. Should be in the UI layer.
		MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
		messageBox.setText("SQL error in DeleteModel");
		messageBox.setMessage("Error: " + e.getMessage());
		messageBox.open();
		e.printStackTrace();
		System.exit(-1);
	}
}
