/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.formats;

import java.io.PrintStream;
import java.util.regex.Pattern;

import se.smhi.sharkadm.datasets.fileimport.FileImportInfo;

/**
 * The purpose of this class is to connect file import packages (zip files) to
 * the right file import class.
 * Information about used format is a part of the dataset which has 
 * to be included in the zip file. 
 */
public class SelectFormat {
	private PrintStream logInfo;
	private FileImportInfo importInfo;
	
	public SelectFormat(PrintStream logInfo, FileImportInfo importInfo) {
		this.importInfo = importInfo;
		this.logInfo = logInfo;
	}

	public FormatBase getFileImport(String zipFile, String format) {		
		FormatBase fileImport = null;

		format = format.toLowerCase().trim(); // Use lower case when comparing.
		
		if (format.equals("")) {
			importInfo.addConcatError("Empty import format string. Zip file: " + zipFile );
		}

		else if (format.contains(":")) {
			String[] strings = format.split(Pattern.quote(":"));
			String importFormat = strings[0].trim();

			// Bacterioplankton.
			if (importFormat.equals("bacterioplankton")) {
				fileImport = new FormatFileBacterioplankton(logInfo, importInfo);
			}
			else if (importFormat.equals("bacterioplanktonbiomad")) {
				fileImport = new FormatFileBacterioplankton(logInfo, importInfo);
			}

			// Chlorophyll.
			if (importFormat.equals("chlorophyll")) {
				fileImport = new FormatFileChlorophyll(logInfo, importInfo);
			}
			
			// Chlorophyll.
			if (importFormat.equals("chlorophyll")) {
				fileImport = new FormatFileChlorophyll(logInfo, importInfo);
			}
			
			// Profile
			if (importFormat.equals("profile")) {
				fileImport = new FormatFileProfile(logInfo, importInfo);
			}
			
			// Epibenthos.
			else if (importFormat.equals("epibenthos")) { // Epi.
				fileImport = new FormatFilePhytobenthos(logInfo, importInfo);
			}
			else if (importFormat.equals("epibenthosbiomad")) { // Epi.
				fileImport = new FormatFilePhytobenthosBiomad(logInfo, importInfo);
			}
			else if (importFormat.equals("epibenthosgrunda")) { // Epi.
				fileImport = new FormatFileGrunda(logInfo, importInfo);
			}
			// EpibenthosMartrans.
			/*else if (importFormat.equals("epibenthosmartrans")) {
				fileImport = new FormatXmlMartrans(logInfo, importInfo);
			}*/
			// EpibenthosDropvideo.
			else if (importFormat.equals("epibenthosdropvideo")) {
				fileImport = new FormatFileEpibenthosDropvideo(logInfo, importInfo);
			}
			// Greyseal.
			else if (importFormat.equals("greyseal")) {
				fileImport = new FormatFileGreyseal(logInfo, importInfo);
			}
			
			// Harbourseal.
			else if (importFormat.equals("harbourseal")) {
				fileImport = new FormatFileHarbourseal(logInfo, importInfo);
			}
			
			// Harbourporpoise.
			else if (importFormat.equals("harbourporpoise")) {
				fileImport = new FormatFileHarbourporpoise(logInfo, importInfo);
			}
			
			// PhysicalChemical.
			else if (importFormat.equals("physicalchemical")) {
				fileImport = new FormatFilePhysicalChemical(logInfo, importInfo);
			}
			
			// Phytoplankton.
			else if (importFormat.equals("phytoplankton")) {
				fileImport = new FormatFilePhytoplankton(logInfo, importInfo);
			}
			else if (importFormat.equals("phytoplanktonbiomad")) {
				fileImport = new FormatFilePhytoplanktonBiomad(logInfo, importInfo);
			}
			
			// PlanktonBarcoding.
			else if (importFormat.equals("barcoding")) {
				fileImport = new FormatFilePlanktonBarcoding(logInfo, importInfo);
			}

			// Picoplankton.
			else if (importFormat.equals("picoplankton")) {
				fileImport = new FormatFilePicoplankton(logInfo, importInfo);
			}

			// PrimaryProduction.
			else if (importFormat.equals("primaryproduction")) {
				fileImport = new FormatFilePrimaryProduction(logInfo, importInfo);
			}
			else if (importFormat.equals("primaryproductionbiomad")) {
				fileImport = new FormatFilePrimaryProduction(logInfo, importInfo);
			}
			
			// RingedSeal.
			else if (importFormat.equals("ringedseal")) {
				fileImport = new FormatFileRingedseal(logInfo, importInfo);
			}
			
			// SealPathology.
			else if (importFormat.equals("sealpathology")) {
				fileImport = new FormatFileSealPathology(logInfo, importInfo);
			}
			
			// Sediamentation.
			else if (importFormat.equals("sedimentation")) {
				fileImport = new FormatFileSedimentation(logInfo, importInfo);
			}		
			
			// Zoobenthos.
			else if (importFormat.equals("zoobenthos")) {
				fileImport = new FormatFileZoobenthos(logInfo, importInfo);
			}
			else if (importFormat.equals("zoobenthosbiomad")) {
				fileImport = new FormatFileZoobenthosBiomad(logInfo, importInfo);
			}			
			else if (importFormat.equals("zoobenthosbeda")) {
				fileImport = new FormatFileBeda(logInfo, importInfo);
			}
			
			// Zooplankton.
			else if (importFormat.equals("zooplankton")) {
				fileImport = new FormatFileZooplankton(logInfo, importInfo);
			}
			else if (importFormat.equals("zooplanktonbiomad")) {
				fileImport = new FormatFileZooplanktonBiomad(logInfo, importInfo);
			}
			
			// Jellyfish.
			else if (importFormat.equals("jellyfish")) {
				fileImport = new FormatFileJellyfish(logInfo, importInfo);
			}
			
		}

		else {
			importInfo.addConcatError("File import aborted. Invalid import format: " + format + ".");
		}
		
		return fileImport;
	}
	
}
