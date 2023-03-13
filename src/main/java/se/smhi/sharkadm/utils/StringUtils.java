/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

public class StringUtils {

	public static boolean isNumeric(String value) {
		try {  
			Integer.parseInt(value); 
			return true;  
		}  
		catch( Exception e)  
		{  
			return false;  
		}  
	}

	public static String join(String delimiter, List<String> StringList) {
		// This is a part of Java8, but we also run an older Java version.
		String resultString = ""; 
		String delimiterString = "";
		for (String stringItem : StringList) {
			resultString += delimiterString;
			delimiterString = delimiter;
			resultString += stringItem;
		}
		return resultString;
	}

	public static String convToMd5(String inputString) {

		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(StandardCharsets.UTF_8.encode(inputString));
			return String.format("%032x", new BigInteger(1, md5.digest()));
		} 
		catch (Exception e) { }
		
		return "";
		
		
//		String md5String = "";
//		try {
//			byte[] bytesFromInputString = inputString.getBytes("UTF-8");
//			MessageDigest md = MessageDigest.getInstance("MD5");
//			byte[] md5Bytes = md.digest(bytesFromInputString);
//			md5String = Hex.encodeHexString(resultByte);
//		} 
//		catch (Exception e) { }
//		
//		return md5String;
	}
}
