/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.nacos.core.utils;

import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * A watch service that <em>watches</em> registered objects for changes and
 * events. For example a file manager may use a watch service to monitor a
 * directory for changes so that it can update its display of the list of files
 * when files are created or deleted.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class WatchFileUtils {

    private static final Map<String, WatchWorker> alreadyRegisterPaths = new HashMap<>(32);

    private static final ThreadPoolExecutor WATCH_EXECUTOR = new ThreadPoolExecutor(0, 32,
            60L, java.util.concurrent.TimeUnit.SECONDS,
            new java.util.concurrent.SynchronousQueue<>(),
            new java.util.concurrent.ThreadFactory() {

                private AtomicInteger idGenerate = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName("com.alibaba.nacos.core.watch-file." + idGenerate.getAndIncrement());
                    thread.setDaemon(true);
                    return thread;
                }
            }, (r, executor) -> Loggers.WATCH_FILE.warn("A maximum of 32 directories are supported"));

    public synchronized static <T> void registerWatch(String directory, Consumer<WatchEvent> consumer) {
        try {
            WatchWorker watchWorker;
            if (alreadyRegisterPaths.containsKey(directory)) {
                watchWorker = alreadyRegisterPaths.get(directory);
                watchWorker.consumers.add(consumer);
            } else {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                Paths.get(directory).register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
                watchWorker = new WatchWorker(watchService, directory);
                WATCH_EXECUTOR.submit(watchWorker);
                alreadyRegisterPaths.put(directory, watchWorker);
            }
        } catch (Exception e) {
            Loggers.WATCH_FILE.error("registerWatch has error : {}", ExceptionUtil.getAllExceptionMsg(e));
        }
    }

    private static class WatchWorker implements Callable<Void> {

        private Set<Consumer<WatchEvent>> consumers = new CopyOnWriteArraySet<>();

        private volatile boolean isDeregister = false;

        private WatchService watchService;

        private String directory;

        public WatchWorker(WatchService watchService, String directory) {
            this.watchService = watchService;
            this.directory = directory;
        }

        @Override
        public Void call () throws Exception {
            while (!isDeregister) {
                WatchKey watchKey = watchService.take();
                List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
                for (WatchEvent<?> event : watchEvents) {
                    for (Consumer<WatchEvent> eventConsumer : consumers) {
                        eventConsumer.accept(event);
                    }
                }
                watchKey.reset();
            }
            return null;
        }

        public void deregister() {
            isDeregister = true;
        }
    }

}
