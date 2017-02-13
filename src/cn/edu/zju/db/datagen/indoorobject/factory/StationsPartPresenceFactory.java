package cn.edu.zju.db.datagen.indoorobject.factory;

import cn.edu.zju.db.datagen.database.DB_WrapperLoad;
import cn.edu.zju.db.datagen.database.spatialobject.AccessPoint;
import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import cn.edu.zju.db.datagen.indoorobject.station.Station;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alex9 on 2016/8/4.
 */
public class StationsPartPresenceFactory extends StationFactory {

    private int[] APFlag = new int[DB_WrapperLoad.accessPointConnectorT.size() + 100];

    @Override
    protected List<Station> createStationsOnPartition(String type, Partition partition, int stationNumber) {
        return partition.getAPs()
                       .stream()
                       .filter(this::isAPNeeded)
                       .map(ap -> {
                           Point2D.Double chkPoint = new Point2D.Double(ap.getLocation2D().getX() - 0.4, ap.getLocation2D().getY() - 0.4);
                           APFlag[DB_WrapperLoad.accessPointConnectorT.indexOf(ap)] = 1;
                           return createStation(type, partition, chkPoint);
                       })
                       .collect(Collectors.toList());

    }

    private Station createStation(String type, Partition partition, Point2D.Double location) {
        Station station = (Station) createIndoorObject(type);

        station.setLocation(location);
        station.setCurrentPartition(partition);
        station.setCurrentFloor(partition.getFloor());
        station.setId(++stationID);
        station.setType(2);
        return station;
    }

    private boolean isAPNeeded(AccessPoint accessPoint) {
        return !(APFlag[DB_WrapperLoad.accessPointConnectorT.indexOf(accessPoint)] == 1 || Math.random() < 0.3);
    }
}
