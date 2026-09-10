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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.golorp.emarf.properties.App;

/**
 * PdfUtil
 */
public final class PdfUtil {

    /** logger */
    private static final Logger LOG = LoggerFactory.getLogger(PdfUtil.class);

    /** libreoffice */
    private static final String LIBRE_PATH = App.get("librePath");

    /** デフォルトコンストラクタ */
    private PdfUtil() {
    }

    /**
     * @param xlsPath
     * @return PDFファイルパス
     * @throws IOException
     * @throws InterruptedException
     */
    public static String byXlsx(final String xlsPath) {

        // 出力先ディレクトリは入力ファイルと同じ場所
        File inputFile = new File(xlsPath);
        String outDir = inputFile.getParent();

        return byXlsx(xlsPath, outDir);
    }

    /**
     * @param xlsPath
     * @param outDir
     * @return PDFファイルパス
     * @throws IOException
     * @throws InterruptedException
     */
    public static String byXlsx(final String xlsPath, final String outDir) {

        // コマンド引数の組み立て
        // 引数にスペースが含まれる場合を考慮し、リストで渡すのがJavaの安全なベストプラクティスです
        List<String> command = new ArrayList<>();
        command.add(LIBRE_PATH);
        command.add("--headless");
        command.add("--convert-to");
        command.add("pdf");
        command.add(xlsPath);
        command.add("--outdir");
        command.add(outDir);

        // ProcessBuilderの設定（C#のProcessStartInfoに相当）
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        // C#の RedirectStandardOutput / Error に相当
        // ログを Java の標準出力・エラー出力にそのまま流す設定（不要なら削除、またはファイルを指定してください）
        processBuilder.inheritIO();

        int exitCode = 0;
        try {
            // プロセスの開始
            Process process = processBuilder.start();
            // プロセスの終了を待機（C#の process.WaitForExit() に相当）
            exitCode = process.waitFor();
        } catch (IOException e) {
            LOG.warn(e.getMessage());
        } catch (InterruptedException e) {
            LOG.warn(e.getMessage());
        }

        // 出力されるPDFのファイル名を組み立て
        File inputFile = new File(xlsPath);
        String inputFileName = inputFile.getName();
        String baseName = inputFileName.contains(".")
                ? inputFileName.substring(0, inputFileName.lastIndexOf('.'))
                : inputFileName;

        File pdfFile = new File(outDir, baseName + ".pdf");
        String pdfPath = pdfFile.getAbsolutePath();

        // ファイルの存在確認
        if (pdfFile.exists()) {
            return pdfPath;
        }

        throw new RuntimeException("出力PDFファイルが見つかりません。LibreOfficeの出力を確認してください。終了コード: " + exitCode + ", パス: " + pdfPath);
    }

}
