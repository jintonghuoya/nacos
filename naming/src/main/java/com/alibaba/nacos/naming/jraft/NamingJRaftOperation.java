package com.alibaba.nacos.naming.jraft;

import com.alibaba.nacos.core.jraft.JRaftOperation;
import com.alibaba.nacos.naming.jraft.rpc.NamingJRaftRequest;

/**
 * @author jack_xjdai
 * @date 2020/4/16 11:10
 * @description: TODO
 */
public class NamingJRaftOperation extends JRaftOperation {
    public NamingJRaftOperation(int operation, NamingJRaftRequest request) {
        super(operation, request);
    }

    public static NamingJRaftOperation createRegisterInstancesOperation(final NamingJRaftRequest value) {
        return new NamingJRaftOperation(NamingJRaftOperationFactory.REGISTER_INSTANCES.getCode(), value);
    }
    public static NamingJRaftOperation createDeregisterInstancesOperation(final NamingJRaftRequest value) {
        return new NamingJRaftOperation(NamingJRaftOperationFactory.DEREGISTER_INSTANCES.getCode(), value);
    }
}
