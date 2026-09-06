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

package jp.co.golorp.emarf.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.golorp.emarf.entity.IEntity;
import jp.co.golorp.emarf.exception.SysError;
import jp.co.golorp.emarf.generator.BeanGenerator;
import jp.co.golorp.emarf.lang.StringUtil;
import jp.co.golorp.emarf.util.Messages;
import jp.co.golorp.emarf.util.ResourceBundles;

/**
 * フォームバリデータ
 *
 * @author golorp
 */
public final class FormValidator {

    /** logger */
    private static final Logger LOG = LoggerFactory.getLogger(FormValidator.class);

    /** グリッド先頭文字のパターン */
    private static Pattern gridNamePattern = Pattern.compile("\\.[a-z]");

    /** ~に囲まれたパターン（正規表現部の名称変換用） */
    private static Pattern regexpPattern = Pattern.compile("\\~\\~(.+?)\\~\\~");

    /** BeanGenerator.properties */
    private static ResourceBundle bundle = ResourceBundles.getBundle(BeanGenerator.class);

    /***/
    private static String reason = "";

    static {
        if (bundle != null) {
            reason = bundle.getString("column.reason");
        }
    }

    /** プライベートコンストラクタ */
    private FormValidator() {
    }

    /**
     * フォームの検証
     * @param errors 「エラー項目名：エラーメッセージ」のマップ
     * @param formClassName フォームクラス名
     * @param postJson 送信値のマップ
     * @return 値を設定後のフォーム
     */
    public static IForm validate(final Map<String, String> errors, final String formClassName,
            final Map<String, Object> postJson) {

        IForm form = toBean(formClassName, postJson);
        if (form == null) {
            return null;
        }

        // バリデーションを実行
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<IForm>> results = null;

        if (formClassName.endsWith("RegistForm")) {
            results = validator.validate(form, Regist.class);
        } else {
            // DeleteFormなら主キーと楽観ロック項目以外はチェックしない
            results = validator.validate(form, Delete.class);
        }

        for (ConstraintViolation<IForm> result : results) {

            // Formクラス名（一覧画面：MUserSRegistForm、詳細画面：MUserRegistForm）
            String formName = result.getRootBean().getClass().getSimpleName();

            // プロパティ名（一覧画面：mUserGrid[0].userMei、詳細画面：userMei）
            String propertyName = result.getPropertyPath().toString();

            // グリッドの場合は先頭文字を大文字化（MUserGrid[0].userMei）
            Matcher gridNameMatcher = gridNamePattern.matcher(propertyName);
            if (gridNameMatcher.find()) {
                propertyName = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
            }

            // エラー項目物理名
            String itemId = formName + "." + propertyName;

            // エラー未取得の項目の場合
            if (!errors.containsKey(itemId)) {

                // Validatorのメッセージを取得
                String message = result.getMessage();

                // 添え字抜きで項目名を取得してみる
                // TEntityRegistForm.TEntity2RegistForm.hidukeYmd
                String itemName = itemId.replaceFirst("\\[\\d+\\]", "");
                String itemMei = Messages.get(itemName);

                // モデル名.プロパティ名で項目名を取得してみる
                // TEntity2.hidukeYmd
                if (itemMei == null) {
                    String[] s = itemName.replaceAll("S?(Refer|Regist)Form", "").split("\\.");
                    itemMei = Messages.get(s[s.length - 2] + "." + s[s.length - 1]);
                }

                // フォーム名なしで項目名を取得してみる
                // hidukeYmd
                if (itemMei == null) {
                    itemMei = Messages.get(itemName.replaceFirst(".+(Refer|Regist)Form\\.", ""));
                }

                // 画面名.プロパティ名で項目名を取得してみる
                // Passmail.email
                if (itemMei == null) {
                    String[] s = itemName.replaceAll("Form", "").split("\\.");
                    itemMei = Messages.get(s[s.length - 2] + "." + s[s.length - 1]);
                }

                // {0}をエラー項目論理名で置換
                if (itemMei != null) {
                    message = message.replaceAll("\\{0\\}", itemMei);
                }

                // 正規表現文字列を名称変換
                Matcher regexpMatcher = regexpPattern.matcher(message);
                while (regexpMatcher.find()) {
                    String reName = Messages.get(regexpMatcher.group(1));
                    if (reName != null && reName.length() > 0) {
                        message = message.replace(regexpMatcher.group(), reName);
                    } else {
                        message = message.replaceAll("\\~\\~\\((.+?)\\)\\?\\~\\~", "$1");
                    }
                }

                errors.put(itemId, message);
            }
        }

        return form;
    }

