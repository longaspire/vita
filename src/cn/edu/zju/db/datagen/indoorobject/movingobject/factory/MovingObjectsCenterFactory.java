package cn.edu.zju.db.datagen.indoorobject.movingobject.factory;

import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import cn.edu.zju.db.datagen.indoorobject.IndoorObjsFactory;
import cn.edu.zju.db.datagen.indoorobject.movingobject.MovingObj;
import diva.util.java2d.Polygon2D;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class MovingObjectsCenterFactory extends MovingObjectsFactory {

    public MovingObjectsCenterFactory() {

    }

    @Override
    public void generateMovingObjsInPart(Partition partition, ArrayList<MovingObj> movingObjs, int pointNum,
                                         String movingObjType) {
        double partArea = partition.calMBRArea();

        Point2D.Double center = getAvgPointInPartition(partition);

        for (int i = 0; i < pointNum; i++) {
            double newX = 0;
            double newY = 0;
            if (Math.random() > 0.2) {
                double angle = Math.random() * Math.PI;
                double range = Math.random() * 2; // range of the circle
                newX = center.getX() + Math.cos(angle) * range;
                newY = center.getY() + Math.sin(angle) * range;
            } else {
                newX = partition.calRandomPointInMBR().getX();
                newY = partition.calRandomPointInMBR().getY();
            }
            Point2D.Double randomPoint = new Point2D.Double(newX, newY);
            MovingObj movingObj = (MovingObj) IndoorObjsFactory.createIndoorObject(movingObjType);
            movingObj.setLocation(randomPoint);
            movingObj.setCurrentFloor(partition.getFloor());
            movingObj.setCurrentPartition(partition);
            movingObj.setId(++IndoorObjsFactory.movingObjID);
            movingObjs.add(movingObj);
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
