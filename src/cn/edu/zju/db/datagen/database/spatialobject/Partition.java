package cn.edu.zju.db.datagen.database.spatialobject;

import diva.util.java2d.Polygon2D;
import org.postgis.Polygon;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Partition extends FloorItem {

    private ArrayList<AccessPoint> accesspoints = new ArrayList<AccessPoint>();
    private ArrayList<AccessPoint> APConnectors = new ArrayList<AccessPoint>();
    private Polygon2D.Double polygon2D = new Polygon2D.Double();
    private Polygon polygonGIS = new Polygon();
    private Partition oriPart = null;
    private ArrayList<Partition> decParts = null;
    private ArrayList<Connector> conns = new ArrayList<Connector>();

    public Partition() {
    }

    public Polygon getPolygonGIS() {
        return polygonGIS;
    }

    public void setPolygonGIS(Polygon polygonGIS) {
        this.polygonGIS = polygonGIS;
    }

    public ArrayList<Connector> getConns() {
        return conns;
    }

    public void setConns(ArrayList<Connector> conns) {
        this.conns = conns;
    }

    public Polygon2D.Double getPolygon2D() {
        return polygon2D;
    }

    public void setPolygon2D(Polygon2D.Double polygon) {
        this.polygon2D = polygon;
    }

    public ArrayList<AccessPoint> getAPs() {
        return accesspoints;
    }

    public void setAPs(ArrayList<AccessPoint> aps) {
        this.accesspoints = aps;
    }

    public ArrayList<AccessPoint> getAPConnectors() {
        return this.APConnectors;
    }

    public void setAPConnectors(ArrayList<AccessPoint> APConnectors) {
        this.APConnectors = APConnectors;
    }

    public String toString() {
        return this.name;
    }

    public String toString2() {
        return this.itemID + "\t" + this.name + "\t" + this.polygon2D;
    }

    public ArrayList<Partition> getConParts() {
        ArrayList<Partition> heffer = new ArrayList<Partition>();

        for (AccessPoint ap : accesspoints) {
            for (Partition p : ap.getPartitions()) {
                if (p.getItemID() != itemID)
                    heffer.add(p);
            }
        }

        for (Connector connector : conns) {
            for (Partition part : connector.getPartitions()) {
                if (part.getItemID() != itemID) {
                    heffer.add(part);
                }
            }
        }

        return heffer;
    }

    public Partition getOriPart() {
        return oriPart;
    }

    public void setOriPart(Partition oriPart) {
        this.oriPart = oriPart;
    }

    public ArrayList<Partition> getDecParts() {
        return decParts;
    }

    public void setDecParts(ArrayList<Partition> decParts) {
        this.decParts = decParts;
    }

    public double calMBRArea() {
        double minX = this.getPolygon2D().getBounds2D().getMinX();
        double minY = this.getPolygon2D().getBounds2D().getMinY();
        double maxX = this.getPolygon2D().getBounds2D().getMaxX();
        double maxY = this.getPolygon2D().getBounds2D().getMaxY();
        double area = Math.abs(maxX - minX) * Math.abs(maxY - minY);
        return area;
    }

    public Point2D.Double calRandomPointInMBR() {
        double minX = this.getPolygon2D().getBounds2D().getMinX();
        double minY = this.getPolygon2D().getBounds2D().getMinY();
        double maxX = this.getPolygon2D().getBounds2D().getMaxX();
        double maxY = this.getPolygon2D().getBounds2D().getMaxY();
        double randX = Math.random() * (maxX - minX) + minX;
        double randY = Math.random() * (maxY - minY) + minY;
        return new Point2D.Double(randX, randY);
    }
}
