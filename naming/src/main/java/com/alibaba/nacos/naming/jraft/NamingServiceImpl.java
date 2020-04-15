package com.alibaba.nacos.naming.jraft;

import com.alibaba.nacos.core.jraft.AbstractJRaftService;
import com.alibaba.nacos.core.jraft.NacosClosure;
import com.alibaba.nacos.core.jraft.NacosServer;
import com.alibaba.nacos.core.jraft.NamingService;
import com.alibaba.nacos.core.jraft.rpc.NamingRequest;

/**
 * @author jack_xjdai
 * @date 2020/4/15 19:17
 * @description: TODO
 */
public class NamingServiceImpl extends AbstractJRaftService implements NamingService {
    public NamingServiceImpl(NacosServer nacosServer) {
        super(nacosServer);
    }

    @Override
    public void register(NamingRequest request, NacosClosure closure) {

    }

    @Override
    public void deregister(NamingRequest request, NacosClosure closure) {

    }
}
