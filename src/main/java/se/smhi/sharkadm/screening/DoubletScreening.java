/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.screening;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.ModelVisitor;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;

/**
 * 
 * The Visitor pattern is used to walk through the memory model.
 * 
*/

public class DoubletScreening {
//public class DoubletScreening extends ModelVisitor {
//
//	private PrintWriter screeningWriter;
//	private PrintStream logInfo;
//	
////	private List<String> doubletCheckList = new ArrayList<String>();
//	private Map<String, String> doubletCheckMap = new HashMap<String, String>();
//	
//
//	public void performScreening(List<Dataset> datasetList, PrintStream logInfo) {
//		
//		this.logInfo = logInfo;
//
//		if (datasetList.size() < 1) {
//			return; // Nothing to export.
//		}
//
//		// Create file.
//		DateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
////		File homedir = new File(System.getProperty("user.home"));
////		File exportFile = new File(homedir, "Screening" +
////				datetimeFormat.format(new Date()) + ".txt");
//		File screeningFile = new File("Screening" +
//				datetimeFormat.format(new Date()) + ".txt");
//
//		try {
//			screeningWriter = new PrintWriter(new FileWriter(screeningFile));
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//
//		// Should be checked for all loaded datasets.
//		doubletCheckMap.clear();
//		
//		// Iterate over deliveries.
//		for (Dataset dataset : datasetList) {
//
////			doubletCheckList.clear();
//			
//			logInfo.println("Screening: " + dataset.getField("dataset.dataset_file_name"));
//			logInfo.println("");
//			
//			dataset.Accept(this);
//		}
//
//		// Close file.
//		screeningWriter.close();
//	}
//
//	public void visitDataset(Dataset dataset) {
//
//
//		// Iterate over visits.
//		for (Visit visit : dataset.getVisits()) {			
//			visit.Accept(this);
//		}
//	}
//
//	public void visitVisit(Visit visit) {
//
//		// Iterate over samples.
//		for (Sample sample : visit.getSamples()) {
//			sample.Accept(this);
//		}
//	}
//
//	public void visitSample(Sample sample) {
//
//		// Create the keyvalue string that should be used when detecting doublets. 
//		String keyvalue = "";
//		keyvalue += " Date: " + sample.getParent().getField("visit.visit_date");
//		keyvalue += " Station: " + sample.getParent().getField("visit.station_name");
//		
//		keyvalue += " Min depth: " + sample.getField("sample.sample_min_depth_m");
//		keyvalue += " Max depth: " + sample.getField("sample.sample_max_depth_m");
//
//		String datasetName = sample.getParent().getParent().getField("dataset.dataset_file_name");
//			
//		if (doubletCheckMap.containsKey(keyvalue)) {
//			String message = "Doublet found for: " + keyvalue +
//			"     Dataset 1: " + doubletCheckMap.get(keyvalue) + 
//			"     Dataset 2: " + datasetName; 
//			logInfo.println(message);
//			screeningWriter.println(message);
//		} else {
//			doubletCheckMap.put(keyvalue, datasetName);
//		}
//		
////		// Iterate over variables.
////		for (Variable variable : sample.getVariables()) {
////			variable.Accept(this);
////		}
//	}
//
//	public void visitVariable(Variable variable) {
//		
////		// Create the keyvalue string that should be used when detecting doublets. 
////		String keyvalue = "";
////		keyvalue += " Date: " + variable.getParent().getParent().getVisit_date();
////		keyvalue += " Station: " + variable.getParent().getParent().getField("visit.station_name");
////		keyvalue += " Parameter: " + variable.getParameter();
////		
////		if (variable.getParent().getParent().getParent().getDatatype().equals("Phytoplankton")) {
////			keyvalue += " Min depth: " + variable.getParent().getField("sample.sample_min_depth_m");
////			keyvalue += " Max depth: " + variable.getParent().getField("sample.sample_max_depth_m");
////			keyvalue += " Species: " + variable.getField("variable.scientific_name");
////			keyvalue += " Sizeclass: " + variable.getField("variable.size_class");
////
////		}
////		else if (variable.getParent().getParent().getParent().getDatatype().equals("Zooplankton")) {
////			keyvalue += " Min depth: " + variable.getParent().getField("sample.sample_min_depth_m");
////			keyvalue += " Max depth: " + variable.getParent().getField("sample.sample_max_depth_m");
////			keyvalue += " Species: " + variable.getField("variable.scientific_name");
////			keyvalue += " Sex: " + variable.getField("variable.sex_code");
////			keyvalue += " Stage: " + variable.getField("variable.dev_stage_code");
////		}
////		else if (variable.getParent().getParent().getParent().getDatatype().equals("Zoobenthos")) {
////			keyvalue += " Min depth: " + variable.getParent().getField("sample.sample_min_depth_m");
////			keyvalue += " Max depth: " + variable.getParent().getField("sample.sample_max_depth_m");
////			keyvalue += " Species: " + variable.getField("variable.scientific_name");
////			keyvalue += " Stage: " + variable.getField("variable.dev_stage_code");
////		}
////			
////		if (doubletCheckList.contains(keyvalue)) {
////			logInfo.println("ERROR. Doublet found for: " + keyvalue);
////			screeningWriter.println("ERROR. Doublet found for: " + keyvalue);
////		} else {
////			doubletCheckList.add(keyvalue);
////		}
//			
//	}
//
}

