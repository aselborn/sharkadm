/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.fileimport;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.ModelVisitor;

/**
 * Base class for file import scripts.
 */
public abstract class SingleFileImport extends ModelVisitor {
	
	protected PrintStream logInfo;
	
	protected int errorCounter = 0;
	
	// Concatenated error list. Param 2 = error counter
	private Map<String, Integer> concatErrorList = new HashMap<String, Integer>();


////TODO NOT USED	private String[] expectedColumns;
	private String[] headerFields;
	
	public SingleFileImport(PrintStream logInfo2) {
		this.logInfo = logInfo2;
	}
	
	/**
	 * This abstract function should be implemented in sub-classes.
	 */
	public abstract void importFiles(String zipFileName);
	
	public abstract void importFiles(String zipFileName, Dataset dataset); // TODO Development...
	
	public void clearConcatErrors() {
		this.concatErrorList.clear();
	}
	
	public void addConcatError(String errorString) {
		int counter;
		if (this.concatErrorList.containsKey(errorString)) {
			counter = this.concatErrorList.get(errorString) + 1;
		} else {
			counter = 1;
		}
		this.concatErrorList.put(errorString, counter);
	}
	
	public Map<String, Integer> getConcatErrors() {
		return concatErrorList;
	}
	
//	public List<String[]> getFileContent(String zipFileName, String partOfZip) {
//		try {
//			return ParseFileUtil.parseDataFile(
//						ZipFileUtil.getZipFileEntry(zipFileName, partOfZip), true);
//		} catch (Exception e) {
//			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
//			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
//			messageBox.setText("File import");
//			messageBox.setMessage("Can\'t open file: " + partOfZip + "\n" + "in " + zipFileName + ".");
//			messageBox.open();
////			e.printStackTrace();
//			logInfo.println("ERROR: Can\'t open file:" + partOfZip + ".");
//			errorCounter++;			
//			return null;
//		}
//	}
		
//	public void setExpectedColumns(String[] expectedColumns) {
//// TODO NOT USED		this.expectedColumns = expectedColumns;
//	}
	
	// Used to check that the columns in the file corresponds to the expected column list.
	public void checkHeader(String[] header) {
//		if (expectedColumns != null) {			
//			for (int i = 0; i < expectedColumns.length; i++) {
//				if (!header[i].equals(expectedColumns[i])) {
//					logInfo.println("ERROR: Invalid column in header: " + header[i]+ ", expected: " + expectedColumns[i] + ".");
//					errorCounter++;
//				}
//			}
//		}
		headerFields = header;
	}

	// Checks the number of expected rows. Specified in "dataset_note.txt".
	public void checkRowCount(int rowCount, String rowCountDescription) {
//		if (datasetNote.getDatasetNote().containsKey(rowCountDescription)) {
//			int expRowCount = Integer.parseInt(datasetNote.getDatasetNote().get(rowCountDescription));
//			if (rowCount != expRowCount) {
//				logInfo.println("ERROR: Expected number of rows: "+ expRowCount + ".");
//				errorCounter++;
//			}
//		} else {
//			logInfo.println("WARNING: Row counter not specified in \"dataset_note.txt\".");
//		}
	}
	
	// Check if cell exists.
	public Boolean cellExist(String[] row, String columnName) {
		for (int i = 0; i < headerFields.length; i++) {
			if (headerFields[i].equals(columnName)) {
				return true;
			}
		}
		return false;
	}

	// Gets the cell content based on expected columns.
	public String getCell(String[] row, String columnName) {
//		for (int i = 0; i < expectedColumns.length; i++) {
//			if (expectedColumns[i].equals(columnName)) {
//				return row[i];
//			}
//		}
		for (int i = 0; i < headerFields.length; i++) {
			if (headerFields[i].equals(columnName)) {
				return row[i];
			}
		}
		if (!columnName.equals("EXPID") && 
			!columnName.equals("SERNO") && 
			!columnName.equals("R„ttningskommentar") && 
			!columnName.equals("RŽTTNINGSK") ) {
			// Don't log errors for fields used to match expeditions, etc.
			if (logInfo != null) {
				logInfo.println("ERROR: " + columnName + " is not a valid column name.");
			} else {
				System.out.println("ERROR: " + columnName + " is not a valid column name.");
			}
			errorCounter++;
		}
		return "";
	}

