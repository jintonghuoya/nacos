/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.core.distributed.distro;

import com.alibaba.nacos.consistency.Config;
import com.alibaba.nacos.consistency.Log;
import com.alibaba.nacos.consistency.LogProcessor;
import com.alibaba.nacos.consistency.ap.APProtocol;
import com.alibaba.nacos.consistency.ap.Mapper;
import com.alibaba.nacos.consistency.request.GetRequest;
import com.alibaba.nacos.consistency.store.KVStore;
import com.alibaba.nacos.core.cluster.NodeManager;
import com.alibaba.nacos.core.distributed.AbstractConsistencyProtocol;
import com.alibaba.nacos.core.distributed.distro.core.DistroServer;
import com.alibaba.nacos.core.distributed.distro.utils.DistroExecutor;
import com.alibaba.nacos.core.utils.SpringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@SuppressWarnings("all")
public class DistroProtocol extends AbstractConsistencyProtocol<DistroConfig> implements APProtocol<DistroConfig> {

    private KVManager kvManager;

    private DistroServer distroServer;

    private NodeManager nodeManager;

    @Override
    public void init(DistroConfig config) {
        this.nodeManager = SpringUtils.getBean(NodeManager.class);
        this.kvManager = new KVManager();
        this.distroServer = new DistroServer(nodeManager, kvManager, config);

        loadLogDispatcher(config.listLogProcessor());

        // distro server start

        distroServer.start();
    }

    @Override
    public <R> R metaData(String key, String... subKey) {
        return (R) metaData.get(key, subKey);
    }

    @Override
    public <D> D getData(GetRequest request) throws Exception {
        final String key = request.getKey();
        for (Map.Entry<String, LogProcessor> entry : allProcessor().entrySet()) {
            final LogProcessor processor = entry.getValue();
            if (processor.interest(key)) {
                return processor.getData(request);
            }
        }
        return null;
    }

    @Override
    public boolean submit(Log data) throws Exception {
        final String key = data.getKey();
        for (Map.Entry<String, LogProcessor> entry : allProcessor().entrySet()) {
            final LogProcessor processor = entry.getValue();
            if (processor.interest(key)) {
                processor.onApply(data);
                return distroServer.submit(data);
            }
        }
        return false;
    }

    @Override
    public CompletableFuture<Boolean> submitAsync(Log data) {
        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        DistroExecutor.executeByGlobal(() -> {
            try {
                future.complete(submit(data));
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public boolean batchSubmit(Map<String, List<Log>> datums) {
        for (Map.Entry<String, List<Log>> entry : datums.entrySet()) {
            final LogProcessor processor = allProcessor().get(entry.getKey());
            DistroExecutor.executeByGlobal(() -> {
                for (Log log : entry.getValue()) {
                    try {
                        processor.onApply(log);
                    } catch (Exception e) {
                    }
                }
            });
        }
        return true;
    }

    @Override
    public Class<? extends Config> configType() {
        return DistroConfig.class;
    }

    @Override
    public void shutdown() {
        distroServer.shutdown();
    }

    @Override
    public Mapper mapper() {
        return distroServer.getDistroMapper();
    }

    @Override
    public <D> KVStore<D> createKVStore(String storeName) {
        DistroKVStore<D> kvStore = new DistroKVStore<>(storeName);
        this.kvManager.addKVStore(kvStore);

        // Because Distro uses DistroProtocol internally, so LogProcessor is implemented, need to add

        loadLogDispatcher(Collections.singletonList(kvStore.getKVLogProcessor()));
        return kvStore;
    }

    public DistroServer getDistroServer() {
        return distroServer;
    }
}