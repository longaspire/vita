package cn.edu.zju.db.datagen.indoorobject.utility;

import cn.edu.zju.db.datagen.indoorobject.station.Station;
import org.khelekore.prtree.MBRConverter;


public class StationMBRConverter implements MBRConverter<Station> {


    @Override
    public int getDimensions() {
        return 2;
    }

    @Override
    public double getMin(int axis, Station t) {
        if (axis == 0) {
            return t.getCurrentLocation().getX();
        } else if (axis == 1) {
            return t.getCurrentLocation().getY();
        }

        return -1;
    }

    @Override
    public double getMax(int axis, Station t) {
        if (axis == 0) {
            return t.getCurrentLocation().getX();
        } else if (axis == 1) {
            return t.getCurrentLocation().getY();
        }

        return -1;
    }

}
