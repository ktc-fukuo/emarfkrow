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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import jp.co.golorp.emarf.util.MapList;

/**
 * Oracle用データソース実装
 *
 * @author golorp
 */
public class DataSourcesAssistOracle extends DataSourcesAssist {

    @Override
    public final String dateAdd(final String columnName, final int d) {
        return columnName + " + " + d;
    }

    @Override
    public final String nvlSysdate(final String columnName) {
        return "NVL (" + columnName + ", SYSDATE)";
    }

    @Override
    public final String nvlZero(final String columnName) {
        return "NVL (" + columnName + ", 0)";
    }

    /**
     * @param tableName テーブル名
     * @return テーブルコメント
     */
    protected String getTableComment(final String tableName) {
        String sql = "SELECT COMMENTS FROM USER_TAB_COMMENTS WHERE TABLE_NAME = '" + tableName + "'";
        MapList mapList = Queries.select(sql, null, null);
        Map<String, Object> map = mapList.get(0);
        if (map.get("COMMENTS") != null) {
            return map.get("COMMENTS").toString();
        }
        return null;
    }

    /**
     * @param tableName テーブル名
     * @param columnName カラム名
     * @return カラムコメント
     */
    protected String getColumnComment(final String tableName, final String columnName) {
        String sql = "SELECT COMMENTS FROM USER_COL_COMMENTS WHERE TABLE_NAME = '" + tableName + "' AND COLUMN_NAME = '"
                + columnName + "'";
        MapList mapList = Queries.select(sql, null, null);
        Map<String, Object> map = mapList.get(0);
        if (map.get("COMMENTS") != null) {
            return map.get("COMMENTS").toString();
        }
        return null;
    }

