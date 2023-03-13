/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.fileimport.misc;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import se.smhi.sharkadm.datasets.fileimport.SingleFileImport;
import se.smhi.sharkadm.facades.ModelFacade;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.station.StationManager;
import se.smhi.sharkadm.station.StationObject;
import se.smhi.sharkadm.utils.GeoPosition;
import se.smhi.sharkadm.utils.ParseFileUtil;

public class FileImportStations extends SingleFileImport {
		
	public FileImportStations(PrintStream logInfo) {
		super(logInfo);
	}
	
	public void importFiles(String zipFileName, Dataset dataset) {
		
	}

	public void importFiles(String zipFileName) {
		List<String[]> fileContent;
		BufferedReader bufferedReader;
		
		try {
			bufferedReader = ParseFileUtil.GetSharkConfigFile("station.txt");

			fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
			if (fileContent != null) {				
				StationManager.instance().clearStationList();				
				importStationList(fileContent);
			}
			
		} catch (Exception e) {
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Station import");
			messageBox.setMessage("Failed to import Station list.");
			messageBox.open();
		}
		
		// Used by the Observer pattern.
		ModelFacade.instance().modelChanged();
	}
	
	private void importStationList(List<String[]> fileContent) {
		String[] header = null;
//		setExpectedColumns(stationHeader);
				
		int rowCounter = 1;
		int addedItems = 0;
		for (String[] row : fileContent) {
			if (header == null) { 
				// The first line contains the header.
				header = row;
				checkHeader(header);
			} else {
				rowCounter++;
				
				///// OLD:
				// Used columns:
				// STATION_NAME	
				// (LATITUDE_WGS84_SWEREF99_DM) // Old name.
				// (LONGITUDE_WGS84_SWEREF99_DM) // Old name.	
				// LAT_DM // Used DM or DD.	
				// LONG_DM // Used DM or DD.
				// LAT_DD // Used DM or DD.
				// LONG_DD	// Used DM or DD.
				// SYNONYM_NAMES	
				// EU_CD	
				// OUT_OF_BOUNDS_RADIUS
				
				//// NEW:
			// REG_ID
			// REG_ID_GROUP
				// STATION_NAME
				// SYNONYM_NAMES
			// ICES_STATION_NAME
				// LAT_DM
				// LONG_DM
				// LATITUDE_WGS84_SWEREF99_DD
				// LONGITUDE_WGS84_SWEREF99_DD
			// LATITUDE_SWEREF99TM
			// LONGITUDE_SWEREF99TM
				// OUT_OF_BOUNDS_RADIUS
			// WADEP
					// EU_CD
			// MEDIA
			// COMNT
			// OLD_SHARK_ID


				StationObject stationObject = new StationObject();

				
				stationObject.setStation_id(getCell(row, "REG_ID_GROUP"));
				stationObject.setSample_location_id(getCell(row, "REG_ID"));
				
				
				stationObject.setStation_name(getCell(row, "STATION_NAME"));
				stationObject.setSynonym_names(getCell(row, "SYNONYM_NAMES"));
				stationObject.setViss_eu_id(getCell(row, "EU_CD"));
				
//				if (stationObject.getStation_name().equals("SK15")) {
//					System.out.println("DEBUG");
//				}
				
				String validRadius = getCell(row, "OUT_OF_BOUNDS_RADIUS");
				if (validRadius.equals("")) {
					stationObject.setMaxValidDistance("500"); // Default value 500 m.
				} else {
					stationObject.setMaxValidDistance(validRadius);
				}

				if (!getCell(row, "LATITUDE_WGS84_SWEREF99_DD").equals("") && 
						!getCell(row, "LONGITUDE_WGS84_SWEREF99_DD").equals("")) {
//				if (!getCell(row, "LAT_DD").equals("") && 
//						!getCell(row, "LONG_DD").equals("")) {
					try {
						stationObject.setStation_position(
								new GeoPosition(getCell(row, "LATITUDE_WGS84_SWEREF99_DD"), 
												getCell(row, "LONGITUDE_WGS84_SWEREF99_DD")));
//								new GeoPosition(getCell(row, "LAT_DD"), getCell(row, "LONG_DD")));
					} catch (Exception e) {
						System.out.println("ERROR: Can't import station row. Wrong LAT_DD or LONG_DD. Row counter: " + rowCounter);
						continue;
					}
				} else {
					try {
						stationObject.setStation_position(new GeoPosition(
								GeoPosition.convertFromBiomad(getCell(row, "LAT_DM")), 
								GeoPosition.convertFromBiomad(getCell(row, "LONG_DM"))));
					} catch (Exception e) {
						System.out.println("ERROR: Can't import station row. Wrong LAT_DM or LONG_DM.  Row counter: " + rowCounter);
						continue;
					}
				}
				
				// Check position and and skip station if out of bounds. 
				if ((stationObject.getStation_position().getLatitude() > 70.0) ||
					(stationObject.getStation_position().getLatitude() < 50.0) ||
					(stationObject.getStation_position().getLongitude() > 25.0) ||
					(stationObject.getStation_position().getLongitude() < 5.0)) {

					System.out.println("ERROR: Position out of bounds. Station name: " + 
										stationObject.getStation_name());
					logInfo.println("ERROR: Position out of bounds. Station name: " + 
									stationObject.getStation_name());
					continue;
				}
				
				StationManager.instance().addStation(stationObject);

				addedItems++;
			}
		}
		logInfo.println("INFO: Added stations: " + addedItems + ".");
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
