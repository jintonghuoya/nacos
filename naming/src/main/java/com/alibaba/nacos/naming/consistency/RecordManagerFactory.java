package com.alibaba.nacos.naming.consistency;

import com.alibaba.nacos.naming.core.Instances;
import com.alibaba.nacos.naming.core.Service;
import com.alibaba.nacos.naming.misc.SwitchDomain;
import com.alibaba.nacos.naming.pojo.Record;

public enum RecordManagerFactory {
    SWITCH_DOMAIN("switchDomain", SwitchDomain.class, true, "switch_domain.data", "switch_domain.meta", SwitchDomainManager.getInstance()),
    SERVICE("service", Service.class, true, "service.data", "service.meta", ServiceManager.getInstance()),
    INSTANCES("instances", Instances.class, true, "instances.data", "instances.meta", InstancesManager.getInstance());

    private String id;
    private Class clazz;
    private Boolean isNeedSnapshot;
    private String snapshotDataFilename;
    private String snapshotMetaFilename;
    private RecordManager recordManager;

    RecordManagerFactory(String id,
                         Class clazz,
                         Boolean isNeedSnapshot,
                         String snapshotDataFilename,
                         String snapshotMetaFilename,
                         RecordManager<? extends Record> recordManager) {
        this.id = id;
        this.clazz = clazz;
        this.isNeedSnapshot = isNeedSnapshot;
        this.snapshotDataFilename = snapshotDataFilename;
        this.snapshotMetaFilename = snapshotMetaFilename;
        this.recordManager = recordManager;
    }

    public String getId() {
        return id;
    }

    public Class getClazz() {
        return clazz;
    }

    public Boolean getNeedSnapshot() {
        return isNeedSnapshot;
    }

    public String getSnapshotDataFilename() {
        return snapshotDataFilename;
    }

    public String getSnapshotMetaFilename() {
        return snapshotMetaFilename;
    }

    public RecordManager getRecordManager() {
        return recordManager;
    }

    public static RecordManager<? extends Record> determineRecordManager(String id) {
        for (RecordManagerFactory recordManagerFactory : RecordManagerFactory.values()) {
            if (recordManagerFactory.getId().equals(id)) {
                return recordManagerFactory.getRecordManager();
            }
        }
        throw new IllegalArgumentException("没有找到相对应的RecordManager");
    }


    public static RecordManager<? extends Record> determineRecordManagerByClass(Class clazz) {
        for (RecordManagerFactory recordManagerFactory : RecordManagerFactory.values()) {
            if (recordManagerFactory.getClazz().equals(clazz)) {
                return recordManagerFactory.getRecordManager();
            }
        }
        throw new IllegalArgumentException("没有找到相对应的RecordManager");
    }
}
