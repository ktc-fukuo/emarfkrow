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

import jp.co.golorp.emarf.util.MapList;

/**
 * PostgreSQL用データソース実装
 *
 * @author golorp
 */
public final class DataSourcesAssistPostgreSQL extends DataSourcesAssist {

    @Override
    public ResultSet getTables(final DatabaseMetaData metaData, final String schemaPattern) throws SQLException {
        return metaData.getTables(null, "public", null, new String[] { "TABLE", "VIEW" });
    }

    @Override
    public ResultSet getColumns(final DatabaseMetaData metaData, final String schemaPattern,
            final String tableNamePattern) throws SQLException {
        return metaData.getColumns(null, "public", tableNamePattern.toLowerCase(), null);
    }

    @Override
    public ResultSet getPrimaryKeys(final DatabaseMetaData metaData, final String schemaPattern,
            final String tableName) throws SQLException {
        return metaData.getPrimaryKeys(null, "public", tableName.toLowerCase());
    }

    @Override
    protected String getTableComment(final String tableName) {
        String sql = "";
        sql += "SELECT ";
        sql += "      c.relname ";
        sql += "    , d.description ";
        sql += "FROM ";
        sql += "    pg_class c ";
        sql += "    INNER JOIN pg_namespace n ";
        sql += "        ON n.oid = c.relnamespace ";
        sql += "        AND n.nspname = 'public' ";
        sql += "    INNER JOIN pg_description d ";
        sql += "        ON d.objoid = c.oid ";
        sql += "        AND d.objsubid = 0 ";
        sql += "WHERE ";
        sql += "    c.relname = '" + tableName + "'";
        MapList mapList = Queries.select(sql, null, null);
        return mapList.get(0).get("TABLE_COMMENT").toString();
    }

