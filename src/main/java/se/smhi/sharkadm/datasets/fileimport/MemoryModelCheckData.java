/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.fileimport;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import se.smhi.sharkadm.datasets.columns.ColumnInfoManager;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.ModelVisitor;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.species_old.TaxonManager;
import se.smhi.sharkadm.utils.GeoPosition;
import se.smhi.sharkadm.utils.GeodesiSwedishGrids;

/**
 *	Checks mandatory memory model data. Mostly attributes in the model objects
 *	are checked since they are needed for web searches etc.
 *	The code is based on the Visitor pattern.
 */
public class MemoryModelCheckData extends ModelVisitor {

	protected PrintStream logInfo;
	protected FileImportInfo importInfo;

	private GeodesiSwedishGrids rt90 = new GeodesiSwedishGrids("rt90_2.5_gon_v");

	private ColumnInfoManager columnInfoManager = new ColumnInfoManager();
	
	private List<String> mandatoryFieldList = new ArrayList<String>();
	
	private List<String> duplicateCheckList = new ArrayList<String>();


	public MemoryModelCheckData(PrintStream logInfo, FileImportInfo importInfo) {
		this.logInfo = logInfo;
		this.importInfo = importInfo;
	}
		
	@Override
	public void visitDataset(Dataset dataset) {
		
		// List of mandatory fields.
		mandatoryFieldList.add("dataset.dataset_file_name");
		mandatoryFieldList.add("dataset.dataset_file_path");
//		mandatoryFieldList.add("dataset.country_code");
		mandatoryFieldList.add("dataset.monitoring_years");
		mandatoryFieldList.add("dataset.delivery_datatype");
		mandatoryFieldList.add("dataset.reporting_institute_code");
//		mandatoryFieldList.add("dataset.delivery_orderer_code");
		mandatoryFieldList.add("dataset.reported_by");
		mandatoryFieldList.add("dataset.import_format");
		
		mandatoryFieldList.add("visit.visit_year");
//		mandatoryFieldList.add("visit.visit_month");
		mandatoryFieldList.add("visit.visit_date");
		mandatoryFieldList.add("visit.station_name");
		mandatoryFieldList.add("visit.visit_latitude_dd");
		mandatoryFieldList.add("visit.visit_longitude_dd");
		mandatoryFieldList.add("visit.visit_latitude_dm");
		mandatoryFieldList.add("visit.visit_longitude_dm");

		mandatoryFieldList.add("sample.sample_datatype");
		mandatoryFieldList.add("sample.sample_date");
//		mandatoryFieldList.add("sample.sample_time");
//		mandatoryFieldList.add("sample.sample_month");
		mandatoryFieldList.add("sample.sample_latitude_dd");
		mandatoryFieldList.add("sample.sample_longitude_dd");
		mandatoryFieldList.add("sample.sample_latitude_dm");
		mandatoryFieldList.add("sample.sample_longitude_dm");
		mandatoryFieldList.add("sample.sample_min_depth_m");
		mandatoryFieldList.add("sample.sample_max_depth_m");
//		mandatoryFieldList.add("sample.monitoring_program_code");
		mandatoryFieldList.add("sample.sample_project_code");
		mandatoryFieldList.add("sample.sampling_laboratory_code");
//		mandatoryFieldList.add("sample.sampled_by");

		mandatoryFieldList.add("variable.parameter");
		mandatoryFieldList.add("variable.value");
//		mandatoryFieldList.add("variable.unit");
//		mandatoryFieldList.add("variable.quality_flag");
//		mandatoryFieldList.add("variable.analytical_laboratory_code");
//		mandatoryFieldList.add("variable.analysed_by");

		if (dataset.getVisits().isEmpty()) {
			importInfo.addConcatWarning("Dataset contains no visits. Dataset: " + dataset.getField("dataset.dataset_file_name"));
		}
		
//		if (!dataset.getField("dataset.country_code").equals("77")) {
//			importInfo.addConcatWarning("dataset.country_code is not 77 ( = Sweden).");
//		}
		
		// Check based on field format.
		for (String internalKey : dataset.getFieldKeys()) {
			// Adjust decimal and float values.
			String fieldValue = dataset.getField(internalKey);
			String fieldFormat = columnInfoManager.getColumnInfoObjectFromInternalKey(internalKey).getFieldFormat();
			// Check.
			this.checkValueBasedOnFormat(internalKey, fieldValue, fieldFormat);
		}

		// Check mandatory fields.
		for (String internalKey : mandatoryFieldList) {
			if (internalKey.startsWith("dataset.")) {
				if (dataset.getField(internalKey).equals("")) {
					importInfo.addConcatError("Mandatory value is empty. Internal key: " + internalKey);
				}
			}
		}

		// Used by the Visitor pattern.
		for (Visit visit : dataset.getVisits()) {			
			visit.Accept(this);
		}

	}

