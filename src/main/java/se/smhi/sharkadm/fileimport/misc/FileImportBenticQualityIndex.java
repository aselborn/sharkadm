/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.fileimport.misc;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.List;

import se.smhi.sharkadm.datasets.calc.BqiSpecies;
import se.smhi.sharkadm.datasets.fileimport.SingleFileImport;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.utils.ParseFileUtil;

public class FileImportBenticQualityIndex extends SingleFileImport {
		
	public FileImportBenticQualityIndex(PrintStream logInfo) {
		super(logInfo);
	}
	
	public void importFiles(String zipFileName, Dataset dataset) {
		
	}

	public void importFiles(String zipFileName) {
		List<String[]> fileContent;
		BufferedReader bufferedReader;
		
		BqiSpecies.instance().clearSpecies();

		//
		// Sensitivity lists from HVMFS 2013:19.
		//
		try {
			bufferedReader = ParseFileUtil.GetSharkConfigFile("qualityindex_bqi_westcoast_species.txt");

			fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
			if (fileContent != null) {				
				importClassifiedSpecies(fileContent, "WEST");
			} else {
				System.out.println("ERROR: Failed to open file: qualityindex_bqi_westcoast_species.txt.");
			}
			
		} catch (Exception e) {
//			throw e;
			System.out.println("ERROR: Failed to open file: qualityindex_bqi_westcoast_species.txt. Exception: " + e.getMessage());
		}
		
		try {
			bufferedReader = ParseFileUtil.GetSharkConfigFile("qualityindex_bqi_eastcoast_species.txt");

			fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
			if (fileContent != null) {				
				importClassifiedSpecies(fileContent, "EAST");
			} else {
				System.out.println("ERROR: Failed to open file: qualityindex_bqi_eastcoast_species.txt.");
			}
			
		} catch (Exception e) {			
//			throw e;			
			System.out.println("ERROR: Failed to open file: qualityindex_bqi_eastcoast_species.txt. Exception: " + e.getMessage());
		}
		
		try {
			bufferedReader = ParseFileUtil.GetSharkConfigFile("qualityindex_bqi_excluded_species.txt");

			fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
			if (fileContent != null) {				
				importExcludedSpecies(fileContent);
			} else {
				System.out.println("ERROR: Failed to open file: qualityindex_bqi_excluded_species.txt.");
			}
			
		} catch (Exception e) {			
//			throw e;			
			System.out.println("ERROR: Failed to open file: qualityindex_bqi_excluded_species.txt. Exception: " + e.getMessage());
		}
		
		
		//
		// Sensitivity lists from BEDA (HAFOK).
		//
		try {
			bufferedReader = ParseFileUtil.GetSharkConfigFile("qualityindex_bqi_westcoast_species_beda.txt");

			fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
			if (fileContent != null) {				
				importClassifiedSpecies(fileContent, "WEST-BEDA");
			} else {
				System.out.println("ERROR: Failed to open file: qualityindex_bqi_westcoast_species_beda.txt.");
			}
			
		} catch (Exception e) {
//			throw e;
			System.out.println("ERROR: Failed to open file: qualityindex_bqi_westcoast_species_beda.txt. Exception: " + e.getMessage());
		}
		
		try {
			bufferedReader = ParseFileUtil.GetSharkConfigFile("qualityindex_bqi_eastcoast_species_beda.txt");

			fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
			if (fileContent != null) {				
				importClassifiedSpecies(fileContent, "EAST-BEDA");
			} else {
				System.out.println("ERROR: Failed to open file: qualityindex_bqi_eastcoast_species_beda.txt.");
			}
			
		} catch (Exception e) {			
//			throw e;			
			System.out.println("ERROR: Failed to open file: qualityindex_bqi_eastcoast_species_beda.txt. Exception: " + e.getMessage());
		}
		
		
		
	}
	
	private void importClassifiedSpecies(List<String[]> fileContent, String westEastArea) {
		String[] header = null;

		for (String[] row : fileContent) {
			if (header == null) { 
				// The first line contains the header.
				header = row;
				checkHeader(header);
			} else {
				String scientificName = getCell(row, "scientific_name");
				String sensitivityValue = getCell(row, "sensitivity_value").replace(",", ".");
				
				if (!scientificName.equals("")) {
					BqiSpecies.instance().addClassifiedSpecies(scientificName, sensitivityValue, westEastArea);					
				}
			}
		}
	}

	private void importExcludedSpecies(List<String[]> fileContent) {
		String[] header = null;

		for (String[] row : fileContent) {
			if (header == null) { 
				// The first line contains the header.
				header = row;
				checkHeader(header);
			} else {
				String scientificName = getCell(row, "scientific_name");
				String rank = getCell(row, "rank");
								
				if (!scientificName.equals("")) {
					BqiSpecies.instance().addExcludedSpecies(scientificName, rank);
				}
			}
		}
	}

	@Override
	public void visitDataset(Dataset dataset) {
	}

	@Override
	public void visitSample(Sample sample) {
	}

	@Override
	public void visitVariable(Variable variable) {
	}

	@Override
	public void visitVisit(Visit visit) {
	}
}
