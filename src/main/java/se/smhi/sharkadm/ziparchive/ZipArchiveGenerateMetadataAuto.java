/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.ziparchive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.ModelVisitor;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;

public class ZipArchiveGenerateMetadataAuto extends ModelVisitor {

    private Integer min_year = null;
    private Integer max_year = null;
    private String min_date = null;
    private String max_date = null;
    private Double min_longitude = null;
    private Double max_longitude = null;
    private Double min_latitude = null;
    private Double max_latitude = null;
    private Map<String, LocalParameterUnitClass> parameterUnitList = new HashMap<String, LocalParameterUnitClass>();
    // Public result:
    List<String> metadataFileContent = new ArrayList<String>();
	
	@Override
	public void visitDataset(Dataset dataset) {
		// Clear all.
		metadataFileContent.clear();

		for (Visit visit : dataset.getVisits()) {
			visit.Accept(this);
		}
		
		// Example:
		// dataset_name: SHARK_Speciesobs_1992-1995_Phytobentos
		// dataset_version: TEST2014-03-06
		// dataset_category: Speciesobs
		// dataset_file_name: SHARK_Speciesobs_1992-1995_Phytobentos_version_TEST2014-03-06.zip
		String datasetName = dataset.getField("dataset.dataset_name");
		String datasetFileName = dataset.getField("dataset.dataset_file_name");
		String datasetVersion = "";
		String datasetCategory = "";
		String[] datasetFileNameParts = datasetFileName.split(Pattern.quote("_"));
		if (datasetFileNameParts.length > 2) { 
			datasetCategory = datasetFileNameParts[1].trim(); 
		}
		if (datasetFileName.contains("_version_")) { 
			datasetVersion = datasetFileNameParts[datasetFileNameParts.length - 1].trim();
			datasetVersion = datasetVersion.replace(".zip", "");
		}
		metadataFileContent.add("dataset_name: " + datasetName);
		metadataFileContent.add("dataset_category: " + datasetCategory);
		metadataFileContent.add("dataset_version: " + datasetVersion);
		metadataFileContent.add("dataset_file_name: " + dataset.getField("dataset.dataset_file_name"));

		if (min_year != null) {
			metadataFileContent.add("min_year: " + min_year.toString());
		}
		if (max_year != null) {
			metadataFileContent.add("max_year: " + max_year.toString());
		}
		if (min_date != null) {
			metadataFileContent.add("min_date: " + min_date.toString());
		}
		if (max_date != null) {
			metadataFileContent.add("max_date: " + max_date.toString());
		}
		if (min_longitude != null) {
			metadataFileContent.add("min_longitude: " + min_longitude.toString());
		}
		if (max_longitude != null) {
			metadataFileContent.add("max_longitude: " + max_longitude.toString());
		}
		if (min_latitude != null) {
			metadataFileContent.add("min_latitude: " + min_latitude.toString());
		}
		if (max_latitude != null) {
			metadataFileContent.add("max_latitude: " + max_latitude.toString());
		}
		Integer index = 0;
		for (LocalParameterUnitClass paramUnitObject : parameterUnitList.values()) {
			metadataFileContent.add("parameters#" + index.toString() + ".parameter: " + paramUnitObject.parameter);
			metadataFileContent.add("parameters#" + index.toString() + ".unit: " + paramUnitObject.unit);
			index += 1;
		}
		
	}

	@Override
	public void visitVisit(Visit visit) {

		Integer year = visit.getFieldAsInteger("visit.visit_year");
		String date = visit.getField("visit.visit_date");
		Double latitude = visit.getPosition().getLatitude();
		Double longitude = visit.getPosition().getLongitude();
		
		if (year != null) {
			if ((min_year == null) || (min_year > year)) {
				min_year = year;
			}
			if ((max_year == null) || (max_year < year)) {
				max_year = year;
			}
		}
		if (date != null) {
			if ((min_date == null) || (min_date.compareTo(date) > 0)) {
				min_date = date;
			}
			if ((max_date == null) || (max_date.compareTo(date) < 0)) {
				max_date = date;
			}
		}
		
		for (Sample sample : visit.getSamples()) {
			sample.Accept(this);
		}
		if (latitude != null) {
			if ((min_latitude == null) || (min_latitude > latitude)) {
				min_latitude = latitude;
			}
			if ((max_latitude == null) || (max_latitude < latitude)) {
				max_latitude = latitude;
			}
		}
		if (longitude != null) {
			if ((min_longitude == null) || (min_longitude > longitude)) {
				min_longitude = longitude;
			}
			if ((max_longitude == null) || (min_longitude < longitude)) {
				max_longitude = longitude;
			}
		}

	}

	@Override
	public void visitSample(Sample sample) {
		for (Variable variable : sample.getVariables()) {
			variable.Accept(this);
		}
	}

	@Override
	public void visitVariable(Variable variable) {

		String parameter = variable.getParameter();
		String unit = variable.getUnit();
		String paramUnitKey = parameter + ":" + unit; 
		if (!parameterUnitList.containsKey(paramUnitKey)) {
			parameterUnitList.put(paramUnitKey, new LocalParameterUnitClass(parameter, unit));
		}
	}
	
	// Local classes.
	class LocalParameterUnitClass {
		String parameter = "";
    	String unit = "";
    	public LocalParameterUnitClass(String parameter, String unit) {
			super();
			this.parameter = parameter;
			this.unit = unit;
		}
    }
 
//	class LocalStationClass {
//		String stationName = "";
//    	String stationId = "";
//    	public LocalStationClass(String stationName, String stationId) {
//			super();
//			this.stationName = stationName;
//			this.stationId = stationId;
//		}
//    }
 
}
