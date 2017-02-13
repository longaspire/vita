package cn.edu.zju.db.datagen.ifc.datamanipulation;

import cn.edu.zju.db.datagen.database.*;
import cn.edu.zju.db.datagen.database.spatialobject.AccessPoint;
import cn.edu.zju.db.datagen.database.spatialobject.Connector;
import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import diva.util.java2d.Polygon2D;
import org.postgis.Polygon;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.UUID;

public class Decomposition {

    // Threshold determining when a partition without any turning points should be decomposed depending on each width/height ratio
    private static Double tShape = 0.2;
    // Threshold determining if a partition should be decomposed by looking at the waste space of its minimum bounding rectangle
    private static Double t_mbr = 0.90;
    // Threshold determining at which angle a turning point is considered a turning point
    private static Double t_angle = 200.0;
    // Threshold determining how far away from the middle the virtual door can be placed in order to align it with any turning points
    // This is set individually for each polygon, as a percentage of the length of the dimension used
    private static Double t_align = 0.0;

    /*
     * Finds out if a polygon is defined clockwise or anti-clockwise
     * http://stackoverflow.com/questions/1165647/how-to-determine-if-a-list-of-polygon-points-are-in-clockwise-order
     */
    private static boolean clockwisePolygon(Polygon2D.Double polygon) {
        boolean clockwise = true;
        ArrayList<Point2D.Double> pointList = new ArrayList<Point2D.Double>();
        Double edgevalue = 0.0;

        for (int i = 0; i < polygon.getVertexCount(); i++) {
            if (pointList.isEmpty()) {
                pointList.add(new Point2D.Double(polygon.getX(i), polygon.getY(i)));
            } else if (pointList.size() == 1) {
                pointList.add(new Point2D.Double(polygon.getX(i), polygon.getY(i)));
                Point2D.Double p1 = pointList.get(0);
                Point2D.Double p2 = pointList.get(1);
                edgevalue = edgevalue + ((p2.x - p1.x) * (p2.y + p1.y));
                pointList.remove(0);
            }
        }
        if (edgevalue < 0.0) {
            clockwise = false;
        }
        return clockwise;
    }

    /*
     * Creates a vector from two arrays of doubles
     */
    private static double[] createVector(double[] p0, double[] p1) {
        double v[] = {p1[0] - p0[0], p1[1] - p0[1]};
        return v;
    }

    /*
     * Compute the angle between two edges to find out if it is a turning point
     */
    private static double computeAngle(Point2D.Double p00, Point2D.Double p01, Point2D.Double p02, boolean clockwise) {
        double[] p0 = {p00.x, p00.y};
        double[] p1 = {p01.x, p01.y};
        double[] p2 = {p02.x, p02.y};

        double[] v0 = createVector(p0, p1);
        double[] v1 = createVector(p1, p2);

        double dotProduct = v0[0] * v1[0] + v0[1] * v1[1];
        double crossProduct = (v0[0] * v1[1] - v1[0] * v0[1]);

        double angle;
        if (clockwise) {
            angle = Math.toDegrees(Math.PI + Math.atan2(crossProduct, dotProduct));
        } else {
            angle = Math.toDegrees(Math.PI - Math.atan2(crossProduct, dotProduct));
        }

        return angle;
    }

    /*
     * Take out polygon edges from a PolygonGIS and store them in an arraylist
     * of Line2Ds
     */
    public static ArrayList<Line2D.Double> polygonEdges(Polygon polygonGIS) {
        Polygon2D.Double polygon = DB_WrapperSpatial.convertPolygonGIStoPolygon2D(polygonGIS);
        ArrayList<Line2D.Double> edges = new ArrayList<Line2D.Double>();
        ArrayList<Point2D.Double> interList = new ArrayList<Point2D.Double>();

        for (int i = 0; i < polygon.getVertexCount(); i++) {
            if (interList.isEmpty()) {
                interList.add(new Point2D.Double(polygon.getX(i), polygon.getY(i)));
            } else {
                edges.add(new Line2D.Double(interList.get(0), new Point2D.Double(polygon.getX(i), polygon.getY(i))));
                interList.clear();
                interList.add(new Point2D.Double(polygon.getX(i), polygon.getY(i)));
            }
        }
        return edges;
    }

