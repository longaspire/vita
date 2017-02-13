package cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject;

import java.awt.geom.Point2D;

public class Stair {
    private String globalID = null;
    private String name = null;
    private Point2D.Double coord = new Point2D.Double();
    private String floorID = null;
    private String partID = null;
    private String upperPartID = null;
    private String upperFloorID = new String();
    private Point2D.Double upperFloorCoords = new Point2D.Double();

    //Empty constructor
    public Stair() {
    }

    public String getGlobalID() {
        return globalID;
    }

    public void setGlobalID(String globalID) {
        this.globalID = globalID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getPartID() {
        return partID;
    }

    public void setPartID(String partID) {
        this.partID = partID;
    }

    public String getUpperPartID() {
        return upperPartID;
    }

    public void setUpperPartID(String upperPartID) {
        this.upperPartID = upperPartID;
    }

    public String getUpperFloorID() {
        return upperFloorID;
    }

    public void setUpperFloorID(String upperFloorID) {
        this.upperFloorID = upperFloorID;
    }

    public Point2D.Double getUpperFloorCoords() {
        return upperFloorCoords;
    }

    public void setUpperFloorCoords(Point2D.Double upperFloorCoords) {
        this.upperFloorCoords = upperFloorCoords;
    }
}
