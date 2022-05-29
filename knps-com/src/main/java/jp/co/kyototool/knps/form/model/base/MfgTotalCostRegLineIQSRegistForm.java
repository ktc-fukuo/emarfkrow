package jp.co.kyototool.knps.form.model.base;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import jp.co.golorp.emarf.process.BaseProcess;
import jp.co.golorp.emarf.validation.IForm;

/**
 * MFG_TOTAL_COST_REG_LINE_I_Q一覧登録フォーム
 *
 * @author emarfkrow
 */
public class MfgTotalCostRegLineIQSRegistForm implements IForm {

    /** logger */
    private static final Logger LOG = LoggerFactory.getLogger(MfgTotalCostRegLineIQRegistForm.class);

    /** MFG_TOTAL_COST_REG_LINE_I_Q登録フォームのリスト */
    @Valid
    private List<MfgTotalCostRegLineIQRegistForm> mfgTotalCostRegLineIQGrid;

    /**
     * @return MFG_TOTAL_COST_REG_LINE_I_Q登録フォームのリスト
     */
    public List<MfgTotalCostRegLineIQRegistForm> getMfgTotalCostRegLineIQGrid() {
        return mfgTotalCostRegLineIQGrid;
    }

    /**
     * @param p MFG_TOTAL_COST_REG_LINE_I_Q登録フォームのリスト
     */
    public void setMfgTotalCostRegLineIQGrid(final List<MfgTotalCostRegLineIQRegistForm> p) {
        this.mfgTotalCostRegLineIQGrid = p;
    }

    /** 関連チェック */
    @Override
    public void validate(final Map<String, String> errors, final BaseProcess baseProcess) {
        LOG.debug("not overridden in subclasses.");
    }

}