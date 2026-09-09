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
package jp.co.golorp.emarf;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import jp.co.golorp.emarf.generator.BeanGenerator;
import jp.co.golorp.emarf.util.ResourceBundles;

/**
 * Hello world!
 *
 */
public final class App {

    /**
     * エントリポイント
     *
     * @param args コマンドライン引数
     */
    public static void main(final String[] args) {

        //プロジェクトパスを取得
        Path currentPath = Paths.get("");

        //パスの文字列を取得
        String absolutePath = currentPath.toAbsolutePath().toString();

        //ソースパスにプロジェクトパスのリソースフォルダを追加
        ResourceBundles.getSrcPaths().add(absolutePath + File.separator + "src\\main\\resources");

        //ジェネレータ起動
        BeanGenerator.generate(absolutePath);
    }

    /**
     * プライベートコンストラクタ
     */
    private App() {
    }
}
