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

package com.alibaba.nacos.config.server.service.dump.processor;

import com.alibaba.nacos.config.server.manager.AbstractTask;
import com.alibaba.nacos.config.server.manager.TaskProcessor;
import com.alibaba.nacos.config.server.model.ConfigInfoTagWrapper;
import com.alibaba.nacos.config.server.model.Page;
import com.alibaba.nacos.config.server.service.ConfigService;
import com.alibaba.nacos.config.server.service.PersistService;
import com.alibaba.nacos.config.server.service.dump.DumpService;
import com.alibaba.nacos.config.server.utils.GroupKey2;
import com.alibaba.nacos.config.server.utils.LogUtil;

import static com.alibaba.nacos.config.server.utils.LogUtil.defaultLog;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class DumpAllTagProcessor implements TaskProcessor {

    static final int PAGE_SIZE = 1000;
    final DumpService dumpService;
    final PersistService persistService;

    public DumpAllTagProcessor(DumpService dumpService) {
        this.dumpService = dumpService;
        this.persistService = dumpService.getPersistService();
    }

    @Override
    public boolean process(String taskType, AbstractTask task) {
        int rowCount = persistService.configInfoTagCount();
        int pageCount = (int) Math.ceil(rowCount * 1.0 / PAGE_SIZE);

        int actualRowCount = 0;
        for (int pageNo = 1; pageNo <= pageCount; pageNo++) {
            Page<ConfigInfoTagWrapper> page = persistService.findAllConfigInfoTagForDumpAll(pageNo, PAGE_SIZE);
            if (page != null) {
                for (ConfigInfoTagWrapper cf : page.getPageItems()) {
                    boolean result = ConfigService.dumpTag(cf.getDataId(), cf.getGroup(), cf.getTenant(), cf.getTag(),
                            cf.getContent(), cf.getLastModified());
                    LogUtil.dumpLog.info("[dump-all-Tag-ok] result={}, {}, {}, length={}, md5={}", result,
                            GroupKey2.getKey(cf.getDataId(), cf.getGroup()), cf.getLastModified(), cf.getContent()
                                    .length(), cf.getMd5());
                }

                actualRowCount += page.getPageItems().size();
                defaultLog.info("[all-dump-tag] {} / {}", actualRowCount, rowCount);
            }
        }
        return true;
    }
}
