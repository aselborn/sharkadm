/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute.
 */


package se.smhi.sharkadm.datasets.formats;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeFilter;

import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.SAXException;
import se.smhi.sharkadm.datasets.fileimport.FileImportInfo;
import se.smhi.sharkadm.facades.ModelFacade;
import se.smhi.sharkadm.fileimport.misc.FileImportTranslateAllColumns;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Import format for Martrans. Rules: - Columns used in the import-matrix:
 * MARTRANS. - Import files are in XML format. - The XML elements should be
 * organized as follows:
 *
 * <MarTransExport> <...> Dataset fields <undersokning> <...> Dataset fields
 * <lokal> <...> <tillfalle> Create visit + "lokal" fields. <...> <hydrografi>
 * Create sample + "tillfalle" fields. <...> Sample fields <transekt> <...>
 * <avsnitt> Create sample + "tillfalle" and "transekt" fields. <...> Sample
 * fields <avsnittart> Create variable <...> Variable fields <prov> Create
 * sample <...> Sample fields <provart> Create variable <...> Variable fields
 * <transekttaxaminmax> Create sample and variable + "tillfalle" and "transekt"
 * fields. <...> Variable fields
 * <p>
 * BACKLOG: Fix database and remove keyTranslate.put("lokal.havsomrade_namn",
 * "NOT_USED");.
 * <p>
 * <p>
 * BACKLOG: sample.sample_misc.subst_cover_stone sample.section.sect_cover_stone
 * <p>
 * - <avsnitt> - <avsnittart> <taxonID>233403</taxonID> <taxon_namn>Balanus
 * improvisus</taxon_namn> <epibiotisk>0</epibiotisk>
 * <tackningsgrad>1</tackningsgrad> <ejfastsittande>0</ejfastsittande>
 * <foto>0</foto> <belagg>0</belagg> </avsnittart> - <avsnittsubstrat>
 * <substratkod>3</substratkod> <substrat_substrat>Sten</substrat_substrat>
 * <tackningsgrad>50</tackningsgrad> </avsnittsubstrat> </avsnitt>
 */

public class FormatXmlMartrans extends FormatXmlBase {

    Boolean isTransektTaxaMinMaxCreated = false;

    public FormatXmlMartrans(PrintStream logInfo, FileImportInfo importInfo) {
        super(logInfo, importInfo);
        FileImportTranslateAllColumns.instance().loadFile();
    }

    public void importFiles(String zipFileName, Dataset dataset) {
        this.dataset = dataset;

        String importMatrixColumn = "";
        if (dataset.getImport_format().contains(":")) {
            String[] strings = dataset.getImport_format().split(Pattern.quote(":"));
            importMatrixColumn = strings[1];
        } else {
            importInfo.addConcatError("Error in format description in 'delivery_note.txt'. Import aborted. ");
            return;
        }

        loadKeyTranslator(importMatrixColumn, "import_matrix_epibenthos.txt");
        // loadKeyTranslator(importMatrixColumn, "import_matrix.txt");
        dataset.setImport_matrix_column(importMatrixColumn);

//		// TODO: TEST TEST TEST
//
//		keyTranslate.put("provsubstrat.substrat_substrat", "TEMP.substrate");
//		keyTranslate.put("provsubstrat.substratkod", "TEMP.substrate_code");
//		keyTranslate.put("provsubstrat.tackningsgrad", "TEMP.substrate_cover");
//		keyTranslate.put("provsubstrat.kommentar", "TEMP.substrate_comment");
//
//		keyTranslate.put("avsnittsubstrat.substrat_substrat", "TEMP.sect_substrate");
//		keyTranslate.put("avsnittsubstrat.substratkod", "TEMP.sect_substrate_code");
//		keyTranslate.put("avsnittsubstrat.tackningsgrad", "TEMP.sect_substrate_cover");
//		keyTranslate.put("avsnittsubstrat.kommentar", "TEMP.sect_substrate_comment");

        if (getTranslateKeySize() == 0) {
            importInfo
                    .addConcatError("Empty column in import matrix. Import aborted.");
            return;
        }

//		dataset.setImport_status("TEST");
//		importInfo.addConcatWarning("TEST IMPORT. Import format under development.");
        dataset.setImport_status("DATA");

        // Imports the data file.
        Path filePath = null;
        InputStream stream = null;

        try {
            if (Files.exists(Paths.get(zipFileName, "processed_data", "data.xml"))) {
                filePath = Paths.get(zipFileName, "processed_data", "data.xml");

                importData(filePath);

                //stream = new FileInputStream(filePath.toFile());
                //importData(stream);
                //stream.close();
            }
        } catch (Exception e) {
            importInfo.addConcatError("FAILED TO IMPORT FILE.");
        }
        // Used by the Observer pattern.
        ModelFacade.instance().modelChanged();
    }

