package se.smhi.sharkadm.sql;


import se.smhi.sharkadm.model.Sample;

import java.sql.*;
import java.util.*;
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
        String  codeValues = "";
        StringBuilder bu = new StringBuilder();
        StringBuilder sqlIn = new StringBuilder();
        Map<String, String> codeMap = new HashMap<>();

        List<String> resultList = new ArrayList<>();

        bu.append(" SELECT * FROM translate_codes_NEW WHERE field = ? AND code in (?)");
        ArrayList<String> sqlInList = new ArrayList<String>(Arrays.asList(code));

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

                if (resultList.contains(rs.getString(nameOfColumn)))
                    continue;

                resultList.add(rs.getString(nameOfColumn));
                codeMap.put(rs.getString("code"), nameOfColumn);

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (resultList.size() != code.split(",").length){
            for(String co : sqlInList){
                //String[] codes = co.replace(" ", "").trim().split(",");
                String[] codes = co.trim().split(",");
                for(String missingCode : codes){
                    if (!codeMap.containsKey(missingCode)){
                        String additionalCode = getTranslatePublicValueFromColumns(missingCode, projectCode);
                        if (additionalCode != "")
                            resultList.add(additionalCode);
                    }
                }
            }
        }
        codeValues =String.join(",", resultList);
        return codeValues.length() == 0 ?  null : codeValues ;

    }

    public TranslateHeaderDto getTranslateHeaderInternalKeyRowFromShortColumn(String shortColumn) {

        TranslateHeaderDto translateObject = new TranslateHeaderDto();

        StringBuilder bu = new StringBuilder();
        bu.append("SELECT internal_key, english, swedish, darwin_core, comment FROM translate_headers ");
        bu.append( " WHERE short = ?");

        try {
            PreparedStatement pstmt = mConnection.prepareStatement(bu.toString());
            pstmt.setString(1, shortColumn);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                translateObject.setInternal_key(rs.getString("internal_key"));
                translateObject.setShort_text(shortColumn);
                translateObject.setEnglish(rs.getString("english"));
                translateObject.setSwedish(rs.getString("swedish"));
                translateObject.setDarwin_core(rs.getString("darwin_code"));
                translateObject.setComments(rs.getString("comments"));
            }

            return translateObject;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    /*
        This function should return the number of codes from public_value as provided in the code
     */

    public List<TranslateCodesNewDto> getTranslateCodeNewDto(String code, String filter) {

        List<TranslateCodesNewDto> translateObjectList = new ArrayList<>();
        int sizeOfDubleCodes = code.split(",").length;

        StringBuilder bu = new StringBuilder();
        StringBuilder sqlIn = new StringBuilder();

        bu.append(" SELECT * from translate_codes_NEW ");
        bu.append( " WHERE code IN ");

        ArrayList<String> sqlInList = new ArrayList<String>(Arrays.asList(code));

        sqlIn.append("(");
        sqlIn.append(sqlInList.stream().collect(Collectors.joining("', '", "'", "'")).replace(",", "','").replace(" ", ""));
        sqlIn.append(")");

        bu.append(sqlIn);

        try {

            PreparedStatement pstmt = mConnection.prepareStatement(bu.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()){

                TranslateCodesNewDto translateObject= new TranslateCodesNewDto();

                if (translateObjectList.contains(translateObject))
                    continue;

                translateObject.setId(rs.getInt("id"));
                translateObject.setField(rs.getString("field"));
                translateObject.setFilter(rs.getString("filter"));
                translateObject.setPublic_value(rs.getString("public_value"));
                translateObject.setCode(rs.getString("code"));
                translateObject.setSwedish(rs.getString("swedish"));
                translateObject.setEnglish(rs.getString("english"));
                translateObject.setSynonyms(rs.getString("synonyms"));
                translateObject.setIces_biology(rs.getString("ices_biology"));
                translateObject.setIces_physical_and_chemical(rs.getString("ices_physical_and_chemical"));
                translateObject.setBodc_nerc(rs.getString("bodc_nerc"));
                translateObject.setDarwincore(rs.getString("darwincore"));
                translateObject.setComments(rs.getString("comments"));
                translateObject.setSource(rs.getString("source"));
                translateObject.setEdmo(rs.getString("edmo"));
                translateObject.setIces(rs.getString("ices"));
                translateObject.setNerc_name(rs.getString("nerc_name"));
                translateObject.setNerc_id(rs.getString("nerc_id"));

                translateObjectList.add(translateObject);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return translateObjectList;
    }

    private String getTranslatePublicValueFromColumns(String code, String field){

        String publicValue = "";
        List<String> results = new ArrayList<>();

        StringBuilder bu = new StringBuilder();
        bu.append( "SELECT public_value FROM translate_codes_NEW " );
        bu.append( " WHERE code = ?");
        bu.append( " OR swedish = ?");
        bu.append( " OR english = ?");
        bu.append( " OR ices_biology = ?");
        bu.append( " OR ices_physical_and_chemical = ?");
        bu.append( " OR bodc_nerc = ?");
        bu.append( " OR darwincore = ?");
        bu.append( " OR synonyms LIKE ?");

        try {
            PreparedStatement pstmt = mConnection.prepareStatement(bu.toString());
            pstmt.setString(1, code);
            pstmt.setString(2, code);
            pstmt.setString(3, code);
            pstmt.setString(4, code);
            pstmt.setString(5, code);
            pstmt.setString(6, code);
            pstmt.setString(7, code);
            pstmt.setString(8, code);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                if (results.contains(rs.getString("public_value")))
                    continue;
                results.add(rs.getString("public_value"));
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (results.size() > 0){
            publicValue = results.get(0);
        }

        return publicValue;

    }


    public List<String> getTranslatePublicValueAsStrings(String code, String filter) {

        List<String> publicCodes = new ArrayList<>();
        Map<String, String> codeMap = new HashMap<>();

        int sizeOfDoubleCodes = code.split(",").length;

        StringBuilder bu = new StringBuilder();
        StringBuilder sqlIn = new StringBuilder();

        bu.append(" SELECT * from translate_codes_NEW ");
        bu.append( " WHERE code IN ");

        ArrayList<String> sqlInList = new ArrayList<String>(Arrays.asList(code));

        sqlIn.append("(");
        sqlIn.append(sqlInList.stream().collect(Collectors.joining("', '", "'", "'")).replace(",", "','").replace(" ", ""));
        sqlIn.append(")");

        bu.append(sqlIn);

        try {

            PreparedStatement pstmt = mConnection.prepareStatement(bu.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()){
                if (publicCodes.contains(rs.getString("public_value")))
                    continue;
                publicCodes.add(rs.getString("public_value"));
                codeMap.put(rs.getString("code").trim(), rs.getString("public_value").trim());
            }

            //if not match! start search!
            if (publicCodes.size() != sizeOfDoubleCodes){
                for(String co : sqlInList){
                    //String[] codes = co.replace(" ", "").trim().split(",");
                    String[] codes = co.trim().split(",");
                    for(String missingCode : codes){
                        if (!codeMap.containsKey(missingCode)){
                            String additionalCode = getTranslatePublicValueFromColumns(missingCode.replaceFirst(" ", ""), filter);
                            if (additionalCode != "")
                                publicCodes.add(additionalCode);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return publicCodes;
    }
}
