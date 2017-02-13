package cn.edu.zju.db.datagen.algorithm;

import cn.edu.zju.db.datagen.indoorobject.station.RSSIPackage;

import java.awt.geom.Point2D;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PXMForOne extends AlgorithmForOne {

    private int objectId;

    private Hashtable<Integer, ProximityRecord> currentProximityRecords = new Hashtable<Integer, ProximityRecord>();

    public PXMForOne(String inputFileName, String outputFileName) {
        super(inputFileName, outputFileName);
        // TODO Auto-generated constructor stub
        this.objectId = Integer.parseInt(this.getInputFileName().substring(this.getInputFileName().lastIndexOf("_") + 1, this.getInputFileName().length() - 4));
//		System.out.print(inputFileName);
        String comments = "stationId" + "\t" + "t_start" + "\t" + "t_end" + "\t" + "t_max" + "\t"
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
        boolean calculated = false;

        for (String record : records) {
            if (!record.equals("")) {
                List<String> lists = Arrays.asList(record.split("\t"));
                Integer fromID = Integer.parseInt(lists.get(0));
                Double x = Double.parseDouble(lists.get(1));
                Double y = Double.parseDouble(lists.get(2));
                int rssi = Integer.parseInt(lists.get(3));
                String currentTimeStamp = lists.get(4);
                Date timestamp;
                SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
                try {
                    timestamp = sdf.parse(currentTimeStamp);
                    RSSIPackage packTemp = new RSSIPackage(new Point2D.Double(x, y), fromID, timestamp.getTime(), rssi);
                    packTemps.add(packTemp);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (calculated == true) {
                    calculated = false;
                }
            } else if (calculated == false) {
//				System.out.println(packTemps);
                RSSIPackage strongestPack = calculateStrongestPack(packTemps);
                System.out.println(strongestPack);
                if (strongestPack != null) {
                    if (currentProximityRecords.containsKey(objectId)) {
                        ProximityRecord proximityRecord = currentProximityRecords.get(objectId);
                        if (proximityRecord.getStationId() == strongestPack.getFromID()) {
                            proximityRecord.addMeasurements(strongestPack);
                        } else {
                            result.add(proximityRecord.toString());
                            currentProximityRecords.remove(objectId);
                        }
                    } else {
                        ProximityRecord proximityRecord = new ProximityRecord(objectId, strongestPack.getFromID());
                        proximityRecord.addMeasurements(strongestPack);
                        currentProximityRecords.put(objectId, proximityRecord);
                    }
                } else {
                    if (currentProximityRecords.containsKey(objectId)) {
                        ProximityRecord proximityRecord = currentProximityRecords.get(objectId);
                        result.add(proximityRecord.toString());
                        currentProximityRecords.remove(objectId);
                    }
                }
                packTemps.clear();
                calculated = true;
            }
        }
//		for (String item : result) {
//			System.out.println(item);
//		}
        System.out.println(result.size());
        return result;
    }

    private RSSIPackage calculateStrongestPack(ArrayList<RSSIPackage> packs) {
        // TODO Auto-generated method stub
        System.out.println(packs);
        RSSIPackage strongest_pack = null;
        if (packs.size() >= 2) {
            Collections.sort(packs);
            strongest_pack = packs.get(packs.size() - 1);
        } else if (packs.size() == 1) {
            strongest_pack = packs.get(0);
        } else {
            strongest_pack = null;
        }

        return strongest_pack;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

}
