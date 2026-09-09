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
package jp.co.golorp.emarf.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.co.golorp.emarf.io.FileUtil;
import jp.co.golorp.emarf.lang.StringUtil;

/**
 * SQL出力
 *
 * @author golorp
 */
public final class SqlGenerator extends BeanGenerator {

    /** プライベートコンストラクタ */
    private SqlGenerator() {
    }

    /**
     * SQL生成
     * @param tableInfos
     */
    public static void generate(final List<TableInfo> tableInfos) {

        //SQLフォルダ
        String sqlDir = getProjectDir() + File.separator + DIR_S;
        FileUtil.reMkDir(sqlDir);

        //検索SQL
        for (TableInfo tableInfo : tableInfos) {
            SqlGenerator.sqlSearch(sqlDir, tableInfo);
            SqlGenerator.sqlCorrect(sqlDir, tableInfo);
        }
    }

    /**
     * 各モデルの検索SQL出力
     * @param sqlDir SQLファイル出力ディレクトリ
     * @param table テーブル情報
     */
    private static void sqlSearch(final String sqlDir, final TableInfo table) {
        String entity = StringUtil.toPascalCase(table.getName());
        int refs = 0; //参照モデルの番号
        List<String> s = new ArrayList<String>();
        for (ColumnInfo column : table.getColumns().values()) { //カラム行追加
            String prefix = "    , ";
            if (s.size() == 0) {
                s.add("SELECT");
                //                if (table.isGantt()) {
                //                    s.add("      * ");
                //                    s.add("FROM");
                //                    s.add("    ( ");
                //                    s.add("        SELECT");
                //                    s.add("              SYS_CONNECT_BY_PATH (a.\"" + column.getName() + "\", ',') AS PATH,");
                //                } else if (table.isGraph()) {
                //                    s.add("    DISTINCT");
                //                    s.add("      '''' || LISTAGG (DISTINCT a.labels, ''',''' ON OVERFLOW TRUNCATE '...' WITH COUNT) WITHIN GROUP (ORDER BY a.labels) OVER (PARTITION BY a.type) || '''' AS \"labels\" ");
                //                    s.add("    , a.type ");
                //                    s.add("    , a.stack ");
                //                    s.add("    , a.label ");
                //                    s.add("    , LISTAGG (a.DATA, ',') WITHIN GROUP (ORDER BY a.labels) OVER (PARTITION BY a.type, a.label) AS DATA ");
                //                    s.add("FROM ");
                //                    s.add("    ( ");
                //                    s.add("SELECT ");
                //                }
                prefix = "      ";
            }
            s.add(prefix + SqlGenerator.getQuoted(column));
            if (column.getRefer() != null) { // 列の参照モデル情報があればカラム名の補完
                String meiSql = getMeiSql(refs, table, column);
                if (meiSql != null) {
                    ++refs;
                    s.add("    " + meiSql);
                }
            }
        }
        if (table.getName().matches(ELDEST_RE)) {
            int i = 0;
            for (TableInfo bro : table.getBrothers()) {
                ++i;
                for (String colName : bro.getNonPrimaryKeys()) {
                    if (!colName.matches("(?i)^" + UPDATE_AT + "$") && BeanGenerator.isMeta(colName)) {
                        continue;
                    }
                    ColumnInfo column = bro.getColumns().get(colName);
                    s.add("    , " + SqlGenerator.getQuoted(column, bro.getName(), "c" + i));
                }
            }
        }
        //        if (table.isGantt()) {
        //            s.add("    , b.DEPENDENCIES");
        //        }
        s.add("FROM");
        s.add("    " + table.getName() + " a ");
        if (table.getName().matches(ELDEST_RE)) {
            int i = 0;
            for (TableInfo bro : table.getBrothers()) {
                ++i;
                s.add("    LEFT OUTER JOIN " + bro.getName() + " c" + i + " ");
                s.add("        ON 1 = 1 ");
                for (String pk : bro.getPrimaryKeys()) {
                    s.add("        AND c" + i + "." + pk + " = a." + pk + " ");
                }
            }
        }
        //        if (table.isGantt()) {
        //            String pk = table.getPrimaryKeys().get(0);
        //            String oya = null;
        //            for (ColumnInfo column : table.getColumns().values()) {
        //                if (column.getRefer() == table) {
        //                    oya = column.getName();
        //                    break;
        //                }
        //                if (column.getName().matches("(?i)^.+" + pk + "$")) {
        //                    oya = column.getName();
        //                    break;
        //                }
        //            }
        //            s.add(assist.addDependencies(table, pk, oya));
        //            s.add("        START WITH");
        //            s.add("            a." + oya + " IS NULL ");
        //            s.add("        CONNECT BY");
        //            s.add("            PRIOR " + pk + " = a." + oya);
        //            s.add("    ) a ");
        //        }
        s.add("WHERE");
        s.add("    1 = 1 ");
        for (ColumnInfo column : table.getColumns().values()) {
            addWhere(s, column);
        }
        //        if (table.isGantt()) {
        //            s.add("ORDER BY");
        //            s.add("    a.PATH DESC");
        //        } else if (table.isGraph()) {
        //            s.add("    ) a ");
        //        }
        if (!table.isView()) {
            s.add("ORDER BY");
            if (table.getPrimaryKeys().size() > 0) {
                String orders = "";
                if (table.getPrimaryKeys().size() == 1) {
                    for (ColumnInfo column : table.getColumns().values()) {
                        if (StringUtil.endsWith(ORDER_SUFS, column.getName())) {
                            if (orders.length() > 0) {
                                orders += "    , ";
                            } else {
                                orders += "    ";
                            }
                            orders += "a." + ASSIST.quotedSQL(column.getName()) + "\r\n";
                        }
                    }
                }
                for (String pk : table.getPrimaryKeys()) {
                    if (orders.length() > 0) {
                        orders += "    , ";
                    } else {
                        orders += "    ";
                    }
                    orders += "a." + ASSIST.quotedSQL(pk) + "\r\n";
                }
                if (table.getPrimaryKeys().size() > 1) {
                    for (ColumnInfo column : table.getColumns().values()) {
                        if (StringUtil.endsWith(ORDER_SUFS, column.getName())) {
                            if (orders.length() > 0) {
                                orders += "    , ";
                            } else {
                                orders += "    ";
                            }
                            orders += "a." + ASSIST.quotedSQL(column.getName()) + "\r\n";
                        }
                    }
                }
                s.add(orders.replaceFirst("\r\n$", ""));
            } else {
                for (int i = 1; i <= table.getColumns().size(); i++) {
                    if (i == 1) {
                        s.add("    " + i);
                    } else {
                        s.add("    , " + i);
                    }
                }
            }
        }

        FileUtil.writeFile(sqlDir + File.separator + entity + "Search.sql", s);
    }

