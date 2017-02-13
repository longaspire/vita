package cn.edu.zju.db.datagen.ifc.dataextraction;

import cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Elevator;
import cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Floor;
import cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Partition;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.*;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.*;

public class ExtractElevators extends IfcFileParser {

    public ExtractElevators() throws Exception {
        super();
        // TODO Auto-generated constructor stub
    }

    // Retrieves all IfcTransportElement objects from an IFC file
    @SuppressWarnings("unchecked")
    private static Collection<IfcTransportElement> GetAllTransportElements() throws Exception {
        Collection<IfcTransportElement> transportElements = null;
        transportElements = (Collection<IfcTransportElement>) ifcModel.getCollection(IfcTransportElement.class);
        return transportElements;
    }

    // Retrieves the placement coordinates of an IfcTransportElement object
    private static Point2D.Double GetCoordinate(IfcTransportElement elevator) {
        Point2D.Double p = new Point2D.Double();
        // Go through the Ifc hierarchy to reach coordinates
        IfcLocalPlacement local = (IfcLocalPlacement) elevator.getObjectPlacement();
        IfcAxis2Placement3D relat = (IfcAxis2Placement3D) local.getRelativePlacement();
        IfcCartesianPoint co = relat.getLocation();
        LIST<IfcLengthMeasure> tempList = co.getCoordinates();
        p.setLocation(tempList.get(0).value, tempList.get(1).value);
        return p;
    }

    // Retrieves the z-value (elevation) for an elevators' start and end point
    private static List<Double> getElevatorMinMax(IfcTransportElement e, List<Floor> floors) throws Exception {
        IfcProductDefinitionShape repr = (IfcProductDefinitionShape) e.getRepresentation();
        LIST<IfcRepresentation> repres = repr.getRepresentations();
        ArrayList<Double> list = new ArrayList<Double>();
        for (int i = 0; i < repres.size(); i++) {
            IfcRepresentation r = repres.get(i);
            SET<IfcRepresentationItem> items = null;
            items = r.getItems();
            Iterator<IfcRepresentationItem> itera;
            itera = items.iterator();
            while (itera.hasNext()) {
                IfcRepresentationItem repItem = itera.next();
                IfcClosedShell brep = null;
                brep = ((IfcFacetedBrep) repItem).getOuter();
                SET<IfcFace> faces = null;
                faces = brep.getCfsFaces();
                Iterator<IfcFace> iterat;
                iterat = faces.iterator();
                while (iterat.hasNext()) {
                    IfcFace face = iterat.next();
                    SET<IfcFaceBound> outBound = null;
                    outBound = face.getBounds();
                    Iterator<IfcFaceBound> iterato;
                    iterato = outBound.iterator();
                    while (iterato.hasNext()) {
                        IfcFaceBound faceBound = iterato.next();
                        IfcPolyLoop bound = null;
                        bound = (IfcPolyLoop) faceBound.getBound();
                        LIST<IfcCartesianPoint> cartPoints = null;
                        cartPoints = bound.getPolygon();
                        for (int j = 0; j < cartPoints.size(); j++) {
                            IfcCartesianPoint p = cartPoints.get(j);
                            List<IfcLengthMeasure> coords = null;
                            coords = p.getCoordinates();
                            // Collect all z-coordinates of an elevator
                            list.add(coords.get(2).value);
                        }
                    }
                }
            }
        }
        // Finds the min and max z-value from the list
        List<Double> d = findMinMax(list);
        // Maps the elevator to a floor
        String floorID = MapE2F(e);
        // Find the real min and max according to the global coordinate system
        List<Double> real_d = findRealMinMAx(floorID, d, floors);

        return real_d;
    }

    // Finds the minimum and maximum value from a list of doubles
    private static List<Double> findMinMax(List<Double> d) {
        List<Double> list = new ArrayList<Double>();
        Double tempMin = (double) 0;
        Double tempMax = (double) 0;
        for (int i = 0; i < d.size(); i++) {
            tempMin = Math.min(d.get(i), tempMin);
            tempMax = Math.max(d.get(i), tempMax);
        }
        list.add(tempMin);
        list.add(tempMax);
        return list;
    }

