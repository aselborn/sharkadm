/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.fileimport;

import java.text.DecimalFormat;

import se.smhi.sharkadm.utils.ConvUtils;

public class FileImportUtils {

	private  FileImportInfo importInfo;
	
	public FileImportUtils(FileImportInfo importInfo) {
		this.importInfo = importInfo;
	}

	// Converts from decimal comma to decimal point.
	public String convDecimal(String string) {
		String value = string.trim().replace(",", ".");
		return value.replace(" ", ""); // Remove 1000-delimiter.
	}
	
	// Removes the decimal part.
	public String convNoDecimal(String string) {
		String value = string.trim().replace(",", ".");
		if (value.indexOf(".") != -1) {
			return value.substring(0, value.indexOf("."));
		} else {
			return value.replace(" ", ""); // Remove 1000-delimiter.
		}
	}
	
	// Return value with one decimal. // TODO: Replace by format and variable number of decimals.
	public String convOneDecimal(String string) {
		String value = string.trim().replace(",", ".");
		if ((value.indexOf(".") != -1) && (value.length() >= (value.indexOf(".") + 1))){
			return value.substring(0, value.indexOf(".") + 2);
		} else {
			return value;
		}
	}
	
	// Return value with one decimal. // TODO: Replace by format and variable number of decimals.
	public String convToNumberString(String string) {
		String value = string.trim().replace(",", ".");
		try {
			Double number = ConvUtils.convStringToDouble(value);
			return number.toString();
		} catch (Exception e) {
			return value;
		}
	}
	
	// Converts date to YYYY-MM-DD format (if not ISO format).
	public String convDate(String string) {
		String value = string.trim();
		value = convNoDecimal(value);
		
		// Don't change if ISO-format.
		if (value.contains("-")) {
			
			
			// Change common problems when day not known. YYYY-MM is valid ISO.
			if (value.length() == 10) {
				if (value.substring(4, 10).equals("-00-00")) {
					return value.substring(0, 4); // Only YYYY.
				}
				if (value.substring(7, 10).equals("-00")) {
					return value.substring(0, 7); // Only YYYY-MM.
				}
				if (value.substring(0, 10).equals("-DD")) {
					return value.substring(0, 7); // Only YYYY-MM.
				}
			}
			
			
			if (value.length() > 10) {
				return value.substring(0, 10); // Only date part.
			} else {
				return value;
			}
		}
		
		// If "20000101" convert to "2000-01-01".	
		if (value.equals("")) {
			return "";
		} else if (value.length() >= 8) {
			return value.substring(0, 4) + "-" + value.substring(4, 6) + "-" + value.substring(6, 8);
		} else {
			importInfo.addConcatWarning("Can't convert date from: " + value + ".");
			return "";
		}
	}
	
	// Converts time to HH:MM:SS format (if not ISO format).
	public String convTime(String string) {
		
		if (string.equals("95")) {
			System.out.println("DEBUG.");
		}
		
		
		
		try {
			String value = string.trim();
			value = convNoDecimal(value);
			
			// Don't change if ISO-format.
			if (value.contains(":")) {
				return value;
			}
			
			Integer integerValue;
			
			// Don't change if not pure integer value.
			try {
				integerValue = new Integer(value);
			}
			catch (Exception e) {
				importInfo.addConcatWarning("Can't convert time from: "+ value + ".");
				return string;
			}
			
			// Divide into hours and minutes.
			DecimalFormat df = new DecimalFormat("0000");
			String newString = df.format(integerValue);
			
			return newString.substring(0, 2) + ":" + newString.substring(2, 4);
			
//			// If "45" or "945" or "1245" convert to "9:45:00" or "12:45:00".	
//			if (value.equals("")) {
//				return "";
//			} 
//			else if (value.length() < 3) { 
//				// Only minutes.
//				if (Integer.parseInt(value) > 59) {
//					importInfo.addConcatWarning("Can't convert time from: "+ value + ".");
//					return "";
//				}
//				return "00:" + value + ":00";
//			} 
//			else if (value.length() == 3) {
//				if (Integer.parseInt(value.substring(1, 3)) > 59) {
//					importInfo.addConcatWarning("Can't convert time from: "+ value + ".");
//					return "";
//				}
//				return value.substring(0, 1) + ":" + value.substring(1, 3) + ":00";
//			} 
//			else if (value.length() == 4) {
//				if (Integer.parseInt(value.substring(0, 2)) > 23) {
//					importInfo.addConcatWarning("Can't convert time from: "+ value + ".");
//					return "";
//				}
//				if (Integer.parseInt(value.substring(2, 4)) > 59) {
//					importInfo.addConcatWarning("Can't convert time from: "+ value + ".");
//					return "";
//				}
//				return value.substring(0, 2) + ":" + value.substring(2, 4) + ":00";
//			} 
//			else {
//				importInfo.addConcatWarning("Can't convert time from: "+ value + ".");
//				return "";
//			}
		}
		catch (Exception e) {
			return string;
		} 
	}
	
	// Converts time to decimal hour.
	public String convTimeHour(String valueString) {
		try {
//			System.out.println("DEBUG: convTimeHour" + valueString);
			// Already correct format.
			if (valueString.contains(".")) {
				return valueString;
			}
			// Convert decimal to international decimal.
			if (valueString.contains(",")) {
				return valueString.replace(",", ".");
			}
			// As time format.
			if (valueString.contains(":")) {
				String[] parts = valueString.split(":");
				String part1 = parts[0];
				String part2 = parts[1];
				double hours = Float.parseFloat(part1);
				double minutes = Float.parseFloat(part2);
				double decimalHour = hours + minutes / 60;
				decimalHour = Math.round(decimalHour * 100.0) / 100.0;
				return Double.toString(decimalHour);
			}
			// Other alternatives unchanged.
			return valueString;
		}
		catch (Exception e) {
			return valueString;
		} 
	}
	
	// Converts time to hh:mm:ss if reported as mm:ss.
	public String convTimeMinSec(String valueString) {
		try {
			
			String[] parts = valueString.split(":");
			if (parts.length == 3) {
				// Ok format.
				return valueString;
			}
			if (parts.length == 2) {
				// Hour part probably missing.
				DecimalFormat df = new DecimalFormat("00");
				Integer minInteger = Integer.parseInt(parts[0]);
				Integer secInteger = Integer.parseInt(parts[1]);
				String minPart = df.format(minInteger);
				String secPart = df.format(secInteger);
				return "00:" + minPart + ":" + secPart;
			}
			// Return original if no match.
			return valueString;
		}
		catch (Exception e) {
			return valueString;
		} 
	}
	
}