    /*
     * Decomposes a partition to one or several sub-partitions, maps access
     * points connected to the original partition to the new partitions and
     * creates virtual access points between intersecting decomposed partitions
     */
    public static void decomposePart(Connection con, Partition partOrg) throws SQLException {
        ArrayList<Polygon2D.Double> finalList = new ArrayList<Polygon2D.Double>();
        Polygon2D.Double polygon = DB_WrapperSpatial.convertPolygonGIStoPolygon2D(partOrg.getPolygonGIS());
        ArrayList<Polygon2D.Double> tempList = new ArrayList<Polygon2D.Double>();
        decompositionAlg(con, polygon, tempList, partOrg); // The decomposition

        finalList = tempList;
        ArrayList<Partition> partArray = new ArrayList<Partition>();
        char c = 'A';
        // If the polygon has been decomposed
        if (finalList.size() > 1) {
            // Partitions for the decomposed polygons are created
            for (Polygon2D.Double polyG : finalList) {
                Partition partDec = new Partition();
                partDec.setName(partOrg.getName() + c);
                partDec.setGlobalID(partOrg.getGlobalID() + c);
                partDec.setPolygonGIS(DB_WrapperSpatial.convertPolygon2DtoPolygonGIS(polyG));
                partDec.setPolygon2D(polyG);
                partDec.setFloor(partOrg.getFloor());
                Integer id = DB_WrapperInsert.insertPartition(con, partDec.getName(), partDec.getGlobalID(), partOrg.getFloor().getGlobalID(), polyG);
                partDec.setItemID(id);
                // Map to original part
                DB_WrapperInsert.insertDecompRel(con, partOrg.getItemID(), id);
                partArray.add(partDec);
                c++;
            }

            // Access points mapped to the original partition are being mapped
            // to the decomposed partitions
            for (AccessPoint ap : partOrg.getAPs()) {
                TreeMap<Double, Partition> distMap = new TreeMap<Double, Partition>();
                // Map to closest of decomposed partitions
                for (Partition p : partArray) {
                    distMap.put(DB_WrapperSpatial.ST_Distance_Point_Polygon(con, ap.getLocationGIS(), p.getPolygonGIS()), p);
                }
                Partition shortestP = distMap.firstEntry().getValue();
                Partition conPart = null;

                // Find the other partition connected to the access point
                for (Partition p : ap.getPartitions()) {
                    if (p != partOrg) {
                        conPart = p;
                    }
                }

                ArrayList<Partition> parts = new ArrayList<Partition>();
                parts.add(shortestP);
                if (conPart != null)
                    parts.add(conPart);
                ap.setPartitions(parts);

                shortestP.getAPs().add(ap);

                DB_WrapperInsert.flushPartitionForAPConnections(con, ap.getGlobalID());
                DB_WrapperInsert.connectPartAndAP(con, shortestP.getItemID(), ap.getItemID());
                if (conPart != null)
                    DB_WrapperInsert.connectPartAndAP(con, conPart.getItemID(), ap.getItemID());
            }

            //Connector mapped to the original partition is mapped to the decomposed partition
            for (Connector connector : partOrg.getConns()) {
                TreeMap<Double, Partition> distMap = new TreeMap<Double, Partition>();
                for (Partition p : partArray) {
                    distMap.put(DB_WrapperSpatial.ST_Distance_Point_Polygon(con, connector.getLocationGIS(), p.getPolygonGIS()), p);
                }
                Partition shortestP = distMap.firstEntry().getValue();
                Partition conPart = null;

                for (Partition p : connector.getPartitions()) {
                    if (p != partOrg) {
                        conPart = p;
                    }
                }
                ArrayList<Partition> parts = new ArrayList<Partition>();
                parts.add(shortestP);
                if (conPart != null) {
                    connector.setUpperPartition(conPart);
                    parts.add(conPart);
                }
                connector.setPartitions(parts);        //connector.setPartitions()? setUpperPart()?

                shortestP.getConns().add(connector);
                DB_WrapperInsert.flushPartitionForConnectors(con, connector.getGlobalID());
                DB_WrapperInsert.connectPartAndCon(con, shortestP.getItemID(), connector.getItemID());
                if (conPart != null) {
                    DB_WrapperInsert.connectPartAndCon(con, conPart.getItemID(), connector.getItemID());
                }
            }


            //Create Virtual Access Point between two decomposed partition
            for (int i = 0; i < partArray.size(); i++) {
                Partition part1 = partArray.get(i);
                for (int j = i + 1; j < partArray.size(); j++) {
                    Partition part2 = partArray.get(j);
                    ArrayList<Line2D.Double> intersectLines = DB_WrapperSpatial.ST_Intersection_Polygon_Polygon(con, part1.getPolygon2D(), part2.getPolygon2D());
                    if (!intersectLines.isEmpty()) {
                        Line2D.Double apLine = intersectLines.get(0);
                        String apGlobalID = UUID.randomUUID().toString();
                        String name1 = part1.getItemID() + "_and_" + part2.getItemID();
                        String name2 = part2.getItemID() + "_and_" + part1.getItemID();
                        Integer apID = DB_WrapperInsert.insertVirtualAccessPointWithGeom(con, name1, name2,
                                DB_WrapperSpatial.ST_Centroid_Line(con, apLine), apGlobalID,
                                part1.getFloor().getGlobalID(), 2, apLine);
                        DB_WrapperInsert.connectPartAndAP(con, part1.getItemID(), apID);
                        DB_WrapperInsert.connectPartAndAP(con, part2.getItemID(), apID);
                    }
                }
            }

        }
    }

