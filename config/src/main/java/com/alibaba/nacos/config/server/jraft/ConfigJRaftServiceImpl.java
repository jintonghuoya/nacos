package com.alibaba.nacos.config.server.jraft;

import com.alibaba.nacos.core.jraft.AbstractJRaftService;
import com.alibaba.nacos.core.jraft.JRaftClosure;
import com.alibaba.nacos.core.jraft.JRaftServer;
import com.alibaba.nacos.config.server.jraft.rpc.ConfigJRaftRequest;

/**
 * @author jack_xjdai
 * @date 2020/4/15 19:17
 * @description: TODO
 */
public class ConfigJRaftServiceImpl extends AbstractJRaftService implements ConfigJRaftService {

    public ConfigJRaftServiceImpl(JRaftServer JRaftServer) {
        super(JRaftServer);
    }

    @Override
    public void publish(ConfigJRaftRequest request, JRaftClosure closure) {
        applyOperation(ConfigJRaftOperation.createPublishConfigOperation(request), closure);
    }

    @Override
    public void remove(ConfigJRaftRequest request, JRaftClosure closure) {
        applyOperation(ConfigJRaftOperation.createRemoveConfigOperation(request), closure);
    }
}
