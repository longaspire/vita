package cn.edu.zju.db.datagen.database.spatialobject;

import java.util.ArrayList;

public class Building extends Item {

    private Integer fileID = null;
    private ArrayList<Floor> floors = new ArrayList<Floor>();
    public static int floorHeight = 5;

    public Building() {
    }

    public Integer getFileID() {
        return fileID;
    }

    public void setFileID(Integer fileID) {
        this.fileID = fileID;
    }

    public ArrayList<Floor> getFloors() {
        return floors;
    }

    public void setFloors(ArrayList<Floor> floors) {
        this.floors = floors;
    }
}
