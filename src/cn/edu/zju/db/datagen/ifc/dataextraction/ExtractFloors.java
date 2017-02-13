package cn.edu.zju.db.datagen.ifc.dataextraction;

import cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Floor;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcBuilding;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcBuildingStorey;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcRelAggregates;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcRelDecomposes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class ExtractFloors extends IfcFileParser {

    public ExtractFloors() throws Exception {
        super();
        // TODO Auto-generated constructor stub
    }

    // Retrieves all IfcBuildingStorey objects from an IFC file and returns a list of IfcBuildingStorey objects
    @SuppressWarnings("unchecked")
    private static Collection<IfcBuildingStorey> getAllBuildingStoreys() throws Exception {
        Collection<IfcBuildingStorey> buildingStoreys = null;
        buildingStoreys = (Collection<IfcBuildingStorey>) ifcModel.getCollection(IfcBuildingStorey.class);
        return buildingStoreys;
    }

    // Maps a floor to a building and returns the globalID of the building
    private static String MapF2B(IfcBuildingStorey b) {
        String buildingID = null;
        Set<IfcRelDecomposes> decomposeInv = b.getDecomposes_Inverse();
        Iterator<IfcRelDecomposes> iter = decomposeInv.iterator();
        while (iter.hasNext()) {
            IfcRelAggregates rel = (IfcRelAggregates) iter.next();
            IfcBuilding relatObject = (IfcBuilding) rel.getRelatingObject();
            buildingID = (relatObject.getGlobalId()).toString();
        }
        return buildingID;
    }

    // Collects all IfcFloor objects in a list of object type Floor
    public static ArrayList<Floor> GetAllFloorObjects() {
        ArrayList<Floor> floors = new ArrayList<Floor>();
        try {
            Collection<IfcBuildingStorey> allfloors = getAllBuildingStoreys();

            // Maps each needed attribute of the current IfcFloor to the Floor object and adds the Floor object to the list
            for (IfcBuildingStorey f : allfloors) {
                Floor floor = new Floor();
                floor.setGlobalID(f.getGlobalId().toString());
                if (f.getName() != null) {
                    floor.setName(f.getName().toString());
                } else {
                    floor.setName("FloorNamePlaceHolder");
                }
                floor.setElevation(f.getElevation().value);
                floor.setBuildingID(MapF2B(f));
                floors.add(floor);
            }
            System.out.println(floors.size() + " floors");
//			gui.IfcManagerApplet.consoleArea.append(floors.size() + " floors\n");
        } catch (Exception e) {
            System.out.println("Error in ExtractFloors");
            e.printStackTrace();
        }
        return floors;
    }
}
