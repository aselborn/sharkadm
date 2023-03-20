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
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.ModelElement;
import se.smhi.sharkadm.model.ModelTopNode;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.userinterface.SharkAdmMainStatusBar;
import se.smhi.sharkadm.userinterface.SharkAdmMainWindow;

public class ModelViewerTreeUi extends TreeViewer implements Observer {
	private ModelViewerElementItemUi modelElementItem;
	private Composite parent;

	public ModelViewerTreeUi(Composite parent) {
		super(parent, SWT.SINGLE);
		this.parent = parent;

		ModelFacade.instance().addObserver(this);
		
		this.setLabelProvider(new ImportModelLabelProvider());
		this.setContentProvider(new ImportModelContentProvider());
		this.setInput(ModelTopNode.instance().getDatasetList().toArray());

//		this.expandAll();
		this.expandToLevel(1);

		this.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				modelElementItem.showModelElement((ModelElement) selection
						.getFirstElement());
			}
		});

		this.addDoubleClickListener(new IDoubleClickListener() {			
			public void doubleClick(DoubleClickEvent event) {
//				resetInput();
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				modelElementItem.showModelElement((ModelElement) selection
						.getFirstElement());
			}
		});

	}

	public void setModelElementItem(ModelViewerElementItemUi itemViewer) {
		this.modelElementItem = itemViewer;
	}
	
	public void resetInput() {
		Object datasetNoteArray[] = ModelTopNode.instance().getDatasetList().toArray();
		this.setInput(datasetNoteArray);
		SharkAdmMainStatusBar.setField3("Memory models: " + datasetNoteArray.length + " items");

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

class ImportModelLabelProvider extends LabelProvider {

	public Image getImage(Object modelElement) {
		return null;
	}

	public String getText(Object modelElement) {
		if (modelElement instanceof Dataset) {
			return "DELIVERY: "
					+ ((Dataset) modelElement).getField("dataset.dataset_file_name");
		} else if (modelElement instanceof Visit) {
			if (((Visit) modelElement).getVisit_position() != null) {
				return "VISIT: Date: "
				+ ((Visit) modelElement).getField("visit.visit_date") + "   Station: "
				+ ((Visit) modelElement).getField("visit.station_name") + " " + "   Pos(DD): "
				+ ((Visit) modelElement).getVisit_position().getLatitudeAsString() + "/"
				+ ((Visit) modelElement).getVisit_position().getLongitudeAsString() + "";
			} else {
				return "VISIT: Date: "
				+ ((Visit) modelElement).getField("visit.visit_date") + "   Station: "
				+ ((Visit) modelElement).getField("visit.station_name");
			}
		} else if (modelElement instanceof Sample) {
			if (((Sample) modelElement).getPosition() != null) {
				return "SAMPLE: Id: " 
				+ ((Sample) modelElement).getField("sample.sample_id") + "   Depth: "
				+ ((Sample) modelElement).getField("sample.sample_min_depth_m") + " - "
				+ ((Sample) modelElement).getField("sample.sample_max_depth_m") + "   Pos(DD): "
				+ ((Sample) modelElement).getPosition().getLatitudeAsString() + "/"
				+ ((Sample) modelElement).getPosition().getLongitudeAsString() + "";
			} else {
				return "SAMPLE: Id: " 
				+ ((Sample) modelElement).getField("sample.sample_id") + "   Depth: "
				+ ((Sample) modelElement).getField("sample.sample_min_depth_m") + " - "
				+ ((Sample) modelElement).getField("sample.sample_max_depth_m");
			}
		} else if ((modelElement instanceof Variable) && (((Variable) modelElement).isCommunity())) {
			return "COMMUNITY: Parameter: "
			+ ((Variable) modelElement).getParameter() + "   Value: "
			+ ((Variable) modelElement).getValue() + "   Unit: "
			+ ((Variable) modelElement).getUnit() + "   Species: "
			+ ((Variable) modelElement).getField("variable.scientific_name");
		} else if (modelElement instanceof Variable) {
			return "VARIABLE: Parameter: "
			+ ((Variable) modelElement).getParameter() + "   Value: "
			+ ((Variable) modelElement).getValue() + "   Unit: "
			+ ((Variable) modelElement).getUnit();
		}
		return "<NOT IMPLEMENTED>";
	}

}

class ImportModelContentProvider extends ArrayContentProvider implements
		ITreeContentProvider {

	public Object[] getChildren(Object modelElement) {
		if (modelElement instanceof Dataset) {
			return ((Dataset) modelElement).getVisits().toArray();
		} else if (modelElement instanceof Visit) {
			return ((Visit) modelElement).getSamples().toArray();
		} else if (modelElement instanceof Sample) {
			return ((Sample) modelElement).getVariables().toArray();
		}
		return null;
	}

	public Object getParent(Object modelElement) {
		if (modelElement instanceof Dataset) {
			return null;
		} else if (modelElement instanceof Visit) {
			return ((Visit) modelElement).getParent();
		} else if (modelElement instanceof Sample) {
			return ((Sample) modelElement).getParent();
		} else if (modelElement instanceof Variable) {
			return ((Variable) modelElement).getParent();
		}
		return null;
	}

	public boolean hasChildren(Object modelElement) {
		if (modelElement instanceof Dataset) {
			return !((Dataset) modelElement).getVisits().isEmpty();
		} else if (modelElement instanceof Visit) {
			return !((Visit) modelElement).getSamples().isEmpty();
		} else if (modelElement instanceof Sample) {
			return !((Sample) modelElement).getVariables().isEmpty();
		}
		return false;
	}

}
