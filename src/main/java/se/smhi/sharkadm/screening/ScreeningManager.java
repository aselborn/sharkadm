/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.screening;

import java.io.PrintStream;

import se.smhi.sharkadm.model.ModelTopNode;


public class ScreeningManager {

	private PrintStream logInfo;	

	public ScreeningManager(PrintStream logInfo) {
		this.logInfo = logInfo;
	}
	
	public void performScreening() {
		
//		logInfo.println("");
//		logInfo.println("Screening for doublets.");
//		logInfo.println("");
//
//		DoubletScreening screening = new DoubletScreening();
//		screening.performScreening(ModelTopNode.instance().getDatasetList(), logInfo);
		
		DatabaseDoubletScreening databasescreening = new DatabaseDoubletScreening();
		databasescreening.performScreening(logInfo);
	}

}
