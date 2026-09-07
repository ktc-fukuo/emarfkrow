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

package jp.co.golorp.emarf.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.ResourceBundle;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.golorp.emarf.exception.SysError;
import jp.co.golorp.emarf.generator.BeanGenerator;
import jp.co.golorp.emarf.io.FileUtil;
import jp.co.golorp.emarf.lang.StringUtil;
import jp.co.golorp.emarf.properties.App;
import jp.co.golorp.emarf.time.DateTimeUtil;
import jp.co.golorp.emarf.util.Messages;
import jp.co.golorp.emarf.util.ResourceBundles;

/**
 * エクセル操作ユーティリティ
 *
 * @author golorp
 */
public final class XlsxUtil {

    /** logger */
    private static final Logger LOG = LoggerFactory.getLogger(XlsxUtil.class);

    /** セパレータ */
    private static String sep = File.separator;

    /** properties */
    private static ResourceBundle bundle = ResourceBundles.getBundle(BeanGenerator.class);

    /** options項目サフィックス */
    private static String[] optionsSuffixs;

    static {
        if (bundle != null) {
            optionsSuffixs = bundle.getString("input.options.suffixs").split(",");
        }
    }

    /** プライベートコンストラクタ */
    private XlsxUtil() {
    }

    /**
     * @param pathes テンプレートファイル保管ディレクトリ内のパス構成
     * @param layoutFileName レイアウトファイル名
     * @param layoutSheetMap Map<レイアウトシート名, Map<追加するシート名, Map<データの名称, Object>>>
     * @param baseMei 保存ファイル接頭辞
     * @return 一時ディレクトリに作成した帳票ファイルパス
     */
    public static String getGeneratedPath(final List<String> pathes, final String layoutFileName,
            final Map<String, Map<String, Map<String, Object>>> layoutSheetMap, final String baseMei) {

        String extension = null;
        File workFile = null;

        // テンプレートファイルのロック防止のため、テンプレートファイルを作業用ファイルにコピー
        String layoutsDir = App.get("layoutsVirtualDir");
        if (layoutsDir == null) {
            layoutsDir = "/WEB-INF/layouts";
        }
        String layoutFilePath = layoutsDir + sep + String.join(sep, pathes) + sep + layoutFileName;
        File layoutFile = FileUtil.get(layoutFilePath);
        if (!layoutFile.exists()) {
            if (pathes.size() > 0) {
                pathes.remove(pathes.size() - 1);
                return getGeneratedPath(pathes, layoutFileName, layoutSheetMap, baseMei);
            } else {
                //                throw new SysError("error.notexist.file", layoutFileName);
                // なければエクセルファイルを作る
                Workbook layoutBook = new XSSFWorkbook();
                extension = layoutFileName.replaceAll("^.+\\.", "");
                workFile = XlsxUtil.write(layoutBook, baseMei, extension);
            }
        } else {
            Workbook layoutBook = XlsxUtil.file2Workbook(layoutFile);
            extension = layoutFile.getName().replaceAll("^.+\\.", "");
            workFile = XlsxUtil.write(layoutBook, baseMei, extension);
        }

        Calendar begin = Calendar.getInstance();
        LOG.debug("XLSX generate start.");

        // 作業用ファイルをワークブックとして取得
        Workbook workbook = XlsxUtil.file2Workbook(workFile);
        // シーケンシャルアクセス専用のチューニング
        // workbook = new SXSSFWorkbook((XSSFWorkbook) workbook);

        // ワークブック用データjsonでループ
        for (Entry<String, Map<String, Map<String, Object>>> layoutSheet : layoutSheetMap.entrySet()) {

            // レイアウトシート名
            String layoutSheetName = layoutSheet.getKey();

            // 追加シートデータ
            Map<String, Map<String, Object>> addSheetMap = layoutSheet.getValue();

            // 作業用ファイルにシート追加
            addSheets(workbook, layoutSheetName, addSheetMap);
        }

        // 作業用ファイルを別名で保管して作業用ファイルを削除
        File file = XlsxUtil.write(workbook, baseMei, extension);
        workFile.delete();

        Calendar end = Calendar.getInstance();
        long millis = end.getTimeInMillis() - begin.getTimeInMillis();
        LOG.debug("XLSX generate end in " + millis + " millis. [" + file.getAbsolutePath() + "]");

        // 保管したファイルパスを返す
        return file.getAbsolutePath();
    }

