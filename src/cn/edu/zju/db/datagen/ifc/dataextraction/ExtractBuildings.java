package cn.edu.zju.db.datagen.ifc.dataextraction;

import cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Building;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.IfcBuilding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExtractBuildings extends IfcFileParser {

    public ExtractBuildings() throws Exception {
        super();
        // TODO Auto-generated constructor stub
    }

    //Retrieves all IfcBuilding objects from an IFC file and returns a list of IfcBuildingStorey objects
    @SuppressWarnings("unchecked")
    private static Collection<IfcBuilding> getAllBuildings() throws Exception {
        Collection<IfcBuilding> buildings = null;
        buildings = (Collection<IfcBuilding>) IfcFileParser.ifcModel.getCollection(IfcBuilding.class);
        return buildings;
    }

    //Collects all IfcBuilding objects in a list of object type Building
    public static List<Building> GetAllBuildingObjects() {
        List<Building> buildings = new ArrayList<Building>();
        try {
            Collection<IfcBuilding> allbuildings = getAllBuildings();

            //Maps each needed attribute of the current IfcBuilding to the Building object and adds the Building object to the list
            for (IfcBuilding b : allbuildings) {
                Building building = new Building();
                building.setGlobalID(b.getGlobalId().toString());
                if (b.getName() != null) {
                    building.setName(b.getName().toString());
                } else {
                    building.setName("BuildingNamePlaceholder");
                }
                buildings.add(building);
            }
            System.out.println(buildings.size() + " building(s)");
//	        gui.IfcManagerApplet.consoleArea.append(buildings.size() + " building(s)\n");
        } catch (Exception e) {
            System.out.println("Error in ExtractBuilding");
            e.printStackTrace();
        }
        return buildings;
    }
}
