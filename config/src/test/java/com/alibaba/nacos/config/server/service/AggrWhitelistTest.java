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
package com.alibaba.nacos.config.server.service;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class AggrWhitelistTest {

    AggrWhitelist service;

    @Before
    public void before() throws Exception {
        service = new AggrWhitelist();
    }

    @Test
    public void testIsAggrDataId() {
        List<String> list = new ArrayList<String>();
        list.add("com.taobao.jiuren.*");
        list.add("NS_NACOS_SUBSCRIPTION_TOPIC_*");
        list.add("com.taobao.tae.AppListOnGrid-*");
        service.compile(list);

        assertFalse(service.isAggrDataId("com.abc"));
        assertFalse(service.isAggrDataId("com.taobao.jiuren"));
        assertFalse(service.isAggrDataId("com.taobao.jiurenABC"));
        assertTrue(service.isAggrDataId("com.taobao.jiuren.abc"));
        assertTrue(service.isAggrDataId("NS_NACOS_SUBSCRIPTION_TOPIC_abc"));
        assertTrue(service.isAggrDataId("com.taobao.tae.AppListOnGrid-abc"));
    }
}
