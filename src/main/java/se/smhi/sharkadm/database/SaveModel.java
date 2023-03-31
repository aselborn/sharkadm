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
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import se.smhi.sharkadm.datasets.columns.ColumnInfoManager;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.utils.SqlPreparedStatementRow;
import se.smhi.sharkadm.ziparchive.ZipArchiveManager;

public class SaveModel {

//	private GeodesiSwedishGrids sweref99tm = new GeodesiSwedishGrids();
//	private GeodesiSwedishGrids rt90 = new GeodesiSwedishGrids("rt90_2.5_gon_v");
	private ColumnInfoManager columnInfoManager = new ColumnInfoManager();

	// === Dataset. ===
	private String sqlInsertDataset = "insert into dataset ("
		+ "dataset_package_name, "
		+ "reporting_institute_name, "
		+ "reported_by, "
		+ "delivery_comment, "
		+ "keyvalue_dataset, "
		+ "import_file_name, "
		+ "import_file_path, "
		+ "import_format, "
		+ "imported_by, "
		+ "import_datetime, "
		+ "import_status, "
		+ "import_matrix_column "
		+ ") values (?, ?, ?, ?, ?, ?, ?, ?, current_user, current_timestamp, ?, ?);";

	// === Visit. ===
	private String sqlInsertVisit = "insert into visit ("
		+ "dataset_oid, "
		+ "visit_id, "
		+ "visit_date, "
		+ "visit_year, "
		+ "visit_month, "
		+ "visit_location_id, "
		+ "visit_latitude_dd, "
		+ "visit_longitude_dd, "
		+ "station_name, "
		+ "station_name_uppercase, "
		+ "reported_station_name, "
		+ "station_viss_eu_id, "
		+ "keyvalue_visit, "
		+ "visit_position "
		+ ") values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ST_GeomFromText(?));";

	// === Sample. ===
	private String sqlInsertSample = "insert into sample (" 
		+ "visit_oid, "
		+ "check_status, "
		+ "data_checked_by, "
		+ "sample_id, "
		+ "sample_datatype, "
		+ "sample_latitude_dd, "
		+ "sample_longitude_dd, "
		+ "sample_min_depth_m, "
		+ "sample_max_depth_m, "
		+ "sample_project, "
		+ "sample_orderer, "
		+ "keyvalue_sample, "
		+ "keyvalue_params, "
		+ "sample_position "
		+ ") values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ST_GeomFromText(?));";
	
	// === Sample project lookup. ===
	private String sqlInsertProjectLookup = "insert into project_lookup (" 
		+ "lookup_project, "
		+ "sample_oid "
		+ ") values (?, ?);";
	
	// === Sample orderer lookup. ===
	private String sqlInsertOrdererLookup = "insert into orderer_lookup (" 
		+ "lookup_orderer, "
		+ "sample_oid "
		+ ") values (?, ?);";
	
	// === Variable. ===
	private String sqlInsertVariable = "insert into variable ("
		+ "sample_oid, " 
		+ "parameter, " 
		+ "value, " 
		+ "value_float, " 
		+ "unit, " 
		+ "quality_flag, " 
		+ "dyntaxa_id, "     
		+ "scientific_name, "     
		+ "reported_scientific_name, "
		+ "keyvalue_variable "
		+ ") values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

// MySQL private String sqlLastInsertId = "select last_insert_id();";
	private String sqlLastInsertId = "select lastval();";
	
	private PreparedStatement stmtInsertDataset;
	private PreparedStatement stmtInsertVisit;
	private PreparedStatement stmtInsertSample;
	private PreparedStatement stmtInsertVariable;
	private PreparedStatement stmtLastInsertId;
	// 
	private PreparedStatement stmtInsertSampleProjectLookup;	
	private PreparedStatement stmtInsertSampleOrdererLookup;

	private ResultSet rs;
	private Connection lastUsedConnection = null;

	private static SaveModel instance = new SaveModel(); // Singleton

	public static SaveModel instance() { // Singleton.
		return instance;
	}

	private SaveModel() { // Singleton.
		initiatePreparedStatements();
	}

