/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.userinterface;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import se.smhi.sharkadm.datasets.fileimport.SingleFileImport;
import se.smhi.sharkadm.fileimport.misc.FileImportBvolNomp;
import se.smhi.sharkadm.fileimport.misc.FileImportDynamicTaxa;
import se.smhi.sharkadm.fileimport.misc.FileImportStations;
import se.smhi.sharkadm.fileimport.misc.FileImportTrophicType;
import se.smhi.sharkadm.userinterface.parts.DatabaseBrowserUi;
import se.smhi.sharkadm.userinterface.parts.ImportDatasetsUi;
import se.smhi.sharkadm.userinterface.parts.ModelViewerManagerUi;
import se.smhi.sharkadm.userinterface.parts.SpeciesBrowserUi;

/**
 *	This is the main window in the application<br/> 
 *	"Sharkadm - Administration of Marine Environmental Monitoring Data"<br/>
 *	<br/>
 *	The main window contains a menu bar and a set of tabs. Each tab content
 *	as well as menu items connected to the tab content  
 *	is located in the package "se.smhi.sharkadm.ui.swt.parts".<br/> 
 */
public class SharkAdmMainWindow {
	
//	static Browser browser = null; // HTML TEST.

	// Settings are used to store positions etc. during 
	private SharkAdmMainUiSettings settings = SharkAdmMainUiSettings.instance();

	public SharkAdmMainWindow()
	{
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	public void launchApp() {
		Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setText("SHARKadm - Administration of marine environmental monitoring data ");
		SharkAdmMainStatusBar.setField1("SharkAdm Version 2.3");
		// ========== Window position ==========
		
		// Position window at startup. Use default position if at least one corner is outside
		// actual client area. This will reduce problems with a temporary second screen.
		final Rectangle clientArea = shell.getMonitor().getClientArea();
		if ((clientArea.width < 1000) || (clientArea.height < 600)) {
			// If the screen is small then use the whole screen.
			shell.setBounds(0, 0, clientArea.width, clientArea.height);  
		}
		else if ((settings.getInt("main_window_left", -10) < -8 ) || 
				 (settings.getInt("main_window_left", 0) + settings.getInt("main_window_width", 0) > (clientArea.width + 8 )) || 
				 (settings.getInt("main_window_top", -10) < -8 ) || 
				 (settings.getInt("main_window_top", 0) + settings.getInt("main_window_height", 0) > (clientArea.height + 8))) {
			// Previous position was outside actual client area (multiple screens was probably used). Use default position.
			shell.setBounds(clientArea.width/2-500, clientArea.height/2-300, 1000, 600);
		} else {
			// Previous position was inside actual client area. Possible to used stored position.
			shell.setBounds(settings.getInt("main_window_left", clientArea.width/2-500), 
					settings.getInt("main_window_top", clientArea.height/2-300), 
					settings.getInt("main_window_width", 1000), 
					settings.getInt("main_window_height", 600));
		}

		FormLayout shellLayout = new FormLayout();
		shell.setLayout(shellLayout);

		// Store window positions when changed.
		Listener listener = new Listener() {
			public void handleEvent(Event e) {
				Rectangle bounds = shell.getBounds();
				settings.setInt("main_window_left", bounds.x);
				settings.setInt("main_window_top", bounds.y);
				settings.setInt("main_window_width", bounds.width);
				settings.setInt("main_window_height", bounds.height);
			}
		};	
		shell.addListener(SWT.Move, listener);
		shell.addListener(SWT.Resize, listener);
		
		// ========== Main menu ==========
		
		Menu bar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(bar);
		
		// File menu and sub menus.
		MenuItem fileMenu = new MenuItem(bar, SWT.CASCADE);
		fileMenu.setText("&File");
		
		Menu fileSubMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenu.setMenu(fileSubMenu);
		
		MenuItem fileExitItem = new MenuItem(fileSubMenu, SWT.CASCADE);
		fileExitItem.setText("&Exit");
		fileExitItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				shell.dispose();
			}
		});
		
		MenuItem importDatasetMenu = new MenuItem(bar, SWT.CASCADE);
		importDatasetMenu.setText("&Import dataset");

		MenuItem memoryModelMenu = new MenuItem(bar, SWT.CASCADE);
		memoryModelMenu.setText("&Memory model");

		MenuItem databaseMenu = new MenuItem(bar, SWT.CASCADE);
		databaseMenu.setText("&Database");

		MenuItem speciesMenu = new MenuItem(bar, SWT.CASCADE);
		speciesMenu.setText("&Species");
		
