package com.alibaba.nacos.naming.jraft.rpc;

import com.alibaba.nacos.core.jraft.JRaftClosure;
import com.alibaba.nacos.naming.jraft.NamingJRaftService;
import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;
import com.alipay.sofa.jraft.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeregisterInstancesRequestProcessor extends AsyncUserProcessor<DeregisterInstancesJRaftRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(DeregisterInstancesRequestProcessor.class);

    private final NamingJRaftService namingJRaftService;

    public DeregisterInstancesRequestProcessor(NamingJRaftService namingJRaftService) {
        super();
        this.namingJRaftService = namingJRaftService;
    }

    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, DeregisterInstancesJRaftRequest request) {
        final JRaftClosure closure = new JRaftClosure() {
            @Override
            public void run(Status status) {
                asyncCtx.sendResponse(getJRaftGenericResponse());
            }
        };

        this.namingJRaftService.deregisterInstances(request, closure);
    }

    @Override
    public String interest() {
        return DeregisterInstancesJRaftRequest.class.getName();
    }
}
