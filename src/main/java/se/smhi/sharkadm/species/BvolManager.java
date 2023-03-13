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

public class BvolManager {
	
	private static BvolManager instance = new BvolManager(); // Singleton.
	
	private List<BvolObject> bvolList = new ArrayList<BvolObject>(); 
	private Map<String, BvolObject> bvolLookup = new HashMap<String, BvolObject>();
	private Map<String, String> aphiaidLookup = new HashMap<String, String>();
	
	private BvolManager() { // Singleton.
	}
	
	public static BvolManager instance() { // Singleton.
		return instance;
	}
	
	public void clearLists() {
		this.bvolList.clear();
		this.bvolLookup.clear();
	}

	public void addBvol(BvolObject bvolObject) {
		this.bvolList.add(bvolObject);
	}

//	public String getAphiaId(String scientificName, String sizeClass) {
//		if ((bvolList.size() > 0) && (bvolLookup.size() == 0)) {
//			this.generateBvolLookup();
//		}
//		String key = scientificName + "<:>" + sizeClass;
//		if (bvolLookup.containsKey(key)) {
//			return bvolLookup.get(key).getAphiaId();
//		}
//		return "";
//	}

	public String getAphiaId(String scientificName) {
		if ((bvolList.size() > 0) && (aphiaidLookup.size() == 0)) {
			this.generateAphiaidLookup();
		}
		if (aphiaidLookup.containsKey(scientificName)) {
			return aphiaidLookup.get(scientificName);
		}
		return "";
	}

	public BvolObject getBvolObject(String scientificName, String sizeClass) {
		if ((bvolList.size() > 0) && (bvolLookup.size() == 0)) {
			this.generateBvolLookup();
		}
		String key = scientificName + "<:>" + sizeClass;
		if (bvolLookup.containsKey(key)) {
			return bvolLookup.get(key);
		}
		return null;
	}

	void generateBvolLookup() {
		for (BvolObject bvolObject: this.bvolList) {
			String key = bvolObject.getScientificName() + "<:>" + bvolObject.getSizeClass();
			this.bvolLookup.put(key, bvolObject);
		}
	}

	void generateAphiaidLookup() {
		for (BvolObject bvolObject: this.bvolList) {
			String scientificName = bvolObject.getScientificName();
			String aphiaId = bvolObject.getAphiaId();
			if (!scientificName.equals("")) {
				if (!aphiaId.equals("")) {
					if (!aphiaidLookup.containsKey(scientificName)) {
						this.aphiaidLookup.put(scientificName, aphiaId);
					}
				}
			}
		}
	}

}
