package com.alibaba.nacos.config.server.jraft.rpc;

import com.alibaba.nacos.core.jraft.ConfigService;
import com.alibaba.nacos.core.jraft.NacosClosure;
import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;
import com.alipay.sofa.jraft.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RegisterInstanceRequest processor.
 */
public class PublishRequestProcessor extends AsyncUserProcessor<PublishRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(PublishRequestProcessor.class);

    private final ConfigService configService;

    public PublishRequestProcessor(ConfigService configService) {
        super();
        this.configService = configService;
    }

    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, PublishRequest request) {
        final NacosClosure closure = new NacosClosure() {
            @Override
            public void run(Status status) {
                asyncCtx.sendResponse(getValueResponse());
            }
        };

        this.configService.publish(request, closure);
    }

    @Override
    public String interest() {
        return PublishRequest.class.getName();
    }
}
