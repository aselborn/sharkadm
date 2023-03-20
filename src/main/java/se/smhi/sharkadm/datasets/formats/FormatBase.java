/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.formats;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.opencsv.*;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import se.smhi.sharkadm.datasets.fileimport.FileImportInfo;
import se.smhi.sharkadm.datasets.fileimport.FileImportUtils;
import se.smhi.sharkadm.facades.ModelFacade;
import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;
import se.smhi.sharkadm.utils.ErrorLogger;
import se.smhi.sharkadm.utils.ParseFileUtil;
import se.smhi.sharkadm.verifydata.ColumnCode;

/**
 * Base class for import scripts. 
 * Import scripts based on this abstract class should:
 * - import data on the form Dataset - Visit - Sample - Variable,
 * - use the key translation mechanism in the file "import_matrix_<datatype>.txt",
 * - support the control flow defined in the class FileImportManager.    
 */
public abstract class FormatBase {
	
	protected Dataset dataset; // Top node for current import.
	protected Visit currentVisit = null;
	protected Sample currentSample = null;
	protected Variable currentVariable = null;

	// TODO: Implement generic file reader connected to import format.
	protected Map<String, String> keyTranslate = new HashMap<String, String>();
	//
	protected String visitKeyColumns = "";
	protected String sampleKeyColumns = "";
	
	protected PrintStream logInfo;	
	protected FileImportInfo importInfo;
	protected FileImportUtils utils;

	private static Logger mLog = Logger.getLogger("mprog-log");
	public FormatBase(PrintStream logInfo, FileImportInfo importInfo) {
		this.logInfo = logInfo;
		this.importInfo = importInfo;
		this.utils = new FileImportUtils(importInfo);
	}

	/*
		A shared base function to verify that DATA.txt contains "MPROG" tag.
	 */
	protected BufferedReader verifyDataFile(File inFile, String colCodeMissing){

		boolean isCodeMissing = false;
		BufferedReader bufReader = null;

		String missingCode = codeMissing(inFile.toPath(), colCodeMissing);

		if (missingCode != null){

			mLog.log(Level.INFO, "The file ".concat(inFile.getAbsolutePath())
					.concat(" does not have " .concat(colCodeMissing).concat(" column! program has fetched = "
							.concat(missingCode).concat(" from delivery_note.txt"))));

			isCodeMissing =true;
			return insertCodeToData(Paths.get(inFile.getAbsolutePath()), colCodeMissing,missingCode);

		} else {
			List<ColumnCode> listMissing = verifyColumnName(Paths.get(inFile.getAbsolutePath().toString()), colCodeMissing);
			if (listMissing.size() > 0){

				mLog.log(Level.INFO, "The file ".concat(inFile.getAbsolutePath())
						.concat(" has ".concat(colCodeMissing).concat(" , but in ".concat(String.valueOf(listMissing.size()).concat(" cases the ".concat(colCodeMissing).concat(" is empty"))))));

				isCodeMissing =true;

				return insertSelectiveCodeToData(Paths.get(inFile.getAbsolutePath()), listMissing, colCodeMissing);
			}

		}

		return null;
	}



	/*
		if any file is ending with .mprog. Remove the original file and rename this to the original filename.
	 */