    private void importData(Path xmlFilePath) {

        try {

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document theDocument = builder.parse(xmlFilePath.toFile());

            DocumentTraversal traversal = (DocumentTraversal) theDocument;

            // DOM tree walker
            TreeWalker walker = traversal.createTreeWalker(
                    theDocument.getDocumentElement(),
                    NodeFilter.SHOW_ELEMENT,
                    null,
                    true);

            traverseXmlElements(walker, 0);

        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }

    }

    private void traverseXmlElements(TreeWalker walker, int level) {

        level++;

        Node node = walker.getCurrentNode();

        createMemoryModel(node);

        for (Node n = walker.firstChild();
             n != null;
             n = walker.nextSibling()) {
            traverseXmlElements(walker, level);
        }

        walker.setCurrentNode(node);

        // how depth is it?
        //if (level > DEPTH_XML) {
        //	DEPTH_XML = level;
        //}

    }
	/*private void importData(InputStream inputStream) {
		try {
			// Parse the document. Use DOM.
			DOMParser parser = new DOMParser();
			parser.parse(new InputSource(inputStream));

			// Get the root of the DOM document.
			DocumentImpl document = (DocumentImpl) parser.getDocument();
			Node root = document.getLastChild();

			// Traverse the tree structure
			NodeFilterElements nodeFilter = new NodeFilterElements();
			TreeWalkerImpl treeWalker = (TreeWalkerImpl) document
					.createTreeWalker(root, NodeFilter.SHOW_ELEMENT,
							(NodeFilter) nodeFilter, true);

			walkDownInTree(treeWalker);
		} catch (Exception e) {
			// Note: It is not recommended to put message dialog here. Should be
			// in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR
					| SWT.OK);
			messageBox.setText("Error when parsing XML file.");
			messageBox.setMessage("Error: " + e.getMessage());
			messageBox.open();

			ErrorLogger.println("error: " + e);
			e.printStackTrace();
			System.exit(0);
		}
	}*/

    // Traverses the tree structure.
	/*public void walkDownInTree(TreeWalkerImpl treeWalker) {
		Node elementNode = treeWalker.getCurrentNode();
		createMemoryModel(elementNode);

		for (Node child = treeWalker.firstChild(); child != null; child = treeWalker
				.nextSibling()) {
			walkDownInTree(treeWalker);
		}
		treeWalker.setCurrentNode(elementNode);
	}*/