	private void initiatePreparedStatements() {
		try {
			lastUsedConnection = DbConnect.instance().getConnection();

			stmtInsertDataset = DbConnect.instance().getConnection().prepareStatement(sqlInsertDataset);
			stmtInsertVisit = DbConnect.instance().getConnection().prepareStatement(sqlInsertVisit);
			stmtInsertSample = DbConnect.instance().getConnection().prepareStatement(sqlInsertSample);
			stmtInsertVariable = DbConnect.instance().getConnection().prepareStatement(sqlInsertVariable);
			stmtLastInsertId = DbConnect.instance().getConnection().prepareStatement(sqlLastInsertId);
			//
			stmtInsertSampleProjectLookup = DbConnect.instance().getConnection().prepareStatement(sqlInsertProjectLookup);
			stmtInsertSampleOrdererLookup = DbConnect.instance().getConnection().prepareStatement(sqlInsertOrdererLookup);
		} catch (SQLException e) {
			HandleSqlError(e, "");
		} catch (Exception e) {
			HandleError(e);
		}
	}
	
	public int insertDataset(Dataset dataset) {
		if ((stmtInsertDataset == null) || (lastUsedConnection != DbConnect.instance().getConnection())) {
			initiatePreparedStatements();
		}
		try {
			
			// Check last used ZIP file name.
			String datasetName = dataset.getField("dataset.dataset_name");
			String datasetFileName = ZipArchiveManager.instance().getLatestZipFileVersion(datasetName);
			if (! datasetFileName.equals("")) {
				dataset.addField("dataset.dataset_file_name", datasetFileName);
			}

			SqlPreparedStatementRow row = new SqlPreparedStatementRow(stmtInsertDataset);
			row.addCleanedString(dataset.getField("dataset.dataset_file_name") ); // dataset_package_name
			row.addCleanedString(dataset.getField("dataset.reporting_institute_name_sv") ); // reporting_institute_name			
			row.addCleanedString(dataset.getField("dataset.reported_by") ); // reported_by			
			row.addCleanedString(dataset.getField("dataset.dataset_comment") ); // dataset_comment

			columnInfoManager.clearKeyValueStringList();
			columnInfoManager.addAllFieldsOnNodeLevel(dataset, "swedish");
			row.addCleanedString(columnInfoManager.getKeyValueStringListAsString().replace("<->", ", ")); // keyvalue_dataset
			row.addCleanedString(dataset.getField("dataset.dataset_file_name")); // import_file_name
			row.addCleanedString(dataset.getField("dataset.dataset_file_path")); // import_file_path			
			row.addCleanedString(dataset.getField("dataset.import_format")); // import_format
			row.addCleanedString(dataset.getImport_status()); // import_status
			row.addCleanedString(dataset.getImport_matrix_column()); // import_matrix_column
			
			stmtInsertDataset.executeUpdate();
			
			rs = stmtLastInsertId.executeQuery();
			rs.next();
			return rs.getInt(1);

		} catch (SQLException e) {
			HandleSqlError(e, stmtInsertDataset.toString());
			return 0;
		} catch (Exception e) {
			HandleError(e);
			return 0;
		}
	}

	public int insertVisit(Visit visit) {
		if ((stmtInsertVisit == null) || (lastUsedConnection != DbConnect.instance().getConnection())) {
			initiatePreparedStatements();
		}
		try {
			SqlPreparedStatementRow row = new SqlPreparedStatementRow(stmtInsertVisit);
			row.addOid(visit.getParent().getDataset_oid()); // dataset_oid
			row.addCleanedString(visit.getField("visit.visit_id")); // visit_id
			row.addCleanedString(visit.getField("visit.visit_date")); // visit_date
			row.addCleanedString(visit.getField("visit.visit_year")); // visit_year
			row.addCleanedString(visit.getField("visit.visit_month")); // visit_month			

//			row.addCleanedString(VisitLocationManager.calculateKey(
//											visit.getPosition().getLatitude(), 
//											visit.getPosition().getLongitude())); // visit_location_id.
			row.addCleanedString(visit.getField("visit.visit_location_id")); // visit_location_id.
			
			row.addFloat(visit.getPosition().getLatitude()); // visit_latitude_dd
			row.addFloat(visit.getPosition().getLongitude()); // visit_longitude_dd
			if (visit.getField("visit.station_name").equals("")) {
				row.addCleanedString("-"); // Use "-" to make it possible to search.
			} else {
				row.addCleanedString(visit.getField("visit.station_name")); // station_name
			}			
			row.addCleanedString(visit.getField("visit.station_name").replace(" ", "").toUpperCase()); // station_name_uppercase
			row.addCleanedString(visit.getField("visit.reported_station_name")); // reported_station_name
			row.addCleanedString(visit.getField("visit.station_viss_eu_id")); // station_viss_eu_id


			columnInfoManager.clearKeyValueStringList();
			columnInfoManager.addAllFieldsOnNodeLevel(visit, "swedish");
			row.addCleanedString(columnInfoManager.getKeyValueStringListAsString().replace("<->", ", ")); // keyvalue_visit

			row.addCleanedString(visit.getPosition().getDbPoint()); // visit_position

			stmtInsertVisit.executeUpdate();

			rs = stmtLastInsertId.executeQuery();
			rs.next();
			return rs.getInt(1);

		} catch (SQLException e) {
			HandleSqlError(e, stmtInsertVisit.toString());
			return 0;
		} catch (Exception e) {
			HandleError(e);
			return 0;
		}
	}

