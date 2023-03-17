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
import se.smhi.sharkadm.fileimport.misc.FileImportTranslateAllColumns;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.utils.ConvUtils;
import se.smhi.sharkadm.utils.GeoPosition;
import se.smhi.sharkadm.utils.ParseFileUtil;

/**
 * Import format for Ringed seal. Swedish: Vikare.
 * Rules:
 * - Column used in the import-matrix: VIKARE.
 * - Generates one Visit for each Transect/Section. 
 * - The Transect start position is used as position for Visit.
 * - Generates one Sample if the number of seals counted > 0 (is equal to FNFLA = Y).
 * 
 * BACKLOG: Store only Samples where countnr > 0. Changes needed in Sharkweb 1.1.4 (left join in visit sql). See also postReorganizeSample and postReorganizeVariable.
 */
public class FormatFileRingedseal extends FormatFileBase {

	public FormatFileRingedseal(PrintStream logInfo, FileImportInfo importInfo) {
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

		loadKeyTranslator(importMatrixColumn, "import_matrix_ringedseal.txt");
//		loadKeyTranslator(importMatrixColumn, "import_matrix.txt");
		dataset.setImport_matrix_column(importMatrixColumn);

		if (getTranslateKeySize() == 0) {
			importInfo.addConcatError("Empty column in import matrix. Import aborted.");
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
				
				// Create station name from transect-id and section-id.
//				addVisitField("visit.reported_station_name", "(" + getCell(row, "TRANS") + ":" + getCell(row, "SECTION") + ")");
				addVisitField("visit.reported_station_name", getCell(row, "TRANS") + ":" + getCell(row, "SECTION"));
				
//				// Use Section start position for Sample. NOTE: Moved to import matrix.
//				addVisitField("visit.visit_reported_latitude", getCell(row, "START_SECTION_LAT"));
//				addVisitField("visit.visit_reported_longitude", getCell(row, "START_SECTION_LONG"));

				// Create or reuse sample for this row.
				getCurrentSample(row);

				// Use Section start position for Sample. NOTE: Moved to import matrix.
//				addSampleField("sample.sample_reported_latitude", getCell(row, "START_SECTION_LAT"));
//				addSampleField("sample.sample_reported_longitude", getCell(row, "START_SECTION_LONG"));
				
				// Create community variable for this row.
				currentVariable = new Variable(true);
				currentSample.addVariable(currentVariable);

				// Add each column value.
				for (String columnName : header) {
					String key = translateKey(columnName);
					addVariableField(key, getCell(row, columnName));
				}
				
				// Use transect start position for sample.
				if (currentSample.getField("sample.sample_reported_latitude").equals("") ||
					currentSample.getField("sample.sample_reported_longitude").equals("")) {

					String latStr = currentSample.getField("sample.section_start_latitude");
					String longStr = currentSample.getField("sample.section_start_longitude");
					if (latStr.equals("") || longStr.equals("")) {
							latStr = currentVisit.getField("visit.visit_transect_start_latitude");
							longStr = currentVisit.getField("visit.visit_transect_start_longitude");
					}
					currentSample.addField("sample.sample_reported_latitude", latStr);
					currentSample.addField("sample.sample_reported_longitude", longStr);
				}
				
				// Comment to be used on calculated parameters. See postReformatVariable(). 
				String iceObsString = "";				
				if (!getCell(row, "ICE_OBS").equals("")) {
					iceObsString = "�rets yta: " + getCell(row, "ICE_OBS") + "%. ";
				}
				String commentString = 
					"Ber�kningen baseras p� andelen inventerad isyta. " +
					iceObsString +
					"(Kan anv�ndas f�r �rsvis summering.)";
				currentVariable.addField("TEMP.variable_comment", commentString);
				
				addedItems++;
			}
		}
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Data file, processed rows: " + addedItems
					+ ".");
		}
	}

	@Override
	public void getCurrentVisit(String[] row) {
		String keyString = getCell(row, "SDATE") + ":"
		+ getCell(row, "TRANS_ID") + ":" // New.
		+ getCell(row, "OBS_LAT") + ":" // New.
		+ getCell(row, "OBS_LONG") + ":" // New.
		+ getCell(row, "TRANS") + ":" // Old.
		+ getCell(row, "SECTION"); // Old.
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
		String keyString = getCell(row, "SDATE") + ":"
		+ getCell(row, "STIME") + ":" // New.
		+ getCell(row, "TRANS_ID") + ":"
		+ getCell(row, "OBS_LAT") + ":"
		+ getCell(row, "OBS_LONG") + ":"
		+ getCell(row, "LATIT") + ":" // New.
		+ getCell(row, "LONGI") + ":" // New.
		+ getCell(row, "TRANS") + ":"
		+ getCell(row, "SECTION") + ":"
		+ getCell(row, "PICT_NUMB"); // New.
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
// BACKLOG:			// Remove Sample if species not found.
//			if (!sample.getField("sample.fauna_flora_found").equals("Y")) {
//				sample.getParent().removeSample(sample);
//			}

	}

	@Override
	public void postReorganizeVariable(Variable variable) {
// BACKLOG:			// Remove Variable if species not found.
//			if (!variable.getParent().getField("sample.fauna_flora_found").equals("Y")) {
//				variable.getParent().removeVariable(variable);
//			}
		
	}

	@Override
	public void postReformatDataset(Dataset dataset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postReformatVisit(Visit visit) {
		// Start transect.
		GeoPosition geoPos = null;
		String startLatitude = visit.getField("visit.visit_transect_start_latitude");
		String startLongitude = visit.getField("visit.visit_transect_start_longitude");
		geoPos = convertLatLong(startLatitude, startLongitude);
		if (geoPos != null) {
			visit.addField("visit.visit_transect_start_latitude_dd", geoPos.getLatitudeAsString());
			visit.addField("visit.visit_transect_start_longitude_dd", geoPos.getLongitudeAsString());
			visit.addField("visit.visit_transect_start_latitude_dm", GeoPosition.convertToDM(geoPos.getLatitude()));
			visit.addField("visit.visit_transect_start_longitude_dm", GeoPosition.convertToDM(geoPos.getLongitude()));
		}
		
		// End transect.
		String endLatitude = visit.getField("visit.visit_transect_end_latitude");
		String endLongitude = visit.getField("visit.visit_transect_end_longitude");
		geoPos = null;
		geoPos = convertLatLong(endLatitude, endLongitude);
		if (geoPos != null) {
			visit.addField("visit.visit_transect_end_latitude_dd", geoPos.getLatitudeAsString());
			visit.addField("visit.visit_transect_end_longitude_dd", geoPos.getLongitudeAsString());
			visit.addField("visit.visit_transect_end_latitude_dm", GeoPosition.convertToDM(geoPos.getLatitude()));
			visit.addField("visit.visit_transect_end_longitude_dm", GeoPosition.convertToDM(geoPos.getLongitude()));
		}
	}

	private GeoPosition convertLatLong(String latString, String longString) {

		Double latitude = null;
		Double longitude = null;
		GeoPosition geoPos = null;
		try {
			latitude = ConvUtils.convStringToDouble(latString.replace(",", "."));
			longitude = ConvUtils.convStringToDouble(longString.replace(",", "."));
		} catch (Exception e) {
			importInfo.addConcatWarning("Lat/long error. Lat: " + latString +
										" Long: " + longString);
		}
		if ((latitude != null) && (longitude != null)) { 
			if ((latitude < 90.0) && (longitude < 90.0)) { // As DD, decimal degree.
				geoPos = new GeoPosition(latitude, longitude);
			}
			else {			
				geoPos = new GeoPosition(
					GeoPosition.convertFromBiomad(latString), 
					GeoPosition.convertFromBiomad(longString));
			}
		}else {
			System.out.println("Lat/long = null");
		}
		return geoPos;
	}

	@Override
	public void postReformatSample(Sample sample) {
		// Start section.
		GeoPosition geoPos = null;
		String startLatitude = sample.getField("sample.section_start_latitude");
		String startLongitude = sample.getField("sample.section_start_longitude");
		geoPos = convertLatLong(startLatitude, startLongitude);
		if (geoPos != null) {
			sample.addField("sample.section_start_latitude_dd", geoPos.getLatitudeAsString());
			sample.addField("sample.section_start_longitude_dd", geoPos.getLongitudeAsString());
			sample.addField("sample.section_start_latitude_dm", GeoPosition.convertToDM(geoPos.getLatitude()));
			sample.addField("sample.section_start_longitude_dm", GeoPosition.convertToDM(geoPos.getLongitude()));
		}
		
		// End section.
		String endLatitude = sample.getField("sample.section_end_latitude");
		String endLongitude = sample.getField("sample.section_end_longitude");
		geoPos = null;
		geoPos = convertLatLong(endLatitude, endLongitude);
		if (geoPos != null) {
			sample.addField("sample.section_end_latitude_dd", geoPos.getLatitudeAsString());
			sample.addField("sample.section_end_longitude_dd", geoPos.getLongitudeAsString());
			sample.addField("sample.section_end_latitude_dm", GeoPosition.convertToDM(geoPos.getLatitude()));
			sample.addField("sample.section_end_longitude_dm", GeoPosition.convertToDM(geoPos.getLongitude()));
		}
	}

	@Override
	public void postReformatVariable(Variable variable) {
		// Add QFLAG stored as TempField.  
		for (String tempKey : variable.getTempFieldKeys().toArray(new String[variable.getTempFieldKeys().size()])) {
			if (tempKey.startsWith("TEMP.QFLAG." + variable.getParameter())) {
				
				variable.addField("variable.quality_flag", variable.getTempField(tempKey));
				variable.removeTempField(tempKey);				

				// Translate.
				String qualityFlagValue = variable.getField("variable.quality_flag");
				qualityFlagValue = FileImportTranslateAllColumns.instance().translateValue("quality_flag", qualityFlagValue);
				variable.addField("variable.quality_flag", qualityFlagValue);
			}
		}

		// Calculate new parameters/units.
		try {
			if (variable.getParameter().equals("# counted")) {
				Double value = ConvUtils.convStringToDouble(variable.getValue());
				Double coefficient = ConvUtils.convStringToDouble(
						variable.getField("variable.coefficient"));
				if ((value != null) &&
					(coefficient != null)) {

					// VALUE = COUNTNR * COEFF
					Double param = value * coefficient;
//					param = Math.round(param * 10000.0) / 10000.0; // 4 decimals.
	
					Variable newVariable = variable.copyVariableAndData();
					variable.getParent().addVariable(newVariable);						
					newVariable.setParameter("Calculated # counted");
//					newVariable.setValue(param.toString());
					newVariable.setValue(ConvUtils.convDoubleToString(param));
					newVariable.setUnit("ind");
					newVariable.addField("variable.calc_by_dc", "Y");
					// Add comment.
					newVariable.addField("variable.variable_comment", 
										 variable.getField("TEMP.variable_comment"));
				}
			}
		} catch (Exception e) {
			importInfo.addConcatWarning("Failed to calculate value. Parameter:" + variable.getParameter());
		}

	}

}