    // Parse node data and create memory model.
    public void createMemoryModel(Node elementNode) {
        if (elementNode.getNodeName().equals("MarTransExport")) {
            currentVisit = null;
            currentSample = null;
            currentVariable = null;
        } else if (elementNode.getNodeName().equals("undersokning")) {
            currentVisit = null;
            currentSample = null;
            currentVariable = null;
        } else if (elementNode.getNodeName().equals("lokal")) {
            currentVisit = null;
            currentSample = null;
            currentVariable = null;
        } else if ((elementNode.getNodeName().equals("tillfalle"))) {


//			createVisit(elementNode); // Adds fields for tempNodeLokal and
//										// elementNode.
            currentVisit = null;
            currentSample = null;
            currentVariable = null;

            isTransektTaxaMinMaxCreated = false;

        } else if ((elementNode.getNodeName().equals("hydrografi"))) {


            createVisit(elementNode);
            createSample(elementNode);

            currentVariable = null;
        } else if ((elementNode.getNodeName().equals("transekt"))) {

            createVisit(elementNode); // Adds fields for tempNodeLokal and elementNode.

            currentSample = null;
            currentVariable = null;

        } else if ((elementNode.getNodeName().equals("avsnitt"))) {
            createSample(elementNode);

            currentVariable = null;
        } else if ((elementNode.getNodeName().equals("avsnittart"))) {

            createVariable(elementNode);
        } else if ((elementNode.getNodeName().equals("provsubstrat"))) {
            createVariable(elementNode);
        } else if ((elementNode.getNodeName().equals("avsnittsubstrat"))) {
            createVariable(elementNode);
        } else if ((elementNode.getNodeName().equals("prov"))) {
            createSample(elementNode);
            currentVariable = null;
        } else if ((elementNode.getNodeName().equals("provart"))) {
            createVariable(elementNode);
        } else if ((elementNode.getNodeName().equals("transekttaxaminmax"))) {
            createTransektTaxaMinMax(elementNode);
        }

        addDataToModel(elementNode);
    }

    // Parse node data and create memory model.
    public void addDataToModel(Node elementNode) {
        if (elementNode.getParentNode().getNodeName().equals("#document")) {
            // No data fields.
        } else if (elementNode.getParentNode().getNodeName()
                .equals("MarTransExport")) {
            addDatasetField(elementNode);
        } else if (elementNode.getParentNode().getNodeName()
                .equals("undersokning")) {
            addDatasetField(elementNode);
        } else if (elementNode.getParentNode().getNodeName().equals("lokal")) {
            // Added later in createVisit.
        } else if (elementNode.getParentNode().getNodeName()
                .equals("tillfalle")) {
            // Added later in createSample.
        } else if (elementNode.getParentNode().getNodeName()
                .equals("hydrografi")) {
            addSampleField(elementNode);
        } else if (elementNode.getParentNode().getNodeName().equals("transekt")) {


            // // Added later in createSample.
            // Added later in createVisit.


        } else if (elementNode.getParentNode().getNodeName().equals("avsnitt")) {
            addSampleField(elementNode);
        } else if ((elementNode.getParentNode().getNodeName()
                .equals("avsnittart"))) {
            addVariableField(elementNode);
        } else if ((elementNode.getParentNode().getNodeName()
                .equals("provsubstrat"))) {
            addVariableField(elementNode);
        } else if ((elementNode.getParentNode().getNodeName()
                .equals("avsnittsubstrat"))) {
            addVariableField(elementNode);
        } else if ((elementNode.getParentNode().getNodeName().equals("prov"))) {
            addSampleField(elementNode);
        } else if ((elementNode.getParentNode().getNodeName().equals("provart"))) {
            addVariableField(elementNode);
        } else if ((elementNode.getParentNode().getNodeName()
                .equals("transekttaxaminmax"))) {
            addVariableField(elementNode);
        } else {
            importInfo.addConcatWarning("Element not parsed : "
                    + elementNode.getParentNode().getNodeName() + ":"
                    + elementNode.getNodeName() + " "
                    + elementNode.getTextContent());
        }
    }

