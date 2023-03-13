/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.calc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.smhi.sharkadm.datasets.fileimport.FileImportInfo;
import se.smhi.sharkadm.location.VisitLocationManager;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.utils.ConvUtils;

import se.smhi.sharkadm.utils.ErrorLogger;

public class BenticQualityIndex {

	private static BenticQualityIndex instance = new BenticQualityIndex(); // Singleton.	
	private Map<String, String> latlong_west_east = new HashMap<String, String>();	
	private List<String> speciesInSampleList = new ArrayList<String>();
	private FileImportInfo importInfo;
		
	private BenticQualityIndex() { // Singleton.
	}
	
	public static BenticQualityIndex instance() { // Singleton.
		return instance;
	}
	
	public void clearAll() {
		this.latlong_west_east.clear();
		this.speciesInSampleList.clear();
	}
	
	public void calculateBqiForSample(Sample sample, FileImportInfo importInfo) {
		
		// TEST
//		if (sample.getParent().getField("visit.reported_station_name").startsWith("ALSB")) {
//			if (sample.getParent().getField("visit.visit_date").equals("2016-06-23")) {
//				System.out.println("DEBUG BQI Control: " + sample.getParent().getField("visit.reported_station_name") +
//						"   " + sample.getParent().getField("visit.visit_date"));
//			}
//		}		
		
		this.importInfo = importInfo;
		
		String errorCode = "";
		
		// Semi-quantitative sampling should not be used.
		//		sample.method_documentation
		//		sample.method_reference_code
		String method_documentation = sample.getField("sample.method_documentation");
		if (method_documentation.equals("Förenklad inventering av mjukbottenfauna genom sållning, artidentifiering och uppskattning av abundanser direkt i fält.")) {
			importInfo.addConcatWarning("BQIm: Not calculated for semi-quantitative data. Method:" + method_documentation);
			
			
			errorCode = "BQIm: Not calculated for semi-quantitative data.";
			return;
		}
		
		BqiSpecies.instance().loadSpeciesLists(importInfo);
		if (!BqiSpecies.instance().checkIfSpeciesListsAreLoaded()) {
			importInfo.addConcatError("BQIm: Species lists not loaded.");
			return;
		}
		
		// Step 1: Check if sample is valid for BQI. Sampler area = 0.1 m2, mesh size = 1 mm.
		Double samplerAreaCm2 = 0.0;
		Double meshSizeUm = 0.0;
		try {
			samplerAreaCm2 = Double.parseDouble(sample.getField("sample.sampler_area_cm2").replace(",", "."));
		} catch (Exception e) { }
		
		if ((samplerAreaCm2 < 750.0) || (samplerAreaCm2 > 1250.0)) {
			importInfo.addConcatWarning("BQIm: Wrong sampler area: " + sample.getField("sample.sampler_area_cm2"));
//		if ((samplerAreaCm2 < 800.0) || (samplerAreaCm2 > 1200.0)) {
//			importInfo.addConcatWarning("BQIm: Wrong sampler area: " + sample.getField("sample.sampler_area_cm2"));
			
			
			errorCode = errorCode + "BQIm: Wrong sampler area: " + sample.getField("sample.sampler_area_cm2");
			return; // Don't calculate BQI if wrong sampler.
		}
		
		// Step 2: Check sample location. West or East area. 
		String location_id = VisitLocationManager.calculateKey(
				sample.getParent().getPosition().getLatitude(), 
				sample.getParent().getPosition().getLongitude()); // visit_location_id.
		// Reuse old value or get new value for east or west.
		String east_west = "";
		if (this.latlong_west_east.containsKey(location_id)) {
			east_west = this.latlong_west_east.get(location_id);
		} else {
			east_west = BqiShapefileReader.instance().getWestEastFromShapeFile(sample.getParent().getPosition().getLatitude(), 
													  sample.getParent().getPosition().getLongitude());
			this.latlong_west_east.put(location_id, east_west);
		}
		
		if (east_west.equals("")) {
			return; // Outside area.
		}
		if (east_west.equals("NOT-USED")) {
			return; // Outside area.
		}
		
		// Step 3: Iterate over rows in sample.
		double bqi = 0.0;
		double numberOfSpecies = 0.0;
//		double numberOfClassifiedSpecies = 0.0;
		double numberOfIndividuals = 0.0;
		double numberOfClassifiedIndividuals = 0.0;
		double bqiSum = 0.0;
		
		speciesInSampleList.clear();
		
		String debugCalcDetails = "";
		
		for (Variable variable : sample.getVariables()) {
			
			String scientificName = variable.getField("variable.scientific_name");
			
//			// TEST
//			if (scientificName.equals("Limnodrilus")) {
//				System.out.println("DEBUG BQI: Limnodrilus");
//			}
			
			String parameter = variable.getField("variable.parameter");
			String value_string = variable.getField("variable.value");
			String unit = variable.getField("variable.unit");
			// Only use the parameter for counted individuals.
			if (!parameter.equals("# counted")) {
				continue; 
			}			
			// Check if species should be used.
			if (BqiSpecies.instance().isSpeciesExcluded(scientificName)) {
				
				debugCalcDetails += "  - " + scientificName + " <Excluded taxa>\n";
				
				continue;
			}
			
			// Old: variable.mesh_size_um
			// New: variable.upper_mesh_size_um and variable.lower_mesh_size_um
			String meshSizeUmString = "0.0";
			if (variable.containsField("variable.lower_mesh_size_um")) {
				meshSizeUmString = variable.getField("variable.lower_mesh_size_um");
			} else {
				meshSizeUmString = variable.getField("variable.mesh_size_um");					
			}
			try {
				meshSizeUm = Double.parseDouble(meshSizeUmString.replace(",", "."));					
			} catch (Exception e) { }
			if ((meshSizeUm != 1000.0)) {
				importInfo.addConcatWarning("BQIm: Wrong mesh size: " + variable.getField("variable.mesh_size_um"));
				
				
				if (errorCode.length() == 0) {
					errorCode = "BQIm: Wrong mesh size: " + variable.getField("variable.mesh_size_um");
				}
				return; // Don't calculate BQI if wrong sampler.
			}
			
			// Add number of individuals.
			Double value = 0.0;
			try {
				value = Double.parseDouble(value_string);
				numberOfIndividuals += value;
			} catch (NumberFormatException e) {
				importInfo.addConcatError("BQIm: Wrong variable.value: " + value_string);
			}

			// Classified species.
			
			
			
			Boolean beda = true;
			
			
			
			
			Double bqiValue = BqiSpecies.instance().getSensitivityValue(scientificName, east_west, beda);
			if (bqiValue != null) {
				bqiSum += bqiValue * value;
				numberOfClassifiedIndividuals += value;
				
				debugCalcDetails += "  - " + scientificName + " S: " + ConvUtils.convDoubleToString(bqiValue) + " N: " + ConvUtils.convDoubleToString(value) + "\n";
			} else {
				debugCalcDetails += "  - " + scientificName + " S: <Not available> N: " + ConvUtils.convDoubleToString(value) + "\n";
				importInfo.addConcatWarning("BQIm: " + east_west + "  Sensitivity value missing for: " + scientificName);
			}

			// Translate to species group. (Chironomidae, Clitellata (Oligochaeta), Ostracoda).
			String taxonName = BqiSpecies.instance().translateToGroupedTaxa(scientificName);
			// Add species to species list.			
			if (!this.speciesInSampleList.contains(taxonName)) {
				this.speciesInSampleList.add(taxonName);
			}		
		}

		numberOfSpecies = this.speciesInSampleList.size();
		
		// Adjust counted individuals if another sampler area is used.
		double adjustedNumberOfIndividuals = numberOfIndividuals * 1000.0 / samplerAreaCm2;

		if (numberOfClassifiedIndividuals > 0.0) {
			// Calculate BQIm for one sample. 
			bqi = bqiSum / numberOfClassifiedIndividuals * 
				  Math.log10( numberOfSpecies + 1 ) * 
//				  (numberOfIndividuals / (numberOfIndividuals + 5));
			  	  (adjustedNumberOfIndividuals / (adjustedNumberOfIndividuals + 5.0));
			
			// Add BQI parameter to sample.
			
			if (true) {
//			if (false) {
				// Normal.
				addVariable(sample, "BQIm", bqi, "index/sample");
			} else {
				// Debug.
				if (errorCode.length() > 0) {
					if (errorCode.length() >= 60) {
						addVariable(sample, "BQIm-DEBUG", bqi, errorCode.substring(0, 60));
					} else {
						addVariable(sample, "BQIm-DEBUG", bqi, errorCode);
					}
				} else {
					addVariable(sample, "BQIm", bqi, "index/sample");
				}
			
				// TEST
				String bqiDebugInfo;
				if (errorCode.length() > 0) {
					bqiDebugInfo = "DEBUG BQIm:" + 
							"\n" + 
							"  Station/date: " + sample.getParent().getField("visit.reported_station_name") + " / " + sample.getField("sample.sample_date") + 
							"\n" + 
							"  Error code: " + errorCode;
				} else {
					bqiDebugInfo = "DEBUG BQIm:" + 
							"\n" + 
							"  Station/date: " + sample.getParent().getField("visit.reported_station_name") + " / " + sample.getField("sample.sample_date") + 
							"\n" + 
							"  East/west: " + east_west + "  " + sample.getParent().getPosition().getLatitude() + 
							                              ", " + sample.getParent().getPosition().getLongitude() + 
							"\n" + 
							"  BQIm: " + ConvUtils.convDoubleToString(bqi) + 
							"\n" + 
							"  BQImSum: " + ConvUtils.convDoubleToString(bqiSum) + 
							"  Species: " + ConvUtils.convDoubleToString(numberOfSpecies) + 
							"\n" + 
							"  Individuals: " + ConvUtils.convDoubleToString(numberOfIndividuals) + 
							"  ClassifiedIndividuals: " + ConvUtils.convDoubleToString(numberOfClassifiedIndividuals) + 
							"\n" + 
							"  Sampler area: " + ConvUtils.convDoubleToString(samplerAreaCm2) + 
							"  Adjusted Individuals: " + ConvUtils.convDoubleToString(adjustedNumberOfIndividuals) + 
							"\n" + 
							debugCalcDetails;
				}
				System.out.println(bqiDebugInfo);
				ErrorLogger.println(bqiDebugInfo);
			}
		}
	}
	
	public void addVariable(Sample sample, String parameter, Double value, String unit) {
		
		Variable newVariable = new Variable(false);
		sample.addVariable(newVariable);
		
		newVariable.addField("variable.parameter", parameter);
		try {
			newVariable.addField("variable.value", ConvUtils.convDoubleToString(value));
		} catch (Exception e) {
			newVariable.addField("variable.value", "");
		}
		newVariable.addField("variable.unit", unit);
		
		newVariable.addField("variable.calc_by_dc", "Y");
	}
	
}








































