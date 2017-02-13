package cn.edu.zju.db.datagen.indoorobject.factory;

import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import cn.edu.zju.db.datagen.indoorobject.movingobject.MovingObj;
import cn.edu.zju.db.datagen.indoorobject.utility.SpatialHandler;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alex9 on 2016/8/2.
 */
public class MovingObjectsCenterFactory extends MovingObjectFactory{

    @Override
    List<MovingObj> createMovingObjsOnPartition(String type, Partition partition, int movingObjectNum) {
        double radius = 2;
        List<Point2D.Double> randomPointsNearCenter = SpatialHandler.getRandomPointsNearCenter(partition.getPolygon2D(), movingObjectNum, radius);
        return randomPointsNearCenter
                       .stream()
                       .map(point -> createMovingObject(type, partition, point))
                       .collect(Collectors.toList());
    }


    private MovingObj createMovingObject(String type, Partition partition, Point2D.Double location) {
        MovingObj movingObj = (MovingObj)createIndoorObject(type);

        movingObj.setCurrentFloor(partition.getFloor());
        movingObj.setCurrentPartition(partition);
        movingObj.setLocation(location);
        movingObj.setId(++IndoorObjectFactory.movingObjID);
        return movingObj;
    }





}
