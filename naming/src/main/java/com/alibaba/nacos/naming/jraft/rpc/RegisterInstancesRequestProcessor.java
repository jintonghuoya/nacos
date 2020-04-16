package com.alibaba.nacos.naming.jraft.rpc;

import com.alibaba.nacos.core.jraft.JRaftClosure;
import com.alibaba.nacos.naming.jraft.NamingJRaftService;
import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;
import com.alipay.sofa.jraft.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterInstancesRequestProcessor extends AsyncUserProcessor<RegisterInstancesJRaftRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterInstancesRequestProcessor.class);

    private final NamingJRaftService namingJRaftService;

    public RegisterInstancesRequestProcessor(NamingJRaftService namingJRaftService) {
        super();
        this.namingJRaftService = namingJRaftService;
    }

    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, RegisterInstancesJRaftRequest request) {
        final JRaftClosure closure = new JRaftClosure() {
            @Override
            public void run(Status status) {
                asyncCtx.sendResponse(getJRaftGenericResponse());
            }
        };

        this.namingJRaftService.registerInstances(request, closure);
    }

    @Override
    public String interest() {
        return RegisterInstancesJRaftRequest.class.getName();
    }
}
