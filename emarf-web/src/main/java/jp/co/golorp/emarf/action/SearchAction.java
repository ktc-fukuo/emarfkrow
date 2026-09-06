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

package jp.co.golorp.emarf.action;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import jp.co.golorp.emarf.lang.StringUtil;
import jp.co.golorp.emarf.sql.Queries;
import jp.co.golorp.emarf.util.MapList;
import jp.co.golorp.emarf.util.Messages;

/**
 * 検索の汎用アクション
 *
 * @author golorp
 */
public class SearchAction extends BaseAction {

    /**
     * 該当データを全件返す
     */
    @Override
    public Map<String, Object> running(final LocalDateTime at, final String by, final Map<String, Object> form) {

        Map<String, Object> postedJson = form;

        Integer rows = null;
        Integer page = null;
        if (postedJson.containsKey("rows")) {
            if (!StringUtil.isNullOrWhiteSpace(postedJson.get("rows"))) {
                rows = Integer.valueOf(postedJson.get("rows").toString());
            }
            if (!StringUtil.isNullOrWhiteSpace(postedJson.get("page"))) {
                page = Integer.valueOf(postedJson.get("page").toString());
            }

            HttpSession ses = this.getSession();
            String requestURI = this.getRequestURI();
            if (page == 0) {
                page = 1;
                ses.removeAttribute(requestURI);
            } else {
                @SuppressWarnings("unchecked")
                Map<String, Object> temp = (Map<String, Object>) ses.getAttribute(requestURI);
                postedJson = temp;
            }
        }

        String sql = this.loadSqlFile(this.getBaseName());

        MapList list = Queries.select(sql, postedJson, rows, page);

        Map<String, Object> map = new HashMap<String, Object>();

        if (list == null) {
            if (form.get("isSilent") == null) {
                map.put("INFO", Messages.get("info.nodata"));
            } else if (!form.get("isSilent").equals("true")) {
                map.put("INFO", Messages.get("info.nodata"));
            }
        } else {
            map.put(this.getBaseName(), list);
            if (rows != null) {
                Object totalRows = postedJson.get("totalRows");
                map.put("totalRows", totalRows);
                map.put("currentPage", page);
            }
        }

        return map;
    }

}
