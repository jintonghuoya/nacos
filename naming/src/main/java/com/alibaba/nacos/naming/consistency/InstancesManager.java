package com.alibaba.nacos.naming.consistency;

import com.alibaba.nacos.naming.core.Instances;

/**
 * @author jack_xjdai
 * @date 2020/4/11 20:50
 * @description: 负责管理Instances数据
 */
public class InstancesManager extends AbstractRecordManager<Instances> {
    private InstancesManager() {
    }

    private static class SingletonHolder {
        private static InstancesManager INSTANCE = new InstancesManager();
    }

    public static InstancesManager getInstance() {
        return InstancesManager.SingletonHolder.INSTANCE;
    }

}
