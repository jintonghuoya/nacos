package com.alibaba.nacos.naming.consistency;

import com.alibaba.nacos.naming.core.Instances;
import com.alibaba.nacos.naming.core.Service;
import com.alibaba.nacos.naming.misc.SwitchDomain;

/**
 * @author jack_xjdai
 * @date 2020/4/13 13:50
 * @description: RecordManager工厂类
 * Record是需要做落地磁盘快照的接口类
 * 实现它的实体类都可以支持落地磁盘做快照
 */
public enum RecordManagerFactory {
    SWITCH_DOMAIN("switchDomain", SwitchDomain.class, false, "switch_domain.data", "switch_domain.meta", SwitchDomainManager.getInstance()),
    SERVICE("service", Service.class, true, "service.data", "service.meta", ServiceManager.getInstance()),
    INSTANCES("instances", Instances.class, true, "instances.data", "instances.meta", InstancesManager.getInstance());

    // 唯一标识
    private String id;
    // 存储数据的类型，即Datum<T extends Record>中的T
    private Class clazz;
    // 是否需要落地磁盘做快照
    private Boolean isNeedSnapshot;
    // 快照对应的数据文件名
    private String snapshotDataFilename;
    // 快照对应的元数据文件名
    private String snapshotMetaFilename;
    // 存储数据对应的RecordManager的实现类
    private RecordManager recordManager;

    RecordManagerFactory(String id,
                         Class clazz,
                         Boolean isNeedSnapshot,
                         String snapshotDataFilename,
                         String snapshotMetaFilename,
                         RecordManager recordManager) {
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

    /**
     * 根据id获取RecordManager
     *
     * @param id
     * @return
     */
    public static RecordManager determineRecordManager(String id) {
        for (RecordManagerFactory recordManagerFactory : RecordManagerFactory.values()) {
            if (recordManagerFactory.getId().equals(id)) {
                return recordManagerFactory.getRecordManager();
            }
        }
        throw new IllegalArgumentException("没有找到相对应的RecordManager");
    }

    /**
     * 根据Datum的Value的类型确定RecordManager
     *
     * @param clazz
     * @return
     */
    public static RecordManager determineRecordManagerByClass(Class clazz) {
        for (RecordManagerFactory recordManagerFactory : RecordManagerFactory.values()) {
            if (recordManagerFactory.getClazz().equals(clazz)) {
                return recordManagerFactory.getRecordManager();
            }
        }
        throw new IllegalArgumentException("没有找到相对应的RecordManager");
    }

    /**
     * 根据Datum的Value的类型确定RecordManagerFactory
     *
     * @param clazz
     * @return
     */
    public static RecordManagerFactory determineRecordManagerFactoryByClass(Class clazz) {
        for (RecordManagerFactory recordManagerFactory : RecordManagerFactory.values()) {
            if (recordManagerFactory.getClazz().equals(clazz)) {
                return recordManagerFactory;
            }
        }
        throw new IllegalArgumentException("没有找到相对应的RecordManagerFactory");
    }
}
