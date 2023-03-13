package se.smhi.sharkadm.database;

/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute.
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import se.smhi.sharkadm.utils.ErrorLogger;



/**
 *	<p>
 *	DbConnect creates a connection to a Postgresql/PostGIS database the first time it is called.<br/>
 *	DbConnect is implemented as a singleton class.
 *	</p>
 *
 *	<p>Basic usage:
 *	<pre>
 *	DbConnect.instance().setDatabaseServer("localhost") {
 *	DbConnect.instance().setDatabaseName("shark_web") {
 *	DbConnect.instance().setUser("test") {
 *	DbConnect.instance().setPassword("test") {
 *	Connection connection = DbConnect.instance().getConnection();
 *	</pre></p>
 */
public class DbConnect {
    private static DbConnect instance = new DbConnect(); // Singleton.

    private String databaseServer = "";

    private String databaseName = "";

    private String user = "";

    private String password = "";

    private Connection con = null;

    private DbConnect() { // Singleton.
    }

    public static DbConnect instance() { // Singleton.
        return instance;
    }

    public boolean isConnected() {
        if (con == null) {
            return false;
        }
        return true;
    }

    public Connection getConnection() {
        if (con == null) {
            connect();
        }
        return con;
    }

    private void connect() {
        try {
            String url = "";
            Class.forName("org.postgresql.Driver");
            url = 	"jdbc:postgresql://" +
                    databaseServer + ":5432/" +
                    databaseName +
                    "?user=" + user +
                    "&password=" + password;

            con = DriverManager.getConnection(url);

        } catch (SQLException e) {
            ErrorLogger.println("DbManager, SQLException: " + e.getMessage());
            ErrorLogger.println("DbManager, SQLState: " + e.getSQLState());
            ErrorLogger.println("DbManager, VendorError: " +
                    e.getErrorCode());
        } catch (Exception e) {
            ErrorLogger.println("DbManager, Exception: ");
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        ErrorLogger.println("DbManager: Close connection to mySql...");
        try {
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            ErrorLogger.println("DbManager, SQLException: " + e.getMessage());
            ErrorLogger.println("DbManager, SQLState: " + e.getSQLState());
            ErrorLogger.println("DbManager, VendorError: " + e.getErrorCode());
        } catch (Exception e) {
            ErrorLogger.println("DbManager, Exception: ");
            e.printStackTrace();
        }
        con = null;
        databaseServer = "";
        databaseName = "";
        user = "";
        password = "";
    }

    public void beginTransaction() throws SQLException {
        if (con != null) {
            con.setAutoCommit(false);
        }
    }

    public void commitTransaction() throws SQLException {
        if (con != null) {
            con.commit();
        }
    }

    public void rollbackTransaction() throws SQLException {
        if (con != null) {
            con.rollback();
        }
    }


    public void endTransaction() throws SQLException {
        if (con != null) {
            con.setAutoCommit(true);
        }
    }

    public void psqlVacuum() {
        Statement stmt;
        try {
            stmt = this.getConnection().createStatement();
            stmt.execute("VACUUM ANALYZE variable;");
            stmt.execute("VACUUM ANALYZE sample;");
            stmt.execute("VACUUM ANALYZE visit;");
            stmt.execute("VACUUM ANALYZE dataset;");
            stmt.execute("VACUUM ANALYZE visit_location;");
//			stmt.execute("VACUUM ANALYZE shark_settings;");
//			stmt.execute("VACUUM ANALYZE taxon;");
        } catch (SQLException e) {
            System.out.println("ERROR: Postgres VACUUM ANALYZE failed.");
//			HandleError(e);
        }
    }

    public void setDatabaseServer(String databaseServer) {
        this.databaseServer = databaseServer;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabaseServer() {
        return databaseServer;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getUser() {
        return user;
    }

}