    /**
     * マップをインスタンス化
     * @param <T> 返却クラス
     * @param className クラス名
     * @param postJson 送信値のマップ
     * @return 指定クラスのインスタンス
     */
    public static <T> T toBean(final String className, final Map<String, Object> postJson) {
        return toBean(className, postJson, false, false);
    }

    /**
     * マップをインスタンス化（グリッドフォーム用）
     * @param <T> 返却クラス
     * @param className クラス名
     * @param postJson 送信値のマップ
     * @return 指定クラスのインスタンス
     */
    public static <T> T toGridForm(final String className, final Map<String, Object> postJson) {
        return toBean(className, postJson, true, false);
    }

    /**
     * マップをインスタンス化
     * @param <T> 返却クラス
     * @param className jsonを変換するクラス名
     * @param postJson 送信値のマップ
     * @param isGridRow グリッド行ならtrue
     * @param isNested 兄弟モデルの無限ループ防止
     * @return 指定クラスのインスタンス
     */
    private static <T> T toBean(final String className, final Map<String, Object> postJson, final boolean isGridRow,
            final boolean isNested) {
        Class<?> clazz = forNameIf(className); // 変換後のフォームクラスインスタンスを取得
        if (clazz == null) {
            return null;
        }
        Object o = null;
        try {
            o = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new SysError(e);
        }
        Method[] methods = clazz.getMethods(); // フォームクラスインスタンスのセッターでループ
        for (Method method : methods) {
            String methodName = method.getName();
            if (!methodName.startsWith("set")) {
                continue;
            }
            String fieldName = null;
            Object value = null;
            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?>[] interfaces = parameterTypes[0].getInterfaces();
            if (/*!isGridRow &&*/ !isNested
                    && (interfaces.length > 0 && (interfaces[0] == IEntity.class || interfaces[0] == IForm.class))) {
                // Grid行でもネストでもなく、EntityクラスかFormクラスの場合（兄弟モデルの場合）はネスト
                String nestName = parameterTypes[0].getName();
                Object bro = toBean(nestName, postJson, false, true);
                if (isNullOrWhiteSpace(bro)) {
                    value = bro;
                }
            } else {
                String entityName = clazz.getSimpleName().replaceAll("RegistForm$", "");
                // fieldName（フィールド名か、兄弟モデルか、グリッドID）
                fieldName = StringUtil.toCamelCase(methodName.replaceFirst("^set", ""));
                String upperName = StringUtil.toUpperCase(methodName.replaceFirst("^set", ""));
                // １．まず「EntityName.」付きで取得してみる
                value = postJson.get(entityName + "." + fieldName);
                // ２．主キーか楽観ロック用なら、Entityに関わらず「fieldName」か「FIELD_NAME」で取得してみる（兄弟モデル用）
                if (value == null) {
                    for (Annotation a : method.getAnnotations()) {
                        if (a.annotationType() == PrimaryKeys.class /*|| a.annotationType() == OptLock.class*/) {
                            for (Entry<String, Object> e : postJson.entrySet()) {
                                String k = e.getKey();
                                if (k.endsWith("." + fieldName) || k.equals(fieldName)
                                        || k.toUpperCase().endsWith("." + upperName)
                                        || k.toUpperCase().equals(upperName)) {
                                    value = e.getValue();
                                    break;
                                }
                            }
                            if (value != null) {
                                break;
                            }
                        }
                    }
                }
                // ３．"FieldName"でも取得してみる（グリッド本体用）
                if (value == null) {
                    value = postJson.get(StringUtil.toPascalCase(fieldName));
                }
                // ４．"[FieldName]s"を"[FieldName]Grid"にして取得してみる（グリッド本体用その２）
                if (value == null) {
                    value = postJson.get(StringUtil.toPascalCase(fieldName).replaceAll("s$", "Grid"));
                }
                // 「Entity.」付きのアッパーでも取得してみる（グリッド行の兄弟モデル用）
                if (value == null) {
                    entityName = StringUtil.toUpperCase(entityName).replaceAll("\\_([0-9]+)", "$1");
                    value = postJson.get(entityName + "." + upperName);
                }
                if (!isNested && value == null) { // 送信値をfieldNameで取得してみる（グリッド行用・他モデルと誤爆するのは避ける）
                    value = postJson.get(fieldName);
                }
                if (!isNested && value == null) { // スネークでも取得してみる（グリッド行用）
                    value = postJson.get(StringUtil.toSnakeCase(fieldName));
                }
                if (!isNested && value == null) { // 数字の前の「_」を消して、スネークでも取得してみる（グリッド行用）
                    value = postJson.get(StringUtil.toSnakeCase(fieldName).replaceAll("\\_([0-9]+)", "$1"));
                }
                if (!isNested && value == null) { // アッパーでも取得してみる（グリッド行用）
                    value = postJson.get(StringUtil.toUpperCase(fieldName));
                }
                if (!isNested && value == null) { // 数字の前の「_」を消して、アッパーでも取得してみる（グリッド行用）
                    value = postJson.get(StringUtil.toUpperCase(fieldName).replaceAll("\\_([0-9]+)", "$1"));
                }
                if (!isNested && value == null) { // ケバブでも取得してみる（グリッド行用）
                    value = postJson.get(StringUtil.toKebabCase(fieldName));
                }
                if (!isNested && value == null) { // アッパーケバブでも取得してみる（グリッド行用）
                    value = postJson.get(StringUtil.toUpperKebabCase(fieldName));
                }
            }
            if (value != null) { // 送信値がある場合
                try {
                    if (value instanceof List) { // 送信値がListの場合
                        if (isNested) { // 兄弟モデルには子モデルを付けない
                            continue;
                        }
                        List<?> list = (List<?>) value;
                        if (list.size() > 0 && list.get(0) instanceof Map) {
                            // 送信値の一つ目がMapの場合（何らかのクラスであるという事）
                            String packageName = clazz.getPackage().getName();
                            String gridId = StringUtil.toPascalCase(fieldName);
                            String gridClassName = packageName + "." + gridId;
                            List<T> formList = new ArrayList<T>();
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> gridData = (List<Map<String, Object>>) list;
                            for (Map<String, Object> row : gridData) {
                                if (row.isEmpty()) {
                                    formList.add(null);
                                } else {
                                    if (!StringUtil.isNullOrWhiteSpace(reason)) {
                                        String reasonName = StringUtil.toCamelCase(reason);
                                        if (!StringUtil.isNullOrWhiteSpace(postJson.get(reasonName))) {
                                            row.put(reason, postJson.get(reasonName));
                                        }
                                    }
                                    @SuppressWarnings("unchecked")
                                    T t = (T) toGridForm(gridClassName, row);
                                    formList.add(t);
                                }
                            }
                            value = formList;
                        }
                        method.invoke(o, value);
                    } else if (value instanceof IEntity || value instanceof IForm) {
                        method.invoke(o, value);
                    } else {
                        method.invoke(o, value.toString());
                    }
                } catch (Exception e) {
                    throw new SysError(e);
                }
            }
        }
        @SuppressWarnings("unchecked")
        T t = (T) o;
        return t;
    }

