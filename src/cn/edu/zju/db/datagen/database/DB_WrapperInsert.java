package cn.edu.zju.db.datagen.database;

import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Door;
import cn.edu.zju.db.datagen.ifc.datamanipulation.Decomposition;
import cn.edu.zju.db.datagen.ifc.datamanipulation.GeometricCalc;
import cn.edu.zju.db.datagen.indoorobject.utility.SpatialHandler;
import diva.util.java2d.Polygon2D;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class DB_WrapperInsert extends DB_Create {

    public static void insertBuilding(Connection con, String name,
                                      String globalid, Integer fileID) throws SQLException {

        PreparedStatement pst = null;
        String query = null;

        query = "UPDATE " + T_BUILDING + " SET " + ITEM_NAME + "=? WHERE " + ITEM_GLOBALID + "=?;"
                        + "INSERT INTO " + T_BUILDING + "(" + ITEM_NAME + ", " + ITEM_GLOBALID + ", " + BUILDING_FILEID + ") "
                        + "(select ? as " + ITEM_NAME + ", " +
                        "? as " + ITEM_GLOBALID + ", " +
                        "? as " + BUILDING_FILEID + " "
                        + "WHERE NOT EXISTS (SELECT 1 FROM " + T_BUILDING + " WHERE " + ITEM_GLOBALID + "=?));";

        pst = con.prepareStatement(query);
        pst.setString(1, name);
        pst.setString(2, globalid);
        pst.setString(3, name);
        pst.setString(4, globalid);
        pst.setInt(5, fileID);
        pst.setString(6, globalid);
        pst.executeUpdate();
    }

    /*
     * Insert a floor if it doesn't exist, else updates the floor according to the globalid.
     */
    public static void insertFloor(Connection con, String name,
                                   String globalid, String buildingGlobalID) throws SQLException {

        PreparedStatement pst = null;
        ResultSet rs = null;
        String query = null;
        Integer buildingID = 0;

        //TODO: Create exceptions, if foreign key elements doesn't exist.

        // See if the building exists
        query = "SELECT " + ITEM_ITEMID + " FROM " + T_BUILDING + " WHERE " + ITEM_GLOBALID + "=?";
        pst = con.prepareStatement(query);
        pst.setString(1, buildingGlobalID);
        rs = pst.executeQuery();

        if (!rs.next()) {
            System.out.print("The building specified does not seem to exist. Did you remember to add it?");
            return;
        } else {
            buildingID = rs.getInt(1);
        }

        query = "UPDATE " + T_FLOOR + " SET " + ITEM_NAME + "=? WHERE " + ITEM_GLOBALID + "=?;"
                        + "INSERT INTO " + T_FLOOR + "(" + ITEM_NAME + ", " + ITEM_GLOBALID + ", " + FLOOR_BUILDINGID + ") "
                        + "(select ? as " + ITEM_NAME + ", ? as " + ITEM_GLOBALID + ", ? as " + FLOOR_BUILDINGID + " "
                        + "WHERE NOT EXISTS (SELECT 1 FROM " + T_FLOOR + " WHERE " + ITEM_GLOBALID + "=?));";

        pst = con.prepareStatement(query);
        pst.setString(1, name);
        pst.setString(2, globalid);
        pst.setString(3, name);
        pst.setString(4, globalid);
        pst.setInt(5, buildingID);
        pst.setString(6, globalid);
        pst.executeUpdate();

    }

    /*
     * Inserts a partition along with a placement if given. Updates the placement and
     * partition if they already exist. Updates the partition if recognized by its globalid.
     */
    public static Integer insertPartition(Connection con, String name, String globalid, String floorglobalid, ArrayList<Point2D.Double> polyline)
            throws SQLException {

        PreparedStatement pst = null;
        ResultSet rs = null;
        Integer floorID = 0;
        String query = null;
        ResultSet result = null;
        Integer partid = 0;

        // Find out if the floor exists
        query = "SELECT " + ITEM_ITEMID + " FROM " + T_FLOOR + " WHERE " + ITEM_GLOBALID + "=?";
        pst = con.prepareStatement(query);
        pst.setString(1, floorglobalid);
        rs = pst.executeQuery();

        if (!rs.next()) {
            System.out.print("The floor specified does not seem to exist. Did you remember to add it? \n");
            return 0;
        } else {
            floorID = rs.getInt(1);
        }

        // Find out if partition exists
        query = "SELECT 1 FROM " + T_PARTITION + " WHERE " + ITEM_GLOBALID + "=?";
        pst = con.prepareStatement(query);
        pst.setString(1, globalid);
        rs = pst.executeQuery();

        String polylinetext = null;

        if (polyline != null && !polyline.isEmpty()) {
            polylinetext = "POLYGON((";
            Boolean first = true;
            for (Point2D.Double p : polyline) {
                if (!first) {
                    polylinetext += ", ";
                }
                polylinetext += p.getX() + " " + p.getY();
                first = false;
            }
            polylinetext += "))";
        }

        if (rs.next()) {
            if (polyline != null && !polyline.isEmpty()) {
                query = "UPDATE " + T_PARTITION + " SET " + ITEM_NAME + "=?, " + FLOORITEM_FLOORID + "=?, " + PART_GEOM + "=ST_GeomFromText('" + polylinetext + "',-1) WHERE " + ITEM_GLOBALID + "=? RETURNING " + ITEM_ITEMID + "";
                pst = con.prepareStatement(query);
                pst.setString(1, name);
                pst.setInt(2, floorID);
                pst.setString(3, globalid);
                result = pst.executeQuery();
                result.next();
            } else {
                query = "UPDATE " + T_PARTITION + " SET " + ITEM_NAME + "=?, " + FLOORITEM_FLOORID + "=?, " + PART_GEOM + "=null WHERE " + ITEM_GLOBALID + "=? RETURNING " + ITEM_ITEMID + "";
                pst.close();
                pst = con.prepareStatement(query);
                pst.setString(1, name);
                pst.setInt(2, floorID);
                pst.setString(3, globalid);
                result = pst.executeQuery();
                result.next();
            }
        } else {
            if (polyline != null && !polyline.isEmpty()) {
                query = "INSERT INTO " + T_PARTITION + "(" + ITEM_NAME + "," + ITEM_GLOBALID + "," + FLOORITEM_FLOORID + ", " + PART_GEOM + ")"
                                + "(select ? as " + ITEM_NAME + ", ? as " + ITEM_GLOBALID + ", ? as " + FLOORITEM_FLOORID + ", ST_GeomFromText('" + polylinetext + "',-1) as " + PART_GEOM + ") RETURNING " + ITEM_ITEMID + "";
                pst.close();
                pst = con.prepareStatement(query);
                pst.setString(1, name);
                pst.setString(2, globalid);
                pst.setInt(3, floorID);
                result = pst.executeQuery();
                result.next();
                partid = result.getInt(1);
            } else {
                query = "INSERT INTO " + T_PARTITION + "(" + ITEM_NAME + "," + ITEM_GLOBALID + "," + FLOORITEM_FLOORID + ")"
                                + "(select ? as " + ITEM_NAME + ", ? as " + ITEM_GLOBALID + ", ? as " + FLOORITEM_FLOORID + ") RETURNING " + ITEM_ITEMID + "";
                pst.close();
                pst = con.prepareStatement(query);
                pst.setString(1, name);
                pst.setString(2, globalid);
                pst.setInt(3, floorID);
                result = pst.executeQuery();
                result.next();
                partid = result.getInt(1);
            }
        }
        return partid;
    }


    public static Integer insertPartition(Connection con, String name, String globalid, String floorglobalid, Polygon2D.Double polygon)
            throws SQLException {

        PreparedStatement pst = null;
        ResultSet rs = null;
        Integer floorID = 0;
        String query = null;
        ResultSet result = null;
        Integer partid = 0;

        // Find out if the floor exists
        query = "SELECT " + ITEM_ITEMID + " FROM " + T_FLOOR + " WHERE " + ITEM_GLOBALID + "=?";
        pst = con.prepareStatement(query);
        pst.setString(1, floorglobalid);
        rs = pst.executeQuery();

        if (!rs.next()) {
            System.out.print("The floor specified does not seem to exist. Did you remember to add it? \n");
            return 0;
        } else {
            floorID = rs.getInt(1);
        }

        // Find out if partition exists
        query = "SELECT 1 FROM " + T_PARTITION + " WHERE " + ITEM_GLOBALID + "=?";
        pst = con.prepareStatement(query);
        pst.setString(1, globalid);
        rs = pst.executeQuery();

        String polylinetext = DB_WrapperSpatial.convertPolygon2DtoString(polygon);

        if (rs.next()) {
            if (polygon != null) {
                query = "UPDATE " + T_PARTITION + " SET " + ITEM_NAME + "=?, " + FLOORITEM_FLOORID + "=?, " + PART_GEOM + "=ST_GeomFromText('" + polylinetext + "',-1) WHERE " + ITEM_GLOBALID + "=? RETURNING " + ITEM_ITEMID + "";
                pst = con.prepareStatement(query);
                pst.setString(1, name);
                pst.setInt(2, floorID);
                pst.setString(3, globalid);
                result = pst.executeQuery();
                result.next();
            } else {
                query = "UPDATE " + T_PARTITION + " SET " + ITEM_NAME + "=?, " + FLOORITEM_FLOORID + "=?, " + PART_GEOM + "=null WHERE " + ITEM_GLOBALID + "=? RETURNING " + ITEM_ITEMID + "";
                pst.close();
                pst = con.prepareStatement(query);
                pst.setString(1, name);
                pst.setInt(2, floorID);
                pst.setString(3, globalid);
                result = pst.executeQuery();
                result.next();
            }
        } else {
            if (polygon != null) {
                query = "INSERT INTO " + T_PARTITION + "(" + ITEM_NAME + "," + ITEM_GLOBALID + "," + FLOORITEM_FLOORID + ", " + PART_GEOM + ")"
                                + "(select ? as " + ITEM_NAME + ", ? as " + ITEM_GLOBALID + ", ? as " + FLOORITEM_FLOORID + ", ST_GeomFromText('" + polylinetext + "',-1) as " + PART_GEOM + ") RETURNING " + ITEM_ITEMID + "";
                pst.close();
                pst = con.prepareStatement(query);
                pst.setString(1, name);
                pst.setString(2, globalid);
                pst.setInt(3, floorID);
                result = pst.executeQuery();
                result.next();
                partid = result.getInt(1);
            } else {
                query = "INSERT INTO " + T_PARTITION + "(" + ITEM_NAME + "," + ITEM_GLOBALID + "," + FLOORITEM_FLOORID + ")"
                                + "(select ? as " + ITEM_NAME + ", ? as " + ITEM_GLOBALID + ", ? as " + FLOORITEM_FLOORID + ") RETURNING " + ITEM_ITEMID + "";
                pst.close();
                pst = con.prepareStatement(query);
                pst.setString(1, name);
                pst.setString(2, globalid);
                pst.setInt(3, floorID);
                result = pst.executeQuery();
                result.next();
                partid = result.getInt(1);
            }
        }
        return partid;
    }

    /*
     * 'apType' used for different things
     * 0: Original real AP
     * 1: AP created from insert AP method
     * 2: AP created from decomposition (virtual)
     */
    public static Integer insertAccessPointWithGeom(Connection con, String name, Point2D.Double placement, String globalid, String floorglobalid, Integer apType, Line2D.Double line) throws SQLException {
        PreparedStatement pst = null;
        ResultSet rs = null;
        Integer floorID = 0;
        String query = null;
        ResultSet result = null;

        query = "SELECT " + ITEM_ITEMID + " FROM " + T_FLOOR + " WHERE " + ITEM_GLOBALID + "=?";
        pst = con.prepareStatement(query);
        pst.setString(1, floorglobalid);
        rs = pst.executeQuery();

        if (!rs.next()) {
            System.out.print("The floor for the door specified does not seem to exist. Did you remember to add it? \n");
            return 0;
        } else {
            floorID = rs.getInt(1);
        }

        // Find out if access point exists
        query = "SELECT 1 FROM " + T_ACCESSPOINT + " WHERE " + ITEM_GLOBALID + "=?";
        pst = con.prepareStatement(query);
        pst.setString(1, globalid);
        rs = pst.executeQuery();

        String pointgeom = "ST_GeomFromText('POINT(" + placement.getX() + " " + placement.getY() + ")', -1)";

        String lineString = "ST_GeomFromText('" + DB_WrapperSpatial.convertLine2DtoString(line) + "', -1)";

        if (rs.next()) {
            query = "UPDATE " + T_ACCESSPOINT + " SET " + ITEM_NAME + "=?, " + FLOORITEM_FLOORID + "=?, " + AP_TYPE + "=?, " + AP_LOCATION + "=" + pointgeom + ", " + AP_LINE + "=" + lineString + " WHERE " + ITEM_GLOBALID + "=? RETURNING " + ITEM_ITEMID + "";
            pst = con.prepareStatement(query);
            pst.setString(1, name);
            pst.setInt(2, floorID);
            pst.setInt(3, apType);
            pst.setString(4, globalid);
            result = pst.executeQuery();

        } else {
            query = "INSERT INTO " + T_ACCESSPOINT + "(" + ITEM_NAME + "," + ITEM_GLOBALID + "," + FLOORITEM_FLOORID + "," + AP_TYPE + "," + AP_LOCATION + "," + AP_LINE + ")"
                            + "(select ? as " + ITEM_NAME + ", ? as " + ITEM_GLOBALID + ", ? as " + FLOORITEM_FLOORID + ", ? as " + AP_TYPE + ", " + pointgeom + " as " + AP_LOCATION + ", " + lineString + " as " + AP_LINE + ") RETURNING " + ITEM_ITEMID + "";
            pst.close();
            pst = con.prepareStatement(query);
            pst.setString(1, name);
            pst.setString(2, globalid);
            pst.setInt(3, floorID);
            pst.setInt(4, apType);
            result = pst.executeQuery();
        }

        result.next();
        Integer apID = result.getInt(1);
        return apID;
    }


    public static Integer insertVirtualAccessPointWithGeom(Connection con,
                                                           String name1, String name2, Point2D.Double placement, String globalid, String floorglobalid, Integer apType, Line2D.Double line) throws SQLException {
        PreparedStatement pst = null;
        ResultSet rs = null;
        Integer floorID = 0;
        String query = null;
        ResultSet result = null;

        query = "SELECT " + ITEM_ITEMID + " FROM " + T_FLOOR + " WHERE " + ITEM_GLOBALID + "=?";
        pst = con.prepareStatement(query);
        pst.setString(1, floorglobalid);
        rs = pst.executeQuery();

        if (!rs.next()) {
            System.out.print("The floor for the door specified does not seem to exist. Did you remember to add it? \n");
            return 0;
        } else {
            floorID = rs.getInt(1);
        }

        // Find out if access point exists
        query = "SELECT 1 FROM " + T_ACCESSPOINT + " WHERE " + ITEM_NAME + "=?"
                        + " OR " + ITEM_NAME + "=?";
        pst = con.prepareStatement(query);
        pst.setString(1, name1);
        pst.setString(2, name2);
        rs = pst.executeQuery();

        String pointgeom = "ST_GeomFromText('POINT(" + placement.getX() + " " + placement.getY() + ")', -1)";

        String lineString = "ST_GeomFromText('" + DB_WrapperSpatial.convertLine2DtoString(line) + "', -1)";

        if (rs.next()) {
            query = "UPDATE " + T_ACCESSPOINT + " SET " + ITEM_GLOBALID + "=?, " + FLOORITEM_FLOORID + "=?, " + AP_TYPE + "=?, " + AP_LOCATION + "=" + pointgeom + ", " + AP_LINE + "=" + lineString + " WHERE " + ITEM_NAME + "=? RETURNING " + ITEM_ITEMID + "";
            pst = con.prepareStatement(query);
            pst.setString(1, globalid);
            pst.setInt(2, floorID);
            pst.setInt(3, apType);
            pst.setString(4, name1);
            result = pst.executeQuery();

        } else {
            query = "INSERT INTO " + T_ACCESSPOINT + "(" + ITEM_NAME + "," + ITEM_GLOBALID + "," + FLOORITEM_FLOORID + "," + AP_TYPE + "," + AP_LOCATION + "," + AP_LINE + ")"
                            + "(select ? as " + ITEM_NAME + ", ? as " + ITEM_GLOBALID + ", ? as " + FLOORITEM_FLOORID + ", ? as " + AP_TYPE + ", " + pointgeom + " as " + AP_LOCATION + ", " + lineString + " as " + AP_LINE + ") RETURNING " + ITEM_ITEMID + "";
            pst.close();
            pst = con.prepareStatement(query);
            pst.setString(1, name1);
            pst.setString(2, globalid);
            pst.setInt(3, floorID);
            pst.setInt(4, apType);
            result = pst.executeQuery();
        }

        result.next();
        Integer apID = result.getInt(1);
        return apID;
    }

    /*
     * Deletes all connections to partitions for an access point, given its globalid. Used for
     * lazy update.
     */
    public static void flushPartitionForAPConnections(Connection con,
                                                      String globalidDoor) throws SQLException {

        PreparedStatement pst = null;
        String query = null;
        ResultSet rs = null;

        query = "SELECT " + ITEM_ITEMID + " FROM " + T_ACCESSPOINT + " WHERE " + ITEM_GLOBALID + "=?";
        pst = con.prepareStatement(query);
        pst.setString(1, globalidDoor);
        rs = pst.executeQuery();
        if (rs.next()) {
            query = "DELETE FROM " + T_APTOPART + " WHERE " + A2P_APID + "=?";
            pst = con.prepareStatement(query);
            pst.setInt(1, rs.getInt(1));
            pst.executeUpdate();
            query = "DELETE FROM " + T_CONNECTIVITY + " WHERE " + CON_APID + "=?";
            pst = con.prepareStatement(query);
            pst.setInt(1, rs.getInt(1));
            pst.executeUpdate();
        }
    }

    public static void flushPartitionForConnectors(Connection con,
                                                   String globalidStair) throws SQLException {
        PreparedStatement pst = null;
        String query = null;
        ResultSet rs = null;

        query = "SELECT " + ITEM_ITEMID + " FROM " + T_CONNECTOR + " WHERE " + ITEM_GLOBALID + "=?";
        pst = con.prepareStatement(query);
        pst.setString(1, globalidStair);
        rs = pst.executeQuery();
        if (rs.next()) {
            query = "DELETE FROM " + T_CONTOPART + " WHERE " + C2P_CONID + "=?";
            pst = con.prepareStatement(query);
            pst.setInt(1, rs.getInt(1));
            pst.executeUpdate();
        }
    }


	/*
	 * Insert a connector
	 */
///*	public static Integer insertConnector(Connection con, String name,
//			String globalid, Point2D.Double location, String globalidfloor, Point2D.Double upperPoint, String upperFloorID)
//			throws SQLException {
//
//		PreparedStatement pst = null;
//		String query = null;
//		ResultSet result = null;
//		Integer itemid = null;
//		Integer floorid = null;
//		
//		query = "SELECT "+ITEM_ITEMID+" FROM "+T_FLOOR+" where "+ITEM_GLOBALID+" = ?"; 
//		pst = con.prepareStatement(query);
//		pst.setString(1, globalidfloor);
//		result = pst.executeQuery();
//		result.next();
//		floorid = result.getInt(1);
//		
//		//TODO: Create exceptions, if foreign key elements doesn't exist.
//		
//		String pointgeom = "ST_GeomFromText('POINT("+location.getX()+" "+location.getY()+")', -1)";
//		
//		if(upperPoint != null && upperFloorID != null) {
//			
//			query = "SELECT "+ITEM_ITEMID+" FROM "+T_FLOOR+" where "+ITEM_GLOBALID+" = ?"; 
//			pst = con.prepareStatement(query);
//			pst.setString(1, upperFloorID);
//			result = pst.executeQuery();
//			result.next();
//			Integer upperfloorid = result.getInt(1);
//			
//			String upperPointGeom = "ST_GeomFromText('POINT("+upperPoint.getX()+" "+upperPoint.getY()+")', -1)";
//		
//			query = "INSERT INTO "+T_CONNECTOR+"("+ITEM_NAME+","+ITEM_GLOBALID+","+FLOORITEM_FLOORID+"," +
//					""+AP_LOCATION+","+AP_TYPE+","+CONN_UPPERFLOOR+","+CONN_UPPERPOINT+")"
//					+ "(select ? as "+ITEM_NAME+", ? as "+ITEM_GLOBALID+", ? as "+FLOORITEM_FLOORID+"," +
//					""+pointgeom+" as "+AP_LOCATION+", ? as "+AP_TYPE+", ? as "+CONN_UPPERFLOOR+"," +
//					""+upperPointGeom+" as "+CONN_UPPERPOINT+") RETURNING "+ITEM_ITEMID+"";
//			pst.close();
//			pst = con.prepareStatement(query);
//			pst.setString(1, name);
//			pst.setString(2, globalid);
//			pst.setInt(3, floorid);
//			pst.setInt(4, new Integer(4));
//			pst.setInt(5, upperfloorid);
//			result = pst.executeQuery();
//			result.next();
//			itemid = result.getInt(1);
//			
//		} else {
//			query = "INSERT INTO "+T_CONNECTOR+"("+ITEM_NAME+","+ITEM_GLOBALID+","+FLOORITEM_FLOORID+"," +
//					""+AP_LOCATION+","+AP_TYPE+")"
//					+ "(select ? as "+ITEM_NAME+", ? as "+ITEM_GLOBALID+", ? as "+FLOORITEM_FLOORID+"," +
//					""+pointgeom+" as "+AP_LOCATION+", ? as "+AP_TYPE+") RETURNING "+ITEM_ITEMID+"";
//			pst.close();
//			pst = con.prepareStatement(query);
//			pst.setString(1, name);
//			pst.setString(2, globalid);
//			pst.setInt(3, floorid);
//			pst.setInt(4, new Integer(4));
//			result.close();
//			result = pst.executeQuery();
//			result.next();
//			itemid = result.getInt(1);
//		}
//		
//		return itemid;
//	}*/

    public static Integer insertConnector(Connection con, String name,
                                          String globalid, Point2D.Double location, String globalidfloor, Point2D.Double upperPoint, String upperFloorID)
            throws SQLException {

        PreparedStatement pst = null;
        String query = null;
        ResultSet result = null;
        Integer itemid = null;
        Integer floorid = null;

        query = "SELECT " + ITEM_ITEMID + " FROM " + T_FLOOR + " where " + ITEM_GLOBALID + " = ?";
        pst = con.prepareStatement(query);
        pst.setString(1, globalidfloor);
        result = pst.executeQuery();

        if (!result.next()) {
            System.out.print("The floor specified does not seem to exist. Did you remember to add it? \n");
            return 0;
        } else {
            floorid = result.getInt(1);
        }

        // Find out if stair exists
        query = "SELECT 1 FROM " + T_CONNECTOR + " WHERE " + ITEM_GLOBALID + "=?";
        pst = con.prepareStatement(query);
        pst.setString(1, globalid);
        result = pst.executeQuery();
        if (result.next()) {
            return 0;
        }

        //TODO: Create exceptions, if foreign key elements doesn't exist.

        String pointgeom = "ST_GeomFromText('POINT(" + location.getX() + " " + location.getY() + ")', -1)";

        Integer stairid = insertAccessPointConnector(con, name, pointgeom, globalid, floorid, 4);

        if (upperPoint != null && upperFloorID != null) {

            query = "SELECT " + ITEM_ITEMID + " FROM " + T_FLOOR + " where " + ITEM_GLOBALID + " = ?";
            pst = con.prepareStatement(query);
            pst.setString(1, upperFloorID);
            result = pst.executeQuery();
            result.next();
            Integer upperfloorid = result.getInt(1);

            String upperPointGeom = "ST_GeomFromText('POINT(" + upperPoint.getX() + " " + upperPoint.getY() + ")', -1)";

            query = "INSERT INTO " + T_CONNECTOR + "(" + ITEM_ITEMID + "," + ITEM_NAME + "," + ITEM_GLOBALID + "," + FLOORITEM_FLOORID + "," +
                            "" + AP_LOCATION + "," + AP_TYPE + "," + CONN_UPPERFLOOR + "," + CONN_UPPERPOINT + ")"
                            + "(select ? as " + ITEM_ITEMID + ", ? as " + ITEM_NAME + ", ? as " + ITEM_GLOBALID + ", ? as " + FLOORITEM_FLOORID + "," +
                            "" + pointgeom + " as " + AP_LOCATION + ", ? as " + AP_TYPE + ", ? as " + CONN_UPPERFLOOR + "," +
                            "" + upperPointGeom + " as " + CONN_UPPERPOINT + ") RETURNING " + ITEM_ITEMID + "";
            pst.close();
            pst = con.prepareStatement(query);
            pst.setInt(1, stairid);
            pst.setString(2, name);
            pst.setString(3, globalid);
            pst.setInt(4, floorid);
            pst.setInt(5, new Integer(4));
            pst.setInt(6, upperfloorid);
            result = pst.executeQuery();
            result.next();
            itemid = result.getInt(1);
        } else {
            query = "INSERT INTO " + T_CONNECTOR + "(" + ITEM_ITEMID + "," + ITEM_NAME + "," + ITEM_GLOBALID + "," + FLOORITEM_FLOORID + "," +
                            "" + AP_LOCATION + "," + AP_TYPE + ")"
                            + "(select ? as " + ITEM_ITEMID + ", ? as " + ITEM_NAME + ", ? as " + ITEM_GLOBALID + ", ? as " + FLOORITEM_FLOORID + "," +
                            "" + pointgeom + " as " + AP_LOCATION + ", ? as " + AP_TYPE + ") RETURNING " + ITEM_ITEMID + "";
            pst.close();
            pst = con.prepareStatement(query);
            pst.setInt(1, stairid);
            pst.setString(2, name);
            pst.setString(3, globalid);
            pst.setInt(4, floorid);
            pst.setInt(5, new Integer(4));
            result.close();
            result = pst.executeQuery();
            result.next();
            itemid = result.getInt(1);
        }
        return itemid;
    }

    public static Integer insertAccessPointConnector(Connection con, String name, String pointGeom,
                                                     String globalid, Integer floorid, Integer apType) throws SQLException {
        PreparedStatement pst = null;
        ResultSet rs = null;
        String query = null;
        ResultSet result = null;

        query = "SELECT 1 FROM " + T_ACCESSPOINT + " WHERE " + ITEM_GLOBALID + "=?";
        pst = con.prepareStatement(query);
        pst.setString(1, globalid);
        rs = pst.executeQuery();
        if (rs.next()) {
            query = "UPDATE " + T_ACCESSPOINT + " SET " + ITEM_NAME + "=?, " + FLOORITEM_FLOORID + "=?, " + AP_TYPE + "=?, " + AP_LOCATION + "=" + pointGeom + ", " + " WHERE " + ITEM_GLOBALID + "=? RETURNING " + ITEM_ITEMID + "";
            pst = con.prepareStatement(query);
            pst.setString(1, name);
            pst.setInt(2, floorid);
            pst.setInt(3, apType);
            pst.setString(4, globalid);
            result = pst.executeQuery();

        } else {
            query = "INSERT INTO " + T_ACCESSPOINT + "(" + ITEM_NAME + "," + ITEM_GLOBALID + "," + FLOORITEM_FLOORID + "," + AP_TYPE + "," + AP_LOCATION + ")"
                            + "(select ? as " + ITEM_NAME + ", ? as " + ITEM_GLOBALID + ", ? as " + FLOORITEM_FLOORID + ", ? as " + AP_TYPE + ", " + pointGeom + " as " + AP_LOCATION + ") RETURNING " + ITEM_ITEMID + "";
            pst.close();
            pst = con.prepareStatement(query);
            pst.setString(1, name);
            pst.setString(2, globalid);
            pst.setInt(3, floorid);
            pst.setInt(4, apType);
            result = pst.executeQuery();
        }

        result.next();
        Integer apID = result.getInt(1);
        return apID;
    }

    public static void connectPartAndAP(Connection con, Integer partID, Integer apID) throws SQLException {

        PreparedStatement pst = null;
        String query = null;

        if (partID != 0 && apID != 0) {
            query = "INSERT INTO " + T_APTOPART + "(" + A2P_PARTID + "," + A2P_APID + ") VALUES(?,?)";
            pst = con.prepareStatement(query);
            pst.setInt(1, partID);
            pst.setInt(2, apID);
            pst.executeUpdate();
        }
    }


    public static void connectPartAndCon(Connection con, Integer partID, Integer conID) throws SQLException {

        PreparedStatement pst = null;
        String query = null;

        if (partID != 0 && conID != 0) {
            query = "INSERT INTO " + T_CONTOPART + "(" + A2P_PARTID + "," + C2P_CONID + ") VALUES(?,?)";
            pst = con.prepareStatement(query);
            pst.setInt(1, partID);
            pst.setInt(2, conID);
            pst.executeUpdate();
        }
    }


    public static void connectPartAndCon(Connection con, String partGlobalID, Integer conID) throws SQLException {

        PreparedStatement pst = null;
        ResultSet rs = null;
        Integer partID = 0;
        String query = null;

        query = "SELECT " + ITEM_ITEMID + " FROM " + T_PARTITION + " WHERE " + ITEM_GLOBALID + "=?";
        pst = con.prepareStatement(query);
        pst.setString(1, partGlobalID);
        rs = pst.executeQuery();
        if (rs.next()) {
            partID = rs.getInt(1);
        }

        if (partID != 0) {
            query = "INSERT INTO " + T_CONTOPART + "(" + C2P_CONID + "," + C2P_PARTID + ") VALUES(?,?)";
            pst = con.prepareStatement(query);
            pst.setInt(1, conID);
            pst.setInt(2, partID);
            pst.executeUpdate();
        }
    }


    /*
     * Does an insert in aptopart according to the globalid's for a partition and an
     * access point. Delete must be done first, so that lazy update works. This only
     * does insert. Does not handle globalid's that does not exist, but checks
     */
    public static void connectPartAndAP(Connection con, String globalidPart,
                                        String globalidAP) throws SQLException {

        PreparedStatement pst = null;
        ResultSet rs = null;
        Integer partid = 0;
        Integer apid = 0;
        String query = null;

        query = "SELECT " + ITEM_ITEMID + " FROM " + T_PARTITION + " WHERE " + ITEM_GLOBALID + "=?";
        pst = con.prepareStatement(query);
        pst.setString(1, globalidPart);
        rs = pst.executeQuery();
        if (rs.next()) {
            partid = rs.getInt(1);
        }

        query = "SELECT " + ITEM_ITEMID + " FROM " + T_ACCESSPOINT + " WHERE " + ITEM_GLOBALID + "=?";
        pst = con.prepareStatement(query);
        pst.setString(1, globalidAP);
        rs = pst.executeQuery();
        if (rs.next()) {
            apid = rs.getInt(1);
        }

        if (partid != 0 && apid != 0) {

            query = "INSERT INTO " + T_APTOPART + "(" + A2P_PARTID + "," + A2P_APID + ") VALUES(?,?)";
            pst.close();
            pst = con.prepareStatement(query);
            pst.setInt(1, partid);
            pst.setInt(2, apid);
            pst.executeUpdate();
        }
    }

    public static void insertDecompRel(Connection con, Integer partOriginalID,
                                       Integer partDecompID) throws SQLException {

        PreparedStatement pst = null;
        ResultSet rs = null;
        Integer partOrgID = 0;
        Integer partDecID = 0;
        String query = null;

        query = "SELECT " + ITEM_ITEMID + " FROM " + T_PARTITION + " WHERE " + ITEM_ITEMID + "=?";
        pst = con.prepareStatement(query);
        pst.setInt(1, partOriginalID);
        rs = pst.executeQuery();
        if (rs.next()) {
            partOrgID = rs.getInt(1);
        }

        query = "SELECT " + ITEM_ITEMID + " FROM " + T_PARTITION + " WHERE " + ITEM_ITEMID + "=?";
        pst = con.prepareStatement(query);
        pst.setInt(1, partDecompID);
        rs = pst.executeQuery();
        if (rs.next()) {
            partDecID = rs.getInt(1);
        }

        if (partOrgID != 0 && partDecID != 0) {

            query = "INSERT INTO " + T_DECOMPREL + "(" + DECO_ORIGINAL + "," + DECO_DECOMP + ") VALUES(?,?)";
            pst.close();
            pst = con.prepareStatement(query);
            pst.setInt(1, partOrgID);
            pst.setInt(2, partDecID);
            pst.executeUpdate();
        }
    }

    public static void insertDoorBetweenPart(Connection con, Partition from, Partition to) throws SQLException {
        Door newDoor = createDoorForConRenewed(from, to);
        insertAccessPointWithGeom(con, newDoor.getName(), newDoor.getFinalCoord(), newDoor.getGlobalID(), newDoor.getFloorID(), 1, newDoor.getRepLine());
        connectPartAndAP(con, from.getGlobalID(), newDoor.getGlobalID());
        connectPartAndAP(con, to.getGlobalID(), newDoor.getGlobalID());
    }

    /*
     * Finds the appropriate place to insert an access point between to existing partition
     * to create a connection between them.
     * Several conditions/outcomes exists here, and all are not handled yet...
     */
    private static Door createDoorForConRenewed(Partition from, Partition to) throws SQLException {
        Door newDoor = new Door();
        Connection con = DB_Connection.connectToDatabase("conf/moovework.properties");

        //Get edges from partition polygons
        ArrayList<Line2D.Double> edgesFrom = Decomposition.polygonEdges(from.getPolygonGIS());
        ArrayList<Line2D.Double> edgesTo = Decomposition.polygonEdges(to.getPolygonGIS());

        //Get Polygon2D representations from partition polygons
        Polygon2D.Double polygonFrom = DB_WrapperSpatial.convertPolygonGIStoPolygon2D(from.getPolygonGIS());
        Polygon2D.Double polygonTo = DB_WrapperSpatial.convertPolygonGIStoPolygon2D(to.getPolygonGIS());

        //First - See if they have any overlapping edges
//        ArrayList<Line2D.Double> intersectionLines = DB_WrapperSpatial.ST_Intersection_Polygon_Polygon(con, polygonFrom, polygonTo);

        List<Line2D.Double> intersectionLines = SpatialHandler.polygonIntersectionLines(polygonFrom, polygonTo);
        if (!intersectionLines.isEmpty()) {
            // They have one or more overlapping edges...
            if (intersectionLines.size() > 1) {
                // They have more than one
                TreeMap<Double, Line2D.Double> lineLengths = new TreeMap<Double, Line2D.Double>();
                for (Line2D.Double l : intersectionLines) {
                    lineLengths.put(GeometricCalc.getLineLength(l), l);
                }

                Line2D.Double longestLine = lineLengths.lastEntry().getValue();
                Point2D.Double centerLongestLine = DB_WrapperSpatial.ST_Centroid_Line(con, longestLine);
                Line2D.Double ap_line = GeometricCalc.createSameLineDifLength(longestLine, centerLongestLine, 455.0);

                newDoor.setFinalCoord(centerLongestLine);
                newDoor.setFloorID(from.getFloor().getGlobalID());
                newDoor.setGlobalID("AP_" + from.getName() + "_to_" + to.getName());
                newDoor.setRepLine(ap_line);
                newDoor.setName("AP_" + from.getName() + "_to_" + to.getName());
            } else {
                // They have only one
                Line2D.Double mutualLine = intersectionLines.get(0);
                Point2D.Double centerMutualLine = DB_WrapperSpatial.ST_Centroid_Line(con, mutualLine);
                Line2D.Double ap_line = GeometricCalc.createSameLineDifLength(mutualLine, centerMutualLine, 455.0);

                newDoor.setFinalCoord(centerMutualLine);
                newDoor.setFloorID(from.getFloor().getGlobalID());
                newDoor.setGlobalID("AP_" + from.getName() + "_to_" + to.getName());
                newDoor.setRepLine(ap_line);
                newDoor.setName("AP_" + from.getName() + "_to_" + to.getName());
            }
        } else {

            //No overlapping edges, so first - find the edges from each polygon that are closest to the other

            Double dist = Double.MAX_VALUE;
            ArrayList<Line2D.Double> fromEdgesClosest = new ArrayList<Line2D.Double>();
            for (Line2D.Double edge : edgesFrom) {
                try {
                    Double dist_temp = DB_WrapperSpatial.ST_Distance_Line_Polygon(con, edge, to.getPolygonGIS());
                    if (dist_temp < dist) {
                        fromEdgesClosest.clear();
                        fromEdgesClosest.add(edge);
                        dist = dist_temp;
                    } else if (dist_temp.equals(dist)) {
                        fromEdgesClosest.add(edge);
                    }
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            dist = Double.MAX_VALUE;
            ArrayList<Line2D.Double> toEdgesClosest = new ArrayList<Line2D.Double>();
            for (Line2D.Double edge : edgesTo) {
                try {
                    Double dist_temp = DB_WrapperSpatial.ST_Distance_Line_Polygon(con, edge, from.getPolygonGIS());
                    if (dist_temp < dist) {
                        toEdgesClosest.clear();
                        toEdgesClosest.add(edge);
                        dist = dist_temp;
                    } else if (dist_temp.equals(dist)) {
                        toEdgesClosest.add(edge);
                    }
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            //Next - find the edges among the closest that have common path segments with the other polygon's edges
            ArrayList<Line2D.Double> fromEdgeCommons = new ArrayList<Line2D.Double>();
            ArrayList<Line2D.Double> toEdgeCommons = new ArrayList<Line2D.Double>();

            Double common_c = 0.0;

            for (Line2D.Double fromEdge : fromEdgesClosest) {
                for (Line2D.Double toEdge : toEdgesClosest) {
                    Double common = findCommon(con, fromEdge, toEdge);
                    if (common > common_c) {
                        if (!fromEdgeCommons.contains(fromEdge)) fromEdgeCommons.add(fromEdge);
                        if (!toEdgeCommons.contains(toEdge)) toEdgeCommons.add(toEdge);
                    }
                }
            }

            //Next - if multiple edges still exists, choose the ones that have the best (smallest) angle between them - the 'most' parallel
            if (fromEdgeCommons.size() > 1 || toEdgeCommons.size() > 1) {

                Double angle_min = Double.MAX_VALUE;

                Line2D.Double fromtheBestLine = null;
                Line2D.Double totheBestLine = null;

                for (Line2D.Double fromEdge : fromEdgeCommons) {
                    for (Line2D.Double toEdge : toEdgeCommons) {
                        Double angle = GeometricCalc.getAngleBetweenLines(fromEdge, toEdge);
                        if (angle < angle_min) {
                            angle_min = angle;
                            fromtheBestLine = fromEdge;
                            totheBestLine = toEdge;
                        }
                    }
                }

                //Find the mutual shortest line between the lines
                Point2D fromMaxX, fromMaxY, fromMinX, fromMinY;
                if (fromtheBestLine.getX1() > fromtheBestLine.getX2()) {
                    fromMaxX = fromtheBestLine.getP1();
                    fromMinX = fromtheBestLine.getP2();
                } else {
                    fromMaxX = fromtheBestLine.getP2();
                    fromMinX = fromtheBestLine.getP1();
                }
                if (fromtheBestLine.getY1() > fromtheBestLine.getY2()) {
                    fromMaxY = fromtheBestLine.getP1();
                    fromMinY = fromtheBestLine.getP2();
                } else {
                    fromMaxY = fromtheBestLine.getP2();
                    fromMinY = fromtheBestLine.getP1();
                }

                Point2D toMaxX, toMaxY, toMinX, toMinY;
                if (totheBestLine.getX1() > totheBestLine.getX2()) {
                    toMaxX = totheBestLine.getP1();
                    toMinX = totheBestLine.getP2();
                } else {
                    toMaxX = totheBestLine.getP2();
                    toMinX = totheBestLine.getP1();
                }
                if (totheBestLine.getY1() > totheBestLine.getY2()) {
                    toMaxY = totheBestLine.getP1();
                    toMinY = totheBestLine.getP2();
                } else {
                    toMaxY = totheBestLine.getP2();
                    toMinY = totheBestLine.getP1();
                }

                Double fromLineLength = GeometricCalc.getLineLength(fromtheBestLine);
                Double toLineLength = GeometricCalc.getLineLength(totheBestLine);

                Line2D.Double shortest = fromLineLength < toLineLength ? fromtheBestLine : totheBestLine;
                Point2D.Double p1 = (java.awt.geom.Point2D.Double) shortest.getP1();
                Point2D.Double p2 = (java.awt.geom.Point2D.Double) shortest.getP2();
                Double slopeShortest = null;
                Point2D.Double first = new Point2D.Double();
                Point2D.Double second = new Point2D.Double();
                if ((p2.getY() - p1.getY()) == 0) {
                    //line is horizontal, slope 0
                    slopeShortest = 0.0;
                } else if ((p2.getX() - p1.getX()) == 0) {
                    //line is vertical, slope infinity
                    slopeShortest = Double.NaN;
                } else {
                    slopeShortest = (p2.getX() - p1.getX()) / (p2.getY() - p1.getY());
                }

                if (Double.isNaN(slopeShortest)) {
                    //vertical
                    if (fromMinY.getY() < toMinY.getY()) {
                        first.setLocation(toMinY);
                    } else {
                        first.setLocation(fromMinY);
                    }
                    if (fromMaxY.getY() > toMaxY.getY()) {
                        second.setLocation(toMaxY);
                    } else {
                        second.setLocation(fromMaxY);
                    }
                } else if (slopeShortest == 0) {
                    //horizontal
                    if (fromMinX.getX() < toMinX.getX()) {
                        first.setLocation(toMinX);
                    } else {
                        first.setLocation(fromMinX);
                    }
                    if (fromMaxX.getX() > toMaxX.getX()) {
                        second.setLocation(toMaxX);
                    } else {
                        second.setLocation(fromMaxX);
                    }
                } else {

                    if (slopeShortest > 1) {
                        if (fromMinY.getY() < toMinY.getY()) {
                            first.setLocation(toMinY);
                        } else {
                            first.setLocation(fromMinY);
                        }
                        if (fromMaxY.getY() > toMaxY.getY()) {
                            second.setLocation(toMaxY);
                        } else {
                            second.setLocation(fromMaxY);
                        }
                    } else {
                        if (fromMinX.getX() < toMinX.getX()) {
                            first.setLocation(toMinX);
                        } else {
                            first.setLocation(fromMinX);
                        }
                        if (fromMaxX.getX() > toMaxX.getX()) {
                            second.setLocation(toMaxX);
                        } else {
                            second.setLocation(fromMaxX);
                        }
                    }
                }

                Line2D.Double mutualLine = new Line2D.Double(first, second);
                Point2D.Double centerMutLine = DB_WrapperSpatial.ST_Centroid_Line(con, mutualLine);
                Line2D.Double shortestLineBetweenFromTo = DB_WrapperSpatial.ST_ShortestLine_Line_Line(con, fromtheBestLine, totheBestLine);
                Line2D.Double doorRep = GeometricCalc.createPerpendicularLine(shortestLineBetweenFromTo, centerMutLine, 455.0);
                newDoor.setFinalCoord(centerMutLine);
                newDoor.setFloorID(from.getFloor().getGlobalID());
                newDoor.setGlobalID("AP_" + from.getName() + "_to_" + to.getName());
                newDoor.setRepLine(doorRep);
                newDoor.setName("AP_" + from.getName() + "_to_" + to.getName());


            }
            //If only one edges from each polygon exits - create the access point
            else if (!fromEdgeCommons.isEmpty() && !toEdgeCommons.isEmpty()) {

                Line2D.Double fromtheBestLine = fromEdgeCommons.get(0);
                Line2D.Double totheBestLine = toEdgeCommons.get(0);

                //Find the mutual shortest line between the lines
                Point2D fromMaxX, fromMaxY, fromMinX, fromMinY;
                if (fromtheBestLine.getX1() > fromtheBestLine.getX2()) {
                    fromMaxX = fromtheBestLine.getP1();
                    fromMinX = fromtheBestLine.getP2();
                } else {
                    fromMaxX = fromtheBestLine.getP2();
                    fromMinX = fromtheBestLine.getP1();
                }
                if (fromtheBestLine.getY1() > fromtheBestLine.getY2()) {
                    fromMaxY = fromtheBestLine.getP1();
                    fromMinY = fromtheBestLine.getP2();
                } else {
                    fromMaxY = fromtheBestLine.getP2();
                    fromMinY = fromtheBestLine.getP1();
                }

                Point2D toMaxX, toMaxY, toMinX, toMinY;
                if (totheBestLine.getX1() > totheBestLine.getX2()) {
                    toMaxX = totheBestLine.getP1();
                    toMinX = totheBestLine.getP2();
                } else {
                    toMaxX = totheBestLine.getP2();
                    toMinX = totheBestLine.getP1();
                }
                if (totheBestLine.getY1() > totheBestLine.getY2()) {
                    toMaxY = totheBestLine.getP1();
                    toMinY = totheBestLine.getP2();
                } else {
                    toMaxY = totheBestLine.getP2();
                    toMinY = totheBestLine.getP1();
                }

                Double fromLineLength = GeometricCalc.getLineLength(fromtheBestLine);
                Double toLineLength = GeometricCalc.getLineLength(totheBestLine);

                Line2D.Double shortest = fromLineLength < toLineLength ? fromtheBestLine : totheBestLine;
                Point2D.Double p1 = (java.awt.geom.Point2D.Double) shortest.getP1();
                Point2D.Double p2 = (java.awt.geom.Point2D.Double) shortest.getP2();
                Double slopeShortest = (p2.getX() - p1.getX()) / (p2.getY() - p1.getY());

                Point2D.Double first = new Point2D.Double();
                Point2D.Double second = new Point2D.Double();
                if ((p2.getY() - p1.getY()) == 0) {
                    //line is horizontal, slope 0
                    slopeShortest = 0.0;
                } else if ((p2.getX() - p1.getX()) == 0) {
                    //line is vertical, slope infinity
                    slopeShortest = Double.NaN;
                } else {
                    slopeShortest = (p2.getX() - p1.getX()) / (p2.getY() - p1.getY());
                }

                if (Double.isNaN(slopeShortest)) {
                    //vertical
                    if (fromMinY.getY() < toMinY.getY()) {
                        first.setLocation(toMinY);
                    } else {
                        first.setLocation(fromMinY);
                    }
                    if (fromMaxY.getY() > toMaxY.getY()) {
                        second.setLocation(toMaxY);
                    } else {
                        second.setLocation(fromMaxY);
                    }
                } else if (slopeShortest == 0) {
                    //horizontal
                    if (fromMinX.getX() < toMinX.getX()) {
                        first.setLocation(toMinX);
                    } else {
                        first.setLocation(fromMinX);
                    }
                    if (fromMaxX.getX() > toMaxX.getX()) {
                        second.setLocation(toMaxX);
                    } else {
                        second.setLocation(fromMaxX);
                    }
                } else {

                    if (slopeShortest > 1) {
                        if (fromMinY.getY() < toMinY.getY()) {
                            first.setLocation(toMinY);
                        } else {
                            first.setLocation(fromMinY);
                        }
                        if (fromMaxY.getY() > toMaxY.getY()) {
                            second.setLocation(toMaxY);
                        } else {
                            second.setLocation(fromMaxY);
                        }
                    } else {
                        if (fromMinX.getX() < toMinX.getX()) {
                            first.setLocation(toMinX);
                        } else {
                            first.setLocation(fromMinX);
                        }
                        if (fromMaxX.getX() > toMaxX.getX()) {
                            second.setLocation(toMaxX);
                        } else {
                            second.setLocation(fromMaxX);
                        }
                    }
                }

                Line2D.Double mutualLine = new Line2D.Double(first, second);
                Point2D.Double centerMutLine = DB_WrapperSpatial.ST_Centroid_Line(con, mutualLine);
                Line2D.Double shortestLineBetweenFromTo = DB_WrapperSpatial.ST_ShortestLine_Line_Line(con, fromtheBestLine, totheBestLine);
                Line2D.Double doorRep = GeometricCalc.createPerpendicularLine(shortestLineBetweenFromTo, centerMutLine, 455.0);
                newDoor.setFinalCoord(centerMutLine);
                newDoor.setFloorID(from.getFloor().getGlobalID());
                newDoor.setGlobalID("AP_" + from.getName() + "_to_" + to.getName());
                newDoor.setRepLine(doorRep);
                newDoor.setName("AP_" + from.getName() + "_to_" + to.getName());
            }
        }
        con.close();
        return newDoor;
    }

    /*
     * This methods finds the common path segment of two lines.
     */
    private static Double findCommon(Connection con, Line2D.Double a, Line2D.Double b) throws SQLException {

        //First - find the maximum and minimum points in terms of x and y coordinate

        //For line a
        Point2D.Double aMaxX, aMinX, aMaxY, aMinY;
        if (a.getX1() > a.getX2()) {
            aMaxX = (java.awt.geom.Point2D.Double) a.getP1();
            aMinX = (java.awt.geom.Point2D.Double) a.getP2();
        } else {
            aMaxX = (java.awt.geom.Point2D.Double) a.getP2();
            aMinX = (java.awt.geom.Point2D.Double) a.getP1();
        }
        if (a.getY1() > a.getY2()) {
            aMaxY = (java.awt.geom.Point2D.Double) a.getP1();
            aMinY = (java.awt.geom.Point2D.Double) a.getP2();
        } else {
            aMaxY = (java.awt.geom.Point2D.Double) a.getP2();
            aMinY = (java.awt.geom.Point2D.Double) a.getP1();
        }

        //For line b
        Point2D.Double bMaxX, bMinX, bMaxY, bMinY;
        if (b.getX1() > b.getX2()) {
            bMaxX = (java.awt.geom.Point2D.Double) b.getP1();
            bMinX = (java.awt.geom.Point2D.Double) b.getP2();
        } else {
            bMaxX = (java.awt.geom.Point2D.Double) b.getP2();
            bMinX = (java.awt.geom.Point2D.Double) b.getP1();
        }
        if (b.getY1() > b.getY2()) {
            bMaxY = (java.awt.geom.Point2D.Double) b.getP1();
            bMinY = (java.awt.geom.Point2D.Double) b.getP2();
        } else {
            bMaxY = (java.awt.geom.Point2D.Double) b.getP2();
            bMinY = (java.awt.geom.Point2D.Double) b.getP1();
        }

        //Find the lengths on the lines and determine slope of it
        Double aLineLength = GeometricCalc.getLineLength(a);
        Double bLineLength = GeometricCalc.getLineLength(b);

        Line2D.Double shortest = aLineLength < bLineLength ? a : b;
        Point2D.Double p1 = (java.awt.geom.Point2D.Double) shortest.getP1();
        Point2D.Double p2 = (java.awt.geom.Point2D.Double) shortest.getP2();
        Double slopeShortest;
        Double commonDist1 = 0.0;
        Double commonDist2 = 0.0;
        if ((p2.getY() - p1.getY()) == 0) {
            //line is horizontal, slope 0
            slopeShortest = 0.0;
        } else if ((p2.getX() - p1.getX()) == 0) {
            //line is vertical, slope infinity
            slopeShortest = Double.NaN;
        } else {
            slopeShortest = Math.abs((p2.getX() - p1.getX()) / (p2.getY() - p1.getY()));
        }

        //Depending on the slope,
        if (Double.isNaN(slopeShortest)) {
            //vertical
            //If one line encompasses the other, just return the shortest
            if (aLineLength < bLineLength) {
                if (aMinY.getY() > bMinY.getY() && aMaxY.getY() < bMaxY.getY()) {
                    return GeometricCalc.getLineLength(a);
                }
            } else {
                if (bMinY.getY() > aMinY.getY() && bMaxY.getY() < aMaxY.getY()) {
                    return GeometricCalc.getLineLength(b);
                }
            }

            //If not, find common segment by creating a perpendicular line and find the intersection with the other line
            if (aMinY.getY() < bMinY.getY()) {
                //create perpendicular to that point
                Line2D.Double perp = GeometricCalc.createPerpendicularLine(b, bMinY, 2000.0);
                Point2D.Double intersect = DB_WrapperSpatial.ST_Intersection_Line_Line(con, a, perp);

                if (intersect != null) {
                    commonDist1 = GeometricCalc.getLineLength(new Line2D.Double(intersect, aMaxY));
                }
            } else {
                //create perpendicular to that point
                Line2D.Double perp = GeometricCalc.createPerpendicularLine(a, aMinY, 2000.0);
                Point2D.Double intersect = DB_WrapperSpatial.ST_Intersection_Line_Line(con, b, perp);

                if (intersect != null) {
                    commonDist1 = GeometricCalc.getLineLength(new Line2D.Double(intersect, bMaxY));
                }
            }

            if (aMaxY.getY() > bMaxY.getY()) {
                //create perpendicular to that point
                Line2D.Double perp = GeometricCalc.createPerpendicularLine(b, bMaxY, 2000.0);
                Point2D.Double intersect = DB_WrapperSpatial.ST_Intersection_Line_Line(con, a, perp);

                if (intersect != null) {
                    commonDist2 = GeometricCalc.getLineLength(new Line2D.Double(intersect, aMinY));
                }
            } else {
                //create perpendicular to that point
                Line2D.Double perp = GeometricCalc.createPerpendicularLine(a, aMaxY, 2000.0);
                Point2D.Double intersect = DB_WrapperSpatial.ST_Intersection_Line_Line(con, b, perp);

                if (intersect != null) {
                    commonDist2 = GeometricCalc.getLineLength(new Line2D.Double(intersect, bMinY));
                }
            }
        } else if (slopeShortest == 0) {
            //horizontal
            //If one line encompasses the other, just return the shortest
            if (aLineLength < bLineLength) {
                if (aMinX.getX() > bMinX.getX() && aMaxX.getX() < bMaxX.getX()) {
                    return GeometricCalc.getLineLength(a);
                }
            } else {
                if (bMinX.getX() > aMinX.getX() && bMaxX.getX() < aMaxX.getX()) {
                    return GeometricCalc.getLineLength(b);
                }
            }

            //If not, find common segment by creating a perpendicular line and find the intersection with the other line
            if (aMinX.getX() < bMinX.getX()) {
                //create perpendicular to that point
                Line2D.Double perp = GeometricCalc.createPerpendicularLine(b, bMinX, 2000.0);
                Point2D.Double intersect = DB_WrapperSpatial.ST_Intersection_Line_Line(con, a, perp);

                if (intersect != null) {
                    commonDist1 = GeometricCalc.getLineLength(new Line2D.Double(intersect, aMaxX));
                }
            } else {
                //create perpendicular to that point
                Line2D.Double perp = GeometricCalc.createPerpendicularLine(a, aMinX, 2000.0);
                Point2D.Double intersect = DB_WrapperSpatial.ST_Intersection_Line_Line(con, b, perp);

                if (intersect != null) {
                    commonDist1 = GeometricCalc.getLineLength(new Line2D.Double(intersect, bMaxX));
                }
            }
            if (aMaxX.getX() > bMaxX.getX()) {
                //create perpendicular to that point
                Line2D.Double perp = GeometricCalc.createPerpendicularLine(b, bMaxX, 2000.0);
                Point2D.Double intersect = DB_WrapperSpatial.ST_Intersection_Line_Line(con, a, perp);

                if (intersect != null) {
                    commonDist2 = GeometricCalc.getLineLength(new Line2D.Double(intersect, aMinX));
                }
            } else {
                //create perpendicular to that point
                Line2D.Double perp = GeometricCalc.createPerpendicularLine(a, aMaxX, 2000.0);
                Point2D.Double intersect = DB_WrapperSpatial.ST_Intersection_Line_Line(con, b, perp);

                if (intersect != null) {
                    commonDist2 = GeometricCalc.getLineLength(new Line2D.Double(intersect, bMinX));
                }
            }
        } else {
            // the line is not horizontal or vertical, ergo use slope to find common
            if (slopeShortest < 1) {
                //If one line encompasses the other, just return the shortest
                if (aLineLength < bLineLength) {
                    if (aMinY.getY() > bMinY.getY() && aMaxY.getY() < bMaxY.getY()) {
                        return GeometricCalc.getLineLength(a);
                    }
                } else {
                    if (bMinY.getY() > aMinY.getY() && bMaxY.getY() < aMaxY.getY()) {
                        return GeometricCalc.getLineLength(b);
                    }
                }

                //If not, find common segment by creating a perpendicular line and find the intersection with the other line
                if (aMinY.getY() < bMinY.getY()) {
                    //create perpendicular to that point
                    Line2D.Double perp = GeometricCalc.createPerpendicularLine(b, bMinY, 2000.0);
                    Point2D.Double intersect = DB_WrapperSpatial.ST_Intersection_Line_Line(con, a, perp);

                    if (intersect != null) {
                        commonDist1 = GeometricCalc.getLineLength(new Line2D.Double(intersect, aMaxY));
                    }
                } else {
                    //create perpendicular to that point
                    Line2D.Double perp = GeometricCalc.createPerpendicularLine(a, aMinY, 2000.0);
                    Point2D.Double intersect = DB_WrapperSpatial.ST_Intersection_Line_Line(con, b, perp);

                    if (intersect != null) {
                        commonDist1 = GeometricCalc.getLineLength(new Line2D.Double(intersect, bMaxY));
                    }
                }

                if (aMaxY.getY() > bMaxY.getY()) {
                    //create perpendicular to that point
                    Line2D.Double perp = GeometricCalc.createPerpendicularLine(b, bMaxY, 2000.0);
                    Point2D.Double intersect = DB_WrapperSpatial.ST_Intersection_Line_Line(con, a, perp);

                    if (intersect != null) {
                        commonDist2 = GeometricCalc.getLineLength(new Line2D.Double(intersect, aMinY));
                    }
                } else {
                    //create perpendicular to that point
                    Line2D.Double perp = GeometricCalc.createPerpendicularLine(a, aMaxY, 2000.0);
                    Point2D.Double intersect = DB_WrapperSpatial.ST_Intersection_Line_Line(con, b, perp);

                    if (intersect != null) {
                        commonDist2 = GeometricCalc.getLineLength(new Line2D.Double(intersect, bMinY));
                    }
                }
            } else {
                //If one line encompasses the other
                if (aLineLength < bLineLength) {
                    if (aMinX.getX() > bMinX.getX() && aMaxX.getX() < bMaxX.getX()) {
                        return GeometricCalc.getLineLength(a);
                    }
                } else {
                    if (bMinX.getX() > aMinX.getX() && bMaxX.getX() < aMaxX.getX()) {
                        return GeometricCalc.getLineLength(b);
                    }
                }
                if (aMinX.getX() < bMinX.getX()) {
                    //create perpendicular to that point
                    Line2D.Double perp = GeometricCalc.createPerpendicularLine(b, bMinX, 2000.0);
                    Point2D.Double intersect = DB_WrapperSpatial.ST_Intersection_Line_Line(con, a, perp);

                    if (intersect != null) {
                        commonDist1 = GeometricCalc.getLineLength(new Line2D.Double(intersect, aMaxX));
                    }
                } else {
                    //create perpendicular to that point
                    Line2D.Double perp = GeometricCalc.createPerpendicularLine(a, aMinX, 2000.0);
                    Point2D.Double intersect = DB_WrapperSpatial.ST_Intersection_Line_Line(con, b, perp);

                    if (intersect != null) {
                        commonDist1 = GeometricCalc.getLineLength(new Line2D.Double(intersect, bMaxX));
                    }
                }
                if (aMaxX.getX() > bMaxX.getX()) {
                    //create perpendicular to that point
                    Line2D.Double perp = GeometricCalc.createPerpendicularLine(b, bMaxX, 2000.0);
                    Point2D.Double intersect = DB_WrapperSpatial.ST_Intersection_Line_Line(con, a, perp);

                    if (intersect != null) {
                        commonDist2 = GeometricCalc.getLineLength(new Line2D.Double(intersect, aMinX));
                    }
                } else {
                    //create perpendicular to that point
                    Line2D.Double perp = GeometricCalc.createPerpendicularLine(a, aMaxX, 2000.0);
                    Point2D.Double intersect = DB_WrapperSpatial.ST_Intersection_Line_Line(con, b, perp);

                    if (intersect != null) {
                        commonDist2 = GeometricCalc.getLineLength(new Line2D.Double(intersect, bMinX));
                    }
                }
            }
        }

        Double longest = commonDist1 < commonDist2 ? commonDist2 : commonDist1;
        return longest;
    }
}