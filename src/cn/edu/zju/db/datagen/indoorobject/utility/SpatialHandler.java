package cn.edu.zju.db.datagen.indoorobject.utility;

import cn.edu.zju.db.datagen.database.spatialobject.Floor;
import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import diva.util.java2d.Polygon2D;
import org.khelekore.prtree.PRTree;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex9 on 2016/7/19.
 */
public class SpatialHandler {

    public static Polygon2D.Double convertPoints2Polygon(List<Point2D.Double> points) {
        Polygon2D.Double polygon = new Polygon2D.Double();
        boolean isFirst = true;
        for (Point2D.Double point : points) {
            if (isFirst) {
                polygon.moveTo(point.getX(), point.getY());
                isFirst = false;
            } else {
                polygon.lineTo(point.getX(), point.getY());
            }
        }
        polygon.closePath();
        return polygon;
    }

    public static boolean isPolygonSelfIntersection(Polygon2D.Double polygon2D) {
        List<Line2D.Double> linesForPolygon = findLinesForPolygon(polygon2D);
        for (int i = 0; i < linesForPolygon.size(); i++) {
            Line2D.Double line1 = linesForPolygon.get(i);
            for (int j = i + 1; j < linesForPolygon.size(); j++) {
                Line2D.Double line2 = linesForPolygon.get(j);
                boolean midIntersection = isMidIntersection(line1, line2);
                if (midIntersection) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<Line2D.Double> findLinesForPolygon(Polygon2D.Double polygon) {
        List<Line2D.Double> lines = new ArrayList<Line2D.Double>();
        int verCount = polygon.getVertexCount();
        for (int i = 0; i < polygon.getVertexCount(); i++) {
            Line2D.Double line = new Line2D.Double(polygon.getX(i), polygon.getY(i), polygon.getX((i + 1) % verCount),
                                                          polygon.getY((i + 1) % verCount));
            lines.add(line);
        }
        return lines;
    }

    public static boolean isMidIntersection(Line2D.Double line1, Line2D.Double line2) {
        Point2D.Double interPoint = new Point2D.Double();
        if (getLineIntersection(line1, line2, interPoint) == 1) {
            if (!isPointEqualsLineEdge(line1, interPoint) && !isPointEqualsLineEdge(line2, interPoint)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPointEqualsLineEdge(Line2D.Double line, Point2D.Double point) {
        if ((line.getX1() == point.getX() && line.getY1() == point.getY()) || (line.getX2() == point.getX() && line.getY2() == point.getY())) {
            return true;
        }
        return false;
    }

    /**
     * detect if two lines intersect, and return the result 1--intersect, 0--no
     * intersect, interPoint--intersect point
     */
    public static int getLineIntersection(Line2D.Double line1, Line2D.Double line2, Point2D.Double interPoint) {
        double p0_x = line1.getX1(), p0_y = line1.getY1(), p1_x = line1.getX2(), p1_y = line1.getY2();
        double p2_x = line2.getX1(), p2_y = line2.getY1(), p3_x = line2.getX2(), p3_y = line2.getY2();
        double s1_x, s1_y, s2_x, s2_y;
        s1_x = p1_x - p0_x;
        s1_y = p1_y - p0_y;
        s2_x = p3_x - p2_x;
        s2_y = p3_y - p2_y;

        double s, t;
        s = (-s1_y * (p0_x - p2_x) + s1_x * (p0_y - p2_y)) / (-s2_x * s1_y + s1_x * s2_y);
        t = (s2_x * (p0_y - p2_y) - s2_y * (p0_x - p2_x)) / (-s2_x * s1_y + s1_x * s2_y);

        if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
            // Collision detected
            interPoint.setLocation(p0_x + (t * s1_x), p0_y + (t * s1_y));
            return 1;
        }

        return 0; // No collision

    }

    // calculate distance using Heron's formula and the law of cosines
    public static double calDistancePointSegment(Point2D.Double point, Line2D.Double line) {
        double a, b, c;
        Point2D.Double pA = new Point2D.Double(line.getX1(), line.getY1());
        Point2D.Double pB = new Point2D.Double(line.getX2(), line.getY2());
        a = point.distance(pA);
        if (a <= 0.01) {
            return 0;
        }
        b = point.distance(pB);
        if (b <= 0.01) {
            return 0;
        }
        c = pA.distance(pB);
        if (c <= 0.01) {
            return a;
        }
        if (a * a >= b * b + c * c) {
            return b;
        }
        if (b * b >= a * a + c * c) {
            return a;
        }
        double l = (a + b + c) / 2;
        double s = Math.sqrt((l * (l - a) * (l - b) * (l - c)));
        return 2 * s / c;
    }

    public static List<Line2D.Double> polygonIntersectionLines(Polygon2D.Double polygon1, Polygon2D.Double polygon2) {
        List<Line2D.Double> resultLines = new ArrayList<>();
        List<Line2D.Double> lines1 = SpatialHandler.findLinesForPolygon(polygon1);
        List<Line2D.Double> lines2 = SpatialHandler.findLinesForPolygon(polygon2);
        for (Line2D.Double line1 : lines1) {
            for (Line2D.Double line2 : lines2) {
                int relation = SpatialHandler.findLinesRelation(line1, line2);
                if (relation == 1) {
                    resultLines.add(SpatialHandler.calInterLine(line1, line2));
                }
            }
        }
        return resultLines;
    }

    /*
 * return: 1--reclosing(virtual door exists); 2--perpendicular(certain
 * angle, lines close); 3--far away(close to parallel, lines far);
 * -1--unknown if two lines are far, no door; if lines are close and angle
 * close and shadow large, virtual door exists; else no door
 */
    public static int findLinesRelation(Line2D.Double line1, Line2D.Double line2) {
        if (calDistanceLines(line1, line2) > Constants.conLineDistanceThres) {
            return 3;
        }
        if (calAngleLines(line1, line2) > Constants.conAngleThres) {
            return 2;
        }
        if (calInterLengthLines(line1, line2) > Constants.conIntersectLengthThres) {
            return 1;
        }
        return -1;
    }

    /*
 * calculate distance between two lines. if two lines intersect, distance is
 * 0 else get each end point distance to the other line and get the minimum
 * one
 */
    public static double calDistanceLines(Line2D.Double line1, Line2D.Double line2) {
        Point2D.Double intersectPoint = new Point2D.Double();
        if (getLineIntersection(line1, line2, intersectPoint) == 1) {
            return 0;
        }
        double dist1 = calDistancePointSegment(new Point2D.Double(line1.getX1(), line1.getY1()), line2);
        double dist2 = calDistancePointSegment(new Point2D.Double(line1.getX2(), line1.getY2()), line2);
        double dist3 = calDistancePointSegment(new Point2D.Double(line2.getX1(), line2.getY1()), line1);
        double dist4 = calDistancePointSegment(new Point2D.Double(line2.getX2(), line2.getY2()), line1);
        double min1 = Math.min(dist1, dist2);
        double min2 = Math.min(dist3, dist4);
        return Math.min(min1, min2);
    }


    /**
     * define angle between two lines Math.atan2 return a double value, which is
     * bigger than -PI, smaller than +PI define angle between line and x axis,
     * between 0, PI (remember <= is necessary)
     */
    public static double calAngleLines(Line2D.Double line1, Line2D.Double line2) {

        double angle1 = Math.atan2(line1.getY1() - line1.getY2(), line1.getX1() - line1.getX2());
        if (angle1 <= 0) {
            angle1 = angle1 + Math.PI;
        }
        double angle2 = Math.atan2(line2.getY1() - line2.getY2(), line2.getX1() - line2.getX2());
        if (angle2 <= 0) {
            angle2 = angle2 + Math.PI;
        }
        double delta = Math.abs(angle1 - angle2);
        delta = Math.min(delta, Math.abs(delta - Math.PI));
        return delta;

    }

    /**
     * get the length of overlap line of line1 and line2 if they do not have an
     * overlap line, return 0
     */
    private static double calInterLengthLines(Line2D.Double line1, Line2D.Double line2) {
        Line2D.Double interLine = calInterLine(line1, line2);
        if (interLine == null) {
            return 0;
        }
        return calLineLength(interLine);
    }

    /*
 * get the overlap line of line1 and line2 inter line 1 and 2 are two
 * different occasions of projection return the smaller location one
 */
    public static Line2D.Double calInterLine(Line2D.Double line1, Line2D.Double line2) {
        Point2D.Double point1 = new Point2D.Double(line1.getX1(), line1.getY1());
        Point2D.Double point2 = new Point2D.Double(line1.getX2(), line1.getY2());
        Point2D.Double point3 = new Point2D.Double(line2.getX1(), line2.getY1());
        Point2D.Double point4 = new Point2D.Double(line2.getX2(), line2.getY2());
        Line2D.Double interLine1 = minInterLine(point1, point2, line2);
        Line2D.Double interLine2 = minInterLine(point3, point4, line1);
        if (interLine1 != null && interLine2 != null) {
            return pickLine(interLine1, interLine2);
        } else if (interLine1 != null) {
            return interLine1;
        } else if (interLine2 != null) {
            return interLine2;
        }
        return null;
    }

    // probably wrong!!!
    // if projection point1 and projection point2 are both on line, then return
    // Line(projpoint1, projpoint2)
    // if one of them is on the line, choose the right direction and return
    // subline
    // else return null
    private static Line2D.Double minInterLine(Point2D.Double point1, Point2D.Double point2, Line2D.Double line) {
        Point2D.Double projPoint1 = calProjectionPoint(point1, line);
        Point2D.Double projPoint2 = calProjectionPoint(point2, line);
        boolean p1Flag = isProjPointOnSegment(projPoint1, line);
        boolean p2Flag = isProjPointOnSegment(projPoint2, line);

        if (p1Flag == true && p2Flag == true) {
            return new Line2D.Double(projPoint1.getX(), projPoint1.getY(), projPoint2.getX(), projPoint2.getY());
        }
        if (p1Flag == true && p2Flag == false) {
            if ((projPoint1.getX() == line.getX1() && projPoint1.getY() == line.getY1())
                        || (projPoint1.getX() == line.getX2() && projPoint1.getY() == line.getY2())) {
                return null;
            }
            Point2D.Double picked = pickPoint(projPoint1, projPoint2, line);
            return new Line2D.Double(projPoint1.getX(), projPoint1.getY(), picked.getX(), picked.getY());
        }
        if (p1Flag == false && p2Flag == true) {
            Point2D.Double picked = pickPoint(projPoint2, projPoint1, line);
            if ((projPoint2.getX() == line.getX1() && projPoint2.getY() == line.getY1())
                        || (projPoint2.getX() == line.getX2() && projPoint2.getY() == line.getY2())) {
                return null;
            }
            return new Line2D.Double(projPoint2.getX(), projPoint2.getY(), picked.getX(), picked.getY());
        }
        return null;
    }

    /*
 * calculate one point's projection point of a line.
 */
    private static Point2D.Double calProjectionPoint(Point2D.Double point, Line2D.Double line) {
        double x1 = line.getX1();
        double y1 = line.getY1();
        double x2 = line.getX2();
        double y2 = line.getY2();
        double x3 = point.getX();
        double y3 = point.getY();
        double k = ((y2 - y1) * (x3 - x1) - (x2 - x1) * (y3 - y1)) / (Math.pow((y2 - y1), 2) + Math.pow((x2 - x1), 2));
        double x4 = x3 - k * (y2 - y1);
        double y4 = y3 + k * (x2 - x1);
        return new Point2D.Double(x4, y4);
    }

    private static boolean isProjPointOnSegment(Point2D.Double point, Line2D.Double line) {
        double xMin = Math.min(line.getX1(), line.getX2());
        double xMax = Math.max(line.getX1(), line.getX2());
        double yMin = Math.min(line.getY1(), line.getY2());
        double yMax = Math.max(line.getY1(), line.getY2());
        if ((point.getX() <= xMax + Constants.containsProjPointThres
                     && point.getX() >= xMin - Constants.containsProjPointThres)
                    && (point.getY() <= yMax + Constants.containsProjPointThres
                                && point.getY() >= yMin - Constants.containsProjPointThres)) {
            return true;
        } else {
            return false;
        }
    }

    // maybe refractor to make it face to the right direction first
    // choose a point which is on the same direction of line(point1, point2)
    private static Point2D.Double pickPoint(Point2D.Double point1, Point2D.Double point2, Line2D.Double line) {
        if (point1.getX() > point2.getX()) {
            if (line.getX1() > line.getX2()) {
                return new Point2D.Double(line.getX2(), line.getY2());
            } else {
                return new Point2D.Double(line.getX1(), line.getY1());
            }
        } else if (point1.getX() < point2.getX()) {
            if (line.getX1() > line.getX2()) {
                return new Point2D.Double(line.getX1(), line.getY1());
            } else {
                return new Point2D.Double(line.getX2(), line.getY2());
            }
        } else {
            if (point1.getY() > point2.getY()) {
                if (line.getY1() > line.getY2()) {
                    return new Point2D.Double(line.getX2(), line.getY2());
                } else {
                    return new Point2D.Double(line.getX1(), line.getY1());
                }
            } else {
                if (line.getY1() > line.getY2()) {
                    return new Point2D.Double(line.getX1(), line.getY1());
                } else {
                    return new Point2D.Double(line.getX2(), line.getY2());
                }
            }
        }

    }


    public static double calLineLength(Line2D.Double line) {
        return (line.getX1() - line.getX2()) * (line.getX1() - line.getX2())
                       + (line.getY1() - line.getY2()) * (line.getY1() - line.getY2());
    }

    /*
     * pick a line according to its location return the smaller x one; or return
     * the smaller y one; or minimum x, minimum y are both the same. return the
     * smaller bigger location one
     */
    public static Line2D.Double pickLine(Line2D.Double line1, Line2D.Double line2) {
        double min1X = Math.min(line1.getX1(), line1.getX2());
        double min1Y = Math.min(line1.getY1(), line1.getY2());
        double min2X = Math.min(line2.getX1(), line2.getX2());
        double min2Y = Math.min(line2.getY1(), line2.getY2());
        double max1X = Math.max(line1.getX1(), line1.getX2());
        double max1Y = Math.max(line1.getY1(), line1.getY2());
        double max2X = Math.max(line2.getX1(), line2.getX2());
        double max2Y = Math.max(line2.getY1(), line2.getY2());
        return min1X < min2X ? line1
                       : (min1X > min2X ? line2
                                  : (min1Y < min2Y ? line1
                                             : (min1Y > min2Y ? line2
                                                        : (max1X < max2X ? line1
                                                                   : (max1X > max2X ? line2
                                                                              : (max1Y < max2Y ? line1
                                                                                         : (max1Y > max2Y ? line2 : line1)))))));
    }


    public static Partition findPartitionForPoint(Floor floor, Point2D.Double point) {
        PRTree<Partition> rtree = floor.getPartitionsRTree();
        for (Partition part : rtree.find(point.getX(), point.getY(), point.getX(), point.getY())) {
            if (part.getPolygon2D().contains(point)) {
                return part;
            }
        }
        return null;
    }

    public static Point2D.Double getRandomPointInMBR(Polygon2D.Double polygon2D) {
        double minX = polygon2D.getBounds2D().getMinX();
        double minY = polygon2D.getBounds2D().getMinY();
        double maxX = polygon2D.getBounds2D().getMaxX();
        double maxY = polygon2D.getBounds2D().getMaxY();
        double randX = Math.random() * (maxX - minX) + minX;
        double randY = Math.random() * (maxY - minY) + minY;
        return new Point2D.Double(randX, randY);
    }

    public static Point2D.Double getAvgPointOnPolygon(Polygon2D.Double polygon2D) {
        int numCount = polygon2D.getVertexCount();
        double xSum = 0, ySum = 0;
        for (int i = 0; i < numCount; i++) {
            xSum += polygon2D.getX(i);
            ySum += polygon2D.getY(i);
        }
        return new Point2D.Double(xSum / numCount, ySum / numCount);
    }

    public static Point2D.Double getPointNearCenter(Point2D.Double centerPoint, double radius) {
        double newX = 0;
        double newY = 0;
        double angle = Math.random() * Math.PI;
        double range = Math.random() * radius; // range of the circle
        newX = centerPoint.getX() + Math.cos(angle) * range;
        newY = centerPoint.getY() + Math.sin(angle) * range;
        return new Point2D.Double(newX, newY);
    }

    public static List<Point2D.Double> getRandomPointsNearCenter(Polygon2D.Double polygon2D, int pointNum, double radius) {
        List<Point2D.Double> randomPoints = new ArrayList<>();
        Point2D.Double center = getAvgPointOnPolygon(polygon2D);
        for (int i = 0; i < pointNum; i++) {
            Point2D.Double randomPoint = Math.random() > 0.2 ? getPointNearCenter(center, radius) : getRandomPointInMBR(polygon2D);
            randomPoints.add(randomPoint);
        }
        return randomPoints;
    }

    public static Point2D.Double calPolygonCenter(Polygon2D.Double polygon2D) {
        double sumX = 0;
        double sumY = 0;
        for (int i = 0; i < polygon2D.getVertexCount(); i++) {
            sumX += polygon2D.getX(i);
            sumY += polygon2D.getY(i);
        }
        return new Point2D.Double(sumX / polygon2D.getVertexCount(), sumY / polygon2D.getVertexCount());
    }

    public static double calDistancePointPolygon(Point2D.Double point, Polygon2D.Double polygon) {
        double result = 10000;
        for (int i = 0; i < polygon.getVertexCount(); i++) {
            int preIndex = i - 1;
            if (preIndex < 0) {
                preIndex = polygon.getVertexCount() - 1;
            }

            Line2D.Double line = new Line2D.Double(polygon.getX(i), polygon.getY(i), polygon.getX(preIndex), polygon.getY(preIndex));
            double distPointSegment = calDistancePointSegment(point, line);
            if (distPointSegment < result) {
                result = distPointSegment;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        Line2D.Double line1 = new Line2D.Double(0, 0, 10, 10);
        Line2D.Double line2 = new Line2D.Double(10, 10, 13, 15);
        Point2D.Double interPoint = new Point2D.Double();
        int lineIntersection = getLineIntersection(line1, line2, interPoint);
        System.out.println(interPoint);
        System.out.println(lineIntersection);

        List<Point2D.Double> points = new ArrayList<Point2D.Double>();
        for (int i = 0; i < 6; i++) {
            Point2D.Double p = new Point2D.Double(i * 10, i * i);
            points.add(p);
        }
//        points.add(new Point2D.Double(1, 0));
        Polygon2D.Double polygon2D = SpatialHandler.convertPoints2Polygon(points);
        System.out.println(polygon2D);
        System.out.println(isPolygonSelfIntersection(polygon2D));
    }


}
