package com.alibaba.nacos.naming.consistency;

import com.alibaba.nacos.naming.pojo.Record;

import java.util.Map;

/**
 * @author Jack_xj Dai
 * @description 存磁盘的记录的管理接口类
 */
public interface RecordManager<T extends Record> {
    Map<String, Datum<T>> getDatums();

    void setDatums(Map<String, Datum<T>> datums);
}
