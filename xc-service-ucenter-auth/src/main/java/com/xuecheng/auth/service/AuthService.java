package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;//过期时间

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //登录 获取jwt令牌
    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        //申请令牌
        AuthToken authToken = applyToken(username,password,clientId,clientSecret);
        if (authToken == null){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLTOKEN_FAIL);
        }
        //将token存储到redis
        String access_token = authToken.getAccess_token();
        String content = JSON.toJSONString(authToken);
        boolean saveTokenResult = saveToken(access_token,content,tokenValiditySeconds);
        if (!saveTokenResult){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_SAVEFAIL);
        }
        return authToken;
    }
    //将token存储到redis
    private boolean saveToken(String access_token, String content, int tokenValiditySeconds) {
        //令牌名称
        String name = "user_token:"+access_token;
        //保存令牌到redis
        stringRedisTemplate.boundValueOps(name).set(content,tokenValiditySeconds, TimeUnit.SECONDS);
        //获取过期时间
        Long expire = stringRedisTemplate.getExpire(name);
        return expire > 0;
    }
    //申请令牌
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret) {
        //采用客户端负载均衡,从eureka获取认证服务的ip和端口
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        if (serviceInstance == null){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_AUTHSERVER_NOTFOUND);
        }
        URI uri = serviceInstance.getUri(); //申请令牌的url
        //令牌申请的地址 http://localhost:40400/auth/oauth/token
        String authUrl = uri+"/auth/oauth/token";

        //请求的内容分两部分
        //1.header信息,包括http basic认证信息
        LinkedMultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        //BasicWGNXZWJBcHA6WGNXZWJBcHA=
        String httpbasic =  httpbasic(clientId,clientSecret);
        headers.add("Authorization",httpbasic);// Authorization

        //2.包括grant_type,username,password
        LinkedMultiValueMap<String,String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);
        //定义请求
        HttpEntity<MultiValueMap<String,String>> multiValueMapHttpEntity = new HttpEntity<>(body,headers);
        //指定restTemplate当遇到400或401响应时候也不要抛出异常，也要正常返回值
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response)throws IOException {
                //当响应的值为400或401时候也要正常响应，不要抛出异常
                if (response.getRawStatusCode() !=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });
        Map map = null;

        try{
            //远程调用申请令牌
            ResponseEntity<Map> exchange = restTemplate.exchange(authUrl, HttpMethod.POST, multiValueMapHttpEntity, Map.class);
            map = exchange.getBody();
        }catch (RestClientException e){
            e.printStackTrace();
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLTOKEN_FAIL);
        }
        if (map == null || map.get("access_token") == null || map.get("refresh_token") == null|| map.get("jti") == null){
            //jti是jwt令牌的唯一标识作为用户身份令牌
            String error_description = (String) map.get("error_description");
            if (!StringUtils.isEmpty(error_description)){
                if (error_description.equals("坏的凭证")){
                    ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
                }else if (error_description.indexOf("UserDetailsService returned null")>=0){
                    ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
                }
            }
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLTOKEN_FAIL);
        }

        AuthToken authToken = new AuthToken();
        //访问令牌(jwt)
        String jwt_token = (String) map.get("access_token");
        //刷新令牌
        String refresh_token = (String) map.get("refresh_token");
        //jti
        String jti = (String) map.get("jti");
        authToken.setAccess_token(jti);
        authToken.setJwt_token(jwt_token);
        authToken.setRefresh_token(refresh_token);
        return authToken;
    }
    //获取httpbasic的串
    private String httpbasic(String clientId, String clientSecret) {
        //将客户端id和客户端密码拼接，按“客户端id:客户端密码”拼接
        String str = clientId+":"+clientSecret;
        //将字符串进行base64编码
        byte[] encode = Base64Utils.encode(str.getBytes());
        return "Basic "+new String(encode);
    }
    //根据令牌从redis中查询jwt
    public AuthToken getUserToken(String access_token) {
        String userToken = "user_token:"+access_token;
        String userTokenStr = stringRedisTemplate.opsForValue().get(userToken);
        if (userTokenStr != null){
            AuthToken authToken = null;
            try {
                authToken = JSON.parseObject(userTokenStr, AuthToken.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return authToken;
        }
        return null;
    }
    //删除redis中的token
    public boolean deleteToken(String uid) {
        String name = "user_token:"+uid;
        stringRedisTemplate.delete(name);
        return true;
    }
}
