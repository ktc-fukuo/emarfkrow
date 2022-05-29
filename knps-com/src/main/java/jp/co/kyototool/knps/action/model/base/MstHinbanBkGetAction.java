package jp.co.kyototool.knps.action.model.base;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jp.co.kyototool.knps.entity.MstHinbanBk;

import jp.co.golorp.emarf.action.BaseAction;

/**
 * MST_HINBAN_BK照会
 *
 * @author emarfkrow
 */
public class MstHinbanBkGetAction extends BaseAction {

    /** MST_HINBAN_BK照会処理 */
    @Override
    public Map<String, Object> running(final LocalDateTime now, final String id, final Map<String, Object> postJson) {

        Map<String, Object> map = new HashMap<String, Object>();

        // 主キーが不足していたら終了
        Object hinban = postJson.get("hinban");
        if (hinban == null) {
            hinban = postJson.get("MstHinbanBk.hinban");
        }
        if (hinban == null) {
            return map;
        }
        Object yy = postJson.get("yy");
        if (yy == null) {
            yy = postJson.get("MstHinbanBk.yy");
        }
        if (yy == null) {
            return map;
        }

        MstHinbanBk mstHinbanBk = MstHinbanBk.get(hinban, yy);
        map.put("MstHinbanBk", mstHinbanBk);
        return map;
    }

}