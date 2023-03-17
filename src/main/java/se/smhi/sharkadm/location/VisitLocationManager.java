/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2015 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.location;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.smhi.sharkadm.database.SaveVisitLocations;
import se.smhi.sharkadm.utils.ErrorLogger;

public class VisitLocationManager {

	private static VisitLocationManager instance = new VisitLocationManager(); // Singleton.
	
	private Map<String, VisitLocation> visitLocationMap = new HashMap<String, VisitLocation>(); // Key and object.
	
	private VisitLocationManager() { // Singleton.
	}
	
	public static VisitLocationManager instance() { // Singleton.
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
		visitLocationMap.put(key, new VisitLocation(key, latitude, longitude));
	}

	public Map<String, VisitLocation> getVisitLocationMap() {
		return visitLocationMap;
	}

	public void addVisitLocationObjectsToMap(Map<VisitLocation, String> objectAndResultMap) {
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
		
		List<VisitLocation> visitLocations = new ArrayList<VisitLocation>();
		for (VisitLocation visitLocation : VisitLocationManager.instance().getVisitLocationMap().values()) {
			visitLocations.add(visitLocation);
		}
		
		// - Län
		// - Kommuner
		shapefileName = "KOMMUNER_LAN.shp";		
		new ShapeFileReader().addPropertyValuesToVisitLocations(shapefilePath, shapefileName, visitLocations);		
				
		// - Vattendistrikt enligt Svenskt VattenARkiv (SVAR) 
		// - Områdestyp(er) enligt Svenskt Vattenarkiv (SVAR) 
		shapefileName = "havsomr_y_2012_2.shp";		
		new ShapeFileReader().addPropertyValuesToVisitLocations(shapefilePath, shapefileName, visitLocations);		
				
		// - Typområde(n) enligt Havsdirektivet 
		// - Vattenförekomst(er) enligt Svenskt VattenARkiv (SVAR) 
		shapefileName = "havdirtyper_2012_delatKattegatt.shp";		
		new ShapeFileReader().addPropertyValuesToVisitLocations(shapefilePath, shapefileName, visitLocations);		
				
		// - HELCOM/OSPAR-områden
		shapefileName = "KONVENTION.shp";		
		new ShapeFileReader().addPropertyValuesToVisitLocations(shapefilePath, shapefileName, visitLocations);		
		
		// - Svensk ekonomisk zon (EEZ)
		// - Havsbassänger enligt Havsmiljödirektivet (MSFD)
		shapefileName = "MSFD_areas.shp";		
//		shapefileName = "MSFD_areas_TM.shp";		
		new ShapeFileReader().addPropertyValuesToVisitLocations(shapefilePath, shapefileName, visitLocations);		
	}

	public void saveVisitLocationsToDatabase() {
		
		this.calculateContent();
		
		for (String stationId : this.visitLocationMap.keySet()) {
			SaveVisitLocations.instance().insertVisitLocations(this.visitLocationMap.get(stationId));
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
			String idString = VisitLocationManager.calculateKey(latitude, longitude);
			if (!locationIds.contains(idString)) {
				addVisitLocation(latitude, longitude);
			}
		}
		
		// Calculate content based on Shape files.
		VisitLocationManager.instance().readDataFromShapefiles();
	
	}
}
