package com.alibaba.nacos.naming.consistency.snapshot;

import com.alibaba.nacos.consistency.snapshot.CallFinally;
import com.alibaba.nacos.consistency.snapshot.Reader;
import com.alibaba.nacos.consistency.snapshot.SnapshotOperation;
import com.alibaba.nacos.consistency.snapshot.Writer;
import com.alibaba.nacos.naming.consistency.persistent.raft.RaftStore;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jack_xjdai
 * @date 2020/4/713:50
 * @description: 注册中心快照操作
 */
public class NamingSnapshotOperation implements SnapshotOperation {

    @Autowired
    private RaftStore raftStore;

    /**
     * 定时保存快照
     *
     * @param writer      {@link Writer}
     * @param callFinally Callback {@link CallFinally#run(boolean, Throwable)} when the snapshot operation is complete
     */
    @Override
    public void onSnapshotSave(Writer writer, CallFinally callFinally) {

    }

    /**
     * 系统启动时，加载快照文件
     * 之前版本的数据都是一个实例存到一个文件里
     * 此次更新会把内存中的实例信息作为一个整体保存到磁盘作为某一个时间点的快照
     * 所以需要做对老版本的兼容（如果是老版本的话，根据老版本的加载方式，将数据加载到内存，
     * 并将当前的内存结构打一个快照；并且删除之前的目录；下次启动就按照新版本的启动方式了）
     * <p>
     * FIXME 加载快照的时候，牵扯到之前的term，这块放到哪里加载？
     *
     * @param reader {@link Reader}
     * @return
     */
    @Override
    public boolean onSnapshotLoad(Reader reader) {
        if (raftStore.isNeedSwitchStorage()) {
            // 1.将已经加载到内存的数据，做成新版本的snapshot
            // 2.停止老版本快照生成的定时任务，并且删除当前老版本的数据目录的数据
        } else {
            // 正常执行逻辑即可
        }
        return false;
    }
}