	public int insertSample(Sample sample) {
		if ((stmtInsertSample == null) || (lastUsedConnection != DbConnect.instance().getConnection())) {
			initiatePreparedStatements();
		}
		try {
			SqlPreparedStatementRow row = new SqlPreparedStatementRow(stmtInsertSample);
			
			row.addOid(sample.getParent().getVisit_oid()); // visit_oid
			row.addCleanedString(sample.getParent().getParent().getField("dataset.check_status_sv")); // check_status
			row.addCleanedString(sample.getParent().getParent().getField("dataset.data_checked_by_sv")); // data_checked_by

//			row.addCleanedString(sample.getField("sample.sample_id")); // sample_id
			row.addCleanedString(sample.getField("sample.shark_sample_id")); // shark_sample_id
			
			
			
			row.addCleanedString(sample.getField("sample.sample_datatype")); // sample_datatype
//			row.addCleanedString(sample.getField("sample.sample_date")); // sample_date
			row.addFloat(sample.getPosition().getLatitude()); // sample_latitude_dd
			row.addFloat(sample.getPosition().getLongitude()); // sample_longitude_dd
			row.addFloatFromString(sample.getField("sample.sample_min_depth_m")); // sample_min_depth
			row.addFloatFromString(sample.getField("sample.sample_max_depth_m")); // sample_max_depth				
//			row.addCleanedString(sample.getField("sample.sampler_type_code")); // sampler_type
			
			row.addCleanedString(sample.getField("sample.sample_project_name_sv").replace("<->", ", ")); // sample_project_name
			
			row.addCleanedString(sample.getField("sample.sample_orderer_name_sv").replace("<->", ", ")); // monitoring_program_code

			columnInfoManager.clearKeyValueStringList();
			columnInfoManager.addAllFieldsOnNodeLevel(sample, "swedish");
			row.addCleanedString(columnInfoManager.getKeyValueStringListAsString().replace("<->", ", ")); // keyvalue_sample

			String keyValueListForParams = "";
			// Datatype 'Physical and Chemical' does not contain species info. 
			// Parameter values for the 'params as columns' view is stored on sample level.
			if (sample.getField("sample.sample_datatype").equals("Physical and Chemical")) {
				String separator = "";
				for (Variable variable : sample.getVariables()) {
					String parameter = variable.getParameter();
					String value = variable.getValue().replace(",", ".");
					String unit = variable.getUnit();
					String qflag = variable.getField("variable.quality_flag");
					
					if (qflag.equals("B")) {
						value = "<remove>";
					}

					if ((!parameter.equals("")) && (!value.equals(""))) {
						if (unit.equals("")) {
							keyValueListForParams += separator + parameter + ":" + value.replace("<remove>", "");
							separator = "\t"; // Tab.
							keyValueListForParams += separator + "QFLAG " + parameter + ":" + qflag;
						} else {
							keyValueListForParams += separator + parameter + " (" + unit + "):" + value.replace("<remove>", "");
							separator = "\t"; // Tab.
							keyValueListForParams += separator + "QFLAG " + parameter + ":" + qflag;
						}
					}
				}
			}
			row.addCleanedString(keyValueListForParams); // keyvalue_params

			row.addCleanedString(sample.getPosition().getDbPoint()); // sample_position
			
			stmtInsertSample.executeUpdate();

			rs = stmtLastInsertId.executeQuery();
			rs.next();
			Integer sampleOid = rs.getInt(1);
			
			// Add values to Project Lookup.	
//			String[] projectParts = sample.getField("sample.sample_project_name_sv").split(Pattern.quote(","));
			String[] projectParts = sample.getField("sample.sample_project_name_sv").split(Pattern.quote("<->"));
			for (String part : projectParts) {
				SqlPreparedStatementRow lookupRow = new SqlPreparedStatementRow(stmtInsertSampleProjectLookup);
				lookupRow.addCleanedString(part.trim());
				lookupRow.addInt(sampleOid);
				stmtInsertSampleProjectLookup.executeUpdate();
			}
			
			// Add values to Orderer Lookup.
//			String[] ordererParts = sample.getField("sample.sample_orderer_name_sv").split(Pattern.quote(","));
			String[] ordererParts = sample.getField("sample.sample_orderer_name_sv").split(Pattern.quote("<->"));
			for (String part : ordererParts) {
				SqlPreparedStatementRow lookupRow = new SqlPreparedStatementRow(stmtInsertSampleOrdererLookup);
				lookupRow.addCleanedString(part.trim());
				lookupRow.addInt(sampleOid);
				stmtInsertSampleOrdererLookup.executeUpdate();
			}
			
			return sampleOid;

		} catch (SQLException e) {
			HandleSqlError(e, stmtInsertSample.toString());
			return 0;
		} catch (Exception e) {
			HandleError(e);
			return 0;
		}
	}

