package com.xuecheng.framework.exception;

import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 统一异常捕获类
 */
@ControllerAdvice //控制器增强,
public class ExceptionCatch {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionCatch.class);
    //使用EXCEPTIONS存放异常类型和错误代码的映射，ImmutableMap的特点的一旦创建不可改变，并且线程安全
    private static ImmutableMap<Class<? extends Throwable>,ResultCode> EXCEPTIONS;
    //使用builder来构建EXCEPTIONS
    protected static ImmutableMap.Builder<Class<? extends Throwable>,ResultCode> builder = ImmutableMap.builder();

    /**
     * 捕获到CustomException类型的异常就会执行该方法
     * @param customException
     * @return
     */
    @ExceptionHandler(CustomException.class)
    @ResponseBody  //将异常信息转为json响应给前台
    public ResponseResult customException(CustomException customException){
        //记录日志
        LOGGER.error("catch exception:{}",customException.getMessage());
        ResultCode resultCode = customException.getResultCode();
        return new ResponseResult(resultCode);
    }
    /**
     * 捕获到Exception类型的异常就会执行该方法
     * @param exception
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody  //将异常信息转为json响应给前台
    public ResponseResult exception(Exception exception){
        //记录日志
        LOGGER.error("catch exception:{}",exception.getMessage());
        if (EXCEPTIONS == null){
            //map为空,开始构建
            EXCEPTIONS = builder.build();
        }
        //从EXCEPTIONS找异常类型对应的错误代码,找到返回给用户;找不到返回给用户99999异常代码
        ResultCode resultCode = EXCEPTIONS.get(exception.getClass());
        if (resultCode != null){
            return new ResponseResult(resultCode);
        } else {
            //返回99999异常
            return new ResponseResult(CommonCode.SERVER_ERROR);
        }
    }

    static {
        //定义异常类型所对应的异常代码
        builder.put(HttpMessageNotReadableException.class, CommonCode.INVALID_PARAMETER);
    }
}
