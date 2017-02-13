package cn.edu.zju.db.datagen.ifc.dataextraction;

import cn.edu.zju.db.datagen.database.DB_WrapperSpatial;
import cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Door;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.*;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.sql.Connection;
import java.util.*;

public class ExtractDoors extends IfcFileParser {

    public ExtractDoors() throws Exception {
        super();
        // TODO Auto-generated constructor stub
    }

    // Retrieves all IfcDoor objects from an IFC file
    @SuppressWarnings("unchecked")
    private static Collection<IfcDoor> GetAllDoors() throws Exception {
        Collection<IfcDoor> doors = null;
        doors = (Collection<IfcDoor>) ifcModel.getCollection(IfcDoor.class);
        return doors;
    }

    // Retrieves the placement coordinates of an IfcDoor object
    private static Point2D.Double GetCoordinate(IfcDoor door) {
        IfcAxis2Placement3D relat = null;
        IfcCartesianPoint co = null;
        LIST<IfcLengthMeasure> tempList = null;
        Point2D.Double innerCoord = null;
        Point2D.Double middleCoord = new Point2D.Double();
        Point2D.Double outerCoord = new Point2D.Double();
        ArrayList<Point2D.Double> directions = (ArrayList<Point2D.Double>) GetDirection(door);
        Point2D.Double finalCoord = null;
        // Go through the Ifc hierarchy to find the coords of the door
        IfcLocalPlacement objectPlace = (IfcLocalPlacement) door.getObjectPlacement();
        IfcLocalPlacement placeRelTo1 = (IfcLocalPlacement) objectPlace.getPlacementRelTo();

        // CASE1
        if (placeRelTo1.getPlacementRelTo() != null) {
            IfcLocalPlacement placeRelTo2 = (IfcLocalPlacement) placeRelTo1.getPlacementRelTo();
            relat = (IfcAxis2Placement3D) placeRelTo2.getRelativePlacement();
            co = relat.getLocation();
            tempList = co.getCoordinates();
            innerCoord = new Point2D.Double(tempList.get(0).value, tempList.get(1).value);
        }

        // CASE2
        relat = (IfcAxis2Placement3D) placeRelTo1.getRelativePlacement();
        co = relat.getLocation();
        tempList = co.getCoordinates();
        middleCoord = new Point2D.Double(tempList.get(0).value, tempList.get(1).value);

        // CASE3
        relat = (IfcAxis2Placement3D) objectPlace.getRelativePlacement();
        co = relat.getLocation();
        tempList = co.getCoordinates();
        outerCoord = new Point2D.Double(tempList.get(0).value, tempList.get(1).value);

        // Initially finalCoord is set to middleCoord
        finalCoord = new Point2D.Double(middleCoord.getX(), middleCoord.getY());

        Double x = null;
        Double y = null;
        // Depending on the direction of the door, different arithmetic calculations are
        // performed to calculate the correct coords of door
        if (innerCoord != null) {

            // Special case
            if (directions.get(2) != null) {
                x = directions.get(2).getX();
                y = directions.get(2).getY();
            } else {
                x = 1.0;
                y = 1.0;
            }
            // The four different normal directions
            if (x == 1) {
                finalCoord.setLocation((innerCoord.getX()) + (finalCoord.getX()), (innerCoord.getY()) + (finalCoord.getY()));
            } else if (x == -1) {
                finalCoord.setLocation((innerCoord.getX()) - (finalCoord.getX()), (innerCoord.getY()) - (finalCoord.getY()));
            } else if (y == 1) {
                finalCoord.setLocation((innerCoord.getX()) - (finalCoord.getY()), (innerCoord.getY()) + (finalCoord.getX()));
            } else if (y == -1) {
                finalCoord.setLocation((innerCoord.getX()) + (finalCoord.getY()), (innerCoord.getY()) - (finalCoord.getX()));
            } // If the direction is odd
            else {
                Double vectorRad = Math.atan(y / x);
                if (x < 0) {
                    vectorRad += Math.PI;
                }
                Double newX = finalCoord.getX() * Math.cos(vectorRad) - finalCoord.getY() * Math.sin(vectorRad) + innerCoord.getX();
                Double newY = finalCoord.getX() * Math.sin(vectorRad) + finalCoord.getY() * Math.cos(vectorRad) + innerCoord.getY();

                finalCoord.setLocation(newX, newY);
            }
        }

        // Special case
        if (directions.get(0).getX() == -1.0) {
            outerCoord.setLocation(-outerCoord.getX(), outerCoord.getY());
        }

        if (middleCoord != null) {
            x = null;
            y = null;
            // Special case
            if (directions.get(2) != null) {
                x = directions.get(2).getX();
                y = directions.get(2).getY();
            } else {
                x = 1.0;
                y = 0.0;
            }
            // The four different normal directions
            if (x == 1) {
                finalCoord.setLocation((finalCoord.getX()) + (outerCoord.getX()), (finalCoord.getY()) + (outerCoord.getY()));
            } else if (x == -1) {
                finalCoord.setLocation((finalCoord.getX()) - (outerCoord.getX()), (finalCoord.getY()) - (outerCoord.getY()));
            } else if (y == 1) {
                finalCoord.setLocation((finalCoord.getX()) - (outerCoord.getY()), (finalCoord.getY()) + (outerCoord.getX()));
            } else if (y == -1) {
                finalCoord.setLocation((finalCoord.getX()) + (outerCoord.getY()), (finalCoord.getY()) - (outerCoord.getX()));
            } // If the direction is odd
            else {
                Double vectorRad = Math.atan(y / x);
                if (x < 0) {
                    vectorRad += Math.PI;
                }
                Double newX = outerCoord.getX() * Math.cos(vectorRad) - outerCoord.getY() * Math.sin(vectorRad) + finalCoord.getX();
                Double newY = outerCoord.getX() * Math.sin(vectorRad) + outerCoord.getY() * Math.cos(vectorRad) + finalCoord.getY();

                finalCoord.setLocation(newX, newY);
            }
        }
        return finalCoord;
    }

