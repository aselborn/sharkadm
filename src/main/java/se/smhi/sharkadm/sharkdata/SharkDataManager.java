/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

//package sharkadm.sharkdata;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//
//import com.jcraft.jsch.JSchException;
//import com.jcraft.jsch.SftpException;
//
//public class SharkDataManager {
//	
//	private static SharkDataManager instance = new SharkDataManager(); // Singleton.
//	
//	private SharkDataManager() { // Singleton.
//	}
//	
//	public static SharkDataManager instance() { // Singleton.
//		return instance;
//	}
//	
//	public void copyAllDatasets(Boolean production) {
//		SftpToSharkData sftpSharkData = new SftpToSharkData();
//		try {
//			sftpSharkData.connectToSharkData();
//			
//			String zipArchivePath = "\\\\winfs\\data\\prodkap\\sharkweb\\SHARKdata_datasets";
//			
////			// TODO: For test and development.
////			zipArchivePath ="SHARKdata_datasets";
//			
//			File zipArchiveFolder = new File(zipArchivePath);
//	        File[] files = zipArchiveFolder.listFiles();
//	        if (files != null) {
//		        for (File file : files) {
//		            if (file.isFile()) {
//		            	String fileName = file.getName();
//		            	if (fileName.startsWith("SHARK_")) {
//		            		sftpSharkData.copyDatasetToSharkData(zipArchivePath + "\\" + fileName, production);
//		            	}
//		            }
//		        }
//	        }
//			
//			sftpSharkData.disconnect();
//
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JSchException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SftpException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	public void deleteAllDatasets(Boolean production) {
//		SftpToSharkData sftpSharkData = new SftpToSharkData();
//		try {
//			sftpSharkData.connectToSharkData();
//			String resourceFilePath = "*.*";
//			sftpSharkData.removeDatasetFromSharkData(resourceFilePath, production);
//			sftpSharkData.disconnect();
//		} catch (JSchException e) {
//			e.printStackTrace();
//		} catch (SftpException e) {
//			e.printStackTrace();
//		}
//	}
//	
//}
