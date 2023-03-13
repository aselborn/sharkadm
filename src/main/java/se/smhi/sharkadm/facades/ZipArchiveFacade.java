/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.facades;

import java.util.Observable;

import se.smhi.sharkadm.ziparchive.ZipArchiveManager;

/**
 * This class implements the Observer pattern. 
 * ZipArchiveFacade is implemented as a singleton class.
 */
public class ZipArchiveFacade extends Observable {
	
	private static ZipArchiveFacade instance = new ZipArchiveFacade(); // Singleton.

	public static ZipArchiveFacade instance() { // Singleton.
		return instance;
	}

	private ZipArchiveFacade() { // Singleton.
	}

	public void updateZipArchiveFile(Boolean testZip) {
		ZipArchiveManager.instance().updateZipArchiveFile(testZip);
	}	
	
//	public void copyZipFilesToSharkDataOverSftp() {
//		ZipArchiveManager.instance().copyZipFilesToSharkDataOverSftp();
//	}	
	
}
