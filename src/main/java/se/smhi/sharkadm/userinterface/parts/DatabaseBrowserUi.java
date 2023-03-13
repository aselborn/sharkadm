/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.userinterface.parts;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import se.smhi.sharkadm.database.DbConnect;
import se.smhi.sharkadm.database.DeleteModel;
import se.smhi.sharkadm.database.ReadDataset;
import se.smhi.sharkadm.database.SaveSettings;
import se.smhi.sharkadm.database.SaveSpecies;
import se.smhi.sharkadm.database.SaveVisitLocations;
import se.smhi.sharkadm.datasets.fileimport.SingleFileImport;
import se.smhi.sharkadm.facades.DatabaseFacade;
import se.smhi.sharkadm.facades.ModelFacade;
import se.smhi.sharkadm.fileimport.misc.FileImportSettings;
import se.smhi.sharkadm.location.VisitLocationManager;
import se.smhi.sharkadm.location.VisitLocationManager_TEST;
import se.smhi.sharkadm.settings.SettingsManager;
import se.smhi.sharkadm.species_old.TaxonManager;
import se.smhi.sharkadm.userinterface.SharkAdmMainStatusBar;
import se.smhi.sharkadm.userinterface.SharkAdmMainUiSettings;

public class DatabaseBrowserUi implements Observer {

	private DatasetLabelProvider labelProvider = new DatasetLabelProvider();
	private TableViewer importedItemsTableViewer;
	private static SharkAdmMainUiSettings settings = SharkAdmMainUiSettings.instance();
	private Composite parent;
	
	private final Text serverText;
	private final Text databaseText;
	private final Text userText;
	private final Text dbSpeciesNumberText;
	private final Text dbStationNumberText;
	private final Text dbSettingsNumberText;
	
	// Used in drag-n-drop and ProgressMonitorDialog.
///	private static PrintStream logPrintStream;
	DropTargetEvent dropTargetEvent;
	String dragNDropFile;

	public DatabaseBrowserUi(final Composite parent, MenuItem topMenuItem) {
		this.parent = parent;
		DatabaseFacade.instance().addObserver(this);

		// ========== Sub menu ==========
		
		Menu importSubMenu = new Menu(parent.getShell(), SWT.DROP_DOWN);
		topMenuItem.setMenu(importSubMenu);
		
		MenuItem connectMenuItem = new MenuItem(importSubMenu, SWT.CASCADE);
		connectMenuItem.setText("&Connect to database");
		MenuItem disconnectMenuItem = new MenuItem(importSubMenu, SWT.CASCADE);
		disconnectMenuItem.setText("&Disconnect");
		
		// ========== Widget declarations ==========
				
		// Declaration of itemsInDbGroup.
		Group itemsInDbGroup = new Group(parent, SWT.NONE);
		itemsInDbGroup.setText("Deliveries in database");

		// Declaration of importedItemsTable.
		importedItemsTableViewer = new TableViewer(
				itemsInDbGroup, SWT.BORDER | SWT.MULTI
						| SWT.FULL_SELECTION);
		final Table importedItemsTable = importedItemsTableViewer.getTable();
		importedItemsTable.setHeaderVisible(true);
		importedItemsTable.setLinesVisible(true);

		String[] columnNames = new String[] { 
				"Dataset package name", 
				"Import time", 
				"Import format",
				"Import status",
				"Matrix column",
				"Reported by",
				"Imported by", 
				"File path", 
				"File name" };
		int[] columnWidths = new int[] { 
				220, 140, 100, 100, 100, 100, 100, 300, 100 };
		int[] columnAlignments = new int[] { 
				SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT };
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn column = new TableColumn(importedItemsTable, 
					columnAlignments[i]);
			column.setText(columnNames[i]);
			column.setWidth(columnWidths[i]);
		}

		importedItemsTableViewer.setLabelProvider(labelProvider);
		importedItemsTableViewer.setContentProvider(new ArrayContentProvider());
		
		Object datasetNoteArray[] = ReadDataset.instance().readDatasetListTable().toArray();
		importedItemsTableViewer.setInput(datasetNoteArray);
	    
		// Declaration of removeFromDbButton.
		Button removeFromDbButton = new Button(itemsInDbGroup, SWT.PUSH);
		removeFromDbButton.setText("Remove marked row(s) from database...");

		// Declaration of dragNDropHintLabel.
		Label dragNDropHintLabel = new Label(itemsInDbGroup, SWT.LEFT);
//		dragNDropHintLabel.setText("Hint: It is possible to drag-n-drop zipped dataset packages to the area above.");
		
