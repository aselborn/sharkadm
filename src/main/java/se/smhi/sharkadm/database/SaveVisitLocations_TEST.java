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
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import se.smhi.sharkadm.location.VisitLocation_TEST;
import se.smhi.sharkadm.utils.SqlPreparedStatementRow;

public class SaveVisitLocations_TEST {
	
	private String sqlInsertVisitLocation = "insert into visit_location ("
		+ "location_id, "		
		+ "location_nation, " 
		+ "location_county, " 
		+ "location_municipality, " 
		+ "location_water_district, " 
		+ "location_svar_sea_area_code, " 
		+ "location_svar_sea_area_name, " 
		+ "location_water_category, " 
		+ "location_type_area, " 
		+ "location_helcom_ospar_area, "
		+ "location_economic_zone, "
		+ "location_sea_basin, "
//		+ "location_protected_area_names, " 
		+ "keyvalue_location " 

		+ ") values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	private String sqlInsertProtectedArea = "insert into protected_area ("
		+ "visit_location_id, " 
		+ "protected_area_type " 

	    + ") values (?, ?);";

	private static SaveVisitLocations_TEST instance = new SaveVisitLocations_TEST(); // Singleton.

	public static SaveVisitLocations_TEST instance() { // Singleton.
		return instance;
	}

	private SaveVisitLocations_TEST() { // Singleton.

	}

	public int countVisitLocations() {
		if (!DbConnect.instance().isConnected()) {
			return 0;
		}
		Statement stmt = null;
		ResultSet rs = null;
		int counter = 0;
		try {
			stmt = DbConnect.instance().getConnection().createStatement();
			rs = stmt.executeQuery("select count(*) from visit_location; ");
			while (rs.next()) {
				counter = rs.getInt(1);
			}
			stmt.close();
			return counter;

		} catch (SQLException e) {
			HandleError(e);
			return 0;
		} finally {
		}
	}

	public void deleteVisitLocations() {
		if (!DbConnect.instance().isConnected()) {
			return;
		}
		Statement stmt = null;
		try {			
			stmt = DbConnect.instance().getConnection().createStatement();
//			stmt.execute("delete from protected_area; ");
			stmt.execute("delete from visit_location; ");
			stmt.close();
		} catch (SQLException e) {
			HandleError(e);
		}		
	}

	public void getAllVisitLocationIds(List<String> locationKeys) {
		if (!DbConnect.instance().isConnected()) {
			return;
		}
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = DbConnect.instance().getConnection().createStatement();
			rs = stmt.executeQuery("select location_id from visit_location; ");
			while (rs.next()) {
				locationKeys.add(rs.getString(1));
			}
			stmt.close();

		} catch (SQLException e) {
			HandleError(e);
			return;
		} finally {
		}
	}

	public void getAllUniqueVisitPositions(List<float[]> positions) {
		if (!DbConnect.instance().isConnected()) {
			return;
		}
		Statement stmt = null;
		ResultSet rs = null;
		try {

//			stmt = DbConnect.instance().getConnection().createStatement();
//			rs = stmt.executeQuery("select distinct visit_latitude_dd, visit_longitude_dd from visit; ");
//			while (rs.next()) {
//				float[] array = new float[2];
//				array[0] = rs.getFloat(1);
//				array[1] = rs.getFloat(2);
//				positions.add(array);
//			}
//			stmt.close();

			stmt = DbConnect.instance().getConnection().createStatement();
			rs = stmt.executeQuery("select distinct visit_location_id from visit;");
			while (rs.next()) {
				// Key format example "N55.1234 E15.1234".				
				String locationIdString = rs.getString(1);
				locationIdString = locationIdString.replace("N", "").replace("E", "").replace("S", "").replace("W", "");
				if (locationIdString.length() > 12) {
					String[] lat_long = locationIdString.split(Pattern.quote(" "));
//					String latitude = locationIdString.substring(1, 8);
//					String longitude = locationIdString.substring(10, 17);
					float[] array = new float[2];
					array[0] = new Float(lat_long[0]);
					array[1] = new Float(lat_long[1]);
					positions.add(array);
				} else {
					System.out.println("DEBUG");
				}
				
			}
			stmt.close();		

		} catch (SQLException e) {
			HandleError(e);
			return;
		} finally {
		}
	}

