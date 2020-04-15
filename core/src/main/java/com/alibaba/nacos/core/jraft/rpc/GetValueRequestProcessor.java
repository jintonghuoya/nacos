package com.alibaba.nacos.core.jraft.rpc;

import com.alibaba.nacos.core.jraft.NacosClosure;
import com.alibaba.nacos.core.jraft.NamingService;
import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;
import com.alipay.sofa.jraft.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GetValueRequest processor.
 *
 * @author boyan (boyan@alibaba-inc.com)
 * <p>
 * 2018-Apr-09 5:48:33 PM
 */
public class GetValueRequestProcessor extends AsyncUserProcessor<GetValueRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(GetValueRequestProcessor.class);

    private final NamingService namingService;

    public GetValueRequestProcessor(NamingService namingService) {
        super();
        this.namingService = namingService;
    }

    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, GetValueRequest request) {
        final NacosClosure closure = new NacosClosure() {
            @Override
            public void run(Status status) {
                asyncCtx.sendResponse(getValueResponse());
            }
        };

        this.namingService.get(request.isReadOnlySafe(), closure);
    }

    @Override
    public String interest() {
        return GetValueRequest.class.getName();
    }
}
