package com.alibaba.nacos.naming.jraft;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.core.boot.SpringContext;
import com.alibaba.nacos.core.jraft.JRaftOperation;
import com.alibaba.nacos.core.jraft.JRaftOperationFactory;
import com.alibaba.nacos.naming.consistency.KeyBuilder;
import com.alibaba.nacos.naming.consistency.persistent.raft.RaftConsistencyServiceImpl;
import com.alibaba.nacos.naming.jraft.rpc.RegisterInstancesJRaftRequest;

/**
 * @author jack_xjdai
 * @date 2020/4/16 11:11
 * @description: TODO
 */
public enum NamingJRaftOperationFactory implements JRaftOperationFactory {
    REGISTER_INSTANCES(1000) {
        @Override
        void doProcess(NamingJRaftOperation operation) throws NacosException {
            RegisterInstancesJRaftRequest registerInstancesRequest = (RegisterInstancesJRaftRequest) operation.getParameter();
            SpringContext.getAppContext()
                .getBean(RaftConsistencyServiceImpl.class)
                .put(KeyBuilder.buildInstanceListKey("", "", false),
                    registerInstancesRequest.getInstances());
        }
    },
    DEREGISTER_INSTANCES(1001) {
        @Override
        void doProcess(NamingJRaftOperation operation) throws NacosException {
            RegisterInstancesJRaftRequest registerInstancesRequest = (RegisterInstancesJRaftRequest) operation.getParameter();
            SpringContext.getAppContext()
                .getBean(RaftConsistencyServiceImpl.class)
                .remove(KeyBuilder.buildInstanceListKey("", "", false));

        }
    },
    ;

    private int code;

    public int getCode() {
        return code;
    }

    NamingJRaftOperationFactory(int code) {
        this.code = code;
    }

    abstract void doProcess(NamingJRaftOperation operation) throws NacosException;

    /**
     * 处理入口
     *
     * @param operation
     * @throws NacosException
     */
    public void process(JRaftOperation operation) throws NacosException {
        doProcess((NamingJRaftOperation) operation);
    }
}
