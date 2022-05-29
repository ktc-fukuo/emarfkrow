package jp.co.kyototool.knps.action.model.base;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jp.co.kyototool.knps.entity.MfgKanbanStatus;

import jp.co.golorp.emarf.action.BaseAction;
import jp.co.golorp.emarf.exception.OptLockError;
import jp.co.golorp.emarf.util.Messages;
import jp.co.golorp.emarf.validation.FormValidator;

/**
 * MFG_KANBAN_STATUS削除
 *
 * @author emarfkrow
 */
public class MfgKanbanStatusDeleteAction extends BaseAction {

    /** MFG_KANBAN_STATUS削除処理 */
    @Override
    public Map<String, Object> running(final LocalDateTime now, final String id, final Map<String, Object> postJson) {

        // 主キーが不足していたらエラー
        Object kanbanId = postJson.get("kanbanId");
        if (kanbanId == null) {
            kanbanId = postJson.get("MfgKanbanStatus.kanbanId");
        }
        if (kanbanId == null) {
            throw new OptLockError("error.cant.delete");
        }
        Object serialNo = postJson.get("serialNo");
        if (serialNo == null) {
            serialNo = postJson.get("MfgKanbanStatus.serialNo");
        }
        if (serialNo == null) {
            throw new OptLockError("error.cant.delete");
        }

        MfgKanbanStatus e = FormValidator.toBean(MfgKanbanStatus.class.getName(), postJson);
        if (e.delete() != 1) {
            throw new OptLockError("error.cant.delete");
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("INFO", Messages.get("info.delete"));
        return map;
    }

}