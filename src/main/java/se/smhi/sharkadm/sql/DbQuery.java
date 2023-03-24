package se.smhi.sharkadm.sql;

import se.smhi.sharkadm.model.Sample;

import java.nio.file.Path;
import java.sql.*;

public class DbQuery  {
  /*
        Will return a string of one or more fields.
     */
    private Connection mConnection = ConnectionManager.getInstance().getConnection();
    public String getTranslateCodeColumnValue(String projectCode, Sample sample, String nameOfColumn) {

        StringBuilder codeValues = new StringBuilder();
        StringBuilder bu = new StringBuilder();
        bu.append(" SELECT * FROM translate_codes_NEW WHERE field = ? AND code = ? OR code = ?");
        String[] projCode = sample.getField("sample.".concat(projectCode)).split(",");

        try {

            int prmIdx = 1;

            PreparedStatement pstmt = mConnection.prepareStatement(bu.toString());
            pstmt.setString(prmIdx, projectCode);

            if (projCode.length>1){
                for (int n = 0; n<= projCode.length -1; n++){
                    pstmt.setString(++prmIdx, projCode[n]);
                }
            } else{
                pstmt.setString(++prmIdx, projCode[0]);
            }


            ResultSet rs = pstmt.executeQuery();
            int cnt = 0;
            while (rs.next()){
                if (cnt > 0){
                    codeValues.append("<->");
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
}
