package cn.edu.zju.db.datagen.algorithm;

import cn.edu.zju.db.datagen.indoorobject.utility.IdrObjsUtility;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

public class ReferencePoint {

    private int rp_id;

    private Point2D.Double location;

    public ReferencePoint(Double location) {
        super();
        this.rp_id = ++IdrObjsUtility.rp_id_count;
        this.location = location;
    }

    public ReferencePoint(int rp_id, Double location) {
        super();
        this.rp_id = rp_id;
        this.location = location;
    }

    public int getRp_id() {
        return rp_id;
    }

    public void setRp_id(int rp_id) {
        this.rp_id = rp_id;
    }

    public Point2D.Double getLocation() {
        return location;
    }

    public void setLocation(Point2D.Double location) {
        this.location = location;
    }


}