    // Retrieves the direction coordinates of an IfcDoor object
    private static ArrayList<Point2D.Double> GetDirection(IfcDoor door) {
        ArrayList<DOUBLE> tempList = new ArrayList<DOUBLE>();
        ArrayList<Point2D.Double> directions = new ArrayList<Point2D.Double>();
        IfcLocalPlacement objectPlace = (IfcLocalPlacement) door.getObjectPlacement();
        IfcLocalPlacement placeRelTo1 = (IfcLocalPlacement) objectPlace.getPlacementRelTo();
        IfcAxis2Placement3D relatOuter = (IfcAxis2Placement3D) objectPlace.getRelativePlacement();
        // Retrieves the outer direction
        if (relatOuter.getRefDirection() != null) {
            if (relatOuter.getRefDirection().getDirectionRatios() != null) {
                tempList = relatOuter.getRefDirection().getDirectionRatios();
                directions.add(0, new Point2D.Double(tempList.get(0).value, tempList.get(1).value));
            } else {
                directions.add(0, new Point2D.Double(1.0, 0.0));
            }
        } else {
            directions.add(0, new Point2D.Double(1.0, 0.0));
        }
        // Retrieves the middle direction
        IfcAxis2Placement3D relatMiddle = (IfcAxis2Placement3D) placeRelTo1.getRelativePlacement();
        if (relatMiddle.getRefDirection() != null) {
            if (relatMiddle.getRefDirection().getDirectionRatios() != null) {
                tempList = relatMiddle.getRefDirection().getDirectionRatios();
                if (tempList.get(0).value == 0 && tempList.get(1).value == 1) {
                    directions.add(1, new Point2D.Double(1.0, 0.0));
                } else {
                    directions.add(1, new Point2D.Double(tempList.get(0).value, tempList.get(1).value));
                }
            } else {
                directions.add(1, null);
            }
        } else {
            directions.add(1, null);
        }
        // Retrieves the inner direction
        if (placeRelTo1.getPlacementRelTo() != null) {
            IfcLocalPlacement placeRelTo2 = (IfcLocalPlacement) placeRelTo1.getPlacementRelTo();
            if (placeRelTo2.getRelativePlacement() != null) {
                IfcAxis2Placement3D relatInner = (IfcAxis2Placement3D) placeRelTo2.getRelativePlacement();
                if (relatInner.getRefDirection() != null) {
                    if (relatInner.getRefDirection().getDirectionRatios() != null) {
                        tempList = relatInner.getRefDirection().getDirectionRatios();
                        directions.add(2, new Point2D.Double(tempList.get(0).value, tempList.get(1).value));
                    } else {
                        directions.add(2, null);
                    }
                } else {
                    directions.add(2, null);
                }
            }
        }
        return directions;
    }

