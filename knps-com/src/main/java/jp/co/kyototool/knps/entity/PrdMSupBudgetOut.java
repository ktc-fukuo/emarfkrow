package jp.co.kyototool.knps.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.golorp.emarf.entity.IEntity;
import jp.co.golorp.emarf.sql.Queries;

/**
 * PRD_M_SUP_BUDGET_OUT
 *
 * @author emarfkrow
 */
public class PrdMSupBudgetOut implements IEntity {

    /** YYYY */
    private String yyyy;

    /**
     * @return YYYY
     */
    public String getYyyy() {
        return this.yyyy;
    }

    /**
     * @param o YYYY
     */
    public void setYyyy(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.yyyy = String.valueOf(o.toString());
        } else {
            this.yyyy = null;
        }
    }

    /** MM */
    private String mm;

    /**
     * @return MM
     */
    public String getMm() {
        return this.mm;
    }

    /**
     * @param o MM
     */
    public void setMm(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.mm = String.valueOf(o.toString());
        } else {
            this.mm = null;
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

    /** ROUTING */
    private java.math.BigDecimal routing;

    /**
     * @return ROUTING
     */
    public java.math.BigDecimal getRouting() {
        return this.routing;
    }

    /**
     * @param o ROUTING
     */
    public void setRouting(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.routing = new java.math.BigDecimal(o.toString());
        } else {
            this.routing = null;
        }
    }

    /** WC_CODE */
    private String wcCode;

    /**
     * @return WC_CODE
     */
    public String getWcCode() {
        return this.wcCode;
    }

    /**
     * @param o WC_CODE
     */
    public void setWcCode(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.wcCode = String.valueOf(o.toString());
        } else {
            this.wcCode = null;
        }
    }

    /** OPE_DETAIL */
    private String opeDetail;

    /**
     * @return OPE_DETAIL
     */
    public String getOpeDetail() {
        return this.opeDetail;
    }

    /**
     * @param o OPE_DETAIL
     */
    public void setOpeDetail(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.opeDetail = String.valueOf(o.toString());
        } else {
            this.opeDetail = null;
        }
    }

    /** SUP_CODE */
    private String supCode;

    /**
     * @return SUP_CODE
     */
    public String getSupCode() {
        return this.supCode;
    }

    /**
     * @param o SUP_CODE
     */
    public void setSupCode(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.supCode = String.valueOf(o.toString());
        } else {
            this.supCode = null;
        }
    }

    /** ORDER_COUNTS */
    private java.math.BigDecimal orderCounts;

    /**
     * @return ORDER_COUNTS
     */
    public java.math.BigDecimal getOrderCounts() {
        return this.orderCounts;
    }

    /**
     * @param o ORDER_COUNTS
     */
    public void setOrderCounts(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.orderCounts = new java.math.BigDecimal(o.toString());
        } else {
            this.orderCounts = null;
        }
    }

    /** ORDER_UNIT */
    private java.math.BigDecimal orderUnit;

    /**
     * @return ORDER_UNIT
     */
    public java.math.BigDecimal getOrderUnit() {
        return this.orderUnit;
    }

    /**
     * @param o ORDER_UNIT
     */
    public void setOrderUnit(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.orderUnit = new java.math.BigDecimal(o.toString());
        } else {
            this.orderUnit = null;
        }
    }

    /** ORDER_AMOUNT */
    private java.math.BigDecimal orderAmount;

    /**
     * @return ORDER_AMOUNT
     */
    public java.math.BigDecimal getOrderAmount() {
        return this.orderAmount;
    }

    /**
     * @param o ORDER_AMOUNT
     */
    public void setOrderAmount(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.orderAmount = new java.math.BigDecimal(o.toString());
        } else {
            this.orderAmount = null;
        }
    }

    /** ITEM_KBN */
    private java.math.BigDecimal itemKbn;

    /**
     * @return ITEM_KBN
     */
    public java.math.BigDecimal getItemKbn() {
        return this.itemKbn;
    }

    /**
     * @param o ITEM_KBN
     */
    public void setItemKbn(final Object o) {
        if (!jp.co.golorp.emarf.lang.StringUtil.isNullOrBlank(o)) {
            this.itemKbn = new java.math.BigDecimal(o.toString());
        } else {
            this.itemKbn = null;
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
     * PRD_M_SUP_BUDGET_OUT照会
     *
     * @param param1 YYYY
     * @param param2 MM
     * @param param3 HINBAN
     * @param param4 ROUTING
     * @return PRD_M_SUP_BUDGET_OUT
     */
    public static PrdMSupBudgetOut get(final Object param1, final Object param2, final Object param3, final Object param4) {

        List<String> whereList = new ArrayList<String>();
        whereList.add("TRIM (yyyy) = TRIM (:yyyy)");
        whereList.add("TRIM (mm) = TRIM (:mm)");
        whereList.add("TRIM (hinban) = TRIM (:hinban)");
        whereList.add("routing = :routing");

        String sql = "SELECT * FROM PRD_M_SUP_BUDGET_OUT WHERE " + String.join(" AND ", whereList);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("yyyy", param1);
        params.put("mm", param2);
        params.put("hinban", param3);
        params.put("routing", param4);

        return Queries.get(sql, params, PrdMSupBudgetOut.class);
    }

    /**
     * PRD_M_SUP_BUDGET_OUT追加
     *
     * @param now システム日時
     * @param id 登録者
     * @return 追加件数
     */
    public int insert(final LocalDateTime now, final String id) {

        // PRD_STORE_MAINTEの登録
        if (this.prdStoreMainte != null) {
            this.prdStoreMainte.setYyyy(this.getYyyy());
            this.prdStoreMainte.setMm(this.getMm());
            this.prdStoreMainte.setHinban(this.getHinban());
            this.prdStoreMainte.setRouting(this.getRouting());
            this.prdStoreMainte.insert(now, id);
        }

        // PRD_Y_SUP_BUDGET_OUTの登録
        if (this.prdYSupBudgetOut != null) {
            this.prdYSupBudgetOut.setYyyy(this.getYyyy());
            this.prdYSupBudgetOut.setMm(this.getMm());
            this.prdYSupBudgetOut.setHinban(this.getHinban());
            this.prdYSupBudgetOut.setRouting(this.getRouting());
            this.prdYSupBudgetOut.insert(now, id);
        }

        // PRD_M_SUP_BUDGET_OUTの登録
        List<String> nameList = new ArrayList<String>();
        nameList.add("yyyy -- :yyyy");
        nameList.add("mm -- :mm");
        nameList.add("hinban -- :hinban");
        nameList.add("routing -- :routing");
        nameList.add("wc_code -- :wc_code");
        nameList.add("ope_detail -- :ope_detail");
        nameList.add("sup_code -- :sup_code");
        nameList.add("order_counts -- :order_counts");
        nameList.add("order_unit -- :order_unit");
        nameList.add("order_amount -- :order_amount");
        nameList.add("item_kbn -- :item_kbn");
        nameList.add("time_stamp_create -- :time_stamp_create");
        nameList.add("time_stamp_change -- :time_stamp_change");
        nameList.add("user_id_create -- :user_id_create");
        nameList.add("user_id_change -- :user_id_change");
        String name = String.join("\r\n    , ", nameList);

        String sql = "INSERT INTO PRD_M_SUP_BUDGET_OUT(\r\n      " + name + "\r\n) VALUES (\r\n      " + getValues() + "\r\n)";

        Map<String, Object> params = toMap(now, id);

        return Queries.regist(sql, params);
    }

    private String getValues() {
        List<String> valueList = new ArrayList<String>();
        valueList.add(":yyyy");
        valueList.add(":mm");
        valueList.add(":hinban");
        valueList.add(":routing");
        valueList.add(":wc_code");
        valueList.add(":ope_detail");
        valueList.add(":sup_code");
        valueList.add(":order_counts");
        valueList.add(":order_unit");
        valueList.add(":order_amount");
        valueList.add(":item_kbn");
        valueList.add("TO_TIMESTAMP (:time_stamp_create, 'YYYY-MM-DD\"T\"HH24:MI:SS.FF3')");
        valueList.add("TO_TIMESTAMP (:time_stamp_change, 'YYYY-MM-DD\"T\"HH24:MI:SS.FF3')");
        valueList.add(":user_id_create");
        valueList.add(":user_id_change");
        return String.join("\r\n    , ", valueList);
    }

    /**
     * PRD_M_SUP_BUDGET_OUT更新
     *
     * @param now システム日時
     * @param id 更新者
     * @return 更新件数
     */
    public int update(final LocalDateTime now, final String id) {

        // PRD_STORE_MAINTEの登録
        if (this.prdStoreMainte != null) {
            prdStoreMainte.setYyyy(this.getYyyy());
            prdStoreMainte.setMm(this.getMm());
            prdStoreMainte.setHinban(this.getHinban());
            prdStoreMainte.setRouting(this.getRouting());
            try {
                prdStoreMainte.insert(now, id);
            } catch (Exception e) {
                prdStoreMainte.update(now, id);
            }
        }

        // PRD_Y_SUP_BUDGET_OUTの登録
        if (this.prdYSupBudgetOut != null) {
            prdYSupBudgetOut.setYyyy(this.getYyyy());
            prdYSupBudgetOut.setMm(this.getMm());
            prdYSupBudgetOut.setHinban(this.getHinban());
            prdYSupBudgetOut.setRouting(this.getRouting());
            try {
                prdYSupBudgetOut.insert(now, id);
            } catch (Exception e) {
                prdYSupBudgetOut.update(now, id);
            }
        }

        // PRD_M_SUP_BUDGET_OUTの登録
        String sql = "UPDATE PRD_M_SUP_BUDGET_OUT\r\nSET\r\n      " + getSet() + "\r\nWHERE\r\n    " + getWhere();
        Map<String, Object> params = toMap(now, id);
        return Queries.regist(sql, params);
    }

    private String getSet() {
        List<String> setList = new ArrayList<String>();
        setList.add("yyyy = :yyyy");
        setList.add("mm = :mm");
        setList.add("hinban = :hinban");
        setList.add("routing = :routing");
        setList.add("wc_code = :wc_code");
        setList.add("ope_detail = :ope_detail");
        setList.add("sup_code = :sup_code");
        setList.add("order_counts = :order_counts");
        setList.add("order_unit = :order_unit");
        setList.add("order_amount = :order_amount");
        setList.add("item_kbn = :item_kbn");
        setList.add("time_stamp_change = TO_TIMESTAMP (:time_stamp_change, 'YYYY-MM-DD\"T\"HH24:MI:SS.FF3')");
        setList.add("user_id_change = :user_id_change");
        String set = String.join("\r\n    , ", setList);
        return set;
    }

    /**
     * PRD_M_SUP_BUDGET_OUT削除
     *
     * @return 削除件数
     */
    public int delete() {

        // PRD_STORE_MAINTEの削除
        if (this.prdStoreMainte != null) {
            this.prdStoreMainte.delete();
        }

        // PRD_Y_SUP_BUDGET_OUTの削除
        if (this.prdYSupBudgetOut != null) {
            this.prdYSupBudgetOut.delete();
        }

        // PRD_M_SUP_BUDGET_OUTの削除
        String sql = "DELETE FROM PRD_M_SUP_BUDGET_OUT WHERE " + getWhere();

        Map<String, Object> params = toMap(null, null);

        return Queries.regist(sql, params);
    }

    private String getWhere() {
        List<String> whereList = new ArrayList<String>();
        whereList.add("TRIM (yyyy) = TRIM (:yyyy)");
        whereList.add("TRIM (mm) = TRIM (:mm)");
        whereList.add("TRIM (hinban) = TRIM (:hinban)");
        whereList.add("routing = :routing");
        whereList.add("time_stamp_change = TO_TIMESTAMP ('" + this.timeStampChange + "', 'YYYY-MM-DD\"T\"HH24:MI:SS.FF3')");
        return String.join(" AND ", whereList);
    }

    private Map<String, Object> toMap(final LocalDateTime now, final String id) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("YYYY", this.yyyy);
        params.put("MM", this.mm);
        params.put("HINBAN", this.hinban);
        params.put("ROUTING", this.routing);
        params.put("WC_CODE", this.wcCode);
        params.put("OPE_DETAIL", this.opeDetail);
        params.put("SUP_CODE", this.supCode);
        params.put("ORDER_COUNTS", this.orderCounts);
        params.put("ORDER_UNIT", this.orderUnit);
        params.put("ORDER_AMOUNT", this.orderAmount);
        params.put("ITEM_KBN", this.itemKbn);
        params.put("time_stamp_create", now);
        params.put("user_id_create", id);
        params.put("time_stamp_change", now);
        params.put("user_id_change", id);
        return params;
    }

    /**
     * PRD_STORE_MAINTE
     */
    private PrdStoreMainte prdStoreMainte;

    /**
     * @return PRD_STORE_MAINTE
     */
    @com.fasterxml.jackson.annotation.JsonProperty("PrdStoreMainte")
    public PrdStoreMainte getPrdStoreMainte() {
        return this.prdStoreMainte;
    }

    /**
     * @param p PRD_STORE_MAINTE
     */
    public void setPrdStoreMainte(final PrdStoreMainte p) {
        this.prdStoreMainte = p;
    }

    /**
     * @return PRD_STORE_MAINTE
     */
    public PrdStoreMainte referPrdStoreMainte() {
        if (this.prdStoreMainte == null) {
            try {
                this.prdStoreMainte = PrdStoreMainte.get(this.yyyy, this.mm, this.hinban, this.routing);
            } catch (jp.co.golorp.emarf.exception.NoDataError e) {
            }
        }
        return this.prdStoreMainte;
    }

    /**
     * PRD_Y_SUP_BUDGET_OUT
     */
    private PrdYSupBudgetOut prdYSupBudgetOut;

    /**
     * @return PRD_Y_SUP_BUDGET_OUT
     */
    @com.fasterxml.jackson.annotation.JsonProperty("PrdYSupBudgetOut")
    public PrdYSupBudgetOut getPrdYSupBudgetOut() {
        return this.prdYSupBudgetOut;
    }

    /**
     * @param p PRD_Y_SUP_BUDGET_OUT
     */
    public void setPrdYSupBudgetOut(final PrdYSupBudgetOut p) {
        this.prdYSupBudgetOut = p;
    }

    /**
     * @return PRD_Y_SUP_BUDGET_OUT
     */
    public PrdYSupBudgetOut referPrdYSupBudgetOut() {
        if (this.prdYSupBudgetOut == null) {
            try {
                this.prdYSupBudgetOut = PrdYSupBudgetOut.get(this.yyyy, this.mm, this.hinban, this.routing);
            } catch (jp.co.golorp.emarf.exception.NoDataError e) {
            }
        }
        return this.prdYSupBudgetOut;
    }
}