package dev.rosemarylab.dataquerying.internal.sql;

import dev.rosemarylab.dataquerying.api.SqlQuery;
import jakarta.persistence.criteria.JoinType;

import java.util.List;

public class SqlQueryExecutor<T> implements SqlQuery<T> {

    @Override
    public SqlQuery<T> select(List<String> columns) {
        return null;
    }

    @Override
    public SqlQuery<T> from(String table) {
        return null;
    }

    @Override
    public SqlQuery<T> from(String table, String alias) {
        return null;
    }

    @Override
    public SqlQuery<T> where(String condition, Object... params) {
        return null;
    }

    @Override
    public SqlQuery<T> and(String condition, Object... params) {
        return null;
    }

    @Override
    public SqlQuery<T> or(String condition, Object... params) {
        return null;
    }

    @Override
    public SqlQuery<T> join(String tableWithAlias, JoinType joinType, String onCondition, Object... params) {
        return null;
    }

    @Override
    public SqlQuery<T> groupBy(String... columns) {
        return null;
    }

    @Override
    public SqlQuery<T> having(String condition, Object... params) {
        return null;
    }

    @Override
    public SqlQuery<T> orderBy(String... columns) {
        return null;
    }

    @Override
    public SqlQuery<T> limit(int maxRows) {
        return null;
    }

    @Override
    public SqlQuery<T> offset(int startPosition) {
        return null;
    }

    @Override
    public String build() {
        return "";
    }
}
