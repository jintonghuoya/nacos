package com.alibaba.nacos.common.exception;

import com.alibaba.nacos.common.status.IStatus;

public class BusinessException extends RuntimeException {

    private Integer code;
    private String message;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 统一异常处理
     *
     * @param status 状态码
     */
    public BusinessException(IStatus status) {
        this(status.getCode(), status.getMessage());
    }

    /**
     * 统一异常处理
     *
     * @param code
     * @param message
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

}
