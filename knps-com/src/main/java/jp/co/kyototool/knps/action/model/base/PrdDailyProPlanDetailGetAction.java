package jp.co.kyototool.knps.action.model.base;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jp.co.kyototool.knps.entity.PrdDailyProPlanDetail;

import jp.co.golorp.emarf.action.BaseAction;

/**
 * PRD_DAILY_PRO_PLAN_DETAIL照会
 *
 * @author emarfkrow
 */
public class PrdDailyProPlanDetailGetAction extends BaseAction {

    /** PRD_DAILY_PRO_PLAN_DETAIL照会処理 */
    @Override
    public Map<String, Object> running(final LocalDateTime now, final String id, final Map<String, Object> postJson) {

        Map<String, Object> map = new HashMap<String, Object>();

        // 主キーが不足していたら終了
        Object childPlanNo = postJson.get("childPlanNo");
        if (childPlanNo == null) {
            childPlanNo = postJson.get("PrdDailyProPlanDetail.childPlanNo");
        }
        if (childPlanNo == null) {
            return map;
        }

        PrdDailyProPlanDetail prdDailyProPlanDetail = PrdDailyProPlanDetail.get(childPlanNo);
        map.put("PrdDailyProPlanDetail", prdDailyProPlanDetail);
        return map;
    }

}