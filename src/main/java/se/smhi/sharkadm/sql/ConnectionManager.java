package se.smhi.sharkadm.sql;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class ConnectionManager {

    protected Connection getConnection(Path path){
        try {
            String url = "jdbc:sqlite:".concat(path.toAbsolutePath().toString());

            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
