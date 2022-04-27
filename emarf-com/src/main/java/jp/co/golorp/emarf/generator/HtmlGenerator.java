package jp.co.golorp.emarf.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import jp.co.golorp.emarf.io.FileUtil;
import jp.co.golorp.emarf.lang.StringUtil;
import jp.co.golorp.emarf.util.ResourceBundles;

public final class HtmlGenerator {

    /** properties */
    private static ResourceBundle bundle;

    /** グリッド列幅ピクセル乗数 */
    private static final int COLUMN_WIDTH_PX_MULTIPLIER = 10;

    /** 登録日時カラム名 */
    private static String insertDt;
    /** 登録者カラム名 */
    private static String insertBy;
    /** 更新日時カラム名 */
    private static String updateDt;
    /** 更新者カラム名 */
    private static String updateBy;

    /** options項目サフィックス */
    private static String[] optionsSuffixs;
    /** データjson */
    private static String json;
    /** options項目キー */
    private static String optionParamKey;
    /** options項目値 */
    private static String optionValue;
    /** options項目ラベル */
    private static String optionLabel;

    /** テキストエリア項目サフィックス */
    private static String[] textareaSuffixs;

    /** 日付入力サフィックス */
    private static String[] inputDateSuffixs;
    /** 日時入力サフィックス */
    private static String[] inputDateTimeSuffixs;
    /** 年月入力サフィックス */
    private static String[] inputMonthSuffixs;
    /** 時刻入力サフィックス */
    private static String[] inputTimeSuffixs;
    /** 範囲指定サフィックス */
    private static String[] inputRangeSuffixs;
    /** フラグサフィックス */
    private static String[] inputFlagSuffixs;

    /** 参照IDサフィックス */
    private static String[] referIdSuffixs;
    /** 参照名サフィックス */
    private static String referMeiSuffix;

    private HtmlGenerator() {
    }

    /**
     * @param projectDir
     * @param tableInfos
     */
    static void generate(final String projectDir, final List<TableInfo> tableInfos) {

        bundle = ResourceBundles.getBundle(BeanGenerator.class);

        insertDt = bundle.getString("BeanGenerator.insert_dt");
        insertBy = bundle.getString("BeanGenerator.insert_by");
        updateDt = bundle.getString("BeanGenerator.update_dt");
        updateBy = bundle.getString("BeanGenerator.update_by");

        optionsSuffixs = bundle.getString("BeanGenerator.options.suffixs").split(",");
        json = bundle.getString("BeanGenerator.options.json");
        optionParamKey = bundle.getString("BeanGenerator.options.paramkey");
        optionValue = bundle.getString("BeanGenerator.options.value");
        optionLabel = bundle.getString("BeanGenerator.options.label");

        textareaSuffixs = bundle.getString("BeanGenerator.textarea.suffixs").split(",");

        inputDateSuffixs = bundle.getString("BeanGenerator.input.date.suffixs").split(",");
        inputDateTimeSuffixs = bundle.getString("BeanGenerator.input.datetime.suffixs").split(",");
        inputMonthSuffixs = bundle.getString("BeanGenerator.input.month.suffixs").split(",");
        inputTimeSuffixs = bundle.getString("BeanGenerator.input.time.suffixs").split(",");
        inputRangeSuffixs = bundle.getString("BeanGenerator.input.range.suffixs").split(",");
        inputFlagSuffixs = bundle.getString("BeanGenerator.input.flag.suffixs").split(",");

        referIdSuffixs = bundle.getString("BeanGenerator.refer.id.suffixs").split(",");
        referMeiSuffix = bundle.getString("BeanGenerator.refer.mei.suffix");

        // 出力フォルダを再作成
        String htmlDir = projectDir + File.separator + bundle.getString("BeanGenerator.htmlPath");
        FileUtil.reMkDir(htmlDir);

        String gridDir = projectDir + File.separator + bundle.getString("BeanGenerator.gridPath");
        FileUtil.reMkDir(gridDir);

        for (TableInfo tableInfo : tableInfos) {

            // 検索画面のHTMLを出力
            HtmlGenerator.htmlIndex(htmlDir, tableInfo);

            // 検索画面のGridColumnを出力
            HtmlGenerator.htmlGridColumns(gridDir, tableInfo);

            // 単画面を出力
            HtmlGenerator.htmlDetail(htmlDir, tableInfo);

            // thymeleafのプロパティファイルを出力
            HtmlGenerator.htmlProperties(htmlDir, tableInfo);
        }
    }

