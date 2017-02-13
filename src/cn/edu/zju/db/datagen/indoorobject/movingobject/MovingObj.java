package cn.edu.zju.db.datagen.indoorobject.movingobject;

import cn.edu.zju.db.datagen.database.spatialobject.Building;
import cn.edu.zju.db.datagen.database.spatialobject.Floor;
import cn.edu.zju.db.datagen.indoorobject.IndoorObject;
import cn.edu.zju.db.datagen.indoorobject.station.Pack;
import cn.edu.zju.db.datagen.indoorobject.station.Station;
import cn.edu.zju.db.datagen.indoorobject.utility.IdrObjsUtility;

import java.awt.geom.Point2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;

abstract public class MovingObj extends IndoorObject implements Runnable {

    public static double scanRange = 20;
    protected static int scanRate = 1000;
    protected static double maxStepLength = 0.03;
    protected static int moveRate = 100;
    protected static double maxSpeed = maxStepLength / (moveRate / 1000.0); // default
    // 0.3m/s
    protected BufferedOutputStream buff;
    protected ArrayList<Station> inRangeStations;
    protected ArrayList<Pack> inRangePacks;
    protected int packIndex = 1;
    protected Point2D.Double estimateLocation;
    protected boolean pause = false;
    protected boolean arrived = false;
    protected static boolean trackingFlag = false;
    protected static boolean trajectoryFlag = false;
    protected BufferedOutputStream trajectoryBuff;
    // protected Point2D.Double oldLocation = new Point2D.Double();
    private int moveAroundCount;
    private int lifeSpan;
    private long initMovingTime;
    private boolean active;

    public MovingObj() {

    }

    public static double getScanRange() {
        return scanRange;
    }

    public static void setScanRange(double range) {
        scanRange = range;
    }

    public static double getMaxStepLength() {
        return maxStepLength;
    }

    public static void setMaxStepLength(double length) {
        maxStepLength = length;
        maxSpeed = maxStepLength / (moveRate / 1000);
    }

    public static int getMoveRate() {
        return moveRate;
    }

    public static void setMoveRate(int rate) {
        moveRate = rate;
        maxSpeed = maxStepLength / (moveRate / 1000);
    }

    public static double getMaxSpeed() {
        return MovingObj.maxSpeed;
    }

    public static void setMaxSpeed(double maxSpeed) {
        MovingObj.maxSpeed = maxSpeed;
    }

    public ArrayList<Pack> getPackages() {
        return this.inRangePacks;
    }

    public void setPackages(ArrayList<Pack> packs) {
        this.inRangePacks = packs;
    }

    public abstract void moveOneStep();

    public void calRSSI() {
        inRangeStations = findAllStations();
        inRangePacks = calRSSIPackagesFromStations(inRangeStations);
    }

    public long getInitMovingTime() {
        return initMovingTime;
    }

