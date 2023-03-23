/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.fileimport.misc;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import se.smhi.sharkadm.datasets.fileimport.SingleFileImport;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.sql.SqliteManager;
import se.smhi.sharkadm.translate.TranslateCodesManager_NEW;
import se.smhi.sharkadm.translate.TranslateCodesObject_NEW;
import se.smhi.sharkadm.utils.ParseFileUtil;

public class FileImportTranslateCodes_NEW extends SingleFileImport {
	
	private TranslateCodesManager_NEW translateCodesManager = TranslateCodesManager_NEW.instance();
	private SqliteManager mSqliteManager = SqliteManager.getInstance();
	public FileImportTranslateCodes_NEW(PrintStream logInfo) {
		super(logInfo);
	}

	public void importFiles(String zipFileName, Dataset dataset) {
	}

	public void importFiles(String zipFileName) {
		
		this.translateCodesManager.clear();
		
		this.importTranslateCodesFile("translate_codes_NEW.txt");

		// Prepare lookup lists.
		this.translateCodesManager.prepareTranslateLists();

		translateCodesManager.setFilesLoaded(true);
	}
	
	public void importTranslateCodesFile(String fileName ) {
		List<String[]> fileContent;
		BufferedReader bufferedReader = null;
		
		try {
			bufferedReader = ParseFileUtil.GetSharkConfigFile(fileName);
			
			
			fileContent = ParseFileUtil.parseDataFile(bufferedReader, false);
			if (fileContent != null) {					
				importTranslateCodesList(fileContent);
			}
			
		} catch (Exception e) {
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Config import");
			messageBox.setMessage("Failed to import " + fileName + ".");
			messageBox.open();
		}
	}
	
	private void importTranslateCodesList(List<String[]> fileContent) {
		String[] header = null;

		for (String[] row : fileContent) {
			if (header == null) { 
				// The first line contains the header.
				header = row;
			} else {
				if (row.length < 4) {
					continue;
				}
				
				TranslateCodesObject_NEW object = new TranslateCodesObject_NEW();
				object.parseRow(header, row);
				this.translateCodesManager.addTranslateObject(object);
				
				String field = object.getField();
				if (field.equals("laboratory")) {
					// Add sampling_laboratory_code.
					object = new TranslateCodesObject_NEW();
					object.parseRow(header, row);
					object.setField("sampling_laboratory_code");
					this.translateCodesManager.addTranslateObject(object);
					// Add sampling_laboratory_code_phyche.
					object = new TranslateCodesObject_NEW();
					object.parseRow(header, row);
					object.setField("sampling_laboratory_code_phyche");
					this.translateCodesManager.addTranslateObject(object);
					// Add analytical_laboratory_code.
					object = new TranslateCodesObject_NEW();
					object.parseRow(header, row);
					object.setField("analytical_laboratory_code");
					this.translateCodesManager.addTranslateObject(object);
					// Add reporting_institute_code.
					object = new TranslateCodesObject_NEW();
					object.parseRow(header, row);
					object.setField("reporting_institute_code");
					this.translateCodesManager.addTranslateObject(object);
					// Add sample_orderer_code.
					object = new TranslateCodesObject_NEW();
					object.parseRow(header, row);
					object.setField("sample_orderer_code");
					this.translateCodesManager.addTranslateObject(object);
				}
			}
		}
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
