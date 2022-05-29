package jp.co.kyototool.knps.form.model.base;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.golorp.emarf.process.BaseProcess;
import jp.co.golorp.emarf.validation.IForm;

/**
 * MST_STRUCTURE登録フォーム
 *
 * @author emarfkrow
 */
public class MstStructureRegistForm implements IForm {

    /** logger */
    private static final Logger LOG = LoggerFactory.getLogger(MstStructureRegistForm.class);

    /** MAN_HINBAN */
    @jakarta.validation.constraints.Size(max = 25)
    private String manHinban;

    /**
     * @return MAN_HINBAN
     */
    public String getManHinban() {
        return manHinban;
    }

    /**
     * @param p MAN_HINBAN
     */
    public void setManHinban(final String p) {
        this.manHinban = p;
    }

    /** HINBAN */
    @jakarta.validation.constraints.Size(max = 25)
    private String hinban;

    /**
     * @return HINBAN
     */
    public String getHinban() {
        return hinban;
    }

    /**
     * @param p HINBAN
     */
    public void setHinban(final String p) {
        this.hinban = p;
    }

    /** COUNTS */
    @jakarta.validation.constraints.Size(max = 11)
    private String counts;

    /**
     * @return COUNTS
     */
    public String getCounts() {
        return counts;
    }

    /**
     * @param p COUNTS
     */
    public void setCounts(final String p) {
        this.counts = p;
    }

    /** DELETE_FLAG */
    @jakarta.validation.constraints.Size(max = 1)
    private String deleteFlag;

    /**
     * @return DELETE_FLAG
     */
    public String getDeleteFlag() {
        return deleteFlag;
    }

    /**
     * @param p DELETE_FLAG
     */
    public void setDeleteFlag(final String p) {
        this.deleteFlag = p;
    }

    /** BOZAI_F */
    @jakarta.validation.constraints.Size(max = 1)
    private String bozaiF;

    /**
     * @return BOZAI_F
     */
    public String getBozaiF() {
        return bozaiF;
    }

    /**
     * @param p BOZAI_F
     */
    public void setBozaiF(final String p) {
        this.bozaiF = p;
    }

    /** MST_STRUCTURE_BK */
    @jakarta.validation.Valid
    private List<MstStructureBkRegistForm> mstStructureBkGrid;

    /**
     * @return MST_STRUCTURE_BK
     */
    public List<MstStructureBkRegistForm> getMstStructureBkGrid() {
        return mstStructureBkGrid;
    }

    /**
     * @param p
     */
    public void setMstStructureBkGrid(final List<MstStructureBkRegistForm> p) {
        this.mstStructureBkGrid = p;
    }

    /** 関連チェック */
    @Override
    public void validate(final Map<String, String> errors, final BaseProcess baseProcess) {
        LOG.trace("not overridden in subclasses.");
    }

}