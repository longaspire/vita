package cn.edu.zju.db.datagen.indoorobject.movingobject.factory;

import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import cn.edu.zju.db.datagen.indoorobject.IndoorObjsFactory;
import cn.edu.zju.db.datagen.indoorobject.movingobject.MovingObj;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class MovingObjectsEvenFactory extends MovingObjectsFactory {

    public MovingObjectsEvenFactory() {

    }

    @Override
    public void generateMovingObjsInPart(Partition partition, ArrayList<MovingObj> movingObjs, int pointNum,
                                         String movingObjType) {
        for (int i = 0; i < pointNum; i++) {
            Point2D.Double randomPoint = partition.calRandomPointInMBR();
            if (partition.getPolygon2D().contains(randomPoint) == false) {
                continue;
            }
            MovingObj movingObj = (MovingObj) IndoorObjsFactory.createIndoorObject(movingObjType);
            movingObj.setCurrentFloor(partition.getFloor());
            movingObj.setCurrentPartition(partition);
            movingObj.setLocation(randomPoint);
            movingObj.setId(++IndoorObjsFactory.movingObjID);
            movingObjs.add(movingObj);
        }

    }


}
