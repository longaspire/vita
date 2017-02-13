package cn.edu.zju.db.datagen.database.spatialobject;

import diva.util.java2d.Polygon2D;
import org.postgis.LineString;
import org.postgis.Point;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class AccessPoint extends FloorItem {

    private Point locationGIS = new Point();
    private Point2D.Double location2D = new Point2D.Double();
    private ArrayList<Partition> partitions = new ArrayList<Partition>();
    private ArrayList<ApToPart> ap2parts = new ArrayList<ApToPart>();

    private Line2D.Double line2D = new Line2D.Double();
    private LineString lineGIS = new LineString();
    private Polygon2D.Double line2DClickBox = new Polygon2D.Double();
    private Integer apType = null;

    public ArrayList<ApToPart> getAp2parts() {
        return ap2parts;
    }

    public void setAp2parts(ArrayList<ApToPart> ap2parts) {
        this.ap2parts = ap2parts;
    }

    public Integer getApType() {
        return apType;
    }

    public void setApType(Integer apType) {
        this.apType = apType;
    }

    public Polygon2D.Double getLine2DClickBox() {
        return line2DClickBox;
    }

    public void setLine2DClickBox(Polygon2D.Double line2dClickBox) {
        line2DClickBox = line2dClickBox;
    }

    public LineString getLineGIS() {
        return lineGIS;
    }

    public void setLineGIS(LineString lineGIS) {
        this.lineGIS = lineGIS;
    }

    public Line2D.Double getLine2D() {
        return line2D;
    }

    public void setLine2D(Line2D.Double line) {
        this.line2D = line;
    }

    public AccessPoint() {
    }

    public Point2D.Double getLocation2D() {
        return location2D;
    }

    public void setLocation2D(Point2D.Double location2d) {
        location2D = location2d;
    }

    public Point getLocationGIS() {
        return locationGIS;
    }

    public void setLocationGIS(Point locationGIS) {
        this.locationGIS = locationGIS;
    }

    public ArrayList<Partition> getPartitions() {
        return partitions;
    }

    public void setPartitions(ArrayList<Partition> partitions) {
        this.partitions = partitions;
    }

    @Override
    public int compareTo(Object arg0) {
        AccessPoint anotherItem = (AccessPoint) arg0;
        return this.getItemID().compareTo(anotherItem.getItemID());
    }

    public String toString() {
        return this.itemID + "\t" + this.name + "\t" + this.location2D + "\t";
    }
}
