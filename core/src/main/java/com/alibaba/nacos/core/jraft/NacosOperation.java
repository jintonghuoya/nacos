package com.alibaba.nacos.core.jraft;

import java.io.Serializable;

/**
 * The counter operation
 *
 * @author likun (saimu.msm@antfin.com)
 */
public class NacosOperation implements Serializable {

    private static final long serialVersionUID = -6597003954824547294L;

    /** Get value */
    public static final byte  GET              = 0x01;
    /** Increment and get value */
    public static final byte  INCREMENT        = 0x02;

    private byte              op;
    private long              delta;

    public static NacosOperation createGet() {
        return new NacosOperation(GET);
    }

    public static NacosOperation createIncrement(final long delta) {
        return new NacosOperation(INCREMENT, delta);
    }

    public NacosOperation(byte op) {
        this(op, 0);
    }

    public NacosOperation(byte op, long delta) {
        this.op = op;
        this.delta = delta;
    }

    public byte getOp() {
        return op;
    }

    public long getDelta() {
        return delta;
    }
}
