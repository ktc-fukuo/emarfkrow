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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.golorp.emarf.io.FileUtil;
import jp.co.golorp.emarf.lang.StringUtil;

/**
 * HTMLファイル出力
 *
 * @author golorp
 */
public final class HtmlGeneratorIndex extends HtmlGenerator {

    /** logger */
    private static final Logger LOG = LoggerFactory.getLogger(HtmlGeneratorIndex.class);

    /**
     * コンストラクタ
     */
    private HtmlGeneratorIndex() {
    }

    /**
     * 検索画面 HTML出力
     * @param htmlDir HTMLファイル出力ディレクトリ
     * @param table テーブル情報
     * @param tables テーブル情報
     */
    public static void htmlIndex(final String htmlDir, final TableInfo table, final List<TableInfo> tables) {
        String e = StringUtil.toPascalCase(table.getName());
        List<String> s = new ArrayList<String>();
        addHtmlHead(s, e + "S", table.getName());
        s.add("<script th:src=\"@{/model/" + e + ".js}\"></script>");
        s.add("<script th:src=\"@{/model/" + e + "GridColumns.js}\"></script>");
        Map<TableInfo, Integer> added = new HashMap<TableInfo, Integer>();
        added.put(table, 1);
        htmlNestGrid(s, table, tables, added, false, "", false);
        s.add("</head>\r\n<body>\r\n  <div layout:fragment=\"article\">");
        s.add("    <h2 th:text=\"#{" + e + "S.h2}\">h2</h2>\r\n    <!-- 検索フォーム -->");
        s.add("    <form name=\"" + e + "SearchForm\" action=\"" + e + "Search.ajax\" class=\"search\">");
        if (!table.isGraph()) {
            s.add("      <input type=\"hidden\" name=\"rows\" value=\"" + GRID_ROWS + "\" />");
            s.add("      <input type=\"hidden\" name=\"page\" value=\"0\" />");
        }
        s.add("      <fieldset>\r\n        <legend th:text=\"#{" + e + "S.legend}\">legend</legend>");
        htmlFields(table, s, false, false, false);
        s.add("      </fieldset>\r\n      <div class=\"buttons\">\r\n        <button type=\"button\" id=\"Reset" + e
                + "\" th:text=\"#{common.reset}\" class=\"reset\" onClick=\"Dialogate.reset(event);\">reset</button>");
        boolean isAnew = isAnew(table);
        if (isAnew && table.getRebornFrom() == null) {
            //            boolean isDeriver = false;
            //            for (ColumnInfo col : table.getColumns().values()) {
            //                if (col.getDeriveFrom() != null) {
            //                    isDeriver = true;
            //                    break;
            //                }
            //            }
            String anewClass = "anew";
            //            if (isDeriver) {
            //                anewClass += " derive";
            //            }
            if (table.getPrimaryKeys().size() > 0) {
                s.add("        <a th:href=\"@{/model/" + e + ".html(anew)}\" target=\"dialog\" id=\"" + e
                        + "\" class=\"" + anewClass + "\" th:text=\"#{" + e + ".add}\" tabindex=\"-1\">"
                        + table.getName() + "</a>");
            }
            //            if (table.getName().matches(ELDEST_RE)) {
            //                for (TableInfo bro : table.getBrothers()) {
            //                    String b = StringUtil.toPascalCase(bro.getName());
            //                    s.add("        <a th:href=\"@{/model/" + b + ".html}\" target=\"dialog\" id=\"" + b + "\" class=\""
            //                            + anewClass + "\" style=\"display: none;\" th:text=\"#{" + b + ".add}\" tabindex=\"-1\">"
            //                            + bro.getName() + "</a>");
            //                }
            //            }
            // 派生元の選択は表示しない
            //            // 派生先に該当する場合は派生元の指定項目を配置
            //            if (isDeriver) {
            //                HashSet<String> deriveFroms = new HashSet<String>();
            //                for (ColumnInfo col : table.getColumns().values()) {
            //                    if (col.getDeriveFrom() != null && !deriveFroms.contains(col.getDeriveFrom().getName())) {
            //                        deriveFroms.add(col.getDeriveFrom().getName());
            //                        String field = null;
            //                        for (String pk : col.getDeriveFrom().getPrimaryKeys()) {
            //                            field = e + ".derivee" + StringUtil.toPascalCase(pk);
            //                            s.add(htmlFieldsInput(field, "text", "derivee", col.getDeriveFrom().getColumns().get(pk),
            //                                    null));
            //                        }
            //                        String refer = StringUtil.toPascalCase(col.getDeriveFrom().getName());
            //                        s.add("          <a id=\"" + field + "\" th:href=\"@{/model/" + refer + "S.html?action=" + refer
            //                                + "Correct.ajax}\" target=\"dialog\" class=\"derivee\" th:text=\"#{common.correct}\" tabindex=\"-1\">...</a>");
            //                    }
            //                }
            //            }
        }
        s.add("      </div>\r\n      <div class=\"submits\">");
        s.add(getSearchButton(table, e));
        s.add("      </div>\r\n    </form>\r\n    <!-- 一覧フォーム -->\r\n    <form name=\""
                + e + "SRegistForm\" action=\"" + e + "SRegist.ajax\" class=\"regist\">");
        s.add("      <h3 th:text=\"#{" + e + ".h3}\">h3</h3>");
        if (table.isGraph()) {
            s.add("      <canvas id=\"" + e + "Graph\"></canvas>");
        } else {
            String addRow = "";
            if (isAnew) {
                boolean isNotAddRow = false;
                for (ColumnInfo column : table.getColumns().values()) {
                    if (BeanGenerator.isMetaBy(column.getName())) {
                        continue; // メタ情報ならスキップ
                    }
                    if (StringUtil.endsWith(FILE_SUFS, column.getName())) { //ファイル列があれば新規行なし
                        isNotAddRow = true;
                        break;
                    }
                }
                if (table.getChildren().size() > 0) { //子モデルがあれば新規行なし
                    isNotAddRow = true;
                }
                if (!isNotAddRow) {
                    addRow = " data-addRow=\"true\"";
                }
            }
            int frozenColumn = -1;
            if (!table.isView()) {
                frozenColumn += table.getPrimaryKeys().size();
            }
            String editable = "";
            if (table.isView() || table.isStatusFlow() || table.isHistory() || table.getChildren().size() > 0) {
                editable = "data-editable=\"false\" ";
            }
            s.add("      <div id=\"" + e + "Grid\" data-selectionMode=\"checkbox\"" + addRow + " data-frozenColumn=\""
                    + frozenColumn + "\" " + editable + "th:data-href=\"@{/model/" + e + ".html}\"></div>");
            s.add("      <div id=\"" + e + "Pager\"></div>\r\n      <div class=\"buttons\">");
            if (!table.isHistory() && (!table.isView() || table.isConvView()) && !table.isStatusFlow()) {
                s.add("        <button type=\"button\" class=\"reset\" id=\"Reset" + e
                        + "S\" th:text=\"#{common.reset}\" onClick=\"Base.listReset('" + e + "');\">reset</button>");
            }
            s.add("        <a th:href=\"@{" + e + "Search.xlsx(baseMei=#{" + e + "S.h2})}\" id=\"" + e
                    + "Search.xlsx\" th:text=\"#{common.xlsx}\" class=\"output\" tabindex=\"-1\">xlsx</a>");
            if (table.getSummaryTo() != null) { // 集約先リンク
                String summaryEntity = StringUtil.toPascalCase(table.getSummaryTo().getName());
                s.add("        <a th:href=\"@{/model/" + summaryEntity + ".html}\" id=\"" + summaryEntity
                        + "\" target=\"dialog\" th:text=\"#{" + summaryEntity
                        + ".sum}\" class=\"summary\" tabindex=\"-1\">" + table.getSummaryTo().getName() + "</a>");
            }
            if ((!table.isView() && !table.isStatusFlow() && !table.isHistory()) || table.isConvView()) {
                addGridReferHiddenLinks(s, table); // 履歴モデルでないテーブルか、組み合わせビュー（検索条件で使う）なら、非表示の参照ボタンを出力
            }
            if (!table.isView() && !table.isStatusFlow() && !table.isHistory() && table.getChildren().size() == 0) { // 履歴モデルでないテーブルで、子モデルを持たない場合
                if (!table.getColumns().containsKey(DELETE_F) && !table.getColumns().containsKey(HAISHI_BI)) {
                    if (table.getPrimaryKeys().size() > 0) {
                        s.add("        <button type=\"submit\" id=\"Delete" + e + "S\" data-action=\"" + e
                                + "SDelete.ajax\" class=\"delete selectRows\" th:text=\"#{common.delete}\" tabindex=\"-1\">delete</button>");
                    }
                } // 削除フラグも有効期間終了日もないなら、物理削除ボタンを表示
                addKessaiButtons(table, e, s);
            }
            s.add("      </div>\r\n      <div class=\"submits\">");
            if (!table.isHistory() && (!table.isView() || table.isConvView()) && !table.isStatusFlow()
                    && table.getChildren().size() == 0) {
                String onClick = "";
                if (table.getHistory() != null && !StringUtil.isNullOrWhiteSpace(REASON)) {
                    onClick = " onclick=\"if (!Base.rirekiTx(this)) { return false; }\"";
                }
                if (table.getPrimaryKeys().size() > 0) {
                    s.add("        <button id=\"Regist" + e + "S\" type=\"submit\"" + onClick
                            + " class=\"regist\" th:text=\"#{common.regist}\">regist</button>");
                }
            }
            s.add("      </div>");
        }
        s.add("    </form>\r\n  </div>\r\n</body>\r\n</html>");
        FileUtil.writeFile(htmlDir + File.separator + e + "S.html", s);
    }

