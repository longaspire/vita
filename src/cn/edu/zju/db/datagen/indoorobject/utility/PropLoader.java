package cn.edu.zju.db.datagen.indoorobject.utility;

import cn.edu.zju.db.datagen.database.DB_Main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropLoader {

    private FileInputStream in;
    private Properties props;
    private String stationType;
    private String stationDistributerType;
    private String stationMaxNumInPart;
    private String stationNumArea;
    private String stationScanRange;
    private String stationScanRate;
    private String movingObjType;
    private String movingObjDistributerType;
    private String movingObjMaxNumInPart;
    private String movingObjMaxStepLength;
    private String movingObjMoveRate;
    private String movingObjMaxLifeSpan;

    public void loadProp(String prop) {
        try {
            in = new FileInputStream(prop);
//			out = new FileOutputStream(prop);
            props = new Properties();
            props.load(in);
        } catch (IOException ex) {
            Logger lgr = Logger.getLogger(DB_Main.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
        }

        stationType = props.getProperty("stationType");
        stationDistributerType = props.getProperty("stationDistributerType");
        stationMaxNumInPart = props.getProperty("stationMaxNumInPart");
        stationNumArea = props.getProperty("stationNumArea");
        stationScanRange = props.getProperty("stationScanRange");
        stationScanRate = props.getProperty("stationScanRate");

        movingObjType = props.getProperty("movingObjType");
        movingObjDistributerType = props.getProperty("movingObjDistributerType");
        movingObjMaxNumInPart = props.getProperty("movingObjMaxNumInPart");
        movingObjMaxStepLength = props.getProperty("movingObjMaxStepLength");
        movingObjMoveRate = props.getProperty("movingObjMoveRate");
        movingObjMaxLifeSpan = props.getProperty("movingObjMaxLifeSpan");

        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public String getStationType() {
        return this.stationType;
    }

    public String getStationDistributerType() {
        return this.stationDistributerType;
    }

    public String getStationMaxNumInpart() {
        return this.stationMaxNumInPart;
    }

    public String getStationNumArea() {
        return this.stationNumArea;
    }

    public String getStationScanRange() {
        return this.stationScanRange;
    }

    public String getStationScanRate() {
        return this.stationScanRate;
    }

    public String getMovingObjType() {
        return this.movingObjType;
    }

    public String getMovingObjDistributerType() {
        return this.movingObjDistributerType;
    }

    public String getMovingObjMaxNumberInPart() {
        return this.movingObjMaxNumInPart;
    }

    public String getMovingObjMaxStepLength() {
        return this.movingObjMaxStepLength;
    }

    public String getMovingObjMoveRate() {
        return this.movingObjMoveRate;
    }

    public String getMaxMovingObjLifeSpan() {
        return this.movingObjMaxLifeSpan;
    }


    public static void main(String[] args) {


        Properties props = new Properties();
        FileInputStream in;
        try {
            in = new FileInputStream("conf/pattern.properties");
            props.load(in);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String movingObjType = props.getProperty("movingObj");
        System.out.println(movingObjType);
//		txtMovingObjectType.setText(movingObjType);
        String distributerType = props.getProperty("distributer");
//		txtDistributerType.setText(distributerType);
        String maxSpeed = props.getProperty("maxSpeed");
//		txtMaxSpeed.setText(maxSpeed);
        String maxMovingNumInPartition = props.getProperty("maxMovingNumInPartition");
//		txtMaxNumInPart.setText(maxMovingNumInPartition);
        String stationType = props.getProperty("station");
//		txtStationType.setText(stationType);

//		PropLoader indoorInitlizer = new PropLoader();
//		indoorInitlizer.loadProp("conf/pattern.properties");
//		
//		indoorInitlizer.setDistributerClass("datagen.indoorobject.movingobject.Test");
//		try {
//			FileOutputStream out = new FileOutputStream("conf/pattern.properties");
//			indoorInitlizer.props.setProperty("distributer", "datagen.indoorobject.movingobject.Test2");
//			indoorInitlizer.props.store(out, null);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		System.out.println(indoorInitlizer.getDistributerClass());

//		Class<?> demo = null;
//		try{
//			demo = Class.forName(indoorInitlizer.getMovingObjClass());
//		} catch(Exception ex){
//			ex.printStackTrace();
//		}
//		
//		MovingObj movingObj = null;
//		try{
//			movingObj = (MovingObj)demo.newInstance();
//		} catch(InstantiationException ex){
//			ex.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		}
//		
//		System.out.println(movingObj.getMaxSpeed());
////		movingObj.moveOneStep();
    }
}
