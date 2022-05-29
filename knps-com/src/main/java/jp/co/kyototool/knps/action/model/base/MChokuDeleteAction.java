package jp.co.kyototool.knps.action.model.base;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jp.co.kyototool.knps.entity.MChoku;

import jp.co.golorp.emarf.action.BaseAction;
import jp.co.golorp.emarf.exception.OptLockError;
import jp.co.golorp.emarf.util.Messages;
import jp.co.golorp.emarf.validation.FormValidator;

/**
 * M_CHOKU削除
 *
 * @author emarfkrow
 */
public class MChokuDeleteAction extends BaseAction {

    /** M_CHOKU削除処理 */
    @Override
    public Map<String, Object> running(final LocalDateTime now, final String id, final Map<String, Object> postJson) {

        // 主キーが不足していたらエラー
        Object chokucd = postJson.get("chokucd");
        if (chokucd == null) {
            chokucd = postJson.get("MChoku.chokucd");
        }
        if (chokucd == null) {
            throw new OptLockError("error.cant.delete");
        }

        MChoku e = FormValidator.toBean(MChoku.class.getName(), postJson);
        if (e.delete() != 1) {
            throw new OptLockError("error.cant.delete");
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("INFO", Messages.get("info.delete"));
        return map;
    }

}