    /**
     * @param table
     * @param e
     * @param s
     */
    public static void addKessaiButtons(final TableInfo table, final String e, final List<String> s) {
        //ステータス列名の指定があり、テーブルにステータス列があるなら、承認ボタン・否認ボタンを表示
        if (table.getColumns().containsKey(STATUS)) {
            String onClick = "";
            if (table.getStatusFlow() != null && !StringUtil.isNullOrWhiteSpace(REASON)) {
                onClick = " onclick=\"if (!Base.kessaiTx(this)) { return false; }\"";
            }
            String es = e + "S";
            s.add("        <button type=\"submit\"" + onClick + " id=\"Permit" + es + "\" data-action=\"" + es
                    + "Permit.ajax\" class=\"permit selectRows\" th:text=\"#{common.permit}\" tabindex=\"-1\">permit</button>");
            s.add("        <button type=\"submit\"" + onClick + " id=\"Forbid" + es + "\" data-action=\"" + es
                    + "Forbid.ajax\" class=\"forbid selectRows\" th:text=\"#{common.forbid}\" tabindex=\"-1\">forbid</button>");
            s.add("        <button type=\"submit\"" + onClick + " id=\"Apply" + es + "\" data-action=\"" + es
                    + "Apply.ajax\" class=\"apply selectRows\" th:text=\"#{common.apply}\" tabindex=\"-1\">apply</button>");
            s.add("        <button type=\"submit\"" + onClick + " id=\"Cancel" + es + "\" data-action=\"" + es
                    + "Cancel.ajax\" class=\"cancel selectRows\" th:text=\"#{common.cancel}\" tabindex=\"-1\">cancel</button>");
        }
    }