	// Converts from decimal comma to decimal point.
	public String getCellDecimal(String[] row, String columnName) {
		String value = getCell(row, columnName).replace(",", ".");
		return value.replace(" ", ""); // Remove 1000-delimiter.
	}
	
	// Removes the decimal part.
	public String getCellNoDecimals(String[] row, String columnName) {
		String value = getCell(row, columnName).replace(",", ".");
		if (value.indexOf(".") != -1) {
			return value.substring(0, value.indexOf("."));
		} else {
			return value.replace("", ""); // Remove 1000-delimiter.
		}
	}
	
	// Return value with one decimal. // TODO: Replace by format and variable number of decimals.
	public String getCellOneDecimal(String[] row, String columnName) {
		String value = getCell(row, columnName).replace(",", ".");
		if ((value.indexOf(".") != -1) && (value.length() >= (value.indexOf(".") + 1))){
			return value.substring(0, value.indexOf(".") + 2);
		} else {
			return value;
		}
	}
	
	// Converts date to YYYY-MM-DD format (if not ISO format).
	public String getCellDate(String[] row, String columnName) {
		String value = getCell(row, columnName);
		
		// Don't change if ISO-format.
		if (value.contains("-")) {
			return value;
		}
		
		// If "20000101" convert to "2000-01-01".	
		if (value.equals("")) {
			return "";
		} else if (value.length() == 8) {
			return value.substring(0, 4) + "-" + value.substring(4, 6) + "-" + value.substring(6, 8);
		} else {
			logInfo.println("ERROR: Can't convert time from: "+ value + ".");
			errorCounter++;
			return "";
		}
	}
	
	// Converts time to HH:MM:SS format (if not ISO format).
	public String getCellTime(String[] row, String columnName) {
		String value = getCellNoDecimals(row, columnName);
		
		// Don't change if ISO-format.
		if (value.contains(":")) {
			return value;
		}
		
		// If "45" or "945" or "1245" convert to "9:45:00" or "12:45:00".	
		if (value.equals("")) {
			return "";
		} else if (value.length() < 3) { 
			// Only minutes.
			if (Integer.parseInt(value) > 59) {
				logInfo.println("ERROR: Can't convert time from: "+ value + ".");
				errorCounter++;
				return "";
			}
			return "00:" + value + ":00";
		} else if (value.length() == 3) {
			if (Integer.parseInt(value.substring(1, 3)) > 59) {
				logInfo.println("ERROR: Can't convert time from: "+ value + ".");
				errorCounter++;
				return "";
			}
			return value.substring(0, 1) + ":" + value.substring(1, 3) + ":00";
		} else if (value.length() == 4) {
			if (Integer.parseInt(value.substring(0, 2)) > 23) {
				logInfo.println("ERROR: Can't convert time from: "+ value + ".");
				errorCounter++;
				return "";
			}
			if (Integer.parseInt(value.substring(2, 4)) > 59) {
				logInfo.println("ERROR: Can't convert time from: "+ value + ".");
				errorCounter++;
				return "";
			}
			return value.substring(0, 2) + ":" + value.substring(2, 4) + ":00";
		} else {
			logInfo.println("ERROR: Can't convert time from: "+ value + ".");
			errorCounter++;
			return "";
		}
	}
	
	// Converts degree minute. 2 digits. TODO: Remove this function later.
	public String getCellDegMinute(String[] row, String columnName) {
		String value = getCell(row, columnName);
		if((value.indexOf(".") == 2) || (value.indexOf(",") == 2)) {
			return value;
		} else {
			return "0" + value;
		}
	}
	
	public int getErrorCounter() {
		return errorCounter;
	}

}
