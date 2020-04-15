package com.alibaba.nacos.core.jraft;

/**
 * @author jack_xjdai
 * @date 2020/4/1519:08
 * @description: NamingService接口类
 */
public interface NamingService extends JRaftService {
    void register();
    void deregister();

    void get(final boolean readOnlySafe, final NacosClosure closure);
}
