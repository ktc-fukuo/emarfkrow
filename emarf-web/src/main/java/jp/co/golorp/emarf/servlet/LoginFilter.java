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

package jp.co.golorp.emarf.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.golorp.emarf.action.BaseAction;
import jp.co.golorp.emarf.exception.AppError;
import jp.co.golorp.emarf.exception.SysError;
import jp.co.golorp.emarf.generator.BeanGenerator;
import jp.co.golorp.emarf.lang.StringUtil;
import jp.co.golorp.emarf.properties.App;
import jp.co.golorp.emarf.servlet.http.ServletUtil;
import jp.co.golorp.emarf.util.Messages;
import jp.co.golorp.emarf.util.ResourceBundles;

/**
 * 認証・認可フィルタ
 *
 * @author golorp
 */
@WebFilter("/*")
public class LoginFilter implements Filter {

    /** BeanGenerator.properties */
    private static ResourceBundle bundle = ResourceBundles.getBundle(BeanGenerator.class);

    /** 認証されたキー */
    public static final String AUTHN_KEY = "AUTHN_KEY";

    /** 認証された名称のキー */
    public static final String AUTHN_MEI = "AUTHN_MEI";

    /** 認証情報のキー */
    public static final String AUTHN_INFO = "AUTHN_INFO";

    /** 認可情報のキー */
    public static final String AUTHZ_INFO = "AUTHZ_INFO";

    /** ログインフォームのキー */
    public static final String LOGIN_FORM = "LOGIN_FORM";

    /** ロガー */
    private static final Logger LOG = LoggerFactory.getLogger(LoginFilter.class);

    /** 認証処理使用有無 */
    private static final String AUTHN = App.get("loginfilter.authn");

    /** ログイン画面 */
    private static final String LOGIN_PAGE = App.get("loginfilter.login.page");

    /** ログイン処理URI */
    private static final String LOGIN_URI = App.get("loginfilter.login.uri");

    /** ログアウト処理URI */
    private static final String LOGOUT_URI = App.get("loginfilter.logout.uri");

    /** 認証除外URIの正規表現 */
    private static final String EXCLUDE_REGEXP = App.get("loginfilter.exclude.regexp");

    /** パスワードリセットメール送信画面 */
    private static final String PASSMAIL_PAGE = App.get("loginfilter.passmail.page");

    /** パスワードリセットメール送信処理URI */
    private static final String PASSMAIL_URI = App.get("loginfilter.passmail.uri");

    /** パスワードリセット画面 */
    private static final String PASSRESET_PAGE = App.get("loginfilter.passreset.page");

    /** パスワードリセット処理URI */
    private static final String PASSRESET_URI = App.get("loginfilter.passreset.uri");

    /***/
    private static String pkgA = "com.example.action";

