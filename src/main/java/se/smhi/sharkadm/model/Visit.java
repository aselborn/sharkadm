/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.model;

import java.util.ArrayList;
import java.util.List;

import se.smhi.sharkadm.station.StationObject;
import se.smhi.sharkadm.utils.GeoPosition;

/**
 *
 */
public class Visit extends ModelElement {

	private Dataset parent;
	private List<Sample> children = new ArrayList<Sample>();

	// Note: CamelCase not used to make it easier to cut-n-paste.	
	private int visit_oid; // Used in database as primary key.
	private StationObject stationObject = null; 
	private GeoPosition visit_position = null;
	
	private String fileImportKeyString; // Used as id when loading from file.

	public Visit() {
	}

	// Used to match visits during import.
	public Visit(String fileImportKeyString) {
		this.fileImportKeyString = fileImportKeyString;
	}

	public String getFileImportKeyString() {
		return fileImportKeyString;
	}

	public void addSample(Sample sample) {
		children.add(sample);
		sample.setParent(this);
	}

	public void removeSample(Sample sample) {
		children.remove(sample);
	}

	public List<Sample> getSamples() {
		return children;
	}

	public void setParent(Dataset dataset) {
		this.parent = dataset;
	}

	public Dataset getParent() {
		return parent;
	}

	// Design pattern: Visitor.
	public void Accept(ModelVisitor visitor) {
		visitor.visitVisit(this);
	}

	public GeoPosition getPosition() {
		if (visit_position == null) {
//			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
//			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
//			messageBox.setText("Error in Visit:getLatLong");
//			messageBox.setMessage("Error: " + "No latitude/logitude position available. Date: " + visit_date + ".");
//			messageBox.open();
			return new GeoPosition(0, 0);			
		}
		return visit_position;
	}

	public void setPosition(GeoPosition position) {
		this.visit_position = position;

		this.addField("visit.visit_latitude_dd", this.getPosition().getLatitudeAsString());
		this.addField("visit.visit_longitude_dd", this.getPosition().getLongitudeAsString());
		this.addField("visit.visit_latitude_dm", GeoPosition.convertToDM(this.getPosition().getLatitude()));
		this.addField("visit.visit_longitude_dm", GeoPosition.convertToDM(this.getPosition().getLongitude()));
	}

	public int getVisit_oid() {
		return visit_oid;
	}

	public void setVisit_oid(int visit_oid) {
		this.visit_oid = visit_oid;
	}

	public StationObject getStationObject() {
		return stationObject;
	}

	public void setStationObject(StationObject stationObject) {
		this.stationObject = stationObject;
	}

	public GeoPosition getVisit_position() {
		return visit_position;
	}

	public void setVisit_position(GeoPosition visit_position) {
		this.visit_position = visit_position;
	}

	public boolean containsTempField(String key) {
		return tempFieldMap.containsKey(key);
	}

	public String getTempField(String key) {
		if (tempFieldMap.containsKey(key)) {
			return tempFieldMap.get(key);
		}
		return "";
	}

}
