/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.facades;

import java.util.Observable;

import se.smhi.sharkadm.model.ModelTopNode;

/**
 * This class implements the Observer pattern.
 * ModelFacade is implemented as a singleton class.
 */
public class ModelFacade extends Observable {
	
	private static ModelFacade instance = new ModelFacade(); // Singleton.

	public static ModelFacade instance() { // Singleton.
		return instance;
	}

	private ModelFacade() { // Private constructor, Singleton.
	}
	
	public void clearMemoryModel() {
		ModelTopNode.instance().resetModel();
	}
		
	// Used by the Observer pattern.
	public void modelChanged() {
		this.setChanged();
		this.notifyObservers();
	}

}
