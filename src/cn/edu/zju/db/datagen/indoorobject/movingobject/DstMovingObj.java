package cn.edu.zju.db.datagen.indoorobject.movingobject;

import cn.edu.zju.db.datagen.database.DB_WrapperLoad;
import cn.edu.zju.db.datagen.database.spatialobject.AccessPoint;
import cn.edu.zju.db.datagen.database.spatialobject.Floor;
import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import cn.edu.zju.db.datagen.indoorobject.utility.IdrObjsUtility;
import cn.edu.zju.db.datagen.spatialgraph.D2DGraph;
import cn.edu.zju.db.datagen.spatialgraph.NoSuchDoorException;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DstMovingObj extends MovingObj {

    Point2D.Double curDestPoint;
    Floor curDestFloor;
    Partition curDestPartition;
    ArrayList<AccessPoint> curPath = new ArrayList<AccessPoint>();
    ArrayList<Point2D.Double> curPathPoints = new ArrayList<Point2D.Double>();
    int moveStepNum = 1;
    //	private boolean arrived = false;
//	private boolean pause = false;
    int stayCount = 0;
    //	private boolean arrived = false;
//	private boolean pause = false;

    public DstMovingObj() {

    }

    public DstMovingObj(Floor floor, Point2D.Double location) {
        this.currentFloor = floor;
        this.currentLocation = location;
    }

    public DstMovingObj(Floor floor, Partition partition, Point2D.Double location) {
        this.currentFloor = floor;
        this.currentPartition = partition;
        this.currentLocation = location;
    }

    public DstMovingObj(Floor floor, Partition partition, Point2D.Double location,
                        Floor curDestFloor, Partition destPart, Point2D.Double curDestPoint) {
        this.currentFloor = floor;
        this.currentPartition = partition;
        this.currentLocation = location;
        this.curDestFloor = curDestFloor;
        this.curDestPartition = destPart;
        this.curDestPoint = curDestPoint;
    }

    public Point2D.Double getCurDestPoint() {
        return this.curDestPoint;
    }

    public void setCurDestPoint(Point2D.Double dest) {
        this.curDestPoint = dest;
    }

    public Floor getCurDestFloor() {
        return this.curDestFloor;
    }

    public void setCurDestFloor(Floor floor) {
        this.curDestFloor = floor;
    }

    public Partition getCurDestPartition() {
        return this.curDestPartition;
    }

    public void setCurDestPartition(Partition part) {
        this.curDestPartition = part;
    }

    public ArrayList<AccessPoint> getCurPath() {
        return curPath;
    }

    public void setCurPath(ArrayList<AccessPoint> pathList) {
        curPath = pathList;
    }

    public void genRandomDest() {
        ArrayList<Partition> partitions = DB_WrapperLoad.partitionDecomposedT;
        Partition destPart = partitions.get((int) (Math.random() * partitions.size()));
        Floor destFloor = destPart.getFloor();
        Point2D.Double destCenter = destPart.calRandomPointInMBR();
//		Point2D.Double destCenter = getAvgPointInPartition(destPart);
        this.setCurDestFloor(destFloor);
        this.setCurDestPartition(destPart);
        this.setCurDestPoint(destCenter);
        System.out.println(id + " is on partition " + IdrObjsUtility.findPartitionForPoint(this.getCurrentFloor(), this.getCurrentLocation()));
        System.out.println(id + "'s " + "destination is " + destPart + "\n");
    }


    /* thread of a destination moving object
     * find partition of destination, and see if it can be reached
     * move and record its around stations and RSSI
     * */
    @Override
    public void run() {

		/* find destination partition, if they are in the same part, there no need to generate curPath
		 * if generation failed(distance to destination is max), return*/
//		Partition curDestPartition = IndoorObjsUtility.findPartitionForPoint(currentFloor, curDestPoint);
        if (curDestPoint == null) {
            genRandomDest();
        }
        arrived = false;
        if (curDestPartition == currentPartition) {
            System.out.println(id + " to " + this.getCurDestPoint() + " is straight");
        } else if (!generatePath2Dest()) {
            arrived = true;
            System.out.println(id + "'s destination cannot reach!");
            return;
        }

        //if the destination cannot be reached, it won't create a trajectory file for it.
        moveByTimer();
        if (trackingFlag) {
            createFile();
            calWriteRSSIByTimer();
        }

        if (trajectoryFlag) {
            createTrajectoryFile();
            writeTrajectoryByTimer();
        }

        System.out.println(id + " has end");

    }

    /* generateMIWD return minimum indoor walking distance(through partitions and doors)
     * between this and destination. accessPoints array - store start and end access points,
     * if MIWD is MAX, destination cannot be reached, return false
     * else generate curPath between start door and end door
     * */
    boolean generatePath2Dest() {
        this.getCurPath().clear();

        AccessPoint[] accessPoints = new AccessPoint[2];
        double miwd = this.generateMIWD(this.getCurDestFloor(), curDestPoint, accessPoints);
        if (miwd >= D2DGraph.MAX_DISTANCE) {
            return false;
        }
        if (accessPoints[0] == null || accessPoints[1] == null) {
            return false;
        }

        try {
            this.getCurrentFloor().getD2dGraph().getPath(curPath, accessPoints[0], accessPoints[1]);
        } catch (NoSuchDoorException e) {
            e.printStackTrace();
        }
        curPath.add(0, accessPoints[0]);
        curPath.add(curPath.size(), accessPoints[1]);

        generatePathPoints(curPath);
        return true;

    }

    void generatePathPoints(ArrayList<AccessPoint> accessPoints) {
        curPathPoints.clear();
        for (AccessPoint ap : accessPoints) {
            if (ap.getApType() == 0 || ap.getApType() == 2) {
                Line2D.Double apLine = ap.getLine2D();
                double len = Math.random() * apLine.getP1().distance(apLine.getP2());
                Point2D.Double tmpDest = getLocationInLine((Point2D.Double) apLine.getP1(), (Point2D.Double) apLine.getP2(), len);
                curPathPoints.add(tmpDest);
            } else {
                curPathPoints.add(ap.getLocation2D());
            }
        }
    }


    //move one step every 100ms, stop when it arrives destination
    void moveByTimer() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                if (arrived == true) {
                    System.out.println(id + " is killed");
                    this.cancel();
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

                //change destination test;
                if (moveStepNum > 400 && Math.random() * 10 > 8) {
                    moveStepNum = 0;
                    genRandomDest();
                    System.out.println(id + " Chage Dest.........");
                    if (curDestPartition == currentPartition) {
                        System.out.println("Straight");
                    } else if (generatePath2Dest() == false) {
                        arrived = true;
                        System.out.println(id + "'s destination cannot reach!");
                        this.cancel();
                        return;
                    }
                }

                //if they are in the same partition and distance is smaller to speed, arrived
                if (curPath.size() == 0 && maxSpeed > currentLocation.distance(curDestPoint)) {
                    System.out.println(id + " Got destination");
                    setLocation(curDestPoint);
                    arrived = true;
                    this.cancel();
                    return;
                }



                moveOneStep();
            }

        }, 0, moveRate);

    }


    @Override
    public void moveOneStep() {

        if (stayCount > 0) {
            stayCount--;
            return;
        }
        if (Math.random() > 0.98) {
            stayCount = (int) (Math.random() * getMoveAroundCount());
//			System.out.println("Count is " + count);
            return;
        }
        moveInLine(curPath, maxSpeed * (Math.random() * 0.5 + 0.5));
    }

    /* move one step on the curPath, the recursion method
     * accessPoints--doors need to pass through, range--remain range can walk in this time
     * 1.get next turning point on the curPath
     * 2.if it cannot be reached(range is too small), set the new location and return
     * 3.else pass the turning point, and set new location and partition
     * 	 and move on, delete the turning point, range is altered to range-distance2turningPoint
     * */
    void moveInLine(ArrayList<AccessPoint> accessPoints, double range) {
//		System.out.println(moveStepNum++);		
        moveStepNum++;
        Point2D.Double nextDest = getNextDest(accessPoints);
        double dist2NextLoc = this.currentLocation.distance(nextDest);
        if (range < dist2NextLoc || accessPoints.size() == 0) {
            Point2D.Double nextLocation = getLocationInLine(this.getCurrentLocation(), nextDest, range);
            this.setLocation(nextLocation);
            return;
        } else {
            AccessPoint passedAP = accessPoints.remove(0);
            curPathPoints.remove(0);
//            System.out.println(id + " is on " + getCurrentPartition());
            this.setLocation(nextDest);
            if (accessPoints.size() > 0) {
                this.setCurrentPartition(accessPoints.get(0).getPartitions().get(0));
            } else {
                this.setCurrentPartition(getAdjacentPartition(passedAP, this.getCurrentPartition()));
            }

            if (passedAP.getApType() == 4) {
                System.out.println(id + " Change Floor!");
                if (this.currentPartition == null) {
                    return;
                }
                this.setCurrentFloor(this.currentPartition.getFloor());
            }
            moveInLine(accessPoints, range - dist2NextLoc);
        }
    }


    //if there are still doors, the first door's location is next destination, else curDestPoint is
    Point2D.Double getNextDest(List<AccessPoint> accessPoints) {
        if (accessPoints.size() > 0) {
            return curPathPoints.get(0);
//            //if there are still access points to get through
//            if (accessPoints.get(0).getApType() == 0 || accessPoints.get(0).getApType() == 2) {
//                Line2D.Double apLine = accessPoints.get(0).getLine2D();
//                double len = Math.random() * apLine.getP1().distance(apLine.getP2());
//                return getLocationInLine((Point2D.Double) apLine.getP1(), (Point2D.Double) apLine.getP2(), len);
//            } else {
//                return accessPoints.get(0).getLocation2D();
//            }
        } else {
            return curDestPoint;
        }
    }

    //get a point on the line(start to end), and distance to start is range
    private Point2D.Double getLocationInLine(Point2D.Double start, Point2D.Double end, double range) {
        double deltaY = end.getY() - start.getY();
        double deltaX = end.getX() - start.getX();
        double gradient = range / (start.distance(end));
        double newY = start.getY() + gradient * deltaY;
        double newX = start.getX() + gradient * deltaX;
        return new Point2D.Double(newX, newY);
    }

    private Partition getAdjacentPartition(AccessPoint AP, Partition partition) {
        for (Partition part : AP.getPartitions()) {
            if (part != partition) {
                return part;
            }
        }
        System.out.println("Single Partition Door");
        return partition;
    }

    //get RSSI every 1000ms
    void calWriteRSSIByTimer() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                if (pause) {
                    try {
                        hangThread();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

//				while(pause == true){
//					;
//				}
                if (arrived) {
                    finWrite();
                    return;
                } else {
                    calRSSI();
                    writeRSSI();
                }
            }

        }, 0, scanRate);

    }


    @Override
    public String getFileName() {
        String outputPath = IdrObjsUtility.outputDir;
        outputPath = IdrObjsUtility.rssiDir + "//Dest_RSSI_" + id + ".txt";
        return outputPath;
    }

    @Override
    public String getTrajectoryFileName() {
        String outputPath = IdrObjsUtility.outputDir;
        outputPath = IdrObjsUtility.trajDir + "//Dest_Traj_" + id + ".txt";
        return outputPath;
    }

    void writeTrajectoryByTimer() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                if (pause) {
                    try {
                        hangThread();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (arrived) {
                    finTrajWrite();
                    return;
                } else {
                    writeTrajectory();
                }
            }

        }, 0, moveRate);

    }


}
