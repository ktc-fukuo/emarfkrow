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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import jp.co.golorp.emarf.io.FileUtil;
import jp.co.golorp.emarf.lang.StringUtil;
import jp.co.golorp.emarf.properties.App;
import jp.co.golorp.emarf.util.ResourceBundles;

/**
 * 詳細画面アクション出力
 */
public final class DetailActionGenerator {

    /** 起動時の自動生成か */
    private static boolean isGenerateAtStartup;

    /** プロジェクトディレクトリ */
    private static String projectDir;

    /** BeanGenerator.properties */
    private static ResourceBundle bundle = ResourceBundles.getBundle(BeanGenerator.class);
    /** 適用日カラム名 */
    private static String tekiyoBi;
    /** 削除フラグ */
    private static String deleteF;
    /** ステータス区分 */
    private static String status;

    /** javaファイル出力ルートパス */
    private static String javaDir = "src\\main\\java";

    /** actionパッケージ */
    private static String pkgA = "com.example.action.model.base";
    /** entityパッケージ */
    private static String pkE = "com.example.entity";

    /** プライベートコンストラクタ */
    private DetailActionGenerator() {
    }

    /**
     * 各ファイル出力 主処理
     * @param dir プロジェクトのディレクトリ
     * @param tables
     */
    public static void generate(final String dir, final List<TableInfo> tables) {

        //webからの自動生成ならコンパイルまで行う
        if (App.get("generateAtStartup") != null) {
            isGenerateAtStartup = App.get("generateAtStartup").toLowerCase().equals("true");
        }

        //プロジェクトディレクトリを退避
        projectDir = dir;

        if (bundle != null) {

            tekiyoBi = bundle.getString("column.start");
            deleteF = bundle.getString("column.delete");
            status = bundle.getString("column.status");

            javaDir = bundle.getString("dir.java");

            pkgA = bundle.getString("java.package.action") + ".model.base";
            pkE = bundle.getString("java.package.entity");
        }

        javaActionDetailRegist(tables);
        javaActionDetailGet(tables);
        javaActionDetailDelete(tables);
        if (!StringUtil.isNullOrWhiteSpace(status)) {
            javaActionDetailApply(tables);
            javaActionDetailCancel(tables);
            javaActionDetailPermit(tables);
            javaActionDetailForbid(tables);
        }
    }

