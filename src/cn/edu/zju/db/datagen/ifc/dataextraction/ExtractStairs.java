package cn.edu.zju.db.datagen.ifc.dataextraction;

import cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Floor;
import cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Partition;
import cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Stair;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.*;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ExtractStairs extends IfcFileParser {

    public ExtractStairs() throws Exception {
        super();
        // TODO Auto-generated constructor stub
    }

    // Retrieves all IfcStair objects from an IFC file
    @SuppressWarnings("unchecked")
    private static Collection<IfcStair> GetAllStairs() throws Exception {
        Collection<IfcStair> stairs = null;
        stairs = (Collection<IfcStair>) IfcFileParser.ifcModel.getCollection(IfcStair.class);
        return stairs;
    }

    // Retrieves the placement coordinate for a given IfcStair object
    private static Point2D.Double getCoordinate(IfcStair stair) {
        Point2D.Double p = new Point2D.Double();
        // Go through the IFC hierarchy to reach coordinates
        IfcLocalPlacement local = (IfcLocalPlacement) stair.getObjectPlacement();
        IfcAxis2Placement3D relat = (IfcAxis2Placement3D) local.getRelativePlacement();
        IfcCartesianPoint co = relat.getLocation();
        LIST<IfcLengthMeasure> tempList = co.getCoordinates();
        p.setLocation(tempList.get(0).value, tempList.get(1).value);
        return p;
    }

    // Retrieves the direction coordinate for a given IfcStair object
    private static List<Double> getDirection(IfcStair stair) {
        List<Double> list = new ArrayList<Double>();
        // Go through the Ifc hierarchy
        IfcLocalPlacement local = (IfcLocalPlacement) stair.getObjectPlacement();
        IfcAxis2Placement3D relat = (IfcAxis2Placement3D) local.getRelativePlacement();
        if (relat.getRefDirection() != null) {
            IfcDirection direction = relat.getRefDirection();
            if (direction.getDirectionRatios() != null) {
                List<DOUBLE> tempList = direction.getDirectionRatios();
                for (int i = 0; i < (tempList.size() - 1); i++) {
                    Double c = tempList.get(i).value;
                    list.add(c);
                }
            }
        }
        return list;
    }

    // Collects all coordinates used to represent an IfcStair object
    private static List<ArrayList<Double>> getAllStairCoords(IfcStair e) throws Exception {
        List<ArrayList<Double>> coordList = new ArrayList<ArrayList<Double>>();
        if (e.getRepresentation() != null) {
            IfcProductDefinitionShape repr = (IfcProductDefinitionShape) e.getRepresentation(); // getRepresentation
            if (repr.getRepresentations() != null) {
                LIST<IfcRepresentation> repres = repr.getRepresentations(); // getRepresentations (LIST)
                for (int i = 0; i < repres.size(); i++) {
                    IfcShapeRepresentation r = (IfcShapeRepresentation) repres.get(i);
                    SET<IfcRepresentationItem> items = r.getItems(); // getItems (SET)
                    Iterator<IfcRepresentationItem> itera;
                    itera = items.iterator();
                    while (itera.hasNext()) {
                        IfcRepresentationItem repItem = itera.next();
                        // STAIR TYPE 1 & 2
                        if (repItem instanceof IfcMappedItem) {
                            IfcRepresentationMap repMap = ((IfcMappedItem) repItem).getMappingSource();
                            IfcShapeRepresentation shapeRep = (IfcShapeRepresentation) repMap.getMappedRepresentation();
                            SET<IfcRepresentationItem> items1 = shapeRep.getItems();
                            Iterator<IfcRepresentationItem> iterat;
                            iterat = items1.iterator();
                            while (iterat.hasNext()) {
                                IfcRepresentationItem reItem = iterat.next();
                                // STAIR TYPE 1
                                if (reItem instanceof IfcFacetedBrep) {
                                    stairFaceted(reItem, coordList);
                                }
                                // STAIR TYPE 2
                                if (reItem instanceof IfcShellBasedSurfaceModel) {

                                    stairShellBased(reItem, coordList);
                                }
                            }
                        }
                        // STAIR TYPE 3
                        if (repItem instanceof IfcFacetedBrep) {
                            stairFaceted(repItem, coordList);
                        }
                        // STAIR TYPE 4
                        if (repItem instanceof IfcShellBasedSurfaceModel) {
                            stairShellBased(repItem, coordList);
                        }
                    }

                }
            }
        }
        return coordList;
    }

    private static Stair getStairFromNullIfc(IfcStair ifcStair, List<Floor> floors, List<Partition> partitions) {
        Stair stair = new Stair();
        stair.setGlobalID(ifcStair.getGlobalId().toString());
        stair.setName(ifcStair.getName().toString());
        stair.setFloorID(MapS2F(ifcStair));
        Point2D.Double curFloorCord = null;
        Point2D.Double upperFloorCord = null;
        double minX = 1_000_000;
        double minY = 1_000_000;
        double maxX = -1_000_000;
        double maxY = -1_000_000;
        SET<IfcRelDecomposes> decomposedBy_inverse = ifcStair.getIsDecomposedBy_Inverse();
        for (IfcRelDecomposes ifcRelDecomposes : decomposedBy_inverse) {
            for (IfcObjectDefinition ifcObjectDefinition : ifcRelDecomposes.getRelatedObjects()) {
                if (ifcObjectDefinition instanceof IfcStairFlight) {
                    for (IfcRepresentation ifcRepresentation : ((IfcStairFlight) ifcObjectDefinition).getRepresentation().getRepresentations()) {
                        IfcShapeRepresentation ifcShapeRepresentation = (IfcShapeRepresentation) ifcRepresentation;
                        if (ifcShapeRepresentation.getRepresentationIdentifier().getDecodedValue().equals("Boundary")) {
                            for (IfcRepresentationItem ifcRepresentationItem : ifcShapeRepresentation.getItems()) {
                                IfcGeometricSet ifcGeometricSet = (IfcGeometricSet) ifcRepresentationItem;
                                for (IfcGeometricSetSelect ifcGeometricSetSelect : ifcGeometricSet.getElements()) {
                                    IfcPolyline ifcPolyline = (IfcPolyline) ifcGeometricSetSelect;
                                    for (IfcCartesianPoint ifcCartesianPoint : ifcPolyline.getPoints()) {
                                        double tmpX = ifcCartesianPoint.getCoordinates().get(0).value;
                                        if (tmpX < minX) {
                                            minX = tmpX;
                                        }
                                        if (tmpX > maxX) {
                                            maxX = tmpX;
                                        }
                                        double tmpY = ifcCartesianPoint.getCoordinates().get(1).value;
                                        if (tmpY < minY) {
                                            minY = tmpY;
                                        }
                                        if (tmpY > maxY) {
                                            maxY = tmpY;
                                        }
                                        System.out.println("location " + tmpX + " " + tmpY);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        curFloorCord = new Point2D.Double(minX, minY);
        upperFloorCord = new Point2D.Double(maxX, maxY);

        stair.setCoord(convSIpt(curFloorCord));
        stair.setPartID(mapS2P(stair.getCoord(), stair.getFloorID(), partitions));

        Double maxZ = extractMaxZ(ifcStair);
        stair.setUpperFloorID(MapS2UF2(stair.getFloorID(), maxZ, floors));
        stair.setUpperFloorCoords(convSIpt(upperFloorCord));
        stair.setUpperPartID(mapS2P(stair.getUpperFloorCoords(), stair.getUpperFloorID(), partitions));


        return stair;
    }

    private static Double extractMaxZ(IfcStair e) {
        double maxZ = 0;
        SET<IfcRelDecomposes> decomposedBy_inverse = e.getIsDecomposedBy_Inverse();
        for (IfcRelDecomposes ifcRelDecomposes : decomposedBy_inverse) {
            for (IfcObjectDefinition ifcObjectDefinition : ifcRelDecomposes.getRelatedObjects()) {
                if (ifcObjectDefinition instanceof IfcStairFlight) {
                    for (IfcRepresentation ifcRepresentation : ((IfcStairFlight) ifcObjectDefinition).getRepresentation().getRepresentations()) {
                        IfcShapeRepresentation ifcShapeRepresentation = (IfcShapeRepresentation) ifcRepresentation;
                        if (ifcShapeRepresentation.getRepresentationIdentifier().getDecodedValue().equals("Body")) {
                            for (IfcRepresentationItem ifcRepresentationItem : ifcShapeRepresentation.getItems()) {
                                IfcExtrudedAreaSolid ifcExtrudedAreaSolid = (IfcExtrudedAreaSolid) ifcRepresentationItem;
                                IfcCartesianPoint location = ifcExtrudedAreaSolid.getPosition().getLocation();
                                IfcLengthMeasure z = location.getCoordinates().get(2);
                                if (z.value > maxZ) {
                                    maxZ = z.value;
                                }
                            }
                        }
                    }
                }
            }
        }
        return maxZ;
    }

    // STAIR TYPE 3
    private static List<ArrayList<Double>> stairFaceted(IfcRepresentationItem repItem, List<ArrayList<Double>> coordList) {
        List<Double> co = new ArrayList<Double>();
        IfcClosedShell outer = ((IfcFacetedBrep) repItem).getOuter();

        SET<IfcFace> CfsFaces = outer.getCfsFaces();
        Iterator<IfcFace> iterat;
        iterat = CfsFaces.iterator();
        while (iterat.hasNext()) {
            IfcFace face = iterat.next();
            SET<IfcFaceBound> outBound = face.getBounds();
            Iterator<IfcFaceBound> iterato;
            iterato = outBound.iterator();
            while (iterato.hasNext()) {
                IfcFaceBound faceBound = iterato.next();
                IfcPolyLoop bound = (IfcPolyLoop) faceBound.getBound();
                LIST<IfcCartesianPoint> cartPoints = bound.getPolygon();
                for (int j = 0; j < cartPoints.size(); j++) {
                    IfcCartesianPoint p = cartPoints.get(j);
                    co = ExtractCoords(p);
                    coordList.add((ArrayList<Double>) co);
                }
            }
        }
        return coordList;
    }

    // STAIR TYPE 4
    private static List<ArrayList<Double>> stairShellBased(IfcRepresentationItem repItem, List<ArrayList<Double>> coordList) {
        List<Double> co = new ArrayList<Double>();
        SET<IfcShell> sbsm = ((IfcShellBasedSurfaceModel) repItem).getSbsmBoundary();
        Iterator<IfcShell> ite;
        ite = sbsm.iterator();

        while (ite.hasNext()) {
            IfcOpenShell s = (IfcOpenShell) ite.next();
            SET<IfcFace> CfsFaces = s.getCfsFaces();
            Iterator<IfcFace> iter;
            iter = CfsFaces.iterator();
            while (iter.hasNext()) {
                IfcFace face = iter.next();
                SET<IfcFaceBound> outBound = face.getBounds();
                Iterator<IfcFaceBound> iterat;
                iterat = outBound.iterator();
                while (iterat.hasNext()) {
                    IfcFaceBound faceBound = iterat.next();
                    IfcPolyLoop bound = (IfcPolyLoop) faceBound.getBound();
                    LIST<IfcCartesianPoint> cartPoints = bound.getPolygon();
                    for (int j = 0; j < cartPoints.size(); j++) {
                        IfcCartesianPoint p = cartPoints.get(j);
                        co = ExtractCoords(p);
                        coordList.add((ArrayList<Double>) co);
                    }
                }
            }
        }
        return coordList;
    }

    // Finds coordinates with the highest z-value
    private static Point2D.Double getUpperCoords(List<ArrayList<Double>> allCoords, List<Double> stairDirection, Point2D.Double coord) {
        Point2D.Double upperCoords = new Point2D.Double();

        List<ArrayList<Double>> maxCoords = findMax(allCoords);
        List<ArrayList<Double>> realMaxCoords = convertCoordstoGlobal(maxCoords, stairDirection, coord);

        for (int i = 0; i < realMaxCoords.size(); i++) {
            List<Double> d = realMaxCoords.get(i);
            upperCoords.setLocation(d.get(0), d.get(1));
        }
        return upperCoords;
    }

    // Converts relative coords to real coords according to the global coordinate system
    private static List<ArrayList<Double>> convertCoordstoGlobal(List<ArrayList<Double>> allCoords, List<Double> stairDirection, Point2D coord) {
        List<ArrayList<Double>> finalList = new ArrayList<ArrayList<Double>>();

        // Rounding of the direction values and converting them to integers
        Integer x = (int) StrictMath.round(stairDirection.get(0));
        Integer y = (int) StrictMath.round(stairDirection.get(1));

        for (int i = 0; i < allCoords.size(); i++) {
            List<Double> tempList = new ArrayList<Double>();
            List<Double> co = allCoords.get(i);
            // Depending on the direction of the space, different arithmetic calculations are
            // performed to calculate the correct coords of the polyline point
            if (x == 1) {
                tempList.add((coord.getX()) + (convSI(co.get(0))));
                tempList.add((coord.getY()) + (convSI(co.get(1))));
            }
            if (x == -1) {
                tempList.add(((coord.getX()) - convSI(co.get(0))));
                tempList.add(((coord.getY()) - convSI(co.get(1))));
            }
            if (y == 1) {
                tempList.add(((coord.getX()) - convSI(co.get(1))));
                tempList.add((coord.getY()) + convSI(co.get(0)));
            }
            if (y == -1) {
                tempList.add((coord.getX()) + convSI(co.get(1)));
                tempList.add((coord.getY()) - convSI(co.get(0)));
            }
            finalList.add((ArrayList<Double>) tempList);
        }

        return finalList;
    }

    // Converts an IfcCartesianPoint object to a list of doubles
    private static List<Double> ExtractCoords(IfcCartesianPoint p) {
        LIST<IfcLengthMeasure> tempList;
        ArrayList<Double> list = new ArrayList<Double>();

        tempList = p.getCoordinates();
        // Convert the coords from a list of IfcLengthMeasure objects to a list of doubles
        for (int i = 0; i < tempList.size(); i++) {
            Double c = tempList.get(i).value;
            list.add(c);
        }
        return list;
    }

    // Finds the coordinate(s) with the largest z-value
    private static List<ArrayList<Double>> findMax(List<ArrayList<Double>> d) {
        // Reasonable to initiate tempMinZ to 0, since we do no accept stairs not starting at the floor level
        Double maxZ = (double) 0;
        List<ArrayList<Double>> maxList = new ArrayList<ArrayList<Double>>();
        for (int i = 0; i < d.size(); i++) {
            List<Double> a = d.get(i);
            if (Math.max((a.get(2)), maxZ) == a.get(2)) {
                maxZ = a.get(2);
            }
        }
        for (int i = 0; i < d.size(); i++) {
            List<Double> a = d.get(i);
            if (a.get(2) == maxZ) {
                List<Double> co = new ArrayList<Double>();
                co.add(a.get(0));
                co.add(a.get(1));
                co.add(a.get(2));
                maxList.add((ArrayList<Double>) co);
            }
        }
        return maxList;
    }

    // Maps a stair to the floor on which it is defined and returns the globalID of the floor
    private static String MapS2F(IfcStair s) {
        String floorID = null;
        if (s.getContainedInStructure_Inverse() != null) {
            SET<IfcRelContainedInSpatialStructure> struct_inv = s.getContainedInStructure_Inverse();
            Iterator<IfcRelContainedInSpatialStructure> iter;
            iter = struct_inv.iterator();
            while (iter.hasNext()) {
                IfcRelContainedInSpatialStructure rel = iter.next();
                IfcBuildingStorey bs = (IfcBuildingStorey) rel.getRelatingStructure();
                floorID = bs.getGlobalId().toString();
            }
        }
        return floorID;
    }

    // Maps a stair the upper floor it connects
    private static String MapS2UF(String floorID, List<ArrayList<Double>> maxCoords, List<Floor> floors) {
        String upperFloorID = null;
        Double floorElevation = null;
        if (!maxCoords.isEmpty()) {
            ArrayList<Double> d = maxCoords.get(0);
            if (!d.isEmpty()) {
                Double maxZ = d.get(2);
                Double realMaxZ;
                Double max = null;
                Double min = null;
                Double diff = null;
                Double realDiff = (double) 1000000;
                String tempFloorID = null;

                for (int i = 0; i < floors.size(); i++) {
                    Floor f = floors.get(i);
                    if (f.getGlobalID().equals(floorID)) {
                        floorElevation = f.getElevation();
                    }
                }
                realMaxZ = floorElevation + maxZ;
                for (int i = 0; i < floors.size(); i++) {
                    Floor f = floors.get(i);
                    max = Math.max(realMaxZ, f.getElevation());
                    min = Math.min(realMaxZ, f.getElevation());
                    diff = max - min;
                    if (diff < realDiff) {
                        realDiff = diff;
                        tempFloorID = f.getGlobalID();
                    }
                }
                upperFloorID = tempFloorID;
            }
        }
        return upperFloorID;
    }

    private static String MapS2UF2(String floorId, double maxZ, List<Floor> floors) {
        String upperFloorId = null;
        Double floorElevation = null;
        Double realMaxZ = null;
        Double realDiff = (double) 1_000_000;
        Double max = null;
        Double min = null;
        Double diff = null;
        String tempFloorId = null;
        for (Floor floor : floors) {
            if (floor.getGlobalID().equals(floorId)) {
                floorElevation = floor.getElevation();
            }
        }
        realMaxZ = floorElevation + maxZ;
        for (Floor floor : floors) {
            max = Math.max(realMaxZ, floor.getElevation());
            min = Math.min(realMaxZ, floor.getElevation());
            diff = max - min;
            if (diff < realDiff) {
                realDiff = diff;
                tempFloorId = floor.getGlobalID();
            }
            upperFloorId = tempFloorId;
        }
        return upperFloorId;
    }

/*	// Maps a stair to a partition
    private static String MapS2P(Point2D coord, String floorID, List<Partition> rooms) {
		String roomID = null;
		if (floorID != null) {
			for (Partition r : rooms) {
				if (r.getFloorID() != null) {
					if (floorID.equals(r.getFloorID())) {
						if (!r.getPolyline().isEmpty()) {
							ArrayList<Point2D.Double> pts = r.getPolyline();
							Path2D.Double polygon = new Path2D.Double();
							boolean first = true;
							for (Point2D p : pts) {
								if (first) {
									polygon.moveTo(p.getX(), p.getY());
									first = false;
								} else {
									polygon.lineTo(p.getX(), p.getY());
								}
							}
							polygon.closePath();

							if (polygon.contains(coord)) {
								roomID = r.getGlobalID();
								return roomID;
							}
						}
					}
				}
			}
		}
		return null;
	}*/

    private static String mapS2P(Point2D coord, String floorID, List<Partition> rooms) {
        double min_dist = Double.MAX_VALUE;
        String roomID = null;
        for (Partition room : rooms) {
            if (room.getFloorID().equals(floorID)) {    //may be modify it to get four bigger bounds of stair, check if it contains
                Path2D.Double polygon = getPath2DFromPointsList(room.getPolyline());
                if (polygon.contains(coord)) {
                    roomID = room.getGlobalID();
                    System.out.println(coord);
                    System.out.println(polygon.getBounds2D());
                    return roomID;
                }

                //distance calculation should be modified
                double tempDist = room.getCoord().distance(coord);
                if (tempDist < min_dist) {
                    roomID = room.getGlobalID();
                    min_dist = tempDist;
                }
            }
        }
        return roomID;
    }

    private static Path2D.Double getPath2DFromPointsList(ArrayList<Point2D.Double> points) {
        Path2D.Double polygon = new Path2D.Double();
        boolean first = true;
        for (Point2D p : points) {
            if (first) {
                polygon.moveTo(p.getX(), p.getY());
                first = false;
            } else {
                polygon.lineTo(p.getX(), p.getY());
            }
        }
        polygon.closePath();
        return polygon;

    }

    // Collects all IfcStair objects in a list of object type Stair
    public static List<Stair> GetAllStairObjects(ArrayList<Floor> floors, ArrayList<Partition> partitions) {
        List<Stair> stairs = new ArrayList<Stair>();
        try {
            Collection<IfcStair> ifcstairs = GetAllStairs();
            for (IfcStair ifcstair : ifcstairs) {
                if (ifcstair.getRepresentation() == null) {
                    stairs.add(getStairFromNullIfc(ifcstair, floors, partitions));
                } else {
                    Stair stair = new Stair();
                    List<ArrayList<Double>> allCoords = getAllStairCoords(ifcstair);
                    List<ArrayList<Double>> maxList = findMax(allCoords);
                    String floorID = MapS2F(ifcstair);
                    if (floorID != null) {
                        String upperFloorID = MapS2UF(floorID, maxList, floors);
                        if (!floorID.equals(upperFloorID)) {
                            List<Double> stairDirection = getDirection(ifcstair);
                            stair.setGlobalID(ifcstair.getGlobalId().toString());
                            if (ifcstair.getName() != null) {
                                stair.setName(ifcstair.getName().toString());
                            } else {
                                stair.setName("stairNamePlaceholder");
                            }
                            stair.setCoord(convSIpt(getCoordinate(ifcstair)));
                            stair.setFloorID(floorID);
                            stair.setUpperFloorID(upperFloorID);
                            if (!stairDirection.isEmpty()) {
                                stair.setUpperFloorCoords(convSIpt(getUpperCoords(allCoords, stairDirection, stair.getCoord())));
                            }
//						stair.setPartID(MapS2P(stair.getCoord(), stair.getFloorID(), partitions));
//						stair.setUpperPartID(MapS2P(stair.getUpperFloorCoords(), stair.getUpperFloorID(), partitions));
                            stair.setPartID(mapS2P(stair.getCoord(), stair.getFloorID(), partitions));
                            stair.setUpperPartID(mapS2P(stair.getUpperFloorCoords(), stair.getUpperFloorID(), partitions));
                            if (stair.getCoord().getX() < 0 || stair.getCoord().getY() < 0) {
                                stair.setCoord(stair.getUpperFloorCoords());
                                stair.setPartID(mapS2P(stair.getCoord(), stair.getFloorID(), partitions));
                            }
                            stairs.add(stair);
                        }
                    }
                }
            }
            System.out.println(stairs.size() + " stairs");
//			gui.IfcManagerApplet.consoleArea.append(stairs.size() + " stairs\n");
        } catch (Exception e) {
            System.out.println("Error in ExtractStairs");
            e.printStackTrace();
        }
        return stairs;
    }

}
