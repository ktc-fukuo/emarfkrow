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
    private static String projectDir;

    /** BeanGenerator.properties */
    private static ResourceBundle bundle;

    /** actionパッケージ */
    private static String actionPackage;

    /** entityパッケージ */
    private static String entityPackage;

    /** javaファイル出力ルートパス */
    private static String javaPath;

    /** コンパイルまでするか */
    private static boolean isCompile;

    /** 更新日時カラム名 */
    private static String updateDt;

    /** ステータス区分 */
    private static String statusKb;

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
        projectDir = dir;

        // BeanGenerator.properties読み込み
        bundle = ResourceBundles.getBundle(BeanGenerator.class);

        actionPackage = bundle.getString("BeanGenerator.java.package.action");

        entityPackage = bundle.getString("BeanGenerator.java.package.entity");

        javaPath = bundle.getString("BeanGenerator.java.path");

        //webからの自動生成ならコンパイルまで行う
        if (App.get("EmarfListener.autogenerate") != null) {
            isCompile = App.get("EmarfListener.autogenerate").toLowerCase().equals("true");
        }

        updateDt = bundle.getString("BeanGenerator.update_dt");

        statusKb = bundle.getString("BeanGenerator.status_kb");

        IndexActionGenerator.deleteAction(tableInfos);
        IndexActionGenerator.registAction(tableInfos);
        IndexActionGenerator.permitAction(tableInfos);
        IndexActionGenerator.forbidAction(tableInfos);
    }

    /**
     * 検索画面 登録処理出力
     * @param tableInfos テーブル情報のリスト
     */
    private static void deleteAction(final List<TableInfo> tableInfos) {

        // 出力フォルダを再作成
        String packagePath = actionPackage.replace(".", File.separator);
        String packageDir = projectDir + File.separator + javaPath + File.separator + packagePath;

        Map<String, String> javaFilePaths = new LinkedHashMap<String, String>();

        for (TableInfo tableInfo : tableInfos) {
            String tableName = tableInfo.getTableName();
            String remarks = tableInfo.getRemarks();

            String pascal = StringUtil.toPascalCase(tableName);

            List<String> s = new ArrayList<String>();
            s.add("package " + actionPackage + ";");
            s.add("");
            s.add("import java.time.LocalDateTime;");
            s.add("import java.util.HashMap;");
            s.add("import java.util.List;");
            s.add("import java.util.Map;");
            s.add("");
            s.add("import " + entityPackage + "." + pascal + ";");
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
            s.add("public class " + pascal + "SDeleteAction extends BaseAction {");
            s.add("");
            s.add("    /** " + remarks + "一覧削除処理 */");
            s.add("    @Override");
            s.add("    public Map<String, Object> running(final LocalDateTime now, final String execId, final Map<String, Object> postJson) {");
            s.add("");
            s.add("        Map<String, Object> map = new HashMap<String, Object>();");
            s.add("");
            s.add("        int count = 0;");
            s.add("");
            s.add("        @SuppressWarnings(\"unchecked\")");
            s.add("        List<Map<String, Object>> gridData = (List<Map<String, Object>>) postJson.get(\"" + pascal
                    + "Grid\");");
            s.add("        for (Map<String, Object> gridRow : gridData) {");
            s.add("");
            s.add("            if (gridRow.isEmpty()) {");
            s.add("                continue;");
            s.add("            }");
            s.add("");
            s.add("            // 主キーが不足していたらエラー");
            for (String pk : tableInfo.getPrimaryKeys()) {
                s.add("            if (jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(gridRow.get(\"" + pk
                        + "\"))) {");
                s.add("                throw new OptLockError(\"error.cant.delete\");");
                s.add("            }");
            }
            s.add("");
            s.add("            " + pascal + " e = FormValidator.toBean(" + pascal + ".class.getName(), gridRow);");
            List<TableInfo> childInfos = tableInfo.getChildInfos();
            BeanGenerator.getDeleteChilds(s, "e", childInfos, 1);
            s.add("");
            s.add("            if (e.delete() != 1) {");
            s.add("                throw new OptLockError(\"error.cant.delete\");");
            s.add("            }");
            s.add("            ++count;");
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

            String javaFilePath = packageDir + File.separator + pascal + "SDeleteAction.java";
            javaFilePaths.put(javaFilePath, actionPackage + "." + pascal + "SDeleteAction");

            FileUtil.writeFile(javaFilePath, s);
        }

        if (isCompile) {
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
        String packagePath = actionPackage.replace(".", File.separator);
        String packageDir = projectDir + File.separator + javaPath + File.separator + packagePath;

        Map<String, String> javaFilePaths = new LinkedHashMap<String, String>();

        for (TableInfo tableInfo : tableInfos) {
            String entity = StringUtil.toPascalCase(tableInfo.getTableName());
            String remarks = tableInfo.getRemarks();

            List<String> s = new ArrayList<String>();
            s.add("package " + actionPackage + ";");
            s.add("");
            s.add("import java.time.LocalDateTime;");
            s.add("import java.util.HashMap;");
            s.add("import java.util.List;");
            s.add("import java.util.Map;");
            s.add("");
            s.add("import " + entityPackage + "." + entity + ";");
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
            s.add("public class " + entity + "SRegistAction extends BaseAction {");
            s.add("");
            s.add("    /** " + remarks + "一覧登録処理 */");
            s.add("    @Override");
            s.add("    public Map<String, Object> running(final LocalDateTime now, final String execId, final Map<String, Object> postJson) {");
            s.add("");
            s.add("        Map<String, Object> map = new HashMap<String, Object>();");
            s.add("");
            s.add("        int count = 0;");
            s.add("");
            s.add("        @SuppressWarnings(\"unchecked\")");
            s.add("        List<Map<String, Object>> gridData = (List<Map<String, Object>>) postJson.get(\"" + entity
                    + "Grid\");");
            s.add("        for (Map<String, Object> gridRow : gridData) {");
            s.add("");
            s.add("            if (gridRow.isEmpty()) {");
            s.add("                continue;");
            s.add("            }");
            s.add("");
            s.add("            " + entity + " e = FormValidator.toBean(" + entity + ".class.getName(), gridRow);");
            s.add("");
            s.add("            // 主キーが不足していたらINSERT");
            s.add("            boolean isNew = false;");
            for (String primaryKey : tableInfo.getPrimaryKeys()) {
                String accessor = StringUtil.toPascalCase(primaryKey);
                s.add("            if (jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(e.get" + accessor + "())) {");
                s.add("                isNew = true;");
                s.add("            }");
            }
            if (tableInfo.getColumnInfos().containsKey(updateDt)
                    || tableInfo.getColumnInfos().containsKey(updateDt.toUpperCase())) {
                String accessor = StringUtil.toPascalCase(updateDt);
                s.add("            // 楽観ロック値がなくてもINSERT");
                s.add("            if (jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(e.get" + accessor + "())) {");
                s.add("                isNew = true;");
                s.add("            }");
            }
            if (!tableInfo.isView()) {
                s.add("");
                s.add("            e.setStatusKb(0);");
            }
            s.add("");
            s.add("            if (isNew) {");
            s.add("");
            s.add("                if (e.insert(now, execId) != 1) {");
            s.add("                    throw new OptLockError(\"error.cant.insert\");");
            s.add("                }");
            s.add("                ++count;");
            s.add("");
            s.add("            } else {");
            s.add("");
            s.add("                if (e.update(now, execId) != 1) {");
            s.add("                    throw new OptLockError(\"error.cant.update\");");
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
            s.add("        map.put(\"INFO\", Messages.get(\"info.regist\"));");
            s.add("        return map;");
            s.add("    }");
            s.add("");
            s.add("}");

            String javaFilePath = packageDir + File.separator + entity + "SRegistAction.java";
            javaFilePaths.put(javaFilePath, actionPackage + "." + entity + "SRegistAction");

            FileUtil.writeFile(javaFilePath, s);
        }

        if (isCompile) {
            for (Entry<String, String> e : javaFilePaths.entrySet()) {
                BeanGenerator.javaCompile(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 検索画面 承認処理出力
     * @param tableInfos テーブル情報のリスト
     */
    private static void permitAction(final List<TableInfo> tableInfos) {

        // 出力フォルダを再作成
        String packagePath = actionPackage.replace(".", File.separator);
        String packageDir = projectDir + File.separator + javaPath + File.separator + packagePath;

        Map<String, String> javaFilePaths = new LinkedHashMap<String, String>();

        for (TableInfo tableInfo : tableInfos) {

            if (tableInfo.isView()) {
                continue;
            }

            String entity = StringUtil.toPascalCase(tableInfo.getTableName());
            String remarks = tableInfo.getRemarks();

            List<String> s = new ArrayList<String>();
            s.add("package " + actionPackage + ";");
            s.add("");
            s.add("import java.time.LocalDateTime;");
            s.add("import java.util.HashMap;");
            s.add("import java.util.List;");
            s.add("import java.util.Map;");
            s.add("");
            s.add("import " + entityPackage + "." + entity + ";");
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
            s.add("public class " + entity + "SPermitAction extends BaseAction {");
            s.add("");
            s.add("    /** " + remarks + "一覧承認処理 */");
            s.add("    @Override");
            s.add("    public Map<String, Object> running(final LocalDateTime now, final String execId, final Map<String, Object> postJson) {");
            s.add("");
            s.add("        Map<String, Object> map = new HashMap<String, Object>();");
            s.add("");
            s.add("        int count = 0;");
            s.add("");
            s.add("        @SuppressWarnings(\"unchecked\")");
            s.add("        List<Map<String, Object>> gridData = (List<Map<String, Object>>) postJson.get(\"" + entity
                    + "Grid\");");
            s.add("        for (Map<String, Object> gridRow : gridData) {");
            s.add("");
            s.add("            if (gridRow.isEmpty()) {");
            s.add("                continue;");
            s.add("            }");
            s.add("");
            s.add("            " + entity + " e = FormValidator.toBean(" + entity + ".class.getName(), gridRow);");
            s.add("");
            s.add("            // 主キーが不足していたらエラー");
            String params = "";
            for (String primaryKey : tableInfo.getPrimaryKeys()) {
                String property = StringUtil.toCamelCase(primaryKey);
                String accessor = StringUtil.toPascalCase(primaryKey);
                s.add("            Object " + property + " = e.get" + accessor + "();");
                s.add("            if (" + property + " == null) {");
                s.add("                throw new OptLockError(\"error.cant.permit\");");
                s.add("            }");
                if (params.length() > 0) {
                    params += ", ";
                }
                params += property;
            }
            List<TableInfo> childInfos = tableInfo.getChildInfos();
            BeanGenerator.getPermitChilds(s, "e", childInfos, 1);
            s.add("");
            s.add("            " + entity + " f = " + entity + ".get(" + params + ");");
            s.add("            f.set" + StringUtil.toPascalCase(statusKb) + "(1);");
            s.add("            if (f.update(now, execId) != 1) {");
            s.add("                throw new OptLockError(\"error.cant.permit\");");
            s.add("            }");
            s.add("            ++count;");
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

            String javaFilePath = packageDir + File.separator + entity + "SPermitAction.java";
            javaFilePaths.put(javaFilePath, actionPackage + "." + entity + "SPermitAction");

            FileUtil.writeFile(javaFilePath, s);
        }

        if (isCompile) {
            for (Entry<String, String> e : javaFilePaths.entrySet()) {
                BeanGenerator.javaCompile(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 検索画面 否認処理出力
     * @param tableInfos テーブル情報のリスト
     */
    private static void forbidAction(final List<TableInfo> tableInfos) {

        // 出力フォルダを再作成
        String packagePath = actionPackage.replace(".", File.separator);
        String packageDir = projectDir + File.separator + javaPath + File.separator + packagePath;

        Map<String, String> javaFilePaths = new LinkedHashMap<String, String>();

        for (TableInfo tableInfo : tableInfos) {

            if (tableInfo.isView()) {
                continue;
            }

            String entity = StringUtil.toPascalCase(tableInfo.getTableName());
            String remarks = tableInfo.getRemarks();

            List<String> s = new ArrayList<String>();
            s.add("package " + actionPackage + ";");
            s.add("");
            s.add("import java.time.LocalDateTime;");
            s.add("import java.util.HashMap;");
            s.add("import java.util.List;");
            s.add("import java.util.Map;");
            s.add("");
            s.add("import " + entityPackage + "." + entity + ";");
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
            s.add("public class " + entity + "SForbidAction extends BaseAction {");
            s.add("");
            s.add("    /** " + remarks + "一覧承認処理 */");
            s.add("    @Override");
            s.add("    public Map<String, Object> running(final LocalDateTime now, final String execId, final Map<String, Object> postJson) {");
            s.add("");
            s.add("        Map<String, Object> map = new HashMap<String, Object>();");
            s.add("");
            s.add("        int count = 0;");
            s.add("");
            s.add("        @SuppressWarnings(\"unchecked\")");
            s.add("        List<Map<String, Object>> gridData = (List<Map<String, Object>>) postJson.get(\"" + entity
                    + "Grid\");");
            s.add("        for (Map<String, Object> gridRow : gridData) {");
            s.add("");
            s.add("            if (gridRow.isEmpty()) {");
            s.add("                continue;");
            s.add("            }");
            s.add("");
            s.add("            " + entity + " e = FormValidator.toBean(" + entity + ".class.getName(), gridRow);");
            s.add("");
            s.add("            // 主キーが不足していたらエラー");
            String params = "";
            for (String primaryKey : tableInfo.getPrimaryKeys()) {
                String property = StringUtil.toCamelCase(primaryKey);
                String accessor = StringUtil.toPascalCase(primaryKey);
                s.add("            Object " + property + " = e.get" + accessor + "();");
                s.add("            if (" + property + " == null) {");
                s.add("                throw new OptLockError(\"error.cant.forbid\");");
                s.add("            }");
                if (params.length() > 0) {
                    params += ", ";
                }
                params += property;
            }
            List<TableInfo> childInfos = tableInfo.getChildInfos();
            BeanGenerator.getForbidChilds(s, "e", childInfos, 1);
            s.add("");
            s.add("            " + entity + " f = " + entity + ".get(" + params + ");");
            s.add("            f.set" + StringUtil.toPascalCase(statusKb) + "(-1);");
            s.add("            if (f.update(now, execId) != 1) {");
            s.add("                throw new OptLockError(\"error.cant.forbid\");");
            s.add("            }");
            s.add("            ++count;");
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

            String javaFilePath = packageDir + File.separator + entity + "SForbidAction.java";
            javaFilePaths.put(javaFilePath, actionPackage + "." + entity + "SForbidAction");

            FileUtil.writeFile(javaFilePath, s);
        }

        if (isCompile) {
            for (Entry<String, String> e : javaFilePaths.entrySet()) {
                BeanGenerator.javaCompile(e.getKey(), e.getValue());
            }
        }
    }
}
