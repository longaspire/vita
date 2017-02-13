package cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject;

public class Floor {

    private String globalID = null;
    private String name = null;
    private Double elevation = null;
    private String buildingID = null;
    private Integer dbid = null;

    // Empty constructor
    public Floor() {
    }

    public String toString() {
        return name;
    }

    public String getGlobalID() {
        return globalID;
    }

    public void setGlobalID(String globalID) {
        this.globalID = globalID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getElevation() {
        return elevation;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    public String getBuildingID() {
        return buildingID;
    }

    public void setBuildingID(String buildingID) {
        this.buildingID = buildingID;
    }

    public Integer getDbid() {
        return dbid;
    }

    public void setDbid(Integer dbid) {
        this.dbid = dbid;
    }
}
