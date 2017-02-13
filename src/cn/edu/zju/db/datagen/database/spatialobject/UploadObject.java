package cn.edu.zju.db.datagen.database.spatialobject;

import java.io.File;
import java.sql.Timestamp;

public class UploadObject {

    private Integer upload_id = 0;
    private String filename = "";
    private String file_type = "IFC";
    private Integer file_size = 0;
    private File file_uploaded = null;
    private Timestamp created = null;
    private Timestamp edited = null;
    private String description = "";

    public void setUploadId(int upload_id) {
        this.upload_id = upload_id;
    }

    public Integer getUploadId() {
        return this.upload_id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFile_type() {
        return file_type;
    }

    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }

    public Integer getFile_size() {
        return file_size;
    }

    public void setFile_size(Integer file_size) {
        this.file_size = file_size;
    }

    public File getFile_uploaded() {
        return file_uploaded;
    }

    public void setFile_uploaded(File file_uploaded) {
        this.file_uploaded = file_uploaded;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public Timestamp getEdited() {
        return edited;
    }

    public void setEdited(Timestamp edited) {
        this.edited = edited;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return upload_id + ". " + filename;
    }

}
