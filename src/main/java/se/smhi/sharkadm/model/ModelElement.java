/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import se.smhi.sharkadm.utils.ConvUtils;

public abstract class ModelElement {

	// Design pattern: Visitor.
	public abstract void Accept(ModelVisitor visitor);

	// Key/value pairs for imported data.
	private Map<String, String> fieldMap = new HashMap<String, String>();
	
	// Key/value pairs for temporary used data. Normally marked "TEMP" in first column in import matrix. 
	public Map<String, String> tempFieldMap = new HashMap<String, String>();
	
	public Map<String, String> getFieldMap() {
		return fieldMap;
	}

	public Set<String> getFieldKeys() {
		return fieldMap.keySet();
	}

	public String getField(String key) {
		if (!fieldMap.containsKey(key)) {
			return "";
		}
		return fieldMap.get(key);
	}

	public String getFieldDecimalPoint(String key) {
		if (!fieldMap.containsKey(key)) {
			return "";
		}
		return fieldMap.get(key).replace(",", ".").replace(" ", "");
	}

	public String getFieldAsCleanString(String key) {
		if (!fieldMap.containsKey(key)) {
			return "";
		}
		String string = fieldMap.get(key);
		string  = string.replace("\"", "");
		string = string.replace("\n", " ");		
		return string;
	}

	public Double getFieldAsDouble(String key) {
		if (!fieldMap.containsKey(key)) {
			return null;
		}
		Double value;
		try {
			value = ConvUtils.convStringToDouble(fieldMap.get(key));
		} catch (NumberFormatException e) {
			return null;
		}
		return value;
	}

	public Integer getFieldAsInteger(String key) {
		if (!fieldMap.containsKey(key)) {
			return null;
		}
		Integer value;
		try {
			value = ConvUtils.convStringToInteger(fieldMap.get(key));
		} catch (NumberFormatException e) {
			return null;
		}
		return value;
	}

	public void addField(String key, String value) {
		if (!value.equals("")) {
		fieldMap.put(key, value);
		} else {
			if (fieldMap.containsKey(key)) {
				fieldMap.remove(key);
			}
		}
	}

	public void appendToField(String key, String value) {
		// To be used for comments etc. where the new string should be appended.
		if (!value.equals("")) {
			if (fieldMap.containsKey(key)) {
				fieldMap.put(key, fieldMap.get(key) + " " + value);
			} else {
				fieldMap.put(key, value);
			}
		}
	}

	public void removeField(String key) {
		fieldMap.remove(key);
	}

	public boolean containsField(String key) {
		return fieldMap.containsKey(key);
	}

	// Temporary fields. Quality flags etc.
	public void addTempField(String key, String value) {
		if (!value.equals("")) {
			tempFieldMap.put(key, value);
		}
	}

	public Set<String> getTempFieldKeys() {
		return tempFieldMap.keySet();
	}

	public String getTempField(String key) {
		if (!tempFieldMap.containsKey(key)) {
			return "";
		}
		return tempFieldMap.get(key);
	}

}
