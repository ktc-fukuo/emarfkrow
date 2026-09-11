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
package jp.co.golorp.emarf.form.base;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import jp.co.golorp.emarf.process.BaseProcess;
import jp.co.golorp.emarf.validation.IForm;

/**
 * ログインフォーム
 * @author toshiyuki
 *
 */
public abstract class LoginFormBase implements IForm {

    /***/
    @NotBlank
    @Pattern(regexp = "[ -~]*")
    private String userId;

    /**
     * @return email
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param p
     */
    public void setUserId(final String p) {
        this.userId = p;
    }

    /***/
    @NotBlank
    @Pattern(regexp = "[ -~]*")
    private String passwd;

    /**
     * @return password
     */
    public String getPasswd() {
        return passwd;
    }

    /**
     * @param p
     */
    public void setPasswd(final String p) {
        this.passwd = p;
    }

    /**
     * ログインユーザ名
     */
    private String authnMei;

    /**
     * @return authnMei
     */
    public String getAuthnMei() {
        return authnMei;
    }

    /**
     * @param p
     */
    public void setAuthnMei(final String p) {
        this.authnMei = p;
    }

    /** */
    private String email;

    /**
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param p
     */
    public void setEmail(final String p) {
        this.email = p;
    }

    /**
     * 認証情報
     */
    private Map<String, String> authnInfo;

    /**
     * @return authInfo
     */
    public Map<String, String> getAuthnInfo() {
        return authnInfo;
    }

    /**
     * @param p
     */
    public void setAuthnInfo(final Map<String, String> p) {
        this.authnInfo = p;
    }

    /**
     * 認可情報<正規表現, ビットフラグ>
     */
    private Map<String, Integer> authzInfo;

    /**
     * @return authzInfo
     */
    public Map<String, Integer> getAuthzInfo() {
        return authzInfo;
    }

    /**
     * @param p
     */
    public void setAuthzInfo(final Map<String, Integer> p) {
        this.authzInfo = p;
    }

    @Override
    public void validate(final Map<String, String> errors, final BaseProcess baseProcess) {
    }

    /**
     * @param requestUri
     * @return String エラーID
     */
    public abstract Map<String, Integer> getTableAuthZ(String requestUri);

    /**
     * 認可評価済みのエラーID<URI, エラーID>
     */
    private Map<String, Map<String, Integer>> tablesAuthZ = new HashMap<String, Map<String, Integer>>();

    /**
     * @return Map<String, String> authzIds
     */
    public Map<String, Map<String, Integer>> getTablesAuthZ() {
        return tablesAuthZ;
    }

    /**
     * @param p
     */
    public void setTablesAuthZ(final Map<String, Map<String, Integer>> p) {
        this.tablesAuthZ = p;
    }

}
