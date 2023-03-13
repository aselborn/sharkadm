/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.screening;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import se.smhi.sharkadm.database.DbConnect;

public class DatabaseDoubletScreening {

	private PrintWriter screeningWriter;
	private PrintWriter screeningWriter_2;
	private PrintStream logInfo;
	
	private Integer numberOfDuplicates = 0;
	private Integer numberOfSamples = 0;
	private Integer totalNumberOfDuplicates = 0;
	private Integer totalNumberOfSamples = 0;
	private List<String> resultString = new ArrayList<String>();
	
	public void performScreening(PrintStream logInfo) {
		
		this.logInfo = logInfo;
		
		// Create log file.
		DateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		File screeningFile = new File("Screening_duplicate_samples_" +
				datetimeFormat.format(new Date()) + ".txt");
		try { 
			screeningWriter = new PrintWriter(new FileWriter(screeningFile));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			File screeningFile_2 = new File("\\\\winfs\\data\\prodkap\\sharkweb\\Screening_duplicate_samples_" +
					datetimeFormat.format(new Date()) + ".txt");
			try { 
				screeningWriter_2 = new PrintWriter(new FileWriter(screeningFile_2));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e1) { }

		this.logMessage("");
		this.logMessage("=== Checking for duplicate samples in the sharkweb database. ===");
		this.logInfo.println("");
		this.logInfo.println("");
		this.logInfo.println("- Note: Details can be found in this file: " + screeningFile.toString());
		this.logInfo.println("");
		
		this.totalNumberOfDuplicates = 0;
		this.totalNumberOfSamples = 0;
		
		checkDatatype("Physical and Chemical");
		checkDatatype("Bacterioplankton");
		checkDatatype("Chlorophyll");
		checkDatatype("Epibenthos");
		checkDatatype("Grey seal");
		checkDatatype("Harbour seal");
		checkDatatype("Phytoplankton");
		checkDatatype("Picoplankton");
		checkDatatype("Primary production");
		checkDatatype("Ringed seal");
		checkDatatype("Seal pathology");
		checkDatatype("Sedimentation");
		checkDatatype("Zoobenthos");
		checkDatatype("Zooplankton");
		
		this.logMessage("");
		this.logMessage("");
		this.logMessage("Total number of duplicate samples found: " + this.totalNumberOfDuplicates.toString());
		this.logMessage("Total number of samples checked: " + this.totalNumberOfSamples.toString());
		this.logMessage("");
		this.logMessage("=== Finished. ===");

		// Close file.
		screeningWriter.close();
		
		try { 
			screeningWriter_2.close();
		} catch (Exception e1) { }

	}
	
	public void checkDatatype(String datatype) {

		this.logMessage("");
		this.logMessage("- Checking for duplicates in: " + datatype);
		
		this.resultString.clear();
		
		getSampleIdsFromDatabase(datatype);
	
		this.logMessage("   - Number of duplicate samples found: " + this.resultString.size());
		this.logMessage("   - Number of samples checked: " + this.numberOfSamples);
		this.totalNumberOfDuplicates += this.resultString.size();
		this.totalNumberOfSamples += this.numberOfSamples;
		
		for (String row : this.resultString) {
			this.screeningWriter.println(row);
//			logInfo.println(row);
			try { 
				screeningWriter_2.println(row);
			} catch (Exception e1) { }
		}		
	}
	
	public void getSampleIdsFromDatabase(String datatype) {

		if (!DbConnect.instance().isConnected()) {
			return;
		}
		Statement stmt = null;
		ResultSet rs = null;
		try {
			// Count number of rows.
			String sqlString = ""; 
			sqlString += "SELECT count(*) "; 
			sqlString += "FROM sample "; 
			sqlString += "WHERE sample_datatype = '" + datatype + "' "; 
			
			stmt = DbConnect.instance().getConnection().createStatement();
			rs = stmt.executeQuery(sqlString);
			while (rs.next()) {
				this.numberOfSamples = Integer.parseInt(rs.getString(1));
			}
			
			// Check for duplicates.
			sqlString = ""; 
			sqlString += "SELECT sample_id, keyvalue_sample, count(*) "; 
			sqlString += "FROM sample "; 
			sqlString += "WHERE sample_datatype = '" + datatype + "' "; 
			sqlString += "GROUP BY sample_id, keyvalue_sample "; 
			sqlString += "HAVING count(*) > 1 "; 
			
			stmt = DbConnect.instance().getConnection().createStatement();
			rs = stmt.executeQuery(sqlString);
			
			while (rs.next()) {
				String keyvalueSample = rs.getString(2);
				String[] keyvalueList = keyvalueSample.split(Pattern.quote("\t"));
				String sampleIdMd5 = "";
				for (String keyvalue: keyvalueList) {
					if (keyvalue.contains("shark_sample_id_md5:")) {
						sampleIdMd5 = keyvalue.replace("shark_sample_id_", "");
					}
				}
				this.resultString.add("- sample_id: " + rs.getString(1) + "   " + sampleIdMd5 + "   Number of:" + rs.getString(3));
			}
			stmt.close();

		} catch (SQLException e) {
			HandleError(e);
			return;
		}
	}
	
	public void logMessage(String logMessage) {
		this.screeningWriter.println(logMessage);
		this.logInfo.println(logMessage);
		try { 
			screeningWriter_2.println(logMessage);
		} catch (Exception e1) { }
	}

	private void HandleError(SQLException e) {
		// Note: It is not recommended to put message dialog here. Should be in the UI layer.
		MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
		messageBox.setText("SQL error in DatabaseDoubletScreening");
		messageBox.setMessage("Error: " + e.getMessage());
		messageBox.open();
		e.printStackTrace();
		System.exit(-1);
	}

}

