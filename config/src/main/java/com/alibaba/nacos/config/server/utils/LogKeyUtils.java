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

package com.alibaba.nacos.config.server.utils;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class LogKeyUtils {

    private static final String LINK_STR = "$@$";

    public static final String build(String str, Object... params) {
        final StringBuilder sb = new StringBuilder();
        sb.append(str);
        for (Object obj : params) {
            sb.append(LINK_STR).append(obj);
        }
        return sb.toString();
    }

}