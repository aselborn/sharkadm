/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.formats;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

import se.smhi.sharkadm.datasets.fileimport.FileImportInfo;
import se.smhi.sharkadm.facades.ImportFacade;
import se.smhi.sharkadm.facades.ModelFacade;
import se.smhi.sharkadm.fileimport.misc.FileImportTranslate;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.utils.ConvUtils;
import se.smhi.sharkadm.utils.GeoPosition;
import se.smhi.sharkadm.utils.GeodesiSwedishGrids;
import se.smhi.sharkadm.utils.ParseFileUtil;

/**
 * Import format for Harbourseal.
 * 
 */
public class FormatFileHarbourseal extends FormatFileBase {

	public FormatFileHarbourseal(PrintStream logInfo, FileImportInfo importInfo) {
		super(logInfo, importInfo);
	}

	public void importFiles(String zipFileName, Dataset dataset) {
		this.dataset = dataset;

		String importMatrixColumn = "";
		if (dataset.getImport_format().contains(":")) {
			String[] strings = dataset.getImport_format().split( Pattern.quote(":"));
			importMatrixColumn = strings[1];
		} else {
			importInfo.addConcatError("Error in format description in 'delivery_note.txt'. Import aborted. ");
			return;
		}

		loadKeyTranslator(importMatrixColumn, "import_matrix_harbourseal.txt");
//		loadKeyTranslator(importMatrixColumn, "import_matrix.txt");
		dataset.setImport_matrix_column(importMatrixColumn);

		if (getTranslateKeySize() == 0) {
			importInfo
					.addConcatError("Empty column in import matrix. Import aborted.");
			return;
		}
		
		dataset.setImport_status("DATA");

		// Use translate.txt for cell content replacement, if available.
		valueTranslate = new FileImportTranslate(zipFileName);
		if (valueTranslate.isTranslateUsed()) {
			importInfo.addConcatInfo("Translate file (translate.txt) from ZIP file is used.");
		}

		// Imports the data file.
		List<String[]> fileContent;
		BufferedReader bufferedReader = null;
		Path filePath = null;
		
		try {
			if (Files.exists(Paths.get(zipFileName, "processed_data", "data.txt"))) {
				filePath = Paths.get(zipFileName, "processed_data", "data.txt");
			} else if (Files.exists(Paths.get(zipFileName, "processed_data", "data.dat"))) {
				filePath = Paths.get(zipFileName, "processed_data", "data.dat");
			} else if (Files.exists(Paths.get(zipFileName, "processed_data", "data.skv"))) {
				filePath = Paths.get(zipFileName, "processed_data", "data.skv");
			}

			//Verify that DATA.txt HAS MPROG! If not, add it, then read it.
			bufferedReader = verifyDataFile(filePath.toFile(), "MPROG");

			if (bufferedReader == null)
				bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));

			// Import of file DATA.txt.
			if (bufferedReader != null) {
				fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
				importData(fileContent);
			}


