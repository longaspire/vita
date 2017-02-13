package cn.edu.zju.db.datagen.indoorobject.movingobject.factory;

import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import cn.edu.zju.db.datagen.indoorobject.movingobject.MovingObj;

import java.util.ArrayList;

public abstract class MovingObjectsFactory implements SimpleMovingObjCreateInterface {

    public MovingObjectsFactory() {
        // TODO Auto-generated constructor stub
    }

    public abstract void generateMovingObjsInPart(Partition partition, ArrayList<MovingObj> movingObjs, int pointNum,
                                                  String movingObjType);

    public String toString() {
        return this.getClass().getSimpleName();
    }
}
