package cn.edu.zju.db.datagen.indoorobject;

import cn.edu.zju.db.datagen.database.spatialobject.Floor;
import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import cn.edu.zju.db.datagen.indoorobject.movingobject.MovingObj;
import cn.edu.zju.db.datagen.indoorobject.movingobject.factory.MovingObjectsFactory;
import cn.edu.zju.db.datagen.indoorobject.station.Station;
import cn.edu.zju.db.datagen.indoorobject.station.factory.StationsFactory;
import cn.edu.zju.db.datagen.indoorobject.utility.PropLoader;

import java.util.ArrayList;

public class IndoorObjsFactory {

    public static int movingObjID = 1;
    public static int stationID = 1;


    public IndoorObjsFactory() {

    }

    public void generateStationsOnFloor(Floor floor, ArrayList<Station> stations) {
        PropLoader propLoader = new PropLoader();
        propLoader.loadProp("conf/pattern.properties");

        Class<?> stationReflaction = null;
        StationsFactory stationsInit = null;

        try {
            stationReflaction = Class.forName(propLoader.getStationDistributerType());
            stationsInit = (StationsFactory) stationReflaction.newInstance();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        int stationNumInPartition = Integer.parseInt(propLoader.getStationMaxNumInpart());
        int stationNumInArea = Integer.parseInt(propLoader.getStationNumArea());
        int maxStationNum = 0;
        ArrayList<Partition> partitions = floor.getPartsAfterDecomposed();
        for (Partition partition : partitions) {
            String stationType = propLoader.getStationType();
            maxStationNum = Math.min(stationNumInPartition, (int) (stationNumInArea * partition.calMBRArea() / 100));
            stationsInit.generateStationsInPart(partition, stations, maxStationNum, stationType);
        }

    }

    public void generateMovingObjsOnFloor(Floor floor, ArrayList<MovingObj> movingObjs) {
        PropLoader propLoader = new PropLoader();
        propLoader.loadProp("conf/pattern.properties");

        Class<?> movObjReflaction = null;
        MovingObjectsFactory movingObjectsFactory = null;
        try {
            movObjReflaction = Class.forName(propLoader.getMovingObjDistributerType());
            movingObjectsFactory = (MovingObjectsFactory) movObjReflaction.newInstance();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        int maxPointNum = Integer.parseInt(propLoader.getMovingObjMaxNumberInPart());
        ArrayList<Partition> partitions = floor.getPartsAfterDecomposed();
        for (Partition partition : partitions) {
            if (Math.random() < 0.9)
                continue;
            String movingObjType = propLoader.getMovingObjType();
            movingObjectsFactory.generateMovingObjsInPart(partition, movingObjs, maxPointNum, movingObjType);
        }

    }


    public static IndoorObject createIndoorObject(String objType) {
        Class<?> reflaction;
        IndoorObject indoorObj = null;
        try {
            reflaction = Class.forName(objType);
            indoorObj = (IndoorObject) reflaction.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return indoorObj;
    }

}
