package com.alibaba.nacos.naming.consistency;

import com.alibaba.nacos.naming.core.Instances;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jack_xjdai
 * @date 2020/4/11 20:50
 * @description: 负责管理服务注册中心核心数据结构组件
 */
public class InstancesManager {
    // 核心数据结构
    private volatile Map<String, Datum<Instances>> datums = new ConcurrentHashMap<>();

    public Map<String, Datum<Instances>> getDatums() {
        return datums;
    }

    public void setDatums(Map<String, Datum<Instances>> datums) {
        this.datums = datums;
    }
}