    /**
     * @param table
     * @param e
     * @return String
     */
    public static String getSearchButton(final TableInfo table, final String e) {
        String searchButton = null;
        if (table.isGraph()) {
            searchButton = "        <button id=\"Search" + e + "\" type=\"submit\" class=\"search\" data-graphId=\"" + e
                    + "Graph\" th:text=\"#{common.search}\">search</button>";
        } else {
            searchButton = "        <button id=\"Search" + e + "\" type=\"submit\" class=\"search\" data-gridId=\"" + e
                    + "Grid\" th:text=\"#{common.search}\">search</button>";
        }
        return searchButton;
    }

    /**
     * 検索画面 グリッド定義出力
     * @param gridDir グリッド出力ディレクトリ
     * @param table テーブル情報
     */
    public static void htmlGridColumns(final String gridDir, final TableInfo table) {

        String entity = StringUtil.toPascalCase(table.getName());

        List<String> s = new ArrayList<String>();
        s.add("/**");
        s.add(" * " + table.getName() + " grid columns");
        s.add(" */");
        s.add("");
        //grid列名が取れない事があるのでonloadまで遅らせる
        s.add("let " + entity + "GridColumns = [];");
        s.add("");
        s.add("$(function() {");
        s.add("    " + entity + "GridColumns = [");

        //主キー列の出力
        if (!table.isView()) {
            for (String pk : table.getPrimaryKeys()) {
                ColumnInfo primaryKey = table.getColumns().get(pk);
                String gridColumn = htmlGridColumn(table, primaryKey, "");
                if (gridColumn != null) {
                    s.add("        " + gridColumn);
                }
            }
        }

        //非主キー列の出力
        for (String columnName : table.getNonPrimaryKeys()) {

            //VIEWなら「SEARCH_」を出力しない
            if (table.isView() && StringUtil.startsWith(VIEW_CRITERIA_PREFIXS, columnName)) {
                continue;
            }

            //カラム名が「TABLE_NAME」なら出力しない
            if (columnName.matches("(?i)^" + VIEW_DETAIL + "$")) {
                continue;
            }

            ColumnInfo column = table.getColumns().get(columnName);
            String gridColumn = htmlGridColumn(table, column, "");
            if (gridColumn != null) {
                s.add("        " + gridColumn);
            }
        }

        if (table.getName().matches(ELDEST_RE)) {
            for (TableInfo bro : table.getBrothers()) {
                for (String colName : bro.getNonPrimaryKeys()) {
                    if (colName.matches("(?i)^" + UPDATE_TS + "$")) {
                        continue;
                    }
                    ColumnInfo column = bro.getColumns().get(colName);
                    String gridColumn = htmlGridColumn(bro, column, bro.getName() + ".");
                    if (gridColumn != null) {
                        s.add("        " + gridColumn);
                    }
                }
            }
        }

        s.add("    ];");
        s.add("});");

        FileUtil.writeFile(gridDir + File.separator + entity + "GridColumns.js", s);

        s = new ArrayList<String>();
        s.add("/**");
        s.add(" * " + table.getName() + " script");
        s.add(" */");
        FileUtil.writeFile(gridDir + File.separator + entity + ".js", s);
    }

