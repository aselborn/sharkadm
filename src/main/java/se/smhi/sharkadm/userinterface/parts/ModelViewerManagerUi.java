/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.userinterface.parts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import se.smhi.sharkadm.facades.ModelFacade;

public class ModelViewerManagerUi {

	final ModelViewerTreeUi importModelTree;
	
	public ModelViewerManagerUi(final Composite parent, MenuItem topMenuItem) {

		// ========== Sub menu ==========
		
		Menu modelSubMenu = new Menu(parent.getShell(), SWT.DROP_DOWN);
		topMenuItem.setMenu(modelSubMenu);
		
		MenuItem clearMemoryModelMenuItem = new MenuItem(modelSubMenu, SWT.CASCADE);
		clearMemoryModelMenuItem.setText("&Clear memory model");
		
		new MenuItem(modelSubMenu, SWT.SEPARATOR);

		MenuItem modelTreeShowDeliveriesItem = new MenuItem(modelSubMenu, SWT.CASCADE);
		modelTreeShowDeliveriesItem.setText("&Show deliveries");
		
		MenuItem modelTreeShowVisitsItem = new MenuItem(modelSubMenu, SWT.CASCADE);
		modelTreeShowVisitsItem.setText("&Show visits");
		
		MenuItem modelTreeShowSamplesItem = new MenuItem(modelSubMenu, SWT.CASCADE);
		modelTreeShowSamplesItem.setText("&Show samples");
		
		MenuItem modelTreeShowVariablesItem = new MenuItem(modelSubMenu, SWT.CASCADE);
		modelTreeShowVariablesItem.setText("&Show variables");
		
		// ========== Memory model top level widgets and layout  ==========
		
		FormLayout formLayout = new FormLayout();
	    parent.setLayout(formLayout);		
	    
	    // Sash: Vertical delimiter.
		Sash verticalSash = new Sash(parent, SWT.VERTICAL);
	    final FormData verticalSashData = new FormData();
	    verticalSashData.left = new FormAttachment(60);
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
	    
	    // TabFolder.
	    TabFolder navigatorTabFolder = new TabFolder(navigatorComposite, SWT.NONE);
	    	    
	    // Tab: Model element tree.
	    TabItem importModelTreeTab = new TabItem(navigatorTabFolder, SWT.NONE);
	    importModelTreeTab.setText("Memory model tree");
	    Composite importModelTreeComposite = new Composite(navigatorTabFolder, SWT.NONE);
	    importModelTreeComposite.setLayout(new FillLayout());
	    importModelTreeTab.setControl(importModelTreeComposite);
	    	   	    
	    // ModelTreeViewer
	    importModelTree = new ModelViewerTreeUi(importModelTreeComposite);
		
		// Composite: ModelElement item to the right.
	    Composite itemComposite = new Composite(parent, SWT.NONE);
	    FillLayout itemLayout = new FillLayout();
	    itemLayout.marginWidth = 5;
	    itemLayout.marginHeight = 5;
	    itemLayout.spacing = 5;
	    itemComposite.setLayout(itemLayout);
	    itemComposite.setLayout(itemLayout);
		final FormData itemData = new FormData();
		itemData.left = new FormAttachment(verticalSash);
		itemData.right = new FormAttachment(100, -2);
		itemData.top = new FormAttachment(0);
		itemData.bottom = new FormAttachment(100, -5);
		itemComposite.setLayoutData(itemData);

		// Group: ModelElement item.
		ModelViewerElementItemUi modelElementItem = new ModelViewerElementItemUi(itemComposite);
		// Inform tree...
		importModelTree.setModelElementItem(modelElementItem);		

	
		// ========== Listeners ==========
		
		Listener listener;
		
		listener = new Listener() {
			public void handleEvent(Event event) {
				ModelFacade.instance().clearMemoryModel();				
			}
		};
		clearMemoryModelMenuItem.addListener(SWT.Selection, listener);
		
		listener = new Listener() {
			public void handleEvent(Event event) {
				importModelTree.collapseAll();
				importModelTree.expandToLevel(1);
			}
		};		
		modelTreeShowDeliveriesItem.addListener(SWT.Selection, listener);
		
		listener = new Listener() {
			public void handleEvent(Event event) {
				importModelTree.collapseAll();
				importModelTree.expandToLevel(2);
			}
		};		
		modelTreeShowVisitsItem.addListener(SWT.Selection, listener);
		
		listener = new Listener() {
			public void handleEvent(Event event) {
				importModelTree.collapseAll();
				importModelTree.expandToLevel(3);
			}
		};		
		modelTreeShowSamplesItem.addListener(SWT.Selection, listener);
		
		listener = new Listener() {
			public void handleEvent(Event event) {
				importModelTree.collapseAll();
				importModelTree.expandToLevel(4);
			}
		};		
		modelTreeShowVariablesItem.addListener(SWT.Selection, listener);
	}
	
}
