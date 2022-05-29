package jp.co.kyototool.knps.form.model.base;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import jp.co.golorp.emarf.process.BaseProcess;
import jp.co.golorp.emarf.validation.IForm;

/**
 * MST_WC_BK一覧登録フォーム
 *
 * @author emarfkrow
 */
public class MstWcBkSRegistForm implements IForm {

    /** logger */
    private static final Logger LOG = LoggerFactory.getLogger(MstWcBkRegistForm.class);

    /** MST_WC_BK登録フォームのリスト */
    @Valid
    private List<MstWcBkRegistForm> mstWcBkGrid;

    /**
     * @return MST_WC_BK登録フォームのリスト
     */
    public List<MstWcBkRegistForm> getMstWcBkGrid() {
        return mstWcBkGrid;
    }

    /**
     * @param p MST_WC_BK登録フォームのリスト
     */
    public void setMstWcBkGrid(final List<MstWcBkRegistForm> p) {
        this.mstWcBkGrid = p;
    }

    /** 関連チェック */
    @Override
    public void validate(final Map<String, String> errors, final BaseProcess baseProcess) {
        LOG.debug("not overridden in subclasses.");
    }

}