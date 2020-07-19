package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//过滤器的抽象类
@Component
public class LoginFilter extends ZuulFilter {

    @Autowired
    AuthService authService;

    @Override
    public String filterType() {//返回字符串代表过滤器的类型
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;//int值来定义过滤器的执行顺序，数值越小优先级越高
    }

    @Override
    public boolean shouldFilter() {
        return true;//执行该过滤器
    }

    @Override
    public Object run() throws ZuulException {//过滤器的业务逻辑
        //上下文对象
        RequestContext requestContext = RequestContext.getCurrentContext();
        //请求对象
        HttpServletRequest request = requestContext.getRequest();
        //HttpServletResponse response = requestContext.getResponse();
        //查询身份令牌
        String access_token = authService.getTokenFromCookie(request);
        if (access_token == null){
            //拒绝访问
            access_denied();
        }
        //从redis中校验身份令牌是否过期
        long expire = authService.getExpire(access_token);
        if (expire<=0){
            //拒绝访问
            access_denied();
        }
        //查询jwt令牌
        String jwt = authService.getJwtFromHeader(request);
        if (jwt == null){
            access_denied();
        }
        return null;

//        //取出头部信息Authorization
//        String authorization = request.getHeader("Authorization");
//        if (StringUtils.isEmpty(authorization)){//没有Authorization
//            requestContext.setSendZuulResponse(false);//拒绝访问
//            requestContext.setResponseStatusCode(200);//设置响应状态码
//            ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
//            //转换成json
//            String toJSONString = JSON.toJSONString(responseResult);
//            requestContext.setResponseBody(toJSONString);
//
//            requestContext.getResponse().setContentType("application/json;charset=utf-8");
//            return null;
//        }
    }
    //拒绝访问方法
    private void access_denied(){
        //上下文对象
        RequestContext requestContext = RequestContext.getCurrentContext();
        requestContext.setSendZuulResponse(false);//拒绝访问
        //设置响应内容
        ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
        //转换成json
        String toJSONString = JSON.toJSONString(responseResult);
        requestContext.setResponseBody(toJSONString);
        //设置状态码
        requestContext.setResponseStatusCode(200);
        HttpServletResponse response = requestContext.getResponse();
        response.setContentType("application/json;charset=utf-8");
    }
}
