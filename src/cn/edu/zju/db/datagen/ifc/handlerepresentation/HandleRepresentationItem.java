package cn.edu.zju.db.datagen.ifc.handlerepresentation;

import cn.edu.zju.db.datagen.ifc.dataextraction.IfcFileParser;
import openifctools.com.openifcjavatoolbox.ifc2x3tc1.*;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.Collection;

/**
 * Created by alex9 on 2016/8/12.
 */
public class HandleRepresentationItem {

    public static Point2D.Double positionFromProductDefinitionShape(IfcProductDefinitionShape definitionShape) {
        for (IfcRepresentation ifcRepresentation : definitionShape.getRepresentations()) {
            positionFromIfcRepresentation(ifcRepresentation);
        }
        return null;
    }

    public static void positionFromIfcRepresentation(IfcRepresentation ifcRepresentation) {
        for (IfcRepresentationItem ifcRepresentationItem : ifcRepresentation.getItems()) {
            positionFromIfcRepresentationItem(ifcRepresentationItem);
        }
    }


    public static void positionFromIfcRepresentationItem(IfcRepresentationItem representationItem) {
        if (representationItem instanceof IfcMappedItem) {
            HandleMappedItem.positionFromIfcRepresentationMap((IfcMappedItem) representationItem);
        }

        if (representationItem instanceof IfcGeometricRepresentationItem) {
            HandleGeometricRepresentationItem.positionFromIfcGeomRepresItem((IfcGeometricRepresentationItem)representationItem);
        }

        if (representationItem instanceof IfcStyledItem) {
            HandleStyledItem.positionFromIfcStyledItem((IfcStyledItem) representationItem);
        }

        if (representationItem instanceof IfcTopologicalRepresentationItem) {
            HandleTopoRepresItem.positionFromIfcTopoRepresItem((IfcTopologicalRepresentationItem) representationItem);
        }
    }


    public static void main(String[] args) throws Exception {
        File file = new File("D:\\JavaWorkspace\\datagenerator\\IFC Files\\Clinic_A_20110906_optimized.ifc");
        IfcFileParser.readFile(file);
        Collection<IfcDoor> ifcDoors = (Collection<IfcDoor>) IfcFileParser.ifcModel.getCollection(IfcDoor.class);
        for (IfcDoor door : ifcDoors) {
            positionFromProductDefinitionShape((IfcProductDefinitionShape) door.getRepresentation());
        }

    }

}
