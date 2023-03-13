/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.userinterface.parts;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import se.smhi.sharkadm.species_old.ExportSpeciesList;
import se.smhi.sharkadm.userinterface.SharkAdmMainStatusBar;

public class SpeciesBrowserUi {
	
	final SpeciesTreeViewerUi speciesTree;

	public SpeciesBrowserUi(final Composite parent, MenuItem topMenuItem) {

		// ========== Sub menu ==========
		
		Menu modelSubMenu = new Menu(parent.getShell(), SWT.DROP_DOWN);
		topMenuItem.setMenu(modelSubMenu);
		
		MenuItem modelTreeShowKingdomsItem = new MenuItem(modelSubMenu, SWT.CASCADE);
		modelTreeShowKingdomsItem.setText("&Show kingdoms");
		
		MenuItem modelTreeShowLevel2Item = new MenuItem(modelSubMenu, SWT.CASCADE);
		modelTreeShowLevel2Item.setText("&Show level 2");
		
		MenuItem modelTreeShowLevel3Item = new MenuItem(modelSubMenu, SWT.CASCADE);
		modelTreeShowLevel3Item.setText("&Show level 3");
		
		MenuItem modelTreeShowLevel4Item = new MenuItem(modelSubMenu, SWT.CASCADE);
		modelTreeShowLevel4Item.setText("&Show level 4");
		
		MenuItem modelTreeShowLevel5tem = new MenuItem(modelSubMenu, SWT.CASCADE);
		modelTreeShowLevel5tem.setText("&Show level 5");
		
		MenuItem modelTreeShowVariablesItem = new MenuItem(modelSubMenu, SWT.CASCADE);
		modelTreeShowVariablesItem.setText("&Show all levels");				
		
		// ========== Widget declarations ==========
		
		FormLayout formLayout = new FormLayout();
	    parent.setLayout(formLayout);		
	    	    
	    // Sash: Vertical delimiter.
		Sash verticalSash = new Sash(parent, SWT.VERTICAL);
	    final FormData verticalSashData = new FormData();
	    verticalSashData.left = new FormAttachment(50);
	    verticalSashData.top = new FormAttachment(0);
	    verticalSashData.bottom = new FormAttachment(100);
	    verticalSash.setLayoutData(verticalSashData);	    
	    verticalSash.addListener(SWT.Selection, new Listener() {
	        public void handleEvent(Event event) {
	            if (event.detail != SWT.DRAG) {
	            	verticalSashData.left =
                    new FormAttachment(100*event.x/parent.getBounds().width);
	            	parent.layout();
	            }
	        }
	    });

		// Composite: Navigator to the left.
	    Composite navigatorComposite = new Composite(parent, SWT.NONE);
	    
	    // TabFolder.
	    TabFolder navigatorTabFolder = new TabFolder(navigatorComposite, SWT.NONE);
	    	    
	    // Tab: Species tree.
	    TabItem taxonTreeTab = new TabItem(navigatorTabFolder, SWT.NONE);
	    taxonTreeTab.setText("Taxon tree");
	    Composite taxonTreeComposite = new Composite(navigatorTabFolder, SWT.NONE);
	    taxonTreeComposite.setLayout(new FillLayout());
	    taxonTreeTab.setControl(taxonTreeComposite);
	    	   	    
	    // SpeciesTreeViewer
		speciesTree = new SpeciesTreeViewerUi(taxonTreeComposite);

	    // Tab: Species table.
	    TabItem taxonTableTab = new TabItem(navigatorTabFolder, SWT.NONE);
	    taxonTableTab.setText("Taxon list");
	    Composite taxonTableComposite = new Composite(navigatorTabFolder, SWT.NONE);
	    taxonTableComposite.setLayout(new FillLayout());
	    taxonTableTab.setControl(taxonTableComposite);

	    // SpeciesTable Viewer
		SpeciesTableViewerUi speciesTable = new SpeciesTableViewerUi(taxonTableComposite);

		// Composite: Taxon item to the right.
	    Composite itemComposite = new Composite(parent, SWT.NONE);

		// Group: Taxon item.
		SpeciesItemViewerUi taxonItem = new SpeciesItemViewerUi(itemComposite);
		// Inform table and tree.
		speciesTable.setTaxonItem(taxonItem);
		speciesTree.setTaxonItem(taxonItem);

		// Declaration of speciesManagementGroup.
		Group speciesManagementGroup = new Group(parent, SWT.NONE);
		speciesManagementGroup.setText("Species Management");
		
		// Declaration of exportSpeciesListButton.
		Button exportSpeciesListButton = new Button(speciesManagementGroup, SWT.PUSH);
		exportSpeciesListButton.setText("Export species list...");

		// Declaration of openSpeciesListButton.
		Button openSpeciesListButton = new Button(speciesManagementGroup, SWT.PUSH);
		openSpeciesListButton.setText("Open species list (shark_species_list.txt)");

		// ==================== Layout ====================
		
		FormData formData;

		// Layout for navigatorComposite.
	    FillLayout navigatorLayout = new FillLayout();
	    navigatorLayout.marginWidth = 5;
	    navigatorLayout.marginHeight = 5;
	    navigatorLayout.spacing = 5;
	    navigatorComposite.setLayout(navigatorLayout);
		final FormData navigatorData = new FormData();
		navigatorData.left = new FormAttachment(0);
		navigatorData.right = new FormAttachment(verticalSash);
		navigatorData.top = new FormAttachment(0);
		navigatorData.bottom = new FormAttachment(100, -5);
		navigatorComposite.setLayoutData(navigatorData);
		
		// Layout for itemComposite.
	    FillLayout itemLayout = new FillLayout();
	    itemLayout.marginWidth = 5;
	    itemLayout.marginHeight = 5;
	    itemLayout.spacing = 5;
	    itemComposite.setLayout(itemLayout);
		final FormData itemData = new FormData();
		itemData.left = new FormAttachment(verticalSash);
		itemData.right = new FormAttachment(100, -5);
		itemData.top = new FormAttachment(0);
		itemData.bottom = new FormAttachment(speciesManagementGroup);
		itemComposite.setLayoutData(itemData);

	    // Layout for speciesManagementGroup.
		formLayout = new FormLayout();
		formLayout.marginWidth = 10;
		formLayout.marginHeight = 10;
		formLayout.spacing = 5;
		speciesManagementGroup.setLayout(formLayout);
		formData = new FormData();
//		formData.top = new FormAttachment(80);
		formData.left = new FormAttachment(verticalSash, 5);
		formData.right = new FormAttachment(100, -10);
		formData.bottom = new FormAttachment(100, -10);
		speciesManagementGroup.setLayoutData(formData);

		// Layout for exportSpeciesListButton.
		formData = new FormData();
		formData.top = new FormAttachment(0, 5);
		formData.right = new FormAttachment(openSpeciesListButton, -5);
		exportSpeciesListButton.setLayoutData(formData);		

		// Layout for openSpeciesListButton.
		formData = new FormData();
		formData.top = new FormAttachment(0, 5);
		formData.right = new FormAttachment(100, -5);
		openSpeciesListButton.setLayoutData(formData);		

	    // ========== Listeners ==========
		
		Listener listener;
				
		// Listener for modelTreeShowKingdomsItem.
		listener = new Listener() {
			public void handleEvent(Event event) {
				speciesTree.collapseAll();
				speciesTree.expandToLevel(1);
			}
		};		
		modelTreeShowKingdomsItem.addListener(SWT.Selection, listener);
		
		// Listener for modelTreeShowLevel2Item.
		listener = new Listener() {
			public void handleEvent(Event event) {
				speciesTree.collapseAll();
				speciesTree.expandToLevel(2);
			}
		};		
		modelTreeShowLevel2Item.addListener(SWT.Selection, listener);
		
		// Listener for modelTreeShowLevel3Item.
		listener = new Listener() {
			public void handleEvent(Event event) {
				speciesTree.collapseAll();
				speciesTree.expandToLevel(3);
			}
		};		
		modelTreeShowLevel3Item.addListener(SWT.Selection, listener);
		
		// Listener for modelTreeShowLevel4Item.
		listener = new Listener() {
			public void handleEvent(Event event) {
				speciesTree.collapseAll();
				speciesTree.expandToLevel(4);
			}
		};		
		modelTreeShowLevel4Item.addListener(SWT.Selection, listener);
		
		// Listener for modelTreeShowLevel5tem.
		listener = new Listener() {
			public void handleEvent(Event event) {
				speciesTree.collapseAll();
				speciesTree.expandToLevel(5);
			}
		};		
		modelTreeShowLevel5tem.addListener(SWT.Selection, listener);
		
		// Listener for modelTreeShowVariablesItem.
		listener = new Listener() {
			public void handleEvent(Event event) {
				speciesTree.expandAll();
			}
		};		
		modelTreeShowVariablesItem.addListener(SWT.Selection, listener);
		
		// Listener for exportSpeciesListButton.
		listener = new Listener() {
			public void handleEvent(Event event) {
				SharkAdmMainStatusBar.setField5("Busy: Exporting species to 'shark_species_list.txt'.");
				ExportSpeciesList.export(System.getProperty("user.dir"), "shark_species_list.txt");
				SharkAdmMainStatusBar.setField5("");
			}
		};
		exportSpeciesListButton.addListener(SWT.Selection, listener);
		
		// Listener for openSpeciesListButton.
		listener = new Listener() {
			public void handleEvent(Event event) {
				
				Program.launch("shark_species_list.txt");
				
//				try {
//					Desktop.getDesktop().open(new File("shark_species_list.txt"));
//				} catch (IOException e) {
//					// Try with the SWT alternative instead. 
//					Program.launch("shark_species_list.txt");
//				}
			};
		};
		openSpeciesListButton.addListener(SWT.Selection, listener);
	}
	
}
