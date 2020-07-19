package com.xuecheng.order.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import java.util.concurrent.Executor;

//线程池配置类 实现并行执行任务
@Configuration
@EnableScheduling
public class AsyncTaskConfig  implements AsyncConfigurer,SchedulingConfigurer {

    //线程池数量
    private int corePoolSize = 5;

    @Bean
    public ThreadPoolTaskScheduler taskScheduler(){
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        //初始化线程池
        scheduler.initialize();
        //线程池数量
        scheduler.setPoolSize(corePoolSize);
        return scheduler;
    }

    @Override
    public Executor getAsyncExecutor() {
        Executor executor = taskScheduler();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }


    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.setTaskScheduler(taskScheduler());
    }
}