	@Override
	public void visitVisit(Visit visit) {
		importInfo.incVisitCounter(); // Count for summary report.
		
		if (visit.getSamples().isEmpty()) {
			if (visit.getStationObject() != null) {
				importInfo.addConcatWarning("Visit contains no samples. Date/Station: " + visit.getField("visit.visit_date") + '/' + visit.getStationObject().getStation_name());
			} else {
				importInfo.addConcatWarning("Visit contains no samples. Date: " + visit.getField("visit.visit_date"));
			}
		}
		
		if (visit.getField("visit.visit_year").length() != 4) {
			importInfo.addConcatWarning("visit.visit_year is not four digits long. Year: " + visit.getField("visit.visit_year")); 
		}
		
		if (visit.getField("visit.visit_month").length() > 2) {
			importInfo.addConcatWarning("visit.visit_month is too long. Month: " + visit.getField("visit.visit_month")); 
		}
		
		if (visit.getField("visit.visit_date").length() != 10) {
			importInfo.addConcatWarning("visit.visit_date has wrong format (yyyy-mm-dd expected). Month: " + visit.getField("visit.visit_month")); 
		}

		// Position visit_position
		if (visit.getPosition() == null) {
			importInfo.addConcatWarning("Visit position is missing."); 
		} else {
			if ((visit.getPosition().getLatitude() < 40.00) || 
				(visit.getPosition().getLatitude() > 80.0) || 
				(visit.getPosition().getLongitude() < -25.0) || 
				(visit.getPosition().getLongitude() > 40.0) ) {
				importInfo.addConcatError("Visit position out of bounds." + 
						visit.getPosition().getLatitudeAsString() + ' ' + visit.getPosition().getLongitudeAsString());
				// Set to 'zero-position'.
				visit.setPosition(new GeoPosition(0.0, 0.0));
			}			
		}

		// Check based on field format.
		for (String internalKey : visit.getFieldKeys()) {
			// Adjust decimal and float values.
			String fieldValue = visit.getField(internalKey);
			String fieldFormat = columnInfoManager.getColumnInfoObjectFromInternalKey(internalKey).getFieldFormat();
			// Check.
			this.checkValueBasedOnFormat(internalKey, fieldValue, fieldFormat);
		}

		// Check mandatory fields.
		for (String internalKey : mandatoryFieldList) {
			if (internalKey.startsWith("visit.")) {
				if (visit.getField(internalKey).equals("")) {
					importInfo.addConcatError("Mandatory value is empty. Internal key: " + internalKey);
				}
			}
		}
		
		// Used by the Visitor pattern.
		for (Sample sample : visit.getSamples()) {			
			sample.Accept(this);
		}

	}

