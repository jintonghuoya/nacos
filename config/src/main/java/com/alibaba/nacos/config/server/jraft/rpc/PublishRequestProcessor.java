package com.alibaba.nacos.config.server.jraft.rpc;

import com.alibaba.nacos.config.server.jraft.ConfigJRaftService;
import com.alibaba.nacos.core.jraft.JRaftClosure;
import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;
import com.alipay.sofa.jraft.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublishRequestProcessor extends AsyncUserProcessor<PublishJRaftRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(PublishRequestProcessor.class);

    private final ConfigJRaftService configJraftService;

    public PublishRequestProcessor(ConfigJRaftService configJraftService) {
        super();
        this.configJraftService = configJraftService;
    }

    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, PublishJRaftRequest request) {
        final JRaftClosure closure = new JRaftClosure() {
            @Override
            public void run(Status status) {
                asyncCtx.sendResponse(getJRaftGenericResponse());
            }
        };

        this.configJraftService.publish(request, closure);
    }

    @Override
    public String interest() {
        return PublishJRaftRequest.class.getName();
    }
}
