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

import se.smhi.sharkadm.datasets.calc.BenticQualityIndex;
import se.smhi.sharkadm.datasets.fileimport.FileImportInfo;
import se.smhi.sharkadm.facades.ImportFacade;
import se.smhi.sharkadm.facades.ModelFacade;
import se.smhi.sharkadm.fileimport.misc.FileImportTranslate;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.utils.ConvUtils;
import se.smhi.sharkadm.utils.ParseFileUtil;

public class FormatFileBeda extends FormatFileBase {
		
		public FormatFileBeda(PrintStream logInfo, FileImportInfo importInfo) {
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

		loadKeyTranslator(importMatrixColumn, "import_matrix_zoobenthos.txt");
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
			if (Files.exists(Paths.get(zipFileName, "processed_data", "dataHugg.txt"))) {
				filePath = Paths.get(zipFileName, "processed_data", "dataHugg.txt");
				bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));
				fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
				if (fileContent != null) {
					importHugg(fileContent);
				}
			}
			if (Files.exists(Paths.get(zipFileName, "processed_data", "dataGlodVatten.txt"))) {
				filePath = Paths.get(zipFileName, "processed_data", "dataGlodVatten.txt");
				bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));
				fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
				if (fileContent != null) {
					importGlodVatten(fileContent);
				}
			}
			if (Files.exists(Paths.get(zipFileName, "processed_data", "dataBottenvatten.txt"))) {
				filePath = Paths.get(zipFileName, "processed_data", "dataBottenvatten.txt");
				bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));
				fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
				if (fileContent != null) {
					importBottenvatten(fileContent);
				}
			}
			if (Files.exists(Paths.get(zipFileName, "processed_data", "dataRedox.txt"))) {
				filePath = Paths.get(zipFileName, "processed_data", "dataRedox.txt");
				bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));
				fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
				if (fileContent != null) {
					importRedox(fileContent);
				}
			}
			if (Files.exists(Paths.get(zipFileName, "processed_data", "dataSedimentfarg.txt"))) {
				filePath = Paths.get(zipFileName, "processed_data", "dataSedimentfarg.txt");
				bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));
				fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
				if (fileContent != null) {
					importSedimentfarg(fileContent);
				}
			}
			
			
			
			if (Files.exists(Paths.get(zipFileName, "processed_data", "dataKornstorlek.txt"))) {
				filePath = Paths.get(zipFileName, "processed_data", "dataKornstorlek.txt");
				bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));
				fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
				if (fileContent != null) {
					importKornstorlek(fileContent);
				}
			}
			
			
			
		} catch (Exception e) {
			importInfo.addConcatError("FAILED TO IMPORT FILE.");
		}

		// Used by the Observer pattern.
		ModelFacade.instance().modelChanged();
	}

	private void importHugg(List<String[]> fileContent) {
//		Undersökning	stnbet	stnnamn	station.latitud	station.longitud	kluster	kluster_beskrivning	station.djup	station.djupintervall	station.kommentar	Mprog	besok.datum	expledare	kvalitetsgranskare	besok.djup	besok.djupintervall	besok.latitud	besok.longitud	slabo	ackrediterad	uppdragsgivare	projektnr	shipc	posys	vindriktning	vindhastighet	vaghojd	besok.h2s	sedimentbeskrivning	oxideratskikt	besok.faltkommentar	besok.labkommentar	SMTYP	provNr	ovreSall	nedreSall	huggyta	huggvikt	provvolym	provdjup	noFauna	METFP	prov.h2s	poolat	alabo	sorteratAv	vagtAv	sorteringsdatum	vagningsdatum	prov.faltkommentar	prov.labkommentar	nport	cport	taxonID	namn	taxon.datum	sflag	abundans	vvprefix	vatvikt	tvprefix	torrvikt	atvvprefix	askfritorrvikt	determinator	provart.kommentar	countnr

		String[] header = null;
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Hugg file: " + fileContent.size() + " rows (header included).");
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
				
				// Create or reuse sample for each row.
				getCurrentSample(row);
				
				// Create community variable for this row.
				currentVariable = new Variable(true);
				currentSample.addVariable(currentVariable);
				
				// Save lat/long for this sample to be used for extra samples.
				String tmpLat = getCell(row, "besok.latitud");
				String tmpLong = getCell(row, "besok.longitud");
				if ((!tmpLat.equals("")) && (!tmpLat.equals(""))) {
					currentVisit.addTempField("sample.TEMP.besok.latitud", tmpLat);
					currentVisit.addTempField("sample.TEMP.besok.longitud", tmpLong);
				}
				
				// Add each column value.
				for (String columnName : header) {
					String key = translateKey(columnName);
					addVariableField(key, getCell(row, columnName));
				}					
				addedItems++;
			}
		}
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Hugg file, processed rows: " + addedItems + ".");
		}
	}

	private void importGlodVatten(List<String[]> fileContent) {
//		Undersökning	stnbet	datum	provNr	SMTYP	kommentar	ovreniv	nedreniv	vattenhalt	glodforlust
		
		String[] header = null;
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: GlodVatten file: " + fileContent.size() + " rows (header included).");
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
				
				// Create or reuse sample for each row.
				getCurrentSampleExtra(row, "GlodVatten"); // Note: Not same sample as in dataHugg.
				
				// Create variable for this row.
				currentVariable = new Variable(false);
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
			logInfo.println("Info: GlodVatten file, processed rows: " + addedItems + ".");
		}
	}
	
	private void importBottenvatten(List<String[]> fileContent) {
//		Undersökning	stnbet	datum	bottenvattenProv.provnr	SMTYP	kommentar	bottenvatten.provnr	salinitet	temperatur	syrehalt	syremattnad
		
		String[] header = null;
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Bottenvatten file: " + fileContent.size() + " rows (header included).");
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
				
				// Create or reuse sample for each row.
				getCurrentSampleExtra(row, "Bottenvatten"); // Note: Not same sample as in dataHugg.
				
				// Create variable for this row.
				currentVariable = new Variable(false);
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
			logInfo.println("Info: Bottenvatten file, processed rows: " + addedItems + ".");
		}
	}
	
	private void importRedox(List<String[]> fileContent) {
		
		String[] header = null;
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Redox file: " + fileContent.size() + " rows (header included).");
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
				
				// Create or reuse sample for each row.
				getCurrentSampleExtra(row, "Redox"); // Note: Not same sample as in dataHugg.
				
				// Create variable for this row.
				currentVariable = new Variable(false);
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
			logInfo.println("Info: Redox file, processed rows: " + addedItems + ".");
		}
	}
	
	private void importSedimentfarg(List<String[]> fileContent) {
		
		String[] header = null;
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Sedimentfarg file: " + fileContent.size() + " rows (header included).");
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
				
				// Create or reuse sample for each row.
				getCurrentSampleExtra(row, "Sedimentfarg"); // Note: Not same sample as in dataHugg.
				
				// Create variable for this row.
				currentVariable = new Variable(false);
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
			logInfo.println("Info: Sedimentfarg file, processed rows: " + addedItems + ".");
		}
	}
	
	private void importKornstorlek(List<String[]> fileContent) {
		
		String[] header = null;
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Kornstorlek file: " + fileContent.size() + " rows (header included).");
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
				
				// Create or reuse sample for each row.
				getCurrentSampleExtra(row, "Kornstorlek"); // Note: Not same sample as in dataHugg.
				
				// Create variable for this row.
				currentVariable = new Variable(false);
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
			logInfo.println("Info: Kornstorlek file, processed rows: " + addedItems + ".");
		}
	}
	
	@Override
	public void getCurrentVisit(String[] row) {		
		// Hugg: stnbet	besok.datum			
		// GlodVatten: stnbet	datum			
		// Bottenvatten: stnbet	datum			
		String dateString = getCell(row, "besok.datum");
		if (dateString.length() == 0) {
			dateString = getCell(row, "datum");
		}							
		if (dateString.length() > 10) {
			dateString = dateString.substring(0, 10);
		}
		
		String keyString = 
			getCell(row, "stnbet") + ":" +
			dateString;
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
		// Hugg: stnbet	besok.datum	provNr			
		// GlodVatten: stnbet	datum	provNr			
		// Bottenvatten: stnbet	datum	bottenvattenProv.provnr
		
		String dateString = getCell(row, "besok.datum");
		if (dateString.length() == 0) {
			dateString = getCell(row, "datum");
		}							
		if (dateString.length() > 10) {
			dateString = dateString.substring(0, 10);
		}
		
		String keyString = 
					getCell(row, "stnbet") + ":" +
					dateString + ":" +
					getCell(row, "provNr") + ":" + 
					getCell(row, "SMTYP") + ":" + 
					getCell(row, "bottenvattenProv.provnr") + ":" +
					getCell(row, "ovreniv") + ":" +
					getCell(row, "nedreniv");
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

//	public void getCurrentVisitExtra(String[] row, String sampleDescr) {		
//		// Hugg: stnbet	besok.datum			
//		// GlodVatten: stnbet	datum			
//		// Bottenvatten: stnbet	datum			
//		String dateString = getCell(row, "besok.datum");
//		if (dateString.length() == 0) {
//			dateString = getCell(row, "datum");
//		}							
//		if (dateString.length() > 10) {
//			dateString = dateString.substring(0, 10);
//		}
//		
//		String keyString = 
//			getCell(row, "stnbet") + ":" +
//			dateString + ":" +
//			sampleDescr;
//		Visit visit = null;
//		for (Visit v : dataset.getVisits()) {
//			if (v.getFileImportKeyString().equals(keyString)) {
//				visit = v;
//				currentVisit = v;
//			}
//		}
//		if (visit == null) {
//			currentVisit = new Visit(keyString);
//			dataset.addVisit(currentVisit);
//		}
//	}

	public void getCurrentSampleExtra(String[] row, String sampleDescr) {
		// Hugg: stnbet	besok.datum	provNr			
		// GlodVatten: stnbet	datum	provNr			
		// Bottenvatten: stnbet	datum	bottenvattenProv.provnr
		
		String dateString = getCell(row, "besok.datum");
		if (dateString.length() == 0) {
			dateString = getCell(row, "datum");
		}							
		if (dateString.length() > 10) {
			dateString = dateString.substring(0, 10);
		}
		
		String keyString = 
			getCell(row, "stnbet") + ":" +
					dateString + ":" +
					getCell(row, "provNr") + ":" + 
					getCell(row, "SMTYP") + ":" +
					getCell(row, "bottenvattenProv.provnr") + ":" +
//					getCell(row, "ovreniv") + ":" +
//					getCell(row, "nedreniv") + ":" +
					getCellByKey(row, "variable.upper_mesh_size_um") + ":" +
					getCellByKey(row, "variable.lower_mesh_size_um") + ":" +
					sampleDescr;
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
		}
		
		// Add sample-id-prefix to be used later as a part in sample-id.
		if      (sampleDescr.equals("GlodVatten")) { currentSample.addField("sample.TEMP.sample_id_prefix", "GV-"); }
		else if (sampleDescr.equals("Bottenvatten")) { currentSample.addField("sample.TEMP.sample_id_prefix", "BV-"); }
		else if (sampleDescr.equals("Redox")) { currentSample.addField("sample.TEMP.sample_id_prefix", "RX-"); }
		else if (sampleDescr.equals("Sedimentfarg")) { currentSample.addField("sample.TEMP.sample_id_prefix", "SF-"); }

		else if (sampleDescr.equals("Kornstorlek")) { currentSample.addField("sample.TEMP.sample_id_prefix", "KS-"); }
		
		// Add sample lat/long for extra samples. Stored in visit from "DataHugg".
		String tmpLat = currentSample.getTempField("sample.TEMP.besok.latitud");
		String tmpLong = currentSample.getTempField("sample.TEMP.besok.longitud");
		if ((!tmpLat.equals("")) && (!tmpLat.equals(""))) {
			currentSample.addField("sample.sample_reported_latitude", tmpLat);
			currentSample.addField("sample.sample_reported_longitude", tmpLong);
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
		
		// Fix date&time when both are reported in Visit_date.
		String visitDate = sample.getParent().getField("visit.visit_date");
		if (!visitDate.equals("")) {
			String tmpDate = "";
			String tmpTime = "";
			if (visitDate.length() > 10) {
				tmpDate = visitDate.substring(0, 10).trim();
				tmpTime = visitDate.substring(10).trim();
				
				visitDate = tmpDate;
				sample.addField("sample.sample_time", tmpTime);
				
				System.out.println("DEBUG Beda: Date: " + tmpDate + "   time: " + tmpTime);
			}
		}
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

		// We need a dummy variable to indicate empty samples.
		if ((sample.getField("sample.fauna_flora_found").equals("N")) ||
			(sample.getField("sample.fauna_flora_found").equals("1"))){
			Variable newVariable = new Variable(true);
			sample.addVariable(newVariable);
//			newVariable.setParameter("NO SPECIES IN SAMPLE");
			newVariable.setParameter("No species in sample");
			newVariable.setValue("0");
			newVariable.setUnit("ind");
			newVariable.addField("variable.reported_scientific_name", "<no fauna/flora>");
		}
		
		// Calculate BQI, Bentic Quality Index.
		try {
		BenticQualityIndex.instance().calculateBqiForSample(sample, importInfo);		
		} catch (Exception e) {
			importInfo.addConcatWarning(
					"Failed to calculate BQI for sample.   Date: " + sample.getParent().getField("visit.visit_date") + 
					"   Station: " + sample.getParent().getField("visit.reported_station_name") );
//			System.out.println("DEBUG: Exception in calculateBqiForSample.");
		}
	}

	@Override
	public void postReformatVariable(Variable variable) {
		// Calculate values and change parameters/units.
		try {
			if (variable.getParameter().equals("# counted")) {
				Double value = variable.getValueAsDouble();

				Double samplerArea = variable.getParent().getFieldAsDouble("sample.sampler_area_cm2");
				Double countedPortions = variable.getFieldAsDouble("variable.counted_portions");
				Double numberOfPortions = variable.getFieldAsDouble("variable.number_of_portions");
				
				if (countedPortions == null) {
					countedPortions = 1.0;
				}
				if (numberOfPortions == null) {
					numberOfPortions = 1.0;
				}
				
				if (samplerArea != null) {
					
					// VALUE = (COUNTNR * NPORT) / (SAREA * CPORT) * 10000
					Double param = (value * numberOfPortions)
							/ (samplerArea * countedPortions) * 10000.0;
//					param = Math.round(param * 10.0) / 10.0; // 1 decimal.
	
					Variable newVariable = variable.copyVariableAndData();
//					newVariable.setParameter("ABUND");
					newVariable.setParameter("Abundance");
//					newVariable.setValue(param.toString());
					newVariable.setValue(ConvUtils.convDoubleToString(param));
					newVariable.setUnit("ind/m2");
					newVariable.addField("variable.calc_by_dc", "Y");
					//
					variable.getParent().addVariable(newVariable);
				}

			}
		} catch (Exception e) {
			importInfo.addConcatWarning("Failed to calculate value. Parameter:" + variable.getParameter());
		}
		
	}

}

