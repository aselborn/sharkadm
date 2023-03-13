/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.fileimport.misc;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import se.smhi.sharkadm.datasets.fileimport.SingleFileImport;
import se.smhi.sharkadm.facades.ModelFacade;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.settings.SettingsManager;
import se.smhi.sharkadm.utils.ParseFileUtil;
import se.smhi.sharkadm.utils.StringUtils;

/**
 * Settings are stored in different files. The content is used by the web application Sharkweb.
 * 1. 'settings.txt': Contains a key-value list. The value parts contains structured data 
 *    in JSON format or fragments of SQL scripts.
 * 2. 'translate_header.txt': Contains columns with translations of the header rows in tables. 
 * 3. 'sharkweb_settings_viss_eu_cd.txt': Used to translate URL parameters to various selection settings. 
 *
 */
public class FileImportSettings extends SingleFileImport {

 
	public FileImportSettings(PrintStream logInfo) {
		super(logInfo);
	}
	
	public void importFiles(String zipFileName, Dataset dataset) {
		
	}

	public void importFiles(String zipFileName) {
		List<String[]> fileContent;
		BufferedReader bufferedReader = null;
		
		SettingsManager.instance().clearSettingsList();
		
		// SETTINGS.
		try {
			logInfo.println("INFO: Import 'sharkweb_settings.txt'");
			bufferedReader = ParseFileUtil.GetSharkConfigFile("sharkweb_settings.txt");
			
			fileContent = ParseFileUtil.parseDataFile(bufferedReader, false);
			if (fileContent != null) {					
				importSettingsList(fileContent);
			}
			
		} catch (Exception e) {
			logInfo.println("Failed to import Settings list ('sharkweb_settings.txt'). Error: " + e.getMessage());
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Settings import");
			messageBox.setMessage("Failed to import Settings list ('sharkweb_settings.txt').");
			messageBox.open();
		}
		
		// HEADER TRANSLATE.
		try {
			logInfo.println("INFO: Import 'translate_headers.txt'");
			bufferedReader = ParseFileUtil.GetSharkConfigFile("translate_headers.txt");
			
			fileContent = ParseFileUtil.parseDataFile(bufferedReader, false);
			if (fileContent != null) {					
				importSettingsHeadersList(fileContent);
			}
			
		} catch (Exception e) {
			logInfo.println("Failed to import 'translate_header.txt'. Error: " + e.getMessage());
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Settings import");
			messageBox.setMessage("Failed to import 'translate_header.txt'.");
			messageBox.open();
		}
		
		// COLUMN VIEWS.
		try {
			logInfo.println("INFO: Import 'column_views.txt'");
			bufferedReader = ParseFileUtil.GetSharkConfigFile("column_views.txt");
			
			fileContent = ParseFileUtil.parseDataFile(bufferedReader, false);
			if (fileContent != null) {					
				importSettingsColumnViews(fileContent);
			}
			
		} catch (Exception e) {
			logInfo.println("Failed to import 'column_views.txt'. Error: " + e.getMessage());
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Settings import");
			messageBox.setMessage("Failed to import 'column_views.txt'.");
			messageBox.open();
		}
		
		// VISS EU CD.
		try {
			logInfo.println("INFO: Import 'sharkweb_settings_viss_eu_cd.txt'");
			bufferedReader = ParseFileUtil.GetSharkConfigFile("sharkweb_settings_viss_eu_cd.txt");
			
			fileContent = ParseFileUtil.parseDataFile(bufferedReader, false);
			if (fileContent != null) {					
				importSettingsVissEuCd(fileContent);
			}
			
		} catch (Exception e) {
			logInfo.println("Failed to import 'sharkweb_settings_viss_eu_cd.txt'. Error: " + e.getMessage());
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Settings import");
			messageBox.setMessage("Failed to import 'sharkweb_settings_viss_eu_cd.txt'.");
			messageBox.open();
		}
		
		// Used by the Observer pattern.
		ModelFacade.instance().modelChanged();
	}
	
