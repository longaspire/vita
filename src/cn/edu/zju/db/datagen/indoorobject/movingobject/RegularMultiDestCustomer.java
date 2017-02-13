package cn.edu.zju.db.datagen.indoorobject.movingobject;


import cn.edu.zju.db.datagen.database.DB_WrapperLoad;
import cn.edu.zju.db.datagen.database.spatialobject.Floor;
import cn.edu.zju.db.datagen.database.spatialobject.Partition;

import java.awt.geom.Point2D;
import java.util.*;

public class RegularMultiDestCustomer extends DstMovingObj {

    protected boolean finished = false;
    private List<Destination> destinations;
    private Destination curDestination;
    private int maxInDestinationCount;
    private int minInDestinationCount;

    public RegularMultiDestCustomer() {
    }

    public RegularMultiDestCustomer(Floor floor, Point2D.Double location) {
        super(floor, location);
    }

    public RegularMultiDestCustomer(Floor floor, Partition partition, Point2D.Double location) {
        super(floor, partition, location);
    }

    public RegularMultiDestCustomer(Floor floor, Partition partition, ArrayList<Destination> destinations) {
        this.currentFloor = floor;
        this.currentPartition = partition;
        this.destinations = destinations;
    }

    public Destination getCurDestination() {
        return this.curDestination;
    }

    public void setCurDestination(Destination dest) {
        this.curDestination = dest;
        this.setCurDestFloor(dest.getDestFloor());
        this.setCurDestPartition(dest.getDestPart());
        this.setCurDestPoint(dest.getDestLocation());
    }

    public List<Destination> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<Destination> destinations) {
        this.destinations = destinations;
    }

    public int getMinInDestinationCount() {
        return minInDestinationCount;
    }

    public void setMinInDestinationCount(int minInDestinationCount) {
        this.minInDestinationCount = minInDestinationCount;
    }

    public int getMaxInDestinationCount() {
        return maxInDestinationCount;
    }

    public void setMaxInDestinationCount(int maxInDestinationCount) {
        this.maxInDestinationCount = maxInDestinationCount;
    }

    @Override
    public void run() {

        moveByTimer();
        if (trackingFlag) {
            createFile();
            calWriteRSSIByTimer();
        }

        if (trajectoryFlag) {
            createTrajectoryFile();
            writeTrajectoryByTimer();
        }

    }


    public void genMultiDestinations() {
//		int destCount = getMinDestinationNumber() + (int) ((getMaxDestinationNumber() - getMinDestinationNumber()) * Math.random());
        destinations = new ArrayList<>();
        int destCount = (int) (5 * (Math.random() + 1));
        for (int i = 0; i < destCount; i++) {
            Destination destination = genDestinationByLevel();
            destinations.add(destination);
        }
        Collections.sort(destinations);
    }

    private Destination genDestinationByLevel() {
        Partition destPartition = DB_WrapperLoad.partitionDecomposedT.get((int) (getCurrentFloor().getPartitions().size() * Math.random()));
        Point2D.Double destPoint = destPartition.calRandomPointInMBR();
        return new Destination(destPartition.getFloor(), destPartition, destPoint);
    }

    @Override
    void moveByTimer() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int destinationIndex = 0;
            int inDestinationCount = 0;
            int totalInDestinationCount;
            boolean pathGened = false;

            @Override
            public void run() {
                if (pause) {
                    try {
                        hangThread();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(finished) {
                    this.cancel();
                }

                if(destinationIndex >= destinations.size()) {
                    finished = true;
                    System.out.println("id " + " is finished");
                    this.cancel();
                    return;
                }

                if (arrived) {
                    if (destinationIndex < destinations.size()) {
                        if (inDestinationCount < totalInDestinationCount) {
                            moveInPartition();
                            inDestinationCount++;
                        } else {
                            totalInDestinationCount = 0;
                            destinationIndex++;
                            pathGened = false;
                            arrived = false;
                        }
                    } else {
                        System.out.println(id + " is finished");
                        finished = true;
                        this.cancel();
                        return;
                    }
                } else {
                    if (!pathGened) {
                        setCurDestination(destinations.get(destinationIndex));
                        arrived = false;
                        if (curDestPartition == currentPartition) {
                            System.out.println(id + " to " + getCurDestPoint() + " is straight");
                            arrived = false;
                            pathGened = true;
                        } else if (!generatePath2Dest()) {
                            arrived = true;
                            pathGened = false;
                            System.out.println(id + "'s destination cannot reach! " + curDestination);
                            inDestinationCount = 0;
                            totalInDestinationCount = (int) (300 * Math.random());
//                            destinationIndex++;
                        } else {
                            pathGened = true;
                        }
                    } else if (curPath.size() == 0 && maxSpeed > currentLocation.distance(curDestPoint)) {
                        System.out.println(id + " Got destination");
                        setLocation(curDestPoint);
                        setCurrentPartition(getCurDestPartition());
                        setCurrentFloor(getCurDestFloor());
                        inDestinationCount = 0;
                        totalInDestinationCount = (int) (300 * Math.random());
                        arrived = true;
                    } else {
                        moveOneStep();
                    }
                }


            }
        }, 0, moveRate);

    }


    /*
        在timer中模拟移动每一步，如果当前的stay count还没有结束，那么就在原点附近区域移动，等待计时结束
        如果当前是在移动状态，那么有0.02的可能性会在该点停留random个30个单位时间
        否则按照路线移动moveInLine
        */
    @Override
    public void moveOneStep() {

        if (stayCount > 0) {
            stayCount--;
            moveInPartition();
            return;
        }
        if (Math.random() > 0.98) {
            stayCount = (int) (Math.random() * getMoveAroundCount());
//			System.out.println("Count is " + count);
            return;
        }
        moveInLine(curPath, getMaxSpeed() * (Math.random() * 0.5 + 0.5));
    }

    private void moveInPartition() {
        double radius = getMaxSpeed() * Math.random();
        double angle = 2 * Math.PI * Math.random();
        Point2D.Double randomPoint = calNearPoint(radius, angle);
        int count = 0;
        while (!getCurrentPartition().getPolygon2D().contains(randomPoint)) {
            radius = getMaxSpeed() * Math.random();
            angle = 2 * Math.PI * Math.random();
            randomPoint = calNearPoint(radius, angle);
            if (++count > 10) {
                return;
            }
        }
        this.setLocation(randomPoint);
    }

    private Point2D.Double calNearPoint(double radius, double angle) {
        double newX = this.getCurrentLocation().getX() + radius * Math.cos(angle);
        double newY = this.getCurrentLocation().getY() + radius * Math.sin(angle);
        return new Point2D.Double(newX, newY);
    }


    //get RSSI every 1000ms
    @Override
    void calWriteRSSIByTimer() {


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

                if (finished) {
                    finWrite();
                    this.cancel();
                    System.out.println(id + " finished writing rssi");
                    return;
                } else {
                    calRSSI();
                    writeRSSI();
                }
            }

        }, 0, scanRate);

    }

    @Override
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

                if (finished) {
                    finTrajWrite();
                    this.cancel();
                    System.out.println(id + " finished writing trajectory");
                    return;
                } else {
                    writeTrajectory();
                }
            }

        }, 0, moveRate);

    }

    public boolean isFinished() {
        return this.finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
