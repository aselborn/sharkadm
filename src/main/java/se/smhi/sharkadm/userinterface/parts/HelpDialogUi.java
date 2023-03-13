/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.userinterface.parts;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import se.smhi.sharkadm.userinterface.SharkAdmMainUiSettings;

/**
 *	This class contains a separate window used to display help content.
 *	The text to be shown is located in src/SHARK_DOC/*.html
 *	Note that it is not possible to use images in the html code since the Browser object is
 *	loaded by the use of the setText method. 
 *
 *	BACKLOG: Help-windows are not closed when the main window is closed.
 *	BACKLOG: Check if browser.setUrl can be used instead of browser.setText.
 */
public class HelpDialogUi {

	static SharkAdmMainUiSettings settings = SharkAdmMainUiSettings.instance();

	public static void display(Display parentDisplay, String htmlFile) {
		FormData formData;
		Browser browser = null;

		final Shell shell = new Shell(parentDisplay);
		shell.setText("SHARKadm - Help");
		// Center shell on screen.
		Rectangle clientArea = shell.getMonitor().getClientArea();
		shell.setBounds(
				(int) Math.round(clientArea.width * 0.4),
				(int) Math.round(clientArea.height * 0.05), 
				(int) Math.round(clientArea.width * 0.55), 
				(int) Math.round(clientArea.height * 0.90));
		FormLayout shellLayout = new FormLayout();
		shell.setLayout(shellLayout);

		ClassLoader classLoader = parentDisplay.getClass().getClassLoader(); // Some object related to the project is needed.
		InputStream inputStream;
		BufferedReader bufferedReader = null;
		
		try {
			browser = new Browser (shell, SWT.NONE);
		} catch (SWTError e) {
			System.out.println ("Could not create Browser: " + e.getMessage ());
			return;
		}

		try {
			// File is bundled in jar.
			inputStream = classLoader
					.getResourceAsStream("SHARK_DOC/" + htmlFile);
			bufferedReader = new BufferedReader(new InputStreamReader(
					inputStream));
			String row;
			String content = "";
			while ((row = bufferedReader.readLine()) != null) {
				content += row;
			}
			browser.setText(content);
//			browser.setUrl("http://latlong.mellifica.se");
		} catch (Exception e) {
			System.out.println ("Could not read help file: " + htmlFile);
			return;
		}
		
		formData = new FormData();
		formData.left = new FormAttachment(0, 0);
		formData.top = new FormAttachment(0, 0);
		formData.right = new FormAttachment(100, 0);
		formData.bottom = new FormAttachment(100, 0);
		browser.setLayoutData(formData);

		shell.open();

		while (!shell.isDisposed()) {
			if (parentDisplay.isDisposed()) {
				shell.dispose();
			}
			if (!parentDisplay.readAndDispatch()) {
				parentDisplay.sleep();
			}
		}
//		parentDisplay.dispose();
	}

}