	@Override
	public void visitSample(Sample sample) {
		importInfo.incSampleCounter(); // Count for summary report.
		
		if (sample.getVariables().isEmpty()) {
			importInfo.addConcatWarning("Sample contains no variables.");
		}
		
		// Position sample_position
		if (sample.getPosition() == null) {
			importInfo.addConcatWarning("sample.sample_position is missing."); 
		} else {
			if ((sample.getPosition().getLatitude() < 40.00) || 
				(sample.getPosition().getLatitude() > 80.0) || 
				(sample.getPosition().getLongitude() < -25.0) || 
				(sample.getPosition().getLongitude() > 40.0) ) {
				importInfo.addConcatWarning("Sample position out of bounds." + 
						sample.getPosition().getLatitudeAsString() + ' ' + sample.getPosition().getLongitudeAsString());
				// Set to 'zero-position'.
				sample.setPosition(new GeoPosition(0.0, 0.0));
			}			
		}
		
		// Check based on field format.
		for (String internalKey : sample.getFieldKeys()) {
			// Adjust decimal and float values.
			String fieldValue = sample.getField(internalKey);
			String fieldFormat = columnInfoManager.getColumnInfoObjectFromInternalKey(internalKey).getFieldFormat();
			// Check.
			this.checkValueBasedOnFormat(internalKey, fieldValue, fieldFormat);
		}

		// Check mandatory fields.
		for (String internalKey : mandatoryFieldList) {
			if (internalKey.startsWith("sample.")) {
				if (sample.getField(internalKey).equals("")) {
					importInfo.addConcatError("Mandatory value is empty. Internal key: " + internalKey);
				}
			}
		}

		// Used by the Visitor pattern.
		for (Variable variable : sample.getVariables()) {			
			variable.Accept(this);
		}
		
		
		
		
		
		// New test 2022-02-01: Test for duplicates inside sample.
		duplicateCheckList.clear();
		String sampleIdMd5 = sample.getField("sample.shark_sample_id_md5");
		Integer duplicateCounter = 0;
		for (Variable variable : sample.getVariables()) {
			
			String parameter = variable.getField("variable.parameter");
			String unit = variable.getField("variable.unit");
			String value = variable.getField("variable.value");
			if ((parameter.equals("")) && (unit.equals("")) && (value.equals(""))) {
				continue;
			}

			String scientificName = variable.getField("variable.scientific_name");
			String sizeClass = variable.getField("variable.size_class");
			
			String cellVolume = variable.getField("variable.reported_cell_volume_um3");
			
			String speciesFlag = variable.getField("variable.species_flag_code");
			if (speciesFlag.equals("")) {
				speciesFlag = variable.getField("variable.species_flag");
			}
			String trophicType = variable.getField("variable.trophic_type_code");
			String sexCode = variable.getField("variable.sex_code");
			String devStage = variable.getField("variable.dev_stage_code");
			String sizeMin = variable.getField("variable.size_min_um");
			String sizeMax = variable.getField("variable.size_max_um");
			String stratum = variable.getField("variable.stratum_code");
			if (stratum.equals("")) {
				stratum = variable.getField("variable.stratum_id");
			}
			String samplePartId = variable.getField("variable.sample_part_id");
			String upperMeshSizeUm = variable.getField("variable.upper_mesh_size_um");
			String lowerMeshSizeUm = variable.getField("variable.lower_mesh_size_um");
			
			String epibiont = variable.getField("variable.epibiont");
			String detached = variable.getField("variable.detached");
			String samplePartMin = variable.getField("variable.sample_part_min_cm");
			String samplePartMax = variable.getField("variable.sample_part_max_cm");
			
			String checkString = parameter + "+" + 
								 unit + "+" + 
								 scientificName + "+" + 
								 sizeClass + "+" + 

								 cellVolume + "+" + 
								 
								 speciesFlag + "+" + 
								 trophicType + "+" + 
								 sexCode + "+" + 
								 devStage + "+" + 
								 sizeMin + "+" + 
								 sizeMax + "+" + 
								 stratum + "+" + 
								 epibiont + "+" + 
								 detached + "+" + 
								 samplePartMin + "+" + 
								 samplePartMax + "+" + 
								 
								 samplePartId + "+" + 
								 upperMeshSizeUm + "+" + 
								 lowerMeshSizeUm;
					
			if (duplicateCheckList.contains(checkString)) {
				duplicateCounter += 1;
				String counterStr = duplicateCounter.toString();
				System.out.println("DUPLICATES: MD5: " + sampleIdMd5 + " (" + counterStr + "): Key: " + checkString);
				importInfo.addConcatError("Duplicates: MD5: " + sampleIdMd5 + " (" + counterStr + "): Key: " + checkString);
			} else {
				duplicateCheckList.add(checkString);
			}
		}

	}

