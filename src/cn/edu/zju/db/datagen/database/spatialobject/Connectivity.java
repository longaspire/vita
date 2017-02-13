package cn.edu.zju.db.datagen.database.spatialobject;

import java.util.ArrayList;

public class Connectivity {

    private AccessPoint ap = null;
    private Partition part1 = null;
    private Partition part2 = null;
    private ArrayList<AccessRule> accRules = new ArrayList<AccessRule>();

    public Connectivity() {
        // TODO Auto-generated constructor stub
    }

    public AccessPoint getAp() {
        return ap;
    }

    public void setAp(AccessPoint ap) {
        this.ap = ap;
    }

    public Partition getPart1() {
        return part1;
    }

    public void setPart1(Partition part1) {
        this.part1 = part1;
    }

    public Partition getPart2() {
        return part2;
    }

    public void setPart2(Partition part2) {
        this.part2 = part2;
    }

    public ArrayList<AccessRule> getAccRules() {
        return accRules;
    }

    public void setAccRules(ArrayList<AccessRule> accRules) {
        this.accRules = accRules;
    }

    @Override
    public String toString() {
        return ap + "\t" + part1 + "\t" + part2 + "\t" + accRules;
    }


}
