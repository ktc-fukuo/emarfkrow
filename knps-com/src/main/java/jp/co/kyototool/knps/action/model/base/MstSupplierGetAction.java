package jp.co.kyototool.knps.action.model.base;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jp.co.kyototool.knps.entity.MstSupplier;

import jp.co.golorp.emarf.action.BaseAction;

/**
 * MST_SUPPLIER照会
 *
 * @author emarfkrow
 */
public class MstSupplierGetAction extends BaseAction {

    /** MST_SUPPLIER照会処理 */
    @Override
    public Map<String, Object> running(final LocalDateTime now, final String id, final Map<String, Object> postJson) {

        Map<String, Object> map = new HashMap<String, Object>();

        // 主キーが不足していたら終了
        Object supCode = postJson.get("supCode");
        if (supCode == null) {
            supCode = postJson.get("MstSupplier.supCode");
        }
        if (supCode == null) {
            return map;
        }

        MstSupplier mstSupplier = MstSupplier.get(supCode);
        mstSupplier.referInvStockProcessSupplys();
        mstSupplier.referMstSupplierBks();
        map.put("MstSupplier", mstSupplier);
        return map;
    }

}