	public int insertVariable(Variable variable) {
		if ((stmtInsertVariable == null) || (lastUsedConnection != DbConnect.instance().getConnection())) {
			initiatePreparedStatements();
		}
		try {
			SqlPreparedStatementRow row = new SqlPreparedStatementRow(stmtInsertVariable);
			row.addOid(variable.getParent().getSample_oid()); // sample_oid
			row.addCleanedString(variable.getParameter()); // parameter
			
			if (!variable.getField("variable.quality_flag").equals("B")) {
				row.addCleanedString(variable.getValue().replace(",", ".")); // value
				row.addFloatFromString(variable.getValue()); // value_float
			} else {
				// If QFLAG=B the value should be Blank/NaN.
				row.addCleanedString(""); // value
				row.addFloatFromString(""); // value_float
			}
			
			row.addCleanedString(variable.getUnit()); // unit
			row.addCleanedString(variable.getField("variable.quality_flag")); // quality_flag
			row.addIntFromString(variable.getDyntaxaId()); // dyntaxa_id
			row.addCleanedString(variable.getField("variable.scientific_name")); // scientific_name
			row.addCleanedString(variable.getField("variable.reported_scientific_name")); // reported_scientific_name

			columnInfoManager.clearKeyValueStringList();
			columnInfoManager.addAllFieldsOnNodeLevel(variable, "swedish");
			row.addCleanedString(columnInfoManager.getKeyValueStringListAsString().replace("<->", ", ")); // keyvalue_variable
	
			stmtInsertVariable.executeUpdate();

			rs = stmtLastInsertId.executeQuery();
			rs.next();
			return rs.getInt(1);

		} catch (SQLException e) {
			HandleSqlError(e, stmtInsertVariable.toString());
			return 0;
		} catch (Exception e) {
			HandleError(e);
			return 0;
		}
	}

	private void HandleSqlError(SQLException e, String moreInfo) {
		// Note: It is not recommended to put message dialog here. Should be in the UI layer.
		MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
		messageBox.setText("SQL error when saving to database");
		messageBox.setMessage("SQL error: " + e.getMessage() + "\n\nSQL query:\n" + moreInfo);
		messageBox.open();
		e.printStackTrace();
		System.exit(-1);
	}

	private void HandleError(Exception e) {
		// Note: It is not recommended to put message dialog here. Should be in the UI layer.
		MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
		messageBox.setText("Error when saving to database");
		messageBox.setMessage("Error: " + e.getMessage());
		messageBox.open();
		e.printStackTrace();
		System.exit(-1);
	}

}
