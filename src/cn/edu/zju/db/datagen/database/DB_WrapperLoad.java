package cn.edu.zju.db.datagen.database;

import cn.edu.zju.db.datagen.database.spatialobject.*;
import cn.edu.zju.db.datagen.ifc.datamanipulation.GeometricCalc;
import cn.edu.zju.db.datagen.indoorobject.utility.SpatialHandler;
import diva.util.java2d.Polygon2D;
import org.postgis.LineString;
import org.postgis.Point;
import org.postgis.Polygon;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DB_WrapperLoad extends DB_Create {

    /*
     * Object tables (Reconsider datatypes)
     */
    public static ArrayList<Floor> floorT = new ArrayList<Floor>();
    public static ArrayList<Partition> partitionT = new ArrayList<Partition>();
    public static ArrayList<Partition> partitionDecomposedT = new ArrayList<Partition>();
    public static ArrayList<AccessPoint> accesspointT = new ArrayList<AccessPoint>();
    public static ArrayList<DecompRel> decomprelT = new ArrayList<DecompRel>();
    public static ArrayList<ApToPart> aptopartT = new ArrayList<ApToPart>();
    public static ArrayList<ConToPart> contopartT = new ArrayList<ConToPart>();

    public static ArrayList<Connector> connectorT = new ArrayList<Connector>();
    public static ArrayList<AccessRule> accessruleT = new ArrayList<AccessRule>();
    public static ArrayList<Connectivity> connectivityT = new ArrayList<Connectivity>();

    public static ArrayList<AccessPoint> accessPointConnectorT = new ArrayList<AccessPoint>();

    private static void resetAllTables() {
        floorT = new ArrayList<Floor>();
        partitionT = new ArrayList<Partition>();
        partitionDecomposedT = new ArrayList<Partition>();
        accesspointT = new ArrayList<AccessPoint>();
        aptopartT = new ArrayList<ApToPart>();
        contopartT = new ArrayList<ConToPart>();
        decomprelT = new ArrayList<DecompRel>();

        connectorT = new ArrayList<Connector>();
        accessruleT = new ArrayList<AccessRule>();
        connectivityT = new ArrayList<Connectivity>();
        accessPointConnectorT = new ArrayList<AccessPoint>();
    }

    private static void newConnectItAll() {
        for (AccessPoint ap : accesspointT) {
            ArrayList<Partition> rooms = getPartitionForAP(ap.getItemID());
            ArrayList<ApToPart> ap2parts = getAp2PartsForAP(ap.getItemID());
            ap.setPartitions(rooms);
            ap.setAp2parts(ap2parts);
        }

        for (Partition p : partitionT) {
            ArrayList<AccessPoint> doors = getAPsForPartition(p.getItemID());
            p.setAPs(doors);
            p.setConns(newGetConnectorsForPart(p));
            p.setAPConnectors(getAPConnectorsForPart(p));
            p.setOriPart(getOriForPart(p.getItemID()));
            p.setDecParts(getDecForPart(p.getItemID()));
        }

        for (Connector c : connectorT) {
            c.setPartitions(newGetPartitionForCon(c.getItemID()));
        }

        for (Floor f : floorT) {
            f.setAccessPoints(newGetDoorsForFloor(f));
            f.setPartitions(newGetRoomsForFloor(f));
            f.setConnectors(newGetConnectorsForFloor(f));
        }

        for (Connectivity c : connectivityT) {
            c.setAccRules(newGetAccessRules(c));
        }

        //added function, get all the partitions for one floor(decomposed excepted)
        for (Floor f : floorT) {
            ArrayList<Partition> partitions = f.getPartitions();
            ArrayList<Partition> partsAfterDecomposed = new ArrayList<Partition>();
            for (Partition part : partitions) {
                if (part.getName().equals("OUTDOOR") || !part.getDecParts().isEmpty()) {
                    continue;
                }
                partitionDecomposedT.add(part);        //get all the decomposed partition
                partsAfterDecomposed.add(part);
            }
            f.setPartsAfterDecomposed(partsAfterDecomposed);
        }

        for (Connector connector : connectorT) {
            Floor curFloor = connector.getFloor();
            curFloor.getConFloors().add(connector.getUpperFloor());
            if (connector.getUpperFloor() != null) {
                connector.getUpperFloor().getConFloors().add(curFloor);
            }
        }
    }


    /*
     * Look-up methods
     */
    public static Partition newGetRoom(Integer roomID) {
        for (Partition r : partitionT) {
            if (r.getItemID().equals(roomID)) {
                return r;
            }
        }
        return null;
    }

    private static ArrayList<Partition> newGetRoomsForFloor(Floor floor) {
        ArrayList<Partition> rooms = new ArrayList<Partition>();
        for (Partition r : partitionT) {
            if (r.getFloor().equals(floor)) {
                rooms.add(r);
            }
        }
        return rooms;
    }

    private static AccessPoint newGetDoor(Integer doorID) {
        for (AccessPoint d : accesspointT) {
            if (d.getItemID().equals(doorID)) {
                return d;
            }
        }
        return null;
    }

    private static ArrayList<AccessPoint> newGetDoorsForFloor(Floor floor) {
        ArrayList<AccessPoint> doors = new ArrayList<AccessPoint>();
        for (AccessPoint d : accesspointT) {
            if (d.getFloor().equals(floor)) {
                doors.add(d);
            }
        }
        return doors;
    }

    private static Floor newGetFloor(Integer floorID) {
        for (Floor f : floorT) {
            if (f.getItemID().equals(floorID)) {
                return f;
            }
        }
        return null;
    }

    private static Partition getOriForPart(Integer partID) {
        for (DecompRel dcr : decomprelT) {
            if (dcr.getPartDecID().equals(partID)) {
                return newGetRoom(dcr.getPartOrgID());
            }
        }
        return null;
    }

    private static ArrayList<Partition> getDecForPart(Integer partID) {
        ArrayList<Partition> decParts = new ArrayList<Partition>();
        for (DecompRel dcr : decomprelT) {
            if (dcr.getPartOrgID().equals(partID)) {
                decParts.add(newGetRoom(dcr.getPartDecID()));
            }
        }
        return decParts;
    }

    private static ArrayList<Partition> getPartitionForAP(Integer apID) {

        ArrayList<Partition> partitions = new ArrayList<Partition>();

        for (ApToPart a2p : aptopartT) {
            if (a2p.getApID().equals(apID)) {
                partitions.add(newGetRoom(a2p.getPartID()));
            }
        }
        return partitions;
    }

    private static ArrayList<Partition> newGetPartitionForCon(Integer conID) {

        ArrayList<Partition> partitions = new ArrayList<Partition>();

        for (ConToPart c2p : contopartT) {
            if (c2p.getConID().equals(conID)) {
                partitions.add(newGetRoom(c2p.getPartID()));
            }
        }
        return partitions;
    }

    private static ArrayList<ApToPart> getAp2PartsForAP(Integer apID) {
        ArrayList<ApToPart> a2ps = new ArrayList<ApToPart>();

        for (ApToPart a2p : aptopartT) {
            if (a2p.getApID().equals(apID)) {
                a2ps.add(a2p);
            }
        }
        return a2ps;
    }

    private static ArrayList<AccessPoint> getAPsForPartition(Integer partID) {

        ArrayList<AccessPoint> accesspoints = new ArrayList<AccessPoint>();

        for (ApToPart a2p : aptopartT) {
            if (a2p.getPartID().equals(partID)) {
                accesspoints.add(newGetDoor(a2p.getApID()));
            }
        }
        return accesspoints;
    }

    private static ArrayList<AccessRule> newGetAccessRules(Connectivity connect) {

        ArrayList<AccessRule> accrules = new ArrayList<AccessRule>();

        for (AccessRule ar : accessruleT) {
            if (ar.getCon().equals(connect)) {
                accrules.add(ar);
            }
        }
        return accrules;
    }

    private static Connector newGetConnector(Integer connID) {

        for (Connector c : connectorT) {
            if (c.getItemID().equals(connID)) {
                return c;
            }
        }
        return null;
    }

    private static ArrayList<AccessPoint> getAPConnectorsForPart(Partition p) {
        ArrayList<AccessPoint> aps = new ArrayList<AccessPoint>();
        for (AccessPoint ap : p.getAPs()) {
            aps.add(ap);
        }
        for (Connector connector : p.getConns()) {
            aps.add(connector);
        }
        return aps;
    }

    private static ArrayList<Connector> newGetConnectorsForPart(Partition p) {

        ArrayList<Connector> connectors = new ArrayList<Connector>();

        for (ConToPart c2p : contopartT) {
            if (c2p.getPartID().equals(p.getItemID())) {
                if (newGetConnector((c2p.getConID())).getApType() == 4) {
                    connectors.add(newGetConnector(c2p.getConID()));
                }
            }
        }
        return connectors;
    }

    private static ArrayList<Connector> newGetConnectorsForFloor(Floor floor) {

        ArrayList<Connector> conns = new ArrayList<Connector>();

        for (Connector c : connectorT) {
            if (c.getFloor().equals(floor)) {
                conns.add(c);
            }
        }
        return conns;
    }

    public static ArrayList<AccessRule> newGetAccessRulesForCon(Connectivity con) {

        ArrayList<AccessRule> accrules = new ArrayList<AccessRule>();

        for (AccessRule a : accessruleT) {
            if (con.getAp().getItemID().equals(a.getCon().getAp().getItemID())) {
                accrules.add(a);
            }
        }
        return accrules;
    }

    private static Connectivity newGetConnectivity(Integer apID) {
        for (Connectivity c : connectivityT) {
            if (c.getAp().getItemID().equals(apID)) {
                return c;
            }
        }
        return null;
    }

    /*
     * This method generates polygons to represent the 2D shape of a partition.
     * The size of the map, used to show these representations, is taken into account,
     * scaling each polygon to fit it.
     */
    private static void generatePolygons() {

        for (Partition r : partitionT) {
            if (r.getPolygonGIS() != null) {
                Polygon2D.Double polygon = new Polygon2D.Double();

                boolean isFirst = true;
                for (int i = 0; i < r.getPolygonGIS().numPoints() - 1; i++) {

                    Point point = r.getPolygonGIS().getPoint(i);
                    Point2D.Double targetPoint = new Point2D.Double();
                    Integer halfH = 300;

                    Double x = point.getX() / (2 * halfH);
                    x = x + 300;
                    Double y = (point.getY() / (2 * halfH));
                    y = Math.abs(300 - y);

                    targetPoint.setLocation(x, y);

                    if (isFirst) {
                        polygon.moveTo(targetPoint.getX(), targetPoint.getY());
                        isFirst = false;
                    } else {
                        polygon.lineTo(targetPoint.getX(), targetPoint.getY());
                    }
                }
                polygon.closePath();
                r.setPolygon2D(polygon);
            }

        }
    }

    /*
     * This method scales the placement of access point (and connectors),
     * according to the size of the map used for representation.
     */
    private static void generalizePlacement() {
        for (AccessPoint d : accesspointT) {
            Point2D.Double point = new Point2D.Double(d.getLocationGIS().getX(), d.getLocationGIS().getY());

            Integer halfH = 300;
            Double x = point.getX() / (2 * halfH);
            x = x + 300;
            Double y = point.getY() / (2 * halfH);
            y = Math.abs(300 - y);
            point.setLocation(x, y);
            d.setLocation2D(point);

            Point2D.Double first = (java.awt.geom.Point2D.Double) DB_WrapperSpatial.convertLinestringGIStoLine2D(d.getLineGIS()).getP1();

            x = first.getX() / (2 * halfH);
            x = x + 300;
            y = first.getY() / (2 * halfH);
            y = Math.abs(300 - y);
            first.setLocation(x, y);

            Point2D.Double last = (java.awt.geom.Point2D.Double) DB_WrapperSpatial.convertLinestringGIStoLine2D(d.getLineGIS()).getP2();

            x = last.getX() / (2 * halfH);
            x = x + 300;
            y = last.getY() / (2 * halfH);
            y = Math.abs(300 - y);
            last.setLocation(x, y);

            d.setLine2D(new Line2D.Double(first, last));

            for (int i = 0; i < d.getLine2DClickBox().getVertexCount(); i++) {
                x = d.getLine2DClickBox().getX(i) / (2 * halfH);
                x = x + 300;
                y = d.getLine2DClickBox().getY(i) / (2 * halfH);
                y = Math.abs(300 - y);

                d.getLine2DClickBox().setX(i, x);
                d.getLine2DClickBox().setY(i, y);
            }
        }

        for (Connector c : connectorT) {
            Point2D.Double point = new Point2D.Double(c.getLocationGIS().getX(), c.getLocationGIS().getY());

            Integer halfH = 300;
            Double x = point.getX() / (2 * halfH);
            x = x + 300;
            Double y = point.getY() / (2 * halfH);
            y = Math.abs(300 - y);

            point.setLocation(x, y);
            c.setLocation2D(point);

            if (c.getLocationUpperGIS() == null) {
                continue;
            }
            Point2D.Double upperPoint = null;
            upperPoint = new Point2D.Double(c.getLocationUpperGIS().getX(), c.getLocationUpperGIS().getY());
            x = upperPoint.getX() / (2 * halfH);
            x = x + 300;
            y = upperPoint.getY() / (2 * halfH);
            y = Math.abs(300 - y);

            upperPoint.setLocation(x, y);
            c.setUpperLocation2D(upperPoint);

        }
    }

    private static void generalizePlacementForConnector() {
        for (Connector connector : connectorT) {
            if (isConnectorTooFar(connector)) {
                for (Partition partition : connector.getPartitions()) {
                    if (partition.getFloor() == connector.getFloor()) {
                        connector.setLocation2D(SpatialHandler.calPolygonCenter(partition.getPolygon2D()));
                    } else {
                        connector.setUpperLocation2D(SpatialHandler.calPolygonCenter(partition.getPolygon2D()));
                    }
                }
            }
        }
    }

    private static boolean isConnectorTooFar(Connector connector) {
        double distanceThreshold = 50;
        for (Partition partition : connector.getPartitions()) {
            if (SpatialHandler.calDistancePointPolygon(connector.getLocation2D(), partition.getPolygon2D()) > distanceThreshold) {
                return true;
            }
        }
        return false;
    }

    /*
     * Takes care of all Object-Relational Mapping methods,
     * and polygon creation + placement scaling, for a specific file/building.
     */
    public static void loadALL(Connection con, Integer fileID) throws SQLException {


        resetAllTables();

        Integer buildingID = getBuildingID(con, fileID);

        loadFloorTable(con, buildingID);
        loadApToPartitionTable(con);
        loadConToPartitionTable(con);
        loadDecompRelTable(con);

        for (Floor f : floorT) {
            loadPartitionTable(con, f.getItemID());
            loadAccessPointTable(con, f.getItemID());
            loadConnectorTable(con, f.getItemID());
//			loadAccessPointConnectorTable(con, f.getItemID());
        }

        loadConnectivityTable(con);
        loadAccessRuleTable(con);

        generatePolygons();
        generalizePlacement();        //Connector's upper location to be added

        newConnectItAll();
        generalizePlacementForConnector();
    }

    public static Integer getBuildingID(Connection con, Integer fileID) throws SQLException {
        PreparedStatement pst = null;
        ResultSet rs = null;
        String query = null;
        Integer result = null;

        query = "SELECT " + ITEM_ITEMID + " FROM " + T_BUILDING + " WHERE " + BUILDING_FILEID + "=?";
        pst = con.prepareStatement(query);
        pst.setInt(1, fileID);
        rs = pst.executeQuery();
        if (rs.next()) result = rs.getInt(1);
        return result;
    }

    public static File loadFile(Connection con, UploadObject uo) throws SQLException {
        PreparedStatement pst = null;
        ResultSet rs = null;
        String query = null;
        File file = null;

        query = "SELECT upload_binary_file " +
                        "FROM uploads WHERE upload_id = ?";
        pst = con.prepareStatement(query);
        pst.setInt(1, uo.getUploadId());
        rs = pst.executeQuery();

        try {
            rs.next();
            file = new File(uo.getFilename());
            FileOutputStream fos = new FileOutputStream(file);
            byte[] fileBytes = rs.getBytes(1);
            fos.write(fileBytes);
            fos.close();
            uo.setFile_uploaded(file);

            rs.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return file;
    }

    public static ArrayList<UploadObject> loadFileTable(Connection con) throws SQLException {

        PreparedStatement pst = null;
        ResultSet rs = null;
        String query = null;
        ArrayList<UploadObject> files = new ArrayList<UploadObject>();

        query = "SELECT upload_id, " +
                        "upload_file_name, " +
                        "upload_file_type, " +
                        "upload_created, " +
                        "upload_edited, " +
                        "upload_description," +
                        "octet_length(upload_binary_file) as size FROM uploads";
        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        while (rs.next()) {
            UploadObject uo = new UploadObject();

            uo.setUploadId(rs.getInt(1));
            uo.setFilename(rs.getString(2));
            System.out.println("load File table " + rs.getString(2));
            uo.setFile_type(rs.getString(3));
            uo.setCreated(rs.getTimestamp(4));
            uo.setEdited(rs.getTimestamp(5));
            uo.setDescription(rs.getString(6));

            uo.setFile_size(rs.getInt("size"));

            files.add(uo);
        }
        return files;
    }

    private static void loadFloorTable(Connection con, Integer buildingID) throws SQLException {

        PreparedStatement pst = null;
        ResultSet rs = null;
        String query = null;

        query = "SELECT * FROM " + T_FLOOR + " WHERE " + FLOOR_BUILDINGID + "=?";
        pst = con.prepareStatement(query);
        pst.setInt(1, buildingID);
        rs = pst.executeQuery();

        ArrayList<Floor> floors = new ArrayList<Floor>();

        while (rs.next()) {
            Floor f = new Floor();
            f.setGlobalID(rs.getString(ITEM_GLOBALID));
            f.setItemID(rs.getInt(ITEM_ITEMID));
            f.setName(rs.getString(ITEM_NAME));
            floors.add(f);
        }
        floorT = floors;
    }

    private static void loadPartitionTable(Connection con, Integer floorID) throws SQLException {

        PreparedStatement pst = null;
        ResultSet rs = null;
        String query = null;

        query = "SELECT " + ITEM_GLOBALID + "," +
                        ITEM_ITEMID + "," +
                        ITEM_NAME + "," +
                        FLOORITEM_FLOORID + "," +
                        "ST_AsText(" + PART_GEOM + ") as geom" +
                        " FROM " + T_PARTITION + " WHERE " + FLOORITEM_FLOORID + "=?";
        pst = con.prepareStatement(query);
        pst.setInt(1, floorID);
        rs = pst.executeQuery();

        ArrayList<Partition> rooms = new ArrayList<Partition>();

        while (rs.next()) {
            Partition p = new Partition();
            p.setGlobalID(rs.getString(ITEM_GLOBALID));
            p.setItemID(rs.getInt(ITEM_ITEMID));
            p.setName(rs.getString(ITEM_NAME));
            p.setFloor(newGetFloor(rs.getInt(FLOORITEM_FLOORID)));
            if (rs.getString("geom") != null) {
                p.setPolygonGIS(new Polygon(rs.getString("geom")));
            } else {
                p.setPolygonGIS(null);
            }
            rooms.add(p);
        }
        partitionT.addAll(rooms);
    }

    private static void loadAccessPointTable(Connection con, Integer floorID) throws SQLException {

        PreparedStatement pst = null;
        ResultSet rs = null;
        String query = null;

        query = "SELECT " + ITEM_GLOBALID + "," +
                        ITEM_ITEMID + "," +
                        ITEM_NAME + "," +
                        FLOORITEM_FLOORID + "," +
                        AP_TYPE + "," +
                        "ST_AsText(" + AP_LOCATION + ") as location," +
                        "ST_AsText(" + AP_LINE + ") as line" +
                        " FROM ONLY " + T_ACCESSPOINT + " WHERE " + FLOORITEM_FLOORID + "=?";
        ;
        pst = con.prepareStatement(query);
        pst.setInt(1, floorID);
        rs = pst.executeQuery();

        ArrayList<AccessPoint> doors = new ArrayList<AccessPoint>();

        while (rs.next()) {
            AccessPoint ap = new AccessPoint();
            ap.setGlobalID(rs.getString(ITEM_GLOBALID));
            ap.setItemID(rs.getInt(ITEM_ITEMID));
            ap.setName(rs.getString(ITEM_NAME));
            ap.setLocationGIS(new Point(rs.getString("location")));
            ap.setApType(rs.getInt(AP_TYPE));

            ap.setFloor(newGetFloor(rs.getInt(FLOORITEM_FLOORID)));
            if (rs.getInt("AP_TYPE") != 4) {
                ap.setLineGIS(new LineString(rs.getString("line")));
                ap.setLine2DClickBox(GeometricCalc.createBoundingRectFromLine(ap.getLineGIS()));
                doors.add(ap);
            }

        }
        accesspointT.addAll(doors);
        accessPointConnectorT.addAll(doors);
    }


//	private static void loadAccessPointConnectorTable(Connection con, Integer floorID) throws SQLException {
//		
//		PreparedStatement pst = null;
//		ResultSet rs = null;
//		String query = null;
//		
//		query = "SELECT "+ITEM_GLOBALID+"," +
//				ITEM_ITEMID+","+
//				ITEM_NAME+","+
//				FLOORITEM_FLOORID+","+
//				AP_TYPE+","+
//				"ST_AsText("+AP_LOCATION+") as location" +
//				" FROM "+T_ACCESSPOINT+" WHERE "+FLOORITEM_FLOORID+"=?";;
//		pst = con.prepareStatement(query);
//		pst.setInt(1, floorID);
//		rs = pst.executeQuery();
//		
//		ArrayList<AccessPoint> doors = new ArrayList<AccessPoint>();
//		
//		while(rs.next()) {
//			AccessPoint ap = new AccessPoint();
//			ap.setGlobalID(rs.getString(ITEM_GLOBALID));
//			ap.setItemID(rs.getInt(ITEM_ITEMID));
//			ap.setName(rs.getString(ITEM_NAME));
//			ap.setLocationGIS(new Point(rs.getString("location")));
//			ap.setApType(rs.getInt(AP_TYPE));
//			
//			ap.setCurrentFloor(newGetFloor(rs.getInt(FLOORITEM_FLOORID)));
//			doors.add(ap);
//			
//		}
//		accessPointConnectorT.addAll(doors);
//	}


    private static void loadApToPartitionTable(Connection con) throws SQLException {

        PreparedStatement pst = null;
        ResultSet rs = null;
        String query = null;

        query = "SELECT * FROM " + T_APTOPART;
        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        ArrayList<ApToPart> doortorooms = new ArrayList<ApToPart>();

        while (rs.next()) {
            ApToPart d2r = new ApToPart();
            d2r.setApID(rs.getInt(A2P_APID));
            d2r.setPartID(rs.getInt(A2P_PARTID));

            doortorooms.add(d2r);
        }
        aptopartT = doortorooms;
    }

    private static void loadConToPartitionTable(Connection con) throws SQLException {

        PreparedStatement pst = null;
        ResultSet rs = null;
        String query = null;

        query = "SELECT * FROM " + T_CONTOPART;
        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        ArrayList<ConToPart> contoparts = new ArrayList<ConToPart>();

        while (rs.next()) {
            ConToPart c2p = new ConToPart();
            c2p.setConID(rs.getInt(C2P_CONID));
            c2p.setPartID(rs.getInt(C2P_PARTID));

            contoparts.add(c2p);
        }
        contopartT = contoparts;
    }

    private static void loadDecompRelTable(Connection con) throws SQLException {

        PreparedStatement pst = null;
        ResultSet rs = null;
        String query = null;

        query = "SELECT * FROM " + T_DECOMPREL;
        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        ArrayList<DecompRel> decompRels = new ArrayList<DecompRel>();

        while (rs.next()) {
            DecompRel dcr = new DecompRel();
            dcr.setPartOrgID(rs.getInt(DECO_ORIGINAL));
            dcr.setPartDecID(rs.getInt(DECO_DECOMP));

            decompRels.add(dcr);
        }
        decomprelT = decompRels;
    }

    private static void loadConnectorTable(Connection con, Integer floorID) throws SQLException {
        PreparedStatement pst = null;
        ResultSet rs = null;
        String query = null;

        query = "SELECT " + ITEM_GLOBALID + "," +
                        ITEM_ITEMID + "," +
                        ITEM_NAME + "," +
                        FLOORITEM_FLOORID + "," +
                        CONN_UPPERFLOOR + "," +
                        AP_TYPE + "," +
                        "ST_AsText(" + AP_LOCATION + ") as location," +
                        "ST_AsText(" + CONN_UPPERPOINT + ") as upperlocation" +
                        " FROM " + T_CONNECTOR + " WHERE " + FLOORITEM_FLOORID + "=?";
        pst = con.prepareStatement(query);
        pst.setInt(1, floorID);
        rs = pst.executeQuery();

        ArrayList<Connector> connectors = new ArrayList<Connector>();

        while (rs.next()) {
            Connector conn = new Connector();
            conn.setName(rs.getString(ITEM_NAME));
            conn.setItemID(rs.getInt(ITEM_ITEMID));
            conn.setGlobalID(rs.getString(ITEM_GLOBALID));
            conn.setFloor(newGetFloor(rs.getInt(FLOORITEM_FLOORID)));
            conn.setLocationGIS(new Point(rs.getString("location")));
            conn.setApType(rs.getInt(AP_TYPE));


            conn.setUpperFloor(newGetFloor(rs.getInt(CONN_UPPERFLOOR)));
            if (rs.getString("upperlocation") != null)
                conn.setLocationUpperGIS(new Point(rs.getString("upperlocation")));

            connectors.add(conn);
        }
        connectorT.addAll(connectors);
        accessPointConnectorT.addAll(connectors);
    }

    private static void loadAccessRuleTable(Connection con) throws SQLException {
        PreparedStatement pst = null;
        ResultSet rs = null;
        String query = null;

        query = "SELECT * FROM accessrule";
        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        ArrayList<AccessRule> accrules = new ArrayList<AccessRule>();

        while (rs.next()) {
            AccessRule accrule = new AccessRule();
            accrule.setAccID(rs.getInt(ACC_ID));
            accrule.setName(rs.getString(ACC_NAME));
            accrule.setCon(newGetConnectivity(rs.getInt(CON_APID)));

            accrules.add(accrule);
        }
        accessruleT = accrules;
    }

    private static void loadConnectivityTable(Connection con) throws SQLException {
        PreparedStatement pst = null;
        ResultSet rs = null;
        String query = null;

        query = "SELECT * FROM connectivity";
        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        ArrayList<Connectivity> cons = new ArrayList<Connectivity>();

        while (rs.next()) {
            AccessPoint ap = newGetDoor(rs.getInt(CON_APID));
            if (ap != null) {
                Connectivity connect = new Connectivity();
                connect.setAp(newGetDoor(rs.getInt(CON_APID)));
                connect.setPart1(newGetRoom(rs.getInt(CON_PART1ID)));
                connect.setPart2(newGetRoom(rs.getInt(CON_PART2ID)));
                cons.add(connect);
            }
        }
        connectivityT = cons;
    }
}