    // Finds the real minimum and maximum value according the global coordinate system from a list of doubles and a floor
    private static List<Double> findRealMinMAx(String floorID, List<Double> d, List<Floor> floors) {
        List<Double> list = new ArrayList<Double>();
        Double elevation = null;
        Double realMin = null;
        Double realMax = null;
        for (int i = 0; i < floors.size(); i++) {
            if ((floors.get(i).getGlobalID().toString()).equals(floorID)) {
                elevation = floors.get(i).getElevation();
                realMin = d.get(0) + elevation;
                realMax = d.get(1) + elevation;
                list.add(realMin);
                list.add(realMax);
            }
        }
        return list;
    }

    // Maps an IfcTransportElement object to the floor (IfcBuildingStorey object) on which it is defined and returns the globalID of the
    // floor
    private static String MapE2F(IfcTransportElement e) {
        String floorID = null;
        SET<IfcRelContainedInSpatialStructure> struct_inv = e.getContainedInStructure_Inverse();
        Iterator<IfcRelContainedInSpatialStructure> iter;
        iter = struct_inv.iterator();
        while (iter.hasNext()) {
            IfcRelContainedInSpatialStructure rel = iter.next();
            IfcBuildingStorey bs;
            bs = (IfcBuildingStorey) rel.getRelatingStructure();
            floorID = bs.getGlobalId().toString();
        }
        return floorID;
    }

    // Maps an IfcTransportElement object to the floors it connects
    private static ArrayList<String> MapE2Fs(List<Double> minMax, List<Floor> floors) {
        List<Floor> connFloors = new ArrayList<Floor>();
        Double min = minMax.get(0);
        Double max = minMax.get(1);
        for (int i = 0; i < floors.size(); i++) {
            Double elevation = floors.get(i).getElevation();
            if (min <= elevation && elevation <= max) {
                connFloors.add(floors.get(i));
            }
        }
        Collections.sort(connFloors, new Comparator<Floor>() {
            public int compare(Floor one, Floor other) {
                return one.getElevation().compareTo(other.getElevation());
            }
        });

        ArrayList<String> ids = new ArrayList<String>();
        for (Floor f : connFloors) {
            ids.add(f.getGlobalID().toString());
        }
        return ids;
    }

    // Maps an IfcTransportElement object to the partitions it connects
    private static ArrayList<String> MapE2P(Point2D coord, List<String> connFloorIDs, List<Partition> rooms) {
        ArrayList<String> roomIDs = new ArrayList<String>();
        for (String s : connFloorIDs) {
            for (Partition r : rooms) {
                if (s.equals(r.getFloorID())) {
                    List<Point2D.Double> pts = r.getPolyline();
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
                        String str = r.getGlobalID();
                        roomIDs.add(str);
                    }
                }
            }
        }
        return roomIDs;
    }

    // Collects all IfcTransportElements objects in a list of object type Elevator
    public static List<Elevator> GetAllElevatorObjects(ArrayList<Floor> floors, ArrayList<Partition> partitions) {
        List<Elevator> elevators = new ArrayList<Elevator>();
        try {
            Collection<IfcTransportElement> transportElements = GetAllTransportElements();
            for (IfcTransportElement transEle : transportElements) {
                Elevator elevator = new Elevator();
                elevator.setGlobalID(transEle.getGlobalId().toString());
                elevator.setFloorID(MapE2F(transEle));
                List<Double> minMax = getElevatorMinMax(transEle, floors);
                elevator.setConnFloorIDs(MapE2Fs(minMax, floors));
                elevator.setCoord(convSIpt(GetCoordinate(transEle)));
                elevator.setConnPartitionIDs(MapE2P(elevator.getCoord(), elevator.getConnFloorIDs(), partitions));
                elevators.add(elevator);
            }
            System.out.println(elevators.size() + " elevators");
//			gui.IfcManagerApplet.consoleArea.append(elevators.size() + " elevators\n");
        } catch (Exception e) {
            System.out.println("Error in ExtractElevators");
        }
        return elevators;
    }
}
