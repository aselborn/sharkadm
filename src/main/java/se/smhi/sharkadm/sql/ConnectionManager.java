package se.smhi.sharkadm.sql;

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
        try {
            //String url = "jdbc:sqlite:".concat(path.toAbsolutePath().toString());
            String url = "jdbc:sqlite:".concat( new File("sharkadm.db").getAbsolutePath());
            //return DriverManager.getConnection("jdbc:sqlite::memory:");
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
