/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.species;

import java.util.HashMap;
import java.util.Map;

import se.smhi.sharkadm.fileimport.misc.FileImportTranslateToBvol;

public class BvolTranslate {
	
	private static BvolTranslate instance = new BvolTranslate(); // Singleton.
	
	private Map<String, String[]> nameSizeTranslate = new HashMap<String, String[]>();
	private Map<String, String> nameWithoutSizeTranslate = new HashMap<String, String>();
	private Map<String, String> nameTranslate = new HashMap<String, String>();
	
	private BvolTranslate() { // Singleton.
	}
	
	public static BvolTranslate instance() { // Singleton.
		return instance;
	}
	
	public void clear() {
		this.nameSizeTranslate.clear();
		this.nameTranslate.clear();
	}

	public boolean containsNameSize(String name, String sizeClass) {
		// Check if file is loaded.
		if (!FileImportTranslateToBvol.instance().isFileLoaded()) {
			FileImportTranslateToBvol.instance().loadFile();
		}
		String key = name + "<:>" + sizeClass;
		return nameSizeTranslate.containsKey(key);
	}

	public String[] translateNameSize(String name, String sizeClass) {
		
		if (this.containsNameSize(name, sizeClass)) {
			String key = name + "<:>" + sizeClass;
			return nameSizeTranslate.get(key);
		};
		return new String[] {name, sizeClass};
	}

	public boolean containsNameWithoutSize(String name) {
		// Check if file is loaded.
		if (!FileImportTranslateToBvol.instance().isFileLoaded()) {
			FileImportTranslateToBvol.instance().loadFile();
		}
		String key = name;
		return nameWithoutSizeTranslate.containsKey(key);
	}

	public String translateNameWithoutSize(String name) {
		
		if (this.containsNameWithoutSize(name)) {
			String key = name;
			return nameWithoutSizeTranslate.get(key);
		};
		return name;
	}

	public boolean containsName(String name) {
		// Check if file is loaded.
		if (!FileImportTranslateToBvol.instance().isFileLoaded()) {
			FileImportTranslateToBvol.instance().loadFile();
		}

		String key = name;
		return nameTranslate.containsKey(key);
	}

	public String translateName(String name) {
		
		if (this.containsName(name)) {
			String key = name;
			return nameTranslate.get(key);
		};
		return name;
	}

	public void addNameSizeTranslate(String fromName, String fromSize, String toName, String toSize) {
		String fromKey = fromName + "<:>" + fromSize;
		this.nameSizeTranslate.put(fromKey, new String[] { toName, toSize });
		// Check if also needed without size class.
		if (!fromName.equals(toName)) {
			if (!this.nameWithoutSizeTranslate.containsKey(fromName)) {
				this.nameWithoutSizeTranslate.put(fromName, toName);
			}
		}
	}

	public void addNameTranslate(String fromName, String toName) {
		if (!this.nameTranslate.containsKey(fromName)) {		
			this.nameTranslate.put(fromName, toName);
		} else {
			String oldName = this.nameTranslate.get(fromName);
			if (!oldName.equals(toName)) {
				System.out.println("DEBUG: BVOL translate. Another name already exists: " + 
						fromName + "  to: " + oldName  + "  to(new): " + fromName );
				
			}
		}
	}
}
