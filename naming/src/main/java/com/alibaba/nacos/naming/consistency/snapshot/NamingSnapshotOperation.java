package com.alibaba.nacos.naming.consistency.snapshot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.consistency.snapshot.Reader;
import com.alibaba.nacos.consistency.snapshot.Writer;
import com.alibaba.nacos.consistency.snapshot.*;
import com.alibaba.nacos.core.utils.DiskUtils;
import com.alibaba.nacos.naming.consistency.Datum;
import com.alibaba.nacos.naming.consistency.KeyBuilder;
import com.alibaba.nacos.naming.core.Instance;
import com.alibaba.nacos.naming.core.Instances;
import com.alibaba.nacos.naming.core.Service;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.naming.misc.SwitchDomain;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import com.alipay.sofa.jraft.util.Utils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author jack_xjdai
 * @date 2020/4/713:50
 * @description: 注册中心快照操作
 */
public class NamingSnapshotOperation implements SnapshotOperation {

    // 老版本的naming的实例的镜像存储路径
    private final String oldSnapshotDir = UtilsAndCommons.DATA_BASE_DIR + File.separator + "data";

    private String snapshotDataFilename;

    private String snapshotMetaFilename;

    /**
     * 通过判断老版本的snapshot目录下是否有文件，来确定是否需要做存储版本切换
     *
     * @return
     */
    private File[] listOldSnapshotDirs() {
        File tempDir = new File(this.oldSnapshotDir);
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            throw new IllegalStateException("cloud not make out directory: " + tempDir.getName());
        }
        return tempDir.listFiles();
    }


    /**
     * 定时保存快照
     * 异步执行，COW
     * 不阻塞读与写
     *
     * @param writer      {@link Writer}
     * @param callFinally Callback {@link CallFinally#run(boolean, Throwable)} when the snapshot operation is complete
     */
    @Override
    public void onSnapshotSave(Writer writer, CallFinally callFinally) {
        snapshotDataFilename = writer.getPath() + File.separator + "data";
        snapshotMetaFilename = writer.getPath() + File.separator + "meta";
        Utils.runInThread(() -> {
            try {
                doSnapshot();
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
        snapshotDataFilename = reader.getPath() + File.separator + "data";
        snapshotMetaFilename = reader.getPath() + File.separator + "meta";

        if (listOldSnapshotDirs().length > 0) {
            // 1.读取老版本的数据到内存中
            try {
                // TODO 将加载出来的datums赋值给新版JRaft的存储
                Map<String, Datum> datums = loadOldSnapshot();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 2.将读取到的数据，先做一次新版本的snapshot
            try {
                doSnapshot();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 3.删除老版本的snapshot文件
            try {
                deleteOldSnapshotFiles();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            // 正常执行逻辑即可
            try {
                loadSnapshot();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void deleteOldSnapshotFiles() throws IOException {
        DiskUtils.deleteDirectory(oldSnapshotDir);
    }

    /**
     * 加载老版本保存在磁盘上的数据
     *
     * @throws IOException
     */
    private Map<String, Datum> loadOldSnapshot() throws IOException {
        Map<String, Datum> datums = new ConcurrentHashMap<>();
        Datum datum = null;
        long start = System.currentTimeMillis();
        for (File snapshotDir : listOldSnapshotDirs()) {
            if (snapshotDir.isDirectory() && snapshotDir.listFiles() != null) {
                for (File datumFile : snapshotDir.listFiles()) {
                    datum = readOldDatum(datumFile, snapshotDir.getName());
                    if (datum != null) {
                        datums.put(datum.key, datum);
                    }
                }
                continue;
            }
            datum = readOldDatum(snapshotDir, StringUtils.EMPTY);
            if (datum != null) {
                datums.put(datum.key, datum);
            }
        }
        Loggers.RAFT.info("finish loading all datums, size: {} cost {} ms.", datums.size(), (System.currentTimeMillis() - start));
        return datums;
    }


    /**
     * 读取老版本缓存到磁盘上的Datum
     *
     * @param file
     * @param namespaceId
     * @return
     * @throws IOException
     */
    public Datum readOldDatum(File file, String namespaceId) throws IOException {

        ByteBuffer buffer;
        FileChannel fc = null;
        try {
            fc = new FileInputStream(file).getChannel();
            buffer = ByteBuffer.allocate((int) file.length());
            fc.read(buffer);

            String json = new String(buffer.array(), StandardCharsets.UTF_8);
            if (StringUtils.isBlank(json)) {
                return null;
            }

            if (KeyBuilder.matchSwitchKey(file.getName())) {
                return JSON.parseObject(json, new TypeReference<Datum<SwitchDomain>>() {
                });
            }

            if (KeyBuilder.matchServiceMetaKey(file.getName())) {

                Datum<Service> serviceDatum;

                try {
                    serviceDatum = JSON.parseObject(json.replace("\\", ""), new TypeReference<Datum<Service>>() {
                    });
                } catch (Exception e) {
                    JSONObject jsonObject = JSON.parseObject(json);

                    serviceDatum = new Datum<>();
                    serviceDatum.timestamp.set(jsonObject.getLongValue("timestamp"));
                    serviceDatum.key = jsonObject.getString("key");
                    serviceDatum.value = JSON.parseObject(jsonObject.getString("value"), Service.class);
                }

                if (StringUtils.isBlank(serviceDatum.value.getGroupName())) {
                    serviceDatum.value.setGroupName(Constants.DEFAULT_GROUP);
                }
                if (!serviceDatum.value.getName().contains(Constants.SERVICE_INFO_SPLITER)) {
                    serviceDatum.value.setName(Constants.DEFAULT_GROUP
                        + Constants.SERVICE_INFO_SPLITER + serviceDatum.value.getName());
                }

                return serviceDatum;
            }

            if (KeyBuilder.matchInstanceListKey(file.getName())) {

                Datum<Instances> instancesDatum;

                try {
                    instancesDatum = JSON.parseObject(json, new TypeReference<Datum<Instances>>() {
                    });
                } catch (Exception e) {
                    JSONObject jsonObject = JSON.parseObject(json);
                    instancesDatum = new Datum<>();
                    instancesDatum.timestamp.set(jsonObject.getLongValue("timestamp"));

                    String key = jsonObject.getString("key");
                    String serviceName = KeyBuilder.getServiceName(key);
                    key = key.substring(0, key.indexOf(serviceName)) +
                        Constants.DEFAULT_GROUP + Constants.SERVICE_INFO_SPLITER + serviceName;

                    instancesDatum.key = key;
                    instancesDatum.value = new Instances();
                    instancesDatum.value.setInstanceList(JSON.parseObject(jsonObject.getString("value"),
                        new TypeReference<List<Instance>>() {
                        }));
                    if (!instancesDatum.value.getInstanceList().isEmpty()) {
                        for (Instance instance : instancesDatum.value.getInstanceList()) {
                            instance.setEphemeral(false);
                        }
                    }
                }

                return instancesDatum;
            }

            return JSON.parseObject(json, Datum.class);

        } catch (Exception e) {
            Loggers.RAFT.warn("waning: failed to deserialize key: {}", file.getName());
            throw e;
        } finally {
            if (fc != null) {
                fc.close();
            }
        }
    }


    /**
     * 加载
     *
     * @return
     */
    private void loadSnapshot() throws IOException {
        LocalFileMeta localFileMeta = readMeta(snapshotMetaFilename);

        int snapshotLength = (int) localFileMeta.get("snapshotLength");

        // TODO 将加载出来的datums，赋值给内存结构
        Map<String, Datum> datums = readData(snapshotDataFilename, snapshotLength);
    }


    /**
     * 将当前最新的内存数据存储到磁盘
     * 1.data信息落地磁盘
     * 2.meta信息落地磁盘
     */
    private void doSnapshot() throws IOException {
        // 这个应该是从新的数据结构获取
        // TODO 从别的地方获取
        ConcurrentMap<String, Datum> datums = new ConcurrentHashMap<>();
        byte[] dataBytes = JSON.toJSONBytes(datums);

        saveFile(snapshotDataFilename, dataBytes);

        LocalFileMeta localFileMeta = new LocalFileMeta();
        localFileMeta.append("snapshotFilename", snapshotDataFilename);
        localFileMeta.append("snapshotTimestamp", System.currentTimeMillis());
        localFileMeta.append("snapshotLength", dataBytes.length);
        byte[] metaBytes = JSON.toJSONBytes(localFileMeta);
        saveFile(snapshotMetaFilename, metaBytes);
    }

    /**
     * 基于NIO读取Data信息
     */
    private Map<String, Datum> readData(String filename, int fileLength) throws IOException {
        FileInputStream in = null;
        FileChannel channel = null;
        try {

            File file = new File(filename);
            if (!file.exists()) {
                throw new IllegalArgumentException();
            }

            in = new FileInputStream(file);
            channel = in.getChannel();

            ByteBuffer buffer = ByteBuffer.allocate(fileLength);
            channel.read(buffer);
            buffer.flip();

            return JSON.parseObject(buffer.array(), new TypeReference<ConcurrentHashMap<String, Datum>>() {
            }.getType());
        } finally {
            if (in != null) {
                in.close();
            }
            if (channel != null) {
                channel.close();
            }
        }
    }

    /**
     * 基于NIO读取Meta信息
     */
    private LocalFileMeta readMeta(String filename) throws IOException {
        FileInputStream in = null;
        FileChannel channel = null;
        try {

            File file = new File(filename);
            if (!file.exists()) {
                throw new IllegalArgumentException();
            }

            in = new FileInputStream(file);
            channel = in.getChannel();

            ByteBuffer buffer = ByteBuffer.allocate(4 * 1024);
            channel.read(buffer);
            buffer.flip();

            return JSON.parseObject(buffer.array(), LocalFileMeta.class);
        } finally {
            if (in != null) {
                in.close();
            }
            if (channel != null) {
                channel.close();
            }
        }
    }


    /**
     * 基于NIO读取磁盘文件
     *
     * @param filename
     * @param data
     * @throws IOException
     */
    private void saveFile(String filename, byte[] data) throws IOException {
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);
        RandomAccessFile file = null;
        FileOutputStream out = null;
        FileChannel snapshotFileChannel = null;
        try {
            file = new RandomAccessFile(filename, "rw");
            out = new FileOutputStream(file.getFD());
            snapshotFileChannel = out.getChannel();
            snapshotFileChannel.write(dataBuffer);
            snapshotFileChannel.force(false);
        } finally {
            if (snapshotFileChannel != null) {
                snapshotFileChannel.close();
            }
            if (out != null) {
                out.close();
            }
            if (file != null) {
                file.close();
            }
        }
    }
}
