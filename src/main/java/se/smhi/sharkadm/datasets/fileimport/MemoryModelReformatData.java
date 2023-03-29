/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.fileimport;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import se.smhi.sharkadm.datasets.columns.ColumnInfoManager;
import se.smhi.sharkadm.datasets.formats.FormatBase;
import se.smhi.sharkadm.location.VisitLocationManager;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.ModelVisitor;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.species.TaxaWorms;
import se.smhi.sharkadm.species.TrophicTypeManager;
import se.smhi.sharkadm.species_old.TaxonManager;
import se.smhi.sharkadm.species_old.TaxonNode;
import se.smhi.sharkadm.sql.SqliteManager;
import se.smhi.sharkadm.station.StationManager;
import se.smhi.sharkadm.translate.TranslateCodesManager;
import se.smhi.sharkadm.translate.TranslateCodesManager_NEW;
import se.smhi.sharkadm.translate.TranslateCodesObject_NEW;
import se.smhi.sharkadm.utils.ConvUtils;
import se.smhi.sharkadm.utils.GeoPosition;
import se.smhi.sharkadm.utils.GeodesiSwedishGrids;
import se.smhi.sharkadm.utils.StringUtils;

/**
 *	The code is based on the Visitor pattern.
 */
public class MemoryModelReformatData extends ModelVisitor {

	protected PrintStream logInfo;
	protected FileImportInfo importInfo;
	private FormatBase fileImport;
	private FileImportUtils utils;

	TranslateCodesManager translateCodes = new TranslateCodesManager();
	TranslateCodesManager_NEW translateCodes_NEW = TranslateCodesManager_NEW.instance();
	ColumnInfoManager columnInfoManager = new ColumnInfoManager();
	TrophicTypeManager trophicTypeManager = TrophicTypeManager.instance();

	private Double maxSectionDistanceEnd = -99.0;
	private String maxSectionDistanceEndString = "";
	
	public MemoryModelReformatData(PrintStream logInfo, FileImportInfo importInfo, FormatBase fileImport) {
		this.logInfo = logInfo;
		this.importInfo = importInfo;
		this.fileImport = fileImport;
		this.utils = new FileImportUtils(importInfo);
		// 
		translateCodes.importTranslateCodesFile();
	}
		
