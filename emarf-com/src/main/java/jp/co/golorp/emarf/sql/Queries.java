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

package jp.co.golorp.emarf.sql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.golorp.emarf.entity.IEntity;
import jp.co.golorp.emarf.exception.NoDataError;
import jp.co.golorp.emarf.exception.OptLockError;
import jp.co.golorp.emarf.exception.SysError;
import jp.co.golorp.emarf.io.FileUtil;
import jp.co.golorp.emarf.lang.StringUtil;
import jp.co.golorp.emarf.properties.App;
import jp.co.golorp.emarf.util.MapList;

/**
 * SQL実行クラス
 *
 * @author golorp
 */
public final class Queries {

    /** logger */
    private static final Logger LOG = LoggerFactory.getLogger(Queries.class);

    /** セパレータ */
    private static final String SEP = File.separator;

    /** プライベートコンストラクタ */
    private Queries() {
    }

    /**
     * 名前付きパラメータでエンティティを１件取得
     * @param <T> 返却クラス
     * @param namedSql 名前付きパラメータSQL
     * @param params   パラメータのマップ
     * @param c        返却クラスに反映するエンティティクラス
     * @return T 返却クラス
     */
    public static <T> T get(final String namedSql, final Map<String, Object> params, final Class<?> c) {

        List<T> list = Queries.select(namedSql, params, c, null, null);

        if (list == null || list.size() == 0) {
            throw new NoDataError("info.nodata");
        }

        if (list == null || list.size() > 1) {
            throw new NoDataError("info.duplicate");
        }

        T t = (T) list.get(0);
        return t;
    }

    /**
     * アドホッククエリで検索
     * @param adhocSql アドホッククエリ
     * @param rows     ページ行数
     * @param page     ページ番号
     * @return 検索結果の{@link MapList}
     */
    public static MapList select(final String adhocSql, final Integer rows, final Integer page) {
        return Queries.select(adhocSql, null, null, rows, page);
    }

    /**
     * 名前付きパラメータSQLで検索
     * @param namedSql 名前付きパラメータSQL
     * @param params   パラメータのマップ
     * @param rows     ページ行数
     * @param page     ページ番号
     * @return 検索結果の{@link MapList}
     */
    public static MapList select(final String namedSql, final Map<String, Object> params, final Integer rows,
            final Integer page) {
        return Queries.select(namedSql, params, null, rows, page);
    }

    /**
     * 名前付きパラメータSQLで検索
     * @param <T>      返却クラス
     * @param namedSql 名前付きパラメータSQL
     * @param params   パラメータのマップ
     * @param c        返却クラスに反映するエンティティクラス
     * @param rows     ページ行数
     * @param page     ページ番号
     * @return エンティティクラスのリスト
     */
    public static <T> T select(final String namedSql, final Map<String, Object> params, final Class<?> c,
            final Integer rows, final Integer page) {

        Map<String, Object> newParams = new HashMap<String, Object>();

        // 参照系のSQLはparamsの値が""ならキー自体を消し込む
        if (params != null) {
            for (Entry<String, Object> entry : params.entrySet()) {
                String k = entry.getKey();
                Object v = entry.getValue();
                if (!StringUtil.isNullOrWhiteSpace(v)) {
                    // 参照系のSQLは「モデル名.フィールド名」をフィールド名だけにする
                    k = k.replaceAll("^.+\\.", "");
                    newParams.put(k, v);
                }
            }
        }

        List<Object> args = new ArrayList<Object>();

        String sql = convRawSql(namedSql, newParams, args, rows, page);

        // id列を付与（SlickGridのDataView対応）
        String idSql = sql;
        //if (params == null && rows == null && page == null) {
        if (rows == null) {
            LOG.trace("skip ided process.");
        } else {
            String sql2 = sql.replaceAll("^[\\s\\t]*", ""); // 行頭の空白文字削除
            sql2 = sql2.replaceAll("--[^\\r\\n]*", ""); // １行コメント削除
            sql2 = sql2.replaceAll("[\\r\\n]", ""); // 改行文字削除
            sql2 = sql2.replaceAll("\\/\\*[^\\*]*\\*\\/", ""); // ブロックコメント削除
            if (sql2.startsWith("SELECT")) {
                DataSourcesAssist assist = DataSources.getAssist();
                idSql = assist.addIdColumn(sql);
            }
        }

        if (rows != null && rows > 0) {

            // 上限なしの件数を取得
            MapList data = Queries.selectByRawSql("SELECT COUNT(*) AS \"TOTAL_ROWS\" FROM (" + idSql + ") SUB", args,
                    null);
            Map<String, Object> item = data.get(0);
            Integer totalRows = Integer.valueOf(item.get("TOTAL_ROWS").toString());
            params.put("totalRows", totalRows);

            // ページング
            DataSourcesAssist assist = DataSources.getAssist();
            idSql = assist.getPagedSql(idSql, rows, page);
        }

        return selectByRawSql(idSql, args, c);
    }

