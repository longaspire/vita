package cn.edu.zju.db.datagen.indoorobject.movingobject;


import cn.edu.zju.db.datagen.database.spatialobject.Floor;
import cn.edu.zju.db.datagen.database.spatialobject.Partition;

import java.awt.geom.Point2D.Double;

public class Destination implements Comparable<Object> {

    private Floor destFloor;
    private Partition destPart;
    private Double destLocation;

    public Destination() {
        // TODO Auto-generated constructor stub
    }

    public Destination(Floor destFloor, Double destLocation) {
        super();
        this.destFloor = destFloor;
        this.destLocation = destLocation;
    }

    public Destination(Floor destFloor, Partition destPart, Double destLocation) {
        super();
        this.destFloor = destFloor;
        this.destPart = destPart;
        this.destLocation = destLocation;
    }

    public Floor getDestFloor() {
        return destFloor;
    }

    public void setDestFloor(Floor destFloor) {
        this.destFloor = destFloor;
    }

    public Double getDestLocation() {
        return destLocation;
    }

    public void setDestLocation(Double destLocation) {
        this.destLocation = destLocation;
    }

    public Partition getDestPart() {
        return destPart;
    }

    public void setDestPart(Partition destPart) {
        this.destPart = destPart;
    }

    @Override
    public String toString() {
        return "Destination [destFloor=" + destFloor + ", destPart=" + destPart + ", destLocation=" + destLocation
                       + "]";
    }


    @Override
    public int compareTo(Object o) {
        Destination another = (Destination) o;
        String curFloorName = this.getDestFloor().getBuilding().getName() + this.getDestFloor().getName();
        String anoFloorName = another.getDestFloor().getBuilding().getName() + another.getDestFloor().getName();
        return curFloorName.compareTo(anoFloorName);
    }
}
