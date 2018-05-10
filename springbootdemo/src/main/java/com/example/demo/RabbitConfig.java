package com.example.demo;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by jingcaohong on 2018/4/20.
 *
 * RabbitMq 环境配置，声明队列，交互器，并且进行绑定
 *
 */
@Configuration
public class RabbitConfig {

    //声明队列
    //网页端消息
    @Bean
    public Queue webPicTensorQueue() {
        return new Queue("webPicTensorQueue", true); // true表示持久化该队列
    }

    @Bean
    public Queue webSuccessQueue() {
        return new Queue("webSuccessQueue", true);
    }

    //安卓端消息队列
    @Bean Queue androidPicTensorQueue(){
        return new Queue("androidPicTensorQueue",true);
    }

    //声明交互器
    @Bean
    TopicExchange topicExchange() {
        return new TopicExchange("topicExchange");
    }

    //绑定
    @Bean
    public Binding binding1() {
        return BindingBuilder.bind(webPicTensorQueue()).to(topicExchange()).with("key.web");
    }
    @Bean
    public Binding binding2() {
        return BindingBuilder.bind(webSuccessQueue()).to(topicExchange()).with("key.suc");
    }
    @Bean
    public Binding binding3(){
        return BindingBuilder.bind(androidPicTensorQueue()).to(topicExchange()).with("key.android");
    }

}
