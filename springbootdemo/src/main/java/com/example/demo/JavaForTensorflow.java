package com.example.demo;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;

/**
 * Created by jingcaohong on 2018/3/20.
 *
 * 调用tensorflow对图片进行处理
 *
 */
@Component
public class JavaForTensorflow {

    private static final Log LOGGER = LogFactory.getLog(JavaForTensorflow.class);

    private static final int pix = 255;

    @Value("${web.output-img-path}")
    private String outputPath;

    /**
     * 调用model对图片进行处理
     *
     * @param filePos
     * @param time
     * @param picIndex
     * @param mDeviceID
     * @throws IOException
     */
    public void picUseTensorflow(String filePos,String time,int picIndex,String mDeviceID) throws IOException
    {
        //System.out.println(TensorFlow.version());
        try(SavedModelBundle bundle=SavedModelBundle.load("src\\main\\resources\\model","serve"))
        {
            Session session=bundle.session();
            Tensor xTensor = Tensor.create(getImagePixel(filePos));
            //float[] floatValues = new float[64 * 64];
            int width=0;
            int height=0;
            try
            {
                File file = new File(filePos);
                BufferedImage bi = ImageIO.read(file);
                width = bi.getWidth();
                height = bi.getHeight();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            //HR表示输入，copyTo表示将tensor数据结构转化为float数组
            float [][][][] y = session.runner().feed("input", xTensor).fetch("output").run().get(0).copyTo(new float [1][width*8][height*8][3]);
            //System.out.println(y[0][0][0][0]);
            setImage(mut255(y[0]),time,picIndex,mDeviceID);
            //System.out.println(mut255(y[0])[0][0][0]);
        }

        //setImage(getImagePixel("F:\\PyCharmProjects\\NTIRECompetition\\data\\DIV2K_valid_HR\\0802.png")[0]);
    }


    public static float[][][] mut255(float[][][] image){
        //final int pix = 255;
        //float max = (float)-1e-5;
        for(int i = 0; i < image.length; i++)
            for(int j = 0; j < image[0].length; j++)
                for(int k = 0; k < image[0][0].length; k++)
                {
                    image[i][j][k] = image[i][j][k] * pix;
//					if (image[i][j][k]>max)
//					{
//						max=image[i][j][k];
//					}
                }
        //System.out.println(max);
        return image;
    }

    /**
     * 方法说明
     *
     * @param image 参数
     * @return 参数
     */
    public static float[][][][] getImagePixel(String image)
    {
        //float[] floatValues = new float[64 * 64];
        File file = new File(image);
        BufferedImage bi = null;
        try
        {
            bi = ImageIO.read(file);
            int width = bi.getWidth();
            int height = bi.getHeight();
            float[][][][] imagePixel = new float[1][width][height][3];
            int minx = bi.getMinX();
            int miny = bi.getMinY();
            //System.out.println("width: " + width + " height: " + height + " minx: " + minx + " miny: " + miny);
            //System.out.println("is Alpha: " + bi.isAlphaPremultiplied());
            LOGGER.info("width: " + width + " height: " + height + " minx: " + minx + " miny: " + miny);
            LOGGER.info("is Alpha: " + bi.isAlphaPremultiplied());
            for (int x = minx; x < width; x++)
            {
                for (int y = miny; y < height; y++)
                {
                    int pixel = bi.getRGB(x, y);
                    int rgb0 = (pixel & 0xff0000) >> 16;
                    int rgb1 = (pixel & 0xff00) >> 8;
                    int rgb2 = (pixel & 0xff);
                    //System.out.println(rgb0 + "," + rgb1 + "," + rgb2);
                    imagePixel[0][x][y][0] = (float)rgb0;
                    imagePixel[0][x][y][1] = (float)rgb1;
                    imagePixel[0][x][y][2] = (float)rgb2;
                    // 数值归一化
                    //floatValues[(y * 64) + x] = rgb0 * (1f / 255f) - 0.5f;
                }
            }
            return imagePixel;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 方法说明
     *
     * @param
     */
    public static boolean setImage(float[][][] image,String time,int picIndex,String mDeviceID)
    {
        try
        {
            //创建一个不带透明色的BufferedImage对象(至于到底要创建带不带透明色的对象，我并不清楚，这是实验得出的结果)
            BufferedImage bufferedImage = new BufferedImage(image.length,image[0].length
                    , BufferedImage.TYPE_INT_RGB);
            //System.out.println(image.length + "   " + image[0].length);
            int rgb = 0;
            for(int i = 0; i < image.length; i++)
            {
                for(int j = 0; j < image[0].length; j++)
                {
                    //这里将r、g、b再转化为rgb值，因为bufferedImage没有提供设置单个颜色的方法，
                    //只能设置rgb。rgb最大为8388608，当大于这个值时，应减去255*255*255即16777216
                    //rgb = (image[i][j][0] * 256 + image[i][j][1]) * 256 + image[i][j][2];
                    rgb = (turnFloatToInt(image[i][j][0])<<16) | (turnFloatToInt(image[i][j][1])<<8) | turnFloatToInt(image[i][j][2]);
                    if(rgb > 8388608)
                    {
                        rgb = rgb - 16777216;
                    }
                    //将rgb值写回图片
                    bufferedImage.setRGB(i, j, rgb);
                }
            }
            //System.out.println(bufferedImage.getRGB(440, 500));
            //生成图片为jpg
            /*Date date = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
            String time = df.format(date);*/
            //ImageIO.write(bufferedImage, "jpg",  new File("C:\\Users\\Administrator\\Desktop\\outputImage\\"+"pic-"+time+".jpg")); C:/PicServer/inputImage/
            ImageIO.write(bufferedImage, "jpg",  new File("\\notebooks\\tensorflow\\jeremyhong\\PicServer\\outputImage\\"+ "out-"+mDeviceID+"-"+"pic-"+time+"-"+picIndex+".jpg"));

            //ImageIO.write(bufferedImage, "jpg",  new File("C:\\PicServer\\outputImage\\"+ "out-"+mDeviceID+"-"+"pic-"+time+"-"+picIndex+".jpg"));
        }
        catch (Exception e)
        {
            // TODO: handle exception
            e.printStackTrace();
        }
        return true;
    }

    private static int turnFloatToInt(float value)
    {
        return (int)(value+0.5);
    }
}