    public void createVisit(Node elementNode) {
        currentVisit = new Visit();
        dataset.addVisit(currentVisit);

        String parentNodeName = elementNode.getParentNode().getNodeName();

//		// Add fields from parent nodes.
//		if (parentNodeName.equals("lokal")) {
//			Node tmpNodeChild = elementNode.getParentNode().getFirstChild();
//			while (tmpNodeChild != null) {
//				if ((tmpNodeChild.getNodeType() == Node.ELEMENT_NODE)
//						&& (tmpNodeChild.getChildNodes().getLength() <= 1)) {
//					addVisitField(tmpNodeChild);
//				}
//				tmpNodeChild = tmpNodeChild.getNextSibling();
//			}
//		}


        // Add fields from parent nodes.
        if (parentNodeName.equals("tillfalle")) {
            // Add data from parent node "tillfalle".
            Node tmpNodeChild = elementNode.getParentNode().getFirstChild();
            while (tmpNodeChild != null) {
                if ((tmpNodeChild.getNodeType() == Node.ELEMENT_NODE)
                        && (tmpNodeChild.getChildNodes().getLength() <= 1)) {
                    addVisitField(tmpNodeChild);
                }
                tmpNodeChild = tmpNodeChild.getNextSibling();
            }

            // Add data from parent-parent node "lokal".
            if (elementNode.getParentNode().getParentNode().getNodeName().equals("lokal")) {
                tmpNodeChild = elementNode.getParentNode().getParentNode().getFirstChild();
                while (tmpNodeChild != null) {
                    if ((tmpNodeChild.getNodeType() == Node.ELEMENT_NODE)
                            && (tmpNodeChild.getChildNodes().getLength() <= 1)) {
                        addVisitField(tmpNodeChild);
                    }
                    tmpNodeChild = tmpNodeChild.getNextSibling();
                }
            }
        }


        if (elementNode.getNodeName().equals("transekt")) {
            System.out.println("DEBUG: " + "  transekt ");
            Node tmpNodeChild = elementNode.getFirstChild();
            while (tmpNodeChild != null) {
                if ((tmpNodeChild.getNodeType() == Node.ELEMENT_NODE)
                        && (tmpNodeChild.getChildNodes().getLength() <= 1)) {
                    addVisitField(tmpNodeChild);
                }
                tmpNodeChild = tmpNodeChild.getNextSibling();
            }
        }

    }

    public void createSample(Node elementNode) {
        currentSample = new Sample();
        currentVisit.addSample(currentSample);

        String parentNodeName = elementNode.getParentNode().getNodeName();

        // Add fields from parent node.
        if (parentNodeName.equals("tillfalle")) {
            // Add fields from parent node "tillfalle".
            Node tmpNodeChild = elementNode.getParentNode().getFirstChild();
            while (tmpNodeChild != null) {
                if ((tmpNodeChild.getNodeType() == Node.ELEMENT_NODE)
                        && (tmpNodeChild.getChildNodes().getLength() <= 1)) {
                    addVisitField(tmpNodeChild);
                }
                tmpNodeChild = tmpNodeChild.getNextSibling();
            }
        }


//		// Add fields from parent nodes.
//		if (parentNodeName.equals("transekt")) {
//			// Add data from parent node "transekt".
//			Node tmpNodeChild = elementNode.getParentNode().getFirstChild();
//			while (tmpNodeChild != null) {
//				if ((tmpNodeChild.getNodeType() == Node.ELEMENT_NODE)
//						&& (tmpNodeChild.getChildNodes().getLength() <= 1)) {
//					addSampleField(tmpNodeChild);
//				}
//				tmpNodeChild = tmpNodeChild.getNextSibling();
//			}
//
//			// Add data from parent-parent node "tillfalle".
//			if (elementNode.getParentNode().getParentNode().getNodeName()
//					.equals("tillfalle")) {
//				tmpNodeChild = elementNode.getParentNode().getParentNode()
//						.getFirstChild();
//				while (tmpNodeChild != null) {
//					if ((tmpNodeChild.getNodeType() == Node.ELEMENT_NODE)
//							&& (tmpNodeChild.getChildNodes().getLength() <= 1)) {
//						addVisitField(tmpNodeChild);
//					}
//					tmpNodeChild = tmpNodeChild.getNextSibling();
//				}
//			}
//		}


    }

    public void createVariable(Node elementNode) {
        currentVariable = new Variable(true);
        currentSample.addVariable(currentVariable);
    }