    /**
     * 名前付きパラメータをプレースホルダに変換し、プレースホルダの出現箇所に応じてパラメータのMapをList化
     * @param namedSql     名前付きパラメータSQL
     * @param params       パラメータのマップ
     * @param repackedArgs プレースホルダの出現順に詰め直したパラメータ値のリスト
     * @param rows         ページ行数
     * @param page         ページ番号
     * @return プレースホルダに変換後のSQL
     */
    private static String convRawSql(final String namedSql, final Map<String, Object> params,
            final List<Object> repackedArgs, final Integer rows, final Integer page) {

        Map<String, Object> snakes = new HashMap<String, Object>();

        // paramsのキーをsnake化
        if (params != null) {
            for (Entry<String, Object> e : params.entrySet()) {
                String k = e.getKey();
                Object v = e.getValue();
                snakes.put(StringUtil.toSnakeCase(k), v);
            }
        }

        // SQL
        String rawSql = namedSql;
        String logSql = namedSql;

        // SQL内の、名前付きパラメータ「:***」を、SQLから削除
        Matcher m = Pattern.compile("(?<=:)[A-Za-z][0-9A-Z\\_a-z]+").matcher(rawSql);
        while (m.find()) {
            String parameterName = m.group();

            if (parameterName.equals("MI") || parameterName.equals("SS")) {
                continue;
            }

            String blockRE = ".*\\/\\* *:" + parameterName + " *\\*\\/(\\r|\\n|.)+\\/\\* *:" + parameterName
                    + " *\\*\\/[\\r\\n]+";
            Pattern p = Pattern.compile(blockRE);

            if (!snakes.containsKey(parameterName)) {
                // パラメータのキーに含まれない場合

                // ブロック削除
                //                rawSql = rawSql.replaceFirst(blockRE, "");
                //                logSql = logSql.replaceFirst(blockRE, "");
                rawSql = p.matcher(rawSql).replaceFirst("");
                logSql = p.matcher(logSql).replaceFirst("");

                // １行削除
                rawSql = rawSql.replaceFirst(".*:" + parameterName + "[^_\r\n]*([\r\n]+|$)", "");
                logSql = logSql.replaceFirst(".*:" + parameterName + "[^_\r\n]*([\r\n]+|$)", "");

                // データが条件に後方一致する検索の対応
                rawSql = rawSql.replaceFirst(".*:" + parameterName + "[^_\r\n]+LIKE[^\r\n]+([\r\n]+|$)", "");
                logSql = logSql.replaceFirst(".*:" + parameterName + "[^_\r\n]+LIKE[^\r\n]+([\r\n]+|$)", "");

            } else if (snakes.get(parameterName) == null) {
                // パラメータ値がない場合

                String simplesql = rawSql.replaceAll("/(\\/\\*.+\\*\\/|--[^\\r\\n]+)/", "").toUpperCase();
                int insertIndex = simplesql.indexOf("INSERT");
                int updateIndex = simplesql.indexOf("UPDATE");
                int deleteIndex = simplesql.indexOf("DELETE");
                if (insertIndex < 0 && updateIndex < 0 && deleteIndex < 0) {

                    // ブロック削除
                    rawSql = p.matcher(rawSql).replaceFirst("");
                    logSql = p.matcher(logSql).replaceFirst("");

                    // １行削除
                    rawSql = rawSql.replaceFirst(".*:" + parameterName + "[^_\r\n]*([\r\n]+|$)", "");
                    logSql = logSql.replaceFirst(".*:" + parameterName + "[^_\r\n]*([\r\n]+|$)", "");
                }
            }
        }

        // SQLコメント内の名前付きパラメータ「-- :***」を、パラメータ値で置換
        m = Pattern.compile("(?<=--) *:[A-Za-z][0-9A-Z\\_a-z]+").matcher(rawSql);
        while (m.find()) {
            String namedParameter = m.group();
            String parameterName = namedParameter.replaceAll("^ *:", "");

            if (parameterName.equals("MI") || parameterName.equals("SS")) {
                continue;
            }

            String parameterValue = "";

            Object o = snakes.get(parameterName);

            if (o != null) {
                parameterValue = o.toString();
                // コメント内に「:」が残らないように「_」で置換
                parameterValue = parameterValue.replaceAll(":", "_");
            }

            rawSql = rawSql.replaceFirst(":" + parameterName, parameterValue);
            logSql = logSql.replaceFirst(":" + parameterName, parameterValue);
        }

        // SQL内の名前付きパラメータ「:***」をプレースホルダ「?」に置換して、出現箇所に応じてパラメータを追加
        m = Pattern.compile("(?<=:)[A-Za-z][0-9A-Z\\_a-z]+").matcher(rawSql);
        while (m.find()) {
            String parameterName = m.group();

            if (parameterName.equals("MI") || parameterName.equals("SS")) {
                continue;
            }

            Object o = snakes.get(parameterName);

            if (o instanceof List) {

                @SuppressWarnings("unchecked")
                List<String> list = (List<String>) o;
                String ss = "";
                for (String s : list) {
                    if (!ss.equals("")) {
                        ss += ", ";
                    }
                    ss += "'" + s.replaceAll("'", "’") + "'";
                }
                rawSql = rawSql.replaceFirst(":" + parameterName, ss);
                logSql = logSql.replaceFirst(":" + parameterName, ss);

            } else {

                // 実行用SQLとログ用SQLの名前付きパラメータ「:***」をプレースホルダ「?」に置換
                rawSql = rawSql.replaceFirst(":" + parameterName, "?");
                if (o == null) {
                    logSql = logSql.replaceFirst(":" + parameterName, "''");
                } else {
                    logSql = logSql.replaceFirst(":" + parameterName, "'" + o.toString() + "'");
                }

                // LocalDateTimeのまま渡すと日付リテラルエラーを起こす
                if (o instanceof LocalDateTime) {
                    o = o.toString();
                }

                // 出現箇所に応じてパラメータを追加
                repackedArgs.add(o);
            }
        }

        // ログ用SQLを出力
        LOG.debug("\r\n\r\n" + logSql + "\r\n");

        return rawSql;
    }

