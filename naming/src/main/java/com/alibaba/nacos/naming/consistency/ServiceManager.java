package com.alibaba.nacos.naming.consistency;

import com.alibaba.nacos.naming.core.Service;

/**
 * @author jack_xjdai
 * @date 2020/4/11 20:50
 * @description: 负责管理Service数据
 */
public class ServiceManager extends AbstractRecordManager<Service> {
    private ServiceManager() {
    }

    private static class SingletonHolder {
        private static ServiceManager INSTANCE = new ServiceManager();
    }

    public static ServiceManager getInstance() {
        return ServiceManager.SingletonHolder.INSTANCE;
    }
}
