package com.alibaba.nacos.naming.jraft.rpc;

import com.alibaba.nacos.naming.core.Instances;

public class DeregisterInstancesJRaftRequest extends NamingJRaftRequest {
    private Instances instances;

    public Instances getInstances() {
        return instances;
    }

    public void setInstances(Instances instances) {
        this.instances = instances;
    }
}