    /**
     * @param tableName テーブル名
     * @return 複数のユニークインデクスについての列情報
     */
    protected MapList getUniqueIndexes(final String tableName) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT \n");
        sb.append("    uic.* \n");
        sb.append("FROM \n");
        sb.append("    ( \n");
        sb.append("        SELECT \n");
        sb.append("            ui.table_name \n");
        sb.append("            , ui.index_name \n");
        sb.append("            , COUNT(uic.column_name) AS column_count \n");
        sb.append("        FROM \n");
        sb.append("            user_indexes ui                     --インデクス \n");
        sb.append("            INNER JOIN user_ind_columns uic     --インデクス列 \n");
        sb.append("                ON uic.table_name = ui.table_name \n");
        sb.append("                AND uic.index_name = ui.index_name \n");
        sb.append("            LEFT OUTER JOIN user_constraints uc --主キー \n");
        sb.append("                ON uc.owner = ui.table_owner \n");
        sb.append("                AND uc.table_name = ui.table_name \n");
        sb.append("                AND uc.constraint_type = 'P' \n");
        sb.append("        WHERE \n");
        sb.append("            ui.table_name = '" + tableName + "' \n");
        sb.append("            AND ui.index_type = 'NORMAL' \n");
        sb.append("            AND ui.uniqueness = 'UNIQUE' \n");
        sb.append("            AND uc.owner IS NULL                --主キー以外のインデクス \n");
        sb.append("        GROUP BY \n");
        sb.append("            ui.table_name \n");
        sb.append("            , ui.index_name \n");
        sb.append("    ) ui \n");
        sb.append("    INNER JOIN user_ind_columns uic             --インデクス列 \n");
        sb.append("        ON uic.table_name = ui.table_name \n");
        sb.append("        AND uic.index_name = ui.index_name \n");
        sb.append("ORDER BY \n");
        sb.append("    ui.table_name \n");
        sb.append("    , ui.column_count \n");
        sb.append("    , ui.index_name \n");
        sb.append("    , uic.column_position \n");
        String sql = sb.toString();
        MapList mapList = Queries.select(sql, null, null);
        return mapList;
    }

    /**
     * @param array 文字列配列
     * @return 結合後の文字列
     */
    public String joinedSQL(final String[] array) {
        return String.join(" || ", array);
    }

    /**
     * @param s カラム物理名
     * @return 日付変換SQL
     */
    public String toDateSQL(final String s) {
        return "TO_DATE (SUBSTR (" + s + ", 0, 10), 'YYYY-MM-DD')";
    }

    /**
     * @param s カラム物理名
     * @return ミリ秒タイムスタンプ変換SQL
     */
    public String toDateTimeSQL(final String s) {
        return "TO_TIMESTAMP (REPLACE (SUBSTR (" + s + ", 0, 19), 'T', ' '), 'YYYY-MM-DD HH24:MI:SS')";
    }

    /**
     * @param s カラム物理名
     * @return ミリ秒タイムスタンプ変換SQL
     */
    public String toTimestampSQL(final String s) {
        return "TO_TIMESTAMP (REPLACE (SUBSTR (" + s + ", 0, 23), 'T', ' '), 'YYYY-MM-DD HH24:MI:SS.FF3')";
    }

    /**
     * @param s カラム物理名
     * @param format フォーマット文字列
     * @return フォーマットSQL
     */
    public String formatedSQL(final String s, final String format) {
        return "TO_CHAR (" + toTimestampSQL(s) + ", '" + format + "')";
    }

    /**
     * @param columnName カラム物理名
     * @return 囲み後のSQL文字列
     */
    public String quotedSQL(final String columnName) {
        return "\"" + columnName + "\"";
    }

    /**
     * @param columnName カラム物理名
     * @return エスケープ済みで囲み後のSQL文字列
     */
    public String quoteEscapedSQL(final String columnName) {
        return "\\\"" + columnName + "\\\"";
    }

    /**
     * @param sql 発行するSQL
     * @return ID列を付加したSQL
     */
    public String addIdColumn(final String sql) {
        return "SELECT ROWNUM AS \"id\", sub.* FROM (" + sql + ") sub ORDER BY ROWNUM";
    }

    /**
     * @param sql  発行するSQL
     * @param rows ページ行数
     * @param page ページ番号
     * @return ページ繰りしたSQL
     */
    public String getPagedSql(final String sql, final Integer rows, final Integer page) {
        int firstRow = (page - 1) * rows + 1;
        int lastRow = firstRow + rows - 1;
        String pagedSql = "";
        pagedSql += "SELECT ";
        pagedSql += "    B.* ";
        pagedSql += "FROM ";
        pagedSql += "    (";
        pagedSql += "        SELECT ";
        pagedSql += "            ROWNUM AS ROW_NUM, ";
        pagedSql += "            A.* ";
        pagedSql += "        FROM ";
        pagedSql += "            (";
        pagedSql += sql;
        pagedSql += "            ) A";
        pagedSql += "    ) B ";
        pagedSql += "WHERE ";
        pagedSql += "    B.ROW_NUM BETWEEN " + firstRow + " AND " + lastRow;
        return pagedSql;
    }

    /**
     * @param columnName カラム名
     * @return 全半角スペースのトリム文字列
     */
    public String trimedSQL(final String columnName) {
        return "RTRIM (RTRIM (" + columnName + "), '　')";
    }

    /** */
    @Override
    public String date2CharSQL(final String s) {
        return "TO_CHAR (" + s + ", 'YYYY-MM-DD')";
    }

    /** */
    @Override
    public String time2CharSQL(final String s) {
        return "TO_CHAR (" + s + ", 'HH24:MI:SS')";
    }

    /** */
    @Override
    public String dateTime2CharSQL(final String s) {
        return "TO_CHAR (" + s + ", 'YYYY-MM-DD HH24:MI:SS')";
    }

    /** */
    @Override
    public String timestamp2CharSQL(final String s) {
        return "TO_CHAR (" + s + ", 'YYYY-MM-DD HH24:MI:SS.FF3')";
    }

    /** */
    @Override
    public String sysDate() {
        return "SYSDATE";
    }

    /** */
    @Override
    public String sysTimestamp() {
        return "SYSTIMESTAMP";
    }

    /** */
    @Override
    public int getColumnSize(final ResultSet columns) throws SQLException {
        // viewで「NUMBER（桁指定なし）」になった場合、BigDecimalにするため「11」以上にする
        if (columns.getString("TYPE_NAME").equals("NUMBER") && columns.getInt("COLUMN_SIZE") == 38) {
            return 11;
        }
        return columns.getInt("COLUMN_SIZE");
    }

}
