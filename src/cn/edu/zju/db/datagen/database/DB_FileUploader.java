package cn.edu.zju.db.datagen.database;

import cn.edu.zju.db.datagen.database.spatialobject.UploadObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DB_FileUploader {

    private Connection connection = null;
    private PreparedStatement pst = null;
    private FileInputStream fis = null;

    public DB_FileUploader() {
    }

    public boolean saveObjectToDB(UploadObject object, File file) {
        connection = DB_Connection.connectToDatabase("conf/moovework.properties");
        boolean status = false;

        String query = "INSERT INTO uploads " +
                               "(upload_file_name, " +
                               "upload_file_type, " +
                               "upload_binary_file, " +
                               "upload_created, " +
                               "upload_edited, " +
                               "upload_description) " +
                               "VALUES (?, 'IFC', ?, 'now', 'now', '')";

        try {
            fis = new FileInputStream(file);
//			while(fis.read() != null){
//				
//			}
            pst = connection.prepareStatement(query);
            pst.setString(1, file.getName());
            System.out.println("save object db" + file.getAbsolutePath());
            pst.setBinaryStream(2, fis, (int) file.length());

            pst.executeUpdate();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException er) {
            er.printStackTrace();
        } finally {
            try {
                if (pst != null)
                    pst.close();
                if (connection != null)
                    connection.close();
                if (fis != null)
                    fis.close();
                status = true;
            } catch (IOException | SQLException eps) {
                eps.printStackTrace();
            }
        }
        return status;
    }
}
