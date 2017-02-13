package cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class Door {

    private String globalID = null;
    private String name = null;
    private Point2D.Double finalCoord = new Point2D.Double();
    private String floorID = null;
    private Line2D.Double repLine = new Line2D.Double();

    // Empty constructor
    public Door() {
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

    public Point2D.Double getFinalCoord() {
        return finalCoord;
    }

    public void setFinalCoord(Point2D.Double finalCoord) {
        this.finalCoord = finalCoord;
    }

    public String getFloorID() {
        return floorID;
    }

    public void setFloorID(String floorID) {
        this.floorID = floorID;
    }

    public Line2D.Double getRepLine() {
        return repLine;
    }

    public void setRepLine(Line2D.Double repLine) {
        this.repLine = repLine;
    }
}
