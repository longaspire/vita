package cn.edu.zju.db.datagen.ifc.dataextraction;

import openifctools.com.openifcjavatoolbox.ifc2x3tc1.*;
import openifctools.com.openifcjavatoolbox.ifcmodel.IfcModel;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class IfcFileParser {

    public static IfcModel ifcModel;
    private static Double multiplier;
    protected static Double t_small;

    public static void readFile(File file) throws Exception {
        ifcModel = new IfcModel();
        ifcModel.readStepFile(file);
        System.out.println("Number of element:" + ifcModel.getNumberOfElements());
        findMeasureUnit();
    }

    // Find out if measure unit is in meters or millimeters and set multipliers accordingly
    private static void findMeasureUnit() {
        @SuppressWarnings("unchecked")
        Collection<IfcProject> projects = (Collection<IfcProject>) ifcModel.getCollection(IfcProject.class);
        Iterator<IfcProject> iter;
        iter = projects.iterator();
        while (iter.hasNext()) {
            IfcProject project = iter.next();
            IfcUnitAssignment unitAssign = project.getUnitsInContext();
            SET<IfcUnit> units = unitAssign.getUnits();
            Iterator<IfcUnit> itera;
            itera = units.iterator();
            while (itera.hasNext()) {
                IfcUnit unit = itera.next();
                if (unit instanceof IfcSIUnit) {
                    IfcSIUnit unit1 = (IfcSIUnit) unit;
                    IfcUnitEnum unitEnum = unit1.getUnitType();
                    if (unitEnum.value.name().equals("LENGTHUNIT")) {
                        IfcSIUnitName unitName = (IfcSIUnitName) unit1.getName();
                        IfcSIPrefix preFix = null;
                        if (unit1.getPrefix() != null) {
                            preFix = unit1.getPrefix();
                        }
                        if (unitName.value.name().equals("METRE") && preFix == null) {
                            multiplier = (double) 1000;
                            t_small = 0.10;
                        } else if (unitName.value.name().equals("METRE") && preFix.value.name().equals("MILLI")) {
                            multiplier = (double) 1;
                            t_small = 100.0;
                        }
                    }
                } else {
                    continue;
                }
            }
        }
    }

    // Converts a Double to millimeters
    public static Double convSI(Double d) {
        d = d * multiplier;
        return d;
    }

    // Converts a Point2d to millimeters
    public static Point2D.Double convSIpt(Point2D.Double d) {
        d.setLocation(d.x * multiplier, d.y * multiplier);
        return d;
    }

    // Converts an arraylist of Point2Ds to millimeters
    public static ArrayList<Point2D.Double> convSIpoly(ArrayList<Point2D.Double> d) {

        for (int i = 0; i < d.size(); i++) {
            d.get(i).setLocation(d.get(i).x * multiplier, d.get(i).y * multiplier);
        }
        return d;
    }

    public IfcFileParser() {
    }
}
