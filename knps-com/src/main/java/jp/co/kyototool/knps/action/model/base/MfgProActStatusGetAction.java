package jp.co.kyototool.knps.action.model.base;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jp.co.kyototool.knps.entity.MfgProActStatus;

import jp.co.golorp.emarf.action.BaseAction;

/**
 * MFG_PRO_ACT_STATUS照会
 *
 * @author emarfkrow
 */
public class MfgProActStatusGetAction extends BaseAction {

    /** MFG_PRO_ACT_STATUS照会処理 */
    @Override
    public Map<String, Object> running(final LocalDateTime now, final String id, final Map<String, Object> postJson) {

        Map<String, Object> map = new HashMap<String, Object>();

        // 主キーが不足していたら終了
        Object childPlanNo = postJson.get("childPlanNo");
        if (childPlanNo == null) {
            childPlanNo = postJson.get("MfgProActStatus.childPlanNo");
        }
        if (childPlanNo == null) {
            return map;
        }

        MfgProActStatus mfgProActStatus = MfgProActStatus.get(childPlanNo);
        mfgProActStatus.referPrdDailyProPlanDetail();
        map.put("MfgProActStatus", mfgProActStatus);
        return map;
    }

}