    /**
     * 検索画面 HTML出力
     * @param htmlDir
     * @param tableInfo
     */
    private static void htmlIndex(final String htmlDir, final TableInfo tableInfo) {

        String tableName = tableInfo.getTableName();
        String remarks = tableInfo.getRemarks();
        String pascal = StringUtil.toPascalCase(tableName);
        String pageName = pascal + "S";

        List<String> s = new ArrayList<String>();
        s.add("<!DOCTYPE html>");
        s.add("<html xmlns:th=\"http://www.thymeleaf.org\" xmlns:layout=\"http://www.ultraq.net.nz/web/thymeleaf/layout\" layout:decorate=\"~{common/base}\">");
        s.add("<head>");
        s.add("<meta charset=\"UTF-8\">");
        s.add("<title th:text=\"#{" + pageName + ".title}\">" + remarks + "</title>");
        s.add("<style type=\"text/css\">");
        s.add("</style>");
        s.add("<script type=\"text/javascript\">");
        s.add("");
        s.add("</script>");
        s.add("</head>");
        s.add("<body>");
        s.add("  <div layout:fragment=\"article\">");
        s.add("    <h2 th:text=\"#{" + pageName + ".h2}\"></h2>");
        /* 検索フォーム */
        s.add("    <form name=\"" + pascal + "SearchForm\" action=\"" + pascal + "Search.ajax\" class=\"search\">");
        s.add("      <fieldset>");
        s.add("        <legend th:text=\"#{" + pageName + ".legend}\">legend</legend>");
        htmlFields(tableInfo, s, false, false);
        s.add("      </fieldset>");
        s.add("      <div class=\"buttons\">");
        s.add("        <button type=\"reset\" th:text=\"#{common.reset}\">reset</button>");
        // 採番キーが２つ以上あれば新規ボタンは出力しない
        int numberingCount = 0;
        for (String primaryKey : tableInfo.getPrimaryKeys()) {
            ColumnInfo primaryKeyInfo = tableInfo.getColumnInfos().get(primaryKey);
            if (primaryKeyInfo.isNumbering() && primaryKeyInfo.getReferInfo() == null) {
                ++numberingCount;
            }
        }
        if (numberingCount < 2) {
            s.add("        <a th:href=\"@{/model/" + pascal + ".html}\" id=\"" + pascal
                    + "\" target=\"dialog\" th:text=\"#{" + pascal + ".add}\" tabindex=\"-1\">" + remarks + "</a>");
        }
        s.add("      </div>");
        s.add("      <div class=\"submits\">");
        s.add("        <button id=\"Search" + pascal + "\" class=\"search\" data-gridId=\"" + pascal
                + "Grid\" th:text=\"#{common.search}\">submit</button>");
        s.add("      </div>");
        s.add("    </form>");
        /* 一覧フォーム */
        s.add("    <form name=\"" + pascal + "SRegistForm\" action=\"" + pascal + "SRegist.ajax\" class=\"regist\">");
        s.add("      <h3 th:text=\"#{" + pascal + ".h3}\">h3</h3>");
        String gridId = pascal + "Grid";
        String addRow = "";
        if (tableInfo.getPrimaryKeys().size() == 1) {
            String uniqueKey = tableInfo.getPrimaryKeys().get(0);
            ColumnInfo uniqueKeyInfo = tableInfo.getColumnInfos().get(uniqueKey);
            if (uniqueKeyInfo.isNumbering()) {
                addRow = " data-addRow=\"true\"";
            }
        }
        int frozenColumn = tableInfo.getPrimaryKeys().size() - 1;
        s.add("      <div id=\"" + gridId + "\" data-selectionMode=\"checkbox\"" + addRow + " data-frozenColumn=\""
                + frozenColumn + "\" th:data-href=\"@{/model/" + pascal + ".html}\"></div>");
        s.add("      <div class=\"buttons\">");
        s.add("        <button type=\"button\" th:text=\"#{common.reset}\" onClick=\"$('[id=&quot;Search" + pascal
                + "&quot;]').click();\">reset</button>");
        s.add("        <a th:href=\"@{" + pascal + "Search.xlsx(baseMei=#{" + pascal + "S.h2})}\" id=\"" + pascal
                + "Search.xlsx\" th:text=\"#{common.xlsx}\" tabindex=\"-1\">xlsx</a>");
        s.add("      </div>");
        s.add("      <div class=\"submits\">");
        s.add("        <button id=\"Regist" + pascal
                + "S\" class=\"regist\" th:text=\"#{common.regist}\">submit</button>");
        s.add("      </div>");
        s.add("    </form>");
        s.add("  </div>");
        s.add("</body>");
        s.add("</html>");

        FileUtil.writeFile(htmlDir + File.separator + pageName + ".html", s);
    }

