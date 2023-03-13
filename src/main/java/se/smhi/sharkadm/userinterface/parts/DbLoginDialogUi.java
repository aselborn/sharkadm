/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.userinterface.parts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import se.smhi.sharkadm.database.DbConnect;
import se.smhi.sharkadm.userinterface.SharkAdmMainUiSettings;

public class DbLoginDialogUi {

	static SharkAdmMainUiSettings settings = SharkAdmMainUiSettings.instance();

	static boolean loggedIn = false;

	public static boolean display(Display parentDiplay) {
		FormData formData;

//		Display display = new Display();
		final Shell shell = new Shell(parentDiplay);
		shell.setText("SHARKadm: Database login");
		// Center shell on screen.
		Rectangle clientArea = shell.getMonitor().getClientArea();
		shell.setBounds(clientArea.width / 2 - 200,
				clientArea.height / 2 - 100, 400, 270);
		FormLayout shellLayout = new FormLayout();
		shell.setLayout(shellLayout);

		Label selectServerLabel = new Label(shell, SWT.LEFT);
		selectServerLabel.setText("Select server:");

		final Combo selectServerCombo = new Combo(shell, SWT.READ_ONLY);
		selectServerCombo.setBounds(50, 50, 150, 65);
	    String items[] = {  "<select>", 
	    					"PROD: er-postgresql.smhi.se", 
	    					"TEST: er-postgresql-tst.smhi.se", 
	    					"LOCAL: localhost" };
	    selectServerCombo.setItems(items);
	    selectServerCombo.select(0);

		Label serverLabel = new Label(shell, SWT.LEFT);
		serverLabel.setText("Database server:");
		Label databaseLabel = new Label(shell, SWT.LEFT);
		databaseLabel.setText("Database:");
		Label userLabel = new Label(shell, SWT.LEFT);
		userLabel.setText("Username:");
		Label passwordLabel = new Label(shell, SWT.LEFT);
		passwordLabel.setText("Password:");

		final Text serverText = new Text(shell, SWT.LEFT | SWT.BORDER);
		serverText.setText(settings.getString("database_server", "localhost"));
		final Text databaseText = new Text(shell, SWT.LEFT | SWT.BORDER);
		databaseText.setText(settings.getString("database", "sharkweb"));
		final Text userText = new Text(shell, SWT.LEFT | SWT.BORDER);
		userText.setText(settings.getString("database_user", ""));
		final Text passwordText = new Text(shell, SWT.LEFT | SWT.BORDER | SWT.PASSWORD);
		passwordText.setText("");

		// Used for fast login during development.
		if ((serverText.getText().equals("localhost")) && (userText.getText().equals("postgres"))) {
			passwordText.setText("postgres");
		}
		
		Listener loginListener = new Listener() {
			public void handleEvent(Event event) {
				if (event.keyCode == SWT.CR) {
					SharkAdmMainUiSettings.instance().setString(
							"database_server", serverText.getText());
					SharkAdmMainUiSettings.instance().setString("database",
							databaseText.getText());
					SharkAdmMainUiSettings.instance().setString("database_user",
							userText.getText());
					SharkAdmMainUiSettings.instance().saveSettings();

					DbConnect.instance()
							.setDatabaseServer(serverText.getText());
					DbConnect.instance()
							.setDatabaseName(databaseText.getText());
					DbConnect.instance().setUser(userText.getText());
					DbConnect.instance().setPassword(passwordText.getText());

					if (DbConnect.instance().getConnection() == null) {
						loggedIn = false;
						MessageBox failureBox = new MessageBox(shell, SWT.ICON_ERROR);
						failureBox.setText("Login failure");
						failureBox
								.setMessage("Login to database failed. Please try again.");
						failureBox.open();
					} else {
						loggedIn = true;
						shell.close();
					}
					
				} else if (event.keyCode == SWT.ESC) {
					loggedIn = false;
					shell.close();
				}
///				DataFacade.instance().connectionStatusChanged();
			}
		};

		serverText.addListener(SWT.KeyDown, loginListener);
		databaseText.addListener(SWT.KeyDown, loginListener);
		userText.addListener(SWT.KeyDown, loginListener);
		passwordText.addListener(SWT.KeyDown, loginListener);

//		Button noDbButton = new Button(shell, SWT.PUSH);
//		noDbButton.setText("No database");
//		noDbButton.setSelection(true);
//		noDbButton.setFocus();

//		noDbButton.addListener(SWT.Selection, new Listener() {
//			public void handleEvent(Event event) {
//				DbConnect.instance().setNoDatabase(true);
//				loggedIn = true;
//				shell.close();
//			}
//		});

		Button loginButton = new Button(shell, SWT.PUSH);
		loginButton.setText("Login");
		loginButton.setSelection(true);
		loginButton.setFocus();

		loginButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				SharkAdmMainUiSettings.instance().setString("database_server",
						serverText.getText());
				SharkAdmMainUiSettings.instance().setString("database",
						databaseText.getText());
				SharkAdmMainUiSettings.instance().setString("database_user",
						userText.getText());
				SharkAdmMainUiSettings.instance().saveSettings();

				DbConnect.instance().setDatabaseServer(serverText.getText());
				DbConnect.instance().setDatabaseName(databaseText.getText());
				DbConnect.instance().setUser(userText.getText());
				DbConnect.instance().setPassword(passwordText.getText());

				if (DbConnect.instance().getConnection() == null) {
					loggedIn = false;
					MessageBox failureBox = new MessageBox(shell, SWT.ICON_ERROR);
					failureBox.setText("Login failure");
					failureBox.setMessage("Login to database failed. Please try again.");
					failureBox.open();
				} else {
					loggedIn = true;
					shell.close();
				}
///				DataFacade.instance().connectionStatusChanged();
			}
		});

		Button cancelButton = new Button(shell, SWT.PUSH);
		cancelButton.setText("Cancel");
		cancelButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				loggedIn = false;
				shell.close();
