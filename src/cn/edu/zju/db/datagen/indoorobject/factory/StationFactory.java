package cn.edu.zju.db.datagen.indoorobject.factory;

import cn.edu.zju.db.datagen.database.spatialobject.Floor;
import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import cn.edu.zju.db.datagen.indoorobject.IndoorObject;
import cn.edu.zju.db.datagen.indoorobject.station.Station;
import cn.edu.zju.db.datagen.indoorobject.utility.PropLoader;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alex9 on 2016/8/3.
 */
abstract class StationFactory extends IndoorObjectFactory{


    @Override
    IndoorObject createIndoorObject(String type) {
        return super.createIndoorObject(type);
    }

    List<Station> createIndoorObjectsOnFloor(Floor floor) {
        return createStationsOnFloor(floor);
    }


    private List<Station> createStationsOnFloor(Floor floor) {
        PropLoader propLoader = new PropLoader();
        propLoader.loadProp("conf/pattern.properties");
        String stationType = propLoader.getStationType();

        int stationNumInPartition = Integer.parseInt(propLoader.getStationMaxNumInpart());
        int stationNumInArea = Integer.parseInt(propLoader.getStationNumArea());
        int maxStationNum = 0;

        return floor.getPartitions()
                .stream()
                .map(partition -> createStationsOnPartition(stationType, partition, Math.min(stationNumInPartition, (int) (stationNumInArea * partition.calMBRArea() / 100))))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    protected abstract List<Station> createStationsOnPartition(String type, Partition partition, int stationNumber);
}
