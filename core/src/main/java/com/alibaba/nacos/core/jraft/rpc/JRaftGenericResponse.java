package com.alibaba.nacos.core.jraft.rpc;

import java.io.Serializable;

/**
 * Value response.
 */
public class JRaftGenericResponse implements Serializable {

    private static final long serialVersionUID = -4220017686727146773L;

    private Object value;
    private boolean success;

    /**
     * redirect peer id
     */
    private String redirect;

    private String errorMsg;

    public String getErrorMsg() {
        return this.errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getRedirect() {
        return this.redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public JRaftGenericResponse(long value, boolean success, String redirect, String errorMsg) {
        super();
        this.value = value;
        this.success = success;
        this.redirect = redirect;
        this.errorMsg = errorMsg;
    }

    public JRaftGenericResponse() {
        super();
    }

    @Override
    public String toString() {
        return "ValueResponse [value=" + this.value + ", success=" + this.success + ", redirect=" + this.redirect
            + ", errorMsg=" + this.errorMsg + "]";
    }

}
