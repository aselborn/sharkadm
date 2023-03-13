/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class ConvUtils {
	
	// Static methods for string conversions. 

	// Format double float to string and avoid too many decimals.
	public static String convDoubleToString(Double value) {
		// TEST:		
		// value = 12345678.00000123;
		// value = 123.45678000;
		// value = 0.0000000000000000000000000001234567890;

		String stringValue;
		
		if (value == 0.0) {
			// One decimal to indicate float.
			stringValue = "0.0";
		}
		else if (value > 10000.0) {
			// Unlimited number of significant digits. One decimal to indicate float. 
			stringValue = String.format("%.1f", value);
		} else {
			// Limit to 6 significant digits.
			BigDecimal bd = new BigDecimal(Double.toString(value));
			bd = bd.round(new MathContext(6, RoundingMode.HALF_EVEN));
			stringValue = bd.stripTrailingZeros().toPlainString();
			if (!stringValue.contains(".")) {
				// One decimal to indicate float.
				stringValue = stringValue + ".0";
			}
		}
		return stringValue.replace(",", ".");
	}
	
	// Converts from decimal comma to decimal point.
	public static String convDecimal(String string) {
		String value = string.trim().replace(",", ".");
		return value.replace(" ", ""); // Remove 1000-delimiter.
	}
	
	// Removes the decimal part.
	public static String convNoDecimal(String string) {
		String value = string.trim().replace(",", ".");
		if (value.indexOf(".") != -1) {
			return value.substring(0, value.indexOf("."));
		} else {
			return value.replace(" ", ""); // Remove 1000-delimiter.
		}
	}
	
	// Return value with one decimal. // TODO: Replace by format and variable number of decimals.
	public static String convOneDecimal(String string) {
		String value = string.trim().replace(",", ".");
		if ((value.indexOf(".") != -1) && (value.length() >= (value.indexOf(".") + 1))){
			return value.substring(0, value.indexOf(".") + 2);
		} else {
			return value;
		}
	}
	
	// Return value with one decimal. // TODO: Replace by format and variable number of decimals.
	public static String convToNumberString(String string) {
		String value = string.trim().replace(",", ".");
		Double number = Double.parseDouble(value);
		return number.toString();
	}

	// From string to double. Removes spaces and replaces decimal delimiter.
	public static Double convStringToDouble(String string) {
		String value = string.trim().replace(",", ".").replaceAll(" ", "");
		try {
			return Double.parseDouble(value);
		} catch (Exception e) {
			return null;
		}
	}

	// From string to integer. Removes spaces and replaces decimal delimiter.
	public static Integer convStringToInteger(String string) {
		try {
			return Integer.parseInt(string);
		} catch (Exception e) {
			return null;
		}
	}

}