    /**
     * 検索画面 グリッド定義出力
     * @param gridDir
     * @param tableInfo
     */
    private static void htmlGridColumns(final String gridDir, final TableInfo tableInfo) {

        String tableName = tableInfo.getTableName();
        String entityName = StringUtil.toPascalCase(tableName);

        List<String> s = new ArrayList<String>();
        s.add("/**");
        s.add(" * " + tableInfo.getRemarks() + "グリッド定義");
        s.add(" */");
        s.add("");
        s.add("let " + entityName + "GridColumns = [");

        for (ColumnInfo columnInfo : tableInfo.getColumnInfos().values()) {
            String columnName = columnInfo.getColumnName();
            String lower = columnName.toLowerCase();
            String camel = StringUtil.toCamelCase(lower);
            String name = "Messages['" + entityName + "Grid." + camel + "']";
            String field = lower.toUpperCase();
            int width = columnInfo.getColumnSize() * COLUMN_WIDTH_PX_MULTIPLIER;
            if (width > 300) {
                width = 300;
            }

            String css = "";
            if (columnInfo.isPk()) {
                css = "primaryKey";
            } else if (lower.equals(insertDt) || lower.equals(updateDt)) {
                css = "metaInfo";
            } else if (lower.equals(insertBy) || lower.equals(updateBy)) {
                css = "metaInfo";
            }

            String formatter = "null";
            if (columnInfo.getDataType().equals("java.time.LocalDateTime")) {
                formatter = "Slick.Formatters.Extends.DateTime";
            }

            boolean isRefer = false;
            String referMei = null;
            TableInfo referInfo = columnInfo.getReferInfo();
            if (referInfo != null) {
                if (StringUtil.endsWith(referIdSuffixs, columnName)) {
                    referMei = columnName;
                    for (String suffix : referIdSuffixs) {
                        if (referMei.matches("(?i).+" + suffix + "$")) {
                            referMei = referMei.replaceAll("(?i)" + suffix + "$", referMeiSuffix);
                        }
                    }
                    referMei = StringUtil.toUpperCase(referMei);
                    if (!tableInfo.getColumnInfos().containsKey(referMei)) {
                        isRefer = true;
                    }
                }
            }

            String c = "";
            if (isRefer) {
                c = "Column.refer('" + field + "', " + name + ", " + width + ", '" + css + "', '" + referMei + "'),";
            } else if (columnInfo.isPk()) {
                c = "Column.cell('" + field + "', " + name + ", " + width + ", '" + css + "', " + formatter + "),";
            } else if (lower.equals(insertDt) || lower.equals(updateDt)) {
                c = "Column.cell('" + field + "', " + name + ", " + width + ", '" + css + "', " + formatter + "),";
            } else if (lower.equals(insertBy) || lower.equals(updateBy)) {
                c = "Column.cell('" + field + "', " + name + ", " + width + ", '" + css + "', " + formatter + "),";
            } else if (StringUtil.endsWith(inputFlagSuffixs, lower)) {
                c = "Column.check('" + field + "', " + name + ", " + width + ", '" + css + "'),";
            } else if (StringUtil.endsWith(inputDateSuffixs, lower)) {
                c = "Column.date('" + field + "', " + name + ", " + width + ", '" + css + "', " + formatter + "),";
            } else if (StringUtil.endsWith(inputDateTimeSuffixs, lower)) {
                c = "Column.dateTime('" + field + "', " + name + ", " + width + ", '" + css + "'),";
            } else if (StringUtil.endsWith(inputMonthSuffixs, lower)) {
                c = "Column.month('" + field + "', " + name + ", " + width + ", '" + css + "', " + formatter + "),";
            } else if (StringUtil.endsWith(inputTimeSuffixs, lower)) {
                c = "Column.time('" + field + "', " + name + ", " + width + ", '" + css + "', " + formatter + "),";
            } else if (StringUtil.endsWith(optionsSuffixs, lower)) {
                String options = "{ json: '" + json + "', paramkey: '" + optionParamKey + "', value: '" + optionValue
                        + "', label: '" + optionLabel + "' }";
                c = "Column.select('" + field + "', " + name + ", " + width + ", '" + css + "', " + options + "),";
            } else if (StringUtil.endsWith(textareaSuffixs, lower)) {
                c = "Column.longText('" + field + "', " + name + ", " + width + ", '" + css + "', " + formatter + "),";
            } else {
                c = "Column.text('" + field + "', " + name + ", " + width + ", '" + css + "', " + formatter + "),";
            }
            s.add("    " + c);
        }
        s.add("];");

        FileUtil.writeFile(gridDir + File.separator + entityName + "GridColumns.js", s);
    }

