package com.hnty.ticketsystem.common;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException{

    private final int code;

    public BizException(final int code, final String message) {
        super(message);
        this.code = code;
    }

    public BizException(String message){
        this(4001,message);
    }

}
