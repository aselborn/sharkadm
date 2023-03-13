/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package sharkadm.fileimport.misc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class FileImportChangelog {
	
	private String publicChangelogComment = "";	
	
	@SuppressWarnings("unused")
	private FileImportChangelog() { // Default constructor not used.
		
	}

	public FileImportChangelog(String importFileName) {
		
		// This file should be a part of the zip:ed import file.
		Path filePath = Paths.get(importFileName, "processed_data", "change_log.txt");
		if (Files.notExists(filePath)) {		
			return;
		}	
		
		// Put value in publicChangelogComment.
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath.toFile()));
			parseChangelogFile(bufferedReader);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getPublicChangelogComment() {
		return publicChangelogComment;
	}

	// Reads the public change log comment.
	private void parseChangelogFile(BufferedReader in) {
		try {
			String line;
			int commentIndex;
			
			while ((line = in.readLine()) != null) {				
				// Remove comments.
				commentIndex = line.indexOf("#", 0);
				if (commentIndex > 0) {
					line = line.substring(0, commentIndex);
				}
				
//				String[] rowItems = line.split(":");
				String[] rowItems = line.split(Pattern.quote(":"));
			
				if ((rowItems.length >= 1) && 
					(rowItems[0].trim().equals("public_comment"))) {
					if (rowItems.length >= 2) { 
							publicChangelogComment = rowItems[1].trim();
							return;	// One comment is enough.
						}
				}
			}
			in.close();
			
		} catch (FileNotFoundException e) {
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("FileImportChangelog: File not found. ");
			messageBox.setMessage("Error: " + e.getMessage());
			messageBox.open();
			e.printStackTrace();
//			System.exit(-1);
		} catch (Exception e) {
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("FileImportChangelog: Exception in parseChangelogFile(). ");
			messageBox.setMessage("Error: " + e.getMessage());
			messageBox.open();
			e.printStackTrace();
//			System.exit(-1);
		}
	}

}
