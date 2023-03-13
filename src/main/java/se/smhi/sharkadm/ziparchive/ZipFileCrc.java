/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.ziparchive;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Utility class used to compare the content of two ZIP files by comparing CRC values.
 */
public class ZipFileCrc {

	public static boolean compareZipfiles(String fileName1, String fileName2) {

		Map<String, Long> crcValuesMap = new HashMap<String, Long>();
		boolean isEqual = true;
		try {
			ZipFile zf1;
			ZipFile zf2;
			try {
				zf1 = new ZipFile(fileName1);
				zf2 = new ZipFile(fileName2);
			}
			catch (Exception ex)  {
				System.err.println(ex);
				
				return false;
			}
			
			System.out.println("ZipFile 1: ");
			Enumeration e1 = zf1.entries();
			while (e1.hasMoreElements()) {
				ZipEntry ze = (ZipEntry) e1.nextElement();
				String name = ze.getName();
				
				if (! ze.isDirectory()) {
					long crc = ze.getCrc();
					System.out.println("- The CRC for " + name + " is " + crc);
					crcValuesMap.put(name, crc);
				}
			}
			zf1.close();
			
			System.out.println("ZipFile 2: ");
			Enumeration e2 = zf2.entries();
			while (e2.hasMoreElements()) {
				ZipEntry ze = (ZipEntry) e2.nextElement();
				String name = ze.getName();
				
				if (! ze.isDirectory()) {
					long crc = ze.getCrc();
					System.out.println("- The CRC for " + name + " is " + crc);
					
					if (!name.contains("shark_metadata_auto.txt")) {
						if (crcValuesMap.containsKey(name)) {
							if (crcValuesMap.get(name) == crc) {
								// Ok.
							} else {
								isEqual = false;
							}
							
						} else {
							isEqual = false;
						}
					}
				}
			}
			zf2.close();
			
		} catch (IOException ex) {
			isEqual = false;
			System.err.println(ex);
		}
		
		return isEqual;
	}
	
//	// Test.
//	public static void main(String[] args) {
//		
//		String file1 = "D:\\\\arnold\\\\2a_sharkadm\\\\w_sharkadm\\\\p_sharkadm_branch_2018\\\\SHARKdata_datasets\\\\SHARK_Epibenthos_2017_SVVAEK_YLST_version_2018-10-01.zip";
//		String file2 = "D:\\\\arnold\\\\2a_sharkadm\\\\w_sharkadm\\\\p_sharkadm_branch_2018\\\\SHARKdata_datasets\\\\SHARK_Epibenthos_2017_SVVAEK_YLST_version_2018-10-01 - Kopia.zip";
//		
//		if (compareZipfiles(file1, file2)) {
//			System.out.println("\nResult: Equal.\n");
//		} else {
//			System.out.println("\nResult: NOT equal.\n");
//		}
//	}
}