    /*
     * Decomposes a polygon using turning points
     */
    private static void decompositionAlg(Connection con, Polygon2D.Double polygon, ArrayList<Polygon2D.Double> finalList, Partition p) throws SQLException {
        ArrayList<Point2D.Double> tpList = findTPs(polygon);

        Double areaPol = DB_WrapperSpatial.ST_Area(con, polygon);
        Double areaMBR = DB_WrapperSpatial.ST_Area(con, DB_WrapperSpatial.ST_Envelope_Polygon(con, polygon));
        Double height = polygon.getBounds2D().getHeight();
        Double width = polygon.getBounds2D().getWidth();
        Double min = Math.min(height, width);
        Double max = Math.max(height, width);

        // If waste space is larger than t_mbr the polygon is decomposed
        if (!tpList.isEmpty() && (areaPol / areaMBR) < t_mbr) {
            // Use turning closest to the middle of the polygon
            Point2D.Double tp = closestTP(con, polygon, tpList);

            Line2D.Double widthLine = new Line2D.Double();
            Line2D.Double heightLine = new Line2D.Double();

            // Split polygon on both width and height to determine best split
            widthLine.setLine(tp.getX() - width, tp.getY(), tp.getX() + width, tp.getY());
            ArrayList<Polygon2D.Double> polygonListWidth = DB_WrapperSpatial.ST_Split_Polygon(con, polygon, widthLine);
            Double ratioWidth = findLowestRatioOfPolygons(polygonListWidth);
            heightLine.setLine(tp.getX(), tp.getY() - height, tp.getX(), tp.getY() + height);
            ArrayList<Polygon2D.Double> polygonListHeight = DB_WrapperSpatial.ST_Split_Polygon(con, polygon, heightLine);
            Double ratioHeigth = findLowestRatioOfPolygons(polygonListHeight);

            ArrayList<Polygon2D.Double> polygonList = ratioHeigth <= ratioWidth ? polygonListWidth : polygonListHeight;
            // Recursive until no more turning points or t_mbr is satisfied
            for (Polygon2D.Double polyG : polygonList) {
                decompositionAlg(con, polyG, finalList, p);
            }
        } else if ((min / max) < tShape) {
            // If ratio between dimensions is smaller than tShape the polygon is
            // decomposed
            Point2D.Double center = new Point2D.Double(polygon.getBounds2D().getCenterX(), polygon.getBounds2D().getCenterY());
            Double cX = center.getX();
            Double cY = center.getY();

            Double boundMaxX = polygon.getBounds2D().getMaxX();
            Double boundMinX = polygon.getBounds2D().getMinX();
            Double boundMaxY = polygon.getBounds2D().getMaxY();
            Double boundMinY = polygon.getBounds2D().getMinY();

            Double distX = 0.0;
            Double distY = 0.0;
            Double dist = 0.0;

            for (int i = 0; i < polygon.getVertexCount(); i++) {
                Double pX = polygon.getX(i);
                Double pY = polygon.getY(i);

                // If a point in the polygon is close to the middle of the polygon
                // on the x-axis the polygon is decomposed through this point
                if ((cX - t_align) < pX && (cX + t_align) > pX && !boundMaxX.equals(pX) && !boundMinX.equals(pX)) {
                    dist = Math.abs(cX - pX);
                    if (distX.equals(0.0) || distX > dist) {
                        distX = dist;
                        center.setLocation(pX, cY);
                    }
                }
                // If a point in the polygon is close to the middle of the polygon
                // on the y-axis the polygon is decomposed through this point
                if ((cY - t_align) < pY && (cY + t_align) > pY && !boundMaxY.equals(pY) && !boundMinY.equals(pY)) {
                    dist = Math.abs(cY - pY);
                    if (distY.equals(0.0) || distY > dist) {
                        distY = dist;
                        center.setLocation(cX, pY);
                    }
                }
            }

            ArrayList<Polygon2D.Double> polygonList = decomposePolyDimension(con, polygon, center);
            // Recursive until threshold is met
            for (Polygon2D.Double polyG : polygonList) {
                decompositionAlg(con, polyG, finalList, p);
            }
        } else {
            finalList.add(polygon);
        }
    }

