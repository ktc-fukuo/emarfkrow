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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
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
import jp.co.golorp.emarf.exception.SysError;
import jp.co.golorp.emarf.generator.BeanGenerator;
import jp.co.golorp.emarf.properties.App;
import jp.co.golorp.emarf.servlet.http.ServletUtil;
import jp.co.golorp.emarf.time.DateTimeUtil;
import jp.co.golorp.emarf.util.Messages;
import jp.co.golorp.emarf.util.ResourceBundles;

/**
 * サービス時間フィルタ
 *
 * @author golorp
 */
@WebFilter("/*")
public class ServiceFilter implements Filter {

    /** ロガー */
    private static final Logger LOG = LoggerFactory.getLogger(ServiceFilter.class);

    /** BeanGenerator.properties */
    private static ResourceBundle bundle = ResourceBundles.getBundle(BeanGenerator.class);

    /** 除外URIの正規表現 */
    public static final String EXCLUDE_REGEXP = App.get("servicefilter.exclude.regexp");

    /** 登録系URIの正規表現 */
    private static final String WRITE_URI_RE = App.get("servicefilter.write.uri.regexp");

    /** 書き込み禁止期間 */
    public static final List<String> DONT_WRITES = App.getStartWith("servicefilter.dontwrite");

    /** 読み取り禁止期間 */
    public static final List<String> DONT_READS = App.getStartWith("servicefilter.dontread");

    /** サービスエラーページ */
    private static final String ERROR_PAGE = App.get("servicefilter.error.page");

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
     * サービス時間の確認
     * @param request {@link ServletRequest}
     * @param response {@link ServletResponse}
     * @param chain {@link FilterChain}
     */
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String requestURI = req.getRequestURI();

