/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.fileimport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * Utility class used to read SHARK archive folders.
 */
public class SharkFolderReader {
	
	String sharkFolderName;
	File sharkFolderObject;
	
	static DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd");

	public SharkFolderReader(String sharkFolderPath) {
		this.sharkFolderName = sharkFolderPath;
	}

		
	public void addDatasetInfo(Map<String, String> targetMap) {
		
		File sharkFolderObject = new File(this.sharkFolderName);

		targetMap.put("dataset.dataset_name", sharkFolderObject.getName());
        String dateString = dateTimeFormat.format(new Date());
		targetMap.put("dataset.dataset_file_name", sharkFolderObject.getName().concat("_version_").concat(dateString).concat(".zip"));
		targetMap.put("dataset.dataset_file_path", sharkFolderObject.getPath());
		
		// Parse zip entry into key/value pairs.
		Map<String, String> info = null;
		Path deliveryNotePath = Paths.get(this.sharkFolderName, "processed_data", "delivery_note.txt");
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(deliveryNotePath.toFile()));
			info = parseDatasetNoteFile(bufferedReader);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// Check if dataset is ready for Production, or just for test.
		
		if (info.containsKey("status")) {
			targetMap.put("dataset.status", info.get("status").toLowerCase());
		} else {
			targetMap.put("dataset.status", "test");
		}
		
		// Static info.
		targetMap.put("dataset.data_holding_centre", "Swedish Meteorological and Hydrological Institute (SMHI)");
		targetMap.put("dataset.internet_access", "http://sharkweb.smhi.se, http://sharkdata.se");
		
		if (info.containsKey("landskod")) {
			targetMap.put("dataset.country_code", info.get("landskod"));
		}
		if (info.containsKey("provtagnings�r")) {
			targetMap.put("dataset.monitoring_years", info.get("provtagnings�r"));
		}
		if (info.containsKey("datatyp")) {
			targetMap.put("dataset.delivery_datatype", info.get("datatyp"));
		}
		if (info.containsKey("artliste�r")) {
			targetMap.put("dataset.species_list_year", info.get("artliste�r"));
		}
		if (info.containsKey("rapporterande institut")) {
			targetMap.put("dataset.reporting_institute_code", info.get("rapporterande institut"));
		}
		if (info.containsKey("rapporterande_institut")) { // Duplicate with underscore.
			targetMap.put("dataset.reporting_institute_code", info.get("rapporterande_institut"));
		}
		if (info.containsKey("kontaktperson")) {
			targetMap.put("dataset.reported_by", info.get("kontaktperson"));
		}
		if (info.containsKey("best�llare")) {
			targetMap.put("dataset.delivery_orderer_code", info.get("best�llare"));
		}
		if (info.containsKey("format")) {
			targetMap.put("dataset.import_format", info.get("format"));
		}
		if (info.containsKey("kommentarer")) {
			targetMap.put("dataset.dataset_comment", info.get("kommentarer"));
		}
		
		
		
		
		
		// check_status: Completed, Pending-SMHI, Test.
		// check_status: Klar, P�g�ende-SMHI, Test.
		
//		if (info.containsKey("data kontrollerad av")) {
		if (targetMap.get("dataset.delivery_datatype").equals("PhysicalChemical") || 
			targetMap.get("dataset.delivery_datatype").equals("Physical and Chemical")) {
			// Mostly for physical/chemical data.
			String checkedBy = info.get("data kontrollerad av");
			if (checkedBy.equals("Leverant�r och Datav�rd")) {
				targetMap.put("dataset.check_status_sv", "Klar");
				targetMap.put("dataset.check_status_en", "Completed");
				targetMap.put("dataset.data_checked_by_sv", checkedBy);				
				targetMap.put("dataset.data_checked_by_en", "Deliverer and Datacenter");				
			} else {
				targetMap.put("dataset.check_status_sv", "P�g�ende-SMHI");
				targetMap.put("dataset.check_status_en", "Pending-SMHI");
				targetMap.put("dataset.data_checked_by_sv", checkedBy);
				if (checkedBy.equals("Leverant�r")) {
					targetMap.put("dataset.data_checked_by_en", "Deliverer");
				} else {
					targetMap.put("dataset.data_checked_by_en", ""); // TODO: Translate later.
				}
			}
		} else {
			// Default for biology.
			targetMap.put("dataset.check_status_sv", "Klar");
			targetMap.put("dataset.check_status_en", "Completed");
			targetMap.put("dataset.data_checked_by_sv", "Leverant�r");
			targetMap.put("dataset.data_checked_by_en", "Deliverer");
		}
		
		

	}
		
	// Converts the text based Dataset file into a list of key/value pairs.
	//READS delivery_note.txt
	public Map<String, String> parseDatasetNoteFile(BufferedReader in) {
		Map<String, String> map = new HashMap<String, String>();
		try {
			String line;
			String key;
			String value;
			int commentIndex;
			int delimiterIndex;

			while ((line = in.readLine()) != null) {				
				// Remove comments.
				commentIndex = line.indexOf("#", 0);
				if (commentIndex > 0) {
					line = line.substring(0, commentIndex);
				}
				// If a ":" is found then the row contains a key/value-pair.
				delimiterIndex = line.indexOf(":", 0);
				if (delimiterIndex > 0 ) {
					key = line.substring(0, delimiterIndex).trim();
					value = line.substring(delimiterIndex + 1).trim();
					map.put(key, value);
				}
			}
			in.close();
			
		} catch (FileNotFoundException e) {
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("ParseFileUtil: File not found. ");
			messageBox.setMessage("Check delivery_note.txt in the imported zip-file." + "\n" + 
					"Error: " + e.getMessage());
			messageBox.open();
			e.printStackTrace();
//			System.exit(-1);
		} catch (Exception e) {
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("ParseFileUtil: Exception in parseDeliveryNoteFile(). ");
			messageBox.setMessage("Error: " + e.getMessage());
			messageBox.open();
			e.printStackTrace();
//			System.exit(-1);
		}
		return map;
	}

}
