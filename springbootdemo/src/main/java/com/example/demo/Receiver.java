package com.example.demo;

import com.example.demo.JavaForTensorflow;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * Created by jingcaohong on 2018/3/25.
 *
 * 消息消费者，监听到消息后对图片进行调用tensorflow处理
 */
@Component
public class Receiver {

    private static final Log LOGGER = LogFactory.getLog(Receiver.class);

    @Autowired
    private JavaForTensorflow javaForTensorflow;

    @Autowired
    private Sender sender;

    @Autowired
    private MqttSender mqttSender;


    /**
     * 监听webPicTensorQueue队列消息
     *
     * Web版本，对图片调用tensorflow进行处理，处理完毕后返回图片相关消息给网页端，网页端对图片进行展示
     *
     * @param msg
     * @throws IOException
     */
    @RabbitListener(queues = "webPicTensorQueue")
    public void processMessage1(String msg) throws IOException {
        LOGGER.info(Thread.currentThread().getName() + " 接收到来自webPicTensorQueue队列的消息：" + msg);
        String[] msgArr = msg.split("\\.");
        int i = Integer.parseInt(msgArr[0]);
        String time = msgArr[1];
        String uuid = msgArr[2];
        LOGGER.info("new one javafortensorflow");
        for ( i=i-1;i >=0;i--){
            String filePos = Util.FileUtil.provideFilePath(time,i,uuid);
            LOGGER.info("filePos:"+filePos);
            javaForTensorflow.picUseTensorflow(filePos,time,i,uuid);
            LOGGER.info("javaForTensorflow change success!");
            LOGGER.info("Picture name:"+filePos);
        }
        sender.sendSuccess(msg);
    }

    /**
     * 监听webSuccessQueue队列消息
     * 告知web端图片处理成功
     *
     * @param msg
     */
    @RabbitListener(queues = "webSuccessQueue")
    public void processMessage2(String msg) {
        LOGGER.info(Thread.currentThread().getName() + " 接收到来自webSuccessQueue队列的消息：" + msg);
        LOGGER.info("--------------图片处理完成----------------");
    }

    /**
     * 监听androidPicTensorQueue队列消息
     * 对图片调用tensorflow进行处理，处理完毕后返回图片相关消息给安卓端，安卓端对图片进行展示
     *
     * @param msg
     * @throws IOException
     */
    @RabbitListener(queues = "androidPicTensorQueue")
    public void processMessage3(String msg) throws IOException{
        LOGGER.info("【Android】：" + Thread.currentThread().getName() + " 接收到来自androidPicTensorQueue队列的消息："+msg);
        //解析msg，取出时间，图片数量，设备号
        String[] msgArr = msg.split("\\.");
        int i = Integer.parseInt(msgArr[0]);
        //存储图片张数
        int picNum = i;
        String time = msgArr[1];
        String send_userId = msgArr[2];
        String DeviceID = msgArr[3];
        LOGGER.info("【Android】new one javafortensorflow");
        //循环进行图片处理
        for ( i = i-1;i>=0;i--){
            //获取文件地址
            String filePos = Util.FileUtil.provideFilePath(time,i,DeviceID);
            LOGGER.info("【Android】filePos：" + filePos);
            //调用model对图片进行修改
            javaForTensorflow.picUseTensorflow(filePos,time,i,DeviceID);
            LOGGER.info("【Android】javaForTensorflow change success!");
            LOGGER.info("【Android】Picture name："+filePos);
        }
        String mqttMsg = "图片处理完毕_" + DeviceID + "_" + picNum + "_" + DeviceID + "-pic-" +time;
        //发送MQTT推送，告知安卓端图片处理完毕
        mqttSender.sendMQTTMessage(mqttMsg);
    }

}
