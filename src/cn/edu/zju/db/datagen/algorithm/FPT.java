package cn.edu.zju.db.datagen.algorithm;

import cn.edu.zju.db.datagen.database.DB_WrapperLoad;
import cn.edu.zju.db.datagen.database.spatialobject.Floor;
import cn.edu.zju.db.datagen.indoorobject.movingobject.MovingObj;
import cn.edu.zju.db.datagen.indoorobject.station.RSSIPackage;
import cn.edu.zju.db.datagen.indoorobject.station.Station;
import cn.edu.zju.db.datagen.indoorobject.utility.IdrObjsUtility;
import libsvm.LibSVM;
import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.classification.bayes.NaiveBayesClassifier;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.distance.EuclideanDistance;
import net.sf.javaml.distance.ManhattanDistance;
import net.sf.javaml.tools.data.FileHandler;
import org.khelekore.prtree.MBR2D;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class FPT extends Algorithm {

    public static String outputFileName = "//fingerprinting_record_object";
    public static int seedSize = 10;

    public static String radioMap_path = "//radio_map.data";
    private Classifier classifier;

    private double grid_size;
    private double margin_left;
    private double margin_right;
    private int maximum_radiomap_records_for_each;
    private int minimum_radiomap_records_for_each;
    private int k;
    private int positioningAlgorithmType;
    private int distanceMeasureType;
    private DistanceMeasure dm;
    private int outputFormatType;
    private int maximumSampleNumber;
    private boolean logarithmic;
    private Dataset radiomap;


    public FPT(String proPropPath, String txtInputPath, String txtOutputPath) {

        super(proPropPath, txtInputPath, txtOutputPath);

        if (Boolean.parseBoolean(this.getProps().getProperty("reference_points_distributer_uniform"))) {

            if (Boolean.parseBoolean(this.getProps().getProperty("output_format_probabilistic"))) {
                this.outputFormatType = OutputFormatTypeEnum.PROB.getOutputFormatType();
            } else {
                this.outputFormatType = OutputFormatTypeEnum.DETER.getOutputFormatType();
            }

            grid_size = Double.parseDouble(this.getProps().getProperty("param_grid_size"));
            margin_left = Double.parseDouble(this.getProps().getProperty("param_reference_points_margin_left"));
            margin_right = Double.parseDouble(this.getProps().getProperty("param_reference_points_margin_right"));
            maximumSampleNumber = Integer.parseInt(this.getProps().getProperty("param_maximum_sample_number"));

            if (Boolean.parseBoolean(this.getProps().getProperty("param_distance_measure_manhattan"))) {
                this.setDistanceMeasureType(DistanceMeasureTypeEnum.MAN.getDistanceMeasureType());
                this.dm = new ManhattanDistance();
            } else {
                this.setDistanceMeasureType(DistanceMeasureTypeEnum.EUC.getDistanceMeasureType());
                this.dm = new EuclideanDistance();
            }

            maximum_radiomap_records_for_each = Integer
                                                        .parseInt(this.getProps().getProperty("param_maximum_radiomap_records_for_each"));
            minimum_radiomap_records_for_each = Integer
                                                        .parseInt(this.getProps().getProperty("param_minimum_radiomap_records_for_each"));
            this.setRadiomap(buildRadioMap(grid_size, maximum_radiomap_records_for_each,
                    minimum_radiomap_records_for_each, margin_left, margin_right));

            if (Boolean.parseBoolean(this.getProps().getProperty("algorithm_weighted_kNN"))) {

                positioningAlgorithmType = ClassificationTypeEnum.WKNN.getClassificationType();

                k = Integer.parseInt(this.getProps().getProperty("param_k"));


                this.classifier = new KNearestNeighbors(k * seedSize, this.dm);
                this.classifier.buildClassifier(radiomap);

                System.out.println("build classifier successful!");

                // int correct = 0, wrong = 0;
                //
                // for (Instance instance : radiomap.subList(0, 100)) {
                // Object predictedClassValue =
                // this.classifier.classify(instance);
                // System.out.println(this.classifier.classDistribution(instance));
                // Object realClassValue = instance.classValue();
                // System.out.println(predictedClassValue + "," +
                // realClassValue);
                // if (predictedClassValue.equals(realClassValue))
                // correct++;
                // else
                // wrong++;
                // }
                //
                // System.out.println(correct);
                // System.out.println(wrong);

            } else if (Boolean.parseBoolean(this.getProps().getProperty("algorithm_naive_bayes"))) {

                positioningAlgorithmType = ClassificationTypeEnum.NB.getClassificationType();

                logarithmic = Boolean.parseBoolean(this.getProps().getProperty("param_logarithmic"));

                this.classifier = new NaiveBayesClassifier(true, logarithmic, true);
//				System.out.println(radiomap.size());
                this.classifier.buildClassifier(radiomap);

                System.out.println("build classifier successful!");

            } else if (Boolean.parseBoolean(this.getProps().getProperty("algorithm_svm"))) {

                positioningAlgorithmType = ClassificationTypeEnum.SVM.getClassificationType();


                this.classifier = new LibSVM();
                this.classifier.buildClassifier(radiomap);

                System.out.println("build classifier successful!");

            } else {
                System.out.println(this.getProps().getProperty("algorithm_weighted_kNN"));
            }

        } else {
            System.out.println(this.getProps().getProperty("reference_points_distributer_uniform"));
        }

        // System.out.println(this.radiomap);

    }

    private Dataset buildRadioMap(double gs, int maxrr, int minrr, double ml, double mr) {
        // TODO Auto-generated method stub

        Dataset dataset = new DefaultDataset();

        if (IdrObjsUtility.rp_id_count > 0) {
            IdrObjsUtility.rp_id_count = 0;
        }

        buildReferencePoints(gs, ml, mr);

        Random random = new Random();

        List<RadioMapRecord> rmrs = new ArrayList<RadioMapRecord>();

        for (Floor floor : DB_WrapperLoad.floorT) {
            for (ReferencePoint rp : floor.getReferencePoints()) {
                int times = minrr + (int) (random.nextDouble() * (maxrr - minrr));
                for (int i = 0; i < times; i++) {
                    RadioMapRecord rmr = generateRadioMapRecord(rp, floor);
                    if (rmr.getRssiVector().size() > 0) {
                        dataset.add(rmr.convertToInstance());
                        rmrs.add(rmr);
                    }
                }
            }
        }

        String outputName = this.getOutputPath() + radioMap_path;
        exportRadioMap(dataset, outputName);

        return dataset;

    }

    private void exportRadioMap(Dataset data, String outputName) {

        File outputFile = new File(outputName);
        try {
            FileHandler.exportDataset(data, outputFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private RadioMapRecord generateRadioMapRecord(ReferencePoint rp, Floor floor) {
        // TODO Auto-generated method stub

        double xmin = rp.getLocation().getX() - MovingObj.scanRange;
        double ymin = rp.getLocation().getY() - MovingObj.scanRange;
        double xmax = rp.getLocation().getX() + MovingObj.scanRange;
        double ymax = rp.getLocation().getY() + MovingObj.scanRange;

        RadioMapRecord rmr = new RadioMapRecord();
        rmr.setGroundTruthId(rp.getRp_id() + "");
        rmr.setGroundTruthLocation(rp.getLocation());
        SortedMap<Integer, Integer> rssiVector = new TreeMap<Integer, Integer>();
        for (Station station : floor.getStationsRTree().find(xmin, ymin, xmax, ymax)) {
            double measureDist;
            if ((measureDist = rp.getLocation().distance(station.getCurrentLocation())) <= MovingObj.scanRange) {
                // stations.add(station);
                RSSIPackage pack = (RSSIPackage) station.createPackage(measureDist);
                rssiVector.put(station.getId(), pack.getRSSI());
            }
        }
        rmr.setRssiVector(rssiVector);
        return rmr;
    }

    private void buildReferencePoints(double gs, double ml, double mr) {
        // TODO Auto-generated method stub

        for (Floor floor : DB_WrapperLoad.floorT) {
            if (floor.getPartitionsRTree() != null && floor.getPartitionsRTree().getNumberOfLeaves() > 0) {
                List<ReferencePoint> rps_onefloor = generateReferencePointsForOneFloor(floor,
                        floor.getPartitionsRTree().getMBR2D(), gs, ml, mr);
                floor.setReferencePoints(rps_onefloor);
            }
        }
        // System.out.println("------------");
    }

    private List<ReferencePoint> generateReferencePointsForOneFloor(Floor floor, MBR2D mbr2d, double gs, double ml,
                                                                    double mr) {
        // TODO Auto-generated method stub

        List<ReferencePoint> results = new ArrayList<ReferencePoint>();

        double x = 0;
        double y = 0;

        int count = 0;
        int scount = 0;

        for (x = mbr2d.getMinX() + ml; x < mbr2d.getMaxX() - mr; x += gs) {
            for (y = mbr2d.getMinY() + ml; y < mbr2d.getMaxY() - mr; y += gs) {

                Point2D.Double newPoint = new Point2D.Double(x, y);
                if (IdrObjsUtility.findPartitionForPoint(floor, newPoint) != null) {
                    ReferencePoint rp = new ReferencePoint(newPoint);
                    results.add(rp);
                    scount++;
                }
                count++;
            }
        }

        // System.out.println(count);
        System.out.println(scount + " reference points were generated!(" + (double) scount / count * 100 + "%)");

        return results;
    }

    @Override
    public int calAlgorithmForAll(ExecutorService threadPool) {
        // TODO Auto-generated method stub

        File file = new File(this.getInputPath());
        File[] files = file.listFiles();

        for (File subFile : files) {
            String outputName = this.getOutputPath() + outputFileName + subFile.getName().substring(9);
            FPTForOne ffo = new FPTForOne(subFile.getAbsolutePath(), outputName, this.positioningAlgorithmType, this.classifier, this.k, this.outputFormatType, this.maximumSampleNumber);
//			Thread thread = new Thread(ffo);
//			thread.start();
            threadPool.execute(ffo);
        }

        return 1;
    }

    public static String getRadioMap_path() {
        return radioMap_path;
    }

    public static void setRadioMap_path(String radioMap_path) {
        FPT.radioMap_path = radioMap_path;
    }

    public Classifier getClassifier() {
        return this.classifier;
    }

    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

    public double getGrid_size() {
        return grid_size;
    }

    public void setGrid_size(double grid_size) {
        this.grid_size = grid_size;
    }

    public double getMargin_left() {
        return margin_left;
    }

    public void setMargin_left(double margin_left) {
        this.margin_left = margin_left;
    }

    public double getMargin_right() {
        return margin_right;
    }

    public void setMargin_right(double margin_right) {
        this.margin_right = margin_right;
    }

    public int getMaximum_radiomap_records_for_each() {
        return maximum_radiomap_records_for_each;
    }

    public void setMaximum_radiomap_records_for_each(int maximum_radiomap_records_for_each) {
        this.maximum_radiomap_records_for_each = maximum_radiomap_records_for_each;
    }

    public int getMinimum_radiomap_records_for_each() {
        return minimum_radiomap_records_for_each;
    }

    public void setMinimum_radiomap_records_for_each(int minimum_radiomap_records_for_each) {
        this.minimum_radiomap_records_for_each = minimum_radiomap_records_for_each;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public int getPositioningAlgorithmType() {
        return positioningAlgorithmType;
    }

    public void setPositioningAlgorithmType(int positioningAlgorithmType) {
        this.positioningAlgorithmType = positioningAlgorithmType;
    }

    public Dataset getRadiomap() {
        return radiomap;
    }

    public void setRadiomap(Dataset radiomap) {
        this.radiomap = radiomap;
    }

    public int getOutputFormatType() {
        return outputFormatType;
    }

    public void setOutputFormatType(int outputFormatType) {
        this.outputFormatType = outputFormatType;
    }

    public int getDistanceMeasureType() {
        return distanceMeasureType;
    }

    public void setDistanceMeasureType(int distanceMeasureType) {
        this.distanceMeasureType = distanceMeasureType;
    }

    public boolean isLogarithmic() {
        return logarithmic;
    }

    public void setLogarithmic(boolean logarithmic) {
        this.logarithmic = logarithmic;
    }

    public int getMaximumSampleNumber() {
        return maximumSampleNumber;
    }

    public void setMaximumSampleNumber(int maximumSampleNumber) {
        this.maximumSampleNumber = maximumSampleNumber;
    }


}
