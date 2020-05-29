package com.alibaba.nacos.common.status;

import java.io.Serializable;

/**
 * @author: daixinjie
 * @Date: 2020/05/29 21:52 下午
 * @Description: 状态信息接口
 */
public interface IStatus extends Serializable {

    // 状态码
    int getCode();

    // 状态信息
    String getMessage();
}
