/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.formats;

import java.io.PrintStream;

import se.smhi.sharkadm.datasets.fileimport.FileImportInfo;

/**
 * Base class for file import scripts. 
 */
public abstract class FormatXmlBase extends FormatBase {
	
	public FormatXmlBase(PrintStream logInfo, FileImportInfo importInfo) {
		super(logInfo, importInfo);
	}
	
}
