package jp.co.kyototool.knps.action.model.base;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jp.co.kyototool.knps.entity.MNtanka;

import jp.co.golorp.emarf.action.BaseAction;

/**
 * 日産単価マスタ照会
 *
 * @author emarfkrow
 */
public class MNtankaGetAction extends BaseAction {

    /** 日産単価マスタ照会処理 */
    @Override
    public Map<String, Object> running(final LocalDateTime now, final String id, final Map<String, Object> postJson) {

        Map<String, Object> map = new HashMap<String, Object>();

        // 主キーが不足していたら終了
        Object hhinban = postJson.get("hhinban");
        if (hhinban == null) {
            hhinban = postJson.get("MNtanka.hhinban");
        }
        if (hhinban == null) {
            return map;
        }

        MNtanka mNtanka = MNtanka.get(hhinban);
        map.put("MNtanka", mNtanka);
        return map;
    }

}