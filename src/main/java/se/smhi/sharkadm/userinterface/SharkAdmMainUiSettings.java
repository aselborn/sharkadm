/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.userinterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

/**
 *	This class is used to store windows settings and other user related stuff to a file. Next 
 *	time the application is started these settings are loaded and the application appear as it was 
 *	when it was shut down last time.<br/>
 *	<br/>
 *	Settings are stored in a file named "sharkadm_settings.xml" located in the users home directory, 
 *	for example in: "C:\Documents and Settings\arnold".<br/>
 *	<br/>
 *	Usage example:<br/>
 *		SharkAdmMainUiSettings settings = SharkAdmMainUiSettings.instance();<br/>
 *		settings.getInt("main_window_left", -10);<br/>
 *		settings.setInt("main_window_left", bounds.x);<br/>
 *	
 */
public class SharkAdmMainUiSettings {

	private static SharkAdmMainUiSettings instance = new SharkAdmMainUiSettings(); // Singleton.

	private Properties settings = new Properties();

	private File settingsFile = new File(System.getProperty("user.home"),
			"sharkadm_settings.xml");

	public static SharkAdmMainUiSettings instance() { // Singleton.
		return instance;
	}

	private SharkAdmMainUiSettings() { // Singleton.
		try {
			InputStream fileStream = new FileInputStream(settingsFile);
			settings.loadFromXML(fileStream);
			fileStream.close();
		} catch (InvalidPropertiesFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// Normal exception the first time.
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setString(String property, String value) {
		settings.setProperty(property, value);
		saveSettings();
	}

	public void setInt(String property, int value) {
		settings.setProperty(property, Integer.toString(value));
	}

	public String getString(String property, String defaultValue) {
		if (settings.containsKey(property) == false) {
			setString(property, defaultValue);
		}
		return settings.getProperty(property);
	}

	public int getInt(String property, int defaultValue) {
		if (settings.containsKey(property) == false) {
			setInt(property, defaultValue);
		}
		return Integer.parseInt(settings.getProperty(property));
	}

	public void saveSettings() {
		try {
			settings.storeToXML(new FileOutputStream(settingsFile),
					"SHARK-ADM user settings. Remove this xml file if you want to go back to factory settings.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
