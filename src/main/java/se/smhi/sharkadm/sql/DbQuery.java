package se.smhi.sharkadm.sql;

import org.apache.commons.lang3.StringUtils;
import se.smhi.sharkadm.model.Sample;

import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class DbQuery  {
  /*
        Will return a string of one or more fields.
     */
    private Connection mConnection = ConnectionManager.getInstance().getConnection();
    public String getTranslateCodeColumnValue(String projectCode, Sample sample, String nameOfColumn) {

        StringBuilder codeValues = new StringBuilder();
        StringBuilder bu = new StringBuilder();
        StringBuilder sqlIn = new StringBuilder();
        bu.append(" SELECT * FROM translate_codes_NEW WHERE field = ? AND code in (?)");
        String[] projCode = sample.getField("sample.".concat(projectCode)).split(",");

        ArrayList<String> sqlInList = new ArrayList<String>(Arrays.asList(projCode));

        sqlIn.append("(");
        sqlIn.append(sqlInList.stream().collect(Collectors.joining("', '", "'", "'")));
        sqlIn.append(")");

        try {

            int prmIdx = 1;

            PreparedStatement pstmt = mConnection.prepareStatement(bu.toString().replace("(?)", sqlIn.toString()));
            pstmt.setString(prmIdx, projectCode);

            ResultSet rs = pstmt.executeQuery();
            int cnt = 0;
            while (rs.next()){
                if (cnt > 0){
                    codeValues.append(",");
                }

                codeValues.append(rs.getString(nameOfColumn));
                cnt++;

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return codeValues.length() > 0 ? codeValues.toString() : null;
    }

    public void dropTable(String tableToDrop) {
        String sqlTruncate = " DROP TABLE IF EXISTS ".concat(tableToDrop);

        try {
            mConnection.createStatement().execute(sqlTruncate);
        } catch (SQLException e) {

        }
    }

    public String getTranslateCodeColumnValue(String projectCode, String code, String nameOfColumn) {
        StringBuilder codeValues = new StringBuilder();
        StringBuilder bu = new StringBuilder();
        StringBuilder sqlIn = new StringBuilder();

        bu.append(" SELECT * FROM translate_codes_NEW WHERE field = ? AND code in (?)");
        ArrayList<String> sqlInList = new ArrayList<String>(Arrays.asList(code));

        //String inLst = sqlInList.stream().collect(Collectors.joining("', '", "'", "'")).replace(",", "','").trim();


        sqlIn.append("(");
        sqlIn.append(sqlInList.stream().collect(Collectors.joining("', '", "'", "'")).replace(",", "','").replace(" ", ""));
        sqlIn.append(")");

        try {

            int prmIdx = 1;

            PreparedStatement pstmt = mConnection.prepareStatement(bu.toString().replace("(?)", sqlIn.toString()));
            pstmt.setString(prmIdx, projectCode);

            ResultSet rs = pstmt.executeQuery();
            int cnt = 0;
            while (rs.next()){
                if (cnt > 0){
                    codeValues.append(", "); //ADDING special sign to user
                }
                codeValues.append(rs.getString(nameOfColumn));
                cnt++;

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return codeValues.length() > 0 ? codeValues.toString() : null;
    }
}
