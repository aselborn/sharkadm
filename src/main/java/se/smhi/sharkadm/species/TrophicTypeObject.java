/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.species;

public class TrophicTypeObject {
	
	private String scientificName = "";
	private String sizeClass = "";
	private String trophicType = "";
	
	public String getScientificName() {
		return scientificName;
	}
	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}
	public String getSizeClass() {
		return sizeClass;
	}
	public void setSizeClass(String sizeClass) {
		this.sizeClass = sizeClass;
	}
	public String getTrophicType() {
		return trophicType;
	}
	public void setTrophicType(String trophicType) {
		this.trophicType = trophicType;
	}

}
