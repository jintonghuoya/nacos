package com.alibaba.nacos.core.jraft;

import com.alibaba.nacos.core.jraft.rpc.NamingRequest;

/**
 * @author jack_xjdai
 * @date 2020/4/1519:08
 * @description: NamingService接口类
 */
public interface NamingService {
    void register(NamingRequest request, NacosClosure closure);

    void deregister(NamingRequest request, NacosClosure closure);
}