	private void importSettingsList(List<String[]> fileContent) {
		String[] header = null;
				
		int rowCounter = 1;
		int addedItems = 0;
		for (String[] row : fileContent) {
			if (header == null) { 
				// The first line contains the header.
				header = row;
				checkHeader(header);
			} else {
				rowCounter++;				
				SettingsManager.instance().addSetting(
						getCell(row, "KEY"), getCell(row, "VALUE"));
				addedItems++;
			}
		}
		logInfo.println("INFO: Added settings: " + addedItems + ".");
	}

	private void importSettingsHeadersList(List<String[]> fileContent) {
		String[] header = null;
		
		String header_list_sv = "";
		String header_list_en = "";
		String header_list_short = "";
//		String header_list_metadata = ""; // New: 
//		String header_list_darwin_core = ""; // New: 
		String text_key = "";
		String text_sv = "";
		String text_en = "";
		String text_short = "";
//		String text_metadata = "";
//		String text_darwin_core = "";
		String delimiter_sv = "";
		String delimiter_en = "";
		String delimiter_short = "";
//		String delimiter_metadata = "";
//		String delimiter_darwin_core = "";
		
		int rowCounter = 1;
		int addedItems = 0;
		for (String[] row : fileContent) {
			if (header == null) { 
				// The first line contains the header.
				header = row;
				checkHeader(header);
			} else {
				rowCounter++;
				
				text_key = getCell(row, "internal_key");
				text_sv = getCell(row, "swedish");
				text_en = getCell(row, "english");
				text_short = getCell(row, "short");
//				text_key = getCell(row, "internal_key");
//				text_sv = getCell(row, "swedish_name");
//				text_en = getCell(row, "english_name");
//				text_short = getCell(row, "short_name");
////				text_metadata = getCell(row, "Value metadata");
////				text_darwin_core = getCell(row, "Value darwin core");
				
				if (!text_key.equals("")) {
					if (!text_sv.equals("")) {
						header_list_sv += delimiter_sv + text_key + ":" + text_sv;
						delimiter_sv = ",";
					}
					if (!text_en.equals("")) {
						header_list_en += delimiter_en + text_key + ":" + text_en;
						delimiter_en = ",";
					}
					if (!text_short.equals("")) {
						header_list_short += delimiter_short + text_key + ":" + text_short;
						delimiter_short = ",";
					}
//					if (!text_metadata.equals("")) {
//						header_list_metadata += delimiter_metadata + text_key + ":" + text_metadata;
//						delimiter_metadata = ",";
//					}
//					if (!text_darwin_core.equals("")) {
//						header_list_darwin_core += delimiter_darwin_core + text_key + ":" + text_darwin_core;
//						delimiter_darwin_core = ",";
//					}
					addedItems++;
				}
			}
		}
		SettingsManager.instance().addSetting("db_headers_sv", header_list_sv);
		System.out.println("db_headers_sv: " + header_list_sv);
		
		SettingsManager.instance().addSetting("db_headers_en", header_list_en);
		System.out.println("header_list_en: " + header_list_en);
		SettingsManager.instance().addSetting("db_headers_short", header_list_short);
		System.out.println("header_list_short: " + header_list_short);
		
//		SettingsManager.instance().addSetting("db_headers_metadata", header_list_metadata);
//		SettingsManager.instance().addSetting("db_headers_darwin_core", header_list_darwin_core);

		logInfo.println("INFO: Added settings headers: " + addedItems + ".");
	}

