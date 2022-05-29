package jp.co.kyototool.knps.action.model.base;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jp.co.kyototool.knps.entity.InvSurveyResultHisHalf;

import jp.co.golorp.emarf.action.BaseAction;
import jp.co.golorp.emarf.exception.OptLockError;
import jp.co.golorp.emarf.util.Messages;
import jp.co.golorp.emarf.validation.FormValidator;

/**
 * INV_SURVEY_RESULT_HIS_HALF削除
 *
 * @author emarfkrow
 */
public class InvSurveyResultHisHalfDeleteAction extends BaseAction {

    /** INV_SURVEY_RESULT_HIS_HALF削除処理 */
    @Override
    public Map<String, Object> running(final LocalDateTime now, final String id, final Map<String, Object> postJson) {

        // 主キーが不足していたらエラー
        Object discriminationId = postJson.get("discriminationId");
        if (discriminationId == null) {
            discriminationId = postJson.get("InvSurveyResultHisHalf.discriminationId");
        }
        if (discriminationId == null) {
            throw new OptLockError("error.cant.delete");
        }
        Object yyyy = postJson.get("yyyy");
        if (yyyy == null) {
            yyyy = postJson.get("InvSurveyResultHisHalf.yyyy");
        }
        if (yyyy == null) {
            throw new OptLockError("error.cant.delete");
        }
        Object mm = postJson.get("mm");
        if (mm == null) {
            mm = postJson.get("InvSurveyResultHisHalf.mm");
        }
        if (mm == null) {
            throw new OptLockError("error.cant.delete");
        }

        InvSurveyResultHisHalf e = FormValidator.toBean(InvSurveyResultHisHalf.class.getName(), postJson);
        if (e.delete() != 1) {
            throw new OptLockError("error.cant.delete");
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("INFO", Messages.get("info.delete"));
        return map;
    }

}