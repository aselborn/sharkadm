/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.columns;

public class ColumnInfoObject {
	
	private String nodeLevel = ""; // Example: "visit".
	private String key = ""; // Example: "visit_year".
	private String fieldFormat = ""; // Example: text".
	private String internalFieldName = null; // Example: "visit.visit_year".
	
	
	public String getNodeLevel() {
		return nodeLevel;
	}
	public void setNodeLevel(String nodeLevel) {
		this.nodeLevel = nodeLevel;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getFieldFormat() {
		return fieldFormat;
	}
	public void setFieldFormat(String fieldFormat) {
		this.fieldFormat = fieldFormat;
	}
	public String getInternalFieldName() {
		return internalFieldName;
	}
	public void setInternalFieldName(String internalFieldName) {
		this.internalFieldName = internalFieldName;
	}
 		
}
