package jp.co.kyototool.knps.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.golorp.emarf.entity.IEntity;
import jp.co.golorp.emarf.sql.Queries;

/**
 * INV_STOCK_MTRL_MNG
 *
 * @author emarfkrow
 */
public class InvStockMtrlMng implements IEntity {

    /** STOCK_MANAGEMENT_SECTION */
    private java.math.BigDecimal stockManagementSection;

    /**
     * @return STOCK_MANAGEMENT_SECTION
     */
    public java.math.BigDecimal getStockManagementSection() {
        return this.stockManagementSection;
    }

    /**
     * @param o STOCK_MANAGEMENT_SECTION
     */
    public void setStockManagementSection(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.stockManagementSection = new java.math.BigDecimal(o.toString());
        } else {
            this.stockManagementSection = null;
        }
    }

    /** HINBAN */
    private String hinban;

    /**
     * @return HINBAN
     */
    public String getHinban() {
        return this.hinban;
    }

    /**
     * @param o HINBAN
     */
    public void setHinban(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.hinban = String.valueOf(o.toString());
        } else {
            this.hinban = null;
        }
    }

    /** SUB_INV_CODE */
    private String subInvCode;

    /**
     * @return SUB_INV_CODE
     */
    public String getSubInvCode() {
        return this.subInvCode;
    }

    /**
     * @param o SUB_INV_CODE
     */
    public void setSubInvCode(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.subInvCode = String.valueOf(o.toString());
        } else {
            this.subInvCode = null;
        }
    }

    /** STOCK */
    private java.math.BigDecimal stock;

    /**
     * @return STOCK
     */
    public java.math.BigDecimal getStock() {
        return this.stock;
    }

    /**
     * @param o STOCK
     */
    public void setStock(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.stock = new java.math.BigDecimal(o.toString());
        } else {
            this.stock = null;
        }
    }

    /** MAIN_STOCK_CODE */
    private String mainStockCode;

    /**
     * @return MAIN_STOCK_CODE
     */
    public String getMainStockCode() {
        return this.mainStockCode;
    }

    /**
     * @param o MAIN_STOCK_CODE
     */
    public void setMainStockCode(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.mainStockCode = String.valueOf(o.toString());
        } else {
            this.mainStockCode = null;
        }
    }

    /** LAST_IN_OUT_DATE */
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer.class)
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer.class)
    private java.time.LocalDateTime lastInOutDate;

    /**
     * @return LAST_IN_OUT_DATE
     */
    public java.time.LocalDateTime getLastInOutDate() {
        return this.lastInOutDate;
    }

    /**
     * @param o LAST_IN_OUT_DATE
     */
    public void setLastInOutDate(final Object o) {
        if (o != null && o instanceof Long) {
            java.util.Date d = new java.util.Date((Long) o);
            this.lastInOutDate = java.time.LocalDateTime.ofInstant(d.toInstant(), java.time.ZoneId.systemDefault());
        } else if (o != null && o.toString().matches("^[0-9]+")) {
            java.util.Date d = new java.util.Date(Long.valueOf(o.toString()));
            this.lastInOutDate = java.time.LocalDateTime.ofInstant(d.toInstant(), java.time.ZoneId.systemDefault());
        } else if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.lastInOutDate = java.time.LocalDateTime.parse(o.toString());
        } else {
            this.lastInOutDate = null;
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

    /** DELETE_FLAG */
    private java.math.BigDecimal deleteFlag;

    /**
     * @return DELETE_FLAG
     */
    public java.math.BigDecimal getDeleteFlag() {
        return this.deleteFlag;
    }

    /**
     * @param o DELETE_FLAG
     */
    public void setDeleteFlag(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.deleteFlag = new java.math.BigDecimal(o.toString());
        } else {
            this.deleteFlag = null;
        }
    }

    /**
     * INV_STOCK_MTRL_MNG照会
     *
     * @param param1 HINBAN
     * @param param2 SUB_INV_CODE
     * @param param3 STOCK_MANAGEMENT_SECTION
     * @return INV_STOCK_MTRL_MNG
     */
    public static InvStockMtrlMng get(final Object param1, final Object param2, final Object param3) {

        List<String> whereList = new ArrayList<String>();
        whereList.add("TRIM (hinban) = TRIM (:hinban)");
        whereList.add("TRIM (sub_inv_code) = TRIM (:sub_inv_code)");
        whereList.add("stock_management_section = :stock_management_section");

        String sql = "SELECT * FROM INV_STOCK_MTRL_MNG WHERE " + String.join(" AND ", whereList);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("hinban", param1);
        params.put("sub_inv_code", param2);
        params.put("stock_management_section", param3);

        return Queries.get(sql, params, InvStockMtrlMng.class);
    }

    /**
     * INV_STOCK_MTRL_MNG追加
     *
     * @param now システム日時
     * @param id 登録者
     * @return 追加件数
     */
    public int insert(final LocalDateTime now, final String id) {

        // INV_STOCK_TOOL_MNG_HEDの登録
        if (this.invStockToolMngHed != null) {
            this.invStockToolMngHed.setHinban(this.getHinban());
            this.invStockToolMngHed.setSubInvCode(this.getSubInvCode());
            this.invStockToolMngHed.setStockManagementSection(this.getStockManagementSection());
            this.invStockToolMngHed.insert(now, id);
        }

        // INV_STOCK_MTRL_MNGの登録
        List<String> nameList = new ArrayList<String>();
        nameList.add("stock_management_section -- :stock_management_section");
        nameList.add("hinban -- :hinban");
        nameList.add("sub_inv_code -- :sub_inv_code");
        nameList.add("stock -- :stock");
        nameList.add("main_stock_code -- :main_stock_code");
        nameList.add("last_in_out_date -- :last_in_out_date");
        nameList.add("time_stamp_create -- :time_stamp_create");
        nameList.add("time_stamp_change -- :time_stamp_change");
        nameList.add("user_id_create -- :user_id_create");
        nameList.add("user_id_change -- :user_id_change");
        nameList.add("delete_flag -- :delete_flag");
        String name = String.join("\r\n    , ", nameList);

        String sql = "INSERT INTO INV_STOCK_MTRL_MNG(\r\n      " + name + "\r\n) VALUES (\r\n      " + getValues() + "\r\n)";

        Map<String, Object> params = toMap(now, id);

        return Queries.regist(sql, params);
    }

    private String getValues() {
        List<String> valueList = new ArrayList<String>();
        valueList.add(":stock_management_section");
        valueList.add(":hinban");
        valueList.add(":sub_inv_code");
        valueList.add(":stock");
        valueList.add(":main_stock_code");
        valueList.add("TO_TIMESTAMP (:last_in_out_date, 'YYYY-MM-DD\"T\"HH24:MI:SS.FF3')");
        valueList.add("TO_TIMESTAMP (:time_stamp_create, 'YYYY-MM-DD\"T\"HH24:MI:SS.FF3')");
        valueList.add("TO_TIMESTAMP (:time_stamp_change, 'YYYY-MM-DD\"T\"HH24:MI:SS.FF3')");
        valueList.add(":user_id_create");
        valueList.add(":user_id_change");
        valueList.add(":delete_flag");
        return String.join("\r\n    , ", valueList);
    }

    /**
     * INV_STOCK_MTRL_MNG更新
     *
     * @param now システム日時
     * @param id 更新者
     * @return 更新件数
     */
    public int update(final LocalDateTime now, final String id) {

        // INV_STOCK_TOOL_MNG_HEDの登録
        if (this.invStockToolMngHed != null) {
            invStockToolMngHed.setHinban(this.getHinban());
            invStockToolMngHed.setSubInvCode(this.getSubInvCode());
            invStockToolMngHed.setStockManagementSection(this.getStockManagementSection());
            try {
                invStockToolMngHed.insert(now, id);
            } catch (Exception e) {
                invStockToolMngHed.update(now, id);
            }
        }

        // INV_STOCK_MTRL_MNGの登録
        String sql = "UPDATE INV_STOCK_MTRL_MNG\r\nSET\r\n      " + getSet() + "\r\nWHERE\r\n    " + getWhere();
        Map<String, Object> params = toMap(now, id);
        return Queries.regist(sql, params);
    }

    private String getSet() {
        List<String> setList = new ArrayList<String>();
        setList.add("stock_management_section = :stock_management_section");
        setList.add("hinban = :hinban");
        setList.add("sub_inv_code = :sub_inv_code");
        setList.add("stock = :stock");
        setList.add("main_stock_code = :main_stock_code");
        setList.add("last_in_out_date = TO_TIMESTAMP (:last_in_out_date, 'YYYY-MM-DD\"T\"HH24:MI:SS.FF3')");
        setList.add("time_stamp_change = TO_TIMESTAMP (:time_stamp_change, 'YYYY-MM-DD\"T\"HH24:MI:SS.FF3')");
        setList.add("user_id_change = :user_id_change");
        setList.add("delete_flag = :delete_flag");
        String set = String.join("\r\n    , ", setList);
        return set;
    }

    /**
     * INV_STOCK_MTRL_MNG削除
     *
     * @return 削除件数
     */
    public int delete() {

        // INV_STOCK_TOOL_MNG_HEDの削除
        if (this.invStockToolMngHed != null) {
            this.invStockToolMngHed.delete();
        }

        // INV_STOCK_MTRL_MNGの削除
        String sql = "DELETE FROM INV_STOCK_MTRL_MNG WHERE " + getWhere();

        Map<String, Object> params = toMap(null, null);

        return Queries.regist(sql, params);
    }

    private String getWhere() {
        List<String> whereList = new ArrayList<String>();
        whereList.add("TRIM (hinban) = TRIM (:hinban)");
        whereList.add("TRIM (sub_inv_code) = TRIM (:sub_inv_code)");
        whereList.add("stock_management_section = :stock_management_section");
        whereList.add("time_stamp_change = TO_TIMESTAMP ('" + this.timeStampChange + "', 'YYYY-MM-DD\"T\"HH24:MI:SS.FF3')");
        return String.join(" AND ", whereList);
    }

    private Map<String, Object> toMap(final LocalDateTime now, final String id) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("STOCK_MANAGEMENT_SECTION", this.stockManagementSection);
        params.put("HINBAN", this.hinban);
        params.put("SUB_INV_CODE", this.subInvCode);
        params.put("STOCK", this.stock);
        params.put("MAIN_STOCK_CODE", this.mainStockCode);
        params.put("LAST_IN_OUT_DATE", this.lastInOutDate);
        params.put("DELETE_FLAG", this.deleteFlag);
        params.put("time_stamp_create", now);
        params.put("user_id_create", id);
        params.put("time_stamp_change", now);
        params.put("user_id_change", id);
        return params;
    }

    /**
     * INV_STOCK_TOOL_MNG_HED
     */
    private InvStockToolMngHed invStockToolMngHed;

    /**
     * @return INV_STOCK_TOOL_MNG_HED
     */
    @com.fasterxml.jackson.annotation.JsonProperty("InvStockToolMngHed")
    public InvStockToolMngHed getInvStockToolMngHed() {
        return this.invStockToolMngHed;
    }

    /**
     * @param p INV_STOCK_TOOL_MNG_HED
     */
    public void setInvStockToolMngHed(final InvStockToolMngHed p) {
        this.invStockToolMngHed = p;
    }

    /**
     * @return INV_STOCK_TOOL_MNG_HED
     */
    public InvStockToolMngHed referInvStockToolMngHed() {
        if (this.invStockToolMngHed == null) {
            try {
                this.invStockToolMngHed = InvStockToolMngHed.get(this.hinban, this.subInvCode, this.stockManagementSection);
            } catch (jp.co.golorp.emarf.exception.NoDataError e) {
            }
        }
        return this.invStockToolMngHed;
    }
}