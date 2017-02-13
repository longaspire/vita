package cn.edu.zju.db.datagen.spatialgraph;

import cn.edu.zju.db.datagen.database.DB_WrapperLoad;
import cn.edu.zju.db.datagen.database.spatialobject.AccessPoint;
import cn.edu.zju.db.datagen.database.spatialobject.Connector;
import cn.edu.zju.db.datagen.database.spatialobject.Partition;

import java.util.ArrayList;

public class BuildingD2DGrpah {
    public static ArrayList<Partition> partitions = DB_WrapperLoad.partitionDecomposedT;
    public static ArrayList<AccessPoint> accessPoints = DB_WrapperLoad.accessPointConnectorT;
    public static ArrayList<Connector> connectors = DB_WrapperLoad.connectorT;
    public static double distance[][];
    static int path[][];    //path[i][j] to store the id of path from point i to point j

    public BuildingD2DGrpah() {

    }

    //floyd to get distance between any two access points
    public void generateD2DDistance() {
        distance = new double[1000][1000];
        initlizeDistance();
        initlizePath();
        for (int k = 0; k < 1000; k++) {
            for (int i = 0; i < 1000; i++) {
                for (int j = 0; j < 1000; j++) {
                    if (distance[i][k] != 10000 && distance[k][j] != 10000
                                && distance[i][k] + distance[k][j] < distance[i][j]) {
                        distance[i][j] = distance[i][k] + distance[k][j];
                        path[i][j] = k;
                    }
                }
            }
        }
    }

    /* if two access points are adjacent, the distance is ap1.distance(ap2), otherwise is max
     * ap1.distance(ap2) should take obstacles into consideration, it may not be a line
     * use a much more brute method, if there is not a straight line, follow the bounds of partition
     * */
    private void initlizeDistance() {
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++) {
                distance[i][j] = 10000;
            }
        }
        for (int i = 0; i < accessPoints.size(); i++) {
            AccessPoint ap1 = accessPoints.get(i);
            ArrayList<Partition> ap1Partitions = ap1.getPartitions();
            for (int j = 0; j < ap1Partitions.size(); j++) {
                Partition ap1Part = ap1Partitions.get(j);
                ArrayList<AccessPoint> ap1AccessPoints = ap1Part.getAPs();
                for (int k = 0; k < ap1AccessPoints.size(); k++) {
                    AccessPoint ap2 = ap1AccessPoints.get(k);
                    int id1 = i;
                    int id2 = accessPoints.indexOf(ap2);
                    if (ap1 != ap2) {
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

    private void initlizePath() {
        path = new int[1000][1000];
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++) {
                path[i][j] = -1;
            }
        }
    }

    public static double D2DDistance(AccessPoint ap1, AccessPoint ap2) {
        return distance[accessPoints.indexOf(ap1)][accessPoints.indexOf(ap2)];
    }

    /* get path between ap1 and ap2. path does not contain ap1 and ap2,
     * remember to add them when use it
     * path[i][j]-the best inter point which link i and j, path[i][j] is an ID
     * -1 -- i and j are adjacent or i cannot reach j
     * */
    public static void getPath(ArrayList<Integer> pathList, AccessPoint ap1, AccessPoint ap2) {
        Integer k;
        int id1 = accessPoints.indexOf(ap1);
        int id2 = accessPoints.indexOf(ap2);
        k = path[id1][id2];
        if (path[id1][id2] == -1) {
            return;
        }
        AccessPoint ap3 = accessPoints.get(k);
        getPath(pathList, ap1, ap3);
        pathList.add(k);
        getPath(pathList, ap3, ap2);
    }


    //path has the ID's of Access Point, change it to an ArrayList of Access Point
    public static ArrayList<AccessPoint> IDList2APList(ArrayList<Integer> path) {
        ArrayList<AccessPoint> aps = new ArrayList<AccessPoint>();
        for (int i : path) {
            AccessPoint ap = accessPoints.get(i);
            aps.add(ap);
        }
        return aps;
    }
}
