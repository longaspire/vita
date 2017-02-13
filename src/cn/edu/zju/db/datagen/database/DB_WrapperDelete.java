package cn.edu.zju.db.datagen.database;

import cn.edu.zju.db.datagen.database.spatialobject.AccessPoint;
import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import cn.edu.zju.db.datagen.database.spatialobject.UploadObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DB_WrapperDelete extends DB_Create {

    public DB_WrapperDelete() {
        // TODO Auto-generated constructor stub
    }

    public static void flushAP2PartTable(Connection con, Integer file_id) throws SQLException {
        PreparedStatement pst = null;
        String query = null;

        query = "DELETE FROM " + T_APTOPART + " a2p using (" +
                        "SELECT ap." + ITEM_ITEMID + " FROM " + T_ACCESSPOINT + " ap JOIN (" +
                        "SELECT f." + ITEM_ITEMID + " FROM " + T_FLOOR + " f JOIN (" +
                        "SELECT b." + ITEM_ITEMID + " FROM " + T_BUILDING + " b WHERE " + BUILDING_FILEID + "=?) " +
                        "as o3 ON f." + FLOOR_BUILDINGID + "=o3." + ITEM_ITEMID + ") " +
                        "as o2 ON ap." + FLOORITEM_FLOORID + "=o2." + ITEM_ITEMID + ") " +
                        "as o1 WHERE a2p." + A2P_APID + "=o1." + ITEM_ITEMID + "";
        pst = con.prepareStatement(query);
        pst.setInt(1, file_id);
        pst.executeUpdate();

        //Because of the trigger in the aptopart table, which creates connectivity rows, we must flush that table also to avoid foreign key constraints
        flushConnectivityTable(con, file_id);
    }

    private static void flushConnectivityTable(Connection con, Integer file_id) throws SQLException {
        PreparedStatement pst = null;
        String query = null;

        query = "DELETE FROM " + T_CONNECTIVITY + " con using (" +
                        "SELECT ap." + ITEM_ITEMID + " FROM " + T_ACCESSPOINT + " ap JOIN (" +
                        "SELECT f." + ITEM_ITEMID + " FROM " + T_FLOOR + " f JOIN (" +
                        "SELECT b." + ITEM_ITEMID + " FROM " + T_BUILDING + " b WHERE " + BUILDING_FILEID + "=?) " +
                        "as o3 ON f." + FLOOR_BUILDINGID + "=o3." + ITEM_ITEMID + ") " +
                        "as o2 ON ap." + FLOORITEM_FLOORID + "=o2." + ITEM_ITEMID + ") " +
                        "as o1 WHERE con." + CON_APID + "=o1." + ITEM_ITEMID + "";
        pst = con.prepareStatement(query);
        pst.setInt(1, file_id);
        pst.executeUpdate();
    }

    public static void deletePartition(Connection con, Partition part)
            throws SQLException {
        PreparedStatement pst = null;
        String query = null;

        query = "DELETE FROM " + T_PARTITION + " WHERE item_id=?";
        pst = con.prepareStatement(query);
        pst.setInt(1, part.getItemID());
        pst.executeUpdate();
    }

    public static void deleteAccessPoint(Connection con, AccessPoint ap)
            throws SQLException {
        PreparedStatement pst = null;
        String query = null;

        query = "DELETE FROM " + T_ACCESSPOINT + " WHERE item_id=?";
        pst = con.prepareStatement(query);
        pst.setInt(1, ap.getItemID());
        pst.executeUpdate();
    }

    public static void deleteFile(Connection con, UploadObject uo)
            throws SQLException {
        PreparedStatement pst = null;
        String query = null;

        query = "DELETE FROM uploads WHERE upload_id=?";
        pst = con.prepareStatement(query);
        pst.setInt(1, uo.getUploadId());
        pst.executeUpdate();
    }
}
