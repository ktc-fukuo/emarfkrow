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

package jp.co.golorp.emarf.time;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import jp.co.golorp.emarf.lang.StringUtil;

/**
 * LocalDateTimeのラッパ（テスト用の日時を使用するため）
 *
 * @author golorp
 */
public final class DateTimeUtil {

    /** プライベートコンストラクタ */
    private DateTimeUtil() {
    }

    /**
     * @return 現在日時
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * @return yyyyMMddHHmmssSSS形式の現在日時
     */
    public static String ymdhmsS() {
        return DateTimeUtil.format("yyyyMMddHHmmssSSS");
    }

    /**
     * @param format フォーマット
     * @return フォーマットで指定した形式の現在日時
     */
    public static String format(final String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return DateTimeUtil.now().format(formatter);
    }

    /**
     * @param format フォーマット
     * @param localDateTime 日付
     * @return フォーマットで指定した形式の現在日時
     */
    public static String format(final String format, final LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return localDateTime.format(formatter);
    }

    /**
     * @return 現在日時の{@link Date}
     */
    public static Date date() {
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = ZonedDateTime.of(now(), zone);
        Instant instant = zonedDateTime.toInstant();
        return Date.from(instant);
    }

    /**
     * @param s      対象文字列
     * @param format フォーマット
     * @return フォーマットで指定した形式の現在日時
     */
    public static LocalDateTime parse(final String s, final String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDateTime.parse(s, formatter);
    }

    /**
     * @param o
     * @return LocalDateTime
     */
    public static LocalDateTime parse(final Object o) {

        if (o != null) {

            if (o instanceof Long) {

                Date d = new Date((Long) o);
                return LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());

            } else if (o.toString().matches("^[0-9]+")) {

                Date d = new Date(Long.valueOf(o.toString()));
                return LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());

            } else if (o.toString().matches("^.+\\+\\d{2}:\\d{2}$")) {

                Instant instant = Instant.parse(o.toString());
                return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

            } else if (!StringUtil.isNullOrWhiteSpace(o)) {

                return LocalDateTime.parse(o.toString().replace(" ", "T").replace("/", "-"));
            }
        }

        return null;
    }

}