    /**
     * 詳細画面 登録処理出力
     * @param tables テーブル情報のリスト
     */
    private static void javaActionDetailRegist(final List<TableInfo> tables) {
        // 出力フォルダを再作成
        String packagePath = pkgA.replace(".", File.separator);
        String packageDir = projectDir + File.separator + javaDir + File.separator + packagePath;
        Map<String, String> javaFilePaths = new LinkedHashMap<String, String>();
        for (TableInfo table : tables) {
            if (table.isHistory() || table.isView() || table.isStatusFlow()) {
                continue;
            }
            if (table.getPrimaryKeys().size() == 0) {
                continue;
            }
            String entity = StringUtil.toPascalCase(table.getName());
            String r = table.getRemarks();
            List<String> s = new ArrayList<String>();
            s.add("package " + pkgA + ";");
            s.add("");
            s.add("import java.time.LocalDateTime;");
            s.add("import java.util.HashMap;");
            s.add("import java.util.Map;");
            s.add("");
            s.add("import " + pkE + "." + entity + ";");
            s.add("");
            s.add("import jp.co.golorp.emarf.action.BaseAction;");
            s.add("import jp.co.golorp.emarf.exception.OptLockError;");
            s.add("import jp.co.golorp.emarf.util.Messages;");
            s.add("import jp.co.golorp.emarf.validation.FormValidator;");
            s.add("");
            s.add("/**");
            s.add(" * " + r + "登録");
            s.add(" *");
            s.add(" * @author emarfkrow");
            s.add(" */");
            s.add("public class " + entity + "RegistAction extends BaseAction {");
            s.add("");
            s.add("    /** " + r + "登録処理 */");
            s.add("    @Override");
            s.add("    public Map<String, Object> running(final LocalDateTime at, final String by, final Map<String, Object> form) {");
            s.add("");
            s.add("        Map<String, Object> map = new HashMap<String, Object>();");
            s.add("");
            s.add("        " + entity + " e = FormValidator.toBean(" + entity + ".class.getName(), form);");
            //            if (!table.isView() && !StringUtil.isNullOrWhiteSpace(status) && table.getColumns().containsKey(status)) {
            //                s.add("");
            //                s.add("        e.set" + StringUtil.toPascalCase(status) + "(0);");
            //            }
            s.add("");
            s.add("        if (e.isNew()) {");
            s.add("");
            s.add("            if (e.insert(at, by) != 1) {");
            s.add("                throw new OptLockError(\"error.cant.insert\", \"" + r + "\");");
            s.add("            }");
            if (table.getSummaryOfs().size() > 0) {
                int sCount = 0;
                for (TableInfo summaryOf : table.getSummaryOfs()) {
                    if (summaryOf.getPrimaryKeys().size() != 1) {
                        continue;
                    }
                    ++sCount;
                    String sKey = "summaryKey" + sCount;
                    String e = StringUtil.toPascalCase(summaryOf.getName());
                    String i = StringUtil.toCamelCase(summaryOf.getName());
                    String pk = summaryOf.getPrimaryKeys().get(0);
                    String prop = StringUtil.toCamelCase(pk);
                    s.add("");
                    s.add("            //集約先に該当する場合は、集約元に主キーを反映");
                    s.add("            String " + sKey + " = form.get(\"" + e + "." + prop + "\").toString();");
                    s.add("            if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrWhiteSpace(" + sKey + ")) {");
                    s.add("                String[] summaryKeys = " + sKey + ".trim().split(\",\");");
                    s.add("                for (String pk : summaryKeys) {");
                    s.add("                    " + pkE + "." + e + " " + i + " = " + pkE + "." + e + ".get(pk);");
                    if (!StringUtil.isNullOrWhiteSpace(status) && summaryOf.getColumns().containsKey(status)) {
                        String acc = StringUtil.toPascalCase(status);
                        s.add("                    //承認済みでなければエラー");
                        s.add("                    if (!" + i + ".get" + acc + "().equals(\"1\")) {");
                        s.add("                        throw new OptLockError(\"error.nopermit.summary\", \""
                                + summaryOf.getRemarks() + "\");");
                        s.add("                    }");
                    }
                    for (String fk : table.getPrimaryKeys()) {
                        String acc = StringUtil.toPascalCase(fk);
                        s.add("                    //集約済みならエラー");
                        s.add("                    if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrWhiteSpace(" + i
                                + ".get" + acc + "())) {");
                        s.add("                        throw new OptLockError(\"error.already.summary\", \""
                                + summaryOf.getRemarks() + "\");");
                        s.add("                    }");
                        s.add("                    " + i + ".set" + acc + "(e.get" + acc + "());");
                    }
                    s.add("                    if (" + i + ".update(at, by) != 1) {");
                    s.add("                        throw new OptLockError(\"error.cant.insert\", \""
                            + summaryOf.getRemarks() + "\");");
                    s.add("                    }");
                    s.add("                }");
                    s.add("            }");
                }
            }
            s.add("");
            s.add("            map.put(\"INFO\", Messages.get(\"info.insert\"));");
            s.add("");
            s.add("        } else {");
            s.add("");
            s.add("            if (e.update(at, by) == 1) {");
            s.add("                map.put(\"INFO\", Messages.get(\"info.update\"));");
            s.add("            } else if (e.insert(at, by) == 1) {");
            s.add("                map.put(\"INFO\", Messages.get(\"info.insert\"));");
            s.add("            } else {");
            s.add("                throw new OptLockError(\"error.cant.update\", \"" + r + "\");");
            s.add("            }");
            s.add("        }");
            s.add("");
            s.add("        return map;");
            s.add("    }");
            s.add("");
            s.add("}");

            String javaFilePath = packageDir + File.separator + entity + "RegistAction.java";
            javaFilePaths.put(javaFilePath, pkgA + "." + entity + "RegistAction");

            FileUtil.writeFile(javaFilePath, s);
        }

        if (isGenerateAtStartup) {
            for (Entry<String, String> e : javaFilePaths.entrySet()) {
                BeanGenerator.javaCompile(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 詳細画面 登録処理出力
     * @param tables テーブル情報のリスト
     */
    private static void javaActionDetailGet(final List<TableInfo> tables) {
        // 出力フォルダを再作成
        String pPath = pkgA.replace(".", File.separator);
        String pDir = projectDir + File.separator + javaDir + File.separator + pPath;
        Map<String, String> javaPaths = new LinkedHashMap<String, String>();
        for (TableInfo table : tables) {
            if (table.getPrimaryKeys().size() == 0) {
                continue;
            }
            //entity
            String ent = StringUtil.toPascalCase(table.getName());
            //instance
            String ins = StringUtil.toCamelCase(table.getName());
            List<String> s = new ArrayList<String>();
            s.add("package " + pkgA + ";");
            s.add("");
            s.add("import java.time.LocalDateTime;");
            s.add("import java.util.HashMap;");
            s.add("import java.util.Map;");
            s.add("");
            s.add("import " + pkE + "." + ent + ";");
            s.add("");
            s.add("import jp.co.golorp.emarf.action.BaseAction;");
            s.add("import jp.co.golorp.emarf.exception.NoDataError;");
            s.add("");
            s.add("/**");
            s.add(" * " + table.getName() + "照会");
            s.add(" *");
            s.add(" * @author emarfkrow");
            s.add(" */");
            s.add("public class " + ent + "GetAction extends BaseAction {");
            s.add("");
            s.add("    /** " + table.getName() + "照会処理 */");
            s.add("    @Override");
            s.add("    public Map<String, Object> running(final LocalDateTime at, final String by, final Map<String, Object> form) {");
            s.add("");
            s.add("        Map<String, Object> map = new HashMap<String, Object>();");
            String pks = "";
            if (table.getPrimaryKeys().size() > 0) {
                s.add("");
                s.add("        // 主キーのチェック");
                s.add("        boolean isAllKey = true;");
                Set<String> donePks = new HashSet<String>();
                for (int i = 0; i < table.getPrimaryKeys().size(); i++) {
                    String pk = table.getPrimaryKeys().get(i);
                    if (!donePks.contains(pk)) {
                        donePks.add(pk);
                        String property = StringUtil.toCamelCase(pk);
                        s.add("");
                        s.add("        Object " + property + " = form.get(\"" + property + "\");");
                        s.add("        if (" + property + " == null) {");
                        s.add("            " + property + " = form.get(\"" + ent + "." + property + "\");");
                        s.add("        }");
                        s.add("        if (" + property + " == null) {");
                        if (i == 0) {
                            // 転生元から情報をコピー
                            copyFromRebornee(s, table, tables);
                        }
                        s.add("            isAllKey = false;");
                        s.add("        }");
                    }
                }
                if (table.getParents().size() > 0) {
                    s.add("");
                    s.add("        // 親モデルの取得");
                }
                for (int i = 0; i < table.getPrimaryKeys().size(); i++) {
                    String pk = table.getPrimaryKeys().get(i);
                    if (pks.length() > 0) {
                        pks += ", ";
                    }
                    pks += pk;
                    for (TableInfo parent : table.getParents()) {
                        List<String> oyaKeys = new ArrayList<String>(parent.getPrimaryKeys());
                        String oyaPKs = oyaKeys.toString().replaceAll("[\\[\\]]", "");
                        oyaKeys.remove(tekiyoBi);
                        String oyaKeyCsv = oyaKeys.toString().replaceAll("[\\[\\]]", "");
                        if (!pks.equals(oyaKeyCsv)) {
                            continue;
                        }
                        String pE = StringUtil.toPascalCase(parent.getName());
                        String pI = StringUtil.toCamelCase(parent.getName());
                        String oyaKey = StringUtil.toCamelCase(oyaPKs);
                        s.add("        try {"); // 片親の場合もあるので親なしでもエラーにしない
                        s.add("            " + pkE + "." + pE + " " + pI + " = " + pkE + "." + pE + ".get(" + oyaKey
                                + ");");
                        s.add("            map.put(\"" + pE + "\", " + pI + ");");
                        s.add("        } catch (Exception e) {");
                        s.add("        }");
                    }
                }
                s.add("");
                s.add("        // 主キーが不足していたら終了");
                s.add("        if (!isAllKey) {");
                s.add("            return map;");
                s.add("        }");
            }
            s.add("");
            s.add("        try {");
            s.add("            " + ent + " " + ins + " = " + ent + ".get(" + StringUtil.toCamelCase(pks) + ");");
            if (table.getBrothers().size() > 0) {
                s.add("            // 兄弟");
                for (TableInfo bros : table.getBrothers()) {
                    s.add("            " + ins + ".refer" + StringUtil.toPascalCase(bros.getName()) + "();");
                }
            }
            if (table.getChildren().size() > 0) {
                s.add("            // 子");
                for (TableInfo child : table.getChildren()) {
                    s.add("            " + ins + ".refer" + StringUtil.toPascalCase(child.getName()) + "s();");
                }
            }
            // 転生先リスト
            if (table.getRebornTo() != null) {
                s.add("            // 転生先");
                String pascal = StringUtil.toPascalCase(table.getRebornTo().getName());
                s.add("            " + ins + ".refer" + pascal + "s();");
            }
            // 集約元リスト
            if (table.getSummaryOfs().size() > 0) {
                s.add("            // 集約元");
                for (TableInfo summaryOf : table.getSummaryOfs()) {
                    String pascal = StringUtil.toPascalCase(summaryOf.getName());
                    s.add("            " + ins + ".refer" + pascal + "s();");
                }
            }
            s.add("            map.put(\"" + ent + "\", " + ins + ");");
            s.add("        } catch (NoDataError e) {");
            s.add("            if (form.get(\"IsSilent\") == null || !form.get(\"IsSilent\").equals(\"true\")) {");
            s.add("                throw e;");
            s.add("            }");
            s.add("        }");
            s.add("");
            s.add("        return map;");
            s.add("    }");
            s.add("");
            s.add("}");

            String javaFilePath = pDir + File.separator + ent + "GetAction.java";
            javaPaths.put(javaFilePath, pkgA + "." + ent + "GetAction");

            FileUtil.writeFile(javaFilePath, s);
        }

        if (isGenerateAtStartup) {
            for (Entry<String, String> e : javaPaths.entrySet()) {
                BeanGenerator.javaCompile(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 詳細画面 登録処理出力
     * @param tables テーブル情報のリスト
     */
    private static void javaActionDetailDelete(final List<TableInfo> tables) {

        // 出力フォルダを再作成
        String packagePath = pkgA.replace(".", File.separator);
        String packageDir = projectDir + File.separator + javaDir + File.separator + packagePath;

        Map<String, String> javaFilePaths = new LinkedHashMap<String, String>();

        for (TableInfo table : tables) {

            if (table.isHistory() || table.isView() || table.isStatusFlow()) {
                continue;
            }

            if (table.getPrimaryKeys().size() == 0) {
                continue;
            }

            // 論理削除テーブルならスキップ（削除フラグ指定があり、テーブルに削除フラグがある場合）
            if (!StringUtil.isNullOrWhiteSpace(deleteF) && table.getColumns().containsKey(deleteF)) {
                continue;
            }

            String e = StringUtil.toPascalCase(table.getName());

            List<String> s = new ArrayList<String>();
            s.add("package " + pkgA + ";");
            s.add("");
            s.add("import java.time.LocalDateTime;");
            s.add("import java.util.HashMap;");
            s.add("import java.util.Map;");
            s.add("");
            s.add("import " + pkE + "." + e + ";");
            s.add("");
            s.add("import jp.co.golorp.emarf.action.BaseAction;");
            s.add("import jp.co.golorp.emarf.exception.OptLockError;");
            s.add("import jp.co.golorp.emarf.util.Messages;");
            s.add("import jp.co.golorp.emarf.validation.FormValidator;");
            s.add("");
            s.add("/**");
            s.add(" * " + table.getRemarks() + "削除");
            s.add(" *");
            s.add(" * @author emarfkrow");
            s.add(" */");
            s.add("public class " + e + "DeleteAction extends BaseAction {");
            s.add("");
            s.add("    /** " + table.getRemarks() + "削除処理 */");
            s.add("    @Override");
            s.add("    public Map<String, Object> running(final LocalDateTime at, final String by, final Map<String, Object> form) {");
            s.add("");
            s.add("        // 主キーが不足していたらエラー");

            String params = "";
            for (String primaryKey : table.getPrimaryKeys()) {
                String camel = StringUtil.toCamelCase(primaryKey);
                s.add("        Object " + camel + " = form.get(\"" + camel + "\");");
                s.add("        if (" + camel + " == null) {");
                s.add("            " + camel + " = form.get(\"" + e + "." + camel + "\");");
                s.add("        }");
                s.add("        if (" + camel + " == null) {");
                s.add("            throw new OptLockError(\"error.cant.delete\", \"" + table.getRemarks() + "\");");
                s.add("        }");
                if (params.length() > 0) {
                    params += ", ";
                }
                params += camel;
            }

            s.add("");
            s.add("        " + e + " e = FormValidator.toBean(" + e + ".class.getName(), form);");

            BeanGenerator.getDeleteChilds(s, "e", table.getChildren(), 0);

            s.add("        if (e.delete() != 1) {");
            s.add("            throw new OptLockError(\"error.cant.delete\", \"" + table.getRemarks() + "\");");
            s.add("        }");
            s.add("");
            s.add("        Map<String, Object> map = new HashMap<String, Object>();");
            s.add("        map.put(\"INFO\", Messages.get(\"info.delete\"));");
            s.add("        return map;");
            s.add("    }");
            s.add("");
            s.add("}");

            String javaFilePath = packageDir + File.separator + e + "DeleteAction.java";
            javaFilePaths.put(javaFilePath, pkgA + "." + e + "DeleteAction");

            FileUtil.writeFile(javaFilePath, s);
        }

        if (isGenerateAtStartup) {
            for (Entry<String, String> e : javaFilePaths.entrySet()) {
                BeanGenerator.javaCompile(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 詳細画面 申請処理出力
     * @param tables テーブル情報のリスト
     */
    private static void javaActionDetailApply(final List<TableInfo> tables) {

        // 出力フォルダを再作成
        String packagePath = pkgA.replace(".", File.separator);
        String packageDir = projectDir + File.separator + javaDir + File.separator + packagePath;

        Map<String, String> javaFilePaths = new LinkedHashMap<String, String>();

        for (TableInfo table : tables) {

            if (table.isHistory() || table.isView() || table.isStatusFlow()
                    || !table.getColumns().containsKey(status)) {
                continue;
            }

            String entity = StringUtil.toPascalCase(table.getName());
            String remarks = table.getRemarks();

            List<String> s = new ArrayList<String>();
            s.add("package " + pkgA + ";");
            s.add("");
            s.add("import java.time.LocalDateTime;");
            s.add("import java.util.HashMap;");
            s.add("import java.util.Map;");
            s.add("");
            s.add("import " + pkE + "." + entity + ";");
            s.add("");
            s.add("import jp.co.golorp.emarf.action.BaseAction;");
            s.add("import jp.co.golorp.emarf.exception.OptLockError;");
            s.add("import jp.co.golorp.emarf.util.Messages;");
            s.add("import jp.co.golorp.emarf.validation.FormValidator;");
            s.add("");
            s.add("/**");
            s.add(" * " + remarks + "申請");
            s.add(" *");
            s.add(" * @author emarfkrow");
            s.add(" */");
            s.add("public class " + entity + "ApplyAction extends BaseAction {");
            s.add("");
            s.add("    /** " + remarks + "申請処理 */");
            s.add("    @Override");
            s.add("    public Map<String, Object> running(final LocalDateTime at, final String by, final Map<String, Object> form) {");
            s.add("");
            s.add("        // 主キーが不足していたらエラー");
            String params = "";
            for (String primaryKey : table.getPrimaryKeys()) {
                String property = StringUtil.toCamelCase(primaryKey);
                String accessor = StringUtil.toPascalCase(primaryKey);
                s.add("        Object " + property + " = form.get(\"" + property + "\");");
                s.add("        if (" + property + " == null) {");
                s.add("            " + property + " = form.get(\"" + entity + "." + property + "\");");
                s.add("        }");
                s.add("        if (" + property + " == null) {");
                s.add("            throw new OptLockError(\"error.cant.apply\", \"" + remarks + "\");");
                s.add("        }");
                if (params.length() > 0) {
                    params += ", ";
                }
                params += "e.get" + accessor + "()";
            }
            s.add("");
            s.add("        " + entity + " e = FormValidator.toBean(" + entity + ".class.getName(), form);");
            List<TableInfo> childInfos = table.getChildren();
            BeanGenerator.getApplyChilds(s, "e", childInfos, 0);
            s.add("");
            //s.add("        " + entity + " f = " + entity + ".get(" + params + ");");
            if (table.getColumns().containsKey(status)) {
                String acc = StringUtil.toPascalCase(status);
                String fld = StringUtil.toCamelCase(status);
                s.add("        if (e.get" + acc + "() != null && !e.get" + acc + "().equals(\"\")) {");
                s.add("            throw new jp.co.golorp.emarf.exception.AppError(\"error.notmatch\",");
                s.add("                    Messages.get(\"" + entity + "." + fld
                        + "\"), Messages.get(\"common.notapply\"));");
                s.add("        }");
                s.add("        e.set" + acc + "(0);");
            }
            s.add("        if (e.update(at, by) != 1) {");
            s.add("            throw new OptLockError(\"error.cant.apply\", \"" + remarks + "\");");
            s.add("        }");
            s.add("");
            s.add("        Map<String, Object> map = new HashMap<String, Object>();");
            s.add("        map.put(\"INFO\", Messages.get(\"info.apply\"));");
            s.add("        return map;");
            s.add("    }");
            s.add("");
            s.add("}");

            String javaFilePath = packageDir + File.separator + entity + "ApplyAction.java";
            javaFilePaths.put(javaFilePath, pkgA + "." + entity + "ApplyAction");

            FileUtil.writeFile(javaFilePath, s);
        }

        if (isGenerateAtStartup) {
            for (Entry<String, String> e : javaFilePaths.entrySet()) {
                BeanGenerator.javaCompile(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 詳細画面 取消処理出力
     * @param tables テーブル情報のリスト
     */
    private static void javaActionDetailCancel(final List<TableInfo> tables) {

        // 出力フォルダを再作成
        String packagePath = pkgA.replace(".", File.separator);
        String packageDir = projectDir + File.separator + javaDir + File.separator + packagePath;

        Map<String, String> javaFilePaths = new LinkedHashMap<String, String>();

        for (TableInfo table : tables) {

            if (table.isHistory() || table.isView() || table.isStatusFlow()
                    || !table.getColumns().containsKey(status)) {
                continue;
            }

            String entity = StringUtil.toPascalCase(table.getName());
            String remarks = table.getRemarks();

            List<String> s = new ArrayList<String>();
            s.add("package " + pkgA + ";");
            s.add("");
            s.add("import java.time.LocalDateTime;");
            s.add("import java.util.HashMap;");
            s.add("import java.util.Map;");
            s.add("");
            s.add("import " + pkE + "." + entity + ";");
            s.add("");
            s.add("import jp.co.golorp.emarf.action.BaseAction;");
            s.add("import jp.co.golorp.emarf.exception.OptLockError;");
            s.add("import jp.co.golorp.emarf.util.Messages;");
            s.add("import jp.co.golorp.emarf.validation.FormValidator;");
            s.add("");
            s.add("/**");
            s.add(" * " + remarks + "取消");
            s.add(" *");
            s.add(" * @author emarfkrow");
            s.add(" */");
            s.add("public class " + entity + "CancelAction extends BaseAction {");
            s.add("");
            s.add("    /** " + remarks + "取消処理 */");
            s.add("    @Override");
            s.add("    public Map<String, Object> running(final LocalDateTime at, final String by, final Map<String, Object> form) {");
            s.add("");
            s.add("        // 主キーが不足していたらエラー");
            String params = "";
            for (String primaryKey : table.getPrimaryKeys()) {
                String property = StringUtil.toCamelCase(primaryKey);
                String accessor = StringUtil.toPascalCase(primaryKey);
                s.add("        Object " + property + " = form.get(\"" + property + "\");");
                s.add("        if (" + property + " == null) {");
                s.add("            " + property + " = form.get(\"" + entity + "." + property + "\");");
                s.add("        }");
                s.add("        if (" + property + " == null) {");
                s.add("            throw new OptLockError(\"error.cant.cancel\", \"" + remarks + "\");");
                s.add("        }");
                if (params.length() > 0) {
                    params += ", ";
                }
                params += "e.get" + accessor + "()";
            }
            s.add("");
            s.add("        " + entity + " e = FormValidator.toBean(" + entity + ".class.getName(), form);");
            List<TableInfo> childInfos = table.getChildren();
            BeanGenerator.getCancelChilds(s, "e", childInfos, 0);
            s.add("");
            //s.add("        " + entity + " f = " + entity + ".get(" + params + ");");
            if (table.getColumns().containsKey(status)) {
                String acc = StringUtil.toPascalCase(status);
                String fld = StringUtil.toCamelCase(status);
                s.add("        if (!e.get" + acc + "().equals(\"0\") && !e.get" + acc + "().equals(\"-1\")) {");
                s.add("            throw new jp.co.golorp.emarf.exception.AppError(\"error.notmatch\",");
                s.add("                    Messages.get(\"" + entity + "." + fld
                        + "\"), Messages.get(\"common.apply.forbid\"));");
                s.add("        }");
                s.add("        e.set" + acc + "(null);");
            }
            s.add("        if (e.update(at, by) != 1) {");
            s.add("            throw new OptLockError(\"error.cant.cancel\", \"" + remarks + "\");");
            s.add("        }");
            s.add("");
            s.add("        Map<String, Object> map = new HashMap<String, Object>();");
            s.add("        map.put(\"INFO\", Messages.get(\"info.cancel\"));");
            s.add("        return map;");
            s.add("    }");
            s.add("");
            s.add("}");

            String javaFilePath = packageDir + File.separator + entity + "CancelAction.java";
            javaFilePaths.put(javaFilePath, pkgA + "." + entity + "CancelAction");

            FileUtil.writeFile(javaFilePath, s);
        }

        if (isGenerateAtStartup) {
            for (Entry<String, String> e : javaFilePaths.entrySet()) {
                BeanGenerator.javaCompile(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 詳細画面 承認処理出力
     * @param tables テーブル情報のリスト
     */
    private static void javaActionDetailPermit(final List<TableInfo> tables) {

        // 出力フォルダを再作成
        String packagePath = pkgA.replace(".", File.separator);
        String packageDir = projectDir + File.separator + javaDir + File.separator + packagePath;

        Map<String, String> javaFilePaths = new LinkedHashMap<String, String>();

        for (TableInfo table : tables) {

            if (table.isHistory() || table.isView() || table.isStatusFlow()
                    || !table.getColumns().containsKey(status)) {
                continue;
            }

            String entity = StringUtil.toPascalCase(table.getName());
            String remarks = table.getRemarks();

            List<String> s = new ArrayList<String>();
            s.add("package " + pkgA + ";");
            s.add("");
            s.add("import java.time.LocalDateTime;");
            s.add("import java.util.HashMap;");
            s.add("import java.util.Map;");
            s.add("");
            s.add("import " + pkE + "." + entity + ";");
            s.add("");
            s.add("import jp.co.golorp.emarf.action.BaseAction;");
            s.add("import jp.co.golorp.emarf.exception.OptLockError;");
            s.add("import jp.co.golorp.emarf.util.Messages;");
            s.add("import jp.co.golorp.emarf.validation.FormValidator;");
            s.add("");
            s.add("/**");
            s.add(" * " + remarks + "承認");
            s.add(" *");
            s.add(" * @author emarfkrow");
            s.add(" */");
            s.add("public class " + entity + "PermitAction extends BaseAction {");
            s.add("");
            s.add("    /** " + remarks + "承認処理 */");
            s.add("    @Override");
            s.add("    public Map<String, Object> running(final LocalDateTime at, final String by, final Map<String, Object> form) {");
            s.add("");
            s.add("        // 主キーが不足していたらエラー");
            String params = "";
            for (String primaryKey : table.getPrimaryKeys()) {
                String property = StringUtil.toCamelCase(primaryKey);
                String accessor = StringUtil.toPascalCase(primaryKey);
                s.add("        Object " + property + " = form.get(\"" + property + "\");");
                s.add("        if (" + property + " == null) {");
                s.add("            " + property + " = form.get(\"" + entity + "." + property + "\");");
                s.add("        }");
                s.add("        if (" + property + " == null) {");
                s.add("            throw new OptLockError(\"error.cant.permit\", \"" + remarks + "\");");
                s.add("        }");
                if (params.length() > 0) {
                    params += ", ";
                }
                params += "e.get" + accessor + "()";
            }
            s.add("");
            s.add("        " + entity + " e = FormValidator.toBean(" + entity + ".class.getName(), form);");
            List<TableInfo> childInfos = table.getChildren();
            BeanGenerator.getPermitChilds(s, "e", childInfos, 0);
            s.add("");
            //s.add("        " + entity + " f = " + entity + ".get(" + params + ");");
            if (table.getColumns().containsKey(status)) {
                String acc = StringUtil.toPascalCase(status);
                String fld = StringUtil.toCamelCase(status);
                s.add("        if (e.get" + acc + "() != null && !e.get" + acc + "().equals(\"0\")) {");
                s.add("            throw new jp.co.golorp.emarf.exception.AppError(\"error.notmatch\",");
                s.add("                    Messages.get(\"" + entity + "." + fld
                        + "\"), Messages.get(\"common.applied\"));");
                s.add("        }");
                s.add("        e.set" + acc + "(1);");
            }
            s.add("        if (e.update(at, by) != 1) {");
            s.add("            throw new OptLockError(\"error.cant.permit\", \"" + remarks + "\");");
            s.add("        }");
            s.add("");
            s.add("        Map<String, Object> map = new HashMap<String, Object>();");
            s.add("        map.put(\"INFO\", Messages.get(\"info.permit\"));");
            s.add("        return map;");
            s.add("    }");
            s.add("");
            s.add("}");

            String javaFilePath = packageDir + File.separator + entity + "PermitAction.java";
            javaFilePaths.put(javaFilePath, pkgA + "." + entity + "PermitAction");

            FileUtil.writeFile(javaFilePath, s);
        }

        if (isGenerateAtStartup) {
            for (Entry<String, String> e : javaFilePaths.entrySet()) {
                BeanGenerator.javaCompile(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 詳細画面 否認処理出力
     * @param tables テーブル情報のリスト
     */
    private static void javaActionDetailForbid(final List<TableInfo> tables) {

        // 出力フォルダを再作成
        String packagePath = pkgA.replace(".", File.separator);
        String packageDir = projectDir + File.separator + javaDir + File.separator + packagePath;

        Map<String, String> javaFilePaths = new LinkedHashMap<String, String>();

        for (TableInfo table : tables) {

            if (table.isHistory() || table.isView() || table.isStatusFlow()
                    || !table.getColumns().containsKey(status)) {
                continue;
            }

            String entity = StringUtil.toPascalCase(table.getName());
            String remarks = table.getRemarks();

            List<String> s = new ArrayList<String>();
            s.add("package " + pkgA + ";");
            s.add("");
            s.add("import java.time.LocalDateTime;");
            s.add("import java.util.HashMap;");
            s.add("import java.util.Map;");
            s.add("");
            s.add("import " + pkE + "." + entity + ";");
            s.add("");
            s.add("import jp.co.golorp.emarf.action.BaseAction;");
            s.add("import jp.co.golorp.emarf.exception.OptLockError;");
            s.add("import jp.co.golorp.emarf.util.Messages;");
            s.add("import jp.co.golorp.emarf.validation.FormValidator;");
            s.add("");
            s.add("/**");
            s.add(" * " + remarks + "否認");
            s.add(" *");
            s.add(" * @author emarfkrow");
            s.add(" */");
            s.add("public class " + entity + "ForbidAction extends BaseAction {");
            s.add("");
            s.add("    /** " + remarks + "否認処理 */");
            s.add("    @Override");
            s.add("    public Map<String, Object> running(final LocalDateTime at, final String by, final Map<String, Object> form) {");
            s.add("");
            s.add("        // 主キーが不足していたらエラー");
            String params = "";
            for (String primaryKey : table.getPrimaryKeys()) {
                String property = StringUtil.toCamelCase(primaryKey);
                String accessor = StringUtil.toPascalCase(primaryKey);
                s.add("        Object " + property + " = form.get(\"" + property + "\");");
                s.add("        if (" + property + " == null) {");
                s.add("            " + property + " = form.get(\"" + entity + "." + property + "\");");
                s.add("        }");
                s.add("        if (" + property + " == null) {");
                s.add("            throw new OptLockError(\"error.cant.forbid\", \"" + remarks + "\");");
                s.add("        }");
                if (params.length() > 0) {
                    params += ", ";
                }
                params += "e.get" + accessor + "()";
            }
            s.add("");
            s.add("        " + entity + " e = FormValidator.toBean(" + entity + ".class.getName(), form);");
            List<TableInfo> childInfos = table.getChildren();
            BeanGenerator.getForbidChilds(s, "e", childInfos, 0);
            s.add("");
            //s.add("        " + entity + " f = " + entity + ".get(" + params + ");");
            if (table.getColumns().containsKey(status)) {
                String acc = StringUtil.toPascalCase(status);
                String fld = StringUtil.toCamelCase(status);
                s.add("        if (!e.get" + acc + "().equals(\"0\") && !e.get" + acc + "().equals(\"1\")) {");
                s.add("            throw new jp.co.golorp.emarf.exception.AppError(\"error.notmatch\",");
                s.add("                    Messages.get(\"" + entity + "." + fld
                        + "\"), Messages.get(\"common.apply.permit\"));");
                s.add("        }");
                s.add("        e.set" + acc + "(-1);");
            }
            s.add("        if (e.update(at, by) != 1) {");
            s.add("            throw new OptLockError(\"error.cant.forbid\", \"" + remarks + "\");");
            s.add("        }");
            s.add("");
            s.add("        Map<String, Object> map = new HashMap<String, Object>();");
            s.add("        map.put(\"INFO\", Messages.get(\"info.forbid\"));");
            s.add("        return map;");
            s.add("    }");
            s.add("");
            s.add("}");

            String javaFilePath = packageDir + File.separator + entity + "ForbidAction.java";
            javaFilePaths.put(javaFilePath, pkgA + "." + entity + "ForbidAction");

            FileUtil.writeFile(javaFilePath, s);
        }

        if (isGenerateAtStartup) {
            for (Entry<String, String> e : javaFilePaths.entrySet()) {
                BeanGenerator.javaCompile(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * @param s
     * @param table
     * @param tables
     */
    private static void copyFromRebornee(final List<String> s, final TableInfo table, final List<TableInfo> tables) {

        // 自テーブルが転生先か調査する
        String toTbl = table.getName();
        String toE = StringUtil.toPascalCase(toTbl);
        String toI = StringUtil.toCamelCase(toTbl);

        // 自テーブルを転生先とするテーブルがあるかループ
        int froms = 0;
        for (TableInfo frTbl : tables) {

            boolean isTo = false;
            // 今回テーブルの転生先が、当該テーブルか調査
            String kind = "転生";
            if (frTbl.getRebornTo() != null && frTbl.getRebornTo().getName().equals(toTbl)) {
                isTo = true;
            }
            // 今回テーブルの派生先が、当該テーブルか調査
            for (TableInfo deriveTo : frTbl.getDeriveTos()) {
                if (deriveTo.getName().equals(toTbl)) {
                    isTo = true;
                    kind = "派生";
                    break;
                }
            }
            // 今回テーブルの派生先が、当該テーブルか調査
            for (TableInfo choiceOf : frTbl.getChoosers()) {
                if (choiceOf.getName().equals(toTbl)) {
                    isTo = true;
                    kind = "選抜";
                    break;
                }
            }

            if (!isTo) {
                continue;
            }

            // 自テーブルが転生先であった
            if (froms++ == 0) {
                s.add("");
                s.add("            " + toE + " " + toI + " = new " + toE + "();");
            }

            s.add("");
            s.add("            // " + kind + "先になる場合は" + kind + "元から情報をコピー");
            String frK = "";
            String frC = "";
            for (String fromKey : frTbl.getPrimaryKeys()) {
                String k = StringUtil.toCamelCase(fromKey);
                s.add("            Object " + k + froms + " = form.get(\"" + k + "\");");
                s.add("            if (" + k + froms + " == null) {");
                s.add("                " + k + froms + " = form.get(\"" + toE + "." + k + "\");");
                s.add("            }");
                if (!frK.equals("")) {
                    frK += ", ";
                }
                frK += k + froms;
                if (!frC.equals("")) {
                    frC += " && ";
                }
                frC += k + froms + " != null";
            }
            s.add("            if (" + frC + ") {");
            String frName = frTbl.getName();
            String frE = StringUtil.toPascalCase(frName);
            String frI = StringUtil.toCamelCase(frName);
            s.add("                " + pkE + "." + frE + " " + frI + " = " + pkE + "." + frE + ".get(" + frK + ");");
            for (String frColNm : frTbl.getColumns().keySet()) {
                // メタ情報ならスキップ
                if (BeanGenerator.isMeta(frColNm)) {
                    continue;
                }
                // 自テーブルに転生元と同じカラムがあればコピー
                if (table.getColumns().containsKey(frColNm)) {
                    String a = StringUtil.toPascalCase(frColNm);
                    s.add("                " + toI + ".set" + a + "(" + frI + ".get" + a + "());");
                }
            }
            for (TableInfo frChild : frTbl.getChildren()) {

                // 転生元の子モデル名が転生元に前方一致しなければスキップ
                String frCNm = frChild.getName();
                if (!frCNm.startsWith(frName)) {
                    continue;
                }

                for (TableInfo child : table.getChildren()) {

                    // 自テーブルの子モデル名が自テーブルに前方一致しなければスキップ
                    String childName = child.getName();
                    if (!childName.startsWith(toTbl)) {
                        continue;
                    }

                    String fCE = StringUtil.toPascalCase(frCNm);
                    String fCI = StringUtil.toCamelCase(frCNm);
                    String cE = StringUtil.toPascalCase(childName);
                    String cI = StringUtil.toCamelCase(childName);
                    s.add("                " + frI + ".refer" + StringUtil.toPascalCase(frCNm) + "s();");
                    s.add("                " + toI + ".set" + cE + "s(new java.util.ArrayList<" + pkE + "." + cE
                            + ">());");
                    s.add("                for (" + pkE + "." + fCE + " " + fCI + " : " + frI + ".refer" + fCE
                            + "s()) {");
                    s.add("                    " + pkE + "." + cE + " " + cI + " = new " + pkE + "." + cE + "();");
                    s.add("                    " + cI + ".setId(" + fCI + ".getId());");
                    for (String frCColNm : frChild.getColumns().keySet()) {
                        // メタ情報ならスキップ
                        if (BeanGenerator.isMeta(frCColNm)) {
                            continue;
                        }
                        // 子テーブルに転生元の子モデルと同じカラムがあればコピー
                        if (child.getColumns().containsKey(frCColNm)) {
                            String a = StringUtil.toPascalCase(frCColNm);
                            s.add("                    " + cI + ".set" + a + "(" + fCI + ".get" + a + "());");
                        }
                    }
                    s.add("                    " + toI + ".get" + cE + "s().add(" + cI + ");");
                    s.add("                }");
                    s.add("");
                }
            }
            s.add("            }");

            //            // 転生元は一つしかないはずなので不要かも
            //            break;
        }

        if (froms > 0) {
            s.add("");
            s.add("            map.put(\"" + toE + "\", " + toI + ");");
        }
    }

}
