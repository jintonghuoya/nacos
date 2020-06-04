package com.alibaba.nacos.common.status;

import com.alibaba.nacos.common.exception.BusinessException;

/**
 * @author: daixinjie
 * @Date: 2020/05/29 21:52 下午
 * @Description: System Status
 */
public enum SystemStatus implements IStatus {
    SUCCESS(200, "操作成功！"),
    FAILURE(400, "系统异常！"),
    BASIC_PARAMETER_CHECK(444, "基础参数校验失败！"),
    ;

    SystemStatus(int code, String message) {
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

    public static SystemStatus findSystemStatus(int systemStatusCode) {
        for (SystemStatus systemStatus : SystemStatus.values()) {
            if (systemStatus.getCode() == systemStatusCode) {
                return systemStatus;
            }
        }
        throw new BusinessException(SystemStatus.BASIC_PARAMETER_CHECK);
    }

    public static boolean existCode(int systemStatusCode) {
        for (SystemStatus systemStatus : SystemStatus.values()) {
            if (systemStatus.getCode() == systemStatusCode) {
                return true;
            }
        }
        return false;
    }
}
