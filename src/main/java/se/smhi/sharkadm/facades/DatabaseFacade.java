/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.facades;

import java.util.Observable;

import se.smhi.sharkadm.database.DbConnect;
import se.smhi.sharkadm.database.DeleteModel;

/**
 * This class implements the Observer pattern. Clients that wants to be 
 * informed when a change in the database occurs should register here.
 * Parts of the code that makes changes to the database should call the 
 * method dataChanged when they are finished.
 * DatabaseFacade is implemented as a singleton class.
 */
public class DatabaseFacade extends Observable {
	
	private static DatabaseFacade instance = new DatabaseFacade(); // Singleton.

	public static DatabaseFacade instance() { // Singleton.
		return instance;
	}

	private DatabaseFacade() { // Singleton.
	}

	public void saveModelToDb() {
		// TODO.
	}	
	
	public void dataChanged() {
		this.setChanged();
		this.notifyObservers();
		// Cleare cached results from database.
		DeleteModel.instance().clearDbCache();
	}

	public void connectionStatusChanged() {
		this.setChanged();
		this.notifyObservers();
	}

	public void clearDb() {
		DeleteModel.instance().deleteAll();
	}

//	public void backupToFile(String backupFileName) {
//		DbConnect.instance().backupDatabase(backupFileName);
//	}

	public void psqlVacuum() {
		DbConnect.instance().psqlVacuum();		
	}
}
