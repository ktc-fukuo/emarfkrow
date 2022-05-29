package jp.co.kyototool.knps.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.golorp.emarf.entity.IEntity;
import jp.co.golorp.emarf.sql.Queries;

/**
 * PCH_TEMP_ORDER_DET
 *
 * @author emarfkrow
 */
public class PchTempOrderDet implements IEntity {

    /** TEMP_ORDER_NO */
    private String tempOrderNo;

    /**
     * @return TEMP_ORDER_NO
     */
    public String getTempOrderNo() {
        return this.tempOrderNo;
    }

    /**
     * @param o TEMP_ORDER_NO
     */
    public void setTempOrderNo(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.tempOrderNo = String.valueOf(o.toString());
        } else {
            this.tempOrderNo = null;
        }
    }

    /** TEMP_ORDER_SEQ */
    private java.math.BigDecimal tempOrderSeq;

    /**
     * @return TEMP_ORDER_SEQ
     */
    public java.math.BigDecimal getTempOrderSeq() {
        return this.tempOrderSeq;
    }

    /**
     * @param o TEMP_ORDER_SEQ
     */
    public void setTempOrderSeq(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.tempOrderSeq = new java.math.BigDecimal(o.toString());
        } else {
            this.tempOrderSeq = null;
        }
    }

    /** ORDER_NO */
    private String orderNo;

    /**
     * @return ORDER_NO
     */
    public String getOrderNo() {
        return this.orderNo;
    }

    /**
     * @param o ORDER_NO
     */
    public void setOrderNo(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.orderNo = String.valueOf(o.toString());
        } else {
            this.orderNo = null;
        }
    }

    /** KBN2 */
    private String kbn2;

    /**
     * @return KBN2
     */
    public String getKbn2() {
        return this.kbn2;
    }

    /**
     * @param o KBN2
     */
    public void setKbn2(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.kbn2 = String.valueOf(o.toString());
        } else {
            this.kbn2 = null;
        }
    }

    /** ITEM */
    private String item;

    /**
     * @return ITEM
     */
    public String getItem() {
        return this.item;
    }

    /**
     * @param o ITEM
     */
    public void setItem(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.item = String.valueOf(o.toString());
        } else {
            this.item = null;
        }
    }

    /** MATERIAL */
    private String material;

    /**
     * @return MATERIAL
     */
    public String getMaterial() {
        return this.material;
    }

    /**
     * @param o MATERIAL
     */
    public void setMaterial(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.material = String.valueOf(o.toString());
        } else {
            this.material = null;
        }
    }

    /** ORDER_COUNTS_1 */
    private java.math.BigDecimal orderCounts1;

    /**
     * @return ORDER_COUNTS_1
     */
    public java.math.BigDecimal getOrderCounts1() {
        return this.orderCounts1;
    }

    /**
     * @param o ORDER_COUNTS_1
     */
    public void setOrderCounts1(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.orderCounts1 = new java.math.BigDecimal(o.toString());
        } else {
            this.orderCounts1 = null;
        }
    }

    /** UNIT_NAME */
    private String unitName;

    /**
     * @return UNIT_NAME
     */
    public String getUnitName() {
        return this.unitName;
    }

    /**
     * @param o UNIT_NAME
     */
    public void setUnitName(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.unitName = String.valueOf(o.toString());
        } else {
            this.unitName = null;
        }
    }

    /** ORDER_COUNTS_2 */
    private java.math.BigDecimal orderCounts2;

    /**
     * @return ORDER_COUNTS_2
     */
    public java.math.BigDecimal getOrderCounts2() {
        return this.orderCounts2;
    }

    /**
     * @param o ORDER_COUNTS_2
     */
    public void setOrderCounts2(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.orderCounts2 = new java.math.BigDecimal(o.toString());
        } else {
            this.orderCounts2 = null;
        }
    }

    /** UNIT_NAME_2 */
    private String unitName2;

    /**
     * @return UNIT_NAME_2
     */
    public String getUnitName2() {
        return this.unitName2;
    }

    /**
     * @param o UNIT_NAME_2
     */
    public void setUnitName2(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.unitName2 = String.valueOf(o.toString());
        } else {
            this.unitName2 = null;
        }
    }

    /** ISSHIKI_F */
    private String isshikiF;

    /**
     * @return ISSHIKI_F
     */
    public String getIsshikiF() {
        return this.isshikiF;
    }

    /**
     * @param o ISSHIKI_F
     */
    public void setIsshikiF(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.isshikiF = String.valueOf(o.toString());
        } else {
            this.isshikiF = null;
        }
    }

    /** FUTAI_F */
    private String futaiF;

    /**
     * @return FUTAI_F
     */
    public String getFutaiF() {
        return this.futaiF;
    }

    /**
     * @param o FUTAI_F
     */
    public void setFutaiF(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.futaiF = String.valueOf(o.toString());
        } else {
            this.futaiF = null;
        }
    }

    /** IKKATSU_ORDER_NO */
    private String ikkatsuOrderNo;

    /**
     * @return IKKATSU_ORDER_NO
     */
    public String getIkkatsuOrderNo() {
        return this.ikkatsuOrderNo;
    }

    /**
     * @param o IKKATSU_ORDER_NO
     */
    public void setIkkatsuOrderNo(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.ikkatsuOrderNo = String.valueOf(o.toString());
        } else {
            this.ikkatsuOrderNo = null;
        }
    }

    /** TIME_STAMP_CREATE */
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer.class)
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer.class)
    private java.time.LocalDateTime timeStampCreate;

    /**
     * @return TIME_STAMP_CREATE
     */
    public java.time.LocalDateTime getTimeStampCreate() {
        return this.timeStampCreate;
    }

    /**
     * @param o TIME_STAMP_CREATE
     */
    public void setTimeStampCreate(final Object o) {
        if (o != null && o instanceof Long) {
            java.util.Date d = new java.util.Date((Long) o);
            this.timeStampCreate = java.time.LocalDateTime.ofInstant(d.toInstant(), java.time.ZoneId.systemDefault());
        } else if (o != null && o.toString().matches("^[0-9]+")) {
            java.util.Date d = new java.util.Date(Long.valueOf(o.toString()));
            this.timeStampCreate = java.time.LocalDateTime.ofInstant(d.toInstant(), java.time.ZoneId.systemDefault());
        } else if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.timeStampCreate = java.time.LocalDateTime.parse(o.toString());
        } else {
            this.timeStampCreate = null;
        }
    }

    /** TIME_STAMP_CHANGE */
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer.class)
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer.class)
    private java.time.LocalDateTime timeStampChange;

    /**
     * @return TIME_STAMP_CHANGE
     */
    public java.time.LocalDateTime getTimeStampChange() {
        return this.timeStampChange;
    }

    /**
     * @param o TIME_STAMP_CHANGE
     */
    public void setTimeStampChange(final Object o) {
        if (o != null && o instanceof Long) {
            java.util.Date d = new java.util.Date((Long) o);
            this.timeStampChange = java.time.LocalDateTime.ofInstant(d.toInstant(), java.time.ZoneId.systemDefault());
        } else if (o != null && o.toString().matches("^[0-9]+")) {
            java.util.Date d = new java.util.Date(Long.valueOf(o.toString()));
            this.timeStampChange = java.time.LocalDateTime.ofInstant(d.toInstant(), java.time.ZoneId.systemDefault());
        } else if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.timeStampChange = java.time.LocalDateTime.parse(o.toString());
        } else {
            this.timeStampChange = null;
        }
    }

    /** USER_ID_CREATE */
    private String userIdCreate;

    /**
     * @return USER_ID_CREATE
     */
    public String getUserIdCreate() {
        return this.userIdCreate;
    }

    /**
     * @param o USER_ID_CREATE
     */
    public void setUserIdCreate(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.userIdCreate = String.valueOf(o.toString());
        } else {
            this.userIdCreate = null;
        }
    }

    /** USER_ID_CHANGE */
    private String userIdChange;

    /**
     * @return USER_ID_CHANGE
     */
    public String getUserIdChange() {
        return this.userIdChange;
    }

    /**
     * @param o USER_ID_CHANGE
     */
    public void setUserIdChange(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.userIdChange = String.valueOf(o.toString());
        } else {
            this.userIdChange = null;
        }
    }

    /**
     * PCH_TEMP_ORDER_DET照会
     *
     * @param param1 TEMP_ORDER_NO
     * @param param2 TEMP_ORDER_SEQ
     * @return PCH_TEMP_ORDER_DET
     */
    public static PchTempOrderDet get(final Object param1, final Object param2) {

        List<String> whereList = new ArrayList<String>();
        whereList.add("TRIM (temp_order_no) = TRIM (:temp_order_no)");
        whereList.add("temp_order_seq = :temp_order_seq");

        String sql = "SELECT * FROM PCH_TEMP_ORDER_DET WHERE " + String.join(" AND ", whereList);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("temp_order_no", param1);
        params.put("temp_order_seq", param2);

        return Queries.get(sql, params, PchTempOrderDet.class);
    }

    /**
     * PCH_TEMP_ORDER_DET追加
     *
     * @param now システム日時
     * @param id 登録者
     * @return 追加件数
     */
    public int insert(final LocalDateTime now, final String id) {

        // PCH_TEMP_ORDER_DETの登録
        List<String> nameList = new ArrayList<String>();
        nameList.add("temp_order_no -- :temp_order_no");
        nameList.add("temp_order_seq -- :temp_order_seq");
        nameList.add("order_no -- :order_no");
        nameList.add("kbn2 -- :kbn2");
        nameList.add("item -- :item");
        nameList.add("material -- :material");
        nameList.add("order_counts_1 -- :order_counts_1");
        nameList.add("unit_name -- :unit_name");
        nameList.add("order_counts_2 -- :order_counts_2");
        nameList.add("unit_name_2 -- :unit_name_2");
        nameList.add("isshiki_f -- :isshiki_f");
        nameList.add("futai_f -- :futai_f");
        nameList.add("ikkatsu_order_no -- :ikkatsu_order_no");
        nameList.add("time_stamp_create -- :time_stamp_create");
        nameList.add("time_stamp_change -- :time_stamp_change");
        nameList.add("user_id_create -- :user_id_create");
        nameList.add("user_id_change -- :user_id_change");
        String name = String.join("\r\n    , ", nameList);

        String sql = "INSERT INTO PCH_TEMP_ORDER_DET(\r\n      " + name + "\r\n) VALUES (\r\n      " + getValues() + "\r\n)";

        Map<String, Object> params = toMap(now, id);

        return Queries.regist(sql, params);
    }

    private String getValues() {
        List<String> valueList = new ArrayList<String>();
        valueList.add(":temp_order_no");
        valueList.add(":temp_order_seq");
        valueList.add(":order_no");
        valueList.add(":kbn2");
        valueList.add(":item");
        valueList.add(":material");
        valueList.add(":order_counts_1");
        valueList.add(":unit_name");
        valueList.add(":order_counts_2");
        valueList.add(":unit_name_2");
        valueList.add(":isshiki_f");
        valueList.add(":futai_f");
        valueList.add(":ikkatsu_order_no");
        valueList.add("TO_TIMESTAMP (:time_stamp_create, 'YYYY-MM-DD\"T\"HH24:MI:SS.FF3')");
        valueList.add("TO_TIMESTAMP (:time_stamp_change, 'YYYY-MM-DD\"T\"HH24:MI:SS.FF3')");
        valueList.add(":user_id_create");
        valueList.add(":user_id_change");
        return String.join("\r\n    , ", valueList);
    }

    /**
     * PCH_TEMP_ORDER_DET更新
     *
     * @param now システム日時
     * @param id 更新者
     * @return 更新件数
     */
    public int update(final LocalDateTime now, final String id) {

        // PCH_TEMP_ORDER_DETの登録
        String sql = "UPDATE PCH_TEMP_ORDER_DET\r\nSET\r\n      " + getSet() + "\r\nWHERE\r\n    " + getWhere();
        Map<String, Object> params = toMap(now, id);
        return Queries.regist(sql, params);
    }

    private String getSet() {
        List<String> setList = new ArrayList<String>();
        setList.add("temp_order_no = :temp_order_no");
        setList.add("temp_order_seq = :temp_order_seq");
        setList.add("order_no = :order_no");
        setList.add("kbn2 = :kbn2");
        setList.add("item = :item");
        setList.add("material = :material");
        setList.add("order_counts_1 = :order_counts_1");
        setList.add("unit_name = :unit_name");
        setList.add("order_counts_2 = :order_counts_2");
        setList.add("unit_name_2 = :unit_name_2");
        setList.add("isshiki_f = :isshiki_f");
        setList.add("futai_f = :futai_f");
        setList.add("ikkatsu_order_no = :ikkatsu_order_no");
        setList.add("time_stamp_change = TO_TIMESTAMP (:time_stamp_change, 'YYYY-MM-DD\"T\"HH24:MI:SS.FF3')");
        setList.add("user_id_change = :user_id_change");
        String set = String.join("\r\n    , ", setList);
        return set;
    }

    /**
     * PCH_TEMP_ORDER_DET削除
     *
     * @return 削除件数
     */
    public int delete() {

        // PCH_TEMP_ORDER_DETの削除
        String sql = "DELETE FROM PCH_TEMP_ORDER_DET WHERE " + getWhere();

        Map<String, Object> params = toMap(null, null);

        return Queries.regist(sql, params);
    }

    private String getWhere() {
        List<String> whereList = new ArrayList<String>();
        whereList.add("TRIM (temp_order_no) = TRIM (:temp_order_no)");
        whereList.add("temp_order_seq = :temp_order_seq");
        whereList.add("time_stamp_change = TO_TIMESTAMP ('" + this.timeStampChange + "', 'YYYY-MM-DD\"T\"HH24:MI:SS.FF3')");
        return String.join(" AND ", whereList);
    }

    private Map<String, Object> toMap(final LocalDateTime now, final String id) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("TEMP_ORDER_NO", this.tempOrderNo);
        params.put("TEMP_ORDER_SEQ", this.tempOrderSeq);
        params.put("ORDER_NO", this.orderNo);
        params.put("KBN2", this.kbn2);
        params.put("ITEM", this.item);
        params.put("MATERIAL", this.material);
        params.put("ORDER_COUNTS_1", this.orderCounts1);
        params.put("UNIT_NAME", this.unitName);
        params.put("ORDER_COUNTS_2", this.orderCounts2);
        params.put("UNIT_NAME_2", this.unitName2);
        params.put("ISSHIKI_F", this.isshikiF);
        params.put("FUTAI_F", this.futaiF);
        params.put("IKKATSU_ORDER_NO", this.ikkatsuOrderNo);
        params.put("time_stamp_create", now);
        params.put("user_id_create", id);
        params.put("time_stamp_change", now);
        params.put("user_id_change", id);
        return params;
    }
}