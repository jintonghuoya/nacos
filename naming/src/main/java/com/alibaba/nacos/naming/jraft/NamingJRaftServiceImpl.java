package com.alibaba.nacos.naming.jraft;

import com.alibaba.nacos.core.jraft.AbstractJRaftService;
import com.alibaba.nacos.core.jraft.JRaftClosure;
import com.alibaba.nacos.core.jraft.JRaftServer;
import com.alibaba.nacos.naming.jraft.rpc.NamingJRaftRequest;
import com.alibaba.nacos.naming.jraft.rpc.RegisterInstancesJRaftRequest;

/**
 * @author jack_xjdai
 * @date 2020/4/15 19:17
 * @description: TODO
 */
public class NamingJRaftServiceImpl extends AbstractJRaftService implements NamingJRaftService {
    public NamingJRaftServiceImpl(JRaftServer JRaftServer) {
        super(JRaftServer);
    }

    @Override
    public void registerInstances(RegisterInstancesJRaftRequest request, JRaftClosure closure) {
        applyOperation(NamingJRaftOperation.createRegisterInstancesOperation(request), closure);
    }

    @Override
    public void deregisterInstances(NamingJRaftRequest request, JRaftClosure closure) {
        applyOperation(NamingJRaftOperation.createDeregisterInstancesOperation(request), closure);
    }
}
