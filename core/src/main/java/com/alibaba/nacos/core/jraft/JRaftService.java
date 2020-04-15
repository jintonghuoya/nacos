package com.alibaba.nacos.core.jraft;

/**
 * @author jack_xjdai
 * @date 2020/4/15 19:13
 * @description: JRaft统一服务抽象接口
 */
public interface JRaftService {
    void applyOperation(NacosOperation nacosOperation, NacosClosure nacosClosure);
}