    /**
     * @param table
     * @param column
     * @param prefix フィールド名のプレフィクス（兄弟モデルのカラムを出力する際に、フィールド名が重複するのを避けるために使用）
     * @return 列定義文字列
     */
    private static String htmlGridColumn(final TableInfo table, final ColumnInfo column, final String prefix) {
        String e = StringUtil.toPascalCase(table.getName());
        String n = column.getName();
        String m = "Messages['" + e + "Grid." + StringUtil.toCamelCase(n) + "']";
        boolean isUpdTs = n.matches("(?i)^" + UPDATE_TS + "$");
        if (!isUpdTs && BeanGenerator.isMetaTsBy(n)) {
            return null;
        }

        String css = getCss(table, column, n, isUpdTs);

        String format = "null";
        if (column.getDataType().equals("java.time.LocalDate")) {
            format = "Slick.Formatters.Extends.Date";
        } else if (StringUtil.endsWith(TS_SUFS, n)) {
            format = "Slick.Formatters.Extends.Timestamp";
        } else if (column.getDataType().equals("java.time.LocalDateTime")) {
            format = "Slick.Formatters.Extends.DateTime";
        }
        boolean isMeiRefer = false; // 名称を参照先から取得するか
        String rMei = ""; // 参照名の列名
        TableInfo refer = column.getRefer();
        if (!table.isView() && refer != null) { // 参照テーブルが設定されている場合
            isMeiRefer = true;
            if (StringUtil.endsWith(REFER_PAIRS, n)) { // カラム名が組み合わせキーのいずれかに合致する場合
                for (String[] suffix : REFER_PAIRS) { // 参照設定の組み合わせでループ
                    String keySuffix = suffix[0];
                    String meiSuffix = suffix[1];
                    if (n.matches("(?i).*" + keySuffix + "$")) { // カラム名がキー接尾辞に合致する場合
                        String tempMei = n.replaceAll("(?i)" + keySuffix + "$", meiSuffix); // カラム名の末尾を名称列サフィックスに変換
                        if (table.getColumns().containsKey(tempMei)) { // 名称列がテーブルに含まれている場合は参照先から名称を取得しない
                            rMei = tempMei;
                            isMeiRefer = false;
                            break;
                        }
                    }
                }
            }
            if (isMeiRefer) { // 名称を参照先から取得する場合
                for (String[] suffix : REFER_PAIRS) { // 参照設定の組み合わせで、キー接尾辞に合致するカラム名を探索
                    String[] keySuffixs = suffix[0].split("&");
                    String meiSuffix = suffix[1];
                    if (StringUtil.endsWith(keySuffixs, n)) { // カラム名の末尾を名称列サフィックスに変換して、名称列が参照先テーブルに含まれている場合は、取得する名称を決定する
                        String lastSuffix = keySuffixs[keySuffixs.length - 1];
                        String tempMei = n.replaceAll("(?i)" + lastSuffix + "$", meiSuffix);
                        if (n.equals(tempMei)) {
                            continue;
                        }
                        for (ColumnInfo referColumnInfo : refer.getColumns().values()) {
                            if (tempMei.matches("(?i).*" + referColumnInfo.getName() + "$")) {
                                rMei = tempMei.toUpperCase();
                                break;
                            }
                        }
                        if (!rMei.isBlank()) {
                            break;
                        }
                    }
                }
            }
        }
        int w = column.getColumnSize() * COLUMN_WIDTH_PX_MULTIPLIER;
        if (column.getMaxLength() != null) {
            w = column.getMaxLength() * COLUMN_WIDTH_PX_MULTIPLIER;
        }
        if (w < 30) {
            w = 30;
        } else if (w > 300) {
            w = 300;
        }
        String type = column.getTypeName();
        String cId = column.getName().toUpperCase();
        // 派生元
        for (TableInfo from : table.getDeriveFroms()) {
            if (from.getPrimaryKeys().contains(column.getName())) {
                return "Column.refer('" + prefix + cId + "', " + m + ", " + w + ", '" + css + "', '" + rMei + "'),";
            }
        }
        // 共生元
        for (TableInfo from : table.getMergeFroms()) {
            if (from.getPrimaryKeys().contains(column.getName())) {
                return "Column.refer('" + prefix + cId + "', " + m + ", " + w + ", '" + css + "', '" + rMei + "'),";
            }
        }
        // 集約先
        if (table.getSummaryTo() != null) {
            TableInfo from = table.getSummaryTo();
            if (from.getPrimaryKeys().contains(column.getName())) {
                return "Column.refer('" + prefix + cId + "', " + m + ", " + w + ", '" + css + "', '" + rMei + "'),";
            }
        }
        if (isMeiRefer) {
            if (!type.matches(NUM_RE) || StringUtil.endsWith(INT_NOFORMAT_SUFFIXS, n)) {
                return "Column.refer('" + prefix + cId + "', " + m + ", " + w + ", '" + css + "', '" + rMei + "'),";
            } else {
                return getNumericRefer(column, prefix + cId, m, w, css, rMei);
            }
        } else if (BeanGenerator.isMetaTsBy(n) || column.isReborn()) {
            return "Column.cell('" + prefix + cId + "', " + m + ", " + w + ", '" + css + "', " + format + "),";
        } else if (StringUtil.endsWith(INPUT_FLAG_SUFFIXS, n)) {
            return "Column.check('" + prefix + cId + "', " + m + ", " + w + ", '" + css + "'),";
        } else if (StringUtil.endsWith(INPUT_BIT_SUFFIXS, n)) {
            return "Column.bit('" + prefix + cId + "', " + m + ", " + w + ", '" + css + "'),";
        } else if (StringUtil.endsWith(INPUT_DATE_SUFFIXS, n)) {
            return "Column.date('" + prefix + cId + "', " + m + ", " + w + ", '" + css + "', " + format + "),";
        } else if (StringUtil.endsWith(INPUT_DATE8_SUFFIXS, n) && column.getColumnSize() == 8) {
            return "Column.date8('" + prefix + cId + "', " + m + ", " + w + ", '" + css + "', " + format + "),";
        } else if (StringUtil.endsWith(TS_SUFS, n)) {
            return "Column.cell('" + prefix + cId + "', " + m + ", " + w + ", '" + css + "', " + format + "),";
        } else if (StringUtil.endsWith(INPUT_DATETIME_SUFFIXS, n)) {
            return "Column.dateTime('" + prefix + cId + "', " + m + ", " + w + ", '" + css + "'),";
        } else if (StringUtil.endsWith(INPUT_YM_SUFFIXS, n)) {
            return "Column.month('" + prefix + cId + "', " + m + ", " + w + ", '" + css
                    + "', Slick.Formatters.Extends.Month),";
        } else if (StringUtil.endsWith(INPUT_HOUR_SUFFIXS, n)) {
            return "Column.hour('" + prefix + cId + "', " + m + ", " + w + ", '" + css + "', " + format + "),";
        } else if (StringUtil.endsWith(INPUT_TIME_SUFFIXS, n)) {
            return "Column.time('" + prefix + cId + "', " + m + ", " + w + ", '" + css + "', " + format + "),";
        } else if (StringUtil.endsWith(FILE_SUFS, n)) {
            return "Column.link('" + prefix + cId + "', " + m + ", " + w + ", '" + css + "'),";
        } else if (StringUtil.endsWith(OPTIONS_SUFFIXS, n)) {
            return "Column.select('" + prefix + cId + "', " + m + ", " + w + ", '" + css + "', { json: '" + JSON
                    + "', paramkey: '" + OPT_K + "', value: '" + OPT_V + "', label: '" + OPT_L + "' }),";
        } else if (StringUtil.endsWith(TEXTAREA_SUFFIXS, n)) {
            return "Column.longText('" + prefix + cId + "', " + m + ", " + w + ", '" + css + "', " + format + "),";
        } else if (type.matches(NUM_RE) && !StringUtil.endsWith(INT_NOFORMAT_SUFFIXS, n)) {
            return getNumericColumn(column, prefix + cId, m, w, css, format);
        } else {
            return "Column.text('" + prefix + cId + "', " + m + ", " + w + ", '" + css + "', " + format + "),";
        }
    }

