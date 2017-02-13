package cn.edu.zju.db.datagen.database;

import cn.edu.zju.db.datagen.database.spatialobject.Floor;
import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import diva.util.java2d.Polygon2D;
import org.postgis.*;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DB_WrapperSpatial {

    /*
     * http://postgis.refractions.net/docs/ST_Area.html ST_Area Returns the
     * area of the surface if it is a polygon or multi-polygon.
     */
    public static Double ST_Area(Connection con, Polygon2D.Double pol) throws SQLException {

        PreparedStatement pst = null;
        String query = null;
        ResultSet rs = null;

        String polygonString = convertPolygon2DtoString(pol);

        query = "SELECT ST_Area(ST_GeomFromText('" + polygonString + "',-1));";
        pst = con.prepareStatement(query);
        rs = pst.executeQuery();
        Double area = 0.0;
        if (rs.next())
            area = (double) rs.getFloat(1);
        return area;
    }

    /*
     * http://www.postgis.org/docs/ST_Centroid.html ST_Centroid Returns the
     * geometric center of a geometry.
     */
    public static Point2D.Double ST_Centroid_Line(Connection con, Line2D line) throws SQLException {
        Point2D.Double lineCenter = new Point2D.Double();

        PreparedStatement pst = null;
        String query = null;
        ResultSet rs = null;

        String lineString = convertLine2DtoString((Line2D.Double) line);
        query = "SELECT ST_AsText(ST_Centroid('" + lineString + "'))";

        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        if (rs.next()) {
            Point point = new Point(rs.getString(1));
            lineCenter = convertPointGIStoPoint2D(point);
        }

        return lineCenter;
    }

    /*
     * http://www.postgis.org/docs/ST_Centroid.html ST_Centroid Returns the
     * geometric center of a geometry.
     */
    public static Point2D.Double ST_Centroid_Polygon(Connection con, Polygon2D pol) throws SQLException {
        Point2D.Double polCenter = new Point2D.Double();

        PreparedStatement pst = null;
        String query = null;
        ResultSet rs = null;

        String polString = convertPolygon2DtoString(pol);

        query = "SELECT ST_AsText(ST_Centroid('" + polString + "'))";

        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        if (rs.next()) {
            Point point = new Point(rs.getString(1));
            polCenter = convertPointGIStoPoint2D(point);
        }

        return polCenter;
    }

    /*
     * http://postgis.refractions.net/docs/ST_Distance.html ST_Distance For
     * geometry type Returns the 2-dimensional cartesian minimum distance (based
     * on spatial ref) between two geometries in projected units.
     */
    public static Double ST_Distance_Point_Line(Connection con, Point2D.Double point2d, Line2D.Double partEdge) throws SQLException {
        Double dist = null;

        PreparedStatement pst = null;
        String query = null;
        ResultSet rs = null;

        String point = convertPoint2DtoString(point2d);
        String line = convertLine2DtoString(partEdge);

        query = "SELECT ST_Distance(ST_GeomFromText('" + point + "'), ST_GeomFromText('" + line + "'))";

        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        if (rs.next()) {
            dist = new Double(rs.getString(1));
        }

        return dist;
    }

    /*
     * http://postgis.refractions.net/docs/ST_Distance.html ST_Distance For
     * geometry type Returns the 2-dimensional cartesian minimum distance (based
     * on spatial ref) between two geometries in projected units.
     */
    public static Double ST_Distance_Line_Polygon(Connection con, Line2D.Double edge, Polygon polygon) throws SQLException {
        Double dist = null;

        PreparedStatement pst = null;
        String query = null;
        ResultSet rs = null;

        String edgeGISString = convertLine2DtoString(edge);
        // String polygonGISString = convertPolygon2DtoString(polygon);

        query = "SELECT ST_Distance(ST_GeomFromText('" + edgeGISString + "'), ST_GeomFromText('" + polygon.toString() + "'))";

        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        if (rs.next()) {
            dist = new Double(rs.getString(1));
        }

        return dist;
    }

    /*
     * http://postgis.refractions.net/docs/ST_Distance.html ST_Distance For
     * geometry type Returns the 2-dimensional cartesian minimum distance (based
     * on spatial ref) between two geometries in projected units.
     */
    public static Double ST_Distance_Point_Polygon(Connection con, Point point, Polygon polygon) throws SQLException {
        Double dist = null;

        PreparedStatement pst = null;
        String query = null;
        ResultSet rs = null;

        query = "SELECT ST_Distance(ST_GeomFromText('" + point.toString() + "'), ST_GeomFromText('" + polygon.toString() + "'))";

        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        if (rs.next()) {
            dist = new Double(rs.getString(1));
        }

        return dist;
    }

    /*
     * http://postgis.refractions.net/documentation/manual-1.4/ST_DWithin.html
     * ST_DWithin Returns true if the geometries are within the specified
     * distance of one another
     */
    public static ArrayList<Partition> ST_DWithin_SpatialIndex(Connection con, Double range, Partition origin) throws SQLException {
        ArrayList<Partition> resultList = new ArrayList<Partition>();

        PreparedStatement pst = null;
        String query = null;
        ResultSet rs = null;

        query = "SELECT p2." + DB_Create.ITEM_ITEMID + " " + "FROM partition p1 " +
                        "LEFT JOIN partition p2 " +
                        "ON ST_DWithin(p1.part_geom, p2.part_geom, " + range + ")" +
                        "WHERE p1." + DB_Create.ITEM_GLOBALID + "='" + origin.getGlobalID() + "' AND p2.floor_id=" + origin.getFloor().getItemID() + ";";

        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        while (rs.next()) {
            resultList.add(DB_WrapperLoad.newGetRoom(rs.getInt(DB_Create.ITEM_ITEMID)));
        }

        return resultList;
    }

    /*
     * http://postgis.refractions.net/docs/ST_Envelope.html ST_Envelope -
     * Returns a geometry representing the double precision (float8) bounding
     * box of the supplied geometry.
     */
    public static Polygon2D.Double ST_Envelope_Polygon(Connection con, Polygon2D.Double pol) throws SQLException {

        PreparedStatement pst = null;
        String query = null;
        ResultSet rs = null;

        String polygonString = convertPolygon2DtoString(pol);

        query = "SELECT ST_AsText(ST_Envelope('" + polygonString + "'::geometry));";
        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        Polygon2D.Double boundary = null;
        if (rs.next()) {
            PGgeometry geom = new PGgeometry(rs.getString(1));

            if (geom.getGeoType() == Geometry.POLYGON) {
                boundary = convertPolygonGIStoPolygon2D((Polygon) geom.getGeometry());
            }
        }
        return boundary;
    }

    /*
     * http://postgis.refractions.net/docs/ST_Intersection.html ST_Intersection
     * (T) Returns a geometry that represents the shared portion of geomA and
     * geomB.
     */
    public static Point2D.Double ST_Intersection_Line_Line(Connection con, Line2D.Double lineA, Line2D.Double lineB) throws SQLException {
        PreparedStatement pst = null;
        String query = null;
        ResultSet rs = null;

        Point2D.Double inter2D = null;

        String lineStringA = convertLine2DtoString(lineA);
        String lineStringB = convertLine2DtoString(lineB);

        query = "SELECT ST_AsText(ST_Intersection('" + lineStringA + "'::geometry, '" + lineStringB + "'::geometry));";

        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        if (rs.next()) {
            PGgeometry geom = new PGgeometry(rs.getString(1));

            if (geom.getGeoType() == Geometry.POINT) {
                Point interPoint = (Point) geom.getGeometry();
                inter2D = new Point2D.Double(interPoint.getX(), interPoint.getY());
            }
        }
        return inter2D;
    }

    /*
     * http://postgis.refractions.net/docs/ST_Intersection.html ST_Intersection
     * (T) Returns a geometry that represents the shared portion of geomA and
     * geomB.
     */
    public static ArrayList<Line2D.Double> ST_Intersection_Polygon_Polygon(Connection con, Polygon2D.Double polygonA, Polygon2D.Double polygonB) throws SQLException {
        PreparedStatement pst = null;
        String query = null;
        ResultSet rs = null;

        ArrayList<Line2D.Double> lines = new ArrayList<Line2D.Double>();

        String polygonStringA = convertPolygon2DtoString(polygonA);
        String polygonStringB = convertPolygon2DtoString(polygonB);

        query = "SELECT ST_AsText((ST_Dump(ST_Intersection('" + polygonStringA + "'::geometry, '" + polygonStringB + "'::geometry))).geom);";

        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        while (rs.next()) {
            PGgeometry geom = new PGgeometry(rs.getString(1));

            if (geom.getGeoType() == Geometry.LINESTRING) {
                Line2D.Double line2D = null;
                LineString interLine = (LineString) geom.getGeometry();
                Point2D.Double p1 = new Point2D.Double(interLine.getFirstPoint().getX(), interLine.getFirstPoint().getY());
                Point2D.Double p2 = new Point2D.Double(interLine.getLastPoint().getX(), interLine.getLastPoint().getY());
                line2D = new Line2D.Double();
                line2D.setLine(p1, p2);
                lines.add(line2D);
            }
        }
        return lines;
    }

    /*
     * http://postgis.refractions.net/docs/ST_Intersects.html ST_Intersects
     * Returns TRUE if the Geometries/Geography "spatially intersect in 2D" -
     * (share any portion of space) and FALSE if they don't (they are Disjoint).
     */
    public static Boolean ST_Intersects_Line_Line(Connection con, Line2D.Double line1, Line2D.Double line2) throws SQLException {
        PreparedStatement pst = null;
        String query = null;
        ResultSet rs = null;
        Boolean intersects = null;
        String line1String = convertLine2DtoString(line1);
        String line2String = convertLine2DtoString(line2);

        query = "SELECT ST_Intersects('" + line1String + "'::geometry, '" + line2String + "'::geometry);";

        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        if (rs.next()) {
            if (rs.getString(1).equals("t")) {
                intersects = true;
            } else if (rs.getString(1).equals("f")) {
                intersects = false;
            }
        }
        return intersects;
    }

    /*
     * http://postgis.refractions.net/docs/ST_Intersects.html ST_Intersects
     * Returns TRUE if the Geometries/Geography "spatially intersect in 2D" -
     * (share any portion of space) and FALSE if they don't (they are Disjoint).
     */
    public static Boolean ST_Intersects_Line_Point(Connection con, Line2D.Double line, Point2D.Double p) throws SQLException {
        PreparedStatement pst = null;
        String query = null;
        ResultSet rs = null;
        Boolean intersects = null;
        String lineString = convertLine2DtoString(line);
        String pointString = convertPoint2DtoString(p);

        query = "SELECT ST_Intersects('" + pointString + "'::geometry, '" + lineString + "'::geometry);";

        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        if (rs.next()) {
            if (rs.getString(1).equals("t")) {
                intersects = true;
            } else if (rs.getString(1).equals("f")) {
                intersects = false;
            }
        }
        return intersects;
    }

    /*
     * http://postgis.refractions.net/docs/ST_Intersects.html ST_Intersects
     * Returns TRUE if the Geometries/Geography "spatially intersect in 2D" -
     * (share any portion of space) and FALSE if they don't (they are Disjoint).
     * This method utilises spatial indices in the database.
     */
    public static ArrayList<Partition> ST_Intersects_Line_Partition_SpatialIndex(Connection con, Line2D.Double line, Floor f) throws SQLException {
        PreparedStatement pst = null;
        String query = null;
        ResultSet rs = null;

        String lineString = convertLine2DtoString(line);

        query = "SELECT " + DB_Create.ITEM_ITEMID + " FROM " + DB_Create.T_PARTITION + " p " +
                        "LEFT JOIN " + DB_Create.T_DECOMPREL + " d " +
                        "ON p." + DB_Create.ITEM_ITEMID + " = d." + DB_Create.DECO_ORIGINAL + " " +
                        "WHERE d." + DB_Create.DECO_ORIGINAL + " IS NULL AND ST_Intersects(" + DB_Create.PART_GEOM + ",'" + lineString + "') AND "
                        + DB_Create.FLOORITEM_FLOORID + "=?";
        pst = con.prepareStatement(query);
        pst.setInt(1, f.getItemID());
        rs = pst.executeQuery();

        ArrayList<Partition> parts = new ArrayList<Partition>();
        while (rs.next()) {
            Partition part = DB_WrapperLoad.newGetRoom(rs.getInt(DB_Create.ITEM_ITEMID));
            parts.add(part);
        }
        return parts;
    }

    /*
     * http://postgis.refractions.net/docs/ST_ShortestLine.html ST_ShortestLine
     * Returns the 2-dimensional shortest line between two geometries
     */
    public static Line2D.Double ST_ShortestLine_Line_Line(Connection con, Line2D.Double lineA, Line2D.Double lineB) throws SQLException {
        Line2D.Double result = null;

        PreparedStatement pst = null;
        String query = null;
        ResultSet rs = null;

        String lineAGISString = convertLine2DtoString(lineA);
        String lineBGISString = convertLine2DtoString(lineB);

        query = "SELECT ST_AsText(ST_ShortestLine('" + lineAGISString + "'::geometry, '" + lineBGISString + "'::geometry)) As sline;";

        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        if (rs.next()) {
            result = convertLinestringGIStoLine2D(new LineString(rs.getString(1)));
        }

        return result;
    }

    /*
     * http://postgis.refractions.net/documentation/manual-2.0/ST_Split.html
     * ST_Split Returns a collection of geometries resulting by splitting a
     * geometry.
     */
    public static ArrayList<Polygon2D.Double> ST_Split_Polygon(Connection con, Polygon2D.Double polygon, Line2D.Double line) throws SQLException {
        ArrayList<Polygon2D.Double> polygonList = new ArrayList<Polygon2D.Double>();

        PreparedStatement pst = null;
        String query = null;
        ResultSet rs = null;

        String polygonGIS = convertPolygon2DtoString(polygon);
        String lineString = convertLine2DtoString(line);

        query = "SELECT ST_AsText((ST_Dump(ST_Split(polygon, line))).geom) as wkt " +
                        "FROM (SELECT ST_GeomFromText('" + lineString + "') As line, " + "ST_GeomFromText('" + polygonGIS
                        + "') As polygon) As foo";

        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        while (rs.next()) {
            Polygon polyG = new Polygon(rs.getString(1));
            Polygon2D.Double polygon2D = convertPolygonGIStoPolygon2D(polyG);
            polygonList.add(polygon2D);
        }

        return polygonList;
    }

    private static String convertPoint2DtoString(Point2D.Double p) {
        String pointAsString = "POINT (" + p.getX() + " " + p.getY() + ")";
        return pointAsString;
    }

    public static Point2D.Double convertPointGIStoPoint2D(Point point) {
        Point2D.Double p = new Point2D.Double(point.getX(), point.getY());
        return p;
    }

    public static String convertLine2DtoString(Line2D.Double line2d) {

        String lineAsString = "LINESTRING ( " + line2d.getX1() + " " + line2d.getY1() + ", " + line2d.getX2() + " " + line2d.getY2() + " )";

        return lineAsString;
    }

    public static Line2D.Double convertLinestringGIStoLine2D(LineString lineStringGIS) {
        Point2D.Double first = new Point2D.Double(lineStringGIS.getFirstPoint().getX(), lineStringGIS.getFirstPoint().getY());
        Point2D.Double last = new Point2D.Double(lineStringGIS.getLastPoint().getX(), lineStringGIS.getLastPoint().getY());
        Line2D.Double line = new Line2D.Double(first, last);

        return line;
    }

    public static Polygon2D.Double convertPolygonGIStoPolygon2D(Polygon polygonGIS) {
        Polygon2D.Double polygon = new Polygon2D.Double();
        boolean isFirst = true;

        for (int i = 0; i < polygonGIS.numPoints(); i++) {
            if (isFirst) {
                polygon.moveTo(polygonGIS.getPoint(i).getX(), polygonGIS.getPoint(i).getY());
                isFirst = false;
            } else {
                polygon.lineTo(polygonGIS.getPoint(i).getX(), polygonGIS.getPoint(i).getY());
            }
        }
        return polygon;
    }

    public static String convertPolygon2DtoString(Polygon2D polygon) throws SQLException {
        String polygonAsString = "POLYGON((";
        Boolean first = true;

        for (int i = 0; i < polygon.getVertexCount(); i++) {
            Point2D.Double p = (new Point2D.Double(polygon.getX(i), polygon.getY(i)));
            if (!first) {
                polygonAsString += ", ";
            }
            polygonAsString += p.getX() + " " + p.getY();
            first = false;
        }
        polygonAsString += "))";

        return polygonAsString;
    }

    public static Polygon convertPolygon2DtoPolygonGIS(Polygon2D polygon) throws SQLException {
        String polygonText = "POLYGON((";
        Boolean first = true;

        for (int i = 0; i < polygon.getVertexCount(); i++) {
            Point2D.Double p = (new Point2D.Double(polygon.getX(i), polygon.getY(i)));
            if (!first) {
                polygonText += ", ";
            }
            polygonText += p.getX() + " " + p.getY();
            first = false;
        }
        polygonText += "))";
        Polygon polygonGIS = new Polygon(polygonText);
        return polygonGIS;
    }


    public static ArrayList<Point2D.Double> ST_RandomPoints_In_Partition(Connection con, Polygon2D.Double polygon, int pointNum) throws SQLException {
        PreparedStatement pst = null;
        String query = null;
        ResultSet rs = null;


        Polygon polygonGIS = ringPolygon2DToGIS(polygon);
        String polygonGISString = polygonGIS.toString();
        query = "SELECT ST_AsText(RandomPointsInPolygon(' "
                        + "" + polygonGISString + "', "
                        + pointNum + "))";
        pst = con.prepareStatement(query);
        rs = pst.executeQuery();

        ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
        while (rs.next()) {
            Point point = new Point(rs.getString(1));
            Point2D.Double randomPoint = convertPointGIStoPoint2D(point);
            points.add(randomPoint);
        }
        return points;
    }

    public static Polygon ringPolygon2DToGIS(Polygon2D polygon) throws SQLException {
        String polygonText = "POLYGON((";
        Boolean first = true;

        for (int i = 0; i < polygon.getVertexCount(); i++) {
            Point2D.Double p = (new Point2D.Double(polygon.getX(i), polygon.getY(i)));
            if (!first) {
                polygonText += ", ";
            }
            polygonText += p.getX() + " " + p.getY();
            first = false;
        }
        polygonText += ", " + polygon.getX(0) + " " + polygon.getY(0);
        polygonText += "))";
        Polygon polygonGIS = new Polygon(polygonText);
        return polygonGIS;
    }
}
