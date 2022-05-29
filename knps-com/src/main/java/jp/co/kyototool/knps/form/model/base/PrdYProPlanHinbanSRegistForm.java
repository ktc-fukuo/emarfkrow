package jp.co.kyototool.knps.form.model.base;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import jp.co.golorp.emarf.process.BaseProcess;
import jp.co.golorp.emarf.validation.IForm;

/**
 * PRD_Y_PRO_PLAN_HINBAN一覧登録フォーム
 *
 * @author emarfkrow
 */
public class PrdYProPlanHinbanSRegistForm implements IForm {

    /** logger */
    private static final Logger LOG = LoggerFactory.getLogger(PrdYProPlanHinbanRegistForm.class);

    /** PRD_Y_PRO_PLAN_HINBAN登録フォームのリスト */
    @Valid
    private List<PrdYProPlanHinbanRegistForm> prdYProPlanHinbanGrid;

    /**
     * @return PRD_Y_PRO_PLAN_HINBAN登録フォームのリスト
     */
    public List<PrdYProPlanHinbanRegistForm> getPrdYProPlanHinbanGrid() {
        return prdYProPlanHinbanGrid;
    }

    /**
     * @param p PRD_Y_PRO_PLAN_HINBAN登録フォームのリスト
     */
    public void setPrdYProPlanHinbanGrid(final List<PrdYProPlanHinbanRegistForm> p) {
        this.prdYProPlanHinbanGrid = p;
    }

    /** 関連チェック */
    @Override
    public void validate(final Map<String, String> errors, final BaseProcess baseProcess) {
        LOG.debug("not overridden in subclasses.");
    }

}