	@Override
	public void visitVariable(Variable variable) {
		importInfo.incVariableCounter(); // Count for summary report.

		if (variable.isCommunity()) {
			// dyntaxa_id
			if ((variable.getDyntaxaId().equals("")) || (variable.getDyntaxaId().equals("0"))) {
				importInfo.addConcatWarning("Dyntaxa ID is missing."); 
			}
			
			// reported_scientific_name
			if (variable.getField("variable.reported_scientific_name").equals("")) {
				importInfo.addConcatWarning("Reported_scientific_name is missing."); 
			}
			
			// Check species.
			// If both taxon-id and taxon-name are available:
			String dyntaxaId = variable.getField("variable.dyntaxa_id");
			String reportedTaxonName = variable.getField("variable.reported_scientific_name");
			String usedTaxonName = variable.getField("variable.scientific_name");
			String dvTaxonId = null;

			if ((!dyntaxaId.equals("")) && (!usedTaxonName.equals(""))) {
//			if ((!taxonId.equals("")) && (!reportedTaxonName.equals(""))) {
				dvTaxonId = TaxonManager.instance().getTaxonIdFromName(usedTaxonName);
				if (dvTaxonId.equals(dyntaxaId)) {
					// OK.
				} else if (dvTaxonId.equals("")) {
					importInfo.addConcatWarning("Species not in DV list: " + dyntaxaId + " " + usedTaxonName);
				} else {				
					importInfo.addConcatWarning("Reported dyntaxa-id not equal to current dyntaxa-id: " + dyntaxaId + " " + usedTaxonName + 
											" DV-list-id: " + dvTaxonId);
				}
			}
			// If only taxon-name is available: 
			else if (((variable.getDyntaxaId().equals("0"))) && 
					 (!variable.getField("variable.reported_scientific_name").equals(""))) {
				importInfo.addConcatWarning("Species not in DV list. Used name: " + 
											variable.getField("variable.scientific_name") + 
											"   Reported name: " + 
											variable.getField("variable.reported_scientific_name"));
			}
		}
		
		// Check based on field format.
		for (String internalKey : variable.getFieldKeys()) {
			// Adjust decimal and float values.
			String fieldValue = variable.getField(internalKey);
			String fieldFormat = columnInfoManager.getColumnInfoObjectFromInternalKey(internalKey).getFieldFormat();
			// Check.
			if (!internalKey.equals("value")) {
				this.checkValueBasedOnFormat(internalKey, fieldValue, fieldFormat);
			}
		}

		// Check mandatory fields.
		for (String internalKey : mandatoryFieldList) {
			if (internalKey.startsWith("variable.")) {
				if (variable.getField(internalKey).equals("")) {
					importInfo.addConcatError("Mandatory value is empty. Internal key: " + internalKey);
				}
			}
		}
	}

	private void checkValueBasedOnFormat(String internalKey, String fieldValue, String fieldFormat) {
		if (fieldFormat.equals("text") || fieldValue.equals("")) {
			return;
		}
		
		if (fieldFormat.equals("")) {
			// Mostly from internal keys like 'MISSING KEY: ...' and 'variable.COPY_VARIABLE...'.
//			System.out.println("DEBUG: Format check. Empty field format: " + internalKey);
			return;
		}
		
		if (fieldFormat.equals("date")) {
//			System.out.println("Test: " + fieldValue.substring(4,5));
			if ((fieldValue.length() < 10) || (!fieldValue.substring(4,5).equals("-")) || (!fieldValue.substring(7,8).equals("-"))) {
				importInfo.addConcatError("Format error.   Internal key: " + internalKey + "   Format: " + fieldFormat + "   Value: " + fieldValue);
			}
		}
		else if (fieldFormat.equals("time")) {
			if ((fieldValue.length() < 5) || (!fieldValue.contains(":")) ) {			
				importInfo.addConcatError("Format error.   Internal key: " + internalKey + "   Format: " + fieldFormat + "   Value: " + fieldValue);
			}
		}
		else if (fieldFormat.equals("float")) {
			try {
				Double value = Double.parseDouble(fieldValue);
			} catch (Exception e) {
				importInfo.addConcatError("Format error.   Internal key: " + internalKey + "   Format: " + fieldFormat + "   Value: " + fieldValue);
			}
		}
		else if (fieldFormat.equals("decimal")) {
			try {
				Double value = Double.parseDouble(fieldValue);
			} catch (Exception e) {
				importInfo.addConcatError("Format error.   Internal key: " + internalKey + "   Format: " + fieldFormat + "   Value: " + fieldValue);
			}
		}
		else if (fieldFormat.equals("integer")) {
			try {
				Integer value = Integer.parseInt(fieldValue);
			} catch (Exception e) {
				importInfo.addConcatError("Format error.   Internal key: " + internalKey + "   Format: " + fieldFormat + "   Value: " + fieldValue);
			}
		} 
		else if (fieldFormat.equals("pos-dd")) {
			try {
				Double value = Double.parseDouble(fieldValue);
			} catch (Exception e) {
				importInfo.addConcatError("Format error.   Internal key: " + internalKey + "   Format: " + fieldFormat + "   Value: " + fieldValue);
			}
		} 
		else if (fieldFormat.equals("pos-dm")) {
			if ((fieldValue.length() < 8) && (!fieldValue.contains("."))) {
				importInfo.addConcatError("Format error.   Internal key: " + internalKey + "   Format: " + fieldFormat + "   Value: " + fieldValue);
			}
		} 
	}

}
