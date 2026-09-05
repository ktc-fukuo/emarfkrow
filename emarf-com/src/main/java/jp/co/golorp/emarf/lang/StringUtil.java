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

package jp.co.golorp.emarf.lang;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;

import jp.co.golorp.emarf.exception.SysError;
import jp.co.golorp.emarf.properties.App;

/**
 * 文字列操作クラス
 *
 * @author golorp
 */
public final class StringUtil {

    /** プライベートコンストラクタ*/
    private StringUtil() {
    }

    /**
     * @param s 変換する文字列
     * @return snake_case
     */
    public static String toSnakeCase(final String s) {

        if (s == null) {
            return null;
        }

        // 一旦「数字直後の大文字」「小文字の直後の大文字」の前に「_」を挿入
        // password→password、EMAIL→EMAIL、sampleY_1→sample_Y_1、MUser→MUser）
        String snake = s.replaceAll("([0-9a-z])([A-Z])", "$1_$2");

        // 大文字の直後に小文字が含まれる場合、大文字が連続するなら「_」を挿入
        if (snake.matches(".+[A-Z][a-z].+")) {
            snake = snake.replaceAll("([A-Z])([A-Z])", "$1_$2");
        }

        // 直後に小文字が続く大文字の前に「_」を挿入
        // password→password、EMAIL→EMAIL、sampleY_1→sample_Y_1、MUser→M_User）
        snake = snake.replaceAll("([A-Z][a-z])", "_$1");

        // 数字の連続の前に「_」を挿入
        snake = snake.replaceAll("([0-9]+)", "_$1");

        //先頭から続く「_」を除去。「_」が連続する場合は一つに。「-」は「_」に変更。
        snake = snake.replaceAll("^_+", "").replaceAll("_+", "_").replaceAll("-", "_");

        return snake.toLowerCase();
    }

    /**
     * @param s 変換する文字列
     * @return UPPER_CASE
     */
    public static String toUpperCase(final String s) {
        return toSnakeCase(s).toUpperCase();
    }

    /**
     * @param s 変換する文字列
     * @return camelCase
     */
    public static String toCamelCase(final String s) {

        if (s == null) {
            return null;
        }

        // 一旦スネークケースにしてから"_"で分割
        String forceSnake = toSnakeCase(s.replaceAll("#|\\$", "_"));
        String[] snakes = forceSnake.split("_");

        StringBuilder camel = new StringBuilder();
        for (String snake : snakes) {

            if (camel.toString().equals("")) {

                // 一つ目は全て小文字
                camel.append(snake.toLowerCase());

            } else {

                // 二つ目以降は１文字目を大文字、２文字目以降は小文字
                camel.append(snake.substring(0, 1).toUpperCase());
                if (snake.length() > 1) {
                    camel.append(snake.substring(1).toLowerCase());
                }
            }
        }

        return camel.toString();
    }

    /**
     * @param s 変換する文字列
     * @return PascalCase
     */
    public static String toPascalCase(final String s) {

        if (s == null) {
            return null;
        }

        String camelCase = toCamelCase(s);
        return camelCase.substring(0, 1).toUpperCase() + camelCase.substring(1);
    }

    /**
     * @param s 変換する文字列
     * @return kebab-case
     */
    public static String toKebabCase(final String s) {

        if (s == null) {
            return null;
        }

        String forceSnake = toSnakeCase(s);
        return forceSnake.replaceAll("_", "-");
    }

    /**
     * @param s 変換する文字列
     * @return UPPER-KEBAB-CASE
     */
    public static String toUpperKebabCase(final String s) {

        if (s == null) {
            return null;
        }

        String kebab = toKebabCase(s);
        return kebab.toUpperCase();
    }

    // /**
    // * @param cs
    // * @param list
    // * @return csでjoinした文字列
    // */
    // public static String join(final CharSequence cs, final List<String> list) {
    // return String.join(cs, list.toArray(new String[list.size()]));
    // }

    /**
     * @param s csv文字列
     * @return 「, 」で分解した文字列
     */
    public static String[] split(final String s) {

        if (!s.trim().isEmpty()) {
            return s.split(",\\s*");
        }

        return null;
    }

    /**
     * @param o 検査対象
     * @return nullか空白ならtrue
     */
    public static boolean isNullOrWhiteSpace(final Object o) {
        return o == null || o.toString().trim().equals("");
    }

    /**
     * @param delimiter 区切り文字
     * @param list 結合する文字列のリスト
     * @return 空白を無視して結合した文字列
     */
    public static String join(final String delimiter, final List<String> list) {
        List<String> elements = new ArrayList<String>();
        for (String s : list) {
            if (s.length() > 0) {
                elements.add(s);
            }
        }
        return String.join(delimiter, elements);
    }

