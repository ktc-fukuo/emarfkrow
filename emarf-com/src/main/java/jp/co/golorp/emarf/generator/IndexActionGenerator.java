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

import jp.co.golorp.emarf.io.FileUtil;
import jp.co.golorp.emarf.lang.StringUtil;
import jp.co.golorp.emarf.properties.App;
import jp.co.golorp.emarf.util.ResourceBundles;

/**
 * 検索画面アクション出力
 */
public final class IndexActionGenerator {

    /** プロジェクトディレクトリ */
    private static String prjDir;

    /** BeanGenerator.properties */
    private static ResourceBundle bundle = ResourceBundles.getBundle(BeanGenerator.class);

    /** actionパッケージ */
    private static String actionPkg;

    /** entityパッケージ */
    private static String entityPackage;

    /** javaファイル出力ルートパス */
    private static String javaPath;

    /** 起動時の自動生成か */
    private static boolean isGenerateAtStartup;

    /** ステータス区分 */
    private static String status;

    /** 削除フラグ */
    private static String deleteF;

    /**
     * プライベートコンストラクタ
     */
    private IndexActionGenerator() {
    }

    /**
     * 各ファイル出力 主処理
     * @param dir プロジェクトのディレクトリ
     * @param tableInfos
     */
    public static void generate(final String dir, final List<TableInfo> tableInfos) {

        //プロジェクトディレクトリを退避
        prjDir = dir;

        actionPkg = bundle.getString("java.package.action") + ".model.base";

        entityPackage = bundle.getString("java.package.entity");

        javaPath = bundle.getString("dir.java");

        //webからの自動生成ならコンパイルまで行う
        if (App.get("generateAtStartup") != null) {
            isGenerateAtStartup = App.get("generateAtStartup").toLowerCase().equals("true");
        }

        status = bundle.getString("column.status");

        deleteF = bundle.getString("column.delete");

        IndexActionGenerator.deleteAction(tableInfos);
        IndexActionGenerator.registAction(tableInfos);
        if (!StringUtil.isNullOrWhiteSpace(status)) {
            IndexActionGenerator.applyAction(tableInfos);
            IndexActionGenerator.cancelAction(tableInfos);
            IndexActionGenerator.permitAction(tableInfos);
            IndexActionGenerator.forbidAction(tableInfos);
        }
    }

