/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.formats;

import java.io.PrintStream;
import java.util.regex.Pattern;

import se.smhi.sharkadm.datasets.fileimport.FileImportInfo;
import se.smhi.sharkadm.fileimport.misc.FileImportFilter;
import se.smhi.sharkadm.fileimport.misc.FileImportTranslate;
import se.smhi.sharkadm.fileimport.misc.FileImportTranslateAllColumns;
import se.smhi.sharkadm.sql.SqliteManager;

/**
 * Base class for file import scripts.
 */
public abstract class FormatFileBase extends FormatBase {
	
	private String[] headerFields;
	
	protected FileImportTranslate valueTranslate;
	protected FileImportFilter filter;
	
	public FormatFileBase(PrintStream logInfo, FileImportInfo importInfo) {
		super(logInfo, importInfo);
		FileImportTranslateAllColumns.instance().loadFile();
	}
	
	// Header fields are used when importing from table oriented files.
	public void setHeaderFields(String[] headerFields) {
		this.headerFields = headerFields;
	}

	// Gets the cell content based on header column array.
	public String getCell(String[] row, String columnName) {
		for (int i = 0; i < headerFields.length; i++) {
			if (headerFields[i].equals(columnName)) {
				if ((valueTranslate != null) && (valueTranslate.isTranslateUsed())) {
					String translatedValue =  valueTranslate.translateImportValue(columnName, row[i].trim());
					return translatedValue;
				} else {
					return row[i].trim();
				}
			}
		}
		importInfo.addConcatWarning(columnName + " is not a valid column name.");
		return "";
	}

	// Gets the cell content based on header column array.
	public String getCellNoTranslation(String[] row, String columnName) {
		for (int i = 0; i < headerFields.length; i++) {
			if (headerFields[i].equals(columnName)) {
				return row[i].trim();
			}
		}
		importInfo.addConcatWarning(columnName + " is not a valid column name.");
		return "";
	}

	// Checks if the column in header exists.
	public boolean columnExist(String columnName) {
		for (String column : headerFields) {
			if (column.equals(columnName)) {
				return true;
			}
		}
		return false;
	}
	
	// Gets the cell content based on translated key.
	public String getCellByKey(String[] row, String value) {
		if (keyTranslate.containsValue(value)) {
			for (String key : keyTranslate.keySet()) {
				if (keyTranslate.get(key).equals(value)) {
					// Multiple rows in the map can point to the same key.
					// Continue searching if current column does not exist in header.
					if (columnExist(key)) {
						return getCell(row, key);
					}
				}				
			}
		}
		return "";
	}

	public String getVisitKey(String[] row) {
		String keyString = "";
		if (! visitKeyColumns.equals("")) {
			String separator = "";
			for (String colName : visitKeyColumns.split(Pattern.quote("<+>"))) {
				if (!colName.equals("")) {
					keyString += separator + getCell(row, colName.trim());
					separator = ":";
				}
			}
		}
		return keyString;
	}	
	
	public String getSampleKey(String[] row) {
		String keyString = "";
		if (! sampleKeyColumns.equals("")) {
			String separator = "";
			for (String colName : sampleKeyColumns.split(Pattern.quote("<+>"))) {
				if (!colName.equals("")) {
					keyString += separator + getCell(row, colName.trim());
					separator = ":";
				}
			}
		}
		return keyString;
	}	
	
	public void addVisitField(String key, String reportedColumnValue) {
		
		// Don't add or overwrite by empty string.
		if (reportedColumnValue.equals("")) {
			return;
		}
		
		String columnValue = reportedColumnValue;

		if (key.contains(".")) {
			
//			// Translate values in 'SHARK_CONFIG/translate_all_columns.txt'.
//			try {
//				String[] parts = key.split(Pattern.quote("."));
//				String key_right = parts[1];
//				columnValue = FileImportTranslateAllColumns.instance().translateValue(key_right, reportedColumnValue);
//				if (!reportedColumnValue.equals(columnValue)) {
//					importInfo.addConcatWarning("Translated value: " + key + " Old: " + reportedColumnValue + " New: " + columnValue);
//				}
//			} catch (Exception e) {
//				System.out.println("DEBUG: Exception when trying to translate value: " + reportedColumnValue);
//			}			
			
			String prefix = key.substring(0, key.indexOf("."));
			if (prefix.equals("dataset")) {
				// Check if exists and if content differ.
				if (dataset.containsField(key)) {
					String value = dataset.getField(key);
					if (!compareValuesWithZeroInDecimalPart(columnValue, value)) {
						importInfo.addConcatError("Old value is overwritten." + 
								" Key: " + key + " Old: " + value + 
								" New: " + columnValue);
					}
				}
				// Add column value.
				dataset.addField(key, columnValue);
			} 
			else if (prefix.equals("visit"))  {
				// Check if exists and if content differ.
				if (currentVisit.containsField(key)) {
					String value = currentVisit.getField(key);
					if (!compareValuesWithZeroInDecimalPart(columnValue, value)) {
						importInfo.addConcatError("Old value is overwritten." + 
								" Key: " + key + " Old: " + value + 
								" New: " + columnValue);
					}
				}
				// Add column value.
				currentVisit.addField(key, columnValue);
			} 
			else if (prefix.equals("sample"))  {
				// NOTE: Put on visit level instead of variable.
				if (currentVisit.containsField(key)) {
					String value = currentVisit.getField(key);
					if (!compareValuesWithZeroInDecimalPart(columnValue, value)) {
						importInfo.addConcatError("Old value is overwritten." + 
								" Key: " + key + " Old: " + value + 
								" New: " + columnValue);
					}
				}
				// Add column value.
				currentVisit.addField(key, columnValue);
			} 
			else if (prefix.equals("variable"))  {
				// NOTE: Put on visit level instead of variable.
				if (currentVisit.containsField(key)) {
					String value = currentVisit.getField(key);
					if (!compareValuesWithZeroInDecimalPart(columnValue, value)) {
						importInfo.addConcatError("Old value is overwritten." + 
								" Key: " + key + " Old: " + value + 
								" New: " + columnValue);
					}
				}
				// Add column value.
				currentVisit.addField(key, columnValue);
			}
			else if (prefix.equals("TEMP")) {
				// Used for connected columns. Example TEMP.QFLAG.Aluminium  is connected to Aluminium.
				String[] keyParts = key.split(Pattern.quote("."));
				if (keyParts.length >= 2) {
					currentVisit.addTempField(key, columnValue);
				}
			}
			
		} else {
			if (!key.equals("NOT_USED")) {
				importInfo.addConcatWarning("Invalid key: " + key);
			}
		}		
	}

