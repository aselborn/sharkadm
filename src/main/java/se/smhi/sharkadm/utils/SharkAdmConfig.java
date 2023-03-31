package se.smhi.sharkadm.utils;

import se.smhi.sharkadm.sql.SqliteManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class SharkAdmConfig {

    private static SharkAdmConfig instance = null;
    private File mConfigurationFile = new File("config.properties");
    private SharkAdmConfig(){
        if (!mConfigurationFile.exists()){
            writeDefaultFile();
        }
    }

    private void writeDefaultFile() {
        LocalDate dateObj = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = dateObj.format(formatter);
        try {
            String path = "#winfs$data$prodkap$sharkweb$SHARK_CONFIG$translate_codes_NEW.txt";

            path = path.replace("#", "\\\\\\\\").replace("$", "\\\\");

            FileWriter f = new FileWriter("config.properties");
            f.write("# Default setting file, created by SharkAdm at : ".concat(date));
            f.write("\n\r");
            f.write("# Always use double backslash in paths!");
            f.write("\n\r");
            f.write("translate_codes_NEW=".concat(path));
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
