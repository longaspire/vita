package cn.edu.zju.db.datagen.indoorobject.station.factory;

import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import cn.edu.zju.db.datagen.indoorobject.IndoorObjsFactory;
import cn.edu.zju.db.datagen.indoorobject.station.Station;
import diva.util.java2d.Polygon2D;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class StationsCenterFactory extends StationsFactory {

    public StationsCenterFactory() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void generateStationsInPart(Partition partition, ArrayList<Station> stations, int stationNum, String stationType) {
        Point2D.Double center = getAvgPointInPartition(partition);
//		stationNum = 3;
        for (int i = 0; i < stationNum; i++) {
            double angle = Math.random() * Math.PI;
            double range = Math.random() * 3;        //range of the circle
            double newX = center.getX() + Math.cos(angle) * range;
            double newY = center.getY() + Math.sin(angle) * range;
            Point2D.Double randomPoint = new Point2D.Double(newX, newY);
            Station station = (Station) IndoorObjsFactory.createIndoorObject(stationType);
            station.setLocation(randomPoint);
            station.setCurrentPartition(partition);
            station.setCurrentFloor(partition.getFloor());
            station.setId(IndoorObjsFactory.stationID++);
            stations.add(station);
        }
    }

    private Point2D.Double getAvgPointInPartition(Partition partition) {
        Polygon2D.Double polygon = partition.getPolygon2D();
        int numCount = polygon.getVertexCount();
        double xSum = 0, ySum = 0;
        for (int i = 0; i < numCount; i++) {
            xSum += polygon.getX(i);
            ySum += polygon.getY(i);
        }
        Point2D.Double center = new Point2D.Double(xSum / numCount, ySum / numCount);
        return center;
    }

}
