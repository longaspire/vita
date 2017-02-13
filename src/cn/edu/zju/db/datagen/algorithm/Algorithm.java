package cn.edu.zju.db.datagen.algorithm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

public abstract class Algorithm {

    public static boolean exportFlag = false;

    private String inputPath;
    private String outputPath;
    private Properties props;

    public Algorithm() {
        // TODO Auto-generated constructor stub
    }

    public Algorithm(String proPropPath, String txtInputPath, String txtOutputPath) {

        Properties props = new Properties();
        FileInputStream in;

        try {
            in = new FileInputStream(proPropPath);
            props.load(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.inputPath = txtInputPath;
        this.outputPath = txtOutputPath;
        this.props = props;
        File dir = new File(outputPath);
        dir.mkdirs();
    }

    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
    }

    public abstract int calAlgorithmForAll(ExecutorService threadPool);


}
