package com.alibaba.nacos.core.jraft;

import com.alibaba.nacos.core.utils.Loggers;
import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.SerializerManager;
import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Iterator;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.core.StateMachineAdapter;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jack_xjdai
 * @date 2020/4/15 17:51
 * @description: 基于JRaft实现的Nacos的状态机
 */
public class NacosStateMachine extends StateMachineAdapter {

    /**
     * Leader term
     */
    private final AtomicLong leaderTerm = new AtomicLong(-1);

    public boolean isLeader() {
        return this.leaderTerm.get() > 0;
    }

    /**
     * 请求处理主入口
     *
     * @param iterator
     */
    @Override
    public void onApply(Iterator iterator) {
        while (iterator.hasNext()) {
            long current = 0;
            NacosOperation nacosOperation = null;

            NacosClosure closure = null;
            if (iterator.done() != null) {
                // This task is applied by this node, get value from closure to avoid additional parsing.
                closure = (NacosClosure) iterator.done();
                nacosOperation = closure.getNacosOperation();
            } else {
                // Have to parse FetchAddRequest from this user log.
                final ByteBuffer data = iterator.getData();
                try {
                    nacosOperation = SerializerManager.getSerializer(SerializerManager.Hessian2).deserialize(
                        data.array(), NacosOperation.class.getName());
                } catch (final CodecException e) {
                    Loggers.AUTH.error("Fail to decode IncrementAndGetRequest", e);
                }
            }
            if (nacosOperation != null) {
                switch (nacosOperation.getOp()) {
                    case NacosOperation.GET:
                        current = 1L;
                        Loggers.AUTH.info("Get value={} at logIndex={}", current, iterator.getIndex());
                        break;
                    case NacosOperation.INCREMENT:
                        final long delta = nacosOperation.getDelta();
                        final long prev = 1L;
                        AtomicLong value = new AtomicLong(1L);
                        current = value.addAndGet(delta);
                        Loggers.AUTH.info("Added value={} by delta={} at logIndex={}", prev, delta, iterator.getIndex());
                        break;
                }

                if (closure != null) {
                    closure.success(current);
                    closure.run(Status.OK());
                }
            }
            iterator.next();
        }
    }

    /**
     * 加载快照的入口
     *
     * @param reader
     * @return
     */
    @Override
    public boolean onSnapshotLoad(SnapshotReader reader) {
        return true;
    }

    /**
     * 保存快照的入口
     *
     * @param writer
     * @param done
     */
    @Override
    public void onSnapshotSave(SnapshotWriter writer, Closure done) {
        super.onSnapshotSave(writer, done);
    }


    @Override
    public void onLeaderStart(final long term) {
        this.leaderTerm.set(term);
        super.onLeaderStart(term);

    }

    @Override
    public void onLeaderStop(final Status status) {
        this.leaderTerm.set(-1);
        super.onLeaderStop(status);
    }


}
