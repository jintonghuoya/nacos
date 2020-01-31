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

package com.alibaba.nacos.core.lock;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.model.ResResult;
import com.alibaba.nacos.core.distributed.ConsistencyProtocol;
import com.alibaba.nacos.core.distributed.NDatum;
import com.alibaba.nacos.core.distributed.raft.RaftConfig;
import com.alibaba.nacos.core.executor.ExecutorManager;
import com.alibaba.nacos.core.utils.SpringUtils;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
class DefaultDistributeLock implements DistributedLock {

    private final String key;

    private final long version;

    private ScheduledExecutorService executorService;

    private ConsistencyProtocol<RaftConfig> protocol;

    DefaultDistributeLock(String key) {
        this.key = key;
        this.version = System.currentTimeMillis();
        this.protocol = SpringUtils.getBean("RaftProtocol", ConsistencyProtocol.class);
    }

    @Override
    public boolean tryLock(Duration timeout) {
        boolean result = false;
        LockEntry entry = new LockEntry();
        entry.setKey(key);
        entry.setExpireTime(System.currentTimeMillis() + LIFE_TIME);
        entry.setVersion(version);
        CompletableFuture<ResResult<Boolean>> future = protocol.submitAsync(NDatum.builder()
                .className(LockEntry.class.getCanonicalName())
                .data(JSON.toJSONBytes(entry))
                .key(key)
                .operation(LockOperation.LOCK.getOperation())
                .build());
        try {
            result = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS).getData();
        } catch (Exception ignore) {
        }
        if (result) {
            executorService = ExecutorManager.newSingleScheduledExecutorService(DistributedLock.class.getName() + "-" + key);
            executorService.schedule(this::openAutoReNew, LIFE_TIME - 1000, TimeUnit.MILLISECONDS);
        }
        return result;
    }

    @Override
    public void unLock() {
        LockEntry entry = new LockEntry();
        entry.setKey(key);
        entry.setExpireTime(System.currentTimeMillis() + LIFE_TIME);
        entry.setVersion(version);
        protocol.submitAsync(NDatum.builder()
                .className(LockEntry.class.getCanonicalName())
                .data(JSON.toJSONBytes(entry))
                .key(key)
                .operation(LockOperation.UN_LOCK.getOperation())
                .build());
    }

    // The auto-renewal feature will only be enabled if the lock is acquired successfully

    private void openAutoReNew() {
        LockEntry entry = new LockEntry();
        entry.setKey(key);
        entry.setVersion(version);
        protocol.submitAsync(NDatum.builder()
                .className(LockEntry.class.getCanonicalName())
                .data(JSON.toJSONBytes(entry))
                .key(key)
                .operation(LockOperation.RE_NEW.getOperation())
                .build())
                .thenAccept(booleanResResult -> {
                    if (booleanResResult.getData()) {
                        executorService.schedule(this::openAutoReNew, LIFE_TIME - 1000, TimeUnit.MILLISECONDS);
                    }
                });
    }
}