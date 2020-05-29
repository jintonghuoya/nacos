package com.alibaba.nacos.common.status;

/**
 * @author: daixinjie
 * @Date: 2020/05/29 21:52 下午
 * @Description: Common Status
 */
public enum CommonStatus implements IStatus {
    XSS_CHECK_ERROR(1391, "XSS校验失败"),
    USER_SING_OUT_ERROR(1392, "用户已注销登录"),
    CHECK_FUNC_AUTHORIZATION_ERROR(1393, "功能权限校验不通过"),
    ;

    CommonStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;

    @Override
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