    public void setInitMovingTime(long initMovingTime) {
        this.initMovingTime = initMovingTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public abstract void run();

    public boolean getPauseFlag() {
        return pause;
    }

    public void setPauseFlag(boolean flag) {
        pause = flag;
    }

    public void changeFlag() {
        pause = !pause;
    }

    public boolean isArrived() {
        return this.arrived;
    }

    public void setArrived(boolean flag) {
        arrived = flag;
    }

    public static boolean getTrajectoryFlag() {
        return trajectoryFlag;
    }

    public static void setTrajectoryFlag(boolean flag) {
        trajectoryFlag = flag;
    }

    public static boolean getTrackingFlag() {
        return trackingFlag;
    }

    public static void setTrackingFlag(boolean flag) {
        trackingFlag = flag;
    }

    public int getMoveAroundCount() {
        return moveAroundCount;
    }

    public void setMoveAroundCount(int moveAroundCount) {
        this.moveAroundCount = moveAroundCount;
    }

    public int getLifeSpan() {
        return lifeSpan;
    }

    public void setLifeSpan(int lifeSpan) {
        this.lifeSpan = lifeSpan;
    }

    public synchronized void hangThread() throws InterruptedException {
        wait();
    }

    public synchronized void resumeThread() {
        notifyAll();
    }

    public ArrayList<Station> findAllStations() {
        ArrayList<Station> stations = new ArrayList<Station>();
        findStationsInFloor(this.getCurrentFloor(), stations, scanRange);

        double range = Math.sqrt((Math.pow(scanRange, 2)) - Math.pow(Building.floorHeight, 2));
        for (Floor floor : this.getCurrentFloor().getConFloors()) {
            findStationsInFloor(floor, stations, range);
        }
        return stations;
    }

    private void findStationsInFloor(Floor floor, ArrayList<Station> stations, double range) {
        double xmin = this.getCurrentLocation().getX() - range;
        double ymin = this.getCurrentLocation().getY() - range;
        double xmax = this.getCurrentLocation().getX() + range;
        double ymax = this.getCurrentLocation().getY() + range;
        for (Station station : floor.getStationsRTree().find(xmin, ymin, xmax, ymax)) {
            if (currentLocation.distance(station.getCurrentLocation()) <= range) {
                stations.add(station);
            }
        }
    }

    protected void createFile() {
        try {
            File file = new File(getFileName());
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileOutputStream outStr = new FileOutputStream(file);
            buff = new BufferedOutputStream(outStr);
            String comments = "deviceId" + "\t" + "device_location_x" + "\t" + "device_location_y" + "\t"
                                      + "rssi_measurement" + "\t" + "timeStamp" + "\n";
            buff.write(comments.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void createTrajectoryFile() {
        try {
            File file = new File(getTrajectoryFileName());
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileOutputStream outStr = new FileOutputStream(file);
            trajectoryBuff = new BufferedOutputStream(outStr);
            String comments = "floorId" + "\t" + "partitionId" + "\t" + "location_x" + "\t" + "location_y" + "\t"
                                      + "timeStamp" + "\n";
            trajectoryBuff.write(comments.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // file name is classname_id_date+time.txt
    public abstract String getFileName();

    public abstract String getTrajectoryFileName();

    // pattern is floor_id + partition_id + current_loc(x,y)+ time
    public void writeTrajectory() {
        try {
            Date timestamp = new Date(IdrObjsUtility.objectGenerateStartTime.getTime()
                                              + (System.currentTimeMillis() - IdrObjsUtility.startClickedTime.getTime()));
            String timestamp_str = IdrObjsUtility.sdf.format(timestamp);
            String traj = getCurrentFloor().getItemID() + "\t" + getCurrentPartition().getItemID() + "\t" + getCurrentLocation().getX()
                                  + "\t" + getCurrentLocation().getY() + "\t" + timestamp_str + "\n";
            trajectoryBuff.write(traj.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeRSSI() {
        // writeCurrentStates();
        writeRSSIPackages(inRangePacks);
    }

    // public void writeCurrentStates(){
    // try{
    // buff.write((currentLocation.getX() + " " +
    // currentLocation.getY()).getBytes());
    // buff.write((" " + System.currentTimeMillis() + "\n").getBytes());
    // } catch (IOException e){
    // e.printStackTrace();
    // }
    // }

    public ArrayList<Pack> calRSSIPackagesFromStations(ArrayList<Station> stations) {
        ArrayList<Pack> packs = new ArrayList<Pack>();
        for (Station station : inRangeStations) {
            double distance = calDist2Station(station);
            Pack pack = station.createPackage(distance);
            packs.add(pack);
        }
        return packs;
    }

    private double calDist2Station(Station station) {
        double distance;
        if (station.getCurrentFloor() == this.getCurrentFloor()) {
            distance = station.getCurrentLocation().distance(this.getCurrentLocation());
        } else {
            double tmp = Math.pow(station.getCurrentLocation().getX() - this.getCurrentLocation().getX(), 2)
                                 + Math.pow(station.getCurrentLocation().getY() - this.getCurrentLocation().getY(), 2)
                                 + Math.pow(Building.floorHeight, 2);
            distance = Math.sqrt(tmp);
        }
        return distance;
    }

    // record pattern is record_index, station_location(x, y), station_id,
    // timestamp, rssi
    public void writeRSSIPackages(ArrayList<Pack> packs) {
        try {
            for (Pack pack : packs) {
                Date timestamp = new Date(IdrObjsUtility.objectGenerateStartTime.getTime()
                                                  + (System.currentTimeMillis() - IdrObjsUtility.startClickedTime.getTime()));
                String timestamp_str = IdrObjsUtility.sdf.format(timestamp);
                String packInfo = pack.toString() + "\t" + timestamp_str + "\n";
                buff.write(packInfo.getBytes());
                packIndex++;
            }
            buff.write("\n".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void finWrite() {
        try {
            buff.flush();
            buff.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void finTrajWrite() {
        try {
            trajectoryBuff.flush();
            trajectoryBuff.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