    public void createTransektTaxaMinMax(Node elementNode) {
        // Create sample.

        if (!isTransektTaxaMinMaxCreated) {
            currentSample = new Sample();
            currentVisit.addSample(currentSample);
            isTransektTaxaMinMaxCreated = true;
        }

//		currentSample = new Sample();
//		currentVisit.addSample(currentSample);

        String parentNodeName = elementNode.getParentNode().getNodeName();

        if (parentNodeName.equals("tillfalle")) {
            // Add fields from parent node "tillfalle".
            Node tmpNodeChild = elementNode.getParentNode().getFirstChild();
            while (tmpNodeChild != null) {
                if ((tmpNodeChild.getNodeType() == Node.ELEMENT_NODE)
                        && (tmpNodeChild.getChildNodes().getLength() <= 1)) {
                    addVisitField(tmpNodeChild);
                }
                tmpNodeChild = tmpNodeChild.getNextSibling();
            }
        }

        // Add fields from parent node.
        if (parentNodeName.equals("transekt")) {
            // Add data from parent node "transekt".
            Node tmpNodeChild = elementNode.getParentNode().getFirstChild();
            while (tmpNodeChild != null) {
                if ((tmpNodeChild.getNodeType() == Node.ELEMENT_NODE)
                        && (tmpNodeChild.getChildNodes().getLength() <= 1)) {
                    addSampleField(tmpNodeChild);
                }
                tmpNodeChild = tmpNodeChild.getNextSibling();
            }

            // Add data from parent-parent node "tillfalle".
            if (elementNode.getParentNode().getNodeName().equals("tillfalle")) {
                tmpNodeChild = elementNode.getParentNode().getParentNode()
                        .getFirstChild();
                while (tmpNodeChild != null) {
                    if ((tmpNodeChild.getNodeType() == Node.ELEMENT_NODE)
                            && (tmpNodeChild.getChildNodes().getLength() <= 1)) {
                        addVisitField(tmpNodeChild);
                    }
                    tmpNodeChild = tmpNodeChild.getNextSibling();
                }
            }
        }

        // Create variable.
        currentVariable = new Variable(true);
        currentSample.addVariable(currentVariable);
    }

    public void addDatasetField(Node elementNode) {
        if ((elementNode.getNodeType() == Node.ELEMENT_NODE)
                && (elementNode.getChildNodes().getLength() <= 1)) {

            String key = translateKey(elementNode.getParentNode().getNodeName()
                    + "." + elementNode.getNodeName());

            // Translate values in 'SHARK_CONFIG/translate_all_columns.txt'.
            String reportedColumnValue = elementNode.getTextContent();
            String columnValue = reportedColumnValue;
//			try {
//				String[] parts = key.split(Pattern.quote("."));
//				String key_right = parts[1];
//				columnValue = FileImportTranslateAllColumns.instance().translateValue(key_right, reportedColumnValue);
//				if (!reportedColumnValue.equals(columnValue)) {
//					importInfo.addConcatWarning("Translated value: " + key + " Old: " + reportedColumnValue + " New: " + columnValue);
//				}
//			} catch (Exception e) {
//				System.out.println("DEBUG: Exception when trying to translate value: " + reportedColumnValue);
//			}			

            dataset.addField(key, columnValue);
//			dataset.addField(key, elementNode.getTextContent());
        }
    }

    public void addVisitField(Node elementNode) {
        if ((elementNode.getNodeType() == Node.ELEMENT_NODE)
                && (elementNode.getChildNodes().getLength() <= 1)) {

            String key = translateKey(elementNode.getParentNode().getNodeName()
                    + "." + elementNode.getNodeName());


            //////////////////////////////
            if (elementNode.getNodeName().equals("transektbredd")) {
                System.out.println("DEBUG: " + key + "   Node: " + elementNode.getNodeName());
            }
            //////////////////////////////


            // Translate values in 'SHARK_CONFIG/translate_all_columns.txt'.
            String reportedColumnValue = elementNode.getTextContent();
            String columnValue = reportedColumnValue;
//			try {
//				String[] parts = key.split(Pattern.quote("."));
//				String key_right = parts[1];
//				columnValue = FileImportTranslateAllColumns.instance().translateValue(key_right, reportedColumnValue);
//				if (!reportedColumnValue.equals(columnValue)) {
//					importInfo.addConcatWarning("Translated value: " + key + " Old: " + reportedColumnValue + " New: " + columnValue);
//				}
//			} catch (Exception e) {
//				System.out.println("DEBUG: Exception when trying to translate value: " + reportedColumnValue);
//			}


            // Fix date&time when both are reported in Visit_date.
            if (key.equals("visit.visit_date")) {
                String tmpDate = "";
                String tmpTime = "";
                if (columnValue.length() > 10) {
                    tmpDate = columnValue.substring(0, 10).trim();
                    tmpTime = columnValue.substring(10).trim();

                    columnValue = tmpDate;
                    currentVisit.addField("sample.sample_time", tmpTime);

                    System.out.println("DEBUG Martrans: Date: " + tmpDate + "   time: " + tmpTime);
                }
            }


            currentVisit.addField(key, columnValue);
//			currentVisit.addField(key, elementNode.getTextContent());
        }
    }

