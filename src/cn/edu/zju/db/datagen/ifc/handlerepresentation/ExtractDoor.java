package cn.edu.zju.db.datagen.ifc.handlerepresentation;

import cn.edu.zju.db.datagen.ifc.dataextraction.spatialobject.Door;
import javafx.geometry.Point3D;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.*;
import openifctools.com.openifcjavatoolbox.ifcmodel.IfcModel;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by alex9 on 2016/8/12.
 */
public class ExtractDoor {

    @SuppressWarnings("unchecked")
    public static List<Door> getAllDoors(IfcModel ifcModel) {
        List<Door> doors = new ArrayList<>();
        Collection<IfcDoor> ifcDoors = (Collection<IfcDoor>) ifcModel.getCollection(IfcDoor.class);
        for (IfcDoor ifcDoor : ifcDoors) {
            Door door = extract(ifcDoor);
            doors.add(door);
        }
        return doors;
    }

    private static Door extract(IfcDoor ifcDoor) {
        Door door = new Door();
        door.setGlobalID(ifcDoor.getGlobalId().toString());
        door.setName(ifcDoor.getName().toString());
        return null;
    }

    private static Point2D.Double getCoordinate(IfcDoor door) {
        return null;
    }

    private static Point3D getDirection(IfcDoor ifcDoor) {
        double x, y, z;
        Point3D point3D;
        IfcLocalPlacement localPlacement = (IfcLocalPlacement) ifcDoor.getObjectPlacement();
        IfcLocalPlacement placementRelTo = (IfcLocalPlacement) localPlacement.getPlacementRelTo();
        IfcAxis2Placement3D relativePlacement = (IfcAxis2Placement3D)localPlacement.getRelativePlacement();

        if (relativePlacement.getRefDirection() != null) {
            LIST<DOUBLE> directionRatios = relativePlacement.getRefDirection().getDirectionRatios();
        }
        return null;

    }

    private static Line2D.Double calDoorLine() {
        return null;
    }
}
