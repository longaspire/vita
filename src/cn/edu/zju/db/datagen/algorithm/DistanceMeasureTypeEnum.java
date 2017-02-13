package cn.edu.zju.db.datagen.algorithm;

/**
 * Created by Dell on 2016/1/28.
 */
public enum DistanceMeasureTypeEnum {

    MAN(0, "distance_measure_manhattan"),

    EUC(1, "distance_measure_euclidean");

    private int distanceMeasureType;

    private String distanceMeasureName;

    DistanceMeasureTypeEnum(int distanceMeasureType, String distanceMeasureName) {
        this.distanceMeasureType = distanceMeasureType;
        this.distanceMeasureName = distanceMeasureName;
    }

    public int getDistanceMeasureType() {
        return distanceMeasureType;
    }

    public void setDistanceMeasureType(int distanceMeasureType) {
        this.distanceMeasureType = distanceMeasureType;
    }

    public String getDistanceMeasureName() {
        return distanceMeasureName;
    }

    public void setDistanceMeasureName(String distanceMeasureName) {
        this.distanceMeasureName = distanceMeasureName;
    }


}
