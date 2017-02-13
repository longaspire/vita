package cn.edu.zju.db.datagen.indoorobject.utility;

import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import org.khelekore.prtree.MBRConverter;

public class PartMBRConverter implements MBRConverter<Partition> {

    @Override
    public int getDimensions() {
        return 2;
    }

    @Override
    public double getMin(int axis, Partition t) {
        if (axis == 0) {
            return t.getPolygon2D().getBounds2D().getMinX();
        } else if (axis == 1) {
            return t.getPolygon2D().getBounds2D().getMinY();
        }

        return -1;
    }

    @Override
    public double getMax(int axis, Partition t) {
        if (axis == 0) {
            return t.getPolygon2D().getBounds2D().getMaxX();
        } else if (axis == 1) {
            return t.getPolygon2D().getBounds2D().getMaxY();
        }

        return -1;
    }

}
