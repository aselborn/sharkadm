/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.userinterface.parts;

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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import se.smhi.sharkadm.species_old.TaxonManager;
import se.smhi.sharkadm.species_old.TaxonNameObject;
import se.smhi.sharkadm.species_old.TaxonNode;

public class SpeciesItemViewerUi {
	
	private Text dyntaxaIdField;
	private Text taxonValidNameField;
	private Text taxonTypeIdField;
	private Text validFromDateField;
	private Text validToDateField;
	private Text changedDateField;
	private Text addedDateField;
	private Text activeField;
	private Text taxonCommentField;
	
	TableViewer synonymTableViewer;
	
	public SpeciesItemViewerUi(Composite parent) {

		FormData formData;
		Group itemGroup = new Group(parent, SWT.NONE);
		itemGroup.setText("Species item");
		FormLayout itemGroupLayout = new FormLayout();
		itemGroupLayout.marginWidth = 10;
		itemGroupLayout.marginHeight = 10;
		itemGroupLayout.spacing = 2;
		itemGroup.setLayout(itemGroupLayout);

		//		dyntaxaId
		Label dyntaxaIdLabel = new Label(itemGroup, SWT.NONE);
		dyntaxaIdLabel.setText("Taxon ID:");
		formData = new FormData();
		formData.left = new FormAttachment(0, 3);
		formData.right = new FormAttachment(20, -3);
		formData.top = new FormAttachment(0, 0);
		dyntaxaIdLabel.setLayoutData(formData);

		dyntaxaIdField = new Text(itemGroup, SWT.BORDER);
		formData = new FormData();
		formData.left = new FormAttachment(20, 3);
		formData.right = new FormAttachment(100, -3);
		formData.top = new FormAttachment(0, 3);
		dyntaxaIdField.setLayoutData(formData);

		// Valid name (from TaxonNameObject).
		Label taxonValidNameLabel = new Label(itemGroup, SWT.NONE);
		taxonValidNameLabel.setText("Valid name/author:");
		formData = new FormData();
		formData.left = new FormAttachment(0, 3);
		formData.right = new FormAttachment(20, -3);
		formData.top = new FormAttachment(dyntaxaIdField, 3);
		taxonValidNameLabel.setLayoutData(formData);

		taxonValidNameField = new Text(itemGroup, SWT.BORDER);
		formData = new FormData();
		formData.left = new FormAttachment(20, 3);
		formData.right = new FormAttachment(100, -3);
		formData.top = new FormAttachment(dyntaxaIdField, 3);
		taxonValidNameField.setLayoutData(formData);

//		taxonTypeId
		Label taxonTypeIdLabel = new Label(itemGroup, SWT.NONE);
		taxonTypeIdLabel.setText("Taxon type:");
		formData = new FormData();
		formData.left = new FormAttachment(0, 3);
		formData.right = new FormAttachment(20, -3);
		formData.top = new FormAttachment(taxonValidNameField, 3);
		taxonTypeIdLabel.setLayoutData(formData);

		taxonTypeIdField = new Text(itemGroup, SWT.BORDER);
		formData = new FormData();
		formData.left = new FormAttachment(20, 3);
		formData.right = new FormAttachment(100, -3);
		formData.top = new FormAttachment(taxonValidNameField, 3);
		taxonTypeIdField.setLayoutData(formData);

//		validFromDate
		Label validFromDateLabel = new Label(itemGroup, SWT.NONE);
		validFromDateLabel.setText("Valid from:");
		formData = new FormData();
		formData.left = new FormAttachment(0, 3);
		formData.right = new FormAttachment(20, -3);
		formData.top = new FormAttachment(taxonTypeIdField, 3);
		validFromDateLabel.setLayoutData(formData);

		validFromDateField = new Text(itemGroup, SWT.BORDER);
		formData = new FormData();
		formData.left = new FormAttachment(20, 3);
		formData.right = new FormAttachment(100, -3);
		formData.top = new FormAttachment(taxonTypeIdField, 3);
		validFromDateField.setLayoutData(formData);

//		validToDate
		Label validToDateLabel = new Label(itemGroup, SWT.NONE);
		validToDateLabel.setText("Valid to:");
		formData = new FormData();
		formData.left = new FormAttachment(0, 3);
		formData.right = new FormAttachment(20, -3);
		formData.top = new FormAttachment(validFromDateField, 3);
		validToDateLabel.setLayoutData(formData);

		validToDateField = new Text(itemGroup, SWT.BORDER);
		formData = new FormData();
		formData.left = new FormAttachment(20, 3);
		formData.right = new FormAttachment(100, -3);
		formData.top = new FormAttachment(validFromDateField, 3);
		validToDateField.setLayoutData(formData);

//		changedDate
		Label changedDateLabel = new Label(itemGroup, SWT.NONE);
		changedDateLabel.setText("Changed:");
		formData = new FormData();
		formData.left = new FormAttachment(0, 3);
		formData.right = new FormAttachment(20, -3);
		formData.top = new FormAttachment(validToDateField, 3);
		changedDateLabel.setLayoutData(formData);

		changedDateField = new Text(itemGroup, SWT.BORDER);
		formData = new FormData();
		formData.left = new FormAttachment(20, 3);
		formData.right = new FormAttachment(100, -3);
		formData.top = new FormAttachment(validToDateField, 3);
		changedDateField.setLayoutData(formData);

//		addedDate
		Label addedDateLabel = new Label(itemGroup, SWT.NONE);
		addedDateLabel.setText("Added:");
		formData = new FormData();
		formData.left = new FormAttachment(0, 3);
		formData.right = new FormAttachment(20, -3);
		formData.top = new FormAttachment(changedDateField, 3);
		addedDateLabel.setLayoutData(formData);

		addedDateField = new Text(itemGroup, SWT.BORDER);
		formData = new FormData();
		formData.left = new FormAttachment(20, 3);
		formData.right = new FormAttachment(100, -3);
		formData.top = new FormAttachment(changedDateField, 3);
		addedDateField.setLayoutData(formData);

//		active
		Label activeLabel = new Label(itemGroup, SWT.NONE);
		activeLabel.setText("Active:");
		formData = new FormData();
		formData.left = new FormAttachment(0, 3);
		formData.right = new FormAttachment(20, -3);
		formData.top = new FormAttachment(addedDateField, 3);
		activeLabel.setLayoutData(formData);

		activeField = new Text(itemGroup, SWT.BORDER);
		formData = new FormData();
		formData.left = new FormAttachment(20, 3);
		formData.right = new FormAttachment(100, -3);
		formData.top = new FormAttachment(addedDateField, 3);
		activeField.setLayoutData(formData);

//		comment
		Label taxonCommentLabel = new Label(itemGroup, SWT.NONE);
		taxonCommentLabel.setText("Comment:");
		formData = new FormData();
		formData.left = new FormAttachment(0, 3);
		formData.right = new FormAttachment(20, -3);
		formData.top = new FormAttachment(activeField, 3);
		taxonCommentLabel.setLayoutData(formData);

		taxonCommentField = new Text(itemGroup, SWT.BORDER);
		formData = new FormData();
		formData.left = new FormAttachment(20, 3);
		formData.right = new FormAttachment(100, -3);
		formData.top = new FormAttachment(activeField, 3);
		taxonCommentField.setLayoutData(formData);

		Group synonymGroup = new Group(itemGroup, SWT.NONE);
		synonymGroup.setText("Synonym names");
		FillLayout synonymGroupLayout = new FillLayout();
		synonymGroupLayout.marginWidth = 10;
		synonymGroupLayout.marginHeight = 10;
		synonymGroup.setLayout(synonymGroupLayout);
		formData = new FormData();
		formData.left = new FormAttachment(0, 3);
		formData.right = new FormAttachment(100, -3);
		formData.top = new FormAttachment(taxonCommentField, 3);
		formData.bottom = new FormAttachment(100, -3);
		synonymGroup.setLayoutData(formData);

		synonymTableViewer = new TableViewer(synonymGroup,
				SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		Table synonymTable = synonymTableViewer.getTable();
		synonymTable.setHeaderVisible(true);
		synonymTable.setLinesVisible(true);

//		taxonNametype;
//		name;
//		author;
//		nameValidityCode;
//		nameValidFromDate;
//		nameValidToDate;
//		comment;
		
//		String[] columnNames = new String[] { "Name", "RLIST" };
//		int[] columnWidths = new int[] { 150, 150 };
//		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT };
		String[] columnNames = new String[] { 
				"Type", "Name", "Author", "ValidityCode", "Valid from", "Valid to", "Comment" };
		int[] columnWidths = new int[] { 40, 150, 120, 80, 80, 80, 250 };
		int[] columnAlignments = new int[] { 
				SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT };
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn column = new TableColumn(synonymTable,
					columnAlignments[i]);
			column.setText(columnNames[i]);
			column.setWidth(columnWidths[i]);
		}

		synonymTableViewer.setLabelProvider(new SynonymTableLabelProvider());

		synonymTableViewer.setContentProvider(new ArrayContentProvider());

		// / this.setInput(new SpeciesDbTable().getTaxonArray());

		synonymTableViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						IStructuredSelection selection = (IStructuredSelection) event
								.getSelection();
//						ErrorLogger.println("Selected: "
//								+ selection.getFirstElement());
					}
				});

		synonymTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
