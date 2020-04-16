package com.alibaba.nacos.core.jraft;

import com.alibaba.nacos.core.jraft.rpc.JRaftGenericResponse;
import com.alibaba.nacos.core.jraft.rpc.JRaftRequest;
import com.alipay.remoting.rpc.RpcServer;
import com.alipay.remoting.rpc.protocol.UserProcessor;
import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.RaftGroupService;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.NodeOptions;
import com.alipay.sofa.jraft.rpc.RaftRpcServerFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author jack_xjdai
 * @date 2020/4/1518:02
 * @description: TODO
 */
public class JRaftServer {
    private String rootDataDir;
    private String groupId;
    private PeerId serverId;
    private Map<Integer, JRaftOperationFactory> jRaftOperationFactoryMap;
    private List<UserProcessor<? extends JRaftRequest>> jRaftRequestUserProcessors;
    private NodeOptions nodeOptions;
    private RaftGroupService raftGroupService;
    private Node node;
    private JRaftStateMachine fsm;

    public JRaftServer(final String rootDataDir,
                       final String groupId,
                       final PeerId serverId,
                       final Map<Integer, JRaftOperationFactory> jRaftOperationFactoryMap,
                       final NodeOptions nodeOptions) {
        this.rootDataDir = rootDataDir;
        this.groupId = groupId;
        this.serverId = serverId;
        this.jRaftOperationFactoryMap = jRaftOperationFactoryMap;
        this.nodeOptions = nodeOptions;
    }

    public void addUserProcessor(final List<UserProcessor<? extends JRaftRequest>> jRaftRequestUserProcessors) {
        this.jRaftRequestUserProcessors = jRaftRequestUserProcessors;
    }

    public void initAndStart() throws IOException {
        // 初始化路径
        FileUtils.forceMkdir(new File(rootDataDir));

        // 这里让 raft RPC 和业务 RPC 使用同一个 RPC server, 通常也可以分开
        final RpcServer rpcServer = new RpcServer(serverId.getPort());
        RaftRpcServerFactory.addRaftRequestProcessors(rpcServer);
        // 注册业务处理器
        jRaftRequestUserProcessors.forEach(rpcServer::registerUserProcessor);
        // 初始化状态机
        this.fsm = new JRaftStateMachine(jRaftOperationFactoryMap);
        // 设置状态机到启动参数
        nodeOptions.setFsm(this.fsm);
        // 设置存储路径
        // 日志, 必须
        nodeOptions.setLogUri(rootDataDir + File.separator + JRaftConstants.LOG_URI);
        // 元信息, 必须
        nodeOptions.setRaftMetaUri(rootDataDir + File.separator + JRaftConstants.RAFT_META_URI);
        // snapshot, 可选, 一般都推荐
        nodeOptions.setSnapshotUri(rootDataDir + File.separator + JRaftConstants.SNAPSHOT_URI);
        // 初始化 raft group 服务框架
        this.raftGroupService = new RaftGroupService(groupId, serverId, nodeOptions, rpcServer);
        // 启动
        this.node = this.raftGroupService.start();
    }

    public JRaftStateMachine getFsm() {
        return this.fsm;
    }

    public Node getNode() {
        return this.node;
    }

    public RaftGroupService RaftGroupService() {
        return this.raftGroupService;
    }

    /**
     * Redirect request to new leader
     */
    public JRaftGenericResponse redirect() {
        final JRaftGenericResponse response = new JRaftGenericResponse();
        response.setSuccess(false);
        if (this.node != null) {
            final PeerId leader = this.node.getLeaderId();
            if (leader != null) {
                response.setRedirect(leader.toString());
            }
        }
        return response;
    }
}
