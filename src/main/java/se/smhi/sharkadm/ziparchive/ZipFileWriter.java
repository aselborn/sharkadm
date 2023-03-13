/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.ziparchive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.bean.util.OpencsvUtils;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

import se.smhi.sharkadm.translate.TranslateCodesManager_NEW;
import se.smhi.sharkadm.utils.ParseFileUtil;

/**
 * Utility class used to produce SHARK archive files.
 */
public class ZipFileWriter {

	//Some kind of logger.
	private static Logger mLog = Logger.getLogger("mprog-log");
	

	public static void createZipArchive(String zipFileName, 
										String datatype, 
										String fromPath, 
										String toPath, 
										ZipArchiveGenerateData dataVisitor,
										ZipArchiveGenerateDataColumns dataColumnsVisitor,
										ZipArchiveGenerateMetadataAuto metadataVisitor) {
		try {

			FileHandler fh = new FileHandler("mprog-log.txt");
			mLog.addHandler(fh);
			SimpleFormatter fmtsimple = new SimpleFormatter();
			fh.setFormatter(fmtsimple);

			// Out directory.
			Path outPath = Paths.get(toPath);
	        // Check if directory exists?
	        if (!Files.exists(outPath)) {
	            try {
	            	// Create directory and parents.
	                Files.createDirectories(outPath);
	            } catch (IOException e) {
	                // Fail to create directory.
	                e.printStackTrace();
	                return;
	            }
	        }
	        
	        // Create zip file.
	        String zipName = toPath.concat("\\").concat(zipFileName);
	        ZipOutputStream zipArchiveStream = new ZipOutputStream(new FileOutputStream(zipName));

	        // Add README files.
	        String readmeEn = getReadme(datatype, "en");
	        String readmeSv = getReadme(datatype, "sv");
	        
	        zipArchiveStream.putNextEntry(new ZipEntry("README.txt"));
            byte[] readmeEnBytes = readmeEn.getBytes();
            zipArchiveStream.write(readmeEnBytes, 0, readmeEn.length());
            zipArchiveStream.closeEntry();
            
	        zipArchiveStream.putNextEntry(new ZipEntry("README_sv.txt"));
	        byte[] readmeSvBytes = readmeSv.getBytes();
            zipArchiveStream.write(readmeSvBytes, 0, readmeSv.length());
            zipArchiveStream.closeEntry();
            
	        // Add manually added metadata.
//	        zipArchiveStream.putNextEntry(new ZipEntry("shark_metadata.txt"));
//            byte[] bytes = Files.readAllBytes(Paths.get(fromPath.concat("\\shark_metadata.txt")));
//            zipArchiveStream.write(bytes, 0, bytes.length);
//            zipArchiveStream.closeEntry();
    		try {
    			byte[] bytes = Files.readAllBytes(Paths.get(fromPath.concat("\\shark_metadata.txt")));
    			// Continue if not NoSuchFileException.
                zipArchiveStream.putNextEntry(new ZipEntry("shark_metadata.txt"));
                zipArchiveStream.write(bytes, 0, bytes.length);
                zipArchiveStream.closeEntry();
    		} catch (IOException e) { 
    			e.printStackTrace();
    		}
            
	        // Add automatically added metadata.
	        zipArchiveStream.putNextEntry(new ZipEntry("shark_metadata_auto.txt"));
			for (String row : metadataVisitor.metadataFileContent) {
				String row_and_cr = row + "\r\n"; // New line (windows style).  	
				byte[] data = row_and_cr.getBytes();
				zipArchiveStream.write(data, 0, row_and_cr.length());
			}
            zipArchiveStream.closeEntry();
	        
			// Add calculated shark_data.txt file.
            zipArchiveStream.putNextEntry(new ZipEntry("shark_data.txt"));
			for (String row : dataVisitor.dataFileContent) {
				String row_and_cr = row + "\r\n"; // New line (windows style).  	
				byte[] data = row_and_cr.getBytes();
				zipArchiveStream.write(data, 0, row_and_cr.length());
			}
			zipArchiveStream.closeEntry();
			
			// Add calculated shark_data_columns.txt file. Will not be added to zip if empty.
			if (dataColumnsVisitor.dataFileContent.size() > 0) {
				zipArchiveStream.putNextEntry(new ZipEntry("shark_data_columns.txt"));
				for (String row : dataColumnsVisitor.dataFileContent) {
					String row_and_cr = row + "\r\n"; // New line (windows style).  	
					byte[] data = row_and_cr.getBytes();
					zipArchiveStream.write(data, 0, row_and_cr.length());
				}
				zipArchiveStream.closeEntry();
			}
			
			
			
			// Add code translations file.
			zipArchiveStream.putNextEntry(new ZipEntry("translate_codes.txt"));
			for (String row : TranslateCodesManager_NEW.instance().getUsedRows()) {
				String row_and_cr = row + "\r\n"; // New line (windows style).  	
				byte[] data = row_and_cr.getBytes();
				zipArchiveStream.write(data, 0, row_and_cr.length());
			}
			zipArchiveStream.closeEntry();
			
			
			
			// Add all files and directories in "processed_data". Recursive calls to addDirsAndFilesToZip().
			File[] files = new File(fromPath.concat("\\processed_data")).listFiles();
			String parentDir = "processed_data";
			addDirsAndFilesToZip(files, zipArchiveStream, parentDir);
			
			// Add all files and directories in "received_data". Recursive calls to addDirsAndFilesToZip().
			if (Files.exists(Paths.get(fromPath.concat("\\received_data")))) {
				files = new File(fromPath.concat("\\received_data")).listFiles();
				parentDir = "received_data";
				addDirsAndFilesToZip(files, zipArchiveStream, parentDir);
			}
			
            zipArchiveStream.close();
            
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getReadme(String datatype, String language) {

		// Path to README template.
		String readmeTemplate = "\\\\winfs\\data\\prodkap\\sharkweb\\SHARK_CONFIG\\readme_files\\" + 
								"README_" + datatype + "_" + language + ".txt";
		String readmeContent = "";
		try {
			readmeContent = new String(Files.readAllBytes(Paths.get(readmeTemplate)));
		} catch (IOException e) { }
		
		if (!readmeContent.equals("")) {				
			return readmeContent;
		}
		
		// Return default texts depending on language.
		if (language.equals("sv")) {
			return  "Denna fil inneh�ller marin milj��vervakningsdata som levererats till datav�rden SMHI." + 
					"\r\n\r\n" + 
					"Information: https://www.smhi.se/klimatdata/oceanografi/havsmiljodata" + 
					"\r\n\r\n" + 
					"Kontakt: shark@smhi.se";
		} else {
			return  "This file contains marine environmental monitoring data delivered to the Swedish National data host for oceanographic and marine biological environmental data." + 
					"\r\n\r\n" + 
					"Info at: https://www.smhi.se/klimatdata/oceanografi/havsmiljodata" + 
					"\r\n\r\n" + 
					"Contact: shark@smhi.se";			
		}
	}
	
	public static void addDirsAndFilesToZip(File[] files, ZipOutputStream zipArchiveStream, String parentDir) {
	    
		for (File file : files) {
			
			boolean isMprogMissing = false;
			String processedFile = file.getAbsolutePath();
			String zipEntry = parentDir.concat("\\").concat(file.getName());

	        if (file.isDirectory()) {
				addDirsAndFilesToZip(file.listFiles(), zipArchiveStream, parentDir); // Recursive call.
	        } else {
	            try {
					
					if (file.getName().equals("data.txt")){
						String mProg = mprogCodeMissing(Paths.get(file.getAbsolutePath().toString()));

						if (mProg != null){

							mLog.log(Level.INFO, "The file ".concat(processedFile)
							.concat(" does not have MPROG column! program has fetched = "
							.concat(mProg).concat(" from delivery_note.txt")));

							insertMprogToData(Paths.get(file.getAbsolutePath()), mProg);

							isMprogMissing=true;
						} else {
							List<Mprog> listMissing = verifyExistingMprogCode(Paths.get(file.getAbsolutePath().toString()));
							if (listMissing.size() > 0){
								
								mLog.log(Level.INFO, "The file ".concat(processedFile)
								.concat(" has MPROG, but in ".concat(String.valueOf(listMissing.size()).concat(" cases the MPROG is empty"))));
								isMprogMissing=true;
								//Fix this file. Write mprog_data.txt
								insertSelectiveMprogToData(Paths.get(file.getAbsolutePath()), listMissing);
							}
						}
					}

					byte[] bytes = null;
					if (isMprogMissing){

						String fileToRead = processedFile.replace("data.txt", "mprog_data.txt");
						bytes = Files.readAllBytes(Paths.get(fileToRead));
						zipArchiveStream.putNextEntry(new ZipEntry(zipEntry.replace("mprog_data.txt", "data.txt")));

					} else{
						
						if (zipEntry.contains("mprog_data.txt"))
							continue;
						
						bytes = Files.readAllBytes(Paths.get(processedFile));
						zipArchiveStream.putNextEntry(new ZipEntry(zipEntry));
					}
					
				    zipArchiveStream.write(bytes, 0, bytes.length);
					zipArchiveStream.closeEntry();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	    }
	}

	/*
	 * This method will insert mprogTag to specific rows to mprog_data.txt file.
	 */
	private static void insertSelectiveMprogToData(Path path, List<Mprog> listMissing ){

		String filePathData = path.toString();
		String outputFile = path.toString().replace("data.txt", "mprog_data.txt");

		try {
			CSVParser csvParser = new CSVParserBuilder()
				.withSeparator('\t')
				.withIgnoreQuotations(true)
				.build();

			CSVReader csvReader = new CSVReaderBuilder(new FileReader(filePathData))
					//.withSkipLines(1)
					.withCSVParser(csvParser)
					.build();
			
			try {
				ICSVWriter writer = new CSVWriterBuilder(new FileWriter(outputFile))
						.withSeparator('\t')
						.withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
						.build();

				int rowCounter = 0;
				int colMprog = 0;
				List<String[]> allFileLines = csvReader.readAll();
				int listKeyCount = 0;
				
				int rowKey = 0;
				Mprog myMprog = null;
				for (String[] rowData : allFileLines){
					
					if (rowKey == 0){
						if ( !(listKeyCount >= listMissing.size()) ){
							myMprog = listMissing.get(listKeyCount);
							rowKey = myMprog.getRowNo();
						}
							
					}
				
					ArrayList<String> csvList = new ArrayList<String>(Arrays.asList(rowData));
					
					if (rowCounter == 0){
						colMprog = csvList.indexOf("MPROG");
					}
					
					if (rowCounter == rowKey){
						csvList.set(colMprog, myMprog.getmProg());
						rowKey=0;
						listKeyCount++;
					} 

					rowCounter++;
					writer.writeNext(listToSTringArray(csvList));
				}
				
				writer.close();

			} catch (IOException  | CsvException e) {
				e.printStackTrace();
			}
			

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static String[] listToSTringArray(List<String> csvList){
		
		Object[] myData = csvList.toArray(new String[csvList.size()]);
		String[] stringArray = Arrays.copyOf(myData, myData.length, String[].class);

		return stringArray;
	}

	

	/*
	 * This method will insert the mProgTag to mprog_data.txt file.
	 */
	private static void insertMprogToData(Path path, String mProgCode) {
		
		String filePathData = path.toString();
		String outputFile = path.toString().replace("data.txt", "mprog_data.txt");
		try {

			CSVParser csvParser = new CSVParserBuilder()
				.withSeparator('\t')
				.withIgnoreQuotations(true)
				.build();
		
			CSVReader csvReader = new CSVReaderBuilder(new FileReader(filePathData))
				//.withSkipLines(1)
				.withCSVParser(csvParser)
				.build();

			ICSVWriter writer = new CSVWriterBuilder(new FileWriter(outputFile))
				.withSeparator('\t')
				.withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
				.build();
				int cnt = 0;

				String[] entries = null;
				while ((entries = csvReader.readNext()) != null) {
    			
				ArrayList<String> list = new ArrayList<String>(Arrays.asList(entries));
				
					if (cnt == 0){
						list.add("MPROG");
					} else{
						list.add(mProgCode); // Add the new element here
					}
					int cols = list.size();
					Object[] myData = list.toArray(new String[cols]);
					String[] stringArray = Arrays.copyOf(myData, myData.length, String[].class);
    			
					writer.writeNext(stringArray);
					cnt++;
				}
		
				writer.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CsvException e) {
			e.printStackTrace();
		}
		
	}

	/*
	 * Returns the content from "Övervakningsprogram: " in delivery_note.txt
	 */
	private static String getMprogValueFromDeliveryNote(Path inPath){

		String mprogValue = "";
		String filePathData = inPath.toString();
		String filePathDelivery = filePathData.replace("data.txt", "delivery_note.txt");
		try {
			BufferedReader delivery = new BufferedReader(new InputStreamReader(new FileInputStream(filePathDelivery), "Cp1252"));

			String line = null;

			while ((line = delivery.readLine()) != null) {
    			System.out.println(line);
				if (line.contains("övervakningsprogram:")){
					String p[] = line.split(":");
					mprogValue = p[1].trim();
					continue;
				}
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return mprogValue;
	}

	/*
	 *  If the data.txt file does contain MPROG, but the value for this file is empty
	 *  The data-value should be picked from delivery_note.txt
	 */
	
	private static List<Mprog> verifyExistingMprogCode(Path path) {
		
		String mProgCode = getMprogValueFromDeliveryNote(path);
		String filePathData = path.toString();
		
		//Map<Integer, String> mprogList = new HashMap<Integer, String>();
		List<Mprog> mprogList = new ArrayList<Mprog>();

		try {
			BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(filePathData), "Cp1252"));

			String firstLine = data.readLine();

			if (firstLine.contains("MPROG")){
				CSVParser csvParser = new CSVParserBuilder()
				.withSeparator('\t')
				.withIgnoreQuotations(true)
				.build();

				CSVReader csvReader = new CSVReaderBuilder(new FileReader(filePathData))
				//.withSkipLines(1)
				.withCSVParser(csvParser)
				.build();

				String[] entries = null;
				int colIndex = 0;
				List<String> columnData = new ArrayList<String>();
				
				int cnt = 0;

				try {
					while ((entries = csvReader.readNext()) != null) {
						if (cnt ==0){
							columnData = Arrays.asList(entries);
							colIndex = columnData.indexOf("MPROG");	
						}
					
						String currentMprogValue = entries[colIndex];
						if (currentMprogValue.length() == 0){ // The value is empty. Fetch it from delivery_note.txt
							//mprogList.put(cnt, mProgCode);
							Mprog mprog = new Mprog();
							mprog.setRowNo(cnt);
							mprog.setmProg(mProgCode);
							mprogList.add( mprog);
						}
						cnt++;
					}
				} catch (CsvValidationException e) {
					e.printStackTrace();
				}
			}

		} catch (UnsupportedEncodingException e) {
			
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		return mprogList;
	}

	/*
	 * If the data.txt file does not contain MPROG column. 
	 * This column and data should be added from delivery_note.txt
	 */
	private static String mprogCodeMissing(Path path){

		String mProgCode = null;

		try {
			String filePathData = path.toString();
			BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(filePathData), "Cp1252"));

			String firstLine = data.readLine();

			if (!firstLine.contains("MPROG")){
				data.close();
				return getMprogValueFromDeliveryNote(path);
			}

			data.close();

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return mProgCode;
	}
	
}
