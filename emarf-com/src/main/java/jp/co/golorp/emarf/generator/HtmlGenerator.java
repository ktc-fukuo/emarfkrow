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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.golorp.emarf.io.FileUtil;
import jp.co.golorp.emarf.lang.StringUtil;
import jp.co.golorp.emarf.util.ResourceBundles;

/**
 * HTMLファイル出力
 *
 * @author golorp
 */
public abstract class HtmlGenerator {

    /** 数値項目の正規表現 */
    protected static final String NUM_RE = "INT|DECIMAL|DOUBLE|NUMBER|NUMERIC";

    /** logger */
    private static final Logger LOG = LoggerFactory.getLogger(HtmlGenerator.class);

    /** properties */
    private static ResourceBundle bundle = ResourceBundles.getBundle(BeanGenerator.class);

    /** グリッド列幅ピクセル乗数 */
    protected static final int COLUMN_WIDTH_PX_MULTIPLIER = 10;

    /** 長兄 */
    protected static final String ELDEST_RE;
    /** 弟を設定しないテーブル名 */
    private static String youngestRe;

    /** 参照列名ペア */
    protected static final Set<String[]> REFER_PAIRS = new LinkedHashSet<String[]>();

    /** 適用日カラム名 */
    protected static final String TEKIYO_BI;
    /** 廃止日カラム名 */
    protected static final String HAISHI_BI;
    /** 更新日時カラム名 */
    protected static final String UPDATE_TS;
    /** ステータス区分 */
    protected static final String STATUS;
    /** 削除フラグ */
    protected static final String DELETE_F;
    /** 変更理由 */
    protected static final String REASON;

    /** 数値だがフォーマットしないサフィックス */
    protected static final String[] INT_NOFORMAT_SUFFIXS;
    /** 必須CHAR列の指定 */
    protected static final String CHAR_NOTNULL_RE;

    /** ページ行数 */
    protected static final String GRID_ROWS;

    /** VIEWの検索条件とするプレフィクス */
    protected static final String[] VIEW_CRITERIA_PREFIXS;
    /** VIEWの詳細画面にするテーブル名 */
    protected static final String VIEW_DETAIL;
    /** ガントチャート化を判定する項目名・開始日・終了日のカラムサフィックス */
    private static Set<String[]> ganttColumns = new LinkedHashSet<String[]>();
    /** メニュー化しない正規表現 */
    private static String navIgnoreRe;

    /** 読み取り専用サフィックス */
    protected static final String[] INPUT_READONLY_SUFFIXS;
    /** 数値入力サフィックス */
    private static String[] inputNumberSuffixs;
    /** 年月入力サフィックス */
    protected static final String[] INPUT_YM_SUFFIXS;
    /** 8桁日付入力サフィックス */
    protected static final String[] INPUT_DATE8_SUFFIXS;
    /** タイムスタンプサフィックス */
    protected static final String[] TS_SUFS;
    /** 日時入力サフィックス */
    protected static final String[] INPUT_DATETIME_SUFFIXS;
    /** 日付入力サフィックス */
    protected static final String[] INPUT_DATE_SUFFIXS;
    /** 時刻入力サフィックス */
    protected static final String[] INPUT_HOUR_SUFFIXS;
    /** 時間入力サフィックス */
    protected static final String[] INPUT_TIME_SUFFIXS;
    /** 範囲指定サフィックス */
    private static String[] inputRangeSuffixs;
    /** フラグサフィックス */
    protected static final String[] INPUT_FLAG_SUFFIXS;
    /** ビットフラグサフィックス */
    protected static final String[] INPUT_BIT_SUFFIXS;
    /** ファイルサフィックス */
    protected static final String[] FILE_SUFS;
    /** options項目サフィックス */
    protected static final String[] OPTIONS_SUFFIXS;
    /** pulldown項目サフィックス */
    private static String[] pulldownSuffixs;
    /** テキストエリア項目サフィックス */
    protected static final String[] TEXTAREA_SUFFIXS;

    /** データjson */
    protected static final String JSON;
    /** 区分カラム */
    protected static final String OPT_K;
    /** 区分値カラム */
    protected static final String OPT_V;
    /** 区分値名カラム */
    protected static final String OPT_L;

    /** 業務順のプレフィクス正規表現 */
    private static Map<String, String> navOrderRes = new TreeMap<String, String>();

