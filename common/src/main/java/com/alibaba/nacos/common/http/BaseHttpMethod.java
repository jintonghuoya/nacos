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

package com.alibaba.nacos.common.http;

import com.alibaba.nacos.common.exception.BusinessException;
import com.alibaba.nacos.common.http.handler.RequestHandler;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.status.SystemStatus;
import com.alibaba.nacos.common.utils.HttpMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public enum BaseHttpMethod {

    /**
     * get request
     */
    GET(HttpMethod.GET) {
        @Override
        protected HttpRequestBase createRequest(String url) {
            return new HttpGet(url);
        }
    },

    GET_LARGE(HttpMethod.GET_LARGE) {
        @Override
        protected HttpRequestBase createRequest(String url) {
            return new BaseHttpClient.HttpGetWithEntity(url);
        }
    },

    /**
     * post request
     */
    POST(HttpMethod.POST) {
        @Override
        protected HttpRequestBase createRequest(String url) {
            return new HttpPost(url);
        }
    },

    /**
     * put request
     */
    PUT(HttpMethod.PUT) {
        @Override
        protected HttpRequestBase createRequest(String url) {
            return new HttpPut(url);
        }
    },

    /**
     * delete request
     */
    DELETE(HttpMethod.DELETE) {
        @Override
        protected HttpRequestBase createRequest(String url) {
            return new HttpDelete(url);
        }
    },

    /**
     * head request
     */
    HEAD(HttpMethod.HEAD) {
        @Override
        protected HttpRequestBase createRequest(String url) {
            return new HttpHead(url);
        }
    },

    /**
     * trace request
     */
    TRACE(HttpMethod.TRACE) {
        @Override
        protected HttpRequestBase createRequest(String url) {
            return new HttpTrace(url);
        }
    },

    /**
     * patch request
     */
    PATCH(HttpMethod.PATCH) {
        @Override
        protected HttpRequestBase createRequest(String url) {
            return new HttpPatch(url);
        }
    },

    /**
     * options request
     */
    OPTIONS(HttpMethod.OPTIONS) {
        @Override
        protected HttpRequestBase createRequest(String url) {
            return new HttpTrace(url);
        }
    },

    ;

    private String name;

    private HttpRequest requestBase;

    BaseHttpMethod(String name) {
        this.name = name;
    }

    public void init(String url) {
        requestBase = createRequest(url);
    }

    protected HttpRequestBase createRequest(String url) {
        throw new BusinessException(SystemStatus.UNSUPPORTED_OPERATION_EXCEPTION);
    }

    public void initHeader(Header header) {
        Iterator<Map.Entry<String, String>> iterator = header.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            requestBase.setHeader(entry.getKey(), entry.getValue());
        }
    }

    public void initEntity(Object body, String mediaType) throws Exception {
        if (body == null) {
            return;
        }

        if (requestBase instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest request = (HttpEntityEnclosingRequest) requestBase;
            ContentType contentType = ContentType.create(mediaType);
            StringEntity entity = new StringEntity(RequestHandler.parse(body), contentType);
            request.setEntity(entity);
        }
    }

    public HttpRequestBase getRequestBase() {
        return (HttpRequestBase) requestBase;
    }

    public static BaseHttpMethod sourceOf(String name) {
        for (BaseHttpMethod method : BaseHttpMethod.values()) {
            if (StringUtils.equalsIgnoreCase(name, method.name)) {
                return method;
            }
        }
        throw new BusinessException(SystemStatus.UNSUPPORTED_OPERATION_EXCEPTION, "Unsupported http method : " + name);
    }

}
