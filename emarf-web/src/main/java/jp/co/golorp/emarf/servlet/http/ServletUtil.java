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

package jp.co.golorp.emarf.servlet.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jp.co.golorp.emarf.action.BaseAction;
import jp.co.golorp.emarf.exception.SysError;
import jp.co.golorp.emarf.generator.BeanGenerator;
import jp.co.golorp.emarf.lang.StringUtil;
import jp.co.golorp.emarf.properties.App;
import jp.co.golorp.emarf.servlet.LoginFilter;
import jp.co.golorp.emarf.time.DateTimeUtil;

/**
 * サーブレット用ユーティリティ
 *
 * @author golorp
 */
public final class ServletUtil {

    /** logger */
    private static final Logger LOG = LoggerFactory.getLogger(ServletUtil.class);

    /** servletUrl */
    private static String servletUrl;

    /**
     * @return servletUrl
     */
    public static String getServletUrl() {
        return servletUrl;
    }

    /**
     * @param s
     */
    public static void setServletUrl(final String s) {
        servletUrl = s;
    }

    /** プライベートコンストラクタ */
    private ServletUtil() {
    }

    /**
     * @param request
     * @return BaseAction
     */
    public static BaseAction getAction(final HttpServletRequest request) {

        // サーブレットパスを文字列配列に取得
        String servletPath = request.getServletPath();
        String[] servletPathes = servletPath.split("/");

        // 最後のパスの拡張子部分を「Action」に置換
        String lastPath = servletPathes[servletPathes.length - 1];
        String actionName = lastPath.replaceFirst(".[a-z]+$", "") + "Action";
        servletPathes[servletPathes.length - 1] = actionName;

        BaseAction a = extracted(request, servletPathes, actionName);

        return a;
    }

    /**
     * @param request
     * @param actionName
     * @return BaseAction
     */
    public static BaseAction getAction(final HttpServletRequest request, final String actionName) {

        BaseAction a = extracted(request, null, actionName);

        return a;
    }

    /**
     * @param request
     * @param servletPathes
     * @param actionName
     * @return BaseAction
     */
    private static BaseAction extracted(final HttpServletRequest request, final String[] servletPathes,
            final String actionName) {

        BaseAction a = null;

        try {

            // リクエストに則って、拡張アクションを取ってみる
            String className = BeanGenerator.PKG_A;
            if (servletPathes != null) {
                className += String.join(".", servletPathes);
            }
            a = (BaseAction) (Class.forName(className)).getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            try {

                // モデルパッケージからも、拡張アクションを取ってみる
                String className = BeanGenerator.PKG_A + ".model." + actionName;
                a = (BaseAction) (Class.forName(className)).getDeclaredConstructor().newInstance();

            } catch (Exception e1) {
                try {

                    // モデルのベースパッケージからも、基底アクションを取ってみる
                    String className = BeanGenerator.PKG_A + ".model.base." + actionName;
                    a = (BaseAction) (Class.forName(className)).getDeclaredConstructor().newInstance();

                } catch (Exception e2) {
                    if (actionName.endsWith("SearchAction")) {
                        try {

                            // 検索処理の基底クラスを取ってみる
                            String className = "jp.co.golorp.emarf.action.SearchAction";
                            a = (BaseAction) (Class.forName(className)).getDeclaredConstructor().newInstance();

                        } catch (Exception e3) {
                            throw new SysError(e);
                        }
                    } else if (actionName.endsWith("CorrectAction")) {
                        try {

                            // 選択検索処理の基底クラスを取ってみる
                            String className = "jp.co.golorp.emarf.action.CorrectAction";
                            a = (BaseAction) (Class.forName(className)).getDeclaredConstructor().newInstance();

                        } catch (Exception e3) {
                            throw new SysError(e);
                        }
                    } else if (actionName.endsWith("GetAction")) {
                        try {

                            // 照会処理の基底クラスを取ってみる
                            String className = "jp.co.golorp.emarf.action.GetAction";
                            a = (BaseAction) (Class.forName(className)).getDeclaredConstructor().newInstance();

                        } catch (Exception e3) {
                            throw new SysError(e);
                        }
                    } else if (actionName.endsWith("DownloadAction")) {
                        try {

                            // ダウンロード処理の基底クラスを取ってみる
                            String className = "jp.co.golorp.emarf.action.DownloadAction";
                            a = (BaseAction) (Class.forName(className)).getDeclaredConstructor().newInstance();

                        } catch (Exception e3) {
                            throw new SysError(e);
                        }
                    } else if (actionName.endsWith("AuthzAction")) {
                        try {

                            // 認可処理の基底クラスを取ってみる
                            String className = "jp.co.golorp.emarf.action.AuthzAction";
                            a = (BaseAction) (Class.forName(className)).getDeclaredConstructor().newInstance();

                        } catch (Exception e3) {
                            throw new SysError(e);
                        }
                    } else {
                        throw new SysError(e);
                    }
                }
            }
        }

        // sqlファイルの探索開始パスを取得
        List<String> pathes = ServletUtil.getPathes(request);
        a.setPathes(pathes);

        // sqlファイル名の規定値を取得
        String baseName = ServletUtil.getBaseName(request);
        a.setBaseName(baseName);

        HttpSession ses = request.getSession();
        a.setSession(ses);

        a.setRequestURI(request.getRequestURI());

        if (ses.getAttribute(LoginFilter.AUTHN_KEY) != null) {
            a.setId(ses.getAttribute(LoginFilter.AUTHN_KEY).toString());
        }

        return a;
    }