    @Override
    protected String getColumnComment(final String tableName, final String columnName) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append("      d.description AS column_comment ");
        sb.append("FROM ");
        sb.append("    pg_attribute a ");
        sb.append("    JOIN pg_class c ");
        sb.append("        ON c.oid = a.attrelid ");
        sb.append("    JOIN pg_namespace n ");
        sb.append("        ON n.oid = c.relnamespace ");
        sb.append("    LEFT JOIN pg_description d ");
        sb.append("        ON d.objoid = c.oid ");
        sb.append("        AND d.objsubid = a.attnum ");
        sb.append("WHERE ");
        sb.append("    c.relname = '" + tableName + "' ");
        sb.append("    AND a.attname = '" + columnName + "' ");
        sb.append("    AND n.nspname = 'public' ");
        MapList mapList = Queries.select(sb.toString(), null, null);
        if (mapList == null) {
            return null;
        }
        return mapList.get(0).get("COLUMN_COMMENT").toString();
    }

    @Override
    protected MapList getUniqueIndexes(final String tableName) {

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append("    i.relname AS \"INDEX_NAME\" ");
        sb.append("    , ix.indnatts AS total_column_count ");
        sb.append("    , a.attname AS \"COLUMN_NAME\" ");
        sb.append("    , k.n AS column_position ");
        sb.append("FROM ");
        sb.append("    pg_class t ");
        sb.append("    INNER JOIN pg_index ix ");
        sb.append("        ON t.oid = ix.indrelid ");
        sb.append("    INNER JOIN pg_class i ");
        sb.append("        ON i.oid = ix.indexrelid ");
        sb.append("    INNER JOIN pg_attribute a ");
        sb.append("        ON a.attrelid = t.oid ");
        sb.append("    CROSS JOIN generate_series(1, CAST(current_setting('max_index_keys') AS integer)) AS k(n) ");
        sb.append("WHERE ");
        sb.append("    t.relname = '" + tableName.toLowerCase() + "'");
        sb.append("    AND a.attnum = ix.indkey [k.n - 1] ");
        sb.append("    AND ix.indisunique = TRUE ");
        sb.append("ORDER BY ");
        sb.append("    total_column_count ");
        sb.append("    , \"INDEX_NAME\" ");
        sb.append("    , column_position ");

        MapList mapList = Queries.select(sb.toString(), null, null);

        return mapList;
    }

    @Override
    public String joinedSQL(final String[] array) {
        return "CONCAT (" + String.join(", ", array) + ")";
    }

    @Override
    public String toDateSQL(final String s) {
        return "TO_DATE (SUBSTR (" + s + ", 0, 10), 'YYYY-MM-DD')";
    }

    @Override
    public String toDateTimeSQL(final String s) {
        return "TO_TIMESTAMP (REPLACE (SUBSTR (" + s + ", 0, 19), 'T', ' '), 'YYYY-MM-DD HH24:MI:SS')";
    }

    @Override
    public String toTimestampSQL(final String s) {
        return "TO_TIMESTAMP (REPLACE (SUBSTR (" + s + ", 0, 23), 'T', ' '), 'YYYY-MM-DD HH24:MI:SS.FF3')";
    }

    @Override
    public String date2CharSQL(final String s) {
        return "TO_CHAR (" + s + ", 'YYYY-MM-DD')";
    }

    @Override
    public String time2CharSQL(final String s) {
        return "TO_CHAR (" + s + ", 'HH24:MI')";
    }

    @Override
    public String dateTime2CharSQL(final String s) {
        //        return "TO_CHAR (" + s + ", 'YYYY-MM-DD\\\"T\\\"HH24:MI:SS')";
        return "TO_CHAR (" + s + ", 'YYYY-MM-DD HH24:MI:SS')";
    }

    @Override
    public String timestamp2CharSQL(final String s) {
        //        return "TO_CHAR (" + s + ", 'YYYY-MM-DD\\\"T\\\"HH24:MI:SS.MS')";
        return "TO_CHAR (" + s + ", 'YYYY-MM-DD HH24:MI:SS.MS')";
    }

    @Override
    public String formatedSQL(final String s, final String format) {
        return s;
    }

    @Override
    public String quotedSQL(final String columnNm) {
        return "\"" + columnNm.toLowerCase() + "\"";
    }

    @Override
    public String quoteEscapedSQL(final String columnNm) {
        return "\\\"" + columnNm.toLowerCase() + "\\\"";
    }

    @Override
    public String addIdColumn(final String sql) {
        return "SELECT ROW_NUMBER () OVER () AS \"id\", sub.* FROM (" + sql + ") sub ORDER BY ROW_NUMBER () OVER ()";
    }

    @Override
    public String getPagedSql(final String sql, final Integer rows, final Integer page) {
        int firstRow = (page - 1) * rows;
        return sql + " limit " + rows + " offset " + firstRow;
    }

    @Override
    public String trimedSQL(final String columnName) {
        return "TRIM(TRAILING ' ' FROM " + columnName + ")";
    }

    @Override
    public String dateAdd(final String columnName, final int d) {
        return "DATE_ADD(" + columnName + ", INTERVAL " + d + " DAY)";
    }

    @Override
    public String nvlSysdate(final String columnName) {
        return "IFNULL (" + columnName + ", sysdate())";
    }

    @Override
    public String nvlZero(final String columnName) {
        return "IFNULL (" + columnName + ", 0)";
    }

    @Override
    public String sysDate() {
        return "sysdate()";
    }

    @Override
    public String sysTimestamp() {
        return "CURRENT_TIMESTAMP";
    }

    @Override
    public int getColumnSize(final ResultSet columns) throws SQLException {
        // TODO postgresで確認
        return columns.getInt("COLUMN_SIZE");
    }

    @Override
    public String castInteger(final String rightHand) {
        return "CAST (" + rightHand + " AS INTEGER)";
    }

    @Override
    public String int2charSQL(final String a) {
        return a;
    }

}
