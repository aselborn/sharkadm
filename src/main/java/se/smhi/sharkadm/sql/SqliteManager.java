package se.smhi.sharkadm.sql;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.utils.SharkAdmConfig;

import java.io.*;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SqliteManager {


    public static SqliteManager instance =null ;
    private Connection mConnection = null;
    //private Path mPathToDb = null;
    //private static String dbFileName = "sharkadm.db";

    private DbQuery mDbQuery = null;
    private SqliteManager(){
        mDbQuery = new DbQuery();
        mConnection = ConnectionManager.getInstance().getConnection();

    }

    private List<String> createTranslateCodeTable(String tableCode){

        //dropTable("translate_codes_NEW");
        dropTable("tableCode");

        List<String> columnList = tranlateColumnsAsList(tableCode);

        StringBuilder bu = new StringBuilder();
        bu.append(" CREATE TABLE ".concat(tableCode).concat( "("));
        bu.append(" id INTEGER PRIMARY KEY AUTOINCREMENT,  ");
        for (String col : columnList){
            bu.append(col.concat(" TEXT ,")); // ALL fields of type of TEXT .
        }

        bu.replace(bu.length()-1, bu.length(), ")");

        try (Statement stmt = mConnection.createStatement()) {
            stmt.execute(bu.toString());

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return columnList;
    }

    /*
        Let's read translate_codes_NEW.txt to parse actual columns.
     */
    private List<String> tranlateColumnsAsList(String property){
        File configFile = null;
        String pathToConfig = SharkAdmConfig.getInstance().getProperty(property);
        List<String> columns = new ArrayList<>();
        if (pathToConfig != null){

            configFile = new File(pathToConfig);
            if (!configFile.exists()){
                throw new RuntimeException(new Exception("The configuration file "
                        .concat(configFile.toString()).concat( " is missing!!")));
            }
        }
        try {

            BufferedReader reader = new BufferedReader(new FileReader(configFile.getAbsolutePath()));

            try {
                String colRow = reader.readLine();
                columns = new ArrayList<String>(Arrays.asList(colRow.split("\t")));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }


        return columns;
    }

    public static synchronized SqliteManager getInstance()
    {
        if (instance == null)
            synchronized (SqliteManager.class){
                if (instance == null){
                    instance = new SqliteManager();
                }
            }

        return instance;
    }


    /*
        The text-file is inserted to a representation in the sqlite database.
     */
    public void fillTable(Path fileName) {
        List<String> columnList = null;

        //Some files are saved in DB for easy queries.
        if (fileName.toString()
                .contains("translate_codes_NEW") || fileName.toString()
                .contains("translate_headers") || fileName.toString()
                .contains("column_info") || fileName.toString()
                .contains("translate_all_columns") || fileName.toString()
                .contains("translate_parameters"))
        {
            columnList = createTranslateCodeTable(fileName.getFileName().toString().replace(".txt", ""));
            insertTranslateCodes(fileName, columnList);
        }

    }
    private void insertTranslateCodes(Path fileName, List<String> columnList) {

        StringBuilder bu = new StringBuilder();

        CSVParser csvParser = new CSVParserBuilder()
                .withSeparator('\t')
                .withIgnoreQuotations(true)
                .build();
        try {

            CSVReader csvReader = new CSVReaderBuilder(new FileReader(fileName.toFile()))
                    .withSkipLines(1)
                    .withCSVParser(csvParser)
                    .build();

            String[] entries = null;

            bu.append(" INSERT INTO ".concat(fileName.getFileName().toString().replace(".txt", "")).concat( "("));
            for (String col : columnList){
                bu.append(col.concat(","));
            }
            bu.replace(bu.length() -1, bu.length(), ")");
            bu.append(" VALUES (");
            for (String col : columnList){
                bu.append("?,");
            }
            bu.replace(bu.length() -1, bu.length(), ")");

            PreparedStatement psmtm = mConnection.prepareStatement(bu.toString());
            mConnection.setAutoCommit(false);
            while ((entries = csvReader.readNext()) != null) {

                ArrayList<String> list = new ArrayList<String>(Arrays.asList(entries));

                boolean allEmpty = list.stream().allMatch(s->s.length() == 0);
                if (!allEmpty){
                    for (int i = 0; i< columnList.size(); i++){
                        psmtm.setString(i+1,list.get(i));
                    }

                    psmtm.addBatch();
                }
            }

            int[] x = psmtm.executeBatch();
            mConnection.commit();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    private void dropTable(String tableToDrop) {
        mDbQuery.dropTable(tableToDrop);
    }

    public String getTranslateCodeColumnValue(String projectCode, Sample sample, String nameOfColumn) {
        return mDbQuery.getTranslateCodeColumnValue(projectCode, sample, nameOfColumn);
    }

    public TranslateHeaderDto getTranslateHeaderInternalKeyRowFromShortColumn(String shortColumn){
        return mDbQuery.getTranslateHeaderInternalKeyRowFromShortColumn(shortColumn);
    }

    public String getTranslateCodeColumnValue(String projectCode, String Code, String nameOfColumn){
        return mDbQuery.getTranslateCodeColumnValue(projectCode, Code, nameOfColumn);
    }

    public void translateHeaders() {
        dropTable("translate_headers");
        String pathToFile = SharkAdmConfig.getInstance().getProperty("translate_headers");
        fillTable(new File(pathToFile).toPath());

    }

    public void columnInfo() {
        dropTable("column_info");
        String pathToFile = SharkAdmConfig.getInstance().getProperty("column_info");
        fillTable(new File(pathToFile).toPath());
    }

    public void translateAllColumns() {
        dropTable("translate_all_columns");
        String pathToFile = SharkAdmConfig.getInstance().getProperty("translate_all_columns");
        fillTable(new File(pathToFile).toPath());
    }

    public void translateCodesNew() {
        dropTable("translate_codes_NEW");
        String pathToFile = SharkAdmConfig.getInstance().getProperty("translate_codes_NEW");
        fillTable(new File(pathToFile).toPath());
    }

    public void translateParameters() {
        dropTable("translate_parameters");
        String pathToFile = SharkAdmConfig.getInstance().getProperty("translate_parameters");
        fillTable(new File(pathToFile).toPath());
    }

    protected List<TranslateCodesNewDto> getTranslateCodeNewDto(String code, String filter) {
        return mDbQuery.getTranslateCodeNewDto(code, filter);
    }

    protected List<String> getTranslatePublicValueAsStrings(String code, String filter){
        return mDbQuery.getTranslatePublicValueAsStrings(code, filter);
    }
    public String getTranslatedValueByCodes(String fieldValue, String fieldKey) {

        List<String> uniquePublicValues = new ArrayList<>();
        uniquePublicValues = getTranslatePublicValueAsStrings(fieldValue, fieldKey);

        return String.join(", ", uniquePublicValues);
    }


}