    /**
     * @param refs
     * @param table
     * @param column
     * @return String
     */
    public static String getMeiSql(final int refs, final TableInfo table, final ColumnInfo column) {

        if (table.isView()) {
            return null;
        }

        TableInfo refer = column.getRefer();

        //IDと名称のサフィックスペアでループ
        for (String[] e : REFER_PAIRS) {
            String[] keySufs = e[0].split("&");
            String valSuf = e[1];

            for (String keySuf : keySufs) {

                // 参照元カラム名が参照キーに合致しなければスキップ
                if (!column.getName().matches("(?i)^.*" + keySuf + "$")) {
                    continue;
                }

                // 参照元カラム名のIDサフィックスを名称サフィックスに置換して、参照元の名称カラム名を取得
                String srcKey = column.getName();
                String srcType = column.getDataType();
                String srcVal = srcKey.replaceAll("(?i)" + keySuf + "$", valSuf).toUpperCase();

                // 参照先でID・名称のサフィックスに合致するカラムを取得し、両方取得できなければスキップ
                String destKey = null;
                String destType = null;
                String destVal = null;
                for (String columnName : refer.getColumns().keySet()) {
                    // キー列の検査
                    if (srcKey.matches("(?i)^.*" + columnName + "$")) {
                        destKey = columnName;
                        destType = refer.getColumns().get(destKey).getDataType();
                    }
                    // 値列の検査
                    if (srcVal.matches("(?i)^.*" + columnName + "$")) {
                        destVal = columnName;
                    }
                    // 参照先のキーと値の列名が取れれば中断
                    if (destKey != null && destVal != null) {
                        break;
                    }
                }
                // 参照先のキーと値の列名が取れなければ中断
                if (destKey == null || destVal == null) {
                    continue;
                }

                // 生成した参照元名称カラムが、参照元に既存でない場合はselect句に追加
                boolean isSrcMei = false;
                for (String columnName : table.getColumns().keySet()) {
                    if (columnName.matches("(?i)^" + srcVal + "$")) {
                        isSrcMei = true;
                        break;
                    }
                }
                if (!isSrcMei) {
                    String srcPrefix = srcKey.replaceFirst("(?i)" + destKey + "$", "");
                    String destKeys = "";
                    for (String primaryKey : refer.getPrimaryKeys()) {
                        String destPK = ASSIST.quotedSQL(primaryKey);
                        destPK = "r" + refs + "." + destPK;
                        if (srcType.equals("String") && !destType.equals("String")) {
                            destPK = ASSIST.int2charSQL(destPK);
                        }
                        if (destKeys.length() > 0) {
                            destKeys += " AND ";
                        }
                        String srcFK = "a." + ASSIST.quotedSQL(srcPrefix + primaryKey);
                        if (srcType.equals("String") && !destType.equals("String")) {
                            srcFK = ASSIST.castInteger(srcFK);
                        }
                        destKeys += destPK + " = " + srcFK;
                    }
                    String srcV = ASSIST.quotedSQL(srcVal);
                    String destV = ASSIST.quotedSQL(destVal);
                    return ", (SELECT r" + refs + "." + destV + " FROM " + refer.getName() + " r" + refs + " WHERE "
                            + destKeys + ") AS " + srcV;
                }
            }
        }

        return null;
    }