//				ErrorLogger.println("DoubleClick: "
//						+ selection.getFirstElement());
			}
		});
	}

	public void showTaxon(TaxonNode taxonNode) {
		dyntaxaIdField.setText(Integer.toString(taxonNode.getTaxonObject().getDyntaxaId()));
		TaxonNameObject nameObject = taxonNode.getTaxonObject().getValidNameObject();
		if (nameObject != null) {
			taxonValidNameField.setText(nameObject.getName() + " " + nameObject.getAuthor());
		} else {
			taxonValidNameField.setText("NO VALID NAME");			
		}
//		taxonTypeIdField.setText(Integer.toString(taxonNode.getTaxonObject().getTaxonTypeId()) + ", " +
//				TaxonManager.convertTaxonTypeCodeToString(taxonNode.getTaxonObject().getTaxonTypeId()));
		taxonTypeIdField.setText(taxonNode.getTaxonObject().getTaxonRank());
		validFromDateField.setText(taxonNode.getTaxonObject().getValidFromDate());
		validToDateField.setText(taxonNode.getTaxonObject().getValidToDate());
		changedDateField.setText(taxonNode.getTaxonObject().getChangedDate());
		addedDateField.setText(taxonNode.getTaxonObject().getAddedDate());
		activeField.setText(Boolean.toString(taxonNode.getTaxonObject().isActive()));
		taxonCommentField.setText(taxonNode.getTaxonObject().getComment());
		
		synonymTableViewer.setInput(taxonNode.getTaxonObject().getTaxonNames());
	}

	class SynonymTableLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		public Image getColumnImage(Object arg0, int arg1) {
			return null;
		}

		public String getColumnText(Object nameObject, int columnIndex) {
			if (columnIndex == 0) { // taxonNametype
				return Integer.toString(((TaxonNameObject) nameObject).getTaxonNametype());
			} else if (columnIndex == 1) { // name
				return ((TaxonNameObject) nameObject).getName(); // 0=Scientific
			} else if (columnIndex == 2) { // author
				return ((TaxonNameObject) nameObject).getAuthor();
			} else if (columnIndex == 3) { // nameValidityCode
				return Integer.toString(((TaxonNameObject) nameObject).getNameValidityCode());
			} else if (columnIndex == 4) { // nameValidFromDate
				return ((TaxonNameObject) nameObject).getNameValidFromDate();
			} else if (columnIndex == 5) { // nameValidToDate
				return ((TaxonNameObject) nameObject).getNameValidToDate();
			} else if (columnIndex == 6) { // comment
				return ((TaxonNameObject) nameObject).getComment();
			}
			return "";
		}
	}

}
