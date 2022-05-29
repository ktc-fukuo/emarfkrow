package jp.co.kyototool.knps.action.model.base;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jp.co.kyototool.knps.entity.PrdYSupBudgetPur;

import jp.co.golorp.emarf.action.BaseAction;

/**
 * PRD_Y_SUP_BUDGET_PUR照会
 *
 * @author emarfkrow
 */
public class PrdYSupBudgetPurGetAction extends BaseAction {

    /** PRD_Y_SUP_BUDGET_PUR照会処理 */
    @Override
    public Map<String, Object> running(final LocalDateTime now, final String id, final Map<String, Object> postJson) {

        Map<String, Object> map = new HashMap<String, Object>();

        // 主キーが不足していたら終了
        Object yyyy = postJson.get("yyyy");
        if (yyyy == null) {
            yyyy = postJson.get("PrdYSupBudgetPur.yyyy");
        }
        if (yyyy == null) {
            return map;
        }
        Object mm = postJson.get("mm");
        if (mm == null) {
            mm = postJson.get("PrdYSupBudgetPur.mm");
        }
        if (mm == null) {
            return map;
        }
        Object hinban = postJson.get("hinban");
        if (hinban == null) {
            hinban = postJson.get("PrdYSupBudgetPur.hinban");
        }
        if (hinban == null) {
            return map;
        }
        Object supCode = postJson.get("supCode");
        if (supCode == null) {
            supCode = postJson.get("PrdYSupBudgetPur.supCode");
        }
        if (supCode == null) {
            return map;
        }

        PrdYSupBudgetPur prdYSupBudgetPur = PrdYSupBudgetPur.get(yyyy, mm, hinban, supCode);
        map.put("PrdYSupBudgetPur", prdYSupBudgetPur);
        return map;
    }

}