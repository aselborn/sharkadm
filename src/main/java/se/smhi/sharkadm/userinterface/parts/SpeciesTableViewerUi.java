/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.userinterface.parts;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import se.smhi.sharkadm.facades.ModelFacade;
import se.smhi.sharkadm.species_old.TaxonManager;
import se.smhi.sharkadm.species_old.TaxonNameObject;
import se.smhi.sharkadm.species_old.TaxonNode;
import se.smhi.sharkadm.species_old.TaxonObject;

public class SpeciesTableViewerUi extends TableViewer implements Observer {
	private SpeciesItemViewerUi taxonItem;
	private Table table;
	private Composite parent;

	public SpeciesTableViewerUi(Composite parent) {
		super(parent, SWT.SINGLE | SWT.FULL_SELECTION);
		this.parent = parent;

		ModelFacade.instance().addObserver(this);
		
		table = this.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		String[] columnNames = new String[] { 
			"Taxon-id", "Taxon-type", "Taxonomic hierarchy", 
			"Valid taxon name", "Author",  
			"ITIS", "ERMS-name", "Synonyms" 
		};
		int[] columnWidths = new int[] { 
			80, 80, 80, 80, 80,  
			80, 80, 80, 80, 80, 
			80, 80, 80, 80, 80,  
			80, 80, 80, 80, 80, 80 
		};
		int[] columnAlignments = new int[] { 
			SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT,  
			SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT,  
			SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT,  
			SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, 
			SWT.LEFT  
		};
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn column = new TableColumn(table, columnAlignments[i]);
			column.setText(columnNames[i]);
			column.setWidth(columnWidths[i]);
		}

		this.setLabelProvider(new TaxonTableLabelProvider());

		this.setContentProvider(new ArrayContentProvider());

		// this.setSorter(new BeehiveListsorter());

		this.setInput(TaxonManager.instance().getTaxonList().toArray());
		
		this.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				taxonItem.showTaxon((TaxonNode) selection.getFirstElement());
			}
		});

		this.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				taxonItem.showTaxon((TaxonNode) selection.getFirstElement());
			}
		});
	}

	public void setTaxonItem(SpeciesItemViewerUi taxonItem) {
		this.taxonItem = taxonItem;
	}

	public void resetInput() {
		this.setInput(TaxonManager.instance().getTaxonList().toArray());
//		this.refresh();
	}

	public void update(Observable arg0, Object arg1) {
		// "syncExec" is needed if the Observer is executed in a non-ui thread.
		parent.getDisplay().syncExec(new Runnable() {
			public void run() {
				resetInput();
			}
		});
	}
	
}

class TaxonTableLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	public Image getColumnImage(Object arg0, int arg1) {
		return null;
	}

	public String getColumnText(Object object, int columnIndex) {
		String tmp = "";
		TaxonNode taxonNode = ((TaxonNode)object);
		TaxonObject taxon = taxonNode.getTaxonObject();
		
		if (columnIndex == 0) {
			return Integer.toString(taxon.getDyntaxaId());
			
		} else if (columnIndex == 1) {
//			return TaxonManager.convertTaxonTypeCodeToString(((TaxonObject) taxon).getTaxonTypeId());
			return taxon.getTaxonRank();
			
		} else if (columnIndex == 2) {
			if (taxon.getValidNameObject() != null) {
				tmp = taxon.getValidNameObject().getName();
			} else {
				tmp = "NO VALID NAME";
			}
			TaxonNode parentNode = taxon.getTaxonNode().getParent();
			while (parentNode != null) {
				try {
					if (parentNode.getTaxonObject() != null) {
					tmp = parentNode.getTaxonObject().getValidNameObject()
							.getName()+ " - " + tmp;
					}
				} catch (Exception e) {
					tmp = "NO VALID NAME" + " - " + tmp;
				}
				parentNode = parentNode.getParent();
			}
			return  tmp;

		} else if (columnIndex == 3) {
			TaxonNameObject nameObject = taxon.getValidNameObject();
			if (nameObject != null) {
				return nameObject.getName();
			}
			return "NO VALID NAME";
			
		} else if (columnIndex == 4) {
			TaxonNameObject nameObject = taxon.getValidNameObject();
			if (nameObject != null) {
				return nameObject.getAuthor();
			}
			return "";
			
		} else if (columnIndex == 5) {
			return taxon.getTaxonName(10); // 10 = ITIS.
			
		} else if (columnIndex == 6) {
			return taxon.getTaxonName(11); // 11 = ERMS.
			
		} else if (columnIndex == 7) {
			return taxon.getSynonymNames();
			
		} else if (columnIndex == 8) {
			return "-";
			
		} else if (columnIndex == 9) {
			return "-";
			
		} else if (columnIndex == 10) {
			return "-";
			
		} else if (columnIndex == 11) {
			return "-";
			
		} else if (columnIndex == 12) {
			return "-";
			
		} else if (columnIndex == 13) {
			return "-";
			
		} else if (columnIndex == 14) {
			return "-";
			
		} else if (columnIndex == 15) {
			return "-";
			
		} else if (columnIndex == 16) {
			return "-";
			
		} else if (columnIndex == 17) {
			return "-";
			
		} else if (columnIndex == 18) {
			return "-";
			
		} else if (columnIndex == 19) {
			return "-";
			
		} else if (columnIndex == 20) {
			return "-";
			
		}
		return "ERROR";
	}

}
