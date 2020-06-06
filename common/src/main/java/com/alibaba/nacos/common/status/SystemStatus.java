package com.alibaba.nacos.common.status;

/**
 * @author: jintonghuoya
 * @Date: 2020/05/29 21:52 下午
 * @Description: System Status
 * 这里放常用的通用系统级错误
 */
public enum SystemStatus implements IStatus {
    SUCCESS(200, "操作成功！"),
    FAILURE(400, "系统异常！"),
    ILLEGAL_ARGUMENT_EXCEPTION(600, "illegal argument"),
    RESOURCE_NOT_FOUND_EXCEPTION(601, "resource not found exception"),
    UNSUPPORTED_OPERATION_EXCEPTION(602, "unsupported operation exception"),
    UNSUPPORTED_ENCODING_EXCEPTION(603, "unsupported encoding exception"),
    INDEX_OUT_OF_BOUNDS_EXCEPTION(604, "index out of bounds exception"),
    IO_EXCEPTION(605, "io exception"),
    NULL_POINTER_EXCEPTION(606,"null pointer exception"),;

    SystemStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;

    @Override
    public NacosModule getNacosModule() {
        return NacosModule.SYSTEM;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
