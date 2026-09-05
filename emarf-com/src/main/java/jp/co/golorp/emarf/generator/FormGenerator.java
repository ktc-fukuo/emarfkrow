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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.golorp.emarf.io.FileUtil;
import jp.co.golorp.emarf.lang.StringUtil;
import jp.co.golorp.emarf.properties.App;
import jp.co.golorp.emarf.util.ResourceBundles;

/**
 * フォーム出力
 */
public final class FormGenerator {

    /** logger */
    private static final Logger LOG = LoggerFactory.getLogger(FormGenerator.class);

    /** BeanGenerator.properties */
    private static ResourceBundle bundle = ResourceBundles.getBundle(BeanGenerator.class);

    /** formパッケージ */
    private static String pkgF = "com.example.form.model.base";

    /** javaファイル出力ルートパス */
    private static String javaDir = "src\\main\\java";

    /** プロジェクトディレクトリ */
    private static String projectDir;

    /** 適用日カラム名 */
    private static String tekiyoBi;
    /** 更新日時カラム名 */
    private static String updateTs;

    /** フラグサフィックス */
    private static String[] inputFlagSuffixs;

    /** 起動時の自動生成か */
    private static boolean isGenerateAtStartup;

    /** validサフィックス */
    private static List<String> validSuffixs;

    /** 必須CHAR列の指定 */
    private static String charNotNullRe;
    /** 非必須INT列の指定 */
    private static String numberNullableRe;
    /** タイムスタンプサフィックス */
    private static String[] inputTimestampSuffixs;

    /**
     * プライベートコンストラクタ
     */
    private FormGenerator() {
    }

    /**
     * 各ファイル出力 主処理
     * @param dir プロジェクトのディレクトリ
     * @param tables
     */
    public static void generate(final String dir, final List<TableInfo> tables) {

        projectDir = dir;

        //webからの自動生成ならコンパイルまで行う
        if (App.get("generateAtStartup") != null) {
            isGenerateAtStartup = App.get("generateAtStartup").toLowerCase().equals("true");
        }

        validSuffixs = new ArrayList<String>();
        if (bundle != null) {

            pkgF = bundle.getString("java.package.form") + ".model.base";
            javaDir = bundle.getString("dir.java");
            tekiyoBi = bundle.getString("column.start");
            updateTs = bundle.getString("column.update.timestamp");

            inputFlagSuffixs = bundle.getString("input.flag.suffixs").split(",");

            //validator正規表現の接尾辞を取得
            for (String key : bundle.keySet()) {
                if (key.startsWith("valid.")) {
                    validSuffixs.add(key.replaceFirst("valid.", ""));
                }
            }

            //NOTNULLで必須項目として扱うCHARの列名リスト（ホストの△対応）
            charNotNullRe = bundle.getString("column.char.notnull.re");
            //NOTNULLのINT列で「0」を補填する列名指定
            numberNullableRe = bundle.getString("column.number.nullable.re");
            //タイムスタンプサフィックス
            inputTimestampSuffixs = bundle.getString("input.timestamp.suffixs").split(",");
        }

        //フォームフォルダ
        String pkgFormPath = pkgF.replace(".", File.separator);
        String pkgFormDir = projectDir + File.separator + javaDir + File.separator + pkgFormPath;
        FileUtil.reMkDir(pkgFormDir);

        FormGenerator.javaFormDetailRegist(tables);
        FormGenerator.javaFormIndexRegist(tables);
    }

