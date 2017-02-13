package cn.edu.zju.db.datagen.ifc.datamanipulation;

import cn.edu.zju.db.datagen.ifc.dataextraction.IfcFileParser;
import cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Partition;

import java.awt.geom.Point2D;
import java.util.ArrayList;

/*
 * This class normalize coordinates such that coordinates with either x or y coordinates close to each other are aligned
 */
public class Grid extends IfcFileParser {

    private static Double t_grid = 50.0;

    public static void normalize(Partition part) {
        ArrayList<Point2D.Double> points = part.getPolyline();
        // for x
        for (int i = 0; i < points.size() - 1; i++) {
            ArrayList<Point2D.Double> simX = new ArrayList<Point2D.Double>();
            Point2D.Double p = points.get(i);
            simX.add(p);
            for (int j = 0; j < points.size() - 1; j++) {
                Point2D.Double po = points.get(j);
                if (p.getX() - t_grid < po.getX() && p.getX() + t_grid > po.getX()) {
                    simX.add(po);
                }
            }
            Double sum = 0.0;
            for (Point2D.Double poi : simX) {
                sum += poi.getX();
            }
            Double avgX = sum / simX.size();

            for (Point2D.Double poin : simX) {
                poin.setLocation(avgX, poin.getY());
            }
        }

        // for y
        for (int i = 0; i < points.size() - 1; i++) {
            ArrayList<Point2D.Double> simY = new ArrayList<Point2D.Double>();
            Point2D.Double p = points.get(i);
            simY.add(p);
            for (int j = 0; j < points.size() - 1; j++) {
                Point2D.Double po = points.get(j);
                if (p.getY() - t_grid < po.getY() && p.getY() + t_grid > po.getY() && p != po) {
                    simY.add(po);
                }
            }
            Double sum = 0.0;
            for (Point2D.Double poi : simY) {
                sum += poi.getY();
            }
            Double avgY = sum / simY.size();

            for (Point2D.Double poin : simY) {
                poin.setLocation(poin.getX(), avgY);
            }
        }

        points.get(points.size() - 1).setLocation(points.get(0).getX(), points.get(0).getY());
    }

}
