package com.alibaba.nacos.naming.consistency;

import com.alibaba.nacos.naming.core.Instances;
import com.alibaba.nacos.naming.core.Service;
import com.alibaba.nacos.naming.misc.SwitchDomain;

public enum RecordManagerFactory {
    SWITCH_DOMAIN("switchDomain", SwitchDomain.class, SwitchDomainManager.getInstance()),
    SERVICE("service", Service.class, ServiceManager.getInstance()),
    INSTANCES("instances", Instances.class, InstancesManager.getInstance());

    private String id;
    private Class<?> clazz;
    private RecordManager recordManager;

    RecordManagerFactory(String id, Class<?> clazz, RecordManager recordManager) {
        this.id = id;
        this.clazz = clazz;
        this.recordManager = recordManager;
    }

    public String getId() {
        return id;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public RecordManager getRecordManager() {
        return recordManager;
    }

    public static RecordManager determineRecordManager(String id) {
        for (RecordManagerFactory recordManagerFactory : RecordManagerFactory.values()) {
            if (recordManagerFactory.getId().equals(id)) {
                return recordManagerFactory.getRecordManager();
            }
        }
        throw new IllegalArgumentException("没有找到相对应的RecordManager");
    }


    public static RecordManager determineRecordManagerByClass(Class<?> clazz) {
        for (RecordManagerFactory recordManagerFactory : RecordManagerFactory.values()) {
            if (recordManagerFactory.getClazz().equals(clazz)) {
                return recordManagerFactory.getRecordManager();
            }
        }
        throw new IllegalArgumentException("没有找到相对应的RecordManager");
    }
}