        if (EXCLUDE_REGEXP != null && !requestURI.matches(EXCLUDE_REGEXP)) {

            HttpSession ses = req.getSession();
            String authnKey = "anonymous";
            if (ses.getAttribute(LoginFilter.AUTHN_KEY) != null) {
                authnKey = ses.getAttribute(LoginFilter.AUTHN_KEY).toString();
            }
            String remoteAddr = request.getRemoteAddr();
            LOG.info(
                    "RequestURI: " + req.getRequestURI() + ", RequestBY: " + authnKey + ", RequestFROM: " + remoteAddr);

            if (requestURI.matches(WRITE_URI_RE)) {
                for (String kikanCron : DONT_WRITES) {
                    String[] kikanCrons = kikanCron.split("\\|");
                    if (!isService(kikanCrons[0], kikanCrons[1])) {
                        // 登録サービス時間外
                        Map<String, Object> map = null;
                        map = new HashMap<String, Object>();
                        map.put("ERROR", Messages.get("error.service.write"));
                        ServletUtil.sendJson(res, map);
                        return;
                    }
                }
            } else {
                for (String kikanCron : DONT_READS) {
                    String[] kikanCrons = kikanCron.split("\\|");
                    if (!isService(kikanCrons[0], kikanCrons[1])) {
                        // 照会サービス時間外
                        String contextPath = req.getContextPath() + "/";
                        res.sendRedirect(contextPath + ServiceFilter.ERROR_PAGE);
                        return;
                    }
                }
            }

            /*
             * サービスアクションがあれば実行
             */

            Class<?> c = null;
            try {
                c = Class.forName(pkgA + ".ServiceAction");
            } catch (ClassNotFoundException e) {
                LOG.trace("ServiceAction is not found.");
            }

            if (c != null) {
                try {
                    BaseAction action = (BaseAction) c.getDeclaredConstructor().newInstance();
                    //                Map<String, Object> postJson = new HashMap<String, Object>();
                    Map<String, Object> map = action.run(null);
                    if ((boolean) map.get("DONT_WRITE")) {
                        map = new HashMap<String, Object>();
                        map.put("ERROR", Messages.get("error.service.write"));
                        ServletUtil.sendJson(res, map);
                        return;
                    }
                    if ((boolean) map.get("DONT_READ")) {
                        String contextPath = req.getContextPath() + "/";
                        res.sendRedirect(contextPath + ServiceFilter.ERROR_PAGE);
                        return;
                    }
                } catch (Exception e) {
                    throw new SysError(e);
                }
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * @param teishiCron 停止クーロン式
     * @param saikaiCron 再開クーロン式
     * @return boolean
     */
    public static boolean isService(final String teishiCron, final String saikaiCron) {

        LocalDateTime sysDate = DateTimeUtil.now();

        String y = String.valueOf(sysDate.getYear());
        String format = "yyyy/M/d H:m";

        /*
         * 停止日時
         */

        String[] teishiCrons = teishiCron.split(" +");
        String teishiA = teishiCrons[4];
        String teishiM = teishiCrons[3];
        String teishiD = teishiCrons[2];
        String teishiHH = teishiCrons[1];
        String teishiMM = teishiCrons[0];

        // 月の補完
        if (teishiM.equals("*")) {
            teishiM = String.valueOf(sysDate.getMonth().getValue());
        }
        // 日の補完
        if (teishiD.equals("*")) {
            teishiD = String.valueOf(sysDate.getDayOfMonth());
        }
        // 時の補完
        if (teishiHH.equals("*")) {
            teishiHH = String.valueOf(sysDate.getHour());
        }
        // 分の補完
        if (teishiMM.equals("*")) {
            teishiMM = String.valueOf(sysDate.getMinute());
        }

        String teishiYMDHM = y + "/" + teishiM + "/" + teishiD + " " + teishiHH + ":" + teishiMM;
        LocalDateTime teishiDate = DateTimeUtil.parse(teishiYMDHM, format);
        //        System.out.println("停止：" + DateTimeUtil.format("yyyy/MM/dd HH:mm", teishiDate));

        // 曜日の補完
        if (teishiA.equals("*")) {
            // javaの1originをcronの0originに合わせる
            teishiA = String.valueOf(teishiDate.getDayOfWeek().getValue() % 7);
        }

        // 曜日がずれていたら停止期間内ではない
        if (!String.valueOf(teishiDate.getDayOfWeek().getValue() % 7).equals(teishiA)) {
            return true;
        }

        /*
         * 再開日時
         */

        String[] saikaiCrons = saikaiCron.split(" +");
        String saikaiA = saikaiCrons[4];
        String saikaiM = saikaiCrons[3];
        String saikaiD = saikaiCrons[2];
        String saikaiHH = saikaiCrons[1];
        String saikaiMM = saikaiCrons[0];

        // 月の補完
        if (saikaiM.equals("*")) {
            saikaiM = String.valueOf(sysDate.getMonth().getValue());
        }
        // 日の補完
        if (saikaiD.equals("*")) {
            saikaiD = String.valueOf(sysDate.getDayOfMonth());
        }
        // 時の補完
        if (saikaiHH.equals("*")) {
            saikaiHH = String.valueOf(sysDate.getHour());
        }
        // 分の補完
        if (saikaiMM.equals("*")) {
            saikaiMM = String.valueOf(sysDate.getMinute());
        }

        String saikaiYMDHM = y + "/" + saikaiM + "/" + saikaiD + " " + saikaiHH + ":" + saikaiMM;
        LocalDateTime saikaiDate = DateTimeUtil.parse(saikaiYMDHM, format);
        //        System.out.println("再開：" + DateTimeUtil.format("yyyy/MM/dd HH:mm", saikaiDate));

        // 曜日の補完
        if (saikaiA.equals("*")) {
            // javaの1originをcronの0originに合わせる
            saikaiA = String.valueOf(saikaiDate.getDayOfWeek().getValue() % 7);
        }

        // 曜日がずれていたら停止期間内ではない
        if (!String.valueOf(saikaiDate.getDayOfWeek().getValue() % 7).equals(saikaiA)) {
            return true;
        }

        /*
         * 停止期間内なら停止
         */

        if (teishiDate.isBefore(saikaiDate)) {
            // 停止日時＜再開日時

            // 停止日時から、再開日時までなら、利用不可
            if (teishiDate.isBefore(sysDate) && sysDate.isBefore(saikaiDate)) {
                return false;
            }

        } else {
            // 再開日時＜停止日時

            // 再開日時より過去か、停止日時より未来なら、利用不可
            if (sysDate.isBefore(saikaiDate) || teishiDate.isBefore(sysDate)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void destroy() {

    }

}
