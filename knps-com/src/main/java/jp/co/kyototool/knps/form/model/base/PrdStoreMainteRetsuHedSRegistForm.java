package jp.co.kyototool.knps.form.model.base;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import jp.co.golorp.emarf.process.BaseProcess;
import jp.co.golorp.emarf.validation.IForm;

/**
 * PRD_STORE_MAINTE_RETSU_HED一覧登録フォーム
 *
 * @author emarfkrow
 */
public class PrdStoreMainteRetsuHedSRegistForm implements IForm {

    /** logger */
    private static final Logger LOG = LoggerFactory.getLogger(PrdStoreMainteRetsuHedRegistForm.class);

    /** PRD_STORE_MAINTE_RETSU_HED登録フォームのリスト */
    @Valid
    private List<PrdStoreMainteRetsuHedRegistForm> prdStoreMainteRetsuHedGrid;

    /**
     * @return PRD_STORE_MAINTE_RETSU_HED登録フォームのリスト
     */
    public List<PrdStoreMainteRetsuHedRegistForm> getPrdStoreMainteRetsuHedGrid() {
        return prdStoreMainteRetsuHedGrid;
    }

    /**
     * @param p PRD_STORE_MAINTE_RETSU_HED登録フォームのリスト
     */
    public void setPrdStoreMainteRetsuHedGrid(final List<PrdStoreMainteRetsuHedRegistForm> p) {
        this.prdStoreMainteRetsuHedGrid = p;
    }

    /** 関連チェック */
    @Override
    public void validate(final Map<String, String> errors, final BaseProcess baseProcess) {
        LOG.debug("not overridden in subclasses.");
    }

}