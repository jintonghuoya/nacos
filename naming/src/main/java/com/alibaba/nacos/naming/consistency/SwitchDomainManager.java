package com.alibaba.nacos.naming.consistency;

import com.alibaba.nacos.naming.misc.SwitchDomain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jack_xjdai
 * @date 2020/4/11 20:50
 * @description: 负责管理服务注册中心核心数据结构组件
 */
public class SwitchDomainManager implements RecordManager<SwitchDomain> {
    // 核心数据结构
    private volatile Map<String, Datum<SwitchDomain>> datums = new ConcurrentHashMap<>();

    public Map<String, Datum<SwitchDomain>> getDatums() {
        return datums;
    }

    public void setDatums(Map<String, Datum<SwitchDomain>> datums) {
        this.datums = datums;
    }

    private SwitchDomainManager() {
    }

    private static class SingletonHolder {
        private static SwitchDomainManager INSTANCE = new SwitchDomainManager();
    }

    public static SwitchDomainManager getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