    /**
     * 各モデルの検索SQL出力
     * @param sqlDir SQLファイル出力ディレクトリ
     * @param table テーブル情報
     */
    private static void sqlCorrect(final String sqlDir, final TableInfo table) {

        //参照モデルの連番
        int refs = 0;

        List<String> s = new ArrayList<String>();

        for (ColumnInfo column : table.getColumns().values()) {

            //カラム行追加
            String prefix = "    , ";
            if (s.size() == 0) {
                s.add("SELECT");
                prefix = "      ";
            }
            s.add(prefix + SqlGenerator.getQuoted(column));

            // 列の参照モデル情報があればカラム名の補完
            if (column.getRefer() != null) {
                String meiSql = getMeiSql(refs, table, column);
                if (meiSql != null) {
                    ++refs;
                    s.add("    " + meiSql);
                }
            }
        }

        if (table.getName().matches(ELDEST_RE)) {
            int i = 0;
            for (TableInfo bro : table.getBrothers()) {
                ++i;
                for (String colName : bro.getNonPrimaryKeys()) {
                    if (!colName.matches("(?i)^" + UPDATE_AT + "$") && BeanGenerator.isMeta(colName)) {
                        continue;
                    }
                    ColumnInfo column = bro.getColumns().get(colName);
                    s.add("    , " + SqlGenerator.getQuoted(column, bro.getName(), "c" + i));
                }
            }
        }

        s.add("FROM");
        s.add("    " + table.getName() + " a ");

        if (table.getName().matches(ELDEST_RE)) {
            List<TableInfo> bros = table.getBrothers();
            int i = 0;
            for (TableInfo bro : bros) {
                ++i;
                s.add("    LEFT OUTER JOIN " + bro.getName() + " c" + i + " ");
                s.add("        ON 1 = 1 ");
                if (bro.getColumns().containsKey(DELETE_F)) {
                    s.add("        AND " + ASSIST.nvlZero("c" + i + "." + DELETE_F) + " != 1 ");
                }
                if (bro.getColumns().containsKey(TEKIYO_BI)) {
                    s.add("        AND " + ASSIST.nvlSysdate("c" + i + "." + TEKIYO_BI) + " <= " + ASSIST.sysDate()
                            + " ");
                }
                if (bro.getColumns().containsKey(HAISHI_BI)) {
                    s.add("        AND " + ASSIST.dateAdd(ASSIST.nvlSysdate("c" + i + "." + HAISHI_BI), 1) + " > "
                            + ASSIST.sysDate());
                }
                for (String pk : bro.getPrimaryKeys()) {
                    s.add("        AND c" + i + "." + pk + " = a." + pk + " ");
                }
            }
        }

        //組合せモデル
        if (table.getComboInfos().size() > 0) {
            addComboSql(s, table);
        }

        s.add("WHERE");
        s.add("    1 = 1 ");
        if (table.getColumns().containsKey(DELETE_F)) {
            s.add("    AND " + ASSIST.nvlZero("a." + DELETE_F) + " != 1 ");
        }
        if (table.getColumns().containsKey(TEKIYO_BI)) {
            s.add("    AND " + ASSIST.nvlSysdate("a." + TEKIYO_BI) + " <= " + ASSIST.sysDate() + " ");
        }
        if (table.getColumns().containsKey(HAISHI_BI)) {
            s.add("    AND " + ASSIST.dateAdd(ASSIST.nvlSysdate("a." + HAISHI_BI), 1) + " > " + ASSIST.sysDate()
                    + " ");
        }
        for (ColumnInfo column : table.getColumns().values()) {
            addWhere(s, column);
        }

        //制約モデル
        if (table.getStintInfo() != null) {
            addStintSql(s, table);
        }

        if (!table.isView()) {
            s.add("ORDER BY");
            if (table.getPrimaryKeys().size() > 0) {
                String orders = "";
                if (table.getPrimaryKeys().size() == 1) {
                    for (ColumnInfo column : table.getColumns().values()) {
                        if (StringUtil.endsWith(ORDER_SUFS, column.getName())) {
                            if (orders.length() > 0) {
                                orders += "    , ";
                            } else {
                                orders += "    ";
                            }
                            orders += "a." + ASSIST.quotedSQL(column.getName()) + "\r\n";
                        }
                    }
                }
                for (String pk : table.getPrimaryKeys()) {
                    if (orders.length() > 0) {
                        orders += "    , ";
                    } else {
                        orders += "    ";
                    }
                    orders += "a." + ASSIST.quotedSQL(pk) + "\r\n";
                }
                if (table.getPrimaryKeys().size() > 1) {
                    for (ColumnInfo column : table.getColumns().values()) {
                        if (StringUtil.endsWith(ORDER_SUFS, column.getName())) {
                            if (orders.length() > 0) {
                                orders += "    , ";
                            } else {
                                orders += "    ";
                            }
                            orders += "a." + ASSIST.quotedSQL(column.getName()) + "\r\n";
                        }
                    }
                }
                s.add(orders.replaceFirst("\r\n$", ""));
            } else {
                for (int i = 1; i <= table.getColumns().size(); i++) {
                    if (i == 1) {
                        s.add("    " + i);
                    } else {
                        s.add("    , " + i);
                    }
                }
            }
        }

        String entity = StringUtil.toPascalCase(table.getName());
        FileUtil.writeFile(sqlDir + File.separator + entity + "Correct.sql", s);
    }

