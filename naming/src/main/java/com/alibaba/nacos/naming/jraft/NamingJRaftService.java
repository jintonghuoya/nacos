package com.alibaba.nacos.naming.jraft;

import com.alibaba.nacos.core.jraft.JRaftClosure;
import com.alibaba.nacos.naming.jraft.rpc.NamingJRaftRequest;
import com.alibaba.nacos.naming.jraft.rpc.RegisterInstancesJRaftRequest;

/**
 * @author jack_xjdai
 * @date 2020/4/1519:08
 * @description: NamingJRaftService接口类
 */
public interface NamingJRaftService {
    void registerInstances(RegisterInstancesJRaftRequest request, JRaftClosure closure);

    void deregisterInstances(NamingJRaftRequest request, JRaftClosure closure);
}
