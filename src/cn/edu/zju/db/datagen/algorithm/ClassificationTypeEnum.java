package cn.edu.zju.db.datagen.algorithm;

/**
 * Created by Dell on 2016/1/28.
 */
public enum ClassificationTypeEnum {

    WKNN(0, "algorithm_weighted_kNN"),

    NB(1, "algorithm_naive_bayes"),

    SVM(2, "algorithm_svm");

    private int classificationType;

    private String classificationName;

    ClassificationTypeEnum(int classificationType, String classificationName) {
        this.classificationType = classificationType;
        this.classificationName = classificationName;
    }

    public int getClassificationType() {
        return classificationType;
    }

    public void setClassificationType(int classificationType) {
        this.classificationType = classificationType;
    }

    public String getClassificationName() {
        return classificationName;
    }

    public void setClassificationName(String classificationName) {
        this.classificationName = classificationName;
    }


}
