package cn.edu.zju.db.datagen.algorithm;

import java.io.File;
import java.util.concurrent.ExecutorService;

public class TRI extends Algorithm {

    public static String outputFileName = "//trilateration_record_object";

    private int n;
    private double rssiAt1;

    public TRI(String proPropPath, String txtInputPath, String txtOutputPath) {

        super(proPropPath, txtInputPath, txtOutputPath);

        n = Integer.parseInt(this.getProps().getProperty("n"));
        rssiAt1 = Double.parseDouble(this.getProps().getProperty("rssiAt1"));
    }

    @Override
    public int calAlgorithmForAll(ExecutorService threadPool) {
        // TODO Auto-generated method stub

        File file = new File(this.getInputPath());
        File[] files = file.listFiles();
        for (File subFile : files) {

            String outputName = this.getOutputPath() + outputFileName + subFile.getName().substring(9);
            TRIForOne tfo = new TRIForOne(subFile.getAbsolutePath(), outputName, n, rssiAt1);
//			Thread thread = new Thread(tfo);
//			thread.start();
            threadPool.execute(tfo);

        }

        return 1;
    }

}
