package jp.co.kyototool.knps.action.model.base;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jp.co.kyototool.knps.entity.PchSupplySch;

import jp.co.golorp.emarf.action.BaseAction;
import jp.co.golorp.emarf.exception.OptLockError;
import jp.co.golorp.emarf.util.Messages;
import jp.co.golorp.emarf.validation.FormValidator;

/**
 * PCH_SUPPLY_SCH削除
 *
 * @author emarfkrow
 */
public class PchSupplySchDeleteAction extends BaseAction {

    /** PCH_SUPPLY_SCH削除処理 */
    @Override
    public Map<String, Object> running(final LocalDateTime now, final String id, final Map<String, Object> postJson) {

        // 主キーが不足していたらエラー
        Object supplyNo = postJson.get("supplyNo");
        if (supplyNo == null) {
            supplyNo = postJson.get("PchSupplySch.supplyNo");
        }
        if (supplyNo == null) {
            throw new OptLockError("error.cant.delete");
        }

        PchSupplySch e = FormValidator.toBean(PchSupplySch.class.getName(), postJson);
        if (e.delete() != 1) {
            throw new OptLockError("error.cant.delete");
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("INFO", Messages.get("info.delete"));
        return map;
    }

}