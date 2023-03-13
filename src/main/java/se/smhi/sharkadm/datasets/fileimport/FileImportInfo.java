/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.fileimport;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import se.smhi.sharkadm.facades.ImportFacade;
import se.smhi.sharkadm.utils.ErrorLogger;

public class FileImportInfo {

	private int visitCounter = 0;
	private int sampleCounter = 0;
	private int variableCounter = 0;
	
	private int warningCounter = 0;
	private int errorCounter = 0;
	
	// Concatenated lists. Parameter 2 = error counter.
	private Map<String, Integer> concatInfoList = new HashMap<String, Integer>();
	private Map<String, Integer> concatWarningList = new HashMap<String, Integer>();
	private Map<String, Integer> concatErrorList = new HashMap<String, Integer>();

	public FileImportInfo() {
	}
	
	public void clearAll() {
		visitCounter = 0;
		sampleCounter = 0;
		variableCounter = 0;
		this.concatInfoList.clear();
		this.concatWarningList.clear();
		this.concatErrorList.clear();
		warningCounter = 0;
		errorCounter = 0;
	}
	
	public void clearConcatLists() {
		this.concatInfoList.clear();
		this.concatWarningList.clear();
		this.concatErrorList.clear();
	}
	
	public void incVisitCounter() {
		visitCounter++;
	}

	public void incSampleCounter() {
		sampleCounter++;
	}

	public void incVariableCounter() {
		variableCounter++;
	}

	public void addConcatInfo(String info) {
		int counter = 1;
		if (this.concatInfoList.containsKey(info)) {
			counter = this.concatInfoList.get(info) + 1;
		}
		this.concatInfoList.put(info, counter);
	}
	
	public void addConcatWarning(String info) {
		warningCounter++;
		int counter = 1;
		if (this.concatWarningList.containsKey(info)) {
			counter = this.concatWarningList.get(info) + 1;
		}
		this.concatWarningList.put(info, counter);
	}
	
	public void addConcatError(String info) {
		errorCounter++;
		int counter = 1;
		if (this.concatErrorList.containsKey(info)) {
			counter = this.concatErrorList.get(info) + 1;
		}
		this.concatErrorList.put(info, counter);
	}

	public void logConcatInfo(PrintStream logInfo) {
//		if (ImportFacade.instance().isShowInfo()) {
//			Object[] keys = concatInfoList.keySet().toArray();
//			Arrays.sort(keys);
//			for (Object infoString : keys) {
//				logInfo.println("Info: " + (String)infoString + " (" + 
//						concatInfoList.get(infoString) + " times)");
//				ErrorLogger.println("Info: " + (String)infoString + " (" + 
//						concatInfoList.get(infoString) + " times)");
//			}
//		}
		
		Object[] keys = concatInfoList.keySet().toArray();
		Arrays.sort(keys);
		for (Object infoString : keys) {
			if (ImportFacade.instance().isShowInfo()) {
				logInfo.println("Info: " + (String)infoString + " (" + 
						concatInfoList.get(infoString) + " times)");
			}
			ErrorLogger.println("Info: " + (String)infoString + " (" + 
					concatInfoList.get(infoString) + " times)");
		}
	}
	
	public void logConcatWarnings(PrintStream logInfo) {
		Object[] keys = concatWarningList.keySet().toArray();
		Arrays.sort(keys);
		for (Object warningString : keys) {
			if (ImportFacade.instance().isShowWarnings()) {
				logInfo.println("Warning: " + (String)warningString + " (" + 
						concatWarningList.get(warningString) + " times)");
			}
			ErrorLogger.println("Warning: " + (String)warningString + " (" + 
					concatWarningList.get(warningString) + " times)");
		}
//		if (ImportFacade.instance().isShowWarnings()) {
//			Object[] keys = concatWarningList.keySet().toArray();
//			Arrays.sort(keys);
//			for (Object warningString : keys) {
//				logInfo.println("Warning: " + (String)warningString + " (" + 
//						concatWarningList.get(warningString) + " times)");
//				ErrorLogger.println("Warning: " + (String)warningString + " (" + 
//						concatWarningList.get(warningString) + " times)");
//			}
//		}
	}
	
	public void logConcatErrors(PrintStream logInfo) {
//		if (ImportFacade.instance().isShowErrors()) {
//			Object[] keys = concatErrorList.keySet().toArray();
//			Arrays.sort(keys);
//			for (Object errorString : keys) {
//				logInfo.println("Error: " + (String)errorString + " (" + 
//						concatErrorList.get(errorString) + " times)");
//				ErrorLogger.println("Error: " + (String)errorString + " (" + 
//						concatErrorList.get(errorString) + " times)");
//			}
//		}

		Object[] keys = concatErrorList.keySet().toArray();
		Arrays.sort(keys);
		for (Object errorString : keys) {
			if (ImportFacade.instance().isShowErrors()) {
				logInfo.println("Error: " + (String)errorString + " (" + 
						concatErrorList.get(errorString) + " times)");
			}
			ErrorLogger.println("Error: " + (String)errorString + " (" + 
					concatErrorList.get(errorString) + " times)");
		}
	
	}
	
	public int getVisitCounter() {
		return visitCounter;
	}

	public int getSampleCounter() {
		return sampleCounter;
	}

	public int getVariableCounter() {
		return variableCounter;
	}

	public int getWarningCounter() {
		return warningCounter;
	}

	public int getErrorCounter() {
		return errorCounter;
	}

	public Map<String, Integer> getConcatInfoList() {
		return concatInfoList;
	}

	public Map<String, Integer> getConcatWarningList() {
		return concatWarningList;
	}

	public Map<String, Integer> getConcatErrorList() {
		return concatErrorList;
	}

}
