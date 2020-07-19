package com.xuecheng.learning.mp;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.config.RabbitMQConfig;
import com.xuecheng.learning.service.LearningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Component
public class ChooseCourseTask {
    private static final Logger  LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);

    @Autowired
    LearningService learningService;
    @Autowired
    RabbitTemplate rabbitTemplate;

    /*
         接收选课任务
     */
    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_ADDCHOOSECOURSE)
    public void receiveChoosecourseTask(XcTask xcTask) throws ParseException {
        LOGGER.info("receive choose course task,taskId:{}",xcTask.getId());
        //接收到消息id
        String id = xcTask.getId();
        //添加选课
        //取出消息内容
        String requestBody = xcTask.getRequestBody();
        Map map = JSON.parseObject(requestBody, Map.class);
        String courseId = (String) map.get("courseId");
        String userId = (String) map.get("userId");
        String valid = (String) map.get("valid");
        Date startTime = null;
        Date endTime = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

        if (map.get("startTime") != null){
            startTime = dateFormat.parse((String) map.get("startTime"));
        }
        if (map.get("endTime") != null){
            endTime = dateFormat.parse((String) map.get("startTime"));
        }
        //添加选课
        ResponseResult addcourse = learningService.addcourse(userId, courseId, valid, startTime, endTime, xcTask);
        //选课成功发送响应消息
        if (addcourse.isSuccess()){
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_LEARNING_ADDCHOOSECOURSE,RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE_KEY,xcTask);
        }
    }

}
