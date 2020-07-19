package com.xuecheng.order.mq;

import com.alibaba.druid.util.StringUtils;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


//任务类
@Component
public class ChooseCourseTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);
    @Autowired
    private TaskService taskService;

   /* @Scheduled(cron = "0/3 * * * * *")//每3秒执行一次
    public void task(){
        LOGGER.info("===================定时任务1开始=====================");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("===================定时任务1结束=====================");
    }

    @Scheduled(cron = "0/3 * * * * *")//每3秒执行一次
    public void task2(){
        LOGGER.info("===================定时任务2开始=====================");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("===================定时任务2结束=====================");
    }*/
   //定时发送添加选课任务
   @Scheduled(fixedDelay = 60)//每60秒执行一次
   //@Scheduled(cron = "0/3 * * * * *")//每60秒执行一次
   public void sendChoosecourseTask(){
       //取出当前时间1分钟之前的时间
       Calendar calendar = new GregorianCalendar();
       calendar.setTime(new Date());
       calendar.add(GregorianCalendar.MINUTE,-1);
       Date time = calendar.getTime();
       List<XcTask> taskList = taskService.findTaskList(time, 100);
       //遍历任务列表
       for (XcTask xcTask : taskList) {
           //取出任务
           //任务id
           String taskId = xcTask.getId();
           //版本号
           Integer version = xcTask.getVersion();
           //调用乐观锁方法校验任务是否可以执行
           if (taskService.getTask(taskId,version)>0){
               //发送选课消息
               taskService.pulish(xcTask,xcTask.getMqExchange(),xcTask.getMqRoutingkey());
               LOGGER.info("send choose course task id:{}",taskId);
           }
       }
   }

   //接收选课响应结果
    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE)
    public void receiveFinishChoosecourseTask(XcTask xcTask){
       if (xcTask != null && !StringUtils.isEmpty(xcTask.getId())){
           //删除任务
           taskService.finishTask(xcTask.getId());
       }
    }
}
