package cn.edu.zju.db.datagen.indoorobject.factory;

import cn.edu.zju.db.datagen.database.spatialobject.Floor;
import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import cn.edu.zju.db.datagen.indoorobject.IndoorObject;
import cn.edu.zju.db.datagen.indoorobject.movingobject.MovingObj;
import cn.edu.zju.db.datagen.indoorobject.utility.PropLoader;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alex9 on 2016/8/3.
 */
abstract class MovingObjectFactory extends IndoorObjectFactory {

    @Override
    IndoorObject createIndoorObject(String type) {
        return super.createIndoorObject(type);
    }

    List<MovingObj> createIndoorObjectsOnFloor(Floor floor) {
        return createMovingObjectsOnFloor(floor);
    }


    private List<MovingObj> createMovingObjectsOnFloor(Floor floor) {
        PropLoader propLoader = new PropLoader();
        propLoader.loadProp("conf/pattern.properties");
        int maxNumInPart = Integer.parseInt(propLoader.getMovingObjMaxNumberInPart());
        String movingObjectType = propLoader.getMovingObjType();

        return floor.getPartitions()
                       .stream()
                       .filter(partition -> Math.random() < 0.9)
                       .map(partition -> createMovingObjsOnPartition(movingObjectType, partition, maxNumInPart))
                       .flatMap(Collection::stream)
                       .collect(Collectors.toList());

    }

    abstract List<MovingObj> createMovingObjsOnPartition(String type, Partition partition, int movingObjectNum);
}