    /**
     * @param request
     * @return List<String>
     */
    public static List<String> getPathes(final HttpServletRequest request) {

        String servletPath = request.getServletPath();
        String[] servletPathes = servletPath.split("/");

        List<String> sqlPathes = new ArrayList<String>();
        for (int i = 0; i < servletPathes.length - 1; i++) {
            String s = servletPathes[i];
            if (s.trim().length() == 0) {
                continue;
            }
            sqlPathes.add(s);
        }

        return sqlPathes;
    }

    /**
     * @param request
     * @return String
     */
    public static String getBaseName(final HttpServletRequest request) {

        String servletPath = request.getServletPath();
        String[] servletPathes = servletPath.split("/");

        String lastPath = servletPathes[servletPathes.length - 1];
        return lastPath.replaceFirst(".[a-z]+$", "");
    }

    /**
     * @param request
     * @return Map
     */
    public static Map<String, Object> getPostJson(final HttpServletRequest request) {

        Collection<Part> parts = null;
        try {
            parts = request.getParts();
        } catch (Exception e) {
            LOG.trace(e.getMessage());
        }

        Map<String, Object> map = new HashMap<String, Object>();
        ObjectMapper mapper = ServletUtil.getMapper();

        if (parts != null) {
            // 「enctype="multipart/form-data"」の場合

            Set<String> submittedFileNames = new HashSet<String>();

            // multipartでループ
            for (Part part : parts) {

                String partName = StringUtil.sanitize(part.getName());

                if (part.getSubmittedFileName() != null) {
                    // アップロードファイルの場合

                    String fileName = StringUtil.sanitize(part.getSubmittedFileName());
                    if (!fileName.equals("")) {
                        // アップロードファイル名がある場合

                        // アップロードフォルダに保管
                        String upload = App.get("uploadFolderPath");
                        if (upload == null) {
                            upload = "C:\\upload";
                        }
                        // アップロードフォルダがなければ作成
                        File uploadDir = new File(upload);
                        if (!uploadDir.exists()) {
                            uploadDir.mkdirs();
                        }
                        String ext = fileName.replaceFirst("^.+\\.", "");
                        String saveName = partName + "." + DateTimeUtil.ymdhmsS() + "." + ext;
                        String uploadPath = upload + File.separator + saveName;
                        try {
                            part.write(uploadPath);
                        } catch (IOException e) {
                            throw new SysError(e);
                        }

                        // ファイル名と保管パスを返す
                        //                        map.put(partName + uploadMeiSuffix, fileName);
                        map.put(partName, fileName + "|" + uploadPath);
                        submittedFileNames.add(partName);
                    }

                } else {

                    if (submittedFileNames.contains(partName)) {
                        continue;
                    }

                    // 送信値を読み取り
                    String v = "";
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(part.getInputStream()))) {
                        String s;
                        while ((s = br.readLine()) != null) {
                            v += StringUtil.sanitize(s);
                        }
                    } catch (IOException e) {
                        throw new SysError(e);
                    }

                    if (partName.matches("^.+Grid$")) {

                        try {
                            Map<String, Object> gridValue = mapper.readValue("{\"" + partName + "\":" + v + "}",
                                    new TypeReference<Map<String, Object>>() {
                                    });
                            map.putAll(gridValue);
                        } catch (Exception e) {
                            throw new SysError(e);
                        }

                    } else if (map.containsKey(partName)) {
                        // 二つ目以降の場合

                        Object orgValue = map.get(partName);
                        if (orgValue instanceof List) {
                            // 三つ目以降の場合

                            @SuppressWarnings("unchecked")
                            List<Object> newList = (List<Object>) orgValue;
                            newList.add(v);

                        } else {
                            // 二つ目の場合

                            List<Object> newList = new ArrayList<Object>();
                            newList.add(orgValue);
                            newList.add(v);
                            map.put(partName, newList);
                        }

                    } else {
                        // 一つ目の場合
                        map.put(partName, v);
                    }
                }
            }

        } else {

            // get/post送信の場合
            if (request.getParameterMap().size() > 0) {
                map.putAll(suckParameterMap(request));
            }

            // ajax送信の場合
            try {
                String line = request.getReader().readLine();
                if (line != null) {
                    String s = StringUtil.sanitize(line);
                    if (!StringUtil.isNullOrWhiteSpace(s)) {
                        Map<String, Object> ajaxValues = mapper.readValue(s, new TypeReference<Map<String, Object>>() {
                        });
                        for (Map.Entry<String, Object> ajaxValue : ajaxValues.entrySet()) {
                            String k = ajaxValue.getKey();
                            Object v = ajaxValue.getValue();
                            // get値・post値を優先する
                            if (!map.containsKey(k)) {
                                map.put(k, v);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new SysError(e);
            }
        }

        try {
            LOG.trace("RequestJson: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map));
        } catch (JsonProcessingException e) {
            throw new SysError(e);
        }

        return map;
    }

    /**
     * @return ObjectMapper
     */
    public static ObjectMapper getMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 1. フィールドは private でも全て出力対象にする
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        // 2. getter / is-getter メソッドはすべて出力対象外（NONE）にする
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);

        return mapper;
    }

    /**
     * @param request
     * @return サニタイズ済みのMap
     */
    public static Map<String, Object> suckParameterMap(final HttpServletRequest request) {

        Map<String, Object> map = null;

        for (Entry<String, String[]> e : request.getParameterMap().entrySet()) {
            String k = StringUtil.sanitize(e.getKey());
            String[] v = StringUtil.sanitize(e.getValue());

            if (map == null) {
                map = new LinkedHashMap<String, Object>();
            }

            if (v.length > 1) {
                map.put(k, v);
            } else if (v.length == 1) {
                map.put(k, v[0]);
            } else {
                map.put(k, "");
            }
        }

        return map;
    }

    /**
     * @param request
     * @return Map
     */
    public static Map<String, Object> getPostedJson(final HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String ajaxRequestURI = requestURI.replaceFirst("\\.[a-z]+$", ".ajax");
        HttpSession session = request.getSession();
        Object o = session.getAttribute(ajaxRequestURI);
        @SuppressWarnings("unchecked")
        Map<String, Object> postJson = (Map<String, Object>) o;
        return postJson;
    }

    /**
     * @param response
     * @param map
     */
    public static void sendJson(final HttpServletResponse response, final Map<String, Object> map) {
        try {
            ObjectMapper mapper = ServletUtil.getMapper();
            mapper.registerModule(new JavaTimeModule());
            String s = mapper.writeValueAsString(map);
            LOG.trace("ResponseJson: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map));
            response.getWriter().append(s);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new SysError(e);
        }
    }

    /**
     * {"エンティティ名":エンティティインスタンス} を
     * {"エンティティ名":{"プロパティ名":プロパティ値}} に変換
     * @param map
     * @return Map
     */
    public static Map<String, Object> toSimpleMap(final Map<String, Object> map) {
        try {
            ObjectMapper mapper = ServletUtil.getMapper();
            String s = mapper.writeValueAsString(map);
            return mapper.readValue(s, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new SysError(e);
        }
    }

    /**
     * @param response
     * @param filePath
     * @param fileMei
     */
    public static void respond(final HttpServletResponse response, final Object filePath, final Object fileMei) {

        Calendar begin = Calendar.getInstance();
        LOG.debug("Binary response start.");

        String path = filePath.toString();

        if (path.endsWith(".pdf")) {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline;");
        } else {
            String downloadFileMei = new File(path).getName();
            if (fileMei != null) {
                downloadFileMei = fileMei.toString();
            }
            try {
                downloadFileMei = URLEncoder.encode(downloadFileMei, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                throw new SysError(e);
            }
            response.addHeader("Content-Type", "application/octet-stream");
            response.addHeader("Content-Disposition", "attachment; filename*=UTF-8''" + downloadFileMei);
        }

        try {
            Files.copy(new File(path).toPath(), response.getOutputStream());
            response.getOutputStream().close();
        } catch (IOException e) {
            throw new SysError(e);
        }

        Calendar end = Calendar.getInstance();
        long millis = end.getTimeInMillis() - begin.getTimeInMillis();
        LOG.debug("Binary response end in " + millis + " millis. [" + filePath + "]");
    }

    /**
     * @param response
     * @param filePath
     */
    public static void respondDelete(final HttpServletResponse response, final String filePath) {
        respondDelete(response, filePath, null);
    }

    /**
     * @param response
     * @param filePath
     * @param fileMei
     */
    public static void respondDelete(final HttpServletResponse response, final String filePath, final String fileMei) {

        ServletUtil.respond(response, filePath, fileMei);

        // ファイル削除
        if (App.get("deleteRespondedXlsx").equals("1")) {
            new File(filePath).delete();
        }
    }

}