			if (Files.exists(Paths.get(zipFileName, "processed_data", "lokaler.skv"))) {
				filePath = Paths.get(zipFileName, "processed_data", "lokaler.skv");
				bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));
				fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
				if (fileContent != null) {
					importObservationPositions(fileContent);
				} else {
					importInfo.addConcatWarning("File missing or empty: indwetwt.skv.");
				}
			}			
			
		} catch (Exception e) {
			importInfo.addConcatError("FAILED TO IMPORT FILE.");
		}

		// Used by the Observer pattern.
		ModelFacade.instance().modelChanged();
	}

	private void importData(List<String[]> fileContent) {
		String[] header = null;
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Data file: " + fileContent.size()
					+ " rows (header included).");
		}
		int addedItems = 0;

		currentVisit = null;
		currentSample = null;
		currentVariable = null;

		for (String[] row : fileContent) {
			if (header == null) {
				header = row; // Header row in imported file.
				setHeaderFields(header);
			} else {
				// Create or reuse visit for this row.
				getCurrentVisit(row);

				// Create or reuse sample for this row.
				getCurrentSample(row);

				// Create community variable for this row.
				currentVariable = new Variable(true);
				currentSample.addVariable(currentVariable);

				// Add each column value.
				for (String columnName : header) {
					String key = translateKey(columnName);
					addVariableField(key, getCell(row, columnName));
				}
				addedItems++;
			}
		}
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Data file, processed rows: " + addedItems
					+ ".");
		}
	}


	
	
	
	
	
	private void importObservationPositions(List<String[]> fileContent) {
		String[] header = null;
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Observation positions file: " + fileContent.size() + " rows (header included).");
		}
		int addedItems = 0;

		// Read all rows in the file lokaler.skv. For each row check all variables in the dataset
		// and add a corresponding variable object if all keys match.

		String tmpPositionName;
		String tmpRT90_X;
		String tmpRT90_Y;
		double tmpRT90_X_double;
		double tmpRT90_Y_double;
		GeodesiSwedishGrids rt90Converter = new GeodesiSwedishGrids("rt90_2.5_gon_v");
		
		for (String[] row : fileContent) {
			if (header == null) {
				header = row; // Header row in imported file.
				setHeaderFields(header);
			} else {
				
				tmpPositionName = getCell(row, "Lokal");
				tmpRT90_X = getCell(row, "X");
				tmpRT90_Y = getCell(row, "Y");
//				double tmpRT90_lat;
//				double tmpRT90_long;
				try {
					tmpRT90_X_double = ConvUtils.convStringToDouble(tmpRT90_X);
					tmpRT90_Y_double = ConvUtils.convStringToDouble(tmpRT90_Y);
					double latLong[] = rt90Converter.grid_to_geodetic(tmpRT90_X_double, tmpRT90_Y_double);

					for (Visit tmpVisit : dataset.getVisits()) {
						if (tmpVisit.getField("visit.reported_station_name").equals(tmpPositionName)) {
							
							tmpVisit.addField("visit.visit_reported_latitude", Double.toString(latLong[0]));
							tmpVisit.addField("visit.visit_reported_longitude", Double.toString(latLong[1]));
							
							tmpVisit.setPosition(new GeoPosition(latLong[0], latLong[1]));
						}
					}					
					
				} catch (Exception e) {
					importInfo.addConcatWarning("RT 90 positions invalid. Station: " + tmpPositionName);
				}				

			}
		}
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Observation positions file, processed rows: " + addedItems + ".");
		}
	}				

	
	
	
	
	
	
	
	
	@Override
	public void getCurrentVisit(String[] row) {
		String keyString = getCellByKey(row, "visit.visit_date") + ":"
				+ getCellByKey(row, "visit.reported_station_name");
		Visit visit = null;
		for (Visit v : dataset.getVisits()) {
			if (v.getFileImportKeyString().equals(keyString)) {
				visit = v;
				currentVisit = v;
			}
		}
		if (visit == null) {
			currentVisit = new Visit(keyString);
			dataset.addVisit(currentVisit);
		}
	}

	@Override
	public void getCurrentSample(String[] row) {
		String keyString = getCellByKey(row, "visit.visit_date") + ":"
				+ getCellByKey(row, "visit.reported_station_name") + ":"
				+ getCellByKey(row, "sample.sample_id") + ":"
				+ getCellByKey(row, "sample.sample_time");
		Sample sample = null;
		for (Visit v : dataset.getVisits()) {
			for (Sample s : v.getSamples()) {
				if (s.getFileImportKeyString().equals(keyString)) {
					sample = s;
					currentSample = s;
				}
			}
		}
		if (sample == null) {
			currentSample = new Sample(keyString);
			currentVisit.addSample(currentSample);
			
			
			// currentSample.addField("sample.shark_sample_id_keystring", keyString);
			// currentSample.addField("sample.shark_sample_id_md5", StringUtils.convToMd5(keyString));
			
			
		}
	}

	@Override
	public void getCurrentVariable(String[] row) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postReorganizeDataset(Dataset dataset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postReorganizeVisit(Visit visit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postReorganizeSample(Sample sample) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postReorganizeVariable(Variable variable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postReformatDataset(Dataset dataset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postReformatVisit(Visit visit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postReformatSample(Sample sample) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postReformatVariable(Variable variable) {
		// TODO Auto-generated method stub

	}

}
