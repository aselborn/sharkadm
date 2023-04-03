package se.smhi.sharkadm.utils;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class SharkAdmConfig {

    private static SharkAdmConfig instance = null;
    private File mConfigurationFile = new File("config.properties");
    private SharkAdmConfig(){
        if (!mConfigurationFile.exists()){
            writeDefaultFile();
        } else{
            verifyConfigfile();
        }
    }

    private void verifyConfigfile() {
        String pathTranslatecodesNew = getProperty("translate_codes_NEW");
        String pathTranslateHeaders = getProperty("translate_headers");

        if (pathTranslatecodesNew == null){
            writeDefaultSetting("translate_codes_NEW", getDefaultSetting("translate_codes_NEW", true).concat(".txt"));
        }

        if (pathTranslateHeaders == null)
            writeDefaultSetting("translate_headers", getDefaultSetting("translate_headers", true).concat(".txt"));
    }

    private void writeDefaultSetting(String setting, String value) {
        FileReader fr = null;
        try {

            fr = new FileReader(mConfigurationFile);
            Properties prop = new Properties();
            prop.load(fr);

            try(OutputStream output = new FileOutputStream("config.properties")){

                prop.setProperty(setting, value);
                prop.store(output, null);
            }




        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String getDefaultSetting(String setting, boolean update){
        String pathToDir = "#winfs$data$prodkap$sharkweb$SHARK_CONFIG$";
        String theSetting = "";

        theSetting = pathToDir.concat(setting);
        if (!update)
            theSetting = theSetting.replace("#", "\\\\\\\\").replace("$", "\\\\");
        else
            theSetting = theSetting.replace("#", "\\\\").replace("$", "\\");

        return theSetting;

    }
    private void writeDefaultFile() {
        LocalDate dateObj = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = dateObj.format(formatter);

        try {

            String pathTranslateCodesNew = getDefaultSetting("translate_codes_NEW", false).concat(".txt");
            String pathTranslateHeaders= getDefaultSetting("translate_headers", false).concat(".txt");

            FileWriter f = new FileWriter("config.properties");
            f.write("# Default setting file, created by SharkAdm at : ".concat(date));
            f.write("\n\r");
            f.write("# Always use double backslash in paths!");
            f.write("\n\r");
            f.write("translate_codes_NEW=".concat(pathTranslateCodesNew));
            f.write("\n\r");
            f.write("translate_headers=".concat(pathTranslateHeaders));
            f.write("\n\r");
            f.write("use_memory_database=true");
            f.write("\n\r");
            f.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String getProperty(String setting){
        try {
            String fileContent = null;

            FileReader fr = new FileReader(mConfigurationFile);
            Properties prop = new Properties();

            prop.load(fr);

            return prop.getProperty(setting);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized SharkAdmConfig getInstance(){
        if (instance == null)
            synchronized (SharkAdmConfig.class){
                if (instance == null){
                    instance = new SharkAdmConfig();
                }
            }

        return instance;
    }
}
