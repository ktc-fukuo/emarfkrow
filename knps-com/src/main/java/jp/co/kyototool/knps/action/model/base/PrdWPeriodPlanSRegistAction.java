package jp.co.kyototool.knps.action.model.base;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.kyototool.knps.entity.PrdWPeriodPlan;

import jp.co.golorp.emarf.action.BaseAction;
import jp.co.golorp.emarf.exception.OptLockError;
import jp.co.golorp.emarf.util.Messages;
import jp.co.golorp.emarf.validation.FormValidator;

/**
 * PRD_W_PERIOD_PLAN一覧登録
 *
 * @author emarfkrow
 */
public class PrdWPeriodPlanSRegistAction extends BaseAction {

    /** PRD_W_PERIOD_PLAN一覧登録処理 */
    @Override
    public Map<String, Object> running(final LocalDateTime now, final String id, final Map<String, Object> postJson) {

        Map<String, Object> map = new HashMap<String, Object>();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> gridData = (List<Map<String, Object>>) postJson.get("PrdWPeriodPlanGrid");

        if (gridData.size() == 0) {
            map.put("ERROR", Messages.get("error.nopost"));
            return map;
        }

        for (Map<String, Object> gridRow : gridData) {

            PrdWPeriodPlan e = FormValidator.toBean(PrdWPeriodPlan.class.getName(), gridRow);

            // 主キーが不足していたらINSERT
            boolean isNew = false;
            if (jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(e.getWPlanId())) {
                isNew = true;
            }

            if (isNew) {

                if (e.insert(now, id) != 1) {
                    throw new OptLockError("error.cant.insert");
                }

            } else {

                if (e.update(now, id) != 1) {
                    throw new OptLockError("error.cant.update");
                }
            }
        }

        map.put("INFO", Messages.get("info.regist"));
        return map;
    }

}