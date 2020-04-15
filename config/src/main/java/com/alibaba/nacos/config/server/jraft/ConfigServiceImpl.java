package com.alibaba.nacos.config.server.jraft;

import com.alibaba.nacos.core.jraft.AbstractJRaftService;
import com.alibaba.nacos.core.jraft.ConfigService;
import com.alibaba.nacos.core.jraft.NacosClosure;
import com.alibaba.nacos.core.jraft.NacosServer;
import com.alibaba.nacos.core.jraft.rpc.ConfigRequest;

/**
 * @author jack_xjdai
 * @date 2020/4/15 19:17
 * @description: TODO
 */
public class ConfigServiceImpl extends AbstractJRaftService implements ConfigService {

    public ConfigServiceImpl(NacosServer nacosServer) {
        super(nacosServer);
    }

    @Override
    public void publish(ConfigRequest request, NacosClosure closure) {

    }

    @Override
    public void remove(ConfigRequest request, NacosClosure closure) {

    }
}
