package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    XcTaskRepository xcTaskRepository;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    XcTaskHisRepository xcTaskHisRepository;

    public List<XcTask> findTaskList(Date updateTime,int n){
        //设置分页参数,取出前n条记录
        Pageable pageable = new PageRequest(0,n);
        Page<XcTask> xcTasks = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        return xcTasks.getContent();
    }

    //发送消息
    @Transactional
    public void pulish(XcTask xcTask,String ex,String routingkey){
        //查询任务
        Optional<XcTask> optional = xcTaskRepository.findById(xcTask.getId());
        if (optional.isPresent()){
            XcTask xcTask1 = optional.get();
            rabbitTemplate.convertAndSend(ex,routingkey,xcTask1);
        }
        //更新任务时间为当前时间
        xcTask.setUpdateTime(new Date());
        xcTaskRepository.save(xcTask);
    }
    //使用乐观锁方法校验任务
    @Transactional
    public int getTask(String taskId ,int version){
        int i = xcTaskRepository.updateTaskVersion(taskId, version);
        return i;
    }

    //删除任务
    @Transactional
    public void finishTask(String taskId){
        //查询任务
        Optional<XcTask> taskOptional = xcTaskRepository.findById(taskId);
        if (taskOptional.isPresent()){
            XcTask xcTask = taskOptional.get();
            xcTask.setDeleteTime(new Date());
            XcTaskHis xcTaskHis = new XcTaskHis();
            BeanUtils.copyProperties(xcTask,xcTaskHis);
            //添加历史任务
            xcTaskHisRepository.save(xcTaskHis);
            //删除历史任务
            xcTaskRepository.delete(xcTask);
        }
    }
}
