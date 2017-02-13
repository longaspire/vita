package cn.edu.zju.db.datagen.ifc.dataextraction;

import cn.edu.zju.db.datagen.database.DB_WrapperSpatial;
import cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Partition;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.*;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class ExtractPartitions extends IfcFileParser {

    public ExtractPartitions() throws Exception {
        super();
        // TODO Auto-generated constructor stub
    }

    // Retrieves all IfcSpace objects from an IFC file
    @SuppressWarnings("unchecked")
    private static Collection<IfcSpace> GetAllSpaces() throws Exception {
        Collection<IfcSpace> spaces = null;
        spaces = (Collection<IfcSpace>) ifcModel.getCollection(IfcSpace.class);
        return spaces;
    }

    // Retrieves the placement coordinate for a given IfcSpace object
    private static Point2D.Double GetCoordinate(IfcSpace space) {
        Point2D.Double p = new Point2D.Double();
        Point2D.Double pOuter = new Point2D.Double();
        Point2D.Double pInner = new Point2D.Double();
        List<Double> direction = GetDirecction(space, false);
        IfcCartesianPoint car = null;
        IfcCartesianPoint innerPos = null;

        // Go through the IFC hierarchy to reach coordinates
        IfcLocalPlacement local = (IfcLocalPlacement) space.getObjectPlacement();
        IfcAxis2Placement3D relat = (IfcAxis2Placement3D) local.getRelativePlacement();
        IfcCartesianPoint co = relat.getLocation();
        LIST<IfcLengthMeasure> tempList = co.getCoordinates();
        pOuter.setLocation(tempList.get(0).value, tempList.get(1).value);
        boolean rectProf = false;
        IfcRectangleProfileDef sweptArea = null;

        // Special cases
        IfcProductRepresentation repres1 = (IfcProductRepresentation) space.getRepresentation();
        LIST<IfcRepresentation> repres2 = repres1.getRepresentations();
        for (int i = 0; i < repres2.size(); i++) {
            IfcShapeRepresentation rep = (IfcShapeRepresentation) repres2.get(i);
            SET<IfcRepresentationItem> items = rep.getItems();
            Iterator<IfcRepresentationItem> iter = items.iterator();
            while (iter.hasNext()) {
                IfcRepresentationItem repItem = iter.next();
                // If the IfcSpace is of the type IfcExtrudeAreaSolid, the inner coordinate is saved
                if (repItem instanceof IfcExtrudedAreaSolid) {
                    IfcExtrudedAreaSolid extrudeArea = (IfcExtrudedAreaSolid) repItem;
                    IfcAxis2Placement3D pos = extrudeArea.getPosition();
                    IfcCartesianPoint cart = pos.getLocation();
                    tempList = cart.getCoordinates();
                    pInner.setLocation(tempList.get(0).value, tempList.get(1).value);
                    // If the IfcSpace representation is a IfcRectangleProfileDef
                    if (extrudeArea.getSweptArea() instanceof IfcRectangleProfileDef) {
                        sweptArea = (IfcRectangleProfileDef) extrudeArea.getSweptArea();
                        rectProf = true;
                        car = new IfcCartesianPoint();
                        car.addCoordinates(new IfcLengthMeasure(-sweptArea.getXDim().value / 2));
                        car.addCoordinates(new IfcLengthMeasure(-sweptArea.getYDim().value / 2));
                        innerPos = sweptArea.getPosition().getLocation();
                    }
                }
            }
        }

        // Use the outer coordinate as the space coordinate as default
        p = pOuter;
        // If the outer coord equals (0,0) the inner coord is used
        if (p.getX() == 0.0 && p.getY() == 0.0) {
            p = pInner;
            // If the IfcSpace is of the type IfcRectangleProfileDef the coord is converted
            if (rectProf) {
                p = convertCoords(innerPos, direction, pInner);
                p = convertCoords(car, direction, p);
            }
        }
        return p;
    }

    // Retrieves the direction coordinates for a given IfcSpace object
    private static List<Double> GetDirecction(IfcSpace space, boolean isPolyline) {
        IfcDirection direction = null;
        List<DOUBLE> tempList = new ArrayList<DOUBLE>();
        List<Double> list = new ArrayList<Double>();
        IfcAxis2Placement3D pos = null;
        IfcDirection refDirect = null;
        IfcAxis2Placement3D pos2 = null;
        IfcDirection refDirect2 = null;
        IfcAxis2Placement2D posInner = null;
        IfcDirection refDirectInner = null;

        // Find outer placement coordinate
        IfcLocalPlacement local = (IfcLocalPlacement) space.getObjectPlacement();
        IfcAxis2Placement3D relat = (IfcAxis2Placement3D) local.getRelativePlacement();
        IfcCartesianPoint co = relat.getLocation();
        LIST<IfcLengthMeasure> newtempList = co.getCoordinates();
        Point2D.Double pOuter = new Point2D.Double();
        pOuter.setLocation(newtempList.get(0).value, newtempList.get(1).value);

        IfcProductRepresentation repres1 = (IfcProductRepresentation) space.getRepresentation();
        LIST<IfcRepresentation> repres2 = repres1.getRepresentations();
        for (int i = 0; i < repres2.size(); i++) {
            IfcShapeRepresentation rep = (IfcShapeRepresentation) repres2.get(i);
            SET<IfcRepresentationItem> items = rep.getItems();
            Iterator<IfcRepresentationItem> iter = items.iterator();
            while (iter.hasNext()) {
                IfcRepresentationItem repItem = iter.next();
                // ROOM TYPE 1,2 & 3
                if (repItem instanceof IfcExtrudedAreaSolid) {
                    IfcExtrudedAreaSolid extrudeArea = (IfcExtrudedAreaSolid) repItem;
                    pos = extrudeArea.getPosition();
                    refDirect = pos.getRefDirection();

                    if (refDirect != null) {
                        if (refDirect.getDirectionRatios().get(0).value == 1.0) {
                            refDirect = null;
                        }
                    }
                    // ROOM TYPE 3
                    if (extrudeArea.getSweptArea() instanceof IfcRectangleProfileDef) {
                        IfcRectangleProfileDef swep = (IfcRectangleProfileDef) extrudeArea.getSweptArea();
                        if (swep.getPosition() != null && pOuter.getX() != 0.0 && pOuter.getY() != 0.0) {
                            posInner = swep.getPosition();
                            refDirectInner = posInner.getRefDirection();
                        }
                    }
                }
                // ROOM TYPE 4
                if (repItem instanceof IfcBooleanClippingResult) {
                    IfcBooleanClippingResult boolclip = (IfcBooleanClippingResult) repItem;
                    if (boolclip.getFirstOperand() instanceof IfcExtrudedAreaSolid) {
                        IfcExtrudedAreaSolid firstop = (IfcExtrudedAreaSolid) boolclip.getFirstOperand();
                        pos2 = firstop.getPosition();
                        refDirect2 = pos2.getRefDirection();
                    }
                }
            }
        }
        // ROOM TYPE 3
        if (posInner != null) {
            tempList = refDirectInner.getDirectionRatios();
            // ROOM TYPE 1 & 2
        } else if (refDirect != null) {
            tempList = refDirect.getDirectionRatios();
        } else if (refDirect2 != null) {
            tempList = refDirect2.getDirectionRatios();
        }
        // ROOM TYPE 4
        else if (relat.getRefDirection() != null && isPolyline) {
            direction = relat.getRefDirection();
            tempList = direction.getDirectionRatios();
        }

        // Else set the direction manually to (1,0)
        else {
            list.add(1.0);
            list.add(0.0);
        }

        // Iterate through the found direction coordiantes and save them in a list of doubles
        for (int i = 0; i < tempList.size(); i++) {
            Double c = tempList.get(i).value;
            list.add(c);
        }
        return list;
    }

    // Retrieves the polyline coordinate sets for a given IfcSpace object
    private static ArrayList<Point2D.Double> GetPolyline(Connection con, IfcSpace space) {
        SET<IfcRepresentationItem> items = null;
        Iterator<IfcRepresentationItem> iter = null;
        ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
        Point2D.Double coord = new Point2D.Double();
        List<Double> spaceDirection = GetDirecction(space, true);
        Point2D.Double spaceCoords = GetCoordinate(space);

        // ROOM TYPE 1,2,3 & 4
        if (space.getRepresentation() instanceof IfcProductDefinitionShape) {
            IfcProductDefinitionShape repres1 = (IfcProductDefinitionShape) space.getRepresentation();
            LIST<IfcRepresentation> repres2 = repres1.getRepresentations();
            for (int i = 0; i < repres2.size(); i++) {
                IfcShapeRepresentation rep = (IfcShapeRepresentation) repres2.get(i);
                items = rep.getItems();
                iter = items.iterator();
                while (iter.hasNext()) {
                    IfcRepresentationItem repItem = iter.next();
                    // ROOM TYPE 1,2 & 3
                    if (repItem instanceof IfcExtrudedAreaSolid) {
                        IfcExtrudedAreaSolid extrudeArea = (IfcExtrudedAreaSolid) repItem;
                        // ROOM TYPE 1 & 2
                        if (extrudeArea.getSweptArea() instanceof IfcArbitraryClosedProfileDef) {
                            IfcArbitraryClosedProfileDef sweptArea = (IfcArbitraryClosedProfileDef) extrudeArea.getSweptArea();
                            // ROOM TYPE 1
                            if (sweptArea.getOuterCurve() instanceof IfcPolyline) {
                                partPolyline2(con, extrudeArea, spaceCoords, spaceDirection, points, spaceCoords);
                            }
                            // ROOM TYPE 2
                            if (sweptArea.getOuterCurve() instanceof IfcCompositeCurve) {
                                partCompositeCurve(con, extrudeArea, spaceCoords, spaceDirection, points, spaceCoords);
                            }
                        }
                        // ROOM TYPE 3
                        if (extrudeArea.getSweptArea() instanceof IfcRectangleProfileDef) {
                            partRectangle(con, extrudeArea, spaceCoords, spaceDirection, points, coord);
                        }
                    }
                    // ROOM TYPE 4
                    if (repItem instanceof IfcBooleanClippingResult) {
                        partBooleanClipping(con, repItem, spaceCoords, spaceDirection, points, spaceCoords);
                    }
                }
            }
            // ROOM TYPE 5,6,7,8,9 & 10
        } else if (space.getRepresentation() instanceof IfcProductRepresentation) {
            IfcProductRepresentation repres1 = (IfcProductRepresentation) space.getRepresentation();
            LIST<IfcRepresentation> repres2 = repres1.getRepresentations();
            for (int i = 0; i < repres2.size(); i++) {
                IfcShapeRepresentation rep = (IfcShapeRepresentation) repres2.get(i);
                items = rep.getItems();
                iter = items.iterator();
                while (iter.hasNext()) {
                    IfcRepresentationItem repItem = iter.next();
                    // ROOM TYPE 5,6 & 7
                    if (repItem instanceof IfcExtrudedAreaSolid) {
                        IfcExtrudedAreaSolid extrudeArea = (IfcExtrudedAreaSolid) repItem;
                        // ROOM TYPE 5 & 6
                        if (extrudeArea.getSweptArea() instanceof IfcArbitraryClosedProfileDef) {
                            IfcArbitraryClosedProfileDef sweptArea = (IfcArbitraryClosedProfileDef) extrudeArea.getSweptArea();
                            // ROOM TYPE 5
                            if (sweptArea.getOuterCurve() instanceof IfcPolyline) {
                                partPolyline2(con, extrudeArea, spaceCoords, spaceDirection, points, spaceCoords);
                            }
                            // ROOM TYPE 6
                            if (sweptArea.getOuterCurve() instanceof IfcCompositeCurve) {
                                partCompositeCurve(con, extrudeArea, spaceCoords, spaceDirection, points, spaceCoords);
                            }
                        }
                        // ROOM TYPE 7
                        if (extrudeArea.getSweptArea() instanceof IfcRectangleProfileDef) {
                            partRectangle(con, extrudeArea, spaceCoords, spaceDirection, points, coord);
                        }
                    }
                    // ROOM TYPE 8,9 & 10
                    if (repItem instanceof IfcBooleanClippingResult) {
                        IfcBooleanClippingResult boolClipping = (IfcBooleanClippingResult) repItem;
                        // ROOM TYPE 8
                        if (boolClipping.getFirstOperand() instanceof IfcExtrudedAreaSolid) {
                            IfcExtrudedAreaSolid extrudeArea = (IfcExtrudedAreaSolid) boolClipping.getFirstOperand();
                            partPolyline2(con, extrudeArea, spaceCoords, spaceDirection, points, spaceCoords);
                        }
                        // ROOM TYPE 9 & 10
                        if (boolClipping.getFirstOperand() instanceof IfcBooleanClippingResult) {
                            IfcBooleanClippingResult innerBoolClipping = (IfcBooleanClippingResult) boolClipping.getFirstOperand();
                            // ROOM 9
                            IfcExtrudedAreaSolid extrudeArea = (IfcExtrudedAreaSolid) innerBoolClipping.getFirstOperand();
                            IfcArbitraryClosedProfileDef sweptArea = (IfcArbitraryClosedProfileDef) extrudeArea.getSweptArea();
                            IfcAxis2Placement3D position = extrudeArea.getPosition();
                            IfcDirection refDirection = position.getRefDirection();
                            ArrayList<DOUBLE> tempList = refDirection.getDirectionRatios();
                            spaceDirection.clear();
                            // Iterate through the found direction coordiantes and save them in a list of doubles
                            for (int h = 0; h < tempList.size(); h++) {
                                Double c = tempList.get(h).value;
                                spaceDirection.add(c);
                            }

                            partPolyline(con, sweptArea, spaceCoords, spaceDirection, points, spaceCoords);
                        }
                    }
                }
            }

        }
        return points;
    }

    // ROOM TYPE 1,5 & 9 - Converts the relative polyline coords to real coords and remove intermediate points
    private static void partPolyline(Connection con, IfcArbitraryClosedProfileDef sweptArea, Point2D.Double spaceCoords, List<Double> spaceDirection, ArrayList<Point2D.Double> points,
                                     Point2D.Double coord) {
        IfcPolyline outerCurve1 = (IfcPolyline) sweptArea.getOuterCurve();
        LIST<IfcCartesianPoint> cartesian = outerCurve1.getPoints();
        // Conversion of coordinates
        for (int j = 0; j < cartesian.size(); j++) {
            IfcCartesianPoint c = cartesian.get(j);
            coord = convertCoords(c, spaceDirection, spaceCoords);
            points.add(coord);
        }
        // Removal of intermediate points
        checkIntermediate(con, points);
    }

    // ROOM TYPE 8 - Converts the relative polyline coords to real coords and remove intermediate points
    private static void partPolyline2(Connection con, IfcExtrudedAreaSolid extrudeArea, Point2D.Double spaceCoords, List<Double> spaceDirection, ArrayList<Point2D.Double> points, Point2D.Double coord) {
        IfcDirection exDirection = extrudeArea.getExtrudedDirection();
        Double zValue = exDirection.getDirectionRatios().get(2).value;

        IfcArbitraryClosedProfileDef sweptArea = (IfcArbitraryClosedProfileDef) extrudeArea.getSweptArea();
        IfcPolyline outerCurve1 = (IfcPolyline) sweptArea.getOuterCurve();
        LIST<IfcCartesianPoint> cartesian = outerCurve1.getPoints();
        // Conversion of coordinates
        for (int j = 0; j < cartesian.size(); j++) {
            IfcCartesianPoint c = cartesian.get(j);
            LIST<IfcLengthMeasure> coords = c.getCoordinates();
            IfcLengthMeasure newY = new IfcLengthMeasure(zValue * c.getCoordinates().get(1).value);
            coords.set(1, newY);
            IfcCartesianPoint cNew = new IfcCartesianPoint(coords);

            coord = convertCoords(cNew, spaceDirection, spaceCoords); // FOR Z(-1) FLIP DOG
            points.add(coord);
        }
        // Removal of intermediate points
        checkIntermediate(con, points);
        // points.remove(points.size() - 1);
    }

    // ROOM TYPE 2 & 6 - Converts the relative polyline coords to real coords and remove intermediate points
    private static void partCompositeCurve(Connection con, IfcExtrudedAreaSolid extrudeArea, Point2D.Double spaceCoords, List<Double> spaceDirection, ArrayList<Point2D.Double> points,
                                           Point2D.Double coord) {
        IfcDirection exDirection = extrudeArea.getExtrudedDirection();
        Double zValue = exDirection.getDirectionRatios().get(2).value;

        IfcArbitraryClosedProfileDef sweptArea = (IfcArbitraryClosedProfileDef) extrudeArea.getSweptArea();

        IfcCompositeCurve outerCurve2 = (IfcCompositeCurve) sweptArea.getOuterCurve();
        LIST<IfcCompositeCurveSegment> curveSegment = outerCurve2.getSegments();
        // Conversion of coordinates
        for (int j = 0; j < curveSegment.size(); j++) {
            IfcCompositeCurveSegment c = curveSegment.get(j);
            if (ExtractCompositeCurve(c) != null) {
                LIST<IfcCartesianPoint> car = ExtractCompositeCurve(c);
                if (!car.isEmpty()) {
                    LIST<IfcLengthMeasure> coords = car.get(0).getCoordinates();
                    IfcLengthMeasure newY = new IfcLengthMeasure(zValue * car.get(0).getCoordinates().get(1).value);
                    coords.set(1, newY);
                    IfcCartesianPoint cNew = new IfcCartesianPoint(coords);

                    coord = convertCoords(cNew, spaceDirection, spaceCoords);
                    points.add(coord);
                }
            }
        }
        if (!points.isEmpty())
            points.add(new Point2D.Double(points.get(0).getX(), points.get(0).getY()));
        // Removal of intermediate points
        checkIntermediate(con, points);
    }

    // ROOM TYPE 3 & 7 - Generates polyline coordinates from length and width of the room (Since rectangle always only 4 coords)
    private static void partRectangle(Connection con, IfcExtrudedAreaSolid extrudeArea, Point2D.Double spaceCoords, List<Double> spaceDirection, ArrayList<Point2D.Double> points, Point2D.Double coord) {
        IfcRectangleProfileDef sweptArea = null;
        sweptArea = (IfcRectangleProfileDef) extrudeArea.getSweptArea();
        Double xDim = sweptArea.getXDim().value;
        Double yDim = sweptArea.getYDim().value;

        // The four polyline coords are created
        LIST<IfcLengthMeasure> p1 = new LIST<IfcLengthMeasure>();
        p1.add(new IfcLengthMeasure(0.0));
        p1.add(new IfcLengthMeasure(0.0));
        IfcCartesianPoint c1 = new IfcCartesianPoint(p1);
        LIST<IfcLengthMeasure> p2 = new LIST<IfcLengthMeasure>();
        p2.add(new IfcLengthMeasure(xDim));
        p2.add(new IfcLengthMeasure(0.0));
        IfcCartesianPoint c2 = new IfcCartesianPoint(p2);
        LIST<IfcLengthMeasure> p3 = new LIST<IfcLengthMeasure>();
        p3.add(new IfcLengthMeasure(xDim));
        p3.add(new IfcLengthMeasure(yDim));
        IfcCartesianPoint c3 = new IfcCartesianPoint(p3);
        LIST<IfcLengthMeasure> p4 = new LIST<IfcLengthMeasure>();
        p4.add(new IfcLengthMeasure(0.0));
        p4.add(new IfcLengthMeasure(yDim));
        IfcCartesianPoint c4 = new IfcCartesianPoint(p4);
        LIST<IfcCartesianPoint> cart = new LIST<IfcCartesianPoint>();
        cart.add(c1);
        cart.add(c2);
        cart.add(c3);
        cart.add(c4);
        cart.add(c1);
        // Conversion of coordinates
        for (IfcCartesianPoint c : cart) {
            coord = convertCoords(c, spaceDirection, spaceCoords);
            points.add(coord);
        }
        checkIntermediate(con, points);
    }

    // ROOM TYPE 4 - Converts the relative polyline coords to real coords and remove intermediate points
    private static void partBooleanClipping(Connection con, IfcRepresentationItem repItem, Point2D.Double spaceCoords, List<Double> spaceDirection, ArrayList<Point2D.Double> points,
                                            Point2D.Double coord) {
        IfcBooleanClippingResult boolClipping = (IfcBooleanClippingResult) repItem;
        IfcPolygonalBoundedHalfSpace secondOperand = (IfcPolygonalBoundedHalfSpace) boolClipping.getSecondOperand();
        IfcPolyline polygonBoundary = (IfcPolyline) secondOperand.getPolygonalBoundary();
        LIST<IfcCartesianPoint> cartes = polygonBoundary.getPoints();
        // Conversion of coordinates
        for (int j = 0; j < cartes.size(); j++) {
            IfcCartesianPoint c = cartes.get(j);
            coord = convertCoords(c, spaceDirection, spaceCoords);
            points.add(coord);
        }
        // Removal of intermediate points
        checkIntermediate(con, points);
        // points.remove(points.size() - 1);
    }

    // Removes intermediate points from a polyline. An intermediate point is a point on the polyline which is placed
    // between two points with either the same x- or y-coord and can thus be removed without changing the geometry of the room
    private static void checkIntermediate(Connection con, ArrayList<Point2D.Double> points) {
        // interList contains possible intermediate points
        ArrayList<Point2D.Double> interList = new ArrayList<Point2D.Double>();
        ArrayList<Integer> indexList = new ArrayList<Integer>();

        convSIpoly(points);
        for (int i = 0; i < points.size(); i++) {
            points.get(i).setLocation(Math.round((float) points.get(i).getX()), Math.round((float) points.get(i).getY()));
        }

        for (int i = 0; i < points.size(); i++) {
            // If interList is empty the first point of the polyline is added
            if (interList.isEmpty() || interList.size() == 1) {
                interList.add(points.get(i));
            } else if (interList.size() == 2) {
                interList.add(points.get(i));
                Line2D.Double line = new Line2D.Double(interList.get(0), interList.get(2));
                Boolean intersects = null;
                try {
                    intersects = DB_WrapperSpatial.ST_Intersects_Line_Point(con, line, interList.get(1));
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Double difX = Math.abs(interList.get(0).getX() - interList.get(1).getX());
                Double difY = Math.abs(interList.get(0).getY() - interList.get(1).getY());
                if (intersects) {
                    indexList.add(i - 1);
                    interList.remove(1);
                } else if (difX < t_small && difY < t_small) {
                    indexList.add(i - 1);
                    interList.remove(1);
                } else {
                    interList.remove(0);
                }
            }
        }

        for (int j = indexList.size() - 1; j >= 0; j--) {
            points.remove((int) indexList.get(j));
        }

        // Check if start/end point is intermediate
        Line2D.Double line = new Line2D.Double(points.get(1), points.get(points.size() - 2));
        Boolean intersects = null;
        try {
            intersects = DB_WrapperSpatial.ST_Intersects_Line_Point(con, line, points.get(0));
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (intersects) {
            points.remove(points.size() - 1);
            points.remove(0);
            Point2D.Double newStart = points.get(0);
            points.add(new Point2D.Double(newStart.getX(), newStart.getY()));
        }
    }

    // Convert relative coords to real coords based on a direction and an outer coord
    private static Point2D.Double convertCoords(IfcCartesianPoint p, List<Double> spaceDirection, Point2D.Double spaceCoords) {
        ArrayList<Double> list = new ArrayList<Double>();
        Point2D.Double point = new Point2D.Double();
        Double x = spaceDirection.get(0);
        Double y = spaceDirection.get(1);
        LIST<IfcLengthMeasure> tempList = p.getCoordinates();

        for (int i = 0; i < tempList.size(); i++) {
            Double c = tempList.get(i).value;
            list.add(c);
        }

        // Depending on the direction of the space, different arithmetic calculations are
        // performed to calculate the correct coords of the polyline point
        if (x == 1) {
            point.setLocation(((spaceCoords.getX()) + (list.get(0))), ((spaceCoords.getY()) + (list.get(1))));
        } else if (x == -1) {
            point.setLocation(((spaceCoords.getX()) - (list.get(0))), ((spaceCoords.getY()) - (list.get(1))));
        } else if (y == 1) {
            point.setLocation(((spaceCoords.getX()) - (list.get(1))), ((spaceCoords.getY()) + (list.get(0))));
        } else if (y == -1) {
            point.setLocation(((spaceCoords.getX()) + (list.get(1))), ((spaceCoords.getY()) - (list.get(0))));
        } else {
            Double vectorRad = Math.atan(y / x);
            if (x < 0) {
                vectorRad += Math.PI;
            }
            Double newX = list.get(0) * Math.cos(vectorRad) - list.get(1) * Math.sin(vectorRad) + spaceCoords.getX();
            Double newY = list.get(0) * Math.sin(vectorRad) + list.get(1) * Math.cos(vectorRad) + spaceCoords.getY();

            point.setLocation(newX, newY);
        }
        return point;
    }

    // Extracts an IfcCompositeCurveSegment and returns an IfcCartesianPoint
    private static LIST<IfcCartesianPoint> ExtractCompositeCurve(IfcCompositeCurveSegment c) {
        LIST<IfcCartesianPoint> cart = new LIST<IfcCartesianPoint>();
        // Case 1
        if (c.getParentCurve() instanceof IfcTrimmedCurve) {
            IfcTrimmedCurve parentCurve = (IfcTrimmedCurve) c.getParentCurve();
            if (parentCurve.getBasisCurve() instanceof IfcLine) {
                IfcLine basisCurve = (IfcLine) parentCurve.getBasisCurve();
                IfcCartesianPoint pnt = basisCurve.getPnt();
                if (pnt != null) {
                    cart.add(pnt);
                }
            }
            if (parentCurve.getBasisCurve() instanceof IfcCircle) {
                cart = null;
            }
        }
        // Case 2
        if (c.getParentCurve() instanceof IfcPolyline) {
            IfcPolyline parentCurve = (IfcPolyline) c.getParentCurve();
            cart = parentCurve.getPoints();
        }
        return cart;
    }

    // Maps an IfcSpace object to a floor (IfcBuildingStorey object) and returns the globalID of the floor
    private static String MapP2F(IfcSpace r) {
        String floorID = null;
        Set<IfcRelDecomposes> decomposeInv;
        Iterator<IfcRelDecomposes> iter;
        IfcBuildingStorey relatObject = null;
        decomposeInv = r.getDecomposes_Inverse();
        iter = decomposeInv.iterator();
        while (iter.hasNext()) {
            IfcRelAggregates rel = (IfcRelAggregates) iter.next();
            relatObject = (IfcBuildingStorey) rel.getRelatingObject();
            floorID = (relatObject.getGlobalId()).toString();
        }
        return floorID;
    }

    // Collects all rooms in a list of object type Partition
    public static ArrayList<Partition> GetAllPartitionObjects(Connection con) {
        ArrayList<Partition> partitions = new ArrayList<Partition>();
        try {
            Collection<IfcSpace> spaces = GetAllSpaces();
            // Maps each needed attribute of the current space to a Room object and adds the Room object to the list
            for (IfcSpace space : spaces) {
                Partition part = new Partition();
                part.setGlobalID(space.getGlobalId().toString());

                if (space.getName() != null) {
                    part.setName(space.getName().toString());
                } else {
                    part.setName("roomNamePlaceholder");
                }

                if (space.getLongName() != null) {
                    part.setLongName(space.getLongName().toString());
                } else {
                    part.setLongName("roomLongnamePlaceholder");
                }
                part.setCoord(convSIpt(GetCoordinate(space)));
                part.setPolyline(GetPolyline(con, space));
                part.setFloorID(MapP2F(space));
                partitions.add(part);
            }
            System.out.println(partitions.size() + " partitions");
//			gui.IfcManagerApplet.consoleArea.append(partitions.size() + " partitions\n");
        } catch (Exception e) {
            System.out.println("Error in ExtractPartitionss");
            e.printStackTrace();
        }

        return partitions;
    }
}
