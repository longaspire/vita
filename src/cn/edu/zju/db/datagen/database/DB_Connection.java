package cn.edu.zju.db.datagen.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DB_Connection {

    private static Boolean comments = false;
    private static FileInputStream in = null;
    private static Properties props = new Properties();

    /*
     * Loads the db properties from a text file
     */
    private static void loadProp(String dbProp) {

        if (comments)
            System.out.println("Loading DB properties...");

        try {
            in = new FileInputStream(dbProp);
            props.load(in);

        } catch (IOException ex) {

            Logger lgr = Logger.getLogger(DB_Main.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);

        } finally {

            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                Logger lgr = Logger.getLogger(DB_Main.class.getName());
                lgr.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    /*
     * Tests the JDBC driver and tries to connect to the database, given the properties file
     */
    private static Connection initiateCon(String dbProp) throws SQLException {

        loadProp(dbProp);

        Connection con = null;

        if (comments)
            System.out.println("-------- PostgreSQL " + "JDBC Connection Testing ------------");

        try {

            Class.forName("org.postgresql.Driver");

        } catch (ClassNotFoundException e) {

            if (comments)
                System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
            e.printStackTrace();
        }

        if (comments)
            System.out.println("PostgreSQL JDBC Driver Registered!");

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");

        try {
            if (comments)
                System.out.println("Trying to connect...");
            con = DriverManager.getConnection(url, user, password);

        } catch (SQLException e) {
            throw e;
        }
        return con;
    }

    /*
     * Connects to a given database, based on the database.propterties "dbProp", using the connection input.
     */
    public static Connection connectToDatabase(String dbProp) {
        Connection con = null;
        try {
            con = initiateCon(dbProp);
        } catch (SQLException e) {
            if (comments)
                System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
        }

        if (con != null) {
            if (comments)
                System.out.println("Connection successful!");
        } else {
            if (comments)
                System.out.println("Failed to make connection!");
        }
        return con;
    }
}
