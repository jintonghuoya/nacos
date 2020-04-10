package com.alibaba.nacos.naming.consistency.snapshot;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.consistency.snapshot.CallFinally;
import com.alibaba.nacos.consistency.snapshot.Reader;
import com.alibaba.nacos.consistency.snapshot.SnapshotOperation;
import com.alibaba.nacos.consistency.snapshot.Writer;
import com.alibaba.nacos.naming.consistency.Datum;
import com.alibaba.nacos.naming.consistency.persistent.raft.RaftCore;
import com.alibaba.nacos.naming.consistency.persistent.raft.RaftStore;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import com.alipay.sofa.jraft.util.Utils;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author jack_xjdai
 * @date 2020/4/713:50
 * @description: 注册中心快照操作
 */
public class NamingSnapshotOperation implements SnapshotOperation {

    @Autowired
    private RaftCore raftCore;

    @Autowired
    private RaftStore raftStore;

    private String oldSnapshotDir = UtilsAndCommons.DATA_BASE_DIR + File.separator + "data";

    /**
     * 通过判断老版本的snapshot目录下是否有文件，来确定是否需要做存储版本切换
     *
     * @return
     */
    private File[] listOldSnapshotFiles() {
        File tempDir = new File(this.oldSnapshotDir);
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            throw new IllegalStateException("cloud not make out directory: " + tempDir.getName());
        }
        return tempDir.listFiles();
    }


    /**
     * 定时保存快照
     *
     * @param writer      {@link Writer}
     * @param callFinally Callback {@link CallFinally#run(boolean, Throwable)} when the snapshot operation is complete
     */
    @Override
    public void onSnapshotSave(Writer writer, CallFinally callFinally) {
        String snapshotFilename = writer.getPath() + File.separator + "data";
        Utils.runInThread(() -> {
            try {
                doSnapshot(snapshotFilename);
                callFinally.run(true, null);
            } catch (Throwable t) {
                callFinally.run(false, t);
            }
        });

    }

    /**
     * 系统启动时，加载快照文件
     * <p>
     * 新版本上线后，老版本的RaftCore就应该不执行了
     * 所以不需要考虑之前老版本会有增量数据
     *
     * @param reader {@link Reader}
     * @return
     */
    @Override
    public boolean onSnapshotLoad(Reader reader) {
        String snapshotFilename = reader.getPath() + File.separator + "data";
        if (listOldSnapshotFiles().length > 0) {
            // 1.读取老版本的数据到内存中
            loadOldSnapshot();

            // 2.将读取到的数据，先做一次新版本的snapshot
            doSnapshot(snapshotFilename);

            // 3.删除老版本的snapshot文件
            deleteOldSnapshotFiles();

        } else {
            // 正常执行逻辑即可
            load(snapshotFilename);
        }
        return true;
    }

    private boolean deleteOldSnapshotFiles() {
        return false;
    }

    private boolean loadOldSnapshot() {
        return false;
    }


    /**
     * 加载
     *
     * @param snapshotFilename
     * @return
     */
    private boolean load(String snapshotFilename) {
        return false;
    }


    /**
     * 将当前最新的内存数据存储到磁盘
     * 1.快照存储
     * TODO 2.Meta信息存储
     */
    private boolean doSnapshot(String snapshotFilename) {
        if (Strings.isNullOrEmpty(snapshotFilename)) {
            throw new IllegalArgumentException();
        }

        // 这个应该是从新的数据结构获取
        ConcurrentMap<String, Datum> datums = new ConcurrentHashMap<>();

        ByteBuffer dataBuffer = ByteBuffer.wrap(JSON.toJSONBytes(datums));
        RandomAccessFile file = null;
        FileOutputStream out = null;
        FileChannel snapshotFileChannel = null;
        try {
            file = new RandomAccessFile(snapshotFilename, "rw");
            out = new FileOutputStream(file.getFD());
            snapshotFileChannel = out.getChannel();
            snapshotFileChannel.write(dataBuffer);
            snapshotFileChannel.force(false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (snapshotFileChannel != null) {
                try {
                    snapshotFileChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}
