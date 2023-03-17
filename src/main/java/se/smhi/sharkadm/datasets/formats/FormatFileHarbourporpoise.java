/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2015 SMHI, Swedish Meteorological and Hydrological Institute. 
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

//import se.smhi.sharkadm.utils.ZipFileUtil;
import se.smhi.sharkadm.datasets.fileimport.FileImportInfo;
import se.smhi.sharkadm.facades.ImportFacade;
import se.smhi.sharkadm.facades.ModelFacade;
import se.smhi.sharkadm.fileimport.misc.FileImportTranslate;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.utils.ParseFileUtil;

/**
 * Import format for Harbourporpoise.
 * 
 */
public class FormatFileHarbourporpoise extends FormatFileBase {

	public FormatFileHarbourporpoise(PrintStream logInfo, FileImportInfo importInfo) {
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

		loadKeyTranslator(importMatrixColumn, "import_matrix_harbourporpoise.txt");
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

	
	@Override
	public void getCurrentVisit(String[] row) {
		String keyString = getCellByKey(row, "visit.reported_station_name") + ":"
				+ getCellByKey(row, "visit.visit_date") + ":"
				+ getCellByKey(row, "sample.sample_date");
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
		String keyString = getCellByKey(row, "visit.reported_station_name") + ":"
				+ getCellByKey(row, "visit.visit_date") + ":"
				+ getCellByKey(row, "sample.sample_date");
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
//		for (Variable v : sample.getVariables()) {
//			if ((v.getValue().equals("Yes")) || (v.getValue().equals("y"))) {
//				v.setValue("Y");
//			}
//			else if ((v.getValue().equals("No")) || (v.getValue().equals("n"))) {
//				v.setValue("N");
//			}
//		}
	}

	@Override
	public void postReformatVariable(Variable variable) {
		// TODO Auto-generated method stub

	}

}