    /**
     * 詳細画面 HTML出力
     * @param htmlDir
     * @param tableInfo
     */
    private static void htmlDetail(final String htmlDir, final TableInfo tableInfo) {

        String tableName = tableInfo.getTableName();
        String entityName = StringUtil.toPascalCase(tableName);
        String formName = entityName + "RegistForm";
        String action = entityName + "Regist.ajax";

        List<String> s = new ArrayList<String>();
        s.add("<!DOCTYPE html>");
        s.add("<html xmlns:th=\"http://www.thymeleaf.org\" xmlns:layout=\"http://www.ultraq.net.nz/web/thymeleaf/layout\" layout:decorate=\"~{common/base}\">");
        s.add("<head>");
        s.add("<meta charset=\"UTF-8\">");
        s.add("<title th:text=\"#{" + entityName + ".title}\"></title>");
        s.add("<style type=\"text/css\">");
        s.add("</style>");
        s.add("<script type=\"text/javascript\">");
        s.add("");
        s.add("</script>");
        s.add("</head>");
        s.add("<body>");
        s.add("  <div layout:fragment=\"article\">");
        s.add("    <h2 th:text=\"#{" + entityName + ".h2}\"></h2>");
        s.add("    <form name=\"" + formName + "\" action=\"" + action + "\" class=\"regist\">");
        s.add("      <fieldset>");
        s.add("        <legend th:text=\"#{" + entityName + ".legend}\">legend</legend>");
        htmlFields(tableInfo, s, true, false);
        s.add("      </fieldset>");
        List<TableInfo> brosInfos = tableInfo.getBrosInfos();
        if (brosInfos != null) {
            for (TableInfo brosInfo : brosInfos) {
                String brosName = StringUtil.toPascalCase(brosInfo.getTableName());
                s.add("      <fieldset>");
                s.add("        <legend th:text=\"#{" + brosName + ".legend}\">legend</legend>");
                htmlFields(brosInfo, s, true, true);
                s.add("      </fieldset>");
            }
        }
        //子テーブルリスト
        for (TableInfo childInfo : tableInfo.getChildInfos()) {
            String childName = childInfo.getTableName();
            String pascal = StringUtil.toPascalCase(childName);
            String frozen = String.valueOf(tableInfo.getPrimaryKeys().size());
            s.add("      <h3 th:text=\"#{" + pascal + ".h3}\">h3</h3>");
            s.add("      <a th:href=\"@{/model/" + pascal + ".html}\" id=\"" + pascal
                    + "\" target=\"dialog\" th:text=\"#{" + pascal + ".add}\" class=\"addChild\" tabindex=\"-1\">"
                    + childInfo.getRemarks() + "</a>");
            s.add("      <div id=\"" + pascal
                    + "Grid\" data-selectionMode=\"link\" data-addRow=\"true\" data-frozenColumn=\"" + frozen
                    + "\" th:data-href=\"@{/model/" + pascal + ".html}\"></div>");
        }
        s.add("      <div class=\"buttons\">");
        s.add("        <button type=\"button\" th:text=\"#{common.reset}\" onClick=\"Dialogate.refresh(event);\">reset</button>");
        s.add("        <a th:href=\"@{" + entityName + "Get.xlsx(baseMei=#{" + entityName + ".h2})}\" id=\""
                + entityName + "Get.xlsx\" th:text=\"#{common.xlsx}\" tabindex=\"-1\">xlsx</a>");
        s.add("        <button id=\"Delete" + entityName + "\" class=\"delete\" data-action=\"" + entityName
                + "Delete.ajax\" th:text=\"#{common.delete}\" tabindex=\"-1\">削除</button>");
        s.add("      </div>");
        s.add("      <div class=\"submits\">");
        s.add("        <button id=\"Regist" + entityName
                + "\" class=\"regist\" th:text=\"#{common.regist}\">登録</button>");
        s.add("      </div>");
        s.add("    </form>");
        s.add("  </div>");
        s.add("</body>");
        s.add("</html>");

        FileUtil.writeFile(htmlDir + File.separator + entityName + ".html", s);
    }

