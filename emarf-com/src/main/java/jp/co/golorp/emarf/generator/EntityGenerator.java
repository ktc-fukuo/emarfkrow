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

import jp.co.golorp.emarf.io.FileUtil;
import jp.co.golorp.emarf.lang.StringUtil;
import jp.co.golorp.emarf.sql.DataSources;
import jp.co.golorp.emarf.util.IgnoreCaseList;

/**
 * エンティティ出力
 */
public final class EntityGenerator extends BeanGenerator {

    /***/
    private EntityGenerator() {
    }

    /**
     * エンティティ出力
     * @param tables テーブル情報のリスト
     */
    public static void generate(final List<TableInfo> tables) {

        // フォルダ再作成
        String pkgPath = PKG_E.replace(".", File.separator);
        String pkgDir = getProjectDir() + File.separator + DIR_J + File.separator + pkgPath;
        FileUtil.reMkDir(pkgDir);

        Map<String, String> paths = new LinkedHashMap<String, String>();
        for (TableInfo table : tables) {
            // 全列のカラム名が全角だった場合など
            if (table.getColumns().size() == 0) {
                continue;
            }
            String e = StringUtil.toPascalCase(table.getName());
            List<String> s = new ArrayList<String>();
            s.add("package " + PKG_E + ";");
            s.add("");
            s.add("import jp.co.golorp.emarf.entity.IEntity;");
            s.add("import jp.co.golorp.emarf.lang.StringUtil;");
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
                // p = p.replaceAll("#", "_"); a = a.replaceAll("#", "_");
                s.add("");
                s.add("    /** " + n.toUpperCase() + " */");
                if (StringUtil.endsWith(INPUT_TS_SUFS, n)) {
                    addAnnotationLocalTimeStamp(s);
                } else if (StringUtil.endsWith(INPUT_DT_SUFS, n)) {
                    addAnnotationLocalDateTime(s);
                } else if (t.equals("java.time.LocalDate")) {
                    addAnnotationLocalDate(s);
                } else if (t.equals("java.time.LocalTime")) {
                    addAnnotationLocalTime(s);
                }
                if (column.isPk()) {
                    s.add("    @jp.co.golorp.emarf.validation.PrimaryKeys");
                } else if (column.getName().matches("(?i)^" + UPDATE_AT + "$")) {
                    s.add("    @jp.co.golorp.emarf.validation.OptLock");
                }
                if (StringUtil.endsWith(INPUT_F_SUFS, n)) {
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
                } else if (column.getName().matches("(?i)^" + UPDATE_AT + "$")) {
                    s.add("    @jp.co.golorp.emarf.validation.OptLock");
                }
                s.add("    public " + t + " get" + a + "() {");
                if (t.equals("String") && StringUtil.endsWith(INPUT_YM_SUFS, n)) {
                    s.add("        if (!StringUtil.isNullOrWhiteSpace(this." + p + ")) {");
                    s.add("            return this." + p + ".substring(0, 4) + \"-\" + this." + p + ".substring(4);");
                    s.add("        }");
                }
                s.add("        return this." + p + ";");
                s.add("    }");
                s.add("");
                s.add("    /** @param o " + n.toUpperCase() + " */");
                if (column.isPk()) {
                    s.add("    @jp.co.golorp.emarf.validation.PrimaryKeys");
                } else if (column.getName().matches("(?i)^" + UPDATE_AT + "$")) {
                    s.add("    @jp.co.golorp.emarf.validation.OptLock");
                }
                s.add("    public void set" + a + "(final Object o) {");
                if (t.equals("java.time.LocalDateTime")) {
                    s.add("        this." + p + " = jp.co.golorp.emarf.time.DateTimeUtil.parse(o);");
                } else if (t.equals("java.time.LocalDate")) {
                    s.add("        this." + p + " = null;");
                    s.add("        if (!StringUtil.isNullOrWhiteSpace(o)) {");
                    s.add("            this." + p + " = " + t + ".parse(o.toString().substring(0, 10));");
                    s.add("        }");
                } else if (t.equals("java.time.LocalTime")) {
                    s.add("        this." + p + " = null;");
                    s.add("        if (!StringUtil.isNullOrWhiteSpace(o)) {");
                    s.add("            String text = o.toString().replaceFirst(\"^\\\\d+[\\\\/|\\\\-]\\\\d+[\\\\/|\\\\-]\\\\d+ \", \"\");");
                    s.add("            this." + p + " = " + t + ".parse(text);");
                    s.add("        }");
                } else if (t.equals("java.math.BigDecimal")) {
                    s.add("        this." + p + " = StringUtil.ifNullBigDecimal(o);");
                } else if (StringUtil.endsWith(INPUT_YM_SUFS, n)) {
                    s.add("        this." + p + " = null;");
                    s.add("        if (!StringUtil.isNullOrWhiteSpace(o)) {");
                    s.add("            this." + p + " = " + t + ".valueOf(o.toString().replace(\"-\", \"\"));");
                    s.add("        }");
                } else if (t.equals("String")) {
                    s.add("        this." + p + " = StringUtil.ifNull(o);");
                } else {
                    s.add("        this." + p + " = StringUtil.ifNull" + t + "(o);");
                }
                s.add("    }");
                if (!table.isView() && column.getRefer() != null) { // 子モデルgridで補填用の参照名
                    i = addSanshoMei(s, table, column, i);
                }
            }
            if (!StringUtil.isNullOrWhiteSpace(REASON)
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
            paths.put(path, PKG_E + "." + e);
        }
        if (IS_GENERATE_AT_STARTUP) {
            for (Entry<String, String> e : paths.entrySet()) {
                javaCompile(e.getKey(), e.getValue());
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
        int i = 0;
        for (String primaryKey : table.getPrimaryKeys()) {
            String camel = StringUtil.toCamelCase(primaryKey);
            String t = "        ";
            if (i++ > 0) {
                t += "} else ";
            }
            t += "if (StringUtil.isNullOrWhiteSpace(this." + camel + ")) {";
            s.add(t);
            s.add("            return true;");
        }
        if (table.getPrimaryKeys().size() > 0) {
            s.add("        }");
        }
        if (table.getColumns().containsKey(UPDATE_AT)) {
            String camel = StringUtil.toCamelCase(UPDATE_AT);
            s.add("        if (StringUtil.isNullOrWhiteSpace(this." + camel + ")) {");
            s.add("            return true; // 楽観ロック値がなくてもINSERT");
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
            s.add("        this.id = StringUtil.ifNullInteger(o);");
            s.add("    }");
        }
        return i;
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
    public static void addAnnotationLocalDateTime(final List<String> s) {
        s.add("    @com.fasterxml.jackson.annotation.JsonFormat(pattern = \"yyyy-MM-dd'T'HH:mm:ss\")");
        s.add("    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer.class)");
        s.add("    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer.class)");
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
     */
    public static void addAnnotationLocalTime(final List<String> s) {
        s.add("    @com.fasterxml.jackson.annotation.JsonFormat(pattern = \"hh:mm\")");
        s.add("    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer.class)");
        s.add("    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer.class)");
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
     * @param s
     * @param jsonIndex
     * @return int
     */
    public static int addRirekiTx(final List<String> s, final int jsonIndex) {
        int i = jsonIndex;
        String p = StringUtil.toCamelCase(REASON);
        String a = StringUtil.toPascalCase(REASON);
        s.add("");
        s.add("    /** " + p + " */");
        s.add("    private String " + p + ";");
        s.add("");
        s.add("    /** @return " + p + " */");
        s.add("    @com.fasterxml.jackson.annotation.JsonProperty(value = \"" + REASON + "\", index = " + ++i + ")");
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
                        s.add("        whereList.add(\"" + ASSIST.trimedSQL(q) + " = " + ASSIST.trimedSQL(p) + "\");");
                    } else {

                        // INT列の場合、postgresならcastを入れる
                        if (primaryKey.getTypeName().startsWith("INT")) {
                            p = ASSIST.castInteger(p);
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
                    String q = ASSIST.quoteEscapedSQL(key);
                    // param
                    String p = ":" + StringUtil.toSnakeCase(cleaned);
                    ColumnInfo column = table.getColumns().get(key);
                    if (column.getTypeName().equals("CHAR")) {
                        s.add("        whereList.add(\"" + ASSIST.trimedSQL(q) + " = " + ASSIST.trimedSQL(p)
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
        s.add("        map.put(\"" + StringUtil.toSnakeCase(INSERT_AT) + "\", at);");
        s.add("        map.put(\"" + StringUtil.toSnakeCase(INSERT_BY) + "\", by);");
        String now = "at";
        if (!StringUtil.isNullOrWhiteSpace(UPDATE_TS_FORMAT)) {
            now = "jp.co.golorp.emarf.time.DateTimeUtil.format(\"" + UPDATE_TS_FORMAT + "\", at)";
        }
        s.add("        map.put(\"" + StringUtil.toSnakeCase(UPDATE_AT) + "\", " + now + ");");
        s.add("        map.put(\"" + StringUtil.toSnakeCase(UPDATE_BY) + "\", by);");
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
            String q = ASSIST.quoteEscapedSQL(primaryKey);
            // param
            String p = ":" + StringUtil.toSnakeCase(primaryKey);

            ColumnInfo pkCol = table.getColumns().get(primaryKey);

            if (pkCol.getTypeName().equals("CHAR")) {

                s.add("        whereList.add(\"" + ASSIST.trimedSQL(q) + " = " + ASSIST.trimedSQL(p) + "\");");

            } else if (pkCol.getDataType().equals("java.time.LocalDateTime")) {

                s.add("        whereList.add(\"" + q + " = " + ASSIST.toDateTimeSQL(p) + "\");");

            } else {

                // INT列の場合、postgresならcastを入れる
                if (pkCol.getTypeName().startsWith("INT")) {
                    p = ASSIST.castInteger(p);
                }

                s.add("        whereList.add(\"" + q + " = " + p + "\");");
            }
        }

        // 楽観ロック
        ColumnInfo column = table.getColumns().get(UPDATE_AT);
        if (column != null) {

            String rightHand = "'\" + this." + StringUtil.toCamelCase(UPDATE_AT) + " + \"'";
            if (column.getDataType().equals("java.time.LocalDateTime")) {
                rightHand = ASSIST.toTimestampSQL(rightHand);
            }

            s.add("        whereList.add(\"" + ASSIST.quoteEscapedSQL(UPDATE_AT) + " = " + rightHand + "\");");
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
     * @param columnInfo
     * @return quoteEscaped
     */
    private static String getQuoteEscaped(final ColumnInfo columnInfo) {
        String columnName = columnInfo.getName();
        String quoteEscaped = "a." + ASSIST.quoteEscapedSQL(columnName);
        if (columnInfo.getTypeName().equals("CHAR")) {
            String trimed = ASSIST.trimedSQL(quoteEscaped);
            quoteEscaped = trimed + " AS " + columnName;
        } else if (StringUtil.endsWith(INPUT_BI_SUFS, columnInfo.getName())) {
            quoteEscaped = ASSIST.date2CharSQL(quoteEscaped) + " AS " + columnName;
        } else if (StringUtil.endsWith(INPUT_DT_SUFS, columnInfo.getName())) {
            quoteEscaped = ASSIST.dateTime2CharSQL(quoteEscaped) + " AS " + columnName;
        } else if (StringUtil.endsWith(INPUT_TS_SUFS, columnInfo.getName())) {
            quoteEscaped = ASSIST.timestamp2CharSQL(quoteEscaped) + " AS " + columnName;
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
            primaryKeys.remove(TEKIYO_BI);
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
            if (!StringUtil.isNullOrWhiteSpace(REASON)) {
                String p = StringUtil.toCamelCase(REASON);
                String a = StringUtil.toPascalCase(REASON);
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
            s.add("        nameList.add(\"" + ASSIST.quoteEscapedSQL(columnName) + " -- :" + cleanedKey + "\");");
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
            if (StringUtil.endsWith(INPUT_TS_SUFS, colName) && !isMetaTs(colName)) {
                rightHand = ASSIST.toTimestampSQL(ASSIST.timestamp2CharSQL(ASSIST.sysTimestamp()));
            }
            // INT列の場合、postgresならcastを入れる
            if (column.getTypeName().startsWith("INT")) {
                rightHand = ASSIST.castInteger(rightHand);
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
        if (table.getColumns().containsKey(STATUS_KB)) { // 決裁フロー
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
                        + StringUtil.toCamelCase(STATUS_KB) + ") && !" + StringUtil.class.getName()
                        + ".isNullOrWhiteSpace(this." + StringUtil.toCamelCase(REASON) + ")) {");
                s.add("            " + pascal + " " + camel + " = new " + pascal + "();");
                for (ColumnInfo column : statusFlow.getColumns().values()) {
                    if (column.isPk()) {
                        continue;
                    }
                    String columnName = column.getName();
                    String camelColumn = StringUtil.toCamelCase(columnName);
                    String pascalColumn = StringUtil.toPascalCase(columnName);
                    if (columnName.equalsIgnoreCase(STATUS_TABLE_NAME)) {
                        s.add("            " + camel + ".set" + pascalColumn + "(\"" + table.getName() + "\");");
                    } else if (columnName.equalsIgnoreCase(STATUS_PRIMARY_KEYS)) {
                        String keys = "String.join(\",\"";
                        for (String pk : table.getPrimaryKeys()) {
                            keys += ", this.get" + StringUtil.toPascalCase(pk) + "().toString()";
                        }
                        keys += ")";
                        s.add("            " + camel + ".set" + pascalColumn + "(" + keys + ");");
                    } else if (columnName.equalsIgnoreCase(STATUS_KESSAI_AT)) {
                        s.add("            " + camel + ".set" + pascalColumn + "(at);");
                    } else if (columnName.equalsIgnoreCase(STATUS_KESSAI_ID)) {
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
            if (!StringUtil.isNullOrWhiteSpace(REASON)) {
                String p = StringUtil.toCamelCase(REASON);
                String a = StringUtil.toPascalCase(REASON);
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
                rightHand = ASSIST.castInteger(rightHand);
            }

            s.add("        setList.add(\"" + ASSIST.quoteEscapedSQL(colName) + " = " + rightHand + "\");");
        }
        s.add("        return String.join(\"\\r\\n    , \", setList);");
        s.add("    }");
    }

    /**
     * @param table
     * @param s
     */
    public static void javaEntityCRUDDelete(final TableInfo table, final List<String> s) {

        //削除フラグがなければdeleteメソッドを出力
        if (!table.getColumns().containsKey(DELETE_F)) {
            String e = StringUtil.toPascalCase(table.getName());
            String i = StringUtil.toCamelCase(table.getName());
            s.add("");
            s.add("    /** @return 削除件数 */");
            s.add("    public int delete() {");

            int j = 0;
            for (TableInfo child : table.getChildren()) {
                if (StringUtil.isNullOrWhiteSpace(DELETE_F) || !child.getColumns().containsKey(DELETE_F)) {
                    if (j++ == 0) {
                        s.add("");
                    }
                    String ent = StringUtil.toPascalCase(child.getName());
                    String ins = StringUtil.toCamelCase(child.getName());
                    String r = child.getRemarks();
                    s.add("        // 子：" + child.getRemarks() + "の削除");
                    s.add("        if (this." + ins + "s != null) {");
                    s.add("            for (" + ent + " " + ins + " : this." + ins + "s) {");
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
                if (StringUtil.isNullOrWhiteSpace(DELETE_F) || !bro.getColumns().containsKey(DELETE_F)) {
                    if (j++ == 0) {
                        s.add("");
                    }
                    String b = StringUtil.toCamelCase(bro.getName());
                    String r = bro.getRemarks();
                    s.add("        // 兄弟：" + bro.getRemarks() + "の削除");
                    s.add("        if (this." + b + " != null) {");
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
                if (StringUtil.endsWith(INPUT_FILE_SUFS, columnName)) {
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
        s.add(""); // refer
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
                    s.add("        whereList.add(\"" + ASSIST.trimedSQL(pk) + " = " + ASSIST.trimedSQL(p) + "\");");
                } else if (!pk.matches("(?i)" + TEKIYO_BI)) {
                    s.add("        whereList.add(\"" + pk + " = " + p + "\");");
                }
            }
        }
        s.add("        String sql = \"SELECT \";");
        int cols = 0;
        int refs = 0;
        for (ColumnInfo column : child.getColumns().values()) { //カラム名を列挙
            String quoteEscaped = ASSIST.quoteEscapedSQL(column.getName());
            //時間サフィックスに合致する場合、データソースがOracleならTO_CHAR
            if (StringUtil.endsWith(INPUT_BI_SUFS, column.getName())) {
                quoteEscaped = ASSIST.date2CharSQL(quoteEscaped) + " AS " + column.getName();
            } else if (StringUtil.endsWith(INPUT_HM_SUFS, column.getName())) {
                quoteEscaped = ASSIST.time2CharSQL(quoteEscaped) + " AS " + column.getName();
            } else if (StringUtil.endsWith(INPUT_DT_SUFS, column.getName())) {
                quoteEscaped = ASSIST.dateTime2CharSQL(quoteEscaped) + " AS " + column.getName();
            } else if (StringUtil.endsWith(INPUT_TS_SUFS, column.getName())) {
                quoteEscaped = ASSIST.timestamp2CharSQL(quoteEscaped) + " AS " + column.getName();
            }
            if (cols == 0) {
                s.add("        sql += \"" + quoteEscaped + "\";");
            } else {
                s.add("        sql += \", " + quoteEscaped + "\";");
            }
            ++cols;
            if (column.getRefer() != null) { // 列の参照モデル情報があればカラム名の補完
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
        s.add("        java.util.List<" + ent + "> list = jp.co.golorp.emarf.sql.Queries.select(sql, map, " + ent
                + ".class, null, null);");
        s.add("        if (list != null) {");
        s.add("            return list;");
        s.add("        }");
        s.add("        return new java.util.ArrayList<" + ent + ">();");
        s.add("    }");
        s.add("");
        s.add("    /** " + child.getRemarks() + "を再帰 */");
        s.add("    public void nest" + ent + "s() {");
        s.add("        this." + ins + "s = " + parent + ".refer" + ent + "s(" + params + ");");
        if (child.getChildren().size() > 0) {
            s.add("        for (" + ent + " " + ins + " : this." + ins + "s) {");
            for (TableInfo mago : child.getChildren()) {
                s.add("            " + ins + ".nest" + StringUtil.toPascalCase(mago.getName()) + "s();");
            }
            s.add("        }");
        }
        s.add("    }");
        return i;
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
        String quoted = ASSIST.quoteEscapedSQL(keyName);
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
        primaryKeys.remove(TEKIYO_BI);
        if (primaryKeys.size() > 1) {

            s.add("        java.util.List<String> whereList = new java.util.ArrayList<String>();");

            // 一つ前までループ
            for (int j = 0; j < primaryKeys.size() - 1; j++) {
                String primaryKey = primaryKeys.get(j);
                String quotedKey = ASSIST.quoteEscapedSQL(primaryKey);
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

}
