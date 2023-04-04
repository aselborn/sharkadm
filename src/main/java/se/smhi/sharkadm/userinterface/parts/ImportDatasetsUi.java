/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.userinterface.parts;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import se.smhi.sharkadm.database.DbConnect;
import se.smhi.sharkadm.database.DeleteModel;
import se.smhi.sharkadm.database.GetChildren;
import se.smhi.sharkadm.database.SaveModelToDatabase;
import se.smhi.sharkadm.facades.DatabaseFacade;
import se.smhi.sharkadm.facades.ImportFacade;
import se.smhi.sharkadm.facades.ModelFacade;
import se.smhi.sharkadm.facades.ZipArchiveFacade;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.ModelTopNode;
import se.smhi.sharkadm.userinterface.SharkAdmMainStatusBar;
import se.smhi.sharkadm.userinterface.SharkAdmMainUiSettings;

public class ImportDatasetsUi implements Observer {

	private ImportDatasetsLabelProvider labelProvider = new ImportDatasetsLabelProvider();
	private TableViewer availableDatasetsTableViewer;
	private static SharkAdmMainUiSettings settings = SharkAdmMainUiSettings.instance();
	private Composite parent;
	final Composite parentDisplay;

	
	private Text fileField;
	
	// For streaming to import and screening log window.
	private Text logWindow;
	private static OutputStream logStream;
	private static PrintStream logPrintStream;

	// Used in drag-n-drop and ProgressMonitorDialog.
	String multiImportDirPath;
	// String[] multiImportFileNames;
	ArrayList<String> multiImportFileNames = new ArrayList<String>();
	String multiImportFile;
	
	public ImportDatasetsUi(final Composite parent, MenuItem topMenuItem) {
		this.parent = parent;
		parentDisplay = parent;
		
		DatabaseFacade.instance().addObserver(this);

		final String importFileDirectory = settings.getString(
				"dataset_directory", "");

		// ========== Sub menu ==========

		Menu importSubMenu = new Menu(parent.getShell(), SWT.DROP_DOWN);
		topMenuItem.setMenu(importSubMenu);
		
		MenuItem clearLogMenuItem = new MenuItem(importSubMenu, SWT.CASCADE);
		clearLogMenuItem.setText("&Clear log");

		new MenuItem(importSubMenu, SWT.SEPARATOR);

		final MenuItem showErrorsMenuItem = new MenuItem(importSubMenu, SWT.CASCADE | SWT.CHECK);
		showErrorsMenuItem.setText("&Show errors");
		showErrorsMenuItem.setSelection(false);
		ImportFacade.instance().setShowErrors(false);
		final MenuItem showWarningsMenuItem = new MenuItem(importSubMenu, SWT.CASCADE | SWT.CHECK);
		showWarningsMenuItem.setText("&Show warnings");
		showWarningsMenuItem.setSelection(false);
		ImportFacade.instance().setShowWarnings(false);
		final MenuItem showInfoMenuItem = new MenuItem(importSubMenu, SWT.CASCADE | SWT.CHECK);
		showInfoMenuItem.setText("&Show info");
		showInfoMenuItem.setSelection(false);
		ImportFacade.instance().setShowInfo(false);
		
		// ========== Widget declarations ==========
		
		// === Declaration of selectFileGroup ===
		Group selectFileGroup = new Group(parent, SWT.NONE);
		selectFileGroup.setText("Select directory");

		// Declaration of file.
		Label fileLabel = new Label(selectFileGroup, SWT.NONE);
		fileLabel.setText("Directory:");
		fileField = new Text(selectFileGroup, SWT.BORDER);
		fileField.setText(importFileDirectory);

		Button browseButton = new Button(selectFileGroup, SWT.PUSH);
		browseButton.setText("Browse...");

		// Declaration of datasetsInDirectoryGroup.
		Group datasetsInDirectoryGroup = new Group(parent, SWT.NONE);
		datasetsInDirectoryGroup.setText("Datasets in selected directory");

		// Declaration of datasetsTable.
		availableDatasetsTableViewer = new TableViewer(
				datasetsInDirectoryGroup, SWT.BORDER | SWT.MULTI
						| SWT.FULL_SELECTION);
		final Table datasetsTable = availableDatasetsTableViewer.getTable();
		datasetsTable.setHeaderVisible(true);
		datasetsTable.setLinesVisible(true);

		String[] columnNames = new String[] {"Dataset name", 
											 "Errors", 
											 "Warnings", 
											 "File path"};
		int[] columnWidths = new int[] {300, 70, 70, 900};
		int[] columnAlignments = new int[] {SWT.LEFT, SWT.RIGHT, SWT.RIGHT, SWT.LEFT};
		for (int i = 0; i < columnNames.length; i++) { 
			TableColumn column = new TableColumn(datasetsTable,
												 columnAlignments[i]);
												 column.setText(columnNames[i]);
												 column.setWidth(columnWidths[i]);
		}

		availableDatasetsTableViewer.setLabelProvider(labelProvider);
		availableDatasetsTableViewer.setContentProvider(new ArrayContentProvider());
		
//		Object datasetNoteArray[] = ReadDataset.instance().readDatasetListTable().toArray();
//		availableDatasetsTableViewer.setInput(datasetNoteArray);
		Object datasetNoteArray[] = ImportFacade.instance().getDatasetAndPathList(fileField.getText()).toArray();
		availableDatasetsTableViewer.setInput(datasetNoteArray);
		
		// === Declaration of logGroup ===
		Group logGroup = new Group(parent, SWT.NONE);
		logGroup.setText("Import/screening log");

		// Declaration of screeningLogWindow.
		logWindow = new Text(logGroup, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.MULTI | SWT.READ_ONLY);
		logWindow.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
//		logWindow.append("\nHint: It is possible to drag-n-drop zipped dataset packages to this area.\n");

		// Create a log print stream (similar to System.out) but for the log window.
		logStream = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				if (logWindow.isDisposed()) return;
				logWindow.append(String.valueOf((char) b));
			}
		};
		logPrintStream = new PrintStream(logStream);

		// ========== Buttons ==========
		
		// GROUP: Declaration of memoryModelButtonsGroup.
		Group memoryModelButtonsGroup = new Group(parent, SWT.NONE);
		memoryModelButtonsGroup.setText("Memory model (for QC)");

		// Declaration of importDataButton.
		Button importDataButton = new Button(memoryModelButtonsGroup, SWT.PUSH);
		importDataButton.setText("Import to memory model");

		// Declaration of clearMemoryModelButton.
		Button clearMemoryModelButton = new Button(memoryModelButtonsGroup, SWT.PUSH);
		clearMemoryModelButton.setText("Clear memory model");
		
		// Declaration of autoClearMemoryModelCheckBox.
		final Button autoClearMemoryModelCheckBox = new Button(memoryModelButtonsGroup, SWT.CHECK);
		autoClearMemoryModelCheckBox.setText("Clear automatically");
		autoClearMemoryModelCheckBox.setSelection(true);
		ImportFacade.instance().setAutoClearMemoryModel(true);