    /**
     * プレースホルダのSQLを実行
     * @param <T> 返却クラス
     * @param sql プレースホルダSQL
     * @param args プレースホルダ値のリスト
     * @param c 返却クラスに反映するエンティティクラス
     * @return エンティティクラスのリストか{@link MapList}
     */
    private static <T> T selectByRawSql(final String sql, final List<Object> args, final Class<?> c) {

        // プリペアードステートメント取得
        try (PreparedStatement ps = Connections.getStatement(sql);) {

            // パラメータを設定
            for (int i = 0; i < args.size(); i++) {
                ps.setObject(i + 1, args.get(i));
            }

            // 戻り値の型指定があり、ResultSetが複数件の場合の返却値
            List<Object> objectList = new ArrayList<Object>();

            // 戻り値の型指定がない場合の返却値
            MapList mapList = new MapList();

            // SQLを発行してResultSetを取得
            ResultSet rs = ps.executeQuery();

            // カラム名取得用にメタデータを取得
            ResultSetMetaData metaData = rs.getMetaData();

            // ResultSetでループ
            while (rs.next()) {
                if (c != null) {
                    // 戻り値の型指定がある場合
                    objectList.add(rs2Entity(rs, c));
                } else {
                    // 戻り値の型指定がない場合
                    Map<String, Object> map = new LinkedHashMap<String, Object>();
                    int columnCount = metaData.getColumnCount();
                    for (int i = 0; i < columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i + 1);
                        if (!columnName.equals("id")) {
                            columnName = columnName.toUpperCase();
                        }
                        Object value = rs.getObject(i + 1);
                        map.put(columnName, value);
                    }
                    mapList.add(map);
                }
            }

            if (!objectList.isEmpty()) {
                @SuppressWarnings("unchecked")
                T t = (T) objectList;
                return t;
            }

            if (!mapList.isEmpty()) {
                @SuppressWarnings("unchecked")
                T t = (T) mapList;
                return t;
            }

            return null;

        } catch (Exception e) {
            throw new SysError(e);
        }
    }

    /** クラスキャッシュ */
    private static ConcurrentHashMap<String, Class<?>> classCache = new ConcurrentHashMap<String, Class<?>>();

    /**
     * {@link ResultSet}をエンティティクラスのリストに変換
     * @param <T> 返却クラス
     * @param rs {@link ResultSet}
     * @param c エンティティクラス
     * @return エンティティクラスのリスト
     * @throws Exception
     */
    private static <T> T rs2Entity(final ResultSet rs, final Class<?> c) throws Exception {

        // エンティティのインスタンスを取得
        @SuppressWarnings("unchecked")
        T t = (T) c.getDeclaredConstructor().newInstance();

        // エンティティのフィールドを取得してループ
        Field[] fields = c.getDeclaredFields();
        for (Field field : fields) {

            // 子モデルならスキップ
            Type fieldType = field.getType();
            if (fieldType == List.class) {
                continue;
            }

            // 兄弟モデルならスキップ
            String typeName = fieldType.getTypeName();
            Class<?> fieldClass = null;
            if (classCache.containsKey(typeName)) {
                fieldClass = classCache.get(typeName);
            } else {
                fieldClass = Class.forName(typeName);
                classCache.put(typeName, fieldClass);
            }
            Class<?>[] interfaces = fieldClass.getInterfaces();
            if (interfaces.length > 0 && interfaces[0] == IEntity.class) {
                continue;
            }

            //            // フィールドを書き込み可に設定
            //            field.setAccessible(true);

            // ResultSetから値取得
            String fieldName = field.getName();
            String snake = StringUtil.toSnakeCase(fieldName);
            Object o = null;
            try {
                o = rs.getObject(snake);
            } catch (Exception e) {
                try {
                    // 数字の前の「_」を消して取ってみる
                    o = rs.getObject(snake.replaceAll("\\_([0-9]+)", "$1"));
                } catch (Exception e1) {
                    try {
                        // 数字の後ろの「_」を消して取ってみる
                        o = rs.getObject(snake.replaceAll("([0-9]+)\\_", "$1"));
                    } catch (Exception e2) {
                        try {
                            // ケバブケースでも取ってみる
                            o = rs.getObject(snake.replaceAll("\\_", "-"));
                        } catch (Exception e3) {
                            LOG.trace(e.getMessage() + "[" + snake + "]");
                        }
                    }
                }
            }

            Class<?> columnClass = null;
            if (o != null) {
                columnClass = o.getClass();
            }
            if (fieldType == LocalDateTime.class && columnClass == Timestamp.class) {
                o = ((Timestamp) o).toLocalDateTime();
            }

            String pascal = StringUtil.toPascalCase(fieldName);
            Method setter = c.getDeclaredMethod("set" + pascal, Object.class);
            try {
                setter.invoke(t, o);
            } catch (Exception e) {
                throw new SysError(e);
            }
        }

        return t;
    }

    /**
     * 実行パスから遡ってSQLファイル内容を取得
     * @param sqlPathes 相対パスでsqlファイルを探す場合に使用
     * @param c クラスローダでsqlファイルを探す場合に使用
     * @param sqlName SQLファイル名
     * @return SQLファイル内容
     */
    public static String loadSqlFile(final List<String> sqlPathes, final Class<?> c, final String sqlName) {

        String s = null;

        // サーブレットのsqlフォルダからの相対パスで取得
        s = Queries.loadSqlFile(sqlPathes, sqlName);
        if (s != null) {
            return s;
        }

        // サーブレットコンテキスト内のSQLファイルをロード
        s = Queries.loadSqlFile(c, sqlPathes, sqlName);
        if (s != null) {
            return s;
        }

        // 参照jarを含め、クラスパス内のsqlフォルダからのパッケージ相対位置で取得
        s = Queries.loadSqlFile(c, sqlName);
        if (s != null) {
            return s;
        }

        throw new SysError("error.notexist.file", String.join("\\\\", sqlPathes) + "\\\\" + sqlName + ".sql");
    }

    /**
     * サーブレットコンテキスト内のSQLファイルをロード
     * @param c 呼び出し元のクラス
     * @param sqlPathes サーブレットコンテキストからのパス
     * @param sqlName SQLファイル名
     * @return SQLファイル内容
     */
    private static String loadSqlFile(final Class<?> c, final List<String> sqlPathes, final String sqlName) {

        File file = getExternalPath(c, sqlPathes, sqlName);

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\r\n");
                }
                return sb.toString();
            } catch (IOException e) {
                LOG.trace(e.getMessage());
            }
        }

        return null;
        //throw new SysError("error.notexist.file", sqlName + ".sql");
    }

    /**
     * クラスパス内のSQLファイルをロード
     * @param c 呼び出し元のクラス
     * @param sqlName SQLファイル名
     * @return SQLファイル内容
     */
    private static String loadSqlFile(final Class<?> c, final String sqlName) {

        // スレッドのクラスローダー
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        // 自クラスのクラスローダー
        if (cl == null) {
            cl = c.getClassLoader();
        }

        // システムクラスローダー
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }

        URL url = seekSqlFile(cl, c.getPackage().getName(), sqlName);
        if (url != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\r\n");
                }
                return sb.toString();
            } catch (IOException e) {
                LOG.trace(e.getMessage());
            }
        }

        return null;
        //throw new SysError("error.notexist.file", sqlName + ".sql");
    }

    /**
     * パッケージを後方一致で辿り、クラスパス内のSQLファイルを検索
     * @param cl クラスローダ
     * @param packageName 呼び出し元クラスのパッケージ（先頭から減らしていく）
     * @param sqlName SQLファイル名
     * @return SQLファイルのURL
     */
    private static URL seekSqlFile(final ClassLoader cl, final String packageName, final String sqlName) {

        String[] packages = packageName.split("\\.");

        String packagePath = String.join(SEP, packages);

        String resourcePath = "sql" + SEP + packagePath + SEP + sqlName + ".sql";

        URL url = cl.getResource(resourcePath);

        if (url == null && packages[0].length() > 0) {
            List<String> packageList = new ArrayList<>(Arrays.asList(packages));
            packageList.remove(0);
            packages = packageList.toArray(new String[packageList.size()]);
            url = seekSqlFile(cl, String.join(".", packages), sqlName);
        }

        return url;
    }

    /**
     * サーブレットコンテキスト内のSQLファイルをロード
     * @param sqlPathes サーブレットコンテキストからのパス
     * @param sqlName SQLファイル名
     * @return SQLファイル内容
     */
    private static String loadSqlFile(final List<String> sqlPathes, final String sqlName) {

        File file = seekSqlFile(sqlPathes, sqlName);

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\r\n");
                }
                return sb.toString();
            } catch (IOException e) {
                LOG.trace(e.getMessage());
            }
        }

        return null;
        //throw new SysError("error.notexist.file", sqlName + ".sql");
    }

    /**
     * パスを後方一致で辿り、サーブレットコンテキスト内のSQLファイルを検索
     * @param sqlPathes サーブレットコンテキストからのパス（先頭から減らしていく）
     * @param sqlName SQLファイル名
     * @return SQLファイル
     */
    private static File seekSqlFile(final List<String> sqlPathes, final String sqlName) {

        String sqlDir = App.get("sqlVirtualDir");
        if (sqlDir == null) {
            sqlDir = "/WEB-INF/sql";
        }

        String sqlPath = String.join(SEP, sqlPathes);
        String filePath = sqlDir + SEP + sqlPath + SEP + sqlName + ".sql";
        File file = FileUtil.get(filePath);

        if (!file.exists() && sqlPathes.size() > 0) {
            List<String> copy = new ArrayList<>(sqlPathes);
            copy.remove(0);
            file = seekSqlFile(copy, sqlName);
        }

        return file;
    }

    /**
     * @param c
     * @param sqlPathes
     * @param sqlName
     * @return Path
     * @throws URISyntaxException
     */
    public static File getExternalPath(final Class<?> c, final List<String> sqlPathes, final String sqlName) {

        String sqlPath = "sql" + SEP + String.join(SEP, sqlPathes);

        // 1. このクラスがロードされている場所（JARまたはクラスファイル）を取得
        URL location = c.getProtectionDomain().getCodeSource().getLocation();

        try {
            URI uri = null;
            uri = location.toURI();

            // 2. URIをFileに変換（パスのエンコード問題を回避）
            String uriPath = uri.getRawPath().toString();
            uriPath = uriPath.replace("/target/classes/", "/");
            File jarOrClassFile = new File(uriPath);

            // 3. 親ディレクトリを取得（これがJARのあるディレクトリ）
            File jarDir = jarOrClassFile;
            if (jarOrClassFile.toString().endsWith(".jar")) {
                jarDir = jarOrClassFile.getParentFile();
            }

            // 4. そこから相対パスで目的のファイルにアクセス
            Path path = jarDir.toPath().resolve(sqlPath).resolve(sqlName + ".sql");

            File file = path.toFile();

            if (!file.exists() && sqlPathes.size() > 0) {
                List<String> copy = new ArrayList<>(sqlPathes);
                copy.remove(0);
                file = seekSqlFile(copy, sqlName);
            }

            return file;
        } catch (URISyntaxException e) {
            LOG.trace(e.getMessage());
        }

        return null;
    }

    /**
     * 名前付きパラメータSQLで登録
     * @param namedSql 名前付きパラメータSQL
     * @param params パラメータのマップ
     * @return 登録件数
     */
    public static int regist(final String namedSql, final Map<String, Object> params) {

        List<Object> args = new ArrayList<Object>();

        String sql = Queries.convRawSql(namedSql, params, args, null, null);

        if (sql.equals("")) {
            return 0;
        }

        return Queries.registByRawSql(sql, args);
    }

    /**
     * プレースホルダのSQLで登録
     * @param sql プレースホルダSQL
     * @param args プレースホルダ値
     * @return 登録件数
     */
    private static int registByRawSql(final String sql, final List<Object> args) {

        // プリペアードステートメント取得
        try (PreparedStatement ps = Connections.getStatement(sql);) {

            // パラメータを設定
            for (int i = 0; i < args.size(); i++) {
                if (args.get(i) instanceof java.time.LocalDate) {
                    ps.setObject(i + 1, args.get(i).toString());
                } else {
                    ps.setObject(i + 1, args.get(i));
                }
            }

            // SQLを発行して登録件数を取得
            return ps.executeUpdate();

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new OptLockError("error.cant.insert", e);
        } catch (Exception e) {
            // Tibero対応
            if (e.getMessage().contains("UNIQUE constraint violation")) {
                throw new OptLockError("error.cant.insert", e);
            }
            throw new SysError(e);
        }
    }

}
