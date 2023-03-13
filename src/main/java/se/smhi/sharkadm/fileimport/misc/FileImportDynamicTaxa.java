/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.fileimport.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import se.smhi.sharkadm.species_old.TaxonManager;
import se.smhi.sharkadm.species_old.TaxonNameObject;
import se.smhi.sharkadm.species_old.TaxonNode;
import se.smhi.sharkadm.species_old.TaxonObject;
import se.smhi.sharkadm.utils.ErrorLogger;
import se.smhi.sharkadm.utils.ParseFileUtil;

public class FileImportDynamicTaxa extends SingleFileImport {
	
	private static TaxonManager taxonManager = TaxonManager.instance();

	String currentDateString = "";
	
	public FileImportDynamicTaxa(PrintStream logInfo) {
		super(logInfo);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		currentDateString = dateFormat.format(new Date());
	}	
	
	public void importFiles(String zipFileName, Dataset dataset) {
		
	}
	
	public void importFiles(String zipFileName) {
		
		List<String[]> fileContent;		
		ClassLoader classLoader = this.getClass().getClassLoader();
		InputStream inputStream;
		BufferedReader bufferedReader;
		File external_file = null;
		
		try {
//			bufferedReader = ParseFileUtil.GetSharkConfigFile("Taxon" + ".csv");
			bufferedReader = ParseFileUtil.GetSharkConfigFileCharset("dyntaxa_dwca\\Taxon" + ".csv", "UTF-8");
			fileContent = ParseFileUtil.parseDataFile(bufferedReader, false);
			if (fileContent != null) {
				importDyntaxaDwca(fileContent);
			}
			
			// Import translation list to DynTaxa names.
			bufferedReader = ParseFileUtil.GetSharkConfigFile("translate_to_dyntaxa.txt");
			fileContent = ParseFileUtil.parseDataFile(bufferedReader, false);
			if (fileContent != null) {
				importTranslateToDynTaxaList(fileContent);
			}
			
			// Check all taxa. If parent chain ends up in null, then add it to the top node list.
			logInfo.println("");
			for (String key : taxonManager.getImportIdLookup()
					.keySet()) {
				TaxonNode node = taxonManager.getTaxonNodeFromImportId(key);
				TaxonNode parent = node.getParent();
				while ((parent != null)
						&& (parent.getTaxonObject().getDyntaxaId() != 0)) {
					node = parent;
					parent = node.getParent();
				}
				if ((parent == null) && 
					(node.getTaxonObject().isActive())) {
					if (!taxonManager.getTopNodeList().contains(node)) {
						if (node.getTaxonObject().getDyntaxaId() != 0) { // Don't add another top node with TaxonID = 0.
							taxonManager.addTopNode(node);
							try {
								ErrorLogger.println("ERROR: No parent. Added as top node: " + node.getTaxonObject().getDyntaxaId() + " " +
										node.getTaxonObject().getValidNameObject().getName());
								System.out.println("ERROR: No parent. Added as top node: " + node.getTaxonObject().getDyntaxaId() + " " +
										node.getTaxonObject().getValidNameObject().getName());
							} catch (Exception e) {
								ErrorLogger.println("ERROR: No parent. Added as top node: " + node.getTaxonObject().getDyntaxaId() +
										" <Name not available.> ");
								System.out.println("ERROR: No parent. Added as top node: " + node.getTaxonObject().getDyntaxaId() +
										" <Name not available.> ");
							}
						}
					}
				}
			}
			
//			// List inactive taxa.
//			for (TaxonNode node : taxonManager.getImportIdLookup().values()) {
//				if ( ! node.getTaxonObject().isActive()) {
//					ErrorLogger.println("Taxon not marked as active. Id:" + 
//							node.getTaxonObject().getDyntaxaId() + " Name:" + 
//							node.getTaxonObject().getValidNameObject().getName() + " Valid to: " + 
//							node.getTaxonObject().getValidToDate());
//				}
//				
//			}
//			// End of List inactive taxa.
			
			// Sort top node list and children lists on valid names.
			taxonManager.sortTaxonLists();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		// Used by the Observer pattern.
		ModelFacade.instance().modelChanged();
	}

	
	private void importDyntaxaDwca(List<String[]> fileContent) {
	String[] header = null;
			
	logInfo.println("");
	logInfo.println("Dyntaxa DwC-A file: " + fileContent.size() + " rows");

	int rowCounter = 1;
	int addedItems = 0;
	for (String[] row : fileContent) {
		if (header == null) { 
			// The first line contains the header.
			header = row;
			
//			// TODO: Fix strange characters "ï»¿taxonId". 
			header[0] = "taxonId";
			
			checkHeader(header);
		} else {
			rowCounter++;
				
			// Available columns:
			// - taxonId
			// - acceptedNameUsageID
			// - parentNameUsageID
			// - scientificName
			// - taxonRank
			// - scientificNameAuthorship
			// - taxonomicStatus
			// - nomenclaturalStatus
			// - taxonRemarks
			// - kingdom
			// - phylum
			// - class
			// - order
			// - family
			// - genus
			// - species

			String taxonId = getCell(row, "taxonId");
			// TODO: Fix strange characters "ï»¿taxonId". 
			if (taxonId.equals("")) {
				taxonId = row[0];
			}
			
			// skip empty rows.
			if (taxonId.equals("")) {
				System.out.println("DEBUG DynTaxa: Empty row.");
				continue;
			}
			
			String acceptedNameUsageID = getCell(row, "acceptedNameUsageID");
			String parentNameUsageID = getCell(row, "parentNameUsageID");
			String scientificName = getCell(row, "scientificName");
			String taxonRank = getCell(row, "taxonRank");
			String scientificNameAuthorship = getCell(row, "scientificNameAuthorship");
			String taxonomicStatus = getCell(row, "taxonomicStatus");
			String nomenclaturalStatus = getCell(row, "nomenclaturalStatus");
			String taxonRemarks = getCell(row, "taxonRemarks");
			
			// Check if taxon or taxonname. Convert full ID to taxon-id.
			Boolean isTaxon = true;
			if (taxonId.contains("urn:lsid:dyntaxa.se:TaxonName:")) {
				isTaxon = false;
				taxonId = taxonId.replace("urn:lsid:dyntaxa.se:TaxonName:", "");
				acceptedNameUsageID = acceptedNameUsageID.replace("urn:lsid:dyntaxa.se:Taxon:", "");
				parentNameUsageID = parentNameUsageID.replace("urn:lsid:dyntaxa.se:Taxon:", "");
			} else if (taxonId.contains("urn:lsid:dyntaxa.se:Taxon:")) {
				isTaxon = true;
				taxonId = taxonId.replace("urn:lsid:dyntaxa.se:Taxon:", "");
				acceptedNameUsageID = acceptedNameUsageID.replace("urn:lsid:dyntaxa.se:Taxon:", "");
				parentNameUsageID = parentNameUsageID.replace("urn:lsid:dyntaxa.se:Taxon:", "");
			} else {
				System.out.println("DEBUG DynTaxa: Not Taxon, not TaxonName.");
				continue;
			}
			
			// Handle taxon.
			if (isTaxon == true) {
				// Add taxon node/object if missing.
				if ( ! taxonManager.getImportIdLookup().containsKey(acceptedNameUsageID)) {
					TaxonNode node = new TaxonNode();
					TaxonObject object = new TaxonObject();
					node.setTaxonObject(object);
					object.setDyntaxaId(Integer.parseInt(acceptedNameUsageID));
					taxonManager.addImportIdLookup(acceptedNameUsageID, node);
				}
				// Add parent taxon node/object if missing.
				if ( ! taxonManager.getImportIdLookup().containsKey(parentNameUsageID)) {
					if (!parentNameUsageID.equals("")) {
						TaxonNode node = new TaxonNode();
						TaxonObject object = new TaxonObject();
						node.setTaxonObject(object);
						object.setDyntaxaId(Integer.parseInt(parentNameUsageID));
						taxonManager.addImportIdLookup(parentNameUsageID, node);
					}
				}
				
				// Add more to taxon.
				TaxonNode node = taxonManager.getTaxonNodeFromImportId(acceptedNameUsageID);	
				if (node != null) {					
					TaxonObject object = node.getTaxonObject();
					//
//					object.setTaxonTypeId(translateTaxonRankToId(taxonRank));
					object.setTaxonRank(taxonRank);
					//
					object.setAuthor(scientificNameAuthorship);
					object.setRecommendedGUID(getCell(row, "acceptedNameUsageID")); // Full path.
					if (taxonomicStatus.equals("accepted")){
						object.setActive(true);
					}
					// Add name.
					TaxonNameObject nameObject = new TaxonNameObject(); 
//					nameObject.setTaxonNametype(Integer.parseInt(getCell(row, "NameCategoryId")));
					nameObject.setName(scientificName);
					nameObject.setAuthor(scientificNameAuthorship);
					nameObject.setValidName(true);
					if ((object != null) && (nameObject != null)) {
						object.addTaxonNames(nameObject);
					}
					// Name lookup.
					taxonManager.addImportNameLookup(scientificName, acceptedNameUsageID);
					// Connect to parent taxon.
					TaxonNode parentNode = taxonManager.getTaxonNodeFromImportId(parentNameUsageID);	
					if (parentNode != null) {					
						parentNode.addChild(node);
					} else {
						taxonManager.addTopNode(node);
					}
				}
			}
			// Handle taxon names as synonyms.
			if (isTaxon == false) {
				TaxonNode node = taxonManager.getTaxonNodeFromImportId(acceptedNameUsageID);	
				if (node != null) {					
					TaxonObject object = node.getTaxonObject();
					// Name lookup.
///////////					taxonManager.addImportNameLookup(scientificName, acceptedNameUsageID);
//					
//					
//					dyntaxaId = TaxonManager.instance().getTaxonIdFromName(taxonName);
					TaxonNode taxonNode = TaxonManager.instance().getTaxonNodeFromImportId(acceptedNameUsageID);
					String usedTaxonName = taxonNode.getTaxonObject().getValidNameObject().getName();
					
//					System.out.println("DEBUG: Add synonym: " + "\t" + scientificName + "\t" + usedTaxonName + "\t" + acceptedNameUsageID);
					logInfo.println("DEBUG: Add synonym: " + "\t" + scientificName + "\t" + usedTaxonName + "\t" + acceptedNameUsageID);
					System.out.println("DEBUG: Add synonym: " + scientificName + " --> " + usedTaxonName + " (" + acceptedNameUsageID + ")");
//					logInfo.println("DEBUG: Add synonym: " + scientificName + " --> " + usedTaxonName + " (" + acceptedNameUsageID + ")");
//
//					
				}
				
			}
		}
		addedItems++;
	}
	logInfo.println("INFO: Added items: " + addedItems + ".");
}
	
	
	private int translateTaxonRankToId(String taxonRank) {
		
//	taxonRank
//		if (taxonRank.equals("class")) { return 5; } // Klass.
////		else if (taxonRank.equals("cultivar")) { return 0; }
//		else if (taxonRank.equals("division")) { return 30; } // Avdelning.
//		else if (taxonRank.equals("family")) { return 11; } // Familj.
//		else if (taxonRank.equals("form")) { return 20; } // Form.
////		else if (taxonRank.equals("forma specialis")) { return 0; }
//		else if (taxonRank.equals("genus")) { return 14; } // Släkte.
//		else if (taxonRank.equals("infraclass")) { return 25; } // Infraklass.
//		else if (taxonRank.equals("infrakingdom")) { return 37; } // Infrarike.
//		else if (taxonRank.equals("infraorder")) { return 29; } // Infraordning.
//		else if (taxonRank.equals("infraphylum")) { return 39; } // Infrastam.
//		else if (taxonRank.equals("kingdom")) { return 1; } // Rike.
//		else if (taxonRank.equals("order")) { return 8; } // Ordning.
//		else if (taxonRank.equals("parvclass")) { return 26; } // Parvklass.
//		else if (taxonRank.equals("phylum")) { return 2; } // Stam.
//		else if (taxonRank.equals("section")) { return 16; } // Sektion.
//		else if (taxonRank.equals("species")) { return 17; } // Art.
//		else if (taxonRank.equals("speciesAggregate")) { return 27; } // Kollektivtaxon.
//		else if (taxonRank.equals("subclass")) { return 6; } // Underklass.
//		else if (taxonRank.equals("subdivision")) { return 31; } // Underavdelning.
//		else if (taxonRank.equals("subfamily")) { return 12; } // Underfamilj.
//		else if (taxonRank.equals("subgenus")) { return 15; } // Undersläkte.
//		else if (taxonRank.equals("subkingdom")) { return 35; } // Underrike.
//		else if (taxonRank.equals("suborder")) { return 0; } // Underordning.
//		else if (taxonRank.equals("subphylum")) { return 3; } //Understam.
//		else if (taxonRank.equals("subspecies")) { return 18; } // Underart.
//		else if (taxonRank.equals("subtribe")) { return 44; } // Undertribus.
//		else if (taxonRank.equals("superclass")) { return 4; } // Överklass.
//		else if (taxonRank.equals("superfamily")) { return 10; } // Överfamilj.
//		else if (taxonRank.equals("superorder")) { return 7; } // Överordning.
//		else if (taxonRank.equals("superphylum")) { return 38; } // Överstam.
//		else if (taxonRank.equals("tribe")) { return 13; } // Tribus.
//		else if (taxonRank.equals("unranked")) { return 52; } // Ranglös.
//		else if (taxonRank.equals("variety")) { return 19; }
//		//
//		
//		return 0;

		if (taxonRank.equals("")) { return 0; }
		else if (taxonRank.equals("domain")) { return 1; }
		else if (taxonRank.equals("superkingdom")) { return 2; }
		else if (taxonRank.equals("kingdom")) { return 3; }
		else if (taxonRank.equals("subkingdom")) { return 4; }
		else if (taxonRank.equals("infrakingdom")) { return 5; }
		
		else if (taxonRank.equals("superphylum")) { return 6; }
		else if (taxonRank.equals("phylum")) { return 7; }
		else if (taxonRank.equals("subphylum")) { return 8; }
		else if (taxonRank.equals("infraphylum")) { return 9; }
		
		else if (taxonRank.equals("superdivision")) { return 10; }
		else if (taxonRank.equals("division")) { return 11; }
		else if (taxonRank.equals("subdivision")) { return 12; }

		else if (taxonRank.equals("superclass")) { return 13; }
		else if (taxonRank.equals("class")) { return 14; }
		else if (taxonRank.equals("subclass")) { return 15; }
		else if (taxonRank.equals("infraclass")) { return 16; }
		else if (taxonRank.equals("parvclass")) { return 17; }
		
		else if (taxonRank.equals("superorder")) { return 18; }
		else if (taxonRank.equals("order")) { return 19; }
		else if (taxonRank.equals("suborder")) { return 20; }
		else if (taxonRank.equals("infraorder")) { return 21; }
		else if (taxonRank.equals("parvorder")) { return 22; }
		
		else if (taxonRank.equals("superfamily")) { return 23; }
		else if (taxonRank.equals("family")) { return 24; }
		else if (taxonRank.equals("subfamily")) { return 25; }
		else if (taxonRank.equals("infrafamily")) { return 26; }
		
		else if (taxonRank.equals("supertribe")) { return 27; }
		else if (taxonRank.equals("tribe")) { return 28; }
		else if (taxonRank.equals("subtribe")) { return 29; }
		else if (taxonRank.equals("infratribe")) { return 30; }

		else if (taxonRank.equals("genus")) { return 31; }
		else if (taxonRank.equals("subgenus")) { return 32; }
		
		else if (taxonRank.equals("superspecies")) { return 33; }
		else if (taxonRank.equals("species")) { return 34; }
		else if (taxonRank.equals("subspecies")) { return 35; }
		else if (taxonRank.equals("variety")) { return 36; }
		else if (taxonRank.equals("form")) { return 37; }
		
		else if (taxonRank.equals("section")) { return 38; } // Zoo: Above superfamily, Bot: Below subgenus.
		else if (taxonRank.equals("unranked")) { return 39; }
		else if (taxonRank.equals("cultivar")) { return 40; }
		else if (taxonRank.equals("speciesAggregate")) { return 41; } // S.lat.
		else if (taxonRank.equals("forma specialis")) { return 42; }

		return 0;
		
//	cultivar
//	division
//	family
//	form
//	forma specialis
//	genus
//	infraclass
//	infrakingdom
//	infraorder
//	infraphylum
//	kingdom
//	order
//	parvclass
//	phylum
//	section
//	species
//	speciesAggregate
//	subclass
//	subdivision
//	subfamily
//	subgenus
//	subkingdom
//	suborder
//	subphylum
//	subspecies
//	subtribe
//	superclass
//	superfamily
//	superorder
//	superphylum
//	tribe
//	unranked
//	variety

	}
	
	private void importTranslateToDynTaxaList(List<String[]> fileContent) {
		
		// Create dummy top node for PEG species not in DynTaxa. 
		TaxonNode topNode = new TaxonNode();
		TaxonObject topNodeObject = new TaxonObject();
		topNode.setTaxonObject(topNodeObject);
		taxonManager.addTopNode(topNode);
		topNodeObject.setDyntaxaId(Integer.parseInt("99990000"));
		TaxonNameObject topNodeNameObject = new TaxonNameObject(); 
		topNodeNameObject.setName("PEG Species");
		topNodeNameObject.setValidName(true);
		topNodeObject.addTaxonNames(topNodeNameObject);
		
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
				
				String fromName = getCell(row, "taxon_name_from");
				String dynTaxaName = getCell(row, "taxon_name_to");
				String pegTaxonId = getCell(row, "taxon_id (if not in DynTaxa)");
				
				if (!fromName.equals("")) {
					String taxonId = taxonManager.getTaxonIdFromName(dynTaxaName);
					if (!taxonId.equals("")) {
						taxonManager.addImportNameLookup(fromName, taxonId);						
					} else {
						if (!pegTaxonId.equals("")) {
							// Create dummy node for PEG species not in DynTaxa.
							System.out.println("DEBUG: Added node for PEG: " + fromName + "   " + pegTaxonId);
							TaxonNode node = new TaxonNode();
							TaxonObject object = new TaxonObject();
							node.setTaxonObject(object);
							topNode.addChild(node);
							object.setDyntaxaId(Integer.parseInt(pegTaxonId));
							TaxonNameObject nameObject = new TaxonNameObject(); 
							nameObject.setName(fromName);
							nameObject.setValidName(true);
							object.addTaxonNames(nameObject);
							taxonManager.addImportIdLookup(pegTaxonId, node);
							taxonManager.addImportNameLookup(fromName, pegTaxonId);						
						} else {
							System.out.println("DEBUG: translate_to_dyntaxa.txt. Can't find: " + fromName + " --> " + dynTaxaName + " " + pegTaxonId);
						}
					}
				}
				addedItems++;
			}
		}
//		logInfo.add("INFO: Added items: " + addedItems + ".");
	}
	
	
	@Override
	public void visitDataset(Dataset dataset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitSample(Sample sample) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitVariable(Variable variable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitVisit(Visit visit) {
		// TODO Auto-generated method stub
		
	}
}
