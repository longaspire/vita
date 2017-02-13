package cn.edu.zju.db.datagen.algorithm;

import java.io.File;
import java.util.concurrent.ExecutorService;

public class PXM extends Algorithm {

    public static String outputFileName = "//proximity_record_object";

    public PXM(String proPropPath, String txtInputPath, String txtOutputPath) {
        super(proPropPath, txtInputPath, txtOutputPath);
    }

    @Override
    public int calAlgorithmForAll(ExecutorService threadPool) {
        // TODO Auto-generated method stub
        File file = new File(this.getInputPath());
        File[] files = file.listFiles();
        for (File subFile : files) {
            String outputName = this.getOutputPath() + outputFileName + subFile.getName().substring(9);
            PXMForOne pfo = new PXMForOne(subFile.getAbsolutePath(), outputName);
//			Thread thread = new Thread(pfo);
//			thread.start();
            threadPool.execute(pfo);
        }
        return 1;
    }

}
