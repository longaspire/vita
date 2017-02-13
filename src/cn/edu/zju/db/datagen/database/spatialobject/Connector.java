package cn.edu.zju.db.datagen.database.spatialobject;

import org.postgis.Point;

import java.awt.geom.Point2D;

public class Connector extends AccessPoint {

    private Point locationUpperGIS = null;
    private Point2D.Double upperlocation2D = null;
    private Floor upperFloor = null;
    private Partition upperPartition = null;

    public Connector() {
    }

    public Point getLocationUpperGIS() {
        return locationUpperGIS;
    }

    public void setLocationUpperGIS(Point locationUpperGIS) {
        this.locationUpperGIS = locationUpperGIS;
    }

    public Floor getUpperFloor() {
        return upperFloor;
    }

    public void setUpperFloor(Floor upperFloor) {
        this.upperFloor = upperFloor;
    }

    public Point2D.Double getUpperLocation2D() {
        return this.upperlocation2D;
    }

    public void setUpperLocation2D(Point2D.Double upperLoc) {
        this.upperlocation2D = upperLoc;
    }

    public Partition getUpperPartition() {
        return upperPartition;
    }

    public void setUpperPartition(Partition upperPart) {
        upperPartition = upperPart;
    }
}
