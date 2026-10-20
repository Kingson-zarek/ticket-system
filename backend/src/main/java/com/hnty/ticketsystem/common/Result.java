package com.hnty.ticketsystem.common;

import lombok.Data;

@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok(){
        return ok(null);
    }

    public static <T> Result<T> ok(T data){
        Result result = new Result();
        result.code = 0;
        result.message = "success";
        result.data = data;
        return result;
    }

    public static <T> Result<T> fail(int code, String message){
        Result result = new Result();
        result.code = code;
        result.message = message;
        return result;
    }
}
