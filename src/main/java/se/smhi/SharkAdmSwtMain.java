package se.smhi;/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

import se.smhi.sharkadm.userinterface.SharkAdmMainWindow;

/**
 *	SharkAdmSwtMain is the main class for the SWT version of SharkAdm.<br/>
 *	Contains the "public static void main(String[] args)" method.<br/> 
 */
public class SharkAdmSwtMain {

	public static void main(String[] args) {		
		SharkAdmMainWindow swtApp = new SharkAdmMainWindow();
		swtApp.launchApp();	

//		System.out.println("Test started.");
//		
//		SpeciesManager.instance().loadFiles();
//		
//		LocationManager.instance().loadFiles();
//		
//		System.out.println("Test ended.");
		
	}
	
}