	/*
	 * This method will insert mprogTag to specific rows to mprog_data.txt file.
	 */
	private BufferedReader insertSelectiveCodeToData(Path path, List<ColumnCode> listMissing, String code ){

		String filePathData = path.toString();
		String outputFile = "";
		try {
			outputFile =   Files.createTempFile("tempfil-",".tmp").toFile().getAbsolutePath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try {
			CSVParser csvParser = new CSVParserBuilder()
					.withSeparator(getSeparator(filePathData))
					.withIgnoreQuotations(true)
					.build();

			CSVReader csvReader = new CSVReaderBuilder(new FileReader(filePathData))
					//.withSkipLines(1)
					.withCSVParser(csvParser)
					.build();

			try {
				ICSVWriter writer = new CSVWriterBuilder(new FileWriter(outputFile))
						.withSeparator(getSeparator(filePathData))
						.withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
						.build();

				int rowCounter = 0;
				int colCodeMissing = 0;
				List<String[]> allFileLines = csvReader.readAll();
				int listKeyCount = 0;

				int rowKey = 0;
				ColumnCode myColMissing = null;
				for (String[] rowData : allFileLines){

					if (rowKey == 0){
						if ( !(listKeyCount >= listMissing.size()) ){
							myColMissing = listMissing.get(listKeyCount);
							rowKey = myColMissing.getRowNo();
						}

					}

					ArrayList<String> csvList = new ArrayList<String>(Arrays.asList(rowData));

					if (rowCounter == 0){
						//colCodeMissing = csvList.indexOf("MPROG");
						colCodeMissing = csvList.indexOf(code);
					}

					if (rowCounter == rowKey){
						csvList.set(colCodeMissing, myColMissing.getColCode());
						rowKey=0;
						listKeyCount++;
					}

					rowCounter++;
					writer.writeNext(listToSTringArray(csvList));
				}

				writer.flush();
				writer.close();
				writer = null;

				return new BufferedReader(new FileReader(outputFile));

			} catch (IOException  | CsvException e) {
				e.printStackTrace();
			}


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	private static String[] listToSTringArray(List<String> csvList){

		Object[] myData = csvList.toArray(new String[csvList.size()]);
		String[] stringArray = Arrays.copyOf(myData, myData.length, String[].class);

		return stringArray;
	}

	private List<ColumnCode> verifyColumnName(Path path, String colCodeMissing) {

		String mProgCode = getMprogValueFromDeliveryNote(path);
		String filePathData = path.toString();

		List<ColumnCode> mprogList = new ArrayList<ColumnCode>();

		try {
			BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(filePathData), "Cp1252"));

			String firstLine = data.readLine();

			if (firstLine.contains(colCodeMissing)){
				CSVParser csvParser = new CSVParserBuilder()
						.withSeparator(getSeparator(filePathData))
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
							colIndex = columnData.indexOf(colCodeMissing);
						}

						String currentMprogValue = entries[colIndex];
						if (currentMprogValue.length() == 0){ // The value is empty. Fetch it from delivery_note.txt

							ColumnCode colCode = new ColumnCode();
							colCode.setRowNo(cnt);
							colCode.setColCode(mProgCode);
							mprogList.add(colCode);
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


	private char getSeparator(String filePathData){

		char sep = '\t'; // the default separator (.txt-files)

		String extension = com.google.common.io.Files.getFileExtension(filePathData);

		if (extension.compareTo("skv") == 0){
			sep = ';';
		}

		return sep;
	}

	/*
	 * This method will create a correct file in form of a temp-file.
	 */
	private BufferedReader insertCodeToData(Path path, String columnCode, String parsedValue) {

		String filePathData = path.toString();
		String outputFile ="";

		try {
			outputFile = Files.createTempFile("TempFil", ".tmp").toFile().getAbsolutePath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try {

			CSVParser csvParser = new CSVParserBuilder()
					.withSeparator(getSeparator(filePathData))
					.withIgnoreQuotations(true)
					.build();

			CSVReader csvReader = new CSVReaderBuilder(new FileReader(filePathData))
					//.withSkipLines(1)
					.withCSVParser(csvParser)
					.build();

			ICSVWriter writer = new CSVWriterBuilder(new FileWriter(outputFile))
					.withSeparator(getSeparator(filePathData))
					.withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
					.build();
			int cnt = 0;

			String[] entries = null;
			while ((entries = csvReader.readNext()) != null) {

				ArrayList<String> list = new ArrayList<String>(Arrays.asList(entries));

				if (cnt == 0){
					list.add(columnCode);
				} else{
					list.add(parsedValue); // Add the new element here
				}
				int cols = list.size();
				Object[] myData = list.toArray(new String[cols]);
				String[] stringArray = Arrays.copyOf(myData, myData.length, String[].class);

				writer.writeNext(stringArray);
				cnt++;
			}

			writer.close();
			return new BufferedReader(new FileReader(outputFile));


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CsvException e) {
			e.printStackTrace();
		}

		return null;

	}

	private String codeMissing(Path path, String colCode){

		String colCodeReturned = null;

		try {
			String filePathData = path.toString();
			BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(filePathData), "Cp1252"));

			String firstLine = data.readLine();

			if (!firstLine.contains(colCode)){
				data.close();
				return getMprogValueFromDeliveryNote(path);
			}

			data.close();

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return colCodeReturned;
	}

	/*
	 * Returns the content from "Övervakningsprogram: " in delivery_note.txt
	 * This is hard-coded.
	 */
	private String getMprogValueFromDeliveryNote(Path inPath){

		String mprogValue = "";
		String filePathData = inPath.toString();

		//We expect a file called 'delivery_note.txt' to exist!
		String filePathDelivery = inPath.getParent().toString().concat("\\").concat("delivery_note.txt");
		File f = new File(filePathDelivery);
		if (!f.exists()){
			mLog.log(Level.WARNING, "The file delivery_note.txt does not exist in folder : ".concat(inPath.toFile().getParent()));
			return "";
		}
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

	// These abstract functions must be implemented in sub-classes.


	public abstract void importFiles(String zipFileName, Dataset dataset);
	
	public abstract void getCurrentVisit(String[] row);
	
	public abstract void getCurrentSample(String[] row);
	
	public abstract void getCurrentVariable(String[] row);

	// These abstract functions must be implemented in sub-classes.
	public abstract void postReorganizeDataset(Dataset dataset);
	
	public abstract void postReorganizeVisit(Visit visit);
	
	public abstract void postReorganizeSample(Sample sample);
	
	public abstract void postReorganizeVariable(Variable variable);
	
	// These abstract functions must be implemented in sub-classes.
	public abstract void postReformatDataset(Dataset dataset);
	
	public abstract void postReformatVisit(Visit visit);
	
	public abstract void postReformatSample(Sample sample);
	
	public abstract void postReformatVariable(Variable variable);	

	public String translateKey(String key) {
		if (keyTranslate.containsKey(key)) {
			return keyTranslate.get(key);
		}
		return "MISSING KEY: " + key;
	}	
	
	public int getTranslateKeySize() {
		return keyTranslate.size();
	}	
	
	public void loadKeyTranslator(String formatColumnName, String importMatrixFileName) {
		List<String[]> fileContent;
		String[] header = null;
		int usedFormatColumn = -1;
		ClassLoader classLoader = this.getClass().getClassLoader();
		InputStream inputStream;
		BufferedReader bufferedReader;
		
		try {
			// Checks if file exist outside jar bundle. Use this one first, if exists.
			
//			bufferedReader = ParseFileUtil.GetSharkConfigFile("import_matrix.txt");
			bufferedReader = ParseFileUtil.GetSharkConfigFile(importMatrixFileName);

//			File external_file = new File("SHARK_CONFIG/import_matrix.txt");
//			if (external_file.exists()) {
//				bufferedReader = new BufferedReader(new FileReader(external_file));
//			} else {
//				// File is bundled in jar.
//				inputStream = classLoader
//						.getResourceAsStream("SHARK_CONFIG/import_matrix.txt");
//				bufferedReader = new BufferedReader(new InputStreamReader(
//						inputStream));
//			}

			fileContent = ParseFileUtil.parseDataFile(bufferedReader, true);
			if (fileContent != null) {
				keyTranslate.clear();
				for (String[] row : fileContent) {
					if (header == null) {
						header = row; // Header row in imported file.
						for (int i = 0; i < row.length; i++) {
							if (row[i].equals(formatColumnName)) {
								usedFormatColumn = i;
							}
						}
						if (usedFormatColumn < 0) {
//							importInfo.addConcatError("Can't find expected format column in the import format file (SHARK_CONFIG/import_matrix.txt).");
							importInfo.addConcatError("Can't find expected format column in the import format file (SHARK_CONFIG/" + importMatrixFileName + ").");
						}
					} else {
						
						try {
						
							if ((usedFormatColumn > 0) && // If not right column was found.
								(row.length > usedFormatColumn) && // Some rows are shorter or empty.
								(!row[usedFormatColumn].equals(""))) {	
								
	//							System.out.println("DEBUG: row-item: " + row[usedFormatColumn]);
	
								if (row[0].equals("VISIT_KEY")) {
									visitKeyColumns = row[usedFormatColumn];
								}
								else if (row[0].equals("SAMPLE_KEY")) {
									sampleKeyColumns = row[usedFormatColumn];
								}
								else if (row[usedFormatColumn].contains("<or>")) {
									// Separator exists.
									for (String colName : row[usedFormatColumn].split(Pattern.quote("<or>"))) {
										if (!colName.equals("")) {
											keyTranslate.put(colName.trim(), row[0]);
										}
									}
								} 
								else if ((row[usedFormatColumn].length() >= 5) && 
										 (row[usedFormatColumn].substring(0, 5).contains("<not>"))) {
									// Separator character <-> in the first column = not to be used.
									for (String colName : row[usedFormatColumn].split(Pattern.quote(Pattern.quote("<not>")))) {
										if (!colName.equals("")) {
											keyTranslate.put(colName.trim(), "NOT_USED");										
										}
									}
								}
								else if (row[usedFormatColumn].contains("<|>")) {
									// Separator exists.
	//								for (String colName : row[usedFormatColumn].split("<|>")) {
									for (String colName : row[usedFormatColumn].split(Pattern.quote("<|>"))) {
										if (!colName.equals("")) {
											keyTranslate.put(colName.trim(), row[0]);
										}
									}
								} 
								else if ((row[usedFormatColumn].length() >= 3) && 
										 (row[usedFormatColumn].substring(0, 3).contains("<->"))) {
									// Separator character <-> in the first column = not to be used.
	//								for (String colName : row[usedFormatColumn].split("<->")) {
									for (String colName : row[usedFormatColumn].split(Pattern.quote("<->"))) {
										if (!colName.equals("")) {
											keyTranslate.put(colName.trim(), "NOT_USED");										
										}
									}
								} 
								else {
									// Does not contain separator character.
									keyTranslate.put(row[usedFormatColumn], row[0]);
								}
							}
						} catch (Exception e) {
							importInfo.addConcatError("Failed to load import format file. First column: " + row[0] + ".");
						}
					}
				}
			}
			
			ErrorLogger.println("Debug: KeyTranslator column: " + formatColumnName + ". Size: " + keyTranslate.size());
			
		} catch (Exception e) {
			// Note: It is not recommended to put a message dialog here. Should be in the UI layer.
			importInfo.addConcatError("Failed to load import format file. KeyTranslator column: " + formatColumnName + ".");
		}
		
		// Used by the Observer pattern.
		ModelFacade.instance().modelChanged();
	}

}
