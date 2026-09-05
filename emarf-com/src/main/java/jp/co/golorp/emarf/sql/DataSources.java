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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.golorp.emarf.exception.SysError;
import jp.co.golorp.emarf.generator.BeanGenerator;
import jp.co.golorp.emarf.generator.ColumnInfo;
import jp.co.golorp.emarf.generator.TableInfo;
import jp.co.golorp.emarf.lang.StringUtil;
import jp.co.golorp.emarf.util.IgnoreCaseList;
import jp.co.golorp.emarf.util.MapList;
import jp.co.golorp.emarf.util.ResourceBundles;

/**
 * データソース管理クラス
 *
 * @author golorp
 */
public final class DataSources {

    /** logger */
    private static final Logger LOG = LoggerFactory.getLogger(DataSources.class);

    /** DataSources.properties */
    private static final ResourceBundle BUNDLE = ResourceBundles.getBundle(DataSources.class);

    /** 参照列名ペア */
    private static Set<String[]> referPairs = new LinkedHashSet<String[]>();

    /** 長兄 */
    private static String nonReferRe = "";

    /** 評価対象としないテーブル名の正規表現 */
    private static String ignoreRe;

    /** 兄弟判定で、長兄とするテーブル名の正規表現 */
    private static String eldestRe = "";

    /** 繰上りの弟モデルのテーブル名 */
    private static String fosterRe = "";

    /** 弟を設定しないテーブル名 */
    private static String youngestRe = "";

    /** 子を設定しないテーブル名 */
    private static String dinksRe = "";

    /** 親を設定しないテーブル名 */
    private static String orphansRe = "";

    /** 列評価をスキップする列名 */
    private static String columnIgnoreRe = "";

    /** ステータス区分 */
    private static String status = "";

    /** 変更理由 */
    private static String reason;

    /** VIEWで変換先を指定する列名 */
    private static String viewDetailColumn = "";

    /** ガントチャート化を判定する項目名のカラムサフィックス */
    private static Set<String[]> ganttColumns = new LinkedHashSet<String[]>();

    /** グラフ化を判定する項目名のカラムサフィックス */
    private static Set<String[]> graphColumns = new LinkedHashSet<String[]>();

    /** 数値列で自動採番しないサフィックス */
    private static String noNumberingIntRe = "";

    /** 固定長列で自動採番のサフィックス */
    private static String numberingCharRe = "";

    /** DataSourceのJNDI名 */
    private static final String JNDI_NAME = "JNDIName";

    /** タイムスタンプサフィックス */
    private static String[] inputTimestampSuffixs;

    /** 日時入力サフィックス */
    private static String[] inputDateTimeSuffixs;

    /** 日付入力サフィックス */
    private static String[] inputDateSuffixs;

    /** 時刻入力サフィックス */
    private static String[] inputHourSuffixs;
    //    /** 時間入力サフィックス */
    //    private static String[] inputTimeSuffixs;
    /** 適用日 */
    private static String tekiyoBi;

    /** 範囲指定サフィックス */
    private static String[] inputRangeSuffixs;

    /**
     * データベース名列挙子
     * @author golorp
     */
    private enum DatabaseNames {

        /** mysql. */
        mysql,

        /** oracle. */
        oracle,

        /** tibero. */
        tibero,

        /** sqlite. */
        sqlite,

        /** postgresql. */
        postgresql
    }

    /** データベース毎の処理を受け持つクラス. */
    private static DataSourcesAssist assist = null;

    /** シングルトンインスタンス */
    private static DataSource ds = null;

    /** プライベートコンストラクタ */
    private DataSources() {
    }

    /**
     * @return DataSource
     */
    public static DataSource get() {

        // インスタンス生成済みならそれを返却
        if (ds != null) {
            return ds;
        }

        // JNDIから取得してみる
        String jndiName = BUNDLE.getString(JNDI_NAME);
        try {
            setAssist(jndiName);
            Context context = new InitialContext();
            ds = (DataSource) context.lookup(jndiName);
            return ds;
        } catch (NamingException e) {
            LOG.trace(e.getMessage());
        }

        // DBCPから取得してみる
        Properties properties = new Properties();
        Enumeration<String> keys = BUNDLE.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            String value = BUNDLE.getString(key);
            properties.put(key, value);
            setAssist(value);
        }

