package cn.edu.zju.db.datagen.algorithm;

import cn.edu.zju.db.datagen.database.spatialobject.Floor;
import cn.edu.zju.db.datagen.indoorobject.station.RSSIPackage;
import cn.edu.zju.db.datagen.indoorobject.utility.IdrObjsUtility;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TRIForOne extends AlgorithmForOne {

    private int n;
    private double rssiAt1;

    private IndoorLocation lastPosition;

    public TRIForOne(String inputFileName, String outputFileName) {
        super(inputFileName, outputFileName);
        // TODO Auto-generated constructor stub
        String comments = "floorId" + "\t" + "partitionId" + "\t" + "location_x" + "\t" + "location_y" + "\t"
                                  + "timeStamp" + "\n";
        this.setComments(comments);
    }

    public TRIForOne(String inputFileName, String outputFileName, int n, double rssiAt1) {
        super(inputFileName, outputFileName);
        // TODO Auto-generated constructor stub
        this.n = n;
        this.rssiAt1 = rssiAt1;
        String comments = "floorId" + "\t" + "partitionId" + "\t" + "location_x" + "\t" + "location_y" + "\t"
                                  + "timeStamp" + "\n";
        this.setComments(comments);
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        ArrayList<String> result = calAlgorithmForOneObj(this.getInputFileName());
        exportResult(result, this.getOutputFileName());
    }

    @Override
    public ArrayList<String> calculateTrajectory(ArrayList<String> records) {
        // TODO Auto-generated method stub
        ArrayList<RSSIPackage> packTemps = new ArrayList<RSSIPackage>();
        ArrayList<String> result = new ArrayList<String>();
        // boolean currentRecord = false;
        // Point2D.Double currentPosition = null;
        boolean calculated = false;
        String currentTimeStamp = "";
        for (String record : records) {
            if (!record.equals("")) {
                List<String> lists = Arrays.asList(record.split("\t"));
                Double x = Double.parseDouble(lists.get(1));
                Double y = Double.parseDouble(lists.get(2));
                int rssi = Integer.parseInt(lists.get(3));
                RSSIPackage packTemp = new RSSIPackage(x, y, rssi);
                int stationId = Integer.parseInt(lists.get(0));
                packTemp.setFromID(stationId);
                currentTimeStamp = lists.get(4);
                packTemps.add(packTemp);
                if (calculated == true) {
                    calculated = false;
                }
            } else if (calculated == false) {
                IndoorLocation resultPoint = calculateLocation(packTemps);
                // System.out.println(resultPoint);
                if (resultPoint != null && (!currentTimeStamp.equals(""))) {
                    result.add(resultPoint.toString() + currentTimeStamp + "\n");
                }
                // result.add(resultPoint.distance(currentPosition) + "\n");
                packTemps.clear();
                calculated = true;
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public IndoorLocation calculateLocation(ArrayList<RSSIPackage> packs) {
        if (packs.size() >= 3) {
            Collections.sort(packs);
            RSSIPackage pack1 = packs.get(0);
            RSSIPackage pack2 = packs.get(1);
            RSSIPackage pack3 = packs.get(2);
            Point2D.Double resultPoint = calculateIntersect(pack1, pack2, pack3);

            // determine the floor of the calculated location using the nearest
            // pack's floor
            Floor floor = IdrObjsUtility.allStations.get(pack1.getFromID()).getCurrentFloor();
            IndoorLocation location = new IndoorLocation(resultPoint, floor);
            lastPosition = location;
            return location;
        }

        return lastPosition;
    }

    public Point2D.Double calculateIntersect(RSSIPackage pack1, RSSIPackage pack2, RSSIPackage pack3) {
        double x1 = pack1.getFromLocation().getX();
        double y1 = pack1.getFromLocation().getY();
        double r1 = Math.pow(10, (pack1.getRSSI() + 10 * n) / (rssiAt1));
        double x2 = pack2.getFromLocation().getX();
        double y2 = pack2.getFromLocation().getY();
        double r2 = Math.pow(10, (pack2.getRSSI() + 10 * n) / (rssiAt1));
        double x3 = pack3.getFromLocation().getX();
        double y3 = pack3.getFromLocation().getY();
        double r3 = Math.pow(10, (pack3.getRSSI() + 10 * n) / (rssiAt1));
        double y = (-(x1 * x1 - x2 * x2 + y1 * y1 - y2 * y2) * (x3 - x2)
                            + (x2 * x2 - x3 * x3 + y2 * y2 - y3 * y3) * (x2 - x1) - (r2 * r2 - r3 * r3) * (x2 - x1)
                            + (r1 * r1 - r2 * r2) * (x3 - x2)) / (2 * ((y2 - y1) * (x3 - x2) - (y3 - y2) * (x2 - x1)));
        double x = (r1 * r1 - r2 * r2 - (2 * y * y2 - 2 * y * y1 + x1 * x1 - x2 * x2 + y1 * y1 - y2 * y2))
                           / (2 * (x2 - x1));

        Point2D.Double resultPoint = new Point2D.Double(x, y);
        if (resultPoint.distance(pack1.getFromLocation()) > r1 && resultPoint.distance(pack2.getFromLocation()) > r2
                    && resultPoint.distance(pack3.getFromLocation()) > r3) {
            double avgX = (pack1.getFromLocation().getX() + pack2.getFromLocation().getX()
                                   + pack3.getFromLocation().getX()) / 3;
            double avgY = (pack1.getFromLocation().getY() + pack2.getFromLocation().getY()
                                   + pack3.getFromLocation().getY()) / 3;
            resultPoint = new Point2D.Double(avgX, avgY);
        }
        return resultPoint;
    }

}
