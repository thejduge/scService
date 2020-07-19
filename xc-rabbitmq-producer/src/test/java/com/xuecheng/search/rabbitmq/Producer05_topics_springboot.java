package com.xuecheng.search.rabbitmq;

import com.xuecheng.search.rabbitmq.config.RabbitmqConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 测试使用rabbitmq发送消息到队列
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class Producer05_topics_springboot {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSendEmail(){
        String message = "send a eamil";
        /**
         * 参数 交换机名称  routingKey  message
         */
        rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_TOPICS_INFORM,"inform.email",message);
    }
}