	public void addSampleField(String key, String reportedColumnValue) {
		
		// Don't add or overwrite by empty string.
		if (reportedColumnValue.equals("")) {
			return;
		}
		
		String columnValue = reportedColumnValue;

		if (key.contains(".")) {
			
//			// Translate values in 'SHARK_CONFIG/translate_all_columns.txt'.
//			try {
//				String[] parts = key.split(Pattern.quote("."));
//				String key_right = parts[1];
//				columnValue = FileImportTranslateAllColumns.instance().translateValue(key_right, reportedColumnValue);
//				if (!reportedColumnValue.equals(columnValue)) {
//					importInfo.addConcatWarning("Translated value: " + key + " Old: " + reportedColumnValue + " New: " + columnValue);
//				}
//			} catch (Exception e) {
//				System.out.println("DEBUG: Exception when trying to translate value: " + reportedColumnValue);
//			}			
			
			String prefix = key.substring(0, key.indexOf("."));
			if (prefix.equals("dataset")) {
				// Check if exists and if content differ.
				if (dataset.containsField(key)) {
					String value = dataset.getField(key);
					if (!compareValuesWithZeroInDecimalPart(columnValue, value)) {
						importInfo.addConcatError("Old value is overwritten." + 
								" Key: " + key + " Old: " + value + 
								" New: " + columnValue);
					}
				}
				// Add column value.
				dataset.addField(key, columnValue);
			} 
			else if (prefix.equals("visit"))  {
				// Check if exists and if content differ.
				if (currentVisit.containsField(key)) {
					String value = currentVisit.getField(key);
					if (!compareValuesWithZeroInDecimalPart(columnValue, value)) {
						importInfo.addConcatError("Old value is overwritten." + 
								" Key: " + key + " Old: " + value + 
								" New: " + columnValue);
					}
				}
				// Add column value.
				currentVisit.addField(key, columnValue);
			} 
			else if (prefix.equals("sample"))  {
				// Check if exists and if content differ.
				if (currentSample.containsField(key)) {
					String value = currentSample.getField(key);
					if (!compareValuesWithZeroInDecimalPart(columnValue, value)) {
						importInfo.addConcatError("Old value is overwritten." + 
								" Key: " + key + " Old: " + value + 
								" New: " + columnValue);
					}
				}
				// Add column value.
				currentSample.addField(key, columnValue);
			} 
			else if (prefix.equals("variable"))  {
				// NOTE: Put on sample level instead of variable.
				if (currentSample.containsField(key)) {
					String value = currentSample.getField(key);
					if (!compareValuesWithZeroInDecimalPart(columnValue, value)) {
						importInfo.addConcatError("Old value is overwritten." + 
								" Key: " + key + " Old: " + value + 
								" New: " + columnValue);
					}
				}
				// Add column value.
				currentSample.addField(key, columnValue);
			} 
			else if (prefix.equals("TEMP")) {
				// Used for connected columns. Example TEMP.Q_TEMP-BTL is connected to TEMP_BTL. 
				if (!columnValue.equals("")) {
					String[] keyParts = key.split(Pattern.quote("."));
					if (keyParts.length >= 2) {
						currentSample.addTempField(key, columnValue);
					}
				}			
			} else {
				importInfo.addConcatError("Invalid cluster part in key: " + key);
			}		
		} else {
			if (!key.equals("NOT_USED")) {
				importInfo.addConcatWarning("Invalid key: " + key);
			}
		}		
	}

