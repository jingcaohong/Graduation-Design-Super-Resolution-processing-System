package Util;

import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by jingc on 2018/4/3.
 *
 * 图片类工具
 */
public class ImgUtil {

    /**
     * 初始化
     */
    private ImgUtil(){}


    /**
     * 对本地图片进行Base64位编码
     *
     * @param imageFilePath
     *
     * @return
     */
    public static String encodeImageToBase64(String imageFilePath){

        ByteArrayOutputStream outputStream = null;
        try{
            //读取文件
            File ImageFile = new File(imageFilePath);
            BufferedImage bufferedImage = ImageIO.read(ImageFile);
            outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage,"jpg",outputStream);
        }catch (MalformedURLException e1){
            e1.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        //对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        return "data:image/jpeg;base64,"+encoder.encode(outputStream.toByteArray());//返回Base64编码过的字节数组
    }


}
