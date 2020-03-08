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

package com.alibaba.nacos.core.executor;

import com.alibaba.nacos.common.ThreadPoolManager;
import com.alibaba.nacos.common.utils.ShutdownUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * Unified thread pool creation factory, and actively create thread
 * pool resources by ThreadPoolManager for unified life cycle management
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@SuppressWarnings("PMD.ThreadPoolCreationRule")
public class ExecutorFactory {

    private static final ThreadPoolManager THREAD_POOL_MANAGER = ThreadPoolManager.getInstance();

    private static final String DEFAULT_BIZ = "nacos";

    static {
        ShutdownUtils.addShutdownHook(() -> {
            THREAD_POOL_MANAGER.destroy(DEFAULT_BIZ);
        });
    }

    public static ForkJoinPool newForkJoinPool(final String owner) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        THREAD_POOL_MANAGER.register(DEFAULT_BIZ, owner, forkJoinPool);
        return forkJoinPool;
    }

    public static ForkJoinPool newForkJoinPool(final String owner,
                                               final int nThreads) {
        ForkJoinPool forkJoinPool = new ForkJoinPool(nThreads);
        THREAD_POOL_MANAGER.register(DEFAULT_BIZ, owner, forkJoinPool);
        return forkJoinPool;
    }

    public static ExecutorService newSingleExecutorService(final String owner) {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        THREAD_POOL_MANAGER.register(DEFAULT_BIZ, owner, executorService);
        return executorService;
    }

    public static ExecutorService newSingleExecutorService(final String owner,
                                                           final ThreadFactory threadFactory) {
        ExecutorService executorService = Executors.newFixedThreadPool(1, threadFactory);
        THREAD_POOL_MANAGER.register(DEFAULT_BIZ, owner, executorService);
        return executorService;
    }

    public static ExecutorService newFixExecutorService(final String owner,
                                                        final int nThreads) {
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        THREAD_POOL_MANAGER.register(DEFAULT_BIZ, owner, executorService);
        return executorService;
    }

    public static ExecutorService newFixExecutorService(final String owner,
                                                        final int nThreads,
                                                        final ThreadFactory threadFactory) {
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads, threadFactory);
        THREAD_POOL_MANAGER.register(DEFAULT_BIZ, owner, executorService);
        return executorService;
    }

    public static ScheduledExecutorService newSingleScheduledExecutorService(final String owner,
                                                                             final ThreadFactory threadFactory) {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1, threadFactory);
        THREAD_POOL_MANAGER.register(DEFAULT_BIZ, owner, executorService);
        return executorService;
    }

    public static ScheduledExecutorService newScheduledExecutorService(final String owner,
                                                                       final int nThreads,
                                                                       final ThreadFactory threadFactory) {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(nThreads, threadFactory);
        THREAD_POOL_MANAGER.register(DEFAULT_BIZ, owner, executorService);
        return executorService;
    }

}