    /**
     * @param sql
     * @param table
     */
    private static void addStintSql(final List<String> sql, final TableInfo table) {

        TableInfo stint = table.getStintInfo();

        //        String anotherKey = "";
        //        for (String pk : stint.getPrimaryKeys()) {
        //            if (pk.equals(start)) {
        //                continue;
        //            }
        //            if (!pk.equals(table.getPrimaryKeys().get(0))) {
        //                anotherKey = pk;
        //                break;
        //            }
        //        }

        sql.add("    AND EXISTS ( ");
        sql.add("        SELECT");
        sql.add("              * ");
        sql.add("        FROM");
        sql.add("            " + stint.getName() + " p ");
        sql.add("        WHERE");
        sql.add("            1 = 1 ");
        if (stint.getColumns().containsKey(DELETE_F)) {
            sql.add("            AND " + ASSIST.nvlZero("p." + DELETE_F) + " != 1 ");
        }
        if (stint.getColumns().containsKey(TEKIYO_BI)) {
            sql.add("            AND " + ASSIST.nvlSysdate("p." + TEKIYO_BI) + " <= " + ASSIST.sysDate()
                    + " ");
        }
        if (stint.getColumns().containsKey(HAISHI_BI)) {
            sql.add("            AND " + ASSIST.dateAdd(ASSIST.nvlSysdate("p." + HAISHI_BI), 1) + " > "
                    + ASSIST.sysDate());
        }
        List<String> primaryKeys = new ArrayList<String>(stint.getPrimaryKeys());
        primaryKeys.remove(TEKIYO_BI);
        for (int i = 0; i < primaryKeys.size(); i++) {
            String primaryKey = primaryKeys.get(i);
            if (i < primaryKeys.size() - 1) {
                String snake = StringUtil.toSnakeCase(primaryKey);
                sql.add("            AND p." + primaryKey + " = :" + snake + " ");
            } else {
                sql.add("            AND p." + primaryKey + " = a." + primaryKey + " ");
            }
        }
        sql.add("    ) ");
    }

    /**
     * @param sql
     * @param table
     */
    private static void addComboSql(final List<String> sql, final TableInfo table) {

        int i = 0;

        for (TableInfo combo : table.getComboInfos()) {
            ++i;
            sql.add("    INNER JOIN " + combo.getName() + " c" + i + " ");
            sql.add("        ON 1 = 1 ");
            if (combo.getColumns().containsKey(DELETE_F)) {
                sql.add("        AND " + ASSIST.nvlZero("c" + i + "." + DELETE_F) + " != 1 ");
            }
            if (combo.getColumns().containsKey(TEKIYO_BI)) {
                sql.add("        AND " + ASSIST.nvlSysdate("c" + i + "." + TEKIYO_BI) + " <= " + ASSIST.sysDate()
                        + " ");
            }
            if (combo.getColumns().containsKey(HAISHI_BI)) {
                sql.add("        AND " + ASSIST.dateAdd(ASSIST.nvlSysdate("c" + i + "." + HAISHI_BI), 1) + " > "
                        + ASSIST.sysDate());
            }
            for (String pk : combo.getPrimaryKeys()) {
                sql.add("        AND c" + i + "." + pk + " = a." + pk + " ");
            }
        }
    }