    /**
     * @param table
     * @param column
     * @param n
     * @param isUpdTs
     * @return String
     */
    public static String getCss(final TableInfo table, final ColumnInfo column, final String n, final boolean isUpdTs) {

        String cs = "";

        if (!table.isView()) {
            if (column.isPk()) {
                cs = "primaryKey";
                boolean isParentKey = false;
                if (table.getParents() != null) {
                    for (TableInfo parent : table.getParents()) {
                        List<String> primaryKeys = new ArrayList<String>(parent.getPrimaryKeys());
                        primaryKeys.remove(TEKIYO_BI);
                        if (primaryKeys.contains(n.toUpperCase())) {
                            isParentKey = true;
                            break;
                        }
                    }
                }
                if ((column.isNumbering() && column.getRefer() == null) || isParentKey) {
                    cs += " numbering"; //採番キー かつ 参照キーでない か 親モデルのキー
                }
            } else if (column.isUnique()) {
                cs = "uniqueKey";
            } else if (BeanGenerator.isMetaTsBy(n)) {
                cs = "metaInfo";
                if (isUpdTs) {
                    cs += " optLock";
                }
            } else if (column.getNullable() == 0) {
                if (!column.isPk() && column.getTypeName().equals("CHAR")
                        && !StringUtil.isNullOrWhiteSpace(CHAR_NOTNULL_RE) && !n.matches(CHAR_NOTNULL_RE)) {
                    LOG.trace("skip NotBlank.");
                } else {
                    cs = "notblank";
                }
            }
            if (StringUtil.endsWith(INPUT_READONLY_SUFFIXS, n)) {
                cs += " readonly";
            }
        }

        return cs;
    }