    public void addSampleField(Node elementNode) {
        if ((elementNode.getNodeType() == Node.ELEMENT_NODE)
                && (elementNode.getChildNodes().getLength() <= 1)) {

            String key = translateKey(elementNode.getParentNode().getNodeName()
                    + "." + elementNode.getNodeName());

            // Translate values in 'SHARK_CONFIG/translate_all_columns.txt'.
            String reportedColumnValue = elementNode.getTextContent();
            String columnValue = reportedColumnValue;
//			try {
//				String[] parts = key.split(Pattern.quote("."));
//				String key_right = parts[1];
//				columnValue = FileImportTranslateAllColumns.instance().translateValue(key_right, reportedColumnValue);
//				if (!reportedColumnValue.equals(columnValue)) {
//					importInfo.addConcatWarning("Translated value: " + key + " Old: " + reportedColumnValue + " New: " + columnValue);
//				}
//			} catch (Exception e) {
//				System.out.println("DEBUG: Exception when trying to translate value: " + reportedColumnValue);
//			}			

            currentSample.addField(key, columnValue);
//			currentSample.addField(key, elementNode.getTextContent());
        }
    }

    public void addVariableField(Node elementNode) {
        if ((elementNode.getNodeType() == Node.ELEMENT_NODE)
                && (elementNode.getChildNodes().getLength() <= 1)) {

            String key = translateKey(elementNode.getParentNode().getNodeName()
                    + "." + elementNode.getNodeName());

            // Translate values in 'SHARK_CONFIG/translate_all_columns.txt'.
            String reportedColumnValue = elementNode.getTextContent();
            String columnValue = reportedColumnValue;
//			try {
//				String[] parts = key.split(Pattern.quote("."));
//				String key_right = parts[1];
//				columnValue = FileImportTranslateAllColumns.instance().translateValue(key_right, reportedColumnValue);
//				if (!reportedColumnValue.equals(columnValue)) {
//					importInfo.addConcatWarning("Translated value: " + key + " Old: " + reportedColumnValue + " New: " + columnValue);
//				}
//			} catch (Exception e) {
//				System.out.println("DEBUG: Exception when trying to translate value: " + reportedColumnValue);
//			}			

            currentVariable.addField(key, columnValue);
//			currentVariable.addField(key, elementNode.getTextContent());
        }
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
    public void postReformatDataset(Dataset dataset) {
//		if (dataset.getDeliveryDatatypeCode().equals("PB")) {
//			dataset.addField("dataset.delivery_sample_datatype", "Phytobenthos");
//		}
    }

    @Override
    public void postReformatVisit(Visit visit) {
        // TODO Auto-generated method stub

    }

    @Override
    public void postReformatSample(Sample sample) {
//		if (sample.getField("sample.sample_datatype").equals("PB")) {
//			sample.addField("sample.sample_datatype", "Phytobenthos");
//		}
        // Converts value for the field "nospecies".
        // "nospecies = 0" --> "fauna_flora_found = Y".
        // "nospecies = 1" --> "fauna_flora_found = N".
        if (sample.containsField("sample.fauna_flora_found")) {
            if (sample.getField("sample.fauna_flora_found").equals("0")) {
                sample.addField("sample.fauna_flora_found", "Y");
            } else if (sample.getField("sample.fauna_flora_found").equals("1")) {
                sample.addField("sample.fauna_flora_found", "N");
            }
        }

        // If min-distance has value and not max-distance, copy value to
        // max-distance.
        if ((!sample.getField("sample.transect_min_distance").equals(""))
                && (sample.getField("sample.transect_max_distance").equals(""))) {
            sample.addField("sample.transect_max_distance",
                    sample.getField("sample.transect_min_distance"));
        }

    }

    @Override
    public void postReformatVariable(Variable variable) {

        // Copy from import matrix:
        // TEMP.substrate	provsubstrat.substrat_substra
        // TEMP.substrate_code	provsubstrat.substratkod
        // TEMP.substrate_cover	provsubstrat.tackningsgrad
        // TEMP.substrate_comment	provsubstrat.kommentar

        if (!variable.getField("TEMP.substrate").equals("")) {

            // if (!variable.getField("TEMP.substrate_comment").equals("")) {
            // System.out.println("DEBUG: " +
            // variable.getField("TEMP.substrate_comment"));
            // }

            if (variable.getField("TEMP.substrate").equals("Häll")) {
                variable.getParent().addField("sample.sample_substrate_cover_rock",
                        variable.getField("TEMP.substrate_cover"));
                variable.getParent().addField("sample.sample_substrate_comnt_rock",
                        variable.getField("TEMP.substrate_comment"));
            }
            if (variable.getField("TEMP.substrate").equals("Block")) {
                variable.getParent().addField("sample.sample_substrate_cover_boulder",
                        variable.getField("TEMP.substrate_cover"));
                variable.getParent().addField("sample.sample_substrate_comnt_boulder",
                        variable.getField("TEMP.substrate_comment"));
            }
            if (variable.getField("TEMP.substrate").equals("Sten")) {
                variable.getParent().addField("sample.sample_substrate_cover_stone",
                        variable.getField("TEMP.substrate_cover"));
                variable.getParent().addField("sample.sample_substrate_comnt_stone",
                        variable.getField("TEMP.substrate_comment"));
            }
            if (variable.getField("TEMP.substrate").equals("Grus")) {
                variable.getParent().addField("sample.sample_substrate_cover_gravel",
                        variable.getField("TEMP.substrate_cover"));
                variable.getParent().addField("sample.sample_substrate_comnt_gravel",
                        variable.getField("TEMP.substrate_comment"));
            }
            if (variable.getField("TEMP.substrate").equals("Sand")) {
                variable.getParent().addField("sample.sample_substrate_cover_sand",
                        variable.getField("TEMP.substrate_cover"));
                variable.getParent().addField("sample.sample_substrate_comnt_sand",
                        variable.getField("TEMP.substrate_comment"));
            }
            if (variable.getField("TEMP.substrate").equals("Mjukbotten")) {
                variable.getParent().addField("sample.sample_substrate_cover_softbottom",
                        variable.getField("TEMP.substrate_cover"));
                variable.getParent().addField("sample.sample_substrate_comnt_softbottom",
                        variable.getField("TEMP.substrate_comment"));
            }
        }

        // Copy from import matrix:
        // TEMP.sect_substrate	avsnittsubstrat.substrat_substrat
        // TEMP.sect_substrate_code	avsnittsubstrat.substratkod
        // TEMP.sect_substrate_cover	avsnittsubstrat.tackningsgrad
        // TEMP.sect_substrate_comment	avsnittsubstrat.kommentar

        if (!variable.getField("TEMP.sect_substrate").equals("")) {

            // if (!variable.getField("TEMP.sect_substrate_comment").equals(""))
            // {
            // System.out.println("DEBUG: " +
            // variable.getField("TEMP.sect_substrate_comment"));
            // // return; // TODO TODO:
            // }

            if (variable.getField("TEMP.sect_substrate").equals("Häll")) {
                variable.getParent().addField("sample.section_substrate_cover_rock",
                        variable.getField("TEMP.sect_substrate_cover"));
                variable.getParent().addField("sample.section_substrate_comnt_rock",
                        variable.getField("TEMP.sect_substrate_comment"));
            }
            if (variable.getField("TEMP.sect_substrate").equals("Block")) {
                variable.getParent().addField("sample.section_substrate_cover_boulder",
                        variable.getField("TEMP.sect_substrate_cover"));
                variable.getParent().addField("sample.section_substrate_comnt_boulder",
                        variable.getField("TEMP.sect_substrate_comment"));
            }
            if (variable.getField("TEMP.sect_substrate").equals("Sten")) {
                variable.getParent().addField("sample.section_substrate_cover_stone",
                        variable.getField("TEMP.sect_substrate_cover"));
                variable.getParent().addField("sample.section_substrate_comnt_stone",
                        variable.getField("TEMP.sect_substrate_comment"));
            }
            if (variable.getField("TEMP.sect_substrate").equals("Grus")) {
                variable.getParent().addField("sample.section_substrate_cover_gravel",
                        variable.getField("TEMP.sect_substrate_cover"));
                variable.getParent().addField("sample.section_substrate_comnt_gravel",
                        variable.getField("TEMP.sect_substrate_comment"));
            }
            if (variable.getField("TEMP.sect_substrate").equals("Sand")) {
                variable.getParent().addField("sample.section_substrate_cover_sand",
                        variable.getField("TEMP.sect_substrate_cover"));
                variable.getParent().addField("sample.section_substrate_comnt_sand",
                        variable.getField("TEMP.sect_substrate_comment"));
            }
            if (variable.getField("TEMP.sect_substrate").equals("Mjukbotten")) {
                variable.getParent().addField("sample.section_substrate_cover_softbottom",
                        variable.getField("TEMP.sect_substrate_cover"));
                variable.getParent().addField("sample.section_substrate_comnt_softbottom",
                        variable.getField("TEMP.sect_substrate_comment"));
            }
        }

//		// Connect parameter to corresponding quality field name.
//		// Note. This one differ from other variants of QFLAG management. The
//		// XML base class
//		// does not use 'TempField' and the ordinary 'Field' is used instead.
//		if (variable.getParameter().equals("Secchi depth")) {
//			String tmpFieldKey = "TEMP.Q_SECCHI";
//			// Add field "variable.quality_flag"
//			String tmpValue = variable.getParent().getParent()
//					.getField(tmpFieldKey);
//			variable.addField("variable.quality_flag", tmpValue);
//			// Remove it when used.
//			variable.getParent().getParent().removeField(tmpFieldKey);
//		}
    }

    @Override
    public void postReorganizeDataset(Dataset dataset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void postReorganizeVisit(Visit visit) {

        // // For Martrans: Replace "station-name" with "station-name (RLABO)"
        // String rlabo =
        // visit.getParent().getField("dataset.reporting_institute_code");
        // if (!rlabo.equals("")) {
        // if (visit.containsField("visit.reported_station_name")) {
        // visit.addField("visit.reported_station_name",
        // visit.getField("visit.reported_station_name") + " (" + rlabo + ")");
        // }
        // }

    }

    @Override
    public void postReorganizeSample(Sample sample) {
        // TODO Auto-generated method stub
        // keyTranslate.put("avsnittsubstrat.substrat_substrat",
        // "TEMP.sect_substrate");
        // keyTranslate.put("avsnittsubstrat.substratkod",
        // "TEMP.sect_substrate_code");
        // keyTranslate.put("avsnittsubstrat.tackningsgrad",
        // "TEMP.sect_substratecover");
        // keyTranslate.put("avsnittsubstrat.tackningsgrad",
        // "TEMP.sect_substrate_comment");
    }

    @Override
    public void postReorganizeVariable(Variable variable) {
        // TODO Auto-generated method stub

    }

    // ======================================================================
    // === Note: New class ===
    // === Filter for the XML document. ===
    class NodeFilterElements implements NodeFilter {
        public short acceptNode(Node node) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return FILTER_ACCEPT;
            } else {
                return FILTER_SKIP;
            }
        }
    }

}

