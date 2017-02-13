package cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Elevator {
    private String globalID = null;
    private Point2D.Double coord = new Point2D.Double();
    private String floorID = null;
    private ArrayList<String> connFloorIDs = new ArrayList<String>();
    private ArrayList<String> connPartitionIDs = new ArrayList<String>();

    // Empty constructor
    public Elevator() {
    }

    public String getGlobalID() {
        return globalID;
    }

    public void setGlobalID(String globalID) {
        this.globalID = globalID;
    }

    public Point2D.Double getCoord() {
        return coord;
    }

    public void setCoord(Point2D.Double coord) {
        this.coord = coord;
    }

    public String getFloorID() {
        return floorID;
    }

    public void setFloorID(String floorID) {
        this.floorID = floorID;
    }

    public ArrayList<String> getConnFloorIDs() {
        return connFloorIDs;
    }

    public void setConnFloorIDs(ArrayList<String> connFloorIDs) {
        this.connFloorIDs = connFloorIDs;
    }

    public ArrayList<String> getConnPartitionIDs() {
        return connPartitionIDs;
    }

    public void setConnPartitionIDs(ArrayList<String> connPartitionIDs) {
        this.connPartitionIDs = connPartitionIDs;
    }
}
