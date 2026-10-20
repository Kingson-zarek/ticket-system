package com.hnty.ticketsystem.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //业务异常
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e){
        log.warn("业务异常！："+e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    //参数异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e){
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError == null ? "参数校验失败！" : fieldError.getDefaultMessage();
        return Result.fail(4000,msg);
    }

    //其他类型异常
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e){
        log.error("系统异常：",e);
        return Result.fail(500, e.getMessage());
    }
}