	private void importSettingsColumnViews(List<String[]> fileContent) {
		String[] header = null;
		
//		dataset_content_header_data
//		sample_col_std_header_data
//		sample_col_all_header_data
//		sample_col_physicalchemical_header_data
//		sample_col_physicalchemical_columnparams_header_data
//		sample_col_bacterioplankton_header_data
//		sample_col_chlorophyll_header_data
//		sample_col_epibenthos_header_data
//		sample_col_epibenthos_dropvideo_header_data
//		sample_col_greyseal_header_data
//		sample_col_harbourseal_header_data
//		sample_col_phytoplankton_header_data
//		sample_col_picoplankton_header_data
//		sample_col_primaryproduction_header_data
//		sample_col_ringedseal_header_data
//		sample_col_sealpathology_header_data
//		sample_col_sedimentation_header_data
//		sample_col_zoobenthos_header_data
//		sample_col_zooplankton_header_data
//		sample_col_harbourporpoise_header_data
//		sample_col_profile_header_data
//		sample_col_planktonbarcoding_header_data
//		sample_col_jellyfish_header_data

		List<String> columnList_std = new ArrayList<String>();
		List<String> columnList_all = new ArrayList<String>();
		List<String> columnList_physicalchemical = new ArrayList<String>();
		List<String> columnList_physicalchemical_columnparams = new ArrayList<String>();
		List<String> columnList_bacterioplankton = new ArrayList<String>();
		List<String> columnList_chlorophyll = new ArrayList<String>();
		List<String> columnList_epibenthos = new ArrayList<String>();
		List<String> columnList_epibenthos_dropvideo = new ArrayList<String>();
		List<String> columnList_greyseal = new ArrayList<String>();
		List<String> columnList_harbourseal = new ArrayList<String>();
		List<String> columnList_phytoplankton = new ArrayList<String>();
		List<String> columnList_picoplankton = new ArrayList<String>();
		List<String> columnList_primaryproduction = new ArrayList<String>();
		List<String> columnList_ringedseal = new ArrayList<String>();
		List<String> columnList_sealpathology = new ArrayList<String>();
		List<String> columnList_sedimentation = new ArrayList<String>();
		List<String> columnList_zoobenthos = new ArrayList<String>();
		List<String> columnList_zooplankton = new ArrayList<String>();
		List<String> columnList_harbourporpoise = new ArrayList<String>();
		List<String> columnList_profile = new ArrayList<String>();
		List<String> columnList_planktonbarcoding = new ArrayList<String>();
		List<String> columnList_jellyfish = new ArrayList<String>();

		for (String[] row : fileContent) {

			if (header == null) { 
				// The first line contains the header.
				header = row;
				checkHeader(header);
			} else {
				if (!getCell(row, "sharkweb_overview").equals("")) { columnList_std.add(getCell(row, "sharkweb_overview")); };
				if (!getCell(row, "sharkweb_all").equals("")) { columnList_all.add(getCell(row, "sharkweb_all")); };
				if (!getCell(row, "sharkdata_physicalchemical").equals("")) { columnList_physicalchemical.add(getCell(row, "sharkdata_physicalchemical")); };
				if (!getCell(row, "sharkdata_physicalchemical_columns").equals("")) { columnList_physicalchemical_columnparams.add(getCell(row, "sharkdata_physicalchemical_columns")); };
				if (!getCell(row, "sharkdata_bacterioplankton").equals("")) { columnList_bacterioplankton.add(getCell(row, "sharkdata_bacterioplankton")); };
				if (!getCell(row, "sharkdata_chlorophyll").equals("")) { columnList_chlorophyll.add(getCell(row, "sharkdata_chlorophyll")); };
				if (!getCell(row, "sharkdata_epibenthos").equals("")) { columnList_epibenthos.add(getCell(row, "sharkdata_epibenthos")); };
				if (!getCell(row, "sharkdata_epibenthos_dropvideo").equals("")) { columnList_epibenthos_dropvideo.add(getCell(row, "sharkdata_epibenthos_dropvideo")); };
				if (!getCell(row, "sharkdata_greyseal").equals("")) { columnList_greyseal.add(getCell(row, "sharkdata_greyseal")); };
				if (!getCell(row, "sharkdata_harbourseal").equals("")) { columnList_harbourseal.add(getCell(row, "sharkdata_harbourseal")); };
				if (!getCell(row, "sharkdata_phytoplankton").equals("")) { columnList_phytoplankton.add(getCell(row, "sharkdata_phytoplankton")); };
				if (!getCell(row, "sharkdata_picoplankton").equals("")) { columnList_picoplankton.add(getCell(row, "sharkdata_picoplankton")); };
				if (!getCell(row, "sharkdata_primaryproduction").equals("")) { columnList_primaryproduction.add(getCell(row, "sharkdata_primaryproduction")); };
				if (!getCell(row, "sharkdata_ringedseal").equals("")) { columnList_ringedseal.add(getCell(row, "sharkdata_ringedseal")); };
				if (!getCell(row, "sharkdata_sealpathology").equals("")) { columnList_sealpathology.add(getCell(row, "sharkdata_sealpathology")); };
				if (!getCell(row, "sharkdata_sedimentation").equals("")) { columnList_sedimentation.add(getCell(row, "sharkdata_sedimentation")); };
				if (!getCell(row, "sharkdata_zoobenthos").equals("")) { columnList_zoobenthos.add(getCell(row, "sharkdata_zoobenthos")); };
				if (!getCell(row, "sharkdata_zooplankton").equals("")) { columnList_zooplankton.add(getCell(row, "sharkdata_zooplankton")); };					
				if (!getCell(row, "sharkdata_harbourporpoise").equals("")) { columnList_harbourporpoise.add(getCell(row, "sharkdata_harbourporpoise")); };
				if (!getCell(row, "sharkdata_profile").equals("")) { columnList_profile.add(getCell(row, "sharkdata_profile")); };
				if (!getCell(row, "sharkdata_planktonbarcoding").equals("")) { columnList_planktonbarcoding.add(getCell(row, "sharkdata_planktonbarcoding")); };
				if (!getCell(row, "sharkdata_jellyfish").equals("")) { columnList_jellyfish.add(getCell(row, "sharkdata_jellyfish")); };
			}
		}
		
		// Content.
		SettingsManager.instance().addSetting("dataset_content_header_data", StringUtils.join(", ", columnList_std)); // Use std (=Overview).
		// Other.
		SettingsManager.instance().addSetting("sample_col_std_header_data", StringUtils.join(", ", columnList_std));
		SettingsManager.instance().addSetting("sample_col_all_header_data", StringUtils.join(", ", columnList_all));
		SettingsManager.instance().addSetting("sample_col_physicalchemical_header_data", StringUtils.join(", ", columnList_physicalchemical));
		SettingsManager.instance().addSetting("sample_col_physicalchemical_columnparams_header_data", StringUtils.join(", ", columnList_physicalchemical_columnparams));
		SettingsManager.instance().addSetting("sample_col_bacterioplankton_header_data", StringUtils.join(", ", columnList_bacterioplankton));
		SettingsManager.instance().addSetting("sample_col_chlorophyll_header_data", StringUtils.join(", ", columnList_chlorophyll));
		SettingsManager.instance().addSetting("sample_col_epibenthos_header_data", StringUtils.join(", ", columnList_epibenthos));
		SettingsManager.instance().addSetting("sample_col_epibenthos_dropvideo_header_data", StringUtils.join(", ", columnList_epibenthos_dropvideo));
		SettingsManager.instance().addSetting("sample_col_greyseal_header_data", StringUtils.join(", ", columnList_greyseal));
		SettingsManager.instance().addSetting("sample_col_harbourseal_header_data", StringUtils.join(", ", columnList_harbourseal));
		SettingsManager.instance().addSetting("sample_col_phytoplankton_header_data", StringUtils.join(", ", columnList_phytoplankton));
		SettingsManager.instance().addSetting("sample_col_picoplankton_header_data", StringUtils.join(", ", columnList_picoplankton));
		SettingsManager.instance().addSetting("sample_col_primaryproduction_header_data", StringUtils.join(", ", columnList_primaryproduction));
		SettingsManager.instance().addSetting("sample_col_ringedseal_header_data", StringUtils.join(", ", columnList_ringedseal));
		SettingsManager.instance().addSetting("sample_col_sealpathology_header_data", StringUtils.join(", ", columnList_sealpathology));
		SettingsManager.instance().addSetting("sample_col_sedimentation_header_data", StringUtils.join(", ", columnList_sedimentation));
		SettingsManager.instance().addSetting("sample_col_zoobenthos_header_data", StringUtils.join(", ", columnList_zoobenthos));
		SettingsManager.instance().addSetting("sample_col_zooplankton_header_data", StringUtils.join(", ", columnList_zooplankton));
		SettingsManager.instance().addSetting("sample_col_harbourporpoise_header_data", StringUtils.join(", ", columnList_harbourporpoise));
		SettingsManager.instance().addSetting("sample_col_profile_header_data", StringUtils.join(", ", columnList_profile));
		SettingsManager.instance().addSetting("sample_col_planktonbarcoding_header_data", StringUtils.join(", ", columnList_planktonbarcoding));
		SettingsManager.instance().addSetting("sample_col_jellyfish_header_data", StringUtils.join(", ", columnList_jellyfish));
	}

