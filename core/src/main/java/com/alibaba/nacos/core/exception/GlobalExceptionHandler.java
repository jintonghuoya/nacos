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
package com.alibaba.nacos.core.exception;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.exception.BusinessException;
import com.alibaba.nacos.common.status.SystemStatus;
import com.alibaba.nacos.common.utils.ExceptionUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

/**
 * Global Exception Handler
 * This Exception Handler is for Config and Naming.
 * Both two module depend on core module, so we put this handler in core module.
 * TODO Add Global Metric Collectors.
 *
 * @author jintonghuoya
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * For BusinessException
     *
     * @throws BusinessException
     */
    @ExceptionHandler(NacosException.class)
    public ResponseEntity<String> handleBusinessException(BusinessException ex) throws IOException {
        return ResponseEntity.status(ex.getCode()).body(ExceptionUtil.getAllExceptionMsg(ex));
    }


    /**
     * For Uncaught Exception
     *
     * @throws Exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) throws IOException {
        return ResponseEntity.status(SystemStatus.FAILURE.getCode()).body(ExceptionUtil.getAllExceptionMsg(ex));
    }

}
