package cn.edu.zju.db.datagen.indoorobject.movingobject.factory;

import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import cn.edu.zju.db.datagen.indoorobject.movingobject.MovingObj;

import java.util.ArrayList;

public interface SimpleMovingObjCreateInterface {

    public abstract void generateMovingObjsInPart(Partition partition,
                                                  ArrayList<MovingObj> movingObjs, int pointNum, String movingObjType);

}
