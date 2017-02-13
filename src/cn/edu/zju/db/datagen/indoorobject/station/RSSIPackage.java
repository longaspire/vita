package cn.edu.zju.db.datagen.indoorobject.station;

import java.awt.geom.Point2D;


public class RSSIPackage extends Pack {

    int RSSI;

    public RSSIPackage() {

    }

    public RSSIPackage(int from, long time) {
        super(from, time);
    }

    public RSSIPackage(int from, long time, int rssi) {
        fromID = from;
        timeStamp = time;
        RSSI = rssi;
    }

    public RSSIPackage(double x, double y, int rssi) {
        this.fromLocation = new Point2D.Double(x, y);
        this.RSSI = rssi;
    }

    public RSSIPackage(Point2D.Double fromLoc, int from, long time, int rssi) {
        this.fromLocation = fromLoc;
        this.fromID = from;
        this.timeStamp = time;
        this.RSSI = rssi;
    }

    public int getRSSI() {
        return RSSI;
    }

    public void setRSSI(int rssi) {
        RSSI = rssi;
    }

    @Override
    public String toString() {
        String res = fromID + "\t" + this.fromLocation.getX() + "\t" + this.fromLocation.getY() + "\t" + (int) RSSI;
        return res;
    }

    @Override
    public int compareTo(Object arg0) {
        RSSIPackage temp = (RSSIPackage) arg0;
        if (this.RSSI > temp.RSSI) {
            return -1;
        } else if (this.RSSI == temp.RSSI) {
            return 0;
        } else {
            return 1;
        }
    }


}