		// === Declaration of dbConnectNoteGroup. ===
		Group dbConnectNoteGroup = new Group(parent, SWT.NONE);
		dbConnectNoteGroup.setText("Database connection");

		Label serverLabel = new Label(dbConnectNoteGroup, SWT.LEFT);
		serverLabel.setText("Database server:");
		
		Label databaseLabel = new Label(dbConnectNoteGroup, SWT.LEFT);
		databaseLabel.setText("Database:");
		
		Label userLabel = new Label(dbConnectNoteGroup, SWT.LEFT);
		userLabel.setText("Username:");

		serverText = new Text(dbConnectNoteGroup, SWT.LEFT | SWT.BORDER | SWT.READ_ONLY);
		serverText.setText("");

		databaseText = new Text(dbConnectNoteGroup, SWT.LEFT | SWT.BORDER | SWT.READ_ONLY);
		databaseText.setText("");

		userText = new Text(dbConnectNoteGroup, SWT.LEFT | SWT.BORDER | SWT.READ_ONLY);
		userText.setText("");

		Button connectButton = new Button(dbConnectNoteGroup, SWT.PUSH);
		connectButton.setText("Connect to database...");

		Button disconnectButton = new Button(dbConnectNoteGroup, SWT.PUSH);
		disconnectButton.setText("Disconnect");
		
		// === Declaration of dbSpeciesGroup. ===
		Group dbSpeciesGroup = new Group(parent, SWT.NONE);
		dbSpeciesGroup.setText("Species in database");

		Label dbSpeciesNumberLabel = new Label(dbSpeciesGroup, SWT.LEFT);
		dbSpeciesNumberLabel.setText("Number of taxa:");
		
		dbSpeciesNumberText = new Text(dbSpeciesGroup, SWT.LEFT | SWT.BORDER | SWT.READ_ONLY);
		dbSpeciesNumberText.setText("");

		Button dbSaveSpeciesButton = new Button(dbSpeciesGroup, SWT.PUSH);
		dbSaveSpeciesButton.setText("Save species to database");
		
		Button dbRemoveSpeciesButton = new Button(dbSpeciesGroup, SWT.PUSH);
		dbRemoveSpeciesButton.setText("Remove");

		// === Declaration of dbStationGroup. ===
		Group dbStationGroup = new Group(parent, SWT.NONE);
		dbStationGroup.setText("Visit locations in database");

		Label dbStationNumberLabel = new Label(dbStationGroup, SWT.LEFT);
		dbStationNumberLabel.setText("Number of locations:");
		
		dbStationNumberText = new Text(dbStationGroup, SWT.LEFT | SWT.BORDER | SWT.READ_ONLY);
		dbStationNumberText.setText("");

		Button dbSaveStationButton = new Button(dbStationGroup, SWT.PUSH);
		dbSaveStationButton.setText("Add missing locations");
		
		Button dbRemoveStationButton = new Button(dbStationGroup, SWT.PUSH);
		dbRemoveStationButton.setText("Remove");

		// === Declaration of dbSettingsGroup. ===
		Group dbSettingsGroup = new Group(parent, SWT.NONE);
		dbSettingsGroup.setText("Settings in database");

		Label dbSettingsNumberLabel = new Label(dbSettingsGroup, SWT.LEFT);
		dbSettingsNumberLabel.setText("Number of settings:");
		
		dbSettingsNumberText = new Text(dbSettingsGroup, SWT.LEFT | SWT.BORDER | SWT.READ_ONLY);
		dbSettingsNumberText.setText("");

		Button dbSaveSettingsButton = new Button(dbSettingsGroup, SWT.PUSH);
		dbSaveSettingsButton.setText("Save settings to database");
		
		Button dbRemoveSettingsButton = new Button(dbSettingsGroup, SWT.PUSH);
		dbRemoveSettingsButton.setText("Remove");


		// Maintenance.
		
		
		// === Declaration of dbMaintenanceGroup. ===
		Group dbMaintenanceGroup = new Group(parent, SWT.NONE);
		dbMaintenanceGroup.setText("Database maintenance");

		Button clearDbButton = new Button(dbMaintenanceGroup, SWT.PUSH);
		clearDbButton.setText("Remove all from db...");
		
		Button psqlVacuumButton = new Button(dbMaintenanceGroup, SWT.PUSH);
		psqlVacuumButton.setText("Clean up (PSQL-Vacuum)");
		
		// ==================== Layout ====================
		
		FormLayout formLayout;
		FormData formData;

