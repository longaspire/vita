package cn.edu.zju.db.datagen.indoorobject.station.factory;

import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import cn.edu.zju.db.datagen.indoorobject.IndoorObjsFactory;
import cn.edu.zju.db.datagen.indoorobject.station.Station;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class StationsEvenFactory extends StationsFactory {

    public StationsEvenFactory() {

    }


    //minimum station number is
    @Override
    public void generateStationsInPart(Partition partition, ArrayList<Station> stations, int stationNum, String stationType) {
//		double partArea = partition.calMBRArea();
//		int stationNumMax = (int)(partArea / 50);
//		
//		if(partArea < 50){
//			stationNumArea = (int)(Math.random()*2)*stationNumMax;
//		} else if(partArea < 80){
//			stationNumArea = (int)(1 + Math.random()*2)*stationNumMax;
//		} else {
//			stationNumArea = (int)(2 + Math.random()*4)*stationNumMax;
//		}
//		stationNum = Math.min(stationNum, stationNumMax);

        for (int i = 0; i < stationNum; i++) {

            Point2D.Double randomPoint = partition.calRandomPointInMBR();
            if (partition.getPolygon2D().contains(randomPoint) == false) {
                continue;
            }
            Station station = (Station) IndoorObjsFactory.createIndoorObject(stationType);
            station.setLocation(randomPoint);
            station.setCurrentPartition(partition);
            station.setCurrentFloor(partition.getFloor());
            station.setId(IndoorObjsFactory.stationID++);
            stations.add(station);
        }
    }

}