    /**
     * 詳細画面 フォーム出力
     * @param tableInfos テーブル情報のリスト
     */
    private static void javaFormDetailRegist(final List<TableInfo> tableInfos) {
        String packagePath = pkgF.replace(".", File.separator);
        String packageDir = projectDir + File.separator + javaDir + File.separator + packagePath;
        Map<String, String> javaFilePaths = new LinkedHashMap<String, String>();
        for (TableInfo table : tableInfos) {
            if (table.isHistory() || table.isView() || table.isStatusFlow()) {
                continue;
            }
            String entity = StringUtil.toPascalCase(table.getName());
            List<String> s = new ArrayList<String>();
            s.add("package " + pkgF + ";");
            addImports(s);
            addAuthor(s, table.getRemarks() + "登録フォーム");
            s.add("public class " + entity + "RegistForm implements IForm {");
            s.add("");
            s.add("    /** logger */");
            s.add("    private static final Logger LOG = LoggerFactory.getLogger(" + entity + "RegistForm.class);");
            for (ColumnInfo column : table.getColumns().values()) {
                // レコードメタデータならスキップ。updateDtは楽観ロック用に必要
                boolean isUpdTs = column.getName().matches("(?i)^" + updateTs + "$");
                if (!isUpdTs && BeanGenerator.isMetaTsBy(column.getName())) {
                    continue;
                }
                String prop = StringUtil.toCamelCase(column.getName());
                String acce = StringUtil.toPascalCase(column.getName());
                s.add("");
                s.add("    /** " + column.getRemarks() + " */");
                javaFormDetailRegistChecks(s, table, column);

                if (column.getNullable() == 1) {
                    if (StringUtil.endsWith(inputFlagSuffixs, column.getName())) {
                        s.add("    private String " + prop + " = \"0\";");
                    } else {
                        s.add("    private String " + prop + ";");
                    }
                } else if (column.getDefaultValue() != null && column.getDataType().equals("String")) {
                    s.add("    private String " + prop + " = \"" + column.getDefaultValue() + "\";");
                } else {
                    s.add("    private String " + prop + ";");
                }

                s.add("");
                s.add("    /** @return " + column.getRemarks() + " */");
                if (column.isPk()) {
                    s.add("    @jp.co.golorp.emarf.validation.PrimaryKeys");
                } else if (column.getName().matches("(?i)^" + updateTs + "$")) {
                    s.add("    @jp.co.golorp.emarf.validation.OptLock");
                }
                s.add("    public String get" + acce + "() {");
                s.add("        return " + prop + ";");
                s.add("    }");
                s.add("");
                s.add("    /** @param p " + column.getRemarks() + " */");
                if (column.isPk()) {
                    s.add("    @jp.co.golorp.emarf.validation.PrimaryKeys");
                } else if (column.getName().matches("(?i)^" + updateTs + "$")) {
                    s.add("    @jp.co.golorp.emarf.validation.OptLock");
                }
                s.add("    public void set" + acce + "(final String p) {");
                s.add("        this." + prop + " = p;");
                s.add("    }");
            }
            for (TableInfo brother : table.getBrothers()) { // 兄弟モデル
                String camel = StringUtil.toCamelCase(brother.getName());
                String pascal = StringUtil.toPascalCase(brother.getName());
                s.add("");
                s.add("    /** " + brother.getRemarks() + " */");
                s.add("    @jakarta.validation.Valid");
                s.add("    private " + pascal + "RegistForm " + camel + "RegistForm;");
                s.add("");
                s.add("    /** @return " + pascal + "RegistForm */");
                s.add("    public " + pascal + "RegistForm get" + pascal + "RegistForm() {");
                s.add("        return " + camel + "RegistForm;");
                s.add("    }");
                s.add("");
                s.add("    /** @param p */");
                s.add("    public void set" + pascal + "RegistForm(final " + pascal + "RegistForm p) {");
                s.add("        this." + camel + "RegistForm = p;");
                s.add("    }");
            }
            for (TableInfo child : table.getChildren()) { // 子モデル
                String camel = StringUtil.toCamelCase(child.getName());
                String pascal = StringUtil.toPascalCase(child.getName());
                s.add("");
                s.add("    /** " + child.getRemarks() + " */");
                s.add("    @jakarta.validation.Valid");
                s.add("    private java.util.List<" + pascal + "RegistForm> " + camel + "Grid;");
                s.add("");
                s.add("    /**");
                s.add("     * @return " + child.getRemarks());
                s.add("     */");
                s.add("    public java.util.List<" + pascal + "RegistForm> get" + pascal + "Grid() {");
                s.add("        return " + camel + "Grid;");
                s.add("    }");
                s.add("");
                s.add("    /**");
                s.add("     * @param p");
                s.add("     */");
                s.add("    public void set" + pascal + "Grid(final java.util.List<" + pascal + "RegistForm> p) {");
                s.add("        this." + camel + "Grid = p;");
                s.add("    }");
            }
            javaFormDetailRegistRelCheck(table, s);
            s.add("}");
            String javaFilePath = packageDir + File.separator + entity + "RegistForm.java";
            javaFilePaths.put(javaFilePath, pkgF + "." + entity + "RegistForm");
            FileUtil.writeFile(javaFilePath, s);
        }
        if (isGenerateAtStartup) {
            for (Entry<String, String> e : javaFilePaths.entrySet()) {
                BeanGenerator.javaCompile(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 関連チェック
     * @param table
     * @param s
     */
    private static void javaFormDetailRegistRelCheck(final TableInfo table, final List<String> s) {

        String e = StringUtil.toPascalCase(table.getName());

        s.add("");
        s.add("    /** 関連チェック */");
        s.add("    @Override");
        s.add("    public void validate(final Map<String, String> errors, final BaseProcess baseProcess) {");
        s.add("        LOG.trace(\"validate() not overridden in subclasses.\");");

        // 派生元のマスタチェック
        for (TableInfo from : table.getDeriveFroms()) {
            String fromE = StringUtil.toPascalCase(from.getName());
            String fromI = StringUtil.toCamelCase(from.getName());
            s.add("");
            s.add("        // " + from.getRemarks() + " の派生元チェック TODO できればAssertTrueにしたい");
            s.add("        Map<String, Object> " + fromI + "Params = new java.util.HashMap<String, Object>();");
            String lastKey = null;
            for (String fromPrimaryKey : from.getPrimaryKeys()) {
                lastKey = StringUtil.toCamelCase(fromPrimaryKey);
                s.add("        " + fromI + "Params.put(\"" + lastKey + "\", this.get"
                        + StringUtil.toPascalCase(fromPrimaryKey) + "());");
            }
            s.add("        baseProcess.masterCheck(errors, \"" + fromE + "Search\", \"" + lastKey + "\", " + fromI
                    + "Params, jp.co.golorp.emarf.util.Messages.get(\"" + e + "\"));");
        }

        // 共生元のマスタチェック
        for (TableInfo from : table.getMergeFroms()) {
            String fromE = StringUtil.toPascalCase(from.getName());
            String fromI = StringUtil.toCamelCase(from.getName());
            s.add("");
            s.add("        // " + from.getRemarks() + " の共生元チェック TODO できればAssertTrueにしたい");
            s.add("        Map<String, Object> " + fromI + "Params = new java.util.HashMap<String, Object>();");
            String lastKey = null;
            for (String fromPrimaryKey : from.getPrimaryKeys()) {
                lastKey = StringUtil.toCamelCase(fromPrimaryKey);
                s.add("        " + fromI + "Params.put(\"" + lastKey + "\", this.get"
                        + StringUtil.toPascalCase(fromPrimaryKey) + "());");
            }
            s.add("        baseProcess.masterCheck(errors, \"" + fromE + "Search\", \"" + lastKey + "\", " + fromI
                    + "Params, jp.co.golorp.emarf.util.Messages.get(\"" + e + "\"));");
        }

        // 転生元のマスタチェック
        if (table.getRebornFrom() != null) {
            TableInfo from = table.getRebornFrom();
            String fromE = StringUtil.toPascalCase(from.getName());
            String fromI = StringUtil.toCamelCase(from.getName());
            s.add("");
            s.add("        // " + from.getRemarks() + " の転生元チェック TODO できればAssertTrueにしたい");
            s.add("        Map<String, Object> " + fromI + "Params = new java.util.HashMap<String, Object>();");
            String lastKey = null;
            for (String fromPrimaryKey : from.getPrimaryKeys()) {
                lastKey = StringUtil.toCamelCase(fromPrimaryKey);
                s.add("        " + fromI + "Params.put(\"" + lastKey + "\", this.get"
                        + StringUtil.toPascalCase(fromPrimaryKey) + "());");
            }
            s.add("        baseProcess.masterCheck(errors, \"" + fromE + "Search\", \"" + lastKey + "\", " + fromI
                    + "Params, jp.co.golorp.emarf.util.Messages.get(\"" + e + "\"));");
        }

        // 集約先のマスタチェック
        if (table.getSummaryTo() != null) {
            TableInfo from = table.getSummaryTo();
            String fromE = StringUtil.toPascalCase(from.getName());
            String fromI = StringUtil.toCamelCase(from.getName());
            s.add("");
            s.add("        // " + from.getRemarks() + " の集約先チェック TODO できればAssertTrueにしたい");
            s.add("        Map<String, Object> " + fromI + "Params = new java.util.HashMap<String, Object>();");
            String lastKey = null;
            for (String fromPrimaryKey : from.getPrimaryKeys()) {
                lastKey = StringUtil.toCamelCase(fromPrimaryKey);
                s.add("        " + fromI + "Params.put(\"" + lastKey + "\", this.get"
                        + StringUtil.toPascalCase(fromPrimaryKey) + "());");
            }
            s.add("        baseProcess.masterCheck(errors, \"" + fromE + "Search\", \"" + lastKey + "\", " + fromI
                    + "Params, jp.co.golorp.emarf.util.Messages.get(\"" + e + "\"));");
        }

        // 列ごとに評価
        for (ColumnInfo column : table.getColumns().values()) {

            // 登録者か更新者ならスキップ
            if (BeanGenerator.isMetaBy(column.getName())) {
                continue;
            }

            // 参照モデルがなければスキップ
            if (column.getRefer() == null) {
                continue;
            }

            // 参照モデルがビューならスキップ
            if (column.getRefer().isView()) {
                continue;
            }

            // 参照モデルがワークフローならスキップ
            if (column.getRefer().isStatusFlow()) {
                continue;
            }

            TableInfo refer = column.getRefer();
            String p = StringUtil.toCamelCase(column.getName());
            s.add("");
            s.add("        // " + column.getRemarks() + " のマスタチェック TODO できればAssertTrueにしたい");
            s.add("        Map<String, Object> " + p + "Params = new java.util.HashMap<String, Object>();");

            // 該当する主キーと比べて、カラム名の接頭辞を判定する
            String keyPrefix = "";
            for (String pk : refer.getPrimaryKeys()) {
                if (column.getName().matches("(?i).+" + pk + "$")) {
                    keyPrefix = column.getName().replaceAll("(?i)" + pk + "$", "");
                    if (!keyPrefix.isEmpty()) {
                        keyPrefix += "_";
                    }
                    break;
                }
            }

            for (String pk : refer.getPrimaryKeys()) {
                String keySuf = "";
                if (refer.getColumns().get(pk).getDataType().equals("String")) {
                    keySuf = "Full";
                }
                s.add("        " + p + "Params.put(\"" + StringUtil.toCamelCase(pk) + keySuf + "\", this.get"
                        + StringUtil.toPascalCase(keyPrefix + pk) + "());");
            }

            String cls = StringUtil.toPascalCase(refer.getName());
            s.add("        baseProcess.masterCheck(errors, \"" + cls + "Search\", \"" + p + "\", " + p
                    + "Params, jp.co.golorp.emarf.util.Messages.get(\"" + e + "." + p + "\"));");
        }

        s.add("    }");
    }

    /**
     * @param s
     * @param javadoc
     */
    private static void addAuthor(final List<String> s, final String javadoc) {
        s.add("");
        s.add("/**");
        s.add(" * " + javadoc);
        s.add(" *");
        s.add(" * @author emarfkrow");
        s.add(" */");
    }

    /**
     * @param s
     */
    public static void addImports(final List<String> s) {
        s.add("");
        s.add("import java.util.Map;");
        s.add("");
        s.add("import org.slf4j.Logger;");
        s.add("import org.slf4j.LoggerFactory;");
        s.add("");
        s.add("import jp.co.golorp.emarf.process.BaseProcess;");
        s.add("import jp.co.golorp.emarf.validation.IForm;");
    }

    /**
     * 詳細画面 フォームチェック追加
     * @param s 出力文字列のリスト
     * @param table テーブル情報
     * @param column カラム情報
     */
    private static void javaFormDetailRegistChecks(final List<String> s, final TableInfo table,
            final ColumnInfo column) {

        // 適用日を含む主キー
        List<String> keys = new ArrayList<String>(table.getPrimaryKeys());

        // 適用日以外の主キー
        List<String> koKeys = new ArrayList<String>(table.getPrimaryKeys());
        koKeys.remove(tekiyoBi);

        String colName = column.getName();
        String registGroup = "groups = jp.co.golorp.emarf.validation.Regist.class";
        String regDelGroups = "groups = { jp.co.golorp.emarf.validation.Regist.class, jp.co.golorp.emarf.validation.Delete.class }";

        // 必須チェック
        if (column.getNullable() == 0) {

            if (column.isNumbering() /*&& ci.getColumnName().equals(pks.get(pks.size() - 1)) 一旦、採番キーならスキップに戻す*/) {

                // 採番キーなら除外
                LOG.trace("skip NotBlank.");

                // フラグでも必須チェックを掛ける
                //            } else if (StringUtil.endsWith(inputFlagSuffixs, ci.getColumnName())) {
                //
                //                // フラグも除外
                //                LOG.trace("skip NotBlank.");

            } else if (column.isPk() && table.getParents().size() > 0
                    && !column.getName().matches("(?i)^" + keys.get(keys.size() - 1) + "$")
                    && !column.getName().matches("(?i)^" + koKeys.get(koKeys.size() - 1) + "$")) {

                // 最終キーでなければ、親から取得するはずなので除外
                LOG.trace("skip NotBlank.");

            } else if (!column.isPk() && column.getTypeName().equals("CHAR")
                    && !StringUtil.isNullOrWhiteSpace(charNotNullRe) && !colName.matches(charNotNullRe)) {

                // 主キー以外のCHAR列で、必須CHAR指定に合致しない場合、NULLならスペースを補填する
                LOG.trace("skip NotBlank.");

            } else if (!column.isPk() && column.getTypeName().equals("NUMBER")
                    && !StringUtil.isNullOrWhiteSpace(numberNullableRe) && colName.matches(numberNullableRe)) {

                // 主キー以外のNUMBER列で、非必須INT指定に合致する場合、NULLなら「0」を補填する
                LOG.trace("skip NotBlank.");

            } else if (StringUtil.endsWith(inputTimestampSuffixs, colName)) {

                // タイムスタンプならスキップ
                LOG.trace("skip NotBlank.");

            } else {

                // 主キー以外は登録時のみ必須チェック（削除時はチェックしない）
                if (column.isPk()) {
                    s.add("    @jakarta.validation.constraints.NotBlank(" + regDelGroups + ")");
                } else {
                    s.add("    @jakarta.validation.constraints.NotBlank(" + registGroup + ")");
                }
            }
        }

        int matchLength = 0;
        String validSuffix = null;
        for (String suffix : validSuffixs) {
            Pattern pattern = Pattern.compile("(?i).*(" + suffix + ")$");
            Matcher matcher = pattern.matcher(colName);
            if (matcher.find()) {
                String matched = matcher.group(1);
                if (matchLength < matched.length()) {
                    matchLength = matched.length();
                    validSuffix = suffix;
                }
            }
        }

        if (validSuffix != null) {
            // Patternの指定がある場合

            String re = bundle.getString("valid." + validSuffix);
            s.add("    @jakarta.validation.constraints.Pattern(" + registGroup + ", regexp = \"" + re + "\")");

            // 桁数チェックは正規表現に任せる。<input type="month">が9999-99になるため。
            //            if (column.getTypeName().contains("CHAR")) {
            //                s.add("    @jakarta.validation.constraints.Size(max = " + column.getColumnSize() + ")");
            //            }
            String type = HtmlGenerator.getInputType(column.getName());
            if (type.equals("text") && column.getTypeName().contains("CHAR")) {
                s.add("    @jakarta.validation.constraints.Size(" + registGroup + ", max = " + column.getColumnSize()
                        + ")");
            }

        } else {
            // Patternの指定がない場合

            int columnSize = column.getColumnSize();

            // 形式チェック
            if (column.getTypeName().startsWith("INT") || column.getTypeName().equals("DECIMAL")
                    || column.getTypeName().equals("DOUBLE") || column.getTypeName().equals("NUMBER")
                    || column.getTypeName().equals("NUMERIC")) {

                // DECIMALの場合（整数桁・小数桁）
                int decimalDigits = column.getDecimalDigits();
                int integer = columnSize - decimalDigits;
                String re = "-?([0-9]{0," + integer + "}\\\\.?[0-9]{0," + decimalDigits + "}?)?";
                s.add("    @jakarta.validation.constraints.Pattern(" + registGroup + ", regexp = \"" + re + "\")");

            } else {
                // DECIMAL以外

                // 上記以外の場合は最大桁チェック
                s.add("    @jakarta.validation.constraints.Size(" + registGroup + ", max = " + columnSize + ")");
            }
        }

        if (column.isPk()) {
            s.add("    @jp.co.golorp.emarf.validation.PrimaryKeys");
        } else if (column.getName().matches("(?i)^" + updateTs + "$")) {
            s.add("    @jp.co.golorp.emarf.validation.OptLock");
        }
    }

    /**
     * 検索画面 フォーム出力
     * @param tableInfos テーブル情報のリスト
     */
    private static void javaFormIndexRegist(final List<TableInfo> tableInfos) {

        // 出力フォルダを再作成
        String packagePath = pkgF.replace(".", File.separator);
        String packageDir = projectDir + File.separator + javaDir + File.separator + packagePath;

        Map<String, String> javaFilePaths = new LinkedHashMap<String, String>();

        for (TableInfo table : tableInfos) {

            if (table.isHistory() || table.isView() || table.isStatusFlow()) {
                continue;
            }

            String tableName = table.getName();
            String remarks = table.getRemarks();
            String entity = StringUtil.toPascalCase(tableName);
            String instance = StringUtil.toCamelCase(tableName);

            List<String> s = new ArrayList<String>();
            s.add("package " + pkgF + ";");
            s.add("");
            s.add("import java.util.List;");
            s.add("import java.util.Map;");
            s.add("");
            s.add("// import org.slf4j.Logger;");
            s.add("// import org.slf4j.LoggerFactory;");
            s.add("");
            s.add("import jakarta.validation.Valid;");
            s.add("import jp.co.golorp.emarf.process.BaseProcess;");
            s.add("import jp.co.golorp.emarf.validation.IForm;");
            addAuthor(s, remarks + "一覧登録フォーム");
            s.add("public class " + entity + "SRegistForm implements IForm {");
            s.add("");
            s.add("    // /** logger */");
            s.add("    // private static final Logger LOG = LoggerFactory.getLogger(" + entity + "RegistForm.class);");
            s.add("");
            s.add("    /** " + table.getRemarks() + "登録フォームのリスト */");
            s.add("    @Valid");
            s.add("    private List<" + entity + "RegistForm> " + instance + "Grid;");
            s.add("");
            s.add("    /**");
            s.add("     * @return " + table.getRemarks() + "登録フォームのリスト");
            s.add("     */");
            s.add("    public List<" + entity + "RegistForm> get" + entity + "Grid() {");
            s.add("        return " + instance + "Grid;");
            s.add("    }");
            s.add("");
            s.add("    /**");
            s.add("     * @param p " + table.getRemarks() + "登録フォームのリスト");
            s.add("     */");
            s.add("    public void set" + entity + "Grid(final List<" + entity + "RegistForm> p) {");
            s.add("        this." + instance + "Grid = p;");
            s.add("    }");
            s.add("");
            s.add("    /** 関連チェック */");
            s.add("    @Override");
            s.add("    public void validate(final Map<String, String> errors, final BaseProcess baseProcess) {");
            s.add("        if (this." + instance + "Grid != null) {");
            s.add("            for (int i = 0; i < this." + instance + "Grid.size(); i++) {");
            s.add("                " + entity + "RegistForm form = this." + instance + "Grid.get(i);");
            s.add("                if (form != null) {");
            s.add("                    Map<String, String> gridErrors = new java.util.LinkedHashMap<String, String>();");
            s.add("                    form.validate(gridErrors, baseProcess);");
            s.add("                    BaseProcess.copyGridErrors(errors, \"" + entity + "Grid\", i, gridErrors);");
            s.add("                }");
            s.add("            }");
            s.add("        }");
            s.add("    }");
            s.add("");
            s.add("}");

            String javaFilePath = packageDir + File.separator + entity + "SRegistForm.java";
            javaFilePaths.put(javaFilePath, pkgF + "." + entity + "SRegistForm");

            FileUtil.writeFile(javaFilePath, s);
        }

        if (isGenerateAtStartup) {
            for (Entry<String, String> e : javaFilePaths.entrySet()) {
                BeanGenerator.javaCompile(e.getKey(), e.getValue());
            }
        }
    }
}