		// Layout for parent composite.
		formLayout = new FormLayout();
		formLayout.marginWidth = 5;
		formLayout.marginHeight = 5;
		formLayout.spacing = 5;
		parent.setLayout(formLayout);

		// Layout for itemsInDbGroup.
		formLayout = new FormLayout();
		formLayout.marginWidth = 5;
		formLayout.marginHeight = 5;
		formLayout.spacing = 5;
		itemsInDbGroup.setLayout(formLayout);
		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(dbConnectNoteGroup, -2);
		formData.bottom = new FormAttachment(100, -5);
		itemsInDbGroup.setLayoutData(formData);

		// Layout for importedItemsTable.
		formLayout = new FormLayout();
		formLayout.marginWidth = 5;
		formLayout.marginHeight = 5;
		formLayout.spacing = 5;
		importedItemsTable.setLayout(formLayout);
		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(100, 0);
		formData.bottom = new FormAttachment(removeFromDbButton, 0);
		importedItemsTable.setLayoutData(formData);

		// Layout for removeFromDbButton.
		formData = new FormData();
//		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(100, 0);
		formData.bottom = new FormAttachment(100, 0);
		removeFromDbButton.setLayoutData(formData);

		// Layout for dragNDropHintLabel.
		formData = new FormData();
		formData.top = new FormAttachment(removeFromDbButton, -22);
		formData.left = new FormAttachment(0, 5);
		dragNDropHintLabel.setLayoutData(formData);
		
		//  Layout for dbConnectNoteGroup.
		formLayout = new FormLayout();
		formLayout.marginWidth = 5;
		formLayout.marginHeight = 5;
		formLayout.spacing = 5;
		dbConnectNoteGroup.setLayout(formLayout);
		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(70, 0);
		formData.right = new FormAttachment(100, -2);
//		formData.bottom = new FormAttachment(100, -5);
		dbConnectNoteGroup.setLayoutData(formData);

		// Layout for serverLabel.
		formData = new FormData();
		formData.left = new FormAttachment(0, 5);
		formData.top = new FormAttachment(serverText, -22);
		serverLabel.setLayoutData(formData);

		// Layout for databaseLabel.
		formData = new FormData();
		formData.left = new FormAttachment(0, 5);
		formData.top = new FormAttachment(databaseText, -22);
		databaseLabel.setLayoutData(formData);

		// Layout for userLabel.
		formData = new FormData();
		formData.left = new FormAttachment(0, 5);
		formData.top = new FormAttachment(userText, -22);
		userLabel.setLayoutData(formData);

		
		// Layout for serverText.
		formData = new FormData();
		formData.left = new FormAttachment(serverLabel, 5);
		formData.right = new FormAttachment(100, 0);
		formData.top = new FormAttachment(0, 5);
		serverText.setLayoutData(formData);

		// Layout for databaseText.
		formData = new FormData();
		formData.left = new FormAttachment(serverLabel, 5);
		formData.right = new FormAttachment(100, 0);
		formData.top = new FormAttachment(serverText, 0);
		databaseText.setLayoutData(formData);

		// Layout for userText.
		formData = new FormData();
		formData.left = new FormAttachment(serverLabel, 5);
		formData.right = new FormAttachment(100, 0);
		formData.top = new FormAttachment(databaseText, 0);
		userText.setLayoutData(formData);

		// Layout for connectButton;
		formData = new FormData();
		formData.top = new FormAttachment(userText, 5);
		formData.right = new FormAttachment(100, 0);
		connectButton.setLayoutData(formData);

		// Layout for disconnectButton;
		formData = new FormData();
		formData.top = new FormAttachment(userText, 5);
		formData.right = new FormAttachment(connectButton, 0);
		disconnectButton.setLayoutData(formData);
		
		// Layout for dbSpeciesGroup.
		formLayout = new FormLayout();
		formLayout.marginWidth = 5;
		formLayout.marginHeight = 5;
		formLayout.spacing = 5;
		dbSpeciesGroup.setLayout(formLayout);
		formData = new FormData();
		formData.top = new FormAttachment(dbConnectNoteGroup, 0);
		formData.left = new FormAttachment(70, 0);
		formData.right = new FormAttachment(100, -2);
		dbSpeciesGroup.setLayoutData(formData);

		// Layout for dbSpeciesNumberLabel.
		formData = new FormData();
		formData.left = new FormAttachment(0, 5);
		formData.top = new FormAttachment(dbSpeciesNumberText, -22);
		dbSpeciesNumberLabel.setLayoutData(formData);

