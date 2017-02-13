package cn.edu.zju.db.datagen.database.spatialobject;

abstract class Item implements Comparable<Object> {

    protected Integer itemID = null;
    protected String globalID = null;
    protected String name = null;

    public Integer getItemID() {
        return itemID;
    }

    public void setItemID(Integer itemID) {
        this.itemID = itemID;
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

    @Override
    public int compareTo(Object arg0) {
        Item anotherItem = (Item) arg0;
        return this.getName().compareTo(anotherItem.getName());
    }
}
