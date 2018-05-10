package com.example.demo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Component;

/**
 * Created by jingcaohong on 2018/4/20.
 *
 * MQTT消息生产者
 */
@Component
public class MqttSender {

    private static final Log LOGGER = LogFactory.getLog(MqttSender.class);


    /**
     * 发送消息至Topic
     *
     * @param content
     */
    public void sendMQTTMessage(String content){
        //消息的类型，主题名
        String topic = "Topic_MQTT_Clearer";
        //消息发送模式，选择消息发送的次数
        int qos = 2;
        //服务器地址
        String broker = "tcp://139.199.153.122:61613";
        //客户端的唯一标识
        String clientId = "jeremyhong";
        //消息缓存的方式，内存缓存
        MemoryPersistence persistence = new MemoryPersistence();

        try{
            //创建一个MQTT客户端
            MqttClient sampleClient = new MqttClient(broker,clientId,persistence);
            //消息的配置参数
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName("admin");
            connOpts.setPassword("password".toCharArray());
            //不记忆上一次会话
            connOpts.setCleanSession(true);
            LOGGER.info("Connecting to broker: "+broker);
            //链接服务器
            sampleClient.connect(connOpts);
            LOGGER.info("Connected");
            LOGGER.info("Publishing message: "+content);
            //创建消息
            MqttMessage message = new MqttMessage(content.getBytes());
            //给消息设置发送的模式
            message.setQos(qos);
            //发布消息到服务器
            sampleClient.publish(topic,message);
            LOGGER.info("Message published!");
            //断开链接
            sampleClient.disconnect();
            LOGGER.info("Disconnected!");
            //System.out.println("Disconnected!");
            //System.exit(0);
        }catch (MqttException me){
            LOGGER.warn("reason "+me.getReasonCode());
            LOGGER.warn("msg "+me.getMessage());
            LOGGER.warn("loc "+me.getLocalizedMessage());
            LOGGER.warn("cause "+me.getCause());
            LOGGER.warn("excep "+me);
            me.printStackTrace();
        }
    }
}