		// Layout for dbSpeciesNumberText.
		formData = new FormData();
		formData.left = new FormAttachment(dbSpeciesNumberLabel, 5);
		formData.right = new FormAttachment(100, 0);
		formData.top = new FormAttachment(0, 5);
		dbSpeciesNumberText.setLayoutData(formData);

		// Layout for dbSaveSpeciesButton;
		formData = new FormData();
		formData.top = new FormAttachment(dbSpeciesNumberText, 5);
		formData.right = new FormAttachment(100, 0);
		dbSaveSpeciesButton.setLayoutData(formData);
		
		// Layout for dbRemoveSpeciesButton;
		formData = new FormData();
		formData.top = new FormAttachment(dbSpeciesNumberText, 5);
		formData.right = new FormAttachment(dbSaveSpeciesButton, 0);
		dbRemoveSpeciesButton.setLayoutData(formData);
		
		// Layout for dbStationGroup.
		formLayout = new FormLayout();
		formLayout.marginWidth = 5;
		formLayout.marginHeight = 5;
		formLayout.spacing = 5;
		dbStationGroup.setLayout(formLayout);
		formData = new FormData();
		formData.top = new FormAttachment(dbSpeciesGroup, 0);
		formData.left = new FormAttachment(70, 0);
		formData.right = new FormAttachment(100, -2);
		dbStationGroup.setLayoutData(formData);

		// Layout for dbStationNumberLabel.
		formData = new FormData();
		formData.left = new FormAttachment(0, 5);
		formData.top = new FormAttachment(dbStationNumberText, -22);
		dbStationNumberLabel.setLayoutData(formData);

		// Layout for dbStationNumberText.
		formData = new FormData();
		formData.left = new FormAttachment(dbStationNumberLabel, 5);
		formData.right = new FormAttachment(100, 0);
		formData.top = new FormAttachment(0, 5);
		dbStationNumberText.setLayoutData(formData);

		// Layout for dbSaveStationButton;
		formData = new FormData();
		formData.top = new FormAttachment(dbStationNumberText, 5);
		formData.right = new FormAttachment(100, 0);
		dbSaveStationButton.setLayoutData(formData);
		
		// Layout for dbSaveStationButton;
		formData = new FormData();
		formData.top = new FormAttachment(dbStationNumberText, 5);
		formData.right = new FormAttachment(dbSaveStationButton, 0);
		dbRemoveStationButton.setLayoutData(formData);

		// Layout for dbSettingsGroup.
		formLayout = new FormLayout();
		formLayout.marginWidth = 5;
		formLayout.marginHeight = 5;
		formLayout.spacing = 5;
		dbSettingsGroup.setLayout(formLayout);
		formData = new FormData();
		formData.top = new FormAttachment(dbStationGroup, 0);
		formData.left = new FormAttachment(70, 0);
		formData.right = new FormAttachment(100, -2);
		dbSettingsGroup.setLayoutData(formData);

		// Layout for dbSettingsNumberLabel.
		formData = new FormData();
		formData.left = new FormAttachment(0, 5);
		formData.top = new FormAttachment(dbSettingsNumberText, -22);
		dbSettingsNumberLabel.setLayoutData(formData);

		// Layout for dbSettingsNumberText.
		formData = new FormData();
		formData.left = new FormAttachment(dbSettingsNumberLabel, 5);
		formData.right = new FormAttachment(100, 0);
		formData.top = new FormAttachment(0, 5);
		dbSettingsNumberText.setLayoutData(formData);

		// Layout for dbSaveSettingsButton;
		formData = new FormData();
		formData.top = new FormAttachment(dbSettingsNumberText, 5);
		formData.right = new FormAttachment(100, 0);
		dbSaveSettingsButton.setLayoutData(formData);
		
		// Layout for dbSaveSettingsButton;
		formData = new FormData();
		formData.top = new FormAttachment(dbSettingsNumberText, 5);
		formData.right = new FormAttachment(dbSaveSettingsButton, 0);
		dbRemoveSettingsButton.setLayoutData(formData);
		
		// Layout for dbMaintenanceGroup.
		formLayout = new FormLayout();
		formLayout.marginWidth = 5;
		formLayout.marginHeight = 5;
		formLayout.spacing = 5;
		dbMaintenanceGroup.setLayout(formLayout);
		formData = new FormData();
		formData.top = new FormAttachment(dbSettingsGroup, 0);
		formData.left = new FormAttachment(70, 0);
		formData.right = new FormAttachment(100, -2);
		dbMaintenanceGroup.setLayoutData(formData);

		// Layout for clearDbButton;
		formData = new FormData();
		formData.top = new FormAttachment(0, 5);
		formData.right = new FormAttachment(psqlVacuumButton, 0);
		clearDbButton.setLayoutData(formData);
		
