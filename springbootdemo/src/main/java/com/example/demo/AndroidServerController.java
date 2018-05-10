package com.example.demo;

import Util.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by jingcaohong on 2018/4/6.
 *
 * 安卓服务器 接收安卓请求
 */
@Controller
@RequestMapping("/androidServer")
public class AndroidServerController {

    private static final Log LOGGER = LogFactory.getLog(AndroidServerController.class);

    @Autowired
    private Sender sender;

    @Value("${web.input-img-path}")
    private String inputPath;

    /**
     * 处理安卓文件请求
     * @param send_userId
     * @param mDeviceID
     * @param request
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping(value = "/androidUploadImg")
    public @ResponseBody String androidUploadImg(@RequestParam(value = "send_userId",required = false) String send_userId,
                                                 @RequestParam(value = "mDeviceID",required = false) String mDeviceID,
                                                 HttpServletRequest request)throws ServletException,IOException{
        //记录开始时间
        long beginTime = System.currentTimeMillis();
        LOGGER.info("=================================BEGIN=================================");

        StandardMultipartHttpServletRequest req = (StandardMultipartHttpServletRequest) request;
        //设置request UTF-8编码格式
        request.setCharacterEncoding("utf-8");
        LOGGER.info("【Android】send_userId："+send_userId);
        //指定图片存储路径，图片上传到服务器
        //String filePath = "src/main/resources/inputImage/";
        String filePath = inputPath;
        //文件命名唯一性，加入时间戳
        String time = DateUtil.getTimeStamp();
        try {
            //迭代器获取文件列表
            Iterator<String> iterator = req.getFileNames();
            int i = 0;
            //获取文件
            while(iterator.hasNext()){
                //通过文件名获取文件
                MultipartFile file = req.getFile(iterator.next());
                if(file == null){
                    System.out.println("【Android】File is null!");
                    LOGGER.info("【Android】File is null!");
                }
                //组装文件名，保证唯一性
                String fileName = mDeviceID+"-"+"pic-"+time+"-"+i+".jpg";
                //保存文件
                Util.FileUtil.uploadFile(file.getBytes(),filePath,fileName);
                i++;
            }
            LOGGER.info("【Android】i的值："+i);
            String msg = i + "." + time + "." + send_userId + "." + mDeviceID;
            //发送消息到rabbitmq 消息队列，告知调用tensorflow对文件进行处理
            sender.sendAndroid(msg);

        }catch (Exception e){
            e.printStackTrace();
        }
        //计算程序运行结束时间
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        long endTime = System.currentTimeMillis();
        LOGGER.info("【Android_接收、处理、保存图片用时】："+formatter.format(endTime-beginTime)+"   ==="+(endTime-beginTime));
        LOGGER.info("==================================END==================================");
        return "Picture Received!";
    }
}
