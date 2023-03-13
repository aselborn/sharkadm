/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.userinterface.parts;

import java.util.Arrays;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import se.smhi.sharkadm.model.ModelElement;

public class ModelViewerElementItemUi {

	private TableViewer extraFieldTableViewer;

	SynonymTableLabelProvider labelProvider = new SynonymTableLabelProvider();
	
	public ModelViewerElementItemUi(Composite parent) {

		Group extraFieldGroup = new Group(parent, SWT.NONE);
		extraFieldGroup.setText("Fields");
		FillLayout extraFieldGroupLayout = new FillLayout();
		extraFieldGroupLayout.marginHeight = 5;
		extraFieldGroupLayout.marginWidth = 5;
		extraFieldGroup.setLayout(extraFieldGroupLayout);

		extraFieldTableViewer = new TableViewer(extraFieldGroup, SWT.BORDER
				| SWT.SINGLE | SWT.FULL_SELECTION);
		Table extraFieldTable = extraFieldTableViewer.getTable();
		extraFieldTable.setHeaderVisible(true);
		extraFieldTable.setLinesVisible(true);

		String[] columnNames = new String[] { "Key (level.internal_key)", "Value" };
		int[] columnWidths = new int[] { 200, 500 };
		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT };
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn column = new TableColumn(extraFieldTable,
					columnAlignments[i]);
			column.setText(columnNames[i]);
			column.setWidth(columnWidths[i]);
		}

		// labelProvider = new SynonymTableLabelProvider();
		extraFieldTableViewer.setLabelProvider(labelProvider);
		extraFieldTableViewer.setContentProvider(new ArrayContentProvider());
	}

	public void showModelElement(ModelElement modelElement) {
		labelProvider.setModelElement(modelElement);
		try {
			boolean debugTempFields = false; // DEBUG: true = Show fields in tempFieldMap instead of fieldMap.
			if (debugTempFields == false) {
//				extraFieldTableViewer.setInput(modelElement.getFieldKeys().toArray());
				
				Object[] keys = modelElement.getFieldKeys().toArray();
				Arrays.sort(keys);
				extraFieldTableViewer.setInput(keys);	
				
			} else {
				extraFieldTableViewer.setInput(modelElement.getTempFieldKeys().toArray());
			}
		} catch (Exception e) {
			// Catches null pointer exception in memory model browser.
		}

	}
}

class SynonymTableLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	private ModelElement modelElement;

	public Image getColumnImage(Object arg0, int arg1) {
		return null;
	}

	public String getColumnText(Object modelElement, int columnIndex) {
		if (columnIndex == 0) {
			return (String) modelElement;
		} else {
			boolean debugTempFields = false; // DEBUG: true = Show fields in tempFieldMap instead of fieldMap.
			if (debugTempFields == false) {
				return this.modelElement.getField((String) modelElement);
			} else {
				return this.modelElement.getTempField((String) modelElement);
			}
		}
	}

	public void setModelElement(ModelElement modelElement) {
		this.modelElement = modelElement;
	}

}