	private void importSettingsVissEuCd(List<String[]> fileContent) {
		String[] header = null;
		
		String vissSettings = "{ ";
		String delimiter_rec = "";
		String delimiter_field = "";

		String viss_quality_factor = ""; // VISS kvalitetsfaktor.
		String viss_parameter = ""; // VISS parameter.
		String eu_cd = ""; // EU_CD.
		String year_from = ""; // År från.
		String year_to = ""; // År till.
		String datatype = ""; // Datatyp.
		String parameter = ""; // Parameter.
		String sample_project_name = ""; // Undersökning.
		String scientific_name = ""; // Art/taxon-namn.
		String sample_table_view = ""; // Visa som tabell.
		
		int rowCounter = 1;
		int addedItems = 0;
		delimiter_rec = "";
		for (String[] row : fileContent) {
			if (header == null) { 
				// The first line contains the header.
				header = row;
				checkHeader(header);
			} else {
				rowCounter++;
				
				viss_quality_factor = getCell(row, "VISS kvalitetsfaktor");
				viss_parameter = getCell(row, "VISS parameter");
				
				year_from = getCell(row, "År från");
				year_to = getCell(row, "År till");
				datatype = getCell(row, "Datatyp");
				parameter = getCell(row, "Parameter");
				sample_project_name = getCell(row, "Undersökning");
				scientific_name = getCell(row, "Art/taxon-namn");
				sample_table_view = getCell(row, "Visa som tabell");
				
				if (!viss_quality_factor.equals("")) {
					if (viss_parameter.equals("")) {
						viss_parameter = "NULL"; // Use 'NULL' if not specified. 
					}
					// Concatenate VISS quality factor and parameter as key. ':' as separator.
					vissSettings += delimiter_rec + "\"" + viss_quality_factor + "_" + viss_parameter + "\": { ";
					delimiter_rec = ", ";
					
					delimiter_field = "";
					if (!year_from.equals("")) {
						vissSettings += delimiter_field + "\"year_from\":\"" + year_from + "\"";
						delimiter_field = ", ";
					}
					if (!year_to.equals("")) {
						vissSettings += delimiter_field + "\"year_to\":\"" + year_to + "\"";
						delimiter_field = ", ";
					}
					if (!datatype.equals("")) {
						vissSettings += delimiter_field + "\"datatype\":\"" + datatype + "\"";
						delimiter_field = ", ";
					}
					if (!parameter.equals("")) {
						vissSettings += delimiter_field + "\"parameter\":\"" + parameter + "\"";
						delimiter_field = ", ";
					}
					if (!sample_project_name.equals("")) {
						vissSettings += delimiter_field + "\"project\":\"" + sample_project_name + "\"";
						delimiter_field = ", ";
					}
					if (!scientific_name.equals("")) {
						vissSettings += delimiter_field + "\"taxon\":\"" + scientific_name + "\"";
						delimiter_field = ", ";
					}
					if (!sample_table_view.equals("")) {
						vissSettings += delimiter_field + "\"sample_table_view\":\"" + sample_table_view + "\"";
						delimiter_field = ", ";
					}
					
					vissSettings += " }";					
					addedItems++;
				}
			}
		}
		vissSettings += " }";

		
		SettingsManager.instance().addSetting("viss_url_settings", vissSettings);
		System.out.println("viss_url_settings: " + vissSettings);
		
		logInfo.println("INFO: Added settings VISS EU CD: " + addedItems + ".");
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
