package com.alibaba.nacos.console.jraft;

import com.alibaba.nacos.config.server.jraft.ConfigJRaftOperationFactory;
import com.alibaba.nacos.config.server.jraft.ConfigJRaftService;
import com.alibaba.nacos.config.server.jraft.ConfigJRaftServiceImpl;
import com.alibaba.nacos.config.server.jraft.rpc.PublishRequestProcessor;
import com.alibaba.nacos.config.server.jraft.rpc.RemoveRequestProcessor;
import com.alibaba.nacos.core.jraft.JRaftOperationFactory;
import com.alibaba.nacos.core.jraft.JRaftServer;
import com.alibaba.nacos.core.jraft.rpc.JRaftRequest;
import com.alibaba.nacos.naming.jraft.NamingJRaftOperationFactory;
import com.alibaba.nacos.naming.jraft.NamingJRaftService;
import com.alibaba.nacos.naming.jraft.NamingJRaftServiceImpl;
import com.alibaba.nacos.naming.jraft.rpc.DeregisterInstancesRequestProcessor;
import com.alibaba.nacos.naming.jraft.rpc.RegisterInstancesRequestProcessor;
import com.alipay.remoting.rpc.protocol.UserProcessor;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.NodeOptions;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

/**
 * @author jack_xjdai
 * @date 2020/4/16 10:10
 * @description: TODO 注意这个Bean的启动时机，是否需要和Spring的生命周期结合
 */
@Component
public class JRaftServerStarter {
    @PostConstruct
    public void init() throws IOException {
        // FIXME 此处只是fake出来的参数，后续这块数据应该是系统加载的时候从配置文件获取的
        String[] args = new String[4];
        if (args.length != 4) {
            System.out
                .println("Useage : java com.alipay.sofa.jraft.example.counter.CounterServer {dataPath} {groupId} {serverId} {initConf}");
            System.out
                .println("Example: java com.alipay.sofa.jraft.example.counter.CounterServer /tmp/server1 counter 127.0.0.1:8081 127.0.0.1:8081,127.0.0.1:8082,127.0.0.1:8083");
            System.exit(1);
        }
        final String dataPath = args[0];
        final String groupId = args[1];
        final String serverIdStr = args[2];
        final String initConfStr = args[3];

        final NodeOptions nodeOptions = new NodeOptions();
        // 为了测试,调整 snapshot 间隔等参数
        // 设置选举超时时间为 1 秒
        nodeOptions.setElectionTimeoutMs(1000);
        // 关闭 CLI 服务。
        nodeOptions.setDisableCli(false);
        // 每隔30秒做一次 snapshot
        nodeOptions.setSnapshotIntervalSecs(30);

        // 解析参数
        final PeerId serverId = new PeerId();
        if (!serverId.parse(serverIdStr)) {
            throw new IllegalArgumentException("Fail to parse serverId:" + serverIdStr);
        }
        final Configuration initConf = new Configuration();
        if (!initConf.parse(initConfStr)) {
            throw new IllegalArgumentException("Fail to parse initConf:" + initConfStr);
        }

        // 设置初始集群配置
        nodeOptions.setInitialConf(initConf);

        // 初始化JRaftOperationFactory
        Map<Integer, JRaftOperationFactory> jRaftOperationFactories = new HashMap<>();

        // 添加Naming的JRaftOperationFactory
        Arrays.stream(NamingJRaftOperationFactory.values()).forEach(namingJRaftOperationFactory ->
            jRaftOperationFactories.put(namingJRaftOperationFactory.getCode(), namingJRaftOperationFactory));
        // 添加Config的JRaftOperationFactory
        Arrays.stream(ConfigJRaftOperationFactory.values()).forEach(configJRaftOperationFactory ->
            jRaftOperationFactories.put(configJRaftOperationFactory.getCode(), configJRaftOperationFactory));

        // 构造JRaftServer
        final JRaftServer jRaftServer = new JRaftServer(dataPath, groupId, serverId, jRaftOperationFactories, nodeOptions);

        // 初始化userProcessors
        List<UserProcessor<? extends JRaftRequest>> userProcessors = new ArrayList<>();

        // 初始化Naming的UserProcessor
        NamingJRaftService namingJRaftService = new NamingJRaftServiceImpl(jRaftServer);
        userProcessors.add(new RegisterInstancesRequestProcessor(namingJRaftService));
        userProcessors.add(new DeregisterInstancesRequestProcessor(namingJRaftService));

        // 初始化Config的UserProcessor
        ConfigJRaftService configJRaftService = new ConfigJRaftServiceImpl(jRaftServer);
        userProcessors.add(new PublishRequestProcessor(configJRaftService));
        userProcessors.add(new RemoveRequestProcessor(configJRaftService));

        jRaftServer.addUserProcessor(userProcessors);

        System.out.println("Started counter server at port:"
            + jRaftServer.getNode().getNodeId().getPeerId().getPort());

        // 初始化并启动
        jRaftServer.initAndStart();
    }
}
