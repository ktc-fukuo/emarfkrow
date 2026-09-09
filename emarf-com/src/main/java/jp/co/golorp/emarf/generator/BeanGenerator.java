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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

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
import jp.co.golorp.emarf.util.ResourceBundles;

/**
 * java/html/sql出力
 *
 * @author golorp
 */
public abstract class BeanGenerator {

    /** DB方言クラス */
    protected static final DataSourcesAssist ASSIST = DataSources.getAssist();

    /** 起動時の自動生成か */
    protected static final boolean IS_GENERATE_AT_STARTUP;

    /** 評価対象としないテーブル名の正規表現 */
    public static final String IGNORE_RE;
    /** 長兄 */
    public static final String ELDEST_RE;
    /** 弟を設定しないテーブル名 */
    public static final String YOUNGEST_RE;
    /** 繰上りの弟モデルのテーブル名 */
    public static final String FOSTER_RE;
    /** 子を設定しないテーブル名 */
    public static final String DINKS_RE;
    /** 親を設定しないテーブル名 */
    public static final String ORPHANS_RE;
    /** 参照列名ペア */
    public static final Set<String[]> REFER_PAIRS = new LinkedHashSet<String[]>();
    /** 参照モデルにしない */
    public static final String NON_REFER_RE;

    /** 列評価をスキップする列名 */
    public static final String COL_IGNORE_RE;

    /** 適用日カラム名 */
    public static final String TEKIYO_BI;
    /** 廃止日カラム名 */
    protected static final String HAISHI_BI;
    /** 登録日時カラム名 */
    protected static final String INSERT_AT;
    /** 登録者カラム名 */
    protected static final String INSERT_BY;
    /** 更新日時カラム名 */
    protected static final String UPDATE_AT;
    /** 更新者カラム名 */
    protected static final String UPDATE_BY;

    /** ステータス区分 */
    public static final String STATUS_KB;
    /** 削除フラグ */
    protected static final String DELETE_F;
    /** 変更理由 */
    public static final String REASON;

    /** VIEWの検索条件とするプレフィクス */
    protected static final String[] VIEW_CRITERIA_PREFIXS;
    /** VIEWの詳細画面にするテーブル名 */
    public static final String VIEW_DETAIL;

    /** 決裁フロー：テーブル名 */
    protected static final String STATUS_TABLE_NAME;
    /** 決裁フロー：主キー */
    protected static final String STATUS_PRIMARY_KEYS;
    /** 決裁フロー：決裁日時 */
    protected static final String STATUS_KESSAI_AT;
    /** 決裁フロー：決裁者 */
    protected static final String STATUS_KESSAI_ID;

    /** グラフ化を判定する項目名のカラムサフィックス */
    public static final Set<String[]> GRAPH_COLS = new LinkedHashSet<String[]>();
    /** ガントチャート化を判定する項目名・開始日・終了日のカラムサフィックス */
    public static final Set<String[]> GANTT_COLS = new LinkedHashSet<String[]>();

    /** javaファイル出力ルートパス */
    protected static final String DIR_J;
    /***/
    protected static final String DIR_H;
    /***/
    protected static final String DIR_G;
    /***/
    protected static final String DIR_S;

    /** actionパッケージ */
    public static final String PKG_A;
    /** entityパッケージ */
    protected static final String PKG_E;
    /** formパッケージ */
    protected static final String PKG_F;

    /** ページ行数 */
    protected static final String GRID_ROWS;

    /** 表示順サフィックス */
    protected static final String[] ORDER_SUFS;

    /** 数値だがフォーマットしないサフィックス */
    protected static final String[] INT_NOFORMAT_SUFS;

    /** 数値列で自動採番しないサフィックス */
    public static final String INT_NONUMBERING_RE;
    /** 必須CHAR列の指定 */
    protected static final String CHAR_NOTNULL_RE;
    /** 固定長列で自動採番のサフィックス */
    public static final String CHAR_NUMBERING_RE;
    /** 非必須INT列の指定 */
    protected static final String NUMBER_NULLABLE_RE;
    /** 更新日時フォーマット */
    protected static final String UPDATE_TS_FORMAT;

