package com.alibaba.nacos.naming.consistency;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.naming.pojo.Record;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author jack_xjdai
 * @date 2020/4/15 09:50
 * @description: 负责管理服务注册中心核心数据结构组件
 */
public abstract class AbstractRecordManager<T extends Record> implements RecordManager<T> {
    // 读写锁
    protected ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    // 核心数据结构
    Map<String, Datum<T>> datums = new ConcurrentHashMap<>();

    public Map<String, Datum<T>> getDatums() {
        readLock();
        try {
            return datums;
        } finally {
            readUnlock();
        }
    }

    public void setDatums(Map<String, Datum<T>> datums) {
        writeLock();
        try {
            this.datums = datums;
        } finally {
            writeUnlock();
        }
    }

    @Override
    public void addDatum(String key, Datum<T> value) {
        writeLock();
        try {
            datums.put(key, value);
        } finally {
            writeUnlock();
        }
    }

    @Override
    public void removeDatum(String key) {
        writeLock();
        try {
            datums.remove(key);
        } finally {
            writeUnlock();
        }
    }

    @Override
    public byte[] getDatumsBytes() {
        readLock();
        try {
            return JSON.toJSONBytes(datums);
        } finally {
            readUnlock();
        }
    }

    void readLock() {
        readWriteLock.readLock().lock();
    }

    void readUnlock() {
        readWriteLock.readLock().unlock();
    }

    void writeLock() {
        readWriteLock.writeLock().lock();
    }

    void writeUnlock() {
        readWriteLock.writeLock().unlock();
    }
}
