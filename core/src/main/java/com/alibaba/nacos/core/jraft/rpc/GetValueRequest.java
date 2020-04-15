package com.alibaba.nacos.core.jraft.rpc;

import java.io.Serializable;

/**
 * Get the latest value request.
 *
 * @author boyan (boyan@alibaba-inc.com)
 *
 * 2018-Apr-09 4:54:17 PM
 */
public class GetValueRequest implements Serializable {

    private static final long serialVersionUID = 9218253805003988802L;

    private boolean           readOnlySafe     = true;

    public boolean isReadOnlySafe() {
        return readOnlySafe;
    }

    public void setReadOnlySafe(boolean readOnlySafe) {
        this.readOnlySafe = readOnlySafe;
    }
}
