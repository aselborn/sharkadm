/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.userinterface;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 *	Status bar used in the main window. All methods are static, except the constructor.<br/>
 *<br/>
 *	Usage example:<br/>
 *		SharkAdmMainStatusBar.setField5("Busy: Importing file(s) to memory model.");<br/>
 */
public class SharkAdmMainStatusBar {

	private static Label field1;	
	private static Label field2;	
	private static Label field3;	
	private static Label field4;	
	private static Label field5;
	
	// string1 - string5: Used before an instance exists, must be static.
	private static String string1 = "";
	private static String string2 = "";	
	private static String string3 = "";	
	private static String string4 = "";	
	private static String string5 = "";
	private FormData formData;

	public SharkAdmMainStatusBar(Composite parent) {

		// ========== Widget ==========
		
		FormLayout layout = new FormLayout();
		parent.setLayout(layout);

		field1 = new Label(parent, SWT.NONE);
		field1.setText(string1);
		
		Label separator1 = new Label(parent, SWT.SEPARATOR | SWT.VERTICAL);
		
		field2 = new Label(parent, SWT.NONE);
		field2.setText(string2);
		
		Label separator2 = new Label(parent, SWT.SEPARATOR | SWT.VERTICAL);
		
		field3 = new Label(parent, SWT.NONE);
		field3.setText(string3);
		
		Label separator3 = new Label(parent, SWT.SEPARATOR | SWT.VERTICAL);
		
		field4 = new Label(parent, SWT.NONE);
		field4.setText(string4);
		
		Label separator4 = new Label(parent, SWT.SEPARATOR | SWT.VERTICAL);
		
		field5 = new Label(parent, SWT.NONE);
		field5.setText(string5);

		// ========== Layout ==========
		
		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(18, 0);
		formData.bottom = new FormAttachment(100, 0);
		field1.setLayoutData(formData);

		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(18, 0);
		formData.right = new FormAttachment(22, 0);
		formData.bottom = new FormAttachment(100, 0);
		separator1.setLayoutData(formData);

		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(22, 0);
		formData.right = new FormAttachment(38, 0);
		formData.bottom = new FormAttachment(100, 0);
		field2.setLayoutData(formData);

		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(38, 0);
		formData.right = new FormAttachment(42, 0);
		formData.bottom = new FormAttachment(100, 0);
		separator2.setLayoutData(formData);

		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(42, 0);
		formData.right = new FormAttachment(58, 0);
		formData.bottom = new FormAttachment(100, 0);
		field3.setLayoutData(formData);

		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(58, 0);
		formData.right = new FormAttachment(62, 0);
		formData.bottom = new FormAttachment(100, 0);
		separator3.setLayoutData(formData);

		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(62, 0);
		formData.right = new FormAttachment(68, 0);
		formData.bottom = new FormAttachment(100, 0);
		field4.setLayoutData(formData);

		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(68, 0);
		formData.right = new FormAttachment(72, 0);
		formData.bottom = new FormAttachment(100, 0);
		separator4.setLayoutData(formData);

		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(72, 0);
		formData.right = new FormAttachment(100, 0);
		formData.bottom = new FormAttachment(100, 0);
		field5.setLayoutData(formData);

	}

	public static void setField1(String value) {
		if (SharkAdmMainStatusBar.field1 == null) {
			SharkAdmMainStatusBar.string1 = value;
		} else {
			SharkAdmMainStatusBar.field1.setText(value);
		}
	}

	public static void setField2(String value) {
		if (SharkAdmMainStatusBar.field2 == null) {
			SharkAdmMainStatusBar.string2 = value;
		} else {
			SharkAdmMainStatusBar.field2.setText(value);
		}
	}

	public static void setField3(String value) {
		if (SharkAdmMainStatusBar.field3 == null) {
			SharkAdmMainStatusBar.string3 = value;
		} else {
			SharkAdmMainStatusBar.field3.setText(value);
		}
	}

	public static void setField4(String value) {
		if (SharkAdmMainStatusBar.field4 == null) {
			SharkAdmMainStatusBar.string4 = value;
		} else {
			SharkAdmMainStatusBar.field4.setText(value);
		}
	}

	public static void setField5(String value) {
		if (SharkAdmMainStatusBar.field5 == null) {
			SharkAdmMainStatusBar.string5 = value;
		} else {
			SharkAdmMainStatusBar.field5.setText(value);
		}
	}


}
