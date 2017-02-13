package cn.edu.zju.db.datagen.algorithm;

import cn.edu.zju.db.datagen.indoorobject.station.RSSIPackage;
import cn.edu.zju.db.datagen.indoorobject.station.Station;
import cn.edu.zju.db.datagen.indoorobject.utility.IdrObjsUtility;
import net.sf.javaml.classification.Classifier;
import net.sf.javaml.core.Instance;

import java.util.*;
import java.util.Map.Entry;

public class FPTForOne extends AlgorithmForOne {

    private Classifier classifier;
    private int k;
    private int outputFormatType;
    private int positioningAlgorithmType;
    private int maximumSampleNumber;

    public FPTForOne(String inputFileName, String outputFileName) {
        super(inputFileName, outputFileName);
        // TODO Auto-generated constructor stub
    }

    public FPTForOne(String inputFileName, String outputFileName, int positioningAlgorithmType, Classifier classifier,
                     int k, int outputFormatType, int maximumSampleNumber) {
        super(inputFileName, outputFileName);
        // TODO Auto-generated constructor stub

        if (outputFormatType == OutputFormatTypeEnum.DETER.getOutputFormatType()) {
            String comments = "floorId" + "\t" + "partitionId" + "\t" + "location_x" + "\t" + "location_y" + "\t"
                                      + "timeStamp" + "\n";
            this.setComments(comments);
        } else {
            String comments = "samples" + "\t" + "timeStamp" + "\n";
            this.setComments(comments);
        }

        this.setClassifier(classifier);
        this.k = k;
        this.setOutputFormatType(outputFormatType);
        this.setPositioningAlgorithmType(positioningAlgorithmType);
        if (this.positioningAlgorithmType == ClassificationTypeEnum.WKNN.getClassificationType()) {
            this.maximumSampleNumber = Math.min(maximumSampleNumber, k);
        } else {
            this.maximumSampleNumber = maximumSampleNumber;
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        // System.out.println(this.inputFileName + "," + this.outputFileName);
        ArrayList<String> result = calAlgorithmForOneObj(this.getInputFileName());
        exportResult(result, this.getOutputFileName());
    }

    @Override
    public ArrayList<String> calculateTrajectory(ArrayList<String> records) {
        // TODO Auto-generated method stub

        java.text.DecimalFormat df = new java.text.DecimalFormat("#0.00");

        ArrayList<RSSIPackage> packTemps = new ArrayList<RSSIPackage>();
        ArrayList<String> result = new ArrayList<String>();
        // boolean currentRecord = false;
        // Point2D.Double currentPosition = null;
        boolean calculated = false;
        String currentTimeStamp = "";
        for (String record : records) {
            if (!record.equals("")) {
                List<String> lists = Arrays.asList(record.split("\t"));
                Double x = Double.parseDouble(lists.get(1));
                Double y = Double.parseDouble(lists.get(2));
                int rssi = Integer.parseInt(lists.get(3));
                RSSIPackage packTemp = new RSSIPackage(x, y, rssi);
                int stationId = Integer.parseInt(lists.get(0));
                packTemp.setFromID(stationId);
                currentTimeStamp = lists.get(4);
                packTemps.add(packTemp);
                if (calculated == true) {
                    calculated = false;
                }
            } else if (calculated == false) {

                if (this.outputFormatType == OutputFormatTypeEnum.DETER.getOutputFormatType()) {
                    IndoorLocation resultPoint = null;
                    if (this.positioningAlgorithmType == ClassificationTypeEnum.WKNN.getClassificationType())
                        resultPoint = calculateLocationDeterWKNN(packTemps);
                    else
                        resultPoint = calculateLocationDeter(packTemps);

                    if (resultPoint != null && (!currentTimeStamp.equals(""))) {
                        result.add(resultPoint.toString() + currentTimeStamp + "\n");
                    }

                } else {
                    SortedMap<Double, IndoorLocation> resultPoints = calculateLocationProb(packTemps);
                    if (resultPoints != null && resultPoints.size() > 0 && (!currentTimeStamp.equals(""))) {
                        String resultStr = "";
                        for (Entry<Double, IndoorLocation> item : resultPoints.entrySet()) {
                            String temp = "[(" + item.getValue().getX() + "," + item.getValue().getY() + ","
                                                  + item.getValue().getFloor().getItemID() + "), " + df.format(item.getKey()) + "]";
                            resultStr = resultStr + temp + "\t";
                        }
                        resultStr = resultStr + "\t" + currentTimeStamp + "\n";
                        result.add(resultStr);
                    }
                }


                packTemps.clear();
                calculated = true;
            }
        }
        return result;
    }

    private IndoorLocation calculateLocationDeterWKNN(ArrayList<RSSIPackage> packs) {
        // TODO Auto-generated method stub

        SortedMap<Integer, Integer> rssiVector = new TreeMap<Integer, Integer>();

        for (RSSIPackage pack : packs) {
            // System.out.println(pack.toString());
            rssiVector.put(pack.getFromID(), pack.getRSSI());
        }

        RadioMapRecord rmr = new RadioMapRecord();
        rmr.setRssiVector(rssiVector);
        Instance insta = rmr.convertToInstance();

        Map<Object, Double> distribution = this.classifier.classDistribution(insta);
        SortedMap<Double, Station> nnMap = new TreeMap<Double, Station>();
        // System.out.println(distribution);
        for (Entry<Object, Double> entry : distribution.entrySet()) {
            if (entry.getValue() > 0) {
                nnMap.put(entry.getValue(), IdrObjsUtility.allStations.get(Integer.parseInt((String) entry.getKey())));
            }
        }
        // System.out.println(nnMap);
        if (nnMap.size() == 0) {
            return null;
        } else if (nnMap.size() == 1) {
            Station station = nnMap.get(nnMap.lastKey());
            IndoorLocation location = new IndoorLocation(station.getCurrentLocation(), station.getCurrentFloor());
            return location;
        } else {
            Station station = nnMap.get(nnMap.lastKey());
            int count = 0;
            double probSum = 0;
            double x = 0;
            double y = 0;
            for (Entry<Double, Station> item : nnMap.entrySet()) {
                if (count < this.k) {
                    probSum += item.getKey();
                } else {
                    break;
                }
                count++;
            }
            count = 0;
            for (Entry<Double, Station> item : nnMap.entrySet()) {
                if (count < this.k) {
                    Station sta = item.getValue();
                    x = x + (double) (item.getKey() / probSum) * sta.getCurrentLocation().getX();
                    y = y + (double) (item.getKey() / probSum) * sta.getCurrentLocation().getY();
                } else {
                    break;
                }
                count++;
            }
            IndoorLocation location = new IndoorLocation(x, y, station.getCurrentFloor());
            return location;
        }

        // int stationId = (int) this.classifier.classify(insta);
        // // System.out.println(stationId);
        // if (IdrObjsUtility.allStations.containsKey(stationId)) {
        //
        // Station station = IdrObjsUtility.allStations.get(stationId);
        // IndoorLocation location = new IndoorLocation(station.getCurrentLocation(),
        // station.getCurrentFloor().getItemID());
        // return location;
        //
        // }
        // return null;

    }

    private IndoorLocation calculateLocationDeter(ArrayList<RSSIPackage> packs) {
        // TODO Auto-generated method stub

        SortedMap<Integer, Integer> rssiVector = new TreeMap<Integer, Integer>();

        for (RSSIPackage pack : packs) {
            // System.out.println(pack.toString());
            rssiVector.put(pack.getFromID(), pack.getRSSI());
        }

        RadioMapRecord rmr = new RadioMapRecord();
        rmr.setRssiVector(rssiVector);
        Instance insta = rmr.convertToInstance();

        int stationId = Integer.parseInt((String) this.classifier.classify(insta));
        // System.out.println(stationId);
        if (IdrObjsUtility.allStations.containsKey(stationId)) {

            Station station = IdrObjsUtility.allStations.get(stationId);
            IndoorLocation location = new IndoorLocation(station.getCurrentLocation(), station.getCurrentFloor());
            return location;

        }
        return null;

    }

    private SortedMap<Double, IndoorLocation> calculateLocationProb(ArrayList<RSSIPackage> packs) {
        // TODO Auto-generated method stub

        SortedMap<Double, IndoorLocation> resultPoints = new TreeMap<Double, IndoorLocation>();

        SortedMap<Integer, Integer> rssiVector = new TreeMap<Integer, Integer>();

        for (RSSIPackage pack : packs) {
            // System.out.println(pack.toString());
            rssiVector.put(pack.getFromID(), pack.getRSSI());
        }

        RadioMapRecord rmr = new RadioMapRecord();
        rmr.setRssiVector(rssiVector);
        Instance insta = rmr.convertToInstance();

        Map<Object, Double> distribution = this.classifier.classDistribution(insta);
        SortedMap<Double, Station> nnMap = new TreeMap<Double, Station>();
        for (Entry<Object, Double> entry : distribution.entrySet()) {
            if (entry.getValue() >= 0.01) {
                nnMap.put(entry.getValue(), IdrObjsUtility.allStations.get(Integer.parseInt((String) entry.getKey())));
            }
        }


        if (nnMap.size() == 0) {

            ;

        } else {
//			System.out.println(nnMap);
//			System.out.println(this.maximumSampleNumber);
            int count = 0;
            double probSum = 0;
            for (Entry<Double, Station> item : nnMap.entrySet()) {
                if (count < this.maximumSampleNumber) {
                    probSum += item.getKey();
                } else {
                    break;
                }
                count++;
            }
            count = 0;
            for (Entry<Double, Station> item : nnMap.entrySet()) {
                if (count < this.maximumSampleNumber) {
                    Station station = item.getValue();
                    IndoorLocation location = new IndoorLocation(station.getCurrentLocation(), station.getCurrentFloor());
                    resultPoints.put((double) item.getKey() / probSum, location);
                } else {
                    break;
                }
                count++;
            }
//			System.out.println(resultPoints);
        }

        return resultPoints;

    }

    public Classifier getClassifier() {
        return classifier;
    }

    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

    public int getOutputFormatType() {
        return outputFormatType;
    }

    public void setOutputFormatType(int outputFormatType) {
        this.outputFormatType = outputFormatType;
    }

    public int getPositioningAlgorithmType() {
        return positioningAlgorithmType;
    }

    public void setPositioningAlgorithmType(int positioningAlgorithmType) {
        this.positioningAlgorithmType = positioningAlgorithmType;
    }

    public int getMaximumSampleNumber() {
        return maximumSampleNumber;
    }

    public void setMaximumSampleNumber(int maximumSampleNumber) {
        this.maximumSampleNumber = maximumSampleNumber;
    }

}
