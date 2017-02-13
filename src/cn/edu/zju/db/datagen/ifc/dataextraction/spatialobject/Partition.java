package cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Partition {

    private String globalID = null;
    private String name = null;
    private String longName = null;
    private Point2D.Double coord = new Point2D.Double();
    private String floorID = null;
    private Integer dbid = null;
    private ArrayList<Point2D.Double> polyline = new ArrayList<Point2D.Double>();

    // Empty constructor
    public Partition() {
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

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
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

    public Integer getDbid() {
        return dbid;
    }

    public void setDbid(Integer dbid) {
        this.dbid = dbid;
    }

    public ArrayList<Point2D.Double> getPolyline() {
        return polyline;
    }

    public void setPolyline(ArrayList<Point2D.Double> polyline) {
        this.polyline = polyline;
    }

}