	@Override
	public void visitDataset(Dataset dataset) {

//		// Translate code values to internal codes.
//		String delivererCode = translateCodes.translateSynonym("LABO", dataset.getField("dataset.reporting_institute_code"), importInfo);
////		dataset.addField("dataset.reporting_institute_code", delivererCode);
//
//		// Translate internal codes to Swedish and English.
//		if (delivererCode.equals("")) {
//			dataset.addField("dataset.reporting_institute_name_sv", "-");
//			dataset.addField("dataset.reporting_institute_name_en", "-");
//		} else {
//			dataset.addField("dataset.reporting_institute_name_sv", 
//							 translateCodes.translateToSwedishName("LABO", delivererCode));
//			dataset.addField("dataset.reporting_institute_name_en", 
//					 		 translateCodes.translateToEnglishName("LABO", delivererCode));
//		}
		
		// Used by the Visitor pattern.
		for (Visit visit : dataset.getVisits()) {			
			visit.Accept(this);
		}

		// Makes it possible for import format specific actions.
		if (fileImport != null) {
			fileImport.postReformatDataset(dataset);
		}
		
		// Translate from code synonyms.
		try {
			List<String> fieldKeys = new ArrayList<String>(dataset.getFieldKeys());
			for (String internalKey : fieldKeys) {
				String fieldKey = columnInfoManager.getColumnInfoObjectFromInternalKey(internalKey).getKey();
				String fieldValue = dataset.getField(internalKey);
				if (!fieldKey.equals("") && !fieldValue.equals("")) {
					String translatedValue = translateCodes_NEW.translateSynonym(fieldKey, fieldValue);
					if (!translatedValue.equals("")) {
						if (!fieldValue.equals(translatedValue)) {
						dataset.addField(internalKey, translatedValue);
						importInfo.addConcatWarning("Code translated. Field: " + fieldKey +
								"   Old value: " + fieldValue +
								"   New value: " + translatedValue);
						}
					} else {
						importInfo.addConcatWarning("Code not found. Field: " + fieldKey +
								"   Value: " + fieldValue);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Translate reporting institute.
		String delivererCode = dataset.getField("dataset.reporting_institute_code");
		if (delivererCode.equals("")) {
			dataset.addField("dataset.reporting_institute_name_sv", "-");
			dataset.addField("dataset.reporting_institute_name_en", "-");
		} else {
			TranslateCodesObject_NEW object = translateCodes_NEW.getCodeObject("reporting_institute_code", delivererCode);
			if (object != null) {
				dataset.addField("dataset.reporting_institute_name_sv", object.getSwedish());
				dataset.addField("dataset.reporting_institute_name_en", object.getEnglish());
			}
		}

		// Final cleanup on field level.
		try {
			List<String> fieldKeys = new ArrayList<String>(dataset.getFieldKeys());
			for (String internalKey : fieldKeys) {
				// Adjust decimal and float values.
				String fieldValue = dataset.getField(internalKey);
				String fieldFormat = columnInfoManager.getColumnInfoObjectFromInternalKey(internalKey).getFieldFormat();
				if (fieldFormat.equals("float")) {
					dataset.addField(internalKey, fieldValue.replace(",", ".").replace(" ", ""));
				}
				else if (fieldFormat.equals("decimal")) {
					dataset.addField(internalKey, fieldValue.replace(",", ".").replace(" ", ""));
				} 
				else if (fieldFormat.equals("pos-dd")) {
					dataset.addField(internalKey, fieldValue.replace(",", ".").replace(" ", ""));
				} 
				else if (fieldFormat.equals("pos-dm")) {
					dataset.addField(internalKey, fieldValue.replace(",", "."));
				} 
				else if (fieldFormat.equals("date")) {
					dataset.addField(internalKey, utils.convDate(fieldValue));
				} 
				else if (fieldFormat.equals("time")) {
					dataset.addField(internalKey, utils.convTime(fieldValue));
				} 
				else if (fieldFormat.equals("time-h")) {
					dataset.addField(internalKey, utils.convTimeHour(fieldValue));
				} 
				else if (fieldFormat.equals("time-ms")) {
					dataset.addField(internalKey, utils.convTimeMinSec(fieldValue));
				} 
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	@Override
	public void visitVisit(Visit visit) {
		
		adjustVisitFields(visit);
		
		// visit_year		
		if (visit.containsField("visit.visit_year")) {
			// Convert format.
			visit.addField("visit.visit_year", 
					utils.convNoDecimal(visit.getField("visit.visit_year")));
		} else {
			if (visit.getField("visit.visit_date").length() >= 4) {
				visit.addField("visit.visit_year", visit.getField("visit.visit_date").substring(0, 4));
			}
		}
		
		// visit_date
		if (visit.containsField("visit.visit_date")) {
			
			// Convert format.
			visit.addField("visit.visit_date", 
					utils.convDate(visit.getField("visit.visit_date")));
		}
		
		// visit_month
		if (visit.getField("visit.visit_date").length() >= 10) {
			visit.addField("visit.visit_month", visit.getField("visit.visit_date").substring(5, 7));
		}
		
		// visit_day
		if (visit.getField("visit.visit_date").length() >= 10) {
			visit.addField("visit.visit_day", visit.getField("visit.visit_date").substring(8, 10));
		}

		// visit_position
		if ((visit.containsField("visit.visit_reported_latitude")) &&
			(visit.containsField("visit.visit_reported_longitude"))) {
			Double latitude = 0.0;
			Double longitude = 0.0;
			try {
				latitude = ConvUtils.convStringToDouble(visit.getField(
						"visit.visit_reported_latitude").replace(",",
						"."));
				longitude = ConvUtils.convStringToDouble(visit.getField(
						"visit.visit_reported_longitude").replace(",",
						"."));
			} catch (Exception e) {
				importInfo.addConcatWarning("Lat/long error. Lat: " + 
						visit.getField("visit.visit_reported_latitude") + " Long: " +
						visit.getField("visit.visit_reported_longitude"));
			}
			if ((latitude != null) && (longitude != null)) { 
				if ((latitude < 90.0) && (longitude < 90.0)) { // As DD, decimal degree.
					visit.setPosition(new GeoPosition(latitude, longitude));
				}
				else if ((latitude <= 9999999.0) && (latitude >= 1000000.0) &&
						 (longitude <= 9999999.0) && (longitude >= 1000000.0)) { // As RT90.
					// From RT90 to lat/long.
					GeodesiSwedishGrids rt90 = new GeodesiSwedishGrids("rt90_2.5_gon_v");
					double[] lat_long = rt90.grid_to_geodetic(latitude, longitude);					
					visit.setPosition(new GeoPosition(lat_long[0], lat_long[1]));
				} 
				else if ((latitude <= 9999999.0) && (latitude >= 1000000.0) &&
						 (longitude <= 999999.0) && (longitude >= 100000.0)) { // As SWEREF 99 TM.
					// From RT90 to lat/long.
					GeodesiSwedishGrids sweref99tm = new GeodesiSwedishGrids("sweref_99_tm");
					double[] lat_long = sweref99tm.grid_to_geodetic(latitude, longitude);					
					visit.setPosition(new GeoPosition(lat_long[0], lat_long[1]));
				} 
				else {			
					visit.setPosition(new GeoPosition(
						GeoPosition.convertFromBiomad(visit.getField("visit.visit_reported_latitude")), 
						GeoPosition.convertFromBiomad(visit.getField("visit.visit_reported_longitude"))));
				}
			}else {
				System.out.println("Lat/long = null");
			}
		} 
		else if ((visit.containsField("visit.visit_latitude_dd")) &&
				 (visit.containsField("visit.visit_longitude_dd"))) {

			String lat_dd = visit.getField("visit.visit_latitude_dd");
			String long_dd = visit.getField("visit.visit_longitude_dd");
			visit.setPosition(new GeoPosition(lat_dd, long_dd));
			visit.addField("visit.visit_reported_latitude", lat_dd);
			visit.addField("visit.visit_reported_longitude", long_dd);
		}
		else if ((visit.containsField("visit.latitude_deg")) &&
				 (visit.containsField("visit.latitude_min")) &&
				 (visit.containsField("visit.longitude_deg")) &&
				 (visit.containsField("visit.longitude_min"))) {

			String lat_deg = visit.getField("visit.latitude_deg");
			String lat_min = visit.getField("visit.latitude_min");
			String long_deg = visit.getField("visit.longitude_deg");
			String long_min = visit.getField("visit.longitude_min");
			visit.setPosition(new GeoPosition(
					GeoPosition.convertFromDegMin(lat_deg, lat_min), 
					GeoPosition.convertFromDegMin(long_deg, long_min)));
			visit.addField("visit.visit_reported_latitude", lat_deg + ' ' + lat_min);
			visit.addField("visit.visit_reported_longitude", long_deg + ' ' + long_min);

		} else {
			importInfo.addConcatInfo("Latitude/longitude is not reported on visit level.");
		}			
		
		// Add extra comments to visit_comment.
		if ((visit.containsField("visit.TEMP.add_to_visit_comment")) || 
			(visit.containsField("visit.TEMP.add_to_visit_comment_1")) || 
			(visit.containsField("visit.TEMP.add_to_visit_comment_2"))) {
			String accComment = visit.getField("visit.visit_comment");
			String addComment = visit.getField("visit.TEMP.add_to_visit_comment");
			String addComment1 = visit.getField("visit.TEMP.add_to_visit_comment_1");
			String addComment2 = visit.getField("visit.TEMP.add_to_visit_comment_2");			
			if (!addComment.equals("")) {
				if (accComment.equals("")) { accComment = addComment; }
				else                       { accComment += " / " + addComment; }
			}
			if (!addComment1.equals("")) {
				if (accComment.equals("")) { accComment = addComment1; }
				else                       { accComment += " / " + addComment1; }
			}
			if (!addComment2.equals("")) {
				if (accComment.equals("")) { accComment = addComment2; }
				else                       { accComment += " / " + addComment2; }
			}
			visit.addField("visit.visit_comment", accComment);				
			visit.removeField("visit.TEMP.add_to_visit_comment");
			visit.removeField("visit.TEMP.add_to_visit_comment_1");
			visit.removeField("visit.TEMP.add_to_visit_comment_2");
		}
		
		
	
////////////////////////////////////
		this.maxSectionDistanceEnd = -99.0;
		this.maxSectionDistanceEndString = "";
////////////////////////////////////
		
		
		// Used by the Visitor pattern.
		for (Sample sample : visit.getSamples()) {			
			sample.Accept(this);
		}
				
		
////////////////////////////////////
		if (!visit.containsField("visit.transect_length_m")) {
			if (this.maxSectionDistanceEnd != -99.0) {
				visit.addField("visit.transect_length_m", this.maxSectionDistanceEndString);
			}
		}
////////////////////////////////////
		
		
		// Extra loop over samples. Adds position from visit level if missing in sample.
		for (Sample sample : visit.getSamples()) {
			if ((sample.getPosition().getLatitude() == 0.0) && 
				(sample.getPosition().getLongitude() == 0.0)) { 
				sample.setPosition(visit.getPosition());
			}
		}
		
		// station_name
		if (visit.containsField("visit.reported_station_name")) {
			visit.setStationObject(StationManager.instance().getStationObjectByNameAndDistance(
							visit.getField("visit.reported_station_name"),
							visit.getPosition(), importInfo));
		}
		if (visit.getStationObject() != null) {
			visit.addField("visit.station_name", visit.getStationObject().getStation_name());
			
			
			visit.addField("visit.station_id", visit.getStationObject().getStation_id());
			visit.addField("visit.sample_location_id", visit.getStationObject().getSample_location_id());

			
			visit.addField("visit.station_viss_eu_id", visit.getStationObject().getViss_eu_id());
			visit.addField("visit.station_name_matched", "Y");
		} 
		else {
			visit.addField("visit.station_name", visit.getField("visit.reported_station_name"));
			visit.addField("visit.station_name_matched", "N");
		}

		// Calculate visit_location_id.
		String locationId = VisitLocationManager.calculateKey(
				visit.getPosition().getLatitude(), 
				visit.getPosition().getLongitude());
		visit.addField("visit.visit_location_id", locationId);
		
		// Makes it possible for import format specific actions.
		if (fileImport != null) {
			fileImport.postReformatVisit(visit);
		}
		
		// Translate from code synonyms.
		try {
			List<String> fieldKeys = new ArrayList<String>(visit.getFieldKeys());
			for (String internalKey : fieldKeys) {
				String fieldKey = columnInfoManager.getColumnInfoObjectFromInternalKey(internalKey).getKey();
				String fieldValue = visit.getField(internalKey);
				if (!fieldKey.equals("") && !fieldValue.equals("")) {
					String translatedValue = translateCodes_NEW.translateSynonym(fieldKey, fieldValue);
					if (!translatedValue.equals("")) {
						if (!fieldValue.equals(translatedValue)) {
						visit.addField(internalKey, translatedValue);
						importInfo.addConcatWarning("Code translated. Field: " + fieldKey +
								"   Old value: " + fieldValue +
								"   New value: " + translatedValue);
						}
					} else {
						importInfo.addConcatWarning("Code not found. Field: " + fieldKey +
								"   Value: " + fieldValue);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Final cleanup on field level.
		try {
			List<String> fieldKeys = new ArrayList<String>(visit.getFieldKeys());
			for (String internalKey : fieldKeys) {
				// Adjust decimal and float values.
				String fieldValue = visit.getField(internalKey);
				String fieldFormat = columnInfoManager.getColumnInfoObjectFromInternalKey(internalKey).getFieldFormat();
				if (fieldFormat.equals("float")) {
					visit.addField(internalKey, fieldValue.replace(",", ".").replace(" ", ""));
				}
				else if (fieldFormat.equals("decimal")) {
					visit.addField(internalKey, fieldValue.replace(",", ".").replace(" ", ""));
				} 
				else if (fieldFormat.equals("pos-dd")) {
					visit.addField(internalKey, fieldValue.replace(",", ".").replace(" ", ""));
				} 
				else if (fieldFormat.equals("pos-dm")) {
					visit.addField(internalKey, fieldValue.replace(",", "."));
				} 
				else if (fieldFormat.equals("date")) {
					visit.addField(internalKey, utils.convDate(fieldValue));
				} 
				else if (fieldFormat.equals("time")) {
					visit.addField(internalKey, utils.convTime(fieldValue));
				} 
				else if (fieldFormat.equals("time-h")) {
					visit.addField(internalKey, utils.convTimeHour(fieldValue));
				} 
				else if (fieldFormat.equals("time-ms")) {
					visit.addField(internalKey, utils.convTimeMinSec(fieldValue));
				} 
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	@Override
	public void visitSample(Sample sample) {

		adjustSampleFields(sample);

		// sample_datatype
		if ( ! sample.containsField("sample.sample_datatype")) {
			// Get data type from dataset.
			sample.addField("sample.sample_datatype", sample.getParent().getParent().getField("dataset.delivery_datatype"));
		}
		
		// sample_date
		if (!sample.containsField("sample.sample_date")) {
			// Get sample date from visit.
			sample.addField("sample.sample_date", sample.getParent().getField("visit.visit_date"));
		}
		
		// sample_time
		if (sample.containsField("sample.sample_time")) {
			sample.addField("sample.sample_time", utils.convTime(sample.getField("sample.sample_time")));
		} else {
//			importInfo.addConcatInfo("Sample: sample_time...TODO"); // TODO 
		}
		
		// sample_month
		if (sample.getField("sample.sample_date").length() >= 10) {
			sample.addField("sample.sample_month", sample.getField("sample.sample_date").substring(5, 7));
		}
		
		// sample_position
		if ((sample.containsField("sample.sample_reported_latitude")) &&
			(sample.containsField("sample.sample_reported_longitude"))) {
			Double latitude = 0.0;
			Double longitude = 0.0;
			try {
				latitude = ConvUtils.convStringToDouble(sample.getField(
						"sample.sample_reported_latitude").replace(",",
						"."));
				longitude = ConvUtils.convStringToDouble(sample.getField(
						"sample.sample_reported_longitude").replace(",",
						"."));
				if ((latitude < 90.0) && (longitude < 90.0)) { // As DD, decimal degree.
					sample.setPosition(new GeoPosition(latitude, longitude));
				} else {			
					sample.setPosition(new GeoPosition(
							GeoPosition.convertFromBiomad(sample.getField("sample.sample_reported_latitude")), 
							GeoPosition.convertFromBiomad(sample.getField("sample.sample_reported_longitude"))));
				}
			} catch (Exception e) {
				importInfo.addConcatWarning("Lat/long error. Lat: " + 
						sample.getField("sample.sample_reported_latitude") + " Long: " +
						sample.getField("sample.sample_reported_longitude"));
			}
		}

		// Use position from sample on visit level if visit position is missing or not reported.
		if ((sample.getParent().getPosition().getLatitude() == 0.0) && 
			(sample.getParent().getPosition().getLongitude() == 0.0)) {
			sample.getParent().setPosition(sample.getPosition());
		}
				
		// Transect start/stop latitude/longitude;
		reformatPositionStringsDD(sample, "sample.transect_start_latitude_dd", "sample.transect_start_longitude_dd");
		reformatPositionStringsDD(sample, "sample.transect_end_latitude_dd", "sample.transect_end_longitude_dd");
		reformatPositionStringsDD(sample, "sample.section_start_latitude_dd", "sample.section_start_longitude_dd");
		reformatPositionStringsDD(sample, "sample.section_end_latitude_dd", "sample.section_end_longitude_dd");
		
		reformatPositionStringsDM(sample, "sample.transect_start_latitude_dm", "sample.transect_start_longitude_dm");		
		reformatPositionStringsDM(sample, "sample.transect_end_latitude_dm", "sample.transect_end_longitude_dm");
		reformatPositionStringsDM(sample, "sample.section_start_latitude_dm", "sample.section_start_longitude_dm");		
		reformatPositionStringsDM(sample, "sample.section_end_latitude_dm", "sample.section_end_longitude_dm");
		
		if ((sample.containsField("sample.transect_start_latitude_dd")) &&
			(sample.containsField("sample.transect_start_longitude_dd")) &&
			(!sample.containsField("sample.transect_start_latitude_dm")) &&
			(!sample.containsField("sample.transect_start_longitude_dm"))) {
			Double latitude = 0.0;
			Double longitude = 0.0;
			try {
				latitude = ConvUtils.convStringToDouble(sample.getField(
						"sample.transect_start_latitude_dd").replace(",",
						"."));
				longitude = ConvUtils.convStringToDouble(sample.getField(
						"sample.transect_start_longitude_dd").replace(",",
						"."));				
				sample.addField("sample.transect_start_latitude_dm", GeoPosition.convertToDM(latitude));
				sample.addField("sample.transect_start_longitude_dm", GeoPosition.convertToDM(longitude));
			} catch (Exception e) {
				importInfo.addConcatWarning("Transect start latitude/longitude.");
			}
		}
		if (
			(sample.containsField("sample.transect_end_latitude_dd")) &&
			(sample.containsField("sample.transect_end_longitude_dd")) &&
			(!sample.containsField("sample.transect_end_latitude_dm")) &&
			(!sample.containsField("sample.transect_end_longitude_dm"))) {
			Double latitude = 0.0;
			Double longitude = 0.0;
			try {
				latitude = ConvUtils.convStringToDouble(sample.getField(
						"sample.transect_end_latitude_dd").replace(",",
						"."));
				longitude = ConvUtils.convStringToDouble(sample.getField(
						"sample.transect_end_longitude_dd").replace(",",
						"."));				
				sample.addField("sample.transect_end_latitude_dm", GeoPosition.convertToDM(latitude));
				sample.addField("sample.transect_end_longitude_dm", GeoPosition.convertToDM(longitude));
			} catch (Exception e) {
				importInfo.addConcatWarning("Transect stop latitude/longitude.");
			}
		}
		if (
			(sample.containsField("sample.section_start_latitude_dd")) &&
			(sample.containsField("sample.section_start_longitude_dd")) &&
			(!sample.containsField("sample.section_start_latitude_dm")) &&
			(!sample.containsField("sample.section_start_longitude_dm"))) {
			Double latitude = 0.0;
			Double longitude = 0.0;
			try {
				latitude = ConvUtils.convStringToDouble(sample.getField(
						"sample.section_start_latitude_dd").replace(",",
						"."));
				longitude = ConvUtils.convStringToDouble(sample.getField(
						"sample.section_start_longitude_dd").replace(",",
						"."));				
				sample.addField("sample.section_start_latitude_dm", GeoPosition.convertToDM(latitude));
				sample.addField("sample.section_start_longitude_dm", GeoPosition.convertToDM(longitude));
			} catch (Exception e) {
				importInfo.addConcatWarning("Section start latitude/longitude.");
			}
		}
		if (
			(sample.containsField("sample.section_end_latitude_dd")) &&
			(sample.containsField("sample.section_end_longitude_dd")) &&
			(!sample.containsField("sample.section_end_latitude_dm")) &&
			(!sample.containsField("sample.section_end_longitude_dm"))) {
			Double latitude = 0.0;
			Double longitude = 0.0;
			try {
				latitude = ConvUtils.convStringToDouble(sample.getField(
						"sample.section_end_latitude_dd").replace(",",
						"."));
				longitude = ConvUtils.convStringToDouble(sample.getField(
						"sample.section_end_longitude_dd").replace(",",
						"."));				
				sample.addField("sample.section_end_latitude_dm", GeoPosition.convertToDM(latitude));
				sample.addField("sample.transect_end_longitude_dm", GeoPosition.convertToDM(longitude));
			} catch (Exception e) {
				importInfo.addConcatWarning("Section stop latitude/longitude.");
			}
		}

		// Transect start/stop latitude/longitude;
		if ((!sample.containsField("sample.transect_start_latitude_dd")) &&
			(!sample.containsField("sample.transect_start_longitude_dd")) &&
			(sample.containsField("sample.transect_start_latitude_dm")) &&
			(sample.containsField("sample.transect_start_longitude_dm"))) {
			try {
				sample.addField("sample.transect_start_latitude_dd", new Double(GeoPosition.convertFromBiomad(sample.getField("sample.transect_start_latitude_dm"))).toString());
				sample.addField("sample.transect_start_longitude_dd", new Double(GeoPosition.convertFromBiomad(sample.getField("sample.transect_start_longitude_dm"))).toString());
			} catch (Exception e) {
				importInfo.addConcatWarning("Transect start latitude/longitude.");
			}
		}
		if (
			(!sample.containsField("sample.transect_end_latitude_dd")) &&
			(!sample.containsField("sample.transect_end_longitude_dd")) &&
			(sample.containsField("sample.transect_end_latitude_dm")) &&
			(sample.containsField("sample.transect_end_longitude_dm"))) {
			try {
				sample.addField("sample.transect_end_latitude_dd", new Double(GeoPosition.convertFromBiomad(sample.getField("sample.transect_end_latitude_dm"))).toString());
				sample.addField("sample.transect_end_longitude_dd", new Double(GeoPosition.convertFromBiomad(sample.getField("sample.transect_end_longitude_dm"))).toString());
			} catch (Exception e) {
				importInfo.addConcatWarning("Transect stop latitude/longitude.");
			}
		}
		if (
			(!sample.containsField("sample.section_start_latitude_dd")) &&
			(!sample.containsField("sample.section_start_longitude_dd")) &&
			(sample.containsField("sample.section_start_latitude_dm")) &&
			(sample.containsField("sample.section_start_longitude_dm"))) {
			try {
				sample.addField("sample.section_start_latitude_dd", new Double(GeoPosition.convertFromBiomad(sample.getField("sample.section_start_latitude_dm"))).toString());
				sample.addField("sample.section_start_longitude_dd", new Double(GeoPosition.convertFromBiomad(sample.getField("sample.section_start_longitude_dm"))).toString());
			} catch (Exception e) {
				importInfo.addConcatWarning("Section start latitude/longitude.");
			}
		}
		if (
			(!sample.containsField("sample.section_end_latitude_dd")) &&
			(!sample.containsField("sample.section_end_longitude_dd")) &&
			(sample.containsField("sample.section_end_latitude_dm")) &&
			(sample.containsField("sample.section_end_longitude_dm"))) {
			try {
				sample.addField("sample.section_end_latitude_dd", new Double(GeoPosition.convertFromBiomad(sample.getField("sample.section_end_latitude_dm"))).toString());
				sample.addField("sample.section_end_longitude_dd", new Double(GeoPosition.convertFromBiomad(sample.getField("sample.section_end_longitude_dm"))).toString());
			} catch (Exception e) {
				importInfo.addConcatWarning("Section stop latitude/longitude.");
			}
		}

		// If lat/long is reported on transect or section level only.
		if ((sample.getPosition().getLatitude() == 0.0) &&
			(sample.getPosition().getLongitude() == 0.0)) {
			// Transect.
			if ((sample.containsField("sample.transect_start_latitude_dd")) &&
				(sample.containsField("sample.transect_start_longitude_dd"))) {
				Double latitude = 0.0;
				Double longitude = 0.0;
				try {
					latitude = ConvUtils.convStringToDouble(sample.getField(
							"sample.transect_start_latitude_dd").replace(",",
							"."));
					longitude = ConvUtils.convStringToDouble(sample.getField(
							"sample.transect_start_longitude_dd").replace(",",
							"."));
					if ((latitude < 90.0) && (longitude < 90.0)) { // As DD, decimal degree.
						sample.setPosition(new GeoPosition(latitude, longitude));
						
						// Check if visit position is empty and needs to be set.
						if ((sample.getParent().getPosition().getLatitude() == 0.0) &&
							(sample.getParent().getPosition().getLongitude() == 0.0)) {
							sample.getParent().setPosition(new GeoPosition(latitude, longitude));
						}
					}
				} catch (Exception e) {
					importInfo.addConcatWarning("Lat/long error. Lat: " + 
							sample.getField("sample.transect_start_latitude_dd") + " Long: " +
							sample.getField("sample.transect_start_longitude_dd"));
				}
			}
			// Section.
			else if ((sample.containsField("sample.section_start_latitude_dd")) &&
					 (sample.containsField("sample.section_start_longitude_dd"))) {
				Double latitude = 0.0;
				Double longitude = 0.0;
				try {
					latitude = ConvUtils.convStringToDouble(sample.getField(
							"sample.section_start_latitude_dd").replace(",",
							"."));
					longitude = ConvUtils.convStringToDouble(sample.getField(
							"sample.section_start_longitude_dd").replace(",",
							"."));
					if ((latitude < 90.0) && (longitude < 90.0)) { // As DD, decimal degree.
						sample.setPosition(new GeoPosition(latitude, longitude));
						
						// Check if visit position is empty and needs to be set.
						if ((sample.getParent().getPosition().getLatitude() == 0.0) &&
							(sample.getParent().getPosition().getLongitude() == 0.0)) {
							sample.getParent().setPosition(new GeoPosition(latitude, longitude));
						}
					}
				} catch (Exception e) {
					importInfo.addConcatWarning("Lat/long error. Lat: " + 
							sample.getField("sample.section_start_latitude_dd") + " Long: " +
							sample.getField("sample.section_start_longitude_dd"));
				}
			}
		}
			
		// sample_min_depth_m
		if (sample.containsField("sample.sample_min_depth_m")) {
			// OK.
		} 
		else if (sample.containsField("sample.sample_depth_m")) {
			sample.addField("sample.sample_min_depth_m", sample.getField("sample.sample_depth_m"));
		} 
		else {
//			importInfo.addConcatInfo("Sample: sample_min_depth_m...TODO"); // TODO 
		}
		
		// sample_max_depth_m
		if (sample.containsField("sample.sample_max_depth_m")) {
			// OK.
		} 
		else if (sample.containsField("sample.sample_depth_m")) {
			sample.addField("sample.sample_max_depth_m", sample.getField("sample.sample_depth_m"));
		} 
		else {
//			importInfo.addConcatInfo("Sample: sample_max_depth_m...TODO"); // TODO 
		}
		
		
		/////////////////////////////////////		
		// Epibenthos: sample_min_depth_m for transect/section data.
		if (sample.containsField("sample.sample_min_depth_m")) {
		// OK.
		} 
		else if (sample.containsField("sample.section_start_depth_m")) {
		try {
				sample.addField("sample.sample_min_depth_m", sample.getField("sample.section_start_depth_m"));
				sample.addField("sample.sample_max_depth_m", sample.getField("sample.section_end_depth_m"));
		// Check order.
		Double startDepth = ConvUtils.convStringToDouble(sample.getField("sample.section_start_depth_m"));
		Double endDepth = ConvUtils.convStringToDouble(sample.getField("sample.section_end_depth_m"));
		if (startDepth > endDepth) {
		// Change order.
					sample.addField("sample.sample_min_depth_m", sample.getField("sample.section_end_depth_m"));
					sample.addField("sample.sample_max_depth_m", sample.getField("sample.section_start_depth_m"));
		}
		}
		catch (Exception e) {
		importInfo.addConcatWarning("Failed to use section_start_depth_m or section_end_depth_m."); 				
		}
		}
		
		// Epibenthos: Calculate sampler_area_m2.
		String samplerType = sample.getField("sample.sampler_type_code");
		samplerType = samplerType.toUpperCase();
		if (samplerType.equals("MESHB") || samplerType.equals("RUTA") || samplerType.equals("SQR")) {
			// Don't use transect/section areas for these types.
			// System.out.println("DEBUG: sampler_type_code " + samplerType);
		} else {	
			if (!sample.containsField("sample.sampler_area_m2")) {
				if ((sample.containsField("sample.section_distance_start_m")) && 
					(sample.containsField("sample.section_distance_end_m")) && 
					(sample.getParent().containsField("visit.transect_width_m")) ) { 
					try { 
						Double distanceStart = ConvUtils.convStringToDouble(sample.getField("sample.section_distance_start_m"));
						Double distanceEnd = ConvUtils.convStringToDouble(sample.getField("sample.section_distance_end_m"));
						Double transectWidth = ConvUtils.convStringToDouble(sample.getParent().getField("visit.transect_width_m"));
						Double area = (distanceEnd - distanceStart) * transectWidth;
						sample.addField("sample.sampler_area_m2", ConvUtils.convDoubleToString(area));
					}
					catch (Exception e) {
						importInfo.addConcatWarning("Failed to calculate sampler_area_m2."); 				
					}
				}
			}
		}
		// Epibenthos: Prepare for calculation of transect_length_m.
		if (sample.containsField("sample.section_distance_end_m")) { 
			try { 
				Double sectionDistanceEnd = ConvUtils.convStringToDouble(sample.getField("sample.section_distance_end_m"));
				if (sectionDistanceEnd > maxSectionDistanceEnd) {
					this.maxSectionDistanceEnd = sectionDistanceEnd;
					this.maxSectionDistanceEndString = sample.getField("sample.section_distance_end_m");
				}
			}
			catch (Exception e) {
				importInfo.addConcatWarning("Failed to calculate section_distance_end_m."); 				
			}
		}
		/////////////////////////////////////		
		
		
		// Use orderer from delivery if missing here.
		if (!sample.containsField("sample.sample_orderer_code") || sample.getField("sample.sample_orderer_code").equals("")) {
			sample.addField("sample.sample_orderer_code", sample.getParent().getParent().getField("dataset.delivery_orderer_code"));
		}

		/*
			Bortkommenterat - nedan ?
		 */

//		// Translate project.
//		String projectCode = translateCodes.translateSynonym("project", sample.getField("sample.sample_project_code"), importInfo);
//		sample.addField("sample.sample_project_code", projectCode);
//		if (projectCode.equals("")) {
//			sample.addField("sample.sample_project_name_sv", "-");
//			sample.addField("sample.sample_project_name_en", "-");
//		} else {
//			sample.addField("sample.sample_project_name_sv", 
//							 translateCodes.translateToSwedishName("project", projectCode));
//			sample.addField("sample.sample_project_name_en", 
//					 		 translateCodes.translateToEnglishName("project", projectCode));
//		}
//
//		// Translate orderer.
//		String ordererCode = translateCodes.translateSynonym("LABO", sample.getField("sample.sample_orderer_code"), importInfo);
//		sample.addField("sample.sample_orderer_code", ordererCode);
//		if (ordererCode.equals("")) {
//			sample.addField("sample.sample_orderer_name_sv", "-");
//			sample.addField("sample.sample_orderer_name_en", "-");
//		} else {
//			sample.addField("sample.sample_orderer_name_sv", 
//							 translateCodes.translateToSwedishName("LABO", ordererCode));
//			sample.addField("sample.sample_orderer_name_en", 
//					 		 translateCodes.translateToEnglishName("LABO", ordererCode));
//		}
//
//		// Translate sampling laboratory.
//		String samplingLaboratoryCode = translateCodes.translateSynonym("LABO", sample.getField("sample.sampling_laboratory_code"), importInfo);
////		sample.addField("sample.sampling_laboratory_code", samplingLaboratoryCode);
//		if (samplingLaboratoryCode.equals("")) {
//			sample.addField("sample.sampling_laboratory_name_sv", "-");
//			sample.addField("sample.sampling_laboratory_name_en", "-");
//		} else {
//			sample.addField("sample.sampling_laboratory_name_sv", 
//							 translateCodes.translateToSwedishName("LABO", samplingLaboratoryCode));
//			sample.addField("sample.sampling_laboratory_name_en", 
//					 		 translateCodes.translateToEnglishName("LABO", samplingLaboratoryCode));
//		}
		
		// Add extra comments to sample_comment.
		if ((sample.containsField("sample.TEMP.add_to_sample_comment")) || 
			(sample.containsField("sample.TEMP.add_to_sample_comment_1")) || 
			(sample.containsField("sample.TEMP.add_to_sample_comment_2"))) {
			String accComment = sample.getField("sample.sample_comment");
			String addComment = sample.getField("sample.TEMP.add_to_sample_comment");
			String addComment1 = sample.getField("sample.TEMP.add_to_sample_comment_1");
			String addComment2 = sample.getField("sample.TEMP.add_to_sample_comment_2");
			
			if (!addComment.equals("")) {
				if (accComment.equals("")) { accComment = addComment; }
				else                       { accComment += " / " + addComment; }
			}
			if (!addComment1.equals("")) {
				if (accComment.equals("")) { accComment = addComment1; }
				else                       { accComment += " / " + addComment1; }
			}
			if (!addComment2.equals("")) {
				if (accComment.equals("")) { accComment = addComment2; }
				else                       { accComment += " / " + addComment2; }
			}
			sample.addField("sample.sample_comment", accComment);				
			sample.removeField("sample.TEMP.add_to_sample_comment");
			sample.removeField("sample.TEMP.add_to_sample_comment_1");
			sample.removeField("sample.TEMP.add_to_sample_comment_2");
		}

		// Used by the Visitor pattern.
		for (Object variable : sample.getVariables().toArray()) { // toArray: Variables are added during the loop.			
			((Variable) variable).Accept(this);
		}

		// Makes it possible for import format specific actions.
		if (fileImport != null) {
			fileImport.postReformatSample(sample);
		}
		
		// Translate from code synonyms.
		try {
			List<String> fieldKeys = new ArrayList<String>(sample.getFieldKeys());
			for (String internalKey : fieldKeys) {
				String fieldKey = columnInfoManager.getColumnInfoObjectFromInternalKey(internalKey).getKey();
				String fieldValue = sample.getField(internalKey);
				if (!fieldKey.equals("") && !fieldValue.equals("")) {
					String translatedValue = translateCodes_NEW.translateSynonym(fieldKey, fieldValue);
					if (!translatedValue.equals("")) {
						if (!fieldValue.equals(translatedValue)) {
						sample.addField(internalKey, translatedValue);
						importInfo.addConcatWarning("Code translated. Field: " + fieldKey +
								"   Old value: " + fieldValue +
								"   New value: " + translatedValue);
						}
					} else {
						importInfo.addConcatWarning("Code not found. Field: " + fieldKey +
								"   Value: " + fieldValue);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Translate project.
		String projectCode = sample.getField("sample.sample_project_code");
		//String projectCode = translateCodes.translateSynonym("project", sample.getField("sample.sample_project_code"),importInfo);
		if (projectCode.equals("")) {
			sample.addField("sample.sample_project_name_sv", "-");
			sample.addField("sample.sample_project_name_en", "-");
		} else {

			TranslateCodesObject_NEW object = translateCodes_NEW.getCodeObject("sample_project_code", projectCode);

			if (object != null) {
				sample.addField("sample.sample_project_name_sv", object.getSwedish());
				sample.addField("sample.sample_project_name_en", object.getEnglish());
			} else {
				String projectPublicValue = SqliteManager.getInstance().getTranslateCodeColumnValue("sample_project_code", sample, "public_value"); //public_value is the data-column name in translate_codes_NEW
				String projectNameEn =  SqliteManager.getInstance().getTranslateCodeColumnValue("sample_project_code", sample, "english");
				String projectNameSv =  SqliteManager.getInstance().getTranslateCodeColumnValue("sample_project_code", sample, "swedish");

				if (projectPublicValue != null){
					sample.addField("sample.sample_project_code", projectPublicValue);
				}
				if (projectNameEn != null){
					sample.addField("sample.sample_project_name_en",projectNameEn);
				}
				if (projectNameSv != null){
					sample.addField("sample.sample_project_name_sv",projectNameSv);
				}
			}

		}

		// Translate orderer.
		String ordererCode = sample.getField("sample.sample_orderer_code");
		if (ordererCode.equals("")) {
			sample.addField("sample.sample_orderer_name_sv", "-");
			sample.addField("sample.sample_orderer_name_en", "-");
		} else {
			TranslateCodesObject_NEW object = translateCodes_NEW.getCodeObject("sample_orderer_code", ordererCode);
			if (object != null) {
				sample.addField("sample.sample_orderer_name_sv", object.getSwedish());
				sample.addField("sample.sample_orderer_name_en", object.getEnglish());
			}
			if (object == null){

				String sv = SqliteManager.getInstance().getTranslateCodeColumnValue("laboratory", ordererCode, "swedish");
				String en = SqliteManager.getInstance().getTranslateCodeColumnValue("laboratory", ordererCode, "english");
				sample.addField("sample.sample_orderer_name_sv", sv.length() > 0 ? sv : ordererCode );
				sample.addField("sample.sample_orderer_name_en", en.length() > 0 ? en : ordererCode);
			}
		}

		// Translate sampling laboratory.
		String samplingLaboratoryCode = sample.getField("sample.sampling_laboratory_code");
		if (samplingLaboratoryCode.equals("")) {
			sample.addField("sample.sampling_laboratory_name_sv", "-");
			sample.addField("sample.sampling_laboratory_name_en", "-");
		} else {
			TranslateCodesObject_NEW object = translateCodes_NEW.getCodeObject("sampling_laboratory_code", samplingLaboratoryCode);
			if (object != null) {
				sample.addField("sample.sampling_laboratory_name_sv", object.getSwedish());
				sample.addField("sample.sampling_laboratory_name_en", object.getEnglish());
			} else{
				String s = "";
			}
		}

		// Final cleanup on field level.
		try {
			List<String> fieldKeys = new ArrayList<String>(sample.getFieldKeys());
			for (String internalKey : fieldKeys) {
				// Adjust decimal and float values.
				String fieldValue = sample.getField(internalKey);
				String fieldFormat = columnInfoManager.getColumnInfoObjectFromInternalKey(internalKey).getFieldFormat();
				if (fieldFormat.equals("float")) {
					sample.addField(internalKey, fieldValue.replace(",", ".").replace(" ", ""));
				}
				else if (fieldFormat.equals("decimal")) {
					sample.addField(internalKey, fieldValue.replace(",", ".").replace(" ", ""));
				} 
				else if (fieldFormat.equals("pos-dd")) {
					sample.addField(internalKey, fieldValue.replace(",", ".").replace(" ", ""));
				} 
				else if (fieldFormat.equals("pos-dm")) {
					sample.addField(internalKey, fieldValue.replace(",", "."));
				} 
				else if (fieldFormat.equals("date")) {
					sample.addField(internalKey, utils.convDate(fieldValue));
				} 
				else if (fieldFormat.equals("time")) {
					sample.addField(internalKey, utils.convTime(fieldValue));
				} 
				else if (fieldFormat.equals("time-h")) {
					sample.addField(internalKey, utils.convTimeHour(fieldValue));
				} 
				else if (fieldFormat.equals("time-ms")) {
					sample.addField(internalKey, utils.convTimeMinSec(fieldValue));
				} 
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Calculate shark_sample_id.
		String datatype = sample.getField("sample.sample_datatype");
		String locationId = VisitLocationManager.calculateKey(sample.getParent().getPosition().getLatitude(),
															  sample.getParent().getPosition().getLongitude());

		String sharkSampleId = "";
		if (datatype.equals("Physical and Chemical")) {
			sharkSampleId += "+" + sample.getParent().getField("visit.visit_year");
			sharkSampleId += "+" + sample.getParent().getField("visit.platform_code");
			sharkSampleId += "+" + sample.getParent().getField("visit.visit_id");
			sharkSampleId += "+" + sample.getField("sample.sample_min_depth_m");
			sharkSampleId += "+" + sample.getField("sample.sample_max_depth_m");
			sharkSampleId = "SHARK-HYD" + sharkSampleId; // Prefix.
		} else {
			// Default for other datatypes.
			sharkSampleId += "+" + sample.getParent().getField("visit.visit_year");
			sharkSampleId += "+" + sample.getParent().getField("visit.platform_code");
			sharkSampleId += "+" + sample.getParent().getField("visit.visit_id");
			sharkSampleId += "+" + sample.getParent().getField("visit.visit_date");
			sharkSampleId += "+" + sample.getField("sample.sample_time");
//			sharkSampleId += "+" + sample.getParent().getField("visit.station_name");
			sharkSampleId += "+" + sample.getParent().getField("visit.reported_station_name");
			sharkSampleId += "+" + locationId.replace(" ", "");
			sharkSampleId += "+" + sample.getField("sample.TEMP.sample_id_prefix") + sample.getField("sample.sample_id");
			sharkSampleId += "+" + sample.getField("sample.sample_min_depth_m");
			sharkSampleId += "+" + sample.getField("sample.sample_max_depth_m");
			sharkSampleId += "+" + sample.getField("sample.sampler_type_code");
			//
			if (datatype.equals("Bacterioplankton")) {
				sharkSampleId += "+" + sample.getField("sample.sample_series");
				sharkSampleId = "SHARK-Bact" + sharkSampleId; // Prefix.
			//
			} else if (datatype.equals("Chlorophyll")) {
				sharkSampleId = "SHARK-Chl" + sharkSampleId; // Prefix.
			//	
			} else if (datatype.equals("Epibenthos")) {

                sharkSampleId += "+" + sample.getField("variable.sample_part_id");
                
				sharkSampleId += "+" + sample.getField("sample.transect_id");
				sharkSampleId += "+" + sample.getField("sample.transect_min_distance_m");
				sharkSampleId += "+" + sample.getField("sample.transect_max_distance_m");
				sharkSampleId += "+" + sample.getField("sample.section_start_depth_m");
				sharkSampleId += "+" + sample.getField("sample.section_end_depth_m");
				sharkSampleId += "+" + sample.getField("sample.section_distance_start_m");
				sharkSampleId += "+" + sample.getField("sample.section_distance_end_m");
				sharkSampleId = "SHARK-EB" + sharkSampleId; // Prefix.


			// Dropvideo.
			} else if (sample.getParent().getParent().getField("dataset.import_format").startsWith("Epibenthosdropvideo")) {
				
				sharkSampleId += "+" + sample.getField("sample.transect_id");
				
				sharkSampleId += "+" + sample.getField("sample.transect_min_distance_m");
				sharkSampleId += "+" + sample.getField("sample.transect_max_distance_m");
				sharkSampleId += "+" + sample.getField("sample.section_start_depth_m");
				sharkSampleId += "+" + sample.getField("sample.section_end_depth_m");
				sharkSampleId += "+" + sample.getField("sample.transect_video_name");
				sharkSampleId = "SHARK-Dropvideo" + sharkSampleId; // Prefix.


			//
			} else if (datatype.equals("Grey seal")) {
//				sharkSampleId += "+" + sample.getParent().getField("visit.reported_station_name");
				sharkSampleId = "SHARK-Gseal" + sharkSampleId; // Prefix.
			//
			} else if (datatype.equals("Harbour seal")) {
//				sharkSampleId += "+" + sample.getParent().getField("visit.reported_station_name");
				sharkSampleId = "SHARK-Hseal" + sharkSampleId;	 // Prefix.
			//
			} else if (datatype.equals("Phytoplankton")) {
				if (sample.getParent().getParent().getField("dataset.dataset_name").contains("Cyanob")) {
					sharkSampleId = "SHARK-PP(Cyano)" + sharkSampleId; // Prefix.
				} else {
					sharkSampleId = "SHARK-PP" + sharkSampleId; // Prefix.
				}
			//
			} else if (datatype.equals("Picoplankton")) {
				sharkSampleId = "SHARK-PP(Pico)" + sharkSampleId; // Prefix.
				//
			} else if (datatype.equals("Primary production")) {
				sharkSampleId = "SHARK-PrimProd" + sharkSampleId; // Prefix.
			//
			} else if (datatype.equals("Ringed seal")) {
				
				sharkSampleId += "+" + sample.getField("sample.image_id");
				
				sharkSampleId = "SHARK-Rseal" + sharkSampleId; // Prefix.
			//
			} else if (datatype.equals("Seal pathology")) {
				sharkSampleId = "SHARK-Spat" + sharkSampleId; // Prefix.
			//
			} else if (datatype.equals("Sedimentation")) {
				sharkSampleId = "SHARK-Sed" + sharkSampleId; // Prefix.
			//
			} else if (datatype.equals("Zoobenthos")) {
				sharkSampleId += "+" + sample.getField("sample.TEMP.sample_link");
				sharkSampleId = "SHARK-ZB" + sharkSampleId; // Prefix.
			//
			} else if (datatype.equals("Zooplankton")) {
				sharkSampleId = "SHARK-ZP" + sharkSampleId; // Prefix.
			//
//			} else if (datatype.equals("Epibenthos dropvideo")) {
//				sharkSampleId += "+" + sample.getField("sample.image_sequence");
//				sharkSampleId = "SHARK+EPdrop" + sharkSampleId; // Prefix.
			}
		}
		// Add fields for Sample IDs to sample.
//		if (datatype.equals("Epibenthos") &&
//			! sample.getParent().getParent().getField("dataset.import_format").startsWith("Epibenthosdropvideo")	) {
//			// No clear definition of sample for Epibenthos yet. 
//		} else {
			sample.addField("sample.shark_sample_id", sharkSampleId);
			sample.addField("sample.shark_sample_id_md5", StringUtils.convToMd5(sharkSampleId));
//		}
		
	}

	@Override
	public void visitVariable(Variable variable) {

		adjustVariableFields(variable);
		

		// parameter
		if (variable.containsField("variable.parameter")) {
			variable.setParameter(variable.getField("variable.parameter"));
		} else {
//			importInfo.addConcatInfo("Variable: parameter...TODO"); // TODO 
		}
		
		// value
		if (variable.containsField("variable.value")) {
			variable.setValue(variable.getField("variable.value"));
		} else {
//			importInfo.addConcatInfo("Variable: value...TODO"); // TODO 
		}
		
		// unit
		if (variable.containsField("variable.unit")) {
			variable.setUnit(variable.getField("variable.unit"));
		} else {
//			importInfo.addConcatInfo("Variable: unit...TODO"); // TODO 
		}
		
		// Sample_part min depth.
		if (variable.containsField("variable.sample_part_min_cm")) {
			// OK.
		} 
		else if (variable.containsField("variable.TEMP.sample_part_min_max_cm")) {
			variable.addField("variable.sample_part_min_cm", variable.getField("variable.TEMP.sample_part_min_max_cm"));
		} 
		
		// Sample_part max depth.
		if (variable.containsField("variable.sample_part_max_cm")) {
			// OK.
		} 
		else if (variable.containsField("variable.TEMP.sample_part_min_max_cm")) {
			variable.addField("variable.sample_part_max_cm", variable.getField("variable.TEMP.sample_part_min_max_cm"));
		} 
		
		// Add extra comments to variable_comment.
		if ((variable.containsField("variable.TEMP.add_to_variable_comment")) || 
			(variable.containsField("variable.TEMP.add_to_variable_comment_1")) || 
			(variable.containsField("variable.TEMP.add_to_variable_comment_2"))) {
			String accComment = variable.getField("variable.variable_comment");
			String addComment = variable.getField("variable.TEMP.add_to_variable_comment");
			String addComment1 = variable.getField("variable.TEMP.add_to_variable_comment_1");
			String addComment2 = variable.getField("variable.TEMP.add_to_variable_comment_2");
			
			if (!addComment.equals("")) {
				if (accComment.equals("")) { accComment = addComment; }
				else                       { accComment += " / " + addComment; }
			}
			if (!addComment1.equals("")) {
				if (accComment.equals("")) { accComment = addComment1; }
				else                       { accComment += " / " + addComment1; }
			}
			if (!addComment2.equals("")) {
				if (accComment.equals("")) { accComment = addComment2; }
				else                       { accComment += " / " + addComment2; }
			}
			variable.addField("variable.variable_comment", accComment);				
			variable.removeField("variable.TEMP.add_to_variable_comment");
			variable.removeField("variable.TEMP.add_to_variable_comment_1");
			variable.removeField("variable.TEMP.add_to_variable_comment_2");
		}
		
		
		// Species.
		if ((variable instanceof Variable) && (((Variable) variable).isCommunity())) {
			try {
				// Always use variable.reported_scientific_name as basis for DynTaxa lookup.
				if (!variable.containsField("variable.reported_scientific_name")) {
					if (variable.containsField("variable.scientific_name")) {
						variable.addField("variable.reported_scientific_name", variable.getField("variable.scientific_name"));
					}
					else if (variable.containsField("variable.dyntaxa_id")) {
						variable.addField("variable.reported_scientific_name", variable.getField("variable.dyntaxa_id"));
					}
				}
				// Trim field.
				String reportedScientificName = variable.getField("variable.reported_scientific_name").trim();
				variable.addField("variable.reported_scientific_name", reportedScientificName);
				// Copy default.
				variable.addField("variable.scientific_name", variable.getField("variable.reported_scientific_name"));
				variable.addField("variable.dyntaxa_id", "");
				
				// 
				try {
					if (!reportedScientificName.equals("")) {
						String dyntaxaId = TaxonManager.instance().getTaxonIdFromName(reportedScientificName);
						if (dyntaxaId.equals("")) {
							dyntaxaId = variable.getField("variable.reported_scientific_name");
						}
						TaxonNode taxonNode = TaxonManager.instance().getTaxonNodeFromImportId(dyntaxaId);
						if (taxonNode != null) {
							String taxonName = taxonNode.getTaxonObject().getValidNameObject().getName();
							// Second try if first one not valid.
							dyntaxaId = TaxonManager.instance().getTaxonIdFromName(taxonName);
							taxonNode = TaxonManager.instance().getTaxonNodeFromImportId(dyntaxaId);
							//
							if (!dyntaxaId.equals("")) {
								variable.addField("variable.dyntaxa_id", dyntaxaId);
							}
							String usedTaxonName = taxonNode.getTaxonObject().getValidNameObject().getName();
							if (!usedTaxonName.equals("")) {
								variable.addField("variable.scientific_name", usedTaxonName);
							}
						}
					} 
				}
				catch (Exception e) {
					importInfo.addConcatWarning("Community: Error when match taxa: " + reportedScientificName);				
				}
			} 
			catch (Exception e) {
				importInfo.addConcatWarning("Community: Can't handle dyntaxa_id and/or scientific_name;"); 				
			}
		}
		
		// AphiaId from WoRMS, http://marinespecies.org.
		if (variable.containsField("variable.scientific_name")) {
			String scientificName = variable.getField("variable.scientific_name").trim();
			String oldAphiaId = variable.getField("variable.aphia_id").trim();
			String newAphiaId = TaxaWorms.instance().getAphiaId(scientificName);
			
			if (variable.containsField("variable.bvol_scientific_name")) {
				newAphiaId = variable.getField("variable.bvol_aphia_id").trim();
				variable.addField("variable.aphia_id", newAphiaId);
			}
			
			if (!newAphiaId.equals("")) {
				if (oldAphiaId.equals("")) {
					variable.addField("variable.aphia_id", newAphiaId);
				} else {
					// Don't override old AphiaID, but check if they differ.
					if (!oldAphiaId.equals(newAphiaId)) {
						importInfo.addConcatWarning("Used AphiaID differ from WoRMS: " + scientificName + ".  Used: " + oldAphiaId + "  WoRMS: " + newAphiaId);
					}
				}
			}
			
			// Check if AphiaID exists.
			String aphiaId = variable.getField("variable.aphia_id").trim();
			if (aphiaId.equals("")) {
				importInfo.addConcatWarning("AphiaID is missing for: " + scientificName + ".");
			}
			
		}
		
		// Trophic type.
		if (variable.containsField("variable.scientific_name")) {
			String scientificName = variable.getField("variable.scientific_name").trim();
			String sizeClass = variable.getField("variable.size_class");
			String trophicType = trophicTypeManager.getTrophicType(scientificName, sizeClass);
			String reportedTrophicType = variable.getField("variable.trophic_type_code");			
			
			if (!reportedTrophicType.equals("") && trophicType.equals("NS")) {
				// Reported. 'NS' in list. Use reported.
				variable.addField("variable.trophic_type_code", reportedTrophicType);
			}
			else if (reportedTrophicType.equals("") && trophicType.equals("NS")) {
				// Not reported. 'NS' in list. Use 'NS'.
				variable.addField("variable.trophic_type_code", trophicType);
			}
			else if (reportedTrophicType.equals("") && !trophicType.equals("")) {
				// Not reported. Found in list. Use list.
				variable.addField("variable.trophic_type_code", trophicType);
			}
			else if (!reportedTrophicType.equals("") && trophicType.equals("")) {
				// Reported. Not found in list. Use reported.
				variable.addField("variable.trophic_type_code", reportedTrophicType);				
			}
			else if (!reportedTrophicType.equals("") && !trophicType.equals("") && 
					 !reportedTrophicType.equals(trophicType)) {
				// Reported and found in list. Use list.
				variable.addField("variable.trophic_type_code", trophicType);
			}
			
			// Check and log if modified.
			String newTrophicType = variable.getField("variable.trophic_type_code");			
			if (!reportedTrophicType.equals(newTrophicType)) {
			importInfo.addConcatWarning("Reported trophic type replaced. Scientific name/size: " + 
					scientificName + "/" + sizeClass +  
					"   Old: " + reportedTrophicType +  
					"   New: " + trophicType);
			}
		}

		
//		// Translate code values.
//////		String analysisLaboratoryCode = translateCodes.translateSynonym("laboratory", variable.getField("variable.analytical_laboratory_code"));
////		String analysisLaboratoryCode = translateCodes.translateSynonym("LABO", variable.getField("variable.analytical_laboratory_code"), importInfo);
////		variable.addField("variable.analytical_laboratory_code", analysisLaboratoryCode);
//////		String analysisLaboratoryCodeHyd = translateCodes.translateSynonym("laboratory", variable.getField("variable.sampling_laboratory_code_phyche"));
////		String analysisLaboratoryCodeHyd = translateCodes.translateSynonym("LABO", variable.getField("variable.sampling_laboratory_code_phyche"), importInfo);
////		variable.addField("variable.sampling_laboratory_code_phyche", analysisLaboratoryCodeHyd);
//
//		// Translate code values.
//		String analysisLaboratoryCode = translateCodes.translateSynonym("LABO", variable.getField("variable.analytical_laboratory_code"), importInfo);
////		variable.addField("variable.analytical_laboratory_code", analysisLaboratoryCode);
//		if (analysisLaboratoryCode.equals("")) {
//			variable.addField("variable.analytical_laboratory_name_sv", "-");
//			variable.addField("variable.analytical_laboratory_name_en", "-");
//		} else {
//			variable.addField("variable.analytical_laboratory_name_sv", 
//							 translateCodes.translateToSwedishName("LABO", analysisLaboratoryCode));
//			variable.addField("variable.analytical_laboratory_name_en", 
//					 		 translateCodes.translateToEnglishName("LABO", analysisLaboratoryCode));
//		}
//		String analysisLaboratoryCodeHyd = translateCodes.translateSynonym("LABO", variable.getField("variable.sampling_laboratory_code_phyche"), importInfo);
//		variable.addField("variable.sampling_laboratory_code_phyche", analysisLaboratoryCodeHyd);
//		if (analysisLaboratoryCodeHyd.equals("")) {
//			variable.addField("variable.sampling_laboratory_phyche_name_sv", "-");
//			variable.addField("variable.sampling_laboratory_phyche_name_en", "-");
//		} else {
//			variable.addField("variable.sampling_laboratory_phyche_name_sv", 
//							 translateCodes.translateToSwedishName("LABO", analysisLaboratoryCodeHyd));
//			variable.addField("variable.sampling_laboratory_phyche_name_en", 
//					 		 translateCodes.translateToEnglishName("LABO", analysisLaboratoryCodeHyd));
//		}

		// Makes it possible for import format specific actions.
		if (fileImport != null) {
			fileImport.postReformatVariable(variable);	
		}
		
		// Translate from code synonyms.
		try {
			List<String> fieldKeys = new ArrayList<String>(variable.getFieldKeys());
			for (String internalKey : fieldKeys) {
				String fieldKey = columnInfoManager.getColumnInfoObjectFromInternalKey(internalKey).getKey();
				String fieldValue = variable.getField(internalKey);
				if (!fieldKey.equals("") && !fieldValue.equals("")) {
					String translatedValue = translateCodes_NEW.translateSynonym(fieldKey, fieldValue);
					if (!translatedValue.equals("")) {
						if (!fieldValue.equals(translatedValue)) {
						variable.addField(internalKey, translatedValue);
						importInfo.addConcatWarning("Code translated. Field: " + fieldKey +
								"   Old value: " + fieldValue +
								"   New value: " + translatedValue);
						}
					} else {
						importInfo.addConcatWarning("Code not found. Field: " + fieldKey +
								"   Value: " + fieldValue);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Translate code values.
		String analysisLaboratoryCode = variable.getField("variable.analytical_laboratory_code");
		if (analysisLaboratoryCode.equals("")) {
			variable.addField("variable.analytical_laboratory_name_sv", "-");
			variable.addField("variable.analytical_laboratory_name_en", "-");
		} else {
			TranslateCodesObject_NEW object = translateCodes_NEW.getCodeObject("analytical_laboratory_code", analysisLaboratoryCode);
			if (object != null) {
				variable.addField("variable.analytical_laboratory_name_sv", object.getSwedish());
				variable.addField("variable.analytical_laboratory_name_en", object.getEnglish());
			}
		}
		String analysisLaboratoryCodeHyd = variable.getField("variable.sampling_laboratory_code_phyche");
		if (analysisLaboratoryCodeHyd.equals("")) {
			variable.addField("variable.sampling_laboratory_phyche_name_sv", "-");
			variable.addField("variable.sampling_laboratory_phyche_name_en", "-");
		} else {
			TranslateCodesObject_NEW object = translateCodes_NEW.getCodeObject("sampling_laboratory_code_phyche", analysisLaboratoryCodeHyd);
			if (object != null) {
				variable.addField("variable.sampling_laboratory_phyche_name_sv", object.getSwedish());
				variable.addField("variable.sampling_laboratory_phyche_name_en", object.getEnglish());
			}
		}

		// Final cleanup on field level.
		try {
			List<String> fieldKeys = new ArrayList<String>(variable.getFieldKeys());
			for (String internalKey : fieldKeys) {
				// Adjust decimal and float values.
				String fieldValue = variable.getField(internalKey);
				String fieldFormat = columnInfoManager.getColumnInfoObjectFromInternalKey(internalKey).getFieldFormat();
				if (fieldFormat.equals("float")) {
					variable.addField(internalKey, fieldValue.replace(",", ".").replace(" ", ""));
				}
				else if (fieldFormat.equals("decimal")) {
					variable.addField(internalKey, fieldValue.replace(",", ".").replace(" ", ""));
				} 
				else if (fieldFormat.equals("pos-dd")) {
					variable.addField(internalKey, fieldValue.replace(",", ".").replace(" ", ""));
				} 
				else if (fieldFormat.equals("pos-dm")) {
					variable.addField(internalKey, fieldValue.replace(",", "."));
				} 
				else if (fieldFormat.equals("date")) {
					variable.addField(internalKey, utils.convDate(fieldValue));
				} 
				else if (fieldFormat.equals("time")) {
					variable.addField(internalKey, utils.convTime(fieldValue));
				} 
				else if (fieldFormat.equals("time-h")) {
					variable.addField(internalKey, utils.convTimeHour(fieldValue));
				} 
				else if (fieldFormat.equals("time-ms")) {
					variable.addField(internalKey, utils.convTimeMinSec(fieldValue));
				} 
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void adjustVisitFields(Visit visit) {
		// Example: From "sample.indwetwt.DIVIDE.1000"
		// to "sample.indwetwt" and the value is divided by 1000.
	
		if (visit.getFieldKeys() != null) {
			for (String key : visit.getFieldKeys().toArray(new String[0])) {
				if (visit.containsField(key)) { // Check if removed.
//					String[] keyParts = key.split("[.]");
					String[] keyParts = key.split(Pattern.quote("."));
				
					if ((keyParts.length >= 4) &&
						(keyParts[2].equals("MULTIPLY"))) {
						if ((!visit.getField(key).equals("")) && 
							(!visit.getField(key).equals("NaN"))) {
								
							try {
								Double value = ConvUtils.convStringToDouble(visit.getField(key)) * 
								ConvUtils.convStringToDouble(keyParts[3]);
								visit.addField(keyParts[0] + "." + keyParts[1], ConvUtils.convDoubleToString(value));
							} catch (Exception e) {
								visit.addField(keyParts[0] + "." + keyParts[1], ""); // Empty string if failed. 
								importInfo.addConcatWarning("Failed to multiply value. Key: " + 
										key + " Value: " + visit.getField(key));
							}
						}		
						visit.removeField(key); // Also removes NaN.
					}
	
					if ((keyParts.length >= 4) &&
						(keyParts[2].equals("DIVIDE"))) {
						if ((!visit.getField(key).equals("")) && 
							(!visit.getField(key).equals("NaN"))) {
								
							try {
								Double value = ConvUtils.convStringToDouble(visit.getField(key)) / 
								ConvUtils.convStringToDouble(keyParts[3]);
								visit.addField(keyParts[0] + "." + keyParts[1], ConvUtils.convDoubleToString(value));
							} catch (Exception e) {
								visit.addField(keyParts[0] + "." + keyParts[1], ""); // Empty string if failed. 
								importInfo.addConcatWarning("Failed to divide value. Key: " + 
										key + " Value: " + visit.getField(key));
							}
						}		
						visit.removeField(key); // Also removes NaN.
					}
				}				
			}
		}
	}

	public void adjustSampleFields(Sample sample) {
		// Example: From "sample.indwetwt.DIVIDE.1000"
		// to "sample.indwetwt" and the value is divided by 1000.
	
		if (sample.getFieldKeys() != null) {
			for (String key : sample.getFieldKeys().toArray(new String[0])) {
				if (sample.containsField(key)) { // Check if removed.
//					String[] keyParts = key.split("[.]");
					String[] keyParts = key.split(Pattern.quote("."));
				
					if ((keyParts.length >= 4) &&
						(keyParts[2].equals("MULTIPLY"))) {
						if ((!sample.getField(key).equals("")) && 
							(!sample.getField(key).equals("NaN"))) {
								
							try {
								Double value = ConvUtils.convStringToDouble(sample.getField(key)) * 
								ConvUtils.convStringToDouble(keyParts[3]);
								sample.addField(keyParts[0] + "." + keyParts[1], value.toString());
							} catch (Exception e) {
								sample.addField(keyParts[0] + "." + keyParts[1], ""); // Empty string if failed. 
								importInfo.addConcatWarning("Failed to multiply value. Key: " + 
										key + " Value: " + sample.getField(key));
							}
						}		
						sample.removeField(key); // Also removes NaN.
					}
	
					if ((keyParts.length >= 4) &&
						(keyParts[2].equals("DIVIDE"))) {
						if ((!sample.getField(key).equals("")) && 
							(!sample.getField(key).equals("NaN"))) {
								
							try {
								Double value = ConvUtils.convStringToDouble(sample.getField(key)) / 
								ConvUtils.convStringToDouble(keyParts[3]);
								sample.addField(keyParts[0] + "." + keyParts[1], value.toString());
							} catch (Exception e) {
								sample.addField(keyParts[0] + "." + keyParts[1], ""); // Empty string if failed. 
								importInfo.addConcatWarning("Failed to divide value. Key: " + 
										key + " Value: " + sample.getField(key));
							}
						}		
						sample.removeField(key); // Also removes NaN.
					}
				}				
			}
		}
	}

	public void adjustVariableFields(Variable variable) {
		// Example: From "sample.indwetwt.DIVIDE.1000"
		// to "sample.indwetwt" and the value is divided by 1000.
	
		if (variable.getFieldKeys() != null) {
			for (String key : variable.getFieldKeys().toArray(new String[0])) {
				if (variable.containsField(key)) { // Check if removed.
//					String[] keyParts = key.split("[.]");
					String[] keyParts = key.split(Pattern.quote("."));
				
					if ((keyParts.length >= 4) &&
						(keyParts[2].equals("MULTIPLY"))) {
						if ((!variable.getField(key).equals("")) && 
							(!variable.getField(key).equals("NaN"))) {
								
							try {
								Double value = ConvUtils.convStringToDouble(variable.getField(key)) * 
								ConvUtils.convStringToDouble(keyParts[3]);
								variable.addField(keyParts[0] + "." + keyParts[1], value.toString());
							} catch (Exception e) {
								variable.addField(keyParts[0] + "." + keyParts[1], ""); // Empty string if failed. 
								importInfo.addConcatWarning("Failed to multiply value. Key: " + 
										key + " Value: " + variable.getField(key));
							}
						}		
						variable.removeField(key); // Also removes NaN.
					}
	
					if ((keyParts.length >= 4) &&
						(keyParts[2].equals("DIVIDE"))) {
						if ((!variable.getField(key).equals("")) && 
							(!variable.getField(key).equals("NaN"))) {
								
							try {
								Double value = ConvUtils.convStringToDouble(variable.getField(key)) / 
								ConvUtils.convStringToDouble(keyParts[3]);
								variable.addField(keyParts[0] + "." + keyParts[1], value.toString());
							} catch (Exception e) {
								variable.addField(keyParts[0] + "." + keyParts[1], ""); // Empty string if failed. 
								importInfo.addConcatWarning("Failed to divide value. Key: " + 
										key + " Value: " + variable.getField(key));
							}
						}		
						variable.removeField(key); // Also removes NaN.
					}
				}				
			}
		}
	}

	private void reformatPositionStringsDD(Sample sample, String latitudeFieldName, String longitudeFieldName) {		
		if ((sample.containsField(latitudeFieldName)) && (sample.containsField(latitudeFieldName))) {
			// Convert to decimal point.
			sample.addField(latitudeFieldName, sample.getField(latitudeFieldName).replace(",", "."));
			sample.addField(longitudeFieldName, sample.getField(longitudeFieldName).replace(",", "."));
			// Check if valid format.
			try {
				Double latitude = ConvUtils.convStringToDouble(sample.getField(latitudeFieldName));
				Double longitude = ConvUtils.convStringToDouble(sample.getField(longitudeFieldName));				
			} catch (Exception e) {
				importInfo.addConcatWarning("Invalid format for lat/long. Fields: " + latitudeFieldName + " / " + longitudeFieldName + " = " +
						sample.getField(latitudeFieldName) + " / " + sample.getField(longitudeFieldName));
			}
		}
	}

	private void reformatPositionStringsDM(Sample sample, String latitudeFieldName, String longitudeFieldName) {
		if ((sample.containsField(latitudeFieldName)) && (sample.containsField(latitudeFieldName))) {
			// Convert to decimal point.
			sample.addField(latitudeFieldName, sample.getField(latitudeFieldName).replace(",", "."));
			sample.addField(longitudeFieldName, sample.getField(longitudeFieldName).replace(",", "."));
			// Check if valid format.
			try {
				Double latitude = new Double(GeoPosition.convertFromBiomad(sample.getField(latitudeFieldName)));
				Double longitude = new Double(GeoPosition.convertFromBiomad(sample.getField(longitudeFieldName)));
			} catch (Exception e) {
				importInfo.addConcatWarning("Lat/long error. Lat: " + latitudeFieldName + " / " + longitudeFieldName + ": " +
						sample.getField(latitudeFieldName) + " / " + sample.getField(longitudeFieldName));
			}
		}
	}
}