    /*
     * A method to find the lowest ratio between two dimensions in an array of polygons
     */
    private static Double findLowestRatioOfPolygons(ArrayList<Polygon2D.Double> polygons) {
        Double ratio = Double.MAX_VALUE;
        Double temp_ratio = null;
        for (Polygon2D.Double p : polygons) {
            Double max = Math.max(p.getBounds2D().getHeight(), p.getBounds2D().getWidth());
            Double min = Math.min(p.getBounds2D().getHeight(), p.getBounds2D().getWidth());
            temp_ratio = min / max;
            if (temp_ratio < ratio)
                ratio = temp_ratio;
        }
        return ratio;
    }

    // Finds the turning closest to the middle of a polygon (spatial query on db)
    private static Point2D.Double closestTP(Connection con, Polygon2D.Double polygon, ArrayList<Point2D.Double> tpList) throws SQLException {
        Point2D.Double closestTP = new Point2D.Double();
        Double shortestDist = null;
        Point2D.Double polCenter = DB_WrapperSpatial.ST_Centroid_Polygon(con, polygon);
        for (Point2D.Double p : tpList) {
            Double distance = polCenter.distance(p);
            if (shortestDist == null || distance < shortestDist) {
                shortestDist = distance;
                closestTP = p;
            }
        }
        return closestTP;
    }

    // Find the turning points of a polygon by going through all its points
    private static ArrayList<Point2D.Double> findTPs(Polygon2D.Double polygon) {
        ArrayList<Point2D.Double> interList = new ArrayList<Point2D.Double>();
        ArrayList<Point2D.Double> tpList = new ArrayList<Point2D.Double>();

        for (int i = 0; i < polygon.getVertexCount(); i++) {
            if (interList.isEmpty() || interList.size() == 1) {
                interList.add(new Point2D.Double(polygon.getX(i), polygon.getY(i)));
            } else {
                interList.add(new Point2D.Double(polygon.getX(i), polygon.getY(i)));
                // Define turning point
                Point2D.Double tp = interList.get(1);
                // Find interior angle
                double angle = computeAngle(interList.get(0), tp, interList.get(2), clockwisePolygon(polygon));
                // If concave
                if (angle > t_angle) {
                    tpList.add(tp);
                }
                interList.remove(0);
            }
        }
        return tpList;
    }

    // Splits polygon perpendicular to its longer dimension
    private static ArrayList<Polygon2D.Double> decomposePolyDimension(Connection con, Polygon2D.Double polygon, Point2D.Double point) throws SQLException {
        ArrayList<Polygon2D.Double> polygonList = new ArrayList<Polygon2D.Double>();
        Double height = polygon.getBounds2D().getHeight();
        Double width = polygon.getBounds2D().getWidth();
        Line2D.Double line = new Line2D.Double();
        // Find longer dimension
        if (height >= width) {
            line.setLine(point.getX() - width, point.getY(), point.getX() + width, point.getY());
            polygonList = DB_WrapperSpatial.ST_Split_Polygon(con, polygon, line);
        } else {
            line.setLine(point.getX(), point.getY() - height, point.getX(), point.getY() + height);
            polygonList = DB_WrapperSpatial.ST_Split_Polygon(con, polygon, line);
        }
        return polygonList;
    }


    public static void main(String args[]) throws SQLException {
        Connection con = DB_Connection.connectToDatabase("conf/moovework.properties");
        Integer fileID = 1;
        DB_WrapperLoad.loadALL(con, fileID);
        DB_Import.decompose(con);
    }
}
