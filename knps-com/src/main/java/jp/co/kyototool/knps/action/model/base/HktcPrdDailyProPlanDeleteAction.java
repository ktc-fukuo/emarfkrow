package jp.co.kyototool.knps.action.model.base;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jp.co.kyototool.knps.entity.HktcPrdDailyProPlan;

import jp.co.golorp.emarf.action.BaseAction;
import jp.co.golorp.emarf.exception.OptLockError;
import jp.co.golorp.emarf.util.Messages;
import jp.co.golorp.emarf.validation.FormValidator;

/**
 * HKTC_PRD_DAILY_PRO_PLAN削除
 *
 * @author emarfkrow
 */
public class HktcPrdDailyProPlanDeleteAction extends BaseAction {

    /** HKTC_PRD_DAILY_PRO_PLAN削除処理 */
    @Override
    public Map<String, Object> running(final LocalDateTime now, final String id, final Map<String, Object> postJson) {

        // 主キーが不足していたらエラー
        Object planNo = postJson.get("planNo");
        if (planNo == null) {
            planNo = postJson.get("HktcPrdDailyProPlan.planNo");
        }
        if (planNo == null) {
            throw new OptLockError("error.cant.delete");
        }

        HktcPrdDailyProPlan e = FormValidator.toBean(HktcPrdDailyProPlan.class.getName(), postJson);
        if (e.delete() != 1) {
            throw new OptLockError("error.cant.delete");
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("INFO", Messages.get("info.delete"));
        return map;
    }

}