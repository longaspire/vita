package cn.edu.zju.db.datagen.indoorobject.station;

import cn.edu.zju.db.datagen.database.DB_Connection;
import cn.edu.zju.db.datagen.database.DB_WrapperSpatial;
import cn.edu.zju.db.datagen.database.spatialobject.Floor;
import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import cn.edu.zju.db.datagen.indoorobject.IndoorObject;
import cn.edu.zju.db.datagen.indoorobject.utility.IdrObjsUtility;

import java.awt.geom.Point2D;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

abstract public class Station extends IndoorObject implements Runnable {

    // private int id;
    private int type;
    private String stationInfo;
    protected static double scanRange = 20;
    protected static int scanRate = 1000;

    public Station() {

    }

    public Station(Floor floor) {
        // find a random partition and a random point for station
        try {
            currentFloor = floor;
            ArrayList<Partition> partitions = currentFloor.getPartsAfterDecomposed();
            int partIndex = (int) (Math.random() * partitions.size());
            currentPartition = partitions.get(partIndex);
            Connection con = DB_Connection.connectToDatabase("conf/moovework.properties");
            this.currentLocation = DB_WrapperSpatial
                                           .ST_RandomPoints_In_Partition(con, currentPartition.getPolygon2D(), 1).get(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Station(Floor floor, Point2D.Double loc) {
        this.currentLocation = loc;
        this.currentFloor = floor;
        this.currentPartition = IdrObjsUtility.findPartitionForPoint(floor, loc);
    }

    public Station(Floor floor, Point2D.Double loc, Partition partition) {
        this.currentFloor = floor;
        this.currentLocation = loc;
        this.currentPartition = partition;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int ID) {
        this.id = ID;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getStationInfo() {
        return this.stationInfo;
    }

    // floor + partition + type + id
    public void setStationInfo(String info) {
        this.stationInfo = info;
    }

    public static double getScanRange() {
        return scanRange;
    }

    public static void setScanRange(double range) {
        scanRange = range;
    }

    public static int getScanRate() {
        return scanRate;
    }

    public static void setScanRate(int rate) {
        scanRate = rate;
    }

    @Override
    public String toString() {
        return this.getId() + "\t" + this.getCurrentFloor().getItemID().toString() + "\t"
                       + this.getCurrentPartition().getItemID().toString() + "\t" + this.getCurrentLocation().getX() + "\t"
                       + this.getCurrentLocation().getY() + "\n";
    }

    abstract public void getRSSI();

    abstract public void run();

    abstract public Pack createPackage(double distance);

}
