package se.smhi.sharkadm.sql;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import se.smhi.sharkadm.model.Sample;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

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

    private void createTranslateCodeTable(){

        dropTable("translate_codes_NEW");

        StringBuilder bu = new StringBuilder();
        bu.append("CREATE TABLE translate_codes_NEW (");
        bu.append(" id INTEGER PRIMARY KEY AUTOINCREMENT,  ");
        bu.append(" field Text , ");
        bu.append(" filter Text, ");
        bu.append("public_value Text, " );
        bu.append("code TEXT, " );
        bu.append("swedish TEXT, ");
        bu.append("english TEXT, ");
        bu.append("synonyms TEXT, ");
        bu.append("ices_biology TEXT, ");
        bu.append("ices_physical_and_chemical TEXT, ");
        bu.append("bodc_nerc TEXT, ");
        bu.append("darwincore TEXT, ");
        bu.append("comments TEXT, ");
        bu.append("source TEXT)");

        try (Statement stmt = mConnection.createStatement()) {
            stmt.execute(bu.toString());

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public static synchronized SqliteManager getInstance()
    {
        if (instance == null)
            instance = new SqliteManager();

        return instance;
    }


    /*
        The text-file is inserted to a representation in the sqlite database.
     */
    public void fillTable(Path fileName) {

        switch (fileName.getFileName().toString()){
            case "translate_codes_NEW.txt":
                createTranslateCodeTable();
                insertTranslateCodes(fileName);
                break;

            default:
                break;
        }
    }

    private void insertTranslateCodes(Path fileName) {

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

            bu.append(" INSERT INTO translate_codes_NEW (field, filter, public_value, code, swedish, english, synonyms, ices_biology, ices_physical_and_chemical, bodc_nerc, darwincore, comments, source)");
            bu.append(" VALUES ");
            bu.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            PreparedStatement psmtm = mConnection.prepareStatement(bu.toString());
            mConnection.setAutoCommit(false);
            while ((entries = csvReader.readNext()) != null) {

                ArrayList<String> list = new ArrayList<String>(Arrays.asList(entries));

                psmtm.setString(1, list.get(0));
                psmtm.setString(2, list.get(1));
                psmtm.setString(3, list.get(2));
                psmtm.setString(4, list.get(3));
                psmtm.setString(5, list.get(4));
                psmtm.setString(6, list.get(5));
                psmtm.setString(7, list.get(6));
                psmtm.setString(8, list.get(7));
                psmtm.setString(9, list.get(8));
                psmtm.setString(10, list.get(9));
                psmtm.setString(11, list.get(10));
                psmtm.setString(12, list.get(11));
                psmtm.setString(13, list.get(12));

                psmtm.addBatch();

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
}
