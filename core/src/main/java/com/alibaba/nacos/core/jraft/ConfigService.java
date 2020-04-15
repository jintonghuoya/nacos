package com.alibaba.nacos.core.jraft;

/**
 * @author jack_xjdai
 * @date 2020/4/1519:08
 * @description: ConfigService接口类
 */
public interface ConfigService extends JRaftService {
    void publish();
    void update();
}
