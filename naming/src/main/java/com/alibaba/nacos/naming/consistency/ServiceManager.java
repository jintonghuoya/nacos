package com.alibaba.nacos.naming.consistency;

import com.alibaba.nacos.naming.core.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jack_xjdai
 * @date 2020/4/11 20:50
 * @description: 负责管理服务注册中心核心数据结构组件
 */
public class ServiceManager implements RecordManager<Service> {
    // 核心数据结构
    private volatile Map<String, Datum<Service>> datums = new ConcurrentHashMap<>();

    public Map<String, Datum<Service>> getDatums() {
        return datums;
    }

    public void setDatums(Map<String, Datum<Service>> datums) {
        this.datums = datums;
    }

    private ServiceManager() {
    }

    private static class SingletonHolder {
        private static ServiceManager INSTANCE = new ServiceManager();
    }

    public static ServiceManager getInstance() {
        return ServiceManager.SingletonHolder.INSTANCE;
    }
}
