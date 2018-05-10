package com.example.demo;

import Util.DateUtil;
import Util.ImgUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.UUID;


/**
 * Created by jingcaohong on 2018/3/20.
 *
 * 网页端服务器，接收来自网页端的文件上传请求
 */
@Controller
@RequestMapping("/html")
public class HtmlController {

    private static final Log LOGGER = LogFactory.getLog(HtmlController.class);

    @Autowired
    private Sender sender;

    @Value("${hostname}")
    private String hostname;

    @Value("${web.input-img-path}")
    private String inputPath;

    @Value("${server.port}")
    private String serverPort;

    @Value("${web.output-img-path}")
    private String outputPath;

    /**
     * 跳转到上传文件的页面
     *
     * @return
     */
    @RequestMapping(value = "/gouploadimg",method = RequestMethod.GET)
    public String goUploadImg(){

        return "template/uploadimg";
    }


    /**
     * 处理文件上传,POST请求
     *
     * @param files
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/handleuploadimg",method = RequestMethod.POST)
    public @ResponseBody ModelAndView uploadImg(@RequestParam(value = "files",required = false)MultipartFile[] files,
                                          HttpServletRequest request) throws IOException {

        //生成开始时间，计算程序运行时间
        long beginTime = System.currentTimeMillis();
        LOGGER.info("=================================BEGIN=================================");
        //生成UUID   唯一编码号，作为文件名组成标识之一
        String uuid = UUID.randomUUID().toString().replaceAll("\\-","");
        LOGGER.info("【Web】UUID NUMBER："+uuid);
        //记录图片数量
        int picNum=0;
        //设置request的编码为UTF-8
        request.setCharacterEncoding("utf-8");
        //设置文件存储地址
        //String filePath = "src/main/resources/inputImage/";
        String filePath = inputPath;
        //生成时间戳，保证文件唯一性
        String time = DateUtil.getTimeStamp();
        try{
            //保存图片
            int i=0;
            //判断文件是否为空
            if (files == null || files.length == 0){
                System.out.println("【Web】files does'nt exist！");
                LOGGER.info("【Web】files does'nt exist！");
            }
            if (files !=null && files.length > 0){
                for (;i < files.length;i++){
                    MultipartFile file = files[i];
                    String contentType = file.getContentType();
                    String fileName = uuid + "-" + "pic-" + time + "-" + i + ".jpg";
                    LOGGER.info("【Web】fileName:"+fileName);
                    //保存图片
                    Util.FileUtil.uploadFile(file.getBytes(),filePath,fileName);
                }
            }
            LOGGER.info("【Web】i的值："+i);
            //发送消息给消息队列，调用Tensorflow对图片进行处理
            String msg = i + "." + time + "." + uuid;
            picNum = i;
            sender.sendWeb(msg);

        }catch (Exception e){
            e.printStackTrace();
        }
        //计算程序运行时间
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        long endTime = System.currentTimeMillis();
        LOGGER.info("【Web_接收、处理、保存图片用时】："+formatter.format(endTime-beginTime)+"   ==="+(endTime-beginTime));
        //重定向到等待网页
        return new ModelAndView("redirect:/html/waiting?time="+time+"&picNum="+picNum+"&uuid="+uuid);
    }


    /**
     * 作为中介响应界面，点击按钮后显示处理结果
     *
     * @param time
     * @param picNum
     * @param uuid
     * @param model
     * @return
     */
    @RequestMapping(value = "/waiting",method = RequestMethod.GET)
    public String waiting(@RequestParam(value = "time")String time,
                          @RequestParam(value = "picNum")Integer picNum,
                          @RequestParam(value = "uuid")String uuid,
                          Model model){
        //http://139.199.153.122:666/out-68df4f593825445bbbab280d8f801444-pic-20180423032351-0.jpg
        int i = picNum-1;
        String url ="http://"+hostname+":"+serverPort+"/out-" + uuid + "-pic-" + time + "-" + i + ".jpg";
        LOGGER.info(url);
        model.addAttribute("time",time);
        model.addAttribute("picNum",picNum);
        model.addAttribute("uuid",uuid);
        model.addAttribute("url",url);
        return "template/oneButton";
    }


    /**
     * 展示图片
     *
     * @param time
     * @param picNum
     * @param uuid
     * @param model
     * @return
     */
    @RequestMapping(value = "/showpic",method = RequestMethod.GET)
    public String testpic(@RequestParam(value = "time")String time,
                          @RequestParam(value = "picNum")Integer picNum,
                          @RequestParam(value = "uuid")String uuid,
                          Model model){

        //记录时间，计算程序运行时间
        long beginTime = System.currentTimeMillis();
        //生成ArrayList存储图片
        ArrayList<String> picList = new ArrayList<>();
        for(int i=0;i<picNum;i++){
            String outputPic = outputPath + "out-" + uuid + "-" + "pic-" + time + "-" + i + ".jpg";
            LOGGER.info("【Web】outputPicPos：" + outputPic);
            //对图片进行base64编码
            String outputPicBase64 = ImgUtil.encodeImageToBase64(outputPic);
            //将base64编码存进Arraylist
            picList.add(outputPicBase64);
        }
        model.addAttribute("picList",picList);
        LOGGER.info("【Web】time:"+time);
        LOGGER.info("【Web】picNum:"+picNum);
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        long endTime = System.currentTimeMillis();
        LOGGER.info("【Web_回显网页用时】：" + formatter.format(endTime-beginTime)+"   ==="+(endTime-beginTime));
        LOGGER.info("==================================END==================================");
        return "template/showPicture";
    }

    /**
     * Hello World
     *
     * @param model
     * @return
     */
    @RequestMapping("/hello")
    public String hello(Model model){
        model.addAttribute("name","world!!");
        return "template/hello";
    }

}
