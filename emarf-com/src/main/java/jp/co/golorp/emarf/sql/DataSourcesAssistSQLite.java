/*
Copyright 2022 golorp

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package jp.co.golorp.emarf.sql;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.NotImplementedException;

import jp.co.golorp.emarf.util.MapList;

/**
 * SQLite用データソース実装
 *
 * @author golorp
 */
public final class DataSourcesAssistSQLite extends DataSourcesAssist {

    @Override
    public ResultSet getTables(final DatabaseMetaData metaData, final String schemaPattern) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getColumns(final DatabaseMetaData metaData, final String schemaPattern,
            final String tableNamePattern) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getPrimaryKeys(final DatabaseMetaData metaData, final String schemaPattern, final String tableName)
            throws SQLException {
        return null;
    }

    @Override
    protected String getTableComment(final String tableName) {
        return null;
    }

    @Override
    protected String getColumnComment(final String tableName, final String columnName) {
        return null;
    }

    @Override
    protected MapList getUniqueIndexes(final String tableName) {
        return null;
    }

    @Override
    public String joinedSQL(final String[] array) {
        return null;
    }

    @Override
    public String toDateSQL(final String lowerColumn) {
        return null;
    }

    @Override
    public String toDateTimeSQL(final String s) {
        return s;
    }

    @Override
    public String toTimestampSQL(final String lowerColumn) {
        return null;
    }

    @Override
    public String formatedSQL(final String s, final String format) {
        return s;
    }

    @Override
    public String quotedSQL(final String columnName) {
        return null;
    }

    @Override
    public String quoteEscapedSQL(final String columnName) {
        return null;
    }

    @Override
    public String addIdColumn(final String sql) {
        return null;
    }

    @Override
    public String getPagedSql(final String sql, final Integer rows, final Integer page) {
        return null;
    }

    @Override
    public String trimedSQL(final String columnName) {
        return null;
    }

    @Override
    public String date2CharSQL(final String s) {
        return s;
    }

    @Override
    public String time2CharSQL(final String s) {
        return s;
    }

    @Override
    public String dateTime2CharSQL(final String s) {
        return s;
    }

    @Override
    public String timestamp2CharSQL(final String s) {
        return s;
    }

    @Override
    public String dateAdd(final String columnName, final int d) {
        throw new NotImplementedException();
    }

    @Override
    public String nvlSysdate(final String columnName) {
        throw new NotImplementedException();
    }

    @Override
    public String nvlZero(final String columnName) {
        throw new NotImplementedException();
    }

    @Override
    public String sysDate() {
        throw new NotImplementedException();
    }

    @Override
    public String sysTimestamp() {
        throw new NotImplementedException();
    }

    @Override
    public int getColumnSize(final ResultSet columns) throws SQLException {
        return columns.getInt("COLUMN_SIZE");
    }

    @Override
    public String castInteger(final String rightHand) {
        return rightHand;
    }

    @Override
    public String int2charSQL(final String a) {
        return a;
    }

}
