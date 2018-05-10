package Util;

import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by jingc on 2018/3/29.
 *
 * 文件类工具
 */
public class FileUtil {

    /**
     * 保存文件
     *
     * @param file
     * @param filePath
     * @param fileName
     * @throws Exception
     */
    public static void uploadFile(byte[] file,String filePath,String fileName) throws Exception{
        File targetFile = new File(filePath);
        if(!targetFile.exists()){
            targetFile.mkdirs();
        }
        FileOutputStream out = new FileOutputStream(filePath+fileName);
        out.write(file);
        out.flush();
        out.close();
    }


    /**
     * 提供安卓和网页文件地址
     *
     * @param time
     * @param i
     * @param mDeviceID
     * @return
     */
    public static String provideFilePath(String time,int i,String mDeviceID){
        String filepath = "/notebooks/tensorflow/jeremyhong/PicServer/inputImage/" + mDeviceID + "-" + "pic-" + time + "-" + i + ".jpg";
        return filepath;
    }

}
