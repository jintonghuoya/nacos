package com.alibaba.nacos.core.jraft;

import com.alibaba.nacos.core.jraft.rpc.JRaftGenericResponse;
import com.alipay.sofa.jraft.Closure;

public abstract class JRaftClosure implements Closure {

    private JRaftGenericResponse JRaftGenericResponse;
    private JRaftOperation JRaftOperation;

    public void setJRaftOperation(JRaftOperation JRaftOperation) {
        this.JRaftOperation = JRaftOperation;
    }

    public JRaftOperation getJRaftOperation() {
        return JRaftOperation;
    }

    public JRaftGenericResponse getJRaftGenericResponse() {
        return JRaftGenericResponse;
    }

    public void setJRaftGenericResponse(JRaftGenericResponse JRaftGenericResponse) {
        this.JRaftGenericResponse = JRaftGenericResponse;
    }

    protected void failure(final String errorMsg, final String redirect) {
        final JRaftGenericResponse response = new JRaftGenericResponse();
        response.setSuccess(false);
        response.setErrorMsg(errorMsg);
        response.setRedirect(redirect);
        setJRaftGenericResponse(response);
    }

    protected void success(final Object value) {
        final JRaftGenericResponse response = new JRaftGenericResponse();
        response.setValue(value);
        response.setSuccess(true);
        setJRaftGenericResponse(response);
    }
}
