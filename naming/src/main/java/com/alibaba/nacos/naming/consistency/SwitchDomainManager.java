package com.alibaba.nacos.naming.consistency;

import com.alibaba.nacos.naming.misc.SwitchDomain;

/**
 * @author jack_xjdai
 * @date 2020/4/11 20:50
 * @description: 负责管理SwitchDomain数据
 */
public class SwitchDomainManager extends AbstractRecordManager<SwitchDomain> {
    private SwitchDomainManager() {
    }

    private static class SingletonHolder {
        private static SwitchDomainManager INSTANCE = new SwitchDomainManager();
    }

    public static SwitchDomainManager getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
