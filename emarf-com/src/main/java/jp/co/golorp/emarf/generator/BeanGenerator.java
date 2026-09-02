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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.golorp.emarf.exception.SysError;
import jp.co.golorp.emarf.io.FileUtil;
import jp.co.golorp.emarf.lang.StringUtil;
import jp.co.golorp.emarf.properties.App;
import jp.co.golorp.emarf.sql.DataSources;
import jp.co.golorp.emarf.sql.DataSourcesAssist;
import jp.co.golorp.emarf.util.IgnoreCaseList;
import jp.co.golorp.emarf.util.ResourceBundles;

/**
 * java/html/sql出力
 *
 * @author golorp
 */
public final class BeanGenerator {

    /** logger */
    private static final Logger LOG = LoggerFactory.getLogger(BeanGenerator.class);
    /** DB方言クラス */
    private static DataSourcesAssist assist = DataSources.getAssist();
    /** 起動時の自動生成か */
    private static boolean isGenerateAtStartup;
    /** プロジェクトディレクトリ */
    private static String projectDir;

    /** BeanGenerator.properties */
    private static ResourceBundle bundle = ResourceBundles.getBundle(BeanGenerator.class);
    /** 適用日カラム名 */
    private static String tekiyoBi;
    /** 登録日時カラム名 */
    private static String insertTs;
    /** 登録者カラム名 */
    private static String insertBy;
    /** 更新日時カラム名 */
    private static String updateTs;
    /** 更新者カラム名 */
    private static String updateBy;
    /** ステータス区分 */
    private static String status;
    /** 削除フラグ */
    private static String deleteF;
    /** 変更理由 */
    private static String reason;

    /** 決裁フロー：テーブル名 */
    private static String statusTableName;
    /** 決裁フロー：主キー */
    private static String statusPrimaryKeys;
    /** 決裁フロー：決裁日時 */
    private static String statusKessaiTs;
    /** 決裁フロー：決裁者 */
    private static String statusKessaiId;

    /** javaファイル出力ルートパス */
    private static String javaDir;

    /** actionパッケージ */
    private static String pkgAction;
    /** entityパッケージ */
    private static String pkgE;

    /** 必須CHAR列の指定 */
    private static String charNotNullRe;
    /** 非必須INT列の指定 */
    private static String numberNullableRe;
    /** 更新日時フォーマット */
    private static String updateTsFormat;

    /** 年月入力サフィックス */
    private static String[] inputYMSuffixs;
    /** タイムスタンプサフィックス */
    private static String[] inputTimestampSuffixs;
    /** 日時入力サフィックス */
    private static String[] inputDateTimeSuffixs;
    /** 日付け入力サフィックス */
    private static String[] inputDateSuffixs;
    /** 時刻入力サフィックス */
    private static String[] inputHourSuffixs;
    //    /** 時間入力サフィックス */
    //    private static String[] inputTimeSuffixs;
    /** フラグサフィックス */
    private static String[] inputFlagSuffixs;
    /** ファイルサフィックス */
    private static String[] inputFileSuffixs;

    /** プライベートコンストラクタ */
    private BeanGenerator() {
    }

    /**
     * 各ファイル出力 主処理
     * @param dir プロジェクトのディレクトリ
     */
    public static void generate(final String dir) {

        LOG.info("start.");

        //webからの自動生成ならコンパイルまで行う
        if (App.get("generateAtStartup") != null) {
            isGenerateAtStartup = App.get("generateAtStartup").toLowerCase().equals("true");
        }

        //プロジェクトディレクトリを退避
        projectDir = dir;
        tekiyoBi = bundle.getString("column.start");
        insertTs = bundle.getString("column.insert.timestamp");
        insertBy = bundle.getString("column.insert.id");
        updateTs = bundle.getString("column.update.timestamp");
        updateBy = bundle.getString("column.update.id");
        status = bundle.getString("column.status");
        deleteF = bundle.getString("column.delete");
        reason = bundle.getString("column.reason");
        statusTableName = bundle.getString("status.tableName");
        statusPrimaryKeys = bundle.getString("status.primaryKeys");
        statusKessaiTs = bundle.getString("status.kessaiTs");
        statusKessaiId = bundle.getString("status.kessaiId");
        javaDir = bundle.getString("dir.java");
        pkgAction = bundle.getString("java.package.action") + ".model.base";
        pkgE = bundle.getString("java.package.entity");

        //NOTNULLで必須項目として扱うCHARの列名リスト（ホストの△対応）
        charNotNullRe = bundle.getString("column.char.notnull.re");
        //NOTNULLのINT列で「0」を補填する列名指定
        numberNullableRe = bundle.getString("column.number.nullable.re");
        //登録情報・更新情報の列名
        updateTsFormat = bundle.getString("column.update.timestamp.format");

        inputYMSuffixs = bundle.getString("input.ym.suffixs").split(",");
        inputTimestampSuffixs = bundle.getString("input.timestamp.suffixs").split(",");
        inputDateTimeSuffixs = bundle.getString("input.datetime.suffixs").split(",");
        inputDateSuffixs = bundle.getString("input.date.suffixs").split(",");
        inputHourSuffixs = bundle.getString("input.hour.suffixs").split(",");
        //        inputTimeSuffixs = bundle.getString("input.time.suffixs").split(",");
        inputFlagSuffixs = bundle.getString("input.flag.suffixs").split(",");
        inputFileSuffixs = bundle.getString("input.file.suffixs").split(",");

        /*
         * 出力フォルダ再作成
         */

        //エンティティフォルダ
        String entityPackagePath = pkgE.replace(".", File.separator);
        String entityPackageDir = projectDir + File.separator + javaDir + File.separator + entityPackagePath;
        FileUtil.reMkDir(entityPackageDir);

        //アクションフォルダ
        String actionPackagePath = pkgAction.replace(".", File.separator);
        String actionPackageDir = projectDir + File.separator + javaDir + File.separator + actionPackagePath;
        FileUtil.reMkDir(actionPackageDir);

        /*
         * データベースから自動生成
         */
        // テーブル情報を取得
        List<TableInfo> tables = DataSources.getTables();
        //エンティティクラス
        BeanGenerator.javaEntity(tables);
        //詳細画面アクションクラス
        DetailActionGenerator.generate(projectDir, tables);
        //検索画面アクションクラス
        IndexActionGenerator.generate(projectDir, tables);
        //フォームクラス
        FormGenerator.generate(projectDir, tables);
        //HTMLファイル
        HtmlGenerator.generate(projectDir, tables);
        //検索SQLファイル
        SqlGenerator.generate(projectDir, tables);
        LOG.info("success.");
    }

