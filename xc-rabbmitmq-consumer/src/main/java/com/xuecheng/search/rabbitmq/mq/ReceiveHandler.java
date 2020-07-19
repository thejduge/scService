package com.xuecheng.search.rabbitmq.mq;

import com.rabbitmq.client.Channel;
import com.xuecheng.search.rabbitmq.config.RabbitmqConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReceiveHandler {
    //配置要监听的队列
    @RabbitListener(queues = {RabbitmqConfig.QUEUE_INFORM_EMAIL})
    public void send_email(String message, Message msg, Channel channel){
        System.out.println("接收到的消息是:" + message);
    }
}
