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

import com.alibaba.nacos.config.server.model.Page;
import com.alibaba.nacos.config.server.service.transaction.DatabaseOperate;
import com.alibaba.nacos.config.server.service.transaction.SqlContextUtils;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;

import static com.alibaba.nacos.core.utils.SystemUtils.STANDALONE_MODE;

/**
 * 分页辅助类
 *
 * @param <E>
 * @author boyan
 * @date 2010-5-6
 */

public class PaginationHelper<E> {

    /**
     * 取分页
     *
     * @param services     jdbcTemplate
     * @param sqlCountRows 查询总数的SQL
     * @param sqlFetchRows 查询数据的sql
     * @param args         查询参数
     * @param pageNo       页数
     * @param pageSize     每页大小
     * @param rowMapper
     * @return
     */
    public Page<E> fetchPage(final DatabaseOperate services, final String sqlCountRows, final String sqlFetchRows,
                             final Object[] args, final int pageNo, final int pageSize, final RowMapper<E> rowMapper) {
        return fetchPage(services, sqlCountRows, sqlFetchRows, args, pageNo, pageSize, null, rowMapper);
    }

    public Page<E> fetchPage(final DatabaseOperate services, final String sqlCountRows, final String sqlFetchRows,
                             final Object[] args, final int pageNo, final int pageSize, final Long lastMaxId,
                             final RowMapper<E> rowMapper) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("pageNo and pageSize must be greater than zero");
        }

        // 查询当前记录总数
        Integer rowCountInt = services.queryOne(sqlCountRows, args, Integer.class);
        if (rowCountInt == null) {
            throw new IllegalArgumentException("fetchPageLimit error");
        }

        // 计算页数
        int pageCount = rowCountInt / pageSize;
        if (rowCountInt > pageSize * pageCount) {
            pageCount++;
        }

        // 创建Page对象
        final Page<E> page = new Page<E>();
        page.setPageNumber(pageNo);
        page.setPagesAvailable(pageCount);
        page.setTotalCount(rowCountInt);

        if (pageNo > pageCount) {
            return page;
        }

        final int startRow = (pageNo - 1) * pageSize;
        String selectSQL = "";
        if (isDerby()) {
            selectSQL = sqlFetchRows + " OFFSET " + startRow + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
        } else if (lastMaxId != null) {
            selectSQL = sqlFetchRows + " and id > " + lastMaxId + " order by id asc" + " limit " + 0 + "," + pageSize;
        } else {
            selectSQL = sqlFetchRows + " limit " + startRow + "," + pageSize;
        }

        List<E> result = services.queryMany(selectSQL, args, rowMapper);
        for (E item : result) {
            page.getPageItems().add(item);
        }
        return page;
    }

    public Page<E> fetchPageLimit(final DatabaseOperate services, final String sqlCountRows, final String sqlFetchRows,
                                  final Object[] args, final int pageNo, final int pageSize,
                                  final RowMapper<E> rowMapper) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("pageNo and pageSize must be greater than zero");
        }
        // 查询当前记录总数
        Integer rowCountInt = services.queryOne(sqlCountRows, Integer.class);
        if (rowCountInt == null) {
            throw new IllegalArgumentException("fetchPageLimit error");
        }

        // 计算页数
        int pageCount = rowCountInt / pageSize;
        if (rowCountInt > pageSize * pageCount) {
            pageCount++;
        }

        // 创建Page对象
        final Page<E> page = new Page<E>();
        page.setPageNumber(pageNo);
        page.setPagesAvailable(pageCount);
        page.setTotalCount(rowCountInt);

        if (pageNo > pageCount) {
            return page;
        }

        String selectSQL = sqlFetchRows;
        if (isDerby()) {
            selectSQL = selectSQL.replaceAll("(?i)LIMIT \\?,\\?", "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        }

        List<E> result = services.queryMany(selectSQL, args, rowMapper);
        for (E item : result) {
            page.getPageItems().add(item);
        }
        return page;
    }

    public Page<E> fetchPageLimit(final DatabaseOperate services, final String sqlCountRows, final Object[] args1,
                                  final String sqlFetchRows,
                                  final Object[] args2, final int pageNo, final int pageSize,
                                  final RowMapper<E> rowMapper) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("pageNo and pageSize must be greater than zero");
        }
        // 查询当前记录总数
        Integer rowCountInt = services.queryOne(sqlCountRows, args1, Integer.class);
        if (rowCountInt == null) {
            throw new IllegalArgumentException("fetchPageLimit error");
        }

        // 计算页数
        int pageCount = rowCountInt / pageSize;
        if (rowCountInt > pageSize * pageCount) {
            pageCount++;
        }

        // 创建Page对象
        final Page<E> page = new Page<E>();
        page.setPageNumber(pageNo);
        page.setPagesAvailable(pageCount);
        page.setTotalCount(rowCountInt);

        if (pageNo > pageCount) {
            return page;
        }

        String selectSQL = sqlFetchRows;
        if (isDerby()) {
            selectSQL = selectSQL.replaceAll("(?i)LIMIT \\?,\\?", "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        }

        List<E> result = services.queryMany(selectSQL, args2, rowMapper);
        for (E item : result) {
            page.getPageItems().add(item);
        }
        return page;
    }

    public Page<E> fetchPageLimit(final DatabaseOperate services, final String sqlFetchRows,
                                  final Object[] args, final int pageNo, final int pageSize,
                                  final RowMapper<E> rowMapper) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("pageNo and pageSize must be greater than zero");
        }
        // 创建Page对象
        final Page<E> page = new Page<E>();

        String selectSQL = sqlFetchRows;
        if (isDerby()) {
            selectSQL = selectSQL.replaceAll("(?i)LIMIT \\?,\\?", "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        }

        List<E> result = services.queryMany(selectSQL, args, rowMapper);
        for (E item : result) {
            page.getPageItems().add(item);
        }
        return page;
    }

    public void updateLimit(final DatabaseOperate services, final String sql, final Object[] args) {
        String sqlUpdate = sql;

        if (isDerby()) {
            sqlUpdate = sqlUpdate.replaceAll("limit \\?", "OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY");
        }

        SqlContextUtils.addSqlContext(sqlUpdate, args);
        try {
            services.update(SqlContextUtils.getCurrentSqlContext());
        } finally {
            SqlContextUtils.cleanCurrentSqlContext();
        }
    }

    private boolean isDerby() {
        return (STANDALONE_MODE && !PropertyUtil.isUseMysql()) ||
                PropertyUtil.isEmbeddedDistributedStorage();
    }
}
