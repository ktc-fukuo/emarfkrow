# Quick Start

**主にeclipseを想定した開始方法について記載します。**

---

## リポジトリのクローン

emarfkrow本体とサンプルアプリケーションをcloneします。  
eclipse内のGUIで行っても問題ありません。

```
git clone https://github.com/emarfkrow/emarfkrow.git
git clone https://github.com/emarfkrow/sample.git
```

各リポジトリ内からプロジェクトをインポートします。

- emarf-com：システム共通
- emarf-job：バッチ共通
- emarf-web：ウェブ共通
- blank    ：ZERO-Config版
- samplecom：サンプル共通（自動生成させるプロジェクト）
- samplejob：バッチサンプル
- sampleweb：ウェブサンプル

プロジェクトの依存関係はMavenのPOMファイルで設定しており  
以下の通りになっています。

```
emarf-com
├ emarf-job ┐
└ emarf-web ┴ samplecom
                ├ samplejob
                └ sampleweb
```

blankプロジェクトはデータソースのみ任意のDBに設定してjp.co.golorp.emarf.Appを実行すればすぐに動くものが確認できます。

```
emarf-com
└ emarf-web ─ blank
```


---

## サンプルデータベースの構築

sampleリポジトリ.schemaフォルダのDDLでサンプル用のデータベースを構築します。

- [DB名]/create_database.sql
- [DB名]/create_tables.sql

最低限必要なデータをロードします。

- MHR_SHOKUI_NINKA.xlsx：システム管理者に全権を与えます。
- MHR_USER.xlsx：システム管理者を登録します。
- MHR_USER_POS.xlsx：システム管理者の所属を設定します。
- MSY_KBN_VAL.xlsx：選択項目用の区分を設定します。

---

## 自動生成とデバッグ実行

samplecom内のsrc/main/resources/DataSource.propertiesを配置し、  
samplecom内のApp.javaを「Javaアプリケーション」で実行すると、  
samplecom内のsrc/main内に各種ファイルを出力します。

samplewebをTomcat10_Java17で「サーバーでデバッグ」すると、  
ブラウザにログイン画面が表示されます。

- ログインID：0
- パスワード：password
