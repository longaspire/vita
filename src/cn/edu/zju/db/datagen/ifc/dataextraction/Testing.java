package cn.edu.zju.db.datagen.ifc.dataextraction;

import cn.edu.zju.db.datagen.database.DB_Connection;
import cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.*;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class Testing {

    public static void main(String[] args) {
        Connection con = null;
        try {
            con = DB_Connection.connectToDatabase("conf/moovework.properties");
            File file = new File("D:\\JavaWorkspace\\datagenerator\\IFC Files\\AC11-Institute-Var-2-IFC.ifc");
            IfcFileParser.readFile(file);
            /*DB_FileUploader uploader = new DB_FileUploader();
			UploadObject uploadObj = new UploadObject();
			uploader.saveObjectToDB(uploadObj, file);*/

/*			List<Building> buildingList = ExtractBuildings.GetAllBuildingObjects();
			Building building = buildingList.get(0);
			DB_WrapperInsert.insertBuilding(con, building.getName(), building.getGlobalID(), 4);
			
			ArrayList<Floor> floors = ExtractFloors.GetAllFloorObjects();
			for(Floor floor : floors){
				DB_WrapperInsert.insertFloor(con, floor.getName(), floor.getGlobalID(), floor.getBuildingID());
			}
			
			ArrayList<Partition> partitions = ExtractPartitions.GetAllPartitionObjects(con);
			for(Partition partition : partitions){
				DB_WrapperInsert.insertPartition(con, partition.getName(), 
						partition.getGlobalID(), partition.getFloorID(), 
						partition.getPolyline());
			}
			
			List<Door> doors = ExtractDoors.GetAllDoorObjects(con);*/
			/*for(Door door : doors){
				DB_WrapperInsert.insertDoor
			}*/
            List<Building> buildingList = ExtractBuildings.GetAllBuildingObjects();
            Building building = buildingList.get(0);
            ArrayList<Floor> floors = ExtractFloors.GetAllFloorObjects();
            ArrayList<Partition> partitions = ExtractPartitions.GetAllPartitionObjects(con);
            List<Door> doors = ExtractDoors.GetAllDoorObjects(con);
            List<Elevator> elevators = ExtractElevators.GetAllElevatorObjects(floors, partitions);
            List<Stair> stairs = ExtractStairs.GetAllStairObjects(floors, partitions);
            for (Stair stair : stairs) {
                System.out.println("Stair Name: " + stair.getName());
                System.out.println("GlobalID: " + stair.getGlobalID());
                System.out.println("FlorID:" + stair.getFloorID());
                System.out.println("PartID: " + stair.getPartID());
                System.out.println("Upper Floor ID: " + stair.getUpperFloorID());
                System.out.println("Upper Part ID: " + stair.getUpperPartID());
                System.out.println("Stair Coord: " + stair.getCoord());
                System.out.println("Upper Floor coords: " + stair.getUpperFloorCoords());
                System.out.println("");
            }
//			DB_Import.importStairs(con, floors, partitions);
//			DB_Import.importAll(con, 1, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
