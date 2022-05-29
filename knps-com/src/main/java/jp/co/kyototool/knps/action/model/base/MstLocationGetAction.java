package jp.co.kyototool.knps.action.model.base;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jp.co.kyototool.knps.entity.MstLocation;

import jp.co.golorp.emarf.action.BaseAction;

/**
 * MST_LOCATION照会
 *
 * @author emarfkrow
 */
public class MstLocationGetAction extends BaseAction {

    /** MST_LOCATION照会処理 */
    @Override
    public Map<String, Object> running(final LocalDateTime now, final String id, final Map<String, Object> postJson) {

        Map<String, Object> map = new HashMap<String, Object>();

        // 主キーが不足していたら終了
        Object locationCode = postJson.get("locationCode");
        if (locationCode == null) {
            locationCode = postJson.get("MstLocation.locationCode");
        }
        if (locationCode == null) {
            return map;
        }

        MstLocation mstLocation = MstLocation.get(locationCode);
        map.put("MstLocation", mstLocation);
        return map;
    }

}