    /**
     * エンティティ出力
     * @param tables テーブル情報のリスト
     */
    private static void javaEntity(final List<TableInfo> tables) {
        String pkgPath = pkgE.replace(".", File.separator);
        String pkgDir = projectDir + File.separator + javaDir + File.separator + pkgPath;
        Map<String, String> paths = new LinkedHashMap<String, String>();
        for (TableInfo table : tables) {
            // 全列のカラム名が全角だった場合など
            if (table.getColumns().size() == 0) {
                continue;
            }
            String e = StringUtil.toPascalCase(table.getName());
            List<String> s = new ArrayList<String>();
            s.add("package " + pkgE + ";");
            s.add("");
            s.add("import jp.co.golorp.emarf.entity.IEntity;");
            s.add("import jp.co.golorp.emarf.util.IgnoreCaseLinkedMap;");
            s.add("");
            s.add("/**");
            s.add(" * " + table.getName());
            s.add(" * @author emarfkrow");
            s.add(" */");
            s.add("public class " + e + " implements IEntity {");
            addConstructor(table, e, s);
            int i = addSlickGridId(table, s, 0);
            for (ColumnInfo column : table.getColumns().values()) {
                String n = column.getName(); // name
                String p = StringUtil.toCamelCase(n); // property
                String a = StringUtil.toPascalCase(n); // accessor
                String t = column.getDataType(); // type
                //                p = p.replaceAll("#", "_");
                //                a = a.replaceAll("#", "_");
                s.add("");
                s.add("    /** " + n.toUpperCase() + " */");
                if (t.equals("java.time.LocalDate")) {
                    addAnnotationLocalDate(s);
                } else if (t.equals("java.time.LocalTime")) {
                    addAnnotationLocalTime(s);
                } else if (StringUtil.endsWith(inputTimestampSuffixs, n)) {
                    addAnnotationLocalTimeStamp(s);
                } else if (StringUtil.endsWith(inputDateTimeSuffixs, n)) {
                    addAnnotationLocalDateTime(s);
                }
                if (column.isPk()) {
                    s.add("    @jp.co.golorp.emarf.validation.PrimaryKeys");
                } else if (column.getName().matches("(?i)^" + updateTs + "$")) {
                    s.add("    @jp.co.golorp.emarf.validation.OptLock");
                }
                if (StringUtil.endsWith(inputFlagSuffixs, n)) {
                    // フラグを外した際、何も送信されず更新もかからないため、フラグ項目には初期値を設定しておく
                    if (t.equals("java.math.BigDecimal")) {
                        s.add("    private " + t + " " + p + " = new " + t + "(0);");
                    } else if (t.equals("Integer")) {
                        s.add("    private " + t + " " + p + " = 0;");
                    } else {
                        s.add("    private " + t + " " + p + " = \"0\";");
                    }
                } else {
                    s.add("    private " + t + " " + p + ";");
                }
                s.add("");
                s.add("    /** @return " + n.toUpperCase() + " */");
                s.add("    @com.fasterxml.jackson.annotation.JsonProperty(value = \"" + n.toUpperCase() + "\", index = "
                        + ++i + ")");
                if (column.isPk()) {
                    s.add("    @jp.co.golorp.emarf.validation.PrimaryKeys");
                } else if (column.getName().matches("(?i)^" + updateTs + "$")) {
                    s.add("    @jp.co.golorp.emarf.validation.OptLock");
                }
                s.add("    public " + t + " get" + a + "() {");
                if (t.equals("String") && StringUtil.endsWith(inputYMSuffixs, n)) {
                    s.add("        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrWhiteSpace(this." + p + ")) {");
                    s.add("            return this." + p + ".substring(0, 4) + \"-\" + this." + p + ".substring(4);");
                    s.add("        }");
                }
                s.add("        return this." + p + ";");
                s.add("    }");
                s.add("");
                s.add("    /** @param o " + n.toUpperCase() + " */");
                if (column.isPk()) {
                    s.add("    @jp.co.golorp.emarf.validation.PrimaryKeys");
                } else if (column.getName().matches("(?i)^" + updateTs + "$")) {
                    s.add("    @jp.co.golorp.emarf.validation.OptLock");
                }
                s.add("    public void set" + a + "(final Object o) {");
                s.add("        this." + p + " = null;");
                if (t.equals("java.time.LocalDateTime")) {
                    s.add("        if (o != null && o instanceof Long) {");
                    s.add("            java.util.Date d = new java.util.Date((Long) o);");
                    s.add("            this." + p
                            + " = java.time.LocalDateTime.ofInstant(d.toInstant(), java.time.ZoneId.systemDefault());");
                    s.add("        } else if (o != null && o.toString().matches(\"^[0-9]+\")) {");
                    s.add("            java.util.Date d = new java.util.Date(Long.valueOf(o.toString()));");
                    s.add("            this." + p
                            + " = java.time.LocalDateTime.ofInstant(d.toInstant(), java.time.ZoneId.systemDefault());");
                    s.add("        } else if (o != null && o.toString().matches(\"^.+\\\\+\\\\d{2}:\\\\d{2}$\")) {");
                    s.add("            java.time.Instant instant = java.time.Instant.parse(o.toString());");
                    s.add("            this." + p
                            + " = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());");
                    s.add("        } else if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrWhiteSpace(o)) {");
                    s.add("            this." + p + " = " + t
                            + ".parse(o.toString().replace(\" \", \"T\").replace(\"/\", \"-\"));");
                } else if (t.equals("java.time.LocalDate")) {
                    s.add("        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrWhiteSpace(o)) {");
                    s.add("            this." + p + " = " + t + ".parse(o.toString().substring(0, 10));");
                } else if (t.equals("java.time.LocalTime")) {
                    s.add("        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrWhiteSpace(o)) {");
                    s.add("            String text = o.toString().replaceFirst(\"^\\\\d+[\\\\/|\\\\-]\\\\d+[\\\\/|\\\\-]\\\\d+ \", \"\");");
                    s.add("            this." + p + " = " + t + ".parse(text);");
                } else if (t.equals("java.math.BigDecimal")) {
                    s.add("        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrWhiteSpace(o)) {");
                    s.add("            this." + p + " = new java.math.BigDecimal(o.toString());");
                } else if (StringUtil.endsWith(inputYMSuffixs, n)) {
                    s.add("        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrWhiteSpace(o)) {");
                    s.add("            this." + p + " = " + t + ".valueOf(o.toString().replace(\"-\", \"\"));");
                } else if (t.equals("String")) {
                    s.add("        if (o != null) {");
                    s.add("            this." + p + " = o.toString();");
                } else {
                    s.add("        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrWhiteSpace(o)) {");
                    s.add("            this." + p + " = " + t + ".valueOf(o.toString());");
                }
                s.add("        }");
                s.add("    }");
                if (!table.isView() && column.getRefer() != null) { // 子モデルgridで補填用の参照名
                    i = addSanshoMei(s, table, column, i);
                }
            }
            if (!StringUtil.isNullOrWhiteSpace(reason)
                    && (table.getHistory() != null || table.getStatusFlow() != null)) {
                i = addRirekiTx(s, i);
            }
            javaEntityCRUD(table, s);
            javaEntityUtil(table, s);
            i = javaEntityBrothers(table, s, i);
            i = javaEntityChild(table, s, i);
            i = javaEntityRebornTo(table, s, i);
            i = javaEntitySummaryOf(table, s, i);

            s.add("}");
            String path = pkgDir + File.separator + e + ".java";
            FileUtil.writeFile(path, s);
            paths.put(path, pkgE + "." + e);
        }

        if (isGenerateAtStartup) {
            for (Entry<String, String> e : paths.entrySet()) {
                BeanGenerator.javaCompile(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * @param table
     * @param e
     * @param s
     */
    public static void addConstructor(final TableInfo table, final String e, final List<String> s) {
        s.add("");
        s.add("    /** デフォルトコンストラクタ */");
        s.add("    public " + e + "() {");
        s.add("    }");
        s.add("");
        s.add("    /** @param values */");
        s.add("    public " + e + "(final String[] values) {");
        int j = 0;
        for (String cName : table.getColumns().keySet()) {
            String a = StringUtil.toPascalCase(cName);
            s.add("        this.set" + a + "(values[" + j++ + "]);");
        }
        s.add("    }");
        s.add("");
        s.add("    /** @param map */");
        s.add("    public " + e + "(final java.util.Map<String, Object> map) {");
        for (String cName : table.getColumns().keySet()) {
            String p = StringUtil.toPascalCase(cName);
            s.add("        this.set" + p + "(IgnoreCaseLinkedMap.get(map, \"" + cName + "\"));");
        }
        s.add("    }");
        s.add("");
        s.add("    /** @return boolean 主キーが不足していたらtrue */");
        s.add("    public boolean isNew() {");
        for (String primaryKey : table.getPrimaryKeys()) {
            String camel = StringUtil.toCamelCase(primaryKey);
            s.add("        if (jp.co.golorp.emarf.lang.StringUtil.isNullOrWhiteSpace(this." + camel + ")) {");
            s.add("            return true;");
            s.add("        }");
        }
        if (table.getColumns().containsKey(updateTs)) {
            String camel = StringUtil.toCamelCase(updateTs);
            s.add("        // 楽観ロック値がなくてもINSERT");
            s.add("        if (jp.co.golorp.emarf.lang.StringUtil.isNullOrWhiteSpace(this." + camel + ")) {");
            s.add("            return true;");
            s.add("        }");
        }
        s.add("        return false;");
        s.add("    }");
        s.add("");
        s.add("    /** @return boolean */");
        s.add("    public boolean isEmpty() {");
        s.add("        boolean isEmpty = true;");
        for (String cName : table.getColumns().keySet()) {
            if (isMeta(cName) || table.getColumns().get(cName).isPk()) {
                continue;
            }
            String c = StringUtil.toCamelCase(cName);
            s.add("        isEmpty &= this." + c + " == null || this." + c
                    + ".toString().replaceAll(\"　| \", \"\").equals(\"\");");
        }
        s.add("        return isEmpty;");
        s.add("    }");
    }

    /**
     * @param table
     * @param s
     * @param jsonIndex
     * @return int
     */
    private static int addSlickGridId(final TableInfo table, final List<String> s, final int jsonIndex) {
        int i = jsonIndex;
        if (table.getColumns().get("ID") == null) {
            s.add("");
            s.add("    /** SlickGridのDataView用ID */");
            s.add("    @jp.co.golorp.emarf.validation.GridViewRowId");
            s.add("    private Integer id;");
            s.add("");
            s.add("    /** @return id */");
            s.add("    @com.fasterxml.jackson.annotation.JsonProperty(value = \"id\", index = " + ++i + ")");
            s.add("    public final Integer getId() {");
            s.add("        return id;");
            s.add("    }");
            s.add("");
            s.add("    /** @param o id */");
            s.add("    public final void setId(final Object o) {");
            s.add("        this.id = null;");
            s.add("        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrWhiteSpace(o)) {");
            s.add("            this.id = Integer.valueOf(o.toString());");
            s.add("        }");
            s.add("    }");
        }
        return i;
    }

    /**
     * @param table
     * @param s
     * @param jsonIndex
     * @return int
     */
    private static int javaEntityRebornTo(final TableInfo table, final List<String> s, final int jsonIndex) {

        if (table.getRebornTo() == null) {
            return jsonIndex;
        }

        s.add("");
        s.add("    /* 転生先：" + table.getRebornTo().getRemarks() + " */");
        int i = addChilds(s, jsonIndex, table, table.getRebornTo());

        return i;
    }

    /**
     * @param table
     * @param s
     * @param jsonIndex
     * @return int
     */
    private static int javaEntitySummaryOf(final TableInfo table, final List<String> s, final int jsonIndex) {

        if (table.getSummaryOfs().size() == 0) {
            return jsonIndex;
        }

        int i = jsonIndex;
        for (TableInfo summaryOf : table.getSummaryOfs()) {
            s.add("");
            s.add("    /* 集約元：" + summaryOf.getRemarks() + " */");
            i = addChilds(s, i, table, summaryOf);
        }

        return i;
    }

    /**
     * @param s
     * @param jsonIndex
     * @return int
     */
    public static int addRirekiTx(final List<String> s, final int jsonIndex) {
        int i = jsonIndex;
        String p = StringUtil.toCamelCase(reason);
        String a = StringUtil.toPascalCase(reason);
        s.add("");
        s.add("    /** " + p + " */");
        s.add("    private String " + p + ";");
        s.add("");
        s.add("    /** @return " + p + " */");
        s.add("    @com.fasterxml.jackson.annotation.JsonProperty(value = \"" + reason + "\", index = " + ++i + ")");
        s.add("    public String get" + a + "() {");
        s.add("        return this." + p + ";");
        s.add("    }");
        s.add("");
        s.add("    /** @param o " + p + " */");
        s.add("    public void set" + a + "(final Object o) {");
        s.add("        if (o != null) {");
        s.add("            this." + p + " = o.toString();");
        s.add("        }");
        s.add("    }");
        return i;
    }

    /**
     * @param s
     */
    public static void addAnnotationLocalDateTime(final List<String> s) {
        s.add("    @com.fasterxml.jackson.annotation.JsonFormat(pattern = \"yyyy-MM-dd'T'HH:mm:ss\")");
        s.add("    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer.class)");
        s.add("    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer.class)");
    }

    /**
     * @param s
     */
    public static void addAnnotationLocalTimeStamp(final List<String> s) {
        s.add("    @com.fasterxml.jackson.annotation.JsonFormat(pattern = \"yyyy-MM-dd'T'HH:mm:ss.SSS\")");
        s.add("    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer.class)");
        s.add("    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer.class)");
    }

    /**
     * @param s
     */
    public static void addAnnotationLocalTime(final List<String> s) {
        s.add("    @com.fasterxml.jackson.annotation.JsonFormat(pattern = \"hh:mm\")");
        s.add("    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer.class)");
        s.add("    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer.class)");
    }

    /**
     * @param s
     */
    public static void addAnnotationLocalDate(final List<String> s) {
        s.add("    @com.fasterxml.jackson.annotation.JsonFormat(pattern = \"yyyy-MM-dd\")");
        s.add("    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer.class)");
        s.add("    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer.class)");
    }

    /**
     * @param s
     * @param table
     * @param column
     * @param jsonIndex
     * @return int
     */
    private static int addSanshoMei(final List<String> s, final TableInfo table, final ColumnInfo column,
            final int jsonIndex) {

        int i = jsonIndex;

        String columnMei = column.getRemarks();

        //参照ペアを取得
        String meiSql = SqlGenerator.getMeiSql(0, table, column);
        if (meiSql != null) {

            int columnNameIndex = meiSql.lastIndexOf(" AS ") + 4;

            //参照ペアがあるが名称カラムがなければ追加
            String n = meiSql.substring(columnNameIndex).replaceAll("[ \"`]", "");
            String p = StringUtil.toCamelCase(n);
            String a = StringUtil.toPascalCase(n);
            //            p = p.replaceAll("#", "_");
            //            a = a.replaceAll("#", "_");
            s.add("");
            s.add("    /** " + columnMei + "参照 */");
            s.add("    @jp.co.golorp.emarf.validation.ReferMei");
            s.add("    private String " + p + ";");
            s.add("");
            s.add("    /** @return " + columnMei + "参照 */");
            s.add("    @com.fasterxml.jackson.annotation.JsonProperty(value = \"" + n + "\", index = " + ++i + ")");
            s.add("    public String get" + a + "() {");
            s.add("        return this." + p + ";");
            s.add("    }");
            s.add("");
            s.add("    /** @param o " + columnMei + "参照 */");
            s.add("    public void set" + a + "(final Object o) {");
            s.add("        this." + p + " = null;");
            s.add("        if (o != null) {");
            s.add("            this." + p + " = o.toString();");
            s.add("        }");
            s.add("    }");
        }

        return i;
    }

    /**
     * エンティティにCRUD追加
     * @param table テーブル情報
     * @param s 出力文字列のリスト
     */
    private static void javaEntityCRUD(final TableInfo table, final List<String> s) {

        // 主キーなしならスキップ
        if (table.getPrimaryKeys().size() == 0) {
            return;
        }

        String e = StringUtil.toPascalCase(table.getName());

        s.add("");
        s.add("    /**");
        s.add("     * " + table.getRemarks() + "照会");
        int paramCount = 0;
        String getParams = "";

        // 主キー条件：ビューの場合は、主キーがなくても全ての列を条件にする
        boolean isPrimaryKey = table.getPrimaryKeys().size() > 0;
        if (isPrimaryKey) {
            //        if (!table.isView()) {
            for (String pk : table.getPrimaryKeys()) {
                if (pk.length() > 0) {
                    String columnRemarks = "";
                    if (table.getColumns() != null && table.getColumns().size() > 0) {
                        if (table.getColumns().containsKey(pk)) {
                            columnRemarks = " " + table.getColumns().get(pk).getRemarks();
                        }
                    }
                    s.add("     * @param param" + ++paramCount + columnRemarks);
                    if (getParams.length() > 0) {
                        getParams += ", ";
                    }
                    getParams += "final Object param" + paramCount;
                }
            }
            //        }
        } else {
            for (String key : table.getColumns().keySet()) {
                if (key.length() > 0) {
                    String columnRemarks = "";
                    if (table.getColumns() != null && table.getColumns().size() > 0) {
                        if (table.getColumns().containsKey(key)) {
                            columnRemarks = " " + table.getColumns().get(key).getRemarks();
                        }
                    }
                    s.add("     * @param param" + ++paramCount + columnRemarks);
                    if (getParams.length() > 0) {
                        getParams += ", ";
                    }
                    getParams += "final Object param" + paramCount;
                }
            }
        }

        s.add("     * @return " + table.getRemarks());
        s.add("     */");
        s.add("    public static " + e + " get(" + getParams + ") {");
        s.add("        java.util.List<String> whereList = new java.util.ArrayList<String>();");

        if (isPrimaryKey) {
            //        if (!table.isView()) {
            for (String pk : table.getPrimaryKeys()) {
                if (pk.length() > 0) {
                    // quoted
                    String q = DataSources.getAssist().quoteEscapedSQL(pk);
                    // param
                    String p = ":" + StringUtil.toSnakeCase(pk);
                    ColumnInfo primaryKey = table.getColumns().get(pk);
                    if (primaryKey.getTypeName().equals("CHAR")) {
                        s.add("        whereList.add(\"" + assist.trimedSQL(q) + " = " + assist.trimedSQL(p) + "\");");
                    } else {

                        // INT列の場合、postgresならcastを入れる
                        if (primaryKey.getTypeName().startsWith("INT")) {
                            p = assist.castInteger(p);
                        }

                        s.add("        whereList.add(\"" + q + " = " + p + "\");");
                    }
                }
            }
            //        }
        } else {
            for (String key : table.getColumns().keySet()) {
                if (key.length() > 0) {
                    String cleaned = key.replaceAll("\\$", "_");
                    // quoted
                    String q = DataSources.getAssist().quoteEscapedSQL(key);
                    // param
                    String p = ":" + StringUtil.toSnakeCase(cleaned);
                    ColumnInfo column = table.getColumns().get(key);
                    if (column.getTypeName().equals("CHAR")) {
                        s.add("        whereList.add(\"" + assist.trimedSQL(q) + " = " + assist.trimedSQL(p)
                                + "\");");
                    } else {
                        s.add("        whereList.add(\"" + q + " = " + p + "\");");
                    }
                }
            }
        }

        s.add("        String sql = \"\";");
        s.add("        sql += \"SELECT \\n\";");
        boolean isFirst = true;
        for (ColumnInfo columnInfo : table.getColumns().values()) {
            String sql = "    , ";
            if (isFirst) {
                sql = "      ";
            }
            String quoteEscaped = getQuoteEscaped(columnInfo);
            s.add("        sql += \"" + sql + quoteEscaped + " \\n\";");
            isFirst = false;
        }
        s.add("        sql += \"FROM \\n\";");
        s.add("        sql += \"    " + table.getName() + " a \\n\";");
        s.add("        sql += \"WHERE \\n\";");
        s.add("        sql += String.join(\" AND \\n\", whereList);");
        s.add("        java.util.Map<String, Object> map = new java.util.HashMap<String, Object>();");

        //        if (!table.isView()) {
        paramCount = 0;
        if (isPrimaryKey) {
            for (String pk : table.getPrimaryKeys()) {
                if (pk.length() > 0) {
                    String snake = StringUtil.toSnakeCase(pk);
                    s.add("        map.put(\"" + snake + "\", param" + ++paramCount + ");");
                }
            }
        } else {
            for (String key : table.getColumns().keySet()) {
                if (key.length() > 0) {
                    String cleaned = key.replaceAll("\\$", "_");
                    String snake = StringUtil.toSnakeCase(cleaned);
                    s.add("        map.put(\"" + snake + "\", param" + ++paramCount + ");");
                }
            }
        }
        //        }

        s.add("        return jp.co.golorp.emarf.sql.Queries.get(sql, map, " + e + ".class);");
        s.add("    }");

        if (!table.isView()) {
            javaEntityCRUDInsert(table, s);
            if (!table.isStatusFlow()) {
                javaEntityCRUDUpdate(table, s);
                javaEntityCRUDDelete(table, s);
                javaEntityCRUDTruncate(table, s);
            }
        }
    }

    /**
     * @param table
     * @param s
     */
    public static void javaEntityCRUDDelete(final TableInfo table, final List<String> s) {

        //削除フラグがなければdeleteメソッドを出力
        if (!table.getColumns().containsKey(deleteF)) {
            String e = StringUtil.toPascalCase(table.getName());
            String i = StringUtil.toCamelCase(table.getName());
            s.add("");
            s.add("    /** @return 削除件数 */");
            s.add("    public int delete() {");

            int j = 0;
            for (TableInfo child : table.getChildren()) {
                if (StringUtil.isNullOrWhiteSpace(deleteF) || !child.getColumns().containsKey(deleteF)) {
                    if (j++ == 0) {
                        s.add("");
                    }
                    String ent = StringUtil.toPascalCase(child.getName());
                    String ins = StringUtil.toCamelCase(child.getName());
                    String r = child.getRemarks();
                    s.add("        // " + child.getRemarks() + "の削除");
                    s.add("        if (this." + ins + "s != null) {");
                    s.add("            for (" + ent + " " + ins + " : this." + ins + "s) {");
                    // TODO グリッドの削除ボタンで、友連れ削除時に、更新日時の不一致で削除されない問題
                    s.add("                if (" + ins + ".delete() != 1) {");
                    s.add("                    throw new jp.co.golorp.emarf.exception.OptLockError(\"error.cant.delete\", \""
                            + r + "\");");
                    s.add("                }");
                    s.add("            }");
                    s.add("        }");
                    s.add("");
                }
            }

            // 兄弟
            for (TableInfo bro : table.getBrothers()) {
                // 自テーブル名に兄弟テーブル名が接頭する場合もスキップ（兄テーブルは削除しない）
                if (table.getName().startsWith(bro.getName())) {
                    continue;
                }
                // 兄弟に親がなく自テーブル名に前方一致しければスキップ（弟でもなく別で成り立つため）
                if (bro.getParents() == null || bro.getParents().size() == 0) {
                    if (!bro.getName().startsWith(table.getName())) {
                        continue;
                    }
                }
                // 削除フラグの定義がないか削除フラグ列がない
                if (StringUtil.isNullOrWhiteSpace(deleteF) || !bro.getColumns().containsKey(deleteF)) {
                    if (j++ == 0) {
                        s.add("");
                    }
                    String b = StringUtil.toCamelCase(bro.getName());
                    String r = bro.getRemarks();
                    s.add("        // " + bro.getRemarks() + "の削除");
                    s.add("        if (this." + b + " != null) {");
                    // TODO グリッドの削除ボタンで、友連れ削除時に、更新日時の不一致で削除されない問題
                    s.add("            if (this." + b + ".delete() != 1) {");
                    s.add("                throw new jp.co.golorp.emarf.exception.OptLockError(\"error.cant.delete\", \""
                            + r + "\");");
                    s.add("            }");
                    s.add("        }");
                    s.add("");
                }
            }
            // ファイル列がある場合
            for (String columnName : table.getColumns().keySet()) {
                if (StringUtil.endsWith(inputFileSuffixs, columnName)) {
                    if (j++ == 0) {
                        s.add("");
                    }
                    String params = "";
                    for (String primaryKey : table.getPrimaryKeys()) {
                        String camel = StringUtil.toCamelCase(primaryKey);
                        if (params.length() > 0) {
                            params += ", ";
                        }
                        params += "this." + camel;
                    }
                    s.add("        " + e + " " + i + " = " + e + ".get(" + params + ");");
                    s.add("        try {");
                    s.add("            java.nio.file.Files.delete(java.nio.file.Paths.get(" + i + "."
                            + StringUtil.toCamelCase(columnName) + "));");
                    s.add("        } catch (Exception e) {");
                    s.add("            throw new jp.co.golorp.emarf.exception.SysError(e);");
                    s.add("        }");
                    s.add("");
                }
            }
            s.add("        String sql = \"DELETE FROM " + table.getName() + " WHERE \" + getWhere();");
            s.add("        return jp.co.golorp.emarf.sql.Queries.regist(sql, toMap(null, null));");
            s.add("    }");
        }
    }

    /**
     * @param table
     * @param s
     */
    public static void javaEntityCRUDTruncate(final TableInfo table, final List<String> s) {
        s.add("");
        s.add("    /** @return 削除件数 */");
        s.add("    public static int truncate() {");
        int i = 0;
        for (TableInfo child : table.getChildren()) {
            if (i++ == 0) {
                s.add("");
            }
            s.add("        // " + child.getRemarks() + "のチェック");
            s.add("        if (jp.co.golorp.emarf.sql.Queries.select(\"SELECT COUNT (1) FROM " + child.getName()
                    + "\", null, null).size() > 0) {");
            s.add("            throw new jp.co.golorp.emarf.exception.OptLockError(\"error.cant.truncate\", \""
                    + table.getName() + " by " + child.getName() + "\");");
            s.add("        }");
            s.add("");
        }
        s.add("        String sql = \"TRUNCATE TABLE " + table.getName() + "\";");
        s.add("        return jp.co.golorp.emarf.sql.Queries.regist(sql, null);");
        s.add("    }");
    }

    /**
     * @param columnInfo
     * @return quoteEscaped
     */
    private static String getQuoteEscaped(final ColumnInfo columnInfo) {
        String columnName = columnInfo.getName();
        String quoteEscaped = "a." + assist.quoteEscapedSQL(columnName);
        if (columnInfo.getTypeName().equals("CHAR")) {
            String trimed = assist.trimedSQL(quoteEscaped);
            quoteEscaped = trimed + " AS " + columnName;
        } else if (StringUtil.endsWith(inputDateSuffixs, columnInfo.getName())) {
            quoteEscaped = assist.date2CharSQL(quoteEscaped) + " AS " + columnName;
        } else if (StringUtil.endsWith(inputDateTimeSuffixs, columnInfo.getName())) {
            quoteEscaped = assist.dateTime2CharSQL(quoteEscaped) + " AS " + columnName;
        } else if (StringUtil.endsWith(inputTimestampSuffixs, columnInfo.getName())) {
            quoteEscaped = assist.timestamp2CharSQL(quoteEscaped) + " AS " + columnName;
        }
        return quoteEscaped;
    }

    /**
     * エンティティにINSERT追加
     * @param table テーブル情報
     * @param s 出力文字列のリスト
     */
    private static void javaEntityCRUDInsert(final TableInfo table, final List<String> s) {
        s.add("");
        s.add("    /**");
        s.add("     * " + table.getRemarks() + "追加");
        s.add("     * @param at システム日時");
        s.add("     * @param by 登録者");
        s.add("     * @return 追加件数");
        s.add("     */");
        s.add("    public int insert(final java.time.LocalDateTime at, final String by) {");
        int i = 0;
        // 最後のキーを取得
        ColumnInfo lastKeyInfo = null;
        if (table.getPrimaryKeys() != null && table.getPrimaryKeys().size() > 0) {
            List<String> primaryKeys = new IgnoreCaseList<String>(table.getPrimaryKeys());
            primaryKeys.remove(tekiyoBi);
            String lastKey = primaryKeys.get(primaryKeys.size() - 1);
            lastKeyInfo = table.getColumns().get(lastKey);
            if (lastKeyInfo != null && lastKeyInfo.isNumbering()) {
                if (i++ == 0) {
                    s.add("");
                }
                s.add("        // " + lastKeyInfo.getRemarks() + "の採番処理");
                s.add("        numbering();");
                s.add("");
            }
        }
        for (TableInfo childInfo : table.getChildren()) { // 子モデル
            if (i++ == 0) {
                s.add("");
            }
            String childName = childInfo.getName();
            String camel = StringUtil.toCamelCase(childName);
            String pascal = StringUtil.toPascalCase(childName);
            s.add("        // 子：" + childInfo.getRemarks() + "の登録");
            s.add("        if (this." + camel + "s != null) {");
            s.add("            for (" + pascal + " " + camel + " : this." + camel + "s) {");
            s.add("                if (" + camel + " != null) {");
            for (String primaryKey : table.getPrimaryKeys()) {
                String pascalKey = StringUtil.toPascalCase(primaryKey);
                s.add("                    " + camel + ".set" + pascalKey + "(this.get" + pascalKey + "());");
            }
            s.add("                    " + camel + ".insert(at, by);");
            s.add("                }");
            s.add("            }");
            s.add("        }");
            s.add("");
        }
        for (TableInfo brosInfo : table.getBrothers()) { // 兄弟モデル
            if (i++ == 0) {
                s.add("");
            }
            String brosName = brosInfo.getName();
            String camel = StringUtil.toCamelCase(brosName);
            s.add("        // 兄弟：" + brosInfo.getRemarks() + "の登録");
            s.add("        if (this." + camel + " != null) {");
            for (String primaryKey : table.getPrimaryKeys()) {
                String pascalKey = StringUtil.toPascalCase(primaryKey);
                s.add("            this." + camel + ".set" + pascalKey + "(this.get" + pascalKey + "());");
            }
            s.add("            this." + camel + ".insert(at, by);");
            s.add("        }");
            s.add("");
        }
        if (table.getHistory() != null) { // 履歴モデル
            if (i++ == 0) {
                s.add("");
            }
            String historyName = table.getHistory().getName();
            String camel = StringUtil.toCamelCase(historyName);
            String pascal = StringUtil.toPascalCase(historyName);
            s.add("        // 履歴：" + table.getHistory().getRemarks() + "の登録");
            s.add("        " + pascal + " " + camel + " = new " + pascal + "();");
            for (String columnName : table.getColumns().keySet()) {
                String camelColumn = StringUtil.toCamelCase(columnName);
                String pascalColumn = StringUtil.toPascalCase(columnName);
                s.add("        " + camel + ".set" + pascalColumn + "(this." + camelColumn + ");");
            }
            if (!StringUtil.isNullOrWhiteSpace(reason)) {
                String p = StringUtil.toCamelCase(reason);
                String a = StringUtil.toPascalCase(reason);
                s.add("        " + camel + ".set" + a + "(this." + p + ");");
            }
            s.add("        " + camel + ".insert(at, by);");
            s.add("");
        }
        s.add("        String sql = \"INSERT INTO " + table.getName()
                + "(\\r\\n      \" + names() + \"\\r\\n) VALUES (\\r\\n      \" + values() + \"\\r\\n)\";");
        s.add("        return jp.co.golorp.emarf.sql.Queries.regist(sql, toMap(at, by));");
        s.add("    }");
        s.add("");
        s.add("    /** @return insert用のname句 */");
        s.add("    private String names() {");
        s.add("        java.util.List<String> nameList = new java.util.ArrayList<String>();");
        for (String columnName : table.getColumns().keySet()) {
            String snake = StringUtil.toSnakeCase(columnName);
            String cleanedKey = snake.replaceAll("\\$", "_");
            s.add("        nameList.add(\"" + assist.quoteEscapedSQL(columnName) + " -- :" + cleanedKey + "\");");
        }
        s.add("        return String.join(\"\\r\\n    , \", nameList);");
        s.add("    }");
        s.add("");
        s.add("    /** @return insert用のvalue句 */");
        s.add("    private String values() {");
        s.add("        java.util.List<String> valueList = new java.util.ArrayList<String>();");
        for (Entry<String, ColumnInfo> e : table.getColumns().entrySet()) {
            String colName = e.getKey();
            ColumnInfo column = e.getValue();
            String cleanedName = colName.replaceAll("\\$", "_");
            String rightHand = getRightHand(cleanedName, column);
            if (StringUtil.endsWith(inputTimestampSuffixs, colName) && !isMetaTs(colName)) {
                rightHand = assist.toTimestampSQL(assist.timestamp2CharSQL(assist.sysTimestamp()));
            }
            // INT列の場合、postgresならcastを入れる
            if (column.getTypeName().startsWith("INT")) {
                rightHand = assist.castInteger(rightHand);
            }
            s.add("        valueList.add(\"" + rightHand + "\");");
        }
        s.add("        return String.join(\"\\r\\n    , \", valueList);");
        s.add("    }");
        if (lastKeyInfo != null && lastKeyInfo.isNumbering()) {
            javaEntityCRUDInsertNumbering(table, s, lastKeyInfo);
        }
    }

    /**
     * @param colName
     * @param column
     * @return rightHand
     */
    public static String getRightHand(final String colName, final ColumnInfo column) {
        String rightHand = ":" + StringUtil.toSnakeCase(colName);
        if (column.getDataType().equals("java.time.LocalDate")) {
            rightHand = assist.toDateSQL(rightHand);
        } else if (column.getDataType().equals("java.time.LocalDateTime")) {
            if (StringUtil.endsWith(inputTimestampSuffixs, column.getName())) {
                rightHand = assist.toTimestampSQL(rightHand);
            } else {
                rightHand = assist.toDateTimeSQL(rightHand);
            }
        } else if (!column.isPk() && column.getTypeName().equals("CHAR")
                && !StringUtil.isNullOrWhiteSpace(charNotNullRe)
                && !column.getName().matches(charNotNullRe)) {
            //主キー以外のCHAR列で、必須CHAR指定に合致しない場合、NULLならスペースを補填する
            rightHand = "NVL (" + rightHand + ", ' ')";
        } else if (!column.isPk() && column.getTypeName().equals("NUMBER")
                && !StringUtil.isNullOrWhiteSpace(numberNullableRe)
                && column.getName().matches(numberNullableRe)) {
            //主キー以外のNUMBER列で、非必須INT指定に合致する場合、NULLなら「0」を補填する
            rightHand = "NVL (" + rightHand + ", 0)";
        }
        return rightHand;
    }

    /**
     * エンティティに採番追加
     * @param table テーブル情報
     * @param s 出力文字列のリスト
     * @param lastKey 最終キー情報
     */
    private static void javaEntityCRUDInsertNumbering(final TableInfo table, final List<String> s,
            final ColumnInfo lastKey) {
        String tableName = table.getName();
        String keyName = lastKey.getName();
        String camel = StringUtil.toCamelCase(keyName);
        String quoted = assist.quoteEscapedSQL(keyName);
        s.add("");
        s.add("    /** " + lastKey.getRemarks() + "の採番処理 */");
        s.add("    private void numbering() {");
        s.add("        if (this." + camel + " != null) {");
        s.add("            return;");
        s.add("        }");
        String numbering = "CASE WHEN MAX(e." + quoted + ") IS NULL THEN 0 ELSE MAX(e." + quoted + ") * 1 END + 1";
        String w = "";
        if (lastKey.getTypeName().equals("CHAR")) {
            int columnSize = lastKey.getColumnSize();
            numbering = "LPAD (" + numbering + ", " + columnSize + ", '0')";
            w = " WHERE e." + quoted + " < '" + new String(new char[columnSize]).replace("\0", "9") + "'";
        }
        s.add("        String sql = \"SELECT " + numbering + " AS " + quoted + " FROM " + tableName + " e" + w + "\";");
        s.add("        java.util.Map<String, Object> map = new java.util.HashMap<String, Object>();");

        List<String> primaryKeys = new IgnoreCaseList<>(table.getPrimaryKeys());
        primaryKeys.remove(tekiyoBi);
        if (primaryKeys.size() > 1) {

            s.add("        java.util.List<String> whereList = new java.util.ArrayList<String>();");

            // 一つ前までループ
            for (int j = 0; j < primaryKeys.size() - 1; j++) {
                String primaryKey = primaryKeys.get(j);
                String quotedKey = assist.quoteEscapedSQL(primaryKey);
                String snakeKey = StringUtil.toSnakeCase(primaryKey);
                s.add("        whereList.add(\"e." + quotedKey + " = :" + snakeKey + "\");");
            }

            s.add("        sql += \" WHERE \" + String.join(\" AND \", whereList);");

            // 一つ前までループ
            for (int j = 0; j < primaryKeys.size() - 1; j++) {
                String primaryKey = primaryKeys.get(j);
                String snakeKey = StringUtil.toSnakeCase(primaryKey);
                String camelKey = StringUtil.toCamelCase(primaryKey);
                s.add("        map.put(\"" + snakeKey + "\", this." + camelKey + ");");
            }
        }
        s.add("        jp.co.golorp.emarf.util.MapList mapList = jp.co.golorp.emarf.sql.Queries.select(sql, map, null, null);");
        s.add("        Object o = mapList.get(0).get(\"" + keyName.toUpperCase() + "\");");
        s.add("        this.set" + StringUtil.toPascalCase(keyName) + "(o);");
        s.add("    }");
    }

    /**
     * エンティティにUPDATE追加
     * @param table テーブル情報
     * @param s 出力文字列のリスト
     */
    private static void javaEntityCRUDUpdate(final TableInfo table, final List<String> s) {
        s.add("");
        s.add("    /**");
        s.add("     * " + table.getRemarks() + "更新");
        s.add("     * @param at システム日時");
        s.add("     * @param by 更新者");
        s.add("     * @return 更新件数");
        s.add("     */");
        s.add("    public int update(final java.time.LocalDateTime at, final String by) {");
        int j = 0;
        for (TableInfo child : table.getChildren()) { // 子モデル
            if (j++ == 0) {
                s.add("");
            }
            String e = StringUtil.toPascalCase(child.getName());
            String i = StringUtil.toCamelCase(child.getName());
            s.add("        // 子：" + child.getRemarks() + "の登録");
            s.add("        if (this." + i + "s != null) {");
            s.add("            for (" + e + " " + i + " : this." + i + "s) {");
            s.add("                if (" + i + " == null) {");
            s.add("                    continue;");
            s.add("                }");
            for (String tablePk : table.getPrimaryKeys()) {
                String pk = StringUtil.toCamelCase(tablePk);
                String pkType = StringUtil.toPascalCase(tablePk);
                s.add("                " + i + ".set" + pkType + "(this." + pk + ");");
            }
            s.add("                if (" + i + ".isNew()) {");
            s.add("                    " + i + ".insert(at, by);");
            s.add("                } else {");
            s.add("                    " + i + ".update(at, by);");
            s.add("                }");
            s.add("            }");
            s.add("        }");
            s.add("");
        }
        for (TableInfo younger : table.getBrothers()) { // 兄弟モデル
            if (j++ == 0) {
                s.add("");
            }
            String i = StringUtil.toCamelCase(younger.getName());
            s.add("        // 兄弟：" + younger.getRemarks() + "の登録");
            s.add("        if (this." + i + " != null) {");
            for (String tablePk : table.getPrimaryKeys()) {
                String pkType = StringUtil.toPascalCase(tablePk);
                s.add("            " + i + ".set" + pkType + "(this.get" + pkType + "());");
            }
            s.add("            if (" + i + ".isNew()) {");
            s.add("                " + i + ".insert(at, by);");
            s.add("            } else {");
            s.add("                " + i + ".update(at, by);");
            s.add("            }");
            s.add("        }");
            s.add("");
        }
        if (table.getColumns().containsKey(status)) { // 決裁フロー
            if (table.getStatusFlow() != null) {
                TableInfo statusFlow = table.getStatusFlow();
                if (j++ == 0) {
                    s.add("");
                }
                String statusFlowName = statusFlow.getName();
                String camel = StringUtil.toCamelCase(statusFlowName);
                String pascal = StringUtil.toPascalCase(statusFlowName);
                s.add("        // 決裁：" + statusFlow.getRemarks() + "の登録");
                s.add("        if (!" + StringUtil.class.getName() + ".isNullOrWhiteSpace(this."
                        + StringUtil.toCamelCase(status) + ") && !" + StringUtil.class.getName()
                        + ".isNullOrWhiteSpace(this." + StringUtil.toCamelCase(reason) + ")) {");
                s.add("            " + pascal + " " + camel + " = new " + pascal + "();");
                for (ColumnInfo column : statusFlow.getColumns().values()) {
                    if (column.isPk()) {
                        continue;
                    }
                    String columnName = column.getName();
                    String camelColumn = StringUtil.toCamelCase(columnName);
                    String pascalColumn = StringUtil.toPascalCase(columnName);
                    if (columnName.equalsIgnoreCase(statusTableName)) {
                        s.add("            " + camel + ".set" + pascalColumn + "(\"" + table.getName() + "\");");
                    } else if (columnName.equalsIgnoreCase(statusPrimaryKeys)) {
                        String keys = "String.join(\",\"";
                        for (String pk : table.getPrimaryKeys()) {
                            keys += ", this.get" + StringUtil.toPascalCase(pk) + "().toString()";
                        }
                        keys += ")";
                        s.add("            " + camel + ".set" + pascalColumn + "(" + keys + ");");
                    } else if (columnName.equalsIgnoreCase(statusKessaiTs)) {
                        s.add("            " + camel + ".set" + pascalColumn + "(at);");
                    } else if (columnName.equalsIgnoreCase(statusKessaiId)) {
                        s.add("            " + camel + ".set" + pascalColumn + "(by);");
                    } else {
                        s.add("            " + camel + ".set" + pascalColumn + "(this." + camelColumn + ");");
                    }
                }
                s.add("            " + camel + ".insert(at, by);");
                s.add("        }");
                s.add("");
            }
        }
        if (table.getHistory() != null) { // 履歴モデル
            TableInfo history = table.getHistory();
            if (j++ == 0) {
                s.add("");
            }
            String e = StringUtil.toPascalCase(history.getName());
            String i = StringUtil.toCamelCase(history.getName());
            s.add("        // 履歴：" + history.getRemarks() + "の登録");
            s.add("        " + e + " " + i + " = new " + e + "();");
            for (String columnName : table.getColumns().keySet()) {
                String column = StringUtil.toCamelCase(columnName);
                String columnType = StringUtil.toPascalCase(columnName);
                s.add("        " + i + ".set" + columnType + "(this." + column + ");");
            }
            if (!StringUtil.isNullOrWhiteSpace(reason)) {
                String p = StringUtil.toCamelCase(reason);
                String a = StringUtil.toPascalCase(reason);
                s.add("        " + i + ".set" + a + "(this." + p + ");");
            }
            s.add("        " + i + ".insert(at, by);");
            s.add("");
        }
        s.add("        String sql = \"UPDATE " + table.getName()
                + "\\r\\nSET\\r\\n      \" + getSet() + \"\\r\\nWHERE\\r\\n    \" + getWhere();");
        s.add("        return jp.co.golorp.emarf.sql.Queries.regist(sql, toMap(at, by));");
        s.add("    }");
        s.add("");
        s.add("    /** @return update用のset句 */");
        s.add("    private String getSet() {");
        s.add("        java.util.List<String> setList = new java.util.ArrayList<String>();");
        for (Entry<String, ColumnInfo> e : table.getColumns().entrySet()) {
            String colName = e.getKey();
            ColumnInfo column = e.getValue();

            // 追加時のメタ情報ならスキップ
            if (isMetaIns(colName)) {
                continue;
            }

            String cleanedName = colName.replaceAll("\\$", "_");
            String rightHand = getRightHand(cleanedName, column);

            // INT列の場合、postgresならcastを入れる
            if (column.getTypeName().startsWith("INT")) {
                rightHand = assist.castInteger(rightHand);
            }

            s.add("        setList.add(\"" + assist.quoteEscapedSQL(colName) + " = " + rightHand + "\");");
        }
        s.add("        return String.join(\"\\r\\n    , \", setList);");
        s.add("    }");
    }

    /**
     * エンティティにCRUD用ユーティリティ追加
     * @param table テーブル情報
     * @param s 出力文字列のリスト
     */
    private static void javaEntityUtil(final TableInfo table, final List<String> s) {

        if (table.isView() || table.getPrimaryKeys().size() == 0) {
            return;
        }

        // toMap
        s.add("");
        s.add("    /**");
        s.add("     * @param at システム日時");
        s.add("     * @param by 実行ID");
        s.add("     * @return マップ化したエンティティ");
        s.add("     */");
        s.add("    private java.util.Map<String, Object> toMap(final java.time.LocalDateTime at, final String by) {");
        s.add("        java.util.Map<String, Object> map = new java.util.HashMap<String, Object>();");
        for (String columnName : table.getColumns().keySet()) {
            if (isMetaTsBy(columnName)) {
                continue;
            }
            String snake = StringUtil.toSnakeCase(columnName);
            String p = StringUtil.toCamelCase(columnName);
            //            p = p.replaceAll("#", "_");
            s.add("        map.put(\"" + snake + "\", this." + p + ");");
        }
        s.add("        map.put(\"" + StringUtil.toSnakeCase(insertTs) + "\", at);");
        s.add("        map.put(\"" + StringUtil.toSnakeCase(insertBy) + "\", by);");
        String now = "at";
        if (!StringUtil.isNullOrWhiteSpace(updateTsFormat)) {
            now = "jp.co.golorp.emarf.time.DateTimeUtil.format(\"" + updateTsFormat + "\", at)";
        }
        s.add("        map.put(\"" + StringUtil.toSnakeCase(updateTs) + "\", " + now + ");");
        s.add("        map.put(\"" + StringUtil.toSnakeCase(updateBy) + "\", by);");
        s.add("        return map;");
        s.add("    }");

        if (table.isStatusFlow()) {
            return;
        }

        s.add("");
        s.add("    /** @return where句 */");
        s.add("    private String getWhere() {");
        s.add("        java.util.List<String> whereList = new java.util.ArrayList<String>();");

        // 主キー条件
        for (String primaryKey : table.getPrimaryKeys()) {

            if (primaryKey.length() == 0) {
                continue;
            }

            // quoted
            String q = assist.quoteEscapedSQL(primaryKey);
            // param
            String p = ":" + StringUtil.toSnakeCase(primaryKey);

            ColumnInfo pkCol = table.getColumns().get(primaryKey);

            if (pkCol.getTypeName().equals("CHAR")) {

                s.add("        whereList.add(\"" + assist.trimedSQL(q) + " = " + assist.trimedSQL(p) + "\");");

            } else if (pkCol.getDataType().equals("java.time.LocalDateTime")) {

                s.add("        whereList.add(\"" + q + " = " + assist.toDateTimeSQL(p) + "\");");

            } else {

                // INT列の場合、postgresならcastを入れる
                if (pkCol.getTypeName().startsWith("INT")) {
                    p = assist.castInteger(p);
                }

                s.add("        whereList.add(\"" + q + " = " + p + "\");");
            }
        }

        // 楽観ロック
        ColumnInfo column = table.getColumns().get(updateTs);
        if (column != null) {

            String rightHand = "'\" + this." + StringUtil.toCamelCase(updateTs) + " + \"'";
            if (column.getDataType().equals("java.time.LocalDateTime")) {
                rightHand = assist.toTimestampSQL(rightHand);
            }

            s.add("        whereList.add(\"" + assist.quoteEscapedSQL(updateTs) + " = " + rightHand + "\");");
        }

        s.add("        return String.join(\" AND \", whereList);");
        s.add("    }");
    }

    /**
     * エンティティに弟モデル追加
     * @param table テーブル情報
     * @param s 出力文字列のリスト
     * @param jsonIndex
     * @return int
     */
    private static int javaEntityBrothers(final TableInfo table, final List<String> s, final int jsonIndex) {

        int i = jsonIndex;

        // getパラメータ
        String params = "";
        for (String pk : table.getPrimaryKeys()) {
            if (pk.length() > 0) {
                if (params.length() > 0) {
                    params += ", ";
                }
                params += "this." + StringUtil.toCamelCase(pk);
            }
        }

        for (TableInfo bro : table.getBrothers()) {

            String ent = StringUtil.toPascalCase(bro.getName());
            String ins = StringUtil.toCamelCase(bro.getName());

            s.add("");
            s.add("    /** 兄弟：" + bro.getRemarks() + " */");
            s.add("    private " + ent + " " + ins + ";");
            s.add("");
            s.add("    /** @return " + bro.getRemarks() + " */");
            s.add("    @com.fasterxml.jackson.annotation.JsonProperty(value = \"" + ent + "\", index = " + ++i + ")");
            s.add("    public " + ent + " get" + ent + "() {");
            s.add("        return this." + ins + ";");
            s.add("    }");
            s.add("");
            s.add("    /** @param p " + bro.getRemarks() + " */");
            s.add("    public void set" + ent + "(final " + ent + " p) {");
            s.add("        this." + ins + " = p;");
            s.add("    }");
            s.add("");
            s.add("    /** @return " + bro.getRemarks() + " */");
            s.add("    public " + ent + " refer" + ent + "() {");
            s.add("        if (this." + ins + " == null) {");
            s.add("            try {");
            s.add("                this." + ins + " = " + ent + ".get(" + params + ");");
            s.add("            } catch (jp.co.golorp.emarf.exception.NoDataError e) {");
            s.add("            }");
            s.add("        }");
            s.add("        return this." + ins + ";");
            s.add("    }");
        }

        return i;
    }

    /**
     * エンティティに子モデル追加
     * @param table テーブル情報
     * @param s 出力文字列のリスト
     * @param jsonIndex
     * @return int
     */
    private static int javaEntityChild(final TableInfo table, final List<String> s, final int jsonIndex) {

        int i = jsonIndex;

        for (TableInfo child : table.getChildren()) {
            s.add("");
            s.add("    /*");
            s.add("     * 子モデル：" + child.getRemarks());
            s.add("     */");
            i = addChilds(s, i, table, child);
        }

        return i;
    }

    /**
     * @param s
     * @param jsonIndex
     * @param table
     * @param child
     * @return int
     */
    private static int addChilds(final List<String> s, final int jsonIndex, final TableInfo table,
            final TableInfo child) {

        int i = jsonIndex;

        String parent = StringUtil.toPascalCase(table.getName());

        String params = "";
        for (String pk : table.getPrimaryKeys()) {
            if (pk.length() > 0) {
                if (params.length() > 0) {
                    params += ", ";
                }
                params += "this." + StringUtil.toCamelCase(pk);
            }
        }

        String ent = StringUtil.toPascalCase(child.getName());

        String ins = StringUtil.toCamelCase(child.getName());

        s.add("");
        s.add("    /** " + child.getRemarks() + "のリスト */");
        s.add("    private java.util.List<" + ent + "> " + ins + "s;");
        s.add("");
        s.add("    /** @return " + child.getRemarks() + "のリスト */");
        s.add("    @com.fasterxml.jackson.annotation.JsonProperty(value = \"" + ent + "s\", index = " + ++i + ")");
        s.add("    public java.util.List<" + ent + "> get" + ent + "s() {");
        s.add("        return this." + ins + "s;");
        s.add("    }");
        s.add("");
        s.add("    /** @param list " + child.getRemarks() + "のリスト */");
        s.add("    public void set" + ent + "s(final java.util.List<" + ent + "> list) {");
        s.add("        this." + ins + "s = list;");
        s.add("    }");
        s.add("");
        s.add("    /** @param " + ins + " */");
        s.add("    public void add" + ent + "s(final " + ent + " " + ins + ") {");
        s.add("        if (this." + ins + "s == null) {");
        s.add("            this." + ins + "s = new java.util.ArrayList<" + ent + ">();");
        s.add("        }");
        s.add("        this." + ins + "s.add(" + ins + ");");
        s.add("    }");
        s.add("");
        s.add("    /** @return " + child.getRemarks() + "のリスト */");
        s.add("    public java.util.List<" + ent + "> refer" + ent + "s() {");
        s.add("        this." + ins + "s = " + parent + ".refer" + ent + "s(" + params + ");");
        s.add("        return this." + ins + "s;");
        s.add("    }");

        // refer
        s.add("");
        s.add("    /**");
        int paramIndex = 0;
        String pks = "";
        for (String pk : table.getPrimaryKeys()) {
            if (pk.length() > 0) {
                String property = StringUtil.toCamelCase(pk);
                s.add("     * @param param" + ++paramIndex + " " + property);
                if (pks.length() > 0) {
                    pks += ", ";
                }
                ColumnInfo column = table.getColumns().get(pk);
                pks += "final " + column.getDataType() + " param" + paramIndex;
            }
        }
        s.add("     * @return java.util.List<" + ent + ">");
        s.add("     */");
        s.add("    public static java.util.List<" + ent + "> refer" + ent + "s(" + pks + ") {");
        s.add("        java.util.List<String> whereList = new java.util.ArrayList<String>();");
        for (String pk : table.getPrimaryKeys()) {
            if (pk.length() > 0) {
                String p = ":" + StringUtil.toSnakeCase(pk);
                ColumnInfo primaryKey = table.getColumns().get(pk);
                if (primaryKey.getTypeName().equals("CHAR")) {
                    s.add("        whereList.add(\"" + assist.trimedSQL(pk) + " = " + assist.trimedSQL(p) + "\");");
                } else if (!pk.matches("(?i)" + tekiyoBi)) {
                    s.add("        whereList.add(\"" + pk + " = " + p + "\");");
                }
            }
        }

        //カラム名を列挙
        s.add("        String sql = \"SELECT \";");
        int cols = 0;
        int refs = 0;
        for (ColumnInfo column : child.getColumns().values()) {
            String quoteEscaped = assist.quoteEscapedSQL(column.getName());
            //時間サフィックスに合致する場合、データソースがOracleならTO_CHAR
            if (StringUtil.endsWith(inputDateSuffixs, column.getName())) {
                quoteEscaped = assist.date2CharSQL(quoteEscaped) + " AS " + column.getName();
            } else if (StringUtil.endsWith(inputHourSuffixs, column.getName())) {
                quoteEscaped = assist.time2CharSQL(quoteEscaped) + " AS " + column.getName();
            } else if (StringUtil.endsWith(inputDateTimeSuffixs, column.getName())) {
                quoteEscaped = assist.dateTime2CharSQL(quoteEscaped) + " AS " + column.getName();
            } else if (StringUtil.endsWith(inputTimestampSuffixs, column.getName())) {
                quoteEscaped = assist.timestamp2CharSQL(quoteEscaped) + " AS " + column.getName();
            }
            if (cols == 0) {
                s.add("        sql += \"" + quoteEscaped + "\";");
            } else {
                s.add("        sql += \", " + quoteEscaped + "\";");
            }
            ++cols;
            // 列の参照モデル情報があればカラム名の補完
            if (column.getRefer() != null) {
                String meiSql = SqlGenerator.getMeiSql(refs, table, column);
                if (meiSql != null) {
                    ++refs;
                    meiSql = meiSql.replaceAll("\"", "\\\\\"");
                    s.add("        sql += \"" + meiSql + "\";");
                }
            }
        }
        s.add("        sql += \" FROM " + child.getName() + " a WHERE \" + String.join(\" AND \", whereList);");
        s.add("        sql += \" ORDER BY \";");
        String orders = "";
        if (child.getPrimaryKeys().size() > 0) {
            for (String pk : child.getPrimaryKeys()) {
                if (orders.length() > 0) {
                    orders += ", ";
                }
                orders += pk;
            }
        } else {
            for (paramIndex = 1; paramIndex <= child.getColumns().size(); paramIndex++) {
                if (paramIndex == 1) {
                    orders += ", ";
                }
                orders += paramIndex;
            }
        }
        s.add("        sql += \"" + orders + "\";");
        s.add("        java.util.Map<String, Object> map = new java.util.HashMap<String, Object>();");
        paramIndex = 0;
        for (String pk : table.getPrimaryKeys()) {
            if (pk.length() == 0) {
                continue;
            }
            s.add("        map.put(\"" + StringUtil.toSnakeCase(pk) + "\", param" + ++paramIndex + ");");
        }
        //        s.add("        return Queries.select(sql, map, " + ent + ".class, null, null);");
        s.add("        java.util.List<" + ent + "> list = jp.co.golorp.emarf.sql.Queries.select(sql, map, " + ent
                + ".class, null, null);");
        s.add("        if (list != null) {");
        s.add("            return list;");
        s.add("        }");
        s.add("        return new java.util.ArrayList<" + ent + ">();");
        s.add("    }");
        return i;
    }

    //    /**
    //     * @param child
    //     * @param colName
    //     * @param param
    //     * @return String
    //     */
    //    private static String getTekiyoBiSql(final TableInfo child, final String colName, final String param) {
    //
    //        String sql = colName + " = (";
    //
    //        sql += "SELECT DISTINCT MAX (a2." + colName + ") OVER (PARTITION BY ";
    //
    //        String cKeys = "";
    //        for (String cPk : child.getPrimaryKeys()) {
    //            if (cPk.equals(tekiyoBi)) {
    //                continue;
    //            }
    //            if (cKeys.length() > 0) {
    //                cKeys += ", ";
    //            }
    //            cKeys += "a2." + cPk;
    //        }
    //        sql += cKeys;
    //
    //        sql += ") ";
    //        sql += "FROM " + child.getName() + " a2 ";
    //        sql += "WHERE ";
    //
    //        cKeys = "";
    //        for (String cPk : child.getPrimaryKeys()) {
    //            if (cKeys.length() > 0) {
    //                cKeys += " AND ";
    //            }
    //            if (cPk.equals(tekiyoBi)) {
    //                cKeys += "a2." + cPk + " <= " + param;
    //            } else {
    //                cKeys += "a2." + cPk + " = a." + cPk;
    //            }
    //        }
    //        sql += cKeys;
    //
    //        sql += ")";
    //
    //        return sql;
    //    }

    /**
     * 一覧画面の一括削除処理
     * @param s
     * @param p
     * @param childs
     * @param indent
     */
    public static void getDeleteChilds(final List<String> s, final String p, final List<TableInfo> childs,
            final int indent) {

        String sp = "    ".repeat(indent);

        for (TableInfo child : childs) {

            if (!StringUtil.isNullOrWhiteSpace(deleteF) && child.getColumns().containsKey(deleteF)) {
                continue;
            }

            s.add("");

            String r = child.getRemarks();

            // entity
            String e = StringUtil.toPascalCase(child.getName());

            // instance
            String i = StringUtil.toCamelCase(child.getName());

            int parents = child.getParents().size();
            if (parents == 1) {

                s.add(sp + "        java.util.List<" + pkgE + "." + e + "> " + i + "s = " + p + ".refer" + e + "s();");
                s.add(sp + "        if (" + i + "s != null) {");
                s.add(sp + "            for (" + pkgE + "." + e + " " + i + " : " + i + "s) {");
                if (child.getChildren().size() > 0) {
                    // forでもう一段降りているから「+2」
                    getDeleteChilds(s, i, child.getChildren(), indent + 2);
                }
                s.add("");
                s.add(sp + "                if (" + i + ".delete() != 1) {");
                s.add(sp + "                    throw new OptLockError(\"error.cant.delete\", \"" + r + "\");");
                s.add(sp + "                }");
                s.add(sp + "            }");
                s.add(sp + "        }");

            } else {

                s.add(sp + "        // child:" + e + ", parents:" + parents);
            }

            s.add("");
        }
    }

    /**
     * 一覧画面の一括申請処理
     * @param s
     * @param parent
     * @param childs
     * @param indent
     */
    public static void getApplyChilds(final List<String> s, final String parent, final List<TableInfo> childs,
            final int indent) {

        // indent
        String p = "    ".repeat(indent);

        for (TableInfo child : childs) {

            String r = child.getRemarks();

            // entity
            String e = StringUtil.toPascalCase(child.getName());

            // instance
            String i = StringUtil.toCamelCase(child.getName());

            s.add("");

            int parents = child.getParents().size();
            if (parents > 1) {
                s.add(p + "        // child:" + e + ", parents:" + parents);
                continue;
            }

            s.add(p + "        java.util.List<" + pkgE + "." + e + "> " + i + "s = " + parent + ".refer" + e + "s();");
            s.add(p + "        if (" + i + "s != null) {");
            s.add(p + "            for (" + pkgE + "." + e + " " + i + " : " + i + "s) {");
            if (child.getChildren().size() > 0) {
                // forでもう一段降りているから「+2」
                getApplyChilds(s, i, child.getChildren(), indent + 2);
            }
            s.add("");
            if (child.getColumns().containsKey(status)) {
                s.add(p + "                " + i + ".set" + StringUtil.toPascalCase(status) + "(0);");
            }
            s.add(p + "                if (" + i + ".update(at, by) != 1) {");
            s.add(p + "                    throw new OptLockError(\"error.cant.apply\", \"" + r + "\");");
            s.add(p + "                }");
            s.add(p + "            }");
            s.add(p + "        }");
        }
    }

    /**
     * 一覧画面の一括取消処理
     * @param s
     * @param parent
     * @param childs
     * @param indent
     */
    public static void getCancelChilds(final List<String> s, final String parent, final List<TableInfo> childs,
            final int indent) {

        // indent
        String p = "    ".repeat(indent);

        for (TableInfo child : childs) {

            String r = child.getRemarks();

            // entity
            String e = StringUtil.toPascalCase(child.getName());

            // instance
            String i = StringUtil.toCamelCase(child.getName());

            s.add("");

            int parents = child.getParents().size();
            if (parents > 1) {
                s.add(p + "        // child:" + e + ", parents:" + parents);
                continue;
            }

            s.add(p + "        java.util.List<" + pkgE + "." + e + "> " + i + "s = " + parent + ".refer" + e + "s();");
            s.add(p + "        if (" + i + "s != null) {");
            s.add(p + "            for (" + pkgE + "." + e + " " + i + " : " + i + "s) {");
            if (child.getChildren().size() > 0) {
                // forでもう一段降りているから「+2」
                getCancelChilds(s, i, child.getChildren(), indent + 2);
            }
            s.add("");
            if (child.getColumns().containsKey(status)) {
                s.add(p + "                " + i + ".set" + StringUtil.toPascalCase(status) + "(null);");
            }
            s.add(p + "                if (" + i + ".update(at, by) != 1) {");
            s.add(p + "                    throw new OptLockError(\"error.cant.cancel\", \"" + r + "\");");
            s.add(p + "                }");
            s.add(p + "            }");
            s.add(p + "        }");
        }
    }

    /**
     * 一覧画面の一括承認処理
     * @param s
     * @param parent
     * @param childs
     * @param indent
     */
    public static void getPermitChilds(final List<String> s, final String parent, final List<TableInfo> childs,
            final int indent) {

        // indent
        String p = "    ".repeat(indent);

        for (TableInfo child : childs) {

            String r = child.getRemarks();

            // entity
            String e = StringUtil.toPascalCase(child.getName());

            // instance
            String i = StringUtil.toCamelCase(child.getName());

            s.add("");

            int parents = child.getParents().size();
            if (parents > 1) {
                s.add(p + "        // child:" + e + ", parents:" + parents);
                continue;
            }

            s.add(p + "        java.util.List<" + pkgE + "." + e + "> " + i + "s = " + parent + ".refer" + e + "s();");
            s.add(p + "        if (" + i + "s != null) {");
            s.add(p + "            for (" + pkgE + "." + e + " " + i + " : " + i + "s) {");
            if (child.getChildren().size() > 0) {
                // forでもう一段降りているから「+2」
                getPermitChilds(s, i, child.getChildren(), indent + 2);
            }
            s.add("");
            if (child.getColumns().containsKey(status)) {
                s.add(p + "                " + i + ".set" + StringUtil.toPascalCase(status) + "(1);");
            }
            s.add(p + "                if (" + i + ".update(at, by) != 1) {");
            s.add(p + "                    throw new OptLockError(\"error.cant.permit\", \"" + r + "\");");
            s.add(p + "                }");
            s.add(p + "            }");
            s.add(p + "        }");
        }
    }

    /**
     * 一覧画面の一括否認処理
     * @param s
     * @param parent
     * @param childs
     * @param indent
     */
    public static void getForbidChilds(final List<String> s, final String parent, final List<TableInfo> childs,
            final int indent) {

        String p = "    ".repeat(indent);

        for (TableInfo child : childs) {
            String r = child.getRemarks();
            String e = StringUtil.toPascalCase(child.getName());
            String i = StringUtil.toCamelCase(child.getName());
            s.add("");
            int parents = child.getParents().size();
            if (parents > 1) {
                s.add(p + "        // child:" + e + ", parents:" + parents);
                continue;
            }

            s.add(p + "        java.util.List<" + pkgE + "." + e + "> " + i + "s = " + parent + ".refer" + e + "s();");
            s.add(p + "        if (" + i + "s != null) {");
            s.add(p + "            for (" + pkgE + "." + e + " " + i + " : " + i + "s) {");
            if (child.getChildren().size() > 0) {
                // forでもう一段降りているから「+2」
                getForbidChilds(s, i, child.getChildren(), indent + 2);
            }
            s.add("");
            if (child.getColumns().containsKey(status)) {
                s.add(p + "                " + i + ".set" + StringUtil.toPascalCase(status) + "(-1);");
            }
            s.add(p + "                if (" + i + ".update(at, by) != 1) {");
            s.add(p + "                    throw new OptLockError(\"error.cant.forbid\", \"" + r + "\");");
            s.add(p + "                }");
            s.add(p + "            }");
            s.add(p + "        }");
        }
    }

    /**
     * javaファイルをコンパイル
     * @param javaFilePath javaファイルパス
     * @param className クラス名
     */
    public static void javaCompile(final String javaFilePath, final String className) {

        // 出力ディレクトリ
        String dstDir = projectDir + File.separator + javaDir;

        // クラスパス
        String classPath = System.getProperty("java.class.path", null);

        // 参照ライブラリ
        String pathes = "";
        File classes = new File(BeanGenerator.class.getResource("/").getPath());
        File lib = new File(classes.getParentFile().getAbsolutePath() + File.separator + "lib");
        File[] files = lib.listFiles();
        if (files != null) {
            for (File file : files) {
                pathes += ";" + file.getAbsolutePath();
            }
        }

        // コンパイル
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String[] args = {
                "-d", dstDir,
                "-sourcepath", dstDir,
                "-classpath", classPath + pathes,
                //new File(src, clsName + ".java").getAbsolutePath()
                javaFilePath
        };
        int result = compiler.run(null, null, null, args);
        if (result == 0) {
            LOG.info("compile success. [" + javaFilePath + "]");
        } else {
            LOG.error("compile failure. [" + javaFilePath + "]");
        }

        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new SysError(e);
        }
    }

    /**
     * @param s
     * @return boolean
     */
    public static boolean isMetaTs(final String s) {
        return s.matches("(?i)^" + insertTs + "$") || s.matches("(?i)^" + updateTs + "$");
    }

    /**
     * @param s
     * @return boolean
     */
    public static boolean isMetaIns(final String s) {
        return s.matches("(?i)^" + insertTs + "$") || s.matches("(?i)^" + insertBy + "$");
    }

    /**
     * @param s
     * @return boolean
     */
    public static boolean isMetaTsBy(final String s) {
        return s.matches("(?i)^" + insertTs + "$") || s.matches("(?i)^" + insertBy + "$")
                || s.matches("(?i)^" + updateTs + "$") || s.matches("(?i)^" + updateBy + "$");
    }

    /**
     * @param s
     * @return boolean
     */
    public static boolean isMeta(final String s) {
        return s.matches("(?i)^" + insertTs + "$") || s.matches("(?i)^" + insertBy + "$")
                || s.matches("(?i)^" + updateTs + "$") || s.matches("(?i)^" + updateBy + "$")
                || s.matches("(?i)^" + deleteF + "$") || s.matches("(?i)^" + status + "$");
    }

    /**
     * @param name
     * @return boolean
     */
    public static boolean isMetaBy(final String name) {
        return name.matches("(?i)^" + insertBy + "$") || name.matches("(?i)^" + updateBy + "$");
    }

}
