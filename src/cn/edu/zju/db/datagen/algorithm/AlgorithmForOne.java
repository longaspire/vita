package cn.edu.zju.db.datagen.algorithm;

import java.io.*;
import java.util.ArrayList;

public abstract class AlgorithmForOne implements Runnable {

    private String inputFileName;
    private String outputFileName;
    private String comments;

    public AlgorithmForOne() {
        super();
    }

    public AlgorithmForOne(String inputFileName, String outputFileName) {
        super();
        this.inputFileName = inputFileName;
        this.outputFileName = outputFileName;
        this.comments = "";
    }

    public ArrayList<String> calAlgorithmForOneObj(String fileName) {
        ArrayList<String> data = readRecord(fileName);
        ArrayList<String> result = calculateTrajectory(data);
        return result;
    }

    // read from a file in line
    public ArrayList<String> readRecord(String fileName) {
        FileReader fileReader;
        ArrayList<String> list = new ArrayList<String>();
        boolean firstLine = true;
        try {
            fileReader = new FileReader(fileName);
            BufferedReader buff = new BufferedReader(fileReader);
            String line = null;
            while ((line = buff.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                } else {
                    list.add(line);
                }
            }
            fileReader.close();
            return list;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public abstract ArrayList<String> calculateTrajectory(ArrayList<String> records);

    protected void exportResult(ArrayList<String> result, String outputName) {
        // TODO Auto-generated method stub
        File outputFile = new File(outputName);
        try {
            FileWriter writer = new FileWriter(outputFile);
            writer.write(this.getComments());
            for (String tuple : result) {
                writer.write(tuple);
            }
            writer.close();
            // System.out.println(outputName + " end");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public void setInputFileName(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

}
