package jp.co.kyototool.knps.action.model.base;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jp.co.kyototool.knps.entity.MstWs;

import jp.co.golorp.emarf.action.BaseAction;
import jp.co.golorp.emarf.exception.OptLockError;
import jp.co.golorp.emarf.util.Messages;
import jp.co.golorp.emarf.validation.FormValidator;

/**
 * MST_WS削除
 *
 * @author emarfkrow
 */
public class MstWsDeleteAction extends BaseAction {

    /** MST_WS削除処理 */
    @Override
    public Map<String, Object> running(final LocalDateTime now, final String id, final Map<String, Object> postJson) {

        // 主キーが不足していたらエラー
        Object wsCode = postJson.get("wsCode");
        if (wsCode == null) {
            wsCode = postJson.get("MstWs.wsCode");
        }
        if (wsCode == null) {
            throw new OptLockError("error.cant.delete");
        }

        MstWs e = FormValidator.toBean(MstWs.class.getName(), postJson);
        if (e.delete() != 1) {
            throw new OptLockError("error.cant.delete");
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("INFO", Messages.get("info.delete"));
        return map;
    }

}