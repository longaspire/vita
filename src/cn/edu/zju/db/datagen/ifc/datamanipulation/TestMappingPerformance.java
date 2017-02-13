package cn.edu.zju.db.datagen.ifc.datamanipulation;

import cn.edu.zju.db.datagen.database.DB_Connection;
import cn.edu.zju.db.datagen.database.DB_Import;
import cn.edu.zju.db.datagen.database.DB_WrapperDelete;
import cn.edu.zju.db.datagen.database.DB_WrapperLoad;

import java.sql.Connection;
import java.sql.SQLException;

/*
 * This class contains test cases for testing the influence on performance when partitions are decomposed before they are mapped.
 */
public class TestMappingPerformance {


    private static boolean decompBeforeMapping = false;
    private static Integer fileID = 1;
    private static int noRuns = 100;

    /**
     * @param args
     * @throws SQLException
     */
    public static void main(String[] args) throws SQLException {
        long startTime;
        long stopTime;
        long elapsedTime;
        long totalRunTime = 0;
        long avgRunTime;
        int _no = 0;

        Connection con = DB_Connection.connectToDatabase("conf/moovework.properties");

        DB_WrapperLoad.loadALL(con, fileID);
        if (true) {
            DB_Import.decompose(con);
            DB_WrapperLoad.loadALL(con, fileID);
        }

        for (int i = 0; i < noRuns; i++) {
            DB_WrapperDelete.flushAP2PartTable(con, fileID);
            startTime = System.nanoTime();
            DB_Import.mapD2P(con);
            stopTime = System.nanoTime();
            elapsedTime = (stopTime - startTime) / 1000000;
            totalRunTime += elapsedTime;
            _no += 1;
            System.out.println(_no + ": " + elapsedTime + "ms");
        }
        avgRunTime = totalRunTime / noRuns;
        System.out.println("Total run time: " + totalRunTime + "ms");
        System.out.println("Avg run time: " + avgRunTime + "ms");
        con.close();
    }
}
