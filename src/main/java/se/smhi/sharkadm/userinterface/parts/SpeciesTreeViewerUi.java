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
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import se.smhi.sharkadm.facades.ModelFacade;
import se.smhi.sharkadm.species_old.TaxonManager;
import se.smhi.sharkadm.species_old.TaxonNameObject;
import se.smhi.sharkadm.species_old.TaxonNode;
import se.smhi.sharkadm.species_old.TaxonObject;

public class SpeciesTreeViewerUi extends TreeViewer implements Observer {
	private SpeciesItemViewerUi taxonItem;
	private Composite parent;

	public SpeciesTreeViewerUi(Composite parent) {
		super(parent, SWT.SINGLE);
		this.parent = parent;

		ModelFacade.instance().addObserver(this);
		
		this.setLabelProvider(new SpeciesTreeLabelProvider());
		this.setContentProvider(new SpeciesTreeContentProvider());
		this.setInput(TaxonManager.instance().getTopNodeList());

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
		this.setInput(TaxonManager.instance().getTopNodeList());
//		this.expandAll();
		this.refresh();
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

class SpeciesTreeLabelProvider extends LabelProvider {

	String[] taxonTypes = new String[] { 
//			"", "Kingdom", "Phylum", "Subphylum", "Superclass", 
//			"Class", "Subclass", "Superorder", "Order", "Suborder", 
//			"Superfamily", "Family", "Subfamily", "Tribe", "Genus", 
//			"Subgenus", "Section", "Species", "Subspecies", "Variety", 
//			"Form", "Hybrid", "Cultural variety", "Population", 
//			"Group of families", "Infraclass", "Parvclass", 
//			"Sensu latu", "Species pair", "Infraorder" };
	
//	// 2012:
//	"", "Rike", "Stam", "Understam", "�verklass", "Klass", "Underklass", 
//	"�verordning", "Ordning", "Underordning", "�verfamilj", 
//	"Familj", "Underfamilj", "Tribus", 
//	"Sl�kte", "Undersl�kte", "Sektion", 
//	"Art", "Underart", "Varietet", "Form", "Hybrid", 
//	"Sort", "Population", "Infraklass", "Parvklass", 
//	"Kollektivtaxon", "Artkomplex", "Infraordning", 
//	"Avdelning", "Underavdelning", "Morfotyp", 
//	"Organismgrupp", "Dom�n", "Underrik", "Gren", 
//	"Infrarike", "�verstam", "Infrastam", "�veravdelning", 
//	"Infraavdelning", "Infrafamilj", "�vertribus", 
//	"Undertribus", "Infratribus", "Undersektion", "Serie", 
//	"Underserie", "Aggregat", "Sm�art", "Sortgrupp" };
	
//	// 2014:
//	"", "Rot", "Rike", "StamUnderstam", "�verklass", "Klass", 
//	"Underklass", "�verordning", "Ordning", "Underordning", 
//	"�verfamilj", "Familj", "Underfamilj", "Tribus", "Sl�kte", 
//	"Undersl�kte", "Sektion", "Art", "Underart", "Varietet", 
//	"Form", "Hybrid", "Sort", "Population", "", "Infraklass", 
//	"Parvklass", "Kollektivtaxon", "Artkomplex", "Infraordning", 
//	"Avdelning", "Underavdelning", "Morfotyp", "Organismgrupp", 
//	"Dom�n", "Underrike", "Gren", "Infrarike", "�verstam", 
//	"Infrastam", "�veravdelning", "Infraavdelning", "Infrafamilj", 
//	"�vertribus", "Undertribus", "Infratribus", "Undersektion", 
//	"Serie", "Underserie", "Aggregat", "Sm�art", "Sortgrupp", 
//	"Rangl�s"
//	};

	// 2021, DarwinCore export. Also check FileImportDynamicTaxa.java.
	"",
	"domain",
	"superkingdom",
	"kingdom",
	"subkingdom",
	"infrakingdom",
		
	"superphylum",
	"phylum",
	"subphylum",
	"infraphylum",
		
	"superdivision",
	"division",
	"subdivision",
	
	"superclass",
	"class",
	"subclass",
	"infraclass",
	"parvclass",
		
	"superorder",
	"order",
	"suborder",
	"infraorder",
	"parvorder",
		
	"superfamily",
	"family",
	"subfamily",
	"infrafamily",
		
	"supertribe",
	"tribe",
	"subtribe",
	"infratribe",
	
	"genus",
	"subgenus",
		
	"superspecies",
	"species",
	"subspecies",
	"variety",
	"form",
		
	"section",
	"unranked",
	"cultivar",
	"speciesAggregate",
	"forma specialis",
};

	public Image getImage(Object taxon) {
//		try {
//			if (((TaxonObject) taxon).getTaxonTypeId() == 17) {
//				return new Image(null, "c:\\samples_c.gif");
//			}
//			return new Image(null, "c:\\samples.gif");
//		} catch (Exception e) {
			return null;
//		}
	}

	public String getText(Object taxonNode) {
		TaxonObject taxonObject = ((TaxonNode) taxonNode).getTaxonObject();
		TaxonNameObject nameObject = taxonObject.getValidNameObject();
//		String taxonType = "[" + taxonTypes[taxonObject.getTaxonTypeId()] + "]";
		String taxonType = "[" + taxonObject.getTaxonRank() + "]";
		if (nameObject != null) {
			return taxonType + " " +
				nameObject.getName() + " " + nameObject.getAuthor();
		} else {
			return taxonType + " " +
				"NO VALID NAME";			
		}
	}
}

class SpeciesTreeContentProvider extends ArrayContentProvider implements
		ITreeContentProvider {

	public Object[] getChildren(Object taxonNode) {
		return ((TaxonNode) taxonNode).getChildren().toArray();
	}

	public Object getParent(Object taxon) {
		return ((TaxonNode) taxon).getParent();
	}

	public boolean hasChildren(Object taxonNode) {
		return ((TaxonNode) taxonNode).hasChildren();
	}

}
