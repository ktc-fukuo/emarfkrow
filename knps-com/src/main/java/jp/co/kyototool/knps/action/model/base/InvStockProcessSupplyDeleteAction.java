package jp.co.kyototool.knps.action.model.base;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jp.co.kyototool.knps.entity.InvStockProcessSupply;

import jp.co.golorp.emarf.action.BaseAction;
import jp.co.golorp.emarf.exception.OptLockError;
import jp.co.golorp.emarf.util.Messages;
import jp.co.golorp.emarf.validation.FormValidator;

/**
 * INV_STOCK_PROCESS_SUPPLY削除
 *
 * @author emarfkrow
 */
public class InvStockProcessSupplyDeleteAction extends BaseAction {

    /** INV_STOCK_PROCESS_SUPPLY削除処理 */
    @Override
    public Map<String, Object> running(final LocalDateTime now, final String id, final Map<String, Object> postJson) {

        // 主キーが不足していたらエラー
        Object supCode = postJson.get("supCode");
        if (supCode == null) {
            supCode = postJson.get("InvStockProcessSupply.supCode");
        }
        if (supCode == null) {
            throw new OptLockError("error.cant.delete");
        }
        Object hinban = postJson.get("hinban");
        if (hinban == null) {
            hinban = postJson.get("InvStockProcessSupply.hinban");
        }
        if (hinban == null) {
            throw new OptLockError("error.cant.delete");
        }

        InvStockProcessSupply e = FormValidator.toBean(InvStockProcessSupply.class.getName(), postJson);
        if (e.delete() != 1) {
            throw new OptLockError("error.cant.delete");
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("INFO", Messages.get("info.delete"));
        return map;
    }

}