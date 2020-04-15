package com.alibaba.nacos.naming.jraft.rpc;

import com.alibaba.nacos.core.jraft.NacosClosure;
import com.alibaba.nacos.core.jraft.NamingService;
import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;
import com.alipay.sofa.jraft.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RegisterInstanceRequest processor.
 */
public class RegisterInstanceRequestProcessor extends AsyncUserProcessor<RegisterInstanceRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterInstanceRequestProcessor.class);

    private final NamingService namingService;

    public RegisterInstanceRequestProcessor(NamingService namingService) {
        super();
        this.namingService = namingService;
    }

    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, RegisterInstanceRequest request) {
        final NacosClosure closure = new NacosClosure() {
            @Override
            public void run(Status status) {
                asyncCtx.sendResponse(getValueResponse());
            }
        };

        this.namingService.register(request, closure);
    }

    @Override
    public String interest() {
        return RegisterInstanceRequest.class.getName();
    }
}