		// Layout for psqlVacuumButton;
		formData = new FormData();
		formData.top = new FormAttachment(0, 5);
		formData.right = new FormAttachment(100, 0);
		psqlVacuumButton.setLayoutData(formData);
		
//		// ========== Drag and drop ========== 
//
//		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT; 
//		DropTarget target = new DropTarget(importedItemsTable, operations); 
//		
//		// Receive data in file format. 
//		final FileTransfer fileTransfer = FileTransfer.getInstance(); 
//		Transfer[] transfers = new Transfer[] {fileTransfer}; 
//		target.setTransfer(transfers); 
//		
//		target.addDropListener(new DropTargetListener() { 
//			public void dragEnter(DropTargetEvent event) { 
//				if (event.detail == DND.DROP_DEFAULT) { 
//					if ((event.operations & DND.DROP_COPY) != 0) { 
//						event.detail = DND.DROP_COPY; 
//					} else { 
//						event.detail = DND.DROP_NONE; 
//					} 
//				} 
//     
//				// will accept text but prefer to have files dropped 
//				for (int i = 0; i < event.dataTypes.length; i++) { 
//					if (fileTransfer.isSupportedType(event.dataTypes[i])){ 
//						event.currentDataType = event.dataTypes[i]; 
//						// files should only be copied 
//						if (event.detail != DND.DROP_COPY) { 
//							event.detail = DND.DROP_NONE; 
//						} 
//						break; 
//					} 
//				} 
//			} 
//			public void dragOver(DropTargetEvent event) { 
//			} 
//			public void dragOperationChanged(DropTargetEvent event) { 
//				if (event.detail == DND.DROP_DEFAULT) { 
//					if ((event.operations & DND.DROP_COPY) != 0) { 
//						event.detail = DND.DROP_COPY; 
//					} else { 
//						event.detail = DND.DROP_NONE; 
//					} 
//				} 
//				// allow text to be moved but files should only be copied 
//				if (fileTransfer.isSupportedType(event.currentDataType)){ 
//					if (event.detail != DND.DROP_COPY) { 
//						event.detail = DND.DROP_NONE; 
//					} 
//				} 
//			} 
//			public void dragLeave(DropTargetEvent event) { 
//			}
//
//			public void dropAccept(DropTargetEvent event) { 
//			}
//			public void drop(DropTargetEvent event) { 
//
//				// Ask user before proceeding.
//				MessageBox messageBox = new MessageBox(parent.getShell(),
//						SWT.ICON_QUESTION | SWT.YES | SWT.NO);
//				messageBox.setText("Save Memory Model to Database");
//				messageBox.setMessage("Do you want to import and save " + 
//						((String[]) event.data).length + 
//						((((String[]) event.data).length > 1)? " files " : " file ") +
//						"to the database? ");
//				if (messageBox.open() != SWT.YES) {
//					return;
//				}
//
//				// Login to database, if not already done.
//				if (!DbConnect.instance().isConnected()) {
//					LoginDialogUi.display(parent.getDisplay()); 
//					DatabaseFacade.instance().connectionStatusChanged();
//				}
//				if (!DbConnect.instance().isConnected()) {
//					return;
//				}
//
//				dropTargetEvent = event;
//				// Use progress bar.
//				ProgressMonitorDialog progress = new ProgressMonitorDialog(parent.getShell());
//				try {
//					progress.run(true /*fork*/, true /*cancelable*/, new IRunnableWithProgress() {
//						public void run(IProgressMonitor progressMonitor)
//								throws InvocationTargetException, InterruptedException {
//							if (fileTransfer.isSupportedType(dropTargetEvent.currentDataType)){ 
//								String[] files = (String[])dropTargetEvent.data; 
//								progressMonitor.beginTask("Importing dataset packages", files.length);
//								
//								for (int i = 0; i < files.length; i++) { 
//									if (files[i] != null) {
//										progressMonitor.subTask("Importing " + (new File(files[i])).getName() + "...");
////										progressMonitor.subTask("Importing " + files[i] + "...");
//										if (progressMonitor.isCanceled()) {
//											progressMonitor.done();
//											return;
//										}
//										
//										// "syncExec" is needed when ui tread is accessed.  
//										dragNDropFile = files[i];
//										parent.getDisplay().syncExec(new Runnable() {
//											public void run() {
//												// Import one file.
//												SharkAdmMainStatusBar.setField5("Busy: Importing file.");
//												// Update screen.
//												parent.update();
//												try { Thread.sleep(10); } catch (Exception e) { }	
//												
//												ModelFacade.instance().clearMemoryModel();				
//												ImportFacade.instance().importDatasetPackage(dragNDropFile, System.out);
//
//												// Save one dataset to database.
//												SharkAdmMainStatusBar.setField5("Busy: Checks before saving.");
//												// Update screen.
//												parent.update();
//												try { Thread.sleep(10); } catch (Exception e) { }	
//
//												if (DbConnect.instance().isConnected()) {
//													int numberInMemoryModel = ModelTopNode.instance().getDatasetList().size();
//													if (numberInMemoryModel > 0) {
//															SharkAdmMainStatusBar.setField5("Busy: Saving to database.");
//															
////															Db.instance().beginTransaction();
//															java.util.List<Dataset> datasetList = ModelTopNode.instance().getDatasetList();
//															for (Dataset dataset : datasetList) {
//																int oid = GetChildren.instance().getDatasetUseVersionFilter(dataset.getField("dataset.dataset_file_name"));
//																if (oid == -1) {
//																	dataset.Accept(new SaveModelToDatabase());								
//																} else {
//																	MessageBox messageBox = new MessageBox(parent.getShell(),
//																			SWT.ICON_QUESTION | SWT.YES | SWT.NO);
//																	messageBox.setText("Dataset already exists in database");
//																	messageBox.setMessage("Do you want to replace the old version with " + dataset.getField("dataset.dataset_file_name") + "?");
//																	if (messageBox.open() == SWT.YES) {					
//																		DeleteModel.instance().deleteDataset(oid);
//																		dataset.Accept(new SaveModelToDatabase());
//																	}
//																}
//															}
////															SaveModel.instance().commit();
//															DatabaseFacade.instance().dataChanged();
//										
//															SharkAdmMainStatusBar.setField5("");							
//													}
//												}													
//												SharkAdmMainStatusBar.setField5("");
//											}
//										});
//										progressMonitor.worked(1);
//									}
//								}
//							}
//							progressMonitor.done();
//							ModelFacade.instance().clearMemoryModel();				
//						}
//					});
//				} catch (InvocationTargetException e1) {
//					e1.printStackTrace();
//				} catch (InterruptedException e1) {
//					e1.printStackTrace();
//				}
//			}
//		}); 