    /**
     * @param bro
     * @return boolean
     */
    private static boolean isNullOrWhiteSpace(final Object bro) {
        boolean isNotNullOrWhitespace = false;
        Field[] fields = bro.getClass().getDeclaredFields();
        for (Field field : fields) {
            boolean isPrimaryKeys = false;
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType() == PrimaryKeys.class
                        || annotation.annotationType() == GridViewRowId.class
                        || annotation.annotationType() == ReferMei.class) {
                    isPrimaryKeys = true;
                    break;
                }
            }
            if (!isPrimaryKeys) {
                Object v = null;
                try {
                    field.setAccessible(true);
                    v = field.get(bro);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (!StringUtil.isNullOrWhiteSpace(v) && v.getClass() != ch.qos.logback.classic.Logger.class) {
                    isNotNullOrWhitespace = true;
                    break;
                }
            }
        }
        return isNotNullOrWhitespace;
    }

    /**
     * クラスを探索
     * @param className 対象クラス名
     * @return 探索結果のクラス
     */
    private static Class<?> forNameIf(final String className) {

        try {

            // そのままとってみる
            return Class.forName(className);

        } catch (ClassNotFoundException e) {
            try {

                // modelパッケージのフォームクラスなら、baseパッケージまで掘ってみる
                return Class.forName(className.replaceFirst("\\.model\\.", ".model.base."));

            } catch (ClassNotFoundException e1) {
                try {

                    // ***GridFormなら、***RegistFormにしてみる
                    return Class.forName(className.replaceFirst("Grid", "Regist"));

                } catch (ClassNotFoundException e2) {
                    try {

                        // ***DeleteFormなら、***RegistFormにしてみる
                        return Class.forName(className.replaceFirst("Delete", "Regist"));

                    } catch (ClassNotFoundException e3) {
                        try {

                            // ***Gridなら、***RegistFormにしてみる
                            return Class.forName(className.replaceFirst("Grid", "RegistForm"));

                        } catch (ClassNotFoundException e4) {
                            try {

                                // ***sなら、***にしてみる（カスタムフォームのグリッド本体用）
                                return Class.forName(className.replaceFirst("s$", ""));

                            } catch (ClassNotFoundException e5) {
                                LOG.trace(e.toString());
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

}
