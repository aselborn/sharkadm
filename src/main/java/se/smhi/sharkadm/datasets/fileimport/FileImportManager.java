/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.fileimport;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import se.smhi.sharkadm.datasets.formats.FormatBase;
import se.smhi.sharkadm.datasets.formats.SelectFormat;
import se.smhi.sharkadm.fileimport.misc.FileImportTranslateCodes_NEW;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.ModelTopNode;
import se.smhi.sharkadm.translate.TranslateCodesManager_NEW;
import se.smhi.sharkadm.utils.ErrorLogger;

/**
 * TODO.
 * Design Pattern: Template Method.
 */
public class FileImportManager {
	
	private PrintStream logInfo;	
	private FileImportInfo importInfo = new FileImportInfo();
	private Dataset dataset;
	private FormatBase fileImport;
	DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public FileImportManager(PrintStream logInfo) {
		this.logInfo = logInfo;
	}
		
	public void importSharkFolder(String sharkFolderPath) {		
		logInfo.println("");
		logInfo.println("FILE IMPORT:");
		logInfo.println("File import started at: " + dateTimeFormat.format(new Date()));
		logInfo.println("File: " + sharkFolderPath);
		
		// === Read dataset note ===
		dataset = new Dataset(sharkFolderPath);
		logInfo.println("Import format: " + dataset.getImport_format() + ".");
	
		// DEBUG-log:
		ErrorLogger.println(""); 
		ErrorLogger.println("FILE IMPORT: Started: " + dateTimeFormat.format(new Date())); 
		ErrorLogger.println("File: " + sharkFolderPath);
		ErrorLogger.println("Format: " + dataset.getImport_format() + ".");
		
		// === Create dataset top node. ===
		ModelTopNode.instance().addDataset(dataset);
		
		// === Import data to memory model. ===
		logInfo.println("");
		logInfo.println("PHASE 1: Import data to memory model.");
		ErrorLogger.println("PHASE 1: Import data to memory model.");
		logInfo.println("");
		try {
			ImportDataToMemoryModel(sharkFolderPath, dataset);
		} catch (Exception e) {
			ErrorLogger.println("PHASE 1: Exception: " + e.getMessage());
			// Note: It is not recommended to put message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Exception i PHASE 1: Import data to memory model.");
			messageBox.setMessage("Error: " + e.getMessage());
			messageBox.open();
			e.printStackTrace();
		}
		
		if (fileImport == null) {
			importInfo.logConcatInfo(logInfo);
			importInfo.logConcatWarnings(logInfo);
			importInfo.logConcatErrors(logInfo);
			importInfo.clearConcatLists();
			importInfo.addConcatError("IMPORT INTERRUPTED.");
			importInfo.logConcatErrors(logInfo);
			return;
		}

		// === Reorganize data in memory model. ===
		logInfo.println("");
		logInfo.println("PHASE 2: Reorganize data in memory model.");
		ErrorLogger.println("PHASE 2: Reorganize data in memory model.");
		logInfo.println("");
		try {
			ReorganizeDataInMemoryModel();
		} catch (Exception e) {
			ErrorLogger.println("PHASE 2: Exception: " + e.getMessage());
			// Note: It is not recommended to put message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Exception i PHASE 2: Reorganize data in memory model.");
			messageBox.setMessage("Error: " + e.getMessage());
			messageBox.open();
			e.printStackTrace();
		}
		
		// === Reformat data in memory model. ===
		logInfo.println("");
		logInfo.println("PHASE 3: Reformat data in memory model.");
		ErrorLogger.println("PHASE 3: Reformat data in memory model.");
		logInfo.println("");
		try {
					ReformatDataInMemoryModel();
		} catch (Exception e) {
			ErrorLogger.println("PHASE 3: Exception: " + e.getMessage());
			// Note: It is not recommended to put message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Exception i PHASE 3: Reformat data in memory model.");
			messageBox.setMessage("Error: " + e.getMessage());
			messageBox.open();
			e.printStackTrace();
		}
		
		// === Check mandatory data in memory model. ===
		logInfo.println("");
		logInfo.println("PHASE 4: Check mandatory data in memory model.");
		ErrorLogger.println("PHASE 4: Check mandatory data in memory model.");
		logInfo.println("");
		try {
					CheckDataInMemoryModel(); 
		} catch (Exception e) {
			ErrorLogger.println("PHASE 4: Exception: " + e.getMessage());
			// Note: It is not recommended to put message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("PHASE 4: Check mandatory data in memory model.");
			messageBox.setMessage("Error: " + e.getMessage());
			messageBox.open();
			e.printStackTrace();
		}

		// === Statistics. ===
		logInfo.println("");
		logInfo.println("IMPORT SUMMARY:");
		logInfo.println("- Visits: " + importInfo.getVisitCounter());
		logInfo.println("- Samples: " + importInfo.getSampleCounter());
		logInfo.println("- Variables: " + importInfo.getVariableCounter());
		logInfo.println("");
		logInfo.println("- Warnings: " + importInfo.getWarningCounter());
		logInfo.println("- Errors: " + importInfo.getErrorCounter());
		
		logInfo.println("");
		if (importInfo.getWarningCounter() > 0) {
			logInfo.println("This import contains warnings: " + importInfo.getWarningCounter() + " warning(s).");
		}
		if (importInfo.getErrorCounter() > 0) {
			logInfo.println("This import contains ERRORS: " + importInfo.getErrorCounter() + " error(s).");
		} 
		logInfo.println("");
		logInfo.println("File import finished at: " + dateTimeFormat.format(new Date()));
		logInfo.println("");
	
		ErrorLogger.println("IMPORT SUMMARY:");
		ErrorLogger.println("- Visits: " + importInfo.getVisitCounter());
		ErrorLogger.println("- Samples: " + importInfo.getSampleCounter());
		ErrorLogger.println("- Variables: " + importInfo.getVariableCounter());
		ErrorLogger.println("- Warnings: " + importInfo.getWarningCounter());
		ErrorLogger.println("- Errors: " + importInfo.getErrorCounter());
		
		if (importInfo.getWarningCounter() > 0) {
			ErrorLogger.println("This import contains warnings: " + importInfo.getWarningCounter() + " warning(s).");
		}
		if (importInfo.getErrorCounter() > 0) {
			ErrorLogger.println("This import contains ERRORS: " + importInfo.getErrorCounter() + " error(s).");
		} 
		ErrorLogger.println("File import finished at: " + dateTimeFormat.format(new Date()));
		ErrorLogger.println("");
	}

