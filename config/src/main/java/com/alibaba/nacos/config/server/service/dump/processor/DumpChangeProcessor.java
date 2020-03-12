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
import com.alibaba.nacos.config.server.model.ConfigInfo;
import com.alibaba.nacos.config.server.model.ConfigInfoWrapper;
import com.alibaba.nacos.config.server.service.ConfigService;
import com.alibaba.nacos.config.server.service.PersistService;
import com.alibaba.nacos.config.server.service.dump.DumpService;
import com.alibaba.nacos.config.server.utils.GroupKey2;
import com.alibaba.nacos.config.server.utils.LogUtil;
import com.alibaba.nacos.config.server.utils.MD5;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class DumpChangeProcessor implements TaskProcessor {

    final DumpService dumpService;
    final PersistService persistService;

    // =====================
    final Timestamp startTime;
    final Timestamp endTime;
    public DumpChangeProcessor(DumpService dumpService, Timestamp startTime,
                               Timestamp endTime) {
        this.dumpService = dumpService;
        this.persistService = dumpService.getPersistService();
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public boolean process(String taskType, AbstractTask task) {
        LogUtil.defaultLog.warn("quick start; startTime:{},endTime:{}",
                startTime, endTime);
        LogUtil.defaultLog.warn("updateMd5 start");
        long startUpdateMd5 = System.currentTimeMillis();
        List<ConfigInfoWrapper> updateMd5List = persistService
                .listAllGroupKeyMd5();
        LogUtil.defaultLog.warn("updateMd5 count:{}", updateMd5List.size());
        for (ConfigInfoWrapper config : updateMd5List) {
            final String groupKey = GroupKey2.getKey(config.getDataId(),
                    config.getGroup());
            ConfigService.updateMd5(groupKey, config.getMd5(),
                    config.getLastModified());
        }
        long endUpdateMd5 = System.currentTimeMillis();
        LogUtil.defaultLog.warn("updateMd5 done,cost:{}", endUpdateMd5
                - startUpdateMd5);

        LogUtil.defaultLog.warn("deletedConfig start");
        long startDeletedConfigTime = System.currentTimeMillis();
        List<ConfigInfo> configDeleted = persistService.findDeletedConfig(
                startTime, endTime);
        LogUtil.defaultLog.warn("deletedConfig count:{}", configDeleted.size());
        for (ConfigInfo configInfo : configDeleted) {
            if (persistService.findConfigInfo(configInfo.getDataId(), configInfo.getGroup(),
                    configInfo.getTenant()) == null) {
                ConfigService.remove(configInfo.getDataId(), configInfo.getGroup(), configInfo.getTenant());
            }
        }
        long endDeletedConfigTime = System.currentTimeMillis();
        LogUtil.defaultLog.warn("deletedConfig done,cost:{}",
                endDeletedConfigTime - startDeletedConfigTime);

        LogUtil.defaultLog.warn("changeConfig start");
        long startChangeConfigTime = System.currentTimeMillis();
        List<ConfigInfoWrapper> changeConfigs = persistService
                .findChangeConfig(startTime, endTime);
        LogUtil.defaultLog.warn("changeConfig count:{}", changeConfigs.size());
        for (ConfigInfoWrapper cf : changeConfigs) {
            boolean result = ConfigService.dumpChange(cf.getDataId(), cf.getGroup(), cf.getTenant(),
                    cf.getContent(), cf.getLastModified());
            final String content = cf.getContent();
            final String md5 = MD5.getInstance().getMD5String(content);
            LogUtil.defaultLog.info(
                    "[dump-change-ok] {}, {}, length={}, md5={}",
                    new Object[]{
                            GroupKey2.getKey(cf.getDataId(), cf.getGroup()),
                            cf.getLastModified(), content.length(), md5});
        }
        ConfigService.reloadConfig();
        long endChangeConfigTime = System.currentTimeMillis();
        LogUtil.defaultLog.warn("changeConfig done,cost:{}",
                endChangeConfigTime - startChangeConfigTime);
        return true;
    }
}