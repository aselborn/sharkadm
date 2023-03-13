/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.formats;

import java.io.PrintStream;
import java.util.regex.Pattern;

import se.smhi.sharkadm.datasets.fileimport.FileImportInfo;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;

public class FormatXmlBeda extends FormatXmlBase {
	
	public FormatXmlBeda(PrintStream logInfo, FileImportInfo importInfo) {
		super(logInfo, importInfo);
	}

	public void importFiles(String zipFileName, Dataset dataset) {
		this.dataset = dataset;


		String importMatrixColumn = "";
		if (dataset.getImport_format().contains(":")) {
//			String[] strings = dataset.getImport_format().split(":");
			String[] strings = dataset.getImport_format().split(Pattern.quote(":"));
			importMatrixColumn = strings[1].trim();			
		} else {
			importMatrixColumn = "";
		}
		
		loadKeyTranslator(importMatrixColumn, "import_matrix_phytobenthos.txt");
//		loadKeyTranslator(importMatrixColumn, "import_matrix.txt");
		dataset.setImport_matrix_column(importMatrixColumn);

		if (getTranslateKeySize() == 0) {
			importInfo.addConcatError("Empty column in import matrix. Import aborted.");
			return;
		}
//		dataset.setImport_status("TEST");
//		importInfo.addConcatWarning("TEST IMPORT. Import format under development.");
		dataset.setImport_status("DATA");
	}

	@Override
	public void getCurrentSample(String[] row) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getCurrentVariable(String[] row) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getCurrentVisit(String[] row) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postReorganizeDataset(Dataset dataset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postReorganizeVisit(Visit visit) {
//		visit.addField("visit.visit_year", 
//				utils.convNoDecimal(visit.getField("visit.visit_year")));
//		visit.addField("visit.visit_date", 
//				utils.convDate(visit.getField("visit.visit_date")));
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
