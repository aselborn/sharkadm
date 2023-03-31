package se.smhi.sharkadm.sql;

import se.smhi.sharkadm.utils.SharkAdmConfig;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {

    private static ConnectionManager instance = null;
    private static Connection mConnection = null;
    private ConnectionManager(){
        if (mConnection == null){
            mConnection = getConnected();
        }
    }

    public Connection getConnection(){
        return mConnection;
    }
    public static synchronized ConnectionManager getInstance()
    {
        if (instance == null){
            instance = new ConnectionManager();
        }
        return instance;
    }
    private Connection getConnected(){
        String url = "";
        try {
            boolean useMemory = Boolean.parseBoolean(SharkAdmConfig.getInstance().getProperty("use_memory_database"));
            if (useMemory){
                return DriverManager.getConnection("jdbc:sqlite::memory:");
            } else{
                url = "jdbc:sqlite:".concat( new File("sharkadm.db").getAbsolutePath());
                return DriverManager.getConnection(url);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
