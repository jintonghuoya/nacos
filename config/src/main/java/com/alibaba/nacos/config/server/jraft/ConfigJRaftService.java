package com.alibaba.nacos.config.server.jraft;

import com.alibaba.nacos.core.jraft.JRaftClosure;
import com.alibaba.nacos.config.server.jraft.rpc.ConfigJRaftRequest;

/**
 * @author jack_xjdai
 * @date 2020/4/15 19:08
 * @description: ConfigJRaftService接口类
 */
public interface ConfigJRaftService {
    void publish(ConfigJRaftRequest request, JRaftClosure closure);

    void remove(ConfigJRaftRequest request, JRaftClosure closure);
}