	public int insertVisitLocations(VisitLocation_TEST visitLocation) {

		try {
			PreparedStatement stmtInsertVisitLocation = DbConnect.instance().getConnection().prepareStatement(sqlInsertVisitLocation);
			SqlPreparedStatementRow row = new SqlPreparedStatementRow(stmtInsertVisitLocation);
			
			row.addCleanedString(visitLocation.getLocationId() ); // location_id	
			row.addStringReplaceIfEmpty(visitLocation.getNation() ); // location_nation
			row.addStringReplaceIfEmpty(visitLocation.getCounty() ); // location_county
			row.addStringReplaceIfEmpty(visitLocation.getMunicipality() ); // location_municipality
			row.addStringReplaceIfEmpty(visitLocation.getWaterDistrict() ); // location_water_district
			row.addStringReplaceIfEmpty(visitLocation.getSvarSeaAreaCode() ); // location_svar_sea_area_code
			row.addStringReplaceIfEmpty(visitLocation.getSvarSeaAreaName() ); // location_svar_sea_area_name
			row.addStringReplaceIfEmpty(visitLocation.getWaterCategory() ); // location_water_category
			row.addStringReplaceIfEmpty(visitLocation.getTypeArea() ); // location_type_area
			row.addStringReplaceIfEmpty(visitLocation.getHelcomOsparArea() ); // location_helcom_ospar_area, " 
			row.addStringReplaceIfEmpty(visitLocation.getEconomicZone() ); // location_economic_zone, " 
			row.addStringReplaceIfEmpty(visitLocation.getSeaBasin() ); // location_sea_basin, " 			
//			row.addStringReplaceIfEmpty(visitLocation.getProtectedAreas() ); // location_protected_areas
			
			// Create key/value-list for locations.
			String keyValues = "";
			keyValues = "location_id:" + visitLocation.getLocationId() + "\t";
			keyValues += "location_nation:" + visitLocation.getNation() + "\t";
			keyValues += "location_county:" + visitLocation.getCounty() + "\t";
			keyValues += "location_municipality:" + visitLocation.getMunicipality() + "\t";
			keyValues += "location_water_district:" + visitLocation.getWaterDistrict() + "\t";
			keyValues += "location_svar_sea_area_code:" + visitLocation.getSvarSeaAreaCode() + "\t";
			keyValues += "location_svar_sea_area_name:" + visitLocation.getSvarSeaAreaName() + "\t";
			keyValues += "location_type_area:" + visitLocation.getTypeArea() + "\t";
			keyValues += "location_water_category:" + visitLocation.getWaterCategory() + "\t";
			keyValues += "location_helcom_ospar_area:" + visitLocation.getHelcomOsparArea() + "\t";
			keyValues += "location_economic_zone:" + visitLocation.getEconomicZone() + "\t";
			keyValues += "location_sea_basin:" + visitLocation.getSeaBasin() + "\t";
//			keyValues += "location_protected_area_names:" + visitLocation.getProtectedAreaNames() + "\t";
			// 
			row.addStringReplaceIfEmpty(keyValues); // keyvalue_location

			stmtInsertVisitLocation.executeUpdate();
			return 0;

		} catch (SQLException e) {
			HandleError(e);
			return -1;
		}
	}

	public int insertProtectedArea(VisitLocation_TEST visitLocation, String protectionArea) {
		try {
			PreparedStatement stmtInsertProtectedArea = DbConnect.instance().getConnection().prepareStatement(sqlInsertProtectedArea);
			SqlPreparedStatementRow row = new SqlPreparedStatementRow(stmtInsertProtectedArea);
			row.addCleanedString(visitLocation.getLocationId() );
			row.addCleanedString(protectionArea);
			
			stmtInsertProtectedArea.executeUpdate();
			
			return 0;

		} catch (SQLException e) {
			HandleError(e);
			return -1;
		}
	}
	
	private void HandleError(SQLException e) {
		// Note: It is not recommended to put message dialog here. Should be in the UI layer.
		MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
		messageBox.setText("SQL error in VisitLocations");
		messageBox.setMessage("Error: " + e.getMessage());
		messageBox.open();
		e.printStackTrace();
		System.exit(-1);
	}
	
}
