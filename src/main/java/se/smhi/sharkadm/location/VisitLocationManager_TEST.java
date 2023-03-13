/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.location;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.smhi.sharkadm.database.SaveVisitLocations;
import se.smhi.sharkadm.database.SaveVisitLocations_TEST;
import se.smhi.sharkadm.utils.ErrorLogger;

public class VisitLocationManager_TEST {

	private static VisitLocationManager_TEST instance = new VisitLocationManager_TEST(); // Singleton.
	
	private Map<String, VisitLocation_TEST> visitLocationMap = new HashMap<String, VisitLocation_TEST>(); // Key and object.
	
	private VisitLocationManager_TEST() { // Singleton.
	}
	
	public static VisitLocationManager_TEST instance() { // Singleton.
		return instance;
	}

	public static String calculateKey(Double latitude, Double longitude) {
		// Key format example "N55.1234 E15.1234".
		String latStr = String.format("%.4f", Math.abs(latitude)).replace(",", ".");
		String longStr = String.format("%.4f", Math.abs(longitude)).replace(",", ".");
		
		String latDirection = "N";
		String longDirection = "E";
		if (latitude < 0.0) { latDirection = "S"; }
		if (longitude < 0.0) { 
			longDirection = "W"; 
		}
		
//		if (latStr.startsWith("57.914")) {
//			System.out.println("DEBUG");
//		}
		
		
		
		return latDirection + latStr + " " + longDirection + longStr;		
	}
	
	public void addVisitLocation(Double latitude, Double longitude) {
		// Calculate key.
		String key = calculateKey(latitude, longitude);
		
		// Check if key already exists. Exit if it does.
		if (visitLocationMap.containsKey(key)) {
			return;
		}
		visitLocationMap.put(key, new VisitLocation_TEST(key, latitude, longitude));
	}

	public Map<String, VisitLocation_TEST> getVisitLocationMap() {
		return visitLocationMap;
	}

	public void addVisitLocationObjectsToMap(Map<VisitLocation_TEST, String> objectAndResultMap) {
		for (String key : visitLocationMap.keySet()) {
			objectAndResultMap.put(visitLocationMap.get(key), "");
		}
	}
	
	public void readDataFromShapefiles() {
		
		// Find path to the directory containing shapefiles. 
		String shapefilePath = "";
		String shapefileName = "";
		try {
			// Checks if file exist outside jar bundle. Current directory.
			File external_file = new File("TEST_SHARK_CONFIG\\sharkweb_shapefiles");
			if (external_file.exists()) {
				shapefilePath = "TEST_SHARK_CONFIG\\sharkweb_shapefiles\\";
				System.out.println("\nNOTE: LOCAL COPY OF TEST_SHARK_CONFIG\\sharkweb_shapefiles");
			} else {
				// Checks if file exist outside jar bundle. File service at SMHI.
				external_file = new File("\\\\winfs\\data\\prodkap\\sharkweb\\SHARK_CONFIG\\sharkweb_shapefiles");
				if (external_file.exists()) {
					shapefilePath = "\\\\winfs\\data\\prodkap\\sharkweb\\SHARK_CONFIG\\sharkweb_shapefiles\\";
				} else {
					// File is bundled in jar.
					// Not implemented, maybe in the future, if needed...
				}
			}			
		} catch (Exception e) {
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			System.out.println("Failed to find the directory SHARK_CONFIG\\sharkweb_shapefiles");
		}

		ErrorLogger.println("Reading from Shapefiles. Path: " + shapefilePath);
		
		List<VisitLocation_TEST> visitLocations = new ArrayList<VisitLocation_TEST>();
		for (VisitLocation_TEST visitLocation : VisitLocationManager_TEST.instance().getVisitLocationMap().values()) {
			visitLocations.add(visitLocation);
		}
		
		shapefileName = "an_riks.shp";		
		new ShapeFileReader_TEST().addPropertyValuesToVisitLocations(shapefilePath, shapefileName, visitLocations);		
		shapefileName = "ak_riks.shp";		
		new ShapeFileReader_TEST().addPropertyValuesToVisitLocations(shapefilePath, shapefileName, visitLocations);		
//		shapefileName = "Havsomr_SVAR_2016_3b.shp";		
//		shapefileName = "Havsomr_SVAR_2016_3b_CP1252.shp";		
		shapefileName = "Havsomr_SVAR_2016_3c_CP1252.shp";		
		new ShapeFileReader_TEST().addPropertyValuesToVisitLocations(shapefilePath, shapefileName, visitLocations);	
		
		
//		shapefileName = "havsomr_y_2012_2.shp";		
//		new ShapeFileReader_TEST().addPropertyValuesToVisitLocations(shapefilePath, shapefileName, visitLocations);	
		
		
		shapefileName = "KONVENTION.shp";
		new ShapeFileReader_TEST().addPropertyValuesToVisitLocations(shapefilePath, shapefileName, visitLocations);		
		
	}

	public void saveVisitLocationsToDatabase() {
		
		this.calculateContent();
		
		for (String stationId : this.visitLocationMap.keySet()) {
			SaveVisitLocations_TEST.instance().insertVisitLocations(this.visitLocationMap.get(stationId));
//			SaveVisitLocations.instance().insertProtectedArea(this.visitLocationMap.get(stationId), "");
		}
	}

	public void removeVisitLocationsInDatabase() {
		SaveVisitLocations.instance().deleteVisitLocations();
	}
	
	public void calculateContent() {
		
		List<String> locationIds = new ArrayList<String>();
		List<float[]> positions = new ArrayList<float[]>();

		visitLocationMap.clear();
		
		// Get all id's for visit location stored in database.
		SaveVisitLocations.instance().getAllVisitLocationIds(locationIds);
		
		// Get all unique position for visits stored in database.
		SaveVisitLocations.instance().getAllUniqueVisitPositions(positions);
		
		System.out.println("DEBUG: VisitLocation in db: " + locationIds.size());
		System.out.println("DEBUG: UniqueVisitPositions in db: " + positions.size());

		
		// Match. If not available, store in "visitLocationMap".
		for (float[] position: positions) {
			Double latitude = (double) position[0];
			Double longitude = (double) position[1];
			String idString = VisitLocationManager_TEST.calculateKey(latitude, longitude);
			if (!locationIds.contains(idString)) {
				addVisitLocation(latitude, longitude);
			}
		}
		
		// Calculate content based on Shape files.
		VisitLocationManager_TEST.instance().readDataFromShapefiles();
	
	}
}
