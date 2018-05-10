package com.example.demo;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by jingcaohong on 2018/3/20.
 *
 * 消息生产者，发送消息到rabbitmq队列，告知处理
 */
@Component
public class Sender implements RabbitTemplate.ConfirmCallback, ReturnCallback {

    private static final Log LOGGER = LogFactory.getLog(Sender.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }

    /**
     * 消息确认
     *
     * @param correlationData
     * @param ack
     * @param cause
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            LOGGER.info("消息发送成功:" + correlationData);
        } else {
            LOGGER.info("消息发送失败:" + cause);
        }

    }

    /**
     * 返回消息
     *
     * @param message
     * @param replyCode
     * @param replyText
     * @param exchange
     * @param routingKey
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        LOGGER.info(message.getMessageProperties().getCorrelationId() + " 发送失败");

    }


    /**
     * 发送消息，不需要实现任何接口，供外部调用。
     * Web版本，发送消息到消息队列，告知调用tensorflow处理图片
     *
     * @param msg
     */
    public void sendWeb(String msg){

        CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());

        LOGGER.info("【sendWeb】开始发送消息 : " + msg.toLowerCase());
        rabbitTemplate.convertAndSend("topicExchange", "key.web", msg, correlationId);
        LOGGER.info("【sendWeb】结束发送消息 : " + msg.toLowerCase());
        LOGGER.info("【sendWeb】消费者响应 : " + " 消息处理完成");


    }

    /**
     * 【Web】告知网页端消息处理成功
     *
     * @param msg
     */
    public void sendSuccess(String msg){
        CorrelationData correlationDataId = new CorrelationData((UUID.randomUUID().toString()));
        LOGGER.info("【sendSuccess】开始发送消息 ：" + msg.toLowerCase());
        rabbitTemplate.convertAndSend("topicExchange","key.suc",msg,correlationDataId);
        LOGGER.info("【sendSuccess】结束发送消息 ："+msg.toLowerCase());
        LOGGER.info("【sendSuccess】消费者响应，消息处理完成");
    }


    /**
     * 【安卓端】告知处理图片，消息生产者，发送消息到topic，通过routingKey进行分发
     *
     * @param msg
     */
    public void sendAndroid(String msg){

        //生成correlationId
        CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());

        LOGGER.info("【Android_sendAndroid】开始发送消息 ：" + msg.toLowerCase());
        //给队列发送消息，topic根据routingkey进行分发消息给队列
        rabbitTemplate.convertAndSend("topicExchange", "key.android", msg, correlationId);
        LOGGER.info("【Android_sendAndroid】结束发送消息 ：" + msg.toLowerCase());
        LOGGER.info("【Android_sendAndroid】消费者响应 ：" + " 消息处理完成");
    }
}

