package com.alibaba.nacos.core.jraft;

import java.io.Serializable;

public class JRaftOperation implements Serializable {

    private static final long serialVersionUID = -6597003954824547294L;

    private int operation;
    private Object parameter;

    public JRaftOperation(int operation, Object parameter) {
        this.operation = operation;
        this.parameter = parameter;
    }

    public int getOperation() {
        return operation;
    }

    public Object getParameter() {
        return parameter;
    }
}