    // Retrieves the width of an IfcDoor object
    private static Double GetDoorWidth(IfcDoor d) {
        if (d.getOverallWidth() != null) {
            Double width = d.getOverallWidth().value;
            return width;
        }
        // If no width is specified - the width is set to 91 cm
        return 910.0;
    }

    // Maps an IfcDoor object to a floor (IfcBuildingStorey object) and returns the globalID of the floor
    private static String MapD2F(IfcDoor d) {
        String floorID = null;

        if (d.getContainedInStructure_Inverse() == null) {
            return null;
        }
        Set<IfcRelContainedInSpatialStructure> containInStruc = d.getContainedInStructure_Inverse();
        Iterator<IfcRelContainedInSpatialStructure> iter = containInStruc.iterator();

        while (iter.hasNext()) {
            IfcRelContainedInSpatialStructure rel = iter.next();
            IfcBuildingStorey relatStruc = (IfcBuildingStorey) rel.getRelatingStructure();
            floorID = (relatStruc.getGlobalId()).toString();
        }
        return floorID;
    }

    // Find the coordinates of the endpoints of a door and returns them as a Line2D
    private static Line2D.Double getLine(Point2D.Double coord, Double width, IfcDoor d) {
        Line2D.Double line = new Line2D.Double();
        ArrayList<Point2D.Double> directions = GetDirection(d);
        Point2D.Double lineEndCoord = new Point2D.Double();

        // Change of operational sign depending on direction
        if (directions.get(1) == null) {
            if (directions.get(2) == null) {
                // Do nothing
            } else if (directions.get(0).getX() != 0.0) {
                width = width * directions.get(0).getX();
            } else {
                width = width * directions.get(0).getY();
            }
        }

        Double x = null;
        Double y = null;

        // Set direction to be used depending on available directions
        if (directions.get(2) != null) {
            x = directions.get(2).getX();
            y = directions.get(2).getY();
        } else if (directions.get(2) == null && directions.get(1) == null) {
            x = directions.get(0).getX();
            y = directions.get(0).getY();
        } else {
            x = 1.0;
            y = 0.0;
        }

        // Find line end coordinate based on direction and start coordinate
        if (x == 1) {
            lineEndCoord.setLocation(coord.getX() + width, coord.getY());
        } else if (x == -1) {
            lineEndCoord.setLocation(coord.getX() - width, coord.getY());
        } else if (y == 1) {
            lineEndCoord.setLocation(coord.getX(), coord.getY() + width);
        } else if (y == -1) {
            lineEndCoord.setLocation(coord.getX(), coord.getY() - width);
        } else {
            Double vectorRad = Math.atan(y / x);
            if (x < 0) {
                vectorRad += Math.PI;
            }
            Double newX = width * Math.cos(vectorRad) + coord.getX();
            Double newY = width * Math.sin(vectorRad) + coord.getY();

            lineEndCoord.setLocation(newX, newY);
        }
        line.setLine(coord, lineEndCoord);

        return line;
    }

    // Collects all IfcDoor objects in a list of object type Door
    public static List<Door> GetAllDoorObjects(Connection con) {
        List<Door> doors = new ArrayList<Door>();
        try {
            Collection<IfcDoor> alldoors = GetAllDoors();
            // Maps each needed attribute of the current door to Door object and adds the Door object to the list
            for (IfcDoor d : alldoors) {
                Door door = new Door();
                door.setGlobalID(d.getGlobalId().toString());
                if (d.getName() != null) {
                    door.setName(d.getName().toString());
                }
                door.setRepLine(getLine(convSIpt(GetCoordinate(d)), convSI(GetDoorWidth(d)), d));
                door.setFinalCoord(DB_WrapperSpatial.ST_Centroid_Line(con, door.getRepLine()));
                door.setFloorID(MapD2F(d));
                if (door.getFloorID() == null) {
                    continue;
                }
                doors.add(door);
            }
            System.out.println(doors.size() + " doors");
//			gui.IfcManagerApplet.consoleArea.append(doors.size() + " doors\n");
        } catch (Exception e) {
            System.out.println("Error in ExtractDoors");
            e.printStackTrace();
        }
        return doors;
    }
}
