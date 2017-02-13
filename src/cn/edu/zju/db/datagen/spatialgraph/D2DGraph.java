package cn.edu.zju.db.datagen.spatialgraph;

import cn.edu.zju.db.datagen.database.spatialobject.AccessPoint;
import cn.edu.zju.db.datagen.database.spatialobject.Floor;
import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import cn.edu.zju.db.datagen.indoorobject.utility.SpatialHandler;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class D2DGraph {

    private int doorCount = 4000;
    public static final double MAX_DISTANCE = 100000;

    private double distance[][];
    private int path[][];// path[i][j] to store the id of path from point i to point j

    private List<Partition> partitions;
    private List<AccessPoint> aps;

    public D2DGraph(List<Partition> partitions, List<AccessPoint> aps) {
        this.partitions = partitions;
        this.aps = aps;
        doorCount = aps.size() + 100;
    }

    public List<AccessPoint> getAps() {
        return aps;
    }

    public void setAps(List<AccessPoint> aps) {
        this.aps = aps;
    }

    public List<Partition> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<Partition> partitions) {
        this.partitions = partitions;
    }

    public int[][] getPath() {
        return path;
    }

    public void setPath(int[][] path) {
        this.path = path;
    }

    public double[][] getDistance() {
        return distance;
    }

    public void setDistance(double[][] distance) {
        this.distance = distance;
    }

    // floyd to get distance between any two access points
    public void generateD2DDistance() {
        distance = new double[doorCount][doorCount];
        initlizeDistance();
        initlizePath();
        for (int k = 0; k < doorCount; k++) {
            for (int i = 0; i < doorCount; i++) {
                for (int j = 0; j < doorCount; j++) {
                    if (distance[i][k] != MAX_DISTANCE && distance[k][j] != MAX_DISTANCE
                                && distance[i][k] + distance[k][j] < distance[i][j]) {
                        distance[i][j] = distance[i][k] + distance[k][j];
                        path[i][j] = k;
                    }
                }
            }
        }
    }


    //use shortest distance first, can be modified to make it hard to pass narrow door!
    private void initlizeDistance() {
        for (int i = 0; i < doorCount; i++) {
            for (int j = 0; j < doorCount; j++) {
                distance[i][j] = MAX_DISTANCE;
            }
        }
        for (int i = 0; i < aps.size(); i++) {
            AccessPoint ap1 = aps.get(i);
            List<Partition> ap1Partitions = ap1.getPartitions();
            for (int j = 0; j < ap1Partitions.size(); j++) {
                Partition ap1Partition = ap1Partitions.get(j);
                List<AccessPoint> ap1APs = ap1Partition.getAPs();
                for (int k = 0; k < ap1APs.size(); k++) {
                    AccessPoint ap2 = ap1APs.get(k);
                    int id1 = i;
                    int id2 = aps.indexOf(ap2);

                    if (id1 != id2) {
                        distance[id1][id2] = ap1.getLocation2D().distance(ap2.getLocation2D());
                        distance[id2][id1] = distance[id1][id2];

                    } else {
                        distance[id1][id2] = 0;
                        distance[id2][id1] = 0;
                    }
                }
            }
        }
    }


    /*
     * if two access points are adjacent, the distance is ap1.distance(ap2),
     * otherwise is max ap1.distance(ap2) should take obstacles into
     * consideration, it may not be a line use a much more brute method, if
     * there is not a straight line, follow the bounds of partition
     */
    private void initlizePath() {
        path = new int[doorCount][doorCount];
        for (int i = 0; i < doorCount; i++) {
            for (int j = 0; j < doorCount; j++) {
                path[i][j] = -1;
            }
        }
    }

    public double D2DDistance(AccessPoint ap1, AccessPoint ap2) {
        int id1 = aps.indexOf(ap1);
        int id2 = aps.indexOf(ap2);
        if (id1 == -1 || id2 == -1) {
            return D2DGraph.MAX_DISTANCE;
        }
        return distance[aps.indexOf(ap1)][aps.indexOf(ap2)];
    }

    /*
     * flag = 1 -- same region flag = 2 -- adjacent region flag = 3 -- reachable
     * region flag = -1 -- unreachable region flag = -2 -- outside point, can't
     * find point from rtree
     */
    public int generateMIWDFlag(Floor curFloor, Point2D.Double current, Floor dstFloor, Point2D.Double dst, AccessPoint[] boundAPs) {
        int flag = -1;
        double minDistance;
        double tmpDistance = MAX_DISTANCE;
        Partition partition1 = SpatialHandler.findPartitionForPoint(curFloor, current);
        Partition partition2 = SpatialHandler.findPartitionForPoint(dstFloor, dst);

        // Region region1 = SpatialHandler.findClosestRegion(curFloorPOJO, current);
        // Region region2 = SpatialHandler.findClosestRegion(dstFloorPOJO, dst);
        if (partition1 == partition2) {
            minDistance = current.distance(dst);
            flag = 1;
            return flag;
        } else {
            minDistance = MAX_DISTANCE;
        }
        if (partition1 == null || partition2 == null) {
            flag = -2;
            return flag;
        }

        for (AccessPoint ap1 : partition1.getAPConnectors()) {
            double toDoor1 = current.distance(ap1.getLocation2D());
            for (AccessPoint ap2 : partition2.getAPConnectors()) {
                double toDoor2 = dst.distance(ap2.getLocation2D());
                tmpDistance = toDoor1 + toDoor2 + D2DDistance(ap1, ap2);
                if (tmpDistance < minDistance) {
                    boundAPs[0] = ap1;
                    boundAPs[1] = ap2;
                    minDistance = tmpDistance;
                    flag = 3;
                    if (ap1 == ap2) {
                        flag = 2;
                    }
                }
            }
        }
        return flag;
    }

    // if in the same regions, return empty array
    // if adjacent regions, add the common door
    // if reachable regions, add two doors
    // if < 0, return null pointer
    public List<AccessPoint> generateMidDoors(Floor curFloor, Point2D.Double current, Floor dstFloor, Point2D.Double dst) {
        List<AccessPoint> pathList = new ArrayList<AccessPoint>();
        AccessPoint[] boundAPs = new AccessPoint[2];
        int flag = generateMIWDFlag(curFloor, current, dstFloor, dst, boundAPs);
        if (flag <= 0) {
            return pathList;
        }

        if (flag == 2) {
            pathList.add(boundAPs[0]);
        }
        if (flag == 3) {
            try {
                getPath(pathList, boundAPs[0], boundAPs[1]);
                pathList.add(0, boundAPs[0]);
                pathList.add(pathList.size(), boundAPs[1]);
            } catch (NoSuchDoorException e) {
                e.printStackTrace();
            }
        }
        return pathList;

    }

    public void getPath(List<AccessPoint> pathList, AccessPoint ap1, AccessPoint ap2) throws NoSuchDoorException {
        Integer k;
        int id1 = aps.indexOf(ap1);
        int id2 = aps.indexOf(ap2);
        if (id1 == -1 || id2 == -1) {
            throw new NoSuchDoorException();
        }

        k = path[id1][id2];
        if (path[id1][id2] == -1) {
            return;
        }
        AccessPoint ap3 = aps.get(k);
        getPath(pathList, ap1, ap3);
        pathList.add(aps.get(k));
        getPath(pathList, ap3, ap2);
    }

}