///				DataFacade.instance().connectionStatusChanged();
			}
		});

		// Layout for serverLabel.
		formData = new FormData();
		formData.left = new FormAttachment(0, 10);
		formData.top = new FormAttachment(0, 19);
		selectServerLabel.setLayoutData(formData);

		formData = new FormData();
		formData.left = new FormAttachment(0, 10);
		formData.top = new FormAttachment(selectServerLabel, 19);
		serverLabel.setLayoutData(formData);

		// Layout for databaseLabel.
		formData = new FormData();
		formData.left = new FormAttachment(0, 10);
		formData.top = new FormAttachment(serverLabel, 14);
		databaseLabel.setLayoutData(formData);

		// Layout for userLabel.
		formData = new FormData();
		formData.left = new FormAttachment(0, 10);
		formData.top = new FormAttachment(databaseText, 14);
		userLabel.setLayoutData(formData);

		// Layout for passwordLabel.
		formData = new FormData();
		formData.left = new FormAttachment(0, 10);
		formData.top = new FormAttachment(userText, 14);
		passwordLabel.setLayoutData(formData);

		// Layout for serverText.
		formData = new FormData();
		formData.left = new FormAttachment(serverLabel, 10);
		formData.right = new FormAttachment(100, -10);
		formData.top = new FormAttachment(0, 15);
		selectServerCombo.setLayoutData(formData);

		// Layout for serverText.
		formData = new FormData();
		formData.left = new FormAttachment(serverLabel, 10);
		formData.right = new FormAttachment(100, -10);
		formData.top = new FormAttachment(selectServerCombo, 15);
		serverText.setLayoutData(formData);

		// Layout for databaseText.
		formData = new FormData();
		formData.left = new FormAttachment(serverLabel, 10);
		formData.right = new FormAttachment(100, -10);
		formData.top = new FormAttachment(serverText, 10);
		databaseText.setLayoutData(formData);

		// Layout for userText.
		formData = new FormData();
		formData.left = new FormAttachment(serverLabel, 10);
		formData.right = new FormAttachment(100, -10);
		formData.top = new FormAttachment(databaseText, 10);
		userText.setLayoutData(formData);

		// Layout for passwordText.
		formData = new FormData();
		formData.left = new FormAttachment(serverLabel, 10);
		formData.right = new FormAttachment(100, -10);
		formData.top = new FormAttachment(userText, 10);
		passwordText.setLayoutData(formData);

		// Layout for noDbButton;
//		FormData noDbFormData = new FormData();
//		noDbFormData.bottom = new FormAttachment(100, -10);
//		noDbFormData.left = new FormAttachment(serverLabel, 10);
//		noDbButton.setLayoutData(noDbFormData);

		// Layout for cancelButton;
		FormData cancelFormData = new FormData();
		cancelFormData.bottom = new FormAttachment(100, -10);
		cancelFormData.right = new FormAttachment(100, -10);
		cancelButton.setLayoutData(cancelFormData);

		// Layout for loginButton;
		FormData loginFormData = new FormData();
		loginFormData.bottom = new FormAttachment(100, -10);
		loginFormData.right = new FormAttachment(cancelButton, -10);
		loginButton.setLayoutData(loginFormData);
		
		selectServerCombo.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e) {
	        	String dbServer = selectServerCombo.getText();
	        	System.out.println(dbServer);
	        	
	        	if (dbServer.equals("<select>")) {
	        		serverText.setText("<select>");
	        	}
	        	else if (dbServer.equals("PROD: er-postgresql.smhi.se")) {
	        		serverText.setText("er-postgresql.smhi.se");
	        	}
	        	else if (dbServer.equals("TEST: er-postgresql-tst.smhi.se")) {
	        		serverText.setText("er-postgresql-tst.smhi.se");
	        	}
	        	else if (dbServer.equals("LOCAL: localhost")) {
	        		serverText.setText("localhost");
	        	}
	        }
        });
	        	
		if (userText.getText().equals("")) {
			userText.setFocus();
		} else {
			passwordText.setFocus();			
		}

		shell.open();

		while (!shell.isDisposed()) {
			if (!parentDiplay.readAndDispatch()) {
				parentDiplay.sleep();
			}
		}
//		diplay.dispose();

		return loggedIn;
	}

}
