package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.PassThroughExceptionTranslationStrategy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedis(){
        //定义key
        String key = "user_token:9734b68f-cf5e-456f-9bd6-df578c711390";
        //定义Map
        Map<String,String> map = new HashMap<>();
        map.put("id","001");
        map.put("username","itcast");
        String value = JSON.toJSONString(map);
        //向redis中存储字符串
        stringRedisTemplate.boundValueOps(key).set(value,60, TimeUnit.SECONDS);
        //读取过期时间,过期返回-2
        Long expire = stringRedisTemplate.getExpire(key);
        //根据key获取value
        String s = stringRedisTemplate.opsForValue().get(key);
        System.out.println("值为:"+s+"过期时间为:"+expire);
    }

    @Test
    public void tsetPassWordEncoder(){
        String password = "123456";
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        for (int i = 0; i <10 ; i++) {
            //每个计算出的Hash值都不同
            String encode = passwordEncoder.encode(password);
            System.out.println("第"+(i+1)+"个hash值为:"+encode);
            //密码的Hash值不同但可以校验通过
            boolean matches = passwordEncoder.matches(password, encode);
            System.out.println("校验结果为:"+matches);
        }

    }
}