//		// Declaration of createZipButton.
//		Button createZipButton = new Button(memoryModelButtonsGroup, SWT.PUSH);
//		createZipButton.setText("Create zip file(s) for test...");
//
//		// Declaration of saveToDbButton.
//		Button saveToDbButton = new Button(memoryModelButtonsGroup, SWT.PUSH);
//		saveToDbButton.setText("Save to database...");

		
		// GROUP: Declaration of testButtonsGroup.
		Group testButtonsGroup = new Group(parent, SWT.NONE);
		testButtonsGroup.setText("Test data");

		// Declaration of importCreateZipSaveToDbButton.
		// Declaration of importAndCreateZipButton.
		Button importAndCreateZipButton = new Button(testButtonsGroup, SWT.PUSH);
		importAndCreateZipButton.setText("Import and create zip file(s) for test...");

		// Declaration of importAndSaveToDbButton.
		Button importAndSaveToDbButton = new Button(testButtonsGroup, SWT.PUSH);
		importAndSaveToDbButton.setText("Import and save to database...");


		// GROUP: Declaration of publishButtonsGroup.
		Group publishButtonsGroup = new Group(parent, SWT.NONE);
		publishButtonsGroup.setText("Publish data");

		// Declaration of importCreateZipSaveToDbButton.
		Button importCreateZipSaveToDbButton = new Button(publishButtonsGroup, SWT.PUSH);
		importCreateZipSaveToDbButton.setText("Import, create zip and save to database...");

//		// Declaration of exportZipToSharkdata.
//		Button exportZipToSharkdata = new Button(publishButtonsGroup, SWT.PUSH);
//		exportZipToSharkdata.setText("Export zip file(s) to SHARKdata...");


		// GROUP: Declaration of miscButtonsGroup.
		Group miscButtonsGroup = new Group(parent, SWT.NONE);
		miscButtonsGroup.setText("Miscellaneous");

		// Declaration of screeningButton.
		Button screeningButton = new Button(miscButtonsGroup, SWT.PUSH);
		screeningButton.setText("Screening (duplicate samples in db)");
		
		// Declaration of openLogFileButton.
		Button openLogFileButton = new Button(miscButtonsGroup, SWT.PUSH);
		openLogFileButton.setText("Open log file (shark_adm_log.txt)");
		
		// ==================== Layout ====================
		
		FormLayout formLayout;
		FormData formData;

		// Layout for parent composite.
		formLayout = new FormLayout();
		formLayout.marginWidth = 5;
		formLayout.marginHeight = 5;
		formLayout.spacing = 5;
		parent.setLayout(formLayout);

		// Layout for selectFileGroup.
		FormLayout itemGroupLayout = new FormLayout();
		itemGroupLayout.marginWidth = 5;
		itemGroupLayout.marginHeight = 5;
		itemGroupLayout.spacing = 5;
		selectFileGroup.setLayout(itemGroupLayout);
		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(80, -2);
		selectFileGroup.setLayoutData(formData);

		// Layout for fileLabel and fileField.
		formData = new FormData();
		formData.top = new FormAttachment(0, 3);
		formData.left = new FormAttachment(0, 0);
		fileLabel.setLayoutData(formData);
		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(fileLabel, 0);
		formData.right = new FormAttachment(browseButton, 0);
		fileField.setLayoutData(formData);

		// Layout for browseButton.
		formData = new FormData();
		formData.top = new FormAttachment(0, -3);
		formData.right = new FormAttachment(100, 0);
		browseButton.setLayoutData(formData);
		
		
		// Layout for datasetsInDirectoryGroup.
		formLayout = new FormLayout();
		formLayout.marginWidth = 5;
		formLayout.marginHeight = 5;
		formLayout.spacing = 5;
		datasetsInDirectoryGroup.setLayout(formLayout);
		formData = new FormData();
		formData.top = new FormAttachment(selectFileGroup, 2);
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(80, -2);
		formData.bottom = new FormAttachment(50, -5);
		datasetsInDirectoryGroup.setLayoutData(formData);

		// Layout for datasetsTable.
		formLayout = new FormLayout();
		formLayout.marginWidth = 5;
		formLayout.marginHeight = 5;
		formLayout.spacing = 5;
		datasetsTable.setLayout(formLayout);
		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(100, -2);
		formData.bottom = new FormAttachment(100, 0);
		datasetsTable.setLayoutData(formData);

		// Layout for logGroup.
		formData = new FormData();
		formData.top = new FormAttachment(datasetsInDirectoryGroup, 0);
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(80, -2);
		formData.bottom = new FormAttachment(100, -5);
		logGroup.setLayoutData(formData);		
		formLayout = new FormLayout();
		formLayout.marginWidth = 5;
		formLayout.marginHeight = 5;
		formLayout.spacing = 5;
		logGroup.setLayout(formLayout);
		
		// Layout for logWindow.
		formData = new FormData();
		formData.top = new FormAttachment(datasetsInDirectoryGroup, 0);
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(100, -2);
		formData.bottom = new FormAttachment(100, -2);
		logWindow.setLayoutData(formData);

		// ========== Buttons ==========
		
		// Layout for memoryModelButtonsGroup.
		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(selectFileGroup, 0);
		formData.right = new FormAttachment(100, -2);
		memoryModelButtonsGroup.setLayoutData(formData);		
		formLayout = new FormLayout();
		formLayout.marginWidth = 5;
		formLayout.marginHeight = 5;
		formLayout.spacing = 5;
		memoryModelButtonsGroup.setLayout(formLayout);
		
		// Layout for testButtonsGroup.
		formData = new FormData();
		formData.top = new FormAttachment(memoryModelButtonsGroup, 0);
		formData.left = new FormAttachment(selectFileGroup, 0);
		formData.right = new FormAttachment(100, -2);
		testButtonsGroup.setLayoutData(formData);		
		formLayout = new FormLayout();
		formLayout.marginWidth = 5;
		formLayout.marginHeight = 5;
		formLayout.spacing = 5;
		testButtonsGroup.setLayout(formLayout);
		
		// Layout for publishButtonsGroup.
		formData = new FormData();
		formData.top = new FormAttachment(testButtonsGroup, 0);
		formData.left = new FormAttachment(selectFileGroup, 0);
		formData.right = new FormAttachment(100, -2);
		publishButtonsGroup.setLayoutData(formData);		
		formLayout = new FormLayout();
		formLayout.marginWidth = 5;
		formLayout.marginHeight = 5;
		formLayout.spacing = 5;
		publishButtonsGroup.setLayout(formLayout);
		
		// Layout for miscButtonsGroup.
		formData = new FormData();
		formData.top = new FormAttachment(publishButtonsGroup, 0);
		formData.left = new FormAttachment(selectFileGroup, 0);
		formData.right = new FormAttachment(100, -2);
		miscButtonsGroup.setLayoutData(formData);		
		formLayout = new FormLayout();
		formLayout.marginWidth = 5;
		formLayout.marginHeight = 5;
		formLayout.spacing = 5;
		miscButtonsGroup.setLayout(formLayout);
		
		// Layout for importDataButton.
		formData = new FormData();
		formData.left = new FormAttachment(0, 0);
		importDataButton.setLayoutData(formData);
		
		// Layout for clearMemoryModelButton.
		formData = new FormData();
		formData.top = new FormAttachment(importDataButton, 5);
		clearMemoryModelButton.setLayoutData(formData);
		
		// Layout for autoClearMemoryModelCheckBox.
		formData = new FormData();
		formData.top = new FormAttachment(importDataButton, 10);
		formData.left = new FormAttachment(clearMemoryModelButton, 10);
		autoClearMemoryModelCheckBox.setLayoutData(formData);
		
