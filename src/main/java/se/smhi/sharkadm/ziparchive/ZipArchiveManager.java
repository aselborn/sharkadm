/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.ziparchive;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.ModelTopNode;

public class ZipArchiveManager {
	
	private static ZipArchiveManager instance = new ZipArchiveManager(); // Singleton.
	
	private ZipArchiveManager() { // Singleton.
	}
	
	public static ZipArchiveManager instance() { // Singleton.
		return instance;
	}
	
	public String getLatestZipFileVersion(String datasetName) {
		
		String prodPath = "\\\\winfs\\data\\prodkap\\sharkweb\\SHARKdata_datasets";
		String resultName = "";
		
		File directory = new File(prodPath);
		for (File f : directory.listFiles()) {
			String fileName = f.getName();
		    if (fileName.startsWith(datasetName + "_version")) {
		    	
		    	int compare = fileName.compareTo(resultName);
		    	if (compare > 0) {
		    		resultName = fileName;
		    	}
		    }
		}
		return resultName;
	}
	
	public void updateZipArchiveFile(Boolean testZip) {
		java.util.List<Dataset> datasetList = ModelTopNode.instance().getDatasetList();
		for (Dataset dataset : datasetList) {
			
			// Get info from dataset.
			String datasetName = dataset.getField("dataset.dataset_name");
			String zipFileName = dataset.getField("dataset.dataset_file_name");
			String fromPath = dataset.getField("dataset.dataset_file_path");
			
			String datatype = "";
			String[] nameParts = zipFileName.split("_");
			if (nameParts.length > 2) {
				datatype = nameParts[1];
			}

			// Paths to used zip files directories.
			String testPath = "\\\\winfs\\data\\prodkap\\sharkweb\\SHARKdata_datasets_TEST";
			String tmpPath = "\\\\winfs\\data\\prodkap\\sharkweb\\SHARKdata_datasets_TMP";
			String prodPath = "\\\\winfs\\data\\prodkap\\sharkweb\\SHARKdata_datasets";
			
			
			
//			// FOR LOCAL TEST ONLY.
//			testPath = "SHARKdata_datasets_TEST";
//			tmpPath = "SHARKdata_datasets_TMP";
//			prodPath = "SHARKdata_datasets_PROD";
			
			
			
			// Create directories if not already created.
			try {
				// TEST directory.
				Path test_Path = Paths.get(testPath);
		        // Check if directory exists?
		        if (!Files.exists(test_Path)) {
		            try {
		            	// Create directory and parents.
		                Files.createDirectories(test_Path);
		            } catch (IOException e) {
		                // Fail to create directory.
		                e.printStackTrace();
		                return;
		            }
		        }
				// TMP directory.
				Path tmp_Path = Paths.get(tmpPath);
		        // Check if directory exists?
		        if (!Files.exists(tmp_Path)) {
		            try {
		            	// Create directory and parents.
		                Files.createDirectories(tmp_Path);
		            } catch (IOException e) {
		                // Fail to create directory.
		                e.printStackTrace();
		                return;
		            }
		        }
				// PROD directory.
				Path prod_Path = Paths.get(prodPath);
		        // Check if directory exists?
		        if (!Files.exists(prod_Path)) {
		            try {
		            	// Create directory and parents.
		                Files.createDirectories(prod_Path);
		            } catch (IOException e) {
		                // Fail to create directory.
		                e.printStackTrace();
		                return;
		            }
		        }
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// CLEAN UP. Remove older versions of the same zip file.
			File directory;
			if (testZip) {
				directory = new File(testPath);
			} else {
				directory = new File(tmpPath);
			}
			for (File f : directory.listFiles()) {
			    if (f.getName().startsWith(datasetName + "_version")) {
			        f.delete();
			    }
			}
			
			// CREATE ZIP.
			
			// Create "shark_data.txt".
			ZipArchiveGenerateData dataVisitor = new ZipArchiveGenerateData();
			dataset.Accept(dataVisitor);
			
			// Create "shark_data_columns.txt".
			ZipArchiveGenerateDataColumns dataColumnsVisitor = new ZipArchiveGenerateDataColumns();
			
			// Removed.
			// dataset.Accept(dataColumnsVisitor);
			
			// Create "shark_metadata_auto.txt".
			ZipArchiveGenerateMetadataAuto metadataVisitor = new ZipArchiveGenerateMetadataAuto();
			dataset.Accept(metadataVisitor);
			
			if (testZip) {
				ZipFileWriter.createZipArchive(zipFileName, datatype,
											   fromPath, testPath, 
											   dataVisitor, 
											   dataColumnsVisitor, 
											   metadataVisitor);
			} else {
				ZipFileWriter.createZipArchive(zipFileName, datatype, 
											   fromPath, tmpPath, 
											   dataVisitor, 
											   dataColumnsVisitor, 
											   metadataVisitor);
			}
			
			// COMPARE AND REPLACE IF NOT EQUAL.
			if (testZip == false) {
			
				Boolean equalZipFound = false;
				
				directory = new File(prodPath);
				for (File f : directory.listFiles()) {
				    if (f.getName().startsWith(datasetName + "_version")) {
				    	
						String tmpFileName = Paths.get(tmpPath, zipFileName).toString();
						String prodFileName = f.toString();
						
						Boolean isEqual = ZipFileCrc.compareZipfiles(tmpFileName, prodFileName);
						if (isEqual) {
							equalZipFound = true;
						} else {
							f.delete();
						}
				    }
				}
				
				try {
					File copyFrom = new File(tmpPath, zipFileName);
					File copyTo = new File(prodPath, zipFileName);
					if (equalZipFound == false) {
						Files.copy(copyFrom.toPath(), copyTo.toPath());
					}
					// Delete from TMP directory.
					
					// Sleep 0.1 sec. TEST due to failed delete.
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} 
					
					copyFrom.delete();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
