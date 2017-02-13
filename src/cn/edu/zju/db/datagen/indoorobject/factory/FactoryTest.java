package cn.edu.zju.db.datagen.indoorobject.factory;

import cn.edu.zju.db.datagen.database.DB_Connection;
import cn.edu.zju.db.datagen.database.DB_WrapperLoad;
import cn.edu.zju.db.datagen.indoorobject.movingobject.MovingObj;
import cn.edu.zju.db.datagen.indoorobject.station.Station;
import cn.edu.zju.db.datagen.indoorobject.utility.IdrObjsUtility;
import cn.edu.zju.db.datagen.indoorobject.utility.PropLoader;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by alex9 on 2016/8/4.
 */
public class FactoryTest {


    private static void setMovingObjsInitTime(List<MovingObj> movingObjList) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 30);
        IdrObjsUtility.objectGenerateStartTime = calendar.getTime();

        Calendar calendar1 = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 40);
        IdrObjsUtility.objectGenerateEndTime = calendar1.getTime();
        movingObjList.forEach(movingObj -> {
            movingObj.setInitMovingTime(calGaussianTime());
        });
    }

    private static long calGaussianTime() {
        Random random = new Random();
        long startTime = IdrObjsUtility.objectGenerateStartTime.getTime();
        long endTime = IdrObjsUtility.objectGenerateEndTime.getTime();
        long middle = (long)((endTime - startTime) / 2.0 + startTime);
        long error = (long)((endTime - startTime) * 0.5);
        long gaussianError = (long)(random.nextGaussian() * error);
        System.out.println(gaussianError);
        long gaussianTime = (long)(middle + gaussianError);
        if (gaussianTime < startTime) {
            return startTime;
        } else if(gaussianTime > endTime) {
            return endTime;
        } else {
            return gaussianTime;
        }
    }

    public static void main(String[] args) throws SQLException {
        Connection con = DB_Connection.connectToDatabase("conf/moovework.properties");
        DB_WrapperLoad.loadALL(con, 25);
        PropLoader propLoader = new PropLoader();
        propLoader.loadProp("conf/factory.properties");
        List<Station> stations = new ArrayList<>();
        DB_WrapperLoad.floorT.forEach(floor -> stations.addAll(IndoorObjectFactory.createStationsOnFloor(floor, propLoader.getStationDistributerType())));
        System.out.println(stations);

        List<MovingObj> movingObjs =
                DB_WrapperLoad.floorT
                        .stream()
                        .map(floor -> IndoorObjectFactory.createMovingObjectsOnFloor(floor, propLoader.getMovingObjDistributerType()))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
        System.out.println(movingObjs);

        setMovingObjsInitTime(movingObjs);

        movingObjs.forEach(movingObj -> System.out.println(new Date(movingObj.getInitMovingTime())));
    }
}