    /**
     * @param workbook        作業用ファイル
     * @param layoutSheetName レイアウトシート名
     * @param addSheetMap     Map<追加するシート名, Map<データの名称, Object>>
     */
    private static void addSheets(final Workbook workbook, final String layoutSheetName,
            final Map<String, Map<String, Object>> addSheetMap) {

        // レイアウトシートを取得
        Sheet layoutSheet = workbook.getSheet(layoutSheetName);

        // レイアウトシートがない（テンプレートファイルがない）場合は作成
        if (layoutSheet == null) {

            // レイアウトシート追加
            layoutSheet = workbook.createSheet(layoutSheetName);

            // 追加シート情報でループ
            for (Entry<String, Map<String, Object>> addSheetEntry : addSheetMap.entrySet()) {

                // シート名
                //                String addSheetName = addSheetEntry.getKey();
                // モデル名：データ
                Map<String, Object> addSheetData = addSheetEntry.getValue();

                int r = 0;
                int c = 0;

                // タイトルとプレースホルダの出力
                for (Entry<String, Object> addSheetItem : addSheetData.entrySet()) {

                    String entityName = addSheetItem.getKey();
                    Object sheetData = addSheetItem.getValue();

                    Map<String, Object> dataItem = null;
                    String left = null;
                    String right = null;

                    if (sheetData instanceof List) {
                        // 検索画面の場合
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> dataList = (List<Map<String, Object>>) sheetData;
                        dataItem = dataList.get(0);
                        left = "[[";
                        right = "]]";
                    } else if (sheetData instanceof Map) {
                        // 単票画面の場合
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) sheetData;
                        dataItem = map;
                        left = "{{";
                        right = "}}";
                    }

                    // 列情報を転記
                    if (dataItem != null) {
                        int childs = 0;
                        for (Entry<String, Object> e : dataItem.entrySet()) {
                            String k = e.getKey();
                            Object v = e.getValue();

                            boolean isChilds = false;
                            if (v instanceof List) {
                                // List<?> list = (List<?>) v;
                                // if (!list.isEmpty() && list.get(0) instanceof Map<?, ?>) {
                                if (!StringUtil.endsWith(optionsSuffixs, StringUtil.toSnakeCase(k))) {
                                    isChilds = true;
                                }
                            }

                            if (!isChilds) {
                                // 子モデルリストでない場合
                                setCellValue(layoutSheet, r, c, k);
                                setCellValue(layoutSheet, r + 1, c++, left + entityName + "." + k + right);
                            } else {
                                // 子モデルリストの場合
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> dataList = (List<Map<String, Object>>) v;
                                if (dataList.size() > 0) {
                                    Map<String, Object> childItem = dataList.get(0);
                                    if (childItem != null) {
                                        int r2 = 3 * ++childs;
                                        int c2 = 0;
                                        for (Entry<String, Object> e2 : childItem.entrySet()) {
                                            String k2 = e2.getKey();
                                            // Object v2 = e2.getValue();
                                            setCellValue(layoutSheet, r2, c2, k2);
                                            setCellValue(layoutSheet, r2 + 1, c2++,
                                                    "[[" + e.getKey() + "." + k2 + "]]");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 追加シートの１枚目だけ処理すればいい
                break;
            }
        }

        // タイトル項目のプレースホルダのアドレスを初期化
        Map<String, CellAddress> titleAddresses = new HashMap<String, CellAddress>();

        // レイアウトシートの使用範囲内で、行・列ループして、タイトル項目のアドレスを取得
        for (int r = 0; r <= layoutSheet.getLastRowNum(); r++) {
            Row row = layoutSheet.getRow(r);
            if (row == null) {
                continue;
            }
            for (int c = 0; c < row.getLastCellNum(); c++) {
                Cell cell = CellUtil.getCell(row, c);
                Object o = XlsxUtil.getCellValue(cell);
                if (o == null) {
                    continue;
                }

                String s = String.valueOf(o);
                if (s.matches("^#\\{.+\\}$")) {
                    String key = s.replaceAll("^#\\{|\\}$", "");
                    titleAddresses.put(key, new CellAddress(r, c));
                }
            }
        }

        for (Entry<String, CellAddress> address : titleAddresses.entrySet()) {
            setCellValue(layoutSheet, address.getValue(), Messages.get(address.getKey()));
        }

        // addSheetMapでループ
        for (Entry<String, Map<String, Object>> addSheet : addSheetMap.entrySet()) {

            // 追加するシート名
            String addSheetName = addSheet.getKey();

            // 追加するシートの内容
            Map<String, Object> sheetDataMap = addSheet.getValue();

            // レイアウトシートをコピーしてシート名を変更
            int layoutSheetIndex = workbook.getSheetIndex(layoutSheet);
            Sheet newSheet = workbook.cloneSheet(layoutSheetIndex);
            workbook.setSheetName(workbook.getSheetIndex(newSheet), addSheetName);
            print(newSheet, sheetDataMap);
        }

        // レイアウトシートを削除
        workbook.removeSheetAt(workbook.getSheetIndex(layoutSheet));
    }

    /**
     * @param sheet 対象シート名
     * @param sheetDataMap 設定するデータのマップ
     */
    private static void print(final Sheet sheet, final Map<String, Object> sheetDataMap) {

        for (Entry<String, Object> sheetData : sheetDataMap.entrySet()) {

            // アクション名かエンティティ名（TEntitySearchかTEntity）
            String dataName = sheetData.getKey();

            Object data = sheetData.getValue();

            if (data instanceof List) {
                // 一覧系エクセル

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> list = (List<Map<String, Object>>) data;

                //                if (list.size() == 0) {
                //                    continue;
                //                }

                // レイアウトシートの使用範囲内で、行・列ループして、各プレースホルダのアドレスを取得
                Map<String, CellAddress> meisaiAddresses = new HashMap<String, CellAddress>();

                //                LOG.debug(dataName);
                Range range = getRange(sheet, meisaiAddresses, dataName);
                //                LOG.debug("    b: " + range.getBoR() + ", " + range.getBoC());
                //                LOG.debug("    e: " + range.getEoR() + ", " + range.getEoC());

                if (range == null) {
                    continue;
                }

                // 明細行インデクス
                int meisaiIndex = 0;

                // 使用行数
                int rowHeight = range.getEoR() - range.getBoR() + 1;

                for (Map<String, Object> meisaiJson : list) {

                    // 明細段組み１行目
                    int currentFirstRowIndex = range.getBoR() + meisaiIndex * rowHeight;

                    // 明細２行目以降ならレイアウトをコピー
                    if (meisaiIndex > 0) {
                        copyRange(sheet, range, currentFirstRowIndex);
                    }

                    // 明細行を処理
                    for (Entry<String, Object> itemJson : meisaiJson.entrySet()) {
                        String k = itemJson.getKey();
                        Object v = itemJson.getValue();

                        // プレースホルダを退避済みならセル値を設定
                        CellAddress address = meisaiAddresses.get(k);
                        if (address == null) {
                            address = meisaiAddresses.get(StringUtil.toCamelCase(k));
                        }
                        if (address == null) {
                            address = meisaiAddresses.get(dataName + "." + k);
                        }
                        if (address != null) {
                            int r = currentFirstRowIndex + address.getRow() - range.getBoR();
                            setCellValue(sheet, r, address.getColumn(), v);
                        }
                    }

                    ++meisaiIndex;
                }

            } else if (data instanceof Map) {
                // 単票系エクセル

                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data;

                for (Entry<String, Object> field : map.entrySet()) {
                    String fieldName = field.getKey();
                    Object fieldValue = field.getValue();

                    if (fieldValue instanceof List) {
                        // Listがネストしている場合

                        Map<String, Object> nestMap = new HashMap<String, Object>();
                        nestMap.put(fieldName, fieldValue);
                        print(sheet, nestMap);

                    } else if (fieldValue instanceof Map) {
                        // Mapがネストしている場合

                        Map<String, Object> nestMap = new HashMap<String, Object>();
                        nestMap.put(fieldName, fieldValue);
                        print(sheet, nestMap);

                    } else {
                        // ListでもMapでもない場合

                        // ヘッダ項目と明細項目のプレースホルダのアドレスを初期化
                        Map<String, CellAddress> headerAddresses = new HashMap<String, CellAddress>();

                        // レイアウトシートの使用範囲内で、行・列ループして、各プレースホルダのアドレスを取得
                        for (int r = 0; r <= sheet.getLastRowNum(); r++) {
                            Row row = sheet.getRow(r);
                            if (row == null) {
                                continue;
                            }
                            for (int c = 0; c < row.getLastCellNum(); c++) {
                                Cell cell = CellUtil.getCell(row, c);
                                Object o = XlsxUtil.getCellValue(cell);
                                if (o == null) {
                                    continue;
                                }

                                String s = String.valueOf(o);
                                if (s.matches("^\\{\\{.+\\}\\}$")) {

                                    // ヘッダ項目の座標を退避
                                    String key = s.replaceAll("^\\{\\{|\\}\\}$", "");
                                    headerAddresses.put(key, new CellAddress(r, c));
                                }
                            }
                        }

                        CellAddress address = headerAddresses.get(fieldName);
                        if (address == null) {
                            address = headerAddresses.get(dataName + "." + fieldName);
                        }
                        if (address != null) {
                            setCellValue(sheet, address, fieldValue);
                        }
                    }
                }
            }
        }
    }

    /**
     * @param sheet 対象シート
     * @param meisaiAddresses 「プレースホルダ：セル」のマップ
     * @param prefix プレースホルダの接頭辞
     * @return セル範囲
     */
    private static Range getRange(final Sheet sheet, final Map<String, CellAddress> meisaiAddresses,
            final String prefix) {

        String prefixText = "";
        if (prefix != null) {
            prefixText = prefix + "\\.";
        }

        Range range = new Range();

        for (Integer r = 0; r <= sheet.getLastRowNum(); r++) {

            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }

            for (int c = 0; c < row.getLastCellNum(); c++) {
                Cell cell = CellUtil.getCell(row, c);
                Object o = XlsxUtil.getCellValue(cell);
                if (o == null) {
                    continue;
                }

                String s = String.valueOf(o);
                //                LOG.debug("    " + s);
                if (s.matches("^\\[\\[" + prefixText + ".+\\]\\]$") /*|| s.matches("^\\[\\[.+\\]\\]$")*/) {

                    // 明細項目の座標を退避
                    String key = s.replaceAll("^\\[\\[|\\]\\]$", "");
                    meisaiAddresses.put(key, new CellAddress(r, c));

                    // 明細定義域を更新
                    range.setBoR(r);
                    range.setEoR(r);
                    range.setBoC(c);
                    range.setEoC(c);
                }
            }

            //            LOG.debug("    last:[ r: " + sheet.getLastRowNum() + ", c: " + row.getLastCellNum() + " ]");
        }

        if (range.getBoR() == Integer.MAX_VALUE || range.getBoC() == Integer.MAX_VALUE || range.getEoR() == 0
                || range.getEoC() == 0) {
            //throw new SysError("error.notexist", prefix + " range");
            return null;
        }

        return range;
    }

    /**
     * @param workbook ワークブック
     * @param fileBaseMei 基底ファイル名
     * @param extension 拡張子
     * @return 作成した一時ファイルパス
     */
    private static File write(final Workbook workbook, final String fileBaseMei, final String extension) {

        String tmp = App.get("tempVirtualDir");
        LOG.debug(tmp);
        if (tmp == null) {
            tmp = "/temp";
        }

        if (!FileUtil.get(tmp).exists()) {
            FileUtil.get(tmp).mkdirs();
        }

        // 保存ファイルパス
        String timestamp = DateTimeUtil.ymdhmsS();
        String writeFileName = fileBaseMei + "." + timestamp + "." + extension;
        String writeFilePath = tmp + sep + writeFileName;
        File file = FileUtil.get(writeFilePath);

        // エクセルファイルを保管
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        } catch (Exception e) {
            throw new SysError(e);
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                throw new SysError(e);
            }
        }

        return file;
    }

    /**
     * @param file ファイル
     * @return ファイルをワークブックとして取得
     */
    private static Workbook file2Workbook(final File file) {
        try {
            return new XSSFWorkbook(file);
        } catch (Exception e) {
            throw new SysError(e);
        }
    }

    /**
     * @param cell 対象セル
     * @return セル値
     */
    private static Object getCellValue(final Cell cell) {
        return getCellValue(cell, cell.getCellType());
    }

    /**
     * @param cell 対象セル
     * @param type セルのタイプ
     * @return セル値
     */
    private static Object getCellValue(final Cell cell, final CellType type) {
        switch (type) {
        case BLANK:
            return null;
        case BOOLEAN:
            return cell.getBooleanCellValue();
        case ERROR:
            return null;
        case FORMULA:
            return getCellValue(cell, cell.getCachedFormulaResultType());
        case NUMERIC:
            if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue();
            } else {
                return cell.getNumericCellValue();
            }
        default:
            return cell.getRichStringCellValue().getString();
        }
    }

    /**
     * @param sheet 対象シート
     * @param address セルアドレス
     * @param o セルに設定する値
     */
    private static void setCellValue(final Sheet sheet, final CellAddress address, final Object o) {
        setCellValue(sheet, address.getRow(), address.getColumn(), o);
    }

    /**
     * @param sheet 対象シート
     * @param r 対象行
     * @param c 対象列
     * @param o セルに設定する値
     */
    private static void setCellValue(final Sheet sheet, final int r, final int c, final Object o) {
        Row row = sheet.getRow(r);
        if (row == null) {
            row = sheet.createRow(r);
        }
        Cell cell = row.getCell(c);
        if (cell == null) {
            cell = row.createCell(c);
        }
        setCellValue(cell, o);
    }

    /**
     * @param cell 対象セル
     * @param o セルに設定する値
     */
    private static void setCellValue(final Cell cell, final Object o) {
        if (Objects.nonNull(o)) {
            if (o instanceof Number) {
                cell.setCellValue(Double.valueOf(o.toString()));
            } else if (o instanceof Date) {
                cell.setCellValue((Date) o);
            } else {
                cell.setCellValue(o.toString());
            }
        } else {
            cell.setCellValue("");
        }
    }

    /**
     * セル範囲のコピー
     * @param sheet 対象シート
     * @param range 対象セル範囲
     * @param destRowIndex コピー先の行インデクス
     */
    private static void copyRange(final Sheet sheet, final Range range, final int destRowIndex) {

        LOG.debug("    BoR: " + range.getBoR());
        LOG.debug("    EoR: " + range.getEoR());
        LOG.debug("    destRowIndex: " + destRowIndex);

        // 今回コピーする開始行から終了行まで１行ずつループ
        for (int r = range.getBoR(); r <= range.getEoR(); r++) {

            // コピー対象の行数を取得
            int rownum = destRowIndex + r - range.getBoR();

            // コピー先の行を取得
            Row destRow = sheet.getRow(rownum);

            // 最終行でなければ１行ずらす
            if (rownum <= sheet.getLastRowNum()) {
                sheet.shiftRows(rownum, sheet.getLastRowNum(), 1, true, true);
            }

            // 改めてコピー先の行を作成
            destRow = sheet.createRow(rownum);

            // コピー開始列から終了列までループして書式コピー
            for (int c = range.getBoC(); c <= range.getEoC(); c++) {
                Cell srcCell = sheet.getRow(r).getCell(c);
                Cell destCell = destRow.createCell(c);
                destCell.setCellStyle(srcCell.getCellStyle());
            }
        }
    }

}
