package cn.edu.zju.db.datagen.algorithm;

import cn.edu.zju.db.datagen.indoorobject.station.RSSIPackage;
import cn.edu.zju.db.datagen.indoorobject.utility.IdrObjsUtility;

import java.util.ArrayList;

public class ProximityRecord {

    private int objectId;

    private int stationId;

    private ArrayList<RSSIPackage> measurements = new ArrayList<RSSIPackage>();

    public ProximityRecord(int objectId, int stationId) {
        super();
        this.objectId = objectId;
        this.stationId = stationId;
        this.measurements = new ArrayList<RSSIPackage>();
    }

    public ProximityRecord(int objectId, int stationId, ArrayList<RSSIPackage> measurements) {
        super();
        this.objectId = objectId;
        this.stationId = stationId;
        this.measurements = measurements;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public ArrayList<RSSIPackage> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(ArrayList<RSSIPackage> measurements) {
        this.measurements = measurements;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public int addMeasurements(RSSIPackage measurement) {
        if (this.measurements != null) {
            this.measurements.add(measurement);
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {

        long t_s = Long.MAX_VALUE;
        long t_e = Long.MIN_VALUE;
        long t_max = Long.MIN_VALUE;
        double rssi_max = -10000;

        if (this.measurements != null) {
            for (RSSIPackage item : this.measurements) {
                long curTime = item.getTimeStamp();
                if (curTime <= t_s) {
                    t_s = curTime;
                }
                if (curTime >= t_e) {
                    t_e = curTime;
                }
//				System.out.println(item.getRSSI());
                if (item.getRSSI() >= rssi_max) {
                    t_max = curTime;
                    rssi_max = item.getRSSI();
                }
            }

            return this.getStationId() + "\t" + IdrObjsUtility.sdf.format(t_s) + "\t" + IdrObjsUtility.sdf.format(t_e) + "\t" + IdrObjsUtility.sdf.format(t_max) + "\n";

        }

        return null;
    }

}
