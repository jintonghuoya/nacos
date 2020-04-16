package com.alibaba.nacos.config.server.jraft;

import com.alibaba.nacos.core.jraft.JRaftOperation;
import com.alibaba.nacos.config.server.jraft.rpc.ConfigJRaftRequest;

/**
 * @author jack_xjdai
 * @date 2020/4/16 11:10
 * @description: TODO
 */
public class ConfigJRaftOperation extends JRaftOperation {
    public ConfigJRaftOperation(int operation, ConfigJRaftRequest request) {
        super(operation, request);
    }

    public static ConfigJRaftOperation createPublishConfigOperation(final ConfigJRaftRequest request) {
        return new ConfigJRaftOperation(ConfigJRaftOperationFactory.PUBLISH.getCode(), request);
    }

    public static ConfigJRaftOperation createRemoveConfigOperation(final ConfigJRaftRequest request) {
        return new ConfigJRaftOperation(ConfigJRaftOperationFactory.REMOVE.getCode(), request);
    }
}
