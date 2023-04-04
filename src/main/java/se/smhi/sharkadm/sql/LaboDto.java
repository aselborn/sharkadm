package se.smhi.sharkadm.sql;

import java.util.ArrayList;
import java.util.List;

/*
    All these codes are hardcoded and represent a list of codes that handles the "LABO" field in codelist
    the original file is located here : https://smhi.se/oceanografi/oce_info_data/shark_web/downloads/codelist_SMHI.xlsx
 */

public class LaboDto {

    private static LaboDto instance = null;

    public static synchronized LaboDto getInstance(){
        if (instance == null){
            synchronized (LaboDto.class){
                if (instance == null){
                    instance = new LaboDto();
                }
            }
        }
        return instance;
    }
    private List<String> laboCodes = new ArrayList<>();
    public void setLaboCode(String laboCode){
        laboCodes.add(laboCode);
    }

    public List<String> getLaboCodes() {
        return laboCodes;
    }


}
