package cn.edu.zju.db.datagen.indoorobject.station;

import java.awt.geom.Point2D;

public abstract class Pack implements Comparable<Object> {

    Point2D.Double fromLocation;
    int fromID;            //sender's ID
    long timeStamp;

    public Pack() {

    }

    public Pack(int from, long time) {
        fromID = from;
        timeStamp = time;
    }

    public int getFromID() {
        return fromID;
    }

    public void setFromID(int ID) {
        fromID = ID;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long time) {
        timeStamp = time;
    }

    public Point2D.Double getFromLocation() {
        return this.fromLocation;
    }

    public void setFromLocation(Point2D.Double fromLoc) {
        this.fromLocation = fromLoc;
    }

    public abstract String toString();

    public abstract int compareTo(Object arg0);

}
