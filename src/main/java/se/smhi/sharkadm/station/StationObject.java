/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.station;

import se.smhi.sharkadm.utils.GeoPosition;

public class StationObject {
	
	// CamelCase not used if stored in db. Cut-n-paste.
	
	
	private String station_id = "";
	private String sample_location_id = "";
	
	
	private String station_name = "";
	private String synonym_names = "";
	private String viss_eu_id = "";
	private GeoPosition station_position = null;
	private String maxValidDistance = "";
 		
	public String getStation_id() {
		return station_id;
	}
	public void setStation_id(String station_id) {
		this.station_id = station_id;
	}
	public String getSample_location_id() {
		return sample_location_id;
	}
	public void setSample_location_id(String sample_location_id) {
		this.sample_location_id = sample_location_id;
	}
	
	
	public String getStation_name() {
		return station_name;
	}
	public void setStation_name(String station_name) {
		this.station_name = station_name;
	}
	public String getSynonym_names() {
		return synonym_names;
	}
	public void setSynonym_names(String synonym_names) {
		this.synonym_names = synonym_names;
	}
	public String getViss_eu_id() {
		return viss_eu_id;
	}
	public void setViss_eu_id(String viss_eu_id) {
		this.viss_eu_id = viss_eu_id;
	}
	public GeoPosition getStation_position() {
		return station_position;
	}
	public void setStation_position(GeoPosition station_position) {
		this.station_position = station_position;
	}
	public String getMaxValidDistance() {
		return this.maxValidDistance;
	}
	public void setMaxValidDistance(String maxValidDistance) {
		this.maxValidDistance = maxValidDistance;
	}

}
