package cn.edu.zju.db.datagen.indoorobject.station.factory;

import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import cn.edu.zju.db.datagen.indoorobject.station.Station;

import java.util.ArrayList;

public abstract class StationsFactory implements SimpleStationCreateInterface {

    public StationsFactory() {
        // TODO Auto-generated constructor stub
    }


    public abstract void generateStationsInPart(Partition partition, ArrayList<Station> stations, int stationNum, String stationType);

    public String toString() {
        return this.getClass().getSimpleName();
    }
}
