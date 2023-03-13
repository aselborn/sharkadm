/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import se.smhi.sharkadm.fileimport.misc.FileImportDyntaxaWhiteList;
import se.smhi.sharkadm.fileimport.misc.FileImportRedListSpecies;
import se.smhi.sharkadm.species_old.TaxonNode;
import se.smhi.sharkadm.species_old.TaxonObject;
import se.smhi.sharkadm.utils.SqlPreparedStatementRow;

public class SaveSpecies {

	private String sqlInsertSpecies = "insert into taxon ("
		+ "taxon_dyntaxa_id, " 
		+ "taxon_rank_id, " 
		+ "taxon_rank_name, " 
		+ "taxon_name, " 
		+ "taxon_author, "
		
		+ "taxon_kingdom, "
		+ "taxon_phylum, "
		+ "taxon_class, "
		+ "taxon_order, "
		+ "taxon_family, "
		+ "taxon_genus, "
		+ "taxon_species, "
		
		+ "taxon_hierarchy, "
		+ "taxon_red_list_category, " 
		+ "keyvalue_taxon " 

		+ ") values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";


	private FileImportRedListSpecies fileImportRedListSpecies = null;
	
//	private String sqlInsertSpeciesPeg = "insert into taxon_peg ("
//		+ "taxon_dyntaxa_id, " 
//		+ "size_class, " 
//		+ "species_flag_code, " 
//		+ "trophy, " 
//		+ "unit, " 
//		+ "nr_cells_per_unit, " 
//		+ "volume_um3, " 
//		+ "cell_length_in_filament_um, " 
//		+ "carbon_per_unit_pg, " 
//
//		+ ") values (?, ?, ?, ?, ?, ?, ?, ?, ?);";

	private static SaveSpecies instance = new SaveSpecies(); // Singleton.

	public static SaveSpecies instance() { // Singleton.
		return instance;
	}

	private SaveSpecies() { // Singleton.

	}

	public int countSpecies() {
		if (!DbConnect.instance().isConnected()) {
			return 0;
		}
		Statement stmt = null;
		ResultSet rs = null;
		int counter = 0;
		try {
			stmt = DbConnect.instance().getConnection().createStatement();
			rs = stmt.executeQuery("select count(*) from taxon; ");
			while (rs.next()) {
				counter = rs.getInt(1);
			}
			stmt.close();
			return counter;

		} catch (SQLException e) {
			HandleError(e);
			return 0;
		}
	}

	public void deleteSpecies() {
		if (!DbConnect.instance().isConnected()) {
			return;
		}
		Statement stmt = null;
		try {			
			stmt = DbConnect.instance().getConnection().createStatement();
			stmt.execute("delete from taxon; ");
			stmt.close();
		} catch (SQLException e) {
			HandleError(e);
		}		
	}


	public int insertSpeciesDummy() {

		try {			
			PreparedStatement stmtInsertSpecies = DbConnect.instance().getConnection().prepareStatement(sqlInsertSpecies);
			SqlPreparedStatementRow row = new SqlPreparedStatementRow(stmtInsertSpecies);
			row.addInt(0); // taxon_dyntaxa_id 
			row.addInt(0); // taxon_rank_id
			row.addCleanedString("-"); // taxon_rank_name
			row.addCleanedString("-"); // name 
			row.addCleanedString(""); // author
			row.addCleanedString(""); // taxon_kingdom
			row.addCleanedString(""); // taxon_phylum
			row.addCleanedString(""); // taxon_class
			row.addCleanedString(""); // taxon_order
			row.addCleanedString(""); // taxon_family
			row.addCleanedString(""); // taxon_genus
			row.addCleanedString(""); // taxon_species
			row.addCleanedString("-"); // taxon_hierarchy 
			row.addCleanedString(""); // taxon_red_list_category 
			row.addCleanedString(""); // keyvalue_taxon 

			stmtInsertSpecies.executeUpdate();
			return 0;

		} catch (SQLException e) {
			HandleError(e);
			return -1;
		}
	}