//		// Layout for updateZipArchiveFileButton.
//		formData = new FormData();
//		formData.top = new FormAttachment(clearMemoryModelButton, 5);
//		createZipButton.setLayoutData(formData);
//		
//		// Layout for saveToDbButton.
//		formData = new FormData();
//		formData.top = new FormAttachment(createZipButton, 5);
//		saveToDbButton.setLayoutData(formData);

		// Layout of importAndCreateZipButton.
		formData = new FormData();
		formData.top = new FormAttachment(importCreateZipSaveToDbButton, 5);
		importAndCreateZipButton.setLayoutData(formData);
		
		// Layout of importAndSaveToDbButton.
		formData = new FormData();
		formData.top = new FormAttachment(importAndCreateZipButton, 5);
		importAndSaveToDbButton.setLayoutData(formData);
		
		// Layout of importCreateZipSaveToDbButton.
		formData = new FormData();
		formData.top = new FormAttachment(0, 5);
		importCreateZipSaveToDbButton.setLayoutData(formData);
		
//		// Layout of exportZipToSharkdata.
//		formData = new FormData();
//		formData.top = new FormAttachment(importCreateZipSaveToDbButton, 5);
//		exportZipToSharkdata.setLayoutData(formData);
		
		
		// Layout for screeningButton.
		formData = new FormData();
		formData.top = new FormAttachment(0, 5);
		screeningButton.setLayoutData(formData);
		
		// Layout for openLogFileButton.
		formData = new FormData();
		formData.top = new FormAttachment(screeningButton, 5);
		openLogFileButton.setLayoutData(formData);
		
		// ========== Listeners ==========
		// ========== Listeners ==========
		// ========== Listeners ==========
		
		Listener listener;

		// Listener for clearLogMenuItem.
		listener = new Listener() {
			public void handleEvent(Event event) {
				logWindow.setText("");											
			}
		};
		clearLogMenuItem.addListener(SWT.Selection, listener);
		
		// Listener for showErrorsMenuItem.
		listener = new Listener() {
			public void handleEvent(Event event) {
				if (showErrorsMenuItem.getSelection()) {
					ImportFacade.instance().setShowErrors(true);
				} else {
					ImportFacade.instance().setShowErrors(false);
				}
			}
		};
		showErrorsMenuItem.addListener(SWT.Selection, listener);
		
		// Listener for showWarningsMenuItem.
		listener = new Listener() {
			public void handleEvent(Event event) {
				if (showWarningsMenuItem.getSelection()) {
					ImportFacade.instance().setShowWarnings(true);
				} else {
					ImportFacade.instance().setShowWarnings(false);
				}
			}
		};
		showWarningsMenuItem.addListener(SWT.Selection, listener);
		
		// Listener for showInfoMenuItem.
		listener = new Listener() {
			public void handleEvent(Event event) {
				if (showInfoMenuItem.getSelection()) {
					ImportFacade.instance().setShowInfo(true);
				} else {
					ImportFacade.instance().setShowInfo(false);
				}
			}
		};
		showInfoMenuItem.addListener(SWT.Selection, listener);

		// Listener for autoClearMemoryModelMenuItem.
		listener = new Listener() {
			public void handleEvent(Event event) {
				if (autoClearMemoryModelCheckBox.getSelection()) {
					ImportFacade.instance().setAutoClearMemoryModel(true);
					logWindow.setText("");											
					ModelFacade.instance().clearMemoryModel();				
				} else {
					ImportFacade.instance().setAutoClearMemoryModel(false);
				}
			}
		};
		autoClearMemoryModelCheckBox.addListener(SWT.Selection, listener);
		
		// Listener for browseButton.
		listener = new Listener() {
			public void handleEvent(Event event) {
				DirectoryDialog directoryDialog = new DirectoryDialog(parent.getShell(), 0);
				directoryDialog.setText("Select directory...");
				File importFile = new File(fileField.getText());
				if (importFile.getName().contains(".")) {
					directoryDialog.setFilterPath(importFile.getParent());
				} else {
					directoryDialog.setFilterPath(fileField.getText());
				}
				String path = directoryDialog.open();
				if (path != null) {
					logWindow.setText("");											
					fileField.setText(path);
					settings.setString("dataset_directory", fileField.getText());
					Object datasetNoteArray[] = ImportFacade.instance().getDatasetAndPathList(fileField.getText()).toArray();
					availableDatasetsTableViewer.setInput(datasetNoteArray);
				}
			}
		};
		browseButton.addListener(SWT.Selection, listener);
		
		
		
		listener = new Listener() {
			public void handleEvent(Event e) {
				String path = fileField.getText();
				if (path != null) {
					logWindow.setText("");											
					fileField.setText(path);
					settings.setString("dataset_directory", fileField.getText());
					Object datasetNoteArray[] = ImportFacade.instance().getDatasetAndPathList(fileField.getText()).toArray();
					availableDatasetsTableViewer.setInput(datasetNoteArray);
				}
	        }
	    };
		fileField.addListener(SWT.DefaultSelection, listener);
		
		
		
		// Listener for importDataButton.
		listener = new Listener() {
			public void handleEvent(Event event) {
				SharkAdmMainStatusBar.setField5("Busy: Importing file to memory model.");
				logWindow.setText("");
				// Update screen.
				parent.update();
				try { Thread.sleep(10); } catch (Exception e) { }
				// Import.
				
				TableItem[] selectedRows = datasetsTable.getSelection();
				
				if (selectedRows.length > 0) {
					for (TableItem row : selectedRows) {
						String datasetPath = (String)((Map)row.getData()).get("import_file_path");
						ImportFacade.instance().importSharkFolder(datasetPath, logPrintStream);
					}
				} else {
					MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_WARNING | SWT.OK);
					messageBox.setText("Warning");
					messageBox.setMessage("No file(s) selected.");
					messageBox.open();
				}
				SharkAdmMainStatusBar.setField5("");
			}
		};
		importDataButton.addListener(SWT.Selection, listener);
		
		// Listener for clearMemoryModelButton
		listener = new Listener() {
			public void handleEvent(Event event) {
				logWindow.setText("");
				ModelFacade.instance().clearMemoryModel();				
			}
		};
		clearMemoryModelButton.addListener(SWT.Selection, listener);
