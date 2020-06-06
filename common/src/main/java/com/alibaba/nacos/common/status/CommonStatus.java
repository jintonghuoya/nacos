package com.alibaba.nacos.common.status;

/**
 * @author: jintonghuoya
 * @Date: 2020/05/29 21:52 下午
 * @Description: Common Status
 */
public enum CommonStatus implements IStatus {
    GET_MD5_INSTANCE_ERROR(1000, "MessageDigest get MD5 instance error"),
    NACOS_SERIALIZATION_EXCEPTION(1001, "nacos serialization exception"),
    ;

    CommonStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;

    @Override
    public NacosModule getNacosModule() {
        return NacosModule.COMMON;
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