//		MenuItem importMenu = new MenuItem(bar, SWT.CASCADE);
//		importMenu.setText("&Import (OLD)");

		// Help menu and sub menus.
		MenuItem helpMenu = new MenuItem(bar, SWT.CASCADE);
		helpMenu.setText("&Help");
		Menu helpSubMenu = new Menu(shell, SWT.DROP_DOWN);
		helpMenu.setMenu(helpSubMenu);
		

		MenuItem helpAboutItem = new MenuItem(helpSubMenu, SWT.CASCADE);
		helpAboutItem.setText("&About...");
		helpAboutItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_INFORMATION | SWT.OK);
				messageBox.setText("About");
				messageBox.setMessage("SHARKadm - Administration of Swedish marine monitoring data.\nContact: shark@smhi.se");
				messageBox.open();
			}
		});
		
		// ========== TabFolder ==========

		TabFolder mainTabFolder = new TabFolder(shell, SWT.NONE);
		

		
		// === Tab: ImportManager ===
		TabItem importDatasetsTab = new TabItem(mainTabFolder, SWT.NONE);
		importDatasetsTab.setText("Import datasets");

	    Composite importComposite = new Composite(mainTabFolder, SWT.NONE);
	    importComposite.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
	    importDatasetsTab.setControl(importComposite);

	    new ImportDatasetsUi(importComposite, importDatasetMenu);
		

	    
		// === Tab: MemoryModelBrowser ===
		TabItem modelBrowserTab = new TabItem(mainTabFolder, SWT.NONE);
		modelBrowserTab.setText("Memory model browser");
	    Composite modelBrowserComposite = new Composite(mainTabFolder, SWT.NONE);
	    modelBrowserComposite.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
	    modelBrowserTab.setControl(modelBrowserComposite);
		
	    new ModelViewerManagerUi(modelBrowserComposite, memoryModelMenu);

		// === Tab: DatabaseBrowser ===
		TabItem databaseBrowserTab = new TabItem(mainTabFolder, SWT.NONE);
		databaseBrowserTab.setText("Database");
		
	    Composite databaseBrowserComposite = new Composite(mainTabFolder, SWT.NONE);
	    databaseBrowserComposite.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
	    databaseBrowserTab.setControl(databaseBrowserComposite);
		
	    new DatabaseBrowserUi(databaseBrowserComposite, databaseMenu);

//	    // === Tab: ImportManager ===
//		TabItem importManangerTab = new TabItem(mainTabFolder, SWT.NONE);
//		importManangerTab.setText("Import manager (old)");
//
//	    /* Composite */ importComposite = new Composite(mainTabFolder, SWT.NONE);
//	    importComposite.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
//	    importManangerTab.setControl(importComposite);
//
//	    new ImportManagerUi_OLD(importComposite, importMenu);
		
		// === Tab: SpeciesBrowser ===
		TabItem speciesBrowserTab = new TabItem(mainTabFolder, SWT.NONE);
		speciesBrowserTab.setText("Species browser");

	    Composite speciesBrowserComposite = new Composite(mainTabFolder, SWT.NONE);
	    speciesBrowserComposite.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
	    speciesBrowserTab.setControl(speciesBrowserComposite);
		
	    new SpeciesBrowserUi(speciesBrowserComposite, speciesMenu);

	    // === Active tab at startup ===
	    mainTabFolder.setSelection(0); // 0 = Import manager.
//	    mainTabFolder.setSelection(3); // 3 = Database.
//	    mainTabFolder.setSelection(4); // 4 = CreateZip.

	    // === Status bar ===
	    Composite statusBarComposite = new Composite(shell, SWT.NONE);
	    new SharkAdmMainStatusBar(statusBarComposite);
	    
	    // ========== Layout ========== 

	    FormData formData;
	    
	    // Layout for mainTabFolder.
	    formData = new FormData();
		formData.top = new FormAttachment(0, 5);
		formData.left = new FormAttachment(0, 5);
		formData.right = new FormAttachment(100, -5);
		formData.bottom = new FormAttachment(statusBarComposite, -2);
		mainTabFolder.setLayoutData(formData);

		// Layout for statusBar.
	    formData = new FormData();
		formData.top = new FormAttachment(100, -18);
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(100, 0);
		formData.bottom = new FormAttachment(100, -2);
		statusBarComposite.setLayoutData(formData);
		
	    // ========== Show main window ========== 

		shell.open();

	    // ========== Load data at startup ========== 

		ProgressMonitorDialog progress = new ProgressMonitorDialog(shell);
		try {
			progress.run(true /*fork*/, true /*cancelable*/, 
				new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor)
							throws InvocationTargetException, InterruptedException {
						monitor.beginTask("Loading data", 2);
						
						SingleFileImport fileImport = null;
						PrintStream dummyStream = null;
						try {
							dummyStream = new PrintStream("shark_adm_log.txt");
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}

						monitor.subTask("Loading stations...");
						fileImport = new FileImportStations(dummyStream);
						fileImport.importFiles(""); // Note: File not used.
						monitor.worked(1);
						
						if (monitor.isCanceled()) {
							dummyStream.close();
							return;
						}
						
						monitor.subTask("Loading species...");
						fileImport = new FileImportDynamicTaxa(dummyStream);
						
						// Trophic types.
						FileImportTrophicType fileImportTrophicType = new FileImportTrophicType(dummyStream);
						fileImportTrophicType.importFiles("");
						
						// Bvol.
						FileImportBvolNomp fileImportBvol = new FileImportBvolNomp(dummyStream);
						fileImportBvol.importFiles("");
						
						fileImport.importFiles(""); // Note: File not used.
				
						monitor.worked(1);
						
						dummyStream.close();
						monitor.done();
					}
				});
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
	    // ========== Main event loop ========== 

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
	    // === Close application === 
		settings.saveSettings();
		display.dispose();
	}

}