    /** 読み取り専用サフィックス */
    protected static final String[] INPUT_RO_SUFS;
    /** 数値入力サフィックス */
    protected static final String[] INPUT_NO_SUFS;
    /** 年月入力サフィックス */
    protected static final String[] INPUT_YM_SUFS;
    /** 8桁日付入力サフィックス */
    protected static final String[] INPUT_D8_SUFS;
    /** タイムスタンプサフィックス */
    public static final String[] INPUT_TS_SUFS;
    /** 日時入力サフィックス */
    public static final String[] INPUT_DT_SUFS;
    /** 日付入力サフィックス */
    public static final String[] INPUT_BI_SUFS;
    /** 時刻入力サフィックス */
    public static final String[] INPUT_HM_SUFS;
    /** 時間入力サフィックス */
    protected static final String[] INPUT_TM_SUFS;
    /** 範囲指定サフィックス */
    public static final String[] INPUT_RG_SUFS;
    /** フラグサフィックス */
    protected static final String[] INPUT_F_SUFS;
    /** ビットフラグサフィックス */
    protected static final String[] INPUT_B_SUFS;
    /** ファイルサフィックス */
    protected static final String[] INPUT_FILE_SUFS;
    /** options項目サフィックス */
    protected static final String[] INPUT_OP_SUFS;
    /** pulldown項目サフィックス */
    protected static final String[] INPUT_PD_SUFS;
    /** テキストエリア項目サフィックス */
    protected static final String[] INPUT_TX_SUFFIXS;

    /** データjson */
    protected static final String OPT_J;
    /** 区分カラム */
    protected static final String OPT_K;
    /** 区分値カラム */
    protected static final String OPT_V;
    /** 区分値名カラム */
    protected static final String OPT_L;

    /** validサフィックス */
    protected static final List<String> VALID_SUFS = new ArrayList<String>();

    /** メニュー化しない正規表現 */
    protected static final String NAV_IGNORE_RE;
    /** 業務順のプレフィクス正規表現 */
    protected static final Map<String, String> NAV_ORDER_RES = new TreeMap<String, String>();

    /** logger */
    private static final Logger LOG = LoggerFactory.getLogger(BeanGenerator.class);

    /** BeanGenerator.properties */
    private static ResourceBundle bundle = ResourceBundles.getBundle(BeanGenerator.class);

    /** プロジェクトディレクトリ */
    private static String projectDir;

    /**
     * @return プロジェクトディレクトリ
     */
    public static String getProjectDir() {
        return projectDir;
    }