//		clearMemoryModelMenuItem.addListener(SWT.Selection, listener);
		
//		// Listener for updateZipArchiveFileButton.
//		listener = new Listener() {
//			public void handleEvent(Event event) {
//				SharkAdmMainStatusBar.setField5("Busy: Creating zip file.");
//				int numberInMemoryModel = ModelTopNode.instance().getDatasetList().size();
//				if (numberInMemoryModel > 0) {
//					MessageBox messageBox = new MessageBox(parent.getShell(),
//							SWT.ICON_QUESTION | SWT.YES | SWT.NO);
//					messageBox.setText("Update SHARK archive");
//					messageBox.setMessage("Do you want to create ZIP files for test? " +
//							((numberInMemoryModel > 1)? "\n\n Note: More than one item in the memory model." : "") +
//							" \n\n(Target ZIP path: \\\\winfs\\data\\prodkap\\sharkweb\\SHARKdata_datasets_TEST)");
//					if (messageBox.open() == SWT.YES) {
//						SharkAdmMainStatusBar.setField5("Busy: Updating SHARK archive file(s).");						
//
////						ZipArchiveFacade.instance().updateZipArchiveFile(null, logPrintStream);
//						Boolean testZip = true;
//						ZipArchiveFacade.instance().updateZipArchiveFile(testZip);
//
//						SharkAdmMainStatusBar.setField5("");							
//					}
//				} else {
//					MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_WARNING | SWT.OK);
//					messageBox.setText("Warning");
//					messageBox.setMessage("No items in memory model.");
//					messageBox.open();
//				}
//				SharkAdmMainStatusBar.setField5("");
//			}
//		};
//		createZipButton.addListener(SWT.Selection, listener);
		
