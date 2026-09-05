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

import jp.co.golorp.emarf.io.FileUtil;
import jp.co.golorp.emarf.lang.StringUtil;

/**
 * HTMLファイル出力
 *
 * @author golorp
 */
public final class HtmlGeneratorGantt extends HtmlGenerator {

    /**
     * コンストラクタ
     */
    private HtmlGeneratorGantt() {
    }

    /**
     * ガント画面 HTML出力
     * @param htmlDir HTMLファイル出力ディレクトリ
     * @param table テーブル情報
     * @param tables テーブル情報
     */
    public static void htmlGantt(final String htmlDir, final TableInfo table, final List<TableInfo> tables) {
        String e = StringUtil.toPascalCase(table.getName());
        String es = e + "S";
        List<String> s = new ArrayList<String>();
        addHtmlHead(s, es, table.getName());
        s.add("<script th:src=\"@{/model/" + e + ".js}\"></script>");
        s.add("<script th:src=\"@{/model/" + e + "GridColumns.js}\"></script>");
        s.add("<script th:src=\"@{/model/" + e + "GanttTasks.js}\"></script>");
        Map<TableInfo, Integer> added = new HashMap<TableInfo, Integer>();
        added.put(table, 1);
        htmlNestGrid(s, table, tables, added, false, "", false);
        s.add("</head>");
        s.add("<body>");
        s.add("  <div layout:fragment=\"article\">");
        s.add("    <h2 th:text=\"#{" + es + ".h2}\">h2</h2>");
        s.add("    <!-- 検索フォーム -->");
        s.add("    <form name=\"" + e + "SearchForm\" action=\"" + e + "Search.ajax\" class=\"search\">");
        s.add("      <fieldset>");
        s.add("        <legend th:text=\"#{" + es + ".legend}\">legend</legend>");
        htmlFields(table, s, false, false, false);
        s.add("      </fieldset>");
        s.add("      <div class=\"buttons\">");
        s.add("        <button type=\"button\" id=\"Reset" + e
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
            s.add("        <a th:href=\"@{/model/" + e + ".html(anew)}\" target=\"dialog\" id=\"" + e + "\" class=\""
                    + anewClass + "\" th:text=\"#{" + e + ".add}\" tabindex=\"-1\">" + table.getName() + "</a>");
            //            if (table.getName().matches(ELDEST_RE)) {
            //                for (TableInfo bro : table.getBrothers()) {
            //                    String b = StringUtil.toPascalCase(bro.getName());
            //                    s.add("        <a th:href=\"@{/model/" + b + ".html}\" target=\"dialog\" id=\"" + b + "\" class=\""
            //                            + anewClass + "\" style=\"display: none;\" th:text=\"#{" + b + ".add}\" tabindex=\"-1\">"
            //                            + bro.getName() + "</a>");
            //                }
            //            }
            //            if (isDeriver) {
            //                HashSet<String> deriveFroms = new HashSet<String>();
            //                for (ColumnInfo col : table.getColumns().values()) {
            //                    if (col.getDeriveFrom() != null && !deriveFroms.contains(col.getDeriveFrom().getName())) {
            //                        deriveFroms.add(col.getDeriveFrom().getName());
            //                        String fieldId = null;
            //                        for (String pk : col.getDeriveFrom().getPrimaryKeys()) {
            //                            fieldId = e + ".derivee" + StringUtil.toPascalCase(pk);
            //                            s.add(htmlFieldsInput(fieldId, "text", "", col.getDeriveFrom().getColumns().get(pk), null));
            //                        }
            //                        String r = StringUtil.toPascalCase(col.getDeriveFrom().getName());
            //                        s.add("          <a id=\"" + fieldId + "\" th:href=\"@{/model/" + r + "S.html?action=" + r
            //                                + "Correct.ajax}\" target=\"dialog\" class=\"derivee\" th:text=\"#{common.correct}\" tabindex=\"-1\">...</a>");
            //                    }
            //                }
            //            }
        }
        s.add("      </div>");
        s.add("      <div class=\"submits\">");
        s.add("        <button id=\"Search" + e + "\" type=\"submit\" class=\"search\" data-ganttId=\"" + e
                + "Gantt\" th:text=\"#{common.search}\">search</button>");
        s.add("      </div>");
        s.add("    </form>");
        s.add("    <!-- 一覧フォーム -->");
        s.add("    <h3 th:text=\"#{" + e + ".h3}\">h3</h3>");
        s.add("    <div class=\"ganttWrapper\">");
        s.add("      <svg id=\"" + e + "Gantt\" class=\"gantt\" th:data-href=\"@{/model/" + e + ".html}\"></svg>");
        s.add("    </div>");
        s.add("  </div>");
        s.add("</body>");
        s.add("</html>");
        FileUtil.writeFile(htmlDir + File.separator + e + "G.html", s);
    }

    /**
     * ガント画面 タスク定義出力
     * @param gridDir グリッド出力ディレクトリ
     * @param table テーブル情報
     * @param nameColumn
     * @param startColumn
     * @param endColumn
     * @param sinceColumn
     * @param untilColumn
     */
    public static void htmlGanttTasks(final String gridDir, final TableInfo table, final String nameColumn,
            final String startColumn, final String endColumn, final String sinceColumn, final String untilColumn) {

        String entity = StringUtil.toPascalCase(table.getName());

        List<String> s = new ArrayList<String>();
        s.add("/**");
        s.add(" * " + table.getName() + " gantt tasks");
        s.add(" */");
        s.add("");
        s.add("let " + entity + "GanttTasks = {");
        s.add("");
        s.add("    load: function(data) {");
        s.add("");
        s.add("        let tasks = [];");
        s.add("");
        s.add("        for (let r in data) {");
        s.add("            let row = data[r];");
        s.add("");
        s.add("            let task = {};");
        s.add("            task.id = row." + table.getPrimaryKeys().get(0) + ";");
        s.add("            task.name = row." + nameColumn + ";");
        s.add("            task.start = row." + startColumn + ";");
        s.add("            task.end = row." + endColumn + ";");
        s.add("            task.dependencies = row.DEPENDENCIES ? row.DEPENDENCIES + '' : '';");
        if (sinceColumn != null && untilColumn != null) {
            s.add("");
            s.add("            task.since = row." + sinceColumn + ";");
            s.add("            task.until = row." + untilColumn + ";");
        }
        s.add("");
        for (String k : table.getColumns().keySet()) {
            ColumnInfo col = table.getColumns().get(k);
            if (col.getName().matches("(?i)^(id|name|start|end|dependencies)$")) {
                continue;
            }
            s.add("            task." + StringUtil.toCamelCase(col.getName()) + " = row." + col.getName() + ";");
        }
        s.add("");
        s.add("            tasks.push(task);");
        s.add("        }");
        s.add("");
        s.add("        return tasks;");
        s.add("    }");
        s.add("};");

        FileUtil.writeFile(gridDir + File.separator + entity + "GanttTasks.js", s);
    }

}
