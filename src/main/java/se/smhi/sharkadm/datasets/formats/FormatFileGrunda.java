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
import se.smhi.sharkadm.utils.ParseFileUtil;

public class FormatFileGrunda extends FormatFileBase {
	
	public FormatFileGrunda(PrintStream logInfo, FileImportInfo importInfo) {
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

		loadKeyTranslator(importMatrixColumn, "import_matrix_epibenthos.txt");
//		loadKeyTranslator(importMatrixColumn, "import_matrix.txt");
		dataset.setImport_matrix_column(importMatrixColumn);

		if (getTranslateKeySize() == 0) {
			importInfo.addConcatError("Empty column in import matrix. Import aborted.");
			return;
		}
		dataset.setImport_status("DATA");
		
//		// TEST		keyTranslate.put("Tr�dalger", "variable.COPY_VARIABLE.Cover class filamentous algae.class"); // TODO: ...
//		keyTranslate.put("Tr�dalger", "sample.CREATE_VARIABLE.Cover class filamentous algae.class"); // TODO: ...
		
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
			if (Files.exists(Paths.get(zipFileName, "processed_data", "transekt.txt"))) {
				filePath = Paths.get(zipFileName, "processed_data", "transekt.txt");
				bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));
				fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
				if (fileContent != null) {
					importTransect(fileContent);
				}
			}
			if (Files.exists(Paths.get(zipFileName, "processed_data", "hyd.txt"))) {
				filePath = Paths.get(zipFileName, "processed_data", "hyd.txt");
				bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));
				fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
				if (fileContent != null) {
					importHyd(fileContent);
				}
			}
			if (Files.exists(Paths.get(zipFileName, "processed_data", "segment.txt"))) {
				filePath = Paths.get(zipFileName, "processed_data", "segment.txt");
				bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));
				fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
				if (fileContent != null) {
					importSegment(fileContent);
				}
			}
			if (Files.exists(Paths.get(zipFileName, "processed_data", "ruta.txt"))) {
				filePath = Paths.get(zipFileName, "processed_data", "ruta.txt");
				bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));
				fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
				if (fileContent != null) {
					importSquare(fileContent);
				}
			}			
		} catch (Exception e) {
			importInfo.addConcatError("FAILED TO IMPORT FILE.");
		}

		// Used by the Observer pattern.
		ModelFacade.instance().modelChanged();
	}

	private void importTransect(List<String[]> fileContent) {

//		undersokningundID	undnamn	syfte	referens	metodik	basinventeringsytaytaID	db	Vikens namn	objID	omgivningssnamn	naturtypnaturtypID	naturtyp	inventeringstillfalletillfalleID	BasinventeringsID	F�lt15	Startdatum	Slutdatum	Inventerare	inventeringstillfalleytaID	inventeringstillfalleundID	P�verkansgrad	transektID	Transekt	Start latitud	Start longitud	Slut latitud	Slut longitud	L�ngd	Kommentar	transekttillfalleID	WPSerie	WPIDStart	WPIDSlut

		
		// "tillfalleID is needed to match HYD data."
		keyTranslate.put("tillfalleID", "visit.TEMP.tillfalleID"); // TODO: ...

		
		String[] header = null;
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Transekt" + fileContent.size() + " rows (header included).");
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
				
//				// Create or reuse sample for each row.
//				getCurrentSample(row);
				
				// Add each column value.
				for (String columnName : header) {
					String key = translateKey(columnName);
//					addSampleField(key, getCell(row, columnName));
					addVisitField(key, getCell(row, columnName));
				}					
				addedItems++;
			}
		}
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Transekt file, processed rows: " + addedItems + ".");
		}
		
		
		// Remove "tillfalleID" from keyTranslate.
		keyTranslate.put("tillfalleID", "NOT_USED"); // TODO: ...
		keyTranslate.put("tillfalleID", "NOT_USED"); // TODO: ...

	}

	private void importHyd(List<String[]> fileContent) {
//		hydrID	Latitud	Longitud	Temperatur	Salthalt	Siktdjup	tillfalleID	Turbiditet	WPSerie	WPIDStart
		
		String[] header = null;
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Hyd file: " + fileContent.size() + " rows (header included).");
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
				// Check all transects and create one sample for each with the same "tillfalleID".
				String tillfalleID = getCell(row, "tillfalleID");
				Visit visit = null;
				for (Visit v : dataset.getVisits()) {
					if (v.getField("visit.TEMP.tillfalleID").equals(tillfalleID)) {
						// Create one sample for each row.
						currentVisit = v;
						currentSample = new Sample("");
						currentVisit.addSample(currentSample);
						
						// Add each column value.
						for (String columnName : header) {
							String key = translateKey(columnName);
							addSampleField(key, getCell(row, columnName));
						}					
						addedItems++;
					}
				}
			}
		}
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Hyd file, processed rows: " + addedItems + ".");
		}
	}
	
	private void importSegment(List<String[]> fileContent) {
//		Nr	segmentsegmentID	Startpunkt	Slutpunkt	transektID	Kommentar	Tr�dalger	segmenttaxonsegmentID	taxonID	T�ckningsgrad skala	flagga	determinator	segmenttaxonkommentar
		
		String[] header = null;
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Segment file: " + fileContent.size() + " rows (header included).");
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
				
//				// Special. Visit is samples parent.
//				currentVisit = currentSample.getParent();
				
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
			logInfo.println("Info: Segment file, processed rows: " + addedItems + ".");
		}
	}
	
	private void importSquare(List<String[]> fileContent) {
//		Nr	rutarutID	Avst�nd fr�n start	Djup i meter	Tr�dalger	transektID	Latitud	Longitud	ruttaxonrutID	taxonID	T�ckningsgrad i procent	flagga	determinator	kommentar
		
		String[] header = null;
		if (ImportFacade.instance().isShowInfo()) {
			logInfo.println("Info: Square file: " + fileContent.size() + " rows (header included).");
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

//				// Special. Visit is samples parent.
//				currentVisit = currentSample.getParent();

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
			logInfo.println("Info: Square file, processed rows: " + addedItems + ".");
		}
	}
	
	@Override
	public void getCurrentVisit(String[] row) {
//		String keyString = getCell(row, "tillfalleID");
		String keyString = getCell(row, "transektID");
			
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
		String keyString = getCell(row, "transektID") + ":" +
		   getCell(row, "rutarutID") + ":" +
		   getCell(row, "segmentsegmentID");
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
		// Start position for transect should be used as position for visit.
		sample.getParent().addField("visit.visit_reported_latitude", 
				sample.getField("sample.transect_start_latitude_dd"));		
		sample.getParent().addField("visit.visit_reported_longitude", 
				sample.getField("sample.transect_start_longitude_dd"));
		
		// If min-distance has value and not max-distance, copy value to max-distance.
		if ((!sample.getField("sample.transect_min_distance").equals("")) &&
			(sample.getField("sample.transect_max_distance").equals(""))) {
			sample.addField("sample.transect_max_distance", 
					sample.getField("sample.transect_min_distance"));
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
		// TODO Auto-generated method stub
	}

	@Override
	public void postReformatVariable(Variable variable) {
		// TODO Auto-generated method stub
		
	}

}