    /**
     * @param column
     * @param name
     * @param mei
     * @param width
     * @param css
     * @param referMei
     * @return String
     */
    private static String getNumericRefer(final ColumnInfo column, final String name, final String mei,
            final int width, final String css, final String referMei) {

        if (column.getDecimalDigits() == 3) {

            return "Column.dec3Refer('" + name + "', " + mei + ", " + width + ", '" + css + "', " + referMei + "),";

        } else if (column.getDecimalDigits() == 2) {

            return "Column.dec2Refer('" + name + "', " + mei + ", " + width + ", '" + css + "', " + referMei + "),";

        } else if (column.getDecimalDigits() == 1) {

            return "Column.dec1Refer('" + name + "', " + mei + ", " + width + ", '" + css + "', " + referMei + "),";
        }

        return "Column.dec0Refer('" + name + "', " + mei + ", " + width + ", '" + css + "', '" + referMei + "'),";
    }

    /**
     * @param column
     * @param name
     * @param mei
     * @param width
     * @param css
     * @param formatter
     * @return String
     */
    private static String getNumericColumn(final ColumnInfo column, final String name, final String mei,
            final int width, final String css, final String formatter) {

        if (column.getDecimalDigits() == 3) {

            return "Column.dec3('" + name + "', " + mei + ", " + width + ", '" + css + "', " + formatter + "),";

        } else if (column.getDecimalDigits() == 2) {

            return "Column.dec2('" + name + "', " + mei + ", " + width + ", '" + css + "', " + formatter + "),";

        } else if (column.getDecimalDigits() == 1) {

            return "Column.dec1('" + name + "', " + mei + ", " + width + ", '" + css + "', " + formatter + "),";
        }

        return "Column.dec0('" + name + "', " + mei + ", " + width + ", '" + css + "', " + formatter + "),";
    }

}
