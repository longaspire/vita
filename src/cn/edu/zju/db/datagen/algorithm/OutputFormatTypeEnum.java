package cn.edu.zju.db.datagen.algorithm;

/**
 * Created by Dell on 2016/1/28.
 */
public enum OutputFormatTypeEnum {

    DETER(0, "output_format_deterministic"),

    PROB(1, "output_format_probabilistic");

    private int outputFormatType;

    private String outputFormatName;

    OutputFormatTypeEnum(int outputFormatType, String outputFormatName) {
        this.outputFormatType = outputFormatType;
        this.outputFormatName = outputFormatName;
    }

    public int getOutputFormatType() {
        return outputFormatType;
    }

    public void setOutputFormatType(int outputFormatType) {
        this.outputFormatType = outputFormatType;
    }

    public String getOutputFormatName() {
        return outputFormatName;
    }

    public void setOutputFormatName(String outputFormatName) {
        this.outputFormatName = outputFormatName;
    }


}
