/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2015 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.location;

import se.smhi.sharkadm.utils.GeodesiSwedishGrids;

public class VisitLocation {
	
	private String locationId = ""; // location_id VARCHAR(31) not null, -- PK. Concatenated lat and long. Format "N60.1234 E15.1234".
	private double latitude; // Unit = DD (Decimal Degree).
	private double longitude; // Unit = DD (Decimal Degree).
	private String latitudeDD = ""; // pos_latitude_dd float,
	private String longitudeDD = ""; // pos_longitude_dd float,

	private double nSweref99TM; // pos_n_sweref99tm varchar(32) not null default '',
	private double eSweref99TM; // pos_e_sweref99tm varchar(32) not null default '',
	private double xRT90; // pos_x_rt90 varchar(32) not null default '',
	private double yRT90; // pos_y_rt90 varchar(32) not null default '',

	private String nation = ""; // location_nation VARCHAR(63) not null default '',
	private String county = ""; // location_county VARCHAR(63) not nul default ''l,
	private String municipality = ""; // location_municipality VARCHAR(63) not null default '',
	private String waterDistrict = ""; // location_water_district VARCHAR(63) not null default '',
	private String svarSeaAreaCode = ""; // location_svar_sea_area_code VARCHAR(63) not null default '',
	private String svarSeaAreaName = ""; // location_svar_sea_area_name VARCHAR(127) not null default '',
	private String waterCategory = ""; // location_water_category VARCHAR(63) not null default '',
	private String typeArea = ""; // location_type_area VARCHAR(127) not null default '',
	private String helcomOsparArea = ""; // location_helcom_ospar_area VARCHAR(63) not null default '',
	private String economicZone = ""; // location_economic_zone VARCHAR(63) not null default '', // Added 2016-04-14
	private String seaBasin = ""; // location_sea_basin VARCHAR(63) not null default '', // Added 2016-04-14
	// private String protectedAreas = ""; // location_protected_areas text not null default '',
	

	public VisitLocation(String key, Double latitude, Double longitude) {
		this.locationId = key; 
		this.latitudeDD = String.format("%.4f", latitude).replace(",", ".");
		this.longitudeDD = String.format("%.4f", longitude).replace(",", ".");
		this.latitude = Double.parseDouble(latitudeDD);
		this.longitude = Double.parseDouble(longitudeDD);

		GeodesiSwedishGrids sweref99tm = new GeodesiSwedishGrids();
		double[] n_e = sweref99tm.geodetic_to_grid(latitude, longitude);
		this.nSweref99TM = n_e[0];
		this.eSweref99TM = n_e[1];
		
		GeodesiSwedishGrids rt90 = new GeodesiSwedishGrids("rt90_2.5_gon_v");
		double[] x_y = rt90.geodetic_to_grid(this.latitude, this.longitude);
		this.xRT90 = x_y[0];
		this.yRT90 = x_y[1];
	}	

	public String getRt90WktPointString() {
		return "POINT (" + this.yRT90 + " " + this.xRT90 + ")"; // Note order: long/lat.
	}

	public String getSweref99TmWktPointString() {
		return "POINT (" + this.eSweref99TM + " " + this.nSweref99TM + ")"; // Note order: long/lat.
	}

	public String getGrs80LatLongWktPointString() {
		return "POINT (" + this.longitude + " " + this.latitude + ")"; // Note order: long/lat.
	}

	public String getLocationId() {
		return locationId;
	}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	public String getWaterCategory() {
		return waterCategory;
	}

	public void setWaterCategory(String waterCategoryType) {
		this.waterCategory = waterCategoryType;
	}

	public String getNation() {
		return nation;
	}

	public void setNationName(String nation) {
		this.nation = nation;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public String getMunicipality() {
		return municipality;
	}

	public void setMunicipality(String municipality) {
		this.municipality = municipality;
	}

	public String getWaterDistrict() {
		return waterDistrict;
	}

	public void setWaterDistrict(String waterDistrict) {
		this.waterDistrict = waterDistrict;
	}

	public String getSvarSeaAreaCode() {
		return svarSeaAreaCode;
	}

	public void setSvarSeaAreaCode(String svarSeaAreaCode) {
		this.svarSeaAreaCode = svarSeaAreaCode;
	}

	public String getSvarSeaAreaName() {
		return svarSeaAreaName;
	}

	public void setSvarSeaAreaName(String svarSeaAreaName) {
		this.svarSeaAreaName = svarSeaAreaName;
	}

	public String getTypeArea() {
		return typeArea;
	}

	public void setTypeArea(String typeArea) {
		this.typeArea = typeArea;
	}

	public String getHelcomOsparArea() {
		return helcomOsparArea;
	}

	public void setHelcomOsparArea(String helcomOsparArea) {
		this.helcomOsparArea = helcomOsparArea;
	}

	public String getEconomicZone() {
		return economicZone;
	}

	public void setEconomicZone(String economicZone) {
		this.economicZone = economicZone;
	}

	public String getSeaBasin() {
		return seaBasin;
	}

	public void setSeaBasin(String seaBasin) {
		this.seaBasin = seaBasin;
	}

}
