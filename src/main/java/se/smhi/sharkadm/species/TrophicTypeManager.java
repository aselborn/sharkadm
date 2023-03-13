/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.species;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrophicTypeManager {
	
	private static TrophicTypeManager instance = new TrophicTypeManager(); // Singleton.
	
	private List<TrophicTypeObject> trophicTypeList = new ArrayList<TrophicTypeObject>(); 
	private Map<String, TrophicTypeObject> trophicTypeLookup = new HashMap<String, TrophicTypeObject>();
	
	private TrophicTypeManager() { // Singleton.
	}
	
	public static TrophicTypeManager instance() { // Singleton.
		return instance;
	}
	
	public void clearLists() {
		this.trophicTypeList.clear();
		this.trophicTypeLookup.clear();
	}

	public void addTrophicType(TrophicTypeObject TrophicTypeObject) {
		this.trophicTypeList.add(TrophicTypeObject);
	}

	public String getTrophicType(String scientificName, String sizeClass) {
		if ((trophicTypeList.size() > 0) && (trophicTypeLookup.size() == 0)) {
			this.generateTrophicTypeLookup();
		}
		String key = scientificName + "<:>" + sizeClass;
		if (trophicTypeLookup.containsKey(key)) {
			return trophicTypeLookup.get(key).getTrophicType();
		}
		return "";
	}

	void generateTrophicTypeLookup() {		
		for (TrophicTypeObject trophicTypeObject: this.trophicTypeList) {
			String key = trophicTypeObject.getScientificName() + "<:>" + trophicTypeObject.getSizeClass();
			this.trophicTypeLookup.put(key, trophicTypeObject);
		}
	}

}
