package cn.edu.zju.db.datagen.indoorobject.movingobject;

import cn.edu.zju.db.datagen.database.DB_WrapperLoad;
import cn.edu.zju.db.datagen.database.spatialobject.AccessPoint;
import cn.edu.zju.db.datagen.database.spatialobject.Connector;
import cn.edu.zju.db.datagen.database.spatialobject.Floor;
import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import cn.edu.zju.db.datagen.indoorobject.utility.IdrObjsUtility;

import java.awt.geom.Point2D;
import java.util.Timer;
import java.util.TimerTask;

public class RandomMovingObj extends MovingObj {

    private static final double outThreshold = 2;    //if distance to ap is less than it, it may go to another part
    protected boolean finished = false;
    private int lifeCycle = 50 + (int) (50 * Math.random());    //lifecycle is between 50 to 100

    public RandomMovingObj() {

    }

    public RandomMovingObj(Floor floor, Point2D.Double point) {
        this.currentFloor = floor;
        this.currentLocation = point;
    }

    public RandomMovingObj(Floor floor, Point2D.Double point, Partition partition, double maxSpeed) {
        this.currentLocation = point;
        this.currentPartition = partition;
        this.currentFloor = floor;
        MovingObj.maxSpeed = maxSpeed;
    }

    @Override
    public void run() {
        createFile();
        moveByTimer();
        getRSSIByTimer();
        if (arrived == true) {
            System.out.println(this.id + "Dead.");
        }
    }


    private void moveByTimer() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                if (lifeCycle < 0) {
                    return;
                }

                if (pause == true) {
                    try {
                        hangThread();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if (arrived == true) {
                    this.cancel();
                    return;
                }

                moveOneStep();
            }

        }, 0, moveRate);

    }


    //after moving one step, if the new location is close to an access point
    //there is a chance that it will move in a new partition
    @Override
    public void moveOneStep() {
        Partition currentPart = this.getCurrentPartition();
        if (currentPart == null)
            return;
        moveInPartition(currentPart);
        tryToMoveOut(currentPart);
    }

    //move to a new location in the same partition
    //if it still cannot find a destination after 10 times, stay where it is this time
    public void moveInPartition(Partition currentPart) {
        double radius = Math.random() * MovingObj.maxSpeed;    //move range
        double angle = Math.random() * 2 * Math.PI;            //move angle
        Point2D.Double randomPoint = getRandomPoint(radius, angle);
        int count = 0;
        while (currentPart.getPolygon2D().contains(randomPoint) == false) {
            randomPoint = getRandomPoint(radius, angle);
            if (++count > 10)
                return;
        }
        this.setLocation(randomPoint);
    }

    private Point2D.Double getRandomPoint(double radius, double angle) {        //can set an opposite, to be modified
        double newX = this.getCurrentLocation().getX() + radius * Math.cos(angle);
        double newY = this.getCurrentLocation().getY() + radius * Math.sin(angle);
        return new Point2D.Double(newX, newY);
    }

    //only in a range can move out, try to move out and set the new partition and new location
    //modify to make it move between floors later on
    private boolean tryToMoveOut(Partition currentPart) {
        for (Connector connector : DB_WrapperLoad.connectorT) {
            if (connector.getUpperFloor() == this.getCurrentFloor()) {
                double distance2Con = this.getCurrentLocation().distance(connector.getLocation2D());
                moveOut(connector, distance2Con);
            }
        }
        for (AccessPoint ap : currentPart.getAPConnectors()) {
            double distance2AP = this.getCurrentLocation().distance(ap.getLocation2D());
            moveOut(ap, distance2AP);
        }
        return false;
    }

    private boolean moveOut(AccessPoint ap, double dist) {
        double distance2AP = this.getCurrentLocation().distance(ap.getLocation2D());
        if (distance2AP < outThreshold) {
            if (moveOutRandomly(distance2AP) == true) {
                Partition anoPartition = IdrObjsUtility.getAdjacentPartition(ap, this.getCurrentPartition());
                //this door may connect to outdoor
                if (anoPartition == null) {
                    return false;
                }
                System.out.print(this.id + " Has move from partition " + this.getCurrentPartition().getName());
                System.out.println(" to " + anoPartition.getName());
                this.setCurrentPartition(anoPartition);
                moveInPartition(this.getCurrentPartition());
                if (ap.getApType() == 4) {
                    System.out.println("Move to another floor");
                    this.setCurrentFloor(getCurrentPartition().getFloor());
                }
                return true;
            }
        }
        return false;
    }

    //smaller the distance or random is, more it is to move out
    private boolean moveOutRandomly(double dist) {
        double out = Math.random() * dist;
        if (out < 0.8 * 1.8)
            return true;
        return false;
    }

    private void getRSSIByTimer() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                if (lifeCycle < 0) {
                    finWrite();
                    return;
                } else {
                    lifeCycle--;
                    calRSSI();
                    writeRSSI();
                }
            }

        }, 0, scanRate);
    }

    @Override
    public String getFileName() {
        String outputPath = IdrObjsUtility.outputDir;
        outputPath = IdrObjsUtility.rssiDir + "//Random_RSSI_" + id + ".txt";
        return outputPath;
    }

    @Override
    public String getTrajectoryFileName() {
        String outputPath = IdrObjsUtility.outputDir;
        outputPath = IdrObjsUtility.trajDir + "//Random_Traj_" + id + ".txt";
        return outputPath;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