	public int insertSpecies(TaxonObject species) {
		String taxonKingdom = "";
		String taxonPhylum = "";
		String taxonClass = "";
		String taxonOrder = "";
		String taxonFamily = "";
		String taxonGenus = "";
		String taxonSpecies = "";
		String hierarchy = "";
		String redListCategory = "";
		String keyvalueTaxon = "";

		try {			
			PreparedStatement stmtInsertSpecies = DbConnect.instance().getConnection().prepareStatement(sqlInsertSpecies);
			SqlPreparedStatementRow row = new SqlPreparedStatementRow(stmtInsertSpecies);
			row.addInt(species.getDyntaxaId() ); // taxon_dyntaxa_id 
//			row.addInt(species.getTaxonTypeId() ); // taxon_rank_id
			row.addInt(0); // DUMMY. Rank-id not used.
			row.addCleanedString(species.getTaxonRank()); // taxon_rank_name TODO: Add value.
			if (species.getValidNameObject() != null) {
				
				String validName = species.getValidNameObject().getName();
				// Replace characters DB don't like.
				if (validName.indexOf("'") >= 0) {
					validName = validName.replace("'", "");
				} 
				if (validName.indexOf("�") >= 0) {
					validName = validName.replace("�", "");
				}
				if (validName.indexOf("�") >= 0) {
					validName = validName.replace("�", "x");
				}
				row.addCleanedString(validName); // name 
				row.addCleanedString(species.getValidNameObject().getAuthor() ); // author
			} else {
				row.addCleanedString("NO VALID NAME" ); // name 
				row.addCleanedString("NO VALID AUTHOR" ); // author
			}
			
			String taxonName = "";
			if (species.getValidNameObject() != null) {
				hierarchy = species.getValidNameObject().getName();
				taxonName = species.getValidNameObject().getName();
			} else {
				hierarchy = "";
			}
			
			String validName = "NO VALID NAME";
			if (species.getValidNameObject() != null) {
				validName = species.getValidNameObject().getName();
				// Replace characters DB don't like.
				if (validName.indexOf("'") >= 0) {
					validName = validName.replace("'", "");
				} 
				if (validName.indexOf("�") >= 0) {
					validName = validName.replace("�", "");
				}
				if (validName.indexOf("�") >= 0) {
					validName = validName.replace("�", "x");
				}
			}
			
			// Don't save if rank is missing.
			if (species.getTaxonRank().equals("")) {
				System.out.println("DEBUG: Rank missing: " + validName);
				return 0;
			} 
			
			if (species.getTaxonRank().equals("kingdom")) {
				taxonKingdom = validName;
			}
			if (species.getTaxonRank().equals("phylum")) {
				taxonPhylum = validName;
			}
			if (species.getTaxonRank().equals("class")) {
				taxonClass = validName;
			}
			if (species.getTaxonRank().equals("order")) {
				taxonOrder = validName;
			}
			if (species.getTaxonRank().equals("family")) {
				taxonFamily = validName;
			}
			if (species.getTaxonRank().equals("genus")) {
				taxonGenus = validName;
			}
			if (species.getTaxonRank().equals("species")) {
				taxonSpecies = validName;
			}
				
			TaxonNode parentNode = species.getTaxonNode().getParent();
			while (parentNode != null) {
				try {
					TaxonObject parentObject = parentNode.getTaxonObject();
					if (parentObject != null) {
						
						validName = "NO VALID NAME";
						if (parentObject.getValidNameObject() != null) {
							validName = parentObject.getValidNameObject().getName();
							// Replace characters DB don't like.
							if (validName.indexOf("'") >= 0) {
								validName = validName.replace("'", "");
							} 
							if (validName.indexOf("�") >= 0) {
								validName = validName.replace("�", "");
							}
							if (validName.indexOf("�") >= 0) {
								validName = validName.replace("�", "x");
							}
						}
						
						if (parentObject.getTaxonRank().equals("kingdom")) {
							taxonKingdom = validName;
						}
						if (parentObject.getTaxonRank().equals("phylum")) {
							taxonPhylum = validName;
						}
						if (parentObject.getTaxonRank().equals("class")) {
							taxonClass = validName;
						}
						if (parentObject.getTaxonRank().equals("order")) {
							taxonOrder = validName;
						}
						if (parentObject.getTaxonRank().equals("family")) {
							taxonFamily = validName;
						}
						if (parentObject.getTaxonRank().equals("genus")) {
							taxonGenus = validName;
						}
						if (parentObject.getTaxonRank().equals("species")) {
							taxonSpecies = validName;
						}
						hierarchy = validName + " - " + hierarchy;
					}
				} catch (Exception e) {
					hierarchy = "NO VALID NAME" + " - " + hierarchy;
				}
				parentNode = parentNode.getParent();
			}
			
			
			
//			// TEST Blacklists.
//			List<String> blacklistKingdom = Arrays.asList(
//					"Viruses");
//			List<String> blacklistClass = Arrays.asList(
//					"Aves", 
//					"Reptilia");
//			List<String> blacklistFamily = Arrays.asList(
//					"Campanulaceae");
//			List<String> blacklistGenus = Arrays.asList(
//					"Homo");
//			
//			if (blacklistKingdom.contains(taxonKingdom)) {
//				return 0;
//			}
//			if (blacklistClass.contains(taxonClass)) {
//				return 0;
//			}
//			if (blacklistFamily.contains(taxonFamily)) {
//				return 0;
//			}
//			if (blacklistGenus.contains(taxonGenus)) {
//				return 0;
//			}
			
			
			
			// Red list species.
			if (fileImportRedListSpecies == null) {
				// Load file at first use.
				fileImportRedListSpecies = new FileImportRedListSpecies();
			}
			redListCategory = fileImportRedListSpecies.getRedListCategory(new Integer(species.getDyntaxaId()).toString());
			
			row.addCleanedString(taxonKingdom);
			row.addCleanedString(taxonPhylum);
			row.addCleanedString(taxonClass);
			row.addCleanedString(taxonOrder);
			row.addCleanedString(taxonFamily);
			row.addCleanedString(taxonGenus);
			row.addCleanedString(taxonSpecies);
			row.addCleanedString(hierarchy);			
			row.addCleanedString(redListCategory);
//			row.addCleanedString(taxonKingdom); // TODO: Test instead of redListCategory.
			
			// Create key/value-list for locations.
			String keyValues = "";
			keyValues = "taxon_kingdom:" + taxonKingdom + "\t";
			keyValues += "taxon_phylum:" + taxonPhylum + "\t";
			keyValues += "taxon_class:" + taxonClass + "\t";
			keyValues += "taxon_order:" + taxonOrder + "\t";
			keyValues += "taxon_family:" + taxonFamily + "\t";
			keyValues += "taxon_genus:" + taxonGenus + "\t";
			keyValues += "taxon_species:" + taxonSpecies + "\t";
			keyValues += "taxon_hierarchy:" + hierarchy + "\t";

			keyValues += "taxon_red_list_category:" + redListCategory + "\t";
//			keyValues += "taxon_red_list_category:" + taxonKingdom + "\t"; // TODO: Test instead of redListCategory.
			// 
			row.addCleanedString(keyValues); // keyvalue_taxon.

			
			
//			// Only store to db if taxa in whitelist or higher taxa from whitelist taxa.
			if (FileImportDyntaxaWhiteList.instance().isWhiteListParent(taxonName)) {
				stmtInsertSpecies.executeUpdate();
				return 0;
			} 
			// Rank = genus.
			String rank = FileImportDyntaxaWhiteList.instance().getWhitelistRank(taxonGenus);
			if (rank.equals("genus")) {
				stmtInsertSpecies.executeUpdate();
				return 0;
			}				
			// Rank = family.
			rank = FileImportDyntaxaWhiteList.instance().getWhitelistRank(taxonFamily);
			if (rank.equals("family")) {
				stmtInsertSpecies.executeUpdate();
				return 0;
			}				
			// Rank = order.
			rank = FileImportDyntaxaWhiteList.instance().getWhitelistRank(taxonOrder);
			if (rank.equals("order")) {
				stmtInsertSpecies.executeUpdate();
				return 0;
			}				
			// Rank = class.
			rank = FileImportDyntaxaWhiteList.instance().getWhitelistRank(taxonClass);
			if (rank.equals("class")) {
				stmtInsertSpecies.executeUpdate();
				return 0;
			}				

//			if (FileImportDyntaxaWhiteList.instance().isWhiteListParent(taxonName)) {
//				stmtInsertSpecies.executeUpdate();
//			} else {
//				String rank = FileImportDyntaxaWhiteList.instance().getWhitelistRank(taxonGenus);
//				if (!rank.equals("")) {
//					stmtInsertSpecies.executeUpdate();
//				}				
//			}
			return 0;

		} catch (SQLException e) {
			HandleError(e);
			return -1;
		}
	}

//	public int insertSpeciesPeg(PEGObject peg) {
//
//		try {			
//			PreparedStatement stmtInsertSpeciesPeg = DbConnect.instance().getConnection().prepareStatement(sqlInsertSpeciesPeg);
//			SqlPreparedStatementRow row = new SqlPreparedStatementRow(stmtInsertSpeciesPeg);
//			row.addCleanedString(peg.getDyntaxaId()); // taxon_dyntaxa_id
//			row.addCleanedString(peg.getSizeClassNo()); // size_class
//			row.addCleanedString(peg.getSpecies_flag_code()); // species_flag_code 
//			row.addCleanedString(peg.getTrophy()); // trophy
//			row.addCleanedString(peg.getUnit()); // unit
//			row.addCleanedString(peg.getNoOfCellsPerCountingUnit()); // nr_cells_per_unit
//			row.addCleanedString("TODO: volume_um3"); // volume_um3
//			row.addCleanedString(peg.getFilament()); // cell_length_in_filament_um
//			row.addCleanedString("TODO: carbon_per_unit_pg"); // carbon_per_unit_pg
//			
//			stmtInsertSpeciesPeg.executeUpdate();
//			return 0;
//
//		} catch (SQLException e) {
//			HandleError(e);
//			return -1;
//		}
//	}

	private void HandleError(SQLException e) {
		// Note: It is not recommended to put message dialog here. Should be in the UI layer.
		MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
		messageBox.setText("SQL error in SaveSpecies");
		messageBox.setMessage("Error: " + e.getMessage());
		messageBox.open();
		e.printStackTrace();
		System.exit(-1);
	}
	
}