    static {
        if (bundle != null) {
            pkgA = bundle.getString("java.package.action");
        }
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {

    }

    /**
     * 認証・認可処理
     * @param request {@link ServletRequest}
     * @param response {@link ServletResponse}
     * @param chain {@link FilterChain}
     */
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String requestURI = req.getRequestURI();

        String contextPath = req.getContextPath() + "/";

        if (AUTHN == null || !AUTHN.equals("true")) {
            // 認証なし

            LOG.trace("skip authentication.");

            Map<String, String> authzMap = new HashMap<String, String>();
            authzMap.put("authz", "false");

            HttpSession ses = req.getSession();
            ses.setAttribute(LoginFilter.AUTHN_KEY, "-1");
            ses.setAttribute(LoginFilter.AUTHN_MEI, "anonymous");
            ses.setAttribute(LoginFilter.AUTHZ_INFO, authzMap);

        } else if (!requestURI.matches(LoginFilter.EXCLUDE_REGEXP)
                && !requestURI.equals(contextPath + LoginFilter.PASSMAIL_PAGE)
                && !requestURI.equals(contextPath + LoginFilter.PASSRESET_PAGE)) {
            // １．ログインチェック除外URLでない
            // ２．パスワードリセットメール送信画面でない
            // ３．パスワードリセット画面でない

            HttpSession ses = req.getSession();

            if (requestURI.equals(contextPath + LoginFilter.LOGIN_PAGE)) {
                // ログイン画面の場合は、セッション破棄のみ

                ses.invalidate();

            } else if (requestURI.equals(contextPath + LoginFilter.PASSMAIL_URI)) {
                // パスワードリセットメール送信処理の場合

                execPassmail(req, res);

                return;

            } else if (requestURI.equals(contextPath + LoginFilter.PASSRESET_URI)) {
                // パスワードリセット処理の場合

                execPassreset(req, res);

                return;

            } else if (requestURI.equals(contextPath + LoginFilter.LOGIN_URI)) {
                // ログイン処理の場合は、ログイン処理後にリダイレクト

                Map<String, Object> postJson = ServletUtil.suckParameterMap(req);

                Class<?> c = null;
                try {
                    c = Class.forName(pkgA + ".LoginAction");
                } catch (ClassNotFoundException e) {
                    throw new SysError(e);
                }

                Map<String, Object> map = null;

                try {
                    if (c != null) {
                        BaseAction action = (BaseAction) c.getDeclaredConstructor().newInstance();
                        map = action.run(postJson);
                    }
                } catch (AppError e) {
                    LOG.error(e.getMessage(), e);
                    res.sendRedirect(contextPath + LoginFilter.LOGIN_PAGE + "?ERROR=error.login");
                    return;
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                    throw new SysError(e);
                }

                ses.setAttribute(LoginFilter.AUTHN_KEY, map.get(LoginFilter.AUTHN_KEY));
                ses.setAttribute(LoginFilter.AUTHN_MEI, map.get(LoginFilter.AUTHN_MEI));
                ses.setAttribute(LoginFilter.AUTHN_INFO, map.get(LoginFilter.AUTHN_INFO));
                ses.setAttribute(LoginFilter.AUTHZ_INFO, map.get(LoginFilter.AUTHZ_INFO));
                ses.setAttribute(LoginFilter.LOGIN_FORM, map.get(LoginFilter.LOGIN_FORM));

                String orgRequestURI = StringUtil.sanitize(request.getParameter("requestURI"));
                if (!StringUtil.isNullOrWhiteSpace(orgRequestURI)) {
                    res.sendRedirect(orgRequestURI);
                } else {
                    res.sendRedirect(contextPath);
                }

                return;

            } else if (requestURI.equals(contextPath + LoginFilter.LOGOUT_URI)) {
                // ログアウト処理の場合は、セッション破棄後に、ログイン画面へリダイレクト

                ses.invalidate();
                res.sendRedirect(contextPath + LoginFilter.LOGIN_PAGE + "?INFO=info.logout");
                return;

            } else if (ses.getAttribute(LoginFilter.AUTHN_KEY) == null) {
                // 認証用オブジェクトがセッションにない場合は、セッション破棄してログイン画面へリダイレクト

                ses.invalidate();
                res.sendRedirect(contextPath + LoginFilter.LOGIN_PAGE + "?requestURI=" + requestURI);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * パスワードリセットメール送信
     * @param req {@link HttpServletRequest}
     * @param res {@link HttpServletResponse}
     */
    private void execPassmail(final HttpServletRequest req, final HttpServletResponse res) {
        execAjaxAction(req, res, "PassmailAction");
    }

    /**
     * パスワードリセット
     * @param req {@link HttpServletRequest}
     * @param res {@link HttpServletResponse}
     */
    private void execPassreset(final HttpServletRequest req, final HttpServletResponse res) {
        execAjaxAction(req, res, "PassresetAction");
    }

    /**
     * ajaxアクション実行
     * @param req {@link HttpServletRequest}
     * @param res {@link HttpServletResponse}
     * @param actionName アクション名
     */
    private void execAjaxAction(final HttpServletRequest req, final HttpServletResponse res, final String actionName) {

        Map<String, Object> postJson = ServletUtil.suckParameterMap(req);

        Class<?> c = null;
        try {
            c = Class.forName(bundle.getString("java.package.action") + "." + actionName);
        } catch (ClassNotFoundException e) {
            throw new SysError(e);
        }

        Map<String, Object> map = null;

        try {

            BaseAction action = (BaseAction) c.getDeclaredConstructor().newInstance();
            action.setSession(req.getSession());
            map = action.run(postJson);

        } catch (AppError e) {

            map = new HashMap<String, Object>();
            if (e.getErrors() != null && !e.getErrors().isEmpty()) {
                map.put("ERROR", Messages.get("error"));
                map.put("errors", e.getErrors());
            } else {
                map.put("ERROR", e.getMessage());
            }

        } catch (Exception e) {

            LOG.error(e.getMessage(), e);
            map = new HashMap<String, Object>();
            map.put("FATAL", Messages.get("fatal"));
        }

        // Actionクラスの実行結果をJSONで返却
        ServletUtil.sendJson(res, map);
    }

    @Override
    public void destroy() {

    }

}