    /**
     * htmlにフィールド追加
     *
     * @param tableInfo
     * @param s
     * @param isDetail
     * @param isBrother
     */
    private static void htmlFields(final TableInfo tableInfo, final List<String> s, final boolean isDetail,
            final boolean isBrother) {

        // テーブル物理名
        String tableName = tableInfo.getTableName();

        //エンティティ名
        String entityName = StringUtil.toPascalCase(tableName);

        // カラム情報でループ
        for (ColumnInfo columnInfo : tableInfo.getColumnInfos().values()) {

            //カラム物理名
            String columnName = columnInfo.getColumnName();
            String lower = columnName.toLowerCase();
            String camel = StringUtil.toCamelCase(columnName);
            String id = entityName + "." + camel;
            String remarks = columnInfo.getRemarks();

            if (isBrother && columnInfo.isPk()) {
                // 兄弟モデルの主キーは出力しない
                continue;
            } else if (lower.equals(insertDt) || lower.equals(insertBy)
                    || lower.equals(updateDt) || lower.equals(updateBy)) {
                // メタ情報の場合
                if (!isDetail) {
                    // 検索画面の場合はスキップ（検索条件にはしない）
                    continue;
                } else if (isBrother) {
                    if (lower.equals(updateDt)) {
                        // 詳細画面の兄弟モデルの場合は、更新日時のみhiddenで出力
                        s.add("        <input type=\"hidden\" name=\"" + id + "\" />");
                    }
                    continue;
                }
            }

            boolean isOptions = StringUtil.endsWith(optionsSuffixs, lower);
            boolean isTextarea = StringUtil.endsWith(textareaSuffixs, lower);
            boolean isInputDate = StringUtil.endsWith(inputDateSuffixs, lower);
            boolean isInputDateTime = StringUtil.endsWith(inputDateTimeSuffixs, lower);
            boolean isInputMonth = StringUtil.endsWith(inputMonthSuffixs, lower);
            boolean isInputTime = StringUtil.endsWith(inputTimeSuffixs, lower);
            boolean isInputRange = StringUtil.endsWith(inputRangeSuffixs, lower);

            s.add("        <div id=\"" + camel + "\">");
            if (lower.equals(insertDt) || lower.equals(insertBy)
                    || lower.equals(updateDt) || lower.equals(updateBy)) {
                // メタ情報の場合は表示項目（編集画面の自モデルのみここに到達する）
                String tag = "          ";
                tag += "<label for=\"" + id + "\" th:text=\"#{" + id + "}\">" + remarks + "</label>";
                tag += "<span id=\"" + id + "\"></span>";
                tag += "<input type=\"hidden\" id=\"" + id + "\" name=\"" + id + "\" />";
                s.add(tag);
            } else if (isDetail && columnInfo.isNumbering()) {

                // 編集画面の採番キーは表示項目
                String tag = "          ";
                tag += "<label for=\"" + id + "\" th:text=\"#{" + id + "}\">" + remarks + "</label>";
                tag += "<span id=\"" + id + "\"></span>";
                tag += "<input type=\"hidden\" id=\"" + id + "\" name=\"" + id + "\" class=\"primaryKey\" />";

                if (columnInfo.getReferInfo() != null) {

                    TableInfo referInfo = columnInfo.getReferInfo();
                    String referName = StringUtil.toPascalCase(referInfo.getTableName());

                    // 参照モデルの場合は参照リンクを出力しておき、照会画面ではjsで非表示にする
                    tag += "<a id=\"" + id + "\" th:href=\"@{/model/" + referName
                            + "S.html}\" target=\"dialog\" class=\"primaryKey refer\" th:text=\"#{common.refer}\" tabindex=\"-1\">...</a>";

                    String meiColumnName = getMeiColumnName(columnName);
                    if (!tableInfo.getColumnInfos().containsKey(meiColumnName)) {
                        String meiId = entityName + "." + StringUtil.toCamelCase(meiColumnName);
                        String referDef = getReferDef(entityName, columnName, referInfo);
                        tag += "<span id=\"" + meiId + "\" class=\"refer\"" + referDef + "></span>";
                    }
                }
                s.add(tag);
            } else if (isOptions) {
                // 選択項目の場合
                s.add("          <fieldset id=\"" + id + "List\" data-options=\"" + json
                        + "\" data-optionParams=\""
                        + optionParamKey + ":" + lower + "\" data-optionValue=\""
                        + optionValue
                        + "\" data-optionLabel=\"" + optionLabel + "\">");
                s.add("            <legend th:text=\"#{" + id + "}\">" + remarks + "</legend>");
                s.add("          </fieldset>");
            } else if (isDetail && isTextarea) {
                // テキストエリア項目の場合
                s.add("          <label for=\"" + id + "\" th:text=\"#{" + id + "}\">" + remarks + "</label>");
                s.add("          <textarea id=\"" + id + "\" name=\"" + id + "\"></textarea>");
            } else {
                // 上記以外の場合

                String type = "text";
                if (isInputDate) { // 日付項目
                    type = "date";
                } else if (isInputDateTime) { // 日時項目
                    type = "datetime-local";
                } else if (isInputMonth) { // 年月項目
                    type = "month";
                } else if (isInputTime) { // 時刻項目
                    type = "time";
                }

                int max = columnInfo.getColumnSize();

                String css = "";
                if (isDetail && columnInfo.isPk()) {
                    css = " class=\"primaryKey\"";
                }

                String tag = "          ";
                if (!isDetail && isInputRange) {

                    // 検索画面の範囲指定項目の場合
                    tag += "<label for=\"" + id + "_1\" th:text=\"#{" + id + "}\">" + remarks + "</label>";
                    tag += "<input type=\"" + type + "\" id=\"" + id + "_1\" name=\"" + id + "_1\" maxlength=\"" + max
                            + "\" />";
                    tag += "～";
                    tag += "<input type=\"" + type + "\" id=\"" + id + "_2\" name=\"" + id + "_2\" maxlength=\"" + max
                            + "\" />";

                } else if (columnInfo.getReferInfo() != null) {
                    // 参照モデルの場合

                    TableInfo referInfo = columnInfo.getReferInfo();
                    String referName = StringUtil.toPascalCase(referInfo.getTableName());
                    String referDef = getReferDef(entityName, columnName, referInfo);
                    tag += "<label for=\"" + id + "\" th:text=\"#{" + id + "}\">" + remarks + "</label>";
                    tag += "<input type=\"" + type + "\" id=\"" + id + "\" name=\"" + id + "\" maxlength=\"" + max
                            + "\"" + css + referDef + "" + " />";
                    tag += "<a id=\"" + id + "\" th:href=\"@{/model/" + referName
                            + "S.html}\" target=\"dialog\" class=\"refer\" th:text=\"#{common.refer}\" tabindex=\"-1\">...</a>";

                    String meiColumnName = getMeiColumnName(columnName);
                    if (!tableInfo.getColumnInfos().containsKey(meiColumnName)) {
                        String meiId = entityName + "." + StringUtil.toCamelCase(meiColumnName);
                        tag += "<span id=\"" + meiId + "\" class=\"refer\"" + referDef + "></span>";
                    }
                } else {
                    tag += "<label for=\"" + id + "\" th:text=\"#{" + id + "}\">" + remarks + "</label>";
                    tag += "<input type=\"" + type + "\" id=\"" + id + "\" name=\"" + id + "\" maxlength=\"" + max
                            + "\"" + css + " />";
                }
                s.add(tag);
            }
            s.add("        </div>");
        }
    }

