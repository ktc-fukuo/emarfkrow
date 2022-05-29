package jp.co.kyototool.knps.form.model.base;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import jp.co.golorp.emarf.process.BaseProcess;
import jp.co.golorp.emarf.validation.IForm;

/**
 * MFG_RATE一覧登録フォーム
 *
 * @author emarfkrow
 */
public class MfgRateSRegistForm implements IForm {

    /** logger */
    private static final Logger LOG = LoggerFactory.getLogger(MfgRateRegistForm.class);

    /** MFG_RATE登録フォームのリスト */
    @Valid
    private List<MfgRateRegistForm> mfgRateGrid;

    /**
     * @return MFG_RATE登録フォームのリスト
     */
    public List<MfgRateRegistForm> getMfgRateGrid() {
        return mfgRateGrid;
    }

    /**
     * @param p MFG_RATE登録フォームのリスト
     */
    public void setMfgRateGrid(final List<MfgRateRegistForm> p) {
        this.mfgRateGrid = p;
    }

    /** 関連チェック */
    @Override
    public void validate(final Map<String, String> errors, final BaseProcess baseProcess) {
        LOG.debug("not overridden in subclasses.");
    }

}