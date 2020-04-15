package com.alibaba.nacos.core.jraft;

import com.alibaba.nacos.core.jraft.rpc.ConfigRequest;

/**
 * @author jack_xjdai
 * @date 2020/4/15 19:08
 * @description: ConfigService接口类
 */
public interface ConfigService {
    void publish(ConfigRequest request, NacosClosure closure);

    void remove(ConfigRequest request, NacosClosure closure);
}
