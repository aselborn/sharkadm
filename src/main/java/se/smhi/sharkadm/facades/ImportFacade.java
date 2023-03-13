/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.facades;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import se.smhi.sharkadm.datasets.fileimport.FileImportManager;
import se.smhi.sharkadm.screening.ScreeningManager;

/**
 * This class implements the Observer pattern. ImportFacade is implemented as a
 * singleton class.
 */
public class ImportFacade extends Observable {

	private static ImportFacade instance = new ImportFacade(); // Singleton.

	private boolean showErrors;
	private boolean showWarnings;
	private boolean showInfo;
	private boolean autoClearMemoryModel;

	public static ImportFacade instance() { // Singleton.
		return instance;
	}

	private ImportFacade() { // Private constructor, Singleton.
	}

	public void importDatasetPackage(String zipFile, PrintStream logInfo) {
		if (ImportFacade.instance().isAutoClearMemoryModel()) {
			ModelFacade.instance().clearMemoryModel();
		}
		
		
//		new FileImportManager(logInfo).importDatasetPackage(zipFile);



		new FileImportManager(logInfo).importSharkFolder(zipFile);



	}

	public void performScreening(String zipFile, PrintStream logInfo) {
		// Note: Zip-file not used.
		new ScreeningManager(logInfo).performScreening();
	}

	// Used by the Observer pattern.
	public void modelChanged() {
		this.setChanged();
		this.notifyObservers();
	}

	public boolean isShowErrors() {
		return showErrors;
	}

	public void setShowErrors(boolean showErrors) {
		this.showErrors = showErrors;
	}

	public boolean isShowWarnings() {
		return showWarnings;
	}

	public void setShowWarnings(boolean showWarnings) {
		this.showWarnings = showWarnings;
	}

	public boolean isShowInfo() {
		return showInfo;
	}

	public void setShowInfo(boolean showInfo) {
		this.showInfo = showInfo;
	}

	public boolean isAutoClearMemoryModel() {
		return autoClearMemoryModel;
	}

	public void setAutoClearMemoryModel(boolean autoClearMemoryModel) {
		this.autoClearMemoryModel = autoClearMemoryModel;
	}

	
	// NEW:
	
	public void importSharkFolder(String sharkFolderPath, PrintStream logInfo) {
		if (ImportFacade.instance().isAutoClearMemoryModel()) {
			ModelFacade.instance().clearMemoryModel();
		}
		new FileImportManager(logInfo).importSharkFolder(sharkFolderPath);
	}

	
	
	public List<Map<String, String>> getDatasetAndPathList(String parentFolderPath) {
		
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		File parentFolder = new File(parentFolderPath);
		findFolder(parentFolder, list);
		
		// Sort the list.
		Collections.sort(list, mapComparator);
		
		return list;
	}

    private void findFolder(File parentFolder, 
    						List<Map<String, String>> list) {
    	
        if (parentFolder.getName().startsWith("SHARK_")) {
            System.out.println(parentFolder.getName());
    		Map<String, String> item = new HashMap<String, String>();
    		item.put("dataset_package_name", parentFolder.getName());
    		item.put("import_file_path", parentFolder.getAbsolutePath());
    		list.add(item);
        }
        File[] files = parentFolder.listFiles();
        if (files != null) {
	        for (File file : files) {
	            if (file.isFile()) {
	                continue;
	            }
	            if(file.isDirectory()) {
	               findFolder(file, list);
	            }
	        }
        }
    }
    
    public Comparator<Map<String, String>> mapComparator = new Comparator<Map<String, String>>() {
        public int compare(Map<String, String> m1, Map<String, String> m2) {
            return m1.get("import_file_path").compareTo(m2.get("import_file_path"));
        }
    };

}
