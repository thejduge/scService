package com.xuecheng.auth;

import com.xuecheng.framework.client.XcServiceList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestClient {

    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    RestTemplate restTemplate;

    //申请令牌测试
    @Test
    public void testClient(){
        //采用客户端负载均衡,从eureka获取认证服务的ip和端口
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        URI uri = serviceInstance.getUri(); //申请令牌的url
        //令牌申请的地址 http://localhost:40400/auth/oauth/token
        String authUrl = uri+"/auth/oauth/token";

        //请求的内容分两部分
        //1.header信息,包括http basic认证信息
        LinkedMultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        //BasicWGNXZWJBcHA6WGNXZWJBcHA=
        String httpbasic =  httpbasic("XcWebApp","XcWebApp");
        headers.add("Authorization",httpbasic);// Authorization

        //2.包括grant_type,username,password
        LinkedMultiValueMap<String,String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username","itcast");
        body.add("password","123");
        //定义请求
        HttpEntity<MultiValueMap<String,String>> multiValueMapHttpEntity = new HttpEntity<>(body,headers);
        //指定restTemplate当遇到400或401响应时候也不要抛出异常，也要正常返回值
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response)throws IOException{
                //当响应的值为400或401时候也要正常响应，不要抛出异常
                if (response.getRawStatusCode() !=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });
        //远程调用申请令牌
        ResponseEntity<Map> exchange = restTemplate.exchange(authUrl, HttpMethod.POST, multiValueMapHttpEntity, Map.class);
        Map body1 = exchange.getBody();
        System.out.println("申请的令牌结果为:"+body1);

    }
    //获取httpbasic的串
    private String httpbasic(String clientId, String clientSecret) {
        //将客户端id和客户端密码拼接，按“客户端id:客户端密码”拼接
        String str = clientId+":"+clientSecret;
        //将字符串进行base64编码
        byte[] encode = Base64Utils.encode(str.getBytes());
        return "Basic "+new String(encode);
    }
}