    /**
     * @param sql
     * @param column
     */
    private static void addWhere(final List<String> sql, final ColumnInfo column) {

        String name = column.getName();

        //カラム名が「TABLE_NAME」なら出力しない
        if (name.matches("(?i)^" + VIEW_DETAIL + "$")) {
            return;
        }

        // quoted
        String q = ASSIST.quotedSQL(name);

        // parameters
        String cleanedKey = name.replaceAll("\\$", "_");
        String p = BeanGenerator.getRightHand(cleanedKey, column);

        // trimed
        String t = ASSIST.trimedSQL("a." + q);

        if (StringUtil.endsWith(INPUT_F_SUFS, name)) {

            // FLAG検索
            sql.add("    AND CASE WHEN " + t + " IS NULL THEN '0' ELSE TO_CHAR (a." + q + ") END IN (" + p + ") ");

        } else if (StringUtil.endsWith(INPUT_OP_SUFS, name)) {

            // IN検索
            sql.add("    AND " + t + " IN (" + p + ") ");

        } else if (column.getDataType().equals("String")) {

            if (column.isPk()) {
                sql.add("    AND UPPER (" + t + ") = UPPER (" + p + "_full) ");
            }

            if (name.toUpperCase().equals(OPT_K)) {
                //参照キーの場合は、パラメータをデータで後方一致
                sql.add("    AND UPPER (" + p + ") LIKE UPPER (" + ASSIST.joinedSQL(new String[] { "'%'", t }) + ") ");

            } else {
                //以外の文字列は、データをパラメータで部分一致
                sql.add("    AND UPPER (" + t + ") LIKE UPPER (" + ASSIST.joinedSQL(new String[] { "'%'", p, "'%'" })
                        + ") ");
            }

        } else {

            // INT列の場合、postgresならcastを入れる
            if (column.getTypeName().startsWith("INT")) {
                p = ASSIST.castInteger(p);
            }

            // 以外は等値検索
            sql.add("    AND a." + q + " = " + p + " ");
        }

        // 範囲検索なら追加
        if (StringUtil.endsWith(INPUT_RG_SUFS, name)) {
            sql.add("    AND a." + q + " >= " + BeanGenerator.getRightHand(name + "_1 ", column));
            sql.add("    AND a." + q + " <= " + BeanGenerator.getRightHand(name + "_2 ", column));
        }
    }

    /**
     * @param column
     * @return quoted
     */
    private static String getQuoted(final ColumnInfo column) {
        return getQuoted(column, null, "a");
    }

    /**
     * @param column
     * @param tableName
     * @param alias
     * @return quoted
     */
    public static String getQuoted(final ColumnInfo column, final String tableName, final String alias) {

        String colName = column.getName();
        String cQuoted = ASSIST.quotedSQL(colName);
        String aQuoted = alias + "." + cQuoted;

        String asQuoted = cQuoted;
        if (tableName != null) {
            asQuoted = ASSIST.quotedSQL(tableName + "." + colName);
        }

        if (column.getTypeName().equals("CHAR")) {

            aQuoted = ASSIST.trimedSQL(aQuoted) + " AS " + asQuoted;

        } else if (StringUtil.endsWith(INPUT_BI_SUFS, colName)) {

            aQuoted = ASSIST.date2CharSQL(aQuoted) + " AS " + asQuoted;

        } else if (StringUtil.endsWith(INPUT_HM_SUFS, colName)) {

            aQuoted = ASSIST.time2CharSQL(aQuoted) + " AS " + asQuoted;

        } else if (StringUtil.endsWith(INPUT_DT_SUFS, colName)) {

            aQuoted = ASSIST.dateTime2CharSQL(aQuoted) + " AS " + asQuoted;

        } else if (StringUtil.endsWith(INPUT_TS_SUFS, colName)) {

            aQuoted = ASSIST.timestamp2CharSQL(aQuoted) + " AS " + asQuoted;

        } else {

            aQuoted = aQuoted + " AS " + asQuoted;
        }

        return aQuoted;
    }

}