    static {

        //webからの自動生成ならコンパイルまで行う
        if (App.get("generateAtStartup") != null) {
            IS_GENERATE_AT_STARTUP = App.get("generateAtStartup").toLowerCase().equals("true");
        } else {
            IS_GENERATE_AT_STARTUP = false;
        }

        if (bundle != null && bundle.containsKey("relation.ignore.re")) {
            IGNORE_RE = bundle.getString("relation.ignore.re");
        } else {
            IGNORE_RE = null;
        }

        if (bundle != null && bundle.containsKey("relation.eldest.re")) {
            ELDEST_RE = bundle.getString("relation.eldest.re");
        } else {
            ELDEST_RE = "";
        }

        if (bundle != null && bundle.containsKey("relation.foster.re")) {
            FOSTER_RE = bundle.getString("relation.foster.re");
        } else {
            FOSTER_RE = "";
        }

        if (bundle != null && bundle.containsKey("relation.dinks.re")) {
            DINKS_RE = bundle.getString("relation.dinks.re");
        } else {
            DINKS_RE = "";
        }

        if (bundle != null && bundle.containsKey("relation.orphans.re")) {
            ORPHANS_RE = bundle.getString("relation.orphans.re");
        } else {
            ORPHANS_RE = "";
        }

        if (bundle != null && bundle.containsKey("relation.youngest.re")) {
            YOUNGEST_RE = bundle.getString("relation.youngest.re");
        } else {
            YOUNGEST_RE = "";
        }

        if (bundle != null && bundle.containsKey("relation.refer.pairs")) {
            String[] pairs = bundle.getString("relation.refer.pairs").split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":");
                REFER_PAIRS.add(kv);
            }
        }

        if (bundle != null && bundle.containsKey("relation.nonrefer.re")) {
            NON_REFER_RE = bundle.getString("relation.nonrefer.re");
        } else {
            NON_REFER_RE = "";
        }

        if (bundle != null && bundle.containsKey("column.ignore.re")) {
            COL_IGNORE_RE = bundle.getString("column.ignore.re");
        } else {
            COL_IGNORE_RE = "";
        }

        if (bundle != null && bundle.containsKey("column.start")) {
            TEKIYO_BI = bundle.getString("column.start");
        } else {
            TEKIYO_BI = null;
        }

        if (bundle != null && bundle.containsKey("column.until")) {
            HAISHI_BI = bundle.getString("column.until");
        } else {
            HAISHI_BI = "";
        }

        if (bundle != null && bundle.containsKey("column.insert.timestamp")) {
            INSERT_AT = bundle.getString("column.insert.timestamp");
        } else {
            INSERT_AT = null;
        }

        if (bundle != null && bundle.containsKey("column.insert.id")) {
            INSERT_BY = bundle.getString("column.insert.id");
        } else {
            INSERT_BY = null;
        }

        if (bundle != null && bundle.containsKey("column.update.timestamp")) {
            UPDATE_AT = bundle.getString("column.update.timestamp");
        } else {
            UPDATE_AT = null;
        }

        if (bundle != null && bundle.containsKey("column.update.id")) {
            UPDATE_BY = bundle.getString("column.update.id");
        } else {
            UPDATE_BY = null;
        }

        if (bundle != null && bundle.containsKey("column.status")) {
            STATUS_KB = bundle.getString("column.status");
        } else {
            STATUS_KB = null;
        }

        if (bundle != null && bundle.containsKey("column.delete")) {
            DELETE_F = bundle.getString("column.delete");
        } else {
            DELETE_F = null;
        }

        if (bundle != null && bundle.containsKey("column.reason")) {
            REASON = bundle.getString("column.reason");
        } else {
            REASON = null;
        }

        if (bundle != null && bundle.containsKey("view.criteria.prefix")) {
            VIEW_CRITERIA_PREFIXS = bundle.getString("view.criteria.prefix").split(",");
        } else {
            VIEW_CRITERIA_PREFIXS = null;
        }

        if (bundle != null && bundle.containsKey("view.detail")) {
            VIEW_DETAIL = bundle.getString("view.detail");
        } else {
            VIEW_DETAIL = "";
        }

        if (bundle != null && bundle.containsKey("status.tableName")) {
            STATUS_TABLE_NAME = bundle.getString("status.tableName");
        } else {
            STATUS_TABLE_NAME = null;
        }

        if (bundle != null && bundle.containsKey("status.primaryKeys")) {
            STATUS_PRIMARY_KEYS = bundle.getString("status.primaryKeys");
        } else {
            STATUS_PRIMARY_KEYS = null;
        }

        if (bundle != null && bundle.containsKey("status.kessaiTs")) {
            STATUS_KESSAI_AT = bundle.getString("status.kessaiTs");
        } else {
            STATUS_KESSAI_AT = null;
        }

        if (bundle != null && bundle.containsKey("status.kessaiId")) {
            STATUS_KESSAI_ID = bundle.getString("status.kessaiId");
        } else {
            STATUS_KESSAI_ID = null;
        }

        if (bundle != null && bundle.containsKey("graph.columns")) {
            String[] graphDefs = bundle.getString("graph.columns").split(",");
            for (String graphDef : graphDefs) {
                String[] columns = graphDef.split(":");
                GRAPH_COLS.add(columns);
            }
        }

        if (bundle != null && bundle.containsKey("gantt.columns")) {
            String[] ganttDefs = bundle.getString("gantt.columns").split(",");
            for (String ganttDef : ganttDefs) {
                String[] columns = ganttDef.split(":");
                GANTT_COLS.add(columns);
            }
        }

        if (bundle != null && bundle.containsKey("dir.java")) {
            DIR_J = bundle.getString("dir.java");
        } else {
            DIR_J = "src\\main\\java";
        }

        if (bundle != null && bundle.containsKey("dir.html")) {
            DIR_H = bundle.getString("dir.html");
        } else {
            DIR_H = "src\\main\\resources\\META-INF\\resources\\WEB-INF\\templates\\model";
        }

        if (bundle != null && bundle.containsKey("dir.grid")) {
            DIR_G = bundle.getString("dir.grid");
        } else {
            DIR_G = "src\\main\\resources\\META-INF\\resources\\model";
        }

        if (bundle != null && bundle.containsKey("dir.sql")) {
            DIR_S = bundle.getString("dir.sql");
        } else {
            DIR_S = "src\\main\\resources\\sql";
        }

        if (bundle != null && bundle.containsKey("java.package.action")) {
            PKG_A = bundle.getString("java.package.action") + ".model.base";
        } else {
            PKG_A = "com.example.action.model.base";
        }

        if (bundle != null && bundle.containsKey("java.package.entity")) {
            PKG_E = bundle.getString("java.package.entity");
        } else {
            PKG_E = "com.example.entity";
        }

        if (bundle != null && bundle.containsKey("java.package.form")) {
            PKG_F = bundle.getString("java.package.form") + ".model.base";
        } else {
            PKG_F = "com.example.form.model.base";
        }

        if (bundle != null && bundle.containsKey("grid.rows")) {
            GRID_ROWS = bundle.getString("grid.rows");
        } else {
            GRID_ROWS = "";
        }

        if (bundle != null && bundle.containsKey("column.order.suffixs")) {
            ORDER_SUFS = bundle.getString("column.order.suffixs").split(",");
        } else {
            ORDER_SUFS = null;
        }

        if (bundle != null && bundle.containsKey("column.int.noformat.suffixs")) {
            INT_NOFORMAT_SUFS = bundle.getString("column.int.noformat.suffixs").split(",");
        } else {
            INT_NOFORMAT_SUFS = null;
        }

        if (bundle != null && bundle.containsKey("column.int.nonumbering.re")) {
            INT_NONUMBERING_RE = bundle.getString("column.int.nonumbering.re");
        } else {
            INT_NONUMBERING_RE = "";
        }

        if (bundle != null && bundle.containsKey("column.char.notnull.re")) {
            CHAR_NOTNULL_RE = bundle.getString("column.char.notnull.re");
        } else {
            CHAR_NOTNULL_RE = null;
        }

        if (bundle != null && bundle.containsKey("column.char.numbering.re")) {
            CHAR_NUMBERING_RE = bundle.getString("column.char.numbering.re");
        } else {
            CHAR_NUMBERING_RE = "";
        }

        if (bundle != null && bundle.containsKey("column.number.nullable.re")) {
            NUMBER_NULLABLE_RE = bundle.getString("column.number.nullable.re");
        } else {
            NUMBER_NULLABLE_RE = null;
        }

        if (bundle != null && bundle.containsKey("column.update.timestamp.format")) {
            UPDATE_TS_FORMAT = bundle.getString("column.update.timestamp.format");
        } else {
            UPDATE_TS_FORMAT = null;
        }

        if (bundle != null && bundle.containsKey("input.readonly.suffixs")) {
            INPUT_RO_SUFS = bundle.getString("input.readonly.suffixs").split(",");
        } else {
            INPUT_RO_SUFS = null;
        }

        if (bundle != null && bundle.containsKey("input.ym.suffixs")) {
            INPUT_YM_SUFS = bundle.getString("input.ym.suffixs").split(",");
        } else {
            INPUT_YM_SUFS = null;
        }

        if (bundle != null && bundle.containsKey("input.date8.suffixs")) {
            INPUT_D8_SUFS = bundle.getString("input.date8.suffixs").split(",");
        } else {
            INPUT_D8_SUFS = null;
        }

        if (bundle != null && bundle.containsKey("input.timestamp.suffixs")) {
            INPUT_TS_SUFS = bundle.getString("input.timestamp.suffixs").split(",");
        } else {
            INPUT_TS_SUFS = null;
        }

        if (bundle != null && bundle.containsKey("input.number.suffixs")) {
            INPUT_NO_SUFS = bundle.getString("input.number.suffixs").split(",");
        } else {
            INPUT_NO_SUFS = null;
        }

        if (bundle != null && bundle.containsKey("input.datetime.suffixs")) {
            INPUT_DT_SUFS = bundle.getString("input.datetime.suffixs").split(",");
        } else {
            INPUT_DT_SUFS = null;
        }

        if (bundle != null && bundle.containsKey("input.date.suffixs")) {
            INPUT_BI_SUFS = bundle.getString("input.date.suffixs").split(",");
        } else {
            INPUT_BI_SUFS = null;
        }

        if (bundle != null && bundle.containsKey("input.hour.suffixs")) {
            INPUT_HM_SUFS = bundle.getString("input.hour.suffixs").split(",");
        } else {
            INPUT_HM_SUFS = null;
        }

        if (bundle != null && bundle.containsKey("input.time.suffixs")) {
            INPUT_TM_SUFS = bundle.getString("input.time.suffixs").split(",");
        } else {
            INPUT_TM_SUFS = null;
        }

        if (bundle != null && bundle.containsKey("input.range.suffixs")) {
            INPUT_RG_SUFS = bundle.getString("input.range.suffixs").split(",");
        } else {
            INPUT_RG_SUFS = null;
        }

        if (bundle != null && bundle.containsKey("input.flag.suffixs")) {
            INPUT_F_SUFS = bundle.getString("input.flag.suffixs").split(",");
        } else {
            INPUT_F_SUFS = null;
        }

        if (bundle != null && bundle.containsKey("input.bit.suffixs")) {
            INPUT_B_SUFS = bundle.getString("input.bit.suffixs").split(",");
        } else {
            INPUT_B_SUFS = null;
        }

        if (bundle != null && bundle.containsKey("input.file.suffixs")) {
            INPUT_FILE_SUFS = bundle.getString("input.file.suffixs").split(",");
        } else {
            INPUT_FILE_SUFS = null;
        }

        if (bundle != null && bundle.containsKey("input.options.suffixs")) {
            INPUT_OP_SUFS = bundle.getString("input.options.suffixs").split(",");
        } else {
            INPUT_OP_SUFS = null;
        }

        if (bundle != null && bundle.containsKey("input.pulldown.suffixs")) {
            INPUT_PD_SUFS = bundle.getString("input.pulldown.suffixs").split(",");
        } else {
            INPUT_PD_SUFS = null;
        }

        if (bundle != null && bundle.containsKey("input.textarea.suffixs")) {
            INPUT_TX_SUFFIXS = bundle.getString("input.textarea.suffixs").split(",");
        } else {
            INPUT_TX_SUFFIXS = null;
        }

        if (bundle != null && bundle.containsKey("options.json")) {
            OPT_J = bundle.getString("options.json");
        } else {
            OPT_J = "";
        }

        if (bundle != null && bundle.containsKey("options.key")) {
            OPT_K = bundle.getString("options.key").toUpperCase();
        } else {
            OPT_K = "";
        }

        if (bundle != null && bundle.containsKey("options.value")) {
            OPT_V = bundle.getString("options.value").toUpperCase();
        } else {
            OPT_V = "";
        }

        if (bundle != null && bundle.containsKey("options.label")) {
            OPT_L = bundle.getString("options.label").toUpperCase();
        } else {
            OPT_L = "";
        }

        // validator正規表現の接尾辞を取得
        if (bundle != null) {
            for (String k : bundle.keySet()) {
                if (k.startsWith("valid.")) {
                    VALID_SUFS.add(k.replaceFirst("valid.", ""));
                }
            }
        }

        if (bundle != null && bundle.containsKey("nav.ignore.re")) {
            NAV_IGNORE_RE = bundle.getString("nav.ignore.re");
        } else {
            NAV_IGNORE_RE = "";
        }

        // 業務並び順
        if (bundle != null) {
            for (String k : bundle.keySet()) {
                if (k.startsWith("nav.order.prefix.re.")) {
                    String order = k.replaceFirst("nav.order.prefix.re.", "");
                    String re = bundle.getString(k);
                    NAV_ORDER_RES.put(order, re);
                }
            }
        }
    }

    /** プライベートコンストラクタ */
    protected BeanGenerator() {
    }

    /**
     * 各ファイル出力 主処理
     * @param dir プロジェクトのディレクトリ
     */
    public static void generate(final String dir) {

        LOG.info("start.");

        //プロジェクトディレクトリを退避
        projectDir = dir;

        /*
         * データベースから自動生成
         */
        // テーブル情報を取得
        List<TableInfo> tables = DataSources.getTables();

        //エンティティクラス
        EntityGenerator.generate(tables);

        //アクションフォルダ再作成
        String actionPackagePath = PKG_A.replace(".", File.separator);
        String actionPackageDir = projectDir + File.separator + DIR_J + File.separator + actionPackagePath;
        FileUtil.reMkDir(actionPackageDir);

        //詳細画面アクションクラス
        ActionGeneratorDetail.generate(tables);

        //検索画面アクションクラス
        ActionGeneratorIndex.generate(tables);

        //フォームクラス
        FormGenerator.generate(tables);

        //HTMLファイル
        HtmlGenerator.generate(tables);

        //検索SQLファイル
        SqlGenerator.generate(tables);

        LOG.info("success.");
    }

    /**
     * @param colName
     * @param column
     * @return rightHand
     */
    public static String getRightHand(final String colName, final ColumnInfo column) {
        String rightHand = ":" + StringUtil.toSnakeCase(colName);
        if (column.getDataType().equals("java.time.LocalDate")) {
            rightHand = ASSIST.toDateSQL(rightHand);
        } else if (column.getDataType().equals("java.time.LocalDateTime")) {
            if (StringUtil.endsWith(INPUT_TS_SUFS, column.getName())) {
                rightHand = ASSIST.toTimestampSQL(rightHand);
            } else {
                rightHand = ASSIST.toDateTimeSQL(rightHand);
            }
        } else if (!column.isPk() && column.getTypeName().equals("CHAR")
                && !StringUtil.isNullOrWhiteSpace(CHAR_NOTNULL_RE)
                && !column.getName().matches(CHAR_NOTNULL_RE)) {
            //主キー以外のCHAR列で、必須CHAR指定に合致しない場合、NULLならスペースを補填する
            rightHand = "NVL (" + rightHand + ", ' ')";
        } else if (!column.isPk() && column.getTypeName().equals("NUMBER")
                && !StringUtil.isNullOrWhiteSpace(NUMBER_NULLABLE_RE)
                && column.getName().matches(NUMBER_NULLABLE_RE)) {
            //主キー以外のNUMBER列で、非必須INT指定に合致する場合、NULLなら「0」を補填する
            rightHand = "NVL (" + rightHand + ", 0)";
        }
        return rightHand;
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

            if (!StringUtil.isNullOrWhiteSpace(DELETE_F) && child.getColumns().containsKey(DELETE_F)) {
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

                s.add(sp + "        java.util.List<" + PKG_E + "." + e + "> " + i + "s = " + p + ".refer" + e + "s();");
                s.add(sp + "        if (" + i + "s != null) {");
                s.add(sp + "            for (" + PKG_E + "." + e + " " + i + " : " + i + "s) {");
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

            s.add(p + "        java.util.List<" + PKG_E + "." + e + "> " + i + "s = " + parent + ".refer" + e + "s();");
            s.add(p + "        if (" + i + "s != null) {");
            s.add(p + "            for (" + PKG_E + "." + e + " " + i + " : " + i + "s) {");
            if (child.getChildren().size() > 0) {
                // forでもう一段降りているから「+2」
                getApplyChilds(s, i, child.getChildren(), indent + 2);
            }
            s.add("");
            if (child.getColumns().containsKey(STATUS_KB)) {
                s.add(p + "                " + i + ".set" + StringUtil.toPascalCase(STATUS_KB) + "(0);");
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

            s.add(p + "        java.util.List<" + PKG_E + "." + e + "> " + i + "s = " + parent + ".refer" + e + "s();");
            s.add(p + "        if (" + i + "s != null) {");
            s.add(p + "            for (" + PKG_E + "." + e + " " + i + " : " + i + "s) {");
            if (child.getChildren().size() > 0) {
                // forでもう一段降りているから「+2」
                getCancelChilds(s, i, child.getChildren(), indent + 2);
            }
            s.add("");
            if (child.getColumns().containsKey(STATUS_KB)) {
                s.add(p + "                " + i + ".set" + StringUtil.toPascalCase(STATUS_KB) + "(null);");
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

            s.add(p + "        java.util.List<" + PKG_E + "." + e + "> " + i + "s = " + parent + ".refer" + e + "s();");
            s.add(p + "        if (" + i + "s != null) {");
            s.add(p + "            for (" + PKG_E + "." + e + " " + i + " : " + i + "s) {");
            if (child.getChildren().size() > 0) {
                // forでもう一段降りているから「+2」
                getPermitChilds(s, i, child.getChildren(), indent + 2);
            }
            s.add("");
            if (child.getColumns().containsKey(STATUS_KB)) {
                s.add(p + "                " + i + ".set" + StringUtil.toPascalCase(STATUS_KB) + "(1);");
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

            s.add(p + "        java.util.List<" + PKG_E + "." + e + "> " + i + "s = " + parent + ".refer" + e + "s();");
            s.add(p + "        if (" + i + "s != null) {");
            s.add(p + "            for (" + PKG_E + "." + e + " " + i + " : " + i + "s) {");
            if (child.getChildren().size() > 0) {
                // forでもう一段降りているから「+2」
                getForbidChilds(s, i, child.getChildren(), indent + 2);
            }
            s.add("");
            if (child.getColumns().containsKey(STATUS_KB)) {
                s.add(p + "                " + i + ".set" + StringUtil.toPascalCase(STATUS_KB) + "(-1);");
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
        String dstDir = projectDir + File.separator + DIR_J;

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
    public static boolean isMeta(final String s) {
        return s.matches("(?i)^" + INSERT_AT + "$") || s.matches("(?i)^" + INSERT_BY + "$")
                || s.matches("(?i)^" + UPDATE_AT + "$") || s.matches("(?i)^" + UPDATE_BY + "$")
                || s.matches("(?i)^" + DELETE_F + "$") || s.matches("(?i)^" + STATUS_KB + "$");
    }

    /**
     * @param s
     * @return boolean
     */
    public static boolean isMetaTsBy(final String s) {
        return s.matches("(?i)^" + INSERT_AT + "$") || s.matches("(?i)^" + INSERT_BY + "$")
                || s.matches("(?i)^" + UPDATE_AT + "$") || s.matches("(?i)^" + UPDATE_BY + "$");
    }

    /**
     * @param s
     * @return boolean
     */
    public static boolean isMetaTs(final String s) {
        return s.matches("(?i)^" + INSERT_AT + "$") || s.matches("(?i)^" + UPDATE_AT + "$");
    }

    /**
     * @param name
     * @return boolean
     */
    public static boolean isMetaBy(final String name) {
        return name.matches("(?i)^" + INSERT_BY + "$") || name.matches("(?i)^" + UPDATE_BY + "$");
    }

    /**
     * @param s
     * @return boolean
     */
    public static boolean isMetaIns(final String s) {
        return s.matches("(?i)^" + INSERT_AT + "$") || s.matches("(?i)^" + INSERT_BY + "$");
    }

}
