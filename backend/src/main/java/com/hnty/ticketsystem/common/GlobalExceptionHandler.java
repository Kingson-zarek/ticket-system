package com.hnty.ticketsystem.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理，把异常统一转成 Result
 *
 * @author Crown
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    // 接口/资源不存在
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public Result<Void> handleNotFound(Exception e) {
        return Result.fail(404, "接口不存在");
    }

    // 业务异常，错误码和提示都由抛出方指定
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    // @Valid 校验失败
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError == null ? "参数校验失败" : fieldError.getDefaultMessage();
        return Result.fail(4000, msg);
    }

    // 请求方法不支持（比如 GET 接口用 POST 访问）
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return Result.fail(405, "请求方法不支持");
    }

    // 缺少请求参数
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return Result.fail(400, "缺少参数：" + e.getParameterName());
    }

    // 参数类型转换失败（比如路径变量 /api/tickets/abc 但 id 是 Long）
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Result.fail(400, "参数类型错误：" + e.getName());
    }

    // 请求体 JSON 格式错误 ← 你刚踩的那个
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleBadJson(HttpMessageNotReadableException e) {
        return Result.fail(400, "请求体格式错误，请检查 JSON");
    }

    // 兜底，不把内部异常信息返回给前端
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.fail(500, "系统繁忙，请稍后再试");
    }
}