	public void addVariableField(String key, String reportedColumnValue) {
		
		// Don't add or overwrite by empty string.
		if (reportedColumnValue.equals("")) {
			return;
		}
		
		String columnValue = reportedColumnValue;
		if (key.contains(".")) {
			String prefix = key.substring(0, key.indexOf("."));
			
//			// Translate values in 'SHARK_CONFIG/translate_all_columns.txt'.
//			try {
//				String[] parts = key.split(Pattern.quote("."));
//				String key_right = parts[1];
//				columnValue = FileImportTranslateAllColumns.instance().translateValue(key_right, reportedColumnValue);
//				if (!reportedColumnValue.equals(columnValue)) {
//					importInfo.addConcatWarning("Translated value: " + key + " Old: " + reportedColumnValue + " New: " + columnValue);
//				}
//			} catch (Exception e) {
//				System.out.println("DEBUG: Exception when trying to translate value: " + reportedColumnValue);
//			}			
			
			if (prefix.equals("dataset")) {
				// Check if exists and if content differ.
				if (dataset.containsField(key)) {
					String value = dataset.getField(key);
					if (!compareValuesWithZeroInDecimalPart(columnValue, value)) {
						importInfo.addConcatError("Old value is overwritten." + 
								" Key: " + key + " Old: " + value + 
								" New: " + columnValue);
					}
				}
				// Add column value.
				dataset.addField(key, columnValue);
			} 
			else if (prefix.equals("visit"))  {
				// Check if exists and if content differ.
				if (currentVisit.containsField(key)) {
					String value = currentVisit.getField(key);
					if (!compareValuesWithZeroInDecimalPart(columnValue, value)) {
						importInfo.addConcatError("Old value is overwritten." + 
								" Key: " + key + " Old: " + value + 
								" New: " + columnValue);
					}
				}
				// Add column value.
				currentVisit.addField(key, columnValue);
			} 
			else if (prefix.equals("sample"))  {
				// Check if exists and if content differ.
				if (currentSample.containsField(key)) {
					String value = currentSample.getField(key);
					if (!compareValuesWithZeroInDecimalPart(columnValue, value)) {
						importInfo.addConcatError("Old value is overwritten." + 
								" Key: " + key + " Old: " + value + 
								" New: " + columnValue);
					}
				}
				// Add column value.
				currentSample.addField(key, columnValue);
			} 
			else if (prefix.equals("variable"))  {
				if (currentVariable.containsField(key)) {
					String value = currentVariable.getField(key);
					if (!compareValuesWithZeroInDecimalPart(columnValue, value)) {
						importInfo.addConcatError("Old value is overwritten." + 
								" Key: " + key + " Old: " + value + 
								" New: " + columnValue);
					}
				}
				// Add column value.

				if (key.compareTo("variable.species_flag_code") == 0){

					String publicCodeValue = SqliteManager.getInstance().getTranslateCodeColumnValue("species_flag_code", columnValue, "public_value"); //public_value is the data-column name in translate_codes_NEW

					if (publicCodeValue != null &&  publicCodeValue.length()>0)
						columnValue = publicCodeValue;
				}

				currentVariable.addField(key, columnValue);
				
				// Check for QFLAG. Both "variable.QFLAG.Chlorophyll-a" and "TEMP.QFLAG.Aluminium" is allowed. 
				// The "TEMP.QFLAG.Aluminium" alternative i handled below.
				String[] keyParts = key.split(Pattern.quote("."));
				if (keyParts.length >= 2) {
					if (keyParts[1].equals("QFLAG")) {
						String newKey = key.replace("variable.", "TEMP.");
						currentVariable.addTempField(newKey, columnValue);
					}
					if (keyParts[1].equals("SFLAG")) {
						String newKey = key.replace("variable.", "TEMP.");
						currentVariable.addTempField(newKey, columnValue);
					}
				}

			} 
			else if (prefix.equals("TEMP")) {
				// Used for connected columns. Example TEMP.QFLAG.Chlorophyll-a is connected to Chlorophyll-a. 
				if (!columnValue.equals("")) {
					String[] keyParts = key.split(Pattern.quote("."));
					if (keyParts.length >= 2) {
						currentVariable.addTempField(key, columnValue);
					}
				}
			} else {
				importInfo.addConcatError("Invalid cluster part in key: " + key);
			}		
		} else {
			if (!key.equals("NOT_USED")) {
				importInfo.addConcatWarning("Invalid key: " + key);
			}		
		}		
	}

	public boolean compareValuesWithZeroInDecimalPart(String firstValue, String secondValue) {
		// Normal compare. 
		if (firstValue.length() == secondValue.length()) {
			if (firstValue.equals(secondValue)) {
				return true;
			} else {
				return false;
			}
		}
		// Compare with different lengths.
		String first = firstValue;
		String second = secondValue;
		if ((firstValue.endsWith(".00")) || (firstValue.endsWith(":00"))) {
			first = firstValue.replace(".00", "").replace(":00", "");
		}
		if ((secondValue.endsWith(".00")) || (secondValue.endsWith(":00"))) {
			second = secondValue.replace(".00", "").replace(":00", "");
		}
		if (first.equals(second)) {
			return true;
		} else {
			return false;
		}		
	}

}