    private static String getReferDef(final String entityName, final String columnName, final TableInfo referInfo) {

        if (StringUtil.endsWith(referIdSuffixs, columnName)) {
            String meiColumnName = getMeiColumnName(columnName);
            String srcColumn = null;
            String destColumn = null;
            for (String referColumnName : referInfo.getColumnInfos().keySet()) {
                if (columnName.matches("^.*" + referColumnName + "$")) {
                    srcColumn = referColumnName;
                } else if (meiColumnName.matches("^.*" + referColumnName + "$")) {
                    destColumn = referColumnName;
                }
            }

            String referName = StringUtil.toPascalCase(referInfo.getTableName());
            String id = entityName + "." + StringUtil.toCamelCase(columnName);
            String meiId = entityName + "." + StringUtil.toCamelCase(meiColumnName);
            return " data-json=\"" + referName + "Search.json\" data-srcDef=\"" + srcColumn + ":"
                    + id + "\" data-destDef=\"" + meiId + ":" + destColumn + "\"";
        }

        return "";
    }

    private static String getMeiColumnName(final String columnName) {
        String meiColumnName = columnName;
        for (String referIdSuffix : referIdSuffixs) {
            meiColumnName = meiColumnName.replaceAll("(?i)" + referIdSuffix + "$", referMeiSuffix);
        }
        meiColumnName = StringUtil.toUpperCase(meiColumnName);
        return meiColumnName;
    }

