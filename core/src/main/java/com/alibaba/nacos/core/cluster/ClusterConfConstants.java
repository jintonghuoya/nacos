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

package com.alibaba.nacos.core.cluster;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ClusterConfConstants {

    // example : nacos.node.0.address=127.0.0.1:8848

    public static final String NODE_ADDRESS = "nacos.node.%d.address";

    // example : nacos.node.0.extend-info.raft_node=8000

    public static final String NODE_EXTEND_DATA = "nacos.node.%d.extend-info.(\\w+)";

}