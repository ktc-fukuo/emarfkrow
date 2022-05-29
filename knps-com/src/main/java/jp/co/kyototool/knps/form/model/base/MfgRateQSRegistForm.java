package jp.co.kyototool.knps.form.model.base;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import jp.co.golorp.emarf.process.BaseProcess;
import jp.co.golorp.emarf.validation.IForm;

/**
 * MFG_RATE_Q一覧登録フォーム
 *
 * @author emarfkrow
 */
public class MfgRateQSRegistForm implements IForm {

    /** logger */
    private static final Logger LOG = LoggerFactory.getLogger(MfgRateQRegistForm.class);

    /** MFG_RATE_Q登録フォームのリスト */
    @Valid
    private List<MfgRateQRegistForm> mfgRateQGrid;

    /**
     * @return MFG_RATE_Q登録フォームのリスト
     */
    public List<MfgRateQRegistForm> getMfgRateQGrid() {
        return mfgRateQGrid;
    }

    /**
     * @param p MFG_RATE_Q登録フォームのリスト
     */
    public void setMfgRateQGrid(final List<MfgRateQRegistForm> p) {
        this.mfgRateQGrid = p;
    }

    /** 関連チェック */
    @Override
    public void validate(final Map<String, String> errors, final BaseProcess baseProcess) {
        LOG.debug("not overridden in subclasses.");
    }

}