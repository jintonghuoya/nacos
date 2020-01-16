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
package com.alibaba.nacos.core.env;


import com.alibaba.nacos.core.utils.WatchFileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Reload application.properties
 *
 * @author nkorange
 * @since 1.2.0
 */
@Component
public class ReloadableConfigs {

    private static final Logger logger = LoggerFactory.getLogger(ReloadableConfigs.class);

    private volatile Properties properties;

    @Value("${spring.config.location:}")
    private String path;

    private static final String FILE_PREFIX = "file:";

    @PostConstruct
    public void init() throws IOException {
        WatchFileUtils.registerWatch(readFile(), watchEvent -> {
            try {
                readFile();
            } catch (Exception e) {
                logger.error("read file has error : {}", e);
            }
        });
    }

    private String readFile() throws IOException {
        String directory = null;
        Properties properties = new Properties();
        InputStream inputStream = null;
        if (StringUtils.isNotBlank(path) && path.contains(FILE_PREFIX)) {
            String[] paths = path.split(",");
            path = paths[paths.length - 1].substring(FILE_PREFIX.length());
        }
        try {
            inputStream = new FileInputStream(new File(path + "application.properties"));
            directory = path;
        } catch (Exception ignore) {
        }
        if (inputStream == null) {
            URL url = getClass().getResource("/application.properties");
            directory = url.getPath().replace("/application.properties", "");
            inputStream = url.openStream();
        }
        properties.load(inputStream);
        inputStream.close();
        this.properties = properties;
        return directory;
    }

    public final Properties getProperties() {
        return properties;
    }
}
