package com.alibaba.nacos.core.jraft;

import com.alibaba.nacos.core.utils.Loggers;
import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.SerializerManager;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.entity.Task;
import com.alipay.sofa.jraft.error.RaftError;
import org.apache.commons.lang.StringUtils;

import java.nio.ByteBuffer;

/**
 * @author jack_xjdai
 * @date 2020/4/15 19:13
 * @description: TODO
 */
public abstract class AbstractJRaftService implements JRaftService {

    private NacosServer nacosServer;

    public AbstractJRaftService(NacosServer nacosServer) {
        this.nacosServer = nacosServer;
    }

    private boolean isLeader() {
        return this.nacosServer.getFsm().isLeader();
    }

    private String getRedirect() {
        return this.nacosServer.redirect().getRedirect();
    }

    public void applyOperation(NacosOperation operation, NacosClosure closure) {
        if (!isLeader()) {
            handlerNotLeaderError(closure);
            return;
        }

        try {
            closure.setNacosOperation(operation);
            final Task task = new Task();
            task.setData(ByteBuffer.wrap(
                SerializerManager.getSerializer(SerializerManager.Hessian2).serialize(operation)));
            task.setDone(closure);
            this.nacosServer.getNode().apply(task);
        } catch (CodecException e) {
            String errorMsg = "Fail to encode CounterOperation";
            Loggers.AUTH.error(errorMsg, e);
            closure.failure(errorMsg, StringUtils.EMPTY);
            closure.run(new Status(RaftError.EINTERNAL, errorMsg));
        }
    }

    private void handlerNotLeaderError(final NacosClosure closure) {
        closure.failure("Not leader.", getRedirect());
        closure.run(new Status(RaftError.EPERM, "Not leader"));
    }

}
