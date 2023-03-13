/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.model;

import java.util.ArrayList;
import java.util.List;

import se.smhi.sharkadm.utils.GeoPosition;

public class Sample extends ModelElement {

	private Visit parent;
	private List<Variable> children = new ArrayList<Variable>();

	private int sample_oid; // Used in database as primary key.
	private GeoPosition sample_position;

	private String fileImportKeyString; // Used as id when loading from file.
	
	public Sample() {
	}

	// Used to match visits during import.
	public Sample(String fileImportKeyString) {
		this.fileImportKeyString = fileImportKeyString;
	}

	public String getFileImportKeyString() {
		return fileImportKeyString;
	}

	public void addVariable(Variable variable) {
		children.add(variable);
		variable.setParent(this);
	}
	
	public void removeVariable(Variable variable) {
		children.remove(variable);
	}

	public List<Variable> getVariables() {
		return children;
	}

	public void setParent(Visit samplingEvent) {
		this.parent = samplingEvent;
	}

	public Visit getParent() {
		return parent;
	}

	// Design pattern: Visitor.
	public void Accept(ModelVisitor visitor) {
		visitor.visitSample(this);
	}

	public GeoPosition getPosition() {
		if (sample_position == null) {
			return new GeoPosition(0, 0);			
		}
		return sample_position;
	}

	public void setPosition(GeoPosition position) {
		this.sample_position = position;

		this.addField("sample.sample_latitude_dd", this.getPosition().getLatitudeAsString());
		this.addField("sample.sample_longitude_dd", this.getPosition().getLongitudeAsString());
		this.addField("sample.sample_latitude_dm", GeoPosition.convertToDM(this.getPosition().getLatitude()));
		this.addField("sample.sample_longitude_dm", GeoPosition.convertToDM(this.getPosition().getLongitude()));
	}

	public int getSample_oid() {
		return sample_oid;
	}

	public void setSample_oid(int sample_oid) {
		this.sample_oid = sample_oid;
	}

	public boolean containsTempField(String key) {
		if (tempFieldMap.containsKey(key)) {
			return true;
		}
		else if (parent.tempFieldMap.containsKey(key)) {
			return true;			
		}
		return false;
	}

	public String getTempField(String key) {
		if (tempFieldMap.containsKey(key)) {
			return tempFieldMap.get(key);
		}
		else if (parent.tempFieldMap.containsKey(key)) {
			return parent.tempFieldMap.get(key);			
		}
		return "";
	}
	
	public void removeTempField(String key) {
		if (tempFieldMap.containsKey(key)) {
			tempFieldMap.remove(key);
		}
		else if (parent.tempFieldMap.containsKey(key)) {
			parent.tempFieldMap.remove(key);			
		}		
	}

}