        try {
            ds = BasicDataSourceFactory.createDataSource(properties);
            return ds;
        } catch (Exception e) {
            throw new SysError(e);
        }
    }

    /**
     * @param s データソース名を探す対象の文字列
     */
    private static void setAssist(final String s) {

        if (assist == null) {

            for (DatabaseNames databaseName : DatabaseNames.values()) {

                if (s.contains(databaseName.name())) {

                    if (databaseName == DatabaseNames.mysql) {

                        assist = new DataSourcesAssistMySQL();

                    } else if (databaseName == DatabaseNames.oracle) {

                        assist = new DataSourcesAssistOracle();

                        // oracle.sql.TIMESTAMPをoracle.sql.TIMESTAMPで扱えるようにする
                        System.setProperty("oracle.jdbc.J2EE13Compliant", "true");

                    } else if (databaseName == DatabaseNames.tibero) {

                        assist = new DataSourcesAssistTibero();

                    } else if (databaseName == DatabaseNames.postgresql) {

                        assist = new DataSourcesAssistPostgreSQL();

                    } else if (databaseName == DatabaseNames.sqlite) {

                        assist = new DataSourcesAssistSQLite();

                    }

                    break;
                }
            }
        }
    }

    /**
     * @return DataSourcesAssist
     */
    public static DataSourcesAssist getAssist() {
        if (ds == null) {
            DataSources.get();
        }
        return assist;
    }

    /**
     * プロパティファイルロード
     */
    private static void loadBundle() {

        ResourceBundle bundle = ResourceBundles.getBundle(BeanGenerator.class);

        if (bundle != null) {

            String[] pairs = bundle.getString("relation.refer.pairs").split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":");
                referPairs.add(kv);
            }

            nonReferRe = bundle.getString("relation.nonrefer.re");

            ignoreRe = bundle.getString("relation.ignore.re");

            eldestRe = bundle.getString("relation.eldest.re");

            fosterRe = bundle.getString("relation.foster.re");

            youngestRe = bundle.getString("relation.youngest.re");

            dinksRe = bundle.getString("relation.dinks.re");

            orphansRe = bundle.getString("relation.orphans.re");

            columnIgnoreRe = bundle.getString("column.ignore.re");

            status = bundle.getString("column.status");

            reason = bundle.getString("column.reason");

            viewDetailColumn = bundle.getString("view.detail");

            String[] ganttDefs = bundle.getString("gantt.columns").split(",");
            for (String ganttDef : ganttDefs) {
                String[] columns = ganttDef.split(":");
                ganttColumns.add(columns);
            }

            String[] graphDefs = bundle.getString("graph.columns").split(",");
            for (String graphDef : graphDefs) {
                String[] columns = graphDef.split(":");
                graphColumns.add(columns);
            }

            noNumberingIntRe = bundle.getString("column.int.nonumbering.re");

            numberingCharRe = bundle.getString("column.char.numbering.re");

            inputTimestampSuffixs = bundle.getString("input.timestamp.suffixs").split(",");

            inputDateTimeSuffixs = bundle.getString("input.datetime.suffixs").split(",");

            inputDateSuffixs = bundle.getString("input.date.suffixs").split(",");

            inputHourSuffixs = bundle.getString("input.hour.suffixs").split(",");

            //        inputTimeSuffixs = bundle.getString("input.time.suffixs").split(",");

            inputRangeSuffixs = bundle.getString("input.range.suffixs").split(",");

            tekiyoBi = bundle.getString("column.start").toUpperCase();
        }
    }

    /**
     * スキーマのメタ情報取得
     * @return List<TableInfo>
     */
    public static List<TableInfo> getTables() {
        loadBundle(); // 設定ファイル読み込み
        List<TableInfo> tables = new ArrayList<TableInfo>(); // テーブル情報の取得
        try { // コネクションからデータベースのメタ情報を取得
            Connection cn = Connections.get();
            DatabaseMetaData metaData = cn.getMetaData(); // テーブル情報を取得
            String schemaPattern = BUNDLE.getString("username");
            DataSources.addTables(metaData, schemaPattern, tables);
            Map<String, TableInfo> tableMap = new TreeMap<String, TableInfo>();
            for (TableInfo table : tables) {
                tableMap.put(table.getName(), table);
            }
            tables = new ArrayList<TableInfo>(); // テーブル情報の取得
            for (Entry<String, TableInfo> entry : tableMap.entrySet()) {
                tables.add(entry.getValue());
            }
            LOG.info("主キー一覧取得");
            for (TableInfo table : tables) { // テーブル毎に主キー情報を取得
                addPrimaryKeys(metaData, table);
            }
            for (TableInfo table : tables) { // テーブル毎にカラム情報を取得
                ResultSet columns = assist.getColumns(metaData, schemaPattern, table.getName());
                while (columns.next()) { // テーブルのカラム情報を取得してループ
                    String columnName = columns.getString("COLUMN_NAME");
                    if (!columnName.matches("^[\\#\\$\\-0-9A-Z\\_a-z]+$")) {
                        continue; // カラム名が合致しなければスキップ
                    }
                    if (columnName.matches(columnIgnoreRe)) {
                        continue;
                    }
                    if (columnName.toUpperCase().equals(viewDetailColumn.toUpperCase())) {
                        table.setConvView(true); // 変換ビューならtrue
                    }
                    ColumnInfo column = new ColumnInfo(); // カラム情報を追加
                    table.getColumns().put(columnName, column);
                    column.setName(columnName); // カラム物理名
                    String typeName = columns.getString("TYPE_NAME");
                    if (typeName.matches("(?i)^BPCHAR$")) { // DBデータ型：postgresqlのCHARはBPCHARなので変換
                        typeName = "CHAR";
                    }
                    column.setTypeName(typeName.toUpperCase());
                    column.setColumnSize(assist.getColumnSize(columns)); // カラムサイズ
                    // TODO postgresで確認・viewでnullを引いているケースはcastを入れる
                    //                    if (column.getColumnSize() == 0) {
                    //                        column.setColumnSize(3);
                    //                    }
                    if (StringUtil.endsWith(inputDateSuffixs, columnName)) { // 桁制限
                        column.setMaxLength(10);
                    } else if (StringUtil.endsWith(inputHourSuffixs, columnName)) {
                        column.setMaxLength(5);
                    } else if (StringUtil.endsWith(inputDateTimeSuffixs, columnName)) {
                        column.setMaxLength(19);
                    } else if (StringUtil.endsWith(inputTimestampSuffixs, columnName)) {
                        column.setMaxLength(23);
                    }
                    column.setDecimalDigits(columns.getInt("DECIMAL_DIGITS")); // 小数桁数
                    column.setNullable(columns.getInt("NULLABLE")); // NULL可否
                    column.setDefaultValue(columns.getString("COLUMN_DEF")); // デフォルト値
                    String remarks = columns.getString("REMARKS"); // カラム論理名
                    String note = "";
                    if (remarks == null || remarks.length() == 0) {
                        remarks = assist.getColumnComment(table.getName(), columnName);
                    }
                    if (remarks != null && remarks.contains(":")) {
                        int i = remarks.indexOf(":");
                        note = remarks.substring(i);
                        remarks = remarks.substring(0, i);
                    }
                    if (remarks == null || remarks.length() == 0) {
                        remarks = columnName;
                    }
                    column.setRemarks(remarks);
                    column.setNote(note);
                    if (table.getPrimaryKeys().contains(columnName)) {
                        column.setPk(true);
                    } else {
                        if (table.getUniqueIndexColumns().contains(columnName)) {
                            column.setUnique(true);
                        }
                        table.getNonPrimaryKeys().add(columnName);
                    }
                    String dataType = getDataType(column); // typeNameをjavaデータ型に変換
                    column.setDataType(dataType);
                }
                columns.close();
                if (table.getPrimaryKeys().size() == 0 && table.isView()) {
                    ColumnInfo noCol = table.getColumns().get("NO"); // viewの場合、「NO」列があれば主キーとする
                    if (noCol != null) {
                        table.getPrimaryKeys().add("NO");
                        noCol.setPk(true);
                        table.getNonPrimaryKeys().remove("NO");
                    } else { // viewの場合、主キーのいずれかに後方一致するカラムが連続する限り主キーとする
                        for (ColumnInfo column : table.getColumns().values()) {
                            boolean isPk = false;
                            for (String allKey : allKeys) {
                                if (column.getName().matches("(?i)^.*" + allKey + "$")) {
                                    table.getPrimaryKeys().add(column.getName());
                                    isPk = true;
                                }
                            }
                            if (!isPk) {
                                break;
                            }
                        }
                        for (String primaryKey : table.getPrimaryKeys()) {
                            table.getColumns().get(primaryKey).setPk(true);
                        }
                    }
                }
                LOG.info(table.getName() + " " + table.getPrimaryKeys());
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new SysError(e);
        } finally {
            Connections.close();
        }
        setGantt(tables);
        setGraph(tables);
        setRefer(tables); // 参照モデルの評価
        addBrothers(tables); // 兄弟モデルの評価
        setHistory(tables); // 履歴モデルの評価
        Iterator<TableInfo> motos = tables.iterator();
        while (motos.hasNext()) {
            TableInfo moto = motos.next();
            if (moto.isStatusFlow()) {
                Iterator<TableInfo> sakis = tables.iterator();
                while (sakis.hasNext()) {
                    TableInfo saki = sakis.next();
                    if (!saki.isStatusFlow() && !saki.isHistory() && saki.getColumns().containsKey(status)) {
                        saki.setStatusFlow(moto);
                    }
                }
                break;
            }
        }
        addCombos(tables); // 複合モデルの評価
        addChildren(tables); // 親子モデルの評価
        addDerives(tables); // 派生モデルの評価
        addMerge(tables); // 統合モデルの評価
        setReborn(tables); // 転生モデルの評価
        setReborn(tables); // 転生モデルの再評価
        addChoices(tables); // 選抜モデルの評価
        addSummaryOfs(tables); // 集約モデルの評価
        log(tables);
        return tables;
    }

    /**
     * @param tables
     */
    private static void setGantt(final List<TableInfo> tables) {
        for (TableInfo table : tables) {
            if (!table.isView()) {
                continue;
            }
            if (table.getPrimaryKeys().size() == 1) {
                for (String[] ganttColumn : ganttColumns) {
                    boolean nameColumn = false;
                    boolean startColumn = false;
                    boolean endColumn = false;
                    for (String columnName : table.getColumns().keySet()) {
                        if (StringUtil.endsWithIgnoreCase(ganttColumn[0], columnName)) {
                            nameColumn = true;
                        } else if (StringUtil.endsWithIgnoreCase(ganttColumn[1], columnName)) {
                            startColumn = true;
                        } else if (StringUtil.endsWithIgnoreCase(ganttColumn[2], columnName)) {
                            endColumn = true;
                        }
                    }
                    if (nameColumn && startColumn && endColumn) {
                        table.setGantt(true);
                        break;
                    }
                }
            }
        }
    }

    /**
     * @param tables
     */
    private static void setGraph(final List<TableInfo> tables) {
        for (TableInfo table : tables) {
            if (!table.isView()) {
                continue;
            }
            boolean isGraph = false;
            for (String[] graphColumn : graphColumns) {
                for (String columnName : graphColumn) {
                    if (table.getColumns().containsKey(columnName)) {
                        isGraph = true;
                        continue;
                    }
                    isGraph = false;
                    break;
                }
            }
            table.setGraph(isGraph);
            // グラフなら範囲指定カラムのみ必須にする（検索条件制御のため）
            if (isGraph) {
                for (ColumnInfo column : table.getColumns().values()) {
                    if (StringUtil.endsWith(inputRangeSuffixs, column.getName())) {
                        column.setNullable(0);
                    } else {
                        column.setNullable(1);
                    }
                }
            }
        }
    }

    /**
     * テーブル情報のリストに各テーブルを追加
     *
     * @param metaData メタデータ
     * @param schemaPattern スキーマ名
     * @param tables テーブル情報のリスト
     * @throws SQLException
     */
    private static void addTables(final DatabaseMetaData metaData, final String schemaPattern,
            final List<TableInfo> tables) throws SQLException {

        LOG.info("テーブル一覧取得");

        Map<String, TableInfo> tree = new TreeMap<String, TableInfo>();

        ResultSet rs = assist.getTables(metaData, schemaPattern);

        while (rs.next()) {

            String tableName = rs.getString("TABLE_NAME").toUpperCase();

            //半角文字でなければスキップ
            if (!tableName.matches("^[!-~]+$")) {
                continue;
            }

            //対象外のテーブル名ならスキップ
            if (!StringUtil.isNullOrWhiteSpace(ignoreRe) && tableName.matches(ignoreRe)) {
                continue;
            }

            // テーブル情報を追加
            TableInfo table = new TableInfo();

            // テーブル物理名
            table.setName(tableName);

            // テーブル論理名
            String remarks = rs.getString("REMARKS");
            if (remarks == null || remarks.length() == 0) {
                remarks = assist.getTableComment(tableName);
            }
            if (remarks == null || remarks.length() == 0) {
                remarks = tableName;
            }
            if (remarks.indexOf(":") > 0) {
                remarks = remarks.substring(0, remarks.indexOf(":"));
            }
            table.setRemarks(remarks);

            String tableType = rs.getString("TABLE_TYPE");
            table.setView(tableType.equals("VIEW"));

            table.setStatusFlow(!status.equals("") && tableName.matches("(?i).*" + status + ".*"));

            tree.put(tableName, table);

            LOG.info(tableName);
        }

        rs.close();

        for (TableInfo table : tree.values()) {
            tables.add(table);
        }
    }

    /** */
    private static Set<String> allKeys = new HashSet<String>();

    /**
     * テーブル情報に主キー名のリストを設定
     *
     * @param metaData メタデータ
     * @param table テーブル情報
     * @throws SQLException
     */
    private static void addPrimaryKeys(final DatabaseMetaData metaData, final TableInfo table) throws SQLException {

        List<String> primaryKeys = new ArrayList<String>();

        // テーブルの主キー情報でループ
        String schemaPattern = BUNDLE.getString("username");
        try (ResultSet rs = assist.getPrimaryKeys(metaData, schemaPattern, table.getName())) {
            while (rs.next()) {

                // 対象外のカラム名ならスキップ
                String columnName = rs.getString("COLUMN_NAME");

                if (!columnName.matches("^[0-9A-Za-z\\_\\-]+$")) {
                    continue;
                }

                if (columnName.matches(columnIgnoreRe)) {
                    continue;
                }

                // 主キー順でカラム名を退避
                short keySeq = rs.getShort("KEY_SEQ");
                while (primaryKeys.size() <= keySeq) {
                    primaryKeys.add("");
                }
                primaryKeys.set(keySeq, columnName.toUpperCase());
            }
        }

        // 一部DBではKEY_SEQが「[1]origin」なので「[0]origin」に詰め替え
        for (String primaryKey : primaryKeys) {
            if (primaryKey.length() > 0) {
                table.getPrimaryKeys().add(primaryKey);
            }
        }

        MapList uniqueIndexColumns = assist.getUniqueIndexes(table.getName());
        if (uniqueIndexColumns != null) {

            for (Map<String, Object> uniqueIndexColumn : uniqueIndexColumns) {
                String columnName = null;
                if (uniqueIndexColumn.get("COLUMN_NAME") != null) {
                    columnName = uniqueIndexColumn.get("COLUMN_NAME").toString();
                } else if (uniqueIndexColumn.get("Column_name") != null) {
                    columnName = uniqueIndexColumn.get("Column_name").toString();
                }
                if (!table.getUniqueIndexColumns().contains(columnName)) {
                    table.getUniqueIndexColumns().add(columnName);
                }
            }

            if (table.getPrimaryKeys().size() == 0) {

                // 最初にとれたユニークインデクスを主キー扱いにする
                String indexName = null;
                String preIndexName = null;
                for (Map<String, Object> uniqueIndexColumn : uniqueIndexColumns) {
                    // LOG.info(uniqueIndexColumn.get("TABLE_NAME") + "\t" + uniqueIndexColumn.get("INDEX_NAME") + "\t" + uniqueIndexColumn.get("COLUMN_POSITION") + "\t" + uniqueIndexColumn.get("COLUMN_NAME"));
                    if (uniqueIndexColumn.get("INDEX_NAME") != null) {
                        indexName = uniqueIndexColumn.get("INDEX_NAME").toString();
                    } else if (uniqueIndexColumn.get("Key_name") != null) {
                        indexName = uniqueIndexColumn.get("Key_name").toString();
                    }
                    if (preIndexName == null) {
                        preIndexName = indexName;
                    }
                    if (!preIndexName.equals(indexName)) {
                        break;
                    }
                    String columnName = null;
                    if (uniqueIndexColumn.get("COLUMN_NAME") != null) {
                        columnName = uniqueIndexColumn.get("COLUMN_NAME").toString();
                    } else if (uniqueIndexColumn.get("Column_name") != null) {
                        columnName = uniqueIndexColumn.get("Column_name").toString();
                    }
                    table.getPrimaryKeys().add(columnName);
                }
            }
        }

        for (String primaryKey : table.getPrimaryKeys()) {
            allKeys.add(primaryKey);
        }
    }

    /**
     * @param column 対象のカラム情報
     * @return javaデータ型に変換した文字列
     */
    private static String getDataType(final ColumnInfo column) {

        String typeName = column.getTypeName().toUpperCase();
        String dataType = typeName;

        if (typeName.startsWith("INT")
                || (typeName.equals("NUMBER") && column.getColumnSize() <= 10 && column.getDecimalDigits() == 0)) {

            dataType = "Integer";

            if (column.isPk()) {
                if (!column.getName().matches(noNumberingIntRe)) {
                    column.setNumbering(true);
                }
            }

        } else if (typeName.equals("DECIMAL") || typeName.equals("DOUBLE") || typeName.equals("NUMBER")
                || typeName.equals("NUMERIC")) {

            dataType = "java.math.BigDecimal";

            if (column.isPk() && column.getDecimalDigits() == 0) {
                if (!column.getName().matches(noNumberingIntRe)) {
                    column.setNumbering(true);
                }
            }

        } else if (StringUtil.endsWith(inputDateSuffixs, column.getName())) {

            dataType = "java.time.LocalDate";

        } else if (StringUtil.endsWith(inputHourSuffixs, column.getName())) {

            dataType = "java.time.LocalTime";

        } else if (typeName.equals("DATE") || typeName.equals("TIME") || typeName.equals("DATETIME")
                || typeName.indexOf("TIMESTAMP") >= 0) {

            dataType = "java.time.LocalDateTime";

        } else {

            dataType = "String";

            if (typeName.equals("CHAR")) {
                if (column.isPk()) {
                    if (column.getName().matches(numberingCharRe)) {
                        column.setNumbering(true);
                    }
                }
            }
        }

        return dataType;
    }

    /**
     * マスタテーブル（参照先）から、植え付け先（参照元）を探す
     *
     * @param tables テーブル情報のリスト
     */
    private static void setRefer(final List<TableInfo> tables) {

        LOG.debug("【Refer】");

        // 参照先としてループ
        Iterator<TableInfo> sakis = tables.iterator();
        while (sakis.hasNext()) {
            TableInfo saki = sakis.next();

            // 適用日を除く、主キーがなければスキップ
            List<String> sakiKeys = new ArrayList<String>(saki.getPrimaryKeys());
            sakiKeys.remove(tekiyoBi);
            if (sakiKeys.size() == 0) {
                continue;
            }

            //            // 履歴モデル ならスキップ
            //            if (saki.isHistory()) {
            //                continue;
            //            }

            //            // 同じキーの参照モデルが複数存在するならスキップ
            //            if (saki.getBrothers().size() > 0) {
            //                boolean isReferYng = false;
            //                for (TableInfo yng : saki.getBrothers()) {
            //                    if (isRefer(yng)) {
            //                        isReferYng = true;
            //                        break;
            //                    }
            //                }
            //                if (isReferYng) {
            //                    continue;
            //                }
            //            }

            // 参照先としないテーブル
            if (saki.getName().matches(nonReferRe)) {
                continue;
            }

            // どの参照ペアにもヒットしなければスキップ
            if (!isMatchReferPairs(saki)) {
                continue;
            }

            // 参照先マスタのユニークキー情報
            String sakiLastKey = sakiKeys.get(sakiKeys.size() - 1);

            // 参照元トランとしてループ
            Iterator<TableInfo> motos = tables.iterator();
            while (motos.hasNext()) {
                TableInfo moto = motos.next();

                // 適用日が主キーに含まれる場合は除去
                List<String> motoKeys = new ArrayList<String>(moto.getPrimaryKeys());
                motoKeys.remove(tekiyoBi);

                // 参照元トランのカラム情報でループ（比較元のユニークキーがあれば参照テーブルリストに追加）
                for (Entry<String, ColumnInfo> e : moto.getColumns().entrySet()) {
                    String motoColName = e.getKey();
                    ColumnInfo motoCol = e.getValue();

                    // 参照テーブル自体の主キー自体ならスキップ（親ID参照もあるため）
                    if (moto == saki && motoCol.isPk()) {
                        continue;
                    }

                    if (moto == saki && (saki.isView() || saki.isStatusFlow())) {
                        continue;
                    }

                    if (motoCol.getRefer() != null) {
                        continue;
                    }

                    if (motoColName.matches("(?i)^.*" + sakiLastKey + "$")) {
                        // 参照元カラム名と参照先最終キーが末尾で一致する場合

                        //参照先の全主キーに接頭辞を加えたカラム名が全て存在すれば参照元として認める
                        String prefix = motoColName.replaceAll("(?i)" + sakiLastKey + "$", "");
                        boolean isRefer = true;
                        for (String sakiKey : sakiKeys) {
                            String motoKey = prefix + sakiKey;
                            if (!moto.getColumns().containsKey(motoKey)) {
                                isRefer = false;
                                break;
                            }
                        }
                        if (!isRefer) {
                            continue;
                        }

                        // 既に主キー数のより少ない参照モデルを設定済みならスキップ
                        if (motoCol.getRefer() != null
                                && motoCol.getRefer().getPrimaryKeys().size() < sakiKeys.size()) {
                            continue;
                        }

                        LOG.debug("    " + saki.getName() + " from " + moto.getName() + "." + motoCol.getName());
                        motoCol.setRefer(saki);
                        saki.setRefer(true);

                        // 参照元の複合キーの最終キーなら、参照サブ画面で選択するため採番フラグをオフ
                        if (motoKeys.size() > 1 && motoColName.equals(motoKeys.get(motoKeys.size() - 1))) {
                            motoCol.setNumbering(false);
                        }

                    } else if ((saki.isView() || saki.isStatusFlow()) && !moto.isView() && !moto.isStatusFlow()) {
                        // 参照先候補がビューで、参照元候補がテーブルの場合

                        String[] sakiSuffixs = saki.getName().split("_");
                        String sakiSuffix = sakiSuffixs[sakiSuffixs.length - 1];

                        if (motoColName.matches("(?i)^" + sakiSuffix + ".+$")) {
                            // 参照先ビュー名から始まるカラム名の場合

                            // 既に主キー数のより少ない参照モデルを設定済みならスキップ
                            if (motoCol.getRefer() != null
                                    && motoCol.getRefer().getPrimaryKeys().size() < sakiKeys.size()) {
                                continue;
                            }

                            LOG.debug("    " + saki.getName() + " from " + moto.getName() + "." + motoCol.getName());
                            motoCol.setRefer(saki);
                            saki.setRefer(true);

                            // 参照元の複合キーの最終キーなら、参照サブ画面で選択するため採番フラグをオフ
                            if (motoKeys.size() > 1 && motoColName.equals(motoKeys.get(motoKeys.size() - 1))) {
                                motoCol.setNumbering(false);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 参照ペアのいずれかに該当するか評価
     *
     * @param table
     * @return boolean
     */
    public static boolean isMatchReferPairs(final TableInfo table) {

        List<String> mstKeys = table.getPrimaryKeys();

        for (String[] referPair : referPairs) {
            String[] keySufs = referPair[0].split("&");

            // マスタキーが一つでも合致しなければ次の参照ペアに移動
            boolean isReferKeys = true;
            String lastKeySuf = null;
            for (String mstKey : mstKeys) {
                if (!StringUtil.endsWith(keySufs, mstKey)) {
                    isReferKeys = false;
                    break;
                }
            }
            if (!isReferKeys) {
                continue;
            }

            // この参照ペアにマスタキーが全て一致した

            String lastKey = mstKeys.get(mstKeys.size() - 1);
            lastKeySuf = keySufs[keySufs.length - 1];
            if (lastKey.matches("(?i).*" + lastKeySuf + "$")) {

                // 参照値があるか
                String referMei = lastKey.replaceAll("(?i)" + lastKeySuf + "$", referPair[1]);
                for (String colName : table.getColumns().keySet()) {
                    if (colName.matches("(?i)^" + referMei + "$")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 複合先の複合キー群から参照マスタを探す
     *
     * @param tables
     */
    private static void addCombos(final List<TableInfo> tables) {

        LOG.debug("【Combo】");

        for (TableInfo saki : tables) {

            if (saki.isView() || saki.isStatusFlow()) {
                continue;
            }

            if (saki.isHistory()) {
                continue;
            }

            // 統合先の主キー内の参照モデルが１以下ならスキップ
            Set<TableInfo> combos = new LinkedHashSet<TableInfo>();
            for (String sakiPrimaryKey : saki.getPrimaryKeys()) {
                ColumnInfo pkCol = saki.getColumns().get(sakiPrimaryKey);
                if (pkCol.getRefer() != null) {
                    TableInfo refer = pkCol.getRefer();
                    if (!combos.contains(refer) && !refer.isView() && !refer.isStatusFlow()) {
                        combos.add(refer);
                    }
                }
            }
            if (combos.size() <= 1) {
                continue;
            }

            // 統合モデルに設定
            for (TableInfo combo : combos) {
                LOG.debug("    " + saki.getName() + " : " + combo.getName());
                saki.getComboInfos().add(combo);
            }

            /*
             * 適用日を除く最終キー以外を
             * 参照ダイアログで採用する制約モデルにする
             */

            // ２モデルの組み合わせでなければスキップ（３モデル以上になると複雑すぎる）
            if (saki.getComboInfos().size() != 2) {
                continue;
            }

            // 適用日はスキップ
            List<String> primaryKeys = new ArrayList<String>(saki.getPrimaryKeys());
            if (primaryKeys.get(primaryKeys.size() - 1).equals(tekiyoBi)) {
                primaryKeys.remove(tekiyoBi);
            }

            // 主キーに参照キー以外が含まれるならスキップ
            boolean isStint = true;
            for (String primaryKey : primaryKeys) {
                if (saki.getColumns().get(primaryKey).getRefer() == null) {
                    isStint = false;
                    break;
                }
            }

            if (isStint) {
                TableInfo combo = saki.getComboInfos().get(saki.getComboInfos().size() - 1);
                LOG.debug("        " + combo.getName() + " stint by " + saki.getName());
                combo.setStintInfo(saki);
            }
        }
    }

    /**
     * 各テーブル情報に弟モデルを追加
     *
     * @param tables テーブル情報のリスト
     */
    private static void addBrothers(final List<TableInfo> tables) {

        LOG.debug("【Brother】");

        // 比較元としてループ
        Iterator<TableInfo> elds = tables.iterator();
        while (elds.hasNext()) {
            TableInfo eld = elds.next();

            if (eld.isView() || eld.isStatusFlow()) {
                continue;
            }

            // 比較元に主キーがなければスキップ
            if (eld.getPrimaryKeys().size() == 0) {
                continue;
            }

            // 里子モデルならスキップ
            if (eld.getName().matches(fosterRe)) {
                continue;
            }

            // 末弟モデルならスキップ
            if (eld.getName().matches(youngestRe)) {
                continue;
            }

            // 比較元の主キーに適用日が含まれているか（比較先ごとに行う）
            boolean isTekiyoBiEld = eld.getPrimaryKeys().contains(tekiyoBi);

            // 比較先としてループ
            Iterator<TableInfo> yngs = tables.iterator();
            while (yngs.hasNext()) {
                TableInfo yng = yngs.next();

                if (yng.isView() || yng.isStatusFlow()) {
                    continue;
                }

                // 同じテーブルならスキップ
                if (eld == yng) {
                    continue;
                }

                // 比較先に主キーがなければスキップ
                if (yng.getPrimaryKeys().size() == 0) {
                    continue;
                }

                // 里子モデルならスキップ
                if (yng.getName().matches(fosterRe)) {
                    continue;
                }

                // 長兄モデルならスキップ
                if (yng.getName().matches(eldestRe)) {
                    continue;
                }

                //                // 兄に履歴モデルがないのに弟に履歴モデルがある場合はスキップ
                //                if (moto.getHistory() == null && saki.getHistory() != null) {
                //                    continue;
                //                }

                // 兄弟ともに適用日が主キーに含まれる場合は評価から除外
                List<String> eldKeys = new ArrayList<String>(eld.getPrimaryKeys());
                List<String> yngKeys = new ArrayList<String>(yng.getPrimaryKeys());
                boolean isTekiyoBiYng = yngKeys.contains(tekiyoBi);
                if (isTekiyoBiEld && isTekiyoBiYng) {
                    eldKeys.remove(tekiyoBi);
                    yngKeys.remove(tekiyoBi);
                }

                // 主キーが合致しなければスキップ
                String eldKeyCsv = eldKeys.toString().replaceAll("[\\[\\]]", "");
                String yngKeyCsv = yngKeys.toString().replaceAll("[\\[\\]]", "");
                if (!eldKeyCsv.equals(yngKeyCsv)) {
                    continue;
                }

                // 兄に弟を追加、弟に弟フラグを設定
                LOG.debug("    " + eld.getName() + " and " + yng.getName());
                eld.getBrothers().add(yng);
                yng.setBrother(true);
            }
        }
    }

    /**
     * 各テーブル情報に履歴モデルを設定
     *
     * @param tables テーブル情報のリスト
     */
    private static void setHistory(final List<TableInfo> tables) {

        LOG.debug("【History】");

        // 履歴元としてループ
        Iterator<TableInfo> motos = tables.iterator();
        while (motos.hasNext()) {
            TableInfo moto = motos.next();

            if (moto.isView() || moto.isStatusFlow()) {
                continue;
            }

            // 主キーがなければスキップ
            if (moto.getPrimaryKeys().size() == 0) {
                continue;
            }

            String motoKeyCsv = moto.getPrimaryKeys().toString().replaceAll("[\\[\\]]", "");
            String motoColCsv = moto.getNonPrimaryKeys().toString().replaceAll("[\\[\\]]", "");

            // 比較先でループ
            Iterator<TableInfo> sakis = tables.iterator();
            while (sakis.hasNext()) {
                TableInfo saki = sakis.next();

                if (saki.isView() || saki.isStatusFlow()) {
                    continue;
                }

                // 比較元と比較先が同じならスキップ
                if (moto == saki) {
                    continue;
                }

                // 比較先テーブル名が比較元テーブル名に前方一致しないならスキップ
                if (!saki.getName().matches("(?i)^" + moto.getName() + ".+$")) {
                    continue;
                }

                // 変更理由列は比較対象から除外
                List<String> sakiNonPrimaryKeys = new IgnoreCaseList<String>(saki.getNonPrimaryKeys());
                if (!StringUtil.isNullOrWhiteSpace(reason) && sakiNonPrimaryKeys.contains(reason)) {
                    sakiNonPrimaryKeys.remove(reason);
                }

                String sakiKeyCsv = saki.getPrimaryKeys().toString().replaceAll("[\\[\\]]", "");
                String sakiColCsv = sakiNonPrimaryKeys.toString().replaceAll("[\\[\\]]", "");

                // 比較先の主キーの最初から、比較元の主キーと合致しなければスキップ
                if (!sakiKeyCsv.startsWith(motoKeyCsv)) {
                    continue;
                }

                // 比較先の主キー数が、比較元の主キー数＋１でなければスキップ
                if (saki.getPrimaryKeys().size() != moto.getPrimaryKeys().size() + 1) {
                    continue;
                }

                // キー以外が合致しなければスキップ
                if (!motoColCsv.equals(sakiColCsv)) {
                    continue;
                }

                // 比較先の最終キーが採番キーでないか文字列ならスキップ
                String sakiLastKey = saki.getPrimaryKeys().get(saki.getPrimaryKeys().size() - 1);
                ColumnInfo sakiLastPK = saki.getColumns().get(sakiLastKey);
                if (!sakiLastPK.isNumbering() || sakiLastPK.getTypeName().equals("CHAR")) {
                    continue;
                }

                LOG.debug("    " + moto.getName() + " to " + saki.getName());
                saki.setHistory(true);
                moto.setHistory(saki);
            }
        }
    }

    /**
     * 各テーブル情報に子モデルを設定
     *
     * @param tables テーブル情報のリスト
     */
    private static void addChildren(final List<TableInfo> tables) {

        LOG.debug("【Children】");

        // 親モデルとしてループ
        Iterator<TableInfo> oyas = tables.iterator();
        while (oyas.hasNext()) {
            TableInfo oya = oyas.next();

            if (oya.isView() || oya.isStatusFlow()) {
                continue;
            }

            // 子を設定しないならスキップ
            if (oya.getName().matches(dinksRe)) {
                continue;
            }

            // 主キーがなければスキップ
            if (oya.getPrimaryKeys().size() == 0) {
                continue;
            }

            // 主キーに適用日があるか
            boolean isOyaTekiyoBi = oya.getPrimaryKeys().contains(tekiyoBi);

            // テーブル情報でループ（比較先）
            Iterator<TableInfo> kos = tables.iterator();
            while (kos.hasNext()) {
                TableInfo ko = kos.next();

                if (ko.isView() || ko.isStatusFlow()) {
                    continue;
                }

                // 比較元と同じならスキップ
                if (oya == ko) {
                    continue;
                }

                // 親を設定しないならスキップ
                if (ko.getName().matches(orphansRe)) {
                    continue;
                }

                // 履歴テーブルならスキップ
                if (ko.isHistory()) {
                    continue;
                }

                // // 親に履歴モデルがないのに子に履歴モデルがある場合はスキップ
                // if (oya.getHistory() == null && ko.getHistory() != null) {
                //     continue;
                // }

                // // 親が参照モデルで子がトラン、およびその逆ならスキップ
                // if ((oya.isRefer() && !ko.isRefer()) || (!oya.isRefer() && ko.isRefer())) {
                //     continue;
                // }

                // 親が参照モデルなら子のテーブル名が前方一致しなければスキップ
                // （「MCM_ITEM」について「MCM_ITEM_KOSE」は親子にしたいが「MPR_MANU」は親子にしたくない）
                // （「MLI_WH_LOC_ITEM」について「TLI_TANA」を親子にしたくない）
                if (oya.isRefer() && !ko.getName().startsWith(oya.getName())) {
                    continue;
                }

                boolean isKoTekiyoBi = ko.getPrimaryKeys().contains(tekiyoBi);

                // 親にも主キーに適用日があれば、評価から除外
                List<String> oyaKeys = new ArrayList<String>(oya.getPrimaryKeys());
                List<String> koKeys = new ArrayList<String>(ko.getPrimaryKeys());
                if (isOyaTekiyoBi && isKoTekiyoBi) {
                    oyaKeys.remove(tekiyoBi);
                    koKeys.remove(tekiyoBi);
                }
                // 親には適用日がないが子から適用日を外すと親子かもしれない場合
                if (!isOyaTekiyoBi && isKoTekiyoBi && oyaKeys.size() + 2 == koKeys.size()) {
                    koKeys.remove(tekiyoBi);
                }

                // 適用日を処置した結果、主キーがなければスキップ
                if (koKeys.size() == 0) {
                    continue;
                }

                // 子の主キーの先頭から、親の主キーに合致しなければスキップ
                String oyaKeyCsv = oyaKeys.toString().replaceAll("[\\[\\]]", "");
                String koKeyCsv = koKeys.toString().replaceAll("[\\[\\]]", "");
                if (!koKeyCsv.startsWith(oyaKeyCsv + ",")) {
                    continue;
                }

                // 子の主キー数が「親の主キー数＋１」でなければスキップ
                if (koKeys.size() != oyaKeys.size() + 1) {
                    continue;
                }

                //                // 最終キーが参照モデルならスキップ
                //                String koLastKey = koKeys.get(koKeys.size() - 1);
                //                if (ko.getColumns().get(koLastKey).getRefer() != null) {
                //                    continue;
                //                }

                //                // 設定済みの親モデルに子モデル名が後方一致するならスキップ（親を先勝ちで１つに絞っている）
                //                if (ko.getParents().size() == 1) {
                //                    TableInfo orgParent = ko.getParents().get(0);
                //                    if (ko.getName().startsWith(orgParent.getName())) {
                //                        continue;
                //                    }
                //                }

                // 親モデルの子リストに追加・子モデルの親リストに追加
                LOG.debug("    " + oya.getName() + " has " + ko.getName());
                oya.getChildren().add(ko);
                ko.getParents().add(oya);

                //                // 親子設定した場合、兄弟モデルを消し込み（親が適用日なし、子が適用日ありで、兄弟モデルに誤登録されている可能性があるため）
                //                if (oya.getBrothers().contains(ko)) {
                //                    LOG.debug("    Cancel brother" + oya.getName() + " and " + ko.getName());
                //                    oya.getBrothers().remove(ko);
                //                }
                //                if (ko.getBrothers().contains(oya)) {
                //                    LOG.debug("    Cancel brother" + ko.getName() + " and " + oya.getName());
                //                    ko.getBrothers().remove(oya);
                //                }
            }
        }
    }

    /**
     * 一旦、全ての外部キーについて、派生元から派生先を探す
     *
     * @param tables テーブル情報のリスト
     */
    private static void addDerives(final List<TableInfo> tables) {

        LOG.debug("【Derives】");

        // 派生元としてループ
        Iterator<TableInfo> motos = tables.iterator();
        while (motos.hasNext()) {
            TableInfo moto = motos.next();

            if (moto.getPrimaryKeys().size() == 0) {
                continue;
            }

            // ビュー・参照モデル・兄弟モデル・履歴モデル は派生しない
            if (moto.isView() || moto.isStatusFlow() || moto.isRefer() || moto.isBrother() || moto.isHistory()) {
                continue;
            }

            // 参照モデルの子モデルは転生しない
            boolean isOyaRefer = false;
            for (TableInfo oya : moto.getParents()) {
                if (oya.isRefer()) {
                    isOyaRefer = true;
                    break;
                }
            }
            if (isOyaRefer) {
                continue;
            }

            // 派生先として、テーブル情報をループ
            Iterator<TableInfo> sakis = tables.iterator();
            while (sakis.hasNext()) {
                TableInfo saki = sakis.next();

                // ビュー・参照モデル・履歴モデル は派生先にしない
                if (saki.isHistory() || saki.isView() || saki.isStatusFlow() || saki.isRefer()) {
                    continue;
                }

                if (saki.getPrimaryKeys().size() == 0) {
                    continue;
                }

                // 派生元の主キーでループして、合致する派生先の外部キーを取得
                Set<String> sakiFKs = new LinkedHashSet<String>();
                for (ColumnInfo sakiCol : saki.getColumns().values()) {
                    for (String motoKey : moto.getPrimaryKeys()) {
                        ColumnInfo motoCol = moto.getColumns().get(motoKey);
                        if (!sakiCol.isPk() && isMatchColDef(motoCol, sakiCol)) {
                            sakiFKs.add(sakiCol.getName());
                        }
                    }
                }

                // 転生元の主キーと、転生先外部キーの数が一致しなければスキップ
                if (moto.getPrimaryKeys().size() != sakiFKs.size()) {
                    continue;
                }

                String motoLastPK = moto.getPrimaryKeys().get(moto.getPrimaryKeys().size() - 1);

                LOG.debug("    " + moto.getName() + " to " + saki.getName() + " " + sakiFKs);
                moto.getDeriveTos().add(saki);
                saki.getDeriveFroms().add(moto);

                String sakiLastFK = null;
                for (String sakiKey : sakiFKs) {
                    sakiLastFK = sakiKey;
                    ColumnInfo sakiCol = saki.getColumns().get(sakiKey);
                    if (sakiCol.getNullable() != 1) {
                        // 必須なら上書き
                        sakiCol.setDeriveFrom(moto);
                        // // 参照モデルを未設定なら、派生先列の参照先に、派生元を設定
                        // for (String motoKey : moto.getPrimaryKeys()) {
                        //     if (saki.getColumns().get(motoKey).getRefer() == null) {
                        //         saki.getColumns().get(motoKey).setRefer(moto);
                        //     }
                        // }
                    } else if (sakiKey.matches("(?i)^" + motoLastPK + "$")) {
                        // 必須でない場合は派生元の最終キーと一致するなら上書き
                        sakiCol.setDeriveFrom(moto);
                        // // 参照モデルを未設定なら、派生先列の参照先に、派生元を設定
                        // for (String motoKey : moto.getPrimaryKeys()) {
                        //     if (saki.getColumns().get(motoKey).getRefer() == null) {
                        //         saki.getColumns().get(motoKey).setRefer(moto);
                        //     }
                        // }
                    }
                }

                // 派生先の外部キーのうち、最後のキーにも派生元を設定
                ColumnInfo sakiCol = saki.getColumns().get(sakiLastFK);
                sakiCol.setDeriveFrom(moto);

                // [売上]の派生元は、[受注]と[受注明細]となるが、親の[受注]は、派生元から外したいが参照は可能にしたい。
            }
        }
    }

    /**
     * 統合先から、統合元を探す
     *
     * @param tables テーブル情報のリスト
     */
    private static void addMerge(final List<TableInfo> tables) {

        LOG.debug("【Merge】");

        // 統合先としてループ
        Iterator<TableInfo> sakis = tables.iterator();
        while (sakis.hasNext()) {
            TableInfo saki = sakis.next();

            if (saki.isView() || saki.isStatusFlow()) {
                continue;
            }

            // 派生元が１つ以下ならスキップ
            if (saki.getDeriveFroms().size() <= 1) {
                continue;
            }

            // 転生キーがある場合は転生元を取得
            Set<TableInfo> merges = new LinkedHashSet<TableInfo>();
            for (ColumnInfo sakiCol : saki.getColumns().values()) {
                if (sakiCol.getDeriveFrom() != null && sakiCol.getNullable() != 1) {
                    merges.add(sakiCol.getDeriveFrom());
                }
            }

            // 統合モデルでなければスキップ
            if (merges.size() <= 1) {
                continue;
            }

            for (TableInfo moto : merges) {

                if (moto.isView() || moto.isStatusFlow()) {
                    continue;
                }

                // マスタ同士かトラン同士でなければスキップ
                if (saki.isRefer() != moto.isRefer()) {
                    continue;
                }

                LOG.debug("    " + saki.getName() + " from " + moto.getName() + " " + moto.getPrimaryKeys());

                // 派生情報を消し込み
                moto.getDeriveTos().remove(saki);
                saki.getDeriveFroms().remove(moto);
                for (ColumnInfo sakiCol : saki.getColumns().values()) {
                    if (sakiCol.getDeriveFrom() == moto) {
                        sakiCol.setDeriveFrom(null);
                    }
                }

                // 統合情報を書き込み
                moto.setMergeTo(saki);
                saki.getMergeFroms().add(moto);

                // 参照モデルはやめる
                // for (String motoKey : moto.getPrimaryKeys()) {
                //     saki.getColumns().get(motoKey).setRefer(moto);
                // }
            }
        }
    }

    /**
     * 転生元から、転生先を探す
     *
     * @param tables テーブル情報のリスト
     */
    private static void setReborn(final List<TableInfo> tables) {

        LOG.debug("【Reborn】");

        // 転生元としてループ
        Iterator<TableInfo> motos = tables.iterator();
        while (motos.hasNext()) {
            TableInfo moto = motos.next();

            if (moto.isView() || moto.isStatusFlow()) {
                continue;
            }

            // ユニークな必須の転生先情報
            TableInfo saki = null;
            Set<String> sakiFKs = null;

            // 転生先候補数
            int reborns = 0;

            // 派生先でループ
            for (TableInfo deriveTo : moto.getDeriveTos()) {

                // ビューか決裁フローならスキップ
                if (deriveTo.isView() || deriveTo.isStatusFlow()) {
                    continue;
                }

                // // 派生先に派生元が複数あるならスキップ
                // if (deriveTo.getDeriveFroms().size() > 1) {
                //     continue;
                // }

                // 派生先に親が居るならスキップ
                if (deriveTo.getParents().size() > 0) {
                    continue;
                }

                sakiFKs = new HashSet<String>();

                // 転生先の外部キーがNULL可なら次の転生先を評価
                boolean isNullable = false;
                for (String motoPK : moto.getPrimaryKeys()) {
                    ColumnInfo sakiFK = deriveTo.getColumns().get(motoPK);
                    if (sakiFK.getNullable() == 1) {
                        isNullable = true;
                    }
                    // 派生元が転生先になっていて、派生先が転生元になっているなら、当該派生関係も転生関係とする（２回目で機能する）
                    if (isNullable && moto.getRebornFrom() != null && deriveTo.getRebornTo() != null) {
                        if (deriveTo.getDeriveFroms().size() == 1) {
                            isNullable = false;
                        }
                    }
                    sakiFKs.add(sakiFK.getName());
                }
                if (isNullable) {
                    continue;
                }

                // 転生先として退避
                saki = deriveTo;
                ++reborns;
            }

            // 退避した派生先が１件でない場合は次の転生元を評価
            if (reborns != 1) {
                continue;
            }

            //            // 必須の派生先が１つでなければスキップ
            //            if (moto.getDeriveTos().size() != 1) {
            //                continue;
            //            }
            //
            //            // 派生先の外部キーがNULL可ならスキップ
            //            boolean isReborn = true;
            //            Set<String> sakiKeys = new HashSet<String>();
            //            TableInfo saki = moto.getDeriveTos().get(0);
            //            for (ColumnInfo sakiCol : saki.getColumns().values()) {
            //                if (sakiCol.getDeriveFrom() == null) {
            //                    continue;
            //                }
            //                if (sakiCol.getDeriveFrom() == moto) {
            //                    if (sakiCol.getNullable() == 1) {
            //                        isReborn = false;
            //                        break;
            //                    }
            //                    sakiKeys.add(sakiCol.getName());
            //                }
            //            }
            //            if (!isReborn) {
            //                // 派生元が他の転生先で、派生先が他への転生元なら、当該派生関係も転生関係とする
            //                if (moto.getRebornFrom() == null || saki.getRebornTo() == null) {
            //                    continue;
            //                }
            //            }

            TableInfo preRebornFrom = saki.getRebornFrom();

            if (preRebornFrom != null) {
                if (moto.getPrimaryKeys().size() < preRebornFrom.getPrimaryKeys().size()) {
                    continue;
                }
                preRebornFrom.setRebornTo(null);
                saki.setRebornFrom(null);
            }

            LOG.debug("    " + moto.getName() + " to " + saki.getName() + " " + sakiFKs);

            // 派生情報を消し込み
            moto.getDeriveTos().remove(saki);
            saki.getDeriveFroms().remove(moto);
            for (ColumnInfo sakiCol : saki.getColumns().values()) {
                if (sakiCol.getDeriveFrom() == moto) {
                    sakiCol.setDeriveFrom(null);
                }
            }

            // 転生情報を書き込み
            moto.setRebornTo(saki);
            saki.setRebornFrom(moto);
            for (String sakiKey : sakiFKs) {
                saki.getColumns().get(sakiKey).setReborn(true);
            }
        }
    }

    /**
     * @param src
     * @param dest
     * @return boolean
     */
    public static boolean isMatchColDef(final ColumnInfo src, final ColumnInfo dest) {

        // データ型が異なる
        if (!src.getTypeName().equals(dest.getTypeName())) {
            return false;
        }

        // データサイズが異なる
        if (src.getColumnSize() != dest.getColumnSize()) {
            return false;
        }

        // 小数桁数が異なる
        if (src.getDecimalDigits() != dest.getDecimalDigits()) {
            return false;
        }

        // 比較先カラム名が比較元カラム名と合致しない
        if (!dest.getName().matches("(?i)^" + src.getName() + "$")) {
            return false;
        }

        return true;
    }

    /**
     * 選抜先から、選抜元を探す
     *
     * @param tables テーブル情報のリスト
     */
    private static void addChoices(final List<TableInfo> tables) {

        LOG.debug("【Choices】");

        // 選抜先として、テーブル情報をループ
        Iterator<TableInfo> sakis = tables.iterator();
        while (sakis.hasNext()) {
            TableInfo saki = sakis.next();

            if (saki.isView() || saki.isStatusFlow()) {
                continue;
            }

            // 派生元が１つ以下ならスキップ
            if (saki.getDeriveFroms().size() <= 1) {
                continue;
            }

            // 派生キーがある場合は派生元を取得
            List<TableInfo> choises = new ArrayList<TableInfo>();
            for (ColumnInfo sakiCol : saki.getColumns().values()) {
                if (sakiCol.getDeriveFrom() != null && sakiCol.getNullable() == 1) {
                    TableInfo choise = sakiCol.getDeriveFrom();
                    // 派生元に転生先も統合先もなければ取得
                    if (choise.getRebornTo() == null && choise.getMergeTo() == null) {
                        choises.add(choise);
                    }
                }
            }

            // 選抜モデルでなければスキップ
            if (choises.size() <= 1) {
                continue;
            }

            // 選択肢でループ
            for (TableInfo moto : choises) {

                if (moto.isView() || moto.isStatusFlow()) {
                    continue;
                }

                LOG.debug("    " + saki.getName() + " from " + moto.getName() + " " + moto.getPrimaryKeys());

                //                // 派生情報を消し込み
                //                moto.getDeriveTos().remove(saki);
                //                saki.getDeriveFroms().remove(moto);
                //                for (ColumnInfo sakiCol : saki.getColumns().values()) {
                //                    if (sakiCol.getDeriveFrom() == moto) {
                //                        sakiCol.setDeriveFrom(null);
                //                    }
                //                }

                // 選抜情報を書き込み
                moto.getChoosers().add(saki);
                saki.getChoices().add(moto);

                // 参照モデルはやめる
                // for (String motoKey : moto.getPrimaryKeys()) {
                //     saki.getColumns().get(motoKey).setRefer(moto);
                // }
            }
        }
    }

    /**
     * 集約先から、集約元を探す
     *
     * @param tables テーブル情報のリスト
     */
    private static void addSummaryOfs(final List<TableInfo> tables) {

        LOG.debug("【Summary】");

        // 派生元を集約先としてループ
        Iterator<TableInfo> sakis = tables.iterator();
        while (sakis.hasNext()) {
            TableInfo saki = sakis.next();

            if (saki.isView() || saki.isStatusFlow()) {
                continue;
            }

            // 子モデルはスキップ
            if (saki.getParents().size() > 0) {
                continue;
            }

            // 派生先（集約元）がなければスキップ
            if (saki.getDeriveTos().size() == 0) {
                continue;
            }

            // 派生元（集約先）が他の転生先ならスキップ
            if (saki.getRebornFrom() != null) {
                continue;
            }

            // 派生先（集約元）に集約先向けの派生キーがある場合は派生元（集約先）を取得
            Set<TableInfo> sums = new LinkedHashSet<TableInfo>();
            for (TableInfo moto : saki.getDeriveTos()) {
                for (ColumnInfo motoCol : moto.getColumns().values()) {
                    if (motoCol.getDeriveFrom() == saki && motoCol.getNullable() == 1) {
                        sums.add(moto);
                    }
                }
            }

            for (TableInfo moto : sums) {

                if (moto.isView() || moto.isStatusFlow()) {
                    continue;
                }

                // 集約元に転生先があるならスキップ
                if (moto.getRebornTo() != null) {
                    continue;
                }

                // 集約元に派生先があるならスキップ
                if (moto.getDeriveTos().size() > 0) {
                    continue;
                }

                // 集約元が選抜先に登録済みならスキップ
                if (saki.getChoosers().contains(moto)) {
                    continue;
                }

                LOG.debug("    " + moto.getName() + " to " + saki.getName() + " " + saki.getPrimaryKeys());

                // 派生情報を消し込み
                saki.getDeriveTos().remove(moto);
                moto.getDeriveFroms().remove(saki);
                for (ColumnInfo motoCol : moto.getColumns().values()) {
                    if (motoCol.getDeriveFrom() == saki) {
                        motoCol.setDeriveFrom(null);
                    }
                }

                // 集約情報を書き込み
                moto.setSummaryTo(saki);
                saki.getSummaryOfs().add(moto);
                for (String sakiKey : saki.getPrimaryKeys()) {
                    moto.getColumns().get(sakiKey).setSummary(true);
                }
            }
        }
    }

    /**
     * @param tableInfos
     */
    private static void log(final List<TableInfo> tableInfos) {

        for (TableInfo table : tableInfos) {

            LOG.info("");

            String tablelog = "■" + table.getName();
            if (table.getPrimaryKeys().size() > 0) {
                tablelog += " : " + table.getPrimaryKeys();
            }
            LOG.info(tablelog);

            Map<String, TableInfo> refers = new LinkedHashMap<String, TableInfo>();
            for (ColumnInfo column : table.getColumns().values()) {
                if (column.getRefer() != null) {
                    refers.put(column.getName(), column.getRefer());
                }
            }
            if (refers.size() > 0) {
                LOG.info("    Refers:");
                for (Entry<String, TableInfo> e : refers.entrySet()) {
                    String columnName = e.getKey();
                    TableInfo refer = e.getValue();
                    LOG.info("        " + columnName + " = " + refer.getName() + " " + refer.getPrimaryKeys());
                }
            }

            if (table.getComboInfos().size() > 0) {
                LOG.info("    Combos:");
                for (TableInfo combo : table.getComboInfos()) {
                    LOG.info("        " + combo.getName() + " " + combo.getPrimaryKeys());
                }
            }

            if (table.getBrothers().size() > 0) {
                LOG.info("    Brothers:");
                for (TableInfo brother : table.getBrothers()) {
                    LOG.info("        " + brother.getName() + " " + brother.getPrimaryKeys());
                }
            }

            if (table.getHistory() != null) {
                LOG.info("    History:");
                TableInfo history = table.getHistory();
                LOG.info("        " + history.getName() + " " + history.getPrimaryKeys());
            }

            if (table.getParents().size() > 0) {
                LOG.info("    Parents:");
                for (TableInfo parent : table.getParents()) {
                    LOG.info("        " + parent.getName() + " " + parent.getPrimaryKeys());
                }
            }

            if (table.getChildren().size() > 0) {
                LOG.info("    Children:");
                for (TableInfo child : table.getChildren()) {
                    LOG.info("        " + child.getName() + " " + child.getPrimaryKeys());
                }
            }

            if (table.getDeriveTos().size() > 0) {
                LOG.info("    DeriveTos:");
                for (TableInfo derive : table.getDeriveTos()) {
                    LOG.info("        " + derive.getName() + " " + derive.getPrimaryKeys());
                }
            }

            Map<String, TableInfo> derivees = new LinkedHashMap<String, TableInfo>();
            for (ColumnInfo column : table.getColumns().values()) {
                if (column.getDeriveFrom() != null) {
                    derivees.put(column.getName(), column.getDeriveFrom());
                }
            }
            if (derivees.size() > 0) {
                LOG.info("    DeriveFroms:");
                for (Entry<String, TableInfo> e : derivees.entrySet()) {
                    String columnName = e.getKey();
                    TableInfo derivee = e.getValue();
                    LOG.info("        " + columnName + " = " + derivee.getName() + " " + derivee.getPrimaryKeys());
                }
            }

            if (table.getMergeTo() != null) {
                LOG.info("    MergeTo:");
                TableInfo t = table.getMergeTo();
                LOG.info("        " + t.getName() + " " + t.getPrimaryKeys());
            }

            if (table.getMergeFroms().size() > 0) {
                LOG.info("    MergeFroms:");
                for (TableInfo t : table.getMergeFroms()) {
                    LOG.info("        " + t.getName() + " " + t.getPrimaryKeys());
                }
            }

            if (table.getRebornTo() != null) {
                LOG.info("    RebornTo:");
                TableInfo reborn = table.getRebornTo();
                LOG.info("        " + reborn.getName() + " " + reborn.getPrimaryKeys());
            }

            if (table.getRebornFrom() != null) {
                LOG.info("    RebornFrom:");
                TableInfo rebornFrom = table.getRebornFrom();
                LOG.info("        " + rebornFrom.getName() + " " + rebornFrom.getPrimaryKeys());
            }

            if (table.getChoosers().size() > 0) {
                LOG.info("    Choosers:");
                for (TableInfo chooser : table.getChoosers()) {
                    LOG.info("        " + chooser.getName() + " " + chooser.getPrimaryKeys());
                }
            }

            if (table.getChoices().size() > 0) {
                LOG.info("    ChoiceOfs:");
                for (TableInfo choice : table.getChoices()) {
                    LOG.info("        " + choice.getName() + " " + choice.getPrimaryKeys());
                }
            }

            if (table.getSummaryTo() != null) {
                LOG.info("    SummaryTo:");
                TableInfo t = table.getSummaryTo();
                LOG.info("        " + t.getName() + " " + t.getPrimaryKeys());
            }

            if (table.getSummaryOfs().size() > 0) {
                LOG.info("    SummaryOfs:");
                for (TableInfo t : table.getSummaryOfs()) {
                    LOG.info("        " + t.getName() + " " + t.getPrimaryKeys());
                }
            }

            if (table.getStintInfo() != null) {
                LOG.info("    Stint:");
                TableInfo stint = table.getStintInfo();
                LOG.info("        " + stint.getName() + " " + stint.getPrimaryKeys());
            }
        }

        LOG.info("");
    }
}
