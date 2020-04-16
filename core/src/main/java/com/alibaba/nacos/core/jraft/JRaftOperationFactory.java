package com.alibaba.nacos.core.jraft;

import com.alibaba.nacos.api.exception.NacosException;

/**
 * @author jack_xjdai
 * @date 2020/4/16 11:11
 * @description: 现在只有两类：Naming和Config。
 * 为了区分，规定：
 * NamingJRaftOperationFactory的code为1000~1999
 * ConfigJRaftOperationFactory的code为2000~2999
 */
public interface JRaftOperationFactory {
    void process(JRaftOperation operation) throws NacosException;
}