		// ========== Listeners ==========
		
		Listener listener;
				
		// Listener for removeFromDbButton.
		listener = new Listener() {
			public void handleEvent(Event event) {
				TableItem[] selectedRows = importedItemsTable.getSelection();
				
				MessageBox messageBox = new MessageBox(parent.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				messageBox.setText("Remove from database");
				messageBox.setMessage("Do you want to remove " + selectedRows.length + (selectedRows.length == 1? " item" : " items") + " from the database? ");
				if (messageBox.open() == SWT.YES) {
					SharkAdmMainStatusBar.setField5("Busy: Removing from database.");
					List<Integer> idList = new ArrayList<Integer>();
					for (TableItem row : selectedRows) {
						idList.add(Integer.parseInt((String)((Map)row.getData()).get("oid")));
					}
					// Don't ask gui-table due to observer updates. Use cache.
					for (Integer datasetNoteId : idList) {
						
						try {	
							DbConnect.instance().beginTransaction();

							DeleteModel.instance().deleteDataset(datasetNoteId);
							System.out.println("Import Info " + datasetNoteId + " is removed.");
					//		parent.update();
							
							DbConnect.instance().commitTransaction();
					
						} catch (Exception Exception) {									
					        try {
					        	DbConnect.instance().rollbackTransaction();
					        	System.out.println("Transaction rollback occured.");
					        	
								messageBox.setText("Failed to delete dataset from database");
								messageBox.setMessage("Failed to delete dataset from database. Database transaction rollback occured.");
								messageBox.open();

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
					SharkAdmMainStatusBar.setField5("");					
				}
			}
		};
		removeFromDbButton.addListener(SWT.Selection, listener);
	
	
		// Listener for connectButton.
		listener = new Listener() {
			public void handleEvent(Event event) {
				if (!DbConnect.instance().isConnected()) {
					DbLoginDialogUi.display(parent.getDisplay());
					DatabaseFacade.instance().connectionStatusChanged();
				}					
			}
		};
		connectButton.addListener(SWT.Selection, listener);
		connectMenuItem.addListener(SWT.Selection, listener);

		// Listener for disconnectButton.
		listener = new Listener() {
			public void handleEvent(Event event) {
				DbConnect.instance().closeConnection();
				DatabaseFacade.instance().connectionStatusChanged();
			}
		};
		disconnectButton.addListener(SWT.Selection, listener);
		disconnectMenuItem.addListener(SWT.Selection, listener);
		
		// Listener for dbSaveSpeciesButton.
		listener = new Listener() {
			public void handleEvent(Event event) {
				if (!DbConnect.instance().isConnected()) {
					DbLoginDialogUi.display(parent.getDisplay());
					DatabaseFacade.instance().connectionStatusChanged();
				}					
				SharkAdmMainStatusBar.setField5("Busy: Saving species to database.");
				
				try {	
					DbConnect.instance().beginTransaction();
				
						TaxonManager.instance().saveSpeciesToDatabase();				
						
						DbConnect.instance().commitTransaction();											
				} catch (Exception Exception) {									
			        try {
			        	DbConnect.instance().rollbackTransaction();
			        	System.out.println("Transaction rollback occured.");
			        	
			        	Exception.printStackTrace();
			        	
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
				
				DatabaseFacade.instance().dataChanged();
				SharkAdmMainStatusBar.setField5("");
			}
		};
		dbSaveSpeciesButton.addListener(SWT.Selection, listener);
		
		// Listener for dbRemoveSpeciesButton.
		listener = new Listener() {
			public void handleEvent(Event event) {
				if (!DbConnect.instance().isConnected()) {
					DbLoginDialogUi.display(parent.getDisplay());
					DatabaseFacade.instance().connectionStatusChanged();
				}					
				SharkAdmMainStatusBar.setField5("Busy: Removes species (taxon) from database.");
				
				try {	
					DbConnect.instance().beginTransaction();
				
					TaxonManager.instance().removeSpeciesInDatabase();
						
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

				
				
				
				DatabaseFacade.instance().dataChanged();
				SharkAdmMainStatusBar.setField5("");
			}
		};
		dbRemoveSpeciesButton.addListener(SWT.Selection, listener);

		// Listener for dbSaveStationButton.
		listener = new Listener() {
			public void handleEvent(Event event) {
				if (!DbConnect.instance().isConnected()) {
					DbLoginDialogUi.display(parent.getDisplay()); 
					DatabaseFacade.instance().connectionStatusChanged();
				}					
				SharkAdmMainStatusBar.setField5("Busy: Saving locations to database.");
				
//				VisitLocationManager.instance().saveVisitLocationsToDatabase();
				VisitLocationManager_TEST.instance().saveVisitLocationsToDatabase();
				
				DatabaseFacade.instance().dataChanged();
				SharkAdmMainStatusBar.setField5("");
			}
		};
		dbSaveStationButton.addListener(SWT.Selection, listener);
		
		// Listener for dbRemoveStationButton.
		listener = new Listener() {
			public void handleEvent(Event event) {
				if (!DbConnect.instance().isConnected()) {
					DbLoginDialogUi.display(parent.getDisplay()); 
					DatabaseFacade.instance().connectionStatusChanged();
				}					
				SharkAdmMainStatusBar.setField5("Busy: Removes locations from database.");
				
				try {	
					DbConnect.instance().beginTransaction();
				
					VisitLocationManager.instance().removeVisitLocationsInDatabase();
						
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

				
				
				DatabaseFacade.instance().dataChanged();
				SharkAdmMainStatusBar.setField5("");
			}
		};	
		dbRemoveStationButton.addListener(SWT.Selection, listener);

		// Listener for dbSaveSettingsButton.
		listener = new Listener() {
			public void handleEvent(Event event) {
				if (!DbConnect.instance().isConnected()) {
					DbLoginDialogUi.display(parent.getDisplay()); 
					DatabaseFacade.instance().connectionStatusChanged();
				}					
				SharkAdmMainStatusBar.setField5("Busy: Saving settings to database.");
				SettingsManager.instance().clearSettingsList();
				
				SingleFileImport fileImport = null;
				PrintStream dummyStream = null;
				try {
					dummyStream = new PrintStream(
					"shark_adm_log.txt");
//					"shark_adm_station_import_log.txt");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				fileImport = new FileImportSettings(dummyStream);
				fileImport.importFiles("DUMMY");
				ModelFacade.instance().modelChanged();

				try {	
					DbConnect.instance().beginTransaction();
				
					SettingsManager.instance().saveSettingsToDatabase();
					
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

				DatabaseFacade.instance().dataChanged();
				SharkAdmMainStatusBar.setField5("");
			}
		};
		dbSaveSettingsButton.addListener(SWT.Selection, listener);
		
		// Listener for dbRemoveSettingsButton.
		listener = new Listener() {
			public void handleEvent(Event event) {
				if (!DbConnect.instance().isConnected()) {
					DbLoginDialogUi.display(parent.getDisplay()); 
					DatabaseFacade.instance().connectionStatusChanged();
				}					
				SharkAdmMainStatusBar.setField5("Busy: Removes Settings from database.");
				
				try {	
					DbConnect.instance().beginTransaction();
				
					SettingsManager.instance().removeSettingsInDatabase();
						
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

				
				
				DatabaseFacade.instance().dataChanged();
				SharkAdmMainStatusBar.setField5("");
			}
		};	
		dbRemoveSettingsButton.addListener(SWT.Selection, listener);
		
		// Listener for clearDbButton.
		listener = new Listener() {
			public void handleEvent(Event event) {
				if (!DbConnect.instance().isConnected()) {
					DbLoginDialogUi.display(parent.getDisplay()); 
					DatabaseFacade.instance().connectionStatusChanged();
				}					
				MessageBox messageBox = new MessageBox(parent.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				messageBox.setText("Remove database content");
				messageBox.setMessage("Do you want to remove all data (including species, locations and settings) from the database? ");
				if (messageBox.open() == SWT.YES) {
					SharkAdmMainStatusBar.setField5("Busy: Removing database content.");
					DatabaseFacade.instance().clearDb();
					DatabaseFacade.instance().dataChanged();
					SharkAdmMainStatusBar.setField5("");
				}
			}
		};	
		clearDbButton.addListener(SWT.Selection, listener);

		// Listener for psqlVacuumButton.
		listener = new Listener() {
			public void handleEvent(Event event) {
				if (!DbConnect.instance().isConnected()) {
					DbLoginDialogUi.display(parent.getDisplay()); 
					DatabaseFacade.instance().connectionStatusChanged();
				}					
				SharkAdmMainStatusBar.setField5("Performs psql-vacuum command.");
				DatabaseFacade.instance().psqlVacuum();
				SharkAdmMainStatusBar.setField5("");
			}
		};	
		psqlVacuumButton.addListener(SWT.Selection, listener);
	}

	public void update(Observable arg0, Object arg1) {
		// "syncExec" is needed if the Observer is executed in a non-ui thread.
		parent.getDisplay().syncExec(new Runnable() {
			public void run() {
				if (DbConnect.instance().isConnected()) {
					serverText.setText(settings.getString("database_server", "localhost"));
					databaseText.setText(settings.getString("database", "sharkweb"));
					userText.setText(settings.getString("database_user", "postgres"));			
					dbSpeciesNumberText.setText(Integer.toString(SaveSpecies.instance().countSpecies()));
//					dbStationNumberText.setText(Integer.toString(SaveStations.instance().countStations()));
					dbStationNumberText.setText(Integer.toString(SaveVisitLocations.instance().countVisitLocations()));
					dbSettingsNumberText.setText(Integer.toString(SaveSettings.instance().countSettings()));
				} else {
					serverText.setText("");
					databaseText.setText("");
					userText.setText("");
					dbSpeciesNumberText.setText("");
					dbStationNumberText.setText("");
					dbSettingsNumberText.setText("");
				}
				
				Object datasetNoteArray[] = ReadDataset.instance().readDatasetListTable().toArray();
				importedItemsTableViewer.setInput(datasetNoteArray);
				if (DbConnect.instance().isConnected()) {
					SharkAdmMainStatusBar.setField2("Database: " + datasetNoteArray.length + " items");
				} else {
					SharkAdmMainStatusBar.setField2("Database: " + "disconnected");
				}
//				datasetViewer.resetDataset();
			}
		});
	}
}

class DatasetLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	public Image getColumnImage(Object arg0, int arg1) {
		return null;
	}

	public String getColumnText(Object modelElement, int columnIndex) {
		
		Map<String, String> map = (Map<String, String>) modelElement;
		if (columnIndex == 0) {
			return (String) map.get("dataset_package_name");
		} else  if (columnIndex == 1) {
			return (String) map.get("import_datetime");
		} else if (columnIndex == 2) {
			return (String) map.get("import_format");
		} else if (columnIndex == 3) {
			return (String) map.get("import_status");
		} else if (columnIndex == 4) {
			return (String) map.get("import_matrix_column");
		} else if (columnIndex == 5) {
			return (String) map.get("reported_by");
		} else  if (columnIndex == 6) {
			return (String) map.get("imported_by");
		} else  if (columnIndex == 7) {
			return (String) map.get("import_file_path");
		} else  if (columnIndex == 8) {
			return (String) map.get("import_file_name");
		} else {
			return "";
		}
	}
}
