/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.smhi.sharkadm.datasets.fileimport.SharkFolderReader;

public class Dataset extends ModelElement {
	
	String sharkFolderPath;

	private List<Visit> children = new ArrayList<Visit>();
	
	// Note: CamelCase not used to make it easier to cut-n-paste.	
	private int dataset_oid; // Used in database as primary key.
	private String import_status = "ZIP ONLY"; // DB only. Used as default.
		
	public Dataset(String sharkFolderPath) {
		
		this.sharkFolderPath = sharkFolderPath;
		
		SharkFolderReader sharkFolderReader = new SharkFolderReader(sharkFolderPath);
		
		sharkFolderReader.addDatasetInfo(this.getFieldMap());
	}

	public void addVisit(Visit visit) {
		children.add(visit);
		visit.setParent(this);
	}

	public ModelElement getParent() {
		return null;
	}

	public List<Visit> getVisits() {
		return children;
	}

	// Design pattern: Visitor.
	public void Accept(ModelVisitor visitor) {
		visitor.visitDataset(this);
	}

	public int getDataset_oid() {
		return dataset_oid;
	}

	public void setDataset_oid(int dataset_oid) {
		this.dataset_oid = dataset_oid;
	}

	public String getImport_status() {
		return import_status;
	}

	public void setImport_status(String importStatus) {
		this.import_status = importStatus;
	}

	public String getDeliveryDatatypeCode() {
		return this.getField("dataset.delivery_datatype");
	}

	public String getImport_matrix_column() {
		return this.getField("dataset.import_matrix_column");
	}

	public void setImport_matrix_column(String import_matrix_column) {
		this.addField("dataset.import_matrix_column", import_matrix_column);
	}

	public String getImport_format() {
		return this.getField("dataset.import_format");
	}

	public String getReporting_institute_code() {
		return this.getField("dataset.reporting_institute_code");
	}

}
