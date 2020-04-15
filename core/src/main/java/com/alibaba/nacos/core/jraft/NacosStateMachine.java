package com.alibaba.nacos.core.jraft;

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Iterator;
import com.alipay.sofa.jraft.core.StateMachineAdapter;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;

/**
 * @author jack_xjdai
 * @date 2020/4/1517:51
 * @description: 基于JRaft实现的Nacos的状态机
 */
public class NacosStateMachine extends StateMachineAdapter {

    /**
     * JRaft状态机数据处理主入口
     *
     * @param iterator
     */
    @Override
    public void onApply(Iterator iterator) {

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
}
