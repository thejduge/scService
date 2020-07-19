package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;

/**
 * 自定义异常类
 * @author 惘兩
 * RuntimeException 运行时异常,在代码中抛出的话对代码没有可侵入性;
 * Exception 在代码中抛出的话需要进行处理(声明或者捕获),对代码有侵入性;
 */
public class CustomException extends RuntimeException {
    //错误代码
    ResultCode resultCode;

    public CustomException(ResultCode resultCode){
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }
}
