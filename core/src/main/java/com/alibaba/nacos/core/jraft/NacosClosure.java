package com.alibaba.nacos.core.jraft;

import com.alibaba.nacos.core.jraft.rpc.ValueResponse;
import com.alipay.sofa.jraft.Closure;

/**
 * @author likun (saimu.msm@antfin.com)
 */
public abstract class NacosClosure implements Closure {

    private ValueResponse valueResponse;
    private NacosOperation nacosOperation;

    public void setNacosOperation(NacosOperation nacosOperation) {
        this.nacosOperation = nacosOperation;
    }

    public NacosOperation getNacosOperation() {
        return nacosOperation;
    }

    public ValueResponse getValueResponse() {
        return valueResponse;
    }

    public void setValueResponse(ValueResponse valueResponse) {
        this.valueResponse = valueResponse;
    }

    protected void failure(final String errorMsg, final String redirect) {
        final ValueResponse response = new ValueResponse();
        response.setSuccess(false);
        response.setErrorMsg(errorMsg);
        response.setRedirect(redirect);
        setValueResponse(response);
    }

    protected void success(final long value) {
        final ValueResponse response = new ValueResponse();
        response.setValue(value);
        response.setSuccess(true);
        setValueResponse(response);
    }
}
