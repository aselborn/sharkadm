/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.species;

public class BvolObject {
	
	private String referenceList = "";
	private String scientificName = "";
	private String aphiaId = "";
	private String sizeClass = "";
	private String trophicType = "";
	private String calculatedVolume = "";
	private String calculatedCarbon = "";
	
	public String getReferenceList() {
		return referenceList;
	}
	public void setReferenceList(String referenceList) {
		this.referenceList = referenceList;
	}
	public String getScientificName() {
		return scientificName;
	}
	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}
	public String getAphiaId() {
		return aphiaId;
	}
	public void setAphiaId(String aphiaId) {
		this.aphiaId = aphiaId;
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
	public String getCalculatedVolume() {
		return calculatedVolume;
	}
	public void setCalculatedVolume(String calculatedVolume) {
		this.calculatedVolume = calculatedVolume;
	}
	public String getCalculatedCarbon() {
		return calculatedCarbon;
	}
	public void setCalculatedCarbon(String calculatedCarbon) {
		this.calculatedCarbon = calculatedCarbon;
	}
}
