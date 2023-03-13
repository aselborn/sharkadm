package se.smhi.sharkadm.species;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 */
//public class SpeciesManager {
//
//	private static SpeciesManager instance = new SpeciesManager(); // Singleton.
//
//	private List<TrophicTypeObject> trophicTypeList = new ArrayList<TrophicTypeObject>(); 
//	private Map<String, TrophicTypeObject> trophicTypeLookup = new HashMap<String, TrophicTypeObject>();
//	
//	private SpeciesManager() { // Singleton.
//	}
//
//	public static SpeciesManager instance() { // Singleton.
//		return instance;
//	}
//
//	public void clear() {
//		
//	}
//
//	public void loadFiles() {
//		System.out.println("SpeciesManager: loadFiles().");
////		SpeciesImports
//		this.generateTrophicTypeLookup();
//
//		
//		// Used by the Observer pattern.
////		ModelFacade.instance().modelChanged();
//	}
//
//	public void addTrophicType(TrophicTypeObject TrophicTypeObject) {
//		this.trophicTypeList.add(TrophicTypeObject);
//	}
//
//	public String getTrophicType(String scientificName, String sizeClass) {
//		if ((trophicTypeList.size() > 0) && (trophicTypeLookup.size() == 0)) {
//			this.generateTrophicTypeLookup();
//		}
//		String key = scientificName + "<:>" + sizeClass;
//		if (trophicTypeLookup.containsKey(key)) {
//			return trophicTypeLookup.get(key).getTrophicType();
//		}
//		return "";
//	}
//
//	void generateTrophicTypeLookup() {		
//		for (TrophicTypeObject trophicTypeObject: this.trophicTypeList) {
//			String key = trophicTypeObject.getScientificName() + "<:>" + trophicTypeObject.getSizeClass();
//			this.trophicTypeLookup.put(key, trophicTypeObject);
//		}
//	}
//
//}