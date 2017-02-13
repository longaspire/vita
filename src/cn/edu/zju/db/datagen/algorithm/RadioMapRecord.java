package cn.edu.zju.db.datagen.algorithm;

import cn.edu.zju.db.datagen.indoorobject.IndoorObjsFactory;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.SparseInstance;

import java.awt.geom.Point2D;
import java.util.Map.Entry;
import java.util.SortedMap;

public class RadioMapRecord {

    public static int defaultMinimalRSSI = -100;

    private String groundTruthId;

    private Point2D.Double groundTruthLocation;

    private SortedMap<Integer, Integer> rssiVector;

    public RadioMapRecord() {
        super();
    }

    public RadioMapRecord(String groundTruthId, java.awt.geom.Point2D.Double groundTruthLocation,
                          SortedMap<Integer, Integer> rssiVector) {
        super();
        this.groundTruthId = groundTruthId;
        this.groundTruthLocation = groundTruthLocation;
        this.rssiVector = rssiVector;
    }

    public String getGroundTruthId() {
        return groundTruthId;
    }

    public void setGroundTruthId(String groundTruthId) {
        this.groundTruthId = groundTruthId;
    }

    public Point2D.Double getGroundTruthLocation() {
        return groundTruthLocation;
    }

    public void setGroundTruthLocation(Point2D.Double groundTruthLocation) {
        this.groundTruthLocation = groundTruthLocation;
    }

    public SortedMap<Integer, Integer> getRssiVector() {
        return rssiVector;
    }

    public void setRssiVector(SortedMap<Integer, Integer> rssiVector) {
        this.rssiVector = rssiVector;
    }

    @Override
    public String toString() {

        String result = "";
        // int count = 0;
        for (Entry<Integer, Integer> entry : this.rssiVector.entrySet()) {
            result = result + entry.getKey() + "=" + entry.getValue();
            // if(count < this.rssiVector.size()){
            result += ",";
            // }
            // count++;
        }

        return result + this.groundTruthId;
    }

    public Instance convertToInstance() {

        int vectorSize = IndoorObjsFactory.stationID - 1;

        Instance instance = new SparseInstance(vectorSize, defaultMinimalRSSI);

        for (Entry<Integer, Integer> entry : this.rssiVector.entrySet()) {
            instance.put(entry.getKey() - 1, (double) entry.getValue());
        }

        instance.setClassValue(this.getGroundTruthId());

//		System.out.println(instance);

        return instance;

    }

}