	protected void ImportDataToMemoryModel(String zipFile, Dataset dataset) {
		importInfo.clearConcatLists();
		
		SelectFormat selector = new SelectFormat(logInfo, importInfo);
		fileImport = selector.getFileImport(zipFile, dataset.getImport_format());

		if (fileImport == null) {
			importInfo.addConcatError("Import script not found.");
			return; // TODO Replace with throw exception.
		} else {
			logInfo.println("" +
					"Import script: " +
					fileImport.getClass().getSimpleName() + ".java");
		}
		
		fileImport.importFiles(zipFile, dataset);
		
		importInfo.logConcatInfo(logInfo);
		importInfo.logConcatWarnings(logInfo);
		importInfo.logConcatErrors(logInfo);
	}
	
	protected void ReorganizeDataInMemoryModel() {
		// Reorganize all nodes by using the Visitor pattern.
		importInfo.clearConcatLists();
		
		MemoryModelReorganizeData reorganizeFields = new MemoryModelReorganizeData(logInfo, importInfo, fileImport);
		reorganizeFields.visitDataset(dataset);		

		importInfo.logConcatInfo(logInfo);
		importInfo.logConcatWarnings(logInfo);
		importInfo.logConcatErrors(logInfo);
	}
	
	protected void ReformatDataInMemoryModel() {
		// Reformat all nodes by using the Visitor pattern.
		importInfo.clearConcatLists();
		
		// For code translations.
		TranslateCodesManager_NEW translateCodesManager = TranslateCodesManager_NEW.instance();
		if (!translateCodesManager.isFilesLoaded()) {
			FileImportTranslateCodes_NEW fileImportTranslateCodes = new FileImportTranslateCodes_NEW(logInfo);
			fileImportTranslateCodes.importFiles("");
		}
		MemoryModelReformatData reformatFields = new MemoryModelReformatData(logInfo, importInfo, fileImport);
		reformatFields.visitDataset(dataset);		

		importInfo.logConcatInfo(logInfo);
		importInfo.logConcatWarnings(logInfo);
		importInfo.logConcatErrors(logInfo);
	}
	
	protected void CheckDataInMemoryModel() {
		// Check all nodes by using the Visitor pattern.
		importInfo.clearConcatLists();
		
		MemoryModelCheckData memoryModelCheck = new MemoryModelCheckData(logInfo, importInfo);
		memoryModelCheck.visitDataset(dataset);
		
		importInfo.logConcatInfo(logInfo);
		importInfo.logConcatWarnings(logInfo);
		importInfo.logConcatErrors(logInfo);
	}

}
