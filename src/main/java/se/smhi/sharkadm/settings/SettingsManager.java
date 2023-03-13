/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.settings;

import java.util.HashMap;
import java.util.Map;

import se.smhi.sharkadm.database.SaveSettings;

public class SettingsManager {
	
	private static SettingsManager instance = new SettingsManager(); // Singleton.
	
	private Map<String, String> settingsList = new HashMap<String, String>(); 
	
	private SettingsManager() { // Singleton.
	}
	
	public static SettingsManager instance() { // Singleton.
		return instance;
	}
	
	public void clearSettingsList() {
		settingsList.clear();
	}

	public void addSetting(String settingKey, String settingValue) {
		this.settingsList.put(settingKey, settingValue);
	}

	public boolean contains(String settingsKey) {
		if (this.settingsList.containsKey(settingsKey)) {
			return true;
		}
		return false;
	}

	public Map<String, String> getSettingsList() {
		return settingsList;
	}
	
	public void saveSettingsToDatabase() {
		SaveSettings.instance().deleteSettings();
		for (String settingsKey : this.settingsList.keySet()) {
			if ( !settingsKey.startsWith("#")) { // # - comment row.
				SaveSettings.instance().insertSettings(settingsKey, this.settingsList.get(settingsKey));
			}
		}
	}

	public void removeSettingsInDatabase() {
		SaveSettings.instance().deleteSettings();
	}

}