    static {
        ELDEST_RE = bundle.getString("relation.eldest.re");
        String[] pairs = bundle.getString("relation.refer.pairs").split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            REFER_PAIRS.add(kv);
        }
        TEKIYO_BI = bundle.getString("column.start");
        HAISHI_BI = bundle.getString("column.until");
        UPDATE_TS = bundle.getString("column.update.timestamp");
        STATUS = bundle.getString("column.status");
        DELETE_F = bundle.getString("column.delete");
        REASON = bundle.getString("column.reason");
        INT_NOFORMAT_SUFFIXS = bundle.getString("column.int.noformat.suffixs").split(",");
        CHAR_NOTNULL_RE = bundle.getString("column.char.notnull.re");
        GRID_ROWS = bundle.getString("grid.rows");
        VIEW_CRITERIA_PREFIXS = bundle.getString("view.criteria.prefix").split(",");
        VIEW_DETAIL = bundle.getString("view.detail");
        INPUT_READONLY_SUFFIXS = bundle.getString("input.readonly.suffixs").split(",");
        INPUT_YM_SUFFIXS = bundle.getString("input.ym.suffixs").split(",");
        INPUT_DATE8_SUFFIXS = bundle.getString("input.date8.suffixs").split(",");
        TS_SUFS = bundle.getString("input.timestamp.suffixs").split(",");
        inputNumberSuffixs = bundle.getString("input.number.suffixs").split(",");
        INPUT_DATETIME_SUFFIXS = bundle.getString("input.datetime.suffixs").split(",");
        INPUT_DATE_SUFFIXS = bundle.getString("input.date.suffixs").split(",");
        INPUT_HOUR_SUFFIXS = bundle.getString("input.hour.suffixs").split(",");
        INPUT_TIME_SUFFIXS = bundle.getString("input.time.suffixs").split(",");
        INPUT_FLAG_SUFFIXS = bundle.getString("input.flag.suffixs").split(",");
        INPUT_BIT_SUFFIXS = bundle.getString("input.bit.suffixs").split(",");
        FILE_SUFS = bundle.getString("input.file.suffixs").split(",");
        OPTIONS_SUFFIXS = bundle.getString("input.options.suffixs").split(",");
        TEXTAREA_SUFFIXS = bundle.getString("input.textarea.suffixs").split(",");
        JSON = bundle.getString("options.json");
        OPT_K = bundle.getString("options.key").toUpperCase();
        OPT_V = bundle.getString("options.value").toUpperCase();
        OPT_L = bundle.getString("options.label").toUpperCase();
    }

    /** プライベートコンストラクタ */
    protected HtmlGenerator() {
    }

    /**
     * HTMLファイル出力
     * @param projectDir プロジェクトディレクトリ
     * @param tables テーブル情報のリスト
     */
    static void generate(final String projectDir, final List<TableInfo> tables) {

        youngestRe = bundle.getString("relation.youngest.re");

        String[] ganttDefs = bundle.getString("gantt.columns").split(",");
        for (String ganttDef : ganttDefs) {
            String[] columns = ganttDef.split(":");
            ganttColumns.add(columns);
        }

        navIgnoreRe = bundle.getString("nav.ignore.re");

        inputRangeSuffixs = bundle.getString("input.range.suffixs").split(",");
        pulldownSuffixs = bundle.getString("input.pulldown.suffixs").split(",");

        // 業務並び順
        for (String key : bundle.keySet()) {
            if (key.startsWith("nav.order.prefix.re.")) {
                String order = key.replaceFirst("nav.order.prefix.re.", "");
                String re = bundle.getString(key);
                navOrderRes.put(order, re);
            }
        }

        // 出力フォルダを再作成
        String htmlDir = projectDir + File.separator + bundle.getString("dir.html");
        FileUtil.reMkDir(htmlDir);
        FileUtil.reMkDir(htmlDir + File.separator + ".." + File.separator + "common");

        String gridDir = projectDir + File.separator + bundle.getString("dir.grid");
        FileUtil.reMkDir(gridDir);

        for (TableInfo table : tables) {

            // 検索画面のHTMLを出力
            HtmlGeneratorIndex.htmlIndex(htmlDir, table, tables);

            // 検索画面のGridColumnを出力
            HtmlGeneratorIndex.htmlGridColumns(gridDir, table);

            // 単画面を出力
            HtmlGenerator.htmlDetail(htmlDir, table, tables);

            // thymeleafのプロパティファイルを出力
            HtmlGenerator.htmlProperties(htmlDir, table, tables);

            if (table.isGantt()) {
                for (String[] ganttColumn : ganttColumns) {
                    String nameColumn = null;
                    String startColumn = null;
                    String endColumn = null;
                    String sinceColumn = null;
                    String untilColumn = null;
                    for (String columnName : table.getColumns().keySet()) {
                        if (StringUtil.endsWithIgnoreCase(ganttColumn[0], columnName)) {
                            nameColumn = columnName;
                        } else if (StringUtil.endsWithIgnoreCase(ganttColumn[1], columnName)) {
                            startColumn = columnName;
                        } else if (StringUtil.endsWithIgnoreCase(ganttColumn[2], columnName)) {
                            endColumn = columnName;
                        } else if (ganttColumn.length > 3) {
                            if (StringUtil.endsWithIgnoreCase(ganttColumn[3], columnName)) {
                                sinceColumn = columnName;
                            } else if (StringUtil.endsWithIgnoreCase(ganttColumn[4], columnName)) {
                                untilColumn = columnName;
                            }
                        }
                    }
                    if (nameColumn != null && startColumn != null && endColumn != null) {
                        HtmlGeneratorGantt.htmlGantt(htmlDir, table, tables);
                        HtmlGeneratorGantt.htmlGanttTasks(gridDir, table, nameColumn, startColumn, endColumn,
                                sinceColumn, untilColumn);
                    }
                }
            }
        }

        HtmlGenerator.htmlNav(htmlDir, tables);
    }

    /**
     * @param s
     * @param table
     */
    public static void addGridReferHiddenLinks(final List<String> s, final TableInfo table) {

        Set<String> columnNames = new HashSet<String>();

        String e = StringUtil.toPascalCase(table.getName());
        if (table.getSummaryTo() != null) {
            TableInfo summary = table.getSummaryTo();
            for (ColumnInfo column : table.getColumns().values()) {
                if (BeanGenerator.isMetaBy(column.getName())) {
                    continue;
                }
                if (!summary.getPrimaryKeys().contains(column.getName())) {
                    continue;
                }
                String p = StringUtil.toCamelCase(column.getName());
                String summaryE = StringUtil.toPascalCase(summary.getName());
                String action = "";
                String css = "";
                if (summary.getStintInfo() != null && table != summary.getStintInfo()) {
                    action = "?action=" + summaryE + "Correct.ajax";
                    css = " correct";
                }
                columnNames.add(column.getName());
                s.add("        <a id=\"" + e + "Grid." + p + "\" th:href=\"@{/model/" + summaryE + "S.html" + action
                        + "}\" target=\"dialog\" class=\"refer gridRefer" + css + "\" th:text=\"#{" + summaryE
                        + "S.title} + #{common.refer}\" tabindex=\"-1\">...</a>");
            }
        } else if (table.getMergeFroms() != null && table.getMergeFroms().size() > 0) {
            for (TableInfo mergeFrom : table.getMergeFroms()) {
                for (ColumnInfo column : table.getColumns().values()) {
                    if (BeanGenerator.isMetaBy(column.getName())) {
                        continue;
                    }
                    if (!mergeFrom.getPrimaryKeys().contains(column.getName())) {
                        continue;
                    }
                    String p = StringUtil.toCamelCase(column.getName());
                    String summaryE = StringUtil.toPascalCase(mergeFrom.getName());
                    String action = "";
                    String css = "";
                    if (mergeFrom.getStintInfo() != null && table != mergeFrom.getStintInfo()) {
                        action = "?action=" + summaryE + "Correct.ajax";
                        css = " correct";
                    }
                    columnNames.add(column.getName());
                    s.add("        <a id=\"" + e + "Grid." + p + "\" th:href=\"@{/model/" + summaryE + "S.html" + action
                            + "}\" target=\"dialog\" class=\"refer gridRefer" + css + "\" th:text=\"#{" + summaryE
                            + "S.title} + #{common.refer}\" tabindex=\"-1\">...</a>");
                }
            }
        } else if (table.getChoices() != null && table.getChoices().size() > 0) {
            for (TableInfo choise : table.getChoices()) {
                for (ColumnInfo column : table.getColumns().values()) {
                    if (BeanGenerator.isMetaBy(column.getName())) {
                        continue;
                    }
                    if (!choise.getPrimaryKeys().contains(column.getName())) {
                        continue;
                    }
                    String p = StringUtil.toCamelCase(column.getName());
                    String summaryE = StringUtil.toPascalCase(choise.getName());
                    String action = "";
                    String css = "";
                    if (choise.getStintInfo() != null && table != choise.getStintInfo()) {
                        action = "?action=" + summaryE + "Correct.ajax";
                        css = " correct";
                    }
                    columnNames.add(column.getName());
                    s.add("        <a id=\"" + e + "Grid." + p + "\" th:href=\"@{/model/" + summaryE + "S.html" + action
                            + "}\" target=\"dialog\" class=\"refer gridRefer" + css + "\" th:text=\"#{" + summaryE
                            + "S.title} + #{common.refer}\" tabindex=\"-1\">...</a>");
                }
            }
        }

        if (table.getDeriveFroms() != null && table.getDeriveFroms().size() > 0) {
            for (TableInfo derivee : table.getDeriveFroms()) {
                for (ColumnInfo column : table.getColumns().values()) {
                    if (columnNames.contains(column.getName())) {
                        continue;
                    }
                    if (BeanGenerator.isMetaBy(column.getName())) {
                        continue;
                    }
                    if (!derivee.getPrimaryKeys().contains(column.getName())) {
                        continue;
                    }
                    if (column.getNullable() == 0) {
                        continue;
                    }
                    String p = StringUtil.toCamelCase(column.getName());
                    String summaryE = StringUtil.toPascalCase(derivee.getName());
                    String action = "";
                    String css = "";
                    if (derivee.getStintInfo() != null && table != derivee.getStintInfo()) {
                        action = "?action=" + summaryE + "Correct.ajax";
                        css = " correct";
                    }
                    columnNames.add(column.getName());
                    s.add("        <a id=\"" + e + "Grid." + p + "\" th:href=\"@{/model/" + summaryE + "S.html" + action
                            + "}\" target=\"dialog\" class=\"refer gridRefer" + css + "\" th:text=\"#{" + summaryE
                            + "S.title} + #{common.refer}\" tabindex=\"-1\">...</a>");
                }
            }
        }

        for (ColumnInfo column : table.getColumns().values()) {
            if (column.getRefer() != null && !columnNames.contains(column.getName())) {
                if (BeanGenerator.isMetaBy(column.getName())) {
                    continue;
                }
                String p = StringUtil.toCamelCase(column.getName());
                TableInfo refer = column.getRefer();
                String referE = StringUtil.toPascalCase(refer.getName());
                String action = "";
                String css = "";
                if (refer.getStintInfo() != null && table != refer.getStintInfo()) {
                    action = "?action=" + referE + "Correct.ajax";
                    css = " correct";
                }
                s.add("        <a id=\"" + e + "Grid." + p + "\" th:href=\"@{/model/" + referE + "S.html" + action
                        + "}\" target=\"dialog\" class=\"refer gridRefer" + css + "\" th:text=\"#{" + referE
                        + "S.title} + #{common.refer}\" tabindex=\"-1\">...</a>");
            }
        }
    }

    /**
     * @param s
     * @param e
     * @param tableName
     */
    public static void addHtmlHead(final List<String> s, final String e, final String tableName) {
        s.add("<!DOCTYPE html>");
        s.add("<html xmlns:th=\"http://www.thymeleaf.org\" xmlns:layout=\"http://www.ultraq.net.nz/web/thymeleaf/layout\" layout:decorate=\"~{common/base}\">");
        s.add("<head>");
        s.add("<meta charset=\"UTF-8\">");
        s.add("<title th:text=\"#{" + e + ".title}\">" + tableName + "</title>");
        s.add("<style type=\"text/css\">");
        s.add("</style>");
        s.add("<script type=\"text/javascript\">");
        s.add("");
        s.add("</script>");
    }

    /**
     * 履歴モデルでない かつ ビューでない かつ 転生元が必須でない<br>
     * 適用日を除く主キーが一つ か 組合せモデル か 主キーが２つ以上で全て採番キーでない
     * @param table
     * @return boolean
     */
    protected static boolean isAnew(final TableInfo table) {

        // 履歴モデルなら作成不可
        if (table.isHistory()) {
            return false;
        }

        // 子モデルなら作成不可
        if (table.getParents().size() > 0) {
            return false;
        }

        // ビューなら作成不可
        if (table.isView() || table.isStatusFlow()) {
            return false;
        }

        // 組合せモデルなら作成可
        if (table.getComboInfos().size() > 1) {
            return true;
        }

        // 転生元/*か派生元*/が必須なら作成不可
        for (ColumnInfo column : table.getColumns().values()) {
            if ((column.isReborn() /* || column.isDerive() */) && column.getNullable() != 1) {
                return false;
            }
        }

        // 適用日を除く主キーが、一つなら作成可
        List<String> primaryKeys = new ArrayList<String>(table.getPrimaryKeys());
        primaryKeys.remove(TEKIYO_BI);
        if (primaryKeys.size() == 1) {
            return true;
        }

        // 適用日を除く主キーが２以上ある

        // 複合キーの孤児は兄弟から作れるので新規機能は付けない
        //        // 孤児モデルなら作成可
        //        if (table.getName().matches(orphansRe)) {
        //            return true;
        //        }

        // 適用日を除く主キーのうち、最終キー以外の一つでも、参照モデルでない採番キーなら作成不可
        for (int i = 0; i < primaryKeys.size() - 1; i++) {
            String pk = primaryKeys.get(i);
            ColumnInfo primaryKey = table.getColumns().get(pk);
            if (primaryKey.getRefer() == null && primaryKey.isNumbering()) {
                return false;
            }
        }

        return true;
    }

    /**
     * 詳細画面 HTML出力
     * @param htmlDir HTMLファイル出力ディレクトリ
     * @param table テーブル情報
     * @param tables
     */
    private static void htmlDetail(final String htmlDir, final TableInfo table, final List<TableInfo> tables) {
        String e = StringUtil.toPascalCase(table.getName());
        List<String> s = new ArrayList<String>();
        addHtmlHead(s, e, table.getName());
        s.add("<script th:src=\"@{/model/" + e + ".js}\"></script>");
        s.add("<script th:src=\"@{/model/" + e + "GridColumns.js}\"></script>");
        Map<TableInfo, Integer> added = new HashMap<TableInfo, Integer>();
        added.put(table, 1);
        htmlNestGrid(s, table, tables, added, false, "", false);
        s.add("</head>\r\n<body>\r\n  <div layout:fragment=\"article\">");
        s.add("    <h2 th:text=\"#{" + e + ".h2}\">h2</h2>");
        s.add("    <form name=\"" + e + "RegistForm\" action=\"" + e + "Regist.ajax\" class=\"regist\">");
        for (TableInfo parent : table.getParents()) { // 親モデル
            String p = StringUtil.toPascalCase(parent.getName());
            s.add("      <fieldset class=\"parent\">");
            s.add("        <legend th:text=\"#{" + p + ".legend}\">legend</legend>");
            htmlFields(parent, s, true, false, true);
            s.add("      </fieldset>");
        }
        String css = "";
        if (table.isView()) {
            css = " class=\"view\"";
        }
        s.add("      <fieldset" + css + ">");
        s.add("        <legend th:text=\"#{" + e + ".legend}\">legend</legend>");
        htmlFields(table, s, true, false, false);
        s.add("      </fieldset>");
        if (table.getBrothers() != null) { // 兄弟モデル
            for (TableInfo bro : table.getBrothers()) {
                String b = StringUtil.toPascalCase(bro.getName());
                String className = "";
                if (bro.getName().matches(youngestRe)) {
                    className = " class=\"youngest\"";
                }
                s.add("      <fieldset" + className + ">");
                s.add("        <legend th:text=\"#{" + b + ".legend}\">legend</legend>");
                htmlFields(bro, s, true, true, false);
                s.add("      </fieldset>");
            }
        }
        for (TableInfo child : table.getChildren()) { // 子テーブルリスト
            String c = StringUtil.toPascalCase(child.getName());
            s.add("      <h3 th:text=\"#{" + c + ".h3}\">h3</h3>");
            s.add("      <a th:href=\"@{/model/" + c + ".html(anew)}\" id=\"" + c + "\" target=\"dialog\" th:text=\"#{"
                    + c + ".add}\" class=\"addChild\" tabindex=\"-1\">" + child.getName() + "</a>");
            String addRow = " data-addRow=\"true\"";
            for (ColumnInfo column : child.getColumns().values()) {
                if (StringUtil.endsWith(FILE_SUFS, column.getName())) {
                    addRow = ""; // ファイル列がある場合は新規行を取消
                    break;
                }
            }
            String frozen = String.valueOf(table.getPrimaryKeys().size());
            s.add("      <div id=\"" + c + "Grid\" data-selectionMode=\"link\"" + addRow + " data-frozenColumn=\""
                    + frozen + "\" th:data-href=\"@{/model/" + c + ".html}\" class=\"childs\"></div>");
            addGridReferHiddenLinks(s, child);
        }
        s.add("      <div class=\"buttons\">");
        if (!table.isHistory() && !table.isView() && !table.isStatusFlow()) {
            s.add("        <button type=\"button\" id=\"Refresh" + e
                    + "\" th:text=\"#{common.reset}\" class=\"reset\" onClick=\"Dialogate.refresh(event);\">reset</button>");
        }
        s.add("        <a th:href=\"@{" + e + "Get.xlsx(baseMei=#{" + e + ".h2})}\" id=\""
                + e + "Get.xlsx\" th:text=\"#{common.xlsx}\" class=\"output\" tabindex=\"-1\">xlsx</a>");
        if (table.getRebornTo() != null) { // 転生先がある場合は追加ボタンを出力
            TableInfo reborn = table.getRebornTo();
            String r = StringUtil.toPascalCase(reborn.getName());
            s.add("        <a th:href=\"@{/model/" + r + ".html}\" id=\"" + r + "\" target=\"dialog\" th:text=\"#{" + r
                    + ".add}\" class=\"reborner\" tabindex=\"-1\">" + reborn.getName() + "</a>");
        }
        if (table.getDeriveTos().size() > 0) { // 派生先がある場合は追加ボタンを出力
            for (TableInfo deriveTo : table.getDeriveTos()) {
                String r = StringUtil.toPascalCase(deriveTo.getName());
                s.add("        <a th:href=\"@{/model/" + r + ".html}\" id=\"" + r + "\" target=\"dialog\" th:text=\"#{"
                        + r + ".add}\" class=\"deriveTo\" tabindex=\"-1\">" + deriveTo.getName() + "</a>");
            }
        }
        //        if (table.getChoosers() != null) { // 選抜先がある場合は追加ボタンを出力
        //            for (TableInfo chooser : table.getChoosers()) {
        //                String r = StringUtil.toPascalCase(chooser.getName());s.add("        <a th:href=\"@{/model/" + r + ".html}\" id=\"" + r + "\" target=\"dialog\" th:text=\"#{" + r + ".add}\" class=\"chooser\" tabindex=\"-1\">" + chooser.getRemarks() + "</a>");
        //            }
        //        }
        if (table.getSummaryOfs().size() > 0) { // 集約元がある場合は主キー項目を出力
            for (TableInfo summaryOf : table.getSummaryOfs()) {
                String m = StringUtil.toPascalCase(summaryOf.getName());
                //            if (table.getRebornTo() == null) { // 転生先で必須でない場合でも、自モデルが他の転生先である場合は転生元となるよう変更したので、ここはコメントアウト
                //                // 転生先（自主キーが必須の外部キーになっている）がなければ追加ボタンを出力（転生先で必須でないケース）
                //                s.add("        <a th:href=\"@{/model/" + e + ".html}\" id=\"" + e + "\" target=\"dialog\" th:text=\"#{" + e + ".add}\" class=\"reborner\" tabindex=\"-1\">" + summary.getRemarks() + "</a>");
                //            }
                for (String pk : summaryOf.getPrimaryKeys()) {
                    ColumnInfo primaryKey = summaryOf.getColumns().get(pk);
                    String p = StringUtil.toCamelCase(pk);
                    s.add("        <div class=\"summary " + m + "s\">");
                    s.add("          <label>" + primaryKey.getName() + ": </label>");
                    s.add("          <span id=\"" + m + "." + p + "\"></span>");
                    s.add("          <input type=\"hidden\" id=\"" + m + "." + p + "\" name=\"" + m + "." + p
                            + "\" />");
                    s.add("        </div>");
                }
            }
        }
        s.add("      </div>\r\n      <div class=\"submits\">");
        if (!table.isHistory() && !table.isView() && !table.isStatusFlow()) { // 履歴モデルでもビューでもない場合
            if (!table.getColumns().containsKey(DELETE_F) && !table.getColumns().containsKey(HAISHI_BI)) {
                s.add("        <button id=\"Delete" + e + "\" type=\"submit\" class=\"delete\" data-action=\"" + e
                        + "Delete.ajax\" th:text=\"#{common.delete}\" tabindex=\"-1\">delete</button>");
            } // 削除フラグ列名の指定がないか、テーブルに削除フラグ列がないなら、物理削除ボタンを表示
            addKessaiButtons(e, s, table);
            String onclick = "";
            if (table.getHistory() != null && !StringUtil.isNullOrWhiteSpace(REASON)) {
                onclick = " onclick=\"if (!Base.rirekiTx(this)) { return false; }\"";
            }
            s.add("        <button id=\"Regist" + e
                    + "\" type=\"submit\" class=\"regist\" th:text=\"#{common.regist}\"" + onclick
                    + ">regist</button>");
        }
        s.add("      </div>");
        if (table.getRebornTo() != null) {
            TableInfo rebornTo = table.getRebornTo();
            String c = StringUtil.toPascalCase(rebornTo.getName());
            s.add("      <h3 th:text=\"#{" + c + ".h3}\">h3</h3>");
            String frozen = String.valueOf(rebornTo.getPrimaryKeys().size());
            s.add("      <div id=\"" + c + "Grid\" data-selectionMode=\"link\" data-frozenColumn=\"" + frozen
                    + "\" th:data-href=\"@{/model/" + c + ".html}\" class=\"reborners\"></div>");
        }
        // if (table.getChoosers() != null) {     for (TableInfo chooser : table.getChoosers()) {
        //         String c = StringUtil.toPascalCase(chooser.getName());
        //         s.add("      <h3 th:text=\"#{" + c + ".h3}\">h3</h3>");
        //         String frozen = String.valueOf(chooser.getPrimaryKeys().size());
        //         s.add("      <div id=\"" + c + "Grid\" data-selectionMode=\"link\" data-frozenColumn=\"" + frozen + "\" th:data-href=\"@{/model/" + c + ".html}\" class=\"choosers\"></div>");
        //     }        }
        s.add("    </form>");
        s.add("  </div>");
        s.add("</body>");
        s.add("</html>");
        FileUtil.writeFile(htmlDir + File.separator + e + ".html", s);
    }

    /**
     * @param e
     * @param s
     * @param table
     */
    public static void addKessaiButtons(final String e, final List<String> s, final TableInfo table) {
        //ステータス列名の指定があり、テーブルにステータス列があるなら、承認ボタン・否認ボタンを表示
        if (table.getColumns().containsKey(STATUS)) {
            String onClick = "";
            if (table.getStatusFlow() != null && !StringUtil.isNullOrWhiteSpace(REASON)) {
                onClick = " onclick=\"if (!Base.kessaiTx(this)) { return false; }\"";
            }
            s.add("        <button type=\"submit\"" + onClick + " id=\"Permit" + e + "\" data-action=\"" + e
                    + "Permit.ajax\" class=\"permit\" th:text=\"#{common.permit}\" tabindex=\"-1\">permit</button>");
            s.add("        <button type=\"submit\"" + onClick + " id=\"Forbid" + e + "\" data-action=\"" + e
                    + "Forbid.ajax\" class=\"forbid\" th:text=\"#{common.forbid}\" tabindex=\"-1\">forbid</button>");
            s.add("        <button type=\"submit\"" + onClick + " id=\"Apply" + e + "\" data-action=\"" + e
                    + "Apply.ajax\" class=\"apply\" th:text=\"#{common.apply}\" tabindex=\"-1\">apply</button>");
            s.add("        <button type=\"submit\"" + onClick + " id=\"Cancel" + e + "\" data-action=\"" + e
                    + "Cancel.ajax\" class=\"cancel\" th:text=\"#{common.cancel}\" tabindex=\"-1\">cancel</button>");
        }
    }

    /**
     * 各モデルのプロパティファイル出力
     * @param htmlDir HTMLファイル出力ディレクトリ
     * @param table テーブル情報
     * @param tables
     */
    private static void htmlProperties(final String htmlDir, final TableInfo table, final List<TableInfo> tables) {

        List<String> s = new ArrayList<String>();
        String e = StringUtil.toPascalCase(table.getName());
        String remarks = table.getRemarks();
        s.add(e + "S.title   " + remarks + "検索");
        s.add(e + "S.h2      " + remarks + "検索");
        s.add(e + "S.legend  " + remarks + "検索");
        s.add(e + ".add      " + remarks + "追加");
        s.add(e + ".title    " + remarks);
        s.add(e + ".h2       " + remarks);
        s.add(e + ".legend   " + remarks);
        s.add(e + ".h3       " + remarks + "一覧");

        s.add("");
        for (ColumnInfo column : table.getColumns().values()) {
            String property = StringUtil.toCamelCase(column.getName());
            s.add(e + "." + property + " " + column.getRemarks());
            // 派生元の選択は表示しない
            //if (column.getDeriveFrom() != null) {
            //    for (String primaryKey : column.getDeriveFrom().getPrimaryKeys()) {
            //        ColumnInfo pk = column.getDeriveFrom().getColumns().get(primaryKey);
            //        String p = StringUtil.toCamelCase(pk.getName());
            //        s.add(e + ".derivee" + StringUtil.toPascalCase(p) + " " + pk.getRemarks());
            //    }
            //}
        }

        s.add("");
        for (ColumnInfo column : table.getColumns().values()) {
            String property = StringUtil.toCamelCase(column.getName());
            s.add(e + "Grid." + property + " " + column.getRemarks());
        }
        for (TableInfo brother : table.getBrothers()) {
            for (ColumnInfo column : brother.getColumns().values()) {
                if (!column.isPk() && !BeanGenerator.isMetaTsBy(column.getName())) {
                    String p = StringUtil.toCamelCase(column.getName());
                    s.add(e + "Grid." + p + " " + column.getRemarks());
                }
            }
        }

        for (TableInfo brother : table.getBrothers()) {
            String b = StringUtil.toPascalCase(brother.getName());
            s.add("");
            s.add(b + ".legend   " + brother.getRemarks());
            for (ColumnInfo column : brother.getColumns().values()) {
                String property = StringUtil.toCamelCase(column.getName());
                s.add(b + "." + property + " " + column.getRemarks());
            }
        }

        for (TableInfo parent : table.getParents()) {
            String p = StringUtil.toPascalCase(parent.getName());
            s.add("");
            s.add(p + ".legend   " + parent.getRemarks());
            for (ColumnInfo column : parent.getColumns().values()) {
                String property = StringUtil.toCamelCase(column.getName());
                s.add(p + "." + property + " " + column.getRemarks());
            }
        }

        for (TableInfo child : table.getChildren()) {
            String c = StringUtil.toPascalCase(child.getName());
            String mei = child.getRemarks();
            s.add("");
            s.add(c + ".h3  " + mei + "一覧");
            s.add(c + ".add " + mei + "追加");
            for (ColumnInfo column : child.getColumns().values()) {
                String property = StringUtil.toCamelCase(column.getName());
                s.add(c + "Grid." + property + " " + column.getRemarks());
            }
        }

        if (table.getRebornTo() != null) {
            TableInfo reborn = table.getRebornTo();
            String r = StringUtil.toPascalCase(reborn.getName());
            String mei = reborn.getRemarks();
            s.add("");
            s.add(r + ".h3  " + mei + "一覧");
            s.add(r + ".add " + mei + "追加");
            for (ColumnInfo column : reborn.getColumns().values()) {
                String property = StringUtil.toCamelCase(column.getName());
                s.add(r + "Grid." + property + " " + column.getRemarks());
            }
        }

        if (table.getDeriveTos().size() > 0) {
            for (TableInfo deriveTo : table.getDeriveTos()) {
                String d = StringUtil.toPascalCase(deriveTo.getName());
                s.add("");
                s.add(d + ".add " + deriveTo.getRemarks() + "追加");
            }
        }

        if (table.getSummaryOfs().size() > 0) {
            for (TableInfo summaryOf : table.getSummaryOfs()) {
                String so = StringUtil.toPascalCase(summaryOf.getName());
                s.add("");
                s.add(so + ".add " + summaryOf.getRemarks() + "追加");
            }
        }

        TableInfo summaryTo = table.getSummaryTo();
        if (summaryTo != null) {
            String st = StringUtil.toPascalCase(summaryTo.getName());
            s.add("");
            s.add(st + ".sum " + summaryTo.getRemarks() + "集約");
        }

        FileUtil.writeFile(htmlDir + File.separator + e + ".properties", s);
    }

    /**
     * navファイル出力
     * @param htmlDir HTMLファイル出力ディレクトリ
     * @param tables テーブル情報のリスト
     */
    private static void htmlNav(final String htmlDir, final List<TableInfo> tables) {

        // プレフィクス毎にグループ化
        Map<String, List<TableInfo>> navs = new TreeMap<String, List<TableInfo>>();

        for (TableInfo table : tables) {

            // 親モデルがあればスキップ
            if (table.getParents().size() > 0) {
                continue;
            }

            // テーブル名から「_」より前を取得
            String name = table.getName();
            String prefix = name.replaceAll("_.+$", "");
            String navKey = prefix;

            for (Entry<String, String> navOrderRe : navOrderRes.entrySet()) {
                // propertiesで指定した並び順
                String navOrder = navOrderRe.getKey();
                // propertiesで指定した正規表現
                String prefixRe = navOrderRe.getValue();
                // テーブル名の一つ目の区切りが正規表現にマッチする場合
                if (prefix.matches(prefixRe)) {
                    // テーブル名の一つ目の区切りから正規表現にマッチする部分を業務カテゴリとして取得
                    String category = prefixRe.replaceAll("[^0-9A-Za-z]", "");
                    // 並び順＋業務カテゴリを取得
                    navKey = navOrder + "-" + category;
                }
            }

            List<TableInfo> nav = null;
            if (navs.containsKey(navKey)) {
                nav = navs.get(navKey);
            } else {
                nav = new ArrayList<TableInfo>();
                navs.put(navKey, nav);
            }

            nav.add(table);
        }

        List<String> s = new ArrayList<String>();
        s.add("<!DOCTYPE html>");
        s.add("<html xmlns:th=\"http://www.thymeleaf.org\" xmlns:layout=\"http://www.ultraq.net.nz/web/thymeleaf/layout\">");
        s.add("<body>");
        s.add("  <div class=\"nav\" layout:fragment=\"nav\" th:if=\"${#ctx.session != null && #ctx.session.get('AUTHN_KEY') != null}\">");
        s.add("    <div class=\"toggle\">");
        s.add("      <a href=\"javascript: void(0);\" class=\"toggle\"></a>");
        s.add("    </div>");
        s.add("    <dl>");
        for (Entry<String, List<TableInfo>> nav : navs.entrySet()) {
            String navKey = nav.getKey();
            String category = navKey.replaceAll("^.*\\d*?-", "");
            s.add("      <dt id=\"" + category + "\" th:text=\"#{nav.dt." + category + "}\">"
                    + category + "</dt>");
            s.add("      <dd>");
            s.add("        <ul>");
            //            String preName = "";
            for (TableInfo table : nav.getValue()) {
                if (table.getName().matches(navIgnoreRe) || table.getName().matches(youngestRe)) {
                    continue;
                }
                String name = table.getName();
                String e = StringUtil.toPascalCase(name);
                String css = "table";
                if (table.isHistory()) {
                    css = "history";
                } else if (table.isView()) {
                    css = "view";
                    //                    // 直前のテーブル名と前方一致するなら回り込み
                    //                    if (name.startsWith(preName)) {
                    //                        style = " style=\"float: left;\"";
                    //                    }
                }

                String prefix = name.replaceAll("_.+$", "");
                String type = prefix.replaceAll(category, "");
                if (!StringUtil.isNullOrWhiteSpace(type)) {
                    css += " " + type;
                }

                //                preName = name;
                s.add("          <li><a id=\"" + e + "\" th:href=\"@{/model/" + e + "S.html}\" th:text=\"#{nav." + e
                        + "S}\" class=\"" + css + "\">" + name + "</a></li>");
                if (table.isGantt()) {
                    s.add("          <li><a id=\"" + e + "G\" th:href=\"@{/model/" + e + "G.html}\" th:text=\"#{nav."
                            + e + "G}+#{nav.Gantt}\" class=\"" + css + " ganttNav\">" + name + "</a></li>");
                }
            }
            s.add("        </ul>");
            s.add("      </dd>");
        }
        s.add("    </dl>");
        s.add("  </div>");
        s.add("</body>");
        s.add("</html>");

        String sep = File.separator;
        FileUtil.writeFile(htmlDir + sep + ".." + sep + "common" + sep + "nav.html", s);

        s = new ArrayList<String>();
        for (Entry<String, List<TableInfo>> nav : navs.entrySet()) {
            String category = nav.getKey();
            category = category.replaceAll("^.*\\d*?-", "");
            s.add("nav.dt." + category + " " + category);
            for (TableInfo table : nav.getValue()) {
                String name = table.getName();
                String remarks = table.getRemarks();
                String e = StringUtil.toPascalCase(name);
                s.add("nav." + e + "S " + remarks);
                if (table.isGantt()) {
                    s.add("nav." + e + "G " + remarks);
                }
            }
        }

        FileUtil.writeFile(htmlDir + sep + ".." + sep + "common" + sep + "nav.properties", s);
    }

    /**
     * ネストした兄弟モデル・子モデル・参照モデルのgrid定義出力
     * @param s 出力文字列のリスト
     * @param table    テーブル情報
     * @param tables
     * @param added    出力済みテーブル情報のリスト
     * @param isParent 親モデルか
     * @param indent   インデント
     * @param isRefer  参照モデルか
     */
    protected static void htmlNestGrid(final List<String> s, final TableInfo table, final List<TableInfo> tables,
            final Map<TableInfo, Integer> added, final boolean isParent, final String indent, final boolean isRefer) {

        // 参照モデル
        for (ColumnInfo column : table.getColumns().values()) {
            TableInfo e = column.getRefer();
            if (e != null) {
                if (!added.containsKey(e)) {
                    String name = StringUtil.toPascalCase(e.getName());
                    addGridJs(s, indent, table.getName(), name, "参照モデル");
                    added.put(e, 1);
                    htmlNestGrid(s, e, tables, added, false, indent + "  ", true);
                }
            }
        }
        // 兄弟モデルの参照モデル
        for (TableInfo bro : table.getBrothers()) {
            for (ColumnInfo column : bro.getColumns().values()) {
                TableInfo e = column.getRefer();
                if (e != null) {
                    if (!added.containsKey(e)) {
                        String name = StringUtil.toPascalCase(e.getName());
                        addGridJs(s, indent, table.getName(), name, "兄弟モデルの参照モデル");
                        added.put(e, 1);
                        htmlNestGrid(s, e, tables, added, false, indent + "  ", true);
                    }
                }
            }
        }
        // 転生元モデル：転生元は転生先で参照になるためisReferの外で処理する
        if (table.getRebornFrom() != null) {
            TableInfo e = table.getRebornFrom();
            if (added.get(e) == null || added.get(e) != 0) {
                if (!added.containsKey(e)) {
                    String name = StringUtil.toPascalCase(e.getName());
                    addGridJs(s, indent, table.getName(), name, "転生元モデル");
                }
                added.put(e, 0);
                htmlNestGrid(s, e, tables, added, false, indent + "  ", true);
            }
        }
        // 派生元モデル
        for (ColumnInfo column : table.getColumns().values()) {
            if (column.getDeriveFrom() != null) {
                TableInfo e = column.getDeriveFrom();
                if (added.get(e) != null && added.get(e) == 0) {
                    continue;
                }
                if (!added.containsKey(e)) {
                    String name = StringUtil.toPascalCase(e.getName());
                    addGridJs(s, indent, table.getName(), name, "派生元モデル");
                }
                added.put(e, 0);
                htmlNestGrid(s, e, tables, added, false, indent + "  ", true);
            }
        }
        // 共生元モデル
        if (table.getMergeFroms().size() > 0) {
            for (TableInfo e : table.getMergeFroms()) {
                if (added.get(e) != null && added.get(e) == 0) {
                    continue;
                }
                if (!added.containsKey(e)) {
                    String name = StringUtil.toPascalCase(e.getName());
                    addGridJs(s, indent, table.getName(), name, "共生元モデル");
                }
                added.put(e, 0);
                htmlNestGrid(s, e, tables, added, false, indent + "  ", true);
            }
        }

        if (!isParent && !isRefer) {
            // 親モデル
            for (TableInfo e : table.getParents()) {
                if (added.get(e) != null && added.get(e) == 0) {
                    continue;
                }
                if (!added.containsKey(e)) {
                    String name = StringUtil.toPascalCase(e.getName());
                    addGridJs(s, indent, table.getName(), name, "親モデル");
                }
                added.put(e, 0);
                htmlNestGrid(s, e, tables, added, true, indent + "  ", false);
            }
            // 子モデル
            for (TableInfo e : table.getChildren()) {
                if (added.get(e) != null && added.get(e) == 0) {
                    continue;
                }
                if (!added.containsKey(e)) {
                    String name = StringUtil.toPascalCase(e.getName());
                    addGridJs(s, indent, table.getName(), name, "子モデル");
                }
                added.put(e, 0);
                htmlNestGrid(s, e, tables, added, false, indent + "  ", false);
            }
            // 転生先モデル
            if (table.getRebornTo() != null) {
                TableInfo e = table.getRebornTo();
                if (added.get(e) == null || added.get(e) != 0) {
                    if (!added.containsKey(e)) {
                        String name = StringUtil.toPascalCase(e.getName());
                        addGridJs(s, indent, table.getName(), name, "転生先モデル");
                    }
                    added.put(e, 0);
                    htmlNestGrid(s, e, tables, added, false, indent + "  ", false);
                }
            }
            // 派生先モデル
            if (table.getDeriveTos().size() > 0) {
                for (TableInfo e : table.getDeriveTos()) {
                    if (added.get(e) != null && added.get(e) == 0) {
                        continue;
                    }
                    if (!added.containsKey(e)) {
                        String name = StringUtil.toPascalCase(e.getName());
                        addGridJs(s, indent, table.getName(), name, "派生先モデル");
                    }
                    added.put(e, 0);
                    htmlNestGrid(s, e, tables, added, false, indent + "  ", false);
                }
            }
            //            // 集約元モデル
            //            if (table.getSummaryOfs().size() > 0) {
            //                for (TableInfo e : table.getSummaryOfs()) {
            //                    if (added.get(e) != null && added.get(e) == 0) {
            //                        continue;
            //                    }
            //                    if (!added.containsKey(e)) {
            //                        String name = StringUtil.toPascalCase(e.getName());
            //                        addGridJs(s, indent, table.getName(), name, "集約元モデル");
            //                    }
            //                    added.put(e, 0);
            //                    htmlNestGrid(s, e, tables, added, false, indent + "  ", false);
            //                }
            //            }
            // 集約先モデル
            TableInfo e = table.getSummaryTo();
            if (e != null) {
                if (added.get(e) == null || added.get(e) != 0) {
                    if (!added.containsKey(e)) {
                        String name = StringUtil.toPascalCase(e.getName());
                        addGridJs(s, indent, table.getName(), name, "集約先モデル");
                    }
                    added.put(e, 0);
                    htmlNestGrid(s, e, tables, added, false, indent + "  ", false);
                }
            }
        }
    }

    /**
     * @param s
     * @param indent
     * @param tableMei
     * @param name
     * @param mei
     */
    public static void addGridJs(final List<String> s, final String indent, final String tableMei, final String name,
            final String mei) {
        s.add(indent + "<!-- " + tableMei + "の" + mei + "のグリッド定義 -->");
        s.add(indent + "<script th:src=\"@{/model/" + name + ".js}\"></script>");
        s.add(indent + "<script th:src=\"@{/model/" + name + "GridColumns.js}\"></script>");
    }

    /**
     * htmlにフィールド追加
     * @param t テーブル情報
     * @param s 出力文字列のリスト
     * @param isD 詳細画面ならtrue
     * @param isB 兄弟モデルならtrue
     * @param isP 親モデルならtrue
     */
    protected static void htmlFields(final TableInfo t, final List<String> s, final boolean isD, final boolean isB,
            final boolean isP) {
        if (!isD && t.getStintInfo() != null) { // 検索画面の場合は制約モデルの参照キーを出力
            htmlFieldsStint(t, s);
        }
        String e = StringUtil.toPascalCase(t.getName());
        for (ColumnInfo c : t.getColumns().values()) { // カラム情報でループ
            String cNm = c.getName();
            if ((isB && c.isPk()) || cNm.matches("(?i)^" + VIEW_DETAIL + "$")) {
                continue; // 兄弟モデルの主キー と VIEWの「TABLE_NAME」なら出力しない
            }
            if (t.isView() && isD && StringUtil.startsWith(VIEW_CRITERIA_PREFIXS, cNm)) {
                continue; // VIEWの詳細フォームには「SEARCH_」を出力しない
            }
            if (!isD && (StringUtil.endsWith(FILE_SUFS, cNm) || StringUtil.endsWith(TS_SUFS, cNm))) {
                continue; // 検索条件にはファイル項目とタイムスタンプを出力しない
            }
            if (!isD && t.isGraph() && c.getNullable() == 1) {
                continue; // グラフモデルで必須でなければ出力しない
            }
            String p = StringUtil.toCamelCase(cNm);
            if (BeanGenerator.isMetaTsBy(cNm)) {
                if (!isD) {
                    continue; // メタ情報の場合検索画面ならスキップ（検索条件にはしない）
                }
                if (isB) {
                    if (cNm.matches("(?i)^" + UPDATE_TS + "$")) {
                        s.add("        <input type=\"hidden\" name=\"" + e + "." + p + "\" />");
                    }
                    continue; // 兄弟モデルならスキップ（更新日時だけは楽観ロック用に出力）
                }
            }
            TableInfo rebornFrom = seekRebornFrom(t, c);
            TableInfo mergeFrom = seekMergeFrom(t, c);
            String referCss = addCssByRelation(isD, t, c);
            String fId = e + "." + p;
            s.add("        <div id=\"" + p + "\">");
            if (BeanGenerator.isMetaTsBy(cNm)) { // メタ情報の場合は表示項目（編集画面の自モデルのみここに到達する）
                htmlFieldsMeta(s, fId, c);
                addMeiSpan(s, t, c, "meta");
            } else if (StringUtil.endsWith(OPTIONS_SUFFIXS, cNm) && c.getRefer() == null) { // 参照モデルでない選択項目の場合
                String css = "";
                if (isD && c.isPk()) { // 詳細画面の主キー
                    css += " primaryKey";
                }
                if (isD && t.isHistory()) { // 履歴モデルの詳細画面
                    css += " history";
                } else if (isD && c.isReborn()) { // 詳細画面の転生元外部キー
                    css += " rebornee";
                } else if (isD && c.getDeriveFrom() != null) { // 詳細画面の派生元外部キー
                    css += " derivee";
                } else if (isD && c.isSummary()) { // 詳細画面の集約先外部キー
                    css += " summary";
                }
                if (isD && c.getNullable() == 0) {
                    css += isNotBlank(c);
                }
                if (isD && t.getHistory() == null && !t.isHistory() && !c.isPk()
                        && StringUtil.endsWith(INPUT_READONLY_SUFFIXS, cNm)) {
                    css += " forceReadonly";
                }
                htmlFieldsOptions(s, fId, cNm, css);
            } else if (isD && StringUtil.endsWith(INPUT_READONLY_SUFFIXS, cNm)) { // 読み取り専用の場合
                String css = "";
                if (c.getTypeName().matches(NUM_RE) && !StringUtil.endsWith(INT_NOFORMAT_SUFFIXS, cNm)) {
                    css = getNumericCss(c);
                }
                htmlFieldsSpan(s, fId, c, css);
                if (c.getRefer() != null) {
                    String rNm = StringUtil.toPascalCase(c.getRefer().getName());
                    if (referCss.contains("correct")) {
                        s.add(getCorrectLink(fId, rNm, referCss)); //選択リンク
                    } else {
                        s.add(getReferLink(fId, rNm, referCss)); //参照リンク
                    }
                }
            } else if (isD && t.isHistory()) { // 履歴モデルの詳細画面
                htmlFieldsSpan(s, fId, c, "history");
                addMeiSpan(s, t, c, "");
            } else if (isD && c.isReborn()) { // 詳細画面の転生元外部キー
                htmlFieldsSpan(s, fId, c, "rebornee");
            } else if (isD && c.getDeriveFrom() != null) { // 詳細画面の派生元外部キー
                String css = "derivee";
                if (t.getSummaryOfs().size() > 0) {
                    css += " summaryOf";
                }
                htmlFieldsSpan(s, fId, c, css);
                if (!isP && t.getSummaryOfs().size() == 0 && c.getNullable() == 1) { // NOTNULLなら派生元参照リンクを出さない
                    s.add(getCorrectLink(fId, StringUtil.toPascalCase(c.getDeriveFrom().getName()), css));
                }
            } else if (isD && c.isSummary()) { // 詳細画面の集約先外部キー
                htmlFieldsSpan(s, fId, c, "summary");
            } else if (StringUtil.endsWith(TS_SUFS, cNm)) { // タイムスタンプの場合
                htmlFieldsSpan(s, fId, c, "");
            } else if (isD && StringUtil.endsWith(TEXTAREA_SUFFIXS, cNm)) { // テキストエリア項目の場合
                String css = "";
                if (isD && c.getNullable() == 0) {
                    css += isNotBlank(c);
                }
                htmlFieldsTextarea(s, fId, c.getName(), css);
            } else { // inputの場合
                String type = getInputType(cNm);
                String inputCss = getInputCss(t, isD, c, cNm);
                String format = getFormat(c, cNm);
                if (!isD && StringUtil.endsWith(inputRangeSuffixs, cNm)) { // 検索画面の範囲指定項目の場合
                    if (t.isGraph()) {
                        inputCss += " notblank";
                    }
                    s.add(htmlFieldsRange(fId, type, inputCss, c, format));
                } else if (((!t.isView() && !t.isStatusFlow()) || !isD) && c.getRefer() != null) { // 参照モデルの場合
                    s.add(htmlFieldsRefer(fId, type, inputCss, c, format, t, referCss));
                } else {
                    if (StringUtil.endsWith(INPUT_BIT_SUFFIXS, cNm)) {
                        String tag = "          ";
                        tag += "<label for=\"" + fId + "F\" th:text=\"#{" + fId + "}\">" + c.getName().toUpperCase()
                                + "</label>";
                        tag += "<input type=\"" + type + "\" id=\"" + fId + "F\" name=\"" + fId + "F\" maxlength=\""
                                + c.getColumnSize() + "\" class=\"" + inputCss + "\"" + format + " />";
                        s.add(tag);
                        tag = "          ";
                        tag += "<span id=\"" + fId + "\" class=\"" + inputCss + "\"></span>";
                        tag += "<input type=\"hidden\" id=\"" + fId + "\" name=\"" + fId + "\" class=\"" + inputCss
                                + "\" />";
                        s.add(tag);
                    } else {
                        if (c.getDeriveFrom() != null) {
                            inputCss += " derivee";
                        }
                        s.add(htmlFieldsInput(fId, type, inputCss, c, format));
                        if (rebornFrom != null) {
                            String rNm = StringUtil.toPascalCase(rebornFrom.getName());
                            s.add(getLink(isD, fId, rNm));
                        } else if (c.getDeriveFrom() != null) {
                            String rNm = StringUtil.toPascalCase(c.getDeriveFrom().getName());
                            s.add(getLink(isD, fId, rNm));
                        } else if (mergeFrom != null) {
                            String rNm = StringUtil.toPascalCase(mergeFrom.getName());
                            s.add(getLink(isD, fId, rNm));
                        } else if (t.getSummaryTo() != null
                                && t.getSummaryTo().getPrimaryKeys().contains(c.getName())) {
                            String rNm = StringUtil.toPascalCase(t.getSummaryTo().getName());
                            s.add(getLink(isD, fId, rNm));
                        }
                    }
                }
            }
            s.add("        </div>");
        }
    }

    /**
     * @param isD
     * @param fId
     * @param rNm
     * @return String
     */
    private static String getLink(final boolean isD, final String fId, final String rNm) {
        if (isD) {
            return getCorrectLink(fId, rNm, "refer");
        } else {
            return getReferLink(fId, rNm, "refer");
        }
    }

    /**
     * @param c
     * @param colNm
     * @return String
     */
    public static String getFormat(final ColumnInfo c, final String colNm) {
        String format = "";
        if (StringUtil.endsWith(INPUT_DATE8_SUFFIXS, colNm) && c.getColumnSize() == 8) { // 8桁日付項目
            format = "yymmdd";
        }
        return format;
    }

    /**
     * @param t
     * @param c
     * @return TableInfo
     */
    public static TableInfo seekMergeFrom(final TableInfo t, final ColumnInfo c) {
        TableInfo mergeFrom = null;
        if (t.getMergeFroms() != null && t.getMergeFroms().size() > 0) {
            for (TableInfo from : t.getMergeFroms()) {
                if (from.getPrimaryKeys().contains(c.getName())) {
                    mergeFrom = from;
                }
            }
        }
        return mergeFrom;
    }

    /**
     * @param t
     * @param isD
     * @param c
     * @param colNm
     * @return String
     */
    public static String getInputCss(final TableInfo t, final boolean isD, final ColumnInfo c, final String colNm) {

        String inputCss = addCssByRelation(isD, t, c);

        if (isD && c.getNullable() == 0) { // 詳細画面の必須項目
            inputCss += isNotBlank(c);
        }

        if (StringUtil.endsWith(INPUT_DATE_SUFFIXS, colNm)) { // 日付項目および8桁日付項目
            inputCss += " datepicker";
        } else if (StringUtil.endsWith(INPUT_DATE8_SUFFIXS, colNm) && c.getColumnSize() == 8) {
            inputCss += " datepicker";
        } else if (StringUtil.endsWith(INPUT_TIME_SUFFIXS, colNm)) {
            inputCss += " time";
        } else if (StringUtil.endsWith(INPUT_BIT_SUFFIXS, colNm)) {
            inputCss += " bit right";
        }

        //        if (!StringUtil.isNullOrWhiteSpace(inputCss)) {
        //            inputCss = " class=\"" + inputCss + "\"";
        //        }

        return inputCss;
    }

    /**
     * @param fId
     * @param rNm
     * @param css
     * @return String
     */
    public static String getReferLink(final String fId, final String rNm, final String css) {
        return "          <a id=\"" + fId + "\" th:href=\"@{/model/" + rNm
                + "S.html}\" target=\"dialog\" class=\"" + css
                + "\" th:text=\"#{common.refer}\" tabindex=\"-1\">...</a>";
    }

    /**
     * @param fId
     * @param rNm
     * @param css
     * @return String
     */
    public static String getCorrectLink(final String fId, final String rNm, final String css) {
        return "          <a id=\"" + fId + "\" th:href=\"@{/model/" + rNm + "S.html?action=" + rNm
                + "Correct.ajax}\" target=\"dialog\" class=\"" + css
                + "\" th:text=\"#{common.correct}\" tabindex=\"-1\">...</a>";
    }

    /**
     * @param c
     * @return String
     */
    public static String getNumericCss(final ColumnInfo c) {
        if (c.getDecimalDigits() == 3) {
            return "dec3";
        } else if (c.getDecimalDigits() == 2) {
            return "dec2";
        } else if (c.getDecimalDigits() == 1) {
            return "dec1";
        }
        return "dec0";
    }

    /**
     * @param table
     * @param column
     * @return カラムの転生元
     */
    public static TableInfo seekRebornFrom(final TableInfo table, final ColumnInfo column) {
        TableInfo rebornFrom = null;
        if (table.getRebornFrom() != null) {
            TableInfo reFrom = table.getRebornFrom();
            for (String pk : reFrom.getPrimaryKeys()) {
                if (column.getName().equals(pk)) {
                    rebornFrom = reFrom;
                    break;
                }
            }
        }
        return rebornFrom;
    }

    /**
     * @param column
     * @return String
     */
    private static String isNotBlank(final ColumnInfo column) {
        if (!column.isPk() && column.getTypeName().equals("CHAR")
                && !StringUtil.isNullOrWhiteSpace(CHAR_NOTNULL_RE) && !column.getName().matches(CHAR_NOTNULL_RE)) {
            LOG.trace("skip NotBlank.");
        } else {
            return " notblank";
        }
        return "";
    }

    /**
     * @param isD
     * @param table
     * @param column
     * @return cssClass
     */
    private static String addCssByRelation(final boolean isD, final TableInfo table, final ColumnInfo column) {

        String css = "";

        // 詳細画面のユニークキー
        if (/*isD &&*/ column.isUnique()) {
            css += " uniqueKey";
        }

        // 詳細画面の主キー
        if (/*isD &&*/ column.isPk()) {
            css += " primaryKey";

            // 詳細画面の採番キー
            if (isD && column.isNumbering()) {
                css += " numbering";
            }
        }

        // 詳細画面の参照キー
        if (column.getRefer() != null) {
            css += " refer";

            // 詳細画面の制約キー
            if (isD) {
                TableInfo refer = column.getRefer();
                TableInfo stint = refer.getStintInfo();
                if (stint != null && !table.getName().equals(stint.getName())) {
                    css += " correct";
                }
            }
        }

        return css;
    }

    /**
     * @param colName
     * @return String
     */
    public static String getInputType(final String colName) {

        if (StringUtil.endsWith(INPUT_DATETIME_SUFFIXS, colName)) {
            // 日時項目
            return "datetime-local";

        } else if (StringUtil.endsWith(inputNumberSuffixs, colName)) {
            // 数値項目
            return "number";

        } else if (StringUtil.endsWith(INPUT_YM_SUFFIXS, colName)) {
            // 年月項目
            return "month";

        } else if (StringUtil.endsWith(INPUT_HOUR_SUFFIXS, colName)) {
            // 時刻項目
            return "time";

        } else if (StringUtil.endsWith(FILE_SUFS, colName)) {
            // ファイル
            return "file";
        }

        return "text";
    }

    /**
     * @param table
     * @param s
     */
    private static void htmlFieldsStint(final TableInfo table, final List<String> s) {
        TableInfo stint = table.getStintInfo();
        for (String pk : stint.getPrimaryKeys()) {
            if (!pk.equals(table.getPrimaryKeys().get(0))) {
                ColumnInfo column = stint.getColumns().get(pk);
                if (column.getRefer() != null) {
                    TableInfo refer = column.getRefer();
                    String entity = StringUtil.toPascalCase(refer.getName());
                    String columnName = column.getName();
                    String property = StringUtil.toCamelCase(columnName);
                    String fieldId = entity + "." + property;
                    s.add("        <div id=\"" + property + "\" class=\"stint\">");
                    //                    s.add(htmlFieldsRefer(fieldId, "text", "refer", column, "", table, "refer"));
                    htmlFieldsSpan(s, fieldId, column, "stint");
                    addMeiSpan(s, table, column, "");
                    s.add("        </div>");
                }
            }
        }
    }

    /**
     * @param s
     * @param table
     * @param column
     * @param css
     */
    private static void addMeiSpan(final List<String> s, final TableInfo table, final ColumnInfo column,
            final String css) {

        if (column.getRefer() != null) {
            TableInfo refer = column.getRefer();

            String entity = StringUtil.toPascalCase(table.getName());
            String meiColumnName = getMeiColumnName(column.getName(), refer);

            if (meiColumnName != null && !table.getColumns().containsKey(meiColumnName)) {

                String meiId = entity + "." + StringUtil.toCamelCase(meiColumnName);
                String referDef = getReferDef(entity, column.getName(), refer);

                String cssClass = "";
                if (!StringUtil.isNullOrWhiteSpace(css)) {
                    cssClass = " " + css;
                }
                s.add("          <span id=\"" + meiId + "\" class=\"refer" + cssClass + "\"" + referDef + "></span>");
            }
        }
    }

    /**
     * referタグ出力
     * @param fieldId    入力項目のID
     * @param type       タイプ
     * @param css        スタイル
     * @param column
     * @param format     フォーマット
     * @param table
     * @param referCss
     * @return referタグ
     */
    private static String htmlFieldsRefer(final String fieldId, final String type, final String css,
            final ColumnInfo column, final String format, final TableInfo table, final String referCss) {

        String dataFormat = "";
        if (!StringUtil.isNullOrWhiteSpace(format)) {
            dataFormat = " data-format=\"" + format + "\"";
        }

        String entity = StringUtil.toPascalCase(table.getName());
        String colName = column.getName();
        int max = column.getColumnSize();

        String tag = "          ";
        tag += "<label for=\"" + fieldId + "\" th:text=\"#{" + fieldId + "}\">" + colName.toUpperCase() + "</label>";

        TableInfo refer = column.getRefer();
        String referName = StringUtil.toPascalCase(refer.getName());
        String referDef = getReferDef(entity, colName, refer);
        tag += "<input type=\"" + type + "\" id=\"" + fieldId + "\" name=\"" + fieldId + "\" maxlength=\"" + max
                + "\" class=\"" + css + "\"" + referDef + dataFormat + " />";

        if (referCss.contains("correct")) {
            //選択リンク
            String href = "/model/" + referName + "S.html?action=" + referName + "Correct.ajax";
            tag += "<a id=\"" + fieldId + "\" th:href=\"@{" + href + "}\" target=\"dialog\" class=\"" + referCss
                    + "\" th:text=\"#{common.correct}\" tabindex=\"-1\">...</a>";
        } else {
            //参照リンク
            String href = "/model/" + referName + "S.html";
            tag += "<a id=\"" + fieldId + "\" th:href=\"@{" + href + "}\" target=\"dialog\" class=\"" + referCss
                    + "\" th:text=\"#{common.refer}\" tabindex=\"-1\">...</a>";
        }

        String meiColName = getMeiColumnName(colName, refer);
        if (meiColName != null && !table.getColumns().containsKey(meiColName)) {
            String meiId = entity + "." + StringUtil.toCamelCase(meiColName);
            tag += "<span id=\"" + meiId + "\" class=\"" + referCss + "\"" + referDef + "></span>";
        }

        return tag;
    }

    /**
     * inputタグ出力
     * @param id         入力項目のID
     * @param type       タイプ
     * @param css        スタイル
     * @param column 列情報
     * @param format     フォーマット
     * @return タグ文字列
     */
    protected static String htmlFieldsInput(final String id, final String type, final String css,
            final ColumnInfo column,
            final String format) {

        String dataFormat = "";
        if (!StringUtil.isNullOrWhiteSpace(format)) {
            dataFormat = " data-format=\"" + format + "\"";
        }

        int max = column.getColumnSize();
        if (column.getMaxLength() != null) {
            max = column.getMaxLength();
        }

        String tag = "          ";
        tag += "<label for=\"" + id + "\" th:text=\"#{" + id + "}\">" + column.getName().toUpperCase() + "</label>";
        tag += "<input type=\"" + type + "\" id=\"" + id + "\" name=\"" + id + "\" maxlength=\"" + max + "\" class=\""
                + css + "\"" + dataFormat + " />";

        if (type.equals("file")) {
            tag += "<a id=\"" + id + "\" target=\"blank\"></a>";
            tag += "<input type=\"hidden\" id=\"" + id + "\" name=\"" + id + "\" disabled />";
        }

        return tag;
    }

    /**
     * 範囲指定の入力項目出力
     * @param id         項目ID
     * @param type       タイプ
     * @param css        スタイル
     * @param column 列情報
     * @param format     フォーマット
     * @return タグ文字列
     */
    private static String htmlFieldsRange(final String id, final String type, final String css,
            final ColumnInfo column, final String format) {

        String dataFormat = "";
        if (!StringUtil.isNullOrWhiteSpace(format)) {
            dataFormat = " data-format=\"" + format + "\"";
        }

        int max = column.getColumnSize();
        if (column.getMaxLength() != null) {
            max = column.getMaxLength();
        }

        String required = "";
        if (css.indexOf("notblank") >= 0) {
            required = " required";
        }

        String tag = "          ";
        tag += "<label for=\"" + id + "_1\" th:text=\"#{" + id + "}\">" + column.getName().toUpperCase() + "</label>";
        tag += "<input type=\"" + type + "\" id=\"" + id + "_1\" name=\"" + id + "_1\" maxlength=\"" + max
                + "\" class=\"" + css + "\""
                + dataFormat + required + " />";
        tag += "～";
        tag += "<input type=\"" + type + "\" id=\"" + id + "_2\" name=\"" + id + "_2\" maxlength=\"" + max
                + "\" class=\"" + css + "\""
                + dataFormat + required + " />";

        return tag;
    }

    /**
     * テキストエリア出力
     * @param s 出力文字列のリスト
     * @param id 項目ID
     * @param colName コメント
     * @param css css
     */
    private static void htmlFieldsTextarea(final List<String> s, final String id, final String colName,
            final String css) {

        String cssClass = "";
        if (!StringUtil.isNullOrWhiteSpace(css)) {
            cssClass = " class=\"" + css + "\"";
        }

        s.add("          <label for=\"" + id + "\" th:text=\"#{" + id + "}\">" + colName.toUpperCase() + "</label>");
        s.add("          <textarea id=\"" + id + "\" name=\"" + id + "\"" + cssClass + "></textarea>");
    }

    /**
     * 選択項目の出力
     * @param s 出力文字列のリスト
     * @param id 項目ID
     * @param colName カラム名
     * @param css css
     */
    private static void htmlFieldsOptions(final List<String> s, final String id, final String colName,
            final String css) {

        String cssClass = "";
        if (!StringUtil.isNullOrWhiteSpace(css)) {
            cssClass = " class=\"" + css + "\"";
        }

        String forcePulldown = "";
        if (StringUtil.endsWith(pulldownSuffixs, colName)) {
            forcePulldown = " data-force-pulldown=\"1\"";
        }

        s.add("          <fieldset id=\"" + id + "List\" data-options=\"" + JSON + "\" data-optionParams=\"" + OPT_K
                + ":" + colName + "\" data-optionValue=\"" + OPT_V + "\" data-optionLabel=\"" + OPT_L + "\""
                + cssClass + forcePulldown + ">");
        s.add("            <legend th:text=\"#{" + id + "}\">" + colName + "</legend>");
        s.add("          </fieldset>");
    }

    /**
     * レコードメタ情報の出力
     * @param s 出力文字列のリスト
     * @param id 項目ID
     * @param c カラム情報
     */
    private static void htmlFieldsMeta(final List<String> s, final String id, final ColumnInfo c) {

        String css = "";
        if (StringUtil.endsWith(TS_SUFS, c.getName())) {
            css += " YmdHmsS";
        }

        String tag = "          ";
        tag += "<label th:text=\"#{" + id + "}\" class=\"meta\">" + c.getName() + "</label>";
        tag += "<span id=\"" + id + "\" class=\"meta" + css + "\"></span>";
        tag += "<input type=\"hidden\" id=\"" + id + "\" name=\"" + id + "\" class=\"meta\" />";
        s.add(tag);
    }

    /**
     * 表示項目の出力
     * @param s 出力文字列のリスト
     * @param id 項目ID
     * @param c カラム情報
     * @param cssClass
     */
    private static void htmlFieldsSpan(final List<String> s, final String id, final ColumnInfo c,
            final String cssClass) {

        String css = cssClass;
        if (StringUtil.endsWith(TS_SUFS, c.getName())) {
            css += " YmdHmsS";
        }
        if (!StringUtil.isNullOrWhiteSpace(css)) {
            css = " class=\"" + css + "\"";
        }
        String tag = "          ";
        tag += "<label th:text=\"#{" + id + "}\">" + c.getName() + "</label>";
        tag += "<span id=\"" + id + "\"" + css + "></span>";
        tag += "<input type=\"hidden\" id=\"" + id + "\" name=\"" + id + "\"" + css + " />";
        s.add(tag);
    }

    /**
     * 参照定義の取得
     * @param entityName 参照元エンティティ名
     * @param columnName 参照元カラム名
     * @param referInfo 参照先エンティティ情報
     * @return 参照HTML文字列
     */
    private static String getReferDef(final String entityName, final String columnName, final TableInfo referInfo) {

        //        // カラム名が参照キーに合致する場合
        //        if (StringUtil.endsWith(REFER_PAIRS, columnName)) {

        for (String[] e : REFER_PAIRS) {
            String[] keySufs = e[0].split("&");
            String valSuf = e[1];

            for (String keySuf : keySufs) {

                // カラム名が参照キーに合致しなければスキップ
                if (!columnName.matches("(?i)^.*" + keySuf + "$")) {
                    continue;
                }

                // カラム名のIDサフィックスを名称サフィックスに置換して名称カラム名を取得
                String srcKey = columnName;
                String srcVal = srcKey.replaceAll("(?i)" + keySuf + "$", valSuf);

                // 参照先テーブルの全カラム名を確認して、末尾が合致するカラム名を、参照先のID・名称カラム名として取得
                String destKey = null;
                String destVal = null;
                for (ColumnInfo column : referInfo.getColumns().values()) {
                    String destColumnName = column.getName();
                    if (srcKey.matches("(?i)^.*" + destColumnName + "$")) {
                        destKey = destColumnName;

                    }
                    // 値列の検査
                    if (srcVal.matches("(?i)^.*" + destColumnName + "$")) {
                        destVal = destColumnName;
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

                String srcDefs = "";
                String srcPrefix = srcKey.replaceFirst(destKey + "$", "");
                for (String primaryKey : referInfo.getPrimaryKeys()) {
                    String srcIdName = entityName + "." + StringUtil.toCamelCase(srcPrefix + primaryKey);
                    if (srcDefs.length() > 0) {
                        srcDefs += ",";
                    }
                    srcDefs += primaryKey + ":" + srcIdName;
                }

                String referName = StringUtil.toPascalCase(referInfo.getName());
                String srcMeiName = entityName + "." + StringUtil.toCamelCase(srcVal);

                String referFor = " data-referFor=\"" + entityName + "." + StringUtil.toCamelCase(srcKey) + "\"";
                String dataJson = " data-json=\"" + referName + "Search.json\"";
                String srcDef = " data-srcDef=\"" + srcDefs + "\"";
                String destDef = " data-destDef=\"" + srcMeiName + ":" + destVal + "\"";
                return referFor + dataJson + srcDef + destDef;
            }
        }
        //        }

        return "";
    }

    /**
     * 参照IDカラム名に合致する、参照名称カラム名の取得
     * @param columnName カラム名
     * @param referInfo 参照テーブル情報
     * @return 名称カラム名
     */
    private static String getMeiColumnName(final String columnName, final TableInfo referInfo) {

        for (String[] e : REFER_PAIRS) {
            String[] keySufs = e[0].split("&");
            String valSuf = e[1];

            // カラム名が参照キーに合致する場合
            if (StringUtil.endsWith(keySufs, columnName)) {

                for (String keySuf : keySufs) {

                    // カラム名が参照キーに合致しなければスキップ
                    if (!columnName.matches("(?i)^.*" + keySuf + "$")) {
                        continue;
                    }

                    // カラム名のIDサフィックスを名称サフィックスに置換して名称カラム名を取得
                    String srcIdColumn = columnName;
                    String srcMeiColumn = srcIdColumn.replaceAll("(?i)" + keySuf + "$", valSuf);

                    // 参照先テーブルの全カラム名を確認して、末尾が合致するカラム名を、参照先のID・名称カラム名として取得
                    String destIdColumn = null;
                    String destMeiColumn = null;
                    for (String destColumnName : referInfo.getColumns().keySet()) {
                        if (srcIdColumn.matches("(?i)^.*" + destColumnName + "$")) {
                            destIdColumn = destColumnName;
                        } else if (srcMeiColumn.matches("(?i)^.*" + destColumnName + "$")) {
                            destMeiColumn = destColumnName;
                        }
                    }

                    if (destIdColumn == null || destMeiColumn == null) {
                        continue;
                    }

                    return srcMeiColumn;
                }
            }
        }

        return null;
    }

}