    /**
     * 検索画面 登録処理出力
     * @param tables テーブル情報のリスト
     */
    private static void deleteAction(final List<TableInfo> tables) {

        // 出力フォルダを再作成
        String pkgPath = actionPkg.replace(".", File.separator);
        String pkgDir = prjDir + File.separator + javaPath + File.separator + pkgPath;

        Map<String, String> javaFilePaths = new LinkedHashMap<String, String>();

        for (TableInfo table : tables) {

            if (table.isHistory() || table.isView() || table.isStatusFlow()) {
                continue;
            }

            if (table.getPrimaryKeys().size() == 0) {
                continue;
            }

            //削除フラグがあればスキップ
            if (!StringUtil.isNullOrWhiteSpace(deleteF) && table.getColumns().containsKey(deleteF)) {
                continue;
            }

            String e = StringUtil.toPascalCase(table.getName());
            String remarks = table.getRemarks();

            List<String> s = new ArrayList<String>();
            s.add("package " + actionPkg + ";");
            s.add("");
            s.add("import java.time.LocalDateTime;");
            s.add("import java.util.HashMap;");
            s.add("import java.util.List;");
            s.add("import java.util.Map;");
            s.add("");
            s.add("import " + entityPackage + "." + e + ";");
            s.add("");
            s.add("import jp.co.golorp.emarf.action.BaseAction;");
            s.add("import jp.co.golorp.emarf.exception.OptLockError;");
            s.add("import jp.co.golorp.emarf.util.Messages;");
            s.add("import jp.co.golorp.emarf.validation.FormValidator;");
            s.add("");
            s.add("/**");
            s.add(" * " + remarks + "一覧削除");
            s.add(" *");
            s.add(" * @author emarfkrow");
            s.add(" */");
            s.add("public class " + e + "SDeleteAction extends BaseAction {");
            s.add("");
            s.add("    /** " + remarks + "一覧削除処理 */");
            s.add("    @Override");
            s.add("    public Map<String, Object> running(final LocalDateTime at, final String by, final Map<String, Object> form) {");
            s.add("");
            s.add("        Map<String, Object> map = new HashMap<String, Object>();");
            s.add("");
            s.add("        int count = 0;");
            s.add("");
            s.add("        @SuppressWarnings(\"unchecked\")");
            s.add("        List<Map<String, Object>> data = (List<Map<String, Object>>) form.get(\"" + e + "Grid\");");
            s.add("        if (data != null) {");
            s.add("            for (Map<String, Object> row : data) {");
            s.add("");
            s.add("                if (row.isEmpty()) {");
            s.add("                    continue;");
            s.add("                }");
            s.add("");
            s.add("                // 主キーが不足していたらエラー");
            for (String k : table.getPrimaryKeys()) {
                s.add("                if (jp.co.golorp.emarf.lang.StringUtil.isNullOrWhiteSpace(row.get(\"" + k
                        + "\"))) {");
                s.add("                    throw new OptLockError(\"error.cant.delete\", \"" + remarks + "\");");
                s.add("                }");
            }
            s.add("");
            s.add("                " + e + " e = FormValidator.toBean(" + e + ".class.getName(), row);");
            List<TableInfo> childInfos = table.getChildren();
            BeanGenerator.getDeleteChilds(s, "e", childInfos, 2);
            s.add("                if (e.delete() != 1) {");
            s.add("                    throw new OptLockError(\"error.cant.delete\", \"" + remarks + "\");");
            s.add("                }");
            s.add("                ++count;");
            s.add("            }");
            s.add("        }");
            s.add("");
            s.add("        if (count == 0) {");
            s.add("            map.put(\"ERROR\", Messages.get(\"error.nopost\"));");
            s.add("            return map;");
            s.add("        }");
            s.add("");
            s.add("        map.put(\"INFO\", Messages.get(\"info.delete\"));");
            s.add("        return map;");
            s.add("    }");
            s.add("");
            s.add("}");

            String javaFilePath = pkgDir + File.separator + e + "SDeleteAction.java";
            javaFilePaths.put(javaFilePath, actionPkg + "." + e + "SDeleteAction");

            FileUtil.writeFile(javaFilePath, s);
        }

        if (isGenerateAtStartup) {
            for (Entry<String, String> e : javaFilePaths.entrySet()) {
                BeanGenerator.javaCompile(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 検索画面 登録処理出力
     * @param tableInfos テーブル情報のリスト
     */
    private static void registAction(final List<TableInfo> tableInfos) {

        // 出力フォルダを再作成
        String packagePath = actionPkg.replace(".", File.separator);
        String packageDir = prjDir + File.separator + javaPath + File.separator + packagePath;

        Map<String, String> javaFilePaths = new LinkedHashMap<String, String>();

        for (TableInfo table : tableInfos) {

            if (table.isHistory() || table.isView() || table.isStatusFlow() /* ClassNotFoundよけ */) {
                continue;
            }

            if (table.getPrimaryKeys().size() == 0) {
                continue;
            }

            String e = StringUtil.toPascalCase(table.getName());
            String remarks = table.getRemarks();

            List<String> s = new ArrayList<String>();
            s.add("package " + actionPkg + ";");
            s.add("");
            s.add("import java.time.LocalDateTime;");
            s.add("import java.util.HashMap;");
            s.add("import java.util.List;");
            s.add("import java.util.Map;");
            s.add("");
            s.add("import " + entityPackage + "." + e + ";");
            s.add("");
            s.add("import jp.co.golorp.emarf.action.BaseAction;");
            s.add("import jp.co.golorp.emarf.exception.OptLockError;");
            s.add("import jp.co.golorp.emarf.util.Messages;");
            s.add("import jp.co.golorp.emarf.validation.FormValidator;");
            s.add("");
            s.add("/**");
            s.add(" * " + remarks + "一覧登録");
            s.add(" *");
            s.add(" * @author emarfkrow");
            s.add(" */");
            s.add("public class " + e + "SRegistAction extends BaseAction {");
            s.add("");
            s.add("    /** " + remarks + "一覧登録処理 */");
            s.add("    @Override");
            s.add("    public Map<String, Object> running(final LocalDateTime at, final String by, final Map<String, Object> form) {");
            s.add("");
            s.add("        Map<String, Object> map = new HashMap<String, Object>();");
            s.add("");
            s.add("        int count = 0;");
            s.add("");
            s.add("        @SuppressWarnings(\"unchecked\")");
            s.add("        List<Map<String, Object>> data = (List<Map<String, Object>>) form.get(\"" + e + "Grid\");");
            s.add("        if (data != null) {");
            s.add("            for (Map<String, Object> row : data) {");
            s.add("");
            s.add("                if (row.isEmpty()) {");
            s.add("                    continue;");
            s.add("                }");
            s.add("");
            //            s.add("            String className = " + entity + ".class.getName();");
            //            if (table.isConvView()) {
            //                s.add("            // 変換ビューの場合");
            //                s.add("            className = \"" + entityPackage + ".\" + row.get(\"TABLE_NAME\").toString();");
            //                s.add("            jp.co.golorp.emarf.entity.IEntity e = FormValidator.toBean(className, row);");
            //            } else {
            //            s.add("            " + entity + " e = FormValidator.toBean(className, row);");
            s.add("                " + e + " e = FormValidator.toBean(" + e + ".class.getName(), row);");
            //            }
            //            if (!table.isView() && !StringUtil.isNullOrWhiteSpace(status) && table.getColumns().containsKey(status)) {
            //                s.add("");
            //                s.add("                e.set" + StringUtil.toPascalCase(status) + "(0);");
            //            }
            s.add("");
            s.add("                if (e.isNew()) {");
            s.add("");
            s.add("                    if (e.insert(at, by) != 1) {");
            s.add("                        throw new OptLockError(\"error.cant.insert\", \"" + remarks + "\");");
            s.add("                    }");
            s.add("                    ++count;");
            s.add("");
            s.add("                } else {");
            s.add("");
            s.add("                    if (e.update(at, by) != 1) {");
            s.add("                        throw new OptLockError(\"error.cant.update\", \"" + remarks + "\");");
            s.add("                    }");
            s.add("                    ++count;");
            s.add("                }");
            s.add("            }");
            s.add("        }");
            s.add("");
            s.add("        if (count == 0) {");
            s.add("            map.put(\"ERROR\", Messages.get(\"error.nopost\"));");
            s.add("            return map;");
            s.add("        }");
            s.add("");
            s.add("        map.put(\"INFO\", Messages.get(\"info.regist\"));");
            s.add("        return map;");
            s.add("    }");
            s.add("");
            s.add("}");

            String javaFilePath = packageDir + File.separator + e + "SRegistAction.java";
            javaFilePaths.put(javaFilePath, actionPkg + "." + e + "SRegistAction");

            FileUtil.writeFile(javaFilePath, s);
        }

        if (isGenerateAtStartup) {
            for (Entry<String, String> e : javaFilePaths.entrySet()) {
                BeanGenerator.javaCompile(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 検索画面 申請処理出力
     * @param tables テーブル情報のリスト
     */
    private static void applyAction(final List<TableInfo> tables) {

        // 出力フォルダを再作成
        String packagePath = actionPkg.replace(".", File.separator);
        String packageDir = prjDir + File.separator + javaPath + File.separator + packagePath;

        Map<String, String> javaFilePaths = new LinkedHashMap<String, String>();

        for (TableInfo table : tables) {

            if (table.isHistory() || table.isView() || table.isStatusFlow()
                    || !table.getColumns().containsKey(status)) {
                continue;
            }

            String e = StringUtil.toPascalCase(table.getName());
            String remarks = table.getRemarks();

            List<String> s = new ArrayList<String>();
            s.add("package " + actionPkg + ";");
            s.add("");
            s.add("import java.time.LocalDateTime;");
            s.add("import java.util.HashMap;");
            s.add("import java.util.List;");
            s.add("import java.util.Map;");
            s.add("");
            s.add("import " + entityPackage + "." + e + ";");
            s.add("");
            s.add("import jp.co.golorp.emarf.action.BaseAction;");
            s.add("import jp.co.golorp.emarf.exception.OptLockError;");
            s.add("import jp.co.golorp.emarf.util.Messages;");
            s.add("import jp.co.golorp.emarf.validation.FormValidator;");
            s.add("");
            s.add("/**");
            s.add(" * " + remarks + "一覧申請");
            s.add(" *");
            s.add(" * @author emarfkrow");
            s.add(" */");
            s.add("public class " + e + "SApplyAction extends BaseAction {");
            s.add("");
            s.add("    /** " + remarks + "一覧申請処理 */");
            s.add("    @Override");
            s.add("    public Map<String, Object> running(final LocalDateTime at, final String by, final Map<String, Object> form) {");
            s.add("");
            s.add("        Map<String, Object> map = new HashMap<String, Object>();");
            s.add("");
            s.add("        int count = 0;");
            s.add("");
            s.add("        @SuppressWarnings(\"unchecked\")");
            s.add("        List<Map<String, Object>> data = (List<Map<String, Object>>) form.get(\"" + e + "Grid\");");
            s.add("        if (data != null) {");
            s.add("            for (Map<String, Object> row : data) {");
            s.add("");
            s.add("                if (row.isEmpty()) {");
            s.add("                    continue;");
            s.add("                }");
            s.add("");
            s.add("                " + e + " e = FormValidator.toBean(" + e + ".class.getName(), row);");
            s.add("");
            s.add("                // 主キーが不足していたらエラー");
            String params = "";
            for (String primaryKey : table.getPrimaryKeys()) {
                String property = StringUtil.toCamelCase(primaryKey);
                String accessor = StringUtil.toPascalCase(primaryKey);
                s.add("                Object " + property + " = e.get" + accessor + "();");
                s.add("                if (" + property + " == null) {");
                s.add("                    throw new OptLockError(\"error.cant.apply\", \"" + remarks + "\");");
                s.add("                }");
                if (params.length() > 0) {
                    params += ", ";
                }
                params += property;
            }
            List<TableInfo> childInfos = table.getChildren();
            BeanGenerator.getApplyChilds(s, "e", childInfos, 2);
            s.add("");
            //s.add("                " + e + " f = " + e + ".get(" + params + ");");
            if (table.getColumns().containsKey(status)) {
                String acc = StringUtil.toPascalCase(status);
                s.add("                if (e.get" + acc + "() != null && !e.get" + acc + "().equals(\"\")) {");
                s.add("                    throw new jp.co.golorp.emarf.exception.AppError(\"error.notmatch\",");
                s.add("                            Messages.get(\"common.selectedRow\"), Messages.get(\"common.notapply\"));");
                s.add("                }");
                s.add("                e.set" + acc + "(0);");
            }
            s.add("                if (e.update(at, by) != 1) {");
            s.add("                    throw new OptLockError(\"error.cant.apply\", \"" + remarks + "\");");
            s.add("                }");
            s.add("                ++count;");
            s.add("            }");
            s.add("        }");
            s.add("");
            s.add("        if (count == 0) {");
            s.add("            map.put(\"ERROR\", Messages.get(\"error.nopost\"));");
            s.add("            return map;");
            s.add("        }");
            s.add("");
            s.add("        map.put(\"INFO\", Messages.get(\"info.apply\"));");
            s.add("        return map;");
            s.add("    }");
            s.add("");
            s.add("}");

            String javaFilePath = packageDir + File.separator + e + "SApplyAction.java";
            javaFilePaths.put(javaFilePath, actionPkg + "." + e + "SApplyAction");

            FileUtil.writeFile(javaFilePath, s);
        }

        if (isGenerateAtStartup) {
            for (Entry<String, String> e : javaFilePaths.entrySet()) {
                BeanGenerator.javaCompile(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 検索画面 取消処理出力
     * @param tables テーブル情報のリスト
     */
    private static void cancelAction(final List<TableInfo> tables) {

        // 出力フォルダを再作成
        String packagePath = actionPkg.replace(".", File.separator);
        String packageDir = prjDir + File.separator + javaPath + File.separator + packagePath;

        Map<String, String> javaFilePaths = new LinkedHashMap<String, String>();

        for (TableInfo table : tables) {

            if (table.isHistory() || table.isView() || table.isStatusFlow()
                    || !table.getColumns().containsKey(status)) {
                continue;
            }

            String e = StringUtil.toPascalCase(table.getName());
            String remarks = table.getRemarks();

            List<String> s = new ArrayList<String>();
            s.add("package " + actionPkg + ";");
            s.add("");
            s.add("import java.time.LocalDateTime;");
            s.add("import java.util.HashMap;");
            s.add("import java.util.List;");
            s.add("import java.util.Map;");
            s.add("");
            s.add("import " + entityPackage + "." + e + ";");
            s.add("");
            s.add("import jp.co.golorp.emarf.action.BaseAction;");
            s.add("import jp.co.golorp.emarf.exception.OptLockError;");
            s.add("import jp.co.golorp.emarf.util.Messages;");
            s.add("import jp.co.golorp.emarf.validation.FormValidator;");
            s.add("");
            s.add("/**");
            s.add(" * " + remarks + "一覧取消");
            s.add(" *");
            s.add(" * @author emarfkrow");
            s.add(" */");
            s.add("public class " + e + "SCancelAction extends BaseAction {");
            s.add("");
            s.add("    /** " + remarks + "一覧取消処理 */");
            s.add("    @Override");
            s.add("    public Map<String, Object> running(final LocalDateTime at, final String by, final Map<String, Object> form) {");
            s.add("");
            s.add("        Map<String, Object> map = new HashMap<String, Object>();");
            s.add("");
            s.add("        int count = 0;");
            s.add("");
            s.add("        @SuppressWarnings(\"unchecked\")");
            s.add("        List<Map<String, Object>> data = (List<Map<String, Object>>) form.get(\"" + e + "Grid\");");
            s.add("        if (data != null) {");
            s.add("            for (Map<String, Object> row : data) {");
            s.add("");
            s.add("                if (row.isEmpty()) {");
            s.add("                    continue;");
            s.add("                }");
            s.add("");
            s.add("                " + e + " e = FormValidator.toBean(" + e + ".class.getName(), row);");
            s.add("");
            s.add("                // 主キーが不足していたらエラー");
            String params = "";
            for (String primaryKey : table.getPrimaryKeys()) {
                String property = StringUtil.toCamelCase(primaryKey);
                String accessor = StringUtil.toPascalCase(primaryKey);
                s.add("                Object " + property + " = e.get" + accessor + "();");
                s.add("                if (" + property + " == null) {");
                s.add("                    throw new OptLockError(\"error.cant.cancel\", \"" + remarks + "\");");
                s.add("                }");
                if (params.length() > 0) {
                    params += ", ";
                }
                params += property;
            }
            List<TableInfo> childInfos = table.getChildren();
            BeanGenerator.getCancelChilds(s, "e", childInfos, 2);
            s.add("");
            //s.add("                " + e + " f = " + e + ".get(" + params + ");");
            if (table.getColumns().containsKey(status)) {
                String acc = StringUtil.toPascalCase(status);
                s.add("                if (!e.get" + acc + "().equals(\"0\") && !e.get" + acc + "().equals(\"-1\")) {");
                s.add("                    throw new jp.co.golorp.emarf.exception.AppError(\"error.notmatch\",");
                s.add("                            Messages.get(\"common.selectedRow\"), Messages.get(\"common.apply.forbid\"));");
                s.add("                }");
                s.add("                e.set" + acc + "(null);");
            }
            s.add("                if (e.update(at, by) != 1) {");
            s.add("                    throw new OptLockError(\"error.cant.cancel\", \"" + remarks + "\");");
            s.add("                }");
            s.add("                ++count;");
            s.add("            }");
            s.add("        }");
            s.add("");
            s.add("        if (count == 0) {");
            s.add("            map.put(\"ERROR\", Messages.get(\"error.nopost\"));");
            s.add("            return map;");
            s.add("        }");
            s.add("");
            s.add("        map.put(\"INFO\", Messages.get(\"info.cancel\"));");
            s.add("        return map;");
            s.add("    }");
            s.add("");
            s.add("}");

            String javaFilePath = packageDir + File.separator + e + "SCancelAction.java";
            javaFilePaths.put(javaFilePath, actionPkg + "." + e + "SCancelAction");

            FileUtil.writeFile(javaFilePath, s);
        }

        if (isGenerateAtStartup) {
            for (Entry<String, String> e : javaFilePaths.entrySet()) {
                BeanGenerator.javaCompile(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 検索画面 承認処理出力
     * @param tables テーブル情報のリスト
     */
    private static void permitAction(final List<TableInfo> tables) {

        // 出力フォルダを再作成
        String packagePath = actionPkg.replace(".", File.separator);
        String packageDir = prjDir + File.separator + javaPath + File.separator + packagePath;

        Map<String, String> javaFilePaths = new LinkedHashMap<String, String>();

        for (TableInfo table : tables) {

            if (table.isHistory() || table.isView() || table.isStatusFlow()
                    || !table.getColumns().containsKey(status)) {
                continue;
            }

            String e = StringUtil.toPascalCase(table.getName());
            String remarks = table.getRemarks();

            List<String> s = new ArrayList<String>();
            s.add("package " + actionPkg + ";");
            s.add("");
            s.add("import java.time.LocalDateTime;");
            s.add("import java.util.HashMap;");
            s.add("import java.util.List;");
            s.add("import java.util.Map;");
            s.add("");
            s.add("import " + entityPackage + "." + e + ";");
            s.add("");
            s.add("import jp.co.golorp.emarf.action.BaseAction;");
            s.add("import jp.co.golorp.emarf.exception.OptLockError;");
            s.add("import jp.co.golorp.emarf.util.Messages;");
            s.add("import jp.co.golorp.emarf.validation.FormValidator;");
            s.add("");
            s.add("/**");
            s.add(" * " + remarks + "一覧承認");
            s.add(" *");
            s.add(" * @author emarfkrow");
            s.add(" */");
            s.add("public class " + e + "SPermitAction extends BaseAction {");
            s.add("");
            s.add("    /** " + remarks + "一覧承認処理 */");
            s.add("    @Override");
            s.add("    public Map<String, Object> running(final LocalDateTime at, final String by, final Map<String, Object> form) {");
            s.add("");
            s.add("        Map<String, Object> map = new HashMap<String, Object>();");
            s.add("");
            s.add("        int count = 0;");
            s.add("");
            s.add("        @SuppressWarnings(\"unchecked\")");
            s.add("        List<Map<String, Object>> data = (List<Map<String, Object>>) form.get(\"" + e + "Grid\");");
            s.add("        if (data != null) {");
            s.add("            for (Map<String, Object> row : data) {");
            s.add("");
            s.add("                if (row.isEmpty()) {");
            s.add("                    continue;");
            s.add("                }");
            s.add("");
            s.add("                " + e + " e = FormValidator.toBean(" + e + ".class.getName(), row);");
            s.add("");
            s.add("                // 主キーが不足していたらエラー");
            String params = "";
            for (String primaryKey : table.getPrimaryKeys()) {
                String property = StringUtil.toCamelCase(primaryKey);
                String accessor = StringUtil.toPascalCase(primaryKey);
                s.add("                Object " + property + " = e.get" + accessor + "();");
                s.add("                if (" + property + " == null) {");
                s.add("                    throw new OptLockError(\"error.cant.permit\", \"" + remarks + "\");");
                s.add("                }");
                if (params.length() > 0) {
                    params += ", ";
                }
                params += property;
            }
            List<TableInfo> childInfos = table.getChildren();
            BeanGenerator.getPermitChilds(s, "e", childInfos, 2);
            s.add("");
            //s.add("                " + e + " f = " + e + ".get(" + params + ");");
            if (table.getColumns().containsKey(status)) {
                String acc = StringUtil.toPascalCase(status);
                s.add("                if (e.get" + acc + "() != null && !e.get" + acc + "().equals(\"0\")) {");
                s.add("                    throw new jp.co.golorp.emarf.exception.AppError(\"error.notmatch\",");
                s.add("                            Messages.get(\"common.selectedRow\"), Messages.get(\"common.applied\"));");
                s.add("                }");
                s.add("                e.set" + acc + "(1);");
            }
            s.add("                if (e.update(at, by) != 1) {");
            s.add("                    throw new OptLockError(\"error.cant.permit\", \"" + remarks + "\");");
            s.add("                }");
            s.add("                ++count;");
            s.add("            }");
            s.add("        }");
            s.add("");
            s.add("        if (count == 0) {");
            s.add("            map.put(\"ERROR\", Messages.get(\"error.nopost\"));");
            s.add("            return map;");
            s.add("        }");
            s.add("");
            s.add("        map.put(\"INFO\", Messages.get(\"info.permit\"));");
            s.add("        return map;");
            s.add("    }");
            s.add("");
            s.add("}");

            String javaFilePath = packageDir + File.separator + e + "SPermitAction.java";
            javaFilePaths.put(javaFilePath, actionPkg + "." + e + "SPermitAction");

            FileUtil.writeFile(javaFilePath, s);
        }

        if (isGenerateAtStartup) {
            for (Entry<String, String> e : javaFilePaths.entrySet()) {
                BeanGenerator.javaCompile(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 検索画面 否認処理出力
     * @param tables テーブル情報のリスト
     */
    private static void forbidAction(final List<TableInfo> tables) {

        // 出力フォルダを再作成
        String packagePath = actionPkg.replace(".", File.separator);
        String packageDir = prjDir + File.separator + javaPath + File.separator + packagePath;

        Map<String, String> javaFilePaths = new LinkedHashMap<String, String>();

        for (TableInfo table : tables) {

            if (table.isHistory() || table.isView() || table.isStatusFlow()
                    || !table.getColumns().containsKey(status)) {
                continue;
            }

            String e = StringUtil.toPascalCase(table.getName());
            String remarks = table.getRemarks();

            List<String> s = new ArrayList<String>();
            s.add("package " + actionPkg + ";");
            s.add("");
            s.add("import java.time.LocalDateTime;");
            s.add("import java.util.HashMap;");
            s.add("import java.util.List;");
            s.add("import java.util.Map;");
            s.add("");
            s.add("import " + entityPackage + "." + e + ";");
            s.add("");
            s.add("import jp.co.golorp.emarf.action.BaseAction;");
            s.add("import jp.co.golorp.emarf.exception.OptLockError;");
            s.add("import jp.co.golorp.emarf.util.Messages;");
            s.add("import jp.co.golorp.emarf.validation.FormValidator;");
            s.add("");
            s.add("/**");
            s.add(" * " + remarks + "一覧否認");
            s.add(" *");
            s.add(" * @author emarfkrow");
            s.add(" */");
            s.add("public class " + e + "SForbidAction extends BaseAction {");
            s.add("");
            s.add("    /** " + remarks + "一覧否認処理 */");
            s.add("    @Override");
            s.add("    public Map<String, Object> running(final LocalDateTime at, final String by, final Map<String, Object> form) {");
            s.add("");
            s.add("        Map<String, Object> map = new HashMap<String, Object>();");
            s.add("");
            s.add("        int count = 0;");
            s.add("");
            s.add("        @SuppressWarnings(\"unchecked\")");
            s.add("        List<Map<String, Object>> data = (List<Map<String, Object>>) form.get(\"" + e + "Grid\");");
            s.add("        if (data != null) {");
            s.add("            for (Map<String, Object> row : data) {");
            s.add("");
            s.add("                if (row.isEmpty()) {");
            s.add("                    continue;");
            s.add("                }");
            s.add("");
            s.add("                " + e + " e = FormValidator.toBean(" + e + ".class.getName(), row);");
            s.add("");
            s.add("                // 主キーが不足していたらエラー");
            String params = "";
            for (String primaryKey : table.getPrimaryKeys()) {
                String property = StringUtil.toCamelCase(primaryKey);
                String accessor = StringUtil.toPascalCase(primaryKey);
                s.add("                Object " + property + " = e.get" + accessor + "();");
                s.add("                if (" + property + " == null) {");
                s.add("                    throw new OptLockError(\"error.cant.forbid\", \"" + remarks + "\");");
                s.add("                }");
                if (params.length() > 0) {
                    params += ", ";
                }
                params += property;
            }
            List<TableInfo> childInfos = table.getChildren();
            BeanGenerator.getForbidChilds(s, "e", childInfos, 2);
            s.add("");
            //s.add("                " + e + " f = " + e + ".get(" + params + ");");
            if (table.getColumns().containsKey(status)) {
                String acc = StringUtil.toPascalCase(status);
                s.add("                if (!e.get" + acc + "().equals(\"0\") && !e.get" + acc + "().equals(\"1\")) {");
                s.add("                    throw new jp.co.golorp.emarf.exception.AppError(\"error.notmatch\",");
                s.add("                            Messages.get(\"common.selectedRow\"), Messages.get(\"common.apply.permit\"));");
                s.add("                }");
                s.add("                e.set" + acc + "(-1);");
            }
            s.add("                if (e.update(at, by) != 1) {");
            s.add("                    throw new OptLockError(\"error.cant.forbid\", \"" + remarks + "\");");
            s.add("                }");
            s.add("                ++count;");
            s.add("            }");
            s.add("        }");
            s.add("");
            s.add("        if (count == 0) {");
            s.add("            map.put(\"ERROR\", Messages.get(\"error.nopost\"));");
            s.add("            return map;");
            s.add("        }");
            s.add("");
            s.add("        map.put(\"INFO\", Messages.get(\"info.forbid\"));");
            s.add("        return map;");
            s.add("    }");
            s.add("");
            s.add("}");

            String javaFilePath = packageDir + File.separator + e + "SForbidAction.java";
            javaFilePaths.put(javaFilePath, actionPkg + "." + e + "SForbidAction");

            FileUtil.writeFile(javaFilePath, s);
        }

        if (isGenerateAtStartup) {
            for (Entry<String, String> e : javaFilePaths.entrySet()) {
                BeanGenerator.javaCompile(e.getKey(), e.getValue());
            }
        }
    }
}
