package com.alibaba.nacos.naming.consistency.snapshot;

import com.alibaba.nacos.consistency.snapshot.CallFinally;
import com.alibaba.nacos.consistency.snapshot.LocalFileMeta;
import com.alibaba.nacos.consistency.snapshot.Reader;
import com.alibaba.nacos.consistency.snapshot.Writer;
import com.alibaba.nacos.core.utils.DiskUtils;
import com.alibaba.nacos.naming.consistency.*;
import com.alibaba.nacos.naming.consistency.persistent.raft.RaftStore;
import com.alibaba.nacos.naming.core.Instance;
import com.alibaba.nacos.naming.core.Instances;
import com.alibaba.nacos.naming.core.Service;
import com.alibaba.nacos.naming.misc.SwitchDomain;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author jack_xjdai
 * @date 2020/4/1411:50
 * @description: NamingSnapshotOperation单元测试类
 */
public class NamingSnapshotOperationTest {

    private NamingSnapshotOperation namingSnapshotOperation;

    private final static String SNAPSHOT_DIR = UtilsAndCommons.DATA_BASE_DIR + File.separator + "snapshot";

    private RaftStore raftStore;

    @Before
    public void init() {
        namingSnapshotOperation = new NamingSnapshotOperation();
        raftStore = new RaftStore();
    }

    @After
    public void destroy() throws IOException {
        DiskUtils.deleteDirectory(namingSnapshotOperation.oldSnapshotDir);
    }

    @Test
    public void onSnapshotSave() throws Exception {

        // 先加载快照
        onSnapshotLoad();

        // 在保存快照
        Writer writer = new Writer(SNAPSHOT_DIR);
        namingSnapshotOperation.onSnapshotSave(writer, new CallFinally(new BiConsumer<Boolean, Throwable>() {
            @Override
            public void accept(Boolean aBoolean, Throwable throwable) {
                System.out.println(aBoolean);
            }
        }));
    }

    @Test
    public void onSnapshotLoad() throws Exception {

        // 准备老版本的快照
        prepareOldSnapshot();

        // 加载快照
        Map<String, LocalFileMeta> metaMap = new HashMap<>();
        Reader reader = new Reader(SNAPSHOT_DIR, metaMap);
        namingSnapshotOperation.onSnapshotLoad(reader);

        Assert.assertEquals(1, SwitchDomainManager.getInstance().getDatums().size());
        Assert.assertEquals(1, ServiceManager.getInstance().getDatums().size());
        Assert.assertEquals(2, InstancesManager.getInstance().getDatums().size());
    }

    /**
     * 准备老版本的快照数据，并落地磁盘
     *
     * @throws Exception
     */
    @Test
    public void prepareOldSnapshot() throws Exception {
        //
        // 创建老版本的snapshot目录
        DiskUtils.forceMkdir(namingSnapshotOperation.oldSnapshotDir);

        fakeSwitchDomain();
        fakeServiceSnapshot();
        fakeInstancesSnapshot();
    }

    private void fakeSwitchDomain() throws Exception {
        // SwitchDomain
        Datum<SwitchDomain> switchDomainDatum = new Datum<>();
        switchDomainDatum.key = UtilsAndCommons.getSwitchDomainKey();
        switchDomainDatum.timestamp.getAndIncrement();
        switchDomainDatum.value = new SwitchDomain();

        raftStore.write(switchDomainDatum);
    }

    private void fakeServiceSnapshot() throws Exception {
        Service service = new Service(TEST_SERVICE_NAME);
        service.setProtectThreshold(6.23f);
        service.setEnabled(true);

        Map<String, String> metadataMap = new HashMap<>(16);
        metadataMap.put("testKey", "testValue");
        service.setMetadata(metadataMap);

        service.setNamespaceId(TEST_NAMESPACE);

        // now valid the service. if failed, exception will be thrown
        service.setLastModifiedMillis(System.currentTimeMillis());
        service.recalculateChecksum();
        service.validate();

        Datum<Service> serviceDatum = new Datum<>();

        serviceDatum.key = KeyBuilder.buildServiceMetaKey(TEST_NAMESPACE, TEST_SERVICE_NAME);
        serviceDatum.timestamp.getAndIncrement();
        serviceDatum.value = service;

        raftStore.write(serviceDatum);
    }

    private void fakeInstancesSnapshot() throws Exception {
        Datum<Instances> ephemeralInstancesDatum = new Datum<>();
        ephemeralInstancesDatum.key = KeyBuilder.buildInstanceListKey(TEST_NAMESPACE, TEST_SERVICE_NAME, true);
        ephemeralInstancesDatum.timestamp.getAndIncrement();
        ephemeralInstancesDatum.value = new Instances();
        Instance ephemeralInstance = new Instance("1.1.1.1", 1, TEST_CLUSTER_NAME);
        ephemeralInstancesDatum.value.getInstanceList().add(ephemeralInstance);
        ephemeralInstance = new Instance("2.2.2.2", 2, TEST_CLUSTER_NAME);
        ephemeralInstancesDatum.value.getInstanceList().add(ephemeralInstance);

        raftStore.write(ephemeralInstancesDatum);

        Datum<Instances> persistentInstancesDatum = new Datum<>();
        persistentInstancesDatum.key = KeyBuilder.buildInstanceListKey(TEST_NAMESPACE, TEST_SERVICE_NAME, false);
        persistentInstancesDatum.timestamp.getAndIncrement();
        persistentInstancesDatum.value = new Instances();
        Instance persistentInstance = new Instance("1.1.1.1", 1, TEST_CLUSTER_NAME);
        persistentInstancesDatum.value.getInstanceList().add(persistentInstance);
        persistentInstance = new Instance("2.2.2.2", 2, TEST_CLUSTER_NAME);
        persistentInstancesDatum.value.getInstanceList().add(persistentInstance);

        raftStore.write(persistentInstancesDatum);
    }

    protected static final String TEST_CLUSTER_NAME = "test-cluster";
    protected static final String TEST_SERVICE_NAME = "test-service";
    protected static final String TEST_GROUP_NAME = "test-group-name";
    protected static final String TEST_NAMESPACE = "test-namespace";
}
