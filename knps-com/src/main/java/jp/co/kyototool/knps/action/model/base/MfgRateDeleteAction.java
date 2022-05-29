package jp.co.kyototool.knps.action.model.base;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jp.co.kyototool.knps.entity.MfgRate;

import jp.co.golorp.emarf.action.BaseAction;
import jp.co.golorp.emarf.exception.OptLockError;
import jp.co.golorp.emarf.util.Messages;
import jp.co.golorp.emarf.validation.FormValidator;

/**
 * MFG_RATE削除
 *
 * @author emarfkrow
 */
public class MfgRateDeleteAction extends BaseAction {

    /** MFG_RATE削除処理 */
    @Override
    public Map<String, Object> running(final LocalDateTime now, final String id, final Map<String, Object> postJson) {

        // 主キーが不足していたらエラー
        Object rateCode = postJson.get("rateCode");
        if (rateCode == null) {
            rateCode = postJson.get("MfgRate.rateCode");
        }
        if (rateCode == null) {
            throw new OptLockError("error.cant.delete");
        }

        MfgRate e = FormValidator.toBean(MfgRate.class.getName(), postJson);
        if (e.delete() != 1) {
            throw new OptLockError("error.cant.delete");
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("INFO", Messages.get("info.delete"));
        return map;
    }

}