    /**
     * @param prefixes プレフィックスの配列
     * @param s 検査文字列
     * @return 検査文字列がプレフィックスの何れかに合致すればtrue
     */
    public static boolean startsWith(final String[] prefixes, final String s) {
        if (prefixes == null) {
            return false;
        }
        for (String prefix : prefixes) {
            if (startsWithIgnoreCase(prefix, s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param prefix プレフィックス
     * @param s 検査文字列
     * @return 検査文字列がプレフィックスに合致すればtrue
     */
    public static boolean startsWithIgnoreCase(final String prefix, final String s) {
        if (prefix.length() == 0) {
            return false;
        }
        return s.matches("(?i)^" + prefix + ".*");
    }

    /**
     * @param suffixs サフィックスの配列
     * @param s 検査文字列
     * @return 検査文字列がサフィックスの何れかに合致すればtrue
     */
    public static boolean endsWith(final String[] suffixs, final String s) {
        if (suffixs == null) {
            return false;
        }
        for (String suffix : suffixs) {
            if (endsWithIgnoreCase(suffix, s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param suffixs サフィックスのマップ
     * @param s 検査文字列
     * @return 検査文字列がサフィックスのマップのキーの何れかに合致すればtrue
     */
    public static boolean endsWith(final Map<String, String> suffixs, final String s) {
        for (String suffix : suffixs.keySet()) {
            if (endsWithIgnoreCase(suffix, s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param suffixs サフィックスの配列のセット
     * @param s 検査文字列
     * @return 検査文字列がサフィックスの配列の一つ目の何れかに合致すればtrue
     */
    public static boolean endsWith(final Set<String[]> suffixs, final String s) {
        for (String[] suffix : suffixs) {
            if (endsWithIgnoreCase(suffix[0], s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param suffix サフィックス
     * @param s 検査文字列
     * @return 検査文字列がサフィックスに合致すればtrue
     */
    public static boolean endsWithIgnoreCase(final String suffix, final String s) {
        if (suffix.length() == 0) {
            return false;
        }
        return s.matches("(?i).*" + suffix + "$");
    }

    /**
     * @param s 検査文字列
     * @return 特定の記号を変換
     */
    public static String sanitize(final String s) {
        return s.replaceAll("<", "＜").replaceAll(">", "＞").replaceAll(";", "；");
    }

    /**
     * @param s 検査文字列の配列
     * @return 特定の記号を変換
     */
    public static String[] sanitize(final String[] s) {
        List<String> sanitizeds = new ArrayList<String>();
        if (s != null && s.length > 0) {
            for (String e : s) {
                sanitizeds.add(sanitize(e));
            }
        }
        return sanitizeds.toArray(new String[sanitizeds.size()]);
    }

    //    /** 秘密鍵（16文字） */
    //    private static byte[] secretKey;
    //
    //    /** 暗号化方式 */
    //    private static String algorithm;
    //
    //    /**
    //     * 文字列を16文字の秘密鍵でAES暗号化してBase64した文字列で返す
    //     *
    //     * @param string
    //     *            対象文字列
    //     * @return 暗号化文字列
    //     */
    //    public static String encrypt(final String string) {
    //
    //        if (string == null) {
    //            return null;
    //        }
    //
    //        byte[] input = string.getBytes();
    //
    //        byte[] encryped = cipher(Cipher.ENCRYPT_MODE, input);
    //
    //        byte[] encoded = Base64.encodeBase64(encryped, false);
    //
    //        String ret = new String(encoded);
    //
    //        LOG.debug("Encrypt [" + string + "] to [" + ret + "].");
    //
    //        return ret;
    //    }
    //
    //    /**
    //     * Base64されたAES暗号化文字列を元の文字列に復元する
    //     *
    //     * @param encryped
    //     *            暗号化文字列
    //     * @return 複合化文字列
    //     */
    //    public static String decrypt(final String encryped) {
    //
    //        if (encryped == null) {
    //            return null;
    //        }
    //
    //        byte[] input = Base64.decodeBase64(encryped);
    //
    //        byte[] decrypted = cipher(Cipher.DECRYPT_MODE, input);
    //
    //        String ret = new String(decrypted);
    //
    //        LOG.debug("Decrypt [" + encryped + "] to [" + ret + "].");
    //
    //        return ret;
    //    }
    //
    //    /**
    //     * 暗号化・複合化の共通部分
    //     *
    //     * @param opmode
    //     *            opmode
    //     * @param input
    //     *            input
    //     * @return byte[]
    //     */
    //    private static byte[] cipher(final int opmode, final byte[] input) {
    //
    //        if (secretKey == null) {
    //            secretKey = App.get("loginfilter.encrypt.secret_key").getBytes();
    //        }
    //
    //        if (algorithm == null) {
    //            algorithm = App.get("loginfilter.encrypt.algorithm");
    //        }
    //
    //        Cipher cipher = null;
    //        try {
    //            cipher = Cipher.getInstance(algorithm);
    //        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
    //            throw new SysError(e);
    //        }
    //
    //        try {
    //            cipher.init(opmode, new SecretKeySpec(secretKey, algorithm));
    //        } catch (InvalidKeyException e) {
    //            throw new SysError(e);
    //        }
    //
    //        byte[] bytes = null;
    //        try {
    //            LOG.trace(String.valueOf(input));
    //            bytes = cipher.doFinal(input);
    //        } catch (IllegalBlockSizeException | BadPaddingException e) {
    //            throw new SysError(e);
    //        }
    //
    //        return bytes;
    //    }

    /** ハッシュアルゴリズム */
    private static MessageDigest md;

    /**
     * @param s 対象文字列
     * @return SHA3-512ハッシュ文字列
     */
    public static String hash(final String s) {

        if (md == null) {
            String algorithm = App.get("loginfilter.hash");
            try {
                md = MessageDigest.getInstance(algorithm);
            } catch (NoSuchAlgorithmException e) {
                throw new SysError(e);
            }
        }

        byte[] bytes = md.digest(s.getBytes());

        return Hex.encodeHexString(bytes);
    }

}
