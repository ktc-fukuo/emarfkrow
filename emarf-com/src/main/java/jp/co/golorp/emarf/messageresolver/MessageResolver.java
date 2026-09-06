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

package jp.co.golorp.emarf.messageresolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.messageresolver.StandardMessageResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.util.StringUtils;

import jp.co.golorp.emarf.generator.BeanGenerator;
import jp.co.golorp.emarf.lang.ReflectUtil;
import jp.co.golorp.emarf.properties.App;
import jp.co.golorp.emarf.util.Locales;
import jp.co.golorp.emarf.util.ResourceBundles;

/**
 * Thymeleafのメッセージ拡張
 *
 * @author golorp
 */
public class MessageResolver extends StandardMessageResolver {

    /** 空メッセージ */
    private static final Map<String, String> EMPTY_MESSAGES = Collections.emptyMap();

    /** プロパティファイル拡張子 */
    private static final String EXT = ".properties";

    /** メッセージマップの要素数初期値 */
    private static final int COMBINED_MESSAGES = 20;

    /** ロケールのマップ */
    private static Map<Locale, Map<String, String>> localesMap = new HashMap<Locale, Map<String, String>>();

    /** テンプレートのセット */
    private static Set<String> templatesSet = new HashSet<String>();

    /** BeanGenerator.properties */
    private static ResourceBundle bundle = ResourceBundles.getBundle(BeanGenerator.class);

    /** messages */
    private static String resourceBaseName = "messages";

    /**
     * @param template 対象の画面名
     * @param templateResource 対象画面のリソース
     * @param locale ロケール
     * @return 対象画面のプロパティファイルと共通メッセージプロパティを合わせたマップ
     */
    protected Map<String, String> resolveMessagesForTemplate(final String template,
            final ITemplateResource templateResource, final Locale locale) {

        if (templatesSet.contains(template)) {
            return localesMap.get(locale);
        }

        Map<String, String> map = localesMap.get(locale);
        if (map == null) {
            map = new HashMap<String, String>();
        }

        String path = (String) ReflectUtil.get(templateResource, "path");
        path = path.replaceFirst("S\\.html", ".html");
        path = path.replaceFirst("G\\.html", ".html");
        ReflectUtil.set(templateResource, "path", path);

        // テンプレートのプロパティファイル
        Map<String, String> templateMap = super.resolveMessagesForTemplate(template, templateResource, locale);
        if (!templateMap.isEmpty()) {
            map.putAll(templateMap);
        }

        // プロジェクトでメンテするプロパティファイル
        Map<String, String> contextMap = MessageResolver.getContextMap(locale);
        if (!contextMap.isEmpty()) {
            map.putAll(contextMap);
        }

        //退避
        localesMap.put(locale, map);
        templatesSet.add(template);

        return map;
    }

    /**
     * @return ロケール全体のメッセージと、クラスパスのmessages.propertiesのマップ
     */
    public static Map<String, String> getMessages() {

        Locale locale = Locales.get();

        Map<String, String> localeMap = localesMap.get(locale);
        if (localeMap == null) {
            localeMap = new HashMap<String, String>();
        }

        Map<String, String> contextMap = MessageResolver.getContextMap(locale);

        localeMap.putAll(contextMap);

        return localeMap;
    }

    /**
     * @param locale
     * @return クラスパスのmessages.propertiesのマップ
     */
    private static Map<String, String> getContextMap(final Locale locale) {

        if (App.get("thymeleaf.messageresolver.basename") != null) {
            resourceBaseName = App.get("thymeleaf.messageresolver.basename");
        }

        if (resourceBaseName == null || resourceBaseName.length() == 0) {
            return EMPTY_MESSAGES;
        }

        final List<String> messageResourceNames = computeMessageResourceNamesFromBase(resourceBaseName, locale);

        Map<String, String> combinedMessages = null;
        for (final String messageResourceName : messageResourceNames) {

            try {
                String baseName = messageResourceName.replaceFirst(".properties", "");
                ResourceBundle resourceBundle = ResourceBundle.getBundle(baseName, new UTF8Control());
                if (resourceBundle != null) {
                    if (combinedMessages == null) {
                        combinedMessages = new HashMap<String, String>(COMBINED_MESSAGES);
                    }
                    for (String propertyEntry : resourceBundle.keySet()) {
                        combinedMessages.put(propertyEntry, resourceBundle.getString(propertyEntry));
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }

        if (combinedMessages == null) {
            combinedMessages = new HashMap<String, String>();
        }

        if (bundle != null) {
            for (String key : bundle.keySet()) {
                combinedMessages.put(key, bundle.getString(key));
            }
        }

        return Collections.unmodifiableMap(combinedMessages);
    }

    /**
     * StandardMessageResolutionUtils.computeMessageResourceNamesFromBaseがprivateのため移植
     * @param resourceName プロパティファイルの名称
     * @param locale ロケール
     * @return プロパティファイル名の候補リスト
     */
    private static List<String> computeMessageResourceNamesFromBase(final String resourceName,
            final Locale locale) {

        final List<String> resourceNames = new ArrayList<String>(5);

        String lang = locale.getLanguage();
        String country = locale.getCountry();

        if (StringUtils.isEmptyOrWhitespace(lang)) {
            String message = "Locale \"" + locale.toString() + "\" cannot be used as it does not specify a language.";
            throw new TemplateProcessingException(message);
        }

        resourceNames.add(resourceName + EXT);

        resourceNames.add(resourceName + "_" + lang + EXT);

        if (!StringUtils.isEmptyOrWhitespace(country)) {
            resourceNames.add(resourceName + "_" + lang + "_" + country + EXT);
        }

        if (!StringUtils.isEmptyOrWhitespace(locale.getVariant())) {
            resourceNames.add(resourceName + "_" + lang + "_" + country + "-" + locale.getVariant() + EXT);
        }

        return resourceNames;

    }

}
