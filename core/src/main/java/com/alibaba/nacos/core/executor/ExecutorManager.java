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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@SuppressWarnings("PMD.ThreadPoolCreationRule")
public class ExecutorManager {

    private static final String DEFAULT_EXECUTOR_NAME = "com.alibaba.nacos.executor-pool.";

    private static final String DEFAULT_SCHEDULE_EXECUTOR_NAME = "com.alibaba.nacos.schedule-executor-pool.";

    private static final AtomicInteger POOL_ID = new AtomicInteger(0);

    private static final AtomicInteger SCHEDULE_POOL_ID = new AtomicInteger(0);

    private static final ThreadPoolManager THREAD_POOL_MANAGER = ThreadPoolManager.getInstance();

    static {
        ShutdownUtils.addShutdownHook(() -> {
            THREAD_POOL_MANAGER.destroy("nacos");
        });
    }

    public static ForkJoinPool newFixForkJoinPool(final String owner) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        THREAD_POOL_MANAGER.register("nacos", owner, forkJoinPool);
        return forkJoinPool;
    }

    public static ForkJoinPool newFixForkJoinPool(final String owner,
                                                  final int nThreads) {
        ForkJoinPool forkJoinPool = new ForkJoinPool(nThreads);
        THREAD_POOL_MANAGER.register("nacos", owner, forkJoinPool);
        return forkJoinPool;
    }

    public static ExecutorService newFixExecutorService(final String owner,
                                                        final int nThreads) {
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads,
            new NameThreadFactory(DEFAULT_EXECUTOR_NAME + POOL_ID.getAndIncrement()));
        THREAD_POOL_MANAGER.register("nacos", owner, executorService);
        return executorService;
    }

    public static ExecutorService newFixExecutorService(final String owner,
                                                        final int nThreads,
                                                        final ThreadFactory threadFactory) {
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads, threadFactory);
        THREAD_POOL_MANAGER.register("nacos", owner, executorService);
        return executorService;
    }

    public static ScheduledExecutorService newSingleScheduledExecutorService(final String owner) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(
            new NameThreadFactory(DEFAULT_SCHEDULE_EXECUTOR_NAME + SCHEDULE_POOL_ID.getAndIncrement())
        );
        THREAD_POOL_MANAGER.register("nacos", owner, executorService);
        return executorService;
    }

    public static ScheduledExecutorService newSingleScheduledExecutorService(final String owner,
                                                                             final ThreadFactory threadFactory) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(threadFactory);
        THREAD_POOL_MANAGER.register("nacos", owner, executorService);
        return executorService;
    }

    public static ScheduledExecutorService newScheduledExecutorService(final String owner,
                                                                       final int nThreads) {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(nThreads,
            new NameThreadFactory(DEFAULT_SCHEDULE_EXECUTOR_NAME + SCHEDULE_POOL_ID.getAndIncrement())
        );
        THREAD_POOL_MANAGER.register("nacos", owner, executorService);
        return executorService;
    }

    public static ScheduledExecutorService newScheduledExecutorService(final String owner,
                                                                       final int nThreads,
                                                                       final ThreadFactory threadFactory) {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(nThreads, threadFactory);
        THREAD_POOL_MANAGER.register("nacos", owner, executorService);
        return executorService;
    }


}