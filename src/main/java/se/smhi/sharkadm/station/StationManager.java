/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.station;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import se.smhi.sharkadm.datasets.fileimport.FileImportInfo;
import se.smhi.sharkadm.utils.ConvUtils;
import se.smhi.sharkadm.utils.GeoPosition;

public class StationManager {
	
	private static StationManager instance = new StationManager(); // Singleton.
	
	private Map<String, StationObject> stationNameList = new HashMap<String, StationObject>();
	
	// Contains a list of objects. Added to avoid problems when stations at different 
	// position have same station name.
	private Map<String, List<StationObject>> stationSynonymList = new HashMap<String, List<StationObject>>(); 
	
	private StationManager() { // Singleton.
	}
	
	public static StationManager instance() { // Singleton.
		return instance;
	}
	
	public void clearStationList() {
//		stationIdList.clear();
		stationNameList.clear();
		stationSynonymList.clear();
	}

	public void addStation(StationObject stationObject) {

		// Add to station-id lookup list.
		this.stationNameList.put(cleanUpStationName(stationObject.getStation_name()), stationObject);
		
		// Add to station-synonym-name lookup list. Note: Objects are stored in a list.
//		String[] synonyms = stationObject.getSynonym_names().split(Pattern.quote(","));
		
		String synonymeNames = stationObject.getSynonym_names();
		synonymeNames = synonymeNames.replaceAll("<OR>", "<or>");
		
		String[] synonyms = synonymeNames.split(Pattern.quote("<or>"));
		for (String synonym : synonyms) {
			String cleanSynonym = cleanUpStationName(synonym);
			if (cleanSynonym.length() > 0) {
				if (!stationSynonymList.containsKey(cleanSynonym)) {
					stationSynonymList.put(cleanSynonym, new ArrayList<StationObject>());
				}
				stationSynonymList.get(cleanSynonym).add(stationObject);
			}
		}
	}

	public StationObject getStationObjectByNameAndDistance(String stationName, 
														   GeoPosition visitPosition,
														   FileImportInfo importInfo) {
		Boolean foundInStationList = false;
		Boolean foundInSynonymList = false;
		String errorInfoString = "";

		String cleanStationName = cleanUpStationName(stationName);
		if (this.stationNameList.containsKey(cleanStationName)) {
			foundInStationList = true;
			StationObject stationObject = this.stationNameList.get(cleanStationName);
			// Check distance between station and visit position.
			double distance= stationObject.getStation_position().getDistanceTo(visitPosition);
			if (distance <= ConvUtils.convStringToDouble(stationObject.getMaxValidDistance())) {
				return stationObject;
			} else {
				errorInfoString = "   Distance/valid distance: " + new Double(distance).toString() + " / " +
								  stationObject.getMaxValidDistance();
			}
		}
		// Also check if there is a synonym near the visit position.
		if (this.stationSynonymList.containsKey(cleanStationName)) {
			foundInSynonymList = true;
			for (StationObject synonymObject : this.stationSynonymList.get(cleanStationName)) {
				// Check distance between station and visit position.
				double distance= synonymObject.getStation_position().getDistanceTo(visitPosition);
				if (distance <= ConvUtils.convStringToDouble(synonymObject.getMaxValidDistance())) {
					return synonymObject;
				} else {
					errorInfoString = "   Distance/valid distance: " + new Double(distance).toString() + " / " +
							synonymObject.getMaxValidDistance();
				}
			}
		}
		if (foundInStationList && foundInSynonymList) {
			importInfo.addConcatWarning(
					"Station name and synonym name(s) are found, but visit is 'out of bounds': " + 
					stationName + errorInfoString);
		}
		else if (foundInStationList) {
			importInfo.addConcatWarning(
					"Station name is found, but visit is 'out of bounds': " + stationName + errorInfoString);
		}
		else if (foundInSynonymList) {
			importInfo.addConcatWarning(
				"Synonym station name(s) are found, but visit is 'out of bounds': " + 
				stationName + errorInfoString); 
		}

		return null;
	}

	private String cleanUpStationName(String name) {
		// Convert to lower case and remove spaces.
		return name.toLowerCase().replaceAll(" ", "").trim();
	}

}
