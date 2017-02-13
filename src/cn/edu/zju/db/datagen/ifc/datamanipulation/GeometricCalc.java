package cn.edu.zju.db.datagen.ifc.datamanipulation;

import cn.edu.zju.db.datagen.database.DB_WrapperSpatial;
import diva.util.java2d.Polygon2D;
import org.postgis.LineString;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/*
 * This class contains methods for geometric calculations.
 */
public class GeometricCalc {

    /*
     * Find the length on a line (Pythagora's)
     */
    public static Double getLineLength(Line2D.Double line) {
        Double length = Math.sqrt(Math.pow(Math.abs(line.getX2() - line.getX1()), 2) + Math.pow(Math.abs(line.getY2() - line.getY1()), 2));
        return length;
    }

    /*
     * Find the angle between to lines - always choose the smallest angle a, where 0 <= a <= 90
     */
    public static double getAngleBetweenLines(Line2D line1, Line2D line2) {
        double angle1 = Math.atan2(line1.getY1() - line1.getY2(), line1.getX1() - line1.getX2());
        double angle2 = Math.atan2(line2.getY1() - line2.getY2(), line2.getX1() - line2.getX2());
        double angleD = Math.toDegrees(angle1 - angle2);
        if (angleD > 90) {
            angleD -= 180.0;
        } else if (angleD < -90) {
            angleD += 180.0;
        }
        return Math.abs(angleD);
    }

    /*
     * This method creates a line with a specified length and with the same
     * slope as the line taken as input. It is used to create doors with a
     * static length through the back-end UI
     */
    public static Line2D.Double createSameLineDifLength(Line2D.Double line, Point2D.Double center, Double range) {
        Point2D.Double p1 = (java.awt.geom.Point2D.Double) line.getP1();
        Point2D.Double p2 = (java.awt.geom.Point2D.Double) line.getP2();
        Double Slope;
        Line2D.Double lineResult;

        Double x1, y1, x2, y2;
        if ((p2.getY() - p1.getY()) == 0) {
            // line is horizontal, slope 0
            y1 = center.getY();
            y2 = center.getY();
            x1 = center.getX() - range;
            x2 = center.getX() + range;
        } else if ((p2.getX() - p1.getX()) == 0) {
            // line is vertical, slope infinity
            y1 = center.getY() - range;
            y2 = center.getY() + range;
            x1 = center.getX();
            x2 = center.getX();
        } else {
            Slope = (p2.getX() - p1.getX()) / (p2.getY() - p1.getY());
            double y = center.getY();
            double x = center.getX();
            double m = Slope;

            Double cosine = 1 / Math.sqrt(1 + Math.pow(m, 2));
            Double sine = m / Math.sqrt(1 + Math.pow(m, 2));

            Double xRelat = range * cosine;
            Double yRelat = range * sine;

            x1 = x + xRelat;
            y1 = y + yRelat;

            x2 = x - xRelat;
            y2 = y - yRelat;
        }
        lineResult = new Line2D.Double(x1, y1, x2, y2);
        return lineResult;
    }

    /*
     * This method creates a line with a specified length and a slope orthogonal
     * to the line taken as input. It is i.a. used in the mapping of partitions
     * to access points.
     */
    public static Line2D.Double createPerpendicularLine(Line2D.Double line, Point2D.Double intersectionPoint, Double range) {
        Point2D.Double p1 = (java.awt.geom.Point2D.Double) line.getP1();
        Point2D.Double p2 = (java.awt.geom.Point2D.Double) line.getP2();
        Double bisectorSlope;
        Line2D.Double bisector;

        if ((p2.getY() - p1.getY()) == 0) {
            // line is horizontal, slope 0
            // perpendicular bisector is slope infinity
            bisectorSlope = Double.NaN;
        } else if ((p2.getX() - p1.getX()) == 0) {
            // line is vertical, slope infinity
            // perpendicular bisector is slope 0
            bisectorSlope = 0.0;
        } else {
            bisectorSlope = -1 * (p2.getX() - p1.getX()) / (p2.getY() - p1.getY());
        }

        Double x1, y1, x2, y2;
        if (Double.isNaN(bisectorSlope)) {
            // vertical
            y1 = intersectionPoint.getY() - range;
            y2 = intersectionPoint.getY() + range;
            x1 = intersectionPoint.getX();
            x2 = intersectionPoint.getX();
        } else if (bisectorSlope == 0) {
            // horizontal
            y1 = intersectionPoint.getY();
            y2 = intersectionPoint.getY();
            x1 = intersectionPoint.getX() - range;
            x2 = intersectionPoint.getX() + range;
        } else {
            double y = intersectionPoint.getY();
            double x = intersectionPoint.getX();
            double m = bisectorSlope;

            Double cosine = 1 / Math.sqrt(1 + Math.pow(m, 2));
            Double sine = m / Math.sqrt(1 + Math.pow(m, 2));

            Double xRelat = range * cosine;
            Double yRelat = range * sine;

            x1 = x + xRelat;
            y1 = y + yRelat;

            x2 = x - xRelat;
            y2 = y - yRelat;
        }
        bisector = new Line2D.Double(x1, y1, x2, y2);
        return bisector;
    }

    /*
     * This method creates a bounding rectangle to a line. It is used in the back-end UI,
     * to create a click-box for access points such that the become easier to select.
     */
    public static Polygon2D.Double createBoundingRectFromLine(LineString lineGIS) {
        Polygon2D.Double result = new Polygon2D.Double();

        Line2D.Double line2D = DB_WrapperSpatial.convertLinestringGIStoLine2D(lineGIS);

        Line2D.Double pLine1 = createPerpendicularLine(line2D, (Point2D.Double) line2D.getP1(), 100.0);
        Line2D.Double pLine2 = createPerpendicularLine(line2D, (Point2D.Double) line2D.getP2(), 100.0);

        result.moveTo(pLine1.getX1(), pLine1.getY1());
        result.lineTo(pLine2.getX1(), pLine2.getY1());
        result.lineTo(pLine2.getX2(), pLine2.getY2());
        result.lineTo(pLine1.getX2(), pLine1.getY2());
        result.closePath();

        return result;
    }
}