    /**
     * 各モデルのプロパティファイル出力
     * @param htmlDir
     * @param tableInfo
     */
    private static void htmlProperties(final String htmlDir, final TableInfo tableInfo) {

        String tableName = tableInfo.getTableName();
        String entityName = StringUtil.toPascalCase(tableName);
        String remarks = tableInfo.getRemarks();

        List<String> s = new ArrayList<String>();
        s.add(entityName + "S.title   " + remarks + "検索");
        s.add(entityName + "S.h2      " + remarks + "検索");
        s.add(entityName + "S.legend  " + remarks + "検索");
        s.add(entityName + ".add      " + remarks + "追加");
        s.add(entityName + ".title    " + remarks + "詳細");
        s.add(entityName + ".h2       " + remarks + "詳細");
        s.add(entityName + ".legend   " + remarks + "詳細");
        s.add(entityName + ".h3       " + remarks + "一覧");
        s.add("");
        for (ColumnInfo columnInfo : tableInfo.getColumnInfos().values()) {
            String camel = StringUtil.toCamelCase(columnInfo.getColumnName());
            s.add(entityName + "." + camel + " " + columnInfo.getRemarks());
        }
        s.add("");
        for (ColumnInfo columnInfo : tableInfo.getColumnInfos().values()) {
            String camel = StringUtil.toCamelCase(columnInfo.getColumnName());
            s.add(entityName + "Grid." + camel + " " + columnInfo.getRemarks());
        }
        for (TableInfo brosInfo : tableInfo.getBrosInfos()) {
            String brosName = brosInfo.getTableName();
            String pascal = StringUtil.toPascalCase(brosName);
            String brosMei = brosInfo.getRemarks();
            s.add("");
            s.add(pascal + ".legend   " + brosMei + "詳細");
            for (ColumnInfo columnInfo : brosInfo.getColumnInfos().values()) {
                String camel = StringUtil.toCamelCase(columnInfo.getColumnName());
                s.add(pascal + "." + camel + " " + columnInfo.getRemarks());
            }
        }
        for (TableInfo childInfo : tableInfo.getChildInfos()) {
            String childName = childInfo.getTableName();
            String pascal = StringUtil.toPascalCase(childName);
            String childMei = childInfo.getRemarks();
            s.add("");
            s.add(pascal + ".h3  " + childMei + "一覧");
            s.add(pascal + ".add " + childMei + "追加");
            for (ColumnInfo columnInfo : childInfo.getColumnInfos().values()) {
                String camel = StringUtil.toCamelCase(columnInfo.getColumnName());
                s.add(pascal + "Grid." + camel + " " + columnInfo.getRemarks());
            }
        }

        FileUtil.writeFile(htmlDir + File.separator + entityName + ".properties", s);
    }

}