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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import jp.co.golorp.emarf.io.FileUtil;
import jp.co.golorp.emarf.lang.StringUtil;
import jp.co.golorp.emarf.sql.DataSources;
import jp.co.golorp.emarf.sql.DataSourcesAssist;
import jp.co.golorp.emarf.util.ResourceBundles;

/**
 * SQL出力
 *
 * @author golorp
 */
public final class SqlGenerator {

    /** BeanGenerator.properties */
    private static ResourceBundle bundle = ResourceBundles.getBundle(BeanGenerator.class);

    /** 長兄 */
    private static String eldestRe = "";
    /** 参照列名ペア */
    private static Set<String[]> referPairs = new LinkedHashSet<String[]>();

    /** 適用日 */
    private static String start;
    /** 終了日 */
    private static String until;
    /** 更新日時カラム名 */
    private static String updateTs;
    /** 削除フラグ */
    private static String deleteF;
    /** 表示順サフィックス */
    private static String[] orderSuffixs;
    /** VIEWの詳細画面にするテーブル名 */
    private static String viewDetail;

    /** タイムスタンプサフィックス */
    private static String[] inputTimestampSuffixs;
    /** 日時入力サフィックス */
    private static String[] inputDateTimeSuffixs;
    /** 日付入力サフィックス */
    private static String[] inputDateSuffixs;
    /** 時刻入力サフィックス */
    private static String[] inputHourSuffixs;
    /** 範囲指定サフィックス */
    private static String[] inputRangeSuffixs;
    /** フラグサフィックス */
    private static String[] inputFlagSuffixs;
    /** options項目サフィックス */
    private static String[] inputOptionsSuffixs;

    /** 区分カラム */
    private static String optK;

    /** DataSourcesAssist */
    private static DataSourcesAssist assist = DataSources.getAssist();

    /**  */
    private static String dirSql = "src\\main\\resources\\sql";

    /** プライベートコンストラクタ */
    private SqlGenerator() {
    }

    /**
     * SQL生成
     * @param projectDir
     * @param tableInfos
     */
    public static void generate(final String projectDir, final List<TableInfo> tableInfos) {

        /* 設定ファイル読み込み */
        if (bundle != null) {

            eldestRe = bundle.getString("relation.eldest.re");
            String[] pairs = bundle.getString("relation.refer.pairs").split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":");
                referPairs.add(kv);
            }

            start = bundle.getString("column.start").toUpperCase();
            until = bundle.getString("column.until").toUpperCase();
            updateTs = bundle.getString("column.update.timestamp");
            deleteF = bundle.getString("column.delete").toUpperCase();
            orderSuffixs = bundle.getString("column.order.suffixs").split(",");
            viewDetail = bundle.getString("view.detail");

            inputTimestampSuffixs = bundle.getString("input.timestamp.suffixs").split(",");
            inputDateTimeSuffixs = bundle.getString("input.datetime.suffixs").split(",");
            inputDateSuffixs = bundle.getString("input.date.suffixs").split(",");
            inputHourSuffixs = bundle.getString("input.hour.suffixs").split(",");
            inputRangeSuffixs = bundle.getString("input.range.suffixs").split(",");
            inputFlagSuffixs = bundle.getString("input.flag.suffixs").split(",");
            inputOptionsSuffixs = bundle.getString("input.options.suffixs").split(",");

            optK = bundle.getString("options.key").toUpperCase();

            dirSql = bundle.getString("dir.sql");
        }

        //SQLフォルダ
        String sqlDir = projectDir + File.separator + dirSql;
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
        if (table.getName().matches(eldestRe)) {
            int i = 0;
            for (TableInfo bro : table.getBrothers()) {
                ++i;
                for (String colName : bro.getNonPrimaryKeys()) {
                    if (!colName.matches("(?i)^" + updateTs + "$") && BeanGenerator.isMeta(colName)) {
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
        if (table.getName().matches(eldestRe)) {
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
                        if (StringUtil.endsWith(orderSuffixs, column.getName())) {
                            if (orders.length() > 0) {
                                orders += "    , ";
                            } else {
                                orders += "    ";
                            }
                            orders += "a." + assist.quotedSQL(column.getName()) + "\r\n";
                        }
                    }
                }
                for (String pk : table.getPrimaryKeys()) {
                    if (orders.length() > 0) {
                        orders += "    , ";
                    } else {
                        orders += "    ";
                    }
                    orders += "a." + assist.quotedSQL(pk) + "\r\n";
                }
                if (table.getPrimaryKeys().size() > 1) {
                    for (ColumnInfo column : table.getColumns().values()) {
                        if (StringUtil.endsWith(orderSuffixs, column.getName())) {
                            if (orders.length() > 0) {
                                orders += "    , ";
                            } else {
                                orders += "    ";
                            }
                            orders += "a." + assist.quotedSQL(column.getName()) + "\r\n";
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

        if (assist == null) {
            assist = DataSources.getAssist();
        }

        // BeanGeneratorの子モデル処理時にはnullかも知れない
        if (referPairs.size() == 0) {
            String[] pairs = bundle.getString("relation.refer.pairs").split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":");
                referPairs.add(kv);
            }
        }

        //IDと名称のサフィックスペアでループ
        for (String[] e : referPairs) {
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
                        String destPK = assist.quotedSQL(primaryKey);
                        destPK = "r" + refs + "." + destPK;
                        if (srcType.equals("String") && !destType.equals("String")) {
                            destPK = assist.int2charSQL(destPK);
                        }
                        if (destKeys.length() > 0) {
                            destKeys += " AND ";
                        }
                        String srcFK = "a." + assist.quotedSQL(srcPrefix + primaryKey);
                        if (srcType.equals("String") && !destType.equals("String")) {
                            srcFK = assist.castInteger(srcFK);
                        }
                        destKeys += destPK + " = " + srcFK;
                    }
                    String srcV = assist.quotedSQL(srcVal);
                    String destV = assist.quotedSQL(destVal);
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

        if (table.getName().matches(eldestRe)) {
            int i = 0;
            for (TableInfo bro : table.getBrothers()) {
                ++i;
                for (String colName : bro.getNonPrimaryKeys()) {
                    if (!colName.matches("(?i)^" + updateTs + "$") && BeanGenerator.isMeta(colName)) {
                        continue;
                    }
                    ColumnInfo column = bro.getColumns().get(colName);
                    s.add("    , " + SqlGenerator.getQuoted(column, bro.getName(), "c" + i));
                }
            }
        }

        s.add("FROM");
        s.add("    " + table.getName() + " a ");

        if (table.getName().matches(eldestRe)) {
            List<TableInfo> bros = table.getBrothers();
            int i = 0;
            for (TableInfo bro : bros) {
                ++i;
                s.add("    LEFT OUTER JOIN " + bro.getName() + " c" + i + " ");
                s.add("        ON 1 = 1 ");
                if (bro.getColumns().containsKey(deleteF)) {
                    s.add("        AND " + assist.nvlZero("c" + i + "." + deleteF) + " != 1 ");
                }
                if (bro.getColumns().containsKey(start)) {
                    s.add("        AND " + assist.nvlSysdate("c" + i + "." + start) + " <= " + assist.sysDate()
                            + " ");
                }
                if (bro.getColumns().containsKey(until)) {
                    s.add("        AND " + assist.dateAdd(assist.nvlSysdate("c" + i + "." + until), 1) + " > "
                            + assist.sysDate());
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
        if (table.getColumns().containsKey(deleteF)) {
            s.add("    AND " + assist.nvlZero("a." + deleteF) + " != 1 ");
        }
        if (table.getColumns().containsKey(start)) {
            s.add("    AND " + assist.nvlSysdate("a." + start) + " <= " + assist.sysDate() + " ");
        }
        if (table.getColumns().containsKey(until)) {
            s.add("    AND " + assist.dateAdd(assist.nvlSysdate("a." + until), 1) + " > " + assist.sysDate()
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
                        if (StringUtil.endsWith(orderSuffixs, column.getName())) {
                            if (orders.length() > 0) {
                                orders += "    , ";
                            } else {
                                orders += "    ";
                            }
                            orders += "a." + assist.quotedSQL(column.getName()) + "\r\n";
                        }
                    }
                }
                for (String pk : table.getPrimaryKeys()) {
                    if (orders.length() > 0) {
                        orders += "    , ";
                    } else {
                        orders += "    ";
                    }
                    orders += "a." + assist.quotedSQL(pk) + "\r\n";
                }
                if (table.getPrimaryKeys().size() > 1) {
                    for (ColumnInfo column : table.getColumns().values()) {
                        if (StringUtil.endsWith(orderSuffixs, column.getName())) {
                            if (orders.length() > 0) {
                                orders += "    , ";
                            } else {
                                orders += "    ";
                            }
                            orders += "a." + assist.quotedSQL(column.getName()) + "\r\n";
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
        if (stint.getColumns().containsKey(deleteF)) {
            sql.add("            AND " + assist.nvlZero("p." + deleteF) + " != 1 ");
        }
        if (stint.getColumns().containsKey(start)) {
            sql.add("            AND " + assist.nvlSysdate("p." + start) + " <= " + assist.sysDate()
                    + " ");
        }
        if (stint.getColumns().containsKey(until)) {
            sql.add("            AND " + assist.dateAdd(assist.nvlSysdate("p." + until), 1) + " > "
                    + assist.sysDate());
        }
        List<String> primaryKeys = new ArrayList<String>(stint.getPrimaryKeys());
        primaryKeys.remove(start);
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
            if (combo.getColumns().containsKey(deleteF)) {
                sql.add("        AND " + assist.nvlZero("c" + i + "." + deleteF) + " != 1 ");
            }
            if (combo.getColumns().containsKey(start)) {
                sql.add("        AND " + assist.nvlSysdate("c" + i + "." + start) + " <= " + assist.sysDate()
                        + " ");
            }
            if (combo.getColumns().containsKey(until)) {
                sql.add("        AND " + assist.dateAdd(assist.nvlSysdate("c" + i + "." + until), 1) + " > "
                        + assist.sysDate());
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
        if (name.matches("(?i)^" + viewDetail + "$")) {
            return;
        }

        // quoted
        String q = assist.quotedSQL(name);

        // parameters
        String cleanedKey = name.replaceAll("\\$", "_");
        String p = BeanGenerator.getRightHand(cleanedKey, column);

        // trimed
        String t = assist.trimedSQL("a." + q);

        if (StringUtil.endsWith(inputFlagSuffixs, name)) {

            // FLAG検索
            sql.add("    AND CASE WHEN " + t + " IS NULL THEN '0' ELSE TO_CHAR (a." + q + ") END IN (" + p + ") ");

        } else if (StringUtil.endsWith(inputOptionsSuffixs, name)) {

            // IN検索
            sql.add("    AND " + t + " IN (" + p + ") ");

        } else if (column.getDataType().equals("String")) {

            if (column.isPk()) {
                sql.add("    AND UPPER (" + t + ") = UPPER (" + p + "_full) ");
            }

            if (name.toUpperCase().equals(optK)) {
                //参照キーの場合は、パラメータをデータで後方一致
                sql.add("    AND UPPER (" + p + ") LIKE UPPER (" + assist.joinedSQL(new String[] { "'%'", t }) + ") ");

            } else {
                //以外の文字列は、データをパラメータで部分一致
                sql.add("    AND UPPER (" + t + ") LIKE UPPER (" + assist.joinedSQL(new String[] { "'%'", p, "'%'" })
                        + ") ");
            }

        } else {

            // INT列の場合、postgresならcastを入れる
            if (column.getTypeName().startsWith("INT")) {
                p = assist.castInteger(p);
            }

            // 以外は等値検索
            sql.add("    AND a." + q + " = " + p + " ");
        }

        // 範囲検索なら追加
        if (StringUtil.endsWith(inputRangeSuffixs, name)) {
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
        String cQuoted = assist.quotedSQL(colName);
        String aQuoted = alias + "." + cQuoted;

        String asQuoted = cQuoted;
        if (tableName != null) {
            asQuoted = assist.quotedSQL(tableName + "." + colName);
        }

        if (column.getTypeName().equals("CHAR")) {

            aQuoted = assist.trimedSQL(aQuoted) + " AS " + asQuoted;

        } else if (StringUtil.endsWith(inputDateSuffixs, colName)) {

            aQuoted = assist.date2CharSQL(aQuoted) + " AS " + asQuoted;

        } else if (StringUtil.endsWith(inputHourSuffixs, colName)) {

            aQuoted = assist.time2CharSQL(aQuoted) + " AS " + asQuoted;

        } else if (StringUtil.endsWith(inputDateTimeSuffixs, colName)) {

            aQuoted = assist.dateTime2CharSQL(aQuoted) + " AS " + asQuoted;

        } else if (StringUtil.endsWith(inputTimestampSuffixs, colName)) {

            aQuoted = assist.timestamp2CharSQL(aQuoted) + " AS " + asQuoted;

        } else {

            aQuoted = aQuoted + " AS " + asQuoted;
        }

        return aQuoted;
    }

}