//		// Listener for saveToDbButton.
//		listener = new Listener() {
//			public void handleEvent(Event event) {
//				int numberInMemoryModel = ModelTopNode.instance().getDatasetList().size();
//				if (numberInMemoryModel > 0) {
//					
//					// Always select db and enter password. 
//					DbConnect.instance().closeConnection();
//					DbLoginDialogUi.display(parent.getDisplay()); 
//					DatabaseFacade.instance().connectionStatusChanged();
//					
//					if (!DbConnect.instance().isConnected()) {
//						return;
//					}
//					
//					MessageBox messageBox = new MessageBox(parent.getShell(),
//							SWT.ICON_QUESTION | SWT.YES | SWT.NO);
//					messageBox.setText("Save Memory Model to Database");
//					messageBox.setMessage("Do you want to save the memory model to the database? " +
//							((numberInMemoryModel > 1)? "\n\n Note: More than one item in the memory model." : ""));
//					if (messageBox.open() == SWT.YES) {
//						SharkAdmMainStatusBar.setField5("Busy: Saving memory model to database.");
//						
//						java.util.List<Dataset> datasetList = ModelTopNode.instance().getDatasetList();
//						for (Dataset dataset : datasetList) {
//							
//							try {	
//								DbConnect.instance().beginTransaction();
//								
//								int oid = GetChildren.instance().getDatasetUseVersionFilter(dataset.getField("dataset.dataset_file_name"));
//								if (oid == -1) {
//									dataset.Accept(new SaveModelToDatabase());								
//								} else {
//									messageBox.setText("Dataset already exists in database");
//									messageBox.setMessage("Do you want to replace the old version with " + dataset.getField("dataset.dataset_file_name") + "?");
//									if (messageBox.open() == SWT.YES) {					
//										DeleteModel.instance().deleteDataset(oid);
//										dataset.Accept(new SaveModelToDatabase());
//									}
//								}
//								
//								DbConnect.instance().commitTransaction();
//						
//							} catch (Exception Exception) {									
//						        try {
//						        	DbConnect.instance().rollbackTransaction();
//						        	System.out.println("Transaction rollback occured.");
//						        	
//									messageBox.setText("Failed to save to database");
//									messageBox.setMessage("Failed to save to database. Database transaction rollback occured.");
//									messageBox.open();
//
//								} catch (SQLException e2) {
//									System.out.println("Rollback transaction failed");
//								}
//	
//							} finally {
//						    	try {
//									DbConnect.instance().endTransaction();
//								} catch (SQLException e3) {
//									System.out.println("End transaction failed");
//								}
//						    }
//
//						}
//
//						// Vacuum cleaning needed after delete/insert.
//						try {
//							System.out.println("Vacuum...");
//							DbConnect.instance().psqlVacuum();
//							System.out.println("Vacuum done.");
//						} catch (Exception vacuum_error) {
//							System.out.println("Vacuum failed: " + vacuum_error.getMessage());
//						}
//
//						DatabaseFacade.instance().dataChanged();
//	
//						SharkAdmMainStatusBar.setField5("");							
//					}
//				} else {
//					MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_WARNING | SWT.OK);
//					messageBox.setText("Warning");
//					messageBox.setMessage("No items in memory model.");
//					messageBox.open();
//				}
//			}
//		};
//		saveToDbButton.addListener(SWT.Selection, listener);

		// Listener for (Multi) importAndCreateZipButton.
		listener = new Listener() {
			public void handleEvent(Event event) {
				
				TableItem[] selectedRows = datasetsTable.getSelection();
				multiImportFileNames = new ArrayList<String>();
				
				if (selectedRows.length == 0) {
					MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_WARNING | SWT.OK);
					messageBox.setText("Warning");
					messageBox.setMessage("No file(s) selected.");
					messageBox.open();
				//}
					SharkAdmMainStatusBar.setField5("");
					return;
				} else {
					for (TableItem row : selectedRows) {
						String datasetPath = (String)((Map)row.getData()).get("import_file_path");
						multiImportFileNames.add(datasetPath);
					}
				
					// Ask user before proceeding.
					MessageBox messageBox = new MessageBox(parent.getShell(),
							SWT.ICON_QUESTION | SWT.YES | SWT.NO);
					messageBox.setText("Import and save");
					messageBox.setMessage("Do you want to create ZIP from " + 
							multiImportFileNames.size() + 
							((multiImportFileNames.size() > 1)? " datasets? " : " dataset? ") +
							" \n\n(Target ZIP path: \\\\winfs\\data\\prodkap\\sharkweb\\SHARKdata_datasets_TEST)");
					if (messageBox.open() != SWT.YES) {
						SharkAdmMainStatusBar.setField5("");
						return;
					}					
				
					// Use progress bar.
					ProgressMonitorDialog progress = new ProgressMonitorDialog(parent.getShell());
					try {
						progress.run(true /*fork*/, true /*cancelable*/, new IRunnableWithProgress() {
							public void run(IProgressMonitor progressMonitor)
									throws InvocationTargetException, InterruptedException {
								progressMonitor.beginTask("Importing dataset packages", multiImportFileNames.size());
								// Loop through user selected files.
								for (int i = 0; i < multiImportFileNames.size(); i++) { 
									if (multiImportFileNames.get(i) != null) {
										progressMonitor.subTask("Importing (" + (i+1) + " of " + 
																 multiImportFileNames.size() + "): " + 
																 multiImportFileNames.get(i) + "...");
										if (progressMonitor.isCanceled()) {
											progressMonitor.done();
											return;
										}
										progressMonitor.worked(1);
										// "syncExec" is needed when ui tread is accessed.  
										multiImportFile = new File(multiImportDirPath, multiImportFileNames.get(i)).getAbsolutePath();
										parent.getDisplay().syncExec(new Runnable() {
											public void run() {
												// Import one file.
												SharkAdmMainStatusBar.setField5("Busy: Importing file.");
												// Update screen.
												parent.update();
												try { Thread.sleep(10); } catch (Exception e) { }	
												
												ModelFacade.instance().clearMemoryModel();				
												ImportFacade.instance().importDatasetPackage(multiImportFile, System.out);
	
												// Save one dataset to database.
												SharkAdmMainStatusBar.setField5("Busy: Checks before saving.");
												// Update screen.
												parent.update();
												try { Thread.sleep(10); } catch (Exception e) { }	
	
												int numberInMemoryModel = ModelTopNode.instance().getDatasetList().size();
												if (numberInMemoryModel > 0) {
													SharkAdmMainStatusBar.setField5("Busy: Updating SHARK archive file(s).");
													//SaveModel.instance().beginTransaction();
													java.util.List<Dataset> datasetList = ModelTopNode.instance().getDatasetList();
													for (Dataset dataset : datasetList) {
														
														Boolean testZip = true;
														ZipArchiveFacade.instance().updateZipArchiveFile(testZip);
														
													}
													// Done.
													SharkAdmMainStatusBar.setField5("");							
												}
												SharkAdmMainStatusBar.setField5("");
											}
										});
									}
								}
								progressMonitor.done();
								ModelFacade.instance().clearMemoryModel();				
							}
						});
					} catch (InvocationTargetException e1) {
						e1.printStackTrace();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		};

		/*
			BUTTON for IMPORT TO DATABASE!
		 */
		// Listener for (Multi) importAndSaveToDbButton.

		importAndCreateZipButton.addListener(SWT.Selection, listener);


		listener = new Listener() {
			public void handleEvent(Event event) {
				
				TableItem[] selectedRows = datasetsTable.getSelection();
				multiImportFileNames = new ArrayList<String>();
				
				if (selectedRows.length == 0) {
					MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_WARNING | SWT.OK);
					messageBox.setText("Warning");
					messageBox.setMessage("No file(s) selected.");
					messageBox.open();
				//}
					SharkAdmMainStatusBar.setField5("");
					return;
				} else {
					for (TableItem row : selectedRows) {
						String datasetPath = (String)((Map)row.getData()).get("import_file_path");
						multiImportFileNames.add(datasetPath);
					}
				
					// Ask user before proceeding.
					MessageBox messageBox = new MessageBox(parent.getShell(),
							SWT.ICON_QUESTION | SWT.YES | SWT.NO);
					messageBox.setText("Import and save");
					messageBox.setMessage("Do you want to import and save " + 
							multiImportFileNames.size() + 
							((multiImportFileNames.size() > 1)? " files " : " file ") +
							"to the database? \nNote: Old versions of datasets will be replaced. ");
					if (messageBox.open() != SWT.YES) {
						SharkAdmMainStatusBar.setField5("");
						return;
					}					
//					// Login to database, if not already done.
//					if (!DbConnect.instance().isConnected()) {
//						DbLoginDialogUi.display(parent.getDisplay()); 
//						DatabaseFacade.instance().connectionStatusChanged();
//					}					
					
					// Always select db and enter password. 
					DbConnect.instance().closeConnection();
					DbLoginDialogUi.display(parent.getDisplay()); 
					DatabaseFacade.instance().connectionStatusChanged();
					
					if (!DbConnect.instance().isConnected()) {
						SharkAdmMainStatusBar.setField5("");
						return;
					}
					
					// Use progress bar.
					ProgressMonitorDialog progress = new ProgressMonitorDialog(parent.getShell());
					try {
						progress.run(true /*fork*/, true /*cancelable*/, new IRunnableWithProgress() {
							public void run(IProgressMonitor progressMonitor)
									throws InvocationTargetException, InterruptedException {
								progressMonitor.beginTask("Importing dataset packages", multiImportFileNames.size());
								
								try {

									// Loop through user selected files.
									for (int i = 0; i < multiImportFileNames.size(); i++) {
										
										if (multiImportFileNames.get(i) != null) {
											progressMonitor.subTask("Importing (" + (i+1) + " of " + 
																	multiImportFileNames.size() + "): " +
																	multiImportFileNames.get(i) + "...");
	//										progressMonitor.subTask("Importing " + multiImportFileNames[i] + "...");
											if (progressMonitor.isCanceled()) {
												progressMonitor.done();
												return;
											}
											progressMonitor.worked(1);
	
											// "syncExec" is needed when ui tread is accessed.  
											multiImportFile = new File(multiImportFileNames.get(i)).getAbsolutePath();
											parent.getDisplay().syncExec(new Runnable() {
												public void run() {
													// Import one file.
													SharkAdmMainStatusBar.setField5("Busy: Importing file.");
													// Update screen.
													parent.update();
													try { Thread.sleep(10); } catch (Exception e) { }	
													
													ModelFacade.instance().clearMemoryModel();				
													ImportFacade.instance().importDatasetPackage(multiImportFile, System.out);
		
													// Save one dataset to database.
													SharkAdmMainStatusBar.setField5("Busy: Checks before saving.");
													// Update screen.
													parent.update();
													try { Thread.sleep(10); } catch (Exception e) { }	
		
													if (DbConnect.instance().isConnected()) {
														int numberInMemoryModel = ModelTopNode.instance().getDatasetList().size();
														if (numberInMemoryModel > 0) {
															SharkAdmMainStatusBar.setField5("Busy: Saving to database.");
															
															java.util.List<Dataset> datasetList = ModelTopNode.instance().getDatasetList();
															for (Dataset dataset : datasetList) {
																
																// Use long transaction.
																try {
																	DbConnect.instance().beginTransaction();
																
																		int oid = GetChildren.instance().getDatasetUseVersionFilter(dataset.getField("dataset.dataset_file_name"));
																		if (oid == -1) {
																			dataset.Accept(new SaveModelToDatabase());								
																		} else {
																			// Don't ask if old dataset is removed.
																			DeleteModel.instance().deleteDataset(oid);
																			dataset.Accept(new SaveModelToDatabase());
																		}
																		
																		DbConnect.instance().commitTransaction();								
																
																} catch (Exception Exception) {									
															        try {
															        	DbConnect.instance().rollbackTransaction();
															        	System.out.println("Transaction rollback occured.");
																	} catch (SQLException e2) {
																		System.out.println("Rollback transaction failed");
																	}

																} finally {
															    	try {
																		DbConnect.instance().endTransaction();
																	} catch (SQLException e3) {
																		System.out.println("End transaction failed");
																	}
															    }
															}
														}
													}													
												}
											});
										} 
									}
								
								} catch (Exception Exception) {									
//							        
									try {
								        // Asynchronous call needed to view MessageBox.   
								        parentDisplay.getDisplay().asyncExec(new Runnable() {
											public void run() {
												try {
													MessageBox messageBox = new MessageBox(parent.getShell(),
															SWT.ICON_QUESTION | SWT.YES);
													messageBox.setText("Failed to write to database");
													messageBox.setMessage("Failed to write to database. Last processed dataset not saved to database.");
													messageBox.open();
												} catch (Exception e) {
													System.out.println("Message box failed: " + e.getMessage());
													e.printStackTrace();
												}
											}
										});
									} catch (Exception e) {
										System.out.println("Message box failed (asyncExec): " + e.getMessage());
										e.printStackTrace();
									}
							    }									

								// Vacuum cleaning needed after delete/insert.
								try {
									System.out.println("Vacuum...");
									DbConnect.instance().psqlVacuum();
									System.out.println("Vacuum done.");
								} catch (Exception vacuum_error) {
									System.out.println("Vacuum failed: " + vacuum_error.getMessage());
								}

								progressMonitor.done();
								ModelFacade.instance().clearMemoryModel();				
							}
						});
					} catch (InvocationTargetException e1) {
						e1.printStackTrace();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				// Done.
				DatabaseFacade.instance().dataChanged();
		    	SharkAdmMainStatusBar.setField5("");
			}
		};
		importAndSaveToDbButton.addListener(SWT.Selection, listener);
		
		// Listener for (PROD-Multi) importCreateZipSaveToDbButton.
		listener = new Listener() {
			public void handleEvent(Event event) {
				
				TableItem[] selectedRows = datasetsTable.getSelection();
				multiImportFileNames = new ArrayList<String>();
				
				if (selectedRows.length == 0) {
					MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_WARNING | SWT.OK);
					messageBox.setText("Warning");
					messageBox.setMessage("No file(s) selected.");
					messageBox.open();
				//}
					SharkAdmMainStatusBar.setField5("");
					return;
				} else {
					for (TableItem row : selectedRows) {
						String datasetPath = (String)((Map)row.getData()).get("import_file_path");
						multiImportFileNames.add(datasetPath);
					}
				
					// Ask user before proceeding.
					MessageBox messageBox = new MessageBox(parent.getShell(),
							SWT.ICON_QUESTION | SWT.YES | SWT.NO);
					messageBox.setText("Import and save");
					messageBox.setMessage("Do you want to import and save " + 
							multiImportFileNames.size() + 
							((multiImportFileNames.size() > 1)? " files " : " file ") +
							"to the database? \nNote: Old versions of datasets will be replaced. " +
							" \n\n(Target ZIP path: \\\\winfs\\data\\prodkap\\sharkweb\\SHARKdata_datasets)");
					if (messageBox.open() != SWT.YES) {
						SharkAdmMainStatusBar.setField5("");
						return;
					}					
//					// Login to database, if not already done.
//					if (!DbConnect.instance().isConnected()) {
//						DbLoginDialogUi.display(parent.getDisplay()); 
//						DatabaseFacade.instance().connectionStatusChanged();
//					}					
					
					// Always select db and enter password. 
					DbConnect.instance().closeConnection();
					DbLoginDialogUi.display(parent.getDisplay()); 
					DatabaseFacade.instance().connectionStatusChanged();
					
					if (!DbConnect.instance().isConnected()) {
						SharkAdmMainStatusBar.setField5("");
						return;
					}
					
					// Use progress bar.
					ProgressMonitorDialog progress = new ProgressMonitorDialog(parent.getShell());
					try {
						progress.run(true /*fork*/, true /*cancelable*/, new IRunnableWithProgress() {
							public void run(IProgressMonitor progressMonitor)
									throws InvocationTargetException, InterruptedException {
								progressMonitor.beginTask("Importing dataset packages", multiImportFileNames.size());
								
								try {

									// Loop through user selected files.
									for (int i = 0; i < multiImportFileNames.size(); i++) {
										
										if (multiImportFileNames.get(i) != null) {
											progressMonitor.subTask("Importing (" + (i+1) + " of " + 
																	multiImportFileNames.size() + "): " +
																	multiImportFileNames.get(i) + "...");
	//										progressMonitor.subTask("Importing " + multiImportFileNames[i] + "...");
											if (progressMonitor.isCanceled()) {
												progressMonitor.done();
												return;
											}
											progressMonitor.worked(1);
	
											// "syncExec" is needed when ui tread is accessed.  
											multiImportFile = new File(multiImportFileNames.get(i)).getAbsolutePath();
											parent.getDisplay().syncExec(new Runnable() {
												public void run() {
													// Import one file.
													SharkAdmMainStatusBar.setField5("Busy: Importing file.");
													// Update screen.
													parent.update();
													try { Thread.sleep(10); } catch (Exception e) { }	
													
													ModelFacade.instance().clearMemoryModel();				
													ImportFacade.instance().importDatasetPackage(multiImportFile, System.out);
													
													// Save one dataset as ZIP file.
													int numberInMemoryModelforZip = ModelTopNode.instance().getDatasetList().size();
													if (numberInMemoryModelforZip > 0) {
														SharkAdmMainStatusBar.setField5("Busy: Updating SHARK archive file(s).");
														//SaveModel.instance().beginTransaction();
														java.util.List<Dataset> datasetList = ModelTopNode.instance().getDatasetList();
														for (Dataset dataset : datasetList) {
														
															
															String dataset_status = dataset.getField("dataset.status");
															if (dataset_status.equals("prod")) {
																// Only write to prod zip folder if dataset.status=prod.
																Boolean testZip = false;
																ZipArchiveFacade.instance().updateZipArchiveFile(testZip);
															}
														
														
														}
														// Done.
														SharkAdmMainStatusBar.setField5("");							
													}
													SharkAdmMainStatusBar.setField5("");
		
													// Save one dataset to database.
													SharkAdmMainStatusBar.setField5("Busy: Checks before saving.");
													// Update screen.
													parent.update();
													try { Thread.sleep(10); } catch (Exception e) { }	
		
													if (DbConnect.instance().isConnected()) {
														int numberInMemoryModel = ModelTopNode.instance().getDatasetList().size();
														if (numberInMemoryModel > 0) {
															SharkAdmMainStatusBar.setField5("Busy: Saving to database.");
															
															java.util.List<Dataset> datasetList = ModelTopNode.instance().getDatasetList();
															for (Dataset dataset : datasetList) {
																
																
																String dataset_status = dataset.getField("dataset.status");
																if (!dataset_status.equals("prod")) {
																	String databaseServer = DbConnect.instance().getDatabaseServer();
																	if (databaseServer.equals("er-postgresql.smhi.se")) {
//																	if (databaseServer.equals("localhost")) {
																	// Only write to prod database if dataset.status=prod.
																		continue;
																	}
																}

																
																// Use long transaction.
																try {
																	DbConnect.instance().beginTransaction();
																
																		int oid = GetChildren.instance().getDatasetUseVersionFilter(dataset.getField("dataset.dataset_file_name"));
																		if (oid == -1) {
																			dataset.Accept(new SaveModelToDatabase());								
																		} else {
																			// Don't ask if old dataset is removed.
																			DeleteModel.instance().deleteDataset(oid);
																			dataset.Accept(new SaveModelToDatabase());
																		}
																		
																		DbConnect.instance().commitTransaction();								
																
																} catch (Exception Exception) {									
															        try {
															        	DbConnect.instance().rollbackTransaction();
															        	System.out.println("Transaction rollback occured.");
																	} catch (SQLException e2) {
																		System.out.println("Rollback transaction failed");
																	}

																} finally {
															    	try {
																		DbConnect.instance().endTransaction();
																	} catch (SQLException e3) {
																		System.out.println("End transaction failed");
																	}
															    }
															}
														}
													}													
												}
											});
										} 
									}
								
								} catch (Exception Exception) {									
//							        
									try {
								        // Asynchronous call needed to view MessageBox.   
								        parentDisplay.getDisplay().asyncExec(new Runnable() {
											public void run() {
												try {
													MessageBox messageBox = new MessageBox(parent.getShell(),
															SWT.ICON_QUESTION | SWT.YES);
													messageBox.setText("Failed to write to database");
													messageBox.setMessage("Failed to write to database. Last processed dataset not saved to database.");
													messageBox.open();
												} catch (Exception e) {
													System.out.println("Message box failed: " + e.getMessage());
													e.printStackTrace();
												}
											}
										});
									} catch (Exception e) {
										System.out.println("Message box failed (asyncExec): " + e.getMessage());
										e.printStackTrace();
									}
							    }									

								// Vacuum cleaning needed after delete/insert.
								try {
									System.out.println("Vacuum...");
									DbConnect.instance().psqlVacuum();
									System.out.println("Vacuum done.");
								} catch (Exception vacuum_error) {
									System.out.println("Vacuum failed: " + vacuum_error.getMessage());
								}

								progressMonitor.done();
								ModelFacade.instance().clearMemoryModel();				
							}
						});
					} catch (InvocationTargetException e1) {
						e1.printStackTrace();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				// Done.
				DatabaseFacade.instance().dataChanged();
		    	SharkAdmMainStatusBar.setField5("");
			}
		};
		importCreateZipSaveToDbButton.addListener(SWT.Selection, listener);
	
//		// Listener for exportZipToSharkdata.
//		listener = new Listener() {
//			public void handleEvent(Event event) {
//				SharkAdmMainStatusBar.setField5("");
////				int numberInMemoryModel = ModelTopNode.instance().getDatasetList().size();
////				if (numberInMemoryModel > 0) {
//					MessageBox messageBox = new MessageBox(parent.getShell(),
//							SWT.ICON_QUESTION | SWT.YES | SWT.NO);
//					messageBox.setText("Copy ZIP files to SHARKdata");
//					messageBox.setMessage("Do you want to Copy ZIP files to SHARKdata?" +
//							" \n\n(Source ZIP path: \\\\winfs\\data\\prodkap\\sharkweb\\SHARKdata_datasets)");
//					if (messageBox.open() == SWT.YES) {
//						SharkAdmMainStatusBar.setField5("Busy: Copying to SHARKdata.");						
//
//						ZipArchiveFacade.instance().copyZipFilesToSharkDataOverSftp();
//
//						SharkAdmMainStatusBar.setField5("");							
//					}
////				} else {
////					MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_WARNING | SWT.OK);
////					messageBox.setText("Warning");
////					messageBox.setMessage("No items in memory model.");
////					messageBox.open();
////				}
//				SharkAdmMainStatusBar.setField5("");
//			}
//		};
//		exportZipToSharkdata.addListener(SWT.Selection, listener);
		
		// Listener for screeningButton
		listener = new Listener() {
			public void handleEvent(Event event) {
				SharkAdmMainStatusBar.setField5("Busy: Screening memory model.");
				logWindow.setText("");

				// Ask user before proceeding.
				MessageBox messageBox = new MessageBox(parent.getShell(),
						SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				messageBox.setText("Screening");
				messageBox.setMessage("Do you want to screen the whole database for duplicate samples?");
				if (messageBox.open() != SWT.YES) {
					SharkAdmMainStatusBar.setField5("");
					return;
				}					
			
				// Update screen.
				parent.update();
				try { Thread.sleep(10); } catch (Exception e) { }
				// Import.
				
				// Login to database.
				DbConnect.instance().closeConnection();
				DbLoginDialogUi.display(parent.getDisplay()); 
				DatabaseFacade.instance().connectionStatusChanged();
				
				if (!DbConnect.instance().isConnected()) {
					SharkAdmMainStatusBar.setField5("");
					return;
				}

				ImportFacade.instance().performScreening(null, logPrintStream);
				SharkAdmMainStatusBar.setField5("");
			}
		};
		screeningButton.addListener(SWT.Selection, listener);

		
		// Listener for openLogFileButton
		listener = new Listener() {
			public void handleEvent(Event event) {
				
				Program.launch("shark_adm_log.txt");
				
//				try {
//					Desktop.getDesktop().open(new File("shark_adm_log.txt"));
//				} catch (IOException e) {
//					// Try with the SWT alternative instead. 
//					Program.launch("shark_adm_log.txt");
//				}
			};
		};
		openLogFileButton.addListener(SWT.Selection, listener);
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
	}
}

class ImportDatasetsLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	public Image getColumnImage(Object arg0, int arg1) {
		return null;
	}

	public String getColumnText(Object modelElement, int columnIndex) {
		
		Map<String, String> map = (Map<String, String>) modelElement;
		if (columnIndex == 0) {
			return (String) map.get("dataset_package_name");
		} else  if (columnIndex == 1) {
			return "-";
		} else  if (columnIndex == 2) {
			return "-";
		} else  if (columnIndex == 3) {
			return (String) map.get("import_file_path");
		} else {
			return "";
		}
	}
}

