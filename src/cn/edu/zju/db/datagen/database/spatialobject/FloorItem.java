package cn.edu.zju.db.datagen.database.spatialobject;

abstract class FloorItem extends Item {

    private Floor floor = new Floor();

    public Floor getFloor() {
        return floor;
    }

    public void setFloor(Floor floor) {
        this.floor = floor;
    }
}
