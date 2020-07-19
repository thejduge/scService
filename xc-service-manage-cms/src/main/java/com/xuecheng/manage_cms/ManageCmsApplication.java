package com.xuecheng.manage_cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@EnableDiscoveryClient //表示是Eureka的一个客户端,从Eureka服务中心发现服务
@SpringBootApplication
@EntityScan("com.xuecheng.framework.domain.cms") //扫描实体类
@ComponentScan(basePackages = "com.xuecheng.api") //扫描接口
@ComponentScan(basePackages = "com.xuecheng.manage_cms") //扫描本项目下的所有类
@ComponentScan(basePackages = "com.xuecheng.framework") //扫描common下的所有类
public class ManageCmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManageCmsApplication.class,args);
    }

    //配置一个restTemplate用于远程调用  使用OkHttpClient完成http请求
    @Bean
    public RestTemplate restTemplate(){
        //使用okHttp
        RestTemplate restTemplate = new RestTemplate(new OkHttp3ClientHttpRequestFactory());
        return restTemplate;
    }

}
