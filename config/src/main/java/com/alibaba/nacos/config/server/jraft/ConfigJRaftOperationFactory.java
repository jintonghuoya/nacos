package com.alibaba.nacos.config.server.jraft;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.config.server.jraft.rpc.PublishJRaftRequest;
import com.alibaba.nacos.config.server.service.ConfigService;
import com.alibaba.nacos.core.boot.SpringContext;
import com.alibaba.nacos.core.jraft.JRaftOperation;
import com.alibaba.nacos.core.jraft.JRaftOperationFactory;

/**
 * @author jack_xjdai
 * @date 2020/4/16 11:11
 * @description: TODO
 */
public enum ConfigJRaftOperationFactory implements JRaftOperationFactory {
    PUBLISH(2000) {
        @Override
        void doProcess(ConfigJRaftOperation operation) throws NacosException {
            PublishJRaftRequest publishRequest = (PublishJRaftRequest) operation.getParameter();

            SpringContext.getAppContext()
                .getBean(ConfigService.class)
                .dump("", "", "", "", 0L, "");
        }
    },
    REMOVE(2001) {
        @Override
        void doProcess(ConfigJRaftOperation operation) throws NacosException {
            SpringContext.getAppContext()
                .getBean(ConfigService.class)
                .remove("", "", "");
        }
    },
    ;

    private int code;

    public int getCode() {
        return code;
    }

    ConfigJRaftOperationFactory(int code) {
        this.code = code;
    }

    abstract void doProcess(ConfigJRaftOperation operation) throws NacosException;

    /**
     * 处理入口
     *
     * @param operation
     * @throws NacosException
     */
    public void process(JRaftOperation operation) throws NacosException {
        doProcess((ConfigJRaftOperation) operation);
    }
}
