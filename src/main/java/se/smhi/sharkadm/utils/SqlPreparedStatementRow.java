/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.utils;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SqlPreparedStatementRow {
	
	private PreparedStatement statement;
	private int rowCounter;
	private boolean containsData;

	public SqlPreparedStatementRow(PreparedStatement statement) {
		this.statement = statement;
		rowCounter = 1;
		containsData = false;
	}

	public boolean containsData() {
		return containsData;
	}
	
	public void addOid(int field) throws SQLException {
		statement.setInt(rowCounter, field);
		rowCounter++;
	}

	public void addInt(int field) throws SQLException {
		containsData = true; // TODO: Can't detect null. Zero is a valid value.
		statement.setInt(rowCounter, field);
		rowCounter++;
	}

	public void addIntFromString(String field) throws SQLException {
		if (!field.equals("")) {
			containsData = true;
			if (field.indexOf(".") != -1) {
				field = field.substring(0, field.indexOf("."));
			}
			if (field.indexOf(",") != -1) {
				field = field.substring(0, field.indexOf(","));
			}
			statement.setInt(rowCounter, Integer.parseInt(field));
		} else {
//			statement.setInt(rowCounter, 0);
			statement.setNull(rowCounter, java.sql.Types.INTEGER);
		}
		rowCounter++;
	}

	public void addString(String field) throws SQLException {
		if (!field.equals("")) {
			containsData = true;
		}
		// Replace if string contains LF.
		if (field.contains("\n")) {
			field = field.replace("\n", " ");
		}
		
		statement.setString(rowCounter, field);
		rowCounter++;
	}

	public void addCleanedString(String field) throws SQLException {
		if (!field.equals("")) {
			containsData = true;
		}
		// Replace if string contains LF or '"'.
		field = field.replace("\"", "");
		field = field.replace("\n", " ");		
		
		statement.setString(rowCounter, field);
		rowCounter++;
	}

	public void addStringReplaceIfEmpty(String field) throws SQLException {
		if (!field.equals("")) {
			containsData = true;
		} else {
			field = "-"; // Replace. Use - to make it possible to search.
		}
		// Check if string contains LF. Replace.
		if (field.contains("\n")) {
			field = field.replace("\n", " ");
		}
		
		statement.setString(rowCounter, field);
		rowCounter++;
	}
	public void addFloat(Double field) throws SQLException {
		if (field != null) {
			containsData = true;
		}
		if (field == null) {
			statement.setNull(rowCounter, java.sql.Types.FLOAT);
		} else {
			statement.setDouble(rowCounter, field );
		}		
		rowCounter++;
	}

	public void addFloatFromString(String field) throws SQLException {
		if (!field.equals("")) {
			containsData = true;
		}
		if (field.equals("")) {
			statement.setNull(rowCounter, java.sql.Types.FLOAT);
		} else {
			try {
				Float floatValueFloat = Float.parseFloat(field
						.replace(",", ".").replace(" ", ""));
				statement.setFloat(rowCounter, floatValueFloat);
			} catch (Exception e) {
				System.out.println("ERROR when converting String to Float: " + field);
				statement.setNull(rowCounter, java.sql.Types.FLOAT);
			}
		}		
		rowCounter++;
	}

	public void addPosition(GeoPosition field) throws SQLException {
		statement.setString(rowCounter, field.getDbPoint());
		rowCounter++;
	}

}
