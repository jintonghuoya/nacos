package com.alibaba.nacos.naming.consistency.snapshot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.consistency.snapshot.Reader;
import com.alibaba.nacos.consistency.snapshot.Writer;
import com.alibaba.nacos.consistency.snapshot.*;
import com.alibaba.nacos.core.utils.DiskUtils;
import com.alibaba.nacos.naming.consistency.*;
import com.alibaba.nacos.naming.core.Instance;
import com.alibaba.nacos.naming.core.Instances;
import com.alibaba.nacos.naming.core.Service;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.naming.misc.SwitchDomain;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import com.alipay.sofa.jraft.util.Utils;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jack_xjdai
 * @date 2020/4/713:50
 * @description: 注册中心快照操作
 * <p>
 * TODO 现行的存储是SwitchDomain，Service和Instances三个对象对象都落地磁盘
 * TODO 并且都在RaftCore中管理
 * TODO 需要将这三块的数据，统一存储，统一读取逻辑
 * TODO 或者保留老版本的数据存储格式，每个都存一份
 * TODO 这样就需要考虑存储快照以及读取快照的时候的效率问题
 */
public class NamingSnapshotOperation implements SnapshotOperation {

    public NamingSnapshotOperation() {
    }

    // 老版本的naming的实例的镜像存储路径
    private final String oldSnapshotDir = UtilsAndCommons.DATA_BASE_DIR + File.separator + "data";

    // 快照文件存储的全路径
    private String snapshotDataFilename;

    // 快照元信息存储的全路径
    private String snapshotMetaFilename;

    /**
     * 通过判断老版本的snapshot目录下是否有文件，来确定是否需要做存储版本切换
     *
     * @return
     */
    private File[] listOldSnapshotDirs() {
        File tempDir = new File(this.oldSnapshotDir);
        if (!tempDir.exists()) {
            return null;
        }
        return tempDir.listFiles();
    }

    private boolean isNeedSwitchStorage() {
        File[] oldSnapshotDirs = listOldSnapshotDirs();
        if (null != oldSnapshotDirs && oldSnapshotDirs.length > 0) {
            return true;
        }
        return false;
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

        initSnapshotFilename(writer.getPath());

        Utils.runInThread(() -> {
            try {
                saveSnapshot();
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

        initSnapshotFilename(reader.getPath());

        if (isNeedSwitchStorage()) {
            // 1.读取老版本的数据到内存中
            try {
                // 将加载出来的datums赋值给新版的内存数据结构
                loadOldSnapshot();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 2.将读取到的数据，先做一次新版本的snapshot
            try {
                saveSnapshot();
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
    private void loadOldSnapshot() throws IOException {
        Map<String, Object> compositeMap = new ConcurrentHashMap<>();
        Map<String, Datum<SwitchDomain>> switchDomainDatums = new ConcurrentHashMap<>();
        Map<String, Datum<Service>> serviceDatums = new ConcurrentHashMap<>();
        Map<String, Datum<Instances>> instancesDatums = new ConcurrentHashMap<>();

        compositeMap.put("switchDomainMap", switchDomainDatums);
        compositeMap.put("serviceMap", serviceDatums);
        compositeMap.put("instancesMap", instancesDatums);

        Datum datum = null;
        long start = System.currentTimeMillis();
        for (File snapshotDir : listOldSnapshotDirs()) {
            if (snapshotDir.isDirectory() && snapshotDir.listFiles() != null) {
                for (File datumFile : snapshotDir.listFiles()) {
                    datum = readOldDatum(datumFile, snapshotDir.getName());
                    if (datum.value instanceof SwitchDomain) {
                        switchDomainDatums.put(datum.key, datum);
                    } else if (datum.value instanceof Service) {
                        serviceDatums.put(datum.key, datum);
                    } else if (datum.value instanceof Instances) {
                        instancesDatums.put(datum.key, datum);
                    } else {
                        throw new IllegalArgumentException("不支持的类型......");
                    }
                }
                continue;
            }
            datum = readOldDatum(snapshotDir, StringUtils.EMPTY);
            if (datum != null) {
                if (datum.value instanceof SwitchDomain) {
                    switchDomainDatums.put(datum.key, datum);
                } else if (datum.value instanceof Service) {
                    serviceDatums.put(datum.key, datum);
                } else if (datum.value instanceof Instances) {
                    instancesDatums.put(datum.key, datum);
                } else {
                    throw new IllegalArgumentException("不支持的类型......");
                }
            }
        }

        if (!switchDomainDatums.isEmpty()) {
            SwitchDomainManager.getInstance().setDatums(switchDomainDatums);
        }

        if (!serviceDatums.isEmpty()) {
            ServiceManager.getInstance().setDatums(serviceDatums);
        }

        if (!instancesDatums.isEmpty()) {
            InstancesManager.getInstance().setDatums(instancesDatums);
        }
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

        // 将加载出来的datums，赋值给内存结构
        readData(snapshotDataFilename, snapshotLength);
    }


    /**
     * 将当前最新的内存数据存储到磁盘
     * 1.data信息落地磁盘
     * 2.meta信息落地磁盘
     */
    private void saveSnapshot() throws IOException {
        // 拿到当前的内存数据
        Map<String, Datum<SwitchDomain>> datums = SwitchDomainManager.getInstance().getDatums();

        if (datums.isEmpty()) {
            return;
        }

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
     *
     * @param filename
     * @return
     * @throws IOException
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

    /**
     * 初始化快照相关的文件全路径参数
     *
     * @param baseDir
     */
    private synchronized void initSnapshotFilename(String baseDir) {
        if (Strings.isNullOrEmpty(baseDir)) {
            throw new IllegalArgumentException();
        }
        // 只初始化一次
        if (Strings.isNullOrEmpty(snapshotDataFilename)
            || Strings.isNullOrEmpty(snapshotMetaFilename)) {
            snapshotDataFilename = baseDir + File.separator + "data";
            snapshotMetaFilename = baseDir + File.separator + "meta";
        }
    }
}
