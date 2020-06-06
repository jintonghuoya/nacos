package com.alibaba.nacos.common.exception;

import com.alibaba.nacos.common.status.IStatus;
import com.alibaba.nacos.common.status.NacosModule;

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
        this(status.getNacosModule(), status.getCode(), status.getMessage());
    }

    /**
     * 统一异常处理
     *
     * @param status  状态码
     * @param message 自定义错误信息
     */
    public BusinessException(IStatus status, String message) {
        this(status.getNacosModule(), status.getCode(), message);
    }

    /**
     * 统一异常处理
     *
     * @param status  状态码
     * @param message 自定义错误信息
     * @param e
     */
    public BusinessException(IStatus status, String message, Throwable e) {
        this(status.getNacosModule(), status.getCode(), message, e);
    }

    /**
     * 统一异常处理
     *
     * @param status 状态码
     * @param e
     */
    public BusinessException(IStatus status, Throwable e) {
        this(status.getNacosModule(), status.getCode(), status.getMessage(), e);
    }

    /**
     * 统一异常处理
     *
     * @param nacosModule
     * @param code
     * @param message
     */
    public BusinessException(NacosModule nacosModule, int code, String message) {
        super(message);
        // 处理code
        if (nacosModule.getModuleCode() > 0) {
            this.code = Integer.parseInt(nacosModule.getModuleCode() + "" + code);
        } else {
            this.code = code;
        }
        this.message = message;
    }

    /**
     * 统一异常处理
     *
     * @param nacosModule
     * @param code
     * @param message
     * @param e
     */
    public BusinessException(NacosModule nacosModule, int code, String message, Throwable e) {
        super(message, e);
        // 处理code
        if (nacosModule.getModuleCode() > 0) {
            this.code = Integer.parseInt(nacosModule.getModuleCode() + "" + code);
        } else {
            this.code = code;
        }
        this.message = message;
    }

}
