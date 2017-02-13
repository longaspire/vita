package cn.edu.zju.db.datagen.ifc.datamanipulation;

import cn.edu.zju.db.datagen.database.DB_WrapperSpatial;
import cn.edu.zju.db.datagen.database.spatialobject.AccessPoint;
import cn.edu.zju.db.datagen.database.spatialobject.Partition;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Mapping {

    /*
     * This method is for mapping partitions to an access point.
     */
    public static ArrayList<Partition> MapD2P(Connection con, AccessPoint ap) throws SQLException {

        ArrayList<Partition> connParts = new ArrayList<Partition>();

        //Create perpendicular bisector for access point line
        Line2D.Double line = DB_WrapperSpatial.convertLinestringGIStoLine2D(ap.getLineGIS());
        Point2D.Double midPoint = DB_WrapperSpatial.convertPointGIStoPoint2D(ap.getLocationGIS());
        Line2D.Double bisector = GeometricCalc.createPerpendicularLine(line, midPoint, 500.0);

        //Filter by utilizing intersects query with spatial index
        connParts = DB_WrapperSpatial.ST_Intersects_Line_Partition_SpatialIndex(con, bisector, ap.getFloor());
        Boolean intersects = null;

        //If 2+ partition are connected choose the 2 with the shortest distance to door
        if (connParts.size() > 2) {
            Map<Double, Partition> closestParts = new TreeMap<Double, Partition>();
            Double minDist = null;
            for (Partition part : connParts) {
                ArrayList<Line2D.Double> partEdges = Decomposition.polygonEdges(part.getPolygonGIS());
                for (Line2D.Double edge : partEdges) {
                    intersects = DB_WrapperSpatial.ST_Intersects_Line_Line(con, bisector, edge);
                    if (intersects) {
                        Double temp_dist = DB_WrapperSpatial.ST_Distance_Point_Line(con, midPoint, edge);
                        if (minDist == null || temp_dist < minDist) {
                            minDist = temp_dist;
                            closestParts.put(minDist, part);
                        } else if (temp_dist.equals(minDist)) {
                            minDist = temp_dist - 0.00000001;
                            closestParts.put(minDist, part);
                        } else {
                            closestParts.put(temp_dist, part);
                        }
                    }
                }
            }
            connParts.clear();

            int counter = 0;
            for (Entry<Double, Partition> entry : closestParts.entrySet()) {
                if (counter < 2) {
                    connParts.add(entry.getValue());
                    counter++;
                } else {
                    break;
                }
            }
        }
        return connParts;
    }
}
