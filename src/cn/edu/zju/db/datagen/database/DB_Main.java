package cn.edu.zju.db.datagen.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DB_Main {

    /*
     * Recreates the moovetemplate database. This is the template for the working database, which is called "moovework". It should only be
     * recreated when changes to the database is done.
     */
    private static void createMooveTemplate(Connection con) throws SQLException {

        con = DB_Connection.connectToDatabase("conf/Moove.properties");
        DB_Create.RecreateMooveTempalte(con);
        con.close();
        con = DB_Connection.connectToDatabase("conf/MooveTemplate.properties");
        DB_Create.CreateTablesForMooveTemplate(con);
        con.close();
    }

    /*
     * Recreates the moovework database. This is the actual database to be manipulated. It is created using the template database
     * "moovetemplate". This function should be run when a database flush is needed.
     */
    private static void createMooveWork(Connection con) throws SQLException {

        con = DB_Connection.connectToDatabase("conf/Moove.properties");
        DB_Create.RecreateMooveWork(con);
        con.close();
    }

    /*
     * Right now, main is used to test the code.
     */
    public static void main(String[] argv) {

        Connection con = null;
        PreparedStatement pst = null;

        try {
            createMooveTemplate(con); // Create the template database
            createMooveWork(con); // Create MooveWork based on the template

            con = DB_Connection.connectToDatabase("conf/moovework.properties");

            DB_CreateFunction.CreateRandomPointsInPolygon(con);
            System.out.println("Done with the test...");
        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(PreparedStatement.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
        }

        // Close the preparedstatement and connection if necessary.
        finally {

            try {
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {
                Logger lgr = Logger.getLogger(PreparedStatement.class.getName());
                lgr.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }
}