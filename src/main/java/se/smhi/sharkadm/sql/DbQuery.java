package se.smhi.sharkadm.sql;

import se.smhi.sharkadm.model.Sample;

import java.nio.file.Path;
import java.sql.*;

public class DbQuery extends ConnectionManager{

    private Path mPathToDb = null;

    public DbQuery(Path pathToDb){
     mPathToDb = pathToDb;
    }

    /*
        Will return a string of one or more fields.
     */
    public String getTranslateCodeColumnValue(String projectCode, Sample sample, String nameOfColumn) {

        StringBuilder codeValues = new StringBuilder();
        Connection cn = getConnection(mPathToDb);

        StringBuilder bu = new StringBuilder();
        bu.append(" SELECT * FROM translate_codes_NEW WHERE field = ? AND code = ? OR code = ?");
        String[] projCode = sample.getField("sample.".concat(projectCode)).split(",");

        try {

            int prmIdx = 1;

            PreparedStatement pstmt = cn.prepareStatement(bu.toString());
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
}
