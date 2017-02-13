package cn.edu.zju.db.datagen.database.spatialobject;

public class AccessRule {

    private Integer AccID = null;
    private String name = null;
    private Connectivity con = null;
    private Boolean direction = null;

    public AccessRule() {
    }

    public Integer getAccID() {
        return AccID;
    }

    public void setAccID(Integer accID) {
        AccID = accID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Connectivity getCon() {
        return con;
    }

    public void setCon(Connectivity con) {
        this.con = con;
    }

    public Boolean getDirection() {
        return direction;
    }

    public void setDirection(Boolean direction) {
        this.direction = direction